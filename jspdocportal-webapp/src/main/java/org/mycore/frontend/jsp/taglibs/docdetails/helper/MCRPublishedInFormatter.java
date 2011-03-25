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
package org.mycore.frontend.jsp.taglibs.docdetails.helper;

import org.jdom.Element;

/**
 * Formats detailed information of published in
 * The element content is described in <publishdetails> Element of document datamodel:
 * 
 * Differenzierende Angaben zur Quelle (Nachbildung von Pica-Kategorie 4070) 
 *     <publishdetail>
 *   	  <title>Titel</title>
 *   	  <v>Band</v><j>Jahr</j><a>Heft</a>
 *   	  <d>Tag</d><m>Monat</m><n>Sonderheft</n>
 *   	  <p>Seitenangabe</p><t>Gesamtzahl Seiten</t>
 *   	  <display>modifizierte Anzeigeform</display>
 *     </publishdetail>
 *
 * @author Robert Stephan
 * @version $Revision: 17081 $ $Date: 2010-03-20 18:37:27 +0100 (Sa, 20 Mrz 2010) $
 */

public class MCRPublishedInFormatter {
	public static String format(Element el){
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
			String[] monate = new String[] { "", "Jan.", "Febr.", "M�rz", "Apr.", "Mai", "Jun.", "Jul.", "Aug.", "Sept.", "Okt.", "Nov.", "Dez." };
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

		}
		else{
			result.append(", ").append(s);
		}
		return result.toString();
	}
}
