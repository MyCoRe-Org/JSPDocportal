/*
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
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String VALUE_TRUE = "true";
    private static final String VALUE_FALSE = "false";
    private static final String VALUE_YES = "yes";
    private static final String VALUE_NO = "no";

    private static final String ELEM_NAME_HIDDENS = "hiddens";
    private static final String ELEM_NAME_HIDDEN = "hidden";
    private static final String ATTR_NAME_VAR = "var";
    private static final String ATTR_NAME_DEFAULT = "default";

    private Properties parseQueryString(String query) {
        Properties params = new Properties();
        String[] splitParams = query.replaceAll("&amp;", "&").split("&");
        for (String param : splitParams) {
            String[] splitParam = param.split("=");
            params.put(splitParam[0].trim(), splitParam[1].trim());
        }
        return params;
    }

    public Element resolveElement(String uri) {
        try {
            String query = uri.substring(uri.indexOf('?') + 1);
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
                return getGroupItems();
            } else if (mode.equals("getGroupItemAndLabelForUser")) {
                return getGroupItemAndLabelForUser();
            } else if (mode.equals("getClassificationLabelInItems")) {
                return getClassificationLabelInItems(params);
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("could not resolve URI {}", uri);
            return new Element("error");
        }
    }

    private Element getGroupItems() throws TransformerException {
        Element retitems = new Element("items");
        List<MCRRole> groupIDs = MCRRoleManager.listSystemRoles();
        for (MCRRole groupId : groupIDs) {
            Element item = new Element("item")
                .setAttribute("value", groupId.getName())
                .setAttribute("label", groupId.getName());
            retitems.addContent(item);
        }
        return retitems;
    }

    private Element getGroupItemAndLabelForUser() throws TransformerException {
        Element retitems = new Element("items");
        List<MCRRole> groups = MCRRoleManager.listSystemRoles();
        Iterator<MCRRole> itGroup = groups.iterator();
        while (itGroup.hasNext()) {
            MCRRole group = itGroup.next();
            String id = group.getName();
            if (id.startsWith("create")) {
                Element item = new Element("item").setAttribute("value", id).setAttribute("label",
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
            if (defaultValue == null || defaultValue.equals("")) {
                defaultValue = "DocPortal_class_1";
            }
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
            emptyLeafs = VALUE_YES;
        }
        String withCounter = params.getProperty("withCounter");
        if (withCounter == null || withCounter.equals("")) {
            withCounter = VALUE_TRUE;
        }

        if (classid == null || classid.equals("")) {
            String prop = params.getProperty("prop");
            String defaultValue = params.getProperty("defaultValue");
            if (defaultValue == null || defaultValue.equals("")) {
                defaultValue = "DocPortal_class_1";
            }
            if (prop != null && !prop.equals("")) {
                classid = MCRConfiguration2.getString(prop).orElse(defaultValue);
            } else {
                classid = defaultValue;
            }
        }
        return transformClassToItems(classid, emptyLeafs, isTruthyString(withCounter));
    }

    private Element transformClassToItems(String classid, String emptyLeafs, boolean withCounter)
        throws TransformerException {
        Document classJdom = MCRCategoryTransformer.getMetaDataDocument(
            MCRCategoryDAOFactory.obtainInstance().getCategory(new MCRCategoryID(classid), -1), withCounter);
        return MCREditorClassificationHelper.transformClassificationtoItems(classJdom, isTruthyString(emptyLeafs))
            .getRootElement();
    }

    /**
     * returns true, if the given String has value 'yes' or 'true' (ignores case)
     * @param value - the test value
     * @return true, if test passed
     */
    private static boolean isTruthyString(String value) {
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    private Element transformClassLabelsToItems(String classid) throws TransformerException {
        Document classJdom = MCRCategoryTransformer.getMetaDataDocument(
            MCRCategoryDAOFactory.obtainInstance().getCategory(new MCRCategoryID(classid), -1), false);
        return MCREditorClassificationHelper.transformClassificationLabeltoItems(classJdom, true).getRootElement();
    }

    private Element getSpecialCategoriesInItems(Properties params) throws TransformerException {
        Element retitems = new Element("items");
        String classProp = params.getProperty("classProp");
        String emptyLeafs = params.getProperty("emptyLeafs");
        if (emptyLeafs == null || emptyLeafs.equals("")) {
            emptyLeafs = VALUE_YES;
        } else {
            emptyLeafs = VALUE_NO;
        }
        String withCounter = params.getProperty("withCounter");
        if (withCounter == null || withCounter.equals("")) {
            withCounter = VALUE_TRUE;
        }
        String categoryProp = params.getProperty("categoryProp");
        if (classProp != null && categoryProp != null) {
            String classid = MCRConfiguration2.getString(classProp).orElse("DocPortal_class_1");
            Element items = transformClassToItems(classid, emptyLeafs, isTruthyString(withCounter));
            List<String> values;
            try {
                values = Arrays.asList(MCRConfiguration2.getString(categoryProp).orElse("").split(","));
            } catch (Exception ex) {
                LOGGER.warn("config property {} must be a comma separated list [ {} ]", categoryProp, ex);
                return items;
            }
            for (Iterator<Element> it = items.getDescendants(new ElementFilter("item")); it.hasNext();) {
                Element item = it.next();
                if (values.contains(item.getAttributeValue("value"))) {
                    retitems.addContent(item.clone());
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
        if (notinherit == null || notinherit.equals("")) {
            notinherit = VALUE_TRUE;
        }
        if (heritable == null || heritable.equals("")) {
            heritable = VALUE_FALSE;
        }

        Element hiddens = new Element(ELEM_NAME_HIDDENS);
        Element hidden1 = new Element(ELEM_NAME_HIDDEN);
        hidden1.setAttribute(ATTR_NAME_DEFAULT, classname);
        hidden1.setAttribute(ATTR_NAME_VAR, var + "/@class");
        //		Element hidden2 = new Element(ELEM_NAME_HIDDEN);
        //		hidden2.setAttribute(ATTR_NAME_DEFAULT, parasearch);
        //		hidden2.setAttribute(ATTR_NAME_VAR, var + "/@parasearch");
        //		Element hidden3 = new Element(ELEM_NAME_HIDDEN);
        //		hidden3.setAttribute(ATTR_NAME_DEFAULT, textsearch);
        //		hidden3.setAttribute(ATTR_NAME_VAR, var + "/@textsearch");
        Element hidden4 = new Element(ELEM_NAME_HIDDEN);
        hidden4.setAttribute(ATTR_NAME_DEFAULT, notinherit);
        hidden4.setAttribute(ATTR_NAME_VAR, var + "/@notinherit");
        Element hidden5 = new Element(ELEM_NAME_HIDDEN);
        hidden5.setAttribute(ATTR_NAME_DEFAULT, heritable);
        hidden5.setAttribute(ATTR_NAME_VAR, var + "/@heritable");
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

        String propValue;
        if (bundle != null && !bundle.equals("")) {
            if (lang == null || lang.equals("")) {
                lang = "de";
            }
            propValue = MCRTranslation.translate(prop, Locale.of(lang));
        } else {
            if (defaultValue == null) {
                defaultValue = "";
            }
            propValue = MCRConfiguration2.getString(prop).orElse(defaultValue);
        }

        Element hiddens = new Element(ELEM_NAME_HIDDENS);
        Element hidden = new Element(ELEM_NAME_HIDDEN);
        hidden.setAttribute(ATTR_NAME_VAR, var.replaceAll("\\.", "/"));
        hidden.setAttribute(ATTR_NAME_DEFAULT, propValue);

        hiddens.addContent(hidden);
        return hiddens;
    }

}
