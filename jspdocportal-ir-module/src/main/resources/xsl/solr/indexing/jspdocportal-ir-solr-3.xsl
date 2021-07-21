<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcrmods="http://www.mycore.de/xslt/mods"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcrstring="http://www.mycore.de/xslt/stringutils" 
  exclude-result-prefixes="mods xlink"
  expand-text="yes">
  
  <xsl:import href="xslImport:solr-document-3:solr/indexing/jspdocportal-ir-solr-3.xsl" />
  <!-- already imported earlier in chain: -->
  <!-- <xsl:import href="resource:xsl/functions/mods.xsl" />
       <xsl:import href="resource:xsl/functions/stringutils.xsl" />
  -->
  <xsl:template match="mycoreobject">
    <xsl:apply-imports />

    <xsl:apply-templates select="structure"/>
    <xsl:apply-templates select="metadata"/>
     <xsl:apply-templates select="service"/>
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
    <field name="ir.identifier">[xslt]Saxon</field>
  
  	<xsl:for-each select="def.modsContainer/modsContainer[@type='imported' or @type='created']/mods:mods">
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
        
      <xsl:variable name="var_name">
  		<xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
  		  <xsl:if test="position()> 1"><xsl:value-of select="', '" /></xsl:if>
          <xsl:value-of select="string-join((./mods:namePart[@type='given'], ./mods:namePart[@type='family'], ./mods:namePart[not(@type)], ./mods:namePart[@type='termsOfAddress']),' ')" />
  	    </xsl:for-each>
        <xsl:if test="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
          <xsl:if test="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
            <xsl:value-of select="', '" />
          </xsl:if>  
          <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
            <xsl:if test="position()> 1"><xsl:value-of select="', '" /></xsl:if>
            <xsl:value-of select="string-join((./mods:namePart[not(@type)]),' ')" />
          </xsl:for-each>
        </xsl:if>
      </xsl:variable>
  	  <field name="ir.creator.result"><xsl:value-of select="normalize-space($var_name)"></xsl:value-of></field>
      
     <xsl:variable name="var_name_sort">
      <xsl:for-each select="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
        <xsl:if test="position()> 1"><xsl:value-of select="', '" /></xsl:if>
          <xsl:value-of select="string-join((./mods:namePart[@type='family'], ./mods:namePart[@type='given'], ./mods:namePart[not(@type)], ./mods:namePart[@type='termsOfAddress']),' ')" />
        </xsl:for-each>
        <xsl:if test="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
          <xsl:if test="./mods:name[@type='personal'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
            <xsl:value-of select="', '" />
          </xsl:if>  
          <xsl:for-each select="./mods:name[@type='corporate'][('aut','edt') = ./mods:role/mods:roleTerm[@authority='marcrelator']]">
            <xsl:if test="position()> 1"><xsl:value-of select="', '" /></xsl:if>
            <xsl:value-of select="string-join((./mods:namePart[not(@type)]),' ')" />
          </xsl:for-each>
        </xsl:if>
      </xsl:variable>
      <field name="ir.creator.sort"><xsl:value-of select="normalize-space($var_name_sort)"></xsl:value-of></field>
      
      <xsl:choose>
        <xsl:when test="//service/servstates/servstate[@classid='state']/@categid='reserved'">
          <field name="ir.title.result">{concat('[Reserviert] ', //metadata/def.modsContainer/modsContainer[@type='reserved']/mods:mods/mods:note[@type='provisional_title'])}</field>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="mods:titleInfo[@usage='primary']">
            <field name="ir.title.result">{string-join((string-join((./mods:nonSort, ./mods:title),' '), ./mods:subTitle),': ')}</field>
            <xsl:if test="mods:partNumber">
              <field name="ir.partNumber.result">{./mods:partNumber}</field>
            </xsl:if> 
            <xsl:if test="mods:partNumber|mods:partName">
              <field name="ir.partName.result">{./mods:partName}</field>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
       
      <xsl:if test="mods:genre[@displayLabel='doctype']">
	    <xsl:if test="mcrmods:is-supported(mods:genre[@displayLabel='doctype'])">
       	  <field name="ir.doctype.result"><xsl:value-of select="mcrmods:to-mycoreclass(mods:genre[@displayLabel='doctype'], 'single')//categories/category/label[@xml:lang='de']/@text" /></field>
          <field name="ir.doctype_en.result"><xsl:value-of select="mcrmods:to-mycoreclass(mods:genre[@displayLabel='doctype'], 'single')//categories/category/label[@xml:lang='en']/@text" /></field>
        </xsl:if>
      </xsl:if>
       
      <xsl:for-each select="./mods:originInfo[@eventType='publication']">
        <xsl:choose>
          <xsl:when test="contains(../mods:genre[@displayLabel='doctype']/@valueURI,'#histbest')">
            <xsl:variable name="publisherPlace"
                select="if (./mods:place[not(@supplied='yes')]/mods:placeTerm|./mods:publisher) 
                        then (string-join((./mods:place[not(@supplied='yes')]/mods:placeTerm, ./mods:publisher), ': '))
                        else ()" />
            <field name="ir.originInfo.result">{string-join((./mods:edition, $publisherPlace, ./mods:dateIssued[not(@*)]),  ', ')}</field>
          </xsl:when>
          <xsl:otherwise>
            <field name="ir.originInfo.result">{string-join((./mods:edition, ./mods:publisher, ./mods:dateIssued[not(@*)]), ', ')}</field>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:if test="mods:abstract">
        <field name="ir.abstract300.result">{mcrstring:shorten(mods:abstract[1],300)}</field>
      </xsl:if>
      
      <xsl:for-each select="mods:relatedItem[@displayLabel='appears_in'][1]">
        <xsl:for-each select="mods:titleInfo">
          <field name="ir.host.title.result">{string-join((mods:nonSort, string-join((mods:title, mods:subTitle), ': ')),' ')}</field> 
        </xsl:for-each>
        <xsl:for-each select="mods:part">
          <field name="ir.host.part.result">{string-join((mods:partNumber, mods:partName),' ')}</field> 
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="mods:relatedItem[not(@displayLabel='appears_in')]">
        <xsl:for-each select="mods:recordInfo[1]/mods:recordIdentifier">
          <field name="ir.host.recordIdentifier"><xsl:value-of select="." /></field> 
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="mods:relatedItem[not(@displayLabel='appears_in')][last()]">
        <xsl:for-each select="mods:part/mods:text[@type='sortstring'][1]">
          <field name="ir.sortstring"><xsl:value-of select="." /></field> 
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="mods:relatedItem[@type='series']/mods:part/mods:detail[@type='volume']/mods:number">
        <field name="ir.seriesNumber.result"><xsl:value-of select="." /></field> 
      </xsl:for-each>
      
      <!-- ab hier ungeprüft: -->      
        
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
                <xsl:value-of select="translate(substring(mods:dateIssued[1],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[1],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
            </xsl:when>
             <xsl:when test="mods:originInfo[@eventType='creation']">
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[1],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[1],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='start'],1,4),'xX', '00')" />
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>          
          </xsl:variable>
          
          <xsl:variable name="pubyear_end">
          <xsl:choose>
            <xsl:when test="mods:originInfo[@eventType='publication']">
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[1],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[1],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='publication'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
            </xsl:when>
            <xsl:when test="mods:originInfo[@eventType='creation']">
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[1],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[not(@point) and not(@qualifier)]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[1],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateCreated[@point]]">
                <xsl:value-of select="translate(substring(mods:dateCreated[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
              <xsl:for-each select="mods:originInfo[@eventType='creation'][mods:dateIssued[@point]]">
                <xsl:value-of select="translate(substring(mods:dateIssued[@point='end'],1,4),'xX', '99')" />
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>  
          </xsl:variable>
          
          <xsl:if test="$pubyear_start &gt; 0 and $pubyear_end &gt; 0">
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
               <field name="ir.language_class.facet">rfc5646:<xsl:value-of select="." /></field>
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
          <xsl:for-each select="mods:*[@displayLabel='doctype']">
            <xsl:variable name="categid" select="translate(substring-after(@valueURI,'classifications/'),'#',':')" />
            <field name="ir.doctype_class.facet">
              <xsl:value-of select="$categid" />
            </field>
            <xsl:choose>
              <xsl:when test="starts-with($categid,'doctype:histbest.print.')">
                <field name="ir.material_class.facet">doctype:histbest.print</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.newspaper.')">
                <field name="ir.material_class.facet">doctype:histbest.newspaper</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.manuscript.')">
                <field name="ir.material_class.facet">doctype:histbest.manuscript</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.musicalsource.')">
                <field name="ir.material_class.facet">doctype:histbest.musicalsource</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.personalpapers.')">
                <field name="ir.material_class.facet">doctype:histbest.personalpapers</field>
              </xsl:when>
              <xsl:when test="starts-with($categid,'doctype:histbest.archivalmaterial.')">
                <field name="ir.material_class.facet">doctype:histbest.archivalmaterial</field>
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
  <xsl:template match="def.irControl/irControl">   
    <xsl:for-each select="map/list[@key='mets_filegroups']/entry[text()='ALTO'][1]">
      <field name="ir.contains_msg.facet">ocr</field>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="service">   
    <xsl:for-each select="servstates/servstate">
      <field name="ir.state_class.facet"><xsl:value-of select="concat(@classid,':',@categid)" /></field>
    </xsl:for-each>
    <xsl:if test="servflags/servflag[@type='editedby']">
      <field name="ir.state_class.facet">state:editing</field>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>