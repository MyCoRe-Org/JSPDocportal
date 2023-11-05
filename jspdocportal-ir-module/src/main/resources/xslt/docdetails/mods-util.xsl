<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
     xmlns="http://www.w3.org/1999/xhtml" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:mods="http://www.loc.gov/mods/v3"
     xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
     exclude-result-prefixes="mods xlink">

<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />     
<xsl:import href="resource:xslt/functions/i18n.xsl" />

<xsl:template name="mods-title">
  <xsl:for-each select="./mods:titleInfo[@usage='primary']">
    <h3>
      <xsl:if test="./mods:nonSort">
        <xsl:value-of select="./mods:nonSort" />&#160;  
      </xsl:if>
      <xsl:value-of select="./mods:title" />
      <xsl:if test="./mods:subTitle">
        <br /><small><xsl:value-of select="./mods:subTitle" /></small>
      </xsl:if>
    </h3>
    <xsl:if test="./mods:partNumber or ./mods:partName">
     <h3>
      <xsl:value-of select="./mods:partNumber" />
      <xsl:if test="./mods:partNumber and ./mods:partName">: </xsl:if>
      <xsl:value-of select="./mods:partName" />
     </h3>
  </xsl:if>

  </xsl:for-each>
</xsl:template>

    <xsl:template name="display-name">
      <xsl:param name="name"  />
      <xsl:if test="$name/@type='personal'">
        <xsl:choose>
          <xsl:when test="$name/mods:namePart[@type='family'] and ./mods:namePart[@type='given']">
            <xsl:value-of select="$name/mods:namePart[@type='family']" />,&#160;
            <xsl:value-of select="$name/mods:namePart[@type='given']" />
            <xsl:if test="$name/mods:namePart[@type='termsOfAddress']" >
              &#160;(<xsl:value-of select="$name/mods:namePart[@type='termsOfAddress']" />)
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="$name/mods:namePart[not(@type='date')]">
              <xsl:value-of select="." />&#160;
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$name/mods:nameIdentifier or $name/mods:affiliation">
          <xsl:element name="button">
            <xsl:attribute name="type">button</xsl:attribute>
            <xsl:attribute name="data-toggle">popover</xsl:attribute>
            <xsl:attribute name="class">btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
            <xsl:attribute name="data-mcr-action">popover4person</xsl:attribute>
            <xsl:if test="$name/mods:nameIdentifier[@type='gnd']">
              <xsl:attribute name="data-mcr-value-gnd"><xsl:value-of select="$name/mods:nameIdentifier[@type='gnd']" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="$name/mods:affiliation">
              <xsl:attribute name="data-mcr-value-affiliation"><xsl:value-of select="$name/mods:affiliation" /></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="title">{mcri18n:translate('OMD.ir.docdetails.mods_util.name_info')}</xsl:attribute>
            <xsl:element name="span">
              <xsl:attribute name="class">fa fa-info-circle</xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:if>
      </xsl:if>
      <xsl:if test="$name/@type='corporate'">
        <xsl:for-each select="./mods:namePart">
          <xsl:value-of select="." />
          <xsl:if test="position()!=last()">
            <xsl:text>,&#160;</xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:if test="$name/mods:nameIdentifier or $name/mods:affiliation">
          <xsl:element name="button">
            <xsl:attribute name="type">button</xsl:attribute>
            <xsl:attribute name="data-toggle">popover</xsl:attribute>
            <xsl:attribute name="class">btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
            <xsl:attribute name="data-mcr-action">popover4person</xsl:attribute>
            <xsl:if test="$name/mods:nameIdentifier[@type='gnd']">
              <xsl:attribute name="data-mcr-value-gnd"><xsl:value-of select="$name/mods:nameIdentifier[@type='gnd']" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="$name/mods:affiliation">
              <xsl:attribute name="data-mcr-value-affiliation"><xsl:value-of select="$name/mods:affiliation" /></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="title">{mcri18n:translate('OMD.ir.docdetails.mods_util.name_info')}</xsl:attribute>
            <xsl:element name="span">
              <xsl:attribute name="class">fa fa-info-circle</xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:if>
      </xsl:if>
    
      <xsl:if test="$name/mods:role/mods:roleTerm[@authority='gbv']">
        &#160;<xsl:value-of select="$name/mods:role/mods:roleTerm[@authority='gbv']" />
      </xsl:if>
    </xsl:template>

