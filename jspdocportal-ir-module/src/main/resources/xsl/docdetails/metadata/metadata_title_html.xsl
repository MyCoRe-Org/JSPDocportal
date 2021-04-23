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
  
  <xsl:template name="title">
    <xsl:if test="@type">
      <tr><td class="text-right">
      [{mcri18n:translate(concat('OMD.ir.docdetails.othertitle.type.', @type))}]
      </td></tr>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="mods:partName or mods:partNumber">
        <tr><td>
          <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
        </td></tr>
        <tr><td><strong>
          <xsl:value-of select="string-join((mods:partNumber, mods:partName),' ')" />
        </strong></td></tr>
      </xsl:when>
      <xsl:otherwise>
        <tr><td><strong>
          <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
        </strong></td></tr>
      </xsl:otherwise> 
    </xsl:choose>
  </xsl:template>    
</xsl:stylesheet>