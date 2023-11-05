<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:oai="http://www.openarchives.org/OAI/2.0/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods" exclude-result-prefixes="xsl xlink mods" expand-text="true">

  <xsl:param name="WebApplicationBaseURL" select="''" />

  <xsl:import href="resource:xslt/functions/mods.xsl" />

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
          <oai_dc:dc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
            <dc:identifier>
              <xsl:value-of select="concat($WebApplicationBaseURL, 'resolve/id/', /mycoreobject/@ID)"></xsl:value-of>
            </dc:identifier>
            <xsl:apply-templates select="mods:titleInfo" />
            <xsl:apply-templates select="mods:name" />
            <xsl:apply-templates select="mods:genre" />
            <xsl:apply-templates select="mods:typeOfResource" />
            <xsl:apply-templates select="mods:originInfo" />
            <xsl:apply-templates select="mods:identifier[@type='doi']" />
            <xsl:apply-templates select="mods:identifier[@type='urn']" />
            <xsl:apply-templates select="mods:identifier[not(@type='doi')][not(@type='urn')]" />
            <xsl:apply-templates select="mods:abstract" />
            <xsl:apply-templates select="mods:classification" />
          </oai_dc:dc>
        </xsl:for-each>
      </oai:metadata>
    </oai:record>
  </xsl:template>

  <xsl:template match="mods:titleInfo">
    <dc:title>
      <xsl:value-of select="mods:nonSort" />
      <xsl:if test="mods:nonSort">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="mods:title" />
      <xsl:if test="mods:subTitle">
        <xsl:text>: </xsl:text>
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
  </xsl:template>

  <xsl:template match="mods:typeOfResource">
    <xsl:if test="@collection='yes'">
      <dc:type>Collection</dc:type>
    </xsl:if>
    <xsl:if test=". ='software' and ../mods:genre='database'">
      <dc:type>Dataset</dc:type>
    </xsl:if>
    <xsl:if test=".='software' and ../mods:genre='online system or service'">
      <dc:type>Service</dc:type>
    </xsl:if>
    <xsl:if test=".='software'">
      <dc:type>Software</dc:type>
    </xsl:if>
    <xsl:if test=".='cartographic material'">
      <dc:type>Image</dc:type>
    </xsl:if>
    <xsl:if test=".='multimedia'">
      <dc:type>InteractiveResource</dc:type>
    </xsl:if>
    <xsl:if test=".='moving image'">
      <dc:type>MovingImage</dc:type>
    </xsl:if>
    <xsl:if test=".='three dimensional object'">
      <dc:type>PhysicalObject</dc:type>
    </xsl:if>
    <xsl:if test="starts-with(.,'sound recording')">
      <dc:type>Sound</dc:type>
    </xsl:if>
    <xsl:if test=".='still image'">
      <dc:type>StillImage</dc:type>
    </xsl:if>
    <xsl:if test=". ='text'">
      <dc:type>Text</dc:type>
    </xsl:if>
    <xsl:if test=".='notated music'">
      <dc:type>Text</dc:type>
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
        <xsl:text> </xsl:text>
      </xsl:for-each>
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:if test="mods:namePart[@type='given']">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="mods:namePart[@type='given']" />
      </xsl:if>
      <xsl:if test="mods:nameIdentifier[@type='gnd']">
        <xsl:text> (gnd: </xsl:text>
        <xsl:value-of select="mods:nameIdentifier[@type='gnd']" />
        <xsl:text>)</xsl:text>
      </xsl:if>
      <xsl:if test="mods:displayForm">
        <xsl:text> (</xsl:text>
        <xsl:value-of select="mods:displayForm" />
        <xsl:text>) </xsl:text>
      </xsl:if>
      <xsl:for-each
        select="mods:role[mods:roleTerm[@type='code']!='cre' and mods:roleTerm[@type='code']!='aut' and mods:roleTerm[@type='text']]">
        <xsl:text> [</xsl:text>
        <xsl:value-of select="mods:roleTerm[@type='text']" />
        <xsl:text>] </xsl:text>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="normalize-space($name)" />
  </xsl:template>

  <xsl:template match="mods:identifier">
    <dc:identifier>
      <xsl:variable name="type"
        select="translate(@type,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
      <xsl:choose>
        <!-- 2.0: added identifier type attribute to output, if it is present -->
        <xsl:when test="contains(.,':')">
          <xsl:value-of select="." />
        </xsl:when>
        <xsl:when test="@type">
          <xsl:value-of select="$type" />
          <xsl:text>: </xsl:text>
          <xsl:value-of select="." />
        </xsl:when>
        <xsl:when test="contains ('isbn issn uri doi lccn uri', $type)">
          <xsl:value-of select="$type" />
          <xsl:text>: </xsl:text>
          <xsl:value-of select="." />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:classification[@valueURI]|mods:genre[@valueURI]">
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
    <xsl:if test="contains(@valueURI,'licenseinfo#work') or @displayLabel='accesscondition'">
      <dc:rights>
        {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
      </dc:rights>
    </xsl:if>
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
        <xsl:value-of select="mods:dateIssued" />
      </dc:date>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
