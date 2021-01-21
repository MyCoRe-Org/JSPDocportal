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
package org.mycore.frontend.jsp.taglibs.docdetails;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.taglibs.standard.tag.common.xml.XPathUtil;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * displays a MCR classification object in docdetails
 * 
 * @author Robert Stephan
 *
 */
public class MCRDocDetailsClassificationItemTag extends SimpleTagSupport {
    private static MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.getInstance();
    private static Logger LOGGER = LogManager.getLogger(MCRDocDetailsClassificationItemTag.class);

    private String xp;
    private String css = "";

    public void doTag() throws JspException, IOException {
        MCRDocDetailsRowTag docdetailsRow = (MCRDocDetailsRowTag) findAncestorWithClass(this,
                MCRDocDetailsRowTag.class);
        if (docdetailsRow == null) {
            throw new JspException("This tag must be nested in tag called 'row' of the same tag library");
        }
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        Element result = null;
        try {
            XPathUtil xu = new XPathUtil((PageContext) getJspContext());
            @SuppressWarnings("rawtypes")
            List nodes = xu.selectNodes(docdetailsRow.getContext(), xp);

            if (nodes.size() > 0) {
                Node n = (Node) nodes.get(0);
                if (n instanceof Element) {
                    result = (Element) n;
                    if (result.hasAttribute("classid") && result.hasAttribute("categid")) {
                        String lang = docdetails.getLang();
                        String classid = result.getAttribute("classid");
                        String categid = result.getAttribute("categid");
                        String text = "";
                        try {
                            text = categoryDAO.getCategory(new MCRCategoryID(classid, categid), 0).getLabel(lang).get()
                                    .getText();
                        } catch (NullPointerException npe) {
                            text = "No classification entry found for id: " + categid + " in classfication: " + classid
                                    + "!";
                        }

                        if (css != null && !"".equals(css)) {
                            getJspContext().getOut().write("<td class=\"" + css + "\">");
                        } else {
                            getJspContext().getOut()
                                    .write("<td class=\"" + docdetails.getStylePrimaryName() + "-value\">");
                        }
                        getJspContext().getOut().write(text);
                        getJspContext().getOut().write("</td>");

                        return;
                    }
                }
            }
            //error
            throw new JspException("The XPath expression must match a classification element");
        } catch (Exception e) {
            LOGGER.error("Error processing docdetails:classificationitem tag", e);
        }
    }

    /**
     * the XPath expression to retrieve the object
     * @param xpath
     */
    public void setSelect(String xpath) {
        this.xp = xpath;
    }

    /**
     * the CSS class name, which shall be used
     * @param style
     */
    public void setStyleName(String style) {
        this.css = style;
    }
}
