<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  exclude-result-prefixes="#all"
  expand-text="yes">
  
  <xsl:template name="identifier_k10plus_ppn">
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']">
      <xsl:variable name="category" select="mcrclass:category('identifier', 'k10plus')" />
        <tr><th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
            <td><span class="ir-identifier-text">{.}</span>
              <a class="ir-identifier-portal text-dark small ms-3" title="{mcri18n:translate('OMD.ir.docdetails.metadata.tooltip.identifier')}" 
                 href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',.)}">
                <i class="fas fa-external-link-alt"></i>
              </a>
            </td>
        </tr>
   </xsl:for-each>
  </xsl:template>
  <xsl:template name="identifier2metadataTable">
   <xsl:variable name="recordID" select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28' or @source='DE-519']" />
   <xsl:for-each select="mods:identifier[not(@type='purl')]">
     <xsl:variable name="category" select="mcrclass:category('identifier', @type)" />
       <xsl:if test="$category">
       <!-- Hide URN for DBHSNB -->
       <xsl:if test="not($category/@ID='urn' and $recordID/@source='DE-519')">
         <tr>
           <th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
           <td>
             <xsl:choose>
               <xsl:when test="$category/label[@xml:lang='x-resolve-url']">
                 <a class="ir-identifier-resolve" href="{replace($category/label[@xml:lang='x-resolve-url']/@text, '\{0\}',.)}">{.}</a>
               </xsl:when>
               <xsl:otherwise>
                 <span class="ir-identifier-text">{.}</span>
               </xsl:otherwise>
             </xsl:choose>
             <xsl:if test="$category/label[@xml:lang='x-portal-url']">
               <a class="ir-identifier-portal text-dark small ms-3" title="{mcri18n:translate('OMD.ir.docdetails.metadata.tooltip.identifier')}" 
                  href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',.)}">
                 <i class="fas fa-external-link-alt"></i>
               </a>
             </xsl:if>
           </td>
         </tr>
       </xsl:if>
       </xsl:if>
     </xsl:for-each>
     <xsl:for-each select="mods:identifier[@type='purl']">
       <xsl:variable name="p" select="replace(., 'http://purl.uni-rostock.de', 'https://purl.uni-rostock.de')" />
       <xsl:variable name="categ_purl" select="mcrclass:category('identifier', 'purl')" />
         <tr><th><abbr class="text-nowrap" title="{$categ_purl/label[@xml:lang=$CurrentLang]/@description}">{$categ_purl/label[@xml:lang=$CurrentLang]/@text}</abbr>: </th>
            <td>
              <a href="{$p}">{$p}</a>
               <a class="ir-identifier-portal text-dark small ms-3" title="{mcri18n:translate('OMD.ir.docdetails.metadata.tooltip.identifier')}" 
                  href="{replace($p, '://purl.uni-rostock.de/','://purl.uni-rostock.de/info/purl/')}">
                 <i class="fas fa-external-link-alt"></i>
               </a>
            </td>
         </tr>
     </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
