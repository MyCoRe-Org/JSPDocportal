<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<%@ taglib prefix="mcrdd" 	uri="http://www.mycore.org/jspdocportal/docdetails.tld"%>

<mcrdd:setnamespace prefix="xlink" uri="http://www.w3.org/1999/xlink" />
<mcrdd:setnamespace prefix="mods" uri="http://www.loc.gov/mods/v3" />
<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />
<c:set var="mcrid">
	<c:choose>
		<c:when test="${!empty(requestScope.id)}">${requestScope.id}</c:when>
		<c:otherwise>${param.id}</c:otherwise>
	</c:choose>
</c:set>
<c:set var="from" value="${param.fromWF}" />
<c:set var="debug" value="${param.debug}" />
<c:set var="style" value="${param.style}" />

<c:set var="objectType" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />

<mcr:retrieveObject mcrid="${mcrid}" fromWorkflow="${param.fromWF}" varDOM="doc" cache="true" />

<fmt:message var="pageTitle" key="OMD.headline">
	<fmt:param>${mcrid}</fmt:param>
</fmt:message>

<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#epub')">
  <c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_recherche</c:set>
</x:if>
<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
  <c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_recherche</c:set>
</x:if>
<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#histbest')">
  <c:set var="org.mycore.navigation.path" scope="request">left.histbest.histbest_recherche</c:set>
