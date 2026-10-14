package org.mycore.jspdocportal.ir.pdfdownload.util;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openpdf.text.Document;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

/**
 * draws rectangular boxes around the badges on frontpage.
 */
public class PDFBadgeBoxingEvent extends PdfPageEventHelper {

    private final Map<String, Rectangle> badges = new LinkedHashMap<>();

    @Override
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String tag) {
        if ("!DRAW_BADGES!".equals(tag)) {
            //a chunk with this tag name (at the end of the page) triggers drawing of all badges
            draw(writer, document);
        }
        if (tag != null && tag.startsWith("badge-")) {
            //creates a bounding box for all chunks of the same tag name
            //used for combinations like "Open + Logo + Access"
            badges.merge(tag, new Rectangle(rect), (a, b) -> new Rectangle(
                Math.min(a.getLeft(), b.getLeft()), Math.min(a.getBottom(), b.getBottom()),
                Math.max(a.getRight(), b.getRight()), Math.max(a.getTop(), b.getTop())));
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        //could not be used for drawing
        //onEndPage is sometimes called before onGenericTag
        //so at this point there is nothing to write
        //workaround: use a special tag name to force drawing
    }

    private void draw(PdfWriter writer, Document document) {
        if (badges.size() > 0) {
            float pad = 4f;
            PdfContentByte under = writer.getDirectContentUnder();
            for (Map.Entry<String, Rectangle> entry : badges.entrySet()) {
                if(entry.getKey().equals("badge-license_icon")) {
                    continue;
                }

                Rectangle rect = entry.getValue();
                under.saveState();
                setColors(entry.getKey(), under);
                under.setLineWidth(0.5f);
                under.rectangle(rect.getLeft() - 2 * pad, rect.getBottom() - 2 * pad,
                    rect.getWidth() + 4 * pad, rect.getHeight() + 3 * pad);
                under.fillStroke(); // first filling, then border
                under.restoreState();
            }
        }
    }

    private void setColors(String badgeName, PdfContentByte cb) {
        switch (badgeName) {
            case "badge-doctype" -> {
                cb.setColorFill(Color.GRAY);
                cb.setColorStroke(Color.DARK_GRAY);
            }
            case "badge-openaccess" -> {
                cb.setColorFill(new Color(0, 131, 81)); //dark_green
                cb.setColorStroke(Color.DARK_GRAY);
            }
            case "badge-restrictedaccess" -> {
                cb.setColorFill(Color.ORANGE);
                cb.setColorStroke(Color.DARK_GRAY);
            }
            case "badge-closedaccess" -> {
                cb.setColorFill(Color.RED);
                cb.setColorStroke(Color.DARK_GRAY);
            }
            default -> {
                cb.setColorFill(Color.LIGHT_GRAY);
                cb.setColorStroke(Color.DARK_GRAY);
            }
        }
    }
}
