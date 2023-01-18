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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.iiif.image.impl.MCRIIIFImageNotFoundException;
import org.mycore.iview2.backend.MCRTileInfo;
import org.mycore.iview2.iiif.MCRIVIEWIIIFImageImpl;
import org.mycore.solr.MCRSolrClientFactory;

/**
 * Calculates the TileInfo for the thumbnail image from RecordIdentifier or MyCoRe Object ID
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPThumbnailImageImpl extends MCRIVIEWIIIFImageImpl {

    protected static final String DERIVATE_TYPES = "Derivate.Types";

    private static final Logger LOGGER = LogManager.getLogger(MCRJSPThumbnailImageImpl.class);

    private static Set<String> derivateTypes;

    public MCRJSPThumbnailImageImpl(String implName) {
        super(implName);
        derivateTypes = new HashSet<String>();
        derivateTypes.addAll(Arrays.asList(getProperties().get(DERIVATE_TYPES).split(",")));
    }

    @Override
    protected MCRTileInfo createTileInfo(String id) throws MCRIIIFImageNotFoundException {
        Optional<MCRObjectID> oMcrID = calculateMcrIDFromInput(id);
        if (oMcrID.isPresent()) {
            return retrieveTileInfoByMcrID(oMcrID.get());
        }
        throw new MCRIIIFImageNotFoundException(id);
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

    private MCRTileInfo retrieveTileInfoByMcrID(MCRObjectID mcrID) throws MCRIIIFImageNotFoundException {
        if (mcrID.getTypeId().equals("derivate")) {
            MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrID);
            return new MCRTileInfo(mcrID.toString(), mcrDer.getDerivate().getInternals().getMainDoc(), null);
        } else {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrID);
            for (String type : derivateTypes) {
                for (MCRMetaEnrichedLinkID derLink : mcrObj.getStructure().getDerivates()) {
                    if (derLink.getClassifications().stream()
                        .map(MCRCategoryID::toString)
                        .anyMatch(type::equals)) {
                        final String maindoc = derLink.getMainDoc();
                        if (maindoc != null) {
                            final MCRTileInfo mcrTileInfo = new MCRTileInfo(derLink.getXLinkHref(), maindoc, null);
                            final Optional<Path> tileFile = this.tileFileProvider.getTileFile(mcrTileInfo);
                            if (tileFile.isPresent() && Files.exists(tileFile.get())) {
                                return mcrTileInfo;
                            }
                        }
                    }
                }
            }
        }
        throw new MCRIIIFImageNotFoundException(mcrID.toString());
    }

    @Override
    protected boolean checkPermission(String identifier, MCRTileInfo tileInfo) {
        Optional<MCRObjectID> mcrID = calculateMcrIDFromInput(identifier);
        if (mcrID.isPresent()) {
            return MCRAccessManager.checkPermission(mcrID.get().toString(), MCRAccessManager.PERMISSION_PREVIEW) ||
                MCRAccessManager.checkPermission(tileInfo.getDerivate(), MCRAccessManager.PERMISSION_VIEW) ||
                MCRAccessManager.checkPermission(tileInfo.getDerivate(), MCRAccessManager.PERMISSION_READ);
        } else {
            return false;
        }
    }
}
