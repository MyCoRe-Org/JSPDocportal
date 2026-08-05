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
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRDOMContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.content.transformer.MCRXSLTransformer;
<<<<<<< main
import org.mycore.datamodel.metadata.MCRMetadataManager;
=======
import org.mycore.common.xsl.MCRTemplatesSource;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
>>>>>>> 3798f27 feat: workspace object header hook (#152)
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.xsl.MCRDirectTemplatesSourceTransformer;
import org.mycore.jspdocportal.common.xsl.MCRVirtualStylesheetUtils;
import org.mycore.jspdocportal.common.xsl.MCRVirtualTemplatesSource;
import org.w3c.dom.Document;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * JSP tag that transforms a DOM/JDOM document or an MCRObject (by ID) using an XSL
 * stylesheet, either given directly via 'xslt' or composed virtually from XSL imports
 * via 'xslImports'.
 *  
 * @author Robert Stephan
 * 
 */
/* Example:
 * <mcr:retrieveObject mcrid="${mcrid}" varDOM="doc" />
 * <mcr:transformXSL dom="${doc}" xslt="xsl/xsl3example.xsl" />
 * <mcr:transformXSL dom="${doc}" xslImports="docdetails-metadata" />
 */
public class MCRTransformXslTag extends SimpleTagSupport {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Class<? extends TransformerFactory> SAXON_TRANSFORMER_FACTORY_CLASS;

    static {
        try {
            SAXON_TRANSFORMER_FACTORY_CLASS = MCRClassTools.forName("net.sf.saxon.TransformerFactoryImpl");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Document dom;

    private org.jdom2.Document jdom;

    private String stylesheet;

    private String transformer;

    private String mcrid;

    private String xslImports;

    @Override
    public void doTag() throws JspException, IOException {
        try {
            if (xslImports != null && stylesheet != null) {
                throw new JspException("Attributes 'xslt' and 'xslImports' are mutually exclusive");
            }

<<<<<<< main
            MCRContentTransformer t = null;
            if (transformer != null) {
                t = MCRContentTransformerFactory.getTransformer(transformer);
            } else if (stylesheet != null) {
                Class<? extends TransformerFactory> tfClass =
                    MCRClassTools.forName(MCRConfiguration2.getStringOrThrow("SAXON"));
                t = MCRXSLTransformer.obtainInstance(tfClass, stylesheet);
            }
            if (t == null) {
                throw new MCRException("transformer or stylesheet attribute are not defined or invalid.");
=======
            MCRXSLTransformer t;
            if (xslImports != null) {
                String virtualStylesheet = MCRVirtualStylesheetUtils.createImportStylesheet(xslImports, "html");
                MCRTemplatesSource source = new MCRVirtualTemplatesSource(xslImports, virtualStylesheet);
                t = MCRDirectTemplatesSourceTransformer.obtainInstance(SAXON_TRANSFORMER_FACTORY_CLASS, source);
            } else {
                t = MCRXSLTransformer.obtainInstance(SAXON_TRANSFORMER_FACTORY_CLASS, stylesheet);
>>>>>>> 3798f27 feat: workspace object header hook (#152)
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mcrid != null) {
                MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
                org.jdom2.Document mcrJdom = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID).createXML();
                t.transform(new MCRJDOMContent(mcrJdom), baos);
            } else if (jdom != null) {
                t.transform(new MCRJDOMContent(jdom), baos);
            } else if (dom != null) {
                t.transform(new MCRDOMContent(dom), baos);
            }
            getJspContext().getOut().append(baos.toString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Error in XSLT-Processing ({}): {}", mcrid, stylesheet, e);
            throw new JspException("Error transforming XSL for mcrid=" + mcrid, e);
        }
    }

<<<<<<< main
    public void setTransformer(String transformer) {
        this.transformer = transformer;
=======
    public void setXslImports(String xslImports) {
        this.xslImports = xslImports;
    }

    public String getXslt() {
        return stylesheet;
>>>>>>> 3798f27 feat: workspace object header hook (#152)
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
