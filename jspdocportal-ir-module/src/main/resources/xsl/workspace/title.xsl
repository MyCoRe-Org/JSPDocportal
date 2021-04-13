<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  exclude-result-prefixes="mods xlink mcri18n mcracl">
 
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>

  <xsl:template match="/mycoreobject">
  <xsl:choose>
    <xsl:when test="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods/mods:titleInfo/mods:title">
      <xsl:value-of select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods/mods:titleInfo/mods:title" />
    </xsl:when>
    <xsl:when test="./metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods/mods:titleInfo/mods:title[@type='primary']">
      <xsl:value-of select="./metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods/mods:titleInfo/mods:title[@type='primary']" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="mcri18n:translate('WF.common.newObject')" />
    </xsl:otherwise>
  </xsl:choose>
  </xsl:template>

</xsl:stylesheet>