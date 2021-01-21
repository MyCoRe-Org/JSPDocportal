package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;

public class MCRHasAccessTag extends SimpleTagSupport {
    private String permission;
    private String var;
    private String mcrid;

    private static Logger LOGGER = LogManager.getLogger(MCRHasAccessTag.class);

    public void setPermission(String permission) {
        this.permission = permission;

    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }

    public void doTag() throws JspException, IOException {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            PageContext pageContext = (PageContext) getJspContext();
            
            if (mcrid == null || "".equals(mcrid)) { // allgemeiner check des aktuellen Users
                pageContext.setAttribute(var, Boolean.valueOf(MCRAccessManager.checkPermission(permission)));
            } else {
                pageContext.setAttribute(var, Boolean.valueOf(MCRAccessManager.checkPermission(mcrid, permission)));
            }
        } catch (Exception e) {
            LOGGER.error("could not check access", e);
        }
    }

}