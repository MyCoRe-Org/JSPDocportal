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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.TransformerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRClassTools;
import org.mycore.common.content.MCRDOMContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.w3c.dom.Document;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * This class will add namespace declarations (prefix-uri pairs) to the XPathUtil class
 * which is used by the JSTL XML Tag Library to process XPath expressions.
 * This allows us to use any namespace prefix in XPath Expressions processed by this JSTL.
 * 
 * Uses the Java Reflection Framework to modify private fields
 *  
 * @author Robert Stephan
 * 
 */ 
/* Example:
 * <mcr:retrieveObject mcrid="${mcrid}" varDOM="doc" />
 * <mcr:transformXSL dom="${doc}" xslt="xsl/xsl3example.xsl" />
 */
public class MCRTransformXslTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRTransformXslTag.class);

    private Document dom;

    private org.jdom2.Document jdom;

    private String stylesheet;

    private String mcrid;

    public void doTag() throws JspException, IOException {
        try {
            // this works, if the default transformer is xslt3 (set by property):
            // MCR.LayoutService.TransformerFactoryClass=net.sf.saxon.TransformerFactoryImpl
            // MCRXSLTransformer t = MCRXSLTransformer.getInstance(stylesheet);

            Class<? extends TransformerFactory> tfClass = MCRClassTools.forName("net.sf.saxon.TransformerFactoryImpl");
            MCRXSLTransformer t = MCRXSLTransformer.getInstance(tfClass, stylesheet);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mcrid != null) {
                t.transform(MCRXMLMetadataManager.instance().retrieveContent(MCRObjectID.getInstance(mcrid)), baos);
                getJspContext().getOut().append(baos.toString(StandardCharsets.UTF_8));
                return;
            }
            if (jdom != null) {
                t.transform(new MCRJDOMContent(jdom), baos);
                getJspContext().getOut().append(baos.toString(StandardCharsets.UTF_8));
                return;
            }
            if (dom != null) {
                t.transform(new MCRDOMContent(dom), baos);
                getJspContext().getOut().append(baos.toString(StandardCharsets.UTF_8));
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Error in XSLT-Processing ("+mcrid+"): "+ stylesheet, e);
        }
    }

    public String getXslt() {
        return stylesheet;
    }

    public void setXslt(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }

    public void setDom(Document dom) {
        this.dom = dom;
    }

    public void setJdom(org.jdom2.Document jdom) {
        this.jdom = jdom;
    }
}
