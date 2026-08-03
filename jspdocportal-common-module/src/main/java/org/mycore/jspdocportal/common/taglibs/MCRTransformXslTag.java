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
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRClassTools;
import org.mycore.common.content.MCRDOMContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRTemplatesSource;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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

    private String mcrid;

    private String xslImports;

    @Override
    public void doTag() throws JspException, IOException {
        try {
            if (xslImports != null && stylesheet != null) {
                throw new JspException("Attributes 'xslt' and 'xslImports' are mutually exclusive");
            }

            MCRXSLTransformer t;
            if (xslImports != null) {
                String virtualStylesheet = createVirtualStylesheet(xslImports);
                MCRTemplatesSource source = new MCRVirtualTemplatesSource(xslImports, virtualStylesheet);
                t = new MCRTemplatesSourceXSLTransformer(SAXON_TRANSFORMER_FACTORY_CLASS, source);
            } else {
                t = MCRXSLTransformer.obtainInstance(SAXON_TRANSFORMER_FACTORY_CLASS, stylesheet);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mcrid != null) {
                t.transform(MCRXMLMetadataManager.getInstance().retrieveContent(MCRObjectID.getInstance(mcrid)), baos);
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
            }
        } catch (Exception e) {
            LOGGER.error("Error in XSLT-Processing ({}): {}", mcrid, stylesheet, e);
        }
    }

    public void setXslImports(String xslImports) {
        this.xslImports = xslImports;
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

    public static String createVirtualStylesheet(String importName) {
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <xsl:stylesheet version="3.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

            <xsl:import href="xslImport:%s"/>
            <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8" />

        </xsl:stylesheet>
        """.formatted(importName);
    }

    static class MCRTemplatesSourceXSLTransformer extends MCRXSLTransformer {

        public MCRTemplatesSourceXSLTransformer(Class<? extends TransformerFactory> factoryClass,
            MCRTemplatesSource... templateSources) {
            super(factoryClass);
            this.templateSources = templateSources;
            this.modified = new long[this.templateSources.length];
            this.modifiedChecked = 0L;
            this.templates = new Templates[this.templateSources.length];
        }
    }

    static class MCRVirtualTemplatesSource extends MCRTemplatesSource {

        private final String name;
        private final String content;
        private final Long created;

        public MCRVirtualTemplatesSource(String name, String content) {
            super("virtual_" + name);
            this.name = name;
            this.content = content;
            this.created = System.currentTimeMillis();
        }

        @Override
        public SAXSource getSource() throws SAXException, ParserConfigurationException {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XMLReader reader = factory.newSAXParser().getXMLReader();
            InputSource inputSource = new InputSource(new StringReader(content));
            inputSource.setSystemId("file:/virtual/" + name + ".xsl");

            return new SAXSource(reader, inputSource);
        }

        /**
         * Always returns {@code null}, since this template source is virtual and backed by
         * in-memory content rather than a real resource with a resolvable URL.
         */
        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public long getLastModified() {
            return created;
        }

        @Override
        public MCRCache.ModifiedHandle getModifiedHandle(long checkPeriod) {
            return new MCRCache.ModifiedHandle() {
                @Override
                public long getLastModified() {
                    return created;
                }

                @Override
                public long getCheckPeriod() {
                    return checkPeriod;
                }
            };
        }
    }
}
