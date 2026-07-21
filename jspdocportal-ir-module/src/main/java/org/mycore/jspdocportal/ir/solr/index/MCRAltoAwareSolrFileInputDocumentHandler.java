/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
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

package org.mycore.jspdocportal.ir.solr.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRPathContent;
import org.mycore.jspdocportal.ir.depotapi.HashedDirectoryStructure;
import org.mycore.mets.solr.MCRSolrAltoExtractor;
import org.mycore.solr.MCRSolrConstants;
import org.mycore.solr.MCRSolrIndexType;
import org.mycore.solr.index.file.MCRSolrFileIndexBaseAccumulator;
import org.mycore.solr.index.handlers.document.MCRSolrInputDocumentHandler;
import org.mycore.solr.index.statistic.MCRSolrIndexStatistic;
import org.mycore.solr.index.statistic.MCRSolrIndexStatisticCollector;


/**
 * 
 */
public class MCRAltoAwareSolrFileInputDocumentHandler extends MCRSolrInputDocumentHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Path DEPOT_DIR = Paths.get(MCRConfiguration2.getString("MCR.depotdir").get());

    private MCRSolrAltoExtractor altoExtractor = new MCRSolrAltoExtractor();
    private MCRSolrFileIndexBaseAccumulator fileBaseAccumulator = new MCRSolrFileIndexBaseAccumulator();

    protected Path file;

    protected BasicFileAttributes attrs;

    // get all xlink:href attributes with alto files
    private XPathExpression<Attribute> XPATH_ALTO_FILE_HREF = XPathFactory.instance().compile(
        "/mets:mets/mets:fileSec/mets:fileGrp[@ID='ALTO' or @USE='ALTO']/mets:file/mets:FLocat/@xlink:href",
        Filters.attribute(), null, MCRConstants.METS_NAMESPACE, MCRConstants.XLINK_NAMESPACE);

    public MCRAltoAwareSolrFileInputDocumentHandler(Supplier<SolrInputDocument> documentSupplier, String id,
        MCRSolrIndexType coreType, Path file) {
        super(documentSupplier, id, coreType);
        this.file = file;
    }

    @Override
    public void index() throws SolrServerException, IOException {
        super.index();
        for (SolrClient client : getClients()) {
            processAltoFilesFromDepot(file, client);
        }
    }

    private void processAltoFilesFromDepot(Path metsFile, SolrClient solrClient) {
        if (metsFile.getFileName().toString().endsWith(".mcrviewer.mets.xml")) {
            try {
                Document doc = new MCRPathContent(metsFile).asXML();
                String recordId = doc.getRootElement().getAttributeValue("OBJID").replace("/", "_");
                for (Attribute a : XPATH_ALTO_FILE_HREF.evaluate(doc)) {
                    Path altoFile =
                        HashedDirectoryStructure.createOutputDirectory(DEPOT_DIR, recordId).resolve(a.getValue());
                    SolrInputDocument solrDoc =
                        createSolrInputDocForAltoFile(recordId, metsFile, a.getValue(), altoFile);

                    UpdateRequest updateRequest = getUpdateRequest(MCRSolrConstants.SOLR_UPDATE_PATH);
                    updateRequest.add(solrDoc);
                    String fileName = altoFile.getFileName().toString();
                    try {
                        updateRequest.process(solrClient);
                        LOGGER.info("Successfully indexed alto document for file '{}'.", fileName);
                    } catch (Exception ex) {
                        LOGGER.error(() -> "FATAL: Could not index alto document for file '" + fileName + "'.", ex);
                    }
                }
            } catch (JDOMException | IOException e) {
                // TODO Auto-generated catch block
                LOGGER.error("Error processing Mets file", e);
            }
        }
    }

    private SolrInputDocument createSolrInputDocForAltoFile(String recordId, Path metsFile,
        String altoFilePath, Path altoFile) throws IOException, JDOMException {
        SolrInputDocument solrDoc = new SolrInputDocument();
        
        //get basic solr fields for file from "parent" METS file
        fileBaseAccumulator.accumulate(solrDoc, metsFile,
            Files.readAttributes(metsFile, BasicFileAttributes.class));
        String cleanFilePath = altoFilePath.startsWith("/") ? altoFilePath : "/" + altoFilePath;

        //override some solr fields with data from the current alto file
        BasicFileAttributes pAltoAttrs = Files.readAttributes(altoFile, BasicFileAttributes.class);
        altoExtractor.extract(new MCRPathContent(altoFile).asXML().getRootElement(), solrDoc);
        
        //orig:     id=ifs2:/rosdok_derivate_0000157586:/rosdok_ppn775691062.repos.mets.xml
        //modified: id=ifs_depot:/rosdok_ppn1234567890:/alto/phys_0001.alto.xml
        solrDoc.setField("id", "ifs_depot:/" + recordId + ":" + cleanFilePath);
        solrDoc.setField("objectType", "data_file_alto");
        solrDoc.setField("fileName", altoFile.getFileName().toString());
        solrDoc.setField("filePath", cleanFilePath);
        solrDoc.setField("stream_name", cleanFilePath);
        solrDoc.setField("stream_size", pAltoAttrs.size());
        solrDoc.setField("content", solrDoc.getFieldValue("alto_content"));
        return solrDoc;
    }

    @Override
    public MCRSolrIndexStatistic getStatistic() {
        return MCRSolrIndexStatisticCollector.FILE_TRANSFER;
    }

    @Override
    public String toString() {
        return "index " + this.file;
    }

}
