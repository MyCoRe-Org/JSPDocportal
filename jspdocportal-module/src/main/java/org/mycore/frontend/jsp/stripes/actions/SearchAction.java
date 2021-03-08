package org.mycore.frontend.jsp.stripes.actions;

import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.frontend.jsp.search.MCRSearchResultDataBean;
import org.mycore.frontend.xeditor.MCREditorSession;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCREditorSessionStoreUtils;
import org.mycore.frontend.xeditor.MCRStaticXEditorFileServlet;
import org.mycore.frontend.xeditor.tracker.MCRChangeData;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.solr.search.MCRQLSearchUtils;
import org.mycore.solr.search.MCRSolrSearchUtils;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * Action Bean for Search Handling ... Query Parameters:
 * 
 * _hit=n and _search=UUID -> retrieves the information of the given searcher
 * and redirects to then n-th hit mask=abc.xed -> Opens a new Editor with the
 * given XED File
 * 
 * @author Stephan
 *
 */
@UrlBinding("/search/{mask}")
public class SearchAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(SearchAction.class);

    public static Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    private static Pattern REGEX_XML_EMPTY_ELEMENTS = Pattern.compile("<(a|i|span|div|textarea)\\s([^>]*)?(\\s)?/>");

    public static int DEFAULT_ROWS = 100;

    ForwardResolution fwdResolutionForm = new ForwardResolution("/content/search/search.jsp");

    private String mask = null;

    private boolean showMask;

    private boolean showResults;

    private MCRSearchResultDataBean result;

    public SearchAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
    }

    @DefaultHandler
    public Resolution defaultRes() {
        getContext().getResponse().setCharacterEncoding("UTF-8");
        getContext().getResponse().setContentType("text/xhtml;charset=utf-8");
        HttpServletRequest request = getContext().getRequest();
        if (request.getParameter("_search") != null) {
            result = MCRSearchResultDataBean.retrieveSearchresultFromSession(request, request.getParameter("_search"));
        }
        if (result != null) {
            Integer hit = null;
            if (request.getParameter("_hit") != null) {
                try {
                    hit = Integer.parseInt(request.getParameter("_hit"));
                } catch (NumberFormatException nfe) {
                    hit = null;
                }
            }
            if (request.getParameter("_start") != null) {
                try {
                    result.setStart(Integer.parseInt(request.getParameter("_start")));
                } catch (NumberFormatException nfe) {
                    result.setStart(0);
                }
            }

            if (hit != null && result != null && hit >= 0 && hit < result.getNumFound()) {
                String mcrid = result.getHit(hit).getMcrid();
                return new RedirectResolution("/resolve/id/" + mcrid + "?_search=" + result.getId());
            }
        }
        showMask = true;
        showResults = false;

        if (request.getParameter("q") != null) {
            result = new MCRSearchResultDataBean();
            result.setRows(DEFAULT_ROWS);
            result.setAction("search");
            result.setQuery(request.getParameter("q"));
            result.setMask("");
            showMask=false;
            showResults = true;
        }

        if (StringUtils.isNoneEmpty(request.getParameter("searchField"))) {
            result = new MCRSearchResultDataBean();
            result.setRows(DEFAULT_ROWS);
            result.setAction("search");
            String value = request.getParameter("searchValue");
            if(StringUtils.isNotBlank(value)) {
                result.setQuery("+" + request.getParameter("searchField") + ":" + value);
                   // does not work work with "xxx*":  + ClientUtils.escapeQueryChars(value));
            }
            else {
                result.setQuery("+" + request.getParameter("searchField") + ":*");
            }
            result.setMask("");
            showMask=false;
            showResults = true;
        }
        if (request.getParameter("sortField") != null && request.getParameter("sortValue") != null) {
            result.setSort(request.getParameter("sortField") + " " + request.getParameter("sortValue"));
            result.setStart(0);
        }
        if (request.getParameter("_sort") != null) {
            result.setSort(request.getParameter("_sort"));
            result.setStart(0);
        }

        if (result == null) {
            result = new MCRSearchResultDataBean();
            result.setRows(DEFAULT_ROWS);
        }
        
        result.setMask(mask);

        if (mask == null) {
            showMask = false;
            showResults = true;
            result.setAction("search");
        } else {
            result.setAction("search/" + mask);
        }
        
        String referrer = request.getHeader("referer");
        if (referrer != null && !referrer.contains("/XEditor") && request.getParameter("_search") == null) {
            result.setBackURL(referrer);
        }
    
        Document queryDoc = (Document) request.getAttribute("MCRXEditorSubmission");
        if (queryDoc == null && result != null) {
            queryDoc = result.getMCRQueryXML();
        }

        if (queryDoc != null) {
            request.setAttribute("MCRXEditorSubmission", queryDoc);
            showMask = false;
            showResults = true;
        }

        if (queryDoc == null) {
            String sessionID = request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
            if (sessionID != null) {
                if (sessionID.contains("-")) {
                    sessionID = sessionID.split("-")[0];
                }

                MCREditorSession session = MCREditorSessionStoreUtils.getSessionStore().getSession(sessionID);
                if (session != null) {
                    queryDoc = session.getXMLCleaner().clean(session.getEditedXML());
                    // if we come from a repeater button we should show mask and
                    // hide result
                    MCRChangeData changeData = session.getChangeTracker().findLastChange(queryDoc);
                    if (changeData != null) {
                        if (changeData.getText().contains("target org.mycore.frontend.xeditor.target.MCRInsertTarget")
                                || changeData.getText()
                                        .contains("target org.mycore.frontend.xeditor.target.MCRRemoveTarget")
                                || changeData.getText().contains("target remove")
                                || changeData.getText().contains("org.mycore.frontend.xeditor.target.MCRSwapTarget")) {
                            showMask = true;
                            showResults = false;

                        }
                    }
                }
            }
        }

        if (queryDoc != null) {
            XMLOutputter xml = new XMLOutputter(Format.getPrettyFormat());
            LOGGER.debug(xml.outputString(queryDoc));
            if (queryDoc.getRootElement().getAttribute("mask") != null) {
                result.setMask(queryDoc.getRootElement().getAttributeValue("mask"));
            }
            if (queryDoc.getRootElement().getChild("conditions").getChildren().size() > 0) {
                result.setMCRQueryXML(queryDoc);
                MCRQuery query = MCRQLSearchUtils.buildFormQuery(queryDoc.getRootElement());

                SolrQuery solrQuery = MCRSolrSearchUtils.getSolrQuery(query, queryDoc, request);
                result.setSolrQuery(solrQuery);
            }
        }

        if (request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM) != null) {
            request.getSession().removeAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask());
            request.getSession().setAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask(),
                    request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM));
        }

        fwdResolutionForm.getParameters().remove(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
        if (queryDoc == null) {
            request.getSession().removeAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask());
        }
        if (request.getSession()
                .getAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask()) != null) {

            fwdResolutionForm.addParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM, request.getSession()
                    .getAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask()));
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
        if (result.getSolrQuery() != null && showResults) {
            MCRSearchResultDataBean.addSearchresultToSession(request, result);
            result.doSearch();
        }

        return fwdResolutionForm;
    }

    public String getXeditorHtml() {
        StringWriter out = new StringWriter();

        MCRContent editorContent = null;
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            URL resource = getClass().getResource("/editor/search/" + result.getMask() + ".xed");
            if (resource != null) {
                editorContent = new MCRURLContent(resource);
            }
            if (editorContent != null) {

                Document doc = editorContent.asXML();
                if (doc.getRootElement().getName().equals("form")
                        && doc.getRootElement().getNamespace().equals(NS_XED)) {
                    editorContent = new MCRJDOMContent(doc);
                    editorContent.setDocType("MyCoReWebPage");

                    HttpServletRequest request = getContext().getRequest();

                    String sessionID = request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
                    if (sessionID != null) {
                        result.setXedSessionId(sessionID);
                        sessionID = sessionID.split("-")[0];
                    }

                    MCRContent newContent = MCRStaticXEditorFileServlet.doExpandEditorElements(editorContent, request,
                            (HttpServletResponse) getContext().getResponse(), sessionID,
                            MCRFrontendUtil.getBaseURL() + "search");
                    String content = null;
                    if (newContent != null) {
                        content = newContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    } else {
                        content = editorContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    }

                    // for proper display of glyhicons
                    // replace "<i class='fa fa-plus' /> with "<i class='fa fa-plus'></i>"
                    Matcher m = REGEX_XML_EMPTY_ELEMENTS.matcher(content);
                    content = m.replaceAll("<$1 $2></$1>");
                    LOGGER.debug("Searchmask-Editor-XML\n" + content);

                    out.append(content);

                } else {
                    LOGGER.error("Search does only allow an <xed:form> element as root.");
                    out.append("<span class=\"error\">Please provide an &lt;xed:form&gt; element here!</span>");

                }
            }
        } catch (Exception e) {
            LOGGER.error("SAXException " + e, e);
        }
        return out.toString();
    }

    public boolean isShowMask() {
        return showMask;
    }

    public boolean isShowResults() {
        return showResults;
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

}
