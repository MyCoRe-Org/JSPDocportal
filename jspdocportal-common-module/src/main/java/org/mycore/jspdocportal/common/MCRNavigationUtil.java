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
package org.mycore.jspdocportal.common;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.xml.MCRURIResolver;

/**
 * This class provides utility functionality for navigation.
 * 
 * @author Robert Stephan
 * @version $Revision: 1.8 $ $Date: 2008/05/28 13:43:31 $
 *
 */
public class MCRNavigationUtil {
    private static Namespace NS_NAVIGATION = Namespace.getNamespace("http://www.mycore.org/jspdocportal/navigation");

    /**
     * This method loads the navigation as DOM tree into the applicationScope / servletContext.
     * It enhances the navigation with additional attributes:
     * level: the current level of the navigation item in the navigation tree
     * nodeID: a unique ID for the node (@see java.util.UUID for implementation details)
     * path: the complete navigation path (hierarchy of navitem ids, separated by ".") 
     * 
     * @param session - the HTTPSession
     */
    protected static void loadNavigation(ServletContext sce) {
        Element eNav = MCRURIResolver.instance().resolve("resource:config/navigation.xml");
        annoteNavigation(eNav);

        Document jdomDoc = eNav.getDocument();
        if (jdomDoc == null) {
            jdomDoc = new Document(eNav);
        }

        org.w3c.dom.Document domDoc = null;
        try {
            domDoc = new org.jdom2.output.DOMOutputter().output(jdomDoc);
        } catch (org.jdom2.JDOMException e) {
            LogManager.getLogger(MCRNavigationUtil.class).error("Domoutput failed: ", e);
        }

        //load navigation dom into application scope
        sce.setAttribute("navDom", domDoc);
    }

    /**
     * This method annotates a navigation element.
     * For details see above.
     * 
     * @param rootElement the root element of the navigation document
     */
    private static void annoteNavigation(Element rootElement) {
        for (Element eChild : (List<Element>) rootElement.getChildren("navigation", NS_NAVIGATION)) {
            annoteNavigationItem(eChild, 0, "");
        }
    }

    /**
     * This method annotes a navigation item.
     * For details see above.
     * @param e - the current navigation item
     * @param level - the current level
     * @param path - the current path
     */
    private static void annoteNavigationItem(Element e, int level, String path) {
        e.setAttribute("_level", Integer.toString(level));
        if (!path.equals("")) {
            path = path + ".";
        }
        path = path + e.getAttributeValue("id");
        e.setAttribute("_path", path);
        //e.setAttribute("_nodeID", UUID.randomUUID().toString());
        for (Element eChild : (List<Element>) e.getChildren("navitem", NS_NAVIGATION)) {
            annoteNavigationItem(eChild, level + 1, path);
        }
    }
}
