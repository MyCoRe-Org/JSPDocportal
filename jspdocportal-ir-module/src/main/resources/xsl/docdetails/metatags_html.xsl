<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  version="1.0" exclude-result-prefixes="mods xlink ubr-legal">

  <xsl:param name="WebApplicationBaseURL" select="'http://rosdok.uni-rostock.de/'"/>
  <xsl:param name="WebApplicationTitle" select="'RosDok'"/>
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
  
  <xsl:template match="/">
  <xsl:for-each select="//modsContainer[1]/mods:mods">
    <xsl:variable name="mods_title">
      <xsl:for-each select="./mods:titleInfo[@usage='primary']">
        <xsl:if test="./mods:nonSort">
          <xsl:value-of select="concat(./mods:nonSort,' ')" />
        </xsl:if>
        <xsl:value-of select="./mods:title" />
        <xsl:if test="./mods:subTitle">
         : <xsl:value-of select="./mods:subTitle" />
        </xsl:if>
        <xsl:if test="./mods:partNumber or ./mods:partName">
           ; <xsl:value-of select="./mods:partNumber" />
          <xsl:if test="./mods:partNumber and ./mods:partName">: </xsl:if>
          <xsl:value-of select="./mods:partName" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    
    <title><xsl:value-of select="normalize-space($mods_title)" />  @ <xsl:value-of select="$WebApplicationTitle" /></title>
    <meta name="citation_title" content="{normalize-space($mods_title)}" />
    <meta name="DC.title" content="{normalize-space($mods_title)}" /> 
  
    <xsl:for-each select="./mods:name[@type='personal'][./mods:role/mods:roleTerm[@type='code']='aut' or ./mods:role/mods:roleTerm[@type='code']='cre' or ./mods:role/mods:roleTerm[@type='code']='edt']">
      <xsl:variable name="mods_name" select="concat(./mods:namePart[@type='given'],' ', ./mods:namePart[@type='family'])" />
      <meta name="citation_author" content="{$mods_name}" />
      <meta name="DC.creator" content="{$mods_name}" />
      <meta name="author" content="{$mods_name}" />
    </xsl:for-each>
    
    <xsl:for-each select="./mods:originInfo[@eventType='publication' or @eventType='creation']">
      <xsl:variable name="mods_pubdate" select="./mods:dateIssued" />
      <meta name="citation_publication_date" content="{$mods_pubdate}" />
      <meta name="DC.issued" content="{$mods_pubdate}" />
      
      <xsl:variable name="mods_publisher" select="concat(string-join(./mods:publisher, ', '),' ', string-join(./mods:place/mods:placeTerm, ', '))" />
      <meta name="citation_publisher" content="{$mods_publisher}" />
      <meta name="DC.publisher" content="{$mods_publisher}" />
    </xsl:for-each>
    
    <xsl:for-each select="./mods:originInfo[@eventType='online_publication']/mods:dateCaptured/text()|./mods:originInfo[@eventType='online_publication']/mods:dateIssued/text()">
      <meta name="citation_online_date" content="{.}" />
    </xsl:for-each>
    
    
    
     <xsl:for-each select="./mods:name[@type='corporate'][./mods:role/mods:roleTerm[@type='code']='dgg'][1]">
      <xsl:variable name="mods_dgg" select="./mods:namePart[not(@type)][1]" />
      <meta name="citation_dissertation_institution" content="{$mods_dgg}" />
      <meta name="DC.creator" content="{$mods_dgg}" />
    </xsl:for-each>
    
    <xsl:variable name="mcrid" select="/mycoreobject/@ID" />
    <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[@xlink:title='fulltext'][1]">
      <xsl:variable name="derId" select="@xlink:href" />
      
      <xsl:variable name="derXML" select="document(concat('mcrobject:',$derId))" />
      <!-- Debug: <xsl:variable name="derXML" select="document(concat('http://rosdok.uni-rostock.de/api/v1/objects/',$mcrid,'/derivates/', $derId))" /> -->
      <xsl:for-each select="$derXML/mycorederivate/derivate/internals/internal">
        <xsl:if test="string-length(@maindoc)>0">
          <xsl:variable name="file_fulltext"><xsl:value-of select="$WebApplicationBaseURL"/>file/<xsl:value-of select="$derXML/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href" />/<xsl:value-of select="$derXML/mycorederivate/@ID" />/<xsl:value-of select="@maindoc" /></xsl:variable>
          <meta name="citation_pdf_url" content="{$file_fulltext}" />
          <meta name="DC.identifier" content="{$file_fulltext}" />
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>

    <meta name="citation_abstract_url" content="{$WebApplicationBaseURL}resolve/id/{$mcrid}" />
    <meta name="DC.identifier" content="{$WebApplicationBaseURL}resolve/id/{$mcrid}" />
    
    <xsl:for-each select="./mods:identifier[@type='purl']/text()">
      <meta name="citation_abstract_url" content="{.}" />
      <meta name="DC.identifier" content="{.}" />
    </xsl:for-each>
    <xsl:for-each select="./mods:identifier[@type='urn']/text()">
      <meta name="citation_abstract_url" content="https://nbn-resolving.org/{.}" />
      <meta name="DC.identifier" content="{.}" />
    </xsl:for-each>
    <xsl:for-each select="./mods:identifier[@type='doi']/text()">
      <meta name="citation_doi" content="{.}" />
      <meta name="DC.identifier" content="https://doi.org/{.}" />
    </xsl:for-each>
    <xsl:for-each select="./mods:identifier[@type='isbn']/text()">
      <meta name="citation_isbn" content="{.}" />
      <meta name="DC.identifier" content="{.}" />
    </xsl:for-each>
    
    <xsl:for-each select="./mods:abstract[@type='summary']/text()">
      <meta name="DC.description" content="{.}" />
      <meta name="description" content="{.}" />
    </xsl:for-each>
    <xsl:for-each select="./mods:classification[contains(@valueURI, 'licenseinfo#work')]/text()">
      <meta name="DC.rights" content="{.}" />
    </xsl:for-each>
    <xsl:for-each select="./mods:genre[@displayLabel='doctype']/text()">
      <meta name="DC.type" content="{.}" />
    </xsl:for-each>
  </xsl:for-each>
  <xsl:for-each select="/mycoreobject/service/servdates/servdate[@type='modifydate']/text()">
      <meta name="date" content="{.}" />
  </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>