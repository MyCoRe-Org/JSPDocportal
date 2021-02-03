package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;

public class MCRReceiveMainDocTag extends SimpleTagSupport {
    private static Logger logger = LogManager.getLogger(MCRReceiveMainDocTag.class);

    private String derid;

    private String var;

    public void setDerid(String derid) {
        this.derid = derid;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void doTag() throws JspException, IOException {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            MCRDirectory root = MCRDirectory.getRootDirectory(derid);
            MCRFilesystemNode[] myfiles = root.getChildren(MCRDirectory.SORT_BY_NAME);//getChildren();
            if (myfiles.length > 0) {
                PageContext pageContext = (PageContext) getJspContext();
                pageContext.setAttribute(var, myfiles[0].getName());
            }
        } catch (Exception e) {
            logger.error("error in receiving mcr_obj for jdom and dom", e);
        }
    }
}