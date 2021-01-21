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
package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.common.xsl.MCRTemplatesSource;
import org.mycore.common.xsl.MCRXSLTransformerFactory;
import org.w3c.dom.Node;

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
public class MCRTransformXslTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRTransformXslTag.class);

    private Node node;

    private String stylesheet;

    public void doTag() throws JspException, IOException {
        try {
            MCRTemplatesSource source = new MCRTemplatesSource(stylesheet);
            Transformer t = MCRXSLTransformerFactory.getTransformer(source);
            Map<String, Object> params = MCRParameterCollector.getInstanceFromUserSession().getParameterMap();
            for (String k : params.keySet()) {
                t.setParameter(k, params.get(k));
            }
            Source input = new DOMSource(node);
            StringWriter sw = new StringWriter();
            Result output = new StreamResult(sw);
            t.transform(input, output);
            getJspContext().getOut().append(sw.toString());
        } catch (Exception e) {
            LOGGER.error("Something went wrong processing the XSLT: " + stylesheet, e);
        }
    }

    public Node getXml() {
        return node;
    }

    public void setXml(Node node) {
        this.node = node;
    }

    public String getXslt() {
        return stylesheet;
    }

    public void setXslt(String stylesheet) {
        this.stylesheet = stylesheet;
    }
}
