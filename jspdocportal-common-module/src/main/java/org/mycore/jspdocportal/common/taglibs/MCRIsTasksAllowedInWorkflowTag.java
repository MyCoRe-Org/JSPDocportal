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
 */
package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * returns true,
 * if a specific task is allowed for the type of the given MyCoRe object.
 */
public class MCRIsTasksAllowedInWorkflowTag extends SimpleTagSupport {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROP_KEY_PREFIX = "MCR.Worfklow.MCRObject.Tasks.";

    private String var;
    private String mcrid;
    private String task;

    public void setVar(String var) {
        this.var = var;
    }

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        pageContext.setAttribute(var, false);
        try {
            MCRObjectID mcrobjid = MCRObjectID.getInstance(mcrid);
            Map<String, String> taskMap = MCRConfiguration2.getSubPropertiesMap(PROP_KEY_PREFIX);
            if (taskMap.containsKey(mcrobjid.getTypeId())) {
                List<String> tasks = Arrays.stream(taskMap.get(mcrobjid.getTypeId()).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
                pageContext.setAttribute(var, tasks.contains(task));
            }
        } catch (Exception e) {
            LOGGER.debug("{}", e::getMessage);
        }
    }
}
