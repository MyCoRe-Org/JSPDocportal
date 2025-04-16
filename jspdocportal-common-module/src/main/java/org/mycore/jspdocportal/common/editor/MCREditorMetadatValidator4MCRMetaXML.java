/*
 * $Revision: 1 $ $Date: 08.05.2009 11:51:35 $
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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.validator.MCREditorMetadataValidator;

/**
 * CleanUp MCRMetaXML which is exported by the editor
 * - delete empty elements (ones that only have default attributes for example)
 * 
 * @author Robert Stephan
 *
 */
public class MCREditorMetadatValidator4MCRMetaXML implements MCREditorMetadataValidator {

    //return null if everything is fine
    @Override
    public String checkDataSubTag(Element datasubtag) {
        if (datasubtag.getName().equals("modsContainer")) {
            //remove empty mods subelements !!!
            //            <mods:relatedItem type="host" displayLabel="in">
            //            <mods:part>
            //              <mods:detail type="part">
            //                <mods:number>Band 12</mods:number>
            //              </mods:detail>
            //              <mods:date type="w3cdtf" />
            //              <mods:extend unit="pages" />
            //            </mods:part>
            //           </mods:relatedItem> 

            XPathExpression<Element> xpath = XPathFactory.instance().compile(
                    "./mods:mods/mods:relatedItem/mods:part/mods:detail "
                            + "| ./mods:mods/mods:relatedItem/mods:part/mods:date "
                            + "| ./mods:mods/mods:relatedItem/mods:part/mods:extend",
                    Filters.element(), null, MCRConstants.MODS_NAMESPACE);

            List<Element> data = new ArrayList<>(xpath.evaluate(datasubtag));
            for (Element e : data) {
                if (e.getContentSize() == 0) {
                    e.getParentElement().removeContent(e);
                }
            }

        } else {
            List<Element> l = new ArrayList<>();
            l.addAll(datasubtag.getChildren());
            for (Element eChild : l) {
                // remove empty name elments of pc:person
                //<pc:person xmlns:pc="http://www.d-nb.de/standards/pc/">
                //       <pc:name type="nameUsedByThePerson" Scheme="PND" />
                //</pc:person>
                if (eChild.getName().equals("person")
                        && eChild.getNamespace().getURI().equals("http://www.d-nb.de/standards/pc/")) {
                    List<Element> grandChildren = new ArrayList<>();
                    grandChildren.addAll(eChild.getChildren());
                    for (Element e : grandChildren) {
                        if (e.getName().equals("name") && e.getContentSize() == 0) {
                            eChild.removeContent(e);
                        }
                    }
                }
                if (eChild.getChildren().isEmpty()) {
                    datasubtag.removeContent(eChild);
                }

            }
            if (datasubtag.getChildren().isEmpty()) {
                return ""; //delete element;
            }
        }

        return null;
    }
}
