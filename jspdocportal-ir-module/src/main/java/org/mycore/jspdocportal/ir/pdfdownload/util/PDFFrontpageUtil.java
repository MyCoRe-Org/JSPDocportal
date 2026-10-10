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
package org.mycore.jspdocportal.ir.pdfdownload.util;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRExpandedObject;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.resource.MCRResourceHelper;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfImportedPage;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

public class PDFFrontpageUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'", Locale.GERMAN);
    private static final Color COLOR_BLACK = new Color(0, 0, 0);

    public static void createFrontPage(PdfWriter writer, Document document, String recordIdentifier, String mcrid)
        throws DocumentException {
        document.addCreationDate();
        document.addTitle("RosDok Download");
        document.addAuthor("Universitätsbibliothek Rostock");
        document.addSubject("https://purl.uni-rostock.de/" + recordIdentifier.replace("rosdok_", "rosdok/"));

        try (InputStream is = MCRResourceHelper.getResourceAsStream("/rosdok_schriftzug.png")) {
            byte[] imgBytes = IOUtils.toByteArray(is);
            Image img = Image.getInstance(imgBytes);
            img.scalePercent(25f);
            document.add(img);
        } catch (IOException e) {
            //do nothing
        }

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
        document.add(new Paragraph(
            "Dieses Werk wurde Ihnen durch die Universitätsbibliothek Rostock zum Download bereitgestellt.", font));
        document.add(
            new Paragraph("Für Fragen und Hinweise wenden Sie sich bitte an: digibib.ub@uni-rostock.de .", font));
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        document.add(new Paragraph("Das PDF wurde erstellt am: " + DTF.format(zonedDateTime) + ".", font));
        Rectangle rect = new Rectangle(document.left(), document.top() - 30 * 2.54f,
            document.getPageSize().getWidth() - document.rightMargin(), 10);
        rect.setBorder(Rectangle.BOTTOM);
        rect.setBorderColor(COLOR_BLACK);
        rect.setBorderWidth(1f);
        document.add(rect);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
        MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);

        //Cover Image
        try {
            for (MCRMetaLinkID derID : mcrObj.getStructure().getDerivates()) {
                if ("cover".equals(derID.getXLinkTitle())) {
                    MCRDerivate der = MCRMetadataManager
                        .retrieveMCRDerivate(MCRObjectID.getInstance(derID.getXLinkHref()));
                    String mainDoc = der.getDerivate().getInternals().getMainDoc();
                    document.add(Chunk.NEWLINE);
                    document.add(Chunk.NEWLINE);
                    Image img = Image.getInstance(URI.create(MCRFrontendUtil.getBaseURL() + "file/" + mcrid + "/"
                        + der.getId().toString() + "/" + mainDoc).toURL());
                    img.scaleToFit(document.getPageSize().getWidth() * .33f, document.getPageSize().getHeight() * .33f);
                    img.setAlignment(Image.MIDDLE);

                    document.add(img);
                }
            }
        } catch (Exception e) {
            //do nothing - ignore exception
        }

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        //Metadata
        org.jdom2.Document jdomObj = mcrObj.createXML();
        String xslt = "xslt/docdetails/pdffrontpage_html.xsl";
        try {
            MCRXSLTransformer t = MCRXSLTransformer.obtainInstance(xslt);
            ByteArrayOutputStream baosHTML = new ByteArrayOutputStream();
            t.transform(new MCRJDOMContent(jdomObj), baosHTML);
            String htmlContent = cleanUpHTML(baosHTML.toString(StandardCharsets.UTF_8));
            LOGGER.debug(htmlContent);

            /*
            ByteArrayOutputStream baosPDFFrontpage = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            
            //baseurl of file in the root images directory in the proper JAR
            String baseUrl = PDFFrontpageUtil.class.getResource("/META-INF/resources/images/logo_Closed_Access.svg").toExternalForm();
            renderer.setDocumentFromString(htmlContent, baseUrl);
            renderer.layout();
            renderer.createPDF(baosPDFFrontpage);
            
            PdfReader readerMetadata = new PdfReader(baosPDFFrontpage.toByteArray());
            PdfImportedPage pdfImportMetadata = writer.getImportedPage(readerMetadata, 1);

            writer.getDirectContent().addTemplate(pdfImportMetadata, 50, 200);

            //XMLWorkerHelper.getInstance().parseXHtml(writer, document, new StringReader(htmlContent));
          //  writer.addToBody(new PdfStream(baosPDFFrontpage.toByteArray()));
            Path p = Files.createTempFile("pdffront", ".pdf");
            LOGGER.error("temporary pdf frontpage: " + p.toString());
            Files.copy(new ByteArrayInputStream(baosPDFFrontpage.toByteArray()), p , StandardCopyOption.REPLACE_EXISTING);
            Path pHTML = p.getParent().resolve(p.getFileName().toString().replace(".pdf", ".html"));
            Files.copy(new ByteArrayInputStream(htmlContent.getBytes()), pHTML , StandardCopyOption.REPLACE_EXISTING);
            */

        } catch (Exception e) {
            LOGGER.error("Something went wrong processing the XSLT: {}", xslt, e);
        }
    }

    private static String cleanUpHTML(String content) {
        String c = content.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        c = c.replace(MCRFrontendUtil.getBaseURL()+"images/", ""); //make image URLs relative
        return """
            <html xmlns='http://www.w3.org/1999/xhtml'>
              <head>
                <style>
                  body{font-size:12px; font-family:sans-serif}
                  h4{color: rgb(0, 74, 153);font-family: Verdana,sans-serif;font-size: 120%}
                  a {text-decoration: none !important; font-size:120%;font-weight:bold;color:black;}
                  span.label {color: #777;}
                  span#badgeAccess {height:2em !important; display:inline-block;white-space: nowrap;}
                  span#badgeLicense img{height:2em !important;}
                  p {margin-bottom:0.5em;}
                </style>
              </head>
            """
            + "\n  <body>" + c + "</body>"
            + "\n</html>";
    }
}
