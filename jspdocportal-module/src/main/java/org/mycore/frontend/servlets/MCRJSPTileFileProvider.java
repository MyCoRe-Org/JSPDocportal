/*
 * $Id: MCRTileServlet.java 30904 2014-10-20 14:22:26Z mcrtchef $
 * $Revision: 30904 $ $Date: 2014-10-20 16:22:26 +0200 (Mo, 20 Okt 2014) $
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
package org.mycore.frontend.servlets;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.mycore.common.HashedDirectoryStructure;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.frontend.servlets.tileserver.MCRTileFileProvider;
import org.mycore.imagetiler.MCRImage;

/**
 * Calculate the path for a specific tile of an image.
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPTileFileProvider implements MCRTileFileProvider {
    private static final Logger LOGGER = Logger.getLogger(MCRJSPTileFileProvider.class);
       
    public Path getTileFile(String derivate, String image){
        try{
            Path depotDir = Paths.get(MCRConfiguration.instance().getString("MCR.depotdir"));
            String recordIdentifier = URLDecoder.decode(URLDecoder.decode(derivate, "UTF-8"), "UTF-8");
            Path outputDir = HashedDirectoryStructure.createOutputDirectory(depotDir,  recordIdentifier);
            return MCRImage.getTiledFile(outputDir, null, image);
        }
        catch(MCRConfigurationException cfe){
            LOGGER.error("Property \"MCR.depotdir\" not defined!", cfe);
            return null;
        }
        catch(UnsupportedEncodingException uee){
            LOGGER.error(uee);
            return null;
        }
    }
}
