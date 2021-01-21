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
import java.nio.file.Files;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.DOMOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.datamodel.niofs.MCRPathXML;
import org.mycore.frontend.MCRFrontendUtil;

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
public class MCRRetrieveDerivateContentsXMLTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRRetrieveDerivateContentsXMLTag.class);
    
    private static DOMOutputter DOM_OUTPUTTER = new DOMOutputter();
    
    private String derid;
    
    private int depth=-1;

    private String varDOM;

    private String varJDOM;
    
     /**
     * sets the MCR Object ID (mandatory)
     * 
     * @param mcrID
     *            the MCR object ID
     * 
     */
    public void setDerid(String derid) {
        this.derid = derid;
    }
 
    /**
     * sets the level of sub directories that should be retrieved
     * depth = -1 means every level
     * @param depth
     *            the level as integer
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

   /**
     * sets the name of the variable which stores the XML Document as
     * org.w3c.dom.Document for further use in JSP processing 
     * (stored in request scope)
     * 
     * @param var -
     *            the name of the variable
     */
    public void setVarDOM(String var) {
        this.varDOM = var;
    }

    /**
     * sets the name of the variable which stores the XML Document as
     * org.jdom2.Document for further use in JSP processing 
     * (stored in request scope)
     * 
     * @param var -
     *            the name of the variable
     */

    public void setVarJDOM(String var) {
        this.varJDOM = var;
    }

    /**
     * executes the tag
     */
    public void doTag() throws JspException, IOException {
        try {
            MCRObjectID derObjID = MCRObjectID.getInstance(derid);
            MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(derObjID);
            
            Document doc = listDerivateContentAsXML(mcrDer, "/");
            
            if (varDOM != null) {
                org.w3c.dom.Document dom = DOM_OUTPUTTER.output(doc);
                getJspContext().setAttribute(varDOM, dom, PageContext.REQUEST_SCOPE);
            }
            if (varJDOM != null) {
                getJspContext().setAttribute(varJDOM, doc, PageContext.REQUEST_SCOPE);
            }
        } catch (Exception e) {
            throw new MCRException(e);
        }
    }
    
    private Document listDerivateContentAsXML(MCRDerivate derObj, String path)
            throws IOException {
            Document doc = new Document();

            MCRPath root = MCRPath.getPath(derObj.getId().toString(), "/");
            root = MCRPath.toMCRPath(root.resolve(path));
            if (depth == -1) {
                depth = Integer.MAX_VALUE;
            }
            if (root != null) {
                Element eContents = new Element("contents");
                eContents.setAttribute("mycoreobject", derObj.getOwnerID().toString());
                eContents.setAttribute("mycorederivate", derObj.getId().toString());
                doc.addContent(eContents);
                if (!path.endsWith("/")) {
                    path += "/";
                }
                MCRPath p = MCRPath.getPath(derObj.getId().toString(), path);
                if (p != null && Files.exists(p)) {
                    Element eRoot = MCRPathXML.getDirectoryXML(p).getRootElement();
                    eContents.addContent(eRoot.detach());
                    createXMLForSubdirectories(p, eRoot, 1, depth);
                }

                //add href Attributes
                String baseURL = MCRFrontendUtil.getBaseURL()
                    + MCRConfiguration2.getString("MCR.RestAPI.v1.Files.URL.path").orElseThrow();
                baseURL = baseURL.replace("${mcrid}", derObj.getOwnerID().toString()).replace("${derid}",
                    derObj.getId().toString());
                XPathExpression<Element> xp = XPathFactory.instance().compile(".//child[@type='file']", Filters.element());
                for (Element e : xp.evaluate(eContents)) {
                    String uri = e.getChildText("uri");
                    if (uri != null) {
                        int pos = uri.lastIndexOf(":/");
                        String subPath = uri.substring(pos + 2);
                        while (subPath.startsWith("/")) {
                            subPath = path.substring(1);
                        }
                        e.setAttribute("href", baseURL + subPath);
                    }
                }
            }
            return doc;
        }
    
        private static void createXMLForSubdirectories(MCRPath mcrPath, Element currentElement, int currentDepth,
            int maxDepth) {
            if (currentDepth < maxDepth) {
                XPathExpression<Element> xp = XPathFactory.instance().compile("./children/child[@type='directory']",
                    Filters.element());
                for (Element e : xp.evaluate(currentElement)) {
                    String name = e.getChildTextNormalize("name");
                    try {
                        MCRPath pChild = (MCRPath) mcrPath.resolve(name);
                        Document doc = MCRPathXML.getDirectoryXML(pChild);
                        Element eChildren = doc.getRootElement().getChild("children");
                        if (eChildren != null) {
                            e.addContent(eChildren.detach());
                            createXMLForSubdirectories(pChild, e, currentDepth + 1, maxDepth);
                        }
                    } catch (IOException ex) {
                        LOGGER.error("Error reading subdirectories",  e);
                    }
                }
            }
        }
}
