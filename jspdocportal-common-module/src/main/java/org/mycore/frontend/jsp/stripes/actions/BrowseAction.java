package org.mycore.frontend.jsp.stripes.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.frontend.jsp.search.MCRSearchResultDataBean;
import org.mycore.services.i18n.MCRTranslation;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Action Bean for Search Browsing
 * 
 * 
 * @author Stephan
 *
 */
@UrlBinding("/browse/{mask}")
public class BrowseAction extends MCRAbstractStripesAction implements ActionBean {
    @SuppressWarnings("unused")
    private static Logger LOGGER = LogManager.getLogger(BrowseAction.class);

    public static Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    public static int DEFAULT_ROWS = 20;

    private String mask = null;

    private MCRSearchResultDataBean result;

    public BrowseAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
    }

    @DefaultHandler
    public Resolution defaultRes() {
        ForwardResolution fwdResolutionForm = new ForwardResolution("/WEB-INF/browse/" + mask + ".jsp");

        getContext().getResponse().setCharacterEncoding("UTF-8");
        getContext().getResponse().setContentType("text/xhtml;charset=utf-8");
        HttpServletRequest request = getContext().getRequest();
        if (request.getParameter("_search") != null && request.getParameter("_search").length() > 0) {
            //check against null if session does not exist
            result = MCRSearchResultDataBean.retrieveSearchresultFromSession(request, request.getParameter("_search"));
        }

        if (request.getParameter("q") != null) {
            result = new MCRSearchResultDataBean();
            result.setAction("browse");
            result.setQuery(request.getParameter("q"));
            result.setMask("");
        }

        if (request.getParameter("searchField") != null && request.getParameter("searchValue") != null) {
            result = new MCRSearchResultDataBean();
            result.setAction("browse");
            result.setQuery("+" + request.getParameter("searchField") + ":"
                    + ClientUtils.escapeQueryChars(request.getParameter("searchValue")));
            result.setMask("");
        }
        if (request.getParameter("sortField") != null && request.getParameter("sortValue") != null) {
            result.setSort(request.getParameter("sortField") + " " + request.getParameter("sortValue"));
            result.setStart(0);
        }

        if (result == null) {
            result = new MCRSearchResultDataBean();
            result.setQuery(MCRConfiguration2.getString("MCR.Browse." + mask + ".Query").orElse("*:*"));
            result.setRows(DEFAULT_ROWS);
        }
        result.setMask(mask);
        if (request.getParameter("_sort") != null) {
            result.setSort(request.getParameter("_sort"));
            result.setStart(0);
        } else {
            result.setSort("modified desc");
        }

        if (mask == null) {
            result.setAction("browse");
        } else {
            result.setAction("browse/" + mask);
            result.getFacetFields().clear();
            for (String ff : MCRConfiguration2.getString("MCR.Browse." + mask + ".FacetFields").orElse("")
                    .split(",")) {
                if (ff.trim().length() > 0) {
                    result.getFacetFields().add(ff.trim());
                }
            }
        }

        if (result != null) {
            if (request.getParameter("_add-filter") != null) {
                for (String s : request.getParameterValues("_add-filter")) {
                	if(!s.trim().endsWith(":")) {
                		if (!result.getFilterQueries().contains(s)) {
                			result.getFilterQueries().add(s);
                    	}
                	}
                }
            }

            if (request.getParameter("_remove-filter") != null) {
                for (String s : request.getParameterValues("_remove-filter")) {
                    result.getFilterQueries().remove(s);
                }
            }

            if (request.getParameter("_start") != null) {
                try {
                    result.setStart(Integer.parseInt(request.getParameter("_start")));
                } catch (NumberFormatException nfe) {
                    result.setStart(0);
                }
            }
        }

        if (result.getRows() <= 0) {
            result.setRows(DEFAULT_ROWS);
        }
        if (request.getParameter("rows") != null) {
            try {
                result.setRows(Integer.valueOf(request.getParameter("rows")));
            } catch (NumberFormatException nfe) {
                // do nothing, use default
            }
        }
        if (result.getSolrQuery() != null) {
            MCRSearchResultDataBean.addSearchresultToSession(request, result);
            result.doSearch();
        }

        return fwdResolutionForm;
    }

    public MCRSearchResultDataBean getResult() {
        return result;
    }

    public void setResult(MCRSearchResultDataBean result) {
        this.result = result;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String calcFacetOutputString(String facetKey, String facetValue) {
        String result = facetValue;
        if (facetKey.contains("_msg.facet")) {
            result = MCRTranslation.translate("Browse.Facet." + facetKey.replace("_msg.facet", "") + "." + facetValue);
        }
        if (facetKey.contains("_class.facet")) {
            MCRCategory categ = MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.fromString(facetValue),
                    0);
            if (categ != null) {
                result = categ.getCurrentLabel().get().getText();
            }
        }

        return result;

    }

}
