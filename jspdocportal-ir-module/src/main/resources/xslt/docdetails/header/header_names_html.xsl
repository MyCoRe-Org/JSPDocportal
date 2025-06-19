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
 
  <xsl:import href="resource:xslt/functions/i18n.xsl" />
  <xsl:import href="resource:xslt/functions/acl.xsl" />
  <xsl:import href="resource:xslt/functions/stringutils.xsl" />
  <xsl:import href="resource:xslt/functions/classification.xsl" />
  <xsl:import href="resource:xslt/functions/mods.xsl" />
  
  <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template name="headerNames">
    <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
      <template id="tmpl_ir_popover_body_{generate-id(.)}">
        <xsl:if test="./mods:role/mods:roleTerm[@authority='GBV']">
          <strong>{./mods:role/mods:roleTerm[@authority='GBV']}</strong>
        </xsl:if>
        <table class="w-100" style="min-width:15em">
          <colgroup>
            <col style="width: 15%" />
            <col style="width: 85%" />
          </colgroup>
           
          <xsl:if test="./mods:nameIdentifier[@type='orcid']">
            <tr>
              <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/ORCIDiD_iconvector.svg"  style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.orcid')}" /></th>
              <td><a href="https://orcid.org/{./mods:nameIdentifier[@type='orcid']}">{./mods:nameIdentifier[@type='orcid']}</a></td>
            </tr>
          </xsl:if>
          <xsl:if test="./mods:nameIdentifier[@type='gnd']">
            <tr>
              <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Wabe.png" style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
              <td><a href="https://explore.gnd.network/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
            </tr>
          </xsl:if>
          <xsl:for-each select="./mods:affiliation">
            <tr>
              <th class="text-center align-text-top"><i class="fas fa-university" title="{mcri18n:translate('OMD.ir.docdetails.common.label.affiliation')}" style="font-size:1.5em"></i></th>
              <td>{.}</td>
            </tr>
          </xsl:for-each>
        </table>
      </template>
    </xsl:for-each>
    <p>
      <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <nobr>
          {string-join((./mods:namePart[@type='given'], ./mods:namePart[@type='family'], ./mods:namePart[not(@type)], ./mods:namePart[@type='termsOfAddress']),' ')}
          <button id="btn_ir_click_popover_{generate-id(.)}" class="btn btn-sm ps-1" type="button">
            <xsl:attribute name="data-bs-toggle">popover</xsl:attribute>
            <xsl:attribute name="data-bs-placement">bottom</xsl:attribute>
            <xsl:attribute name="data-ir-popover-body-template">#tmpl_ir_popover_body_{generate-id(.)}</xsl:attribute>
            <i class="fas fa-user-circle"></i>
          </button>
        </nobr>
      
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
    
    <!-- Körperschaften als Herausgeber -->
    <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
      <template id="tmpl_ir_popover_body_{generate-id(.)}">
        <div style="min-width:100em">
          <xsl:if test="./mods:role/mods:roleTerm[@authority='GBV']">
            <strong>{./mods:role/mods:roleTerm[@authority='GBV']}</strong>
          </xsl:if>
          <table class="w-100" style="min-width:15em">
            <colgroup>
              <col style="width: 15%" />
              <col style="width: 85%" />
            </colgroup>
            <xsl:if test="./mods:nameIdentifier[@type='gnd']">
              <tr>
                <th class="text-center"><img src="{$WebApplicationBaseURL}images/ir/GND_RGB_Wabe.png" style="height:1.5em" title="{mcri18n:translate('OMD.ir.docdetails.common.label.gnd')}" /></th>
                <td><a href="https://explore.gnd.network/gnd/{./mods:nameIdentifier[@type='gnd']}">{./mods:nameIdentifier[@type='gnd']}</a></td>
              </tr>
            </xsl:if>
          </table>
        </div>
      </template>
    </xsl:for-each>
    <p>
      <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <nobr>
          {string-join((./mods:namePart[not(@type)]),' ')}
          <button id="btn_ir_click_popover_{generate-id(.)}" class="btn btn-sm ps-1" type="button">
            <xsl:attribute name="data-bs-toggle">popover</xsl:attribute>
            <xsl:attribute name="data-bs-placement">bottom</xsl:attribute>
            <xsl:attribute name="data-ir-popover-body-template">#tmpl_ir_popover_body_{generate-id(.)}</xsl:attribute>
            <i class="fas fa-university"></i>
          </button>
        </nobr>
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
  </xsl:template>
  
  <xsl:template name="headerNames_noPopup">
    <p>
      <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <span style="white-space:nowrap;">
          {string-join((./mods:namePart[@type='given'], ./mods:namePart[@type='family'], ./mods:namePart[not(@type)], ./mods:namePart[@type='termsOfAddress']),' ')}
          <button class="btn btn-sm ps-1" type="button">
            <i class="fas fa-user-circle"></i>
          </button>
        </span>
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
    
    <!-- Körperschaften als Herausgeber -->
    <p>
      <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <span style="white-space:nowrap;">
          {string-join((./mods:namePart[not(@type)]),' ')}
          <button class="btn btn-sm ps-1" type="button">
            <i class="fas fa-university"></i>
          </button>
        </span>
        <xsl:if test="not(position() = last())">&#160;&#160;&#160;</xsl:if>
      </xsl:for-each>
    </p>
  </xsl:template>
  
</xsl:stylesheet>
