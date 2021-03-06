<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  xmlns:mcrmods="xalan://org.mycore.mods.classification.MCRMODSClassificationSupport"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" 
  xmlns:mcr="http://www.mycore.org/" 
  exclude-result-prefixes="mods xlink xalan i18n mcrmods mcrxsl mcr">
 
  <!-- to enable relative urls in import set xsltSystemId attribute in x:transform of JSP XML Tag Library !!! -->
  <xsl:import href="mods-util.xsl" />
  
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>

  <xsl:template match="/">
    <h2><xsl:value-of select="i18n:translate('Webpage.docdetails.deleted.header')" /></h2>
  </xsl:template>

</xsl:stylesheet>