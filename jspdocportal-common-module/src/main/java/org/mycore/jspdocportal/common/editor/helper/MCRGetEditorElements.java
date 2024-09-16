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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.utils.MCRCategoryTransformer;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;

/**
 * this class delivers xml objects that can be included
 * in the MyCoRe Editor Framework
 * 
 * possible mode are so far:
 * 
 * getClassificationItems
 * 
 * @author Heiko Helmbrecht
 * @author Robert Stephan
 *
 */
public class MCRGetEditorElements {
    private static Logger logger = LogManager.getLogger("MCRGetEditorElements");

    private Properties parseQueryString(String query) {
        Properties params = new Properties();
        String[] splitParams = query.replaceAll("&amp;", "&").split("&");
        for (int i = 0; i < splitParams.length; i++) {
            String[] splitParam = splitParams[i].split("=");
            params.put(splitParam[0].trim(), splitParam[1].trim());
        }
        return params;
    }

    public Element resolveElement(String URI) {
        try {
            String query = URI.substring(URI.indexOf("?") + 1);
            Properties params = parseQueryString(query);
            String mode = params.getProperty("mode");
            if (mode.equals("getHiddenVar")) {
                return getHiddenVar(params);
            } else if (mode.equals("getHiddenAttributesForClass")) {
                return getHiddenAttributesForClass(params);
            } else if (mode.equals("getClassificationInItems")) {
                return getClassificationInItems(params);
            } else if (mode.equals("getSpecialCategoriesInItems")) {
                return getSpecialCategoriesInItems(params);
            } else if (mode.equals("getGroupItems")) {
                return getGroupItems(params);
            } else if (mode.equals("getGroupItemAndLabelForUser")) {
                return getGroupItemAndLabelForUser(params);
            } else if (mode.equals("getClassificationLabelInItems")) {
                return getClassificationLabelInItems(params);
            }
            return null;
        } catch (Exception ex) {
            logger.error("could not resolve URI " + URI);
            return new Element("error");
        }
    }

    private Element getGroupItems(Properties params) throws TransformerException {
        Element retitems = new Element("items");
        List<MCRRole> groupIDs = MCRRoleManager.listSystemRoles();

        for (int i = 0; i < groupIDs.size(); i++) {
            org.jdom2.Element item = new org.jdom2.Element("item")
                    .setAttribute("value", (String) groupIDs.get(i).getName())
                    .setAttribute("label", (String) groupIDs.get(i).getName());
            retitems.addContent(item);
        }
        return retitems;
    }

    private Element getGroupItemAndLabelForUser(Properties params) throws TransformerException {
        Element retitems = new Element("items");
        List<MCRRole> groups = MCRRoleManager.listSystemRoles();
        Iterator<MCRRole> itGroup = groups.iterator();
        while (itGroup.hasNext()) {
            MCRRole group = (MCRRole) itGroup.next();
            String ID = group.getName();
            if (ID.startsWith("create")) {
                org.jdom2.Element item = new org.jdom2.Element("item").setAttribute("value", ID).setAttribute("label",
                        group.getName());
                retitems.addContent(item);
            }
        }
        return retitems;
    }

    private Element getClassificationLabelInItems(Properties params) throws TransformerException {
        String classid = params.getProperty("classid");
        if (classid == null || classid.equals("")) {
            String prop = params.getProperty("prop");
            String defaultValue = params.getProperty("defaultValue");
            if (defaultValue == null || defaultValue.equals(""))
                defaultValue = "DocPortal_class_1";
            if (prop != null && !prop.equals("")) {
                classid = MCRConfiguration2.getString(prop).orElse(defaultValue);
            } else {
                classid = defaultValue;
            }
        }
        return transformClassLabelsToItems(classid);
    }

    private Element getClassificationInItems(Properties params) throws TransformerException {
        String classid = params.getProperty("classid");
        String emptyLeafs = params.getProperty("emptyLeafs");
        if (emptyLeafs == null || emptyLeafs.equals("")) {
            emptyLeafs = "yes";
        }
        String withCounter = params.getProperty("withCounter");
        if (withCounter == null || withCounter.equals("")) {
            withCounter = "true";
        }

        if (classid == null || classid.equals("")) {
            String prop = params.getProperty("prop");
            String defaultValue = params.getProperty("defaultValue");
            if (defaultValue == null || defaultValue.equals(""))
                defaultValue = "DocPortal_class_1";
            if (prop != null && !prop.equals("")) {
                classid = MCRConfiguration2.getString(prop).orElse(defaultValue);
            } else {
                classid = defaultValue;
            }
        }
        return transformClassToItems(classid, emptyLeafs, withCounter.equalsIgnoreCase("true"));
    }

    private Element transformClassToItems(String classid, String emptyLeafs, boolean withCounter)
            throws TransformerException {
        Document classJdom = MCRCategoryTransformer.getMetaDataDocument(
                MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.rootID(classid), -1), withCounter);

