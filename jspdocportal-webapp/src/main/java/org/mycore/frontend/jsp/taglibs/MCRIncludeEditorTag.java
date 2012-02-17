package org.mycore.frontend.jsp.taglibs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.ifs2.MCRContent;
import org.mycore.frontend.editor.MCREditorServlet;
import org.mycore.frontend.jsp.NavServlet;
import org.xml.sax.SAXParseException;

public class MCRIncludeEditorTag extends SimpleTagSupport {
	private static Logger logger = Logger.getLogger("MCRIncludeEditorTag.class");
	
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
		String editorBase = "";

		if (editorPath != null && !editorPath.equals("")) {
			if (editorPath.startsWith("http")) {
				editorBase = editorPath;
			} else {
				editorBase = NavServlet.getBaseURL() + editorPath;
			}
			pageContext.getSession().setAttribute("editorPath", editorBase);
		}
		JspWriter out = pageContext.getOut();

		try {
			HttpServletRequest request = (HttpServletRequest) pageContext
					.getRequest();
			File editorFile = new File(pageContext.getServletContext()
					.getRealPath(editorPath));
			Document xml = MCRXMLParserFactory.getParser(false).parseXML(
					MCRContent.readFrom(editorFile.toURI()));

			MCREditorServlet.replaceEditorElements(request, editorFile.toURI()
					.toString(), xml);

			Source xmlSource = new JDOMSource(xml);
			Source xsltSource = new StreamSource(getClass()
					.getResourceAsStream("/xsl/editor_standalone.xsl"));

			// das Factory-Pattern unterstützt verschiedene XSLT-Prozessoren
			TransformerFactory transFact = TransformerFactory.newInstance();
			transFact.setURIResolver(MCRURIResolver.instance());
			Transformer transformer = transFact.newTransformer(xsltSource);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");

			/*
			 * <!-- editor-common.xsl ============ Parameter aus MyCoRe
			 * LayoutServlet ============ --> <xsl:param
			 * name="WebApplicationBaseURL" /> <xsl:param name="ServletsBaseURL"
			 * /> <xsl:param name="DefaultLang" /> <xsl:param name="CurrentLang"
			 * /> <xsl:param name="MCRSessionID" /> <xsl:param
			 * name="HttpSession" /> <xsl:param name="JSessionID" />
			 */
			transformer.clearParameters();
			Properties props = MCRLayoutService
					.buildXSLParameters((HttpServletRequest) pageContext
							.getRequest());
			if (cancelPage != null && cancelPage.length() > 0) {
				props.put("cancelUrl", cancelPage);
			}
			MCRLayoutService.setXSLParameters(transformer, props);
			transformer.transform(xmlSource, new StreamResult(out));
		} catch (TransformerConfigurationException e) {
			logger.error("TransformerConfigurationException: " + e, e);
		} catch (TransformerException e) {
			logger.error("TransformerException " + e, e);
		} catch (SAXParseException e) {
			logger.error("TransformerException " + e, e);
		}
	}
}