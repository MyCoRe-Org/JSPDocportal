/*
 * $RCSfile$
 * $Revision: 19974 $ $Date: 2011-02-20 12:23:20 +0100 (So, 20 Feb 2011) $
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

package org.mycore.jspdocportal.common.legacy;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.controller.MCRResolvingController;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet response the MCRObject certain by the call path
 * <em>.../receive/MCRObjectID</em> or
 * <em>.../servlets/MCRObjectServlet/id=MCRObjectID[&amp;XSL.Style=...]</em>.
 * 
 * @author Robert Stephan
 * 
 * @see MCRResolvingController
 * @deprecated
 */

@Deprecated
public class MCRJSPGlobalResolverServlet extends MCRJSPIDResolverServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = LogManager.getLogger(MCRJSPGlobalResolverServlet.class);

   /**
     * The initalization of the servlet.
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        super.init();
    }

    /**
     * The method replace the default form MCRSearchServlet and redirect the
     * 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String uri = request.getRequestURI();
        String path[] = uri.substring(uri.indexOf("/resolve/") + 9).split("/", -1);
        if (path.length < 2) {
            response.sendError(404, MCRTranslation.translate("Resolver.error.unknownUrlSchema"));
            return;
        }
        String key = path[0];
        String value = path[1];
        
        //cleanup value from anchors, parameters, session ids 
        for (String s : Arrays.asList("#", "?", ";")) {
            if (value.contains(s)) {
                value = value.substring(0, value.indexOf(s));
            }
        }
        if(value.isEmpty()) {
            response.sendError(404, MCRTranslation.translate("Resolver.error.unknownUrlSchema"));
            return;  
        }
        //GND resolving URL for profkat
        if ("gnd".equals(key)) {
            //"gnd_uri": "http://d-nb.info/gnd/14075444X"
            try {
                SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
                SolrQuery solrQuery = new SolrQuery();
                solrQuery.setQuery("gnd_uri:" + MCRSolrUtils.escapeSearchValue("http://d-nb.info/gnd/" + value.trim()));
                solrQuery.setFields("id");
                QueryResponse solrResponse = solrClient.query(solrQuery);
                SolrDocumentList solrResults = solrResponse.getResults();

                Iterator<SolrDocument> it = solrResults.iterator();
                if (it.hasNext()) {
                    SolrDocument doc = it.next();
                    String id = String.valueOf(doc.getFirstValue("id"));
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", MCRFrontendUtil.getBaseURL() + "resolve/id/" + id);
                }
            } catch (SolrServerException e) {
                LOGGER.error(e);
            }
            return;
        }

        String mcrID = null;
        if ("id".equals(key)) {
            mcrID = recalculateMCRObjectID(value);
        } else {
            try {
                value = URLDecoder.decode(URLDecoder.decode(value, "UTF-8"), "UTF-8");
                if("recordIdentifier".equals(key) && !value.contains("/")) {
                    value = value.replaceFirst("_", "/");
                }

                SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
                SolrQuery solrQuery = new SolrQuery(key + ":" + ClientUtils.escapeQueryChars(value));
                solrQuery.setRows(1);
                QueryResponse solrQueryResponse = solrClient.query(solrQuery);
                SolrDocumentList solrResults = solrQueryResponse.getResults();
                if (solrResults.getNumFound() > 0) {
                    mcrID = String.valueOf(solrResults.get(0).getFirstValue("returnId"));
                }

            } catch (SolrServerException | IOException e) {
                LOGGER.error(e);
            }
        }

        if (path.length == 2) {
            if ("xml".equals(request.getParameter("open"))) {
                Document doc = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID)).createXML();
                response.setContentType("text/xml");
                XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
                xout.output(doc, response.getOutputStream());
            } else {
                // show metadata as docdetails view
                try {
                    MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
                    if (!MCRMetadataManager.exists(mcrObjID)) {
                        throw new MCRException("No object with id '" + mcrID + "' found.");
                    }
                    String view = MCRConfiguration2.getString("MCR.JSPDocportal.Doctails.View").orElse("/content/docdetails.jsp");
                    getServletContext().getRequestDispatcher(view + "?id=" + mcrID).forward(request,
                        response);
                } catch (MCRException ex) {
                    request.setAttribute("mcr_exception", ex);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No object with id '" + mcrID + "' found.");
                }
            }
            return;
        }

        String action = path[2];
        if (action.equals("dfgviewer")) {
            String url = "";
            if (path.length == 3 || path.length == 4) {
                url = createURLForDFGViewer(request, mcrID, OpenBy.empty, "");
            }
            if (path.length > 4 && path[3].equals("page")) {
                url = createURLForDFGViewer(request, mcrID, OpenBy.page, path[4]);
            }
            if (path.length > 4 && path[3].equals("nr")) {
                url = createURLForDFGViewer(request, mcrID, OpenBy.nr, path[4]);
            }
            if (path.length > 4 && path[3].equals("part")) {
                url = createURLForDFGViewer(request, mcrID, OpenBy.part, path[4]);
            }
            if (url.length() > 0) {
                LOGGER.debug("DFGViewer URL: " + url);
                response.sendRedirect(url);
            }
            return;
        }
        
        if (action.equals("image")) {
            String url = "";
            if (path.length == 3 || path.length == 4) {
                url = createURLForMyCoReViewer(request, mcrID, OpenBy.empty, "");
            }
            if (path.length > 4 && path[3].equals("page")) {
                url = createURLForMyCoReViewer(request, mcrID, OpenBy.page, path[4]);
            }
            if (path.length > 4 && path[3].equals("nr")) {
                url = createURLForMyCoReViewer(request, mcrID, OpenBy.nr, path[4]);
            }
            if (path.length > 4 && path[3].equals("part")) {
                url = createURLForMyCoReViewer(request, mcrID, OpenBy.part, path[4]);
            }
            if (url.length() > 0) {
                LOGGER.debug("MyCoReViewer URL: " + url);
                response.sendRedirect(url);
            }
            return;
        }

        if (action.equals("pdf")) {
            StringBuffer sbUrl = createURLForMainDocInDerivateWithLabel(request, mcrID, "fulltext");
            if(sbUrl.length()==0) {
                response.sendError(404);
                return;
            }
            if (path.length > 4) {
                if (path[3].equals("page") || path[3].equals("nr")) {
                    sbUrl.append("#page=").append(path[3]);
                }
            }
            LOGGER.debug("PDF URL: " + sbUrl.toString());
            response.sendRedirect(sbUrl.toString());
            return;
        }

        if (action.equals("pdfdownload")) {
            // "pdf download is beeing implemented" page
            // this.getServletContext().getRequestDispatcher("/content/pdfdownload.jsp?id="
            // +mcrID).forward(request, response);
            if (key.equals("recordIdentifier")) {
                response.sendRedirect(response
                    .encodeRedirectURL(request.getContextPath() + "/do/pdfdownload/recordIdentifier/" + value));
            } else {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath()));
            }
            return;
        }

        if (action.equals("cover")) {
            StringBuffer url = createURLForMainDocInDerivateWithLabel(request, mcrID, "Cover");
            LOGGER.debug("Cover URL: " + url.toString());
            response.sendRedirect(url.toString());
            return;
        }
        
        // used in METS-Files in mets:mptr to resolve METS files of parent or child
        if (action.equals("dv_mets")) {
            StringBuffer url = createURLForMainDocInDerivateWithLabel(request, mcrID, "DV_METS");
            LOGGER.debug("METS for DFG-Viewer: " + url.toString());
            response.sendRedirect(url.toString());
            return;
        }

        if (action.equals("fulltext")) {
            if (mcrID.startsWith("mvdok")) {
                String url = MCRFrontendUtil.getBaseURL() + "mjbrenderer?id=" + mcrID;
                response.sendRedirect(url);
            }
        }

        if (action.equals("file") && path.length > 3) {
            String label = path[3];
            long id = -1;
            try {
                id = Integer.parseInt(label);
            } catch (NumberFormatException nfe) {
                // do nothing -> id = -1;
            }
            if (id > 0) {
                MCRObjectID mcrMetaID = MCRObjectID.getInstance(mcrID);
                label = MCRObjectID.getInstance(mcrMetaID.getProjectId() + "_derivate_" + label).toString();
            }
            StringBuffer sbURL = null;
            if (path.length == 4) {
                sbURL = createURLForMainDocInDerivateWithLabel(request, mcrID, label);
            } else {
                sbURL = createRootURLForDerivateWithLabel(request, mcrID, label);
                // display file on remaining path
                for (int i = 4; i < path.length; i++) {
                    sbURL.append("/").append(path[i]);
                }
            }
            response.sendRedirect(sbURL.toString());
            return;
        }
    }
}
