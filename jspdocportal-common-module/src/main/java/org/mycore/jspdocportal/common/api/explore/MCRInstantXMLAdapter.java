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

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * an XmlAdapter which maps an Instant object to the proper string representation
 * used with JAXB
 * 
 * @author Robert Stephan
 *
 * @deprecated, use class from RestAPIv2 in 2020LTS
 */
public class MCRInstantXMLAdapter extends XmlAdapter<String, Instant> {

    @Override
    public Instant unmarshal(String v) throws Exception {
        return Instant.parse(v);
    }

    @Override
    public String marshal(Instant v) throws Exception {
        return v.toString();
    }

}
