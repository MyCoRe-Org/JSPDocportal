<?xml version="1.0"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrmods="xalan://org.mycore.mods.classification.MCRMODSClassificationSupport"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" 
  xmlns:mcr="http://www.mycore.org/" 
  exclude-result-prefixes="mods xlink xalan mcri18n mcrmods mcrxsl mcr">

  <xsl:import href="resource:xsl/functions/i18n.xsl" />
  <xsl:import href="mods-util.xsl" />
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/">
    <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
      
      <table class="ir-table-docdetails">
        <tr>
          <th>
            <xsl:value-of select="mcri18n:translate('OMD.institution')" />
            :
          </th>
          <td>
            <table class="ir-table-docdetails-values">
                 <xsl:for-each select="mods:classification[@displayLabel='institution']/@valueURI">
                  <tr><td>
                  <xsl:call-template name="classLabel">
                    <xsl:with-param name="valueURI"><xsl:value-of select="." /></xsl:with-param>
                  </xsl:call-template>
                  </td></tr>
                  </xsl:for-each>
            </table>
          </td>
        </tr>
        <xsl:if test="mods:name[mods:role/mods:roleTerm[@type='code']='dgs']">
        <tr>
          <th>
            <xsl:value-of select="mcri18n:translate('OMD.referee')" />
            :
          </th>
          <td>
            <table class="ir-table-docdetails-values">
                <xsl:for-each select="mods:name[mods:role/mods:roleTerm[@type='code']='dgs']">
                <tr><td>
                    <xsl:call-template name="display-name">
              <xsl:with-param name="name" select="." />
            </xsl:call-template>
                    <xsl:if test="./mods:affiliation">
                      <br /><span class="small"><xsl:value-of select="./mods:affiliation"/></span>
                    </xsl:if>
            
                </td></tr>
                
                </xsl:for-each>
            </table>
          </td>
        </tr>
        </xsl:if>
        <xsl:if test="mods:originInfo[@eventType='creation']/mods:dateCreated">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.yearsubmitted')" disable-output-escaping="yes"/>:</th>
          <td>
            <table class="ir-table-docdetails-values">
              <tr><td>
                <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:dateCreated" />
              </td></tr>
            </table>
          </td>
        </tr>
        </xsl:if>
        <xsl:if test="mods:originInfo[@eventType='creation']/mods:dateOther[@type='defence']">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.yearaccepted')" disable-output-escaping="yes"/>:</th>
          <td>
            <table class="ir-table-docdetails-values">
              <tr><td>
                <xsl:value-of select="mods:originInfo[@eventType='creation']/mods:dateOther[@type='defence']" />
              </td></tr>
            </table>
          </td>
        </tr>
        </xsl:if>
      </table>
      
      <table class="ir-table-docdetails">
        <xsl:if test="./mods:language/mods:languageTerm">
          <tr>
            <th><xsl:value-of select="mcri18n:translate('OMD.languages')" /> :</th>
            <td><table class="ir-table-docdetails-values">
                <xsl:for-each select="./mods:language">
                  <tr><td>
                    <xsl:call-template name="language">
                      <xsl:with-param name="term"><xsl:value-of select="./mods:languageTerm" /></xsl:with-param>
                      <xsl:with-param name="lang">de</xsl:with-param>
                    </xsl:call-template>
                  </td></tr>
                </xsl:for-each>
              </table></td>
          </tr>
        </xsl:if>
        <xsl:for-each select="mods:titleInfo[@type='translated']">
          <tr>
          <th>
            <xsl:value-of select="mcri18n:translate('OMD.translated-title')" />
            :
          </th>
          <td>
            <table class="ir-table-docdetails-values">
              <tr>
                <td style="text-align:justify">
                  <xsl:value-of select="mods:title" />
                  <xsl:if test="mods:subTitle">
                    : <br /> <xsl:value-of select="mods:subTitle" />
                  </xsl:if>     
                </td>
              </tr>
            </table>
          </td>
          </tr>
        </xsl:for-each>
        <xsl:for-each select="mods:abstract[@type='summary'][2]">
        <tr>
          <th>
            <xsl:value-of select="mcri18n:translate('OMD.translated-abstract')" />
            :
          </th>
          <td>
            <table class="ir-table-docdetails-values">
                <tr><td style="text-align:justify">
                    <xsl:value-of select="." />
                </td></tr>
            </table>
          </td>
        </tr>
        </xsl:for-each>
         <xsl:for-each select="mods:abstract[@type='author_keywords']">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.keywords')" />:</th>
          <td>
            <table class="ir-table-docdetails-values">
              <tr><td>
                <xsl:value-of select="." />
              </td></tr>
            </table>
          </td>
        </tr>
        </xsl:for-each>
        <xsl:if test="mods:classification[@displayLabel='sdnb' or @displayLabel='SDNB']/@valueURI">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.ir.sdnb')" /> :</th>
          <td>
            <table class="ir-table-docdetails-values">
                  <xsl:for-each select="mods:classification[@displayLabel='sdnb' or @displayLabel='SDNB']/@valueURI">
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

        <xsl:if test="mods:subject">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.keywords')" /> :</th>
          <td>
            <table class="ir-table-docdetails-values">
                  <xsl:for-each select="mods:subject">
                    <tr><td>
                      <xsl:for-each select="./mods:topic"> <!-- use string-join in XSLT 2.0 -->
                      <xsl:value-of select="." /> 
                      <xsl:if test="position() != last()"> / </xsl:if>
                      </xsl:for-each>
                    </td></tr>
                  </xsl:for-each>
            </table>
          </td>
        </tr>
      </xsl:if>
       <xsl:if test="mods:note[@type!='creator_info'][@type!='statement of responsibility']">
        <tr>
          <th><xsl:value-of select="mcri18n:translate('OMD.ir.notes')" /> :</th>
          <td>
            <table class="ir-table-docdetails-values">
                <xsl:for-each select="mods:note[@type!='creator_info'][@type!='statement of responsibility']">
                  <tr>
                    <th><xsl:value-of select="./@type" /></th>
                    <td><xsl:value-of select="./text()" /></td>
                  </tr>
                </xsl:for-each>
            </table>
          </td>
        </tr>
      </xsl:if>
      </table>
      
      <table class="ir-table-docdetails">
            <xsl:for-each select="./mods:identifier[@type='doi']">
               <tr><th>DOI:</th>
                   <td>
                     <xsl:value-of select="./text()"/>
                       <xsl:element name="a">
                         <xsl:attribute name="href">https://doi.org/<xsl:value-of select="./text()" /></xsl:attribute>
                         <xsl:attribute name="title">DOI (registriert bei DataCite)</xsl:attribute>
                         <xsl:attribute name="class">pl-3 btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
                         <i class="fas fa-external-link-alt">&#160;</i>
                       </xsl:element>
               </td></tr>
              </xsl:for-each>
              <xsl:for-each select="./mods:identifier[@type='urn']">
               <tr><th>URN:</th>
                   <td>
                     <xsl:value-of select="./text()"/>
                       <xsl:element name="a">
                         <xsl:attribute name="href">https://nbn-resolving.org/process-urn-form?verb=FULL&amp;identifier=<xsl:value-of select="./text()" /></xsl:attribute>
                         <xsl:attribute name="title">URN, registriert bei der Deutschen Nationalbibliothek</xsl:attribute>
                         <xsl:attribute name="class">pl-3 btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
                         <i class="fas fa-external-link-alt">&#160;</i>
                       </xsl:element>
                  </td></tr>
              </xsl:for-each>
              <xsl:for-each select="./mods:identifier[@type='purl']">
               <tr><th>PURL:</th>
                   <td>
                     <xsl:value-of select="./text()"/>
                       <xsl:element name="a">
                         <xsl:attribute name="href"><xsl:value-of select="./text()" /></xsl:attribute>
                         <xsl:attribute name="title">Persistente URL</xsl:attribute>
                         <xsl:attribute name="class">pl-3 btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
                         <i class="fas fa-external-link-alt">&#160;</i>
                       </xsl:element>
                  </td></tr>
              </xsl:for-each>        
              <xsl:for-each select="./mods:identifier[@type='PPN']">
               <tr><th>PPN (Katalog-ID):</th>
                   <td>
                     <xsl:value-of select="./text()"/>
                       <xsl:element name="a">
                         <xsl:attribute name="href">http://opac.lbs-rostock.gbv.de/DB=2/PPNSET?PPN=<xsl:value-of select="./text()" /></xsl:attribute>
                         <xsl:attribute name="title">Bibliothekskatalog (HSB Neubrandenburg)</xsl:attribute>
                         <xsl:attribute name="class">pl-3 btn btn-sm btn-link ir-docdetails-btn-info</xsl:attribute>
                         <i class="fas fa-external-link-alt">&#160;</i>
                       </xsl:element>
                  </td></tr>
              </xsl:for-each>
      </table>
    </xsl:for-each>
  
  </xsl:template>
</xsl:stylesheet>