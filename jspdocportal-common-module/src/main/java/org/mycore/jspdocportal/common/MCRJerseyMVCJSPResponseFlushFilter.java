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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * This filter is necessary if JSPs are rendered by Jersey's MVC-JSP template engine.
 * In combination with Jersey MVC 4.0.x and Tomcat 11 there are issues with the forwarding.
 * The response is internally forwarded to the template JSP.
 * I guess the some buffers are not flushed, so the output is broken and the last bytes are missing.
 * 
 * The filter needs to be registered in web.xml or web-fragment.xml
 * for the standard Servlet for JSPs:
 * 
 *  @author Robert Stephan
 */
public class MCRJerseyMVCJSPResponseFlushFilter implements Filter {
    /*  ```
     *  <filter>
     *      <filter-name>MCRJerseyMVCJSPResponseFlushFilter</filter-name>
     *      <filter-class>org.mycore.jspdocportal.common.MCRJerseyMVCJSPResponseFlushFilter</filter-class>
     *  </filter>
     *  <!-- only on forwards, no effect on direct JSP calls -->
     *  <filter-mapping>
     *      <filter-name>MCRJerseyMVCJSPResponseFlushFilter</filter-name>
     *      <servlet-name>jsp</servlet-name>
     *      <dispatcher>FORWARD</dispatcher>
     *   </filter-mapping>
     *   ```
     */

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        chain.doFilter(request, response);

        try {
            response.getWriter().flush();
        } catch (IllegalStateException ex) {
            // getOutputStream() has already been called for this response
            response.getOutputStream().flush();
        }
    }
}
