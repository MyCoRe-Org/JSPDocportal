package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jdom2.Document;
import org.jdom2.output.XMLOutputter;

public class MCRSetQueryAsStringTag extends SimpleTagSupport {
    private Document jdom;
    private String var;

    public void setVar(String inputVar) {
        var = inputVar;
        return;
    }

    public void setJdom(Document inputJdom) {
        jdom = inputJdom;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        XMLOutputter output = new XMLOutputter(org.jdom2.output.Format.getRawFormat());
        String str = output.escapeAttributeEntities(output.escapeElementEntities(output.outputString(jdom)));

        pageContext.setAttribute(var, str);

        return;
    }

}