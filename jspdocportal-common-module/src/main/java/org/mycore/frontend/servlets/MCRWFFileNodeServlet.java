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

package org.mycore.frontend.servlets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;


/**
 * This Servlet overides only the output methods of mcrfilenodservlet for jsp docportal use 
 * 
 * ToDo - check permission
 * @author Anja Schaar
 * 
 *  
 *  */
public class MCRWFFileNodeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = LogManager.getLogger(MCRWFFileNodeServlet.class);

    /**
     * The initalization of the servlet.
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.debug("servletPath=" + request.getServletPath());
        String uri = request.getPathInfo();
        String filename = null;

        String derivateID = null;
        String mcrObjID = null;

        if (uri != null) {
            LOGGER.debug(" Path = " + uri);
            String path[] = uri.split("/", 4);
            if(path.length!=3) {
                mcrObjID = path[1];
                derivateID = path[2];
                filename = path[3];
            }
        }

        if (filename == null || derivateID == null || mcrObjID==null) {
            getServletContext().getRequestDispatcher("/nav?path=~mycore-error&messageKey=IdNotGiven").forward(request,
                    response);
        }
        Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrObjID), MCRObjectID.getInstance(derivateID));
        Path file = derDir.resolve(filename);
        if (Files.exists(file) && Files.isReadable(file)) {
            // 	 Set the headers.
            if (filename.endsWith("pdf"))
                response.setContentType("application/pdf");
            else if (filename.endsWith("jpg"))
                response.setContentType("image/jpeg");
            else if (filename.endsWith("gif"))
                response.setContentType("image/gif");
            else if (filename.endsWith("png"))
                response.setContentType("image/png");
            else
                response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            // Send the file.
            Files.copy(file,  response.getOutputStream());
        }

    }
}