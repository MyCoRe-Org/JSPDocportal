package org.mycore.frontend.jsp.stripes.actions;

import org.apache.commons.lang3.StringUtils;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.services.i18n.MCRTranslation;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Action that can be used to display html content.
 * 
 * The following parameters can be used:
 * 
 * - show: redirects to a jsp in content directory
 *         The suffix ".jsp" will be added automatically
 *         Subdirectories can be simmulated by "." 
 *         ?show=info.aktuelles would redirect to /content/info/aktuelles.jsp
 *         
 *   main: identifier for text block (stored in mcr-data directory)
 *         which defines the main text content
 *  
 *  info: comma separated list of identifiers for text blocks (stored in mcr-data-directory)
 *         which can be used for info blocks (at the right side)
 *          
 * @author Stephan
 *
 */
@UrlBinding("/site/{path}")
public class WebpageAction extends MCRAbstractStripesAction implements ActionBean {

    private String path;

    String info = null;

    public WebpageAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("info") != null) {
            info = cleanParameter(getContext().getRequest().getParameter("info"));
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        if (path != null) {
            path = path.replace("\\", "/");
            
            if (!path.contains("..") && StringUtils.countMatches(path, "/") <= 3) {
                String navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.navbar." + path.replace("/", ".")).orElse(null);
                if (navPath != null) {
                    getContext().getRequest().setAttribute("org.mycore.navigation.navbar.path", navPath);
                }
                navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.side." + path.replace("/", ".")).orElse(null);
                if (navPath != null) {
                    getContext().getRequest().setAttribute("org.mycore.navigation.side.path", navPath);
                }
                
                navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.left." + path.replace("/", ".")).orElse(null);
                if (navPath != null) {
                    getContext().getRequest().setAttribute("org.mycore.navigation.path", navPath);
                }
                return new ForwardResolution(MCRConfiguration2.getString("MCR.Webpage.Resolution." + path.replace("/", ".")).orElse(
                        MCRConfiguration2.getString("MCR.Webpage.Resolution.default").orElse("/WEB-INF/views/webpage.jsp")));
            }
        }
        return new ForwardResolution("/");

    }

    private String cleanParameter(String s) {
        return s.replaceAll("[^a-zA-Z_0-9.,]", "");
    }

    public String getInfo() {
        return info;
    }
    
    public String getInfoBox() {
        String infoBox =  MCRConfiguration2.getString("MCR.Webpage.Infobox." + path.replace("/", ".")).orElse(null);
        if(infoBox!=null) {
            infoBox = infoBox.replace(".", "/");
        }
        return infoBox;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String calcFacetOutputString(String facetKey, String facetValue) {
        String result = facetValue;
        if (facetKey.contains("_msg.facet")) {
            result = MCRTranslation.translate("Browse.Facet." + facetKey.replace("_msg.facet", "") + "." + facetValue);
        }
        if (facetKey.contains("_class.facet")) {
            MCRCategory categ = MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.fromString(facetValue),
                    0);
            if (categ != null) {
                result = categ.getCurrentLabel().get().getText();
            }
        }

        return result;

    }

}
