package org.mycore.frontend.jsp;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.DOMBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.frontend.servlets.MCRServlet;

public class NavServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(NavServlet.class);

	private static org.jdom2.Document navJdom;

	private static Namespace NS_NAV = Namespace.getNamespace("n", "http://www.mycore.org/jspdocportal/navigation");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");

		ServletContext context = this.getServletContext();

		setBaseURL(request);

		if (navJdom == null) {
			DOMBuilder domB = new DOMBuilder();
			navJdom = domB.build((org.w3c.dom.Document) context.getAttribute("navDom"));
		}

		String path = request.getParameter("path");
		if (path == null) {
			path = "left.start";
		}
		LOGGER.debug("Navigation servlet called with path " + path + " (session "
				+ MCRServlet.getSession(request).getID() + ")");

		Element navitem = null;

		if (path.startsWith("~")) {
			XPathExpression<Element> xpe = XPathFactory.instance()
					.compile("/n:navigations//n:refitem[@id='" + path + "']", Filters.element(), null, NS_NAV);
			Element refitem = xpe.evaluateFirst(navJdom);
			if (refitem != null) {
				navitem = refitem.getParentElement();
			} else {
				LOGGER.debug("navigation.xml does not contain an entry for " + path);
			}
		} else {
			String[] nodes = path.split("\\.");
			StringBuffer xpath = new StringBuffer("/n:navigations");
			for (int i = 0; i < nodes.length; i++) {
				LOGGER.debug("i = " + i);
				if (i == 0) {
					xpath.append("/n:navigation[@id='").append(nodes[i]).append("']");
				} else {
					xpath.append("/n:navitem[@id='").append(nodes[i]).append("']");
				}
			}
			navitem = XPathFactory.instance().compile(xpath.toString(), Filters.element(), null, NS_NAV)
					.evaluateFirst(navJdom);
		}

		if (navitem == null) {
			navitem = XPathFactory.instance().compile("/n:navigations//n:navitem[1]", Filters.element(), null, NS_NAV)
					.evaluateFirst(navJdom);
		}

		MCRServlet.getSession(request).put("navPath", createPath(navitem, null));
		String contentPage = navitem.getAttributeValue("href");
		String extern = navitem.getAttributeValue("extern");
		if (extern != null && extern.equals("true")) {
			response.sendRedirect(navitem.getAttributeValue("href"));
			return;
		}

		org.w3c.dom.Document domYouAreHere = null;
		try {
			domYouAreHere = new org.jdom2.output.DOMOutputter()
					.output(new org.jdom2.Document((Element) navitem.clone()));
		} catch (org.jdom2.JDOMException e) {
			Logger.getLogger(NavServlet.class).error("Domoutput failed: ", e);
		}

		request.setAttribute("youAreHere", domYouAreHere);

		if (contentPage == null || contentPage.equals("")) {
			contentPage = "content/index.jsp";
		}

		try {
			context.getRequestDispatcher("/" + contentPage).include(request, response);
		} catch (Exception e) {

			LOGGER.error("catched error: ", e);
		}

	}

	/**
	 * retrieves the baseURL - this can only be done by a servlet
	 */
	public void setBaseURL(HttpServletRequest request) {
		initWebApplicationBaseURL(getServletContext(), request);

	}

	public static void initWebApplicationBaseURL(ServletContext context, HttpServletRequest request) {
		if (context.getAttribute("WebApplicationBaseURL") == null) {
			LOGGER.debug("baseURL is null");

			String contextPath = request.getContextPath();
			if (contextPath == null) {
				contextPath = "";
			}
			contextPath += "/";
			String requestURL = request.getRequestURL().toString();
			int pos = requestURL.indexOf(contextPath, 9);
			String baseURL = requestURL.substring(0, pos) + contextPath;

			context.setAttribute("WebApplicationBaseURL", baseURL);
			LOGGER.debug("baseURL now set to: " + baseURL);
		}
	}

	private String createPath(Element navItem, String subPath) {
		if (navItem == null) {
			return "";
		}
		if (subPath == null) {
			subPath = "";
		}
		if (subPath.length() > 0) {
			subPath = "." + subPath;
		}
		subPath = navItem.getAttributeValue("id") + subPath;
		if (!navItem.getParentElement().getName().equals("navigations")) {
			subPath = createPath(navItem.getParentElement(), subPath);
		}
		return subPath;
	}
}
