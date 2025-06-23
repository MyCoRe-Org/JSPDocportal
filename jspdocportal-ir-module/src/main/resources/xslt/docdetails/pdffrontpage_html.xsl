<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="mods xlink mcri18n mcracl mcrstring mcrclass mcrmods"
  expand-text="yes">

  <xsl:output method="xhtml" indent="yes" standalone="omit" encoding="UTF-8"/>
   
  <xsl:import href="resource:xslt/functions/i18n.xsl" />
  <xsl:import href="resource:xslt/functions/acl.xsl" />
  <xsl:import href="resource:xslt/functions/stringutils.xsl" />
  <xsl:import href="resource:xslt/functions/classification.xsl" />
  <xsl:import href="resource:xslt/functions/mods.xsl" />
  
  <xsl:import href="resource:xslt/docdetails/header/header_names_html.xsl" />
  <xsl:import href="resource:xslt/docdetails/header/header_otherversions_html.xsl" />
  
  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template match="/mycoreobject">
  
    <!-- Metadata Header -->  
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
      
      <xsl:call-template name="headerNames_noPopup" />
  
      <!-- Title -->
      <xsl:for-each select="./mods:titleInfo[@usage='primary']">
        <xsl:variable name="title_primary">
          {./mods:nonSort} {./mods:title}
          <xsl:if test="./mods:subTitle"> : {./mods:subTitle}</xsl:if>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="./mods:partNumber or ./mods:partName">
              <h3>{$title_primary}</h3>
              <h2><xsl:value-of select="string-join((./mods:partNumber, ./mods:partName), ' : ')" /></h2>
            </xsl:when>
            <xsl:otherwise>
               <h2>{$title_primary}</h2>
            </xsl:otherwise>
          </xsl:choose>
      </xsl:for-each>
    
      <!-- Veröffentlichungsangabe -->
      <xsl:for-each select="./mods:originInfo[@eventType='publication']">
        <p>
          <xsl:choose>
            <xsl:when test="contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest')">
              <xsl:variable name="publisherPlace"
                select="if (./mods:place[not(@supplied='yes')]/mods:placeTerm|./mods:publisher) 
                        then (string-join((./mods:place[not(@supplied='yes')]/mods:placeTerm, ./mods:publisher), ': '))
                        else ()" />
              
              {string-join((./mods:edition, $publisherPlace, ./mods:dateIssued[not(@*)]), ', ')}
            </xsl:when>
            <xsl:otherwise>
              {string-join((./mods:edition, ./mods:publisher, ./mods:dateIssued[not(@*)]), ', ')}
            </xsl:otherwise>
          </xsl:choose>
        </p>
      </xsl:for-each>
    
      <!-- erschienen in -->
      <xsl:for-each select="./mods:relatedItem[@otherType='appears_in']">
        <p>{mcri18n:translate('OMD.ir.docdetails.header.label.appears_in')}
        <xsl:variable name="title">
          <xsl:for-each select="./mods:titleInfo">
            {./mods:nonSort} {./mods:title}
            <xsl:if test="./mods:subTitle"> : {./mods:subTitle}</xsl:if>
          </xsl:for-each>
        </xsl:variable>
        {string-join(($title, 
                      ./mods:part/mods:detail[@type='article']/mods:number,
                      ./mods:originInfo[@eventType="publication"]/mods:publisher,
                      ./mods:originInfo[@eventType='publication']/mods:dateIssued[not(@*)]),
                      ', ')}
        </p> 
      </xsl:for-each>
    
      <!-- DOI / PURL -->
      <p>
        <xsl:choose>
          <xsl:when test="./mods:identifier[@type='doi']">
            <a href="https://doi.org/{./mods:identifier[@type='doi']}">https://doi.org/{./mods:identifier[@type='doi']}</a>
          </xsl:when>
          <xsl:when test="./mods:identifier[@type='purl']">
            <xsl:variable name="p" select="replace(./mods:identifier[@type='purl'], 'http://purl.uni-rostock.de', 'https://purl.uni-rostock.de')" />
            <a href="{$p}">{$p}</a>
          </xsl:when>
        </xsl:choose>
      </p>
      
      <!-- Abstract for EPUB -->
      <xsl:if test="not(contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest'))">
        <xsl:variable name="abstract">
        <xsl:choose>
          <xsl:when test="./mods:abstract[@xml:lang=$CurrentLang]">
            <xsl:value-of select="./mods:abstract[@xml:lang=$CurrentLang]" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="./mods:abstract[1]" />
          </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:for-each select="$abstract[string-length(.)>0]">
        <p class="text-justify small pt-2">
          <strong>Abstract:  </strong>
          <xsl:variable name="text" select="mcrstring:shorten(., 400)" />
          <xsl:choose>
            <xsl:when test="ends-with($text, '…')">
              {substring($text, 0, string-length($text))}
              <span class="collapse" id="spanCollapseAbstract">
                {substring(., string-length($text)+1)}
              </span>
              <button id="btnCollapseAbstract" class="btn btn-secondary btn-sm py-0 px-1" type="button"
                      data-bs-toggle="collapse" data-bs-target="#spanCollapseAbstract" aria-expanded="false" aria-controls="spanCollapseAbstract">
                <i class="fas fa-arrow-right"></i>
              </button>
              <script>
                <xsl:text expand-text="false" disable-output-escaping="true">
                  document.addEventListener("DOMContentLoaded", (event) => {
                    document.getElementById("spanCollapseAbstract").addEventListener("hidden.bs.collapse", event => {
                      document.getElementById("btnCollapseAbstract").innerHTML='&lt;i class="fas fa-arrow-right"&gt;&lt;/i&gt;';
                    });
                    document.getElementById("spanCollapseAbstract").addEventListener("shown.bs.collapse", event => {
                      document.getElementById("btnCollapseAbstract").innerHTML='&lt;i class="fas fa-arrow-left"&gt;&lt;/i&gt;';
                    });
                  });
                </xsl:text>
              </script>
            </xsl:when>
            <xsl:otherwise>
              {$text}
            </xsl:otherwise>
          </xsl:choose>
         </p>
      </xsl:for-each>
      </xsl:if>

      <!-- Badges -->
      <p class="mt-3">
      <xsl:if test="./mods:genre[@displayLabel='doctype']">
        <span class="badge ir-badge-header text-bg-secondary">
          <xsl:value-of select="mcrclass:current-label-text(mcrmods:to-category(./mods:genre[@displayLabel='doctype']))" />
        </span>
        <span>&#160;&#160;&#160;&#160;&#160;</span>
      </xsl:if>
      
      <xsl:call-template name="accessBadge" />
    
      <xsl:if test="./mods:classification[contains(@valueURI, 'licenseinfo#work')]">
        <span>&#160;&#160;&#160;&#160;&#160;</span>
        <xsl:variable name="licecat" select="mcrmods:to-category(./mods:classification[contains(@valueURI, 'licenseinfo#work')])" />

        <span id="badgeWorkLicense" class="badge ir-badge-header ir-badge-license p-0">
          <xsl:choose>
            <xsl:when test="$licecat/label[@xml:lang='x-icon']">
              <img src="{concat($WebApplicationBaseURL,'images',$licecat/label[@xml:lang='x-icon']/@text)}" />&#160;
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$licecat/label[@xml:lang=$CurrentLang]/@text" />
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </xsl:if>
    
      <xsl:if test="/mycoreobject/metadata/def.irControl/irControl/map/list[@key='mets_filegroups']/entry[text() = 'ALTO']">
        <span>&#160;&#160;&#160;&#160;&#160;</span>
        <span class="badge ir-badge-header ir-badge-ocr">
          {mcri18n:translate('OMD.ir.docdetails.header.label.ocr')}
        </span>
      </xsl:if>
      </p> 
 
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="accessBadge">
    <xsl:choose>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'restrictedaccess')]">
        <span class="badge ir-badge-header ir-badge-restrictedaccess">
          {mcri18n:translate('OMD.ir.docdetails.header.access.restricted')} &#160; <img style="height:20px;" src="{$WebApplicationBaseURL}images/logo_Closed_Access_black.png"/> &#160; {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:when>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'closedaccess')]">
        <span class="badge ir-badge-header ir-badge-closedaccess">
            {mcri18n:translate('OMD.ir.docdetails.header.access.closed')} <img style="height:20px;" src="{$WebApplicationBaseURL}images/logo_Closed_Access_black.png" /> &#160;  {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:when> 
      <xsl:otherwise>
        <span class="badge ir-badge-header ir-badge-openaccess" style="border:1px solid grey;">
          {mcri18n:translate('OMD.ir.docdetails.header.access.open')}&#160; <img style="height:20px;" src="{$WebApplicationBaseURL}images/logo_Open_Access_black.png" /> &#160; {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
