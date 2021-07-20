<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:mcrmods="xalan://org.mycore.mods.classification.MCRMODSClassificationSupport"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="mods xlink">
  <xsl:import href="xslImport:solr-document:solr/jspdocportal-ir-solr.xsl" />

  <xsl:template match="mycoreobject">
    <xsl:apply-imports />

    <xsl:apply-templates select="structure"/>
    <xsl:apply-templates select="metadata"/>
 	</xsl:template>

	<xsl:template match="structure">   
      <!-- online type of derivate -->
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject">
         <field name="derivateLabel"><xsl:value-of select="classification[@classid='derivate_types']/@categid" /></field>
      </xsl:for-each>
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover']][1]">
   		 <xsl:if test="string-length(maindoc)>0">
     			<field name="ir.cover_url">file/<xsl:value-of select="/mycoreobject/@ID" />/<xsl:value-of select="@xlink:href" />/<xsl:value-of select="maindoc" /></field>
     	 </xsl:if>
      </xsl:for-each>
     
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']][1]">
          <xsl:if test="string-length(maindoc)>0">
            <field name="ir.pdffulltext_url">file/<xsl:value-of select="/mycoreobject/@ID" />/<xsl:value-of select="@xlink:href" />/<xsl:value-of select="maindoc" /></field>
          </xsl:if>
      </xsl:for-each>
      
      <xsl:for-each select="/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']][1]">
           <xsl:if test="string-length(maindoc)>0">
             <field name="ir.reposmets_url">file/<xsl:value-of select="/mycoreobject/@ID" />/<xsl:value-of select="@xlink:href" />/<xsl:value-of select="maindoc" /></field>
           </xsl:if>
      </xsl:for-each>
  </xsl:template>

  <xsl:template match="metadata">
    <xsl:apply-imports/>
  
  	<xsl:for-each select="def.modsContainer/modsContainer/mods:mods">
        <field name="recordIdentifier"><xsl:value-of select="mods:recordInfo/mods:recordIdentifier" /></field>
  		
      <xsl:if test="mods:identifier[@type='purl']">
              <field name="purl"><xsl:value-of select="mods:identifier[@type='purl']" /></field>  
      </xsl:if>
      <xsl:if test="mods:identifier[@type='PPN']">
              <field name="ppn"><xsl:value-of select="mods:identifier[@type='PPN']" /></field>  
      </xsl:if>
      <xsl:if test="mods:identifier[@type='urn']">
              <field name="urn"><xsl:value-of select="mods:identifier[@type='urn']" /></field>  
      </xsl:if>
        
        <xsl:variable name="var_creator">
  			<xsl:for-each select="mods:name[mods:role/mods:roleTerm/@valueURI='http://id.loc.gov/vocabulary/relators/aut' or mods:role/mods:roleTerm[@authority='marcrelator']='aut' or mods:role/mods:roleTerm[@authority='marcrelator']='cre'] ">
  				<xsl:if test="position()> 1">; </xsl:if>
                <xsl:if test="mods:namePart[@type='family']">
                  <xsl:value-of select="mods:namePart[@type='family']" />
  				  <xsl:value-of select="', '" />
                </xsl:if>
  				<xsl:value-of select="mods:namePart[@type='given']" />
                <xsl:value-of select="mods:namePart[not(@type)]" />
  			</xsl:for-each>
  		</xsl:variable>
  	
  		<field name="ir.creator.result"><xsl:value-of select="normalize-space($var_creator)"></xsl:value-of></field>
  		<xsl:for-each select="mods:titleInfo[1]">
          <field name="ir.title.result"><xsl:if test="mods:nonSort"><xsl:value-of select="mods:nonSort" /><xsl:value-of select="' '" /></xsl:if><xsl:value-of select="mods:title" /><xsl:if test="mods:subTitle"><xsl:value-of select="' : '" /><xsl:value-of select="mods:subTitle" /></xsl:if></field>
          <xsl:if test="mods:partNumber|mods:partName">
            <field name="ir.partTitle.result"><xsl:if test="mods:partNumber"><xsl:value-of select="mods:partNumber" /><xsl:value-of select="' '" /></xsl:if><xsl:value-of select="mods:partName" /></field>
          </xsl:if> 
       </xsl:for-each>
       
       <!-- XSLT3:
	   <xsl:if test="mcrmods:is-supported(mods:classification[@displayLabel='doctype'])">
       		<field name="ir.doctype.result"><xsl:value-of select="mcrmods:to-mycoreclass(mods:classification[@displayLabel='doctype'], 'single')/categories/categories/category/label[@xml:lang='de']/@text" /></field>
       </xsl:if> -->
       <xsl:variable name="classlink" select="mcrmods:getClassCategLink(mods:genre[@displayLabel='doctype'][1])" />
  	   	<xsl:if test="string-length($classlink) &gt; 0">
       		<field name="ir.doctype.result"><xsl:value-of select="document($classlink)/mycoreclass/categories/category/label[@xml:lang='de']/@text" /></field>
       	</xsl:if>
       <xsl:variable name="var_published">
            <xsl:choose>
              <xsl:when test="mods:originInfo[@eventType='publication']"> 
                <xsl:for-each select="mods:originInfo[@eventType='publication'][1]">
                  <xsl:if test="mods:edition"><xsl:value-of select="mods:edition" /><xsl:value-of select="' , '" /></xsl:if>    
                  <xsl:if test="mods:place/mods:placeTerm"><xsl:value-of select="mods:place/mods:placeTerm" /><xsl:value-of select="' : '" /></xsl:if>
                  <xsl:if test="mods:publisher"><xsl:value-of select="mods:publisher" /><xsl:value-of select="' , '" /></xsl:if>
                  <xsl:value-of select="mods:dateIssued" />
                    <xsl:value-of select="mods:dateCreated[@qualifier='approximate']" />
                </xsl:for-each>  
              </xsl:when>
              <xsl:when test="mods:originInfo[@eventType='creation']"> 
                <xsl:for-each select="mods:originInfo[@eventType='creation'][1]">
                  <xsl:if test="mods:edition"><xsl:value-of select="mods:edition" /><xsl:value-of select="' , '" /></xsl:if>    
                  <xsl:if test="mods:place/mods:placeTerm"><xsl:value-of select="mods:place/mods:placeTerm" /><xsl:value-of select="' : '" /></xsl:if>
                  <xsl:if test="mods:publisher"><xsl:value-of select="mods:publisher" /><xsl:value-of select="' , '" /></xsl:if>
                  <xsl:value-of select="mods:dateIssued" />
                  <xsl:value-of select="mods:dateCreated[@qualifier='approximate']" />
                </xsl:for-each>  
              </xsl:when>
            </xsl:choose>
 			
  		</xsl:variable>
  		<field name="ir.originInfo.result"><xsl:value-of select="normalize-space($var_published)"></xsl:value-of></field>
      
        <xsl:if test="mods:abstract">
  	  	<xsl:variable name="var_abstract" select="mods:abstract[1]" />
  	  	<xsl:choose>
  	  		<xsl:when test="string-length($var_abstract)>300">
  	  			<field name="ir.abstract300.result"><xsl:value-of select="concat(substring($var_abstract, 1,300),' ...')" /></field>
  	  		</xsl:when>
  	  		<xsl:otherwise>
  	  			<field name="ir.abstract300.result"><xsl:value-of select="normalize-space($var_abstract)" /></field>
  	  		</xsl:otherwise>
  	  	</xsl:choose>
        </xsl:if>
        
        <xsl:for-each select="mods:relatedItem[not(@displayLabel='appears_in')][1]">
          <xsl:for-each select="mods:recordInfo[1]/mods:recordIdentifier[1]">
              <field name="ir.host.recordIdentifier"><xsl:value-of select="." /></field> 
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="mods:relatedItem[not(@displayLabel='appears_in')][2]">
          <xsl:for-each select="mods:recordInfo[1]/mods:recordIdentifier[1]">
              <field name="ir.host.recordIdentifier2"><xsl:value-of select="." /></field> 
            </xsl:for-each>
        </xsl:for-each>
		<xsl:for-each select="mods:relatedItem[not(@displayLabel='appears_in')][last()]">
            <xsl:for-each select="mods:part/mods:text[@type='sortstring'][1]">
              <field name="ir.sortstring"><xsl:value-of select="." /></field> 
            </xsl:for-each>
        </xsl:for-each>
            
        <xsl:for-each select="mods:relatedItem[@displayLabel='appears_in']">
            <xsl:for-each select="mods:titleInfo">
              <field name="ir.host.title.result"><xsl:if test="mods:nonSort"><xsl:value-of select="mods:nonSort" /><xsl:value-of select="' '" /></xsl:if><xsl:value-of select="mods:title" /><xsl:if test="mods:subTitle"><xsl:value-of select="' : '" /><xsl:value-of select="mods:subTitle" /></xsl:if></field> 
            </xsl:for-each>
            <xsl:for-each select="mods:part">
              <field name="ir.host.part.result"><xsl:if test="mods:partNumber|mods:partName"><xsl:value-of select="' ['" /><xsl:value-of select="mods:partNumber" /><xsl:value-of select="' '" /><xsl:value-of select="mods:partName" /><xsl:value-of select="']'" /></xsl:if><xsl:if test="mods:extent[@unit='page']"><xsl:if test="mods:extent[@unit='page']/mods:total"><xsl:value-of select="', '" /><xsl:value-of select="mods:extent[@unit='page']/mods:total"/><xsl:value-of select="' Seiten'" /></xsl:if><xsl:if test="mods:extent[@unit='page']/mods:start"><xsl:value-of select="', S. '" /><xsl:value-of select="mods:extent[@unit='page']/mods:start"/></xsl:if><xsl:if test="mods:extent[@unit='page']/mods:end"><xsl:value-of select="' - '" /><xsl:value-of select="mods:extent[@unit='page']/mods:end"/></xsl:if></xsl:if></field> 
            </xsl:for-each>
        </xsl:for-each>
        
  	  	<xsl:for-each select="mods:name[mods:role/mods:roleTerm/@valueURI='http://id.loc.gov/vocabulary/relators/aut' or mods:role/mods:roleTerm[@authority='marcrelator']='aut' or mods:role/mods:roleTerm[@authority='marcrelator']='cre'] ">
  	  		<field name="ir.creator_all"><xsl:value-of select="mods:namePart[@type='termsOfAddress']" /><xsl:value-of select="' '"/><xsl:value-of select="mods:namePart[@type='given']" /><xsl:value-of select="' '"/><xsl:value-of select="mods:namePart[@type='family']" /></field>
  	    </xsl:for-each>
  		<xsl:for-each select="//mods:titleInfo/*">
  			<field name="ir.title_all"><xsl:value-of select="text()" /></field>
  		</xsl:for-each>
        <xsl:for-each select="//mods:note[@type='titlewordindex']">
          <field name="ir.title_all"><xsl:value-of select="text()" /></field>
        </xsl:for-each>
        
          <xsl:for-each select="//mods:location//*">
            <field name="ir.location_all"><xsl:value-of select="text()" /></field>
          </xsl:for-each>
          <xsl:for-each select="//mods:name//*">
            <field name="ir.creator_all"><xsl:value-of select="text()" /></field>
          </xsl:for-each>
          
          <xsl:for-each select="mods:identifier[@type]">
            <field name="ir.identifier">[<xsl:value-of select="@type" />]<xsl:value-of select="." /></field> 
          </xsl:for-each>
          
          <xsl:if test="mods:identifier[@type='openaire']">
            <field name="ir.oai.setspec.openaire">openaire</field>
          </xsl:if>
          <xsl:if test="mods:classification[@displayLabel='accesscondition'][contains(@valueURI,'accesscondition#openaccess')]">
             <field name="ir.oai.setspec.open_access">open_access</field>
          </xsl:if>
          
          <xsl:variable name="pubyear_start">
          <xsl:choose>
            <xsl:when test="mods:originInfo[@eventType='publication']">
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued,1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated,1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued,1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated,1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>          
          </xsl:variable>
          
          <xsl:variable name="pubyear_end">
          <xsl:choose>
            <xsl:when test="mods:originInfo[@eventType='publication']">
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued,1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated,1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued,1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated,1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>  
          </xsl:variable>
          
          <xsl:if test="$pubyear_start and $pubyear_end">
            <field name="ir.pubyear_start"><xsl:value-of select="$pubyear_start" /></field>
            <field name="ir.pubyear_end"><xsl:value-of select="$pubyear_end" /></field>
            
          <!-- epoch facets -->
            <xsl:if test="$pubyear_start &lt;= 1500">
              <field name="ir.epoch_class.facet">epoch:1500_and_earlier</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 1600 and $pubyear_end &gt; 1500">
              <field name="ir.epoch_class.facet">epoch:16th_century</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 1700 and $pubyear_end &gt; 1600">
              <field name="ir.epoch_class.facet">epoch:17th_century</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 1800 and $pubyear_end &gt; 1700">
              <field name="ir.epoch_class.facet">epoch:18th_century</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 1900 and $pubyear_end &gt; 1800">
              <field name="ir.epoch_class.facet">epoch:19th_century</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 2000 and $pubyear_end &gt; 1900">
              <field name="ir.epoch_class.facet">epoch:20th_century</field>
            </xsl:if>
            <xsl:if test="$pubyear_start &lt;= 2100 and $pubyear_end &gt; 2000">
              <field name="ir.epoch_class.facet">epoch:21th_century</field>
            </xsl:if>
          </xsl:if>
          
          <!-- more facets -->
          
          <xsl:for-each select="mods:language/mods:languageTerm">
               <field name="ir.language_class.facet">rfc5656:<xsl:value-of select="." /></field>
          </xsl:for-each>
          
          <xsl:choose>
            <xsl:when test="mods:originInfo[@eventType='creation']/mods:place[@supplied='yes']/mods:placeTerm"> 
                <xsl:for-each select="mods:originInfo[@eventType='creation']/mods:place[@supplied='yes']/mods:placeTerm">
                  <field name="ir.place.facet"><xsl:value-of select="translate(.,'[]','')" /></field>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="mods:originInfo[@eventType='creation']/mods:place/mods:placeTerm">
                <field name="ir.place.facet"><xsl:value-of select="translate(.,'[]','')" /></field>
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:for-each select="mods:genre[@displayLabel='doctype']">
            <xsl:variable name="categid" select="translate(substring-after(@valueURI,'classifications/'),'#',':')" />
            <field name="ir.doctype_class.facet">
              <xsl:value-of select="$categid" />
            </field>
            <xsl:choose>
              <xsl:when test="starts-with($categid,'doctype:histbest.print')">
                <field name="ir.doctype_group_class.facet">doctype:histbest.print</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.manuscript')">
                <field name="ir.doctype_group_class.facet">doctype:histbest.manuscript</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.musicalsource')">
                <field name="ir.doctype_group_class.facet">doctype:histbest.musicalsource</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.personalpapers')">
                <field name="ir.doctype_group_class.facet">doctype:histbest.personalpapers</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.archivalmaterial')">
                <field name="ir.doctype_group_class.facet">doctype:histbest.archivalmaterial</field>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='collection']">
                <field name="ir.collection_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='provider']">
                <field name="ir.provider_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='accesscondition']">
                <field name="ir.accesscondition_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='sdnb']">
                <field name="ir.sdnb_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='ghb']">
                <field name="ir.ghb_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
           <xsl:for-each select="mods:classification[@displayLabel='institution']">
                <field name="ir.institution_class.facet"><xsl:value-of select="translate(substring-after(@valueURI,'classifications/'),'#',':')" /></field>
           </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>