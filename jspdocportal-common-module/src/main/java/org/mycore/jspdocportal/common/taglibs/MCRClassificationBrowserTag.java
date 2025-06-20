/**
 * 
 * $Revision: 13468 $ $Date: 2008-04-28 17:49:59 +0200 (Mo, 28 Apr 2008) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategLinkServiceFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * A JSP tag, that includes a classification browser. The displayed content is
 * highly configurable. Look into the TLD file and the bean-style setters for
 * details.
 * 
 * Future implementations could include the following additional features: -
 * sortby : sort by ID or label - objecttype: consider only the specified object
 * type (can currently be done by specifing a searchrestriction) -
 * cssStylePrimaryName: add CSS class attributes for layout (look into Google
 * Web Toolkit's conventions)
 * 
 * @author Robert Stephan
 * @version $Revision: 11610 $ $Date: 2007-05-31 16:39:41 +0200 (Do, 31 Mai
 *          2007) $
 * @since 2.0
 */
public class MCRClassificationBrowserTag extends SimpleTagSupport {
    private static final Logger LOGGER = LogManager.getLogger();

    private List<String> path = new ArrayList<>();

    private String mode;

    private static LoadingCache<String, Integer> cacheHitCount = CacheBuilder.newBuilder()
        .maximumSize(5000)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build(
            new CacheLoader<>() {
                @Override
                public Integer load(String key) throws Exception {
                    SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
                    SolrQuery query = new SolrQuery(key);
                    query.setRows(0);
                    try {
                        QueryRequest queryRequest = new QueryRequest(query);
                        MCRSolrAuthenticationManager.obtainInstance().applyAuthentication(queryRequest,
                            MCRSolrAuthenticationLevel.SEARCH);
                        QueryResponse response = queryRequest.process(solrClient);
                        SolrDocumentList solrResults = response.getResults();
                        return (int) solrResults.getNumFound();
                    } catch (SolrServerException e) {
                        LOGGER.error(e);
                    }

                    return null;
                }
            });

    private static LoadingCache<MCRCategoryID, List<MCRCategory>> cacheCategChildren = CacheBuilder.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build(
            new CacheLoader<>() {
                @Override
                public List<MCRCategory> load(MCRCategoryID key) {
                    return MCRCategoryDAOFactory.obtainInstance().getChildren(key);
                }
            });

    private static LoadingCache<MCRCategoryID, Map<MCRCategoryID, Boolean>> cacheHasLinks = CacheBuilder.newBuilder()
        .maximumSize(10)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build(
            new CacheLoader<>() {
                @Override
                public Map<MCRCategoryID, Boolean> load(MCRCategoryID key) throws Exception {
                    return MCRCategLinkServiceFactory.obtainInstance()
                        .checkForLinks(MCRCategoryDAOFactory.obtainInstance().getCategory(key, 0));
                }
            });

    private static LoadingCache<MCRCategoryID, Map<MCRCategoryID, Number>> cacheLinkCount = CacheBuilder.newBuilder()
        .maximumSize(10)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build(
            new CacheLoader<>() {
                @Override
                public Map<MCRCategoryID, Number> load(MCRCategoryID key) throws Exception {
                    return MCRCategLinkServiceFactory.obtainInstance()
                        .countLinks(MCRCategoryDAOFactory.obtainInstance().getCategory(key, 0), false);
                }
            });

    /**
     * The mode - the property prefix
     */
    public void setModus(String mode) {
        this.mode = mode;
    }

