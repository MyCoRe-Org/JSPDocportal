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

package org.mycore.jspdocportal.ir.commands;

import java.util.Iterator;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.jspdocportal.common.controller.workspace.MCRMODSCatalogService;

/**
 * This class provides a set of commands specific to the JSPDocportal IR Module
 * 
 * @author Robert Stephan
 * 
 * @version $Revision$ $Date$
 */

@MCRCommandGroup(name = "JSPDocportal IR Commands")
public class MCRJSPDocportalIRCommands extends MCRAbstractCommands {
    /** The logger */
    private static Logger LOGGER = LogManager.getLogger(MCRJSPDocportalIRCommands.class);

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static XPathExpression<Element> XP_PPN = XPathFactory.instance().compile(
        "./mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']",
        Filters.element(), null, MODS_NAMESPACE);

    private static MCRMODSCatalogService MODS_CATALOG_SERVICE = MCRConfiguration2
        .getInstanceOf(MCRMODSCatalogService.class, "MCR.Workflow.MODSCatalogService.class").orElse(null);

    /**
    * Update the MODS metadata of the given object with data from catalog
    *   
    */
    @MCRCommand(syntax = "update mods in object {0}",
        help = "This command updates the MODS metadata from actual PICA data from catalog")
    public static final void updateModsInObject(String objectID) {
        try {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objectID));

            MCRMetaElement mcrMetaElement = mcrObj.getMetadata().getMetadataElement("def.modsContainer");
            Iterator<MCRMetaInterface> it = mcrMetaElement.iterator();
            while (it.hasNext()) {
                MCRMetaXML xml = (MCRMetaXML) it.next();
                if ("imported".equals(xml.getType())) {
                    //Element eOldMODS = (Element)xml.getContent().get(0);
                    Optional<Element> oldMODS = xml.getContent().stream().filter(Element.class::isInstance)
                        .map(Element.class::cast).findFirst();
                    if (oldMODS.isPresent()) {
                        Element ePPN = XP_PPN.evaluateFirst(oldMODS.get());
                        if (ePPN != null) {
                            //retrieve MODS by PPN
                            String query = "pica.ppn=" + ePPN.getTextTrim();
                            Element newMODS = MODS_CATALOG_SERVICE.retrieveMODSFromCatalogue(query);
                            if (newMODS != null) {
                                xml.getContent().clear();
                                xml.getContent().add(newMODS);
                                MCRMetadataManager.update(mcrObj);
                                LOGGER.info("MODS updated in " + objectID);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("MODS Update Exception", e);
        }
        LOGGER.error("MODS updated FAILED in " + objectID);
    }
}
