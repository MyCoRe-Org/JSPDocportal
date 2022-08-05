/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
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
package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.frontend.MCRFrontendUtil;
import org.webjars.WebJarAssetLocator;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * A Tag, that discovers files in Webjars (independent of the currently used version of the library.
 * The file is discovery by file name and project name and the path is stored in the provided variable.
 * 
 * @author Robert Stephan
 *
 */
public class MCRWebjarLocatorTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRWebjarLocatorTag.class);

    private String project;

    private String var;

    private String file;
    
    private String htmlElement;

    /**
     * the variable where the relative path should be written to.
     * (optional, if htmlElement is set)
     * @param var as String
     */
    public void setVar(String var) {
        this.var = var;
    }
    
    /**
     * the htmlElement that should be written to output
     * (optional, if var is set)
     * @param htmlElement as String
     */
    public void setHtmlElement(String htmlElement) {
        this.htmlElement = htmlElement;
    }

    /**
     * the webjars project
     * @param project as String
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * the file, that should be retrieved
     * @param file as String
     */
    public void setFile(String file) {
        this.file = file;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        WebJarAssetLocator locator = new WebJarAssetLocator();
        try {
            String url = locator.getFullPath(project, file).substring("META-INF/resources/".length());
            if(var!=null) {
                pageContext.setAttribute(var, url);
            }
            if(htmlElement!=null) {
                if(htmlElement.equals("script")) {
                    pageContext.getOut().print("<script src=\"" + MCRFrontendUtil.getBaseURL() + url + "\"></script>");
                }
                if(htmlElement.equals("stylesheet") || htmlElement.equals("css"))  {
                    pageContext.getOut().print("<link href=\"" + MCRFrontendUtil.getBaseURL() + url + "\" rel=\"stylesheet\">");
                }
            }
        } catch (IllegalArgumentException e) {
            pageContext.getOut().print("<!--ERROR in WebjarLocator: project: " + project + " file: "+file +"\n          message: "+e.getMessage()+"-->");
            pageContext.setAttribute(var, "#NOT_FOUND#" + e.getMessage().replaceAll(" ", "_"));
            LOGGER.error(e);
        }
    }



}