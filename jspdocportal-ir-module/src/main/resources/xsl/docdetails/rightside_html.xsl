<?xml version="1.0"?>
<xsl:stylesheet version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:json="http://www.w3.org/2005/xpath-functions"
  
  exclude-result-prefixes="mods xlink"
  expand-text="yes">

  <xsl:param name="WebApplicationBaseURL" select="'http://rosdok.uni-rostock.de/'"/>
  <xsl:param name="WebApplicationTitle" select="'RosDok'"/>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />
  
  <xsl:output method="html" indent="yes" standalone="no" />
  
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/stringutils.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  <xsl:import href="resource:xsl/functions/acl.xsl" />
    
  <xsl:template match="/">
    <!-- Provider -->
    <xsl:if test="contains(/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI, '/doctype#histbest')">
    <div class="card border border-primary mb-3">
      <div class="card-body py-1 small">
        bereitgestellt durch:
      </div>
      <div class="card-body text-center py-2">
        <xsl:variable name="class_provider" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='provider']" />
        <xsl:variable name="categ">
          <xsl:choose>
            <xsl:when test="$class_provider">
              <xsl:copy-of select="mcrmods:to-mycoreclass($class_provider, 'single')/categories/category" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="isil" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier/@source" />
              <xsl:copy-of select="document('classification:metadata:-1:children:provider')//category[label[@xml:lang='x-isil']/@text=$isil]" />                 
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable> 
        <xsl:variable name="homepage" select="$categ/category/label[@xml:lang='x-homepage']/@text" />
        <a href="{$homepage}">
          <xsl:variable name="json_viewer" select="replace($categ/category/label[@xml:lang='x-dfg-viewer']/@text, '''', '&quot;')" />
          <xsl:variable name="logo_url" select="replace(json-to-xml($json_viewer)/json:map/json:string[@key='logo_url'], 'http://rosdok.uni-rostock.de/',$WebApplicationBaseURL)" />
          <xsl:if test="$logo_url">
            <img src="{$logo_url}"/>
            <br />
          </xsl:if>
          <span class="small"><xsl:value-of select="$categ/category/label[@xml:lang=$CurrentLang]/@text" /></span>
        </a>
      </div>
    </div>
    </xsl:if>
  
    <!-- Cover -->
    <xsl:variable name="recordID" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" />
    <xsl:variable name="showViewer" select="exists(/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS' or @categid='fulltext']])" />
    <xsl:variable name="image">    
      <xsl:choose>
        <xsl:when test="/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover' or @categid='fulltext']]">
          <img style="width:150px" class="border border-secondary" src="{$WebApplicationBaseURL}api/iiif/image/v2/thumbnail/{/mycoreobject/@ID}/full/full/0/default.jpg" />
        </xsl:when>
        <xsl:when test="/mycoreobject[contains(@ID, '_bundle_')]">
          <img style="width:150px" src="{$WebApplicationBaseURL}images/filetypeicons/bundle.png" />
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI, '#data')">
           <img style="width:150px" src="{$WebApplicationBaseURL}images/filetypeicons/data.png" />
        </xsl:when>
        <xsl:otherwise>
			<img style="width:150px" src="{$WebApplicationBaseURL}images/filetypeicons/document.png" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
   
    <xsl:variable name="derid" select="/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS' or @categid='fulltext']]/@xlink:href" />
    <xsl:variable name="access" select="mcracl:check-permission($derid, 'read')" />
    
    <xsl:choose>
      <xsl:when test="$showViewer and $access">
        <div class="ir-box ir-box-docdetails-image text-center" style="position:relative">
    	  <a id="ir-thumbnail-image-parent" href="{$WebApplicationBaseURL}mcrviewer/recordIdentifier/{replace($recordID,'/','_')}" 
             style="display:inline-block;min-height:2em" title="Im MyCoRe Viewer anzeigen"></a>
             <div class="text-center" style="position:absolute;top:.5em;right:0;left:0;">
            <a class="btn btn-light btn-sm border border-secondary" href="{$WebApplicationBaseURL}mcrviewer/recordIdentifier/{replace($recordID, '/','_')}" title="Im MyCoRe Viewer anzeigen">
              <i class="far fa-eye text-primary"></i> Anzeigen
            </a>
          </div>
          <xsl:if test="/mycoreobject/metadata/def.irControl/irControl/map/list[@key='mets_filegroups']/entry[text() = 'ALTO'] 
                       or /mycoreobject/structure/derobjects/derobject/classification[@categid='fulltext']">
            <div class="input-group input-group-sm mt-3 px-3">
              <input id="input_search_in_doc" type="text" class="form-control" onKeyDown="searchInDoc(event)" placeholder="Im Dokument suchen..." aria-describedby="search-in-document-addon" />
              <div class="input-group-append">
                <button type="button" class="btn btn-secondary" id="search-in-document-addon" onclick="searchInDoc(event)" title="Im Dokument suchen"><i class="fa fa-search"></i></button>
              </div>
              <script>
                function searchInDoc(e) {{
                  if(e.target.type == "button"  || (e.target.type == "text" &amp;&amp; e.keyCode === 13)) {{
                    window.open("{$WebApplicationBaseURL}mcrviewer/recordIdentifier/{replace($recordID,'/','_')}?q="+document.getElementById("input_search_in_doc").value,"_self");
                  }}
               }}
              </script>
            </div>
          </xsl:if>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div id="ir-thumbnail-image-parent" class="ir-box ir-box-docdetails-image" style="position:relative">
        </div>
      </xsl:otherwise>
   </xsl:choose>
   <!-- curly bracets are quoted!!! -->
   <script>
     var image = new Image();
     image.onload = function() {{
       image.style.width = "150px";
       image.classList.add("border");
       image.classList.add("border-secondary");
       document.getElementById("ir-thumbnail-image-parent").appendChild(image);
     }}
     image.onerror = function() {{
       // image did not load - show default image
       var err = new Image();
       err.style.width = "150px";
       err.src = "{$WebApplicationBaseURL}images/filetypeicons/empty.png";
       document.getElementById("ir-thumbnail-image-parent").appendChild(err);
     }}
     image.src = "{$image/img/@src}";
   </script>
 
   <!-- Dauerhaft zitieren -->
     <xsl:if test="not(/mycoreobject/service/servstates/servstate/@categid='deleted')">
      <div class="ir-box ir-box-emph">
        <h4 class="text-primary">Dauerhaft zitieren</h4>
        <xsl:choose>
          <xsl:when test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
            <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
              <p><a href="https://doi.org/{.}">https://doi.org/<br class="d-xl-none"/>{.}</a></p>
            </xsl:for-each>
          </xsl:when>
          <xsl:when test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='purl']">
            <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='purl']">
              <p class="d-none d-xl-block"><a href="{.}" style="transform: scaleX(96%) translate(-2%);display: inline-block;white-space: nowrap">
                {substring-before(.,'.de/')}.de/<br class="d-md-none"/>{substring-after(.,'.de/')}
              </a></p>
              <p class="d-xl-none"><a href="{.}">
                {substring-before(.,'.de/')}.de/<br />{substring-after(.,'.de/')}
              </a></p>
            </xsl:for-each>
          </xsl:when>
        </xsl:choose>
      </div>
    </xsl:if>
    
    <xsl:if test="$access">
      <div style="mb-3">
       <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or contains(@categid, 'METS')]]">
         <div class="dropdown w-100 mt-3">
            <button class="btn btn-primary dropdown-toggle w-100" type="button" id="dropdownMenuShow" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
               <i class="far fa-eye pr-2"></i> Anzeigen
            </button>
            
            <div class="dropdown-menu w-100" aria-labelledby="dropdownMenuShow">
              <!-- RosDok-Viewer -->

              <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
              <div class="dropdown-divider"></div>
              <xsl:variable name="url">{$WebApplicationBaseURL}mcrviewer/recordIdentifier/{replace(/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier,'/','_')}</xsl:variable>
              <xsl:variable name="startpagePath" select="if (//def.irControl/irControl/map[@key='ROOT']/entry[@key='start_image']) then (concat('/iview2/',//def.irControl/irControl/map[@key='ROOT']/entry[@key='start_image'],'.iview2')) else ()" />
              <a class="dropdown-item" href="{$url}{$startpagePath}">
                <img src="{$WebApplicationBaseURL}/themes/rosdok/images/rosdok_logo2.png" style="height:1.5em;padding-right:0.5em" />Viewer
              </a>
              </xsl:if>

              <!-- DFG-Viewer -->
              <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
              <div class="dropdown-divider"></div>
              <a class="dropdown-item background-primary" href="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}/dfgviewer">
               <img src="{$WebApplicationBaseURL}images/dfgviewerLogo_blue.svg"  title="{mcri18n:translate('Webpage.docdetails.dfgviewer')}" style="height:1.5em;color:black"/>
              </a>
              </xsl:if>
            </div>
         </div>
       </xsl:if>
         <div class="dropdown w-100 mt-3">
            <button class="btn btn-secondary dropdown-toggle w-100" type="button" id="dropdownMenuDownload" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
               <i class="fas fa-download pr-2"></i> Herunterladen
            </button>
            <div class="dropdown-menu" aria-labelledby="dropdownMenuDownload">
              <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]">
                <div class="dropdown-divider"></div>
                <xsl:call-template name="download-entry" />
              </xsl:for-each>
              <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='data' or @categid='documentation' or @categid='supplement']]">
                <xsl:variable name="derid" select="./@xlink:href" />
                <div class="dropdown-divider"></div>
                <xsl:call-template name="download-entry" />
              </xsl:for-each>
              <xsl:if test="/mycoreobject[not(contains(@ID,'_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
                <xsl:variable name="recordID" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" />
                <xsl:if test="$recordID">
                  <div class="dropdown-divider"></div>
                  <div class="dropdown-item px-2" >
                    <div style="width:350px">
                      <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" title="{mcri18n:translate('Webpage.docdetails.pdfdownload')}" style="height:1.5em;padding-right:0.5em;" />
                      <a href="{$WebApplicationBaseURL}do/pdfdownload/recordIdentifier/{replace($recordID, '/','_')}" target="_blank">
                        <strong>{mcri18n:translate('Webpage.docdetails.pdfdownload')}</strong>
                      </a>
                    </div>
                  </div>
                </xsl:if>
              </xsl:if>
             
               <!-- called by: rosdok_document_0000015736 ->
      NEU + ÜBERARBEITEN: aus 2. MODS-Sektion auslesen ... 
      <xsl:for-each select="/mycoreobject/service/servflags/servflag[@type='external-content']">
        <c:set var="theXML"><xsl:out select="./text()" escapeXml="false" /></c:set>
            <xsl:parse var="theFileDoc" xml="${theXML}" />
            <xsl:set var="theFile" select="$theFileDoc/file" />
            <a class="btn btn-default ir-button ir-button-download" style="text-align:left" title="MD5: <xsl:out select="$theFile/@MD5" />" 
               href="<xsl:out select="$theFile/@URL" />" target="_blank">
              <xsl:choose>
                <xsl:when test="contains($theFile/@URL, '.zip')">
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="{$WebApplicationBaseURL}images/download_zip.png" />  
                </xsl:when>
                <xsl:when test="contains($theFile/@URL, '.pdf')">
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="{$WebApplicationBaseURL}images/download_pdf.png" />  
                </xsl:when>
                <xsl:otherwise>
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="{$WebApplicationBaseURL}images/download_other.png" />
                </xsl:otherwise>
              </xsl:choose>
              <c:set var="mesKey">OMD.derivatedisplay.<xsl:out select="$theFile/@USE"/></c:set>
              <strong>mcri18n:translate('${mesKey}" /></strong><br />
              <span style="font-size: 85%">
                <xsl:out select="$theFile/@OWNERID" />&nbsp;&nbsp;&nbsp;(<xsl:out select="round($theFile/@SIZE div 1024 div 1024 * 10) div 10" /> MB)<br />
              </span>
            </a>
          </xsl:for-each>
          -->
              
              
             </div>
           </div>
         </div>
    </xsl:if>
       
    <xsl:if test="not(/mycoreobject/service/servstates/servstate/@categid='deleted')">
         <div class="ir-box mt-3">
           <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']">
             <xsl:variable name="class_provider" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='provider']" />
             <xsl:variable name="catalogs">
               <xsl:choose>
                 <xsl:when test="$class_provider">
                   <xsl:value-of select="mcrmods:to-mycoreclass($class_provider, 'single')/categories/category/label[@xml:lang='x-catalog']/@text" />
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:variable name="isil" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier/@source" />
                   <xsl:value-of select="document('classification:metadata:-1:children:provider')//category[label[@xml:lang='x-isil']/@text=$isil]/label[@xml:lang='x-catalog']/@text" />                 
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:variable> 
           
             <h4>Export</h4>
             <p>
               <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']">
                 <xsl:if test="$catalogs">
                   <xsl:call-template name="biblio-formate">
                     <xsl:with-param name="catalogs" select="$catalogs" />
                     <xsl:with-param name="ppn" select="." />
                   </xsl:call-template>
                 </xsl:if>
               
              </xsl:for-each>
            </p>
            
            <h4>Portale</h4>
            <p>
              <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']">
                <xsl:variable name="json_urls" select="replace($catalogs, '''', '&quot;')" />
                <xsl:variable name="opac_url" select="json-to-xml($json_urls)/json:map/json:string[@key='opac']" />
                <a class="badge px-1" target="_blank" href="{replace($opac_url, '\{0\}',.)}">OPAC</a>
                <a class="badge px-1" href="https://gso.gbv.de/DB=2.1/PPNSET?PPN={.}">GVK</a>
              </xsl:for-each>              
              <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd16']">
                <a class="badge px-1" target="_blank" href="http://gateway-bayern.de/VD16+{replace(.,' ','+')}">VD16</a>
                </xsl:for-each>
                <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd17']">
                  <a class="badge px-1" target="_blank" href="https://kxp.k10plus.de/DB=1.28/CMD?ACT=SRCHA&amp;IKT=8079&amp;TRM=%27{.}%27">VD17</a>
                </xsl:for-each>
                <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd18']">
                  <a class="badge px-1" target="_blank" href="https://kxp.k10plus.de/DB=1.65/CMD?ACT=SRCHA&amp;IKT=8080&amp;TRM=VD18{replace(.,' ','+')}&amp;ADI_MAT=B&amp;MATCFILTER=Y&amp;MATCSET=Y">VD18</a>
                </xsl:for-each>
                <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='zdb']">
                  <a class="badge px-1" target="_blank" href="https://zdb-katalog.de/list.xhtml?key=cql&amp;t={.}">ZDB</a>
                </xsl:for-each>
                <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='kalliope']">
                  <a class="badge px-1" target="_blank" href="http://kalliope-verbund.info/{.}">Kalliope-Verbundkatalog</a>
                </xsl:for-each>
                <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
                  <a class="badge px-1" target="_blank" href="https://search.datacite.org/works/{.}">DataCite Search</a>
                </xsl:for-each>  
              </p>
           </xsl:if>
           <h4>Teilen</h4>
           <div class="shariff" data-url="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}"
             data-services="[&quot;twitter&quot;, &quot;facebook&quot;, &quot;linkedin&quot;, &quot;xing&quot;, &quot;whatsapp&quot;, &quot;telegram&quot;, &quot;mail&quot;, &quot;info&quot;]"
             data-mail-url="mailto:" data-mail-subject="{mcri18n:translate('OMD.ir.shariff.subject')}" data-mail-body="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}"
             data-orientation="horizontal" data-theme="white">
           </div> <!-- data-theme=standard|grey|white --> 
           <script src="{$WebApplicationBaseURL}modules/shariff_3.2.1/shariff.min.js"></script>
           <p></p>
         </div>
    </xsl:if>
        
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='licenseinfo'][contains(@valueURI, 'licenseinfo#work')]">
         <div class="ir-box">
           <h4>Rechte</h4>
           <xsl:variable name="categ" select="mcrmods:to-mycoreclass(., 'single')/categories/category" />
           <span class="clearfix">
             <img src="{$WebApplicationBaseURL}images{$categ/label[@xml:lang='x-icon']/@text}" class="float-left pr-3" />
             <a href="{$categ/label[@xml:lang='x-uri']/@text}" class="strong">
               <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@text"/>
             </a>  
           </span>
           <p class="text-justify form-text text-muted small">
             <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@description" disable-output-escaping="true" />
           </p>
         </div>
    </xsl:for-each>

    <!--Tools -->
    <div class="my-3"> 
          <div class="float-right">
            <button type="button" class="btn btn-sm ir-button-tools hidden-xs" data-toggle="collapse" data-target="#hiddenTools"
                    title="{mcri18n:translate('Webpage.tools.menu4experts')}">
              <i class="fa fa-cog"></i>
            </button>
          </div>
          <div id="hiddenTools" class="collapse">
            <div style="padding-bottom:6px">
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showXML')}"
                   href="{$WebApplicationBaseURL}api/v1/objects/{/mycoreobject/@ID}" rel="nofollow">XML</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showSOLR')}"
                  href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.Style=solrdocument-3" rel="nofollow">SOLR in</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showSOLR')}"
                  href="{$WebApplicationBaseURL}api/v1/search?q=id:{/mycoreobject/@ID}" rel="nofollow">SOLR doc</a>
              <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]">
                <xsl:variable name="derid" select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]/@xlink:href" />
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showREPOS_METS')}" 
                   href="{$WebApplicationBaseURL}api/v1/objects/${it.id}/derivates/{$derid}/open">METS</a>
              </xsl:if>
              <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[contains(@valueURI, '#epub') or contains(@valueURI, '#data')]">
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.Transformer=rosdok_datacite" rel="nofollow">Datacite</a>
              </xsl:if>
              <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="{$WebApplicationBaseURL}oai?verb=GetRecord&amp;metadataPrefix=oai_dc&amp;identifier=oai:oai.rosdok.uni-rostock.de:{/mycoreobject/@ID}" rel="nofollow">OAI</a>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="{$WebApplicationBaseURL}oai/dnb-urn?verb=GetRecord&amp;metadataPrefix=epicur&amp;identifier=oai:oai-dnb-urn.rosdok.uni-rostock.de:{/mycoreobject/@ID}" rel="nofollow">OAI:DNB_URN</a>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="{$WebApplicationBaseURL}oai/dnb-epflicht?verb=GetRecord&amp;metadataPrefix=xMetaDissPlus&amp;identifier=oai:oai-dnb-epflicht.rosdok.uni-rostock.de:{/mycoreobject/@ID}" rel="nofollow">OAI:DNB_EPFLICHT</a>
              </xsl:if>
            </div>
          </div>
    </div>  
  </xsl:template>
  
  <xsl:template name="download-entry">
    <xsl:variable name="mcrid" select="/mycoreobject/@ID" />
    <xsl:variable name="derid" select="./@xlink:href" />
    <xsl:variable name="fulltext_url">{$WebApplicationBaseURL}file/{$mcrid}/{$derid}/{./maindoc/text()}</xsl:variable>
    <div class="dropdown-item px-2 d-inline-block" onclick="location.href='{$fulltext_url}'">
      <xsl:choose>
        <xsl:when test="ends-with(./maindoc, '.zip')">
          <img align="left" src="{$WebApplicationBaseURL}images/download_zip.png" title="{mcri18n:translate('Webpage.docdetails.zipdownload')}" style="height:1.5em;padding-right:0.5em"/>
        </xsl:when>
        <xsl:when test="ends-with(./maindoc, '.pdf')">
          <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" title="{mcri18n:translate('Webpage.docdetails.pdfdownload')}" style="height:1.5em;padding-right:0.5em;margin-bottom:2em" />
        </xsl:when>
        <xsl:otherwise>
          <img align="left" src="{$WebApplicationBaseURL}images/download_other.png" title="{mcri18n:translate('Webpage.docdetails.otherdownload')}" />
        </xsl:otherwise>
      </xsl:choose>
      <strong>{mcrclass:current-label-text(./classification[@classid='derivate_types'])}</strong>

      <span class="small pl-2">({mcrstring:pretty-filesize(./maindoc_size)})</span> 
      <a class="float-right py-1 small" download="{./maindoc}.md5" onclick="event.stopPropagation();"
         href="data:text/plain;charset=US-ASCII,{encode-for-uri(concat(./maindoc_md5,'  ', ./maindoc))}">
        <i class="fas fa-download"></i> MD5
      </a>
      <br />
      <a href="{$fulltext_url}" target="_blank">
        <span class="small">{mcrstring:abbreviate-center(./maindoc, 40)}</span>
      </a>
    </div>
  </xsl:template>
  
  <xsl:template name="biblio-formate">
    <xsl:param name="catalogs" />
    <xsl:param name="ppn" />
    
    <xsl:variable name="json_urls" select="replace($catalogs, '''', '&quot;')" />
    <xsl:variable name="unapi_url" select="json-to-xml($json_urls)/json:map/json:string[@key='unapi']" />
    <a class="badge px-1" target="_blank" href="{replace(replace($unapi_url, '\{0\}', $ppn), 'picaxml', 'bibtex')}">BibTeX</a>
    <a class="badge px-1" target="_blank" href="{replace(replace($unapi_url, '\{0\}', $ppn), 'picaxml', 'endnote')}">EndNote</a>
    <a class="badge px-1" target="_blank" href="{replace(replace($unapi_url, '\{0\}', $ppn), 'picaxml', 'ris')}">RIS</a>
    <a class="badge px-1" target="_blank" href="{replace(replace($unapi_url, '\{0\}', $ppn), 'picaxml', 'dc')}">DublinCore</a>
    <a class="badge px-1" target="_blank" href="{replace(replace($unapi_url, '\{0\}', $ppn), 'picaxml', 'mods')}">MODS</a>
  </xsl:template>
  
</xsl:stylesheet>