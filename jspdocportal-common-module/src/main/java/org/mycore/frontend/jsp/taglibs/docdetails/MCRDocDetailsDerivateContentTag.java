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
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * includes the content (text / image) of a derivate into the docdetails view
 * 
 * @author Robert Stephan
 *
 */
public class MCRDocDetailsDerivateContentTag extends SimpleTagSupport {
    private String xp;
    private String width = "500px";
    private String encoding = "UTF-8";

    public void doTag() throws JspException, IOException {
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        if (docdetails == null) {
            throw new JspException("This tag must be nested in tag called 'docdetails' of the same tag library");
        }
        MCRDocDetailsRowTag docdetailsRow = (MCRDocDetailsRowTag) findAncestorWithClass(this,
                MCRDocDetailsRowTag.class);
        if (docdetailsRow == null) {
            throw new JspException("This tag must be nested in tag called 'row' of the same tag library");
        }
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            JspWriter out = getJspContext().getOut();

            XPathUtil xu = new XPathUtil((PageContext) getJspContext());
            @SuppressWarnings("rawtypes")
            List nodes = xu.selectNodes(docdetailsRow.getContext(), xp);
            if (nodes.size() > 0) {
                Object o = getJspContext().getAttribute("WebApplicationBaseURL", PageContext.APPLICATION_SCOPE);
                if (o == null) {
                    o = new String("");
                }

                Node n = (Node) nodes.get(0);
                Element eN = (Element) n;
                String derID = eN.getAttributeNS(MCRConstants.XLINK_NAMESPACE.getURI(), "href");
                String title = eN.getAttributeNS(MCRConstants.XLINK_NAMESPACE.getURI(), "label");

                out.write("<td class=\"" + docdetails.getStylePrimaryName() + "-value\">");

                StringBuffer sbUrl = new StringBuffer(o.toString());
                sbUrl.append("file/");
                Document doc = null;
                Node nd = docdetails.getContext();
                if (nd instanceof Document) {
                    doc = (Document) nd;
                } else {
                    doc = nd.getOwnerDocument();
                }
                sbUrl.append(doc.getDocumentElement().getAttribute("ID"));
                sbUrl.append("/");
                sbUrl.append(derID);
                sbUrl.append("/");

                MCRDirectory root = MCRDirectory.getRootDirectory(derID);
                if (root != null) {
                    MCRFilesystemNode[] myfiles = root.getChildren();
                    boolean accessAllowed = MCRAccessManager.checkPermission(derID, "read");
                    for (int j = 0; j < myfiles.length; j++) {
                        MCRFile theFile = (MCRFile) myfiles[j];
                        if (accessAllowed) {
                            String fURL = sbUrl.toString() + theFile.getName();
                            String contentType = theFile.getContentTypeID();
                            if (contentType.contains("html") || contentType.contains("xml")) {
                                String content = retrieveHTMLBody(theFile.getContentAsString(encoding));
                                out.write(content);
                            }
                            if (contentType.contains("jpeg")) {
                                out.write("<a href=\"" + fURL + "\" target=\"_blank\" title=\""
                                        + docdetails.getMessages().getString("OMD.showLargerImage") + "\"  alt=\""
                                        + docdetails.getMessages().getString("OMD.showLargerImage") + "\">");
                                out.write("<img src=\"" + fURL + "\" width=\"" + width + "\" alt=\"" + title
                                        + "\" /></a>");
                            }
                        }
                    }
                }
                out.write("</td>");
            } // error
        } catch (Exception e) {
            throw new JspException("Error executing docdetails:derivatecontent tag", e);
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
     * the width which should be used to display the content 
     * @param width
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * the encoding of the given file
     * @param encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private String retrieveHTMLBody(String content) {
        if (content.contains("<body>") && content.contains("</body>")) {
            int start = content.indexOf("<body>") + 6;
            int ende = content.lastIndexOf("</body>");
            return content.substring(start, ende);
        } else {
            return content;
        }
    }
}
