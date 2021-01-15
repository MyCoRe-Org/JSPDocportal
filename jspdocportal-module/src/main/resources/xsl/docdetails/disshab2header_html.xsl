<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:mcrmods="xalan://org.mycore.mods.classification.MCRMODSClassificationSupport" exclude-result-prefixes="mods mcrmods">

    <!-- to enable relative urls in import set xsltSystemId attribute in x:transform of JSP XML Tag Library !!! -->
	<xsl:import href="mods-util.xsl" />
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

	<xsl:param name="WebApplicationBaseURL"></xsl:param>

	<xsl:template match="/">
		<xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
  		  <p>
            <xsl:for-each select="./mods:name[@type='personal'][position()=1 or mods:role/mods:roleTerm[@type='code']/@valueURI='http://id.loc.gov/vocabulary/relators/aut' or  mods:role/mods:roleTerm='aut']">
              <xsl:call-template name="display-name">
                <xsl:with-param name="name" select="." />
              </xsl:call-template>
            </xsl:for-each>
          </p>
		  
		  <xsl:call-template name="mods-title" />
          
                <p>
					<xsl:call-template name="mods-originInfo" />
				</p>

				<xsl:if test="./mods:identifier[@type='doi']">
					<p>
						<xsl:element name="a">
							<xsl:attribute name="href">https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" /></xsl:attribute>
							https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" />
						</xsl:element>
					</p>
				</xsl:if>
				<xsl:if test="./mods:identifier[@type='purl']">
					<p>
						<xsl:element name="a">
							<xsl:attribute name="href"><xsl:value-of select="./mods:identifier[@type='purl']" /></xsl:attribute>
							<xsl:value-of select="./mods:identifier[@type='purl']" />
						</xsl:element>
					</p>
				</xsl:if>
                <xsl:if test="./mods:abstract">
                  <h5 style="margin-bottom: .25em">Abstract:</h5>
                  <p class="ir-docdetails-abstract">
                    <xsl:value-of select="./mods:abstract" />
                  </p>
                </xsl:if>
  			  <xsl:if test="./mods:classification[@displayLabel='doctype']">
            <p>
            <span class="label label-default ir-label-default">
              <xsl:for-each select="./mods:classification[@displayLabel='doctype']/@valueURI">
                <xsl:call-template name="classLabel">
                  <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
                </xsl:call-template>
              </xsl:for-each>
            </span>
            <xsl:call-template name="accessLabel" />
            </p>
          </xsl:if>
		</xsl:for-each>

	</xsl:template>

</xsl:stylesheet>