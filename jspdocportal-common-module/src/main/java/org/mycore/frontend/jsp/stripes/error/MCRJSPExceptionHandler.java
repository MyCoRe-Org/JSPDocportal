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
package org.mycore.frontend.jsp.stripes.error;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import net.sourceforge.stripes.exception.ExceptionHandler;

/**
 * general error handler for stripes
 * 
 * keep exception and redirect to error.action
 * 
 * @author Robert Stephan
 * 
 * @see https://stripesframework.atlassian.net/wiki/spaces/STRIPES/pages/491987/Exception+Handling
 *
 */
public class MCRJSPExceptionHandler extends DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public void handle(Throwable thr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // ActionBean bean = (ActionBean) request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
        
        request.setAttribute("mcr_exception", thr);
        if(!request.getServletPath().endsWith("error.jsp") && !request.getServletPath().endsWith("error.action")) {
            try {
                request.getRequestDispatcher("/error.action").forward(request, response);
            }
            catch(Throwable t) {
                //ignore
            }
        }
        else if(thr!=null) {
        	OutputStream out = response.getOutputStream();
        	try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
        		pw.append("\n"+thr.getMessage()+"\n");
        		thr.printStackTrace(pw);
        	}
        	catch(Exception e) {
        		//do nothing
        	}
        }
    }
}
