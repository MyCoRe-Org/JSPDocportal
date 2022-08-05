/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.jspdocportal.common.api.explore;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The root object for the REST /explore response.
 * 
 * @author Robert Stephan
 *
 */
@XmlRootElement(name = "response")
@XmlType(propOrder = { "header", "data" })
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(ArrayList.class)
public class MCRAPIExploreResponse {

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "mcrobject")
    @JsonProperty("data")
    private List<MCRAPIExploreResponseObject> data = new ArrayList<MCRAPIExploreResponseObject>();

    @XmlElement(name = "header")
    private MCRAPIExploreResponseHeader header = new MCRAPIExploreResponseHeader();

    public List<MCRAPIExploreResponseObject> getData() {
        return data;
    }

    public MCRAPIExploreResponseHeader getHeader() {
        return header;
    }
}
