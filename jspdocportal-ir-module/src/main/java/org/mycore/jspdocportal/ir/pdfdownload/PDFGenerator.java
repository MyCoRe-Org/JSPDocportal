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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.ir.pdfdownload.util.PDFFrontpageUtil;
import org.mycore.jspdocportal.ir.pdfdownload.util.PDFTOCUtil;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

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
    private Path dataDir;
    private String recordIdentifier;
    private String mcrid;
    private ServletContext ctx;

    public PDFGenerator(Path pdfOutFile, Path dataDir, String recordIdentifier, String mcrid, ServletContext ctx) {
        this.pdfOutFile = pdfOutFile;
        this.dataDir = dataDir;
        this.recordIdentifier = recordIdentifier;
        this.mcrid = mcrid;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 0);
        Path baseDir = dataDir;
        Path ocrDir = baseDir.resolve("ocrpdf");
        if (Files.exists(ocrDir)) {
            createFromPDFOCR(ocrDir);
        } else {
            Path imgDir = baseDir.resolve("images");
            if (Files.exists(imgDir)) {
                createFromImage(imgDir);
            } else {
                // old derivates - create from METS
                createFromMETS();
                // ToDo generate Download Fehler PDF
            }
        }
    }

    private void createFromPDFOCR(Path imgDir) {
        try {
            //create frontpage
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream frontPageBytes = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, frontPageBytes);

            document.open();
            PDFFrontpageUtil.createFrontPage(writer, document, recordIdentifier, mcrid);
            document.close();
            writer.close();

            //content
            Files.createDirectories(pdfOutFile.getParent());
            Path tmpFile = Paths.get(pdfOutFile.toAbsolutePath().toString() + ".tmp");

            document = new Document(PageSize.A4);
            PdfCopy copy = new PdfCopy(document, Files.newOutputStream(tmpFile));
            document.open();
            PdfReader reader = new PdfReader(new ByteArrayInputStream(frontPageBytes.toByteArray()));
            // loop over the pages in that document
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                copy.addPage(copy.getImportedPage(reader, page));
            }
            copy.freeReader(reader);
            reader.close();

            List<Path> imageFiles = Files.list(imgDir).sorted().toList();
            for (int i = 0; i < imageFiles.size(); i++) {

                reader = new PdfReader(Files.newInputStream(imageFiles.get(i)));
                // loop over the pages in that document
                int n = reader.getNumberOfPages();
                for (int page = 1; page <= n; page++) {
                    copy.addPage(copy.getImportedPage(reader, page));
                }
                copy.freeReader(reader);
                reader.close();

                ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier,
                    (i + 1) * 100 / imageFiles.size());

                // this is just for debgging
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            copy.setOutlines(PDFTOCUtil.createTOC(dataDir, 1));
            document.close();

            Files.move(tmpFile, pdfOutFile, StandardCopyOption.REPLACE_EXISTING);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 101);
        } catch (IOException | DocumentException e) {
            LOGGER.error(e);
        }
    }

    private void createFromImage(Path imgDir) {
        try {
            Document document = new Document();
            Files.createDirectories(pdfOutFile.getParent());
            Path tmpFile = Paths.get(pdfOutFile.toAbsolutePath().toString() + ".tmp");
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(tmpFile));

            document.open();
            PDFFrontpageUtil.createFrontPage(writer, document, recordIdentifier, mcrid);

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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            writer.setOutlines(PDFTOCUtil.createTOC(dataDir, 1));
            document.close();
            writer.close();

            Files.move(tmpFile, pdfOutFile, StandardCopyOption.REPLACE_EXISTING);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 101);
        } catch (IOException | DocumentException e) {
            LOGGER.error(e);
            ctx.removeAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier);
        }
    }

    private void createFromMETS() {
        List<String> imgURLs = new ArrayList<>();

        org.jdom2.Document metsXML = null;
        ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 1);

        try {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
            for (MCRMetaLinkID derID : mcrObj.getStructure().getDerivates()) {
                if ("DV_METS".equals(derID.getXLinkTitle())) {
                    MCRDerivate der = MCRMetadataManager
                        .retrieveMCRDerivate(MCRObjectID.getInstance(derID.getXLinkHref()));
                    String mainDoc = der.getDerivate().getInternals().getMainDoc();

                    URL metsURL =
                        URI.create(MCRFrontendUtil.getBaseURL() + "file/" + mcrid + "/" + der.getId().toString()
                            + "/" + mainDoc).toURL();
                    SAXBuilder sb = new SAXBuilder();
                    metsXML = sb.build(metsURL);

                    XPathExpression<Element> xpImages = XPathFactory.instance().compile(
                        "//mets:fileGrp[@USE='DEFAULT']/mets:file/mets:FLocat", Filters.element(), null, NS_METS);

                    for (Element ef : xpImages.evaluate(metsXML)) {
                        imgURLs.add(ef.getAttributeValue("href", NS_XLINK));
                    }

                    break;
                }
            }

            Document document = new Document();
            Files.createDirectories(pdfOutFile.getParent());
            Path tmpFile = Paths.get(pdfOutFile.toAbsolutePath().toString() + ".tmp");
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(tmpFile));

            document.open();
            PDFFrontpageUtil.createFrontPage(writer, document, recordIdentifier, mcrid);

            Image img;
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
            if (!imgURLs.isEmpty()) {
                writer.setOutlines(PDFTOCUtil.createTOC(metsXML, 1));
            }

            document.close();
            writer.close();

            Files.move(tmpFile, pdfOutFile, StandardCopyOption.REPLACE_EXISTING);
            ctx.setAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier, 101);
        } catch (IOException | JDOMException | DocumentException e) {
            LOGGER.error(e);
            ctx.removeAttribute(SESSION_ATTRIBUTE_PROGRESS_PREFIX + recordIdentifier);
        }
    }
}
