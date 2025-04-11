
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

package org.mycore.jspdocportal.common.api.explore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.frontend.jersey.MCRCacheControl;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.annotation.XmlElementWrapper;

/**
 * This RestAPI endpoint provides simple access to the underlying SOLR server.
 * It supports pagination, sorting and filtering.
 * You can configure a global filter query and define solr fields, 
 * which can be delivered as payload in the response.
 * 
 * @author Robert Stephan
 *
 */
@Path("/explore")
@OpenAPIDefinition(tags = {})
public class MCRAPIExplore {
    public static final int MAX_ROWS = 1000;

    @Context
    Request request;

    @Context
    ServletContext context;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON + ";charset=UTF-8" })
    @MCRCacheControl(maxAge = @MCRCacheControl.Age(time = 1, unit = TimeUnit.HOURS), sMaxAge = @MCRCacheControl.Age(time = 1, unit = TimeUnit.HOURS))
    /*
     * @Operation( summary = "Explore objects in this repository", responses
     * = @ApiResponse( content = @Content(array = @ArraySchema(schema
     * = @Schema(implementation = MCRObjectIDDate.class)))), tags =
     * MCRRestUtils.TAG_MYCORE_OBJECT)
     */
    @XmlElementWrapper(name = "mycoreobjects")
    public Response exploreObjects(@QueryParam("start") String start, @QueryParam("rows") String rows,
            @QueryParam("sort") String sort, @QueryParam("filter") List<String> filter) throws IOException {
        Date lastModified = new Date(MCRXMLMetadataManager.getInstance().getLastModified());

        // enable on 2020 LTS
        // if (cachedResponse.isPresent()) {
        //    return cachedResponse.get();
        //}
        MCRAPIExploreResponse response = new MCRAPIExploreResponse();

        SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
        SolrQuery q = new SolrQuery("*:*");
        MCRConfiguration2.getString("MCR.API.Explore.FilterQuery").ifPresent(fq -> {
            q.addFilterQuery(fq);
        });
        processStartParam(start, q);
        processRowsParam(rows, q);
        processSortParam(sort, q);
        processFiltersParam(filter, q);

        try {
            QueryRequest queryRequest = new QueryRequest(q);
            MCRSolrAuthenticationManager.obtainInstance().applyAuthentication(queryRequest,
                MCRSolrAuthenticationLevel.SEARCH);
            QueryResponse solrResponse = queryRequest.process(solrClient);
            response.getHeader().setRows(q.getRows() == null ? 10 : q.getRows());
            SolrDocumentList solrResults = solrResponse.getResults();
            response.getHeader().setStart(solrResults.getStart());
            response.getHeader().setNumFound(solrResults.getNumFound());
            if (!q.getSorts().isEmpty()) {
                response.getHeader().setSort(String.join(",", q.getSorts().stream()
                        .map(x -> x.getItem() + " " + x.getOrder().name()).collect(Collectors.toList())));
            }
            for (int i = 0; i < solrResults.size(); ++i) {
                SolrDocument solrDoc = solrResults.get(i);
                response.getData().add(createResponseObject(solrDoc));
            }

        } catch (SolrServerException | IOException e) {
            throw new InternalServerErrorException(e);
        }

        return Response.ok(response).lastModified(lastModified).build();
    }

    private MCRAPIExploreResponseObject createResponseObject(SolrDocument solrDoc) {
        Date dModified = (Date) solrDoc.getFieldValue("modified");
        MCRAPIExploreResponseObject responseObj = new MCRAPIExploreResponseObject(
                String.valueOf(solrDoc.getFieldValue("id")), dModified.toInstant());

        MCRConfiguration2.getString("MCR.API.Explore.PayloadFields").ifPresent(fields -> {
            for (String field : fields.split(",")) {
                Object value = solrDoc.getFieldValue(field);
                if (value != null) {
                    if (value instanceof List) {
                        for (Object o : (List<?>) value) {
                            responseObj.addPayload(field, o);
                        }
                    } else {
                        responseObj.addPayload(field, value);
                    }
                }
            }
        });
        return responseObj;
    }

    private void processFiltersParam(List<String> filter, SolrQuery q) {
        if (filter != null) {
            for (String f : filter) {
                q.addFilterQuery(f.trim());
            }
        }
    }

    private void processSortParam(String sort, SolrQuery q) {
        if (sort != null) {
            Arrays.stream(sort.split(",")).map(String::trim).forEach(s -> {
                if (s.toLowerCase(Locale.getDefault()).endsWith(" asc")) {
                    q.addSort(s.substring(0, s.length() - 4).trim(), SolrQuery.ORDER.asc);

                } else if (s.toLowerCase(Locale.getDefault()).endsWith(" desc")) {
                    q.addSort(s.substring(0, s.length() - 5).trim(), SolrQuery.ORDER.desc);
                } else {
                    q.addSort(s.trim(), SolrQuery.ORDER.asc);
                }
            });
        }
    }

    private void processRowsParam(String rows, SolrQuery q) {
        if (rows != null) {
            int r = 0;
            try {
                r = Integer.parseInt(rows);
                if (r < 0) {
                    r = 0;
                }
                if (r > MAX_ROWS) {
                    r = MAX_ROWS;
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
            q.setRows(r);
        }
    }

    private void processStartParam(String start, SolrQuery q) {
        if (start != null) {
            int s = 0;
            try {
                s = Integer.parseInt(start);
                if (s < 0) {
                    s = 0;
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
            q.setStart(s);
        }
    }
}
