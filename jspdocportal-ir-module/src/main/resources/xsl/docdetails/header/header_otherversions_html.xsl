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
  
  <xsl:output method="xhtml" indent="yes" standalone="omit" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"></xsl:param>
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <xsl:template name="otherVersions">
    <!-- andere Versionen -->
    <xsl:if test="./mods:relatedItem[@type='otherVersion' or @type='otherFormat']">
       <div class="float-right">
       <p class="mt-3">
            <xsl:for-each select="./mods:relatedItem[@type='otherVersion' or @type='otherFormat']">
            <xsl:element name="a">
              <xsl:attribute name="class">btn btn-sm btn-outline-secondary</xsl:attribute>
              <xsl:choose>
                <xsl:when test="./mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
                  <xsl:attribute name="href" select="concat($WebApplicationBaseURL, 'resolve/recordIdentifier/', replace(./mods:recordInfo/mods:recordIdentifier[@source='DE-28'],'/', '%252F'))" />   
                </xsl:when>
                <xsl:when test="./mods:identifier[@type='doi']">
                  <xsl:attribute name="href" select="concat('https://doi.org/', ./mods:identifier[@type='doi'])" />   
                </xsl:when>
                <xsl:when test="./mods:identifier[@type='purl']">
                  <xsl:attribute name="href" select="./mods:identifier[@type='purl']" />   
                </xsl:when>
              </xsl:choose>
              <xsl:attribute name="data-toggle">popover</xsl:attribute>
              <xsl:attribute name="data-placement">bottom</xsl:attribute>
              <xsl:attribute name="data-html">true</xsl:attribute>
              <xsl:attribute name="data-content">
                <xsl:value-of select="concat(./mods:note[@type='relation_label'], ': &lt;br/&gt;')" />
                &lt;strong&gt;
                <xsl:choose>
                  <xsl:when test="./mods:titleInfo">
                   {string-join((./mods:titleInfo, ./mods:subTitle), ': ')}
                    </xsl:when>
                    <xsl:when test="./mods:identifier[@type='doi']">
                      &lt;a href=&apos;{concat("https://doi.org/", ./mods:identifier[@type="doi"])}&apos;&gt;
                      {concat('https://doi.org/', ./mods:identifier[@type='doi'])}&lt;/a&gt;
                    </xsl:when>
                </xsl:choose>
                &lt;/strong&gt;
                <xsl:if test="./mods:note[@type='format_type']">
                  &lt;br/&gt;({./mods:note[@type='format_type']})
                </xsl:if>
              </xsl:attribute>
              <i class="fas fa-external-link-alt"></i>
         
            </xsl:element>
            &#160;
          </xsl:for-each>            
        </p>
        </div>
     </xsl:if>
  </xsl:template>
  
    <xsl:template name="preceding_succeeding_buttons">
    <!-- Vorgänger, Nachfolger -->
    <xsl:if test="./mods:relatedItem[@type='preceding' or @type='succeeding']">
      <div class="float-right">
       <p class="mt-3">

          <xsl:for-each select="./mods:relatedItem[@type='preceding' or @type='succeeding']">
            <xsl:element name="a">
              <xsl:attribute name="class">btn btn-sm btn-outline-secondary ml-3</xsl:attribute>
              <xsl:choose>
                <xsl:when test="./mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
                  <xsl:attribute name="href" select="concat($WebApplicationBaseURL, 'resolve/recordIdentifier/', replace(./mods:recordInfo/mods:recordIdentifier[@source='DE-28'],'/', '%252F'))" />   
                </xsl:when>
                <xsl:when test="./mods:identifier[@type='doi']">
                  <xsl:attribute name="href" select="concat('https://doi.org/', ./mods:identifier[@type='doi'])" />   
                </xsl:when>
                <xsl:when test="./mods:identifier[@type='purl']">
                  <xsl:attribute name="href" select="./mods:identifier[@type='purl']" />   
                </xsl:when>
              </xsl:choose>
              <xsl:attribute name="data-toggle">popover</xsl:attribute>
              <xsl:attribute name="data-placement">bottom</xsl:attribute>
              <xsl:attribute name="data-html">true</xsl:attribute>
              <xsl:attribute name="data-content">
                <xsl:value-of select="concat(./mods:note[@type='relation_label'], ': &lt;br/&gt;')" />
                &lt;strong&gt;
                <xsl:choose>
                  <xsl:when test="./mods:titleInfo">
                   {string-join((./mods:titleInfo, ./mods:subTitle), ': ')}
                    </xsl:when>
                    <xsl:when test="./mods:identifier[@type='doi']">
                      &lt;a href=&apos;{concat("https://doi.org/", ./mods:identifier[@type="doi"])}&apos;&gt;
                      {concat('https://doi.org/', ./mods:identifier[@type='doi'])}&lt;/a&gt;
                    </xsl:when>
                </xsl:choose>
                &lt;/strong&gt;
                <xsl:if test="./mods:note[@type='format_type']">
                  &lt;br/&gt;({./mods:note[@type='format_type']})
                </xsl:if>
              </xsl:attribute>
              
              <xsl:choose>
                <xsl:when test="./@type='preceding'">
                  <i class="fas fa-step-backward"></i>   
                </xsl:when>
                <xsl:when test="./@type='succeeding'">
                  <i class="fas fa-step-forward"></i>
                </xsl:when>
              </xsl:choose>
            </xsl:element>
            
          </xsl:for-each>
          </p>            
        </div>
     </xsl:if>
  </xsl:template>
  
  
</xsl:stylesheet>
