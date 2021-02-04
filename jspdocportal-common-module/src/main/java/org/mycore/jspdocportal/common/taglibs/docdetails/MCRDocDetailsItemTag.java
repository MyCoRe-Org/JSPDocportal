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
 */
package org.mycore.jspdocportal.common.taglibs.docdetails;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.taglibs.standard.tag.common.xml.XPathUtil;
import org.w3c.dom.Node;

/**
 * Displays / stores a simple item in docdetails table
 * @author mcradmin
 *
 */
public class MCRDocDetailsItemTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRDocDetailsItemTag.class);

    private String xp;

    private String messagekey = "";

    private String datepattern = "";

    private String css = "";

    private String var = "";

    private boolean escapeXml = true;

    public void setSelect(String xpath) {
        this.xp = xpath;
    }

    public void setLabelkeyPrefix(String messagekey) {
        this.messagekey = messagekey;
    }

    public void setStyleName(String style) {
        this.css = style;
    }

    public void setDatePattern(String pattern) {
        this.datepattern = pattern;
    }

    public void setVar(String name) {
        this.var = name;
    }

    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    public void doTag() throws JspException, IOException {
        MCRDocDetailsRowTag docdetailsRow = (MCRDocDetailsRowTag) findAncestorWithClass(this,
                MCRDocDetailsRowTag.class);
        MCRDocDetailsTag docdetails = (MCRDocDetailsTag) findAncestorWithClass(this, MCRDocDetailsTag.class);
        if (docdetailsRow == null && docdetails == null) {
            throw new JspException(
                    "This tag must be nested in tag called 'row' or 'docdetails' of the same tag library");
        }
        Node context;
        if (docdetailsRow != null) {
            context = docdetailsRow.getContext();
        } else {
            context = docdetails.getContext();
        }

        String result = "";
        try {
            XPathUtil xu = new XPathUtil((PageContext) getJspContext());
            @SuppressWarnings("rawtypes")
            List nodes = xu.selectNodes(context, xp);
            if (nodes.size() > 0) {
                if (nodes.get(0) instanceof Node) {
                    result = ((Node) nodes.get(0)).getTextContent();
                } else {
                    result = nodes.get(0).toString();
                }
                if (!"".equals(messagekey)) {
                    String key = messagekey + result;
                    result = docdetails.getMessages().getString(key);
                }
                if (!"".equals(datepattern)) {
                    try {
                        SimpleDateFormat indf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.GERMANY);
                        SimpleDateFormat outdf = new SimpleDateFormat(datepattern, new Locale("de", "DE"));
                        result = outdf.format(indf.parse(result));
                    } catch (Exception e) {
                        try {
                            SimpleDateFormat indf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                            SimpleDateFormat outdf = new SimpleDateFormat(datepattern, new Locale("de", "DE"));
                            result = outdf.format(indf.parse(result));
                        } catch (Exception e2) {
                            try {
                                SimpleDateFormat indf = new SimpleDateFormat("yyyy", Locale.GERMANY);
                                SimpleDateFormat outdf = new SimpleDateFormat(datepattern, new Locale("de", "DE"));
                                result = outdf.format(indf.parse(result));
                            } catch (Exception e3) {
                                result = ((Node) nodes.get(0)).getTextContent();
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.debug("wrong xpath expression: " + xp);
        }
        if (result.equals("#")) {
            result = "";
        }
        if (var.length() > 0) {
            getJspContext().setAttribute(var, result);
        } else {
            String td = null;
            if (css.length() > 0) {
                td = "<td class=\"" + css + "\">";
            } else {
                td = "<td class=\"" + docdetails.getStylePrimaryName() + "-value\">";
            }
            if (escapeXml) {
                result = StringEscapeUtils.escapeXml10(result);
            }
            getJspContext().getOut().print(td + result + "</td>");
        }
    }
}

/*
	<tr>
		<td class="metaname">akademische Selbstverwaltung:</td>
		<td class="metavalue">
			<table border="0" cellpadding="0" cellspacing="4">
			<colgroup>
				<col width="80" />
			</colgroup>
			<tbody>
			<tr>
				<td style="text-align: left;" valign="top">1975-79</td>
				<td style="text-align: left;" valign="top">Mitglied im
				Senat</td>
			</tr>
			<tr>
				<td style="text-align: left;" valign="top">1981-84</td>
				<td style="text-align: left;" valign="top">Dekan</td>
			</tr>
			</tbody></table>
		</td>
</tr>
*/
