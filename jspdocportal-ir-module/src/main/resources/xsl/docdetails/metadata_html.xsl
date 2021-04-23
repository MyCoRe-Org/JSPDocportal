<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:/xsl/docdetails/metadata/metadata_classifications_html.xsl" />
  
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />
  <xsl:output method="html" indent="yes" standalone="no" />
  <xsl:template match="/">
    <table class="ir-table-docdetails">
      <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
        <tr>
          <th>Titel:</th>
          <td><table id="ir-table-docdetails-title" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:titleInfo[@usage='primary']">
              <xsl:call-template name="title" />
            </xsl:for-each>
          </table></td>
        </tr>
        <xsl:if test="./mods:titleInfo[not(@usage='primary')]">
          <tr>
            <th>Weitere Titel:</th>
            <td><table id="ir-table-docdetails-other-title" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:titleInfo[not(@usage='primary')]">
               <xsl:call-template name="title" />
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="./mods:relatedItem[@type='host']">
          <tr>
            <th>Gesamttitel:</th>
            <td>
              <xsl:if test="./mods:relatedItem[@type='host']/mods:recordInfo/mods:recordIdentifier">
                <span class="float-right">
                  <a class="btn btn-outline-secondary btn-sm" href="{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(./mods:relatedItem[@type='host']/mods:recordInfo/mods:recordIdentifier, '/','_')}">Öffnen</a>
                </span>
              </xsl:if>
              <table id="ir-table-docdetails-host-title" class="ir-table-docdetails-values">
                 <xsl:for-each select="./mods:relatedItem[@type='host']/mods:titleInfo">
                   <xsl:call-template name="title" />
                </xsl:for-each>
              </table>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="./mods:relatedItem[@otherType='appears_in']>
          <tr>
            <th>In:</th>
            <td>
              <xsl:for-each select="./mods:relatedItem[@otherType='appears_in']">
                <table id="ir-table-docdetails-host-title" class="ir-table-docdetails-values">
                  <xsl:if test="mods:recordInfo/mods:recordIdentifier">
                    <span class="float-right">
                      <a class="btn btn-outline-secondary btn-sm" href="{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(mods:recordInfo/mods:recordIdentifier, '/','_')}">Öffnen</a>
                    </span>
                  </xsl:if>
                  <xsl:for-each select="mods:titleInfo">
                    <xsl:call-template name="title" />
                  </xsl:for-each>
                  <xsl:for-each select="mods:part">
                    <tr><td>
                      {string-join(.//text(), ' ')}
                    </td></tr>
                  </xsl:for-each>
                  <xsl:for-each select="mods:originInfo[@eventType='publication']">
                    <tr><td>
                      {string-join((mods:publisher, mods:dateIssued[not(@encoding)]),', ')}
                    </td></tr>
                  </xsl:for-each>
                </table>
                <xsl:if test="mods:identifier">
                  <table id="ir-table-docdetails-host-title" class="ir-table-docdetails-values">
                    <xsl:call-template name="identifier2metadataTable" />
                  </table>
                </xsl:if>    
              </xsl:for-each>
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="./mods:note[@type='statement of responsibility']">
          <tr>
            <th>Verantwortlichkeitsangabe:</th>
            <td>
              <table id="ir-table-docdetails-statement_of_responsibility" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:note[@type='statement of responsibility']">
                <tr><td>{.}</td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        
        
        <xsl:if test="mods:name[@type='personal']">
          <tr>
            <th>Beteiligte Personen:</th>
            <td><table id="ir-table-docdetails-name_personal" class="ir-table-docdetails-values w-100">
              <xsl:for-each select="./mods:name[@type='personal']">
                <xsl:if test="mods:namePart[@type='given' or @type='family']">
                  <tr><td colspan="2">
                    <strong>
                      <xsl:value-of select="string-join((mods:namePart[@type='given'], mods:namePart[@type='family']),' ')" />
                      <xsl:if test="mods:namePart[@type='termsOfAddress']">
                       , <xsl:value-of select="string-join(mods:namePart[@type='termsOfAddress'], ', ')" />
                      </xsl:if>
                    </strong>
                    <xsl:choose>
                      <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
                        <span class="float-right">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
                      </xsl:when>
                      <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
                        <span class="float-right">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
                      </xsl:when>
                    </xsl:choose>                    
                    
                  </td></tr>
                </xsl:if>
                <xsl:if test="mods:namePart[not(@type)]">
                  <tr><td colspan="2">
                    <strong><xsl:value-of select="string-join(mods:namePart[not(@type)],' ')" /></strong>
                  </td></tr>
                </xsl:if>
                
                <xsl:if test="./mods:nameIdentifier[@type='orcid']">
                  <tr>
                    <th class="text-center" style="width:3em"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconbwvector.svg"  style="height:1.5em" title="ORCID (Open Researcher and Contributor ID)" /></th>
                    <td><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
                  </tr>
                </xsl:if>
                <xsl:if test="./mods:nameIdentifier[@type='gnd']">
                  <tr>
                    <th class="text-center" style="width:3em"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
                    <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
                  </tr>
                </xsl:if>
                <xsl:for-each select="./mods:affiliation">
                  <tr>
                    <th class="text-center align-text-top"><i class="fas fa-university" title="Einrichtung" style="font-size:1.5em"></i></th>
                    <td>{.}</td>
                  </tr>
                </xsl:for-each>
             </xsl:for-each>
          </table></td>
        </tr>
      </xsl:if>
      <xsl:if test="mods:name[@type='corporate'][not(contains('pbl|prt', mods:role/mods:roleTerm[@authority='marcrelator']))]">
        <tr>
          <th>Beteiligte Körperschaften:</th>
          <td><table id="ir-table-docdetails-name_corporate" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:name[@type='corporate'][not(contains('pbl|prt', mods:role/mods:roleTerm[@authority='marcrelator']))]">
              <xsl:choose>
                <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
                  <tr><td colspan="2">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</td></tr>
                </xsl:when>
                <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
                  <tr><td colspan="2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</td></tr>                 
                </xsl:when>
              </xsl:choose>   
              <tr><td colspan="2">
                <strong><xsl:value-of select="string-join(mods:namePart,', ')" /></strong>
              </td></tr>    
              <xsl:if test="./mods:nameIdentifier[@type='gnd']">
                <tr>
                  <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
                  <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
                </tr>
              </xsl:if>            
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
       <xsl:if test="mods:name[@type='conference']">
        <tr>
          <th>Konferenz:</th>
          <td><table id="ir-table-docdetails-name_conference" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:name[@type='conference']">
              <xsl:choose>
                <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
                  <tr><td colspan="2">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</td></tr>
                </xsl:when>
                <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
                  <tr><td colspan="2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</td></tr>                 
                </xsl:when>
              </xsl:choose>   
              <tr><td colspan="2">
                <xsl:value-of select="string-join(mods:namePart,', ')" />
              </td></tr>                
            </xsl:for-each>
            <xsl:if test="./mods:nameIdentifier[@type='gnd']">
              <tr>
                <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
                <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
              </tr>
            </xsl:if> 
         </table></td>  
        </tr>
      </xsl:if>
      <xsl:if test="mods:abstract">
        <tr>
          <th>Zusammenfassung:</th>
          <td><table id="ir-table-docdetails-summary" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:abstract[@type='summary']">
              <tr>
                <td>{.}</td>
                <td>[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:rfc5646:',@xml:lang))//category)}]</td>
              </tr>                
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>

      <xsl:call-template name="classification2metadataTable">
        <xsl:with-param name="items" select="./mods:genre[@displayLabel='doctype']" />
      </xsl:call-template>
     
      <xsl:call-template name="classification2metadataTable">
        <xsl:with-param name="items" select="./mods:classification[@displayLabel='institution']" />
      </xsl:call-template>
      <xsl:call-template name="classification2metadataTable">
        <xsl:with-param name="items" select="./mods:classification[@displayLabel='provider']" />
      </xsl:call-template>
      
       <xsl:call-template name="classification2metadataTable">
        <xsl:with-param name="items" select="./mods:classification[@displayLabel='collection']" />

      </xsl:call-template>
      
      <xsl:if test="mods:language">
        <tr>
          <th>Sprache:</th>
          <td><table id="ir-table-docdetails-language" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:language/mods:languageTerm">
              <tr><td>
                {mcrclass:current-label-text(document(concat('classification:metadata:0:children:rfc5646:', .))//category[1])}
              </td></tr>                
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
      
     <xsl:if test="mods:physicalDescription">
        <tr>
          <th>Umfang:</th>
          <td><table id="ir-table-docdetails-language" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:physicalDescription">
              <tr><td>
                {string-join((mods:extent, mods:note[@type='source_dimensions'], mods:note[@type='content']),'; ')}
              </td></tr>                
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
      
       <xsl:if test="mods:location/mods:physicalLocation[@type='current']">
        <tr>
          <th>Signatur:</th>
          <td><table id="ir-table-docdetails-physicalLocation" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:location[mods:physicalLocation[@type='current']]">
              <tr><td>
                {concat(mods:physicalLocation,': ', mods:shelfLocator)}
              </td></tr>                
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
      <xsl:if test="mods:identifier">
        <tr>
          <th>Identifikatoren:</th>
          <td><table id="ir-table-docdetails-identifier" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:identifier[not(@type='purl')]">
              <xsl:choose>
                <xsl:when test="@type='uri'">
                    <xsl:variable name="category" select="mcrclass:category('identifier', 'uri')" />
                    <tr><th><abbr title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                    <td>
                        <a href="{.}">{substring-after(.,':ppn:')}</a>
                    </td></tr>
                </xsl:when>
                <xsl:when test="@type='rism'">
                  <xsl:choose>
                    <xsl:when test="contains(., 'ID no.:')">
                      <xsl:variable name="category" select="mcrclass:category('identifier', 'rism')" />
                      <tr>
                      <th><abbr title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <xsl:variable name="rismID" select="substring-after(., 'ID no.:')" />
                      <td><a href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',$rismID)}">{$rismID}</a></td>
                      </tr>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:variable name="category" select="mcrclass:category('identifier', 'rism_series')" />
                      <tr>
                      <th><abbr title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <td>{.}</td>
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="category" select="mcrclass:category('identifier', @type)" />
                  <xsl:if test="$category">
                    <tr>
                      <th><abbr title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <td>
                        <xsl:choose>
                          <xsl:when test="$category/label[@xml:lang='x-portal-url']">
                            <a href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',.)}">{.}</a>
                          </xsl:when>
                          <xsl:otherwise>
                            {.}
                          </xsl:otherwise>
                        </xsl:choose>
                      </td>
                    </tr>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="mods:identifier[@type='purl']">
              <xsl:variable name="categ_purl" select="mcrclass:category('identifier', 'purl')" />
              <tr><th><abbr title="{$categ_purl/label[@xml:lang=$CurrentLang]/@description}">{$categ_purl/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                  <td><a href="{.}">{.}</a></td>
              </tr>
            </xsl:for-each>
            <xsl:variable name="categ_mcrid" select="mcrclass:category('identifier', 'mycore_object_id')" />
            <tr><th><abbr title="{$categ_mcrid/label[@xml:lang=$CurrentLang]/@description}">{$categ_mcrid/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                <td><a href="{/mycoreobject/@ID}">{/mycoreobject/@ID}</a></td>
            </tr>
          </table></td>
        </tr>
      </xsl:if>
    </xsl:for-each>
    
    </table>
  </xsl:template>
</xsl:stylesheet>