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
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.taglibs.standard.tag.common.xml.XPathUtil;
import org.w3c.dom.Node;

/**
 * creates a docdetails item, in which "normal" JSP code can be used to create the output
 * 
 * @author Robert Stephan
 *
 */
public class MCRDocDetailsOutputItemTag extends SimpleTagSupport {
    private String xp;
    private String varxml;

    private Node xmlnode;
    private String css = null;

    public Node getXmlnode() {
        return xmlnode;
    }

    public void doTag() throws JspException, IOException {

        MCRDocDetailsRowTag docdetailsRow = (MCRDocDetailsRowTag) findAncestorWithClass(this,
                MCRDocDetailsRowTag.class);
        if (docdetailsRow == null) {
            throw new JspException("This tag must be nested in tag called 'row' of the same tag library");
        }
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        try {
            JspWriter out = getJspContext().getOut();
            XPathUtil xu = new XPathUtil((PageContext) getJspContext());
           
            @SuppressWarnings("rawtypes")
            List nodes = xu.selectNodes(docdetailsRow.getContext(), xp);
            if (nodes.size() > 0) {
                Node n = (Node) nodes.get(0);
                xmlnode = n;
                getJspContext().setAttribute(varxml, n);

                if (css != null && !"".equals(css)) {
                    out.write("<td class=\"" + css + "\">");
                } else {
                    out.write("<td class=\"" + docdetails.getStylePrimaryName() + "-value\">");
                }
                getJspBody().invoke(out);
                out.write("</td>");
            }
            //error
        } catch (Exception e) {
            throw new JspException("Error executing docdetails:outputitem tag", e);
        }
    }

    /**
     * the XPath expression to the element
     * @param xpath
     */
    public void setSelect(String xpath) {
        this.xp = xpath;
    }

    /**
     * the variable in which the result node should be saved
     * @param var
     */
    public void setVar(String varxml) {
        this.varxml = varxml;
    }

    /**
     * the CSS class name, which shall be used
     * @param style
     */
    public void setStyleName(String style) {
        this.css = style;
    }
}
