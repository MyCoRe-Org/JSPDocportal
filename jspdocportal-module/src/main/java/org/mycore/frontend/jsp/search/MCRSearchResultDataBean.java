/*
 * $RCSfile$
 * $Revision: 19696 $ $Date: 2011-01-04 13:45:05 +0100 (Di, 04 Jan 2011) $
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
package org.mycore.frontend.jsp.search;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.solr.MCRSolrClientFactory;

/**
 * bean that holds searchresult data
 * 
 * @author Robert Stephan
 *
 */
public class MCRSearchResultDataBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger.getLogger(MCRSearchResultDataBean.class);

    private String id;

    private int current = 0;

    private int start = 0;

    private int rows = 10;

    private String sort = "";

    private String action = "";

    private String mask = null;

    private String xedSessionId;

    private Document mcrQueryXML = null;

    private SolrQuery solrQuery = new SolrQuery();

    private QueryResponse solrQueryResponse;

    private String errorMsg = null;
    
    private List<String> filterQueries = new ArrayList<String>();
    
    public MCRSearchResultDataBean() {
        this.id = UUID.randomUUID().toString();
    }

    public static void addSearchresultToSession(HttpServletRequest request, MCRSearchResultDataBean searchresult) {
        @SuppressWarnings("unchecked")
        LRUMap<String, MCRSearchResultDataBean> map = (LRUMap<String, MCRSearchResultDataBean>) request.getSession()
            .getAttribute("mcrSearchResultMap");
        if (map == null) {
            map = new LRUMap<String, MCRSearchResultDataBean>(16);
            request.getSession().setAttribute("mcrSearchResultMap", map);
        }
        map.put(searchresult.getId(), searchresult);
    }

    public static MCRSearchResultDataBean retrieveSearchresultFromSession(HttpServletRequest request, String searchID) {
        @SuppressWarnings("unchecked")
        LRUMap<String, MCRSearchResultDataBean> map = (LRUMap<String, MCRSearchResultDataBean>) request.getSession()
            .getAttribute("mcrSearchResultMap");
        if (map == null) {
            return null;
        }
        return map.get(searchID);
    }

    public void doSearch() {
        solrQueryResponse = null;
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();

        if (rows >= 0) {
            solrQuery.setRows(rows);
        }

        start = Math.max(0, start);
        solrQuery.setStart(start);

        if (!sort.isEmpty()) {
            String[] x = sort.split("\\s|,");
            if (x.length > 1) {
                solrQuery.setSort(SortClause.create(x[0], x[1]));
            }
        }
        
       String[] fqs = solrQuery.getFilterQueries();
       if(fqs!=null){
           for(String fq:fqs){
               solrQuery.removeFilterQuery(fq);
           }
       }
      
       for(String fq:filterQueries){
           if(fq.contains("ir.pubyear_end")){
               fq = fq.replaceFirst("'", "[* TO ");
               fq = fq.replaceFirst("'", "]");
           }
           if(fq.contains("ir.pubyear_start")){
               fq = fq.replaceFirst("'", "[");
               fq = fq.replaceFirst("'", " TO *]");
           }
           solrQuery.addFilterQuery(fq);
       }

        try {
            solrQueryResponse = solrClient.query(solrQuery);
            SolrDocumentList solrResults = solrQueryResponse.getResults();
            if (solrResults.getNumFound() < start) {
                start = 0;
                doSearch();
                return;
            }
            setCurrent(start);
        } catch (SolrServerException | IOException e) {
            LOGGER.error(e);
        }
    }

    public MCRSearchResultEntry getHit(int hit) {
        if (hit < 0 || hit > solrQueryResponse.getResults().getNumFound())
            return null;
        int pos = hit - start;
        if (pos < 0 || pos >= rows) {
            start = (hit / rows) * rows;
            doSearch();
            return getHit(hit);
        }
        current = hit;
        return new MCRSearchResultEntry(solrQueryResponse.getResults().get(pos), hit);
    }
    //	public int findEntryPosition(String mcrid){
    //		for(int i=0;i<entries.size();i++){
    //			if(entries.get(i).getMcrid().equals(mcrid)){
    //				return i;
    //			}
    //		}
    //		return -1;
    //	}

    //setter and getter methods

    public long getNumFound() {
        return solrQueryResponse.getResults().getNumFound();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getNumPages() {
        return Math.round((float) Math.ceil((float) getNumFound() / rows));
    }

    public List<MCRSearchResultEntry> getEntries() {
        ArrayList<MCRSearchResultEntry> result = new ArrayList<MCRSearchResultEntry>();
        SolrDocumentList solrDocs = solrQueryResponse.getResults();
        for (int i=0;i<solrDocs.size(); i++) {
            SolrDocument solrDoc = solrDocs.get(i);
            result.add(new MCRSearchResultEntry(solrDoc, start + i));
        }
        return result;
    }

    public void setQuery(String query) {
        solrQuery.setQuery(query);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Document getMCRQueryXML() {
        return mcrQueryXML;
    }

    public void setMCRQueryXML(Document mcrQueryXML) {
        this.mcrQueryXML = mcrQueryXML;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getSortfields() {
        return MCRConfiguration.instance()
            .getString("MCR.Searchmask." + (mask != null ? mask : "default") + ".sortfields",
                MCRConfiguration.instance().getString("MCR.Searchmask.default.sortfields", ""))
            .trim();
    }

    public String getXedSessionId() {
        return xedSessionId;
    }

    public void setXedSessionId(String xedSessionId) {
        this.xedSessionId = xedSessionId;
    }

    public QueryResponse getSolrQueryResponse() {
        return solrQueryResponse;
    }

    public List<String> getFilterQueries() {
        return filterQueries;
    }

}
