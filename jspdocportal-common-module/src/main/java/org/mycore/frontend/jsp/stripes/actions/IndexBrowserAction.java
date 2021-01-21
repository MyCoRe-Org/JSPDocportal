package org.mycore.frontend.jsp.stripes.actions;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.frontend.jsp.search.MCRSearchResultDataBean;
import org.mycore.solr.MCRSolrClientFactory;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/indexbrowser/{modus}")
public class IndexBrowserAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(IndexBrowserAction.class);
    ForwardResolution fwdResolution = new ForwardResolution("/content/indexbrowser.jsp");

    private TreeSet<String> firstSelector = new TreeSet<String>();
    private Map<String, Long> secondSelector = new TreeMap<String, Long>();
    private String modus = "";
    private String select;
    private MCRSearchResultDataBean mcrSearchResult;

    public IndexBrowserAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("modus") != null) {
            modus = getContext().getRequest().getParameter("modus");
        }
        if (getContext().getRequest().getParameter("select") != null) {
            select = getContext().getRequest().getParameter("select");
        }

    }

    @DefaultHandler
    public Resolution defaultRes() {
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

            SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();

            firstSelector.clear();
            try {
                for (Count c : solrClient.query(q).getFacetFields().get(0).getValues()) {
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
                mcrSearchResult.setAction("search");
                mcrSearchResult.getFacetFields().add(facetfield);
                if (filterQuery != null && filterQuery.length() > 0) {
                    mcrSearchResult.getFilterQueries().add(filterQuery);
                }

                mcrSearchResult.doSearch();
                mcrSearchResult.setBackURL(
                        getContext().getRequest().getContextPath() + "/indexbrowser/" + modus + "?select=" + select);
                MCRSearchResultDataBean.addSearchresultToSession(getContext().getRequest(), mcrSearchResult);

                QueryResponse response = mcrSearchResult.getSolrQueryResponse();
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
            return fwdResolution;
        } catch (MCRConfigurationException e) {
            return new RedirectResolution("/");
        }
    }

    public SortedSet<String> getFirstSelector() {
        return firstSelector;
    }

    public String getModus() {
        return modus;
    }

    public void setModus(String modus) {
        this.modus = modus;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public Map<String, Long> getSecondSelector() {
        return secondSelector;
    }

    public MCRSearchResultDataBean getResult() {
        return mcrSearchResult;
    }

}
