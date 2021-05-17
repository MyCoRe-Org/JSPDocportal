<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:oai="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan xsl xlink mods mcr">
  <xsl:param name="WebApplicationBaseURL" select="''"/>
  <xsl:param name="ServletsBaseURL" select="''"/>
  <xsl:param name="HttpSession" select="''"/>

<!-- /mycore/mycore-mods/src/main/resources/xsl/mods2dc.xsl -->
<!-- <xsl:include href="mods2dc.xsl" -->

  <xsl:include href="mods2record.xsl"/>
  <xsl:include href="mods-utils.xsl"/>
  <xsl:include href="../docdetails/mods-util.xsl"/>

  <xsl:template match="mycoreobject" mode="metadata">
    <xsl:variable name="objId" select="@ID"/>

    <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
      <oai_dc:dc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
        <xsl:apply-templates select="mods:identifier[@type='doi']"/>

        <xsl:apply-templates select="mods:titleInfo"/>
        <xsl:apply-templates select="mods:name"/>
        <xsl:for-each
          select="mods:name[@type='corporate' and not(./mods:nameIdentifier[@type='gnd']='38329-6') and ./mods:role/mods:roleTerm[@type='code']='dgg']">
          <dc:contributor>
            <xsl:call-template name="name"/>
          </dc:contributor>
        </xsl:for-each>
        <xsl:apply-templates select="mods:genre"/>
        <xsl:apply-templates select="mods:typeOfResource"/>
        <dc:type>info:eu-repo/semantics/publishedVersion</dc:type>
        <xsl:apply-templates select="mods:originInfo"/>

        <xsl:apply-templates select="mods:identifier[@type='urn']"/>
        <xsl:apply-templates select="mods:identifier[not(@type='doi')][not(@type='urn')]"/>
        <xsl:apply-templates select="mods:language"/>
        <xsl:apply-templates select="mods:abstract"/>
        <xsl:apply-templates select="mods:classification"/>

      </oai_dc:dc>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="mods:titleInfo">
    <dc:title>
      <xsl:value-of select="mods:nonSort"/>
      <xsl:if test="mods:nonSort">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="mods:title"/>
      <xsl:if test="mods:subTitle">
        <xsl:text>:</xsl:text>
        <xsl:value-of select="mods:subTitle"/>
      </xsl:if>
      <xsl:if test="mods:partNumber">
        <xsl:text>. </xsl:text>
        <xsl:value-of select="mods:partNumber"/>
      </xsl:if>
      <xsl:if test="mods:partName">
        <xsl:text>. </xsl:text>
        <xsl:value-of select="mods:partName"/>
      </xsl:if>
    </dc:title>
  </xsl:template>

  <xsl:template match="mods:name">
    <xsl:if test="@type='personal'">
      <xsl:choose>
        <xsl:when test="mods:role[mods:roleTerm[@type='code']='cre' or mods:roleTerm[@type='code']='aut' ]">
          <dc:creator>
            <xsl:call-template name="name"/>
            <xsl:if test="mods:etal">
              et al.
            </xsl:if>
          </dc:creator>
        </xsl:when>
        <xsl:otherwise>
          <dc:contributor>
            <xsl:call-template name="name"/>
            <xsl:if test="mods:etal">
              et al.
            </xsl:if>
          </dc:contributor>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:abstract">
    <xsl:if test="@type='summary'">
      <dc:description>
        <xsl:value-of select="text()"/>
      </dc:description>
    </xsl:if>
    <xsl:if test="@type='author_keywords'">
      <dc:subject>
        <xsl:value-of select="text()"/>
      </dc:subject>
    </xsl:if>
  </xsl:template>

  <xsl:template name="name">
    <xsl:variable name="name">
      <xsl:for-each select="mods:namePart[not(@type)]">
        <xsl:value-of select="."/>
        <xsl-if test="not(. = ../mods:namePart[not(@type)][last()])">
          <xsl:text>. </xsl:text>
        </xsl-if>
      </xsl:for-each>
      <xsl:value-of select="mods:namePart[@type='family']"/>
      <xsl:if test="mods:namePart[@type='given']">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="mods:namePart[@type='given']"/>
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="normalize-space($name)"/>
  </xsl:template>

  <xsl:template match="mods:identifier">
    <xsl:variable name="type"
      select="translate(@type,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
    <xsl:choose>
      <xsl:when test="$type='openaire'">
        <dc:relation>
          <xsl:value-of select="."/>
        </dc:relation>
      </xsl:when>
      <xsl:when test="contains ('doi', $type)">
        <dc:identifier>
          <xsl:value-of select="concat('https://doi.org/',.)"/>
        </dc:identifier>
      </xsl:when>
      <xsl:when test="contains ('purl urn', $type)">
        <dc:identifier>
          <xsl:value-of select="."/>
        </dc:identifier>
      </xsl:when>
      <xsl:when test="contains ('ark arxiv hdl isbn pissn eissn pmid wos', $type)">
        <dc:relation>
          <xsl:value-of select="concat('info:eu-repo/semantics/altIdentifier/',$type,'/',.)"/>
        </dc:relation>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:classification|mods:genre">
    <xsl:if test="@displayLabel='doctype'">
      <xsl:if test="contains(@valueURI, '#epub.')">
        <dc:type>
          <xsl:call-template name="classLabel">
            <xsl:with-param name="valueURI" select="@valueURI"/>
            <xsl:with-param name="lang" select="'x-openaire'"/>
          </xsl:call-template>
        </dc:type>
      </xsl:if>
      <dc:type>
        <xsl:call-template name="classLabel">
          <xsl:with-param name="valueURI" select="@valueURI"/>
          <xsl:with-param name="lang" select="'en'"/>
        </xsl:call-template>
      </dc:type>
    </xsl:if>
    <xsl:if test="@displayLabel='sdnb'">
      <dc:subject>
        <xsl:value-of select="concat('info:eu-repo/classification/ddc/', substring-after(@valueURI,'#'))"/>
      </dc:subject>
      <dc:subject>
        <xsl:call-template name="classLabel">
          <xsl:with-param name="valueURI" select="@valueURI"/>
          <xsl:with-param name="lang" select="'en'"/>
        </xsl:call-template>
      </dc:subject>
    </xsl:if>
    <xsl:if test="contains(@valueURI,'licenseinfo#work')">
      <xsl:choose>
        <xsl:when test="(contains(@valueURI, 'licenseinfo#work.cclicense') and contains(@valueURI, '.v40'))">
          <dc:rights>
            <xsl:call-template name="classLabel">
              <xsl:with-param name="valueURI" select="./@valueURI"/>
              <xsl:with-param name="lang" select="'x-uri'"/>
            </xsl:call-template>
          </dc:rights>
        </xsl:when>
        <xsl:otherwise>
          <dc:rights>
            <xsl:call-template name="classLabel">
              <xsl:with-param name="valueURI" select="@valueURI"/>
              <xsl:with-param name="lang" select="'en'"/>
            </xsl:call-template>
          </dc:rights>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@displayLabel='accesscondition'">
      <dc:rights>
        <xsl:call-template name="classLabel">
          <xsl:with-param name="valueURI" select="@valueURI"/>
          <xsl:with-param name="lang" select="'x-openaire'"/>
        </xsl:call-template>
      </dc:rights>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:originInfo">
    <xsl:if test="@eventType='publication'">
      <dc:publisher>
        <xsl:value-of select="mods:publisher"/>
        <xsl:if test="mods:place/mods:placeTerm">
          <xsl:text> </xsl:text>
          <xsl:value-of select="mods:place/mods:placeTerm"/>
        </xsl:if>
      </dc:publisher>
      <dc:date>
        <xsl:value-of select="mods:dateIssued"/>
      </dc:date>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:language">
    <dc:language>
      <xsl:value-of select="./mods:languageTerm"/>
    </dc:language>
  </xsl:template>
</xsl:stylesheet>