</x:if>
<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle="${pageTitle}">
  <stripes:layout-component name="html_head">
		<mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/metatags_html.xsl" />
	    <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}modules/shariff_3.0.1/shariff.min.css">
		<script>
     	$( document ).ready(function() {
 			$('[data-mcr-action="popover4person"]').popover({
 		        html:true,
 				content: function(){return popoverContent4Person(this);}
 				
 		    }); 
     	});
     	
     	function popoverContent4Person(html){
     		var gnd = $(html).data('mcr-value-gnd');
     			var gnd_html = "";
     			if($(html).data('mcr-value-gnd')){
     				gnd_html = $('<div style="min-width:200px">')
     					.append($('<strong>').attr('title', 'Gemeinsame Normdatei der Deutschen Nationalbibliothek').append('GND:'))
 						.append(' ').append($('<span>').append(gnd))
 						.append(' ').append($('<a>').attr('type', 'button').addClass('btn btn-xs btn-link').attr('href', 'http://d-nb.info/gnd/'+gnd)
 							.attr('title', 'Gemeinsame Normdatei der Deutschen Nationalbibliothek')
 							.append($('<span>').addClass('fa fa-share-square-o')));
     			}
     			
     			var sep_html = "";
     			if($(html).data('mcr-value-gnd') && $(html).data('mcr-value-affiliation')){
 					sep_html = $('<hr>')
 				}
 			
 				var affi_html = "";
 				if($(html).data('mcr-value-affiliation')){
 					affi_html = $('<div>')
 						.append($('<strong>').append('Einrichtung:')).append('<br>').append($(html).data('mcr-value-affiliation'));
 				}
     		return $('<div>').append('<span style="color:darkred;margin-right:-5px;margin-top:-45px" class="close" onclick="hidePopover(this);"><i class="fa fa-times small"></i></span>')
     		.append(gnd_html).append(sep_html).append(affi_html);
     	}
     	function hidePopover(caller){
     		var id = $(caller).parent().parent().parent().attr("id");
     		$("button[aria-describedby='"+id+"']").click();
     	}
 		</script>
  </stripes:layout-component>
	
  <stripes:layout-component name="main_part">
    <div class="container">
      <div class="row d-block d-lg-none" style="padding: 0px 15px">
        <div class="col-12" style="padding-top:45px">
		  <div class="ir-nav-search-back ir-nav-search ir-box text-right" style="padding:0px 0px 30px 0px">
             <a class="btn btn-primary" href="${WebApplicationBaseURL}/browse/epub" class="btn btn-primary btn-sm">
			    <i class="fas fa-search"></i>
				<fmt:message key="Webpage.docdetails.newsearch" />
			</a>
 		  </div>
          <search:result-navigator mcrid="${mcrid}" mode="one_line"/>
        </div>
      </div>
      <div class="row">  
        <div class="col-12 col-md-8"><%--main area --%>
		  <div class="row">
            <div class="col">
			  <div class="ir-box ir-docdetails-header">
                <x:choose>
                  <x:when select="$doc/mycoreobject/service/servstates/servstate/@categid='deleted'">
                    <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/deleted_header_html.xsl" />
                  </x:when>
                  <x:otherwise>
                    <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/${objectType}2header_html.xsl" />
                  </x:otherwise>
                </x:choose>
			  </div>
		    </div>			
		  </div>
      
          <div class="row">
            <div class="col ir-divider">
              <hr/>
            </div>
          </div>
	
		  <div class="row">
		    <div class="col">
			  <div class="mb-3">
                 <ul id="nav_bar_root" class="nav nav-tabs ir-docdetails-tabs">
                   <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
					<li class="nav-item" role="presentation">
                      <a id="nav_tab_fulltext" class="nav-link" data-toggle="collapse" href="#nav_content_fulltext">Viewer</a>
                    </li>
  				    </x:if>
  				   <x:if select="contains($doc/mycoreobject/@ID, '_bundle_')">
  				   <li class="nav-item" role="presentation">
                      <a  id="nav_tab_structure" class="nav-link" data-toggle="collapse" href="#nav_content_structure">zugehörende Dokumente</a>
                   </li>
				   </x:if>
				   <li class="nav-item" role="presentation">
                      <a id="nav_tab_metadata" class="nav-link" data-toggle="collapse" href="#nav_content_metadata">Metadaten</a>
                   </li>
				   <x:if select="$doc/mycoreobject/structure/derobjects/derobject">
					  <li class="nav-item" role="presentation">
                        <a id="nav_tab_files" class="nav-link" data-toggle="collapse" href="#nav_content_files">Dateien</a>
                      </li>
				   </x:if>
				  </ul>
			  </div>
			
              <div id="nav_content_root" style="padding-bottom:75px">
		          <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
			        <div id="nav_content_fulltext" class="collapse" data-parent="#nav_content_root">
				       <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]">
                         <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]/@xlink:href" /></c:set>
					      <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                          <c:if test="${not hasAccess}">
                           <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                           <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                              <mcr:displayClassificationCategory valueURI="${valueURI}" lang="x-display-de"/>
                            </div>
                          </c:if>
                          <c:if test="${hasAccess}">
                            <search:mcrviewer mcrid="${param.id}" recordIdentifier="${param.id}" doctype="pdf" id="divMCRViewer_2" />
                            <div id="divMCRViewer_2" style="height:80vh; margin:0px 16px; position:relative;"></div>
                          </c:if> 
				       </x:if>
				       <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]">
					     <c:set var="recordidentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier" /></c:set>
                         <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]/@xlink:href" /></c:set>
                         <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                         <c:if test="${not hasAccess}">
                           <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                           <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                             <mcr:displayClassificationCategory valueURI="${valueURI}" lang="x-display-de"/>
                           </div>
                         </c:if>
                         <c:if test="${hasAccess}">
					       <search:mcrviewer mcrid="${param.id}" recordIdentifier="${recordidentifier}" doctype="mets" id="divMCRViewer_1" />
                           <div id="divMCRViewer_1" style="height:80vh; margin:0px 16px; position:relative;"></div>
                         </c:if>
                         <script type="text/javascript">
 	                       $.urlParam = function(name){
    						 var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
   							 return results[1] || 0;
						   }
                      	   window.addEventListener("load", function(){
							 if($.urlParam('_mcrviewer_start')){
	                    		//[0] get Javascript object from Jquery object
	                    		$("#main_navbar")[0].scrollIntoView();
                    		 }
                  		   });
                         </script>
				       </x:if>
			        </div>
		          </x:if>
		          <x:if select="contains($doc/mycoreobject/@ID, '_bundle_')">
			        <div id="nav_content_structure" class="collapse" data-parent="#nav_content_root">
				      <div style="font-size: 85%;min-height:600px">
			    	    <c:set var="recordIdentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier"/></c:set>
					    <search:docdetails-structure hostRecordIdentifier="${recordIdentifier}" hostMcrID="${param.id}" />
				      </div>
			        </div>
		          </x:if>
		          <div id="nav_content_metadata" class="collapse" data-parent="#nav_content_root">
			        <div class="ir-docdetails-data" style="min-height:600px">
				       <x:choose>
				         <x:when select="$doc/mycoreobject/service/servstates/servstate/@categid='deleted'">
				           <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/deleted_details_html.xsl" />
				         </x:when>
					     <x:when select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
						   <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/data2details_html.xsl" />
					     </x:when>
					     <x:otherwise>
						   <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/${objectType}2details_html.xsl" />
					     </x:otherwise>
			          </x:choose>
				      <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/all2footer_html.xsl" />
			        </div>
		          </div>
		          <x:if select="$doc/mycoreobject/structure/derobjects/derobject">
			        <div id="nav_content_files" class="collapse" data-parent="#nav_content_root">
				      <div style="min-height:600px">
                        <table class="ir-table-docdetails">
                          <tbody>
			 		         <x:forEach var="x" select="$doc/mycoreobject/structure/derobjects/derobject/@xlink:href">
			 			       <c:set var="id"><x:out select="$x" /></c:set>
                               <search:derivate-list derid="${id}" showSize="true" />
			 		         </x:forEach>
                          </tbody>
                        </table>
			 	      </div>
			        </div>
		          </x:if>
		     </div><%--END: nav_content_root --%>
             <script type="text/javascript">
	           $(document).ready(function(){
		          $('#nav_content_fulltext').on('shown.bs.collapse', function() {
			        $('#nav_tab_fulltext').addClass('active');
                  });
		
		         $('#nav_content_fulltext').on('hidden.bs.collapse', function() {
			        $('#nav_tab_fulltext').removeClass("active");
		         });
		
		         $('#nav_content_fulltext').on('shown.bs.collapse', function() {
			       $('#nav_tab_fulltext').addClass('active');
		         });
		
		         $('#nav_content_structure').on('hidden.bs.collapse', function() {
			       $('#nav_tab_strcuture').removeClass("active");
		         });
		       
		         $('#nav_content_metadata').on('shown.bs.collapse', function() {
		           $('#nav_tab_metadata').addClass('active');
		         });
		       
		         $('#nav_content_metadata').on('hidden.bs.collapse', function() {
		    	   $('#nav_tab_metadata').removeClass("active");
                 });
		
		         $('#nav_content_files').on('shown.bs.collapse', function() {
			       $('#nav_tab_files').addClass('active');
		         });
		
		         $('#nav_content_files').on('hidden.bs.collapse', function() {
			       $('#nav_tab_files').removeClass("active");
		         });
		
		         $('#nav_content_root div:first-child').addClass('show');
		         $('#nav_bar_root li:first-child a').addClass('active');
		       });
             </script>
          </div>
       </div>
    </div><%-- main area --%>
    <div class="col-xs-12 col-md-4"> <%-- right area --%>
       <div class="ir-facets h-100">
         <div class="d-none d-lg-block">
     	    <c:if test="${empty param._search and (fn:contains(WebApplicationBaseURL, 'dbhsnb') or fn:contains(WebApplicationBaseURL, 'hs-nb'))}">
				<div class="ir-nav-search ir-box text-right" style="padding:0px 0px 30px 0px">
					<a class="btn btn-primary" href="${WebApplicationBaseURL}/browse/epub" class="btn btn-primary btn-sm">
						<i class="fas fa-search"></i>
						<fmt:message key="Webpage.docdetails.newsearch" />
					</a>
				</div>
         	</c:if>
            <search:result-navigator mcrid="${mcrid}" mode="one_line"/>
        </div>
         <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover']] or contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
	       <x:choose>
             <x:when select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='cover']]">
               <div class="ir-box ir-box-docdetails-image">
                 <x:choose>
	                 <x:when select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]"> 
				 		<c:set var="recordID"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" /></c:set>
                          <a href="${WebApplicationBaseURL}mcrviewer/recordIdentifier/${fn:replace(recordID,'/','_')}" title="Im MyCoRe Viewer anzeigen">
        		            <search:derivate-image mcrobj="${param.id}" width="200px" category="cover" />
                		  </a>
                 	 </x:when>
                 	 <x:when select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]"> 
                       <c:set var="mcrid"><x:out select="$doc/mycoreobject/@ID" /></c:set>
	                	 <a href="${WebApplicationBaseURL}resolve/id/${mcrid}/dfgviewer" target="_blank" title="Im DFG Viewer anzeigen">
	                	   <search:derivate-image mcrobj="${param.id}" width="200px" category="cover" />
	                	 </a>
                	</x:when>
                   	<x:otherwise>
        		          <search:derivate-image mcrobj="${param.id}" width="200px" category="cover" />
                	</x:otherwise>
                 </x:choose>
              </div>
            </x:when>
            <x:when select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
                <div class="ir-box ir-box-docdetails-image">
                  <img src="${WebApplicationBaseURL}images/filetypeicons/data.png" alt="resarch data">
                </div>
            </x:when>
         </x:choose>
       </x:if>
       <x:if select="not($doc/mycoreobject/service/servstates/servstate/@categid='deleted')">
       <div class="ir-box ir-box-emph">
            <h4 class="text-primary">Dauerhaft zitieren</h4>
            <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
              <p><a class="ir-link-portal" href="https://doi.org/<x:out select="$x" />">https://doi.org/<br class="visible-md-inline"/><x:out select="$x" /></a></p>
            </x:forEach>
            <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']">
              <p><a class="ir-link-portal" href="http://nbn-resolving.org/<x:out select="$x" />"><x:out select="$x" /></a></p>
            </x:forEach>
            <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='purl']">
			    <c:set var="purl"><x:out select="$x" /></c:set>
                <%--<a href="${purl}"><c:out value="${fn:replace(purl, '.de/', '.de <br />/')}" escapeXml="false"/></a><br />  
                <a class="ir-link-portal" href="${purl}"><c:out value="${purl}" escapeXml="false"/></a><br />--%>
                <c:set var="link">${fn:replace(purl, '.de/', '.de/<br class="visible-md-inline"/>')}</c:set>
                <p><a class="ir-link-portal" href="${purl}"><c:out value="${link}" escapeXml="false"/></a></p>
            </x:forEach>

       </div>
       </x:if>
       <%--Download Area --%>
       <div style="margin-bottom:30px;">
         <x:forEach select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]">
            <c:set var="derid"><x:out select="./@xlink:href" /></c:set>
            <c:set var="fulltext_url">${WebApplicationBaseURL}file/${mcrid}/${derid}/<x:out select="./maindoc/text()" /></c:set>
            <a class="btn btn-primary ir-button-download" href="${fulltext_url}" target="_blank">
                	<c:set var="mesKey"><x:out select="classification[@classid='derivate_types']/@categid"/></c:set>
                    <img align="left" src="${WebApplicationBaseURL}images/download_pdf.png" title = "<fmt:message key="Webpage.docdetails.pdfdownload" />" />
                    <mcr:retrieveDerivateContentsXML derid="${derid}" varDOM="derDoc" />
                    <x:set var="derLink" select="$derDoc//children/child[1]" />
                    <x:if select="$derLink">
                    	<span class="float-right"><small>(<x:out select="round($derLink/size div 1024 div 1024 * 10) div 10" /> MB)</small></span>
                        <strong><mcr:displayClassificationCategory lang="de" classid="derivate_types" categid="${mesKey}"/></strong>
                        <br />
                        <small><x:out select="$derLink/name" /></small>
                    </x:if>
            </a>
         </x:forEach>
         <x:if select="$doc/mycoreobject[not(contains(@ID,'_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
           <c:set var="recordID"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" /></c:set>
           <c:if test="${not empty recordID}">
             <a class="btn btn-primary ir-button-download"  
                 href="${WebApplicationBaseURL}pdfdownload/recordIdentifier/${fn:replace(recordID, '/','_')}" target="_blank">
                <img align="left" src="${WebApplicationBaseURL}images/download_pdf.png" title = "<fmt:message key="Webpage.docdetails.pdfdownload" />" />
                 <strong><fmt:message key="Webpage.docdetails.pdfdownload" /></strong>
             </a>
           </c:if>
         </x:if>
         <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='DV_METS' or @categid='METS']]">
           <a class="btn btn-primary ir-button-download"  
              href="${WebApplicationBaseURL}resolve/id/${mcrid}/dfgviewer" target="_blank">
              <img style="height: 24px; margin: 3px 0px;float:left" src="${WebApplicationBaseURL}images/dfgviewerLogo.svg" title = "<fmt:message key="Webpage.docdetails.dfgviewer" />" />
           </a>
         </x:if>
       
         <x:forEach select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='data' or @categid='documentation' or @categid='supplement']]">
             <c:set var="derid"><x:out select="./@xlink:href" /></c:set>
             <mcr:retrieveDerivateContentsXML derid="${derid}" varDOM="derDoc" />
             <x:set var="derLink" select="$derDoc//children/child[1]" />
             <x:if select="$derLink">
               <a class="btn btn-secondary ir-button-download mt-3" style="text-align:left" title="MD5: <x:out select="$derLink/md5" />" 
                  href="<x:out select="$derLink/@href" />" target="_blank">
                  <x:choose>
                    <x:when select="contains($derLink/@href, '.zip')">
                      <img align="left" src="${WebApplicationBaseURL}images/download_zip.png" />  
                    </x:when>
                    <x:when select="contains($derLink/@href, '.pdf')">
                     <img align="left" src="${WebApplicationBaseURL}images/download_pdf.png" />  
                    </x:when>
                    <x:otherwise>
                      <img align="left" src="${WebApplicationBaseURL}images/download_other.png" />
                    </x:otherwise>
                  </x:choose>
                  <c:set var="mesKey"><x:out select="classification[@classid='derivate_types']/@categid"/></c:set>
                   <span class="float-right"><small>(<x:out select="round($derLink/size div 1024 div 1024 * 10) div 10" /> MB)</small></span>
                    <strong><mcr:displayClassificationCategory lang="de" classid="derivate_types" categid="${mesKey}"/></strong><br />
                    <small><x:out select="$derLink/name" /><br /></small>
               </a>
            </x:if>
          </x:forEach>
          <%-- called by: rosdok_document_0000015736 --%>
          <x:forEach select="$doc/mycoreobject/service/servflags/servflag[@type='external-content']">
            <c:set var="theXML"><x:out select="./text()" escapeXml="false" /></c:set>
            <x:parse var="theFileDoc" xml="${theXML}" />
            <x:set var="theFile" select="$theFileDoc/file" />
            <a class="btn btn-default ir-button ir-button-download" style="text-align:left" title="MD5: <x:out select="$theFile/@MD5" />" 
               href="<x:out select="$theFile/@URL" />" target="_blank">
              <x:choose>
                <x:when select="contains($theFile/@URL, '.zip')">
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_zip.png" />  
                </x:when>
                <x:when select="contains($theFile/@URL, '.pdf')">
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_pdf.png" />  
                </x:when>
                <x:otherwise>
                  <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_other.png" />
                </x:otherwise>
              </x:choose>
              <c:set var="mesKey">OMD.derivatedisplay.<x:out select="$theFile/@USE"/></c:set>
              <strong><fmt:message key="${mesKey}" /></strong><br />
              <span style="font-size: 85%">
                <x:out select="$theFile/@OWNERID" />&nbsp;&nbsp;&nbsp;(<x:out select="round($theFile/@SIZE div 1024 div 1024 * 10) div 10" /> MB)<br />
              </span>
            </a>
          </x:forEach>
          
       </div><%--Download area --%>
       <x:if select="not($doc/mycoreobject/service/servstates/servstate/@categid='deleted')">
       <div class="ir-box mt-3">
   	     <x:if select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
        		    <h4>Export</h4>
                    <p class="small">
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
									<c:set var="ppn"><x:out select="$x" /></c:set>
                                     <x:choose>
                                      <x:when select="starts-with($doc/mycoreobject/@ID, 'rosdok')">
									   <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=bibtex">BibTeX</a>
									   <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=endnote">EndNote</a>
									   <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=ris">RIS</a>
									   <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=dc">DublinCore</a>
									   <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=mods">MODS</a>
                                      </x:when>
                                      <x:when select="starts-with($doc/mycoreobject/@ID, 'dbhsnb')">
                                        <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-519:ppn:${ppn}&format=bibtex">BibTeX</a>
                                        <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-519:ppn:${ppn}&format=endnote">EndNote</a>
                                        <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-519:ppn:${ppn}&format=ris">RIS</a>
                                        <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-519:ppn:${ppn}&format=dc">DublinCore</a>
                                        <a class="ir-link-portal" target="_blank" href="http://unapi.gbv.de/?id=opac-de-519:ppn:${ppn}&format=mods">MODS</a>
                                      </x:when>
                                    </x:choose>
  								</x:forEach>
  		                </p>
  						<h4>Portale</h4>
                        <p class="small">
  								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
									<c:set var="ppn"><x:out select="$x" /></c:set>
                                    <x:choose>
                                      <x:when select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
									     <a class="ir-link-portal" target="_blank" href="http://opac.lbs-rostock.gbv.de/DB=1/PPNSET?PPN=${ppn}">OPAC (UB Rostock)</a>
                                      </x:when>
                                      <x:otherwise>
                                         <a class="ir-link-portal" target="_blank" href="http://opac.lbs-rostock.gbv.de/DB=2/PPNSET?PPN=${ppn}">OPAC (HSB Neubrandenburg)</a>
                                      </x:otherwise>
                                    </x:choose>
									<a class="ir-link-portal" href="https://gso.gbv.de/DB=2.1/PPNSET?PPN=${ppn}">OPAC (GBV)</a>
								</x:forEach>							
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd16']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" target="_blank" href="http://gateway-bayern.de/VD16+${fn:replace(vdnr,' ','+')}">VD16</a>
								</x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd17']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" target="_blank" href="https://kxp.k10plus.de/DB=1.28/CMD?ACT=SRCHA&IKT=8079&TRM=%27${vdnr}%27">VD17</a>
								</x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd18']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" target="_blank" href="https://gso.gbv.de/DB=1.65/CMD?ACT=SRCHA&IKT=8002&TRM=${fn:replace(vdnr,' ','+')}&ADI_MAT=B&MATCFILTER=Y">VD18</a>
								</x:forEach>
                                <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='zdb']">
                                  <c:set var="zdbnr"><x:out select="$x" /></c:set>
                                  <a class="ir-link-portal" target="_blank" href="https://zdb-katalog.de/list.xhtml?key=cql&t=${zdbnr}">ZDB</a>
                                </x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='kalliope']">
									<c:set var="id"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" target="_blank" href="http://kalliope-verbund.info/${id}">Kalliope-Verbundkatalog</a>
								</x:forEach>
                                <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
                                  <c:set var="doi"><x:out select="$x" /></c:set>
                                  <a class="ir-link-portal" target="_blank" href="https://search.datacite.org/works/1{doi}">DataCite Search</a>
                                </x:forEach>	
   					</p>
         </x:if>
		 <h4>Teilen</h4>
         <c:set var="shariff_subject">Dokument auf RosDok</c:set>
         <x:if select="starts-with($doc/mycoreobject/@ID, 'dbhsnb')">
            <c:set var="shariff_subject">Dokument in der Digitalen Bibliothek der Hochschule Neubrandenburg</c:set>
         </x:if>
         <div class="shariff" data-url="${WebApplicationBaseURL}resolve/id/${param.id}"
             data-services="[&quot;twitter&quot;, &quot;facebook&quot;, &quot;googleplus&quot;, &quot;linkedin&quot;, &quot;xing&quot;, &quot;whatsapp&quot;, &quot;mail&quot;, &quot;info&quot;]"
             data-mail-url="mailto:" data-mail-subject="${shariff_subject}" data-mail-body="${WebApplicationBaseURL}resolve/id/${param.id}"
             data-orientation="horizontal" data-theme="standard">
         </div> <%--data-theme=standard|grey|white --%>
         <script src="${WebApplicationBaseURL}modules/shariff_3.0.1/shariff.min.js"></script>
         <p></p>
       </div>
       </x:if>
       <div class="ir-box">
            <h4>Rechte</h4>
            <p>
            	<strong>
            		<a href="https://rightsstatements.org/page/InC/1.0/?language=de">
            			<img src="${WebApplicationBaseURL}images/rightsstatements.org/buttons/InC.white.svg" title="in copyright" style="width:100px;background-color:grey;border:5px solid grey;" class="mr-3">
            			Urheberrechtsschutz
            	     </a>
            	</strong>
                <%-- old:
                <span class="badge badge-secondary float-left mr-2 h-100"><a href="https://rightsstatements.org/page/InC/1.0/?language=de"><img src="${WebApplicationBaseURL}images/rightsstatements.org/buttons/InC.white.svg" title="in copyright" style="width:100px"></a></span>
                <br><strong><a href="https://rightsstatements.org/page/InC/1.0/?language=de">Urheberrechtsschutz</a></strong></p>
                <p class="text-justify form-text text-muted small">Dieses Objekt ist durch das Urheberrecht und/oder verwandte Schutzrechte geschützt. Sie sind berechtigt, das Objekt in jeder Form zu nutzen, die das Urheberrechtsgesetz und/oder einschlägige verwandte Schutzrechte gestatten. Für weitere Nutzungsarten benötigen Sie die Zustimmung der/des Rechteinhaber/s.</p>
                --%>
       </div>

       <div class="my-3"><%--Tools --%>
          <div class="float-right">
            <button type="button" class="btn btn-sm ir-button-tools hidden-xs" data-toggle="collapse" data-target="#hiddenTools"
                    title="<fmt:message key="Webpage.tools.menu4experts" />">
              <i class="fa fa-cog"></i>
            </button>
            <search:show-edit-button mcrid="${mcrid}" cssClass="btn btn-sm btn-primary ir-edit-btn pull-right" />
          </div>
          <div id="hiddenTools" class="collapse">
            <div style="padding-bottom:6px">
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="<fmt:message key="Webpage.tools.showXML" />"
                   href="${WebApplicationBaseURL}api/v1/objects/${mcrid}" rel="nofollow">XML</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="<fmt:message key="Webpage.tools.showSOLR" />"
                  href="${WebApplicationBaseURL}receive/${mcrid}?XSL.Style=solrdocument" rel="nofollow">SOLR in</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" title="<fmt:message key="Webpage.tools.showSOLR" />"
                  href="${WebApplicationBaseURL}api/v1/search?q=id:${mcrid}" rel="nofollow">SOLR doc</a>
              <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]">
                <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='REPOS_METS']]/@xlink:href" /></c:set>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}api/v1/objects/${param.id}/derivates/${derid}/open" class="btn btn-default" title="<fmt:message key="Webpage.tools.showREPOS_METS" />">METS</a>
              </x:if>
              <x:if select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[contains(@valueURI, '#epub') or contains(@valueURI, '#data')]">
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}receive/${mcrid}?XSL.Transformer=rosdok_datacite" rel="nofollow">Datacite</a>
              </x:if>
              <x:if select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']">
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:oai.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI</a>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai/dnb-urn?verb=GetRecord&metadataPrefix=epicur&identifier=oai:oai-dnb-urn.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI:DNB_URN</a>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai/dnb-epflicht?verb=GetRecord&metadataPrefix=xMetaDissPlus&identifier=oai:oai-dnb-epflicht.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI:DNB_EPFLICHT</a>
              </x:if>
            </div>
          </div>
       </div><%--Tools --%>      
     </div>
   </div><%-- right area --%>
      </div><%--row --%>
    </div>
  </stripes:layout-component>
</stripes:layout-render>
