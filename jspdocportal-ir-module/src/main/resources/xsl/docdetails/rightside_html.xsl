<?xml version="1.0"?>
<xsl:stylesheet version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
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
  
  
  <xsl:template match="/">
    <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover']] 
                       or contains(/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
      <xsl:choose>
        <xsl:when test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover']]">
          <div class="ir-box ir-box-docdetails-image">
            <xsl:choose>
              <xsl:when test="/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]">
                <xsl:variable name="recordID" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" />
                <a href="{$WebApplicationBaseURL}mcrviewer/recordIdentifier/{replace($recordID,'/','_')}" title="Im MyCoRe Viewer anzeigen">
                  <img src="{$WebApplicationBaseURL}api/iiif/image/v2/thumbnail/{/mycoreobject/@ID}/full/full/0/default.jpg" style="width:200px" />
                </a>
              </xsl:when>
              <xsl:when test="/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
                <xsl:variable name="mcrid" select="/mycoreobject/@ID" />
                <a href="{$WebApplicationBaseURL}resolve/id/{$mcrid}/dfgviewer" target="_blank" title="Im DFG Viewer anzeigen">
                  <img src="{$WebApplicationBaseURL}api/iiif/image/v2/thumbnail/{/mycoreobject/@ID}/full/full/0/default.jpg" style="width:200px" />
                </a>
              </xsl:when>
              <xsl:otherwise>
                <img src="{$WebApplicationBaseURL}api/iiif/image/v2/thumbnail/{/mycoreobject/@ID}/full/full/0/default.jpg" style="width:200px" />
              </xsl:otherwise>
            </xsl:choose>
          </div>
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
          <div class="ir-box ir-box-docdetails-image">
            <img src="{$WebApplicationBaseURL}images/filetypeicons/data.png" alt="cover image for resarch data" />
          </div>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="not(/mycoreobject/service/servstates/servstate/@categid='deleted')">
      <div class="ir-box ir-box-emph">
        <h4 class="text-primary">Dauerhaft zitieren</h4>
        <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
          <p><a href="https://doi.org/{.}">https://doi.org/<br class="visible-md-inline"/>{.}</a></p>
        </xsl:for-each>
        <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']">
          <p><a href="http://nbn-resolving.org/{.}">{.}</a></p>
        </xsl:for-each>
        <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='purl']">
          <p><a href="{.}">
            {substring-before(.,'.de/')}.de/
              <br class="visible-md-inline"/>
            {substring-after(.,'.de/')}
          </a></p>
        </xsl:for-each>
      </div>
    </xsl:if>
    
    <!--  Download Area -->
    <div style="margin-bottom:30px;">
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]">
        <xsl:call-template name="download-button" />
      </xsl:for-each>
      
      <xsl:if test="/mycoreobject[not(contains(@ID,'_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
        <xsl:variable name="recordID" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" />
        <xsl:if test="$recordID">
          <a class="btn btn-primary ir-button-download"  
             href="{$WebApplicationBaseURL}pdfdownload/recordIdentifier/{replace(recordID, '/','_')}" target="_blank">
            <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" title = "{mcri18n:translate('Webpage.docdetails.pdfdownload')}" />
            <strong>{mcri18n:translate('Webpage.docdetails.pdfdownload')}</strong>
          </a>
        </xsl:if>
      </xsl:if>
      <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
        <a class="btn btn-primary ir-button-download"
           href="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}/dfgviewer" target="_blank">
          <img style="height: 24px; margin: 3px 0px;float:left" src="{$WebApplicationBaseURL}images/dfgviewerLogo.svg" 
               title = "{mcri18n:translate('Webpage.docdetails.dfgviewer')}" />
        </a>
      </xsl:if>
      
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='data' or @categid='documentation' or @categid='supplement']]">
        <xsl:variable name="derid" select="./@xlink:href" />
        <xsl:call-template name="download-button" />
      </xsl:for-each>
          
      <!--
      <!- called by: rosdok_document_0000015736 ->
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
       </div><!--Download area -->
       
       <xsl:if test="not(/mycoreobject/service/servstates/servstate/@categid='deleted')">
         <div class="ir-box mt-3">
           <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
             <h4>Export</h4>
             <p>
               <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
                 <xsl:choose>
                   <xsl:when test="starts-with(/mycoreobject/@ID, 'rosdok')">
                     <xsl:call-template name="biblio-formate">
                       <xsl:with-param name="katalog">opac-de-28</xsl:with-param>
                       <xsl:with-param name="ppn" select="." />
                     </xsl:call-template>
                   </xsl:when>
                 <xsl:when test="starts-with(/mycoreobject/@ID, 'dbhsnb')">
                   <xsl:call-template name="biblio-formate">
                     <xsl:with-param name="katalog">opac-de-519</xsl:with-param>
                     <xsl:with-param name="ppn" select="." />
                   </xsl:call-template>
                 </xsl:when>
               </xsl:choose>
              </xsl:for-each>
            </p>
            
            <h4>Portale</h4>
            <p>
              <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
                <xsl:choose>
                  <xsl:when test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
                    <a class="badge px-1" target="_blank" href="http://opac.lbs-rostock.gbv.de/DB=1/PPNSET?PPN={.}">OPAC (UB Rostock)</a>
                  </xsl:when>
                  <xsl:when test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-519']">
                    <a class="badge px-1" target="_blank" href="http://opac.lbs-rostock.gbv.de/DB=2/PPNSET?PPN={.}">OPAC (HSB Neubrandenburg)</a>
                  </xsl:when>
                </xsl:choose>
                <a class="badge px-1" href="https://gso.gbv.de/DB=2.1/PPNSET?PPN={.}">OPAC (GBV)</a>
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
             data-services="[&quot;twitter&quot;, &quot;facebook&quot;, &quot;googleplus&quot;, &quot;linkedin&quot;, &quot;xing&quot;, &quot;whatsapp&quot;, &quot;mail&quot;, &quot;info&quot;]"
             data-mail-url="mailto:" data-mail-subject="{mcri18n:translate('OMD.ir.shariff.subject')}" data-mail-body="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}"
             data-orientation="horizontal" data-theme="standard">
           </div> <!-- data-theme=standard|grey|white --> 
           <script src="{$WebApplicationBaseURL}modules/shariff_3.0.1/shariff.min.js"></script>
           <p></p>
         </div>
       </xsl:if>
        
       <div class="ir-box">
         <h4>Rechte</h4>
         <p>
           <strong>
             <a href="https://rightsstatements.org/page/InC/1.0/?language=de">
               <img src="{$WebApplicationBaseURL}images/rightsstatements.org/buttons/InC.white.svg" title="in copyright" style="width:100px;background-color:grey;border:5px solid grey;" class="mr-3" />
                  Urheberrechtsschutz
             </a>
             </strong>
                <!-- old:
                <span class="badge badge-secondary float-left mr-2 h-100"><a href="https://rightsstatements.org/page/InC/1.0/?language=de"><img src="{$WebApplicationBaseURL}images/rightsstatements.org/buttons/InC.white.svg" title="in copyright" style="width:100px"></a></span>
                <br><strong><a href="https://rightsstatements.org/page/InC/1.0/?language=de">Urheberrechtsschutz</a></strong></p>
                <p class="text-justify form-text text-muted small">Dieses Objekt ist durch das Urheberrecht und/oder verwandte Schutzrechte geschützt. Sie sind berechtigt, das Objekt in jeder Form zu nutzen, die das Urheberrechtsgesetz und/oder einschlägige verwandte Schutzrechte gestatten. Für weitere Nutzungsarten benötigen Sie die Zustimmung der/des Rechteinhaber/s.</p>
                -->
           </p> 
       </div>

       <div class="my-3"><!--Tools --> 
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
                  href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.Style=solrdocument" rel="nofollow">SOLR in</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showSOLR')}"
                  href="{$WebApplicationBaseURL}api/v1/search?q=id:{/mycoreobject/@ID}" rel="nofollow">SOLR doc</a>
              <xsl:if test="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]">
                <xsl:variable name="derid" select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]/@xlink:href" />
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="{mcri18n:translate('Webpage.tools.showREPOS_METS')}" 
                   href="{$WebApplicationBaseURL}api/v1/objects/${it.id}/derivates/{$derid}/open">METS</a>
              </xsl:if>
              <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[contains(@valueURI, '#epub') or contains(@valueURI, '#data')]">
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
       </div><!-- Tools -->  
  </xsl:template>
  
  <xsl:template name="download-button">
    <xsl:variable name="mcrid" select="/mycoreobject/@ID" />
    <xsl:variable name="derid" select="./@xlink:href" />
    <xsl:variable name="fulltext_url">{$WebApplicationBaseURL}file/{$mcrid}/{$derid}/{./maindoc/text()}</xsl:variable>
        
    <div class="w-100 position-relative" style="padding-right:3em">
      <a class="badge border border-primary text-secondary position-absolute px-1 py-1" 
         style="right:0;bottom:0;height:3.0em"  download="{./maindoc}.md5" 
         href="data:text/plain;charset=US-ASCII,{encode-for-uri(concat(./title[@type='maindoc_md5'],'  ', ./maindoc))}">
         <i class="fas fa-check"></i><br />MD5
      </a>
      <a class="btn btn-primary ir-button-download d-inline-block"
         href="{$fulltext_url}" target="_blank">
         <xsl:choose>
           <xsl:when test="ends-with(./maindoc, '.zip')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_zip.png" title="{mcri18n:translate('Webpage.docdetails.zipdownload')}" />
           </xsl:when>
           <xsl:when test="ends-with(./maindoc, '.pdf')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" title="{mcri18n:translate('Webpage.docdetails.pdfdownload')}" />
           </xsl:when>
           <xsl:otherwise>
             <img align="left" src="{$WebApplicationBaseURL}images/download_other.png" title="{mcri18n:translate('Webpage.docdetails.otherdownload')}" />
           </xsl:otherwise>
         </xsl:choose>
         <span class="float-right"><small>({mcrstring:pretty-filesize(./title[@type='maindoc_size'])})</small></span>
         <strong>{mcrclass:current-label-text(./classification[@classid='derivate_types'])}</strong>
         <br /><small>{mcrstring:abbreviate-center(./maindoc, 40)}</small>
      </a>
    </div>
  </xsl:template>
  <xsl:template name="biblio-formate">
    <xsl:param name="katalog" />
    <xsl:param name="ppn" />
    <a class="badge px-1" target="_blank" href="http://unapi.gbv.de/?id={$katalog}:ppn:{ppn}&amp;format=bibtex">BibTeX</a>
    <a class="badge px-1" target="_blank" href="http://unapi.gbv.de/?id={$katalog}:ppn:{ppn}&amp;format=endnote">EndNote</a>
    <a class="badge px-1" target="_blank" href="http://unapi.gbv.de/?id={$katalog}:ppn:{ppn}&amp;format=ris">RIS</a>
    <a class="badge px-1" target="_blank" href="http://unapi.gbv.de/?id={$katalog}ppn:{ppn}&amp;format=dc">DublinCore</a>
    <a class="badge px-1" target="_blank" href="http://unapi.gbv.de/?id={$katalog}:ppn:{ppn}&amp;format=mods">MODS</a>
  </xsl:template>
  
</xsl:stylesheet>