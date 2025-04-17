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

package org.mycore.jspdocportal.diskcache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.idmapper.MCRIDMapper;
import org.xml.sax.InputSource;

public class MCRDiskcacheURIResolver implements URIResolver {

    private MCRIDMapper mcrIdMapper =
        MCRConfiguration2.getInstanceOf(MCRIDMapper.class, MCRIDMapper.MCR_PROPERTY_CLASS).get();

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] params = href.split(":");

        if (params.length != 3) {
            throw new TransformerException("Invalid href: " + href);
        }

        String cacheId = params[1];
        Map<String, MCRDiskcacheConfig> caches = MCRDiskcacheManager.getInstance().getCaches();
        if (caches.containsKey(cacheId)) {
            MCRDiskcacheConfig cache = caches.get(cacheId);
            Optional<MCRObjectID> oMcrObjId = mcrIdMapper.mapMCRObjectID(params[2]);
            String objId = oMcrObjId.map(MCRObjectID::toString).orElse(params[2]);
            Path file = MCRDiskcacheManager.getInstance().retrieveCachedFile(cache.getId(), objId);
            try {
                return new SAXSource(new InputSource(Files.newBufferedReader(file)));
            } catch (IOException e) {
                //ignore - throw TransformerException
            }
        }
        throw new TransformerException("Invalid call of diskcache: " + href);
    }

}
