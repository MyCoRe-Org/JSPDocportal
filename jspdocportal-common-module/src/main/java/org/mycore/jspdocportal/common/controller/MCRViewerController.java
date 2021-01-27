package org.mycore.jspdocportal.common.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.solr.MCRSolrClientFactory;

/**
 * Controller for opening the MyCoRe Image Viewer
 * 
 * @author Robert Stephan
 *
 */
@Path("/mcrviewer/{field}/{identifier}/{filePath}")
public class MCRViewerController {
    private static Logger LOGGER = LogManager.getLogger(MCRViewerController.class);

    @GET
    public Response doGet(@PathParam("field") String field, @PathParam("identifier") String identifier_,
        @PathParam("filePath") String filePath, @Context HttpServletRequest request) {

        String identifier = URLDecoder.decode(URLDecoder.decode(identifier_, StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
        Map<String, Object> model = new HashMap<>();
        model.put("field", field);
        model.put("identifier", identifier);
        model.put("filePath", filePath);

        Viewable v = new Viewable("/mcrviewer", model);

        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        String value = identifier;
        if ("recordIdentifier".equals(field) && !value.contains("/")) {
            value = value.replaceFirst("_", "/");
        }
        SolrQuery solrQuery = new SolrQuery(field + ":" + ClientUtils.escapeQueryChars(value));
        solrQuery.setRows(1);

        try {
            QueryResponse solrResponse = solrClient.query(solrQuery);
            SolrDocumentList solrResults = solrResponse.getResults();
            if (solrResults.size() > 0) {
                SolrDocument solrDoc = solrResults.get(0);
                model.put("recordIdentifier", String.valueOf(solrDoc.getFieldValue("recordIdentifier")));
                model.put("mcrid", String.valueOf(solrDoc.getFieldValue("returnId")));
                if (solrDoc.getFieldNames().contains("ir.pdffulltext_url")) {
                    model.put("doctype", "pdf");
                    String pdfProviderURL = String.valueOf(solrDoc.getFieldValue("ir.pdffulltext_url"));
                    model.put("pdfProviderURL", pdfProviderURL);
                    model.put("filePath", pdfProviderURL.substring(pdfProviderURL.lastIndexOf("/") + 1));
                } else {
                    model.put("doctype", "mets");
                    model.put("filePath", StringUtils.isEmpty(filePath) ? "iview2/phys_0001.iview2" : filePath);
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
