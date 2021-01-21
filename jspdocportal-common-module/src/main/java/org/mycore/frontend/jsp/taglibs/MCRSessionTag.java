package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

public class MCRSessionTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRSessionTag.class);

    private String info;
    private String var;

    public void setInfo(String info) {
        this.info = info;
    }

    public void setVar(String inputVar) {
        var = inputVar;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        if (info != null && !info.equals("")) {
            if (info.equals("userID")) {

                pageContext.setAttribute(var, mcrSession.getUserInformation().getUserID());

            } else if (info.equals("language")) {

                pageContext.setAttribute(var, mcrSession.getCurrentLanguage());
            } else if (info.equals("IP")) {

                pageContext.setAttribute(var, mcrSession.getCurrentIP());
            } else if (info.equals("ID")) {

                pageContext.setAttribute(var, mcrSession.getID());
            } else {
                LOGGER.error("unknown information: " + info);
            }
        }
        return;
    }

}