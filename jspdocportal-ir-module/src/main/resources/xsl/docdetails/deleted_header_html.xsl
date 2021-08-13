<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n" 
  exclude-result-prefixes="mods xlink mcri18n">
 
  <!-- to enable relative urls in import set xsltSystemId attribute in x:transform of JSP XML Tag Library !!! -->
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>

  <xsl:template match="/mycoreobject">
    <h2><xsl:value-of select="mcri18n:translate('OMD.ir.docdetails.deleted.header')" /></h2>
  </xsl:template>

</xsl:stylesheet>