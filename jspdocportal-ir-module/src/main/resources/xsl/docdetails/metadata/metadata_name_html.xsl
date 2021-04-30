<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:param name="WebApplicationBaseURL" />
  
  <xsl:template name="personal_name">
    <xsl:param name="names" />
    <xsl:for-each select="$names">
      <tr><td colspan="2">
        <xsl:if test="mods:namePart[@type='given' or @type='family']">
          <strong>
            <xsl:value-of select="string-join((mods:namePart[@type='given'], mods:namePart[@type='family']),' ')" />
          </strong>
        </xsl:if>
        <xsl:if test="mods:namePart[not(@type)]">
          <strong>
            <xsl:value-of select="string-join(mods:namePart[not(@type)],' ')" />
          </strong>
        </xsl:if>
        <xsl:if test="mods:namePart[@type='termsOfAddress']">
          , <xsl:value-of select="string-join(mods:namePart[@type='termsOfAddress'], ', ')" />
        </xsl:if>
        <xsl:choose>
          <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
            <span class="ir-table-docdetails-values-label">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
          </xsl:when>
          <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
            <span class="ir-table-docdetails-values-label">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
          </xsl:when>
        </xsl:choose>
      </td></tr>
               
      <xsl:if test="./mods:nameIdentifier[@type='orcid']">
        <tr>
          <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconbwvector.svg"  style="height:1.15em" title="ORCID (Open Researcher and Contributor ID)" /></th>
          <td><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
        </tr>
      </xsl:if>
      <xsl:if test="./mods:nameIdentifier[@type='gnd']">
        <tr>
          <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.25em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
          <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
        </tr>
      </xsl:if>
      <xsl:for-each select="./mods:affiliation">
        <tr>
          <th class="text-center align-text-top"><i class="fas fa-university" title="Einrichtung" style="font-size:1.25em"></i></th>
          <td>{.}</td>
        </tr>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="corporate_name">
    <xsl:param name="names" />
    <xsl:for-each select="$names">
      <tr><td colspan="2">
        <strong><xsl:value-of select="string-join(mods:namePart,', ')" /></strong>
        <xsl:choose>
          <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
            <span class="ir-table-docdetails-values-label">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
          </xsl:when>
          <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
            <span class="ir-table-docdetails-values-label">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
          </xsl:when>
        </xsl:choose>
      </td></tr>
      <xsl:if test="./mods:nameIdentifier[@type='gnd']">
        <tr>
          <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.25em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
          <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
        </tr>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
    
</xsl:stylesheet>
