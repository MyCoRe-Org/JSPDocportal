/*
 * $RCSfile$
 * $Revision$ $Date$
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

package org.mycore.jspdocportal.common.editor;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.services.i18n.MCRTranslation;

/**
 * This URI Resolver can be used to fill select boxes with a small, defined sets of values, 
 * for which classifications are much to complex. 
 * 
 * In the Professorenkatalog and other apps they need to be configurable and work with I18N.
 * 
 * This URI-Resolver is bound to the prefix "props2options"
 * It is called with "props2options:{property-key].
 * 
 * It looks up the given MyCoRe property, which contains a comma-separated list of values.
 * The are used for the value attribute of the select box option element.
 * The text to display is stored as message property in messages_*.properties.
 * Its key will be created by concatenating the property, "." and the value.
 * Empty values can be used for introductory phrases like "Please select ...".

 * mycore.properties: 
 *     OMD.profkat.states=,inprogress,long,short,document,other
 * messaage_*.properties:
 *     OMD.profkat.states.=Bitte wählen
 *     OMD.profkat.states.inprogress=in Bearbeitung
 *     OMD.profkat.states.long=Langeintrag
 *     OMD.profkat.states.short=Kurzeintrag
 *     OMD.profkat.states.document=Dokumenteintrag
 *     OMD.profkat.states.other=Sonstige
 *     
 * result:
 * <pre>
 * {@code
 *     <select id="status" class="form-control">
 *         <option value="">Bitte wählen</option>
 *         <option value="inprogress">in Bearbeitung</option>
 *         <option value="long">Langeintrag</option>
 *         <option value="short">Kurzeintrag</option>
 *         <option value="document">Dokumenteintrag</option>
 *         <option value="other">Sonstige</option>
 *     </select>
 * }
 * </pre>  
 *  @author Robert Stephan                 
 */
public class MCRProps2OptionsResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        //List<Element> result = new ArrayList<>();
        Element result = new Element("select"); // this element will be used as container and not rendered in output
        String key = href.substring(href.indexOf(":") + 1).trim();
        String items = MCRConfiguration2.getString(key).orElse(",");
        if (items.equals(",")) {
            //empty option box
            Element eOption = new Element("option");
            eOption.setAttribute("value", "");
            eOption.setText(MCRTranslation.translate(key + "."));
            result.addContent(eOption);
        } else {
            for (String item : items.split(",")) {
                item = item.trim();
                Element eOption = new Element("option");
                eOption.setAttribute("value", item);
                eOption.setText(MCRTranslation.translate(key + "." + item));
                result.addContent(eOption);
            }
        }
        return new JDOMSource(result);
    }
}
