/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.jspdocportal.common;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;

/**
 * ServletFilter which initializes a MyCoRe Session for JSPs
 * 
 * @author Robert Stephan
 *
 */
public class MCRSessionInitializationFilter implements Filter {
    
 public static final String ATTRIBUTE_NAME_INITIAL_URL = "initialURL"; 
    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String name = getClass().getName() + "_" + UUID.randomUUID().toString();

        //sets the initial URL into a request attribute 
        //because JSPs as views (Jersey MVC) return their own path as request URL
        if (httpRequest.getAttribute(ATTRIBUTE_NAME_INITIAL_URL) == null) {
            httpRequest.setAttribute(ATTRIBUTE_NAME_INITIAL_URL, httpRequest.getRequestURL());
        }
        
        MCRServlet.initializeMCRSession(httpRequest, name);
        
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        
        //update the Language (Locale) in the MCRSession, if a request parameter &lang=xy is present
        MCRFrontendUtil.configureSession(mcrSession, httpRequest, httpResponse);
        
        //this would set the current locale into the JSP Standard Taglib Configuration,
        //but switching the language seems to work without this command
        //import javax.servlet.jsp.jstl.core.Config;
        //Config.set(httpRequest.getSession(), Config.FMT_LOCALE, mcrSession.getLocale());

        chain.doFilter(request, response);

        MCRServlet.cleanupMCRSession(httpRequest, name);
    }

    @Override
    public void destroy() {

    }

}