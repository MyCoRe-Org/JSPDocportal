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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrCoreManager;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * This is duplicated code from org.mycore.restapi.MCRNormalizeMCRObjectIDsFilter
 * I will clean that up later and create a configurable instance
 * The SOLR implementation can than be moved to the mycore-solr module. to reduce dependencies between modules
 * This will reduce compile dependencies between modules. 
 * A minimal default implementation will go into mycore-base.
 * Idea for name: MCRObjectIDDetector / MCRDefaultObjectIDDetector / MCRSOLRObjecIDDetector;
 * 
 * TODO: Migrate / Rename Properties (via deprecated properties)
 * 
 * @author Robert Stephan
 *
 */

public class MCRTemporaryObjectIDNormalizer {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Set<String> SEARCHKEYS_FOR_OBJECTS = MCRConfiguration2
        .getString("MCR.RestAPI.V2.AlternativeIdentifier.Objects.Keys").stream()
        .flatMap(MCRConfiguration2::splitValue).collect(Collectors.toSet());

    private static Set<String> SEARCHKEYS_FOR_DERIVATES = MCRConfiguration2
        .getString("MCR.RestAPI.V2.AlternativeIdentifier.Derivates.Keys").stream()
        .flatMap(MCRConfiguration2::splitValue).collect(Collectors.toSet());

    public static MCRObjectID retrieveMCRDerIDfromSOLR(MCRObjectID mcrObjId, String derid) {
        String result = derid;
        if (derid.contains(":") && !SEARCHKEYS_FOR_DERIVATES.isEmpty()) {
            String key = derid.substring(0, derid.indexOf(":"));
            String value = derid.substring(derid.indexOf(":") + 1);
            if (SEARCHKEYS_FOR_DERIVATES.contains(key)) {
                ModifiableSolrParams params = new ModifiableSolrParams();
                params.set("start", 0);
                params.set("rows", 1);
                params.set("fl", "id");
                params.set("fq", "objectKind:mycorederivate");
                params.set("fq", "returnId:" + mcrObjId.toString());
                params.set("q", key + ":" + ClientUtils.escapeQueryChars(value));
                params.set("sort", "derivateOrder asc");
                QueryResponse solrResponse = null;
                try {
                    solrResponse = MCRSolrCoreManager.getMainSolrClient().query(params);
                } catch (Exception e) {
                    LOGGER.error("Error retrieving derivate id from SOLR", e);
                }
                if (solrResponse != null) {
                    SolrDocumentList solrResults = solrResponse.getResults();
                    if (solrResults.getNumFound() == 1) {
                        result = String.valueOf(solrResults.get(0).getFieldValue("id"));
                    }
                    if (solrResults.getNumFound() == 0) {
                        throw new NotFoundException("No MyCoRe Derivate ID found for query " + derid);
                    }
                    if (solrResults.getNumFound() > 1) {
                        throw new BadRequestException(
                            "The query " + derid + " does not return a unique MyCoRe Derivate ID");
                    }
                }
            }
        }
        if (MCRObjectID.isValid(result)) {
            return MCRObjectID.getInstance(result);
        }
        return null;
    }

    public static MCRObjectID retrieveMCRObjIDfromSOLR(String mcrid) {
        String result = mcrid;
        if (mcrid.contains(":") && !SEARCHKEYS_FOR_OBJECTS.isEmpty()) {
            String key = mcrid.substring(0, mcrid.indexOf(":"));
            String value = URLDecoder.decode(mcrid.substring(mcrid.indexOf(":") + 1), StandardCharsets.UTF_8);
            if (SEARCHKEYS_FOR_OBJECTS.contains(key)) {
                ModifiableSolrParams params = new ModifiableSolrParams();
                params.set("start", 0);
                params.set("rows", 1);
                params.set("fl", "id");
                params.set("fq", "objectKind:mycoreobject");
                params.set("q", key + ":" + ClientUtils.escapeQueryChars(value));
                QueryResponse solrResponse = null;
                try {
                    solrResponse = MCRSolrCoreManager.getMainSolrClient().query(params);
                } catch (Exception e) {
                    LOGGER.error("Error retrieving object id from SOLR", e);
                }
                if (solrResponse != null) {
                    SolrDocumentList solrResults = solrResponse.getResults();
                    if (solrResults.getNumFound() == 1) {
                        result = String.valueOf(solrResults.get(0).getFieldValue("id"));
                    }
                    if (solrResults.getNumFound() == 0) {
                        throw new NotFoundException("No MyCoRe ID found for query " + mcrid);
                    }
                    if (solrResults.getNumFound() > 1) {
                        throw new BadRequestException("The query " + mcrid + " does not return a unique MyCoRe ID");
                    }
                }
            }
        }
        if (MCRObjectID.isValid(result)) {
            return MCRObjectID.getInstance(result);
        }
        return null;
    }
}
