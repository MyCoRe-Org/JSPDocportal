<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2005-11-14 12:51:02 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" encoding="UTF-8" />

<xsl:param name="DefaultLang" />
<xsl:param name="CurrentLang" />

<xsl:template match="/mycoreobject">
 <item>
  <xsl:attribute name="ID">
   <xsl:value-of select="@ID" />
  </xsl:attribute>
  <!-- Title -->
  <label>
  <xsl:if test="metadata/titles/title">
   <xsl:choose>
    <xsl:when test="metadata/titles/title[lang($CurrentLang)]" >
     <xsl:value-of select="metadata/titles/title[lang($CurrentLang)]" />
    </xsl:when>
    <xsl:when test="metadata/titles/title[lang($DefaultLang)]" >
     <xsl:value-of select="metadata/titles/title[lang($DefaultLang)]" />
    </xsl:when>
    <xsl:otherwise>
     <xsl:value-of select="metadata/titles/title" />
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>
  </label>
  <!-- Creator -->
  <xsl:if test="metadata/creators/creator">
   <xsl:for-each select="metadata/creators/creator">
    <data>
     <xsl:value-of select="text()|*" />
    </data>
   </xsl:for-each>
  </xsl:if>
  <!-- Create Date -->
  <xsl:if test="service/servdates/servdate">
   <data>
   <xsl:for-each select="service/servdates/servdate">
    <xsl:if test="@type = 'modifydate'">
     Zuletzt bearbeitet am <xsl:value-of select="text()|*" />
    </xsl:if>
   </xsl:for-each>
   </data>
  </xsl:if>
  <!-- Identifier -->
  <xsl:if test="metadata/identifiers/identifier">
   <data>
    <xsl:choose>
     <xsl:when test="metadata/identifiers/identifier[lang($CurrentLang)]" >
      <xsl:value-of select="metadata/identifiers/identifier[lang($CurrentLang)]"/>
     </xsl:when>
     <xsl:when test="metadata/identifiers/identifier[lang($DefaultLang)]" >
      <xsl:value-of select="metadata/identifiers/identifier[lang($DefaultLang)]"/>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="metadata/identifiers/identifier" />
     </xsl:otherwise>
    </xsl:choose>
   </data>
  </xsl:if>
 </item>
</xsl:template>

</xsl:stylesheet>

