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

package org.mycore.jspdocportal.ir.thumbnail;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.iview2.events.MCRImageTileEventHandler;

/**
 * Wrapper für MyCoRe MCRImageTileEventHandler
 * that catches Exceptions.
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPImageTileEventhandler extends MCRImageTileEventHandler {
    //used because of Exception:
    // java.lang.NullPointerException: Cannot invoke "org.mycore.iview2.services.MCRTilingQueue.remove(String, String)" because the return value of "org.mycore.iview2.services.MCRTilingQueue.getInstance()" is null
    //       at org.mycore.iview2.frontend.MCRIView2Commands.deleteImageTiles(MCRIView2Commands.java:408)
    //       at org.mycore.iview2.events.MCRImageTileEventHandler.handlePathDeleted(MCRImageTileEventHandler.java:66)
    //       at org.mycore.common.events.MCREventHandlerBase.doHandleEvent(MCREventHandlerBase.java:110)
    //       at org.mycore.common.events.MCREventManager.handleEvent(MCREventManager.java:153)
    //       ... 22 more

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void handlePathCreated(MCREvent evt, Path file, BasicFileAttributes attrs) {
        try {
            super.handlePathCreated(evt, file, attrs);
        } catch (Exception e) {
            LOGGER.warn("Error on TileEvent(created): {}: {}", () -> e.getClass().getName(), () -> e.getMessage());
        }
    }

    @Override
    public void handlePathDeleted(MCREvent evt, Path file, BasicFileAttributes attrs) {
        try {
            super.handlePathDeleted(evt, file, attrs);
        } catch (Exception e) {
            LOGGER.warn("Error on TileEvent(deleted): {}, {}", () -> e.getClass().getName(), () -> e.getMessage());
        }
    }

}
