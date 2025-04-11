/*
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
 * 
 */
package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class MCRSessionTag extends SimpleTagSupport {
    private static final Logger LOGGER = LogManager.getLogger();

    /** 
     * Time when the webapp was deployed 
     * coded as base32 String
     */
    private static String deployTimeBase32 = null;

    private String info;

    private String var;

    public void setInfo(String info) {
        this.info = info;
    }

    public void setVar(String inputVar) {
        var = inputVar;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        switch (info) {
            case "userID":
                pageContext.setAttribute(var, mcrSession.getUserInformation().getUserID());
                break;
            case "language":
                pageContext.setAttribute(var, mcrSession.getCurrentLanguage());
                break;
            case "IP":
                pageContext.setAttribute(var, mcrSession.getCurrentIP());
                break;
            case "ID":
                pageContext.setAttribute(var, mcrSession.getID());
                break;
            case "deployTimeBase32":
                if (deployTimeBase32 == null) {
                    //set the deploy time of the web application as base32 String
                    String path2WEBINF = pageContext.getServletContext().getRealPath("/WEB-INF");
                    if (path2WEBINF != null) {
                        Path p = Paths.get(path2WEBINF);
                        if (Files.exists(p)) {
                            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                            deployTimeBase32 = Long.toString(attr.creationTime().toMillis(), 32);
                        }
                    }
                }
                pageContext.setAttribute(var, deployTimeBase32);
                break;
            default:
                LOGGER.info("unknown information key: " + info);
                break;
        }
    }
}
