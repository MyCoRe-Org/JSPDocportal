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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.ir.pdfdownload.PDFGenerator;
import org.mycore.resource.MCRResourceHelper;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

public class PDFFrontpageUtil {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'", Locale.GERMAN);
    private static final Color COLOR_BLACK = new Color(0, 0, 0);

    public static void createFrontPage(PdfWriter writer, Document document,
        String recordIdentifier, SolrDocument solrDoc) throws DocumentException, IOException {
        
        document.setPageSize(PageSize.A4);
        document.setMargins((float) PDFGenerator.BORDER, (float) PDFGenerator.BORDER,
            (float) PDFGenerator.BORDER, (float) PDFGenerator.BORDER);
        document.newPage();

        addRepositoryHeader(document);
        addDocumentDescription(document, solrDoc);
        addBadges(document, solrDoc);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private static void addRepositoryHeader(Document document) {
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
    }

    /** create a short description of the document
     * 
     * Output:
     *   Samuel Gottlieb Vogel von  (Authors)
     *   Brunnen-Diaet  (Title)
     *   [Doberan]: [Verlag nicht ermittelbar], [1826] (Origin info)
     *   https://purl.uni-rostock.de/rosdok/ppn1982113499 (PURL)
     *
     * Solr fields:
     *    "ir.creator.result":"Gottlieb Vogel von",
     *    "ir.title.result":"Brunnen-Diaet",
     *    "ir.doctype.result":"Druck",
     *    "ir.doctype_en.result":"monograph",
     *    "ir.originInfo.result":"[Doberan]: [Verlag nicht ermittelbar], [1826]",
     *    "purl":"https://purl.uni-rostock.de/rosdok/ppn1982113499",
    
     * @param document
     * @param solrDoc
     */
    private static void addDocumentDescription(Document document, SolrDocument solrDoc) throws IOException {

        PdfPTable table = new PdfPTable(new float[] { 2.0f, 1.0f });
        table.setWidthPercentage(100f);

        PdfPCell cellMeta = new PdfPCell();
        cellMeta.setBorder(PdfPCell.NO_BORDER);
        cellMeta.setVerticalAlignment(Element.ALIGN_TOP);

        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
        if (solrDoc.getFirstValue("ir.creator.result") != null) {
            cellMeta.addElement(new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.creator.result")), fontNormal));
        }
        if (solrDoc.getFirstValue("ir.title.result") != null) {
            cellMeta.addElement(new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.title.result")), fontTitle));
        }
        if (solrDoc.getFirstValue("ir.originInfo.result") != null) {
            cellMeta
                .addElement(new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.originInfo.result")), fontNormal));
        }
        if (solrDoc.getFirstValue("purl") != null) {
            cellMeta.addElement(new Paragraph(String.valueOf(solrDoc.getFirstValue("purl")), fontBold));
        }
        table.addCell(cellMeta);

        PdfPCell cellImage = new PdfPCell();
        cellImage.setBorder(PdfPCell.NO_BORDER);
        cellImage.setPaddingLeft(PDFGenerator.BORDER * .5f);
        cellImage.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (solrDoc.getFirstValue("ir.cover_url") != null) {

            Image img = Image.getInstance(
                URI.create(MCRFrontendUtil.getBaseURL()
                    + String.valueOf(solrDoc.getFirstValue("ir.cover_url")))
                    .toURL());
            img.scaleToFit(
                (document.getPageSize().getWidth() - 2.5f * PDFGenerator.BORDER) * .33f,
                (document.getPageSize().getHeight() - 2f * PDFGenerator.BORDER) * .33f);
            img.setAlignment(Image.MIDDLE);
            cellImage.addElement(img);
        }

        table.addCell(cellImage);
        document.add(table);
    }

    // Druck Freier Zugang Lizenz-Logo
    private static void addBadges(Document document, SolrDocument solrDoc) {
        /*
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'restrictedaccess')}">
        <span class="badge ir-badge ir-badge-restrictedaccess">
          <fmt:message key="OMD.ir.docdetails.header.access.restricted" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />            
        </span>
        </c:when>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'closedaccess')}">
        <span class="badge ir-badge ir-badge-closedaccess">
          <fmt:message key="OMD.ir.docdetails.header.access.closed" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />
        </span>
        </c:when>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'openaccess')}">
        <span class="badge ir-badge ir-badge-openaccess">
          <fmt:message key="OMD.ir.docdetails.header.access.open" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Open_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />
        </span>
        </c:when>
        */
    }

}
