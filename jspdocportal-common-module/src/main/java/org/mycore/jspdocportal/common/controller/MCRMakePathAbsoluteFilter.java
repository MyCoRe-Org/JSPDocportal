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
package org.mycore.jspdocportal.common.controller;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * This Filter overrides the base uri of the Jersey RequestContext with its parent uri.
 * 
 * WHY?
 * We have to register our MVC Controller for multiple URLs (endpoints).
 * In an ideal world we would register it with the application root, but there are
 * lots of other services that cannot be put under Jersey MVC control in the application.
 * 
 * By doing this the @Path Annotations in MVC-Controllers may contain the root path
 * of the filter to distinguish between the multiple endpoints.
 *
 *  web.xml:
 *   <servlet>
 *     <servlet-name>MVC Jersey Servlet</servlet-name>
 *     <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
 *     <init-param>
 *       <param-name>javax.ws.rs.Application</param-name>
 *       <param-value>org.mycore.jspdocportal.common.controller.HelloWorldApp</param-value>
 *     </init-param>
 *     <load-on-startup>1</load-on-startup>
 *   </servlet>
 *   <servlet-mapping>
 *       <servlet-name>MVC Jersey Servlet</servlet-name>
 *       <url-pattern>/jersey/*</url-pattern>
 *   </servlet-mapping>
 *       <servlet-mapping>
 *       <servlet-name>MVC Jersey Servlet</servlet-name>
 *       <url-pattern>/georgia/*</url-pattern>
 *   </servlet-mapping>
 * 
 * Example:
 *  localhost:8080/myapp/view/hello
 *  without modification BaseURI: /view   Request Path: /hello
 *  with modification:   BaseURI: /       Request Path: /view/hello
 * 
 * 
 * @author Robert Stephan
 *
 */
@PreMatching
public class MCRMakePathAbsoluteFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        if (requestContext instanceof ContainerRequest) {
            ContainerRequest c = (ContainerRequest) requestContext;
            URI baseURI = c.getBaseUri().resolve("..");
            requestContext.setRequestUri(baseURI, c.getRequestUri());
        }
    }
}
