/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
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

package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;

import org.mycore.common.config.MCRConfiguration2;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * JSP tag that checks whether the object type of given object ID
 * is excluded by a configured list of object types.
 */
public class MCRIsObjectTypeExcludedTag extends MCRAbstractTag {

    private String configKey;
    private String mcrId;
    private String var;

    /**
     * Sets the configuration key containing the excluded object types.
     *
     * @param configKey the configuration key
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     * Sets the object ID to check.
     *
     * @param mcrId the MyCoRe object ID
     */
    public void setMcrId(String mcrId) {
        this.mcrId = mcrId;
    }

    /**
     * Sets the name of the page context attribute that receives the result.
     *
     * @param var the attribute name
     */
    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();

        boolean excluded = false;
        String excludedTypes = MCRConfiguration2.getString(configKey).orElse("");

        if (!excludedTypes.isBlank() && mcrId != null) {
            for (String type : excludedTypes.split(",")) {
                type = type.trim();
                if (!type.isEmpty() && mcrId.contains("_" + type + "_")) {
                    excluded = true;
                    break;
                }
            }
        }

        pageContext.setAttribute(var, excluded);
    }

}
