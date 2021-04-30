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

  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>
   
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/acl.xsl" />
  <xsl:import href="resource:xsl/functions/stringutils.xsl" />
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  
  <xsl:import href="resource:xsl/docdetails/header/header_names_html.xsl" />
  <xsl:import href="resource:xsl/docdetails/header/header_otherversions_html.xsl" />
  
  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template match="/mycoreobject">
    <!-- ID reservation header -->
    <xsl:if test="mcracl:check-permission(@ID, 'writedb')">
      <xsl:if test="./service/servstates/servstate[@categid='reserved']">
        <div class="card card-info border border-info mb-3">
          <div class="card-header bg-info">
            <h4 class="text-white">ID Reservierung</h4>
          </div>
          <div class="card-body">
            <xsl:for-each select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods">
              <xsl:if test="./mods:titleInfo/mods:title">
                <h2>
                  <xsl:value-of select="./mods:titleInfo/mods:title" />
                </h2>
              </xsl:if>
              <xsl:if test="./mods:note">
                <p class="card-text">
                  <xsl:value-of select="./mods:note" />
                </p>
              </xsl:if>
            </xsl:for-each>
          </div>
        </div>
      </xsl:if>
    </xsl:if>
  
    <!-- Metadata Header -->  
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">

      <!-- Button zum übergeordneten Werk -->
      <xsl:if test="./mods:relatedItem[@type='host' or @type='series'][./mods:recordInfo]"> 
        <div class="text-right">
          <xsl:for-each select="./mods:relatedItem[@type='host' or @type='series'][./mods:recordInfo]">
            <xsl:element name="a">
              <xsl:attribute name="class">btn btn-outline-secondary btn-sm</xsl:attribute>
              <xsl:if test="./mods:recordInfo/mods:recordIdentifier">
                <xsl:attribute name="href">{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(./mods:recordInfo/mods:recordIdentifier, '/', '%252F')}</xsl:attribute>
              </xsl:if>
              <xsl:attribute name="data-toggle">popover</xsl:attribute>
              <xsl:attribute name="data-placement">bottom</xsl:attribute>
              <xsl:attribute name="data-html">true</xsl:attribute>
              <xsl:attribute name="data-content">
                &lt;strong&gt;
                <xsl:choose>
                  <xsl:when test="./mods:titleInfo">
                    <xsl:variable name="title">
                      <xsl:for-each select="./mods:titleInfo">
                        {./mods:nonSort} {./mods:title}
                        <xsl:if test="./mods:subTitle"> : {./mods:subTitle}</xsl:if>
                      </xsl:for-each>
                    </xsl:variable>
                    {$title}
                  </xsl:when>
                </xsl:choose>
                &lt;/strong&gt;
              </xsl:attribute>
              <xsl:value-of select="mcri18n:translate(concat('OMD.ir.docdetails.parent.', ./@type))" />
              <xsl:text disable-output-escaping="true">&#38;nbsp;&#38;nbsp;</xsl:text><i class="fa fa-arrow-up"></i>
            </xsl:element>
          </xsl:for-each> 
        </div>
      </xsl:if>
  
      <xsl:call-template name="headerNames" />
  
      <!-- Title -->
      <xsl:for-each select="./mods:titleInfo[@usage='primary']">
        <xsl:variable name="title_primary">
          {./mods:nonSort} {./mods:title}
          <xsl:if test="./mods:subTitle"> : {./mods:subTitle}</xsl:if>
        </xsl:variable>
        <h2>
         <xsl:choose>
            <xsl:when test="./mods:partNumber or ./mods:partName">
              <small>{$title_primary}</small><br />
              <xsl:value-of select="string-join((./mods:partNumber, ./mods:partName), ' : ')" />
            </xsl:when>
            <xsl:otherwise>
               {$title_primary}
            </xsl:otherwise>
          </xsl:choose>
        </h2>
      </xsl:for-each>
    
      <!-- Veröffentlichungsangabe -->
      <xsl:for-each select="./mods:originInfo[@eventType='publication']">
        <p>
          <xsl:choose>
            <xsl:when test="contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest')">
              {string-join((./mods:edition, 
                            (string-join((./mods:place[not(@supplied='yes')]/mods:placeTerm, ./mods:publisher), ': ')),
                            ./mods:dateIssued[not(@*)]),  
                            ', ')}
            </xsl:when>
            <xsl:otherwise>
              {string-join((./mods:edition, ./mods:publisher, ./mods:dateIssued[not(@*)]), ', ')}
            </xsl:otherwise>
          </xsl:choose>
        </p>
      </xsl:for-each>
    
      <!-- erschienen in -->
      <xsl:for-each select="./mods:relatedItem[@otherType='appears_in']">
        <p>In:
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
            <a href="{./mods:identifier[@type='purl']}">{./mods:identifier[@type='purl']}</a>
          </xsl:when>
        </xsl:choose>
      </p>
      
      <!-- Abstract for EPUB -->
      <xsl:if test="not(contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest'))">
        <xsl:for-each select="./mods:abstract[@xml:lang=$CurrentLang]">
        <p class="text-justify"><small>
          <strong>Abstract:  </strong>
          <xsl:variable name="text" select="mcrstring:shorten(., 400)" />
          <xsl:choose>
            <xsl:when test="ends-with($text, '…')">
              {substring($text, 0, string-length($text))}
              <span class="collapse" id="spanCollapseAbstract">
                {substring(., string-length($text)+1)}
              </span>
              <button id="btnCollapseAbstract" class="btn btn-info btn-sm py-0 px-1" type="button" data-toggle="collapse" data-target="#spanCollapseAbstract" aria-expanded="false" aria-controls="spanCollapseAbstract">
                <i class="fas fa-arrow-right"></i>
              </button>
              <script>
                <xsl:text expand-text="false" disable-output-escaping="true">
                  $('#spanCollapseAbstract').on('hidden.bs.collapse', function () {
                    $('#btnCollapseAbstract').empty().append('&lt;i class="fas fa-arrow-right"&gt;&lt;/i&gt;');
                  });
                  $('#spanCollapseAbstract').on('shown.bs.collapse', function () {
                    $('#btnCollapseAbstract').empty().append('&lt;i class="fas fa-arrow-left"&gt;&lt;/i&gt;');
                  });
                </xsl:text>
              </script>
            </xsl:when>
            <xsl:otherwise>
              {$text}
            </xsl:otherwise>
          </xsl:choose>
        </small></p>
      </xsl:for-each>
      </xsl:if>
    
      <!-- Badges -->
      <xsl:if test="./mods:genre[@displayLabel='doctype']">
        <span class="badge ir-badge-header badge-secondary">
          <xsl:value-of select="mcrclass:current-label-text(mcrmods:to-category(./mods:genre[@displayLabel='doctype']))" />
        </span>
        <span>&#160;&#160;</span> 
      </xsl:if>
      
      <xsl:call-template name="accessBadge" />
    
      <xsl:if test="./mods:classification[contains(@valueURI, 'licenseinfo#work')]">
        <span>&#160;&#160;</span>
        <xsl:variable name="licecat" select="mcrmods:to-category(./mods:classification[contains(@valueURI, 'licenseinfo#work')])" />
        <span id="badgeWorkLicense" class="badge ir-badge-header p-0" data-toggle="popover" data-placement="bottom" data-html="true">
          <xsl:attribute name="data-content"><xsl:value-of select="$licecat/label[@xml:lang=$CurrentLang]/@description" /></xsl:attribute>
          <xsl:choose>
            <xsl:when test="$licecat/label[@xml:lang='x-icon']">
              <img style="height:100%" src="{concat($WebApplicationBaseURL,'images',$licecat/label[@xml:lang='x-icon']/@text)}" />&#160;
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$licecat/label[@xml:lang=$CurrentLang]/@text" />
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </xsl:if>
    
      <xsl:if test="/mycoreobject/metadata/def.irControl/irControl/map/list[@key='mets_filegroups']/entry[text() = 'ALTO'] 
                    or /mycoreobject/structure/derobjects/derobject/classification[@categid='fulltext']">
        <span>&#160;&#160;</span>
        <span class="badge ir-badge-header ir-badge-ocr">
          OCR
        </span>
      </xsl:if> 

      <!-- weitere Versionen -->
      <xsl:call-template name="otherVersions" />
       
      <!-- popover javascript -->
      <script>
        <xsl:text expand-text="false" disable-output-escaping="true">
          $(function () {
            $('[data-toggle="popover"]')
               .popover(
                 { delay: { "show": 50, "hide": 2500 }, 
                   trigger:"click hover",
                   sanitize:false,
                   content: function(){
                     var ref = $(this).attr('data-content-ref');
                     return $(ref).children().html();
                   }
                 })
                 .on('shown.bs.popover', function () {
                 var $popup = $(this);
                 if($popup.is('[data-content-ref]')){
                   $(document).on("click", $popup.attr('data-content-ref').replace('#', '#close_'), 
                     function(){
                       $popup.popover('hide');
                   });
                 }
              });
          });
        </xsl:text>
      </script>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="accessBadge">
    <xsl:choose>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'restrictedaccess')]">
        <span class="badge ir-badge-header ir-badge-restrictedaccess">
          Beschränkter <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Closed_Access.png"/>  Zugang
        </span>
      </xsl:when>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'closedaccess')]">
        <span class="badge ir-badge-header ir-badge-closedaccess">
            Kein <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Closed_Access.png" />  Zugang
        </span>
      </xsl:when> 
      <xsl:otherwise>
        <span class="badge ir-badge-header ir-badge-openaccess">
          Freier <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Open_Access.png" /> Zugang
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
