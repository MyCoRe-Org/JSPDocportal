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

package org.mycore.jspdocportal.ir.oai;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

/**
 * This event handler sets the service date "oai-ready" for created / modified MyCoRe Objects.
 * 
 * It should run before saving the XML in MCRMetaXMLEventHandler (id < 20)
 *
 * @author Robert Stephan
 */
public class MCRSetStableServdateEventHandler extends MCREventHandlerBase {

    public static final String SERVDATE_TYPE_STABLE = "stable";
    public static final long DAYS_IN_WAITING = 10;
    
        @Override
    protected final void handleObjectCreated(MCREvent evt, MCRObject obj) {
        if (!obj.isImportMode()) {
            handleObject(obj);
        }
    }

    @Override
    protected final void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if (!obj.isImportMode()) {
            handleObject(obj);
        }
    }

    /**
     * Updates oai-ready servdate 
     * 
     * removes all current servflags of type 'oai-ready'
     * 
     * @param obj - the MyCoRe Object
     */
    protected static void handleObject(MCRObject obj) {
        obj.getService().setDate(SERVDATE_TYPE_STABLE, Date.from(Instant.now().plus(DAYS_IN_WAITING, ChronoUnit.DAYS)));
    }
}
