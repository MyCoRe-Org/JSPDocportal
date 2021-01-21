package org.mycore.frontend.jsp.taglibs;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCRSimpleTagSupport extends SimpleTagSupport {
    protected static Logger LOGGER = LogManager.getLogger(MCRSimpleTagSupport.class);

    protected int getScope(String scope) {
        int ret = PageContext.PAGE_SCOPE;
        if (scope != null) {
            if ("request".equalsIgnoreCase(scope)) {
                ret = PageContext.REQUEST_SCOPE;
            } else if ("application".equalsIgnoreCase(scope)) {
                ret = PageContext.APPLICATION_SCOPE;
            } else if ("session".equalsIgnoreCase(scope)) {
                ret = PageContext.SESSION_SCOPE;
            }
        }
        return ret;
    }
}