    /**
     * @see jakarta.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    @Override
    @SuppressWarnings({ "PMD.NPathComplexity" })
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        if ("clear".equals(pageContext.getRequest().getParameter("_cache"))) {
            cacheCategChildren.invalidateAll();
            cacheHitCount.invalidateAll();
            cacheHasLinks.invalidateAll();
            cacheLinkCount.invalidateAll();
        }

        CBConfig cb = new CBConfig(mode);
        long start = System.currentTimeMillis();

        MCRCategoryID rootClassifID = new MCRCategoryID(cb.classification, cb.category);

        PageContext context = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        String requestPath = StringEscapeUtils.escapeHtml4(request.getParameter("select"));
        StringBuffer url = new StringBuffer(MCRFrontendUtil.getBaseURL());
        url.append("do/classbrowser/").append(mode).append('?');

        @SuppressWarnings("rawtypes")
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String s = paramNames.nextElement();
            if (!s.equals("select") && !s.equals("modus")) {
                url.append(URLEncoder.encode(StringEscapeUtils.escapeHtml4(s), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(StringEscapeUtils.escapeHtml4(request.getParameter(s)),
                        StandardCharsets.UTF_8))
                    .append("&amp;");
            }
        }

        if (requestPath == null) {
            requestPath = "";
        }
        url.append("select=").append(clearPath(requestPath));

        JspWriter out = getJspContext().getOut();
        out.write("\n\n<!-- ClassificationBrowser (" + rootClassifID.getRootID() + ") START  -->");
        if (!MCRCategoryDAOFactory.obtainInstance().exist(rootClassifID)) {
            LOGGER.error("Classification does not exist {}", rootClassifID::getRootID);
            out.write("Classification " + rootClassifID.getRootID() + " does not exist!");
            return;
        }

        String webApplicationBaseURL = MCRFrontendUtil.getBaseURL();
        String subselectWebpage = context.getRequest().getParameter("XSL.subselect.webpage.SESSION");
        if (subselectWebpage == null) {
            subselectWebpage = "";
        }

        String subselectSession = context.getRequest().getParameter("XSL.subselect.session.SESSION");
        if (subselectSession == null) {
            subselectSession = "";
        }
        String subselectVarpath = context.getRequest().getParameter("XSL.subselect.varpath.SESSION");
        if (subselectVarpath == null) {
            subselectVarpath = "";
        }
        boolean isSubselect = !subselectVarpath.equals("");

        // cancel subselect
        if (isSubselect) {
            out.write("<form action=\"" + webApplicationBaseURL + subselectWebpage + "XSL.editor.session.id="
                + subselectSession + "\" method=\"post\">\n");
            out.write("   <input type=\"submit\" class=\"submit\" value=\""
                + MCRTranslation.translate("Editor.Common.button.CancelSelect") + "\" />\n");
            out.write("</form><br/><br/>\n");
        }

        out.write("\n<div class=\"classification-browser classification-browser-" + mode + "\">");
        boolean didIt = false;
        try {
            for (MCRCategory categ : cacheCategChildren.get(rootClassifID)) {
                didIt = outputCategory(cb, categ, MCRFrontendUtil.getBaseURL(), url.toString(), 0, didIt);
            }
        } catch (ExecutionException e) {
            LOGGER.error(e);
        }
        if (!didIt) {
            out.write("\n<b>" + MCRTranslation.translate("Webpage.browse.empty") + "</b>");
        }
        out.write("\n   <div style=\"clear:both\"></div>");
        out.write("\n</div>");
        long d = System.currentTimeMillis() - start;
        out.write("\n\n<!-- ClassificationBrowser (" + rootClassifID.getRootID() + ") ENDE  [" + Long.toString(d)
            + "ms] -->");
        LOGGER.debug("ClassificationBrowser displayed for: {}   ({} ms)", rootClassifID::getRootID, () -> d);

    }

    /**
     * prints a category as li HTML object with a table
     * 
     * @param categ
     *            - the category
     * @param baseURL
     *            - the baseURL of the web application
     * @param cbURL
     *            - the Classification Browser with all parameters
     * @param curLevel
     *            - the current level to calculate the depth
     * @throws IOException
     */
    @SuppressWarnings({ "PMD.NcssCount", "PMD.CognitiveComplexity", "PMD.NPathComplexity" })
    private boolean outputCategory(CBConfig cb, MCRCategory categ, String baseURL, String cbURL, int curLevel,
        boolean didIt) throws IOException {
        JspWriter out = getJspContext().getOut();

        boolean result = didIt;
        boolean hasChildren = countChildrenBySearch(cb, categ.getId().getId()) > 0;
        boolean hasLinks = hasLinks(cb, categ);
        boolean opened = path.contains(categ.getId().getId());

        if (!(cb.hideemptyleaves && !hasLinks)) {
            result = true;
            StringBuffer sbIndent = new StringBuffer("\n   ");
            for (int i = 0; i < curLevel; i++) {
                sbIndent.append("   ");
            }
            String indent = sbIndent.toString();
            out.write(indent + "   <div class=\"cb-item\">");
            out.write(indent + "      <div class=\"cb-icon\">");
            String iconURL = retrieveIconURL(cb, hasChildren, curLevel, hasLinks(cb, categ), (cb.expand || opened));
            if (!cb.expand && hasChildren && curLevel + 1 < cb.level) {
                String title = "";
                if (iconURL.endsWith("_plus.gif")) {
                    title = MCRTranslation.translate("Webpage.browse.open");
                }
                if (iconURL.endsWith("_minus.gif")) {
                    title = MCRTranslation.translate("Webpage.browse.close");
                }
                out.write(indent + "         <a href=\"" + cbURL + "/" + categ.getId().getId() + "\" title=\"" + title
                    + "\">");
            }
            out.write(indent + "            <img class=\"borderless\" src=\"" + baseURL + iconURL + "\" />");
            if (!cb.expand && hasChildren && curLevel + 1 < cb.level) {
                out.write("\n                </a>");
            }
            out.write(indent + "      </div>");
            out.write(indent + "      <div class=\"cb-label\">");
            if (cb.showid) {
                out.write(indent + "         <span class=\"cb-id\">" + categ.getId().getId() + "</span>");
            }

            out.write(
                indent + "         <span class=\"cb-text\">" + categ.getCurrentLabel().get().getText() + "</span>");

            String label = MCRTranslation.translate("Webpage.browse.show");

            if (cb.count) {
                int c = 0;
                if (cb.filter != null) {
                    c = countBySearch(cb, categ.getId().getId());
                } else {
                    Number n;
                    try {
                        n = cacheLinkCount.get(categ.getRoot().getId()).get(categ.getId());
                        if (n != null) {
                            c = n.intValue();
                        }
                    } catch (ExecutionException e) {
                        // do nothing
                    }
                }
                switch (c) {
                    case 0:
                        label = MCRTranslation.translate("Webpage.browse.noentries");
                        break;
                    case 1:
                        label = MCRTranslation.translate("Webpage.browse.entry", 1);
                        break;
                    default:
                        label = MCRTranslation.translate("Webpage.browse.entries", c);
                }
            }
            out.write(indent + "      </div>");

            out.write(indent + "      <div class=\"cb-count\">");
            writeLinkedCategoryItemText(cb, categ, baseURL, label, out);
            out.write(indent + "      </div>");

            if (cb.showdescription) {
                String descr = categ.getCurrentLabel().get().getDescription();
                if (descr != null && descr.length() > 0) {
                    out.write(indent + "      <div class=\"cb-description\">" + descr + "</div>");
                }
            }
            if (cb.showuri) {
                URI uri = categ.getURI();
                if (uri != null && uri.toString().length() > 0) {
                    out.write(indent + "      <div class=\"cb-url\">" + uri.toString() + "</div>");
                }
            }

            if ((cb.expand || opened) && hasChildren) {
                if (curLevel + 1 < cb.level) {

                    try {
                        for (MCRCategory c : cacheCategChildren.get(categ.getId())) {
                            outputCategory(cb, c, baseURL, cbURL, curLevel + 1, didIt);
                        }
                    } catch (ExecutionException e) {
                        LOGGER.error(e);
                    }
                }
            }
            out.write(indent + "   </div>");
        }
        return result;
    }

