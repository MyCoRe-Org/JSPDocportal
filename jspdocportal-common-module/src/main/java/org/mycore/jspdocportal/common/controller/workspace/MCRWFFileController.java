/*
 * $RCSfile$
 * $Revision$ $Date$
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

package org.mycore.jspdocportal.common.controller.workspace;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;

/**
 * This Servlet overides only the output methods of mcrfilenodservlet for jsp docportal use 
 * 
 * ToDo - check permission
 * @author Robert Stephan
 * 
 *  
 */

@javax.ws.rs.Path("/do/wffile/{path: .*}")
public class MCRWFFileController {

    private static Logger LOGGER = LogManager.getLogger(MCRWFFileController.class);

    @GET
    public Response doGet(@PathParam("path") String uri, @Context HttpServletRequest request) {
        String filename = null;
        String derivateID = null;
        String mcrObjID = null;
        if (uri != null) {
            LOGGER.debug(" Path = " + uri);
            String path[] = uri.split("/", 3);
            if (path.length == 3) {
                mcrObjID = path[0];
                derivateID = path[1];
                filename = path[2];
            }
        }

        if (filename == null || derivateID == null || mcrObjID == null) {
            //messageKey=IdNotGiven")
            Response.serverError().build();
        }
        Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrObjID),
            MCRObjectID.getInstance(derivateID));
        Path file = derDir.resolve(filename);
        if (Files.exists(file) && Files.isReadable(file)) {
            StreamingOutput stream = new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        Files.copy(file, output);
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                }
            };

            ResponseBuilder rb = Response.ok(stream);
            // 	 Set the headers.
            if (filename.endsWith("pdf"))
                rb.header("Content-Type", "application/pdf");
            else if (filename.endsWith("jpg"))
                rb.header("Content-Type", "image/jpeg");
            else if (filename.endsWith("gif"))
                rb.header("Content-Type", "image/gif");
            else if (filename.endsWith("png"))
                rb.header("Content-Type", "image/png");
            else
                rb.header("Content-Type", "application/x-download");
            rb.header("Content-Disposition", "attachment; filename=" + filename);

            return rb.build();

        }
        return Response.serverError().build();
    }
}
