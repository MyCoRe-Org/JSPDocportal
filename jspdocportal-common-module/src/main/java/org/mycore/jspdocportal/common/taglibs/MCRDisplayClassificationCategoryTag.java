package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class MCRDisplayClassificationCategoryTag extends SimpleTagSupport {
    private static MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.getInstance();

    private static final Logger LOGGER = LogManager.getLogger();

    private String lang;

    private String classid;

    private String categid;

    private boolean showDescription = false;

    public void doTag() throws JspException, IOException {
        if (lang == null) {
            lang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        }
        if (classid != null && categid != null && lang != null) {
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                if (showDescription) {
                    String descr = categoryDAO.getCategory(new MCRCategoryID(classid, categid), 0).getLabel(lang).get()
                        .getDescription();
                    getJspContext().getOut().write(descr);
                } else {
                    String text = categoryDAO.getCategory(new MCRCategoryID(classid, categid), 0).getLabel(lang).get()
                        .getText();
                    getJspContext().getOut().write(text);
                }
            } catch (Exception e) {
                LOGGER.error("could not display category " + classid + ":" + categid + "@" + lang, e);
            }
        }
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setClassid(String classid) {
        this.classid = classid;
    }

    public void setCategid(String categid) {
        this.categid = categid;
    }

    public void setValueURI(String valueURI) {
        int start = valueURI.lastIndexOf("/");
        int sep = valueURI.lastIndexOf("#");
        if (start >= 0 && sep >= 0 && sep > start) {
            this.classid = valueURI.substring(start + 1, sep);
            this.categid = valueURI.substring(sep + 1);
        }
    }

    public void setShowDescription(boolean b) {
        showDescription = b;
    }

}
