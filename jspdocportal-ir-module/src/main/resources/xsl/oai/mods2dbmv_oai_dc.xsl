<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:oai="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification" xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="xsl xlink mods" expand-text="yes">

  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
          <oai_dc:dc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

            <xsl:apply-templates select="mods:identifier[@type='doi' or @type='purl']" />
            <xsl:apply-templates
              select="mods:identifier[@type='urn'][not(../mods:identifier/@type='doi')]" />

            <xsl:apply-templates select="mods:titleInfo" />

            <xsl:apply-templates select="mods:name[@type='personal']" />
            <xsl:apply-templates
              select="mods:name[@type='corporate' and not(./mods:nameIdentifier[@type='gnd']='38329-6') and ./mods:role/mods:roleTerm[@type='code']='dgg']" />

            <xsl:apply-templates select="mods:originInfo" />
            <xsl:apply-templates select="mods:language" />
            <xsl:apply-templates select="mods:abstract" />
            <xsl:apply-templates select="mods:classification" />
          </oai_dc:dc>
        </xsl:for-each>
      </oai:metadata>
    </oai:record>
  </xsl:template>

  <xsl:template match="mods:identifier">
    <xsl:variable name="type"
      select="translate(@type,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$type='openaire'">
        <dc:relation>
          <xsl:value-of select="." />
        </dc:relation>
      </xsl:when>
      <xsl:when test="contains ('doi', $type)">
        <dc:identifier>
          <xsl:value-of select="concat('https://doi.org/',.)" />
        </dc:identifier>
      </xsl:when>
      <xsl:when test="contains ('purl urn', $type)">
        <dc:identifier>
          <xsl:value-of select="." />
        </dc:identifier>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:titleInfo">
    <dc:title>
      <xsl:value-of select="mods:nonSort" />
      <xsl:if test="mods:nonSort">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="mods:title" />
      <xsl:if test="mods:subTitle">
        <xsl:text>:</xsl:text>
        <xsl:value-of select="mods:subTitle" />
      </xsl:if>
      <xsl:if test="mods:partNumber">
        <xsl:text>. </xsl:text>
        <xsl:value-of select="mods:partNumber" />
      </xsl:if>
      <xsl:if test="mods:partName">
        <xsl:text>. </xsl:text>
        <xsl:value-of select="mods:partName" />
      </xsl:if>
    </dc:title>
  </xsl:template>

  <xsl:template match="mods:name">
    <xsl:if test="@type='personal'">
      <xsl:choose>
        <xsl:when test="mods:role[mods:roleTerm[@type='code']='cre' or mods:roleTerm[@type='code']='aut' ]">
          <dc:creator>
            <xsl:call-template name="name" />
            <xsl:if test="mods:etal">
              et al.
            </xsl:if>
          </dc:creator>
        </xsl:when>
        <xsl:otherwise>
          <dc:contributor>
            <xsl:call-template name="name" />
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
        <xsl:value-of select="text()" />
      </dc:description>
    </xsl:if>
    <xsl:if test="@type='author_keywords'">
      <dc:subject>
        <xsl:value-of select="text()" />
      </dc:subject>
    </xsl:if>
  </xsl:template>

  <xsl:template name="name">
    <xsl:variable name="name">
      <xsl:for-each select="mods:namePart[not(@type)]">
        <xsl:value-of select="." />
        <xsl-if test="not(. = ../mods:namePart[not(@type)][last()])">
          <xsl:text>. </xsl:text>
        </xsl-if>
      </xsl:for-each>
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:if test="mods:namePart[@type='given']">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="mods:namePart[@type='given']" />
      </xsl:if>
    </xsl:variable>
    <xsl:value-of select="normalize-space($name)" />
  </xsl:template>

  <xsl:template match="mods:originInfo">
    <xsl:if test="@eventType='publication'">
      <dc:publisher>
        <xsl:value-of select="mods:publisher" />
        <xsl:if test="mods:place/mods:placeTerm">
          <xsl:text> </xsl:text>
          <xsl:value-of select="mods:place/mods:placeTerm" />
        </xsl:if>
      </dc:publisher>
      <dc:date>
        <xsl:value-of select="mods:dateIssued[not(@encoding)]" />
      </dc:date>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:classification">
    <xsl:if test="@displayLabel='doctype'">
      <dc:type>
        {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
      </dc:type>
    </xsl:if>
    <xsl:if test="@displayLabel='sdnb'">
      <dc:subject>
        {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
      </dc:subject>
    </xsl:if>
    <xsl:if test="contains(@valueURI,'licenseinfo#work')">
      <xsl:choose>
        <xsl:when test="(contains(@valueURI, 'licenseinfo#work.cclicense') and contains(@valueURI, '.v40'))">
          <dc:rights>
            {mcrmods:to-category(.)/label[@xml:lang='x-uri']/@text}
          </dc:rights>
        </xsl:when>
        <xsl:otherwise>
          <dc:rights>
            {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
          </dc:rights>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:language">
    <dc:language>
      <xsl:value-of select="./mods:languageTerm" />
    </dc:language>
  </xsl:template>

</xsl:stylesheet>
