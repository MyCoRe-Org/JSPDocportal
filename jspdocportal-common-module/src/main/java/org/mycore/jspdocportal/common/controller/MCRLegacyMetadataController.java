package org.mycore.jspdocportal.common.controller;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * Action Bean for deprecated /metadata URLs * 
 * @author Stephan
 *
 */
@Path("/metadata/{mcrid}")
public class MCRLegacyMetadataController {

    @GET
    public Response defaultRes(@PathParam("mcrid") String mcrid,
        @Context HttpServletRequest request) {
        return Response.temporaryRedirect(URI.create(request.getContextPath() + "/resolve/id/" + mcrid)).build();
    }
}
