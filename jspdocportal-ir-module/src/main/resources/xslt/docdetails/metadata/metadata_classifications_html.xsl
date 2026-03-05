<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="#all"
  expand-text="yes">
  
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

<!-- 
  <xsl:template name="classification2metadataTable">
    <xsl:param name="displayLabel" as="xs:string" />
    <xsl:param name="mods" />
    <xsl:if test="$mods/mods:*[@displayLabel=$displayLabel]">
      <tr>
        <th>{mcri18n:translate(concat('OMD.ir.docdetails.classifications.', $displayLabel))}</th>
        <td><table id="ir-table-docdetails-summary" class="ir-table-docdetails-values">
          <xsl:for-each select="$mods/mods:*[@displayLabel=$displayLabel]">
            <tr>
              <td>{mcrclass:current-label-text(mcrmods:to-category(.))}</td>
            </tr>                
          </xsl:for-each>
        </table></td>  
        <td>{count($mods/mods:*[@displayLabel=$displayLabel])}</td>
      </tr>
    </xsl:if>
    -->
    <xsl:template name="classification2metadataTable">
      <xsl:param name="items" />
      <xsl:if test="$items">
        <tr>
          <th>{mcri18n:translate(concat('OMD.ir.docdetails.metadata.classifications.', $items[1]/@displayLabel))}:</th>
          <td><table id="ir-table-docdetails-classification-{$items[1]/@displayLabel}" class="ir-table-docdetails-values">
            <xsl:for-each select="$items">
              <tr>
                <td>{mcrclass:current-label-text(mcrmods:to-category(.))}</td>
              </tr>
            </xsl:for-each>
          </table></td>
        </tr>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
