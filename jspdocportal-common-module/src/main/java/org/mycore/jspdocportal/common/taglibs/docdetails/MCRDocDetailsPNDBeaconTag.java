/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.mycore.jspdocportal.common.taglibs.docdetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.jspdocportal.common.taglibs.docdetails.helper.UBRBibliographie;

/**
 * resolves links from PND Beacon Resolver
 * (http://beacon.findbuch.de/seealso/pnd-aks)
 * 
 * @author Robert Stephan
 * 
 */
public class MCRDocDetailsPNDBeaconTag extends SimpleTagSupport {
    private static String UBR_BIBLIOGRAPHY_KEY = "ubr_biblgr";
    private static Logger LOGGER = LogManager.getLogger(MCRDocDetailsPNDBeaconTag.class);
    private static Namespace NS_XHTML = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
    private static XPathExpression<Element> xpeH2 = XPathFactory.instance().compile("//xhtml:h2", Filters.element(),
            null, NS_XHTML);

    private String pnd = "";
    private List<String> whitelist = new ArrayList<String>();
    private List<String> blacklist = new ArrayList<String>();
    private Map<String, String> replaceLabels = new HashMap<String, String>();

    /**
     * the PND number
     * 
     * @param pnd
     */
    public void setPnd(String pnd) {
        this.pnd = pnd;
    }

    /**
     * define a white list of sources that should be displayed
     * 
     * @param wl
     *            a | separated list of shortcuts
     */
    public void setWhitelist(String wl) {
        for (String s : wl.split("\\|")) {
            if (s.trim().length() > 0) {
                whitelist.add(s.trim());
            }
        }
    }

    /**
     * hide certain sources from view
     * 
     * @param bl
     *            a | separated list of shortcuts
     */
    public void setBlacklist(String bl) {
        for (String s : bl.split("\\|")) {
            if (s.trim().length() > 0) {
                blacklist.add(s.trim());
            }
        }
    }

