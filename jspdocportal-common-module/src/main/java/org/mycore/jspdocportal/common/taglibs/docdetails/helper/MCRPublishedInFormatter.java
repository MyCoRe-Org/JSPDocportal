/**
 * $RCSfile$
 * $Revision: 17081 $ $Date: 2010-03-20 18:37:27 +0100 (Sa, 20 Mrz 2010) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.jspdocportal.common.taglibs.docdetails.helper;

import java.util.List;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;

/**
 * Formats detailed information of published in
 * either in mods:relatedItem or mcr:publishdetail element
 *
 * @author Robert Stephan
 * @version $Revision: 17081 $ $Date: 2010-03-20 18:37:27 +0100 (Sa, 20 Mrz 2010) $
 */

public class MCRPublishedInFormatter {

    public static String format(Element el) {
        if (el.getName().equals("publishdetail")) {
            return formatPublishDetail(el);
        }
        if (el.getNamespace().equals(MCRConstants.MODS_NAMESPACE) && el.getName().equals("relatedItem")) {
            return formatRelatedItem(el);
        }
        return "";

    }

    /**
     * @param el the Element containing the content
     *  The content is described in <publishdetails> Element of document datamodel:
     * 	   <publishdetail>
     *   	  <title>Titel</title>
     *   	  <v>Band</v><j>Jahr</j><a>Heft</a>
     *   	  <d>Tag</d><m>Monat</m><n>Sonderheft</n>
     *   	  <p>Seitenangabe</p><t>Gesamtzahl Seiten</t>
     *   	  <display>modifizierte Anzeigeform</display>
     *     </publishdetail>
     * 
     * 
     * @return a formatted String
     */
    private static String formatPublishDetail(Element el) {
        StringBuffer result = new StringBuffer();
        String s = null;
        s = el.getChildTextNormalize("title");
        if (s != null) {
            result.append(s);
        }

        s = el.getChildTextNormalize("display");
        if (s == null) {
            s = el.getChildTextNormalize("v");
            if (s != null) {
                result.append(", Bd. ").append(s);
            }
            s = el.getChildTextNormalize("j");
            if (s != null) {
                result.append(" (").append(s).append(")");
            }
            s = el.getChildTextNormalize("a");
            if (s != null) {
                result.append(", Nr. ").append(s);
            }
            String tag = el.getChildTextNormalize("d");
            String monat = el.getChildTextNormalize("m");
            int iMonat = 0;
            try {
                iMonat = Integer.parseInt(monat);
            } catch (NumberFormatException nfe) {
                // do nothing
            }
            String[] monate = new String[] { "", "Jan.", "Febr.", "März", "Apr.", "Mai", "Jun.", "Jul.", "Aug.",
                    "Sept.", "Okt.", "Nov.", "Dez." };
            if (tag != null && monat != null) {
                result.append(", ");
                for (char c : tag.toCharArray()) {
                    if (Character.isDigit(c)) {
                        result.append(c);
                    } else {
                        result.append(".").append("c");
                    }
                }
                result.append(". ");
                result.append(monate[iMonat]);
            } else if (monat != null) {
                result.append(", ");
                if (monat.contains("-")) {
                    String[] x = monat.split("\\-");
                    for (String xx : x) {
                        int i = 0;
                        try {
                            i = Integer.parseInt(xx);
                            result.append(monate[i]);
                            if (xx != x[x.length - 1]) {
                                result.append("-");
                            }
                        } catch (NumberFormatException e) {

                        }
                    }
                } else if (monat.contains("/")) {
                    String[] x = monat.split("\\/");
                    for (String xx : x) {
                        int i = 0;
                        try {
                            i = Integer.parseInt(xx);
                            result.append(monate[i]);
                            if (xx != x[x.length - 1]) {
                                result.append("/");
                            }
                        } catch (NumberFormatException e) {

                        }
                    }
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(monat);
                        result.append(monate[i]);

                    } catch (NumberFormatException e) {

                    }
                }

            }
            s = el.getChildTextNormalize("p");
            if (s != null) {
                result.append(", S. ").append(s);
            }
            s = el.getChildTextNormalize("t");
            if (s != null) {
                result.append(" insges. ").append(s).append(" S.");
            }

        } else {
            result.append(", ").append(s);
        }
        return result.toString();
    }

    /**
     * @param el the element containing the content
     * The content should be a mods:relatedItem Element
     * look into the MODS standard for further reference.
     * 
     * <relatedItem displayLabel="in" type="host">
     *       <titleInfo>
     *         <title>Jahrbücher des Vereins für Mecklenburgische Geschichte und Altertumskunde</title>
     *       </titleInfo>
     *       <part>
     *         <detail type="volume">
     *           <number>1</number>
     *         </detail>
     *         <date>1836</date>
     *         <extend unit="page">
     *           <start>176</start>
     *           <end>178</end>
     *         </extend>
     *       </part>
     *     </relatedItem>
     *
     * @return the formatted text
     * 
     */
    private static String formatRelatedItem(Element el) {
        StringBuffer sb = new StringBuffer();
        Element eTitleInfo = el.getChild("titleInfo", MCRConstants.MODS_NAMESPACE);
        if (eTitleInfo != null) {
            Element eNonSort = eTitleInfo.getChild("nonSort", MCRConstants.MODS_NAMESPACE);
            if (eNonSort != null) {
                sb.append(eNonSort.getText());
            }
            Element eTitle = eTitleInfo.getChild("title", MCRConstants.MODS_NAMESPACE);
            if (eTitle != null) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(eTitle.getText());
            }
            Element eSubTitle = eTitleInfo.getChild("subTitle", MCRConstants.MODS_NAMESPACE);
            if (eSubTitle != null) {
                if (sb.length() > 0) {
                    sb.append(" : ");
                }
                sb.append(eSubTitle.getText());
            }
            Element ePart = el.getChild("part", MCRConstants.MODS_NAMESPACE);
            if (ePart != null) {
                for (Element eDetail : (List<Element>) (ePart.getChildren("detail", MCRConstants.MODS_NAMESPACE))) {
                    if (eDetail.getAttributeValue("type").equals("volume")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        Element eCaption = eDetail.getChild("caption", MCRConstants.MODS_NAMESPACE);
                        if (eCaption != null) {
                            sb.append(eCaption.getText()).append(" ");
                        } else {
                            sb.append("Bd. ");
                        }
                        Element eNumber = eDetail.getChild("number", MCRConstants.MODS_NAMESPACE);

                        if (eNumber != null) {
                            sb.append(eNumber.getText());
                        }
                    }
                    if (eDetail.getAttributeValue("type").equals("issue")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        Element eCaption = eDetail.getChild("caption", MCRConstants.MODS_NAMESPACE);
                        if (eCaption != null) {
                            sb.append(eCaption.getText()).append(" ");
                        } else {
                            sb.append("Nr. ");
                        }
                        Element eNumber = eDetail.getChild("number", MCRConstants.MODS_NAMESPACE);

                        if (eNumber != null) {
                            sb.append(eNumber.getText());
                        }
                    }
                }

                Element eDate = ePart.getChild("date", MCRConstants.MODS_NAMESPACE);
                if (eDate != null) {
                    sb.append(" (").append(eDate.getText()).append(")");
                }
                for (Element eExtend : (List<Element>) (ePart.getChildren("extend", MCRConstants.MODS_NAMESPACE))) {
                    if (eExtend.getAttributeValue("unit").equals("page")) {
                        String s = eExtend.getChildText("list", MCRConstants.MODS_NAMESPACE);
                        if (s != null) {
                            sb.append(", ").append(s);
                        }
                        s = eExtend.getChildText("total", MCRConstants.MODS_NAMESPACE);
                        if (s != null) {
                            sb.append(", ").append(s).append(" Seiten");
                        }
                        s = eExtend.getChildText("start", MCRConstants.MODS_NAMESPACE);
                        if (s != null) {
                            sb.append(", S. ").append(s);
                        }
                        s = eExtend.getChildText("end", MCRConstants.MODS_NAMESPACE);
                        if (s != null) {
                            sb.append("-").append(s);
                        }
                    }
                }

            }

        }
        return sb.toString();
    }

}
