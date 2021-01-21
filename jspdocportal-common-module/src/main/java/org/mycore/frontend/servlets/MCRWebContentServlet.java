/*
 * $RCSfile$
 * $Revision: 25918 $ $Date: 2013-01-24 13:20:01 +0100 (Do, 24 Jan 2013) $
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

import javax.servlet.ServletException;

import org.mycore.common.config.MCRConfiguration2;

import net.balusc.webapp.FileServlet;

/**
 * This servlet delivers static content from the webcontent directory
 * 
 * @author Robert Stephan
 * 
 */
public class MCRWebContentServlet extends FileServlet {
    private static final long serialVersionUID = 1L;

    /**
     * The initalization of the servlet.
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        setBasePath(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow());
    }
}
