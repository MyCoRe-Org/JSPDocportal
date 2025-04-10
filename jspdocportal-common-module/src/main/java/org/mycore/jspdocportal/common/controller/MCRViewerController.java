package org.mycore.jspdocportal.common.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * Controller for opening the MyCoRe Image Viewer
 * 
 * @author Robert Stephan
 *
 */
@Path("/mcrviewer")
public class MCRViewerController {
    private static final Logger LOGGER = LogManager.getLogger();

    @GET
    @Path("/{field}/{identifier}")
    public Response doGetFirstImage(@PathParam("field") String field, @PathParam("identifier") String identifier_,
        @DefaultValue("") @QueryParam("_mcrviewer_start") String startPage,
        @Context HttpServletRequest request) {
        if ("".equals(startPage)) {
            return doGetWithFile(field, identifier_, "", request);
        } else {
            return doGetWithFile(field, identifier_, "iview2/" + startPage + ".iview2", request);
        }
    }

    @GET
    @Path("/{field}/{identifier}/{filePath:.*}")
    public Response doGetWithFile(@PathParam("field") String field, @PathParam("identifier") String identifier_,
        @PathParam("filePath") String filePath, @Context HttpServletRequest request) {

        String identifier = URLDecoder.decode(URLDecoder.decode(identifier_, StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
        Map<String, Object> model = new HashMap<>();
        model.put("field", field);
        model.put("identifier", identifier);
        if (!StringUtils.isEmpty(filePath)) {
            model.put("filePath", filePath);
        }

        Viewable v = new Viewable("/mcrviewer", model);

        SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
        String value = identifier;
        if ("recordIdentifier".equals(field) && value.contains("/")) {
            value = value.replaceFirst("/", "_");
        }
        SolrQuery solrQuery = new SolrQuery(field + ":" + ClientUtils.escapeQueryChars(value));
        solrQuery.setRows(1);

        try {
            QueryRequest queryRequest = new QueryRequest(solrQuery);
            MCRSolrAuthenticationManager.getInstance().applyAuthentication(queryRequest,
                MCRSolrAuthenticationLevel.SEARCH);
            QueryResponse solrResponse = queryRequest.process(solrClient);
            SolrDocumentList solrResults = solrResponse.getResults();
            if (solrResults.size() > 0) {
                SolrDocument solrDoc = solrResults.get(0);
                model.put("recordIdentifier", String.valueOf(solrDoc.getFieldValue("recordIdentifier")));
                model.put("mcrid", String.valueOf(solrDoc.getFieldValue("returnId")));
                if (solrDoc.getFieldNames().contains("ir.pdffulltext_url")) {
                    model.put("doctype", "pdf");
                    String pdfProviderURL = String.valueOf(solrDoc.getFieldValue("ir.pdffulltext_url"));
                    model.put("pdfProviderURL", pdfProviderURL);
                    if (!model.containsKey("filePath")) {
                        model.put("filePath", pdfProviderURL.substring(pdfProviderURL.lastIndexOf("/") + 1));
                    }
                } else {
                    model.put("doctype", "mets");
                    if (!model.containsKey("filePath")) {
                        model.put("filePath", StringUtils.isEmpty(filePath) ? "iview2/phys_0001.iview2" : filePath);
                    }
                }
            }
        } catch (SolrServerException | IOException e) {
            LOGGER.error(e);
        }

        //this should happen automatically (the parameters should be available in view)
        //add viewer request paramter to link a specific view port
        //http://localhost:8080/rosdok/mcrviewer/recordIdentifier/rosdok_ppn888171862/iview2/phys_0005.iview2
        //?x=1065.7192494788046&y=1139.3954134815867&scale=2.3209677419354837&rotation=0&layout=singlePageLayout

        /*
        for(String p: StringUtils.split("x y scale rotation layout")) {
            if(getContext().getRequest().getParameterMap().containsKey(p)) {
                fwdResolutionForm.addParameter(p, getContext().getRequest().getParameter(p));
            }
        }
        */

        return Response.ok(v).build();
    }
}
