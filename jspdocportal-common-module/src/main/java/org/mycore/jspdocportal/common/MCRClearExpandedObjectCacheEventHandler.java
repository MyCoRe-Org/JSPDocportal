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
package org.mycore.jspdocportal.common;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRExpandedObjectCache;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

/**
 * Eventhandler, that clears the Expanded Object Cache of the MyCoRe object
 * the modified file belongs to.
 * 
 * This is necessary to update the EnrichedDerivateLinkMetadata (size + checksum of maindoc).
 */
public class MCRClearExpandedObjectCacheEventHandler extends MCREventHandlerBase {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handleFileChanged(path, attrs);
    }

    @Override
    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handleFileChanged(path, attrs);
    }

    @Override
    protected void handlePathRepaired(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handleFileChanged(path, attrs);
    }

    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handleFileChanged(path, attrs);
    }

    private void handleFileChanged(Path path, BasicFileAttributes attrs) {
        if (attrs != null && attrs.isDirectory()) {
            return;
        }
        MCRObjectID derivateID = MCRObjectID.getInstance(MCRPath.ofPath(path).getOwner());
        if (!MCRMetadataManager.exists(derivateID)) {
            LOGGER.warn("Derivate {} from file '{}' does not exist.", derivateID, path);
            return;
        }
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateID);

        MCRExpandedObjectCache.getInstance().clear(derivate.getOwnerID());
    }
}