    /**
     * retrieves a URL for the proper category icon to be displayed, based on
     * certain asumption
     * 
     * @param hasChildren
     *            - does the current category has child categories
     * @param curLevel
     *            - the current level
     * @param hasLinks
     *            - are there items in the category
     * @param opened
     *            - is the category already displayed "opened"
     * @return
     */
    private String retrieveIconURL(CBConfig cb, boolean hasChildren, int curLevel, boolean hasLinks, boolean opened) {
        // Basis-Icon-Name bestimmen
        String iconType = getIconType(cb, hasChildren, curLevel, opened);
        
        // Links-Suffix hinzufügen
        String linksSuffix = hasLinks ? "" : "_empty";
        return "images/" + iconType + linksSuffix + ".gif";
    }

    private String getIconType(CBConfig cb, boolean hasChildren, int curLevel, boolean opened) {
        // Expand-Modus: nur geöffnete Ordner mit Kindern zeigen spezielles Icon
        if (cb.expand && opened && hasChildren) {
            return "folder_open";
        }
        // Level-basierte Ansicht: Plus/Minus Icons nur bei Ordnern mit Kindern
        if (curLevel + 1 < cb.level && hasChildren) {
            return opened ? "folder_minus" : "folder_plus";
        }
        // Standard-Fall für alle anderen Situationen
        return "folder_plain";
    }

