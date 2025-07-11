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

package org.mycore.jspdocportal.common.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.jspdocportal.common.search.MCRSearchResultDataBean;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;

/**
 * MVC Controller for Search Browsing
 * 
 * @author Stephan
 *
 */

@Path("/do/browse/{mask}")
public class BrowseController {
    public static final Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    public static final int DEFAULT_ROWS = 20;

    private MCRSearchResultDataBean result;

    @GET
    public Viewable get(@PathParam("mask") String mask,
        @Context HttpServletRequest request) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/browse/" + mask, model);

        if (request.getParameter("_search") != null && request.getParameter("_search").length() > 0) {
            //check against null if session does not exist
            result = MCRSearchResultDataBean.retrieveSearchresultFromSession(request, request.getParameter("_search"));
        }

        processQueryAndSearchParams(request);

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
            if (StringUtils.isEmpty(result.getSort())) {
                result.setSort("modified desc");
            }
        }

        if (mask == null) {
            result.setAction("do/browse");
        } else {
            result.setAction("do/browse/" + mask);
            result.getFacetFields().clear();
            for (String ff : MCRConfiguration2.getString("MCR.Browse." + mask + ".FacetFields").orElse("")
                .split(",")) {
                if (!ff.isBlank()) {
                    result.getFacetFields().add(ff.trim());
                }
            }
        }

        processFilterParams(request);
        processStartAndRowParam(request);

        if (result.getSolrQuery() != null) {
            MCRSearchResultDataBean.addSearchresultToSession(request, result);
            result.doSearch();
        }
        model.put("result", result);
        model.put("util", new Util());

        return v;
    }

    private void processQueryAndSearchParams(HttpServletRequest request) {
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
    }

    private void processFilterParams(HttpServletRequest request) {
        if (result != null) {
            if (request.getParameter("_add-filter") != null) {
                for (String s : request.getParameterValues("_add-filter")) {
                    if (!s.trim().endsWith(":") && s.contains(":")) {
                        String key = s.substring(0, s.indexOf(':'));
                        String values = s.substring(s.indexOf(':') + 1);
                        for (String val : values.trim().split("\\s+")) {
                            String f = key + ":" + val ;
                            if (!result.getFilterQueries().contains(f)) {
                                result.getFilterQueries().add(f);
                            }
                        }
                    }
                }
            }

            if (request.getParameter("_remove-filter") != null) {
                for (String s : request.getParameterValues("_remove-filter")) {
                    result.getFilterQueries().remove(s);
                }
            }
        }
    }

    private void processStartAndRowParam(HttpServletRequest request) {
        if (request.getParameter("_start") != null) {
            try {
                result.setStart(Integer.parseInt(request.getParameter("_start")));
            } catch (NumberFormatException nfe) {
                result.setStart(0);
            }
        }

        if (result.getRows() <= 0) {
            result.setRows(DEFAULT_ROWS);
        }
        if (request.getParameter("rows") != null) {
            try {
                result.setRows(Integer.parseInt(request.getParameter("rows")));
            } catch (NumberFormatException nfe) {
                // do nothing, use default
            }
        }
    }

    //TODO move to jsp tag
    public class Util {
        public String calcFacetOutputString(String facetKey, String facetValue) {
            String result = facetValue;
            if (facetKey.contains("_msg.facet")) {
                result =
                    MCRTranslation.translate("Browse.Facet." + facetKey.replace("_msg.facet", "") + "." + facetValue);
            }
            if (facetKey.contains("_class.facet")) {
                MCRCategory categ =
                    MCRCategoryDAOFactory.obtainInstance().getCategory(MCRCategoryID.ofString(facetValue),
                        0);
                if (categ != null) {
                    result = categ.getCurrentLabel().get().getText();
                }
            }

            return result;

        }
    }

}
