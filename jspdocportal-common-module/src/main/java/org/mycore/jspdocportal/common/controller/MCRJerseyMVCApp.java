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

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.mycore.common.config.MCRConfiguration2;

public class MCRJerseyMVCApp extends ResourceConfig {
    public MCRJerseyMVCApp() {
        super();
        setApplicationName("MyCoRe Jersey MVC");
        String packages = MCRConfiguration2.getString("MCR.JSPDocportal.MVCApp.packages").orElse("org.mycore.jspdocportal.common.controller");
        packages(packages.split(","));
        property(JspMvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/views");
        register(MCRMakePathAbsoluteFilter.class);
        register(JspMvcFeature.class);
        register(MultiPartFeature.class);
    }
}
