<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xslt/functions/classification.xsl" />
  <xsl:import href="resource:xslt/functions/mods.xsl" />
  <xsl:import href="resource:xslt/functions/i18n.xsl" />
  <xsl:import href="resource:xslt/functions/stringutils.xsl" />
  
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />
  <xsl:output method="html" indent="yes" standalone="no" />
  <xsl:template match="/">
    <table class="ir-table-docdetails w-100">
      <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
        <tr>
          <th>{mcri18n:translate('OMD.ir.docdetails.download.label')}</th>
          <td>
             <table id="ir-table-docdetails-download" class="ir-table-docdetails-values">
               <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='edited']/mods:mods/mods:extension[@displayLabel='external_content']/file">
                 <tr><td> 
                   <xsl:call-template name="download-button-extern" />
                 </td></tr>
               </xsl:for-each>
               <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='data' or @categid='documentation' or @categid='supplement']]">
                 <tr><td>
                   <xsl:call-template name="download-button" />
                 </td></tr>
               </xsl:for-each>
            </table>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
  
  <xsl:template name="download-button">
    <xsl:variable name="mcrid" select="/mycoreobject/@ID" />
    <xsl:variable name="derid" select="./@xlink:href" />
    <xsl:variable name="fulltext_url">{$WebApplicationBaseURL}file/{$mcrid}/{$derid}/{./maindoc/text()}</xsl:variable>
      <a class="btn btn-primary ir-button-download d-inline-block"
          href="{$fulltext_url}" target="_blank">
         <xsl:choose>
           <xsl:when test="ends-with(./maindoc, '.zip')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_zip.png" />
           </xsl:when>
           <xsl:when test="ends-with(./maindoc, '.pdf')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" />
           </xsl:when>
           <xsl:otherwise>
             <img align="left" src="{$WebApplicationBaseURL}images/download_other.png" />
           </xsl:otherwise>
         </xsl:choose>
         <span class="small float-end">({mcrstring:pretty-filesize(./maindoc_size)})</span>
         <strong>{mcrclass:current-label-text(./classification[@classid='derivate_types'])}</strong>
         <br /><span class="small">{mcrstring:abbreviate-center(./maindoc, 80)}</span>
      </a>
      <br />
      <div class="mt-3 mb-4 text-end">
      <span class="pr-3" style="font-variant: petite-caps;">MD5-Prüfsumme:</span>
      <a class="btn btn-sm btn-outline-secondary" 
         style=""  download="{./maindoc}.md5" 
         href="data:text/plain;charset=US-ASCII,{encode-for-uri(concat(./maindoc_md5,'  ', ./maindoc))}">
         <i class="fas fa-download pr-2"></i> {./maindoc_md5}
      </a>
      </div>
  </xsl:template>
  
  <xsl:template name="download-button-extern">
      <xsl:variable name="fulltext_url" select="./@URL" />
      <a class="btn btn-primary ir-button-download d-inline-block"
         href="{$fulltext_url}" target="_blank">
         <xsl:choose>
           <xsl:when test="ends-with(./@OWNERID, '.zip')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_zip.png" />
           </xsl:when>
           <xsl:when test="ends-with(./@OWNERID, '.pdf')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" />
           </xsl:when>
           <xsl:otherwise>
             <img align="left" src="{$WebApplicationBaseURL}images/download_other.png" />
           </xsl:otherwise>
         </xsl:choose>
         <span class="small float-end">({mcrstring:pretty-filesize(./@SIZE)})</span>
         <strong>{mcrclass:current-label-text(document(concat('classification:metadata:0:children:derivate_types:',./@USE))//category)}</strong>
         <br /><span class="small">{mcrstring:abbreviate-center(./@OWNERID, 80)}</span>
      </a>
      <br />
      <div class="mt-3 mb-4 text-end">
      <span class="pr-3" style="font-variant: petite-caps;">MD5-Prüfsumme:</span>
      <a class="btn btn-sm btn-outline-secondary" 
         style=""  download="{./@OWNERID}.md5" 
         href="data:text/plain;charset=US-ASCII,{encode-for-uri(concat(./@CHECKSUM,'  ', ./@OWNERID))}">
         <i class="fas fa-download pr-2"></i> {./@CHECKSUM}
      </a>
      </div>
  </xsl:template>
  
</xsl:stylesheet>