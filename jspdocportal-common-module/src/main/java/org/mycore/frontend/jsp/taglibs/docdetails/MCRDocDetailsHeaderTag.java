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
 * displays a header in docdetails table
 * @author Robert Stephan
 *
 */
public class MCRDocDetailsHeaderTag extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        if (docdetails == null) {
            throw new JspException("This tag must be nested in tag called 'docdetails' of the same tag library");
        }
        if (docdetails.getOutputStyle().equals("table")) {
            out.write("<tr><th class=\"" + docdetails.getStylePrimaryName() + "-header\" colspan=\"4\">\n");
            getJspBody().invoke(out);
            out.write("</th></tr>");
        }
        if (docdetails.getOutputStyle().equals("headlines")) {
            out.write("<div class=\"" + docdetails.getStylePrimaryName() + "-header\">\n");
            getJspBody().invoke(out);
            out.write("</div>");
        }
    }
}