    /**
     * define new labels, that should be shown instead of the provided ones e.g.
     * &quot;ubr_biblgr:Universitätsbibliographie Rostock|gvk:Verbundkatalog des
     * GBV|dta:Deutsches Textarchiv&quot;
     * 
     * @param replaceLabels
     *            - a | separated list of items, items consist of shortcut and
     *            label, separated by :
     */
    public void setReplaceLabels(String replaceLabels) {
        for (String s : replaceLabels.split("\\|")) {
            if (s.contains(":")) {
                s = s.trim();
                this.replaceLabels.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1));
            }
        }
    }

    private String createHTML() {
        StringBuffer result = new StringBuffer();
        try {
            StringBuffer sbWhite = new StringBuffer();
            for (String s : whitelist) {
                sbWhite.append(";").append(s);
            }
            if (sbWhite.length() == 0) {
                sbWhite.append(";");
            }
            URL u = new URL("http://beacon.findbuch.de/seealso/pnd-aks/?format=sources&id=" + pnd);

            URLConnection uc = u.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
            String beaconString = "";
            for (String line; (line = br.readLine()) != null;) {
                beaconString = beaconString + line;
            }
            br.close();

            beaconString = beaconString.replace(
                    "<!DOCTYPE html\tPUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\t \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
                    "");
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(beaconString));

            List<String> ids = new ArrayList<String>();
            Map<String, String> urls = new HashMap<String, String>();
            Map<String, String> labels = new HashMap<String, String>();

            UBRBibliographie biblioApp;

            if (whitelist.isEmpty()
                    || whitelist.contains(UBR_BIBLIOGRAPHY_KEY) && !blacklist.contains(UBR_BIBLIOGRAPHY_KEY)) {
                biblioApp = UBRBibliographie.getInstance();
                if (biblioApp.getHitCount(pnd) > 0) {
                    ids.add(UBR_BIBLIOGRAPHY_KEY);
                    urls.put(UBR_BIBLIOGRAPHY_KEY, biblioApp.getURL(pnd));
                    if (replaceLabels.containsKey(UBR_BIBLIOGRAPHY_KEY)) {
                        labels.put(UBR_BIBLIOGRAPHY_KEY, replaceLabels.get(UBR_BIBLIOGRAPHY_KEY));
                    } else {
                        labels.put(UBR_BIBLIOGRAPHY_KEY, biblioApp.getMessage(pnd));
                    }
                }
            }

            for (Element e : xpeH2.evaluate(doc)) {
                String id = e.getAttributeValue("id").substring(4);

                if ((whitelist.isEmpty() || whitelist.contains(id)) && !blacklist.contains(id)) {
                    ids.add(id);
                    urls.put(id, e.getChild("a", NS_XHTML).getAttributeValue("href").replace("&", "&amp;"));
                    if (replaceLabels.containsKey(id)) {
                        labels.put(id, replaceLabels.get(id));
                    } else {
                        labels.put(id, e.getChildTextTrim("a", NS_XHTML));
                    }
                }
            }

            if (!whitelist.isEmpty()) {
                List<String> orderedIds = new ArrayList<String>();
                for (String s : whitelist) {
                    if (s.equals(UBR_BIBLIOGRAPHY_KEY)) {
                        biblioApp = UBRBibliographie.getInstance();
                        if (biblioApp.getHitCount(pnd) > 0) {
                            urls.put(UBR_BIBLIOGRAPHY_KEY, biblioApp.getURL(pnd));
                            if (replaceLabels.containsKey(UBR_BIBLIOGRAPHY_KEY)) {
                                labels.put(UBR_BIBLIOGRAPHY_KEY, replaceLabels.get(UBR_BIBLIOGRAPHY_KEY));
                            } else {
                                labels.put(UBR_BIBLIOGRAPHY_KEY, biblioApp.getMessage(pnd));
                            }
                            orderedIds.add(s);
                        }
                        continue;
                    }
                    if (ids.contains(s)) {
                        orderedIds.add(s);
                    }
                }
                ids = orderedIds;
            }
            result.append("\n<!--" + u.toString() + "-->");
            if (ids.size() > 0) {
                result.append("\n<ul class=\"pndbeaconlist\">");
                for (String s : ids) {
                    result.append("\n   <li><!--" + s + "--><a target=\"_blank\" href=\"" + urls.get(s) + "\">"
                            + labels.get(s) + "</a></li>");
                }
                result.append("\n</ul>");
            }
        } catch (Exception e) {
            LOGGER.error("Exception in MCRDocDetailsPNDBeaconTag", e);
        }

        return result.toString();
    }

    public void doTag() throws JspException, IOException {
        MCRDocDetailsRowTag docdetailsRow = (MCRDocDetailsRowTag) findAncestorWithClass(this,
                MCRDocDetailsRowTag.class);
        if (docdetailsRow == null) {
            throw new JspException("This tag must be nested in tag called 'row' of the same tag library");
        }
        getJspContext().getOut().print(createHTML());
    }

    /**
     * example and test code
     */
    public static void main(String[] args) {
        MCRDocDetailsPNDBeaconTag tag = new MCRDocDetailsPNDBeaconTag();
        tag.setPnd("118558838");
        tag.setPnd("121269450");
        String whitelist = "mat_hr" + "|adbreg" + "|cpl" + "|dewp" + "|lagis" + "|saebi" + "|bbkl" + "|rag" + "|gesa"
                + "|commons" + "|gvk" + "|neerland" + "|ubr_biblgr";

        String labels = "mat_hr:Rostocker Matrikelportal 1419-1945" + "|adbreg:Deutsche Biographie (ADB/NDB)"
                + "|cpl:Leipziger Professorenkatalog" + "|dewp:Wikipedia (DE) Personenartikel"
                + "|lagis:Hessische Biographie" + "|mat_hr:Matrikel Rostock"
                + "|bbkl:Biographisch-Bibliographisches Kirchenlexikon (BBKL)"
                + "|bmlo:Bayerisches Musiker-Lexikon Online (BMLO)" + "|rag:Repertorium Academicum Germanicum (RAG)"
                + "|gesa:Gesamtkatalog deutschsprachiger Leichenpredigten (GESA)" + "|commons:Wikimedia Commons"
                + "|lwl:Portal &qout;Westfälische Geschichte&qout;" + "|vr:Portal &qout;Rheinische Geschichte&qout;"
                + "|rppd:Rheinland-Pfälzische Personendatenbank" + "|hls:Historisches Lexikon der Schweiz (HLS)"
                + "|ps_usbk:Portraitsammlung USB Köln"
                + "|odb:Ostdeutsche Biographie (Kulturstiftung der deutschen Vertriebenen)"
                + "|sozkla:50 Klassiker der Soziologie"
                + "|sandrart:Eintrag in der Online-Edition von Sandrarts &qout;Teutscher Academie&qout;"
                + "|reichka:Akten der Reichskanzlei. Weimarer Republik"
                + "|historicum:Klassiker der Geschichtswissenschaft" + "|gvk:Verbundkatalog des GBV"
                + "|ubr_biblgr:Rostocker Universitätsbibliographie"
                + "|neerland:Bio- en bibliografisch lexicon van de neerlandistiek";

        tag.setWhitelist(whitelist);
        tag.setBlacklist("bvb|bsb");
        tag.setReplaceLabels(labels);
        System.out.println(tag.createHTML());
    }
}