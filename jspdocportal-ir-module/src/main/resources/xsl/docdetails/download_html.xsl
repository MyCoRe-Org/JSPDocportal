<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/stringutils.xsl" />
  
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />
  <xsl:output method="html" indent="yes" standalone="no" />
  <xsl:template match="/">
    <table class="ir-table-docdetails w-100">
      <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
        <tr>
          <th>Download:</th>
          <td>
             <table id="ir-table-docdetails-download" class="ir-table-docdetails-values">
             
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
             <img align="left" src="{$WebApplicationBaseURL}images/download_zip.png" title="{mcri18n:translate('Webpage.docdetails.zipdownload')}" />
           </xsl:when>
           <xsl:when test="ends-with(./maindoc, '.pdf')">
             <img align="left" src="{$WebApplicationBaseURL}images/download_pdf.png" title="{mcri18n:translate('Webpage.docdetails.pdfdownload')}" />
           </xsl:when>
           <xsl:otherwise>
             <img align="left" src="{$WebApplicationBaseURL}images/download_other.png" title="{mcri18n:translate('Webpage.docdetails.otherdownload')}" />
           </xsl:otherwise>
         </xsl:choose>
         <span class="small float-right">({mcrstring:pretty-filesize(./maindoc_size)})</span>
         <strong>{mcrclass:current-label-text(./classification[@classid='derivate_types'])}</strong>
         <br /><span class="small">{mcrstring:abbreviate-center(./maindoc, 40)}</span>
      </a>
      <br />
      <div class="mt-3 mb-4 text-right">
      <span class="pr-3" style="font-variant: petite-caps;">MD5-Prüfsumme:</span>
      <a class="btn btn-sm btn-outline-secondary" 
         style=""  download="{./maindoc}.md5" 
         href="data:text/plain;charset=US-ASCII,{encode-for-uri(concat(./maindoc_md5,'  ', ./maindoc))}">
         <i class="fas fa-download pr-2"></i> {./maindoc_md5}
      </a>
      </div>
  </xsl:template>
</xsl:stylesheet>