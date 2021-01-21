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

package org.mycore.frontend.jsp;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;

/**
 * override MyCoRe Error Servlet to provide custom error handling for JSPDocportal
 * 
 * @author Robert Stephan
 *
 */
public class MCRErrorServlet extends org.mycore.frontend.servlets.MCRErrorServlet{
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = LogManager.getLogger(MCRErrorServlet.class);

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Retrieve the possible error attributes, some may be null
        Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
        String message = (String) req.getAttribute("javax.servlet.error.message");
        @SuppressWarnings("unchecked")
        Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) req
                .getAttribute("javax.servlet.error.exception_type");
        Throwable exception = (Throwable) req.getAttribute("javax.servlet.error.exception");
        String requestURI = (String) req.getAttribute("javax.servlet.error.request_uri");
        String servletName = (String) req.getAttribute("javax.servlet.error.servletName");
        if (LOGGER.isDebugEnabled()) {
            String msg = MessageFormat.format("Handling error {0} for request ''{1}'' message: {2}", statusCode,
                    requestURI, message);
            LOGGER.debug(msg, exception);
            LOGGER.debug("Has current session: " + MCRSessionMgr.hasCurrentSession());
        }

        if (exception != null) {
            req.setAttribute("mcr_exception", exception);
        }
        if (statusCode == null) {
            statusCode = 500;
        }
        resp.setStatus(statusCode);
        try {
            req.getRequestDispatcher("/error.action?i18n=Resolver.error.default&status=" + statusCode).forward(req, resp);
        }
        catch(Throwable thr) {
            LOGGER.error("Error processing ErrorServlet",  thr);
        }
       

    }
}
