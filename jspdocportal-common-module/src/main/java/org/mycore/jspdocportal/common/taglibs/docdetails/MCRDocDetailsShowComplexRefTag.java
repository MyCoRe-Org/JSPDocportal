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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * resolves links from PND Beacon Resolver (http://beacon.findbuch.de/seealso/pnd-aks)
 * 
 * @author Robert Stephan
 *
 */
public class MCRDocDetailsShowComplexRefTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRDocDetailsLinkItemTag.class);
    private String content = "";

    /**
     * the content to be rendered
     * @param pnd
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * <register>
     *  	<werk titel="Etwas von geleehrten Rostockschen Sachen">
     *  		<band titel="Band I" docid="rosdok_document_123456567" seiten="1 2 333 454" /> 
     *  		<band titel="Band III" docid="rosdok_document_123456567" seiten="12 22 333 454" />
     *      </werk>					   		
     *  </register>
     * 
     */

    private String createHTML() {
        StringBuffer result = new StringBuffer();
        SAXBuilder sb = new SAXBuilder();
        try {
            Document doc = sb.build(new StringReader(StringEscapeUtils.unescapeXml(content)));
            List<Element> lstWerk = (List<Element>) doc.getRootElement().getChildren("werk");
            for (int i = 0; i < lstWerk.size(); i++) {
                Element eWerk = lstWerk.get(i);
                result.append(eWerk.getAttributeValue("titel"));
                result.append(", ");
                List<Element> lstBand = (List<Element>) eWerk.getChildren("band");
                for (int j = 0; j < lstBand.size(); j++) {
                    Element eBand = lstBand.get(j);
                    result.append(eBand.getAttributeValue("titel"));
                    result.append(", ");
                    String baseURL = "http://rosdok.uni-rostock.de/resolve/id/" + eBand.getAttributeValue("docid")
                            + "/image/page/";
                    String[] pages = eBand.getAttributeValue("seiten").split("\\s");
                    for (int k = 0; k < pages.length; k++) {
                        String pageLabel = pages[k];
                        String pageNr = pages[k].split("-")[0];
                        result.append(
                                "<a href=\"" + baseURL + pageNr + "\" target=\"_blank\">S. " + pageLabel + "</a>");
                        if (k < pages.length - 1) {
                            result.append(", ");
                        }
                    }
                    if (j < lstBand.size() - 1) {
                        result.append("; ");
                    }
                }
                result.append(".");
                if (i < lstWerk.size() - 1) {
                    result.append("<br />");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing Register Data", e);
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
}