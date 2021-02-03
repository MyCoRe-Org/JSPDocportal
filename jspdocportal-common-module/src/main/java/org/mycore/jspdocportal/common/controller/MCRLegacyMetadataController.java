package org.mycore.jspdocportal.common.controller;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Action Bean for deprecated /metadata URLs * 
 * @author Stephan
 *
 */
@Path("/metadata/{mcrid}")
public class MCRLegacyMetadataController {

    @GET
    public Response defaultRes(@PathParam("mcrid") String mcrid) {
        return Response.temporaryRedirect(URI.create("resolve/id/" + mcrid)).build();
    }
}
