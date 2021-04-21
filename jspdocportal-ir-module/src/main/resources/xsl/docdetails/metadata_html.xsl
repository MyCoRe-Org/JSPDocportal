<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ubr-researchdata="http://purl.uni-rostock.de/ub/standards/ubr-researchdata-information-v1.0"
  xmlns:ubr-legal="http://purl.uni-rostock.de/ub/standards/ubr-legal-information-v1.0"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  version="3.0" exclude-result-prefixes="mods xlink" expand-text="true">
  
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  
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
              <xsl:choose>
                <xsl:when test="mods:partName or mods:partNumber">
                  <tr><td>
                    <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
                  </td></tr>
                  <tr><td><strong>
                    <xsl:value-of select="string-join((mods:partNumber, mods:partName),' ')" />
                  </strong></td></tr>
                </xsl:when>
                <xsl:otherwise>
                  <tr><td><strong>
                    <xsl:value-of select="string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')" />
                  </strong></td></tr>
                </xsl:otherwise> 
              </xsl:choose>
            </xsl:for-each>
          </table></td>
        </tr>
        <xsl:if test="mods:name[@type='personal']">
          <tr>
            <th>Beteiligte Personen:</th>
            <td><table id="ir-table-docdetails-name_personal" class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:name[@type='personal']">
                <xsl:choose>
                  <xsl:when test="mods:role/mods:roleTerm[@authority='GBV']">
                    <tr><td colspan="2">[{string-join(mods:role/mods:roleTerm[@authority='GBV'], ', ')}]</td></tr>
                  </xsl:when>
                  <xsl:when test="mods:role/mods:roleTerm[@authority='marcrelator']">
                    <tr><td colspan="2">[{mcrclass:current-label-text(document(concat('classification:metadata:0:children:marcrelator:',mods:role/mods:roleTerm[@authority='marcrelator']))//category)}]</td></tr>                 
                  </xsl:when>
                </xsl:choose>
                      
                <xsl:if test="mods:namePart[@type='given' or @type='family']">
                  <tr><td colspan="2">
                    <strong><xsl:value-of select="string-join((mods:namePart[@type='given'], mods:namePart[@type='family']),' ')" /></strong>
                  </td></tr>
                </xsl:if>
                <xsl:if test="mods:namePart[not(@type)]">
                  <tr><td colspan="2">
                    <strong><xsl:value-of select="string-join(mods:namePart[not(@type)],' ')" /></strong>
                  </td></tr>
                </xsl:if>
                <xsl:if test="mods:namePart[@type='termsOfAddress']">
                  <tr><td colspan="2">
                    <xsl:value-of select="string-join(mods:namePart[@type='termsOfAddress'], ', ')" />
                  </td></tr>
                 </xsl:if>
                 <xsl:if test="./mods:nameIdentifier[@type='orcid']">
                   <tr>
                     <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconbwvector.svg"  style="height:1.5em" title="ORCID (Open Researcher and Contributor ID)" /></th>
                     <td><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
                   </tr>
                 </xsl:if>
                 <xsl:if test="./mods:nameIdentifier[@type='gnd']">
                   <tr>
                     <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Black_wabe.png" style="height:1.5em" title="GND (Gemeinsame Normdatei der Deutschen Nationalbiblitohek)" /></th>
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
      
      <!--       
      <xsl:if test="./mods:identifier[@type='local']">
          <tr>
            <th>Lokaler Identifikator:</th>
            <td><table class="ir-table-docdetails-values"><tr><td>
               <xsl:value-of select="./mods:identifier[@type='local']" />
            </td></tr></table></td>
          </tr>
        </xsl:if>
        
         <xsl:if test="./mods:name[@type='personal'][position()>1 and not(contains('aut edt cre', ./mods:role/mods:roleTerm[@type='code']/text()))]">
          <tr>
            <th>weitere Beteiligte:</th>
            <td>
              <table class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:name[@type='personal'][position()>1 and not(contains('aut edt', ./mods:role/mods:roleTerm[@type='code']/text()))]">
                  <tr><td>
                  <xsl:call-template name="display-name">
                    <xsl:with-param name="name" select="." />
                  </xsl:call-template>
                  <xsl:if test="position()!=last()">
                    <xsl:text>&#160;;&#160;&#160;</xsl:text>
                  </xsl:if>
                  </td></tr>
              </xsl:for-each>
              </table>
            </td>
          </tr>
        
        </xsl:if>
     
        <xsl:if test="./mods:extension/ubr-researchdata:researchDataInformation/ubr-researchdata:researchDataType">
          <tr>
            <th>Typ:</th>
            <td>
              <table class="ir-table-docdetails-values"><tr><td>
                 <xsl:value-of select="./mods:extension/ubr-researchdata:researchDataInformation/ubr-researchdata:researchDataType" />
               </td></tr></table>
            </td>
          </tr>
        </xsl:if>
        
       <xsl:if test="./mods:subject/mods:topic">
          <tr>
            <th class="docdetails-label">Schlagworte:</th>
            <td>
              <table class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:subject/mods:topic">
                  <tr><td>
                  <xsl:value-of select="./text()" />
                  </td></tr>
              </xsl:for-each>
              </table>
            </td>
          </tr>
        </xsl:if>
        
          <xsl:if test="./mods:relatedItem[@type='isReferencedBy']">
          <tr>
            <th>Publikationen:</th>
            <td>
              <table class="ir-table-docdetails-values">
             <xsl:for-each select="./mods:relatedItem[@type='isReferencedBy']">
             <tr><td>
                <xsl:value-of select="./mods:note" />
                <xsl:choose>
                  <xsl:when test="./mods:identifier[@type='urn']">
                       <br />URN: <xsl:element name="a">
                            <xsl:attribute name="href">http://nbn-resolving.org/<xsl:value-of select="./mods:identifier[@type='urn']" /></xsl:attribute> 
                            <xsl:value-of select="./mods:identifier[@type='urn']" />
                           </xsl:element> 
                  </xsl:when>
                  <xsl:when test="./mods:identifier[@type='doi']">
                       <br />DOI:<xsl:element name="a">
                            <xsl:attribute name="href">https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" /></xsl:attribute> 
                            https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" />
                           </xsl:element> 
                  </xsl:when>
                  <xsl:when test="./mods:identifier[@type='url' or @type='purl']">
                      <br />URL: <xsl:element name="a">
                            <xsl:attribute name="href"><xsl:value-of select="./mods:identifier[@type='url' or @type='purl']" /></xsl:attribute> 
                            <xsl:value-of select="./mods:identifier[@type='url' or @type='purl']" />
                           </xsl:element>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="./mods:identifier" />
                  </xsl:otherwise>
                </xsl:choose>
                </td></tr>
              </xsl:for-each>
              </table>
            </td>
          </tr>
        </xsl:if>
        </table>
      
      <table class="ir-table-docdetails">      
      <xsl:if test="./mods:classification[@displayLabel='doctype']">
          <tr>
            <th>Dokumenttyp:</th>
            <td>
              <table class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:classification[@displayLabel='doctype']/@valueURI">
              <tr><td>
               <xsl:call-template name="classLabel">
                  <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
               </xsl:call-template>
               </td></tr>
               </xsl:for-each>
               </table>
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="./mods:language/mods:languageTerm">
          <tr>
            <th>Sprache(n):</th>
             <td>
              <table class="ir-table-docdetails-values"><tr><td>
             <xsl:call-template name="language">
                <xsl:with-param name="term"><xsl:value-of select="./mods:language/mods:languageTerm" /></xsl:with-param>
                <xsl:with-param name="lang">de</xsl:with-param>
              </xsl:call-template>
              </td></tr></table>
            </td>
          </tr>
        </xsl:if>
        
        <xsl:if test="./mods:classification[@displayLabel='sdnb']">
          <tr>
            <th>DNB-Sachgruppe:</th>
             <td>
              <table class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:classification[@displayLabel='sdnb']/@valueURI">
              <tr><td>
               <xsl:call-template name="classLabel">
                  <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
               </xsl:call-template>
               </td></tr>
               </xsl:for-each>
               </table>
            </td>
          </tr>
        </xsl:if>
        
          <xsl:if test="./mods:classification[@displayLabel='institution']">
          <tr>
            <th>Fakultät:</th>
            <td>
              <table class="ir-table-docdetails-values">
              <xsl:for-each select="./mods:classification[@displayLabel='institution']/@valueURI">
              <tr><td>
               <xsl:call-template name="classLabel">
                  <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
               </xsl:call-template>
               </td></tr>
               </xsl:for-each>
               </table>
            </td>
          </tr>
        </xsl:if>
   
   
   -->
      
    </xsl:for-each>
    
    </table>
  </xsl:template>
</xsl:stylesheet>