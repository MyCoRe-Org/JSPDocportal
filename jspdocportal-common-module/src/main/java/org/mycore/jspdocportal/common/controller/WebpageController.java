package org.mycore.jspdocportal.common.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

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
@Path("/site/{path: .*}")
//@Path("/site/publish")
public class WebpageController {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable get(@PathParam("path") @DefaultValue("publish") String path,
        @Context HttpServletRequest request,
        @QueryParam("info") String infoParam) {
        Map<String, String> model = new HashMap<>();

        String info = cleanParameter(infoParam);
        model.put("info", info);

        String infoBox = MCRConfiguration2.getString("MCR.Webpage.Infobox." + path.replace("/", ".")).orElse(null);
        if (infoBox != null) {
            model.put("infoBox", infoBox.replace(".", "/"));
        }

        if (path != null) {
            path = path.replace("\\", "/");
            model.put("path",  path);

            if (!path.contains("..") && StringUtils.countMatches(path, "/") <= 3) {
                String navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.navbar." + path.replace("/", "."))
                    .orElse(null);
                if (navPath != null) {
                    request.setAttribute("org.mycore.navigation.navbar.path", navPath);
                }
                navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.main." + path.replace("/", "."))
                    .orElse(null);
                if (navPath != null) {
                    request.setAttribute("org.mycore.navigation.main.path", navPath);
                }
                navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.side." + path.replace("/", "."))
                    .orElse(null);
                if (navPath != null) {
                    request.setAttribute("org.mycore.navigation.side.path", navPath);
                }

                navPath = MCRConfiguration2.getString("MCR.Webpage.Navigation.left." + path.replace("/", "."))
                    .orElse(null);
                if (navPath != null) {
                    request.setAttribute("org.mycore.navigation.path", navPath);
                }

                String template = MCRConfiguration2.getString("MCR.Webpage.Resolution." + path.replace("/", "."))
                    .orElse(
                        MCRConfiguration2.getString("MCR.Webpage.Resolution.default").orElse("/webpage"));

                Viewable v = new Viewable(template, model);
                return v;
                //return Response.ok(v).build();
            }
        }
        //return Response.temporaryRedirect(URI.create(request.getContextPath())).build();
        return null;

    }

    private String cleanParameter(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("[^a-zA-Z_0-9.,]", "");
    }

    //TODO move to Tag
    public String calcFacetOutputString(String facetKey, String facetValue) {
        String result = facetValue;
        if (facetKey.contains("_msg.facet")) {
            result = MCRTranslation.translate("Browse.Facet." + facetKey.replace("_msg.facet", "") + "." + facetValue);
        }
        if (facetKey.contains("_class.facet")) {
            MCRCategory categ = MCRCategoryDAOFactory.obtainInstance().getCategory(MCRCategoryID.ofString(facetValue),
                0);
            if (categ != null) {
                result = categ.getCurrentLabel().get().getText();
            }
        }

        return result;

    }

}
