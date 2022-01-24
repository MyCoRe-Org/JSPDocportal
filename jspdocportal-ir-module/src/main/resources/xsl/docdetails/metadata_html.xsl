<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  exclude-result-prefixes="mods xlink" expand-text="true">
  
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
      <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
        <tr>
          <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.title')}</th>
          <td><table id="ir-table-docdetails-title" class="ir-table-docdetails-values">
            <xsl:for-each select="./mods:titleInfo[@usage='primary']">
              <xsl:call-template name="title" />
            </xsl:for-each>
          </table></td>
        </tr>
        <xsl:if test="./mods:titleInfo[not(@usage='primary')]">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.other_title')}</th>
            <td><table id="ir-table-docdetails-other-title" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:titleInfo[not(@usage='primary')]">
               <xsl:call-template name="title" />
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="./mods:relatedItem[@type='host']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.host_title')}</th>
            <td>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.appears_in')}</th>
            <td>
              <xsl:for-each select="./mods:relatedItem[@otherType='appears_in']">
                <table id="ir-table-docdetails-host-title" class="ir-table-docdetails-values">
                  <xsl:if test="mods:note[@type='relation_label']">
                    <tr><td class="small">{mods:note[@type='relation_label']}:</td></tr>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.series')}</th>
            <td>
              <xsl:for-each select="./mods:relatedItem[@type='series']">
                <table id="ir-table-docdetails-series" class="ir-table-docdetails-values">
                <xsl:variable name="theTitle">
                  <xsl:for-each select="mods:titleInfo">
                    <xsl:call-template name="title" />
                  </xsl:for-each>
                  </xsl:variable>
                  <tr><td>
                    <xsl:copy-of select="$theTitle/tr/td/*" />
                  <xsl:for-each select="mods:part/mods:detail/mods:number">
                  <span>, {string-join(.//text(), ' ')}</span>
                  </xsl:for-each>
                  <xsl:for-each select="mods:originInfo[@eventType='publication']">
                      <span>, {string-join((mods:publisher, mods:dateIssued[not(@encoding)]),', ')}</span>
                  </xsl:for-each>
                  </td></tr>
                </table>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:name[@type='personal']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.personal_name')}</th>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.corporate_name')}</th>
            <td><table id="ir-table-docdetails-name_corporate" class="ir-table-docdetails-values">
              <xsl:call-template name="corporate_name">
                <xsl:with-param name="names" select="mods:name[@type='corporate']" />
              </xsl:call-template>
           </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:name[@type='conference']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.conference_name')}</th>
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
                  <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" 
                  title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
                  <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
                </tr>
              </xsl:if>
            </table></td>
          </tr>
        </xsl:if>
      
        <tr><td colspan="2" class="p-0" style="font-size:.5em">&#160;</td></tr>
      
        <xsl:if test="mods:abstract">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.abstract')}</th>
            <td><table id="ir-table-docdetails-summary" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:abstract[@type='summary']">
                <tr>
                  <td class="text-justify">
                     {.}
                    <span class="small pl-2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:rfc5646:',@xml:lang))//category)}]</span>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.language')}</th>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.physical_description')}</th>
            <td><table id="ir-table-docdetails-language" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:physicalDescription">
                <tr><td>
                  {string-join((mods:extent, mods:note[@type='source_dimensions'], mods:note[@type='content']),'; ')}
                </td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
      
        <tr><td colspan="2" class="p-0" style="font-size:.5em">&#160;</td></tr>
            
        <xsl:if test="mods:originInfo[@eventType='publication']">
          <tr>
            <th><xsl:value-of select="mcri18n:translate('OMD.ir.docdetails.metadata.label.origin_info_publication')" disable-output-escaping="yes" /></th>
            <td><table id="ir-table-docdetails-origininfo-publication" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:originInfo[@eventType='publication']">
                <xsl:choose>
                  <xsl:when test="mods:publisher">
                    <xsl:for-each select="mods:publisher">
                      <tr>
                        <td>
                          <xsl:variable name="thePublisher" select="." />
                          {string-join(./following-sibling::mods:place[not(@supplied='yes')][preceding-sibling::mods:publisher[1]=$thePublisher]/mods:placeTerm,', ')}: {.}
                        </td>
                      </tr>
                    </xsl:for-each>
                    <xsl:if test="mods:place[@supplied='yes']/mods:placeTerm">
                      <tr>
                        <td class="small">
                          ({if (count(mods:place[@supplied='yes']/mods:placeTerm)=1) 
                            then mcri18n:translate('OMD.ir.docdetails.metadata.label.supplied_place') 
                            else mcri18n:translate('OMD.ir.docdetails.metadata.label.supplied_places')}
                          {string-join(mods:place[@supplied='yes']/mods:placeTerm,', ')})
                        </td>
                      </tr>      
                  </xsl:if>
                  </xsl:when>
                  <xsl:when test="mods:place[not(@supplied='yes')]/mods:placeTerm">
                    <tr>
                      <td>{string-join(mods:place[not(@supplied='yes')]/mods:placeTerm,', ')}
                        <xsl:if test="mods:place[@supplied='yes']/mods:placeTerm">
                          <span class="small">
                            ({if (count(mods:place[@supplied='yes']/mods:placeTerm)=1) 
                            then mcri18n:translate('OMD.ir.docdetails.metadata.label.supplied_place') 
                            else mcri18n:translate('OMD.ir.docdetails.metadata.label.supplied_places')}
                            {string-join(mods:place[@supplied='yes']/mods:placeTerm,', ')})
                          </span>
                        </xsl:if>
                      </td>
                    </tr>
                  </xsl:when>
                </xsl:choose>
                <xsl:if test="mods:dateIssued[not(@*)]">
                  <tr>
                    <td>
                      {mods:dateIssued[not(@*)]}
                      <xsl:if test="mods:dateIssued[@keyDate='yes' or @point='start' or @point='end']">
                        <xsl:variable name="normalized_date">
                        <xsl:if test="mods:dateIssued[@keyDate='yes' and not(@point='start')]">
                         {mods:dateIssued[@keyDate='yes' and not(@point='start')]}
                        </xsl:if>
                        <xsl:if test="mods:dateIssued[@point='start']">
                          {mcri18n:translate('OMD.ir.docdetails.metadata.label.start_date')} {mods:dateIssued[@point='start']}
                        </xsl:if>
                        <xsl:if test="mods:dateIssued[@point='end']">
                          {mcri18n:translate('OMD.ir.docdetails.metadata.label.end_date')} {mods:dateIssued[@point='end']}
                        </xsl:if>
                        </xsl:variable>
                        <xsl:if test="mods:dateIssued[not(@*)] != normalize-space($normalized_date)">
                          <span class="small pl-2">({mcri18n:translate('OMD.ir.docdetails.metadata.label.normalized_date')} {normalize-space($normalized_date)})</span>
                        </xsl:if>
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
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.origin_info_digitization')}</th>
            <td><table id="ir-table-docdetails-origininfo-digitization" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:originInfo[@eventType='digitization']">
                <tr><td>{string-join((mods:place/mods:placeTerm, string-join((mods:publisher, mods:dateCaptured[not(@*)]), ', ')),': ')}</td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
      
        <xsl:if test="./mods:note[@type='statement of responsibility']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.statement_of_responsibility')}</th>
            <td>
              <table id="ir-table-docdetails-statement_of_responsibility" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:note[@type='statement of responsibility']">
                <tr><td>{.}</td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        
        <xsl:if test="mods:note[@type='source_note' or @type='other' or @type='reproduction']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.other_note')}</th>
            <td><table id="ir-table-docdetails-notes" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:note[@type='source_note' or @type='other' or @type='reproduction']">
                <tr><td>{.}</td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:note[@type='bibliographic_reference']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.bibliographic_reference')}</th>
            <td><table id="ir-table-docdetails-biblref" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:note[@type='bibliographic_reference']">
                <tr><td>{.}</td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:note[@type='external_link']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.external_link')}</th>
            <td><table id="ir-table-docdetails-notes-other" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:note[@type='external_link']">
                <tr><td><a href="{./@xlink:href}">{if (string-length(.)=0) then ./@xlink:href else .}</a></td></tr>
              </xsl:for-each>
            </table></td>
          </tr>
        </xsl:if>
        
        <tr><td colspan="2" class="p-0" style="font-size:.5em">&#160;</td></tr>
              
        <xsl:if test="mods:location/mods:physicalLocation[@type='current']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.current_location')}</th>
            <td><table id="ir-table-docdetails-physicalLocation" class="ir-table-docdetails-values">
              <xsl:for-each select="mods:location[mods:physicalLocation[@type='current']]">
                <tr><td>
                  <span class="small">{concat(mods:physicalLocation,':')}</span><br />{mods:shelfLocator}
                </td></tr>
              </xsl:for-each>
           </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:identifier">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.identifier')}</th>
            <td><table id="ir-table-docdetails-identifier" class="ir-table-docdetails-values">
              <xsl:call-template name="identifier_k10plus_ppn" />
              <xsl:call-template name="identifier2metadataTable" />
            </table></td>
          </tr>
        </xsl:if>
        
        <xsl:if test="./mods:relatedItem[@type='isReferencedBy' or @type='references' or @type='otherFormat' or @type='otherVersion']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.references')}</th>
            <td>
              <table id="ir-table-docdetails-references" class="ir-table-docdetails-values">
                <xsl:for-each select="./mods:relatedItem[@type='isReferencedBy' or @type='references' or @type='otherFormat' or @type='otherVersion']">
                  <tr><td>
                    <xsl:if test="./mods:note[@type='relation_label']">
                      <span class="small">{./mods:note[@type='relation_label']}:</span><br />
                    </xsl:if>
                    <xsl:if test="./mods:note[@type='format_type']">
                      {mods:note[@type='format_type']}
                    </xsl:if>
                    {./mods:titleInfo/mods:title}
                    <table>
                      <xsl:for-each select="./mods:identifier[@type='doi']">
                        <tr><th><abbr class="text-nowrap" title="Digital Object Identifier">DOI</abbr>:</th>
                            <td><a href="https://doi.org/{.}">{.}</a></td>
                        </tr>
                      </xsl:for-each>
                      <xsl:for-each select="./mods:recordInfo/mods:recordIdentifier">     
                        <tr><th><abbr class="text-nowrap" title="Persistente URL">PURL</abbr>: </th>
                            <td><a href="http://purl.uni-rostock.de/{.}">http://purl.uni-rostock.de/{.}</a></td>
                        </tr>
                      </xsl:for-each>
                    </table>
                  </td></tr>
                </xsl:for-each>
              </table>
            </td>
          </tr>
        </xsl:if>
      
        <tr><td colspan="2" class="p-0" style="font-size:.5em">&#160;</td></tr>
      
        <xsl:if test="mods:classification[@displayLabel='accesscondition']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.accesscondition')}</th>
            <td><table id="ir-table-docdetails-identifier" class="ir-table-docdetails-values">
              <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[@displayLabel='accesscondition'][1])" />
              <tr><td>
                {$categ/label[@xml:lang=$CurrentLang]/@text}
                <xsl:if test="$categ/label[@xml:lang=$CurrentLang]/@description">
                  <br />{$categ/label[@xml:lang=$CurrentLang]/@description}
                </xsl:if>
              </td></tr>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:classification[contains(@valueURI, 'licenseinfo#work')]">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.licenses')}</th>
            <td><table id="ir-table-docdetails-licenses" class="ir-table-docdetails-values">
              <tr>
                <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#work')])" />
                <td class="text-justify">
                  <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@text" disable-output-escaping="true" /><br />
                  <span>
                    <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@description" disable-output-escaping="true" />
                  </span>
                </td>
              </tr>
            </table></td>
          </tr>
        </xsl:if>
        <xsl:if test="mods:classification[contains(@valueURI, 'licenseinfo#digitisedimages')]">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.licenses.digitisedimages')}</th>
            <!-- TODO use category from metadata.xml -->
            <td class="text-justify">
             <!-- <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@text" disable-output-escaping="true" /><br /> -->
              <xsl:variable name="categ_work" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#work')])" />
              <xsl:variable name="categ_images" select="mcrclass:category('licenseinfo', 'digitisedimages.norestrictions')" />
              <xsl:value-of select="replace($categ_images/label[@xml:lang=$CurrentLang]/@description,'\{0\}', $categ_work/label[@xml:lang=$CurrentLang]/@text)" disable-output-escaping="true" />
            </td>
          </tr>
        </xsl:if>
        
        <tr><td colspan="2" class="p-0" style="font-size:.5em"><hr /></td></tr>

