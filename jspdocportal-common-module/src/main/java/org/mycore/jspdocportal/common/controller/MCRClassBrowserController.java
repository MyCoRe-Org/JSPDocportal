package org.mycore.jspdocportal.common.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

@Path("/do/classbrowser/{modus}")
public class MCRClassBrowserController {

    @GET
    public Response defaultRes(@PathParam("modus") String modus,
        @Context HttpServletRequest request) {
        if (MCRConfiguration2.getString("MCR.ClassBrowser." + modus + ".Classification").isEmpty()) {
            return Response.temporaryRedirect(URI.create(request.getContextPath() + "/")).build();
        } else {
            Map<String, String> model = new HashMap<>();
            model.put("modus", modus);
            Viewable v = new Viewable("/classbrowser", model);
            return Response.ok(v).build();
        }
    }

    public static List<MCRCategory> flattenClassification(MCRCategory categ) {
        List<MCRCategory> newChildren = new ArrayList<MCRCategory>();
        collect(newChildren, categ);
        return newChildren;
    }

    private static void collect(List<MCRCategory> target, MCRCategory source) {
        for (MCRCategory c : source.getChildren()) {
            MCRCategory cNew = new MCRCategoryImpl();
            cNew.setId(c.getId());
            cNew.getLabels().addAll(c.getLabels());
            target.add(cNew);
            collect(target, c);
        }
    }

}
