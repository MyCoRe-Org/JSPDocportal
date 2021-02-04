package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

public class MCRIncludeEditorTag extends SimpleTagSupport {
    private static Logger logger = LogManager.getLogger(MCRIncludeEditorTag.class);

    private String editorPath;
    private String cancelPage;

    public void setEditorPath(String editorPath) {
        this.editorPath = editorPath;
    }

    public void setCancelPage(String cancelPage) {
        this.cancelPage = cancelPage;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        if (editorPath != null && !editorPath.equals("")) {
            if (!editorPath.startsWith("/")) {
                editorPath = "/" + editorPath;
            }
            pageContext.getSession().setAttribute("editorPath", editorPath);
        }
        StringWriter out = new StringWriter();

        try {
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            URL editorXML = getClass().getResource(editorPath);
            MCRContent editorContent = new MCRURLContent(getClass().getResource(editorPath));
            Document xml = editorContent.asXML();
            // TODO use MCRStaticXEditorFileServlet.doExpandEditorElements
            // MCREditorServlet.replaceEditorElements(request, editorXML.toURI().toString(),
            // xml);

            Source xmlSource = new JDOMSource(xml);
            Source xsltSource = new StreamSource(getClass().getResourceAsStream("/xsl/editor_standalone.xsl"));

            // das Factory-Pattern unterstützt verschiedene XSLT-Prozessoren
            TransformerFactory transFact = TransformerFactory.newInstance();
            transFact.setURIResolver(MCRURIResolver.instance());
            Transformer transformer = transFact.newTransformer(xsltSource);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            /*
             * <!-- editor-common.xsl ============ Parameter aus MyCoRe LayoutServlet
             * ============ --> <xsl:param name="WebApplicationBaseURL" /> <xsl:param
             * name="ServletsBaseURL" /> <xsl:param name="DefaultLang" /> <xsl:param
             * name="CurrentLang" /> <xsl:param name="MCRSessionID" /> <xsl:param
             * name="HttpSession" /> <xsl:param name="JSessionID" />
             */
            transformer.clearParameters();
            MCRParameterCollector paramColl = MCRParameterCollector.getInstanceFromUserSession();
            paramColl.setParametersTo(transformer);
            if (cancelPage != null && cancelPage.length() > 0) {
                paramColl.setParameter("cancelUrl", cancelPage);
            }
            paramColl.setParametersTo(transformer);
            transformer.transform(xmlSource, new StreamResult(out));

            pageContext.getOut().append(out.toString());

        } catch (TransformerConfigurationException e) {
            logger.error("TransformerConfigurationException: " + e, e);
        } catch (TransformerException e) {
            logger.error("TransformerException " + e, e);
        } catch (SAXException e) {
            logger.error("SAXException " + e, e);
        } catch (JDOMException e) {
            logger.error("JDOMException " + e, e);
        }
    }
}