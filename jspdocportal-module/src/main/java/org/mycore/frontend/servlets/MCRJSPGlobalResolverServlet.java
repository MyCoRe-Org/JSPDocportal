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

package org.mycore.frontend.servlets;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileNodeServlet;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

/**
 * This servlet response the MCRObject certain by the call path
 * <em>.../receive/MCRObjectID</em> or
 * <em>.../servlets/MCRObjectServlet/id=MCRObjectID[&XSL.Style=...]</em>.
 * 
 * @author Robert Stephan
 * 
 * @see org.mycore.frontend.servlets.MCRServlet
 */
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
                    getServletContext().getRequestDispatcher("/content/docdetails.jsp?id=" + mcrID).forward(request,
                        response);
                } catch (MCRException ex) {
                    request.setAttribute("mcr_exception", ex);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No object with id '" + mcrID + "' found.");
                }
            }
            return;
        }

        String action = path[2];
        if (action.equals("image") || action.equals("dfgviewer")) {
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

        if (action.equals("pdf")) {
            StringBuffer sbUrl = createURLForMainDocInDerivateWithLabel(request, mcrID, "fulltext");
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
                    .encodeRedirectURL(request.getContextPath() + "/pdfdownload/recordIdentifier/" + value));
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

    // CODE under development - try to solve the "Open Large PDF file" problem
    // MyCoRe FileNodeServlet could do the job
    @Deprecated
    @SuppressWarnings("unused")
    private void showDerivateFile(MCRObjectID mcrID, MCRObjectID mcrDerID, String path, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException {
        // OLD CODE
        // the urn with information about the MCRObjectID
        MCRFilesystemNode mainFile = null;
        if (mcrDerID != null) {
            MCRDirectory root = MCRDirectory.getRootDirectory(mcrDerID.toString());
            if (path != null) {
                mainFile = root.getChildByPath(path);
            } else {
                MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(mcrDerID);
                String mainDoc = der.getDerivate().getInternals().getMainDoc();
                if (mainDoc != null) {
                    mainFile = root.getChildByPath(mainDoc);
                }
                if (mainFile == null) {
                    MCRFilesystemNode[] myfiles = root.getChildren(MCRDirectory.SORT_BY_NAME);
                    if (myfiles.length == 1) {
                        mainFile = myfiles[0];
                    }
                }
            }
        }
        if (mainFile != null) {
            String accessErrorPage = MCRConfiguration.instance().getString("MCR.Access.Page.Error", "");
            if (!MCRAccessManager.checkPermissionForReadingDerivate(mainFile.getOwnerID())) {
                LOGGER.info("MCRFileNodeServlet: AccessForbidden to " + mainFile.getName());
                response.sendRedirect(response.encodeRedirectURL(MCRFrontendUtil.getBaseURL() + accessErrorPage));
                return;
            }

            if (mainFile.getPath().endsWith(".pdf")) {
                openPDF(request, response, mcrID.toString(), (MCRFile) mainFile);
                return;
            }
            sendFile(request, response, (MCRFile) mainFile);
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        getServletContext()
            .getRequestDispatcher("/nav?path=~mycore-error&messageKey=MCRJSPGlobalResolver.error.notfound")
            .forward(request, response);
    }

    // openPDF
    private void openPDF(HttpServletRequest request, HttpServletResponse response, String mcrid, MCRFile mcrFile)
        throws IOException {
        String page = request.getParameter("page");
        String nr = request.getParameter("nr");

        StringBuffer sbURL = new StringBuffer(MCRFrontendUtil.getBaseURL());
        sbURL.append("file/").append(mcrid).append("/").append(mcrFile.getPath());
        if (page != null) {
            sbURL.append("#page=").append(page);
        } else if (nr != null) {
            sbURL.append("#page=").append(nr);
        }
        String url = sbURL.toString();
        if (url.length() > 0) {
            response.sendRedirect(url);
        }

    }

       /**
     * Sends the contents of an MCRFile to the client.
     * 
     * @see MCRFileNodeServlet for implementation details
     * 
     * 
     */
    private void sendFile(HttpServletRequest req, HttpServletResponse res, MCRFile file) throws IOException {
        LOGGER.info("Sending file " + file.getName());

        res.setContentType(file.getContentType().getMimeType());
        res.setContentLength((int) file.getSize());
        res.addHeader("Accept-Ranges", "none"); // Advice client not to attempt
                                                // range requests

        // no transaction needed to copy long streams over slow connections

        try (OutputStream out = new BufferedOutputStream(res.getOutputStream())) {
            file.getContentTo(out);
        }
    }
}
