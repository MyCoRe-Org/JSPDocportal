package org.mycore.jspdocportal.common.controller;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 *       
 * @author Stephan
 *
 */
@Path("/do/sitemap")
public class MCRSitemapController {
    
    private static final String VIEW = MCRConfiguration2.getStringOrThrow("MCR.JSPDocportal.SitemapController.View");

    @GET
    public Response get() {
        Map<String, String> model = new HashMap<>();
        Viewable v = new Viewable(VIEW, model);
        return Response.ok(v).build();
    }
}
