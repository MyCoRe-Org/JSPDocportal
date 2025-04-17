/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
 *
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

package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.output.DOMOutputter;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * part of the MCRDocdetails Tag Library
 * 
 * provides the outer tag, which retrieves the document from database or
 * workflow
 * 
 * If a request parameter debug=true is found, the xml syntax of the 
 * MCR object will be displayed on the website
 * 
 * @author Robert Stephan
 * 
 * @version $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan
 *          2010) $
 */
public class MCRRetrieveObjectTag extends SimpleTagSupport {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final DOMOutputter DOM_OUTPUTTER = new DOMOutputter();
    
    private static final LoadingCache<String, Document> MCROBJECTXML_CACHE = CacheBuilder.newBuilder().maximumSize(300)
            .expireAfterWrite(3, TimeUnit.MINUTES).expireAfterAccess(15, TimeUnit.SECONDS).build(new CacheLoader<>() {
                @Override
                public Document load(String mcrid) throws Exception {
                    try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
                        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
                        return mcrObj.createXML();
                    }
                }
            });
    
    private String mcrid;

    private boolean fromWorkflow;

    private String varDOM;

    private String varJDOM;
    
    private String query;
    
    private String cache="";

    public void setMcrid(String mcrid) {
        this.mcrid = mcrid;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public void setCache(String cache) {
        this.cache = cache;
    }

    /**
     * if set to true, the MCR object is retrieved from workflow directory and
     * not from MyCoRe repository
     * 
     * @param fromWorkflow
     *            true or false
     * 
     */
    public void setFromWorkflow(boolean fromWorkflow) {
        this.fromWorkflow = fromWorkflow;
    }

    /**
     * sets the name of the variable which stores the XML Document as
     * org.w3c.dom.Document for further use in JSP processing 
     * (stored in request scope)
     * 
     * @param var -
     *            the name of the variable
     */
    public void setVarDOM(String var) {
        this.varDOM = var;
    }

    /**
     * sets the name of the variable which stores the XML Document as
     * org.jdom2.Document for further use in JSP processing 
     * (stored in request scope)
     * 
     * @param var -
     *            the name of the variable
     */

    public void setVarJDOM(String var) {
        this.varJDOM = var;
    }

    /**
     * executes the tag
     */
    @Override
    public void doTag() throws JspException, IOException {
        try {
            PageContext pageContext = (PageContext) getJspContext();
            if(StringUtils.isNotBlank(query)) {
                try {
                    SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
                    SolrQuery solrQuery = new SolrQuery();
                    solrQuery.setQuery(query);
                    solrQuery.setFields("id");
                    QueryRequest queryRequest = new QueryRequest(solrQuery);
                    MCRSolrAuthenticationManager.obtainInstance().applyAuthentication(queryRequest,
                        MCRSolrAuthenticationLevel.SEARCH);
                    QueryResponse solrResponse = queryRequest.process(solrClient);
                    SolrDocumentList solrResults = solrResponse.getResults();

                    if(!solrResults.isEmpty()) {
                        mcrid = String.valueOf(solrResults.get(0).getFirstValue("id"));
                    }
                } catch (SolrServerException e) {
                    LOGGER.error(e);
                }
            }
            if(cache.contains("clear") || "clear".equals(pageContext.getRequest().getParameter("_cache"))) {
                MCROBJECTXML_CACHE.invalidate(mcrid);
            }
            
            MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
            Document doc;
            if (fromWorkflow) {
                doc = MCRBPMNUtils.getWorkflowObjectXML(mcrObjID);
            } else {
                if(cache.contains("true") || cache.contains("on")) {
                    doc = MCROBJECTXML_CACHE.get(mcrid);
                }
                else {
                    doc = MCRMetadataManager.retrieveMCRObject(mcrObjID).createXML();
                }
            }
            if (varDOM != null) {
                org.w3c.dom.Document dom = DOM_OUTPUTTER.output(doc);
                getJspContext().setAttribute(varDOM, dom, PageContext.REQUEST_SCOPE);
            }
            if (varJDOM != null) {
                getJspContext().setAttribute(varJDOM, doc, PageContext.REQUEST_SCOPE);
            }
        } catch (Exception e) {
            throw new MCRException(e);
        }
    }
}
