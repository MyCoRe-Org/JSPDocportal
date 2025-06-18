<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xslt/functions/i18n.xsl" />
  <xsl:param name="WebApplicationBaseURL" />
  
  <xsl:template name="personal_name">
    <xsl:param name="names" />
    <xsl:for-each select="$names">
      <tr><td colspan="2">
        <xsl:if test="mods:namePart[@type='given' or @type='family']">
            <xsl:value-of select="string-join((mods:namePart[@type='given'], mods:namePart[@type='family']),' ')" />
        </xsl:if>
        <xsl:if test="mods:namePart[not(@type)]">
            <xsl:value-of select="string-join(mods:namePart[not(@type)],' ')" />
        </xsl:if>
        <xsl:if test="mods:namePart[@type='termsOfAddress']">
          , <xsl:value-of select="string-join(mods:namePart[@type='termsOfAddress'], ', ')" />
        </xsl:if>
        <xsl:choose>
          <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
            <span class="small ps-2">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
          </xsl:when>
          <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
            <span class="small ps-2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator_mycore:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
          </xsl:when>
        </xsl:choose>
      </td></tr>
               
      <xsl:if test="./mods:nameIdentifier[@type='orcid']">
        <tr>
          <th class="text-center small"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconbwvector.svg"  style="height:1.15em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.orcid')}" /></th>
          <td class="small"><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
        </tr>
      </xsl:if>
      <xsl:if test="./mods:nameIdentifier[@type='gnd']">
        <tr>
          <th class="text-center small"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.25em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
          <td class="small"><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
        </tr>
      </xsl:if>
      <xsl:for-each select="./mods:affiliation">
        <tr>
          <th class="text-center align-text-top small"><i class="fas fa-university" title="{mcri18n:translate('OMD.ir.docdetails.common.label.affiliation')}" style="font-size:1.25em"></i></th>
          <td class="small">{.}</td>
        </tr>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="corporate_name">
    <xsl:param name="names" />
    <xsl:for-each select="$names">
      <tr><td colspan="2">
        <span><xsl:value-of select="string-join(mods:namePart[not(@type='date')],', ')" /></span>
        <xsl:choose>
          <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
            <span class="small ps-2">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
          </xsl:when>
          <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
            <span class="small ps-2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator_mycore:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
          </xsl:when>
        </xsl:choose>
      </td></tr>
      <xsl:if test="./mods:nameIdentifier[@type='gnd']">
        <tr>
          <th class="text-center small"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.25em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
          <td class="small"><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
        </tr>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
    
</xsl:stylesheet>
