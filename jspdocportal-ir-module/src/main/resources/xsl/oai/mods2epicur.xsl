<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:urn="http://www.ddb.de/standards/urn"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:oai="http://www.openarchives.org/OAI/2.0/" exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="UTF-8" />

  <xsl:param name="WebApplicationBaseURL" select="''" />

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <epicur
          xsi:schemaLocation="urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd"
          xmlns="urn:nbn:de:1111-2004033116" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <xsl:if test="./metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']">
            <xsl:variable name="urn">
              <xsl:choose>
                <xsl:when
                  test="./metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']">
                  <xsl:value-of
                    select="./metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:message terminate="yes">
                    <xsl:value-of select="concat('Could not find URN in metadata ',@ID)" />
                  </xsl:message>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="epicurType" select="'url_update_general'" />
            <administrative_data>
              <delivery>
                <update_status type="{$epicurType}" />
              </delivery>
            </administrative_data>
            <record>
              <identifier scheme="urn:nbn:de">
                <xsl:value-of select="$urn" />
              </identifier>
              <resource>
                <!-- metadata -->
                <identifier scheme="url" role="primary" origin="original" type="frontpage">
                  <xsl:if test="$epicurType = 'urn_new' or $epicurType= 'url_update_general'">
                    <xsl:attribute name="status">new</xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="concat($WebApplicationBaseURL,'resolve/id/', @ID)" />
                </identifier>
                <format scheme="imt">
                  <xsl:value-of select="'text/html'" />
                </format>
              </resource>
            </record>
          </xsl:if>
        </epicur>
      </oai:metadata>
    </oai:record>

  </xsl:template>

<!-- former XSLT 1.0 for fulltext (PDF) - rewrite it should be used again! -->
<!-- 
  <xsl:template mode="epicurResource" match="der">
    <xsl:variable name="filenumber" select="count(mcr_directory/children//child[@type='file'])" />
    <xsl:choose>
      <xsl:when test="$filenumber = 0" />
      <xsl:when test="$filenumber = 1">
        <resource xmlns="urn:nbn:de:1111-2004033116">
          <xsl:variable name="uri" select="mcr_directory/children//child[@type='file']/uri" />
          <xsl:variable name="derId" select="substring-before(substring-after($uri,':/'), ':')" />
          <xsl:variable name="filePath" select="substring-after(substring-after($uri, ':'), ':')" />
          <identifier scheme="url">
            <xsl:value-of select="concat($WebApplicationBaseURL,'file/',@mcrid, '/',$derId,$filePath)" />
          </identifier>
          <format scheme="imt">
            <xsl:value-of select="$uri/../contentType" />
          </format>
        </resource>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
-->
</xsl:stylesheet>
