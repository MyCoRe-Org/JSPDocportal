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
 * 
 */
package org.mycore.jspdocportal.ir.pdfdownload.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration2;

public class PDFTOCUtil {
    private static Namespace NS_METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

    private static Namespace NS_XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    private static XPathExpression<Element> xpStructMapPhysical, xpStructLink, xpRootDivLogical;

    static {
        xpStructMapPhysical = XPathFactory.instance().compile("//mets:structMap[@TYPE='PHYSICAL']//mets:div",
            Filters.element(), null, NS_METS);
        xpStructLink = XPathFactory.instance().compile("//mets:structLink/mets:smLink", Filters.element(), null,
            NS_METS);
        xpRootDivLogical = XPathFactory.instance().compile("//mets:structMap[@TYPE='LOGICAL']/mets:div",
            Filters.element(), null, NS_METS);
    }

    public static ArrayList<HashMap<String, Object>> createTOC(Path dataDir, int offset) {
        ArrayList<HashMap<String, Object>> outlines = new ArrayList<HashMap<String, Object>>();
        Path metsFile = dataDir.resolve(
            MCRConfiguration2.getString("MCR.SWF.Project.ID").get() + "_" + dataDir.getFileName() + ".repos.mets.xml");
        if (Files.exists(metsFile)) {
            SAXBuilder sb = new SAXBuilder();
            try {
                Document metsDoc = sb.build(metsFile.toFile());
                outlines = createTOC(metsDoc, offset);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outlines;
        } else {
            return new ArrayList<HashMap<String, Object>>();
        }
    }

    public static ArrayList<HashMap<String, Object>> createTOC(Document metsDoc, int offset) {
        ArrayList<HashMap<String, Object>> outlines = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Integer> physStructMap = new HashMap<String, Integer>();
        for (Element e : xpStructMapPhysical.evaluate(metsDoc)) {
            String id = e.getAttributeValue("ID");
            String order = e.getAttributeValue("ORDER");
            if (id != null && order != null) {
                int page = 1;
                try {
                    page = Integer.parseInt(order) + offset;
                } catch (NumberFormatException nfe) {
                    //do nothing, use default
                }
                physStructMap.put(id, page);
            }
        }
        HashMap<String, Integer> logDiv2PageMap = new HashMap<String, Integer>();
        for (Element e : xpStructLink.evaluate(metsDoc)) {
            String from = e.getAttributeValue("from", NS_XLINK);
            String to = e.getAttributeValue("to", NS_XLINK);
            //put only the first occurrence of a logical div id into the map
            if (!logDiv2PageMap.containsKey(from)) {
                logDiv2PageMap.put(from, physStructMap.get(to));
            }
        }

        for (Element e : xpRootDivLogical.evaluate(metsDoc)) {
            addTocEntry(outlines, e, logDiv2PageMap);
        }

        return outlines;
    }

    private static void addTocEntry(ArrayList<HashMap<String, Object>> parent, Element logElem,
        HashMap<String, Integer> logDiv2PageMap) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        parent.add(data);

        String title = logElem.getAttributeValue("LABEL");
        if (title == null) {
            title = logElem.getAttributeValue("TYPE");
            if (title == null) {
                title = "[...]";
            } else {
                title = "[" + title + "]";
            }
        }
        data.put("Action", "GoTo");
        data.put("Title", title);
        data.put("Page", String.format(Locale.ENGLISH, "%d Fit", logDiv2PageMap.get(logElem.getAttributeValue("ID"))));
        List<Element> children = logElem.getChildren("div", NS_METS);
        if (!children.isEmpty()) {
            ArrayList<HashMap<String, Object>> kids = new ArrayList<HashMap<String, Object>>();
            data.put("Kids", kids);
            for (Element e : children) {
                addTocEntry(kids, e, logDiv2PageMap);
            }
        }
    }
}
