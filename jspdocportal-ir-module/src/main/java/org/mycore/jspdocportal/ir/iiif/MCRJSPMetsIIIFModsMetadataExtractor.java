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

package org.mycore.jspdocportal.ir.iiif;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFMetadata;
import org.mycore.mets.iiif.MCRMetsIIIFMetadataExtractor;

import com.google.common.base.Optional;

/**
 * IIIF Presentation API - Metadata
 * based on metadata for MCRViewer:
 * Git: digibib.tools/goobi2mycore/src/branch/develop/src/main/resources/xsl/mods2mcrviewer_html.xsl
 *  
 * @author Robert Stephan
 *
 */
public class MCRJSPMetsIIIFModsMetadataExtractor implements MCRMetsIIIFMetadataExtractor {

    private static final String ELEMENT_NAME__NON_SORT = "nonSort";
    private static final String ELEMENT_NAME__TITLE = "title";
    private static final String ELEMENT_NAME__SUB_TITLE = "subTitle";
    private static final String ELEMENT_NAME__PART_NUMBER = "partNumber";
    private static final String ELEMENT_NAME__PART_NAME = "partName";
    private static final String ATTRIBUTE_NAME__TYPE = "type";
    private static final String ELEMENT_NAME__NAME_PART = "namePart";

    @Override
    public List<MCRIIIFMetadata> extractModsMetadata(Element xmlData) {
        List<MCRIIIFMetadata> iiifMetadataList = new ArrayList<>();
        Element eMods = xmlData.getChild("mods", MCRConstants.MODS_NAMESPACE);
        Optional<String> oContributors = retrieveContributors(eMods);
        if (oContributors.isPresent()) {
            iiifMetadataList.add(new MCRIIIFMetadata("author", oContributors.get()));
        }
        Optional<String> oTitle = retrieveTitle(eMods);
        if (oTitle.isPresent()) {
            iiifMetadataList.add(new MCRIIIFMetadata(ELEMENT_NAME__TITLE, oTitle.get()));
        }

        Optional<String> oOriginInfo = retrieveOriginInfo(eMods);
        if (oOriginInfo.isPresent()) {
            iiifMetadataList.add(new MCRIIIFMetadata("originInfo", oOriginInfo.get()));
        }

        XPathExpression<Element> xpPURL = XPathFactory.instance().compile(
            "mods:identifier[@type='purl']", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        Element ePURL = xpPURL.evaluateFirst(eMods);
        if (ePURL != null) {
            iiifMetadataList.add(new MCRIIIFMetadata("identifier", ePURL.getText()));
        }

        XPathExpression<Element> xpGenre = XPathFactory.instance().compile(
            "mods:genre[@type='intern']", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        Element eGenre = xpGenre.evaluateFirst(eMods);
        if (eGenre != null) {
            iiifMetadataList.add(new MCRIIIFMetadata("genre", eGenre.getText()));
        }
        return iiifMetadataList;
    }

    /*
    <xsl:if test="mods:name[mods:role/mods:roleTerm='oth' or mods:role/mods:roleTerm='aut']">
    <p class="author">
      <xsl:for-each select="mods:name[mods:role/mods:roleTerm='oth' or mods:role/mods:roleTerm='aut']">
        <xsl:if test="./mods:namePart[@type='family']">
          <xsl:value-of select="./mods:namePart[@type='family']" />
          <xsl:value-of select="', '"/>
        </xsl:if>
        <xsl:value-of select="./mods:namePart[@type='given']" />;
        <xsl:value-of select="./mods:namePart[not(@type)]" />;
        <xsl:if test="./mods:namePart[@type='termsOfAddress']">
          <xsl:value-of select="' '"/>
          <xsl:value-of select="./mods:namePart[@type='termsOfAddress']" />;
        </xsl:if>
        <xsl:value-of select="'; '"/>
      </xsl:for-each>
    </p>
    </xsl:if>
    */
    protected Optional<String> retrieveContributors(Element eMods) {
        XPathExpression<Element> xpName = XPathFactory.instance().compile(
            "./mods:name[mods:role/mods:roleTerm='oth' or mods:role/mods:roleTerm='aut']", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        List<Element> eNames = xpName.evaluate(eMods);
        if (!eNames.isEmpty()) {
            StringBuffer sbName = new StringBuffer();
            for (Element eName : eNames) {
                if (eNames.indexOf(eName) > 0) {
                    sbName.append("; ");
                }
                for (Element eNamePart : eName.getChildren(ELEMENT_NAME__NAME_PART, MCRConstants.MODS_NAMESPACE)) {
                    if ("family".equals(eNamePart.getAttributeValue(ATTRIBUTE_NAME__TYPE))) {
                        sbName.append(eNamePart.getTextNormalize());
                        sbName.append(", ");
                    }

                }
                for (Element eNamePart : eName.getChildren(ELEMENT_NAME__NAME_PART, MCRConstants.MODS_NAMESPACE)) {
                    if ("given".equals(eNamePart.getAttributeValue(ATTRIBUTE_NAME__TYPE))) {
                        sbName.append(eNamePart.getTextNormalize());
                    }
                }
                for (Element eNamePart : eName.getChildren(ELEMENT_NAME__NAME_PART, MCRConstants.MODS_NAMESPACE)) {
                    if (null == eNamePart.getAttributeValue(ATTRIBUTE_NAME__TYPE)) {
                        sbName.append(eNamePart.getTextNormalize());
                    }
                }
                for (Element eNamePart : eName.getChildren(ELEMENT_NAME__NAME_PART, MCRConstants.MODS_NAMESPACE)) {
                    if ("termsOfAddress".equals(eNamePart.getAttributeValue(ATTRIBUTE_NAME__TYPE))) {
                        sbName.append(' ');
                        sbName.append(eNamePart.getTextNormalize());
                    }
                }
            }
            return Optional.of(sbName.toString());
        }
        return Optional.absent();
    }

    /*
    <xsl:for-each select="mods:titleInfo[@usage='primary']">
    <p class="title">
      <xsl:if test="./mods:nonSort">
        <xsl:value-of select="./mods:nonSort" />
        &#160;
      </xsl:if>
      <xsl:value-of select="./mods:title" />
      <xsl:if test="./mods:subTitle">
        &#160;:
        <xsl:value-of select="./mods:subTitle" />
      </xsl:if>
    </p>
    <xsl:if test="./mods:partNumber or ./mods:partName">
      <p class="title">
        <xsl:value-of select="./mods:partNumber" />
        <xsl:if test="./mods:partNumber and ./mods:partName"> : </xsl:if>
        <xsl:value-of select="./mods:partName" />
      </p>
    </xsl:if>
    </xsl:for-each>
    */
    protected Optional<String> retrieveTitle(Element eMods) {
        XPathExpression<Element> xpTitle = XPathFactory.instance().compile(
            "mods:titleInfo[@usage='primary']", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        List<Element> eTitles = xpTitle.evaluate(eMods);
        if (!eTitles.isEmpty()) {
            StringBuffer sbTitle = new StringBuffer();
            for (Element eTitle : eTitles) {
                if (eTitle.getChild(ELEMENT_NAME__NON_SORT, MCRConstants.MODS_NAMESPACE) != null) {
                    sbTitle.append(eTitle.getChildText(ELEMENT_NAME__NON_SORT, MCRConstants.MODS_NAMESPACE))
                        .append(' ');
                }
                sbTitle.append(eTitle.getChildText(ELEMENT_NAME__TITLE, MCRConstants.MODS_NAMESPACE)).append(' ');
                if (eTitle.getChild(ELEMENT_NAME__SUB_TITLE, MCRConstants.MODS_NAMESPACE) != null) {
                    sbTitle.append(" : ")
                        .append(eTitle.getChildText(ELEMENT_NAME__SUB_TITLE, MCRConstants.MODS_NAMESPACE));
                }
                if (eTitle.getChild(ELEMENT_NAME__PART_NUMBER, MCRConstants.MODS_NAMESPACE) != null
                    || eTitle.getChild(ELEMENT_NAME__PART_NAME, MCRConstants.MODS_NAMESPACE) != null) {
                    if (sbTitle.length() > 0) {
                        sbTitle.append(" / ");
                    }
                    if (eTitle.getChild(ELEMENT_NAME__PART_NUMBER, MCRConstants.MODS_NAMESPACE) != null) {
                        sbTitle.append(eTitle.getChildText(ELEMENT_NAME__PART_NUMBER, MCRConstants.MODS_NAMESPACE));
                    }
                    if (eTitle.getChild(ELEMENT_NAME__PART_NUMBER, MCRConstants.MODS_NAMESPACE) != null
                        && eTitle.getChild(ELEMENT_NAME__PART_NAME, MCRConstants.MODS_NAMESPACE) != null) {
                        sbTitle.append(" : ");
                    }
                    if (eTitle.getChild(ELEMENT_NAME__PART_NAME, MCRConstants.MODS_NAMESPACE) != null) {
                        sbTitle.append(eTitle.getChildText(ELEMENT_NAME__PART_NAME, MCRConstants.MODS_NAMESPACE));
                    }
                }

            }
            return Optional.of(sbTitle.toString());
        }
        return Optional.absent();
    }

    /*
      <xsl:when test="mods:originInfo[@eventType='publication']">
          <p class="publication">
            <xsl:for-each select="mods:originInfo[@eventType='publication'][1]">
              <xsl:if test="mods:edition">
                <xsl:value-of select="mods:edition" />
                <xsl:value-of select="' , '" />
              </xsl:if>
              <xsl:if test="mods:place[not(@supplied)]/mods:placeTerm">
                <xsl:for-each select="mods:place[not(@supplied)]/mods:placeTerm">
                  <xsl:if test="position() > 1">
                    <xsl:text>, </xsl:text>
                  </xsl:if>
                  <xsl:value-of select="."/>
                </xsl:for-each>
                <xsl:value-of select="' : '" />
              </xsl:if>
              <xsl:if test="mods:publisher">
                <xsl:for-each select="mods:publisher">
                  <xsl:if test="position() > 1">
                    <xsl:text>, </xsl:text>
                  </xsl:if>
                  <xsl:value-of select="."/>
                </xsl:for-each>
                <xsl:value-of select="' , '" />
              </xsl:if>
              <xsl:value-of select="mods:dateIssued[not(@*)]" />
              <xsl:value-of select="mods:dateCreated[@qualifier='approximate']" />
            </xsl:for-each>
          </p>
        </xsl:when>
     */

    protected Optional<String> retrieveOriginInfo(Element eMods) {
        XPathExpression<Element> xpOInfo = XPathFactory.instance().compile(
            "mods:originInfo[@eventType='publication' or @eventType='production']", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        Element eOInfo = xpOInfo.evaluateFirst(eMods);
        if (eOInfo != null) {
            StringBuffer sb = new StringBuffer();
            if (eOInfo.getChild("edition", MCRConstants.MODS_NAMESPACE) != null) {
                sb.append(eOInfo.getChildText("edition", MCRConstants.MODS_NAMESPACE)).append(" , ");
            }
            XPathExpression<Element> xpPlaces = XPathFactory.instance().compile(
                "mods:place[not(@supplied)]/mods:placeTerm", Filters.element(), null,
                MCRConstants.MODS_NAMESPACE);
            List<Element> ePlaces = xpPlaces.evaluate(eOInfo);
            if (!ePlaces.isEmpty()) {
                StringBuffer sbPlaces = new StringBuffer();
                for (Element ePlace : ePlaces) {
                    if (sbPlaces.length() > 0) {
                        sbPlaces.append(", ");
                    }
                    sbPlaces.append(ePlace.getText());
                }
                sb.append(sbPlaces).append(" : ");
            }
            XPathExpression<Element> xpPublishers = XPathFactory.instance().compile(
                "mods:publisher", Filters.element(), null,
                MCRConstants.MODS_NAMESPACE);
            List<Element> ePublishers = xpPublishers.evaluate(eOInfo);
            if (!ePublishers.isEmpty()) {
                StringBuffer sbPublisher = new StringBuffer();
                for (Element ePublisher : ePublishers) {
                    if (sbPublisher.length() > 0) {
                        sbPublisher.append(", ");
                    }
                    sbPublisher.append(ePublisher.getText());
                }
                sb.append(sbPublisher).append(" , ");
            }
            XPathExpression<Element> xpDates = XPathFactory.instance().compile(
                "mods:dateIssued[not(@*)]|mods:dateCreated[not(@*)]|mods:dateCreated[@qualifier='approximate']",
                Filters.element(), null,
                MCRConstants.MODS_NAMESPACE);
            for (Element eDate : xpDates.evaluate(eOInfo)) {
                sb.append(eDate.getText()).append(' ');
            }

            return Optional.of(sb.toString().trim());
        }
        return Optional.absent();
    }
}
