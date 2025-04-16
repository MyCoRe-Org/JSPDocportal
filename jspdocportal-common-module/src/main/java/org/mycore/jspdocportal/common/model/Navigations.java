/*
 * $RCSfile$
 * $Revision: 29729 $ $Date: 2014-04-23 11:28:51 +0200 (Mi, 23 Apr 2014) $
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
 */
package org.mycore.jspdocportal.common.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.jspdocportal.common.navigation.model.Navigation;
import org.mycore.jspdocportal.common.navigation.model.NavigationItem;
import org.mycore.resource.MCRResourceHelper;

import jakarta.servlet.ServletContext;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "navigations", namespace = "http://www.mycore.org/jspdocportal/navigation")
@XmlAccessorType(XmlAccessType.NONE)
public class Navigations {
    private static final Logger LOGGER = LogManager.getLogger();
    
    @XmlElement(name = "navigation", namespace = "http://www.mycore.org/jspdocportal/navigation")
    private List<Navigation> list = new ArrayList<>();

    public List<Navigation> getList() {
        return list;
    }

    public Map<String, Navigation> getMap() {
        Map<String, Navigation> hashMap = new HashMap<>();
        for (Navigation n : list) {
            hashMap.put(n.getId(), n);
        }
        return hashMap;
    }

    public static void marshall(Navigations n, OutputStream os) {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(Navigations.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(n, os);
        } catch (JAXBException e) {
            LOGGER.error(e);
        }
    }

    public static Navigations unmarshall(InputStream ir) {
        try {
            JAXBContext context = JAXBContext.newInstance(Navigations.class);
            Unmarshaller um = context.createUnmarshaller();
            return (Navigations) um.unmarshal(ir);
        } catch (JAXBException e) {
            LOGGER.error(e);
        }
        return null;
    }

    public Navigation retrieveChild(String id) {
        for (Navigation ni : list) {
            if (ni.getId().equals(id)) {
                return ni;
            }
        }
        return null;
    }

    /**
    * This method loads the navigation as DOM tree into the applicationScope / servletContext.
    * It enhances the navigation with additional attributes:
    * level: the current level of the navigation item in the navigation tree
    * nodeID: a unique ID for the node (@see java.util.UUID for implementation details)
    * path: the complete navigation path (hierarchy of navitem ids, separated by ".") 
    * 
    * @param sce - the ServletContext
    */
    public static void loadNavigation(ServletContext sce) {
        Navigations nav = Navigations.unmarshall(MCRResourceHelper.getResourceAsStream("/config/navigation.xml"));
        for (Navigation n : nav.getList()) {
            annotate(n);
        }
        sce.setAttribute("mcr_navigation", nav);
    }

    /**
     * This method annotates a navigation element.
     * For details see above.
     * 
     * @param rootElement the root element of the navigation document
     */

    private static void annotate(Navigation nav) {
        for (NavigationItem ni : nav.getChildren()) {
            ni.setParent(nav);
            annotate(ni, 1, "");
        }
    }

    /**
     * This method annotes a navigation item.
     * For details see above.
     * @param e - the current navigation item
     * @param level - the current level
     * @param path - the current path
     */

    private static void annotate(NavigationItem ni, int level, String path) {
        ni.setLevel(level);
        if (!path.equals("")) {
            path = path + ".";
        }
        path = path + ni.getId();
        ni.setPath(path);
        //e.setAttribute("_nodeID", UUID.randomUUID().toString());
        for (NavigationItem nic : ni.getChildren()) {
            nic.setParent(ni);
            annotate(nic, level + 1, path);
        }
    }
}
