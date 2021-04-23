<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
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

  <xsl:template name="identifier2metadataTable">
   <xsl:for-each select="mods:identifier[not(@type='purl')]">
              <xsl:choose>
                <xsl:when test="@type='uri'">
                    <xsl:variable name="category" select="mcrclass:category('identifier', 'uri')" />
                    <tr><th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                    <td>
                        <a href="{.}">{substring-after(.,':ppn:')}</a>
                    </td></tr>
                </xsl:when>
                <xsl:when test="@type='rism'">
                  <xsl:choose>
                    <xsl:when test="contains(., 'ID no.:')">
                      <xsl:variable name="category" select="mcrclass:category('identifier', 'rism')" />
                      <tr>
                      <th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <xsl:variable name="rismID" select="substring-after(., 'ID no.:')" />
                      <td><a href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',$rismID)}">{$rismID}</a></td>
                      </tr>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:variable name="category" select="mcrclass:category('identifier', 'rism_series')" />
                      <tr>
                      <th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <td>{.}</td>
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="category" select="mcrclass:category('identifier', @type)" />
                  <xsl:if test="$category">
                    <tr>
                      <th><abbr class="text-nowrap" title="{$category/label[@xml:lang=$CurrentLang]/@description}">{$category/label[@xml:lang=$CurrentLang]/@text}</abbr>:</th>
                      <td>
                        <xsl:choose>
                          <xsl:when test="$category/label[@xml:lang='x-portal-url']">
                            <a href="{replace($category/label[@xml:lang='x-portal-url']/@text, '\{0\}',.)}">{.}</a>
                          </xsl:when>
                          <xsl:otherwise>
                            {.}
                          </xsl:otherwise>
                        </xsl:choose>
                      </td>
                    </tr>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="mods:identifier[@type='purl']">
              <xsl:variable name="categ_purl" select="mcrclass:category('identifier', 'purl')" />
              <tr><th><abbr class="text-nowrap" title="{$categ_purl/label[@xml:lang=$CurrentLang]/@description}">{$categ_purl/label[@xml:lang=$CurrentLang]/@text}</abbr>: </th>
                  <td><a href="{.}">{.}</a></td>
              </tr>
            </xsl:for-each>
    
  </xsl:template>
</xsl:stylesheet>
