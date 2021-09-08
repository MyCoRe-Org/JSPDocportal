<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
<!-- based on: https://github.com/MyCoRe-Org/mir/blob/master/mir-module/src/main/resources/xsl/mods2record.xsl -->
<xsl:template match="/">
  <record>
    <metadata>
      <xsl:apply-templates select="mycoreobject" mode="metadata" />
    </metadata>
  </record>
</xsl:template>

</xsl:stylesheet>