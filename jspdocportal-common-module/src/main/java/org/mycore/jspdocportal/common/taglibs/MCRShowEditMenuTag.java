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
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUserManager;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

/**
 * <p>Tag that displays a hamburger menu for editing an object in workspace</p>
 * <p>It checks for the allow permission of the current user and displays an entry"
 * 
 * <p>It has the following attributes:</p>
 * <ul>
 * <li>mcrid: The MyCoRe ID of the object to edit
 * <li>cssClass: additional CSS classes for styling</p>
 * </ul>
 *  
 *  <p>Sample Usage (simple):</p>
 *  
 *  &lt;mcr:showEditMenu mcrid="rosdok_document_00001234", cssClass="float-right" /&gt;
 *  
 * @author Robert Stephan
 *
 */
public class MCRShowEditMenuTag extends MCRAbstractTag {
    private String mcrid;

    private String cssClass = "";

    public void doTag() throws JspException, IOException {
        init();
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
        boolean canEdit = MCRAccessManager.checkPermission(mcrObjID, "writedb");
        if (!canEdit) {
            return;
        }
        JspWriter out = getJspContext().getOut();
        out.append("<div class=\"ir-edit-menu dropdown " + cssClass + "\">");
        out.append("  <button class=\"ir-edit-menu-btn btn btn-primary dropdown-toggle "
            + "\" type=\"button\" id=\"dropdownEditButton\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">");
        out.append("    <i class=\"fas fa-pencil-alt\"></i>");
        out.append("  </button>");
        out.append("  <div class=\"dropdown-menu dropdown-menu-right\" aria-labelledby=\"dropdownEditButton\">");

        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
        if (mcrObj.getService().isFlagTypeSet("editedby")) {
            if (mcrObj.getService().getFlags("editedby").contains(MCRUserManager.getCurrentUser().getUserID())) {
                out.append("    <a class=\"dropdown-item\" href=\"" + MCRFrontendUtil.getBaseURL()
                    + "do/workspace/tasks\">zur Arbeitsmappe</a>");
            } else {
                out.append("    <a class=\"dropdown-item disabled\" href=\"#\">Das Objekt wird von '"
                    + mcrObj.getService().getFlags("editedby").get(0) + "' bearbeitet.</a>");
            }
        } else {
            XPathFactory xpFactory = XPathFactory.instance();
            Document doc = mcrObj.createXML();

            Map<String, String> modeChecks = MCRConfiguration2.getSubPropertiesMap("MCR.Workflow.RetrieveMode.");

            for (Map.Entry<String, String> entry : modeChecks.entrySet()) {
                String mode = entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1);
                String role = mode + "-" + mcrObjID.getTypeId();
                if (MCRUserManager.getCurrentUser().isUserInRole(role)) {
                    XPathExpression<Object> xpCheck = xpFactory.compile(entry.getValue(), Filters.fpassthrough(), null,
                        MCRConstants.MODS_NAMESPACE);
                    if (xpCheck.evaluateFirst(doc) != null) {
                        String label = MCRCategoryDAOFactory.getInstance()
                            .getCategory(new MCRCategoryID("mcr-roles", role), 0)
                            .getCurrentLabel()
                            .orElse(new MCRLabel(MCRSessionMgr.getCurrentSession().getLocale().getLanguage(),
                                "??" + role + "??", ""))
                            .getText();
                        out.append("    <a class=\"dropdown-item\" href=\"" + MCRFrontendUtil.getBaseURL()
                            + "do/startedit?mcrid=" + mcrid + "&mode=" + mode + "\">" + MCRTranslation.translate("WF.menu.edit.action", label)+ "</a>");
                    }

                }
            }
        }
        out.append("  </div>");
        out.append("</div>");
    }

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

}
