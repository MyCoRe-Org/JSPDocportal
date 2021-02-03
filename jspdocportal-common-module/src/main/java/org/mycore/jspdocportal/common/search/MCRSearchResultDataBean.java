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
package org.mycore.jspdocportal.common.search;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.solr.MCRSolrClientFactory;

/**
 * bean that holds searchresult data
 * 
 * @author Robert Stephan
 *
 */
public class MCRSearchResultDataBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = LogManager.getLogger(MCRSearchResultDataBean.class);

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

    private String backURL;

    private QueryResponse solrQueryResponse;

    private String errorMsg = null;

    private List<String> filterQueries = new ArrayList<String>();

    private List<String> facetFields = new ArrayList<String>();

    private Map<String, Map<String, Long>> facetResult = new LinkedHashMap<String, Map<String, Long>>();

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
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();

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
        if (fqs != null) {
            for (String fq : fqs) {
                solrQuery.removeFilterQuery(fq);
            }
        }

        for (String fq : filterQueries) {
            if(fq.substring(1).startsWith("content:")) {
            	String[] x = fq.split(":", 2);
                solrQuery.addFilterQuery("{!join from=returnId to=id}" + x[0] + ":" + ClientUtils.escapeQueryChars(x[1]));
            }

            else if (fq.substring(1).startsWith("ir.pubyear_end:")) {
                if (Pattern.matches("^\\S+:\\d{4}$", fq)) {
                    fq = fq.replaceFirst(":", ":[* TO ");
                    fq = fq + "]";
                    solrQuery.addFilterQuery(fq);
                }
            }
            else if (fq.substring(1).startsWith("ir.pubyear_start:")) {
                if (Pattern.matches("^\\S+:\\d{4}$", fq)) {
                    fq = fq.replaceFirst(":", ":[");
                    fq = fq + " TO *]";
                    solrQuery.addFilterQuery(fq);
                }
                
            } else if (fq.toLowerCase(Locale.getDefault()).contains(" or ")) {
                solrQuery.addFilterQuery(fq);
            } else {
                String[] x = fq.split(":", 2);
                solrQuery.addFilterQuery(x[0] + ":" + ClientUtils.escapeQueryChars(x[1]));
            }
        }
        String[] ffs = solrQuery.getFacetFields();
        if (ffs != null) {
            for (String ff : ffs) {
                solrQuery.removeFacetField(ff);
            }
        }

        for (String ff : facetFields) {
            solrQuery.addFacetField(ff);
        }
        if (facetFields.size() > 0) {
            solrQuery.setFacetMinCount(1);
        }

        try {
            facetResult.clear();
            solrQueryResponse = solrClient.query(solrQuery);
            SolrDocumentList solrResults = solrQueryResponse.getResults();
            if (solrResults.getNumFound() < start) {
                start = 0;
                doSearch();
                return;
            }
            setCurrent(start);

            if (solrQuery.getFacetFields() != null) {
                for (FacetField ff : solrQueryResponse.getFacetFields()) {
                    LinkedHashMap<String, Long> fieldData = new LinkedHashMap<>();
                    for (Count c : ff.getValues()) {
                        fieldData.put(c.getName(), c.getCount());
                    }
                    facetResult.put(ff.getName(), fieldData);
                }
            }

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
        for (int i = 0; i < solrDocs.size(); i++) {
            SolrDocument solrDoc = solrDocs.get(i);
            result.add(new MCRSearchResultEntry(solrDoc, start + i));
        }
        return result;
    }

    public Map<String, Map<String, Long>> getFacetResult() {
        return facetResult;
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
        return MCRConfiguration2
                .getString("MCR.Searchmask." + (mask != null ? mask : "default") + ".sortfields")
                .orElse(MCRConfiguration2.getString("MCR.Searchmask.default.sortfields").orElse(""))
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

    public List<String> getFacetFields() {
        return facetFields;
    }

    public String getBackURL() {
        return backURL;
    }

    public void setBackURL(String backURL) {
        this.backURL = backURL;
    }

}
