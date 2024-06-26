<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:oai="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification" xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="xsl xlink mods" expand-text="yes">

  <xsl:import href="resource:xslt/functions/classification.xsl" />
  <xsl:import href="resource:xslt/functions/mods.xsl" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
          <oai_dc:dc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
            <xsl:apply-templates select="mods:identifier[@type='doi']" />
            <xsl:apply-templates select="mods:titleInfo" />
            <xsl:apply-templates select="mods:name" />

            <xsl:for-each
              select="mods:name[@type='corporate' and not(./mods:nameIdentifier[@type='gnd']='38329-6') and ./mods:role/mods:roleTerm[@type='code']='dgg']">
              <dc:contributor>
                <xsl:call-template name="name" />
              </dc:contributor>
            </xsl:for-each>

            <xsl:apply-templates select="mods:genre" />
            <xsl:apply-templates select="mods:typeOfResource" />
            <dc:type>info:eu-repo/semantics/publishedVersion</dc:type>
            <xsl:apply-templates select="mods:originInfo" />
            <xsl:apply-templates select="mods:identifier[@type='urn']" />
            <xsl:apply-templates select="mods:identifier[not(@type='doi')][not(@type='urn')]" />
            <xsl:apply-templates select="mods:language" />
            <xsl:apply-templates select="mods:abstract" />
            <xsl:apply-templates select="mods:classification" />
            <xsl:apply-templates select="mods:relatedItem" />
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

  <xsl:template match="mods:name[mods:role[mods:roleTerm[@type='code']='cre' or mods:roleTerm[@type='code']='aut']]">
    <dc:creator>
      <xsl:call-template name="name" />
      <xsl:if test="mods:etal">
        et al.
      </xsl:if>
    </dc:creator>
  </xsl:template>
  
  <xsl:template match="mods:name[not(../mods:name/mods:role[mods:roleTerm[@type='code']='cre' or ../mods:name/mods:roleTerm[@type='code']='aut'])][1]"
                priority="2">
    <dc:creator>
      <xsl:call-template name="name" />
      <xsl:if test="mods:etal">
        et al.
      </xsl:if>
    </dc:creator>
  </xsl:template>
  
  <xsl:template match="mods:name[not(mods:role[mods:roleTerm[@type='code']='cre' or mods:roleTerm[@type='code']='aut'])]">
    <dc:contributor>
      <xsl:call-template name="name" />
        <xsl:if test="mods:etal">
          et al.
      </xsl:if>
    </dc:contributor>
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
        <xsl:variable name="p" select="replace(., 'http://purl.uni-rostock.de', 'https://purl.uni-rostock.de')" />
        <dc:identifier>
          <xsl:value-of select="$p" />
        </dc:identifier>
      </xsl:when>
      <!-- disable because of failed check for GRANT Identifier -->
      <!-- 
      <xsl:when test="contains ('ark arxiv hdl isbn pissn eissn pmid wos', $type)">
        <dc:relation>
          <xsl:value-of select="concat('info:eu-repo/semantics/altIdentifier/',$type,'/',.)" />
        </dc:relation>
      </xsl:when>
      -->
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:classification|mods:genre">
    <xsl:if test="@displayLabel='doctype'">
      <xsl:if test="contains(@valueURI, '#epub.') or contains(@valueURI, '#data')">
        <dc:type>
          {mcrmods:to-category(.)/label[@xml:lang='x-openaire']/@text}
        </dc:type>
      </xsl:if>
      <dc:type>
        {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
      </dc:type>
    </xsl:if>
    <xsl:if test="@displayLabel='sdnb'">
      <dc:subject>
        <xsl:value-of select="concat('info:eu-repo/classification/ddc/', substring-after(@valueURI,'#'))" />
      </dc:subject>
      <dc:subject>
        {mcrmods:to-category(.)/label[@xml:lang='en']/@text}
      </dc:subject>
    </xsl:if>
    <xsl:if test="contains(@valueURI,'licenseinfo#work')">
      <xsl:choose>
        <xsl:when test="(contains(@valueURI, 'licenseinfo#work.cclicense') and contains(@valueURI, '.v40'))">
          <dc:rights>{mcrmods:to-category(.)/label[@xml:lang='x-uri']/@text}</dc:rights>
        </xsl:when>
        <xsl:otherwise>
          <dc:rights>{mcrmods:to-category(.)/label[@xml:lang='en']/@text}</dc:rights>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@displayLabel='accesscondition'">
      <dc:rights>
        {mcrmods:to-category(.)/label[@xml:lang='x-openaire']/@text}
      </dc:rights>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:originInfo[@eventType='publication']">
    <xsl:if test="not(../mods:name)">
      <dc:creator>
        <xsl:value-of select="mods:publisher" />
      </dc:creator>
    </xsl:if>

    <dc:publisher>
      <xsl:value-of select="mods:publisher" />
      <xsl:if test="mods:place/mods:placeTerm">
        <xsl:text> </xsl:text>
        <xsl:value-of select="mods:place/mods:placeTerm" />
      </xsl:if>
    </dc:publisher>
    <dc:date>
      <xsl:value-of select="mods:dateIssued[@keyDate='yes']" />
    </dc:date>
  </xsl:template>

  <xsl:template match="mods:language">
    <dc:language>
      <xsl:value-of select="./mods:languageTerm" />
    </dc:language>
  </xsl:template>
  
  <!-- does not work -->
  <!-- OpenAIRE validator For Literature Repositories (3.0): Field Project Identifier (MA)
       A vocabulary of projects is exposed by OpenAIRE through OAI-PMH, and available for all repository managers. 
       Values include the project name and project ID. The projectID equals the Grant Agreement identifier, 
       and is defined by the info:eu-repo namespace term grantAgreement. 
       The syntax is: info:eu-repo/grantAgreement/Funder/FundingProgram/ProjectID /[Jurisdiction]/[ProjectName]/[ProjectAcronym] 
       Note: If any of the field values contains a forward slash (/), it needs to be escaped using URL encoding (%2F). 
       For instance, My/Project would be represented as My%2FProject. -->
  <!--        
  <xsl:template match="mods:relatedItem[@type='isReferencedBy']">
    <xsl:for-each select="mods:identifier[@type='doi']">
      <dc:relation>info:eu-repo/semantics/reference/doi/{replace(., 'https://doi.org/', ''}</dc:relation>
    </xsl:for-each>
  </xsl:template>
  --> 
</xsl:stylesheet>
