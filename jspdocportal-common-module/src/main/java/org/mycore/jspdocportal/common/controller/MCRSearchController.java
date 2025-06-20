package org.mycore.jspdocportal.common.controller;

import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.xeditor.MCREditorSession;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCREditorSessionStoreUtils;
import org.mycore.frontend.xeditor.MCRStaticXEditorFileServlet;
import org.mycore.frontend.xeditor.tracker.MCRChangeData;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.search.MCRSearchResultDataBean;
import org.mycore.resource.MCRResourceHelper;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.solr.search.MCRQLSearchUtils;
import org.mycore.solr.search.MCRSolrSearchUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

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
@Path("/do/search")
public class MCRSearchController {
    private static final String PARAM_MASK = "mask";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Namespace NS_XED = Namespace.getNamespace("xed", "http://www.mycore.de/xeditor");

    private static final Pattern REGEX_XML_EMPTY_ELEMENTS =
        Pattern.compile("<(a|i|span|div|textarea)\\s([^>]*)?(\\s)?/>");

    public static final int DEFAULT_ROWS = 100;

    private MCRSearchResultDataBean result;

    @GET
    public Response resolveRes(@Context HttpServletRequest request,
        @Context HttpServletResponse response) {
        return defaultRes(null, request, response);
    }

    @POST
    @Path("/{mask}")
    public Response submit(@PathParam(PARAM_MASK) String mask, @Context HttpServletRequest request,
        @Context HttpServletResponse response) {
        return defaultRes(mask, request, response);
    }

    @GET
    @Path("/{mask}")
    public Response defaultRes(@PathParam(PARAM_MASK) String mask, @Context HttpServletRequest request,
        @Context HttpServletResponse response) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable(("/search/search"), model);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xhtml;charset=utf-8");

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
                return Response
                    .temporaryRedirect(
                        URI.create(request.getContextPath() + "/resolve/id/" + mcrid + "?_search=" + result.getId()))
                    .build();
            }
        }
        AtomicBoolean showMask = new AtomicBoolean(true);
        AtomicBoolean showResults = new AtomicBoolean(false);

        if (request.getParameter("q") != null) {
            result = new MCRSearchResultDataBean();
            result.setRows(DEFAULT_ROWS);
            result.setAction("do/search");
            result.setQuery(request.getParameter("q"));
            result.setMask("");
            showMask.set(false);
            showResults.set(true);
        }

        if (StringUtils.isNoneEmpty(request.getParameter("searchField"))) {
            result = new MCRSearchResultDataBean();
            result.setRows(DEFAULT_ROWS);
            result.setAction("do/search");
            String value = request.getParameter("searchValue");
            if (StringUtils.isNotBlank(value)) {
                result.setQuery("+" + request.getParameter("searchField") + ":" + value);
                // does not work work with "xxx*": + ClientUtils.escapeQueryChars(value));
            } else {
                result.setQuery("+" + request.getParameter("searchField") + ":*");
            }
            result.setMask("");
            showMask.set(false);
            showResults.set(true);
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
            showMask.set(false);
            showResults.set(true);
            result.setAction("do/search");
        } else {
            result.setAction("do/search/" + mask);
        }

        String referrer = request.getHeader("referer");
        if (referrer != null && !referrer.contains("/XEditor") && request.getParameter("_search") == null) {
            result.setBackURL(referrer);
        }

        initializeQueryDoc(request, showMask, showResults);

        //TODO - Prüfen, ob notwendig ... Parameter-Übergabe für XEditor
        /*
        fwdResolutionForm.getParameters().remove(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
        
        if (request.getSession()
                .getAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask()) != null) {
        
            fwdResolutionForm.addParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM, request.getSession()
                    .getAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask()));
        }
        */

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
        if (result.getSolrQuery() != null && showResults.get()) {
            MCRSearchResultDataBean.addSearchresultToSession(request, result);
            result.doSearch();
        }

        model.put(PARAM_MASK, mask);
        model.put("showMask", showMask.get());
        model.put("showResults", showResults.get());
        model.put("result", result);
        model.put("xeditorHtml", createXeditorHtml(request, response));

        return Response.ok(v).build();

    }

    private void initializeQueryDoc(HttpServletRequest request, AtomicBoolean showMask, AtomicBoolean showResults) {
        Document queryDoc = (Document) request.getAttribute("MCRXEditorSubmission");
        if (queryDoc == null && result != null) {
            queryDoc = result.getMCRQueryXML();
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
                            showMask.set(true);
                            showResults.set(false);

                        }
                    }
                }
            }
        } else {
            request.setAttribute("MCRXEditorSubmission", queryDoc);
            showMask.set(false);
            showResults.set(true);
            XMLOutputter xml = new XMLOutputter(Format.getPrettyFormat());
            String xmlMessage = xml.outputString(queryDoc);
            LOGGER.debug("{}", xmlMessage);
            if (queryDoc.getRootElement().getAttribute(PARAM_MASK) != null) {
                result.setMask(queryDoc.getRootElement().getAttributeValue(PARAM_MASK));
            }
            if (!queryDoc.getRootElement().getChild("conditions").getChildren().isEmpty()) {
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

        if (queryDoc == null) {
            request.getSession().removeAttribute(MCREditorSessionStore.XEDITOR_SESSION_PARAM + "_" + result.getMask());
        }
    }

    private String createXeditorHtml(HttpServletRequest request, HttpServletResponse response) {
        StringWriter out = new StringWriter();

        MCRContent editorContent = null;
        try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
            URL resource = MCRResourceHelper.getResourceUrl("/editor/search/" + result.getMask() + ".xed");
            if (resource != null) {
                editorContent = new MCRURLContent(resource);
            }
            if (editorContent != null) {

                Document doc = editorContent.asXML();
                if (doc.getRootElement().getName().equals("form")
                    && doc.getRootElement().getNamespace().equals(NS_XED)) {
                    editorContent = new MCRJDOMContent(doc);
                    editorContent.setDocType("MyCoReWebPage");

                    String sessionID = request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
                    if (sessionID != null) {
                        result.setXedSessionId(sessionID);
                        sessionID = sessionID.split("-")[0];
                    }

                    MCRContent newContent = MCRStaticXEditorFileServlet.doExpandEditorElements(editorContent, request,
                        response, sessionID,
                        MCRFrontendUtil.getBaseURL() + "do/search/" + result.getMask());
                    String content;
                    if (newContent != null) {
                        content = newContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    } else {
                        content = editorContent.asString().replaceAll("<\\?xml.*?\\?>", "");
                    }

                    // for proper display of glyhicons
                    // replace "<i class='fa fa-plus' /> with "<i class='fa fa-plus'></i>"
                    Matcher m = REGEX_XML_EMPTY_ELEMENTS.matcher(content);
                    content = m.replaceAll("<$1 $2></$1>");
                    LOGGER.debug("Searchmask-Editor-XML\n{}", content);

                    out.append(content);

                } else {
                    LOGGER.error("Search does only allow an <xed:form> element as root.");
                    out.append("<span class=\"error\">Please provide an &lt;xed:form&gt; element here!</span>");

                }
            }
        } catch (Exception e) {
            LOGGER.error("SAXException", e);
        }
        return out.toString();
    }
}
