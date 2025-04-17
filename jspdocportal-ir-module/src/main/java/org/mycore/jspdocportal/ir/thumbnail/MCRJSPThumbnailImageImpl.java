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

package org.mycore.jspdocportal.ir.thumbnail;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.iview2.iiif.MCRThumbnailImageImpl;
import org.mycore.jspdocportal.ir.pi.local.MCRLocalID;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.MCRPIRegistrationInfo;

/**
 * Calculates the TileInfo for the thumbnail image from RecordIdentifier or MyCoRe Object ID
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPThumbnailImageImpl extends MCRThumbnailImageImpl {

    public MCRJSPThumbnailImageImpl(String implName) {
        super(implName);
    }

    @Override
    protected Optional<MCRObjectID> calculateMCRObjectID(String id) {
        if (MCRObjectID.isValid(id)) {
            return Optional.of(MCRObjectID.getInstance(id));
        }
        //we assume it's a RecordIdentifier
        String recordId = URLDecoder.decode(URLDecoder.decode(id, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        if (recordId.contains("/")) {
            recordId = recordId.replace("/", "_");
        }

        Optional<MCRPIRegistrationInfo> oPiInfo = MCRPIManager.getInstance().getInfo(recordId, MCRLocalID.TYPE);
        return oPiInfo.map(MCRPIRegistrationInfo::getMycoreID).map(MCRObjectID::getInstance);
    }
}
