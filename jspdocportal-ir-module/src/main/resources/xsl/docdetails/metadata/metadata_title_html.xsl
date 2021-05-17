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
    <tr><td>
      <xsl:if test="..[local-name()='relatedItem']/mods:recordInfo/mods:recordIdentifier">
        <span class="float-right">
          <a class="btn btn-outline-secondary btn-sm" href="{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(../mods:recordInfo/mods:recordIdentifier, '/','_')}">Öffnen</a>
        </span>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="mods:partName or mods:partNumber">
          <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
          <br />
          <xsl:element name="{if(usage='primary') then 'strong' else 'span'}">
            <xsl:value-of select="string-join((mods:partNumber, mods:partName),' ')" />
          </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{if(@usage='primary') then 'strong' else 'span'}">
            <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
          </xsl:element>
        </xsl:otherwise> 
      </xsl:choose>
      <xsl:if test="@type">
        <span class="float-right small">
          [{mcri18n:translate(concat('OMD.ir.docdetails.othertitle.type.', @type))}]
        </span>
      </xsl:if>
    </td></tr>
  </xsl:template>    
</xsl:stylesheet>