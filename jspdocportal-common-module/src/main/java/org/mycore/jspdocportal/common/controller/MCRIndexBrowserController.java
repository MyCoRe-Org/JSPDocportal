package org.mycore.jspdocportal.common.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.jspdocportal.common.search.MCRSearchResultDataBean;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/do/indexbrowser/{modus}")
public class MCRIndexBrowserController {
    private static final Logger LOGGER = LogManager.getLogger();

    private Set<String> firstSelector = new TreeSet<>();

    private Map<String, Long> secondSelector = new TreeMap<>();

    private MCRSearchResultDataBean mcrSearchResult;

    @GET
    public Response defaultRes(@PathParam("modus") String modus,
        @QueryParam("select") String select,
        @Context HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("modus", modus);
        model.put("select", select);

        Viewable v = new Viewable("/indexbrowser", model);
        try {
            String searchfield = MCRConfiguration2.getString("MCR.IndexBrowser." + modus + ".Searchfield").orElse(null);
            String facetfield = MCRConfiguration2.getString("MCR.IndexBrowser." + modus + ".Facetfield").orElse(null);
            String filterQuery = MCRConfiguration2.getString("MCR.IndexBrowser." + modus + ".FilterQuery").orElse(null);

            // set first selector
            SolrQuery q = new SolrQuery();
            q.setQuery(searchfield + ":*");
            q.addFacetField(facetfield);
            q.add("facet.limit", "-1");
            q.addSort(searchfield, ORDER.asc);
            q.setRows(0);
            q.setStart(0);

            SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();

            firstSelector.clear();
            try {
                QueryRequest queryRequest = new QueryRequest(q);
                MCRSolrAuthenticationManager.obtainInstance().applyAuthentication(queryRequest,
                    MCRSolrAuthenticationLevel.SEARCH);
                QueryResponse response = queryRequest.process(solrClient);
                for (Count c : response.getFacetFields().get(0).getValues()) {
                    if (c.getCount() > 0 && c.getName().length() > 0) {
                        firstSelector.add(c.getName().substring(0, 1));
                    }
                }
            } catch (IOException | SolrServerException e) {
                LOGGER.error(e);
            }

            if (select != null) {
                SolrQuery query = new SolrQuery();
                query.setQuery(searchfield + ":" + select + "*");
                query.addSort(searchfield, ORDER.asc);

                mcrSearchResult = new MCRSearchResultDataBean();
                mcrSearchResult.setSolrQuery(query);
                mcrSearchResult.setRows(Integer.MAX_VALUE);
                mcrSearchResult.setStart(0);
                mcrSearchResult.setAction("do/search");
                mcrSearchResult.getFacetFields().add(facetfield);
                if (filterQuery != null && filterQuery.length() > 0) {
                    mcrSearchResult.getFilterQueries().add(filterQuery);
                }

                mcrSearchResult.doSearch();
                mcrSearchResult.setBackURL(
                    request.getContextPath() + "/do/indexbrowser/" + modus + "?select=" + select);
                MCRSearchResultDataBean.addSearchresultToSession(request, mcrSearchResult);

                QueryResponse response = mcrSearchResult.getSolrQueryResponse();
                processResponse(select, response);
            }
            model.put("firstSelector", firstSelector);
            model.put("secondSelector", secondSelector);
            model.put("result", mcrSearchResult);
            return Response.ok(v).build();
        } catch (MCRConfigurationException e) {
            return Response.temporaryRedirect(URI.create(request.getContextPath() + "/")).build();
        }
    }

    private void processResponse(String select, QueryResponse response) {
        if (response != null) {
            SolrDocumentList solrResults = response.getResults();

            List<FacetField> facets = response.getFacetFields();
            secondSelector.clear();
            if (solrResults.getNumFound() > 20 || select.length() > 1) {
                for (Count c : facets.get(0).getValues()) {
                    if (c.getCount() > 0) {
                        secondSelector.put(c.getName(), c.getCount());
                    }
                }
            }
            if (solrResults.getNumFound() > 20 && select.length() <= 1) {
                // do not display entries, show 2nd selector instead
                mcrSearchResult.getEntries().clear();
            }
        }
    }
}
