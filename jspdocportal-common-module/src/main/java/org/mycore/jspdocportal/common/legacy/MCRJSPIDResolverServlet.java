/*
 * $RCSfile$
 * $Revision$ $Date$
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
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.controller.MCRResolvingController;
import org.mycore.solr.MCRSolrCoreManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;

/**
 * This servlet response the MCRObject certain by the call path
 * <em>.../receive/MCRObjectID</em> or
 * <em>.../servlets/MCRObjectServlet/id=MCRObjectID[&amp;XSL.Style=...]</em>.
 * 
 * @author Robert Stephan
 * 
 * @see org.mycore.frontend.servlets.MCRServlet
 * 
 * @see MCRResolvingController
 * @deprecated
 */
@Deprecated
public class MCRJSPIDResolverServlet extends HttpServlet {
    private static final String PATH__RESOLVE_ID = "resolve/id/";

    private static final String TYPE_LOGICAL = "LOGICAL";

    private static final String TYPE_PHYSICAL = "PHYSICAL";

    private static final String STRUCTMAP__TYPE_PHYSICAL = "/mets:mets/mets:structMap[@TYPE='PHYSICAL']";

    protected enum OpenBy {
        PAGE, NR, PART, EMPTY
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final Namespace NS_METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    
    private static final Namespace NS_XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    /**
     * The method replace the default form MCRSearchServlet and redirect the
     * 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String pdf = request.getParameter("pdf");
        String img = request.getParameter("img");
        String mcrID = null;
        String queryString = "";

        String[] keys = { "id", "ppn", "urn" };
        for (String key : keys) {
            if (request.getParameterMap().containsKey(key)) {
                String value = request.getParameter(key);
                if (key.equals("id")) {
                    value = recalculateMCRObjectID(value);
                    mcrID = value;
                }
                if (value != null) {
                    queryString = key + ":\"" + value + "\"";
                }
                break;
            }
        }

        if (queryString.length() == 0) {
            getServletContext().getRequestDispatcher("/nav?path=~mycore-error&messageKey=IdNotGiven").forward(request,
                response);
        } else {
            SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
            SolrQuery query = new SolrQuery();
            query.setQuery(queryString);

            try {
                QueryRequest queryRequest = new QueryRequest(query);
                MCRSolrAuthenticationManager.getInstance().applyAuthentication(queryRequest,
                    MCRSolrAuthenticationLevel.SEARCH);
                QueryResponse solrResponse = queryRequest.process(solrClient);
                SolrDocumentList solrResults = solrResponse.getResults();

                if (solrResults.getNumFound() > 0) {
                    mcrID = solrResults.get(0).getFirstValue("id").toString();
                    if (pdf != null) {
                        StringBuffer sbURL;
                        String page = request.getParameter("page");
                        String nr = request.getParameter("nr");
                        if (solrResults.get(0).containsKey("ir.pdffulltext_url")) {
                            sbURL = new StringBuffer(MCRFrontendUtil.getBaseURL());
                            sbURL.append(solrResults.get(0).getFirstValue("ir.pdffulltext_url").toString());
                        } else {
                            sbURL = createURLForMainDocInDerivateWithLabel(request, mcrID, "fulltext");
                        }
                        if (page != null) {
                            sbURL.append("#page=").append(page);
                        } else if (nr != null) {
                            sbURL.append("#page=").append(nr);
                        }
                        response.sendRedirect(sbURL.toString());
                        return;

                    } else if (img != null) {
                        String url = "";
                        String page = request.getParameter("page");
                        if (page != null) {
                            url = createURLForDFGViewer(request, mcrID, OpenBy.PAGE, page);
                        }
                        String nr = request.getParameter("nr");
                        if (nr != null) {
                            url = createURLForDFGViewer(request, mcrID, OpenBy.NR, nr);
                        }
                        String part = request.getParameter("part");
                        if (part != null) {
                            url = createURLForDFGViewer(request, mcrID, OpenBy.PART, part);
                        }

                        if (url.isEmpty()) {
                            url = createURLForDFGViewer(request, mcrID, OpenBy.EMPTY, "");
                        }
                        if (url.length() > 0) {
                            LOGGER.debug("DFGViewer URL: {}", url);
                            response.sendRedirect(url);
                            return;
                        }
                    } //end [if(img!=null)] else{
                    response.sendRedirect(MCRFrontendUtil.getBaseURL() + PATH__RESOLVE_ID + mcrID);

                }
            } catch (Exception e) {
                LOGGER.error(e);
                if (mcrID != null) {
                    response.sendRedirect(MCRFrontendUtil.getBaseURL() + PATH__RESOLVE_ID + mcrID);
                }
            }
        }
    }

    protected String recalculateMCRObjectID(String oldID) {
        if (oldID == null) {
            return null;
        }
        String newID = oldID.replace("cpr_staff_0000", "cpr_person_").replace("cpr_professor_0000", "cpr_person_");
        newID = newID.replace("_series_", "_bundle_");
        MCRObjectID mcrObjID = MCRObjectID.getInstance(newID);
        return mcrObjID.toString();
    }

    // createURL for HTML Page
    protected String createURLForHTML(HttpServletRequest request, String mcrID) {
        String anchor = request.getParameter("anchor");
        StringBuffer fileURL = createURLForMainDocInDerivateWithLabel(request, mcrID, "HTML");
        if (anchor != null) {
            fileURL.append('#').append(anchor);
        }
        return fileURL.toString();
    }

    protected StringBuffer createURLForMainDocInDerivateWithLabel(HttpServletRequest request, String mcrID,
        String label) {
        MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
        MCRObjectStructure structure = o.getStructure();
        for (MCRMetaEnrichedLinkID der : structure.getDerivates()) {
            for(Content c: der.getContentList()) {
                if(c instanceof Element && ((Element) c).getName().equals("classification") && "derivate_types".equals(((Element) c).getAttributeValue("classid"))
                        && label.equals(((Element) c).getAttributeValue("categid"))){
                    for(Content c1: der.getContentList()) {
                        if(c1 instanceof Element && ((Element) c1).getName().equals("maindoc")){
                          String maindoc = ((Element)c1).getTextNormalize();
                          StringBuffer sbPath = new StringBuffer(MCRFrontendUtil.getBaseURL());
                          sbPath.append("file/").append(mcrID).append('/').append(der.getXLinkHref()).append('/').append(maindoc);
                          return sbPath;
                        }
                    }
                }
            }
        }

        return new StringBuffer(MCRFrontendUtil.getBaseURL()).append(PATH__RESOLVE_ID).append(mcrID);
    }

    protected StringBuffer createRootURLForDerivateWithLabel(HttpServletRequest request, String mcrID, String label) {
        MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
        MCRObjectStructure structure = o.getStructure();
        
        for (MCRMetaEnrichedLinkID der : structure.getDerivates()) {
            for(Content c: der.getContentList()) {
                if(c instanceof Element && ((Element) c).getName().equals("classification") && "derivate_types".equals(((Element) c).getAttributeValue("classid"))
                        && label.equals(((Element) c).getAttributeValue("categid"))){
                    for(Content c1: der.getContentList()) {
                        if(c1 instanceof Element && ((Element) c1).getName().equals("maindoc")){
                          StringBuffer sbPath = new StringBuffer(MCRFrontendUtil.getBaseURL());
                          return sbPath.append("file/").append(mcrID).append('/').append(der.getXLinkHref());
                        }
                    }
                }
            }
        }

        return new StringBuffer(MCRFrontendUtil.getBaseURL()).append(PATH__RESOLVE_ID).append(mcrID);
    }

    // Create URL for DFG ImageViewer and Forward to it
    // http://dfg-viewer.de/v1/?set%5Bmets%5D=http%3A%2F%2Frosdok.uni-rostock.de%2Fdata%2Fetwas%2Fetwas1737%2Fetwas1737.mets.xml&set%5Bzoom%5D=min
    protected String createURLForDFGViewer(HttpServletRequest request, String mcrID, OpenBy openBy, String nr) {

        String thumb = request.getParameter("thumb");

        StringBuffer sbURL = new StringBuffer("");
        try {
            MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
            for (MCRMetaLinkID derMetaLink : o.getStructure().getDerivates()) {
                if ("METS".equals(derMetaLink.getXLinkTitle()) || "DV_METS".equals(derMetaLink.getXLinkTitle())) {
                    MCRObjectID derID = derMetaLink.getXLinkHrefID();
                    Path root = MCRPath.getRootPath(derID.toString());
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
                        for (Path p : ds) {
                            if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".mets.xml")) {
                                Document docMETS = new MCRPathContent(p).asXML();

                                Element eMETSPhysDiv = null;
                                while (nr.startsWith("0")) {
                                    nr = nr.substring(1);
                                }
                                if (!nr.isEmpty()) {
                                    if (openBy == OpenBy.PAGE) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(STRUCTMAP__TYPE_PHYSICAL
                                                + "/mets:div[@TYPE='physSequence']/mets:div[starts-with(@ORDERLABEL, '"
                                                + nr + "')]", Filters.element(), null, NS_METS)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.NR) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(STRUCTMAP__TYPE_PHYSICAL
                                                + "/mets:div[@TYPE='physSequence']/mets:div[@ORDER='" + nr
                                                + "']", Filters.element(), null, NS_METS)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.PART) {
                                        eMETSPhysDiv = retrieveStructMapDiv(TYPE_PHYSICAL, nr, docMETS);
                                        if (eMETSPhysDiv == null) {
                                            Element eMETSLogDiv = retrieveStructMapDiv(TYPE_LOGICAL, nr, docMETS);
                                            if (eMETSLogDiv != null) {
                                                Element eMETSSmLink = XPathFactory.instance().compile(
                                                    "/mets:mets/mets:structLink" + "//mets:smLink[@xlink:from='"
                                                        + eMETSLogDiv.getAttributeValue("ID") + "']",
                                                    Filters.element(), null, NS_METS, NS_XLINK)
                                                    .evaluateFirst(docMETS);
                                                if (eMETSSmLink != null) {
                                                    eMETSPhysDiv = retrieveStructMapDiv(TYPE_PHYSICAL, eMETSSmLink.getAttributeValue("to", NS_XLINK), docMETS); 
                                                }
                                            }
                                        }
                                    }
                                }
                                if (thumb == null) {
                                    // display in DFG-Viewer
                                    sbURL = new StringBuffer(MCRConfiguration2.getString("MCR.JSPDocportal.DFG-Viewer.BaseURL").orElseThrow().trim());
                                    sbURL.append("?set[mets]=");
                                    sbURL.append(URLEncoder.encode(MCRFrontendUtil.getBaseURL() + "file/" + mcrID + "/"
                                        + p.toString().replace(":/", "/"), "UTF-8"));
                                    if (eMETSPhysDiv != null) {
                                        String order = eMETSPhysDiv.getAttributeValue("ORDER");
                                        if (order != null) {
                                            sbURL.append("&set[image]=").append(order);
                                        }
                                        //else: phys_000 -> goto first page
                                    }
                                } else if (eMETSPhysDiv != null) {
                                    // return thumb image
                                    List<Element> l = eMETSPhysDiv.getChildren();
                                    String fileid = null;
                                    for (Element e : l) {
                                        if (e.getAttributeValue("FILEID").startsWith("THUMB")) {
                                            fileid = e.getAttributeValue("FILEID");
                                        }
                                    }
                                    if (fileid != null) {
                                        // <mets:file MIMETYPE="image/jpeg"
                                        // ID="THUMBS.matrikel1760-1789-Buetzow_c0001">
                                        // <mets:FLocat LOCTYPE="URL"
                                        // xlink:href="http://rosdok.uni-rostock.de/data/matrikel_handschriften/matrikel1760-1789-Buetzow/THUMBS/matrikel1760-1789-Buetzow_c0001.jpg"
                                        // />
                                        // </mets:file>
                                        Element eFLocat = XPathFactory.instance()
                                            .compile("//mets:file[@ID='" + fileid + "']/mets:FLocat",
                                                Filters.element(), null, NS_METS)
                                            .evaluateFirst(docMETS);
                                        if (eFLocat != null) {
                                            sbURL = new StringBuffer(eFLocat.getAttributeValue("href", NS_XLINK));
                                        }
                                    }
                                }

                            }
                        }
                        break;
                    }
                }
            }
        }

        catch (Exception e) {
            LOGGER.error("Error creating URL for DFG Viewer", e);
            return "";
        }
        String url = sbURL.toString();
        String urlMessage = !url.contains(".dv.mets.xml") ? url.replace("dfg-viewer.de/v3", "dfg-viewer.de/show") : url;
        LOGGER.debug("created DFG-ViewerURL: {} -> {}", request::getContextPath, () -> urlMessage);
        return url;
    }
    
    //TODO cleanup: first part (resolving from mets IDs) is the same as in method createURLForDFGViewer()
    protected String createURLForMyCoReViewer(HttpServletRequest request, String mcrID, OpenBy openBy, String nr) {

        StringBuffer sbURL = new StringBuffer(MCRFrontendUtil.getBaseURL()).append(PATH__RESOLVE_ID).append(mcrID);
        try {
            MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
            for (MCRMetaEnrichedLinkID derMetaLink : o.getStructure().getDerivates()) {
                if (!derMetaLink.getClassifications().isEmpty() && 
                    "MCRVIEWER_METS".equals(derMetaLink.getClassifications().get(0).getID())) {
                    MCRObjectID derID = derMetaLink.getXLinkHrefID();
                    Path root = MCRPath.getRootPath(derID.toString());
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
                        for (Path p : ds) {
                            if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".mets.xml")) {
                                Document docMETS = new MCRPathContent(p).asXML();

                                Element eMETSPhysDiv = null;
                                while (nr.startsWith("0")) {
                                    nr = nr.substring(1);
                                }
                                if (!nr.isEmpty()) {
                                    if (openBy == OpenBy.PAGE) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(STRUCTMAP__TYPE_PHYSICAL
                                                + "/mets:div[@TYPE='physSequence']/mets:div[starts-with(@ORDERLABEL, '"
                                                + nr + "')]", Filters.element(), null, NS_METS)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.NR) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(STRUCTMAP__TYPE_PHYSICAL
                                                + "/mets:div[@TYPE='physSequence']/mets:div[@ORDER='" + nr
                                                + "']", Filters.element(), null, NS_METS)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.PART) {
                                        eMETSPhysDiv = retrieveStructMapDiv(TYPE_PHYSICAL, nr, docMETS);
                                        if (eMETSPhysDiv == null) {
                                            Element eMETSLogDiv = retrieveStructMapDiv(TYPE_LOGICAL, nr, docMETS); 
                                            if (eMETSLogDiv != null) {
                                                Element eMETSSmLink = XPathFactory.instance().compile(
                                                    "/mets:mets/mets:structLink" + "//mets:smLink[@xlink:from='"
                                                        + eMETSLogDiv.getAttributeValue("ID") + "']",
                                                    Filters.element(), null, NS_METS, NS_XLINK)
                                                    .evaluateFirst(docMETS);
                                                if (eMETSSmLink != null) {
                                                    eMETSPhysDiv = retrieveStructMapDiv(TYPE_PHYSICAL, eMETSSmLink.getAttributeValue("to", NS_XLINK), docMETS);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (eMETSPhysDiv != null) {
                                    sbURL.append("?_mcrviewer_start=").append(eMETSPhysDiv.getAttributeValue("ID"));
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        catch (Exception e) {
            LOGGER.error("Error creating URL for DFG Viewer", e);
            return "";
        }
        String url = sbURL.toString();
        String urlMessage = !url.contains(".dv.mets.xml") ? url.replace("dfg-viewer.de/v3", "dfg-viewer.de/show") : url;
        LOGGER.debug("created DFG-ViewerURL: {} -> {}", request::getContextPath, () -> urlMessage);
        return url;
    }

    private Element retrieveStructMapDiv(String type, String nr, Document docMETS) {
        return XPathFactory.instance()
            .compile(
                "/mets:mets/mets:structMap[@TYPE='" + type + "']"
                    + "//mets:div[@ID='" + nr + "']",
                Filters.element(), null, NS_METS)
            .evaluateFirst(docMETS);
    }
}
