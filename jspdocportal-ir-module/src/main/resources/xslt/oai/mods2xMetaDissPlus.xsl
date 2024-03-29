<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:oai="http://www.openarchives.org/OAI/2.0/"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"

  xmlns:cmd="http://www.cdlib.org/inside/diglib/copyrightMD"
  xmlns:gndo="http://d-nb.info/standards/elementset/gnd#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
  xmlns:cc="http://www.d-nb.de/standards/cc/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcmitype="http://purl.org/dc/dcmitype/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:pc="http://www.d-nb.de/standards/pc/"
  xmlns:urn="http://www.d-nb.de/standards/urn/"
  xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/"
  xmlns:ddb="http://www.d-nb.de/standards/ddb/"
  xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/"
  xmlns:sub="http://www.d-nb.de/standards/subject/"

  exclude-result-prefixes="cc dc dcmitype dcterms pc urn thesis ddb dini xlink mods xsl gndo rdf cmd"
  xsi:schemaLocation="http://www.d-nb.de/standards/xmetadissplus/  http://files.dnb.de/standards/xmetadissplus/xmetadissplus.xsd">

  <xsl:output method="xml" encoding="UTF-8" />

  <xsl:import href="resource:xslt/functions/i18n.xsl" />
  
  <xsl:param name="ServletsBaseURL" select="''" />
  <xsl:param name="WebApplicationBaseURL" select="''" />
  <xsl:param name="MCR.OAIDataProvider.RepositoryPublisherName" select="''" />
  <xsl:param name="MCR.OAIDataProvider.RepositoryPublisherPlace" select="''" />
  <xsl:param name="MCR.OAIDataProvider.RepositoryPublisherAddress" select="''" />
  <xsl:param name="MCR.Metadata.DefaultLang" select="''" />

  <xsl:variable name="languages" select="document('classification:metadata:-1:children:rfc5646')" />
  <xsl:variable name="marcrelator" select="document('classification:metadata:-1:children:marcrelator')" />
  <xsl:variable name="accesscondition" select="document('classification:metadata:-1:children:accesscondition')" />

  <xsl:key name="contentType" match="child" use="contentType" />
  <xsl:key name="category" match="category" use="@ID" />

  <xsl:variable name="language" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:language/mods:languageTerm[@authority='iso639-2b']/text()" />
  <xsl:variable name="mcrId" select="/mycoreobject/@ID" />
  <xsl:variable name="ifs">
    <xsl:for-each select="mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types' and @categid='fulltext']][1]">
      <der id="{@xlink:href}">
        <xsl:copy-of select="document(concat('xslStyle:mcr_directory-recursive:ifs:',@xlink:href,'/'))" />
      </der>
    </xsl:for-each>
  </xsl:variable>

  <xsl:template match="mycoreobject">
    <oai:record>
      <oai:metadata>
        <xsl:text disable-output-escaping="yes">
          &#60;xMetaDiss:xMetaDiss xmlns:xMetaDiss=&quot;http://www.d-nb.de/standards/xmetadissplus/&quot;
                               xmlns:cc=&quot;http://www.d-nb.de/standards/cc/&quot;
                               xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot;
                               xmlns:dcmitype=&quot;http://purl.org/dc/dcmitype/&quot;
                               xmlns:dcterms=&quot;http://purl.org/dc/terms/&quot;
                               xmlns:pc=&quot;http://www.d-nb.de/standards/pc/&quot;
                               xmlns:urn=&quot;http://www.d-nb.de/standards/urn/&quot;
                               xmlns:doi=&quot;http://www.d-nb.de/standards/doi/&quot;
                               xmlns:hdl=&quot;http://www.d-nb.de/standards/hdl/&quot;
                               xmlns:thesis=&quot;http://www.ndltd.org/standards/metadata/etdms/1.0/&quot;
                               xmlns:ddb=&quot;http://www.d-nb.de/standards/ddb/&quot;
                               xmlns:dini=&quot;http://www.d-nb.de/standards/xmetadissplus/type/&quot;
                               xmlns:sub=&quot;http://www.d-nb.de/standards/subject/&quot;
                               xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;
                               xsi:schemaLocation=&quot;http://www.d-nb.de/standards/xmetadissplus/
                               http://files.dnb.de/standards/xmetadissplus/xmetadissplus.xsd&quot;&#62;
        </xsl:text>
        <xsl:variable name="mods" select="metadata/def.modsContainer/modsContainer/mods:mods" />
        
        <xsl:apply-templates select="$mods" mode="title" />
        <!-- <xsl:apply-templates select="$mods" mode="alternative" /> -->
        <xsl:apply-templates select="$mods" mode="creator" />
        <xsl:apply-templates select="$mods" mode="subject" />
        <xsl:apply-templates select="$mods" mode="abstract" />
        <xsl:apply-templates select="$mods" mode="repositoryPublisher" />
        <xsl:apply-templates select="$mods" mode="contributor" />
        <xsl:apply-templates select="$mods" mode="date" />
        <xsl:apply-templates select="$mods" mode="type" />
        <xsl:apply-templates select="$mods" mode="identifier" />
        <xsl:apply-templates select="$mods" mode="format" />
        <!-- <xsl:apply-templates select="$mods" mode="publisher" /> --> <!-- erzeugt dc:source mit Herausgeber und nicht dem Hinweis zum Original -->
        <!-- <xsl:apply-templates select="$mods" mode="relatedItem2source" />-->
        <xsl:call-template name="language" />
        <!-- <xsl:apply-templates select="$mods" mode="relatedItem2ispartof" /> -->
        <xsl:apply-templates select="$mods" mode="degree" />
        <xsl:call-template name="file" />
        <xsl:apply-templates select="." mode="frontpage" />
        <xsl:apply-templates select="$mods" mode="rights" />
        
        <xsl:text disable-output-escaping="yes">
          &#60;/xMetaDiss:xMetaDiss&#62;
        </xsl:text>
      </oai:metadata>
    </oai:record>
  </xsl:template>

  <xsl:template name="replaceSubSupTags">
    <xsl:param name="content" select="''" />
    <xsl:choose>
      <xsl:when test="contains($content,'&lt;sub&gt;')">
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-before($content,'&lt;sub&gt;')" />
        </xsl:call-template>
        <xsl:text>_</xsl:text>
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-after($content,'&lt;sub&gt;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($content,'&lt;/sub&gt;')">
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-before($content,'&lt;/sub&gt;')" />
        </xsl:call-template>
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-after($content,'&lt;/sub&gt;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($content,'&lt;sup&gt;')">
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-before($content,'&lt;sup&gt;')" />
        </xsl:call-template>
        <xsl:text>^</xsl:text>
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-after($content,'&lt;sup&gt;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($content,'&lt;/sup&gt;')">
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-before($content,'&lt;/sup&gt;')" />
        </xsl:call-template>
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="substring-after($content,'&lt;/sup&gt;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$content" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="title" match="mods:mods">
    <dc:title xsi:type="ddb:titleISO639-2">
      <xsl:attribute name="lang">
        <xsl:value-of select="$language" />
      </xsl:attribute>
      
      <xsl:for-each select="./mods:titleInfo[@usage='primary']">
        <xsl:if test="./mods:nonSort">
          <xsl:value-of select="./mods:nonSort" />
          <xsl:text> </xsl:text>  
      	</xsl:if>
      	<xsl:value-of select="./mods:title" />
      	<xsl:if test="./mods:subTitle">
          <xsl:text> : </xsl:text>
          <xsl:value-of select="./mods:subTitle" />
      	</xsl:if>

    	<xsl:if test="./mods:partNumber or ./mods:partName">
    	  <xsl:text> </xsl:text>
      	  <xsl:value-of select="./mods:partNumber" />
      	  <xsl:if test="./mods:partNumber and ./mods:partName">
      		 <xsl:text>: </xsl:text>
      	  </xsl:if>
      	  <xsl:value-of select="./mods:partName" />
		</xsl:if>
 	  </xsl:for-each>
    </dc:title>
  </xsl:template>

  <xsl:template mode="creator" match="mods:mods">
    <xsl:for-each select="mods:name[mods:role/mods:roleTerm[@type='code'][@authority='marcrelator'][text()='aut']]">
      <dc:creator xsi:type="pc:MetaPers">
        <xsl:apply-templates select="." mode="pc-person" />
      </dc:creator>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template mode="pc-person" match="mods:name">
    <pc:person>
      <xsl:if test="mods:nameIdentifier[@type='gnd']">
        <xsl:attribute name="PND-Nr">
          <xsl:value-of select="mods:nameIdentifier[@type='gnd']" />
        </xsl:attribute>
      </xsl:if>
      <pc:name>
        <xsl:choose>
          <xsl:when test="@type='corporate'">
            <xsl:attribute name="type">otherName</xsl:attribute>
            <xsl:attribute name="otherNameType">organisation</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="type">nameUsedByThePerson</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="@type='corporate'">
            <pc:organisationName>
              <xsl:value-of select="mods:displayForm" />
            </pc:organisationName>
          </xsl:when>
          <xsl:when test="mods:namePart[@type='family'] and mods:namePart[@type='given']">
            <xsl:apply-templates select="mods:namePart[@type='given']" mode="pc-person" />
            <xsl:apply-templates select="mods:namePart[@type='family']" mode="pc-person" />
          </xsl:when>
          <xsl:when test="contains(mods:displayForm, ',')">
            <pc:foreName>
              <xsl:value-of select="normalize-space(substring-after(mods:displayForm,','))" />
            </pc:foreName>
            <pc:surName>
              <xsl:value-of select="normalize-space(substring-before(mods:displayForm,','))" />
            </pc:surName>
          </xsl:when>
          <xsl:otherwise>
            <pc:personEnteredUnderGivenName>
              <xsl:value-of select="mods:displayForm" />
            </pc:personEnteredUnderGivenName>
          </xsl:otherwise>
        </xsl:choose>
      </pc:name>
    </pc:person>
  </xsl:template>
  
  <xsl:template mode="pc-person" match="mods:namePart[@type='family']">
    <pc:surName>
      <xsl:value-of select="normalize-space(.)" />
    </pc:surName>
  </xsl:template>

  <xsl:template mode="pc-person" match="mods:namePart[@type='given']">
    <pc:foreName>
      <xsl:value-of select="." />
    </pc:foreName>
  </xsl:template>

  <xsl:template mode="subject" match="mods:mods">
    <xsl:for-each select="mods:classification[contains(@authorityURI,'/SDNB')]">
      <dc:subject xsi:type="xMetaDiss:DDC-SG">
        <xsl:value-of select="substring-after(@valueURI, '#')" />
      </dc:subject>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="abstract" match="mods:mods">
    <xsl:for-each select="mods:abstract[@type='summary'][not(@altFormat)]">
      <dcterms:abstract xsi:type="ddb:contentISO639-2">
      	<xsl:choose>
      		<xsl:when test="@lang">
      			<xsl:attribute name="lang"><xsl:value-of select="@lang"/></xsl:attribute>
      		</xsl:when>
      		<xsl:when test="@xml:lang='de'">
      			<xsl:attribute name="lang">ger</xsl:attribute>
      		</xsl:when>
      		<xsl:when test="@xml:lang='en'">
      			<xsl:attribute name="lang">eng</xsl:attribute>
      		</xsl:when>
      		<xsl:when test="@xml:lang='fr'">
      			<xsl:attribute name="lang">fre</xsl:attribute>
      		</xsl:when>
      		<xsl:when test="@xml:lang='es'">
      			<xsl:attribute name="lang">spa</xsl:attribute>
      		</xsl:when>
      	</xsl:choose>
        <xsl:call-template name="replaceSubSupTags">
          <xsl:with-param name="content" select="." />
        </xsl:call-template>
      </dcterms:abstract>
    </xsl:for-each>
  </xsl:template>

 <!-- dc:source Publisher als Quelle falsch? - Ich würde hier Hinweis auf das gedruckte-Original (ISBN, ...)  angeben -->
  <xsl:template mode="publisher" match="mods:mods">
    <xsl:variable name="publisherRoles" select="$marcrelator/mycoreclass/categories/category[@ID='pbl']/descendant-or-self::category" />
    <xsl:variable name="publisher_name">
      <xsl:choose>
        <xsl:when test="mods:originInfo[not(@eventType) or @eventType='publication']/mods:publisher">
          <xsl:value-of select="mods:originInfo[not(@eventType) or @eventType='publication']/mods:publisher" />
        </xsl:when>
        <xsl:when test="mods:name[$publisherRoles/@ID=mods:role/mods:roleTerm/text()]">
          <xsl:value-of select="mods:name[mods:role/mods:roleTerm/text()='pbl']/mods:displayForm" />
        </xsl:when>
        <xsl:when test="mods:accessCondition[@type='copyrightMD']/cmd:copyright/cmd:rights.holder/cmd:name">
          <xsl:value-of select="mods:accessCondition[@type='copyrightMD']/cmd:copyright/cmd:rights.holder/cmd:name" />
        </xsl:when>
        <xsl:when test="mods:relatedItem[@type='host']/mods:originInfo[not(@eventType) or @eventType='publication']/mods:publisher">
          <xsl:value-of
            select="mods:mods/mods:relatedItem[@type='host']/mods:originInfo[not(@eventType) or @eventType='publication']/mods:publisher" />
        </xsl:when>
        <xsl:when test="mods:relatedItem[@type='host']/mods:name[mods:role/mods:roleTerm/text()='pbl']">
          <xsl:value-of select="mods:relatedItem[@type='host']/mods:name[mods:role/mods:roleTerm/text()='pbl']/mods:displayForm" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="publisher_place">
      <xsl:choose>
        <xsl:when test="mods:originInfo[not(@eventType) or @eventType='publication']/mods:place/mods:placeTerm[@type='text']">
          <xsl:value-of select="mods:originInfo[not(@eventType) or @eventType='publication']/mods:place/mods:placeTerm[@type='text']" />
        </xsl:when>
        <xsl:when
          test="mods:relatedItem[@type='host']/mods:originInfo[not(@eventType) or @eventType='publication']/mods:place/mods:placeTerm[@type='text']">
          <xsl:value-of
            select="mods:relatedItem[@type='host']/mods:originInfo[not(@eventType) or @eventType='publication']/mods:place/mods:placeTerm[@type='text']" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="string-length($publisher_name) &gt; 0">
      <xsl:choose>
        <xsl:when test="string-length($publisher_place) &gt; 0">
          <dc:source xsi:type="ddb:noScheme">
            <xsl:value-of select="concat($publisher_place,' : ',$publisher_name)" />
          </dc:source>
        </xsl:when>
        <xsl:otherwise>
          <dc:source xsi:type="ddb:noScheme">
            <xsl:value-of select="$publisher_name" />
          </dc:source>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="repositoryPublisher" match="mods:mods">
    <xsl:choose>
      <xsl:when
        test="mods:originInfo[@eventType='publication']/mods:publisher and mods:originInfo[@eventType='publication']/mods:place/mods:placeTerm[@type='text']">
        <xsl:call-template name="repositoryPublisherElement">
          <xsl:with-param name="name" select="mods:originInfo[@eventType='online_publication']/mods:publisher" />
          <xsl:with-param name="place" select="mods:originInfo[@eventType='online_publication']/mods:place/mods:placeTerm[@type='text']" />
          <xsl:with-param name="address" select="''" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="repositoryPublisherElement">
          <xsl:with-param name="name" select="$MCR.OAIDataProvider.RepositoryPublisherName" />
          <xsl:with-param name="place" select="$MCR.OAIDataProvider.RepositoryPublisherPlace" />
          <xsl:with-param name="address" select="$MCR.OAIDataProvider.RepositoryPublisherAddress" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="repositoryPublisherElement">
    <xsl:param name="name" />
    <xsl:param name="place" />
    <xsl:param name="address" />
    <dc:publisher xsi:type="cc:Publisher" type="dcterms:ISO3166" countryCode="DE">
      <cc:universityOrInstitution>
      <xsl:choose>
      	<xsl:when test="$name='Universitätsbibliothek' and $place='Rostock'"> 
      		<xsl:attribute name="cc:GKD-Nr" namespace="http://www.d-nb.de/standards/cc/">25968-8</xsl:attribute>
      	</xsl:when>
      	<xsl:when test="$name='Universität' and $place='Rostock'"> 
      		<xsl:attribute name="cc:GKD-Nr" namespace="http://www.d-nb.de/standards/cc/">38329-6</xsl:attribute>
      	</xsl:when>
      </xsl:choose>
        <cc:name>
          <xsl:value-of select="$name" />
        </cc:name>
        <cc:place>
          <xsl:value-of select="$place" />
        </cc:place>
      </cc:universityOrInstitution>
      <xsl:if test="$address">
        <cc:address cc:Scheme="DIN5008">
          <xsl:value-of select="$address" />
        </cc:address>
      </xsl:if>
    </dc:publisher>
  </xsl:template>

  <xsl:template mode="contributor" match="mods:mods">
    <xsl:for-each select="mods:name[mods:role/mods:roleTerm='ths']">
      <dc:contributor xsi:type="pc:Contributor" thesis:role="advisor">
        <xsl:apply-templates select="." mode="pc-person" />
      </dc:contributor>
    </xsl:for-each>
    <xsl:for-each select="mods:name[mods:role[mods:roleTerm='rev' or mods:roleTerm='dgs']]">
      <dc:contributor xsi:type="pc:Contributor" thesis:role="referee">
        <xsl:apply-templates select="." mode="pc-person" />
      </dc:contributor>
    </xsl:for-each>
    <xsl:for-each select="mods:name[mods:role/mods:roleTerm='edt']">
      <dc:contributor xsi:type="pc:Contributor" thesis:role="editor">
        <xsl:apply-templates select="." mode="pc-person" />
      </dc:contributor>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="date" match="mods:mods">
    <xsl:if test="mods:originInfo[@eventType='creation']/mods:dateCreated[@encoding='iso8601']">
      <dcterms:created xsi:type="dcterms:W3CDTF">
        <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:dateCreated[@encoding='iso8601']" />
      </dcterms:created>
    </xsl:if>
    <xsl:if test="mods:originInfo[@eventType='creation']/mods:dateOther[@type='defence'][@encoding='iso8601']">
      <dcterms:dateAccepted xsi:type="dcterms:W3CDTF">
        <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:dateOther[@type='defence'][@encoding='iso8601']" />
      </dcterms:dateAccepted>
    </xsl:if>

    <xsl:if test="mods:originInfo[@eventType='publication']/mods:dateIssued[@encoding='iso8601']">
      <dcterms:issued xsi:type="dcterms:W3CDTF">
        <xsl:value-of select="mods:originInfo[@eventType='publication']/mods:dateIssued[@encoding='iso8601']" />
      </dcterms:issued>
    </xsl:if>
    <xsl:for-each select="../../../../service/servdates/servdate[@type='modifydate']">
      <dcterms:modified xsi:type="dcterms:W3CDTF">
        <xsl:value-of select="." />
      </dcterms:modified>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="type" match="mods:mods">
    <dc:type xsi:type="dini:PublType">
      <xsl:choose>
        <xsl:when test="mods:classification[contains(@authorityURI,'diniPublType')]">
          <xsl:value-of select="substring-after(mods:classification[contains(@authorityURI,'diniPublType')]/@valueURI,'diniPublType#')" />
        </xsl:when>
        <xsl:when test="mods:genre[contains(@valueURI, 'article')]">
          <xsl:text>contributionToPeriodical</xsl:text>
        </xsl:when>
        <xsl:when test="mods:genre[contains(@valueURI, 'issue')]">
          <xsl:text>PeriodicalPart</xsl:text>
        </xsl:when>
        <xsl:when test="mods:genre[contains(@valueURI, 'journal')]">
          <xsl:text>Periodical</xsl:text>
        </xsl:when>
        <xsl:when test="mods:genre[contains(@valueURI, 'book')]">
          <xsl:text>book</xsl:text>
        </xsl:when>
        <xsl:when test="mods:genre[contains(@valueURI, 'dissertation') or contains(@valueURI, 'habilitation')]">
          <xsl:text>doctoralThesis</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>Other</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </dc:type>
    <dc:type xsi:type="dcterms:DCMIType">
      <xsl:text>Text</xsl:text>
    </dc:type>
    <dini:version_driver>
      <xsl:text>publishedVersion</xsl:text>
    </dini:version_driver>
  </xsl:template>

  <xsl:template mode="identifier" match="mods:mods">
      <!-- only one dc:identifier -->
      <xsl:if test="mods:identifier[@type='urn' and starts-with(text(), 'urn:nbn')]">
        <dc:identifier xsi:type="urn:nbn">
          <xsl:value-of select="mods:identifier[@type='urn' and starts-with(text(), 'urn:nbn')][1]" />
        </dc:identifier>
      </xsl:if>
      <xsl:if test="mods:identifier[@type='doi']">
        <ddb:identifier xsi:type="DOI">
          <xsl:value-of select="mods:identifier[@type='doi'][1]" />
        </ddb:identifier>
      </xsl:if>
      <xsl:if test="mods:identifier[@type='hdl' or @type='handle']">
        <ddb:identifier xsi:type="handle">
          <xsl:value-of select="mods:identifier[@type='hdl' or @type='handle'][1]" />
        </ddb:identifier>
      </xsl:if>
      <xsl:if test="mods:identifier[@type='purl']">
      <xsl:variable name="p" select="replace(mods:identifier[@type='purl'], 'http://purl.uni-rostock.de', 'https://purl.uni-rostock.de')" />
        <ddb:identifier xsi:type="URL">
          <xsl:value-of select="$p" />
        </ddb:identifier>
      </xsl:if>
  </xsl:template>

  <xsl:template mode="format" match="mods:mods">
    <xsl:apply-templates select="$ifs/der/mcr_directory/children/child[generate-id(.)=generate-id(key('contentType', contentType)[1])]"
      mode="format" />
  </xsl:template>

  <xsl:template mode="format" match="child">
    <dcterms:medium xsi:type="dcterms:IMT">
      <xsl:value-of select="contentType" />
    </dcterms:medium>
  </xsl:template>

  <xsl:template mode="relatedItem2source" match="mods:mods">
      <!--  If not use isPartOf use dc:source -->
    <xsl:for-each select="mods:relatedItem[@type='host']">
      <xsl:variable name="hosttitel" select="mods:titleInfo/mods:title" />
      <xsl:variable name="issue" select="mods:part/mods:detail[@type='issue']/mods:number" />
      <xsl:variable name="volume" select="mods:part/mods:detail[@type='volume']/mods:number" />
      <xsl:variable name="startPage" select="mods:part/mods:extent[@unit='pages']/mods:start" />
      <xsl:variable name="endPage" select="mods:part/mods:extent[@unit='pages']/mods:end" />
      <xsl:variable name="volume2">
        <xsl:if test="string-length($volume) &gt; 0">
          <xsl:value-of select="concat('(',$volume,')')" />
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="issue2">
        <xsl:if test="string-length($issue) &gt; 0">
          <xsl:value-of select="concat(', H. ',$issue)" />
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="pages">
        <xsl:if test="string-length($startPage) &gt; 0">
          <xsl:value-of select="concat(', S.',$startPage,'-',$endPage)" />
        </xsl:if>
      </xsl:variable>
      <dc:source xsi:type="ddb:noScheme">
        <xsl:value-of select="concat($hosttitel,$volume2,$issue2,$pages)" />
      </dc:source>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="language">
    <dc:language xsi:type="dcterms:ISO639-2">
      <xsl:value-of select="$language" />
    </dc:language>
  </xsl:template>

<!-- dcterms:isPartOf xsi:type="ddb:Erstkat-ID" >2049984-X</dcterms:isPartOf>
<dcterms:isPartOf xsi:type="ddb:ZS-Ausgabe" >2004</dcterms:isPartOf -->
  <xsl:template mode="relatedItem2ispartof" match="mods:mods">
      <!-- Ausgabe der Schriftenreihe ala: <dcterms:isPartOf xsi:type=“ddb:noScheme“>Bulletin ; 34</dcterms:isPartOf>  -->
    <xsl:if test="mods:relatedItem/@type='series'">
      <dcterms:isPartOf xsi:type="ddb:noScheme">
        <xsl:value-of select="mods:relatedItem[@type='series']/mods:titleInfo/mods:title" />
        <xsl:if test="mods:relatedItem[@type='series']/mods:part/mods:detail[@type='volume']/mods:number">
          <xsl:value-of select="concat(' ; ',mods:relatedItem[@type='series']/mods:part/mods:detail[@type='volume']/mods:number)" />
        </xsl:if>
      </dcterms:isPartOf>
    </xsl:if>
    <xsl:if test="contains(mods:genre/@valueURI, 'issue')">
      <dcterms:isPartOf xsi:type="ddb:ZSTitelID">
        <xsl:value-of select="mods:relatedItem[@type='host']/@xlink:href" />
      </dcterms:isPartOf>
      <xsl:if test="mods:relatedItem[@type='host']/mods:part/mods:detail[@type='volume']">
        <dcterms:isPartOf xsi:type="ddb:ZS-Ausgabe">
          <xsl:choose>
            <xsl:when test="mods:relatedItem[@type='host']/mods:part/mods:detail[@type='issue']">
              <xsl:value-of
                select="concat(normalize-space(mods:relatedItem[@type='host']/mods:part/mods:detail[@type='volume']),
                  ', ',
                  mcri18n:translate('component.mods.metaData.dictionary.issue'),
                  ' ',
                  normalize-space(mods:relatedItem[@type='host']/mods:part/mods:detail[@type='issue']))" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="normalize-space(mods:relatedItem[@type='host']/mods:part/mods:detail[@type='volume'])" />
            </xsl:otherwise>
          </xsl:choose>
        </dcterms:isPartOf>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="degree" match="mods:mods">
    <xsl:variable name="thesis_level">
      <xsl:for-each select="mods:classification">
        <xsl:if test="contains(./@authorityURI,'XMetaDissPlusThesisLevel')">
          <thesis:level>
            <xsl:value-of select="substring-after(./@valueURI,'#')" />
          </thesis:level>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="string-length($thesis_level) &gt; 0">
      <thesis:degree>
        <xsl:copy-of select="$thesis_level" />
        <thesis:grantor xsi:type="cc:Corporate" type="dcterms:ISO3166" countryCode="DE">
          <cc:universityOrInstitution>
            <xsl:choose>
               <!-- wenn Uni Rostock:  Uni + Fakultät (nur falls die DNB das explizit haben will -->
               <!-- 
               <xsl:when test="mods:name[@type='corporate'][mods:nameIdentifier[@type='gnd']='38329-6'][mods:role/mods:roleTerm[@type='code']='dgg']">
                	<xsl:attribute name="cc:GKD-Nr" namespace="http://www.d-nb.de/standards/cc/">38329-6</xsl:attribute>
                	<cc:name>Universität</cc:name>
                	<cc:place>Rostock</cc:place>
	             	<xsl:for-each select="mods:name[@type='corporate'][not(mods:nameIdentifier[@type='gnd']='38329-6')][mods:role/mods:roleTerm[@type='code']='dgg'][1]">
    	          		<cc:department>
    	          			<cc:name>
                  				<xsl:value-of select="mods:namePart[1]" />
                			</cc:name>
                			<cc:place>Rostock</cc:place>
                	</cc:department>
                </xsl:for-each>
              </xsl:when>
              -->
              <xsl:when test="mods:name[@type='corporate'][not(mods:nameIdentifier[@type='gnd']='38329-6')][mods:role/mods:roleTerm[@type='code']='dgg']">
              	<xsl:for-each select="mods:name[@type='corporate'][not(mods:nameIdentifier[@type='gnd']='38329-6')][mods:role/mods:roleTerm[@type='code']='dgg']">
              		<xsl:if test="mods:nameIdentifier[@type='gnd']">
              			<xsl:attribute name="cc:GKD-Nr" namespace="http://www.d-nb.de/standards/cc/">
              				<xsl:value-of select="mods:nameIdentifier[@type='gnd']" />
              			</xsl:attribute>
              		</xsl:if>
              		<cc:name>
                  		<xsl:value-of select="mods:namePart[1]" />
                  		<xsl:for-each select="mods:namePart[position()>1]">
                  		    <xsl:text>. </xsl:text>
                  			<xsl:value-of select="text()" />
                  		</xsl:for-each>
                	</cc:name>
                </xsl:for-each>
              </xsl:when>
              <xsl:when test="mods:name[@type='corporate'][mods:role/mods:roleTerm[@type='code']='dgg']">
              	<xsl:for-each select="mods:name[@type='corporate'][mods:role/mods:roleTerm[@type='code']='dgg']">
              		<xsl:if test="mods:nameIdentifier[@type='gnd']">
              			<xsl:attribute name="cc:GKD-Nr" namespace="http://www.d-nb.de/standards/cc/">
              				<xsl:value-of select="mods:nameIdentifier[@type='gnd']" />
              			</xsl:attribute>
              		</xsl:if>
              		<cc:name>
                  		<xsl:value-of select="mods:namePart[1]" />
                  		<xsl:for-each select="mods:namePart[position()>1]">
                  		    <xsl:text>. </xsl:text>
                  			<xsl:value-of select="text()" />
                  		</xsl:for-each>
                	</cc:name>
                </xsl:for-each>
              </xsl:when>
              <xsl:when test="mods:originInfo[@eventType='creation']/mods:publisher">
                <cc:name>
                  <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:publisher" />
                </cc:name>
                <cc:place>
                  <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:place/mods:placeTerm" />
                </cc:place>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="repositoryPublisher">
                  <xsl:apply-templates select="." mode="repositoryPublisher" />
                </xsl:variable>
                <xsl:comment>value of dc:publisher</xsl:comment>
                <xsl:copy-of select="$repositoryPublisher/dc:publisher/cc:universityOrInstitution/*" />
              </xsl:otherwise>
            </xsl:choose>
          </cc:universityOrInstitution>
        </thesis:grantor>
      </thesis:degree>
    </xsl:if>
  </xsl:template>

  <xsl:template name="file">
    <xsl:if test="$ifs/der">
      <xsl:variable name="ddbfilenumber" select="count($ifs/der/mcr_directory/children//child[@type='file'])" />
      <xsl:variable name="dernumber" select="count($ifs/der)" />
      <ddb:fileNumber>
        <xsl:value-of select="$ddbfilenumber" />
      </ddb:fileNumber>
      <xsl:apply-templates mode="fileproperties" select="$ifs/der">
        <xsl:with-param name="totalFiles" select="$ddbfilenumber" />
      </xsl:apply-templates>
      <ddb:transfer ddb:type="dcterms:URI">
        <xsl:choose>
          <xsl:when test="$ddbfilenumber = 1">
            <xsl:variable name="uri" select="$ifs/der/mcr_directory/children//child[@type='file']/uri" />
            <xsl:variable name="derId" select="substring-before(substring-after($uri,':/'), ':')" />
            <xsl:variable name="filePath" select="substring-after(substring-after($uri, ':'), ':')" />
            <xsl:value-of select="concat($WebApplicationBaseURL,'file/',$mcrId,'/',$derId,$filePath)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="$dernumber = 1">
                <xsl:value-of select="concat($ServletsBaseURL,'MCRZipServlet/',$ifs/der/@id)" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($ServletsBaseURL,'MCRZipServlet/',/mycoreobject/@ID)" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </ddb:transfer>
       <xsl:if test="$ddbfilenumber = 1">
      	<ddb:checksum ddbType="MD5">
      			<xsl:value-of select="$ifs/der/mcr_directory/children//child[@type='file']/md5" />
      	</ddb:checksum>
        </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="fileproperties" match="der[@id]">
    <xsl:param name="totalFiles" />
    <xsl:variable name="derId" select="@id" />
    <xsl:for-each select="mcr_directory/children//child[@type='file']">
      <ddb:fileProperties>
        <xsl:attribute name="ddb:fileName"><xsl:value-of select="name" /></xsl:attribute>
        <xsl:attribute name="ddb:fileID"><xsl:value-of select="uri" /></xsl:attribute>
        <xsl:attribute name="ddb:fileSize"><xsl:value-of select="size" /></xsl:attribute>
        <xsl:if test="$totalFiles &gt; 1">
          <xsl:attribute name="ddb:fileDirectory">
            <xsl:value-of select="concat($derId, substring-after(uri, concat('ifs:/',$derId,':')))" />
          </xsl:attribute>
        </xsl:if>
      </ddb:fileProperties>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="frontpage" match="mycoreobject">
    <ddb:identifier ddb:type="URL_Frontdoor">
      <xsl:value-of select="concat($WebApplicationBaseURL,'resolve/id/',@ID)" />
    </ddb:identifier>
  </xsl:template>

  <xsl:template mode="rights" match="mods:mods">
      <!-- TODO: check access permission -->
      <!-- xsl:element name="ddb:rights">
       <xsl:attribute name="ddb:kind">free</xsl:attribute>
      </xsl:element -->
      <xsl:choose>
        <xsl:when test="mods:classification[contains(@valueURI, '/classifications/accesscondition#openaccess')]">
          <ddb:rights ddb:kind="free" />
        </xsl:when>  
        <xsl:when test="mods:classification[contains(@valueURI, '/classifications/accesscondition#restrictedaccess')]">
    	  <ddb:rights ddb:kind="domain">
    	  	<xsl:variable name="accessCondID" select="substring-after(mods:classification[contains(@valueURI, '/classifications/accesscondition#restrictedaccess')]/@valueURI,'#')" />
    	  	<xsl:if test="$accesscondition//category[@ID=$accessCondID]/label[@xml:lang='x-display-de']">
    	  		<xsl:value-of select="$accesscondition//category[@ID=$accessCondID]/label[@xml:lang='x-display-de']/@text" />
    	  	</xsl:if>
    	  </ddb:rights>
        </xsl:when>
        <xsl:otherwise>
      	  <ddb:rights ddb:kind="unknown" />
        </xsl:otherwise> 
      </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
