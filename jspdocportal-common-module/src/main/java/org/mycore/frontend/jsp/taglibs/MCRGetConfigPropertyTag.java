package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.mycore.common.config.MCRConfiguration2;

public class MCRGetConfigPropertyTag extends SimpleTagSupport {
    private String var;
    private String defaultValue;
    private String prop;

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        pageContext.setAttribute(var, MCRConfiguration2.getString(prop).orElse(defaultValue));
        return;
    }

}