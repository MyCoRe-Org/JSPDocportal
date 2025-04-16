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
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "navitem", namespace = "http://www.mycore.org/jspdocportal/navigation")
@XmlType(propOrder = { "id", "i18n", "href", "permission", "extern", "hidden", "navItemList" })
@XmlAccessorType(XmlAccessType.NONE)
public class NavigationItem implements NavigationObject {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "i18n")
    private String i18n;

    @XmlAttribute(name = "href")
    private String href;

    @XmlAttribute(name = "hidden")
    private Boolean hidden;

    @XmlAttribute(name = "extern")
    private Boolean extern;

    @XmlAttribute(name = "permission")
    private String permission;

    @XmlElement(name = "navitem", namespace = "http://www.mycore.org/jspdocportal/navigation")
    private List<NavigationItem> navItemList = new ArrayList<>();

    private int level;

    private String path;

    private NavigationObject parent;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public boolean isHidden() {
        //watch out for null value
        return hidden != null && hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isExtern() {
        //watch out for null value
        return extern != null && extern;
    }

    public void setExtern(boolean extern) {
        this.extern = extern;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
    public NavigationObject getParent() {
        return parent;
    }

    public void setParent(NavigationObject parent) {
        this.parent = parent;
    }
}
