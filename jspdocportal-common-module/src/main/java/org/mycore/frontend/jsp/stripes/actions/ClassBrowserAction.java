package org.mycore.frontend.jsp.stripes.actions;

import org.mycore.common.config.MCRConfiguration2;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/classbrowser/{modus}")
public class ClassBrowserAction extends MCRAbstractStripesAction implements ActionBean {

    private String modus = "";

    public ClassBrowserAction() {

    }

    @DefaultHandler
    public Resolution defaultRes() {
        if (MCRConfiguration2.getString("MCR.ClassBrowser." + modus + ".Classification").isEmpty()) {
            return new RedirectResolution("/");
        } else {
            return new ForwardResolution("/content/classbrowser.jsp");
        }
    }

    public String getModus() {
        return modus;
    }

    public void setModus(String modus) {
        this.modus = modus;
    }
}
