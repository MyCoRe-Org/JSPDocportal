<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xlink="http://www.w3.org/1999/xlink" 
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="fn">

  <xsl:include href="resource:xsl/copynodes.xsl" />
  
  <xsl:template match="mods:abstract[string-length(@altFormat) &gt; 0]">
      <xsl:variable name="dataURLcontent" select="document(./@altFormat)" />
      <mods:abstract>
        <xsl:copy-of select="@*" />
        <xsl:value-of select="fn:serialize($dataURLcontent/*/node())" />
      </mods:abstract>
  </xsl:template>

  
</xsl:stylesheet>
