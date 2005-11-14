<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2005-11-14 12:51:02 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" encoding="UTF-8" />

<xsl:template match="/mycoreobject">
 <item>
  <xsl:attribute name="ID">
   <xsl:value-of select="@ID" />
  </xsl:attribute>
  <!-- Name -->
  <label>
  <xsl:if test="metadata/names/name">
   <xsl:value-of select="metadata/names/name/fullname" />
  </xsl:if>
  </label>
  <!-- Date -->
  <xsl:if test="metadata/addresses/address">
   <xsl:for-each select="metadata/addresses/address">
    <data>
     <xsl:value-of select="city" />
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
 </item>
</xsl:template>

</xsl:stylesheet>

