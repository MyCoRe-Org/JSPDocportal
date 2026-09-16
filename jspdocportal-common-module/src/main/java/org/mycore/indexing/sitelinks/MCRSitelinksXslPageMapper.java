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
 * along with MyCoRe.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mycore.indexing.sitelinks;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.jdom2.Element;
import org.mycore.common.config.annotation.MCRConfigurationProxy;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRLayoutTransformerFactory;
import org.mycore.indexing.sitelinks.dto.MCRSitelinksLinkObject;
import org.mycore.indexing.sitelinks.dto.MCRSitelinksRootPageDto;
import org.mycore.indexing.sitelinks.dto.MCRSitelinksYearPageDto;

/**
 * Implementation of {@link MCRSitelinksPageMapper} that transforms sitelinks pages to HTML via XSL transformation.
 * <p>
 * This mapper converts sitelinks page objects into XML structures and applies XSL transformations
 * to generate HTML output. The XML structure follows a defined schema with elements for years,
 * pages, and object IDs.
 */
@MCRConfigurationProxy(proxyClass = MCRSitelinksXslPageMapper.Factory.class)
public class MCRSitelinksXslPageMapper implements MCRSitelinksPageMapper {

    private static final String ROOT = "sitelinks-page";
    private static final String YEARS = "years";
    private static final String YEAR = "year";
    private static final String PAGE = "page";
    private static final String OBJECTS = "objects";
    private static final String OBJECT = "object";
    private static final String ATTR_OBJECT__ID = "id";
    private static final String ATTR_OBJECT__FULLTEXT = "fulltext";
    private static final String ATTR_PAGE__NUMBER = "number";
    private static final String ATTR_PAGE__TOTAL_COUNT = "total-count";
    private static final String ATTR_PAGE__YEAR = "year";

    private final MCRContentTransformer transformer;

    /**
     * Constructs a new SitelinksPageMapperImpl with default XSL transformer.
     * <p>
     * The transformer is obtained from the {@link MCRLayoutTransformerFactory}
     * using the root element name as transformer key.
     */
    public MCRSitelinksXslPageMapper() {
        this(new MCRLayoutTransformerFactory().getTransformer(ROOT));
    }

    /**
     * Constructs a new SitelinksPageMapperImpl with an XSL transformer.
     *
     * @param transformer the transformer
     */
    public MCRSitelinksXslPageMapper(MCRContentTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public MCRContent map(MCRSitelinksRootPageDto rootPage) {
        Element root = new Element(ROOT);
        root.addContent(buildYearsElement(rootPage.years()));
        try {
            return transformer.transform(new MCRJDOMContent(root));
        } catch (IOException e) {
            throw new MCRSitelinksMappingException("Error while mapping page", e);
        }
    }

    @Override
    public MCRContent map(MCRSitelinksYearPageDto yearPage) {
        Element root = new Element(ROOT);
        root.addContent(buildPageElement(yearPage.year(), yearPage.page(), yearPage.totalCount(), yearPage.objects()));
        try {
            return transformer.transform(new MCRJDOMContent(root));
        } catch (IOException e) {
            throw new MCRSitelinksMappingException("Error while mapping page", e);
        }
    }

    private static Element buildYearsElement(List<Integer> years) {
        Element yearsElement = new Element(YEARS);
        years.stream()
            .sorted(Comparator.reverseOrder())
            .forEach(y -> yearsElement.addContent(new Element(YEAR).setText(String.valueOf(y))));
        return yearsElement;
    }

    private static Element buildPageElement(int year, int page, long totalCount,
        List<MCRSitelinksLinkObject> linkObjects) {
        Element pageElement = new Element(PAGE);
        pageElement.setAttribute(ATTR_PAGE__NUMBER, String.valueOf(page));
        pageElement.setAttribute(ATTR_PAGE__TOTAL_COUNT, String.valueOf(totalCount));
        pageElement.setAttribute(ATTR_PAGE__YEAR, String.valueOf(year));

        Element objectIdsElement = new Element(OBJECTS);
        linkObjects.forEach(obj -> {
            Element e = new Element(OBJECT);
            e.setAttribute(ATTR_OBJECT__ID, obj.objectId());
            if(obj.fulltextUrl()!=null) {
                e.setAttribute(ATTR_OBJECT__FULLTEXT, obj.fulltextUrl());
            }
            objectIdsElement.addContent(e);
        });
        pageElement.addContent(objectIdsElement);
        return pageElement;
    }

    /**
     * Factory class for creating {@link MCRSitelinksService} instances via configuration.
     * <p>
     * This factory is used by the {@link MCRConfigurationProxy} annotation to automatically
     * instantiate the service with configuration values from properties.
     */
    public static class Factory implements Supplier<MCRSitelinksXslPageMapper> {

        @Override
        public MCRSitelinksXslPageMapper get() {
            return new MCRSitelinksXslPageMapper();
        }
    }
}
