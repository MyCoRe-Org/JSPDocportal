<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcracl="http://www.mycore.de/xslt/acl"
  exclude-result-prefixes="mods xlink mcri18n mcracl">
 
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="resource:xsl/functions/acl.xsl" />
  <xsl:output method="html" indent="yes" standalone="no" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>

  <xsl:template match="/mycoreobject">
  <!-- ID reservation header -->
  <xsl:if test="mcracl:check-permission(@ID, 'writedb')">
    <xsl:if test="./service/servstates/servstate[@categid='reserved']">
      <div class="card card-info border border-info mb-3">
        <div class="card-header bg-info">
          <h4 class="text-white">ID Reservierung</h4>
        </div>
        <div class="card-body">
          <xsl:for-each select="./metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods">
            <xsl:if test="./mods:titleInfo/mods:title">
              <h2>
                <xsl:value-of select="./mods:titleInfo/mods:title" />
              </h2>
            </xsl:if>
            <xsl:if test="./mods:note">
              <p class="card-text">
                <xsl:value-of select="./mods:note" />
              </p>
            </xsl:if>
          </xsl:for-each>
        </div>
      </div>
    </xsl:if>
  </xsl:if>
    
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer[@type='imported']/mods:mods">
         <xsl:if test="./mods:titleInfo/mods:title">
              <h2><xsl:value-of select="./mods:titleInfo/mods:title" /></h2>
         </xsl:if>
    <!-- 
      <xsl:for-each select="./mods:relatedItem[@type='host' or @type='series']/mods:recordInfo">
           <xsl:element name="a">
              <xsl:attribute name="class">btn btn-default btn-sm pull-right ir-docdetails-btn-goto-parent</xsl:attribute>
              <!- temporary FIX:  ->
              	<!- <xsl:attribute name="href"><xsl:value-of select="$WebApplicationBaseURL" />resolve/recordIdentifier/<xsl:value-of select="substring-before(./mods:recordIdentifier, '/')"/>_<xsl:value-of select="substring-after(./mods:recordIdentifier, '/')"/></xsl:attribute> ->
              <xsl:if test="contains(./mods:recordIdentifier, '/')">
              	<xsl:attribute name="href"><xsl:value-of select="$WebApplicationBaseURL" />resolve/recordIdentifier/<xsl:value-of select="substring-before(./mods:recordIdentifier, '/')"/>_<xsl:value-of select="substring-after(./mods:recordIdentifier, '/')"/></xsl:attribute>
              </xsl:if>
              <!- temporary FIX:  ->
              <xsl:if test="contains(./mods:recordIdentifier, '_')">
              	<xsl:attribute name="href"><xsl:value-of select="$WebApplicationBaseURL" />resolve/id/<xsl:value-of select="./mods:recordIdentifier" /></xsl:attribute>
              </xsl:if>
              <xsl:attribute name="title"><xsl:value-of select="../mods:titleInfo/mods:title" /></xsl:attribute>
              <xsl:value-of select="mcri18n:translate('Webpage.docdetails.gotoParent')" />
              <xsl:text disable-output-escaping="yes">&amp;#160;&amp;#160;&lt;i class=&quot;fa fa-arrow-up&quot;&gt;&lt;/i&gt;</xsl:text>
           </xsl:element>
      </xsl:for-each> 
      <p>
        <xsl:call-template name="mods-name" /><br />
      </p>
      
     <xsl:call-template name="mods-title" />
     <xsl:if test="./mods:relatedItem[@displayLabel='appears_in']/mods:titleInfo">
     	<xsl:value-of select="mcri18n:translate('Webpage.docdetails.appearsIn')" /><xsl:text> </xsl:text>
     	<xsl:value-of select="./mods:relatedItem[@displayLabel='appears_in']/mods:titleInfo/*" />
     </xsl:if>

      <p>
      <xsl:call-template name="mods-originInfo" />
      </p>
      <xsl:choose>
       <xsl:when test="./mods:identifier[@type='doi']">
        <p><xsl:element name="a">
            <xsl:attribute name="href">https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" /></xsl:attribute>
             https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" />
          </xsl:element>
         </p>
        </xsl:when>
       <xsl:when test="./mods:identifier[@type='purl']">
        <p>
            <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="./mods:identifier[@type='purl']" /></xsl:attribute>
             <xsl:value-of select="./mods:identifier[@type='purl']" />
          </xsl:element>
         </p>
        </xsl:when>
        </xsl:choose>
        <xsl:if test="./mods:abstract">
        <h5 style="margin-bottom: .25em">Abstract:</h5>
        <p class="small ir-docdetails-abstract">
          <xsl:value-of select="./mods:abstract" />
       </p>       
       </xsl:if>
       <p>
        <xsl:if test="./mods:classification[@displayLabel='doctype']">

        <span class="badge badge-secondary">
          <xsl:for-each select="./mods:classification[@displayLabel='doctype']/@valueURI">
            <xsl:call-template name="classLabel">
              <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
            </xsl:call-template>
          </xsl:for-each>
        </span>
       
      </xsl:if>
      <xsl:call-template name="accessLabel" />
      </p>
       <xsl:if test="./mods:relatedItem[@type='otherVersion']">
       		<p style="margin-top:2em">
       			<xsl:value-of select="mcri18n:translate('Webpage.docdetails.header.otherVersions')" />: 
       			<xsl:for-each select="./mods:relatedItem[@type='otherVersion']">
       				<xsl:element name="a">
       					<xsl:attribute name="href"><xsl:value-of select="./mods:identifier[@type='purl']" /></xsl:attribute>
             			<xsl:value-of select="./mods:note" />
       				</xsl:element>
       				<span> </span>
       			</xsl:for-each>
       		</p>
       </xsl:if>
       -->
    </xsl:for-each>
    
  </xsl:template>

</xsl:stylesheet>