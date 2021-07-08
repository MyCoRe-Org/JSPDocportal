<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
  xmlns:mcrclass="http://www.mycore.de/xslt/classification"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  exclude-result-prefixes="mods xlink mcri18n mcracl mcrstring mcrclass mcrmods"
  expand-text="yes">
 
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/acl.xsl" />
  <xsl:import href="resource:xsl/functions/stringutils.xsl" />
  <xsl:import href="resource:xsl/functions/classification.xsl" />
  <xsl:import href="resource:xsl/functions/mods.xsl" />
  
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template name="headerNames">
    <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
      <div id="popover_content_{generate-id(.)}" style="display: none">
        <div>
          <table class="w-100" style="min-width:15em">
            <colgroup>
              <col span="1" style="width: 15%" />
              <col span="1" style="width: 85%;" />
            </colgroup>
            <xsl:if test="./mods:role/mods:roleTerm[@authority='GBV']">
              <tr>
                <!-- <th class="text-center"><i class="fas fa-portrait" title="Rolle" style="font-size:1.5em"></i></th> -->
                <td colspan="2">
                  <button type="button" id="close_popover_content_{generate-id(.)}" class="close float-right" aria-label="Close">
                    <span aria-hidden="true">&#215;</span>
                  </button>
                  <strong>{./mods:role/mods:roleTerm[@authority='GBV']}</strong>
                
                </td>
              </tr>
            </xsl:if>
            <xsl:if test="./mods:nameIdentifier[@type='orcid']">
              <tr>
                <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconvector.svg"  style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.orcid')}" /></th>
                <td><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
              </tr>
            </xsl:if>
            <xsl:if test="./mods:nameIdentifier[@type='gnd']">
              <tr>
                <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Wabe.png" style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
                <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
              </tr>
            </xsl:if>
            <xsl:for-each select="./mods:affiliation">
              <tr>
                <th class="text-center align-text-top"><i class="fas fa-university" title="{mcri18n:translate('OMD.ir.docdetails.common.label.affiliation')}" style="font-size:1.5em"></i></th>
                <td>{.}</td>
              </tr>
            </xsl:for-each>
          </table>
        </div>
      </div>
    </xsl:for-each>
    <p>
      <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <nobr>
          {string-join((./mods:namePart[@type='given'], ./mods:namePart[@type='family'], ./mods:namePart[not(@type)], ./mods:namePart[@type='termsOfAddress']),' ')}
          <button class="btn btn-sm pl-1" type="button">
            <xsl:attribute name="data-toggle">popover</xsl:attribute>
            <xsl:attribute name="data-placement">bottom</xsl:attribute>
            <xsl:attribute name="data-html">true</xsl:attribute>
            <xsl:attribute name="data-content-ref">#popover_content_{generate-id(.)}</xsl:attribute>
            <i class="fas fa-user-circle"></i>
          </button>
        </nobr>
      
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
    
    <!-- Körperschaften als Herausgeber -->
    <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
      <div id="popover_content_{generate-id(.)}" style="display: none;">
        <div style="min-width:100em">
          <table class="w-100" style="min-width:15em">
            <colgroup>
              <col span="1" style="width: 15%" />
              <col span="1" style="width: 85%;" />
            </colgroup>
            <xsl:if test="./mods:role/mods:roleTerm[@authority='GBV']">
              <tr>
                <!-- <th class="text-center"><i class="fas fa-portrait" title="Rolle" style="font-size:1.5em"></i></th> -->
                <td colspan="2">
                  <button type="button" id="close_popover_content_{generate-id(.)}" class="close float-right" aria-label="Close">
                    <span aria-hidden="true">&#215;</span>
                  </button>
                  <strong>{./mods:role/mods:roleTerm[@authority='GBV']}</strong>
                </td>
              </tr>
            </xsl:if>
            <xsl:if test="./mods:nameIdentifier[@type='gnd']">
              <tr>
                <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Wabe.png" style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
                <td><a href="http://d-nb.info/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
              </tr>
            </xsl:if>
          </table>
        </div>
      </div>
    </xsl:for-each>
    <p>
      <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <nobr>
          {string-join((./mods:namePart[not(@type)]),' ')}
          <button class="btn btn-sm pl-1" type="button">
            <xsl:attribute name="data-toggle">popover</xsl:attribute>
            <xsl:attribute name="data-placement">bottom</xsl:attribute>
            <xsl:attribute name="data-html">true</xsl:attribute>
            <xsl:attribute name="data-content-ref">#popover_content_{generate-id(.)}</xsl:attribute>
            <i class="fas fa-university"></i>
          </button>
        </nobr>
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
  </xsl:template>
</xsl:stylesheet>
