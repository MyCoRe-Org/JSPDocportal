package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCRStaticXEditorFileServlet;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.resource.MCRResourceHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * This tag includes an xeditor definition, which can be provided as attribute
 * editorPath or in the body of the tag.
 * 
 * 
 * @author Robert Stephan
 *
 */
public class MCRIncludeXEditorTag extends SimpleTagSupport {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    private static final Pattern REGEX_XML_EMPTY_ELEMENTS = Pattern.compile("<(a|i|span|div|textarea)\\s([^>]*)?(\\s)?/>");

    private String editorPath;

    private String cancelURL;

    private String sourceURI;

    private String pageURL;

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
     * @param cancelURL
     */
    public void setCancelURL(String cancelURL) {
        this.cancelURL = cancelURL;
    }

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            MCRContent editorContent = null;
            if (editorPath != null && !editorPath.equals("")) {
                if (!editorPath.startsWith("/")) {
                    editorPath = "/" + editorPath;
                }
                editorContent = new MCRURLContent(MCRResourceHelper.getResourceUrl(editorPath));
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

                        MCRContent newContent = MCRStaticXEditorFileServlet.doExpandEditorElements(editorContent,
                                request, (HttpServletResponse) pageContext.getResponse(), sessionID, pageURL);
                        String content;
                        if (newContent != null) {
                            content = newContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                        } else {
                            content = editorContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                        }
                        // for proper display of glyhicons
                        // replace "<i class='fa fa-plus' /> with "<i class='fa fa-plus'></i>"
                        Matcher m = REGEX_XML_EMPTY_ELEMENTS.matcher(content);
                        content = m.replaceAll("<$1 $2></$1>");

                        out.append(content);
                    } else {
                        LOGGER.error("JSPTag <mcr:includeXEditor> can only contain an <xed:form> element");
                        out.append("<span class=\"error\">Please provide an &lt;xed:form&gt; element here!</span>");

                    }
                } catch (Exception e) {
                    LOGGER.error("Exception", e);
                }
            }
        }
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }
}