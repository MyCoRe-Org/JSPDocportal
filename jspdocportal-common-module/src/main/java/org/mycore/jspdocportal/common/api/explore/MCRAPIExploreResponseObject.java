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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This object represents a single result object of a REST /explore response.
 * 
 * @author Robert Stephan
 *
 */
@XmlType(propOrder = { "id", "modified", "links", "payload" })
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(ArrayList.class)
public class MCRAPIExploreResponseObject {
    @XmlAttribute
    private String id;

    @XmlAttribute
    @XmlJavaTypeAdapter(MCRInstantXMLAdapter.class)
    private Instant modified;

    @XmlElement(name = "payload")
    @JsonProperty("payloads")
    private List<MCRAPIExploreResponsePayload> payload;

    @XmlElement(name = "link")
    @JsonProperty("links")
    @JsonSerialize(using = MCRLinkListJsonSerializer.class)
    private List<MCRAPIExploreResponseLink> links = new ArrayList<>();

    public MCRAPIExploreResponseObject(String id, Instant modified) {
        super();
        this.id = id;
        this.modified = modified;
        updateLinks();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        updateLinks();
    }

    public Instant getModified() {
        return modified;
    }

    public void setLastModified(Instant modified) {
        this.modified = modified;
    }

    public void addPayload(String key, Object value) {
        if (payload == null) {
            payload = new ArrayList<>();
        }
        payload.add(new MCRAPIExploreResponsePayload(key, value));
    }

    public List<MCRAPIExploreResponsePayload> getPayload() {
        return payload;
    }

    private void updateLinks() {
        links.clear();
        links.add(new MCRAPIExploreResponseLink("self", MCRFrontendUtil.getBaseURL() + "api/v2/objects/" + id));
        String frontpageURL = MCRFrontendUtil.getBaseURL() + MCRConfiguration2
            .getString("MCR.RestAPI.V2.Links.FrontpageURL").orElse("receive/{id}").replace("{id}", id);
        links.add(new MCRAPIExploreResponseLink("html", frontpageURL));
    }

    public List<MCRAPIExploreResponseLink> getLinks() {
        return links;
    }
}
