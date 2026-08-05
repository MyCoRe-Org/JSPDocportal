/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
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

package org.mycore.jspdocportal.common.xsl;

import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.mycore.common.MCRCache;
import org.mycore.common.xsl.MCRTemplatesSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link MCRTemplatesSource} backed by XSL content held entirely in memory,
 * rather than loaded from a real file or classpath resource.
 */
public class MCRVirtualTemplatesSource extends MCRTemplatesSource {

    private final String name;
    private final String content;

    /**
     * Creates a new virtual template source backed by the given XSL content.
     *
     * @param name a short identifier for this source, used to build the
     *             {@link MCRTemplatesSource#getKey() cache key} and the synthetic
     *             system ID used by {@link #getSource()}; should be unique enough to
     *             avoid collisions with other virtual or real template sources
     * @param content the complete XSL stylesheet content to be compiled
     */
    public MCRVirtualTemplatesSource(String name, String content) {
        super("virtual_" + name);
        this.name = name;
        this.content = content;
    }

    @Override
    public SAXSource getSource() throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XMLReader reader = factory.newSAXParser().getXMLReader();
        InputSource inputSource = new InputSource(new StringReader(content));
        inputSource.setSystemId("file:/virtual/" + name + ".xsl");

        return new SAXSource(reader, inputSource);
    }

    /**
     * Always returns {@code null}, since this template source is virtual and backed by
     * in-memory content rather than a real resource with a resolvable URL.
     */
    @Override
    public URL getURL() {
        return null;
    }

    @Override
    public long getLastModified() {
        return -1L;
    }

    @Override
    public MCRCache.ModifiedHandle getModifiedHandle(long checkPeriod) {
        return new MCRCache.ModifiedHandle() {
            @Override
            public long getLastModified() {
                return -1L;
            }

            @Override
            public long getCheckPeriod() {
                return checkPeriod;
            }
        };
    }
}
