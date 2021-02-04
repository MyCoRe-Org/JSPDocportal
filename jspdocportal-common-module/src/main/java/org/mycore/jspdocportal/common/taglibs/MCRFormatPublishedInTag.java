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
package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.mycore.jspdocportal.common.taglibs.docdetails.helper.MCRPublishedInFormatter;
import org.w3c.dom.NodeList;

/**
 * This tag formats publishedIn information like the one used in datamodel document
 * 
 * @author Robert Stephan
 * @version $Revision: 17081 $ $Date: 2010-03-20 18:37:27 +0100 (Sa, 20 Mrz 2010) $
 */
public class MCRFormatPublishedInTag extends SimpleTagSupport {
    private NodeList xml;

    /**
     * @param xml the xml to set
     */
    public void setXml(NodeList xml) {
        this.xml = xml;
    }

    public void doTag() throws JspException, IOException {
        if (xml != null) {

        }
        if (xml.item(0) instanceof org.w3c.dom.Element) {

            DOMBuilder domBuilder = new DOMBuilder();
            Element el = domBuilder.build((org.w3c.dom.Element) xml.item(0));
            getJspContext().getOut().append(MCRPublishedInFormatter.format(el));
        }
    }
}