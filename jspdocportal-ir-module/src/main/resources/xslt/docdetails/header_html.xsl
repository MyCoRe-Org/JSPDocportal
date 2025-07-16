<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="fn mods xlink mcri18n mcracl mcrstring mcrclass mcrmods"
  expand-text="yes">

  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>
   
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
  <xsl:param name="MCR.DOI.Prefix" />
  <xsl:param name="MCR.Identifier.PURL.BaseURL"></xsl:param>

  <xsl:template match="/mycoreobject">
    <!-- ID reservation header -->
    <xsl:if test="mcracl:check-permission(@ID, 'writedb')">
      <xsl:if test="./service/servstates/servstate[@categid='reserved']">
        <div class="card card-info border border-info mb-3">
          <div class="card-header bg-info py-2">
             <h4 class="text-white my-0">
               {mcri18n:translate('OMD.ir.docdetails.header.title.reserved')}:
             </h4>
          </div>
          <div class="card-body">
            <xsl:for-each select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods">
              <xsl:if test="./mods:note[@type='provisional_title']">
                <h2>
                  <xsl:value-of select="./mods:note[@type='provisional_title']" />
                </h2>
              </xsl:if>
              <xsl:if test="./mods:note[@type='provisional_remarks']">
                <pre>
                  <xsl:value-of select="./mods:note[@type='provisional_remarks']" />
                </pre>
              </xsl:if>
            </xsl:for-each>
          </div>
          <div class="card-footer">
            <!-- TODO use real DOI from mods:identifier after the PI-component works as expected and show only if DOI is present -->
            <span class="d-inline-block" style="width:4em">URN: </span>
            <input id="input_urn" readonly="readonly" style="width:25em;margin-bottom:5px" value="{./metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods/mods:identifier[@type='urn']}" />
            <button class="btn btn-sm ml-1" title="URN kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_urn').value);"><i class="fas fa-clipboard"></i></button>
            <br />
            <span class="d-inline-block" style="width:4em">DOI: </span>
            <input id="input_doi" readonly="readonly" style="width:25em;margin-bottom:5px" value="https://doi.org/{$MCR.DOI.Prefix}/{replace(./metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods/mods:recordInfo/mods:recordIdentifier, '/','_')}" />
            <button class="btn btn-sm ml-1" title="DOI kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_doi').value);"><i class="fas fa-clipboard"></i></button>
            <br />
            <span class="d-inline-block" style="width:4em">PURL: </span>
            <input id="input_purl" readonly="readonly" style="width:25em" value="{$MCR.Identifier.PURL.BaseURL}{./metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods/mods:recordInfo/mods:recordIdentifier}" />
            <button class="btn btn-sm ml-1" title="PURL kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_purl').value);"><i class="fas fa-clipboard"></i></button>
          </div>
        </div>
      </xsl:if>
    </xsl:if>
  
    <!-- Metadata Header -->  
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
      <div>
      <!-- Button zum übergeordneten Werk -->
      <xsl:if test="./mods:relatedItem[@type='host' or @type='series'][./mods:recordInfo/mods:recordIdentifier]"> 
        <div class="text-right">
          <xsl:for-each select="./mods:relatedItem[@type='host' or @type='series'][./mods:recordInfo/mods:recordIdentifier]">
            <xsl:element name="a">
              <xsl:attribute name="class">ir-btn-goto-top btn btn-outline-secondary btn-sm ml-2</xsl:attribute>
              <xsl:attribute name="href">{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(./mods:recordInfo/mods:recordIdentifier, '/', '%252F')}</xsl:attribute>
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
      <xsl:for-each select="./mods:originInfo[@eventType='publication' or @eventType='production']">
        <p>
          <xsl:choose>
            <xsl:when test="contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest')">
              <xsl:variable name="publisherAndPlace">
              
              </xsl:variable>
              <xsl:variable name="publisherWithoutPlace">
              </xsl:variable>
              
              <xsl:variable name="placeWithoutPublisher">
              
              </xsl:variable>
              
              <xsl:variable name="publisherPlace">
                <xsl:choose>
                  <xsl:when test="mods:publisher">
                    {string-join(./mods:place[not(@supplied='yes')][not(following-sibling::mods:publisher)]/mods:placeTerm,', ')}
                    <xsl:for-each select="mods:publisher">
                      <xsl:variable name="thePublisher" select="." />
                      {string-join(./following-sibling::mods:place[not(@supplied='yes')][preceding-sibling::mods:publisher[1]=$thePublisher]/mods:placeTerm,', ')}: {.}{if (not(../mods:publisher[last()]=$thePublisher)) then ', ' else ()}
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    {string-join(./mods:place[not(@supplied='yes')]/mods:placeTerm,', ')}
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
                {if (normalize-space($publisherPlace)='') then string-join((./mods:edition, ./mods:dateIssued[not(@*)], ./mods:dateCreated[not(@*)]), ', ') else string-join((./mods:edition, $publisherPlace, ./mods:dateIssued[not(@*)], ./mods:dateCreated[not(@*)]), ', ')}
            </xsl:when>
            <xsl:otherwise>
              {string-join((./mods:edition, ./mods:publisher, ./mods:dateIssued[not(@*)], ./mods:dateCreated[not(@*)]), ', ')}
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
                      ./mods:originInfo[@eventType='publication' or @eventType='production']/mods:publisher,
                      ./mods:originInfo[@eventType='publication']/mods:dateIssued[not(@*)],
                      ./mods:originInfo[@eventType='production']/mods:dateCreated[not(@*)]),
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
              <button id="btnCollapseAbstract" class="btn btn-secondary btn-sm py-0 px-1" type="button" data-toggle="collapse" data-target="#spanCollapseAbstract" aria-expanded="false" aria-controls="spanCollapseAbstract">
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
         </p>
      </xsl:for-each>
      </xsl:if>
      <xsl:call-template name="preceding_succeeding_buttons" />
            <!-- weitere Versionen -->
      <xsl:call-template name="otherVersions" />
        
      <!-- Badges -->
      <xsl:if test="./mods:classification[contains(@valueURI, 'licenseinfo#work')]">
        <xsl:variable name="licecat" select="mcrmods:to-category(./mods:classification[contains(@valueURI, 'licenseinfo#work')])" />
        <div id="popover_content_{generate-id(./mods:classification[contains(@valueURI, 'licenseinfo#work')])}" style="display: none">
        <div style="min-width:100em">
            <button type="button" id="close_popover_content_{generate-id(./mods:classification[contains(@valueURI, 'licenseinfo#work')])}" class="close float-right" aria-label="Close">
              <span aria-hidden="true">&#215;</span>
            </button>
            <xsl:value-of select="$licecat/label[@xml:lang=$CurrentLang]/@description" disable-output-escaping="true" />
            </div>
        </div>
      </xsl:if> 
      
      <p class="mt-3">
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

        <span id="badgeWorkLicense" class="badge ir-badge-header ir-badge-license p-0" data-toggle="popover" data-placement="bottom" data-html="true"
              data-content-ref="#popover_content_{generate-id(./mods:classification[contains(@valueURI, 'licenseinfo#work')])}">
          <xsl:choose>
            <xsl:when test="fn:ends-with($licecat/label[@xml:lang='x-icon']/@text, '.png')">
              <img src="{concat($WebApplicationBaseURL,'images',$licecat/label[@xml:lang='x-icon']/@text)}" />&#160;
            </xsl:when>
            <xsl:when test="fn:ends-with($licecat/label[@xml:lang='x-icon']/@text, '.svg')">
              <img class="p-1 bg-dark" src="{concat($WebApplicationBaseURL,'images',$licecat/label[@xml:lang='x-icon']/@text)}" />&#160;
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$licecat/label[@xml:lang=$CurrentLang]/@text" />
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </xsl:if>
    
      <xsl:if test="/mycoreobject/metadata/def.irControl/irControl/map/list[@key='mets_filegroups']/entry[text() = 'ALTO']">
        <span>&#160;&#160;</span>
        <span class="badge ir-badge-header ir-badge-ocr">
          {mcri18n:translate('OMD.ir.docdetails.header.label.ocr')}
        </span>
      </xsl:if>
      </p> 

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
      </div>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="accessBadge">
    <xsl:choose>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'restrictedaccess')]">
        <span class="badge ir-badge-header ir-badge-restrictedaccess">
          {mcri18n:translate('OMD.ir.docdetails.header.access.restricted')} <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Closed_Access.png"/>  {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:when>
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'closedaccess')]">
        <span class="badge ir-badge-header ir-badge-closedaccess">
            {mcri18n:translate('OMD.ir.docdetails.header.access.closed')} <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Closed_Access.png" />  {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:when> 
      <xsl:when test="./mods:classification[@displayLabel='accesscondition'][contains(@valueURI, 'openaccess')]">
        <span class="badge ir-badge-header ir-badge-openaccess">
          {mcri18n:translate('OMD.ir.docdetails.header.access.open')} <img style="height:1.5em;padding:0 .25em" src="{$WebApplicationBaseURL}images/logo_Open_Access.png" /> {mcri18n:translate('OMD.ir.docdetails.header.access')}
        </span>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
