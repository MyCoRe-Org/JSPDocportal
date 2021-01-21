package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCRDebugInfoTag extends MCRSimpleTagSupport {
    private static final int REQUEST_PARAM = -1;

    protected static Logger LOGGER = LogManager.getLogger(MCRDebugInfoTag.class);

    public void doTag() throws JspException, IOException {
        try {
            PageContext pageContext = (PageContext) getJspContext();
            if (pageContext.getRequest().getParameter("debug") == null
                    || !pageContext.getRequest().getParameter("debug").equals("true")) {
                //hide debug view
                return;
            }
            JspWriter out = getJspContext().getOut();
            out.write("<div class=\"debuginfo\">");
            out.write("<h3>Variablen</h3>");
            out.write("<table width=\"100%\">");
            out.write("<tr><th>Scope</th><th>Name</th><th>Typ</th><th>Wert</th></tr>");
            writeVariables(out, PageContext.APPLICATION_SCOPE);
            writeVariables(out, PageContext.SESSION_SCOPE);
            writeVariables(out, PageContext.REQUEST_SCOPE);
            writeVariables(out, PageContext.PAGE_SCOPE);
            writeVariables(out, REQUEST_PARAM);
            out.write("</table>");
            out.write("</div>");
        } catch (Exception e) {
            LOGGER.error("could not show debug information");
        }
    }

    /**
     * 
     * @param out - a JSPWriter
     * @param scope  - a class variable like PageContext.*_SCOPE or -1 for request params
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    private void writeVariables(JspWriter out, int scope) throws IOException {
        PageContext ctx = (PageContext) getJspContext();
        Enumeration enumNames;
        if (scope == -1) {
            enumNames = ctx.getRequest().getParameterNames();
        } else {
            enumNames = ctx.getAttributeNamesInScope(scope);
        }
        while (enumNames.hasMoreElements()) {
            Object o = enumNames.nextElement();
            if (o.toString().startsWith("javax.")) {
                continue;
            }
            out.write("<tr>");
            switch (scope) {
            case PageContext.APPLICATION_SCOPE:
                out.write("<td>application</td>");
                break;
            case PageContext.SESSION_SCOPE:
                out.write("<td>session</td>");
                break;
            case PageContext.REQUEST_SCOPE:
                out.write("<td>request</td>");
                break;
            case PageContext.PAGE_SCOPE:
                out.write("<td>page</td>");
                break;
            case REQUEST_PARAM:
                out.write("<td>request-param</td>");
                break;
            default:
                out.write("<td>unknown</td>");
            }

            out.write("<td>" + o.toString() + "</td>");
            Object val = null;
            if (scope == REQUEST_PARAM) {
                val = ctx.getRequest().getParameter(o.toString());
            } else {
                val = ctx.getAttribute(o.toString(), scope);
            }
            out.write("<td>" + val.getClass().getName() + "</td>");
            String s = val.toString();
            if (s.length() > 200) {
                s = s.substring(0, 200) + "...";
            }
            s = s.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;");
            out.write("<td>" + s + "</td>");
            out.write("</tr>");
        }
    }

}