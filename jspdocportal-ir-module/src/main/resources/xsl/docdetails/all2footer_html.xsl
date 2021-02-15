<?xml version="1.0"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  exclude-result-prefixes="mods xlink ubr-legal"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n">
  
  
  <xsl:import href="mods-util.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
     
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="CurrentLang" />
  <xsl:output method="html" indent="yes" standalone="no" />
  <xsl:template match="/">
  <table class="ir-table-docdetails" style="margin-top:45px">
          <tr><td colspan="2"><hr /></td></tr>
         <tr>
            <th>erstellt am:</th>
            <td><table class="ir-table-docdetails-values"><tr><td>
              <xsl:value-of select="substring-before(/mycoreobject/service/servdates/servdate[@type='createdate'],'T')" />
            </td></tr></table>
            </td>
          </tr>
          <tr>
            <th>zuletzt geändert am:</th>
            <td><table class="ir-table-docdetails-values"><tr><td>
              <xsl:value-of select="substring-before(/mycoreobject/service/servdates/servdate[@type='modifydate'],'T')" />
            </td></tr></table>
            </td>
          </tr>
          
      
          <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='licenseinfo'][contains(@valueURI, '#metadata.cc0')]">
            <xsl:variable name="metaClass" select="mcrmods:to-mycoreclass(., 'single')" />
            <tr>
              <th><xsl:value-of select="mcri18n:translate('OMD.metadata-licence')" /></th>
              <td>
                <table class="ir-table-docdetails-values">
                  <tr>
                    <td>
                      <div style="float:left">
                        <xsl:element name="a">
                          <xsl:attribute name="rel">"license"</xsl:attribute> 
                          <xsl:attribute name="href">
                            <xsl:value-of select="$metaClass/categories/category/label[@xml:lang='x-uri']/@text" />
                            <!-- categories/category/label[@xml:lang='x-uri']/@text" -->
                           </xsl:attribute>
                          <xsl:element name="img">
                            <xsl:attribute name="src"><xsl:value-of select="$WebApplicationBaseURL"/>images/creativecommons/p/zero/1.0/88x31.png</xsl:attribute>
                            <xsl:attribute name="style">border-style: none</xsl:attribute>
                            <xsl:attribute name="alt">CC0</xsl:attribute>
                          </xsl:element>
                        </xsl:element>
                      </div>
                      <div class="small" style="position: relative; margin-left:100px;">
                        <xsl:value-of disable-output-escaping="yes" select="replace(replace(
                            $metaClass/categories/category/label[@xml:lang=$CurrentLang]/@description, 
                            '\{0\}', mcri18n:translate('OMD.metadata-licence.institution')),
                            '\{1\}', concat($WebApplicationBaseURL, 'api/v1/objects/', /mycoreobject/@ID))" />
                      </div>
                    </td>
                  </tr>
                </table>
             </td>
            </tr>
          </xsl:for-each>
         
          
  </table>            
  </xsl:template>
</xsl:stylesheet>