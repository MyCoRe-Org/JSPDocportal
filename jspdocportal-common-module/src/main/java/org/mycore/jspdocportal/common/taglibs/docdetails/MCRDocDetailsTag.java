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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.services.i18n.MCRTranslation;
import org.w3c.dom.Document;

/**
 * part of the MCRDocdetails Tag Library
 * 
 * provides the outer tag, which retrieves the document from database or
 * workflow
 * 
 * If a request parameter debug=true is found, the xml syntax of the 
 * MCR object will be displayed on the website
 * 
 * @author Robert Stephan
 * 
 * @version $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan
 *          2010) $
 */
public class MCRDocDetailsTag extends SimpleTagSupport {
    //allowed values are "table" or "headlines"
    private String outputStyle = "table";
    private String mcrID;
    private boolean fromWorkflow = false;
    private String lang = "de";
    private String var;
    private String stylePrimaryName = "docdetails";

    private Document doc;
    private int previousOutput;
    private ResourceBundle messages;

    /**
     * sets the MCR Object ID (mandatory)
     * 
     * @param mcrID
     *            the MCR object ID
     * 
     */
    public void setMcrID(String mcrID) {
        this.mcrID = mcrID;
    }

    /**
     * if set to true, the MCR object is retrieved from workflow directory and
     * not from database (mandatory)
     * 
     * @param mcrID
     *            the MCR object ID
     * 
     */
    public void setFromWorkflow(boolean fromWorkflow) {
        this.fromWorkflow = fromWorkflow;
    }

    /**
     * sets the ISO 639 2-letter Language Codes (Obsolete) (defaults to "de")
     * 
     * used when retrieving labels and messages from ressource bundle
     * 
     * @param lang -
     *            the language as 2-letter ISO 639 Code
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * sets the name of the variable in which to store the XML Document as
     * org.w3c.dom.Node for further use in processing the JSP 
     * (stored in request scope)
     * 
     * @param var -
     *            the name of the variable
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * sets the prefix for CSS-styles names to be used
     * (defaults to "docdetails")
     * 
     * @param stylePrimaryName -
     *            the CSS style name prefix
     */
    public void setStylePrimaryName(String stylePrimaryName) {
        this.stylePrimaryName = stylePrimaryName;
    }

    /**
     * executes the tag
     */
    public void doTag() throws JspException, IOException {
        byte[] data = new byte[0];
        try {
            messages = MCRTranslation.getResourceBundle("messages", new Locale(lang));
            MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
            org.jdom2.Document xml = null;
            if (fromWorkflow) {
                xml = MCRBPMNUtils.getWorkflowObjectXML(mcrObjID);
            } else {
                xml = MCRMetadataManager.retrieve(mcrObjID).createXML();
            }
            DOMOutputter domOut = new DOMOutputter();
            doc = domOut.output(xml);
            if (var != null) {
                getJspContext().setAttribute(var, doc, PageContext.REQUEST_SCOPE);
            }

        } catch (JDOMException e) {
            throw new JspException(e);
        }
        JspWriter out = getJspContext().getOut();
        PageContext pageContext = (PageContext) getJspContext();

        // DEBUG mode: output xml data as text
        if (pageContext.getRequest().getParameter("debug") != null
                && pageContext.getRequest().getParameter("debug").equals("true")) {
            SAXBuilder sb = new SAXBuilder();
            try {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                org.jdom2.Document jdom = sb.build(new ByteArrayInputStream(data));
                StringBuffer debugSB = new StringBuffer("<textarea cols=\"120\" rows=\"30\">").append("MCRObject:\r\n")
                        //.append(JSPUtils.getPrettyString(jdom).replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;"))
                        .append(outputter.outputString(jdom).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""))
                        .append("</textarea>");
                out.println(debugSB.toString());
            } catch (JDOMException e) {
                // do nothing
            }
        }
        if (outputStyle.equals("table")) {
            out.write("<table class=\"" + getStylePrimaryName() + "-table\" cellpadding=\"0\" cellspacing=\"0\">\n");
            out.write("<tbody>\n");

            getJspBody().invoke(out);

            out.write("</tbody>\n");
            out.write("</table>\n");
        }
        if (outputStyle.equals("headlines")) {
            out.write("<div class=\"" + getStylePrimaryName() + "\">\n");
            getJspBody().invoke(out);
            out.write("</div>\n");
        }
    }

    /**
     * returns the context node.
     * for this tag it is always the document node
     * @return the document as org.w3c.dom.Node
     * 
     * @throws JspTagException
     */
    public org.w3c.dom.Node getContext() throws JspTagException {
        // expose the current node as the context
        return doc;
    }

    /**
     * returns the current language used in resource bundles
     * @return the language as 2-letter ISO 639 Code
     */
    public String getLang() {
        return lang;
    }

    /**
     * @return the prefix for CSS-style names
     */
    public String getStylePrimaryName() {
        return stylePrimaryName;
    }

    /**
     * sets the number of previous outputs
     * needed to calculate whether a separator line shall be displayed 
     * (avoids 2 separator lines next to each other due to missing content between them)
     * 
     * @param previousOutput the number of actually displayed rows
     */
    protected void setPreviousOutput(int previousOutput) {
        this.previousOutput = previousOutput;
    }

    /**
     * returns the number of previous outputs
     * needed to calculate whether a separator line shall be displayed 
     * (avoids 2 separator lines next to each other due to missing content between them)
     * 
     * @return the number of actually displayed rows
     */
    protected int getPreviousOutput() {
        return previousOutput;
    }

    /**
     * @return the resource bundle for messages, labels, etc.
     */
    protected ResourceBundle getMessages() {
        return messages;
    }

    public String getOutputStyle() {
        return outputStyle;
    }

    public void setOutputStyle(String outputStyle) {
        this.outputStyle = outputStyle;
    }
}