    /**
     * checks, if there are items in the given category if counting is enabled,
     * this information will be retrieved from the count map otherwise from the
     * hasLinkMaps (faster execution time)
     * 
     * @param category
     *            - the category object
     * @return if there are items in the category
     */
    private boolean hasLinks(CBConfig cb, MCRCategory category) {
        if (cb.filter != null) {
            return countBySearch(cb, category.getId().getId()) > 0;
        }

        if (cb.count) {
            try {
                return cacheLinkCount.get(category.getRoot().getId()).get(category.getId()).longValue() > 0;
            } catch (Exception e) {
                //CacheException + NullPointerException
                return false;
            }
        } else {
            try {
                return cacheHasLinks.get(category.getRoot().getId()).get(category.getId());
            } catch (Exception e) {
                // CacheException and NullpointerException
                return false;
            }
        }
    }

    /**
     * create the text for a category and if necessary a link around it
     * 
     * @param categ
     *            - the MCRCategory to be displayed
     * @param baseURL
     *            - the baseurl
     * @param out
     *            - the JSPWriter
     * @throws IOException
     */
    private void writeLinkedCategoryItemText(CBConfig cb, MCRCategory categ, String baseURL, String label,
        JspWriter out) throws IOException {
        boolean showLinks = cb.linkall || hasLinks(cb, categ);
        if (showLinks) {
            PageContext context = (PageContext) getJspContext();

            HttpServletRequest request = (HttpServletRequest) context.getRequest();
            StringBuffer url = new StringBuffer(baseURL);
            if (request.getParameter("XSL.subselect.session.SESSION") != null) {
                // do a subselect / create a url, that returns to an editor
                url.append("servlets/XMLEditor");
                url.append("?_action=end.subselect");
                url.append("&amp;subselect.session=")
                    .append(StringEscapeUtils.escapeHtml4(request.getParameter("XSL.subselect.session.SESSION")));
                url.append("&amp;subselect.varpath=")
                    .append(StringEscapeUtils.escapeHtml4(request.getParameter("XSL.subselect.varpath.SESSION")));
                url.append("&amp;subselect.webpage=")
                    .append(URLEncoder.encode(
                        StringEscapeUtils.escapeHtml4(request.getParameter("XSL.subselect.webpage.SESSION")),
                        StandardCharsets.UTF_8));
                url.append("&amp;_var_@categid=").append(categ.getId().getId());
                url.append("&amp;_var_@type=")
                    .append(URLEncoder.encode(categ.getCurrentLabel().get().getText(), "UTF-8"));

            } else {
                // "normal" classification browser - do a search
                url.append("do/search");
                String query =
                    URLEncoder.encode(generateQuery(cb, categ.getId().getId()), Charset.defaultCharset().name());
                query = query.replace("\\", "%5C");
                url.append("?q=").append(query);

                if (cb.sortResult != null) {
                    url.append("&_sort=").append(cb.sortResult.trim());
                }
            }
            out.write("<a class=\"btn btn-sm btn-outline-secondary cb-btn\" href=\"" + url.toString() + "\">");
            out.write(label + " <i class=\"fa fa-share\"></i>");
            out.write("</a>");
        } else {
            out.write(label);
        }
    }

    /**
     * clears the path of opened / closed categories of the browser
     * 
     * @param uri
     * @return the cleaned path
     */
    private String clearPath(String uri) {
        final String[] uriParts = uri.split("/");

        // remove double entries from path
        // (if an entry appears the 2nd time it is "closed" and should not be
        // displayed
        // -> so we can remove it here)
        path.clear();
        for (String x : uriParts) {
            if (x.length() > 0) {
                if (path.contains(x)) {
                    path.remove(x);
                } else {
                    path.add(x);
                }
            }
        }

        StringBuffer sbResult = new StringBuffer();
        for (String uriPart : path) {
            sbResult.append('/').append(uriPart);
        }
        return sbResult.toString();
    }

    /**
     * creates a MCRQuery string with the given searchrestriction and the
     * categoryID which can be used for counting and to create the displayed
     * query link
     * 
     * @param categid
     *            - the category id
     * @return a string, representing a MCRQuery in textual syntax
     */
    private String generateQuery(CBConfig cb, String categid) {
        StringBuffer result = new StringBuffer();
        if (cb.filter != null) {
            result.append('+').append(cb.filter.replace("=", ":"));
        }
        result.append(" +category.top:").append(cb.classification).append("\\:").append(categid);
        return result.toString();
    }

