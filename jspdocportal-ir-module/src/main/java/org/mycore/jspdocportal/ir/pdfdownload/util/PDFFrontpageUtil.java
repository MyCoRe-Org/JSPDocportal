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
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.ir.pdfdownload.PDFGenerator;
import org.mycore.resource.MCRResourceHelper;
import org.mycore.services.i18n.MCRTranslation;
import org.openpdf.text.BadElementException;
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
    private static final String SPACES = "    ";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'", Locale.GERMAN);
    private static final Color COLOR_BLACK = new Color(0, 0, 0);

    public static void createFrontPage(PdfWriter writer, Document document,
        String recordIdentifier, SolrDocument solrDoc) throws DocumentException, IOException {

        document.setPageSize(PageSize.A4);
        document.setMargins((float) PDFGenerator.BORDER, (float) PDFGenerator.BORDER,
            (float) PDFGenerator.BORDER, (float) PDFGenerator.BORDER);
        document.newPage();

        addRepositoryHeader(document);
        addDocumentDescription(document, recordIdentifier, solrDoc);
        document.add(Chunk.NEWLINE);
        addBadges(document, solrDoc);
        document.add(Chunk.NEWLINE);
    }

    private static void addRepositoryHeader(Document document) {
        try (InputStream is =
            MCRResourceHelper.getResourceAsStream(MCRTranslation.translate("PDFDownload.pdf.header.logo"))) {
            byte[] imgBytes = IOUtils.toByteArray(is);
            Image img = Image.getInstance(imgBytes);
            fitImageToHeight(img, 30f);
            document.add(img);
        } catch (IOException e) {
            //do nothing
        }

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
        document.add(new Paragraph(MCRTranslation.translate("PDFDownload.pdf.header.hints.p1"), font));
        document.add(new Paragraph(MCRTranslation.translate("PDFDownload.pdf.header.hints.p2"), font));
        document.add(new Paragraph(MCRTranslation.translate("PDFDownload.pdf.header.hints.p3"), font));
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        document.add(new Paragraph(
            MCRTranslation.translate("PDFDownload.pdf.header.hints.created_at", DTF.format(zonedDateTime)), font));
        Rectangle rect = new Rectangle(document.left(), document.top() - 35 * 2.54f,
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
     * @param recordId
     * @param solrDoc
     */
    private static void addDocumentDescription(Document document, String recordId, SolrDocument solrDoc)
        throws IOException {

        PdfPTable table = new PdfPTable(new float[] { 2.0f, 1.0f });
        table.setWidthPercentage(100f);

        PdfPCell cellMeta = new PdfPCell();
        cellMeta.setBorder(PdfPCell.NO_BORDER);
        cellMeta.setVerticalAlignment(Element.ALIGN_TOP);

        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);

        if (solrDoc.getFirstValue("ir.creator.result") != null) {
            cellMeta.addElement(
                new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.creator.result")), fontNormal));
        }
        if (solrDoc.getFirstValue("ir.title.result") != null) {
            Font fontTitleDisplay = solrDoc.getFirstValue("ir.partNumber.result") != null
                || solrDoc.getFirstValue("ir.partName.result") != null
                    ? fontBold : fontTitle;
            cellMeta.addElement(
                new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.title.result")), fontTitleDisplay));
        }
        StringBuffer sbSubtitle = new StringBuffer();
        if (solrDoc.getFirstValue("ir.partNumber.result") != null) {
            sbSubtitle.append(String.valueOf(solrDoc.getFirstValue("ir.partNumber.result")));
        }
        if (solrDoc.getFirstValue("ir.partName.result") != null) {
            if (sbSubtitle.length() > 0) {
                sbSubtitle.append(": ");
            }
            sbSubtitle.append(String.valueOf(solrDoc.getFirstValue("ir.partName.result")));
        }
        if (sbSubtitle.length() > 0) {
            cellMeta.addElement(new Paragraph(sbSubtitle.toString(), fontTitle));
        }

        if (solrDoc.getFirstValue("ir.originInfo.result") != null) {
            cellMeta.addElement(Chunk.NEWLINE);
            cellMeta.addElement(
                new Paragraph(String.valueOf(solrDoc.getFirstValue("ir.originInfo.result")), fontNormal));

        }
        if (solrDoc.getFirstValue("purl") != null) {
            cellMeta.addElement(Chunk.NEWLINE);
            cellMeta.addElement(
                new Paragraph(String.valueOf(solrDoc.getFirstValue("purl")), fontBold));
        }
        table.addCell(cellMeta);

        PdfPCell cellImage = new PdfPCell();
        cellImage.setBorder(PdfPCell.NO_BORDER);
        cellImage.setPaddingLeft(PDFGenerator.BORDER * .5f);
        cellImage.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (solrDoc.getFirstValue("ir.cover_url") != null) {
            try {
                Image img = Image.getInstance(
                    URI.create(MCRFrontendUtil.getBaseURL()
                        + String.valueOf(solrDoc.getFirstValue("ir.cover_url")))
                        .toURL());
                img.scaleToFit(
                    (document.getPageSize().getWidth() - 2.5f * PDFGenerator.BORDER) * .33f,
                    (document.getPageSize().getHeight() - 2f * PDFGenerator.BORDER) * .33f);
                img.setAlignment(Image.MIDDLE);

                cellImage.addElement(img);
            } catch (BadElementException | IOException e) {
                LOGGER.error("Could not write cover image to PDF frontpage for " + recordId, e);
            }
        }

        table.addCell(cellImage);
        document.add(table);
    }

    /** draw badges, eg. Druck + Open Access + License-Logo */
    private static void addBadges(Document document, SolrDocument solrDoc) throws IOException {
        Font fontBadge = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.WHITE);
        Paragraph p = new Paragraph();

        if (solrDoc.getFirstValue("ir.doctype_class.facet") != null) {
            MCRCategoryID cId = MCRCategoryID.ofString(String.valueOf(solrDoc.getFirstValue("ir.doctype_class.facet")));
            MCRCategory categ = MCRCategoryDAO.obtainInstance().getCategory(cId, 0);
            p.add(new Chunk(SPACES));
            p.add(new Chunk(SPACES + categ.getLabel("de").get().getText() + SPACES, fontBadge)
                .setGenericTag("badge-doctype"));
            p.add(new Chunk(SPACES + SPACES + SPACES));
        }

        if (solrDoc.getFirstValue("ir.accesscondition_class.facet") != null) {
            String access = String.valueOf(solrDoc.getFirstValue("ir.accesscondition_class.facet"));
            if (access.contains("openaccess")) {
                p.add(new Chunk(SPACES + "Open ", fontBadge)
                    .setGenericTag("badge-openaccess"));
                Image imgLogo = getImage("/META-INF/resources/images/logo_Open_Access.png");
                imgLogo.scaleToFit(14f, 28f);
                Chunk img = new Chunk(imgLogo, 0f, -7f, true);
                p.add(img);
                p.add(new Chunk(" Access" + SPACES, fontBadge)
                    .setGenericTag("badge-openaccess"));
            }
            if (access.contains("restrictedaccess")) {
                p.add(new Chunk(SPACES + "Restricted ", fontBadge)
                    .setGenericTag("badge-restrictedaccess"));
                Image imgLogo = getImage("/META-INF/resources/images/logo_Closed_Access.png");
                imgLogo.scaleToFit(14f, 28f);
                Chunk img = new Chunk(imgLogo, 0f, -7f, true);
                p.add(img);
                p.add(new Chunk(" Access" + SPACES, fontBadge)
                    .setGenericTag("badge-restrictedaccess"));
            }
            if (access.contains("closedAccess")) {
                p.add(new Chunk(SPACES + "Closed ", fontBadge)
                    .setGenericTag("badge-closedaccess"));
                Image imgLogo = getImage("/META-INF/resources/images/logo_Closed_Access.png");
                imgLogo.scaleToFit(14f, 28f);
                Chunk img = new Chunk(imgLogo, 0f, -7f, true);
                p.add(img);
                p.add(new Chunk(" Access" + SPACES, fontBadge)
                    .setGenericTag("badge-closedaccess"));
            }

        }

        if (solrDoc.getFirstValue("ir.worklicense_class.facet") != null) {
            p.add(new Chunk(SPACES + SPACES));
            MCRCategoryID cId =
                MCRCategoryID.ofString(String.valueOf(solrDoc.getFirstValue("ir.worklicense_class.facet")));
            MCRCategory categ = MCRCategoryDAO.obtainInstance().getCategory(cId, 0);
            String iconPath = categ.getLabel("x-icon").get().getText();
            Image imgLogo = getImage("/META-INF/resources/images" + iconPath);
            fitImageToHeight(imgLogo, 23f);
            Chunk chunkIcon = new Chunk(imgLogo, 0f, -8f, true).setGenericTag("badge-license_icon");
            p.add(chunkIcon);
        }

        //this Chunk with tag name '!DRAW_BADGES!' triggers the drawing event in PDFPageEventHandler
        p.add(new Chunk(SPACES).setGenericTag("!DRAW_BADGES!"));
        document.add(p);
    }

    private static void fitImageToHeight(Image imgLogo, float height) {
        float width = height * imgLogo.getWidth() / imgLogo.getHeight();
        imgLogo.scaleToFit(width, height);
    }

    private static Image getImage(String imgPath) throws IOException {
        try (InputStream is = MCRResourceHelper.getResourceAsStream(imgPath)) {
            byte[] imgBytes = IOUtils.toByteArray(is);
            return Image.getInstance(imgBytes);
        } catch (IOException e) {
            LOGGER.error("Could not read image: " + imgPath, e);
            throw e;
        }
    }
}