<xsl:template name="mods-name">
  <xsl:for-each select="./mods:name[@type='personal'][position()=1 or contains('aut edt cre', ./mods:role/mods:roleTerm[@type='code']/text())]">
    <xsl:call-template name="display-name">
      <xsl:with-param name="name" select="." />
    </xsl:call-template>

    <xsl:if test="position()!=last() or ./../mods:name[@type='corporate'][not(contains('dgg', ./mods:role/mods:roleTerm[@type='code']/text()))]">
      <xsl:text>&#160;;&#160;&#160;</xsl:text>
    </xsl:if>
   </xsl:for-each>
   
   <xsl:for-each select="./mods:name[@type='corporate'][not(contains('dgg', ./mods:role/mods:roleTerm[@type='code']/text()))]">
     <xsl:call-template name="display-name">
       <xsl:with-param name="name" select="." />
     </xsl:call-template>
       
     <xsl:if test="position()!=last()">
       <xsl:text>&#160;;&#160;&#160;</xsl:text>
     </xsl:if>
    </xsl:for-each>
   
</xsl:template>

<xsl:template name="mods-name-short">
<xsl:for-each select="./mods:name[(@type='personal' and(position()=1 or contains('aut edt', ./mods:role/mods:roleTerm[@type='code']/text()))) or @type='corporate']">
    <xsl:if test="position()&lt;4">
    <xsl:if test="./@type='personal'">
    <xsl:choose>
    <xsl:when test="./mods:namePart[@type='family'] and ./mods:namePart[@type='given']">
    <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
    <xsl:value-of select="./mods:namePart[@type='given']" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:for-each select="./mods:namePart[not(@type='date')]">
        <xsl:value-of select="." />&#160;
      </xsl:for-each>
    </xsl:otherwise>
    </xsl:choose>
   </xsl:if>
   
   <xsl:if test="./@type='corporate'">
        <xsl:for-each select="./mods:namePart">
          <xsl:value-of select="." />
          <xsl:if test="position()!=last()">
            <xsl:text>&#160;</xsl:text>
          </xsl:if>
        
        </xsl:for-each>
  </xsl:if>       
       
       <xsl:if test="position()!=last()">
      <xsl:text>&#160;;&#160;&#160;</xsl:text>
    </xsl:if>
    
  
    </xsl:if>
    </xsl:for-each>
    <xsl:if test="count(./mods:name[(@type='personal' and(position()=1 or contains('aut edt', ./mods:role/mods:roleTerm[@type='code']/text()))) or @type='corporate'])&gt;3">
      ...
    </xsl:if>
   
</xsl:template>




<xsl:template name="mods-originInfo">
            <xsl:choose>
              <xsl:when test="mods:originInfo[@eventType='publication']"> 
                <xsl:for-each select="mods:originInfo[@eventType='publication'][1]">
                  <xsl:if test="mods:edition"><xsl:value-of select="mods:edition" /><xsl:value-of select="' , '" /></xsl:if>    
                  <xsl:if test="mods:place/mods:placeTerm"><xsl:value-of select="mods:place/mods:placeTerm" /><xsl:value-of select="' : '" /></xsl:if>
                  <xsl:if test="mods:publisher"><xsl:value-of select="mods:publisher" /><xsl:value-of select="' , '" /></xsl:if>
                  <xsl:value-of select="mods:dateIssued" />
                    <xsl:value-of select="mods:dateCreated[@qualifier='approximate']" />
                </xsl:for-each>  
              </xsl:when>
              <xsl:when test="mods:originInfo[@eventType='creation']"> 
                <xsl:for-each select="mods:originInfo[@eventType='creation'][1]">
                  <xsl:if test="mods:edition"><xsl:value-of select="mods:edition" /><xsl:value-of select="' , '" /></xsl:if>    
                  <xsl:if test="mods:place/mods:placeTerm"><xsl:value-of select="mods:place/mods:placeTerm" /><xsl:value-of select="' : '" /></xsl:if>
                  <xsl:if test="mods:publisher"><xsl:value-of select="mods:publisher" /><xsl:value-of select="' , '" /></xsl:if>
                  <xsl:value-of select="mods:dateIssued" />
                  <xsl:value-of select="mods:dateCreated[@qualifier='approximate']" />
                </xsl:for-each>  
              </xsl:when>
            </xsl:choose>
