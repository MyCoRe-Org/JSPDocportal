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

package org.mycore.jspdocportal.common.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


/**
 * Resolver vor different identifiers 
 * @author Robert Stephan
 * 
 * @see org.mycore.frontend.servlets.MCRServlet
 */

@jakarta.ws.rs.Path("/resolve/{path: .*}")
public class MCRResolvingController  {
    private static Logger LOGGER = LogManager.getLogger(MCRResolvingController.class);
    
    protected enum OpenBy {
        page, nr, part, empty
    };

    @GET
    public Response doGet(@PathParam("path") String uri, @Context HttpServletRequest request) {
        String path[] = uri.split("/", -1);
        if (path.length < 2) {
            return Response.status(Status.NOT_FOUND).encoding("UTF-8").entity(MCRTranslation.translate("Resolver.error.unknownUrlSchema")).type(MediaType.TEXT_PLAIN_TYPE).build();
            
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
            return Response.status(Status.NOT_FOUND).entity(MCRTranslation.translate("Resolver.error.unknownUrlSchema")).type(MediaType.TEXT_PLAIN_TYPE).build();
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
                    return Response.status(Status.MOVED_PERMANENTLY).header("Location", MCRFrontendUtil.getBaseURL() + "resolve/id/" + id).build();
                }
            } catch (IOException | SolrServerException e) {
                Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
            }
            return Response.status(Status.NOT_FOUND).build();
        }

        String mcrID = null;
        if ("id".equals(key)) {
            mcrID = recalculateMCRObjectID(value);
            if (mcrID == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
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
                // show metadata as docdetails view
                try {
                    MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
                    if (!MCRMetadataManager.exists(mcrObjID)) {
                        throw new MCRException("No object with id '" + mcrID + "' found.");
                    }
                    String view = MCRConfiguration2.getString("MCR.JSPDocportal.Doctails.View").orElse("/docdetails");
                    
                    Map<String, Object> model = new HashMap<>();
                    model.put("id", mcrID);
                    Viewable v = new Viewable(view, model);
                    return Response.ok(v).build();
                    
                } catch (MCRException ex) {
                    return Response.status(Status.NOT_FOUND)
                        .entity("No object with id '" + mcrID + "' found.")
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .build();
                }
            }

        String action = path[2];
        if(action.equals("print")) {
         // show metadata as print preview
            try {
                MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
                if (!MCRMetadataManager.exists(mcrObjID)) {
                    throw new MCRException("No object with id '" + mcrID + "' found.");
                }
                String view = MCRConfiguration2.getString("MCR.JSPDocportal.Doctails.Print").orElse("/printdetails");
                
                Map<String, Object> model = new HashMap<>();
                model.put("id", mcrID);
                Viewable v = new Viewable(view, model);
                return Response.ok(v).build();
                
            } catch (MCRException ex) {
                return Response.status(Status.NOT_FOUND)
                    .entity("No object with id '" + mcrID + "' found.")
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
            }   
        }
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
                String nr = path[4];
                if(nr.matches("phys\\d+")) {
                    nr = nr.replace("phys",  "phys_");
                }
                url = createURLForDFGViewer(request, mcrID, OpenBy.part, nr);
            }
            if (url.length() > 0) {
                LOGGER.debug("DFGViewer URL: " + url);
                return Response.temporaryRedirect(URI.create(url)).build();
                
            }
            return Response.status(Status.NOT_FOUND).build();
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
                String nr = path[4];
                if(nr.matches("phys\\d+")) {
                    nr = nr.replace("phys",  "phys_");
                }
                url = createURLForMyCoReViewer(request, mcrID, OpenBy.part, nr);
            }
            if (url.length() > 0) {
                LOGGER.debug("MyCoReViewer URL: " + url);
                return Response.temporaryRedirect(URI.create(url)).build();
            }
            return Response.status(Status.NOT_FOUND).build();
        }

        if (action.equals("pdf")) {
            StringBuffer sbUrl = createURLForMainDocInDerivateWithLabel(request, mcrID, "fulltext");
            if(sbUrl.length()==0) {
                return Response.status(Status.NOT_FOUND).build();
            }
            if (path.length > 4) {
                if (path[3].equals("page") || path[3].equals("nr")) {
                    sbUrl.append("#page=").append(path[3]);
                }
            }
            LOGGER.debug("PDF URL: " + sbUrl.toString());
            return Response.temporaryRedirect(URI.create(sbUrl.toString())).build();
        }

        if (action.equals("pdfdownload")) {
            // "pdf download is beeing implemented" page
            // this.getServletContext().getRequestDispatcher("/content/pdfdownload.jsp?id="
            // +mcrID).forward(request, response);
            if (key.equals("recordIdentifier")) {
                String url = request.getContextPath() + "/do/pdfdownload/recordIdentifier/" + value; 
                return Response.temporaryRedirect(URI.create(url)).build();
                
            } else {
                return Response.temporaryRedirect(URI.create(request.getContextPath())).build();
            }
        }

        if (action.equals("cover")) {
            StringBuffer url = createURLForMainDocInDerivateWithLabel(request, mcrID, "Cover");
            LOGGER.debug("Cover URL: " + url.toString());
            return Response.temporaryRedirect(URI.create(url.toString())).build();
        }
        
        // used in METS-Files in mets:mptr to resolve METS files of parent or child
        if (action.equals("dv_mets")) {
            StringBuffer url = createURLForMainDocInDerivateWithLabel(request, mcrID, "DV_METS");
            LOGGER.debug("METS for DFG-Viewer: " + url.toString());
            return Response.temporaryRedirect(URI.create(url.toString())).build();
        }

        if (action.equals("fulltext")) {
            if (mcrID.startsWith("mvdok")) {
                String url = MCRFrontendUtil.getBaseURL() + "mjbrenderer?id=" + mcrID;
                return Response.temporaryRedirect(URI.create(url)).build();
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
            return Response.temporaryRedirect(URI.create(sbURL.toString())).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    protected String recalculateMCRObjectID(String oldID) {
        if (oldID == null) {
            return null;
        }
        String newID = oldID
            .replace("cpr_staff_0000", "cpr_person_")
            .replace("cpr_professor_0000", "cpr_person_")
            .replace("_series_", "_bundle_");
        if (MCRObjectID.isValid(newID)) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(newID);
            return mcrObjID.toString();
        }
        return null;
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
                          sbPath.append("file/").append(mcrID).append("/").append(der.getXLinkHref()).append("/").append(maindoc);
                          return sbPath;
                        }
                    }
                }
            }
        }

        return new StringBuffer(MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrID);
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
                          return sbPath.append("file/").append(mcrID).append("/").append(der.getXLinkHref().toString());
                        }
                    }
                }
            }
        }

        return new StringBuffer(MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrID);
    }
    
    //TODO cleanup: first part (resolving from mets IDs) is the same as in method createURLForDFGViewer()
    protected String createURLForMyCoReViewer(HttpServletRequest request, String mcrID, OpenBy openBy, String nr) {

        StringBuffer sbURL = new StringBuffer(MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrID);
        try {
            MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
            for (MCRMetaEnrichedLinkID derMetaLink : o.getStructure().getDerivates()) {
                if (derMetaLink.getClassifications().size() > 0 && 
                    "MCRVIEWER_METS".equals(derMetaLink.getClassifications().get(0).getID())) {
                    MCRObjectID derID = derMetaLink.getXLinkHrefID();
                    Path root = MCRPath.getRootPath(derID.toString());
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
                        for (Path p : ds) {
                            if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".mets.xml")) {
                                Namespace nsMets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
                                Namespace nsXlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
                                Document docMETS = new MCRPathContent(p).asXML();

                                Element eMETSPhysDiv = null;
                                while (nr.startsWith("0")) {
                                    nr = nr.substring(1);
                                }
                                if (!nr.isEmpty()) {
                                    if (openBy == OpenBy.page) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile("/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                + "/mets:div[@TYPE='physSequence']/mets:div[starts-with(@ORDERLABEL, '"
                                                + nr + "')]", Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.nr) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile("/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                + "/mets:div[@TYPE='physSequence']/mets:div[@ORDER='" + nr
                                                + "']", Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.part) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(
                                                "/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                    + "//mets:div[@ID='" + nr + "']",
                                                Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                        if (eMETSPhysDiv == null) {
                                            Element eMETSLogDiv = XPathFactory.instance()
                                                .compile(
                                                    "/mets:mets/mets:structMap[@TYPE='LOGICAL']"
                                                        + "//mets:div[@ID='" + nr + "']",
                                                    Filters.element(), null, nsMets)
                                                .evaluateFirst(docMETS);
                                            if (eMETSLogDiv != null) {
                                                Element eMETSSmLink = XPathFactory.instance().compile(
                                                    "/mets:mets/mets:structLink" + "//mets:smLink[@xlink:from='"
                                                        + eMETSLogDiv.getAttributeValue("ID") + "']",
                                                    Filters.element(), null, nsMets, nsXlink)
                                                    .evaluateFirst(docMETS);
                                                if (eMETSSmLink != null) {
                                                    eMETSPhysDiv = XPathFactory.instance()
                                                        .compile(
                                                            "/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                                + "//mets:div[@ID='"
                                                                + eMETSSmLink.getAttributeValue("to",
                                                                    nsXlink)
                                                                + "']",
                                                            Filters.element(), null, nsMets)
                                                        .evaluateFirst(docMETS);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (eMETSPhysDiv != null) {
                                    sbURL.append("?_mcrviewer_start="+eMETSPhysDiv.getAttributeValue("ID"));
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
        if (!url.contains(".dv.mets.xml")) {
            url = url.replace("dfg-viewer.de/v3", "dfg-viewer.de/show");
        }
        LOGGER.debug("created DFG-ViewerURL: " + request.getContextPath() + " -> " + url);
        return url;
    }
    
    // Create URL for DFG ImageViewer and Forward to it
    // http://dfg-viewer.de/v1/?set%5Bmets%5D=http%3A%2F%2Frosdok.uni-rostock.de%2Fdata%2Fetwas%2Fetwas1737%2Fetwas1737.mets.xml&set%5Bzoom%5D=min
    protected String createURLForDFGViewer(HttpServletRequest request, String mcrID, OpenBy openBy, String nr) {

        String thumb = request.getParameter("thumb");

        StringBuffer sbURL = new StringBuffer("");
        try {
            MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrID));
            for (MCRMetaEnrichedLinkID derMetaLink : o.getStructure().getDerivates()) {
                if (derMetaLink.getClassifications().size() > 0 && 
                    "DV_METS".equals(derMetaLink.getClassifications().get(0).getID())) {
                    MCRObjectID derID = derMetaLink.getXLinkHrefID();
                    Path root = MCRPath.getRootPath(derID.toString());
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
                        for (Path p : ds) {
                            if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".mets.xml")) {
                                Namespace nsMets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
                                Namespace nsXlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
                                Document docMETS = new MCRPathContent(p).asXML();

                                Element eMETSPhysDiv = null;
                                while (nr.startsWith("0")) {
                                    nr = nr.substring(1);
                                }
                                if (!nr.isEmpty()) {
                                    if (openBy == OpenBy.page) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile("/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                + "/mets:div[@TYPE='physSequence']/mets:div[starts-with(@ORDERLABEL, '"
                                                + nr + "')]", Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.nr) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile("/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                + "/mets:div[@TYPE='physSequence']/mets:div[@ORDER='" + nr
                                                + "']", Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                    } else if (openBy == OpenBy.part) {
                                        eMETSPhysDiv = XPathFactory.instance()
                                            .compile(
                                                "/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                    + "//mets:div[@ID='" + nr + "']",
                                                Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                        if (eMETSPhysDiv == null) {
                                            Element eMETSLogDiv = XPathFactory.instance()
                                                .compile(
                                                    "/mets:mets/mets:structMap[@TYPE='LOGICAL']"
                                                        + "//mets:div[@ID='" + nr + "']",
                                                    Filters.element(), null, nsMets)
                                                .evaluateFirst(docMETS);
                                            if (eMETSLogDiv != null) {
                                                Element eMETSSmLink = XPathFactory.instance().compile(
                                                    "/mets:mets/mets:structLink" + "//mets:smLink[@xlink:from='"
                                                        + eMETSLogDiv.getAttributeValue("ID") + "']",
                                                    Filters.element(), null, nsMets, nsXlink)
                                                    .evaluateFirst(docMETS);
                                                if (eMETSSmLink != null) {
                                                    eMETSPhysDiv = XPathFactory.instance()
                                                        .compile(
                                                            "/mets:mets/mets:structMap[@TYPE='PHYSICAL']"
                                                                + "//mets:div[@ID='"
                                                                + eMETSSmLink.getAttributeValue("to",
                                                                    nsXlink)
                                                                + "']",
                                                            Filters.element(), null, nsMets)
                                                        .evaluateFirst(docMETS);
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
                                    List<Element> l = (List<Element>) eMETSPhysDiv.getChildren();
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
                                                Filters.element(), null, nsMets)
                                            .evaluateFirst(docMETS);
                                        if (eFLocat != null) {
                                            sbURL = new StringBuffer(eFLocat.getAttributeValue("href", nsXlink));
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
        if (!url.contains(".dv.mets.xml")) {
            url = url.replace("dfg-viewer.de/v3", "dfg-viewer.de/show");
        }
        LOGGER.debug("created DFG-ViewerURL: " + request.getContextPath() + " -> " + url);
        return url;
    }
}
