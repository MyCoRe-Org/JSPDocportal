/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
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
 * 
 */
package org.mycore.jspdocportal.ir.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.ir.depotapi.HashedDirectoryStructure;
import org.mycore.jspdocportal.ir.pdfdownload.PDFGenerator;
import org.mycore.jspdocportal.ir.pdfdownload.PDFGeneratorService;
import org.mycore.solr.MCRSolrClientFactory;

@javax.ws.rs.Path("/do/pdfdownload")
public class MCRPDFDownloadController {
   
    private static final Logger LOGGER = LogManager.getLogger(MCRPDFDownloadController.class);

    @GET
    @javax.ws.rs.Path("recordIdentifier/{recordID}")
    public Response get(@PathParam("recordID") String recordID, @Context HttpServletRequest request, @Context ServletContext servletContext) {
        HashMap<String, Object> model = new HashMap<>();
        
        List<String> errorMessages = new ArrayList<String>();
        model.put("errorMessages",  errorMessages);
        model.put("requestURL", request.getRequestURL().toString());
        
        String path = request.getPathInfo().replace("pdfdownload/recordIdentifier", "").replace("..",
                "");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.length() == 0) {
            return Response.temporaryRedirect(URI.create(request.getContextPath())).build();
        }
        path = path.replace("%25", "%").replace("%2F", "/");
        String recordIdentifier = "";
        if (path.endsWith(".pdf")) {
            // download file if it exists or show progress html
            recordIdentifier = path.substring(0, path.lastIndexOf("/"));
        } else {
            recordIdentifier = path;
        }
        if(!recordIdentifier.contains("/")) {
            recordIdentifier = recordIdentifier.replaceFirst("_", "/");
        }
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery("recordIdentifier:" + recordIdentifier);

        try {
            QueryResponse response = solrClient.query(query);
            SolrDocumentList solrResults = response.getResults();

            if (solrResults.getNumFound() > 0) {
                String filename = recordIdentifier.replace("/", "_") + ".pdf";
                model.put("filename",  filename);

                final Path resultPDF = calculateCacheDir().resolve(recordIdentifier).resolve(filename);
                boolean ready = Files.exists(resultPDF);
                model.put("ready",  ready);

                if (ready) {
                    model.put("filesize", String.format(Locale.GERMANY, "%1.1f%n MB",
                            (double) Files.size(resultPDF) / 1024 / 1024));
                }
                else {
                    model.put("filesize", "O MB");
                }

                if (path.endsWith(".pdf") && ready && getProgress(servletContext, recordIdentifier) < 0) {
                    // download pdf
                    Path fCount = resultPDF.getParent().resolve(resultPDF.getFileName() + ".count");
                    Files.write(fCount, ".".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
                    
                    StreamingOutput stream = new StreamingOutput() {
                        public void write(OutputStream output) throws IOException, WebApplicationException {
                            try {
                                Files.copy(resultPDF, output);
                            } catch (Exception e) {
                                throw new WebApplicationException(e);
                            }
                        }
                    };

                    return Response.ok(stream)
                        .header("Content-Type", "application/pdf")
                        .header("Content-Type", "application/x-download")
                        .header("Content-Disposition", "attachment; filename=" + filename).
                        build();
                }

                String mcrid = String.valueOf(solrResults.get(0).getFirstValue("returnId"));

                if (!ready && getProgress(servletContext, recordIdentifier) < 0) {
                    servletContext.setAttribute(PDFGenerator.SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier,
                            0);
                    Path depotDir = Paths.get(MCRConfiguration2.getString("MCR.depotdir").orElse(""));
                    PDFGeneratorService.execute(new PDFGenerator(resultPDF,
                            HashedDirectoryStructure.createOutputDirectory(depotDir, recordIdentifier),
                            recordIdentifier, mcrid, servletContext));
                }

                if (getProgress(servletContext, recordIdentifier) > 100) {
                    servletContext.removeAttribute(PDFGenerator.SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier);
                }

            } else {
                errorMessages.add("The RecordIdentifier \"<strong>" + recordIdentifier + "\"</strong> is unkown.");
            }
        } catch (SolrServerException | IOException e) {
            LOGGER.error(e);
        }



        model.put("progresss",  getProgress(servletContext, recordIdentifier));
        model.put("recordIdentifier",  recordID.replace("/", "_"));
    
        Viewable v = new Viewable("/pdfdownload", model);
        return Response.ok(v).build();
    }



    public int getProgress(ServletContext servletContext, String recordIdentifier) {
        Integer num = (Integer) servletContext
                .getAttribute(PDFGenerator.SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier);
        if (num == null) {
            return -1;
        } else {
            return num;
        }
    }

    private Path calculateCacheDir() {
        Path cacheDir = Paths.get(MCRConfiguration2.getString("MCR.PDFDownload.CacheDir").orElseThrow());
        return cacheDir;
    }

}
