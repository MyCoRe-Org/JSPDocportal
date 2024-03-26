<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:oai="http://www.openarchives.org/OAI/2.0/">

  <xsl:output method="xml" encoding="UTF-8" />

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <xsl:for-each
          select="./structure/derobjects/derobject[classification[@classid='derivate_types' and @categid='DV_METS']][1]">
          <xsl:variable name="fileuri" select="concat('mcrfile:', ./@xlink:href, '/', ./maindoc)" />
          <xsl:copy-of select="document($fileuri)" />
        </xsl:for-each>
      </oai:metadata>
    </oai:record>
  </xsl:template>

</xsl:stylesheet>