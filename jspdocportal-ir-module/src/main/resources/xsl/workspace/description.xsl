<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  exclude-result-prefixes="mods xlink mcri18n mcracl"
  expand-text="yes">
 
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/acl.xsl" />
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="MCR.DOI.Prefix"></xsl:param>
  <xsl:param name="MCR.Identifier.PURL.BaseURL"></xsl:param>

 <xsl:template match="/mycoreobject">
  <xsl:if test="./metadata/def.modsContainer/modsContainer[@type='reserved']">
    <span class="badge badge-info mr-3">ID-Reservierung</span><strong><xsl:value-of select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods/mods:note[@type='provisional_title']" /></strong>
    <pre><xsl:value-of select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods/mods:note[@type='provisional_remarks']" /></pre>
  </xsl:if>
  <xsl:for-each select="//mods:name[./mods:role/mods:roleTerm[@type='code'][@authority='marcrelator']='aut'][1]">
    <p>
      <xsl:value-of select="concat(./mods:namePart[@type='given'],' ',./mods:namePart[@type='family'])" />
    </p>
  </xsl:for-each>
  <p class="small">
     <span class="d-inline-block" style="width:4em">URN: </span>
     <input id="input_urn_{./@ID}" readonly="readonly" style="width:22em;margin-bottom:5px" value="{//mods:mods/mods:identifier[@type='urn']}" />
     <button class="btn btn-sm ml-1" title="URN kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_urn_{./@ID}').value);"><i class="fas fa-clipboard"></i></button>
     <br />
     <span class="d-inline-block" style="width:4em">DOI: </span>
     <input id="input_doi_{./@ID}" readonly="readonly" style="width:22em;margin-bottom:5px" value="https://doi.org/{$MCR.DOI.Prefix}/{translate(//mods:mods/mods:recordInfo/mods:recordIdentifier,'/','_')}" />
     <button class="btn btn-sm ml-1" title="DOI kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_doi_{./@ID}').value);"><i class="fas fa-clipboard"></i></button>
     <br />
     <span class="d-inline-block" style="width:4em">PURL: </span>
     <input id="input_purl_{./@ID}" readonly="readonly" style="width:22em" value="{$MCR.Identifier.PURL.BaseURL}{//mods:mods/mods:recordInfo/mods:recordIdentifier}" />
     <button class="btn btn-sm ml-1" title="PURL kopieren" onclick="navigator.clipboard.writeText(document.getElementById('input_purl_{./@ID}').value);"><i class="fas fa-clipboard"></i></button>
  </p>
  </xsl:template>

</xsl:stylesheet>