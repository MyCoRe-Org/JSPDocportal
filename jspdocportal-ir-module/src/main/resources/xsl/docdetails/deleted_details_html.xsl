<?xml version="1.0"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="3.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"  
  exclude-result-prefixes="mods xlink mcri18n mods"
  expand-text="yes">

  <xsl:import href="mods-util.xsl" />
  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/">
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
      <table class="table ir-table-docdetails">
      <xsl:if test="./mods:titleInfo">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.deleted_details.titleinfo')}</th>
            <td>
               <table class="ir-table-docdetails-values">
                  <tr>
                    <td>
                        <!-- see template "mods-title" in mods-util.xsl  -->
                        <xsl:for-each select="./mods:titleInfo[@usage='primary']">
                        <p>
                          <strong>
                            <xsl:if test="./mods:nonSort">
                              <xsl:value-of select="./mods:nonSort" />&#160;  
                            </xsl:if>
                            <xsl:value-of select="./mods:title" />
                            <xsl:if test="./mods:subTitle">
                              : <xsl:value-of select="./mods:subTitle" />
                            </xsl:if>
                          </strong>
                          <xsl:if test="./mods:partNumber or ./mods:partName">
                          <br /><strong>
                            <xsl:value-of select="./mods:partNumber" />
                            <xsl:if test="./mods:partNumber and ./mods:partName">: </xsl:if>
                            <xsl:value-of select="./mods:partName" />
                          </strong>
                        </xsl:if>
                        </p>
                      </xsl:for-each>
                       
                       
                       <xsl:choose>
                         <xsl:when test="./mods:identifier[@type='doi']">
                          <p><xsl:element name="a">
                              <xsl:attribute name="href">https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" /></xsl:attribute>
                              https://doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" />
                             </xsl:element></p>
                         </xsl:when>
                         <xsl:when test="./mods:identifier[@type='purl']">
                          <xsl:variable name="p" select="replace(./mods:identifier[@type='purl'], 'http://purl.uni-rostock.de', 'https://purl.uni-rostock.de')" />
                          <p><xsl:element name="a">
                              <xsl:attribute name="href"><xsl:value-of select="$p" /></xsl:attribute>
                              <xsl:value-of select="$p" />
                            </xsl:element></p>
                         </xsl:when>
                       </xsl:choose>
                       <p>
                       <!-- 
                        <xsl:if test="./mods:classification[@displayLabel='doctype']">
                          <span class="label label-default ir-label-default">
                          <xsl:for-each select="./mods:classification[@displayLabel='doctype']/@valueURI">
                            <xsl:call-template name="classLabel">
                              <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
                            </xsl:call-template>
                          </xsl:for-each>
                          </span>
                        </xsl:if>
                        -->
                        <xsl:if test="/mycoreobject/service/servflags/servflag[@type='mcr-delete:doctype']">
                          <span class="label label-default ir-label-default">
                            <xsl:call-template name="classLabel">
                              <xsl:with-param name="valueURI"><xsl:value-of select="/mycoreobject/service/servflags/servflag[@type='mcr-delete:doctype']" /></xsl:with-param>
                            </xsl:call-template>
                          </span>
                        </xsl:if>
                       </p>
                    </td>
                  </tr>
                </table>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="/mycoreobject/service/servdates/servdate[@type='mcr-delete:date']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.deleted_details.delete_date')}</th>
            <td>
               <table class="ir-table-docdetails-values">
                  <tr>
                    <td>
                      <xsl:value-of select="substring-before(/mycoreobject/service/servdates/servdate[@type='mcr-delete:date'],'T')" />
                    </td>
                  </tr>
                </table>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="/mycoreobject/service/servflags/servflag[@type='mcr-delete:note']">
          <tr>
            <th>{mcri18n:translate('OMD.ir.docdetails.deleted_details.delete_note')}</th>
            <td>
               <table class="ir-table-docdetails-values">
                  <tr>
                    <td>
                      <xsl:value-of select="/mycoreobject/service/servflags/servflag[@type='mcr-delete:note']" />
                    </td>
                  </tr>
                </table>
            </td>
          </tr>
        </xsl:if>
      </table>
    </xsl:for-each>
  
  </xsl:template>
</xsl:stylesheet>