package org.mycore.frontend.jsp.stripes.actions.legacy;

import org.mycore.frontend.jsp.stripes.actions.MCRAbstractStripesAction;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * Action Bean for deprecated /metadata URLs * 
 * @author Stephan
 *
 */
@UrlBinding("/metadata/{mcrid}")
public class MetadataAction extends MCRAbstractStripesAction implements ActionBean {
    private String mcrid;

    @DefaultHandler
    public Resolution defaultRes() {
        RedirectResolution res = new RedirectResolution("/resolve/id/" + mcrid);
        return res;
    }

    public String getMcrid() {
        return mcrid;
    }

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }
}