        boolean displayEmptyLeafs = (emptyLeafs.equalsIgnoreCase("yes") || emptyLeafs.equalsIgnoreCase("true"));
        return MCREditorClassificationHelper.transformClassificationtoItems(classJdom, displayEmptyLeafs)
                .getRootElement();
    }

    private Element transformClassLabelsToItems(String classid) throws TransformerException {
        Document classJdom = MCRCategoryTransformer.getMetaDataDocument(
                MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.rootID(classid), -1), false);
        return MCREditorClassificationHelper.transformClassificationLabeltoItems(classJdom, true).getRootElement();
    }

    private Element getSpecialCategoriesInItems(Properties params) throws TransformerException {
        Element retitems = new Element("items");
        String classProp = params.getProperty("classProp");
        String emptyLeafs = params.getProperty("emptyLeafs");
        if (emptyLeafs == null || emptyLeafs.equals("")) {
            emptyLeafs = "yes";
        } else
            emptyLeafs = "no";
        String withCounter = params.getProperty("withCounter");
        if (withCounter == null || withCounter.equals("")) {
            withCounter = "true";
        }
        String categoryProp = params.getProperty("categoryProp");
        if (classProp != null && categoryProp != null) {
            String classid = MCRConfiguration2.getString(classProp).orElse("DocPortal_class_1");
            Element items = transformClassToItems(classid, emptyLeafs, withCounter.equalsIgnoreCase("true"));
            List<String> values = null;
            try {
                values = Arrays.asList(MCRConfiguration2.getString(categoryProp).orElse("").split(","));
            } catch (Exception ex) {
                logger.warn("config property " + categoryProp + " must be a comma separated list [" + ex.getMessage()
                        + "]");
                return items;
            }
            for (Iterator<Element> it = items.getDescendants(new ElementFilter("item")); it.hasNext();) {
                Element item = it.next();
                if (values.contains(item.getAttributeValue("value"))) {
                    retitems.addContent((Element) item.clone());
                }
            }
        }
        return retitems;
    }

    private Element getHiddenAttributesForClass(Properties params) {
        String var = params.getProperty("var").replaceAll("\\.", "/");
        String classname = params.getProperty("classname");
        //	String parasearch = params.getProperty("parasearch");
        //	String textsearch = params.getProperty("textsearch");
        String notinherit = params.getProperty("notinherit");
        String heritable = params.getProperty("heritable");

        // Default-Values
        //	if(parasearch == null || parasearch.equals("")) parasearch = "true";
        //	if(textsearch == null || textsearch.equals("")) textsearch = "true";
        if (notinherit == null || notinherit.equals(""))
            notinherit = "true";
        if (heritable == null || heritable.equals(""))
            heritable = "false";

        Element hiddens = new Element("hiddens");
        Element hidden1 = new Element("hidden");
        hidden1.setAttribute("default", classname);
        hidden1.setAttribute("var", var + "/@class");
        //		Element hidden2 = new Element("hidden");
        //		hidden2.setAttribute("default", parasearch);
        //		hidden2.setAttribute("var", var + "/@parasearch");
        //		Element hidden3 = new Element("hidden");
        //		hidden3.setAttribute("default", textsearch);
        //		hidden3.setAttribute("var", var + "/@textsearch");
        Element hidden4 = new Element("hidden");
        hidden4.setAttribute("default", notinherit);
        hidden4.setAttribute("var", var + "/@notinherit");
        Element hidden5 = new Element("hidden");
        hidden5.setAttribute("default", heritable);
        hidden5.setAttribute("var", var + "/@heritable");
        hiddens.addContent(hidden1);
        //		hiddens.addContent(hidden2);
        //		hiddens.addContent(hidden3);
        hiddens.addContent(hidden4);
        hiddens.addContent(hidden5);
        return hiddens;
    }

    private Element getHiddenVar(Properties params) throws IOException {
        String lang = params.getProperty("lang");
        String bundle = params.getProperty("bundle");
        String prop = params.getProperty("prop");
        String defaultValue = params.getProperty("defaultValue");
        String var = params.getProperty("var");

        String propValue = "";
        if (bundle != null && !bundle.equals("")) {
            if (lang == null || lang.equals("")) {
                lang = "de";
            }
            propValue = MCRTranslation.translate(prop, Locale.of(lang));
        } else {
            if (defaultValue == null)
                defaultValue = "";
            propValue = MCRConfiguration2.getString(prop).orElse(defaultValue);
        }

        Element hiddens = new Element("hiddens");
        Element hidden = new Element("hidden");
        hidden.setAttribute("var", var.replaceAll("\\.", "/"));
        hidden.setAttribute("default", propValue);

        hiddens.addContent(hidden);
        return hiddens;
    }

}
