/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
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
 * along with MyCoRe.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mycore.indexing.sitelinks;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.config.annotation.MCRConfigurationProxy;
import org.mycore.common.config.annotation.MCRProperty;
import org.mycore.indexing.sitelinks.dto.MCRSitelinksLinkObject;
import org.mycore.solr.MCRSolrIndexRegistryManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

/**
 * Solr-based implementation of {@link MCRSitelinksMetadataService}.
 * <p>
 * This implementation queries Solr to retrieve object metadata for sitelinks generation.
 * It expects the following Solr fields to be present:
 * <ul>
 *   <li>{@code mods.yearIssued} - the publication year, used for faceting and filtering</li>
 *   <li>{@code mods.dateIssued} - the full publication date (format: {@code yyyy-MM-dd} or {@code yyyy}),
 *       used for sorting</li>
 *   <li>{@code created} - the object creation timestamp, used as a fallback sort field</li>
 *   <li>{@code id} - the unique object identifier</li>
 * </ul>
 */
@MCRConfigurationProxy(proxyClass = MCRSitelinksSolrMetadataService.Factory.class)
public class MCRSitelinksSolrMetadataService implements MCRSitelinksMetadataService {

    private static final String FIELD_ID =
        MCRConfiguration2.getStringOrThrow("MCR.Sitelinks.SolrField.Id");

    private static final String FIELD_FULLTEXT_URL =
        MCRConfiguration2.getStringOrThrow("MCR.Sitelinks.SolrField.FulltextUrl");

    private static final String FIELD_FACETING =
        MCRConfiguration2.getStringOrThrow("MCR.Sitelinks.SolrField.Faceting");

    private static final String FIELD_SORTING =
        MCRConfiguration2.getStringOrThrow("MCR.Sitelinks.SolrField.Sorting");

    private static final String DEFAULT_SOLR_QUERY = "*:*";

    private final SolrClient solrClient;

    private final MCRSolrAuthenticationManager authenticationManager;

    private final String filterQuery;

    private final String requestHandler;

    /**
     * Constructs a new service instance using the main Solr client.
     *
     * @param filterQuery a Solr filter query applied to all queries (e.g., {@code worldReadable:true})
     * @param requestHandler the Solr request handler path to query (e.g., {@code /select})
     */
    public MCRSitelinksSolrMetadataService(String filterQuery, String requestHandler) {
        SolrClient client = MCRSolrIndexRegistryManager.obtainRegistry()
            .getIndex("main")
            .orElseThrow(() -> new MCRConfigurationException("Solr index 'main' is not configured"))
            .getClient();
        this(client, MCRSolrAuthenticationManager.obtainInstance(), filterQuery,
            requestHandler);
    }

    /**
     * Constructs a new service instance with a custom Solr client.
     *
     * @param solrClient the Solr client to use for queries
     * @param authenticationManager the authentication manager used to authenticate queries against Solr,
     *        applied at {@link MCRSolrAuthenticationLevel#SEARCH} level
     * @param filterQuery a Solr filter query applied to all queries (e.g., {@code worldReadable:true})
     * @param requestHandler the Solr request handler path to query (e.g., {@code /select})
     */
    public MCRSitelinksSolrMetadataService(SolrClient solrClient, MCRSolrAuthenticationManager authenticationManager,
        String filterQuery, String requestHandler) {
        this.solrClient = solrClient;
        this.authenticationManager = authenticationManager;
        this.filterQuery = filterQuery;
        this.requestHandler = requestHandler;
    }

    @Override
    public List<Integer> getYearsWithObjects() {
        final SolrQuery query = new SolrQuery(DEFAULT_SOLR_QUERY);
        query.setRows(0);
        query.addFilterQuery(filterQuery);
        query.setFacet(true);
        query.addFacetField(FIELD_FACETING);
        query.setFacetSort(FacetParams.FACET_SORT_INDEX);
        query.setFacetLimit(-1);
        try {
            final QueryResponse response = getRequest(query).process(solrClient);
            return response.getFacetField(FIELD_FACETING).getValues()
                .stream().map(FacetField.Count::getName).map(Integer::parseInt).toList();
        } catch (SolrServerException | IOException e) {
            throw new MCRException(e);
        }
    }

    @Override
    public LinkObjectsWithCount getObjectIdsByYear(int year, int offset, int limit) {
        final SolrQuery query = new SolrQuery(DEFAULT_SOLR_QUERY);
        query.addFilterQuery(filterQuery);
        query.addFilterQuery(String.format(Locale.ROOT, FIELD_FACETING + ":%s", year));
        query.setFields(FIELD_ID, FIELD_FULLTEXT_URL);
        query.setStart(offset);
        query.setRows(limit);
        query.addSort(FIELD_SORTING, SolrQuery.ORDER.desc);
        query.addSort(FIELD_ID, SolrQuery.ORDER.desc);
        try {
            final QueryResponse response = getRequest(query).process(solrClient);
            long totalCount = response.getResults().getNumFound();
            final List<MCRSitelinksLinkObject> objectIds =
                response.getResults().stream()
                    .map((document) -> new MCRSitelinksLinkObject((String) document.getFieldValue(FIELD_ID),
                        (String) document.getFieldValue(FIELD_FULLTEXT_URL)))
                    .toList();
            return new LinkObjectsWithCount(objectIds, totalCount);
        } catch (SolrServerException | IOException e) {
            throw new MCRException(e);
        }
    }

    private QueryRequest getRequest(SolrQuery query) {
        final QueryRequest request = new QueryRequest(query);
        request.setPath(requestHandler);
        authenticationManager.applyAuthentication(request, MCRSolrAuthenticationLevel.SEARCH);
        return request;
    }

    /**
     * Factory class for creating {@link MCRSitelinksSolrMetadataService} instances via configuration.
     * <p>
     * This factory is used by the {@link MCRConfigurationProxy} annotation to automatically
     * instantiate the service with configuration values from properties.
     */
    public static class Factory implements Supplier<MCRSitelinksSolrMetadataService> {

        /**
         * The Solr filter query configured via {@code .FilterQuery} property.
         */
        @MCRProperty(name = "FilterQuery")
        public String filterQuery;

        /**
         * The Solr request handler path configured via {@code .RequestHandler} property.
         */
        @MCRProperty(name = "RequestHandler")
        public String requestHandler;

        @Override
        public MCRSitelinksSolrMetadataService get() {
            return new MCRSitelinksSolrMetadataService(filterQuery, requestHandler);
        }
    }
}