<!-- 
        <tr>
          <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.technical')}</th>
          <td><table id="ir-table-docdetails-technical" class="ir-table-docdetails-values">
          </table></td>
        </tr>
-->        
        <xsl:variable name="categ_mcrid" select="mcrclass:category('identifier', 'mycore_object_id')" />
        <tr><th><abbr title="{$categ_mcrid/label[@xml:lang=$CurrentLang]/@description}">{$categ_mcrid/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
          <td><a href="{$WebApplicationBaseURL}resolve/id/{/mycoreobject/@ID}">{/mycoreobject/@ID}</a></td>
        </tr>
        <tr><th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.date.create_modify')}</th>
          <td>{format-dateTime(/mycoreobject/service/servdates/servdate[@type='createdate'], '[D01].[M01].[Y0001]')} / {format-dateTime(/mycoreobject/service/servdates/servdate[@type='modifydate'], '[D01].[M01].[Y0001]')}
             <!-- {mcri18n:translate('OMD.ir.docdetails.metadata.label.by')} {/mycoreobject/service/servflags/servflag[@type='createdby']} --> </td>
        </tr>
            
        <xsl:if test="mods:classification[contains(@valueURI, 'licenseinfo#metadata')]">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.metadata.label.licenses.metadata')}</th>
            <td class="text-justify">
              <xsl:variable name="categ" select="mcrmods:to-category(mods:classification[contains(@valueURI, 'licenseinfo#metadata')])" />
              <!-- <xsl:value-of select="$categ/label[@xml:lang=$CurrentLang]/@text" disable-output-escaping="true" /><br /> -->
              <xsl:value-of select="replace($categ/label[@xml:lang=$CurrentLang]/@description,'\{0\}', concat($WebApplicationBaseURL,'api/v1/objects/',/mycoreobject/@ID))" disable-output-escaping="true" />
            </td>
          </tr>
      </xsl:if>
      </xsl:for-each>
    </table>
  </xsl:template>
</xsl:stylesheet>