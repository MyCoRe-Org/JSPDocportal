package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class MCRIsObjectLockedTag extends SimpleTagSupport {
    private String var;
    private String mcrid;

    private static final Logger LOGGER = LogManager.getLogger();

    public void setVar(String var) {
        this.var = var;
    }

    public void setmcrid(String mcrid) {
        this.mcrid = mcrid;
    }

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        Boolean result = Boolean.TRUE;
        try {
            MCRBase mcrBase = MCRMetadataManager.retrieve(MCRObjectID.getInstance(mcrid));
            MCRCategoryID state = mcrBase.getService().getState();
            if (state != null && state.getId().equals("published")) {
                result = Boolean.FALSE;
            }
            if (state != null && state.getId().equals("deleted")) {
                result = Boolean.FALSE;
            }
        } catch (MCRPersistenceException e) {
            LOGGER.debug("{}", e::getMessage);
        }
        pageContext.setAttribute(var, result);
    }
}