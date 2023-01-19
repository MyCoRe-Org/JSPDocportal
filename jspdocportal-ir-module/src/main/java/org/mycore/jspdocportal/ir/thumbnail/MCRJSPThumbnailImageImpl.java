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

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.iiif.image.impl.MCRIIIFImageNotFoundException;
import org.mycore.iview2.backend.MCRTileInfo;
import org.mycore.iview2.iiif.MCRThumbnailImageImpl;
import org.mycore.solr.MCRSolrClientFactory;

/**
 * Calculates the TileInfo for the thumbnail image from RecordIdentifier or MyCoRe Object ID
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPThumbnailImageImpl extends MCRThumbnailImageImpl {
    Logger LOGGER = LogManager.getLogger();
    
    public MCRJSPThumbnailImageImpl(String implName) {
        super(implName);
    }

    @Override
    protected MCRTileInfo createTileInfo(String id) throws MCRIIIFImageNotFoundException {
        Optional<MCRObjectID> oMcrID = calculateMcrIDFromInput(id);
        if (oMcrID.isPresent()) {
            return super.createTileInfo(oMcrID.toString());
        }
        throw new MCRIIIFImageNotFoundException(id);
    }

    @Override
    protected boolean checkPermission(String identifier, MCRTileInfo tileInfo) {
        Optional<MCRObjectID> mcrID = calculateMcrIDFromInput(identifier);
        if (mcrID.isPresent()) {
            return super.checkPermission(mcrID.toString(), tileInfo);
        } else {
            return false;
        }
    }
    
    private Optional<MCRObjectID> calculateMcrIDFromInput(String id) {
        if (MCRObjectID.isValid(id)) {
            return Optional.of(MCRObjectID.getInstance(id));
        }
        //we assume it's a RecordIdentifier
        String recordId = URLDecoder.decode(URLDecoder.decode(id, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        if (!recordId.contains("/")) {
            recordId = recordId.replaceFirst("_", "/");
        }

        //TODO: Use PI component to retrieve MyCoRe ID for RecordIdentifier instead
        //      or use some caching
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery solrQuery = new SolrQuery("recordIdentifier:" + ClientUtils.escapeQueryChars(recordId));
        solrQuery.setRows(1);
        try {
            QueryResponse solrQueryResponse = solrClient.query(solrQuery);
            SolrDocumentList solrResults = solrQueryResponse.getResults();
            if (solrResults.getNumFound() > 0) {
                MCRObjectID mcrID = MCRObjectID
                    .getInstance(String.valueOf(solrResults.get(0).getFirstValue("returnId")));
                return Optional.of(mcrID);
            }
        } catch (IOException | SolrServerException e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }
}
