package org.mycore.frontend.jsp.stripes.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

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
import org.mycore.solr.MCRSolrClientFactory;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Action Bean for opening the MyCoRe Image Viewer
 * 
 * @author Robert Stephan
 *
 */
@UrlBinding("/mcrviewer/{field}/{identifier}/{filePath}")
public class MCRViewerAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(MCRViewerAction.class);

    private String field = null;

    private String identifier = null;

    private String filePath = null;

    private String pdfProviderURL = null;

    private String doctype = null;

    private String recordIdentifier = null;

    private String mcrid = null;

    public MCRViewerAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
    }

    @DefaultHandler
    public Resolution defaultRes() {
        ForwardResolution fwdResolutionForm = new ForwardResolution("/WEB-INF/mcrviewer.jsp");

        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        String value = identifier;
        if("recordIdentifier".equals(field) && !value.contains("/")) {
            value = value.replaceFirst("_", "/");
        }
        SolrQuery solrQuery = new SolrQuery(field + ":" + ClientUtils.escapeQueryChars(value));
        solrQuery.setRows(1);

        try {
            HttpServletRequest request = getContext().getRequest();
            QueryResponse solrResponse = solrClient.query(solrQuery);
            SolrDocumentList solrResults = solrResponse.getResults();
            if (solrResults.size() > 0) {
                SolrDocument solrDoc = solrResults.get(0);
                recordIdentifier = String.valueOf(solrDoc.getFieldValue("recordIdentifier"));
                mcrid = String.valueOf(solrDoc.getFieldValue("returnId"));
                if (solrDoc.getFieldNames().contains("ir.pdffulltext_url")) {
                    doctype = "pdf";
                    pdfProviderURL = String.valueOf(solrDoc.getFieldValue("ir.pdffulltext_url"));
                    filePath = pdfProviderURL.substring(pdfProviderURL.lastIndexOf("/") + 1);
                } else {
                    doctype = "mets";
                    if (StringUtils.isEmpty(filePath)) {
                        String startFile = request.getParameter("_mcrviewer_start");
                        if (startFile != null && startFile.length() > 0) {
                            filePath = "iview2/" + startFile + ".iview2";
                        } else {
                            filePath = "iview2/phys_0001.iview2";
                        }
                    }
                }
            }

        } catch (SolrServerException | IOException e) {
            LOGGER.error(e);
        }
        
        //add viewer request paramter to link a specific view port
        //http://localhost:8080/rosdok/mcrviewer/recordIdentifier/rosdok_ppn888171862/iview2/phys_0005.iview2
        //?x=1065.7192494788046&y=1139.3954134815867&scale=2.3209677419354837&rotation=0&layout=singlePageLayout
        for(String p: StringUtils.split("x y scale rotation layout")) {
            if(getContext().getRequest().getParameterMap().containsKey(p)) {
                fwdResolutionForm.addParameter(p, getContext().getRequest().getParameter(p));
            }
        }

        return fwdResolutionForm;
    }

    public String getField() {
        return field;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPdfProviderURL() {
        return pdfProviderURL;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setIdentifier(String identifier) {
        if (identifier != null) {
            try {
                identifier = URLDecoder.decode(URLDecoder.decode(identifier, "UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                //does not happen
            }
        }
        this.identifier = identifier;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPdfProviderURL(String pdfProviderURL) {
        this.pdfProviderURL = pdfProviderURL;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public String getMcrid() {
        return mcrid;
    }

}
