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
package org.mycore.jspdocportal.common.navigation.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "navigation", namespace = "http://www.mycore.org/jspdocportal/navigation")
@XmlAccessorType(XmlAccessType.NONE)
public class Navigation implements NavigationObject {
    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "navitem", namespace = "http://www.mycore.org/jspdocportal/navigation")
    private List<NavigationItem> navItemList = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public NavigationItem retrieveChild(String id) {
        for (NavigationItem ni : navItemList) {
            if (ni.getId().equals(id)) {
                return ni;
            }
        }
        return null;
    }

    @Override
    public List<NavigationItem> getChildren() {
        return navItemList;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public NavigationObject getParent() {
        return null;
    }
}
