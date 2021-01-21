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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Shows a seperator, which can be some space or a line
 * A 2nd seperator will be ignored, if there is no other content 
 * to display between the two seperators.
 *
 * @author Robert STephan
 *
 */
public class MCRDocDetailsSeparatorTag extends SimpleTagSupport {
    private boolean showLine;

    /**
     * if set to true a line will be displayed
     * @param showLine
     */
    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }

    public void doTag() throws JspException, IOException {
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        if (docdetails == null) {
            throw new JspException("This tag must be nested in tag called 'docdetails' of the same tag library");
        }
        if (docdetails.getPreviousOutput() > 0) {
            JspWriter out = getJspContext().getOut();
            if (docdetails.getOutputStyle().equals("table")) {
                out.print("<tr><td colspan=\"3\" class=\"" + docdetails.getStylePrimaryName() + "-separator\">");
                if (showLine) {
                    out.print("<hr />");
                } else {
                    out.print("&#160;");
                }
                out.println("</td></tr>");
            }
            if (docdetails.getOutputStyle().equals("headlines")) {
                out.print("<div class=\"" + docdetails.getStylePrimaryName() + "-separator\">");
                if (showLine) {
                    out.print("<hr />");
                } else {
                    out.print("&#160;");
                }
                out.println("</div>");
            }
        }
        docdetails.setPreviousOutput(0);
    }
}
