<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="3.0"
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:xlink="http://www.w3.org/1999/xlink"
                 xmlns:mods="http://www.loc.gov/mods/v3">
  <xsl:mode on-no-match="shallow-copy" />
  <xsl:include href="resource:xslt/docdetails/mods-util.xsl" />
  <xsl:param name="WebApplicationBaseURL" />
  <!-- create value URI using valueURIxEditor and authorityURI -->
  <xsl:template match="@valueURIxEditor">
    <xsl:attribute name="valueURI">
          <xsl:value-of select="concat(../@authorityURI,'#',.)" />
        </xsl:attribute>
  </xsl:template>
  <xsl:template match="mods:classification[@displayLabel='institution']">
     <xsl:copy>
       <xsl:apply-templates select="node()|@*"/>
     </xsl:copy>
     <xsl:if test="contains(/mycoreobject/@ID, '_disshab_') or contains(/mycoreobject/@ID, '_thesis_')">
      	<xsl:if test="contains(@valueURI, 'unirostock.')">
       		<mods:name type="corporate" xlink:type="simple">
       	 		<mods:nameIdentifier type="gnd">38329-6</mods:nameIdentifier>
	         	<mods:namePart>Universität Rostock</mods:namePart>
    		     <mods:role>
          			<mods:roleTerm type="code" authority="marcrelator">dgg</mods:roleTerm>
          			<mods:roleTerm authority="GBV" type="text">Grad-verleihende Institution</mods:roleTerm>
         		</mods:role>
       		</mods:name>
       	</xsl:if>

       <xsl:variable name="gndURI">
         <xsl:call-template name="classLabel">
           <xsl:with-param name="valueURI"><xsl:value-of select="concat(./@authorityURI,'#',./@valueURIxEditor)" /></xsl:with-param>
           <xsl:with-param name="lang">x-uri</xsl:with-param>
         </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="fullname">
         <xsl:call-template name="classLabel">
           <xsl:with-param name="valueURI"><xsl:value-of select="concat(./@authorityURI,'#',./@valueURIxEditor)" /></xsl:with-param>
           <xsl:with-param name="lang">x-de-full</xsl:with-param>
         </xsl:call-template>
       </xsl:variable>
       <mods:name type="corporate" xlink:type="simple">
       	<mods:nameIdentifier type="gnd"><xsl:value-of select="substring($gndURI, 22)" /></mods:nameIdentifier>
        <mods:namePart><xsl:value-of select="$fullname" /></mods:namePart>
        <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">dgg</mods:roleTerm>
          <mods:roleTerm authority="GBV" type="text">Grad-verleihende Institution</mods:roleTerm>
          
          </mods:role>
       </mods:name> 
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>