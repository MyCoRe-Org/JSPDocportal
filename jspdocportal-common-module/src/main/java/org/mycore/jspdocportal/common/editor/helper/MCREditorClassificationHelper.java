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

package org.mycore.jspdocportal.common.editor.helper;

import java.util.Iterator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * this class contains helper functions to display classifications
 * in editors and searchmasks
 * 
 * @author Robert Stephan
 * @version $Revision$ $Date$
 *
 */
public class MCREditorClassificationHelper {

	/**
	 * transforms a classification into an items structure
	 * using the categoryIDs as itemIDs
	 * 
	 * @param oldClassif the old classification as JDOM Document
	 * @param displayEmptyLeafs false, if empty leafs should be hidden
	 * @return the item structure as JDOM Document
	 */
	public static Document transformClassificationtoItems(Document oldClassif,
			boolean displayEmptyLeafs) {
		Element root = oldClassif.getRootElement().getChild("categories"); 
		Document itemsDoc = new Document();
		Element newRoot = new Element("items");
		itemsDoc.addContent(newRoot);
		transformClassificationToItems(root, newRoot, displayEmptyLeafs, false);
		return itemsDoc;
	}

	/**
	 * transforms a classification into an items structure
	 * using the category labels as itemIDs
	 * 
	 * @param oldClassif the old classification as JDOM Document
	 * @param displayEmptyLeafs false, if empty leafs should be hidden
	 * @return the item structure as JDOM Document
	 */
	public static Document transformClassificationLabeltoItems(Document oldClassif,
			boolean displayEmptyLeafs) {
		Element root = oldClassif.getRootElement().getChild("categories"); 
		Document itemsDoc = new Document();
		Element newRoot = new Element("items");
		itemsDoc.addContent(newRoot);
		transformClassificationToItems(root, newRoot, displayEmptyLeafs, true);
		return itemsDoc;
	}
	
	
	/**
	 * makes the transformation (recursively called)
	 * @param oldElem the element in the original DOM
	 * @param newElem the element in the new DOM
	 * @param displayEmptyLeafs false, if empty leafs should be hidden
	 * @param useLabelasID true, if the label instead of the ID should be used as new ID
	 */
	private static void transformClassificationToItems(Element oldElem,
			Element newElem, boolean displayEmptyLeafs, boolean useLabelasID) {
		if (oldElem.getName().equals("categories")) {
			Iterator<Element> it1 = oldElem.getChildren().iterator();
			while (it1.hasNext()) {
				transformClassificationToItems(it1.next(), newElem,
						displayEmptyLeafs, useLabelasID);
			}
		}
		if (oldElem.getName().equals("category")) {
			String counter = oldElem.getAttributeValue("counter");
			if (counter == null) {
				counter = "";
				oldElem.setAttribute("counter", counter);
			}
			if (displayEmptyLeafs || (!counter.equals("") && !counter.equals("0"))) {
				Element el = new Element("item");
				if(!useLabelasID){
					el.setAttribute("value", oldElem.getAttributeValue("ID"));
				}
				else{
					el.setAttribute("value", oldElem.getChild("label").getAttributeValue("text"));
				}
				newElem.addContent(el);
				Iterator<Element> it2 = oldElem.getChildren().iterator();
				while (it2.hasNext()) {
					transformClassificationToItems(it2.next(), el,
							displayEmptyLeafs, useLabelasID);
				}
			}
		}

		if (oldElem.getName().equals("label")) {
			Element el = new Element("label");
			el.setAttribute("lang", oldElem.getAttributeValue("lang",
					Namespace.XML_NAMESPACE), Namespace.XML_NAMESPACE);
			el.setText(oldElem.getAttributeValue("text") + " ["
					+ oldElem.getParentElement().getAttributeValue("counter")
					+ "]");
			newElem.addContent(el);

		}
	}
}
