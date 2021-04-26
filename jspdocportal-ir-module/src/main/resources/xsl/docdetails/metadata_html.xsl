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
  <xsl:import href="resource:/xsl/docdetails/metadata/metadata_title_html.xsl" />
  <xsl:import href="resource:/xsl/docdetails/metadata/metadata_identifier_html.xsl" />
    <xsl:import href="resource:/xsl/docdetails/metadata/metadata_name_html.xsl" />
  
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
        <xsl:if test="./mods:relatedItem[@otherType='appears_in']">
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
                  <xsl:if test="mods:note[@type='relation_label']">
                    <tr><td>{mods:note[@type='relation_label']}:</td></tr>
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
        <xsl:if test="./mods:relatedItem[@type='series']">
          <tr>
            <th>Schriftenreihe:</th>
            <td>
              <xsl:for-each select="./mods:relatedItem[@type='series']">
                <table id="ir-table-docdetails-series" class="ir-table-docdetails-values">
                  <xsl:if test="mods:recordInfo/mods:recordIdentifier">
                    <span class="float-right">
                      <a class="btn btn-outline-secondary btn-sm" href="{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(mods:recordInfo/mods:recordIdentifier, '/','_')}">Öffnen</a>
                    </span>
                  </xsl:if>
                  <xsl:for-each select="mods:titleInfo">
                    <xsl:call-template name="title" />
                  </xsl:for-each>
                  <xsl:for-each select="mods:part/mods:detail/mods:number">
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
        <xsl:if test="./mods:relatedItem[@type='otherFormat']">
          <tr>
            <th>Weitere Publikation:</th>
            <td>
              <xsl:for-each select="./mods:relatedItem[@type='otherFormat']">
                <table id="ir-table-docdetails-otherformat" class="ir-table-docdetails-values">
                <tr><td>
                  <xsl:if test="mods:recordInfo/mods:recordIdentifier">
                    <span class="float-right">
                      <a class="btn btn-outline-secondary btn-sm" href="{$WebApplicationBaseURL}resolve/recordIdentifier/{replace(mods:recordInfo/mods:recordIdentifier, '/','_')}">Öffnen</a>
                    </span>
                  </xsl:if>
                  <xsl:if test="mods:note[@type='relation_label']">
                    <span>{mods:note[@type='relation_label']}: </span>
                  </xsl:if>
                  <xsl:if test="mods:note[@type='format_type']">
                    <strong>{mods:note[@type='format_type']}</strong>
                  </xsl:if>
                  </td></tr>
                </table>
                <xsl:if test="mods:identifier">
                  <table id="ir-table-docdetails-otherFormat-ids" class="ir-table-docdetails-values">
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
            <td><table id="ir-table-docdetails-name_personal" class="ir-table-docdetails-values">
                 <xsl:call-template name="personal_name">
                   <xsl:with-param name="names" select="mods:name[@type='personal']" />
                 </xsl:call-template>
               </table>
             </td>
        </tr>
      </xsl:if>
      <xsl:if test="mods:name[@type='corporate']">
      <!-- wenn Verlage + Drucker ausgeschlossen werden sollen: [not(contains('pbl|prt', mods:role/mods:roleTerm[@authority='marcrelator']))] -->
        <tr>
          <th>Beteiligte Körperschaften:</th>
          <td><table id="ir-table-docdetails-name_corporate" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:name[@type='corporate']">
              <tr><td colspan="2">
                <strong><xsl:value-of select="string-join(mods:namePart,', ')" /></strong>
                <xsl:choose>
                  <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
                    <span class="ir-table-docdetails-values-label">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</span>
                  </xsl:when>
                  <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
                    <span class="ir-table-docdetails-values-label">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</span>                 
                  </xsl:when>
                </xsl:choose>  
            
              </td></tr>    
              <xsl:if test="./mods:nameIdentifier[@type='gnd']">
                <tr>
                  <th class="text-center" style="width:3em;"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
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
                <td class="text-justify">
                   {.}
                  <span class="ir-table-docdetails-values-label">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:rfc5646:',@xml:lang))//category)}]</span>
                </td>
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
      <xsl:if test="mods:originInfo[@eventType='publication']">
        <tr>
          <th>Veröffentlichung /<br />Entstehung:</th>
          <td><table id="ir-table-docdetails-origininfo-publication" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:originInfo[@eventType='publication']">
              <xsl:choose>
                <xsl:when test="mods:publisher">
                  <xsl:for-each select="mods:publisher">
                    <tr>
                      <xsl:if test="position()=1">
                        <th rowspan="{count(../mods:publisher)}">Verlage:</th>
                      </xsl:if>
                      <td>
                        <xsl:variable name="thePublisher" select="." />
                          {.} {string-join(./following-sibling::mods:place[not(@supplied='yes')][preceding-sibling::mods:publisher[1]=$thePublisher]/mods:placeTerm,', ')}
                      </td>
                    </tr>
                  </xsl:for-each>
                  <xsl:if test="mods:place[@supplied='yes']/mods:placeTerm">
                    <tr><th>Orte:</th>
                        <td>(normiert: {string-join(mods:place[@supplied='yes']/mods:placeTerm,', ')})</td>
                    </tr>      
                  </xsl:if>
                </xsl:when>
                <xsl:when test="mods:place[not(@supplied='yes')]/mods:placeTerm">
                  <tr><th>Orte:</th>
                      <td>{string-join(mods:place[not(@supplied='yes')]/mods:placeTerm,', ')}
                        <xsl:if test="mods:place[@supplied='yes']/mods:placeTerm">
                         <br />(normiert: {string-join(mods:place[@supplied='yes']/mods:placeTerm,', ')})
                        </xsl:if>
                      </td>
                  </tr> 
                </xsl:when>
              </xsl:choose>
              <xsl:if test="mods:dateIssued[not(@*)]">
                <tr><th>Datum:</th>
                  <td>
                    {mods:dateIssued[not(@*)]}
                    <xsl:if test="mods:dateIssued[@keyDate='yes' or @point='start' or @point='end']">
                    <br />(normiert:
                      <xsl:if test="mods:dateIssued[@keyDate='yes' and not(@point='start')]">
                       {mods:dateIssued[@keyDate='yes' and not(@point='start')]}
                      </xsl:if>
                      <xsl:if test="mods:dateIssued[@point='start']">
                        von {mods:dateIssued[@point='start']}
                      </xsl:if>
                      <xsl:if test="mods:dateIssued[@point='end']">
                        bis {mods:dateIssued[@point='end']}
                      </xsl:if>)
                    </xsl:if>
                  </td>
                </tr>      
              </xsl:if>
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
      
      <xsl:if test="mods:originInfo[@eventType='digitization']">
        <tr>
          <th>Digitalisierung:</th>
          <td><table id="ir-table-docdetails-origininfo-digitization" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:originInfo[@eventType='digitization']">
              <tr><td>{string-join((mods:place/mods:placeTerm, mods:publisher, mods:dateCaptured[not(@*)]), ', ')}</td></tr>
            </xsl:for-each>
          </table></td>  
        </tr>
      </xsl:if>
      
      <xsl:if test="mods:note[@type='source_note' or @type='other' or @type='reproduction']">
        <tr>
          <th>Anmerkungen:</th>
          <td><table id="ir-table-docdetails-notes" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:note[@type='source_note' or @type='other' or @type='reproduction']">
              <tr><td>{.}</td></tr>
            </xsl:for-each>
          </table></td>  
        </tr>
      </xsl:if>
      <xsl:if test="mods:note[@type='bibliographic_reference']">
        <tr>
          <th>Biliographische Referenzen:</th>
          <td><table id="ir-table-docdetails-biblref" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:note[@type='bibliographic_reference']">
              <tr><td>{.}</td></tr>
            </xsl:for-each>
          </table></td>  
        </tr>
      </xsl:if>
      <xsl:if test="mods:note[@type='external_link']">
        <tr>
          <th>Weitere Informationen:</th>
          <td><table id="ir-table-docdetails-notes-other" class="ir-table-docdetails-values">
            <xsl:for-each select="mods:note[@type='external_link']">
              <tr><td><a href="{./@xlink:href}">{if (string-length(.)=0) then ./@xlink:href else .}</a></td></tr>
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
                {concat(mods:physicalLocation,':')}<br /><strong>{mods:shelfLocator}</strong>
              </td></tr>                
            </xsl:for-each>
         </table></td>  
        </tr>
      </xsl:if>
      <xsl:if test="mods:identifier">
        <tr>
          <th>Identifikatoren:</th>
          <td><table id="ir-table-docdetails-identifier" class="ir-table-docdetails-values">
            <xsl:call-template name="identifier2metadataTable" />
          </table></td>
        </tr>
      </xsl:if>
      
      <xsl:if test="mods:classification[@displayLabel='accesscondition']">
        <tr>
          <th>Zugang:</th>
          <td><table id="ir-table-docdetails-identifier" class="ir-table-docdetails-values">
            <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[@displayLabel='accesscondition'][1])" />
            <tr><td><strong>
            {$categ/label[@xml:lang=$CurrentLang]/@text}
            </strong></td></tr>
            <xsl:if test="$categ/label[@xml:lang=$CurrentLang]/@description">
            <tr><td>
              {$categ/label[@xml:lang=$CurrentLang]/@description}
            </td></tr>
            </xsl:if>
            
          </table></td>
        </tr>
      </xsl:if>
      
        <tr>
          <th>Lizenzen/Rechtehinweis:</th>
          <td><table id="ir-table-docdetails-licenses" class="ir-table-docdetails-values">
            <tr><th>Werk:</th>
                <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#work')])" />
                <td class="text-justify">
                  <a href="{$categ/label[@xml:lang='x-uri']/@text}"><img src="{$WebApplicationBaseURL}images{$categ/label[@xml:lang='x-icon']/@text}" /></a>
                  <br /><xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@description" disable-output-escaping="true" />
                 </td>
            </tr>
            <xsl:if test="mods:classification[contains(@valueURI, 'licenseinfo#digitisedimages')]">
              <tr><td colspan="2"><hr /></td></tr>
              <tr><th>Digitalisate:</th>
                <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#digitisedimages')])" />
                <xsl:variable name="categ_icon" select="mcrclass:category('licenseinfo', 'work.cclicense.cc-by-sa.v40')" />
                <td class="text-justify">
                  <a href="{$categ_icon/label[@xml:lang='x-uri']/@text}"><img src="{$WebApplicationBaseURL}images{$categ_icon/label[@xml:lang='x-icon']/@text}" /></a>
                <br />
                  {$categ/label[@xml:lang=$CurrentLang]/@text}
                 </td>
              </tr>
            </xsl:if>
          </table></td>
        </tr>
        
        
        <tr>
          <th>Technische Metadaten:</th>
          <td><table id="ir-table-docdetails-technical" class="ir-table-docdetails-values">
            <xsl:variable name="categ_mcrid" select="mcrclass:category('identifier', 'mycore_object_id')" />
            <tr><th><abbr title="{$categ_mcrid/label[@xml:lang=$CurrentLang]/@description}">{$categ_mcrid/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                <td><a href="{/mycoreobject/@ID}">{/mycoreobject/@ID}</a></td>
            </tr>
            <tr><th>erstellt:</th>
                <td>am {format-dateTime(/mycoreobject/service/servdates/servdate[@type='createdate'], '[D01].[M01].[Y0001]')}
                    von {/mycoreobject/service/servflags/servflag[@type='createdby']}</td>
            </tr>
            <tr><th>geändert:</th>
                <td>am {format-dateTime(/mycoreobject/service/servdates/servdate[@type='modifydate'], '[D01].[M01].[Y0001]')}
                    von {/mycoreobject/service/servflags/servflag[@type='modifiedby']}</td>
            </tr>
            <tr><th>Metadaten-<br/>Lizenz:</th>
                 <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#metadata')])" />
                <td class="text-justify">
                <!-- Logo ausblenden -->
                <!-- 
                  <a href="{$categ/label[@xml:lang='x-uri']/@text}"><img src="{$WebApplicationBaseURL}images{$categ/label[@xml:lang='x-icon']/@text}" /></a>
                <br />
                -->
                  <xsl:value-of select="replace(replace($categ/label[@xml:lang=$CurrentLang]/@description,'\{0\}', mcri18n:translate('OMD.ir.docdetails.license.metadata.owner')), '\{1\}', concat($WebApplicationBaseURL,'api/v1/objects/',/mycoreobject/@ID))" disable-output-escaping="true" />
                 </td>
            </tr>
          </table></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
</xsl:stylesheet>