package org.mycore.jspdocportal.common.controller;

import java.net.URI;

import org.mycore.datamodel.metadata.MCRObjectID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Action Bean for deprecated /metadata URLs * 
 * @author Stephan
 *
 */
@Path("/metadata/{mcrid}")
public class MCRLegacyMetadataController {

    @GET
    public Response defaultRes(@PathParam("mcrid") String paramMcrid,
        @Context HttpServletRequest request) {
        String mcrid = paramMcrid.trim();
        if (MCRObjectID.isValid(mcrid)) {
            return Response.temporaryRedirect(URI.create(request.getContextPath() + "/resolve/id/" + mcrid)).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
