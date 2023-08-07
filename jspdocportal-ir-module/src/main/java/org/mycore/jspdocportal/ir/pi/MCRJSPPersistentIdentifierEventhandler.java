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
package org.mycore.jspdocportal.ir.pi;

import org.mycore.common.events.MCREvent;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.MCRPersistentIdentifierEventHandler;

/**
 * Wrapper for MCRPersistentIdentifierEventHandler
 * which disabled the execution if the object is in import mode.
 * In import mode it will only update the flags in database, but not register any PI.
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPPersistentIdentifierEventhandler extends MCRPersistentIdentifierEventHandler{

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        if(obj.isImportMode()) {
            MCRPIService.updateFlagsInDatabase(obj);
        }
        else {
            super.handleObjectRepaired(evt, obj);
        }
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if(obj.isImportMode()) {
            MCRPIService.updateFlagsInDatabase(obj);
        }
        else {
            super.handleObjectUpdated(evt, obj);
        }
    }

    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        super.handleObjectDeleted(evt, obj);
    }
}
