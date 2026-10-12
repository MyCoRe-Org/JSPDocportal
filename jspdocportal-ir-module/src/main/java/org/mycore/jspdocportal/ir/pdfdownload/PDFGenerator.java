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
package org.mycore.jspdocportal.ir.pdfdownload;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.ir.pdfdownload.util.PDFBadgeBoxingEvent;
import org.mycore.jspdocportal.ir.pdfdownload.util.PDFFrontpageUtil;
import org.mycore.jspdocportal.ir.pdfdownload.util.PDFTOCUtil;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

import jakarta.servlet.ServletContext;

/**
 * use PDFGeneratorService to run (ensures proper shutdown)
 * 
 * @author Robert Stephan
 *
 */
public class PDFGenerator implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final int DEFAULT_DPI = 300;
    public static final int BORDER = 55; // 2cm (72dpi)
    public static final String SESSION_ATTRIBUTE_PROGRESS_PREFIX = "pdfdownload_progress_";

    private static final Namespace NS_METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace NS_XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    private Path pdfOutFile;
    private Path depotDir;
    private String recordIdentifier;
    private ServletContext ctx;
    private SolrDocument solrDoc;

    public PDFGenerator(Path pdfOutFile, Path depotDir, String recordIdentifier, SolrDocument solrDoc,
        ServletContext ctx) {
        this.pdfOutFile = pdfOutFile;
        this.depotDir = depotDir;
        this.recordIdentifier = recordIdentifier;
        this.solrDoc = solrDoc;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        try {
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 0);
            Files.createDirectories(pdfOutFile.getParent());

            Document document = createNewPDF();
            Path tmpFile = Paths.get(pdfOutFile.toAbsolutePath().toString() + ".tmp");
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(tmpFile));
            writer.setPageEvent(new PDFBadgeBoxingEvent());

            document.open();
            Path ocrDir = depotDir.resolve("ocrpdf");
            if (Files.exists(ocrDir)) {
                createFromPDFOCR(document, writer, ocrDir);
            } else {
                Path imgDir = depotDir.resolve("images");
                if (Files.exists(imgDir)) {
                    createFromImage(document, imgDir);
                } else {
                    // old derivates - create from METS
                    Object reposMetsPath = solrDoc.getFirstValue("ir.reposmets_url");
                    if (reposMetsPath != null) {
                        URL metsURL = URI.create(MCRFrontendUtil.getBaseURL() + String.valueOf(reposMetsPath)).toURL();
                        createFromMETS(document, metsURL);
                    }
                }
            }

            PDFFrontpageUtil.createFrontPage(writer, document, recordIdentifier, solrDoc);

            writer.setOutlines(PDFTOCUtil.createTOC(depotDir, 0));

            document.close();
            writer.close();

            Files.move(tmpFile, pdfOutFile, StandardCopyOption.REPLACE_EXISTING);
            ctx.removeAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier);

        } catch (IOException | DocumentException | JDOMException e) {
            LOGGER.error(e);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 109);
        }
    }

    /** 
     * creates a new PDF and sets PDF document metadata 
     */
    private Document createNewPDF() {
        Document document = new Document(PageSize.A4);
        document.addCreationDate();
        document.addTitle("RosDok Download");
        document.addAuthor("Universitätsbibliothek Rostock");
        document.addSubject("https://purl.uni-rostock.de/" + recordIdentifier.replace("rosdok_", "rosdok/"));
        return document;
    }

    private void createFromPDFOCR(Document document, PdfWriter writer, Path imgDir)
        throws IOException, DocumentException {

        List<Path> imageFiles = Files.list(imgDir).sorted().toList();
        for (int i = 0; i < imageFiles.size(); i++) {

            try (PdfReader reader = new PdfReader(Files.newInputStream(imageFiles.get(i)))) {
                // loop over the pages in that document
                int n = reader.getNumberOfPages();
                for (int page = 1; page <= n; page++) {
                    document.newPage();
                    writer.getDirectContent().addTemplate(writer.getImportedPage(reader, page), 0, 0);
                }
                writer.freeReader(reader);
            }

            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier,
                (i + 1) * 100 / imageFiles.size());

            // this is just for debgging
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private void createFromImage(Document document, Path imgDir) throws IOException, DocumentException {
        Image img;
        List<Path> imageFiles = Files.list(imgDir).sorted().toList();

        @SuppressWarnings("unused")
        Rectangle pageSize = document.getPageSize();
        for (int i = 0; i < imageFiles.size(); i++) {
            //default
            img = Image.getInstance(imageFiles.get(i).toAbsolutePath().toString());
            img.scalePercent(72 * 100 / DEFAULT_DPI);
            img.setAbsolutePosition(0f, 0f);

            document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
            document.setMargins(0f, 0f, 0f, 0f);
            document.newPage();

            document.add(img);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier,
                (i + 1) * 100 / imageFiles.size());

            // this is just for debgging
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private void createFromMETS(Document document, URL metsURL) throws IOException, DocumentException, JDOMException {
        List<String> imgURLs = new ArrayList<>();
        Image img;

        SAXBuilder sb = new SAXBuilder();
        org.jdom2.Document metsXML = sb.build(metsURL);

        XPathExpression<Element> xpImages = XPathFactory.instance().compile(
            "//mets:fileGrp[@USE='DEFAULT']/mets:file/mets:FLocat", Filters.element(), null, NS_METS);

        for (Element ef : xpImages.evaluate(metsXML)) {
            imgURLs.add(ef.getAttributeValue("href", NS_XLINK));
        }

        @SuppressWarnings("unused")
        Rectangle pageSize = document.getPageSize();
        for (int i = 0; i < imgURLs.size(); i++) {
            //default
            img = Image.getInstance(URI.create(imgURLs.get(i)).toURL());
            img.scalePercent(72 * 100 / DEFAULT_DPI);
            img.setAbsolutePosition(0f, 0f);

            document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
            document.setMargins(0f, 0f, 0f, 0f);
            document.newPage();

            document.add(img);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier,
                (i + 1) * 100 / imgURLs.size());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }
}
