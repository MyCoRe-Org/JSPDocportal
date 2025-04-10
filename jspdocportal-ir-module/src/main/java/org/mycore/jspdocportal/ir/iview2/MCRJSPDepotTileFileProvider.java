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
package org.mycore.jspdocportal.ir.iview2;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.backend.MCRTileFileProvider;
import org.mycore.iview2.backend.MCRTileInfo;

/**
 * Calculate the path for a specific tile of an image.
 * IIIF-Image-API URL in MyCoRe 2020.06 LTS:
 * http://localhost:8880/rosdok/api/iiif/image/v2/rosdok_ppn818585579%252Fphys_0003/full/full/0/default.jpg
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPDepotTileFileProvider implements MCRTileFileProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    // from Apache Configuration ...
    // AliasMatch "^/depot/demel[_/]([a-z0-9]+)(/.*)$"
    // "/storage/digibib/depot/demel/$1$2"
    // AliasMatch "^/depot/wossidia[_/](.{3})(.{5})(.{3})(/.*)$"
    // "/storage/digibib/depot/wossidia/$1/$1$2/$1$2$3$4"
    // AliasMatch "^/depot/ipac[_/]([a-z]+)[_]([a-z0-9]*)(/.*)$"
    // "/storage/digibib/depot/ipac/$1/$1_$2$3"
    // AliasMatch "^/depot/ipac[_/]([a-z]+)(/.*)$"
    // "/storage/digibib/depot/ipac/$1/$1$2"
    // AliasMatch "^/depot/darl[_/]rlbtext([0-9]{2})(/.*)$"
    // "/storage/digibib/doro/depot/darl/rlbtext$1/rlbtext$1$2"
    // AliasMatch "^/depot/darl[_/]([a-f0-9]{2})([a-f0-9]{3})(.{31})(/.*)$"
    // "/storage/digibib/doro/depot/darl/$1/$1$2/$1$2$3$4"
    // AliasMatch "^/depot/darl%2Frlbtext([0-9]{2})(/.*)$"
    // "/storage/digibib/doro/depot/darl/rlbtext$1/rlbtext$1$2"
    // AliasMatch "^/depot/darl%2F([a-f0-9]{2})([a-f0-9]{3})(.{31})(/.*)$"
    // "/storage/digibib/doro/depot/darl/$1/$1$2/$1$2$3$4"

    public static final String[][] DIRECTORY_PATTERN_REGEX = {
        { "^rosdok[_/](.*)(.{3})(.{4})$", "rosdok/$1/$1$2/$1$2$3" },
        { "^wossidia[_/](.{3})(.{5})(.{3})$", "wossidia/$1/$1$2/$1$2$3" },
        { "^ipac[_/]([a-z]+)[_]([a-z0-9]*)$", "ipac/$1/$1_$2" },
        { "^ipac[_/]([a-z]+)$", "ipac/$1/$1" }
    };

    public static final Map<String, Pattern> DIRECTORY_PATTERNS = new ConcurrentHashMap<>();
    static {
        for (String[] s : DIRECTORY_PATTERN_REGEX) {
            DIRECTORY_PATTERNS.put(s[0], Pattern.compile(s[0]));
        }
    }

    @Override
    public Optional<Path> getTileFile(MCRTileInfo tileInfo) {
        try {
            String recordIdentifier;
            String imagePath;
            if (!StringUtils.isEmpty(tileInfo.derivate())) {
                recordIdentifier = URLDecoder
                    .decode(URLDecoder.decode(tileInfo.derivate(), StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                    .replace("..", "");
                imagePath = "iview2/" + tileInfo.imagePath() + ".iview2";
            } else {
                //fallback for image ids like: rosdok%252Fppn642329060%252Fphys_0002
                String normalizedIdentifier = URLDecoder
                    .decode(URLDecoder.decode(tileInfo.imagePath(), StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                    .replace("..", "");

                if (!normalizedIdentifier.contains("/")) {
                    normalizedIdentifier = normalizedIdentifier.replace("_phys", "/phys");
                }
                if (!normalizedIdentifier.contains("/")) {
                    return Optional.empty();
                }

                recordIdentifier = normalizedIdentifier.substring(0, normalizedIdentifier.lastIndexOf('/'));
                imagePath = normalizedIdentifier.substring(normalizedIdentifier.lastIndexOf('/') + 1);
                if (imagePath.contains(".")) {
                    imagePath = imagePath.substring(0, imagePath.lastIndexOf('.'));
                }

                imagePath = "iview2/" + imagePath + ".iview2";
            }

            Path subPath = retrieveDepotDir(recordIdentifier);
            if (subPath != null) {
                return Optional.of(MCRImage.getTiledFile(subPath, ".", imagePath));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    public static Path retrieveDepotDir(String recordIdentifier) {
        for (String[] s : DIRECTORY_PATTERN_REGEX) {
            Pattern p = DIRECTORY_PATTERNS.get(s[0]);
            Matcher m = p.matcher(recordIdentifier);
            if (m.matches()) {
                String subPath = s[1];
                for (int i = 1; i < 10; i++) {
                    if (i <= m.groupCount() && subPath.contains("$" + i)) {
                        subPath = subPath.replace("$" + i, m.group(i));
                    }
                }
                Path depotDir = Paths.get(MCRConfiguration2.getStringOrThrow("MCR.depotdir"));
                return depotDir.resolve(subPath);
            }
        }
        return null;
    }
}
