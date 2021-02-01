package org.mycore.jspdocportal.common.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;

@Path("/view/classbrowser/{modus}")
public class MCRClassBrowserController {


    @GET
    public Response defaultRes(@PathParam("modus") String modus) {
        if (MCRConfiguration2.getString("MCR.ClassBrowser." + modus + ".Classification").isEmpty()) {
            return Response.temporaryRedirect(URI.create("/")).build();
        } else {
            Map<String, String> model = new HashMap<>();
            model.put("modus", modus);
            Viewable v = new Viewable("/classbrowser", model);
            return Response.ok(v).build();
        }
    }
    }