</xsl:template>

<xsl:template name="language">
    <xsl:param name="term"  />
    <xsl:param name="lang"  />
    <!-- <xsl:variable name="url"><xsl:value-of select="$WebApplicationBaseURL" />api/v1/classifications/rfc5656</xsl:variable> -->
    <xsl:variable name="url">classification:metadata:-1:children:rfc5656</xsl:variable>
    <xsl:value-of select="document($url)/mycoreclass/categories/category[./label[@xml:lang='x-bibl']/@text=$term]/label[@xml:lang=$lang]/@text" />
</xsl:template>
  
  <!-- retrieves the label of a MyCoRe classification category  for a given MODS valueURI -->
  <!--  reverse is necessary since sub-string-after-last is not supported  -->
  <xsl:template name="classLabel">
    <xsl:param name="valueURI"  />
    <xsl:param name="lang">de</xsl:param>
     <xsl:variable name="x">
      <xsl:call-template name="reverse">
        <xsl:with-param name="input"><xsl:value-of select="$valueURI" /></xsl:with-param>
     </xsl:call-template>
     </xsl:variable>

    <xsl:variable name="y" select="substring-before($x, '/')" />
 
    <xsl:variable name="z">
      <xsl:call-template name="reverse">
        <xsl:with-param name="input"><xsl:value-of select="$y" /></xsl:with-param>
     </xsl:call-template>
     </xsl:variable>
          
     <xsl:variable name="classid" select="substring-before($z,'#')" />
     <xsl:variable name="categid" select="substring-after($z,'#')" />
 
     <!-- <xsl:variable name="url"><xsl:value-of select="$WebApplicationBaseURL" />api/v1/classifications/<xsl:value-of select="$classid" />?filter=root:<xsl:value-of select="$categid" />;nochildren;lang:<xsl:value-of select="$lang" /></xsl:variable>
     http://localhost:8080/rosdok/api/v1/classifications/rosdok_class_000000000009?filter=root:00;lang:de;nochildren -->

     <!-- Alternative: URI-Resolver -->
      <xsl:variable name="url">classification:metadata:0:children:<xsl:value-of select="$classid" />:<xsl:value-of select="$categid" /></xsl:variable>
      <xsl:value-of select="document($url)/mycoreclass/categories/category[1]/label[@xml:lang=$lang]/@text" />   
</xsl:template>


  <!-- Reverse Algorithm from: XSLT Cookbook, 2nd Edition -->
  <xsl:template name="reverse">
     <xsl:param name="input"/>
     <xsl:variable name="len" select="string-length($input)"/>
     <xsl:choose>
          <!-- Strings of length less than 2 are trivial to reverse -->
          <xsl:when test="$len &lt; 2">
               <xsl:value-of select="$input"/>
          </xsl:when>
          <!-- Strings of length 2 are also trivial to reverse -->
          <xsl:when test="$len = 2">
               <xsl:value-of select="substring($input,2,1)"/>
               <xsl:value-of select="substring($input,1,1)"/>
          </xsl:when>
          <xsl:otherwise>
               <!-- Swap the recursive application of this template to 
               the first half and second half of input -->
               <xsl:variable name="mid" select="floor($len div 2)"/>
               <xsl:call-template name="reverse">
                    <xsl:with-param name="input"
                         select="substring($input,$mid+1,$mid+1)"/>
               </xsl:call-template>
               <xsl:call-template name="reverse">
                    <xsl:with-param name="input"
                         select="substring($input,1,$mid)"/>
               </xsl:call-template>
          </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