    /**
     * counts using MCRQueries instead of information about links To improve
     * performance a "pull through cache" is used. That means the cache knows
     * how to generate items it does not contain
     * 
     * @param categid
     *            - the category iD
     * @return the number of results of the query for the given ID
     */
    private int countBySearch(CBConfig cb, String categid) {
        try {
            return cacheHitCount.get(generateQuery(cb, categid));
        } catch (ExecutionException e) {
            // do nothing
        }
        return 0;
    }

    /**
     * count children of the given category id using SOLR
     * this only works if categoryIDs are set hierarchically:
     * child category ids MUST include the parent category id at the beginning
     * 
     * @param cb: the classification browser configuration
     * @param categid - the category id
     * @return the number of children
     */
    private int countChildrenBySearch(CBConfig cb, String categid) {
        try {
            return cacheHitCount.get(generateQuery(cb, categid) + "?*");
        } catch (ExecutionException e) {
            // do nothing
        }
        return 0;
    }
}

class CBConfig {
    private static final String PROP_PREFIX = "MCR.ClassBrowser.";

    /*
     * required: set the ID of the classification to be shown
     */
    public String classification;

    /*
     * optional: a category ID, which should be uses as parent Only child
     * objects of this category will be displayed
     * 
     * @param categID - the category ID as string - default: empty string
     */
    public String category;

    /*
     * optional: Decide, if the number of items within the classification shall
     * be shown. Counting is a time consuming process
     * 
     * @param b - a boolean value - default: false
     */
    public boolean count;

    /*
     * optional: Decide, if empty leaves shall be hidden Only categories, that
     * contain items will be displayed
     * 
     * @param b - a boolean value - default: true
     */
    public boolean hideemptyleaves;

    /*
     * optional: Decide, how deep the classification shall be displayed
     * 
     * @param level - an integer value - default is "-1" for infinite
     * 
     */
    public int level;

    /*
     * optional: Decide, if the whole classification tree shall be display at
     * once All items will be visible at the first call
     * 
     * @param expand - a boolean value - default is "false"
     */
    public boolean expand;

    /*
     * optional: Decide, if the description of a category shall be displayed
     * 
     * @param showdescription - a boolean value - default is "false"
     */
    public boolean showdescription;

    /*
     * optional: Decide, if the URI of a category shall be displayed
     * 
     * @param showuri - a boolean value - default is "false"
     */
    public boolean showuri;

    /*
     * optional: Decide, if the ID of a category shall be displayed
     * 
     * @param showid - a boolean value - default is "false"
     */
    public boolean showid;

    /*
     * optional: Decide, if for all items (including empty ones) a link shall be
     * created Could be used in subselects. All items will be visible at the
     * first call
     * 
     * @param linkall - a boolean value - default is "false"
     */
    public boolean linkall;

    /*
     * optional: Specify a restriction for the search for items in a category
     * This allows you to hide certain items.
     * 
     * @param searchrestriction a string, which must be a valid query in MCR
     * textual syntax
     */
    public String filter;

    /*
     * optional: Specify a sorting for the entries belonging to the classification in SOLR syntax
     * @param sort tion a string, which must be a valid query in MCR
     * textual syntax
     */
    public String sortResult;

    public CBConfig(String mode) {
        classification = MCRConfiguration2.getString(PROP_PREFIX + mode + ".Classification").orElseThrow();

        category = MCRConfiguration2.getString(PROP_PREFIX + mode + ".Category").orElse("");

        count = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".Count").orElse(false);

        hideemptyleaves = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".HideEmptyLeaves").orElse(true);

        level = MCRConfiguration2.getInt(PROP_PREFIX + mode + ".Level").orElse(100_000);

        expand = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".Expand").orElse(false);

        showdescription = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".ShowDescription").orElse(false);
        showuri = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".ShowUri").orElse(false);
        showid = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".ShowId").orElse(false);

        linkall = MCRConfiguration2.getBoolean(PROP_PREFIX + mode + ".ShowLinkall").orElse(false);

        filter = MCRConfiguration2.getString(PROP_PREFIX + mode + ".Filter").orElse(null);

        sortResult = MCRConfiguration2.getString(PROP_PREFIX + mode + ".SortResult").orElse(null);
    }
}
