package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class MCRHasAccessTag extends SimpleTagSupport {
    private String permission;
    private String var;
    private String mcrid;

    private static final Logger LOGGER = LogManager.getLogger();

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
