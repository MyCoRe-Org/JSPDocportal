package org.mycore.jspdocportal.common.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

/**
 *       
 * @author Stephan
 *
 */
@Path("/do/sitemap")
public class MCRSitemapController {

    @GET
    public Response get(){
        Map<String, String> model = new HashMap<>();
        Viewable v = new Viewable("/sitemap/sitemap", model);
        return Response.ok(v).build();
    }
}
