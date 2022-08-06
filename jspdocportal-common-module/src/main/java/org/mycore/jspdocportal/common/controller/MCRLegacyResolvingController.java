/*
 * $RCSfile$
 * $Revision: 19974 $ $Date: 2011-02-20 12:23:20 +0100 (So, 20 Feb 2011) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.jspdocportal.common.controller;

import java.net.URI;

import org.mycore.datamodel.metadata.MCRObjectID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * Resolver for old resolving urls (still used in our OPAC) 
 * @author Robert Stephan
 */

@jakarta.ws.rs.Path("/resolve")
public class MCRLegacyResolvingController {
    @GET
    public Response doGet(@PathParam("path") String uri, @Context HttpServletRequest request) {
        String[] keys = new String[] { "id", "ppn", "urn" };
        for (String key : keys) {
            if (request.getParameterMap().containsKey(key)) {
                String value = request.getParameter(key);
                if (key.equals("id")) {
                    value = recalculateMCRObjectID(value);
                }
                if (value != null) {
                    return Response
                        .temporaryRedirect(URI.create(request.getContextPath() + "/resolve/" + key + "/" + value))
                        .build();
                }
                break;
            }
        }
        return Response.temporaryRedirect(URI.create(request.getContextPath())).build();
    }

    protected String recalculateMCRObjectID(String oldID) {
        if (oldID == null) {
            return null;
        }
        String newID = oldID
            .replace("cpr_staff_0000", "cpr_person_")
            .replace("cpr_professor_0000", "cpr_person_")
            .replace("_series_", "_bundle_");
        if (MCRObjectID.isValid(newID)) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(newID);
            return mcrObjID.toString();
        }
        return null;
    }

}
