package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCRStaticXEditorFileServlet;
import org.xml.sax.SAXException;

/**
 * This tag includes an xeditor definition, which can be provided as attribute
 * editorPath or in the body of the tag.
 * 
 * 
 * @author Robert Stephan
 *
 */
public class MCRIncludeXEditorTag extends SimpleTagSupport {
    private static Logger LOGGER = Logger.getLogger(MCRIncludeXEditorTag.class);

    public static Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    private static Pattern REGEX_XML_EMPTY_ELEMENTS = Pattern.compile("<(a|i|span|div)\\s([^/>]*)?(\\s)?/>");

    private String editorPath = null;

    private String cancelURL = null;

    private String sourceURI = null;

    private String pageURL = null;

    /**
     * Path to external editor definition
     * 
     * @param editorPath
     */
    public void setEditorPath(String editorPath) {
        this.editorPath = editorPath;
    }

    /**
     * Path to cancel URL
     * 
     * @param editorPath
     */
    public void setCancelURL(String cancelURL) {
        this.cancelURL = cancelURL;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        boolean doCommitTransaction = false;
        if (!MCRSessionMgr.getCurrentSession().isTransactionActive()) {
            doCommitTransaction = true;
            MCRSessionMgr.getCurrentSession().beginTransaction();
        }
        MCRContent editorContent = null;
        if (editorPath != null && !editorPath.equals("")) {
            if (!editorPath.startsWith("/")) {
                editorPath = "/" + editorPath;
            }
            editorContent = new MCRURLContent(getClass().getResource(editorPath));
        } else {
            if (getJspBody() != null) {
                StringWriter sw = new StringWriter();
                getJspBody().invoke(sw);
                editorContent = new MCRStringContent(sw.toString());
            }
        }
        if (editorContent != null) {
            try {
                JspWriter out = pageContext.getOut();
                Document doc = editorContent.asXML();
                if (doc.getRootElement().getName().equals("form")
                    && doc.getRootElement().getNamespace().equals(NS_XED)) {
                    if (cancelURL != null && cancelURL.length() > 0) {
                        // setze xed:cancel
                        Element elCancel = new Element("cancel", NS_XED).setAttribute("url", cancelURL);
                        doc.getRootElement().addContent(0, elCancel);
                    }
                    if (sourceURI != null && sourceURI.length() > 0) {
                        // setze xed:cancel
                        Element elSource = new Element("source", NS_XED).setAttribute("uri", sourceURI);
                        doc.getRootElement().addContent(0, elSource);
                    }

                    editorContent = new MCRJDOMContent(doc);
                    editorContent.setDocType("MyCoReWebPage");

                    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                    if (pageURL == null) {
                        pageURL = request.getRequestURL().toString();
                        String referer = request.getHeader("Referer");
                        if (referer != null) {
                            pageURL = referer;
                        }
                    }
                    String sessionID = request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
                    if (sessionID != null && sessionID.contains("-")) {
                        sessionID = sessionID.split("-")[0];
                    }

                    MCRContent newContent = MCRStaticXEditorFileServlet.doExpandEditorElements(editorContent, request,
                        (HttpServletResponse) pageContext.getResponse(), sessionID, pageURL);
                    String content = null;
                    if (newContent != null) {
                        content = newContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    } else {
                        content = editorContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    }
                    // for proper display of glyhicons
                    // replace "<i class='glyphicon glyphicon-plus' /> with "<i class='glyphicon glyphicon-plus'></i>"
                    Matcher m = REGEX_XML_EMPTY_ELEMENTS.matcher(content);
                    content = m.replaceAll("<$1 $2></$1>");

                    out.append(content);
                } else {
                    LOGGER.error("JSPTag <mcr:includeXEditor> can only contain an <xed:form> element");
                    out.append("<span class=\"error\">Please provide an &lt;xed:form&gt; element here!</span>");

                }
            } catch (SAXException e) {
                LOGGER.error("SAXException " + e, e);
            } catch (JDOMException e) {
                LOGGER.error("JDOMException " + e, e);
            }
        }
        if (doCommitTransaction) {
            MCRSessionMgr.getCurrentSession().commitTransaction();
        }

    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }
}