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

<mcr:retrieveObject mcrid="${mcrid}" fromWorkflow="${param.fromWF}" varDOM="doc" />
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
	<stripes:layout-component name="html_header">
        <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/metatags_html.xsl" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}modules/shariff/shariff.complete.css">
		<style>
			div.shariff span.share_text{
				display:none;
			}
	
			div.shariff li.shariff-button{
				display:inline;
				min-width:30px !important;
				margin-left:0px;
				flex-grow: 0;
			}

			div.shariff a{
				display:inline-block;
				height: 30px;
				width: 30px;
			}
		</style>
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
     				gnd_html = $('<div>')
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
     		return $('<div>').append('<div style="color:darkred;margin-right:-15px;margin-top:-40px" class="btn btn-xs pull-right" onclick="hidePopover(this);"><i class="fa fa-times"></i></div>')
     		.append(gnd_html).append(sep_html).append(affi_html);
     	}
     	
     	function hidePopover(caller){
     		var id = $(caller).parent().parent().parent().attr("id");
     		$("button[aria-describedby='"+id+"']").click();
     	}
 		</script>
	</stripes:layout-component>
	
	<stripes:layout-component name="main_part">
      <div class="row">
        <div class="col-md-4 col-xs-12 clearfix" style="float:right; margin-top:15px">
            <search:result-navigator mcrid="${mcrid}" mode="one_line"/>
        </div>
        <div class="clearfix visible-xs-block" style="margin-bottom:-45px">&nbsp;</div>
        <div class="col-sm-12 col-md-8">
		<div class="row">
			<div class="col-sm-12">
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
          <div class="col-xs-12 ir-divider">
            <hr/>
          </div>
        </div>
	
		<div class="row">
			<div class="col-sm-12">
				<div class="ir-box">
					<ul id="main_navbar" class="nav nav-tabs">
                        <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[@xlink:title='fulltext' or @xlink:title='MCRVIEWER_METS']">
							<li id="nav_fulltext" role="presentation"><a data-toggle="collapse" href="#div_fulltext">Viewer</a></li>
  						</x:if>
  						<x:if select="contains($doc/mycoreobject/@ID, '_bundle_')">
  							<li id="nav_structure" role="presentation"><a data-toggle="collapse" href="#div_structure">zugehörende Dokumente</a></li>
						</x:if>
						<li id="nav_metadata" role="presentation"><a data-toggle="collapse" href="#div_metadata">Metadaten</a></li>
						<x:if select="$doc/mycoreobject/structure/derobjects/derobject">
							<li id="nav_files" role="presentation"><a data-toggle="collapse" href="#div_files">Dateien</a></li>
						</x:if>
					</ul>
				</div>
			</div>
		</div>
		<div id="main_display" class="row" style="margin-bottom:30px">
		<x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[@xlink:title='fulltext' or @xlink:title='MCRVIEWER_METS']">
			<div id="div_fulltext" class="collapse col-sm-12">
				<x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='fulltext']">
                    <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='fulltext']/@xlink:href" /></c:set>
					 <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                    <c:if test="${not hasAccess}">
                        <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                        <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                          <mcr:displayClassificationCategory valueURI="${valueURI}" lang="x-display-de"/>
                        </div>
                    </c:if>
                    <c:if test="${hasAccess}">
                        <search:mcrviewer mcrid="${param.id}" recordIdentifier="${param.id}" doctype="pdf" id="divMCRViewer_2" />
                    </c:if> 
					<div id="divMCRViewer_2" style="height:600px; margin:0px 16px; position:relative;"></div>
				</x:if>
				<x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='MCRVIEWER_METS']">
					<c:set var="recordidentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier" /></c:set>
                    <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='MCRVIEWER_METS']/@xlink:href" /></c:set>
                    <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                    <c:if test="${not hasAccess}">
                        <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                        <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                          <mcr:displayClassificationCategory valueURI="${valueURI}" lang="x-display-de"/>
                        </div>
                    </c:if>
                    <c:if test="${hasAccess}">
					   <search:mcrviewer mcrid="${param.id}" recordIdentifier="${recordidentifier}" doctype="mets" id="divMCRViewer_1" />
                    </c:if>
					<div id="divMCRViewer_1" style="height:600px; margin:0px 16px; position:relative;"></div>
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
			<div id="div_structure" class="collapse col-sm-12">
				<div class="ir-box" style="font-size: 85%;min-height:600px">
			    	<c:set var="recordIdentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier"/></c:set>
					<search:docdetails-structure hostRecordIdentifier="${recordIdentifier}" hostMcrID="${param.id}" />
				</div>
			</div>
		</x:if>
		<div id="div_metadata" class="collapse col-sm-12">
			<div class="ir-box ir-docdetails-data" style="min-height:600px">
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
			<div id="div_files" class="collapse col-sm-12">
				<div class="ir-box" style="min-height:600px">
                  <table class="table ir-table-docdetails">
                    <tbody>
			 		  <x:forEach var="x" select="$doc/mycoreobject/structure/derobjects/derobject/@xlink:href">
			 			 <c:set var="id"><x:out select="$x" /></c:set>
                         <!-- ${id} -->
			 			 <search:derivate-list derid="${id}" showSize="true" />
			 		  </x:forEach>
                    </tbody>
                  </table>
			 	</div>
			</div>
		</x:if>
		</div>
<script type="text/javascript">
	$(document).ready(function(){
		$('#main_navbar li:first-child').addClass('active');
		$('#main_navbar li:first-child a').attr('href', '#');
		$('#main_display div:first-child').collapse('show');
		
		$('#div_fulltext').on('show.bs.collapse', function () {
			$('#nav_metadata').removeClass('active');
			$('#nav_metadata a').attr('href', '#div_metadata');
			$('#nav_structure').removeClass('active');
			$('#nav_structure a').attr('href', '#div_structure');
			$('#nav_files').removeClass('active');
			$('#nav_files a').attr('href', '#div_files');
			
			$('#nav_fulltext').addClass('active');
			$('#nav_fulltext a').attr('href', '#');
			
			$('#div_metadata').collapse('hide');
			$('#div_structure').collapse('hide');
			$('#div_files').collapse('hide');
		});
		$('#div_metadata').on('show.bs.collapse', function () {
			$('#nav_fulltext').removeClass('active');
			$('#nav_fulltext a').attr('href', '#div_fulltext');
			$('#nav_structure').removeClass('active');
			$('#nav_structure a').attr('href', '#div_structure');
			$('#nav_files').removeClass('active');
			$('#nav_files a').attr('href', '#div_files');
			
			$('#nav_metadata').addClass('active');
			$('#nav_metadata a').attr('href', '#');
			
			$('#div_fulltext').collapse('hide');
			$('#div_structure').collapse('hide');
			$('#div_files').collapse('hide');
		});
		$('#div_structure').on('show.bs.collapse', function () {
			$('#nav_fulltext').removeClass('active');
			$('#nav_fulltext a').attr('href', '#div_fulltext');
			$('#nav_metadata').removeClass('active');
			$('#nav_metadata a').attr('href', '#div_metadata');
			$('#nav_files').removeClass('active');
			$('#nav_files a').attr('href', '#div_files');

			$('#nav_structure').addClass('active');
			$('#nav_structure a').attr('href', '#');
			
			$('#div_fulltext').collapse('hide');
			$('#div_metadata').collapse('hide');
			$('#div_files').collapse('hide');
		});
		$('#div_files').on('show.bs.collapse', function () {
			$('#nav_fulltext').removeClass('active');
			$('#nav_fulltext a').attr('href', '#div_fulltext');
			$('#nav_metadata').removeClass('active');
			$('#nav_metadata a').attr('href', '#div_metadata');
			$('#nav_structure').removeClass('active');
			$('#nav_structure a').attr('href', '#div_structure');
			
			$('#nav_files').addClass('active');
			$('#nav_files a').attr('href', '#');
			
			$('#div_fulltext').collapse('hide');
			$('#div_metadata').collapse('hide');
			$('#div_structure').collapse('hide');
		});
	});

</script>
</div>
  <div class="col-xs-12 col-md-4">
       <x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='cover'] or contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
     
  	   <div class="ir-box ir-box-bordered text-center" style="margin-bottom:30px;">
	     <x:choose>
            <x:when select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='cover']">
              <div style="display:inline-block">
                <x:choose>
	                <x:when select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[@xlink:title='MCRVIEWER_METS']"> 
						<c:set var="recordID"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" /></c:set>
		                <a href="${WebApplicationBaseURL}mcrviewer/recordIdentifier/${fn:replace(recordID,'/','_')}" title="Im MyCoRe Viewer anzeigen">
        		          <search:derivate-image mcrid="${param.id}" width="200px" labelContains="cover" />
                		</a>
                	</x:when>
                	<x:when select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[@xlink:title='DV_METS' or @xlink:title='METS']"> 
                		<c:set var="mcrid"><x:out select="$doc/mycoreobject/@ID" /></c:set>
	                		<a href="${WebApplicationBaseURL}resolve/id/${mcrid}/dfgviewer" target="_blank" title="Im DFG Viewer anzeigen">
	                			<search:derivate-image mcrid="${param.id}" width="200px" labelContains="cover" />
	                		</a>
                	</x:when>
                   	<x:otherwise>
        		          <search:derivate-image mcrid="${param.id}" width="200px" labelContains="cover" />
                	</x:otherwise>
                 </x:choose>
              </div>
            </x:when>
            <x:when select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#data')">
                <img src="${WebApplicationBaseURL}images/filetypeicons/data.png" alt="resarch data">
            </x:when>
          </x:choose>
        </div>
       </x:if>
       <x:if select="not($doc/mycoreobject/service/servstates/servstate/@categid='deleted')">
       <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
          <div class="row">
         
            <div class="col-xs-12">
            
            <h3>Dauerhaft zitieren</h3>
         	<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='purl']">
			    <c:set var="purl"><x:out select="$x" /></c:set>
                <%--<a href="${purl}"><c:out value="${fn:replace(purl, '.de/', '.de <br />/')}" escapeXml="false"/></a><br />  
                <a class="ir-link-portal" href="${purl}"><c:out value="${purl}" escapeXml="false"/></a><br />--%>
                <c:set var="link">${fn:replace(purl, '.de/', '.de/<br class="visible-md-inline"/>')}</c:set>
                <p><a class="ir-link-portal" href="${purl}"><c:out value="${link}" escapeXml="false"/></a></p>
                
              </x:forEach>
			  <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='urn']">
			     <p><a class="ir-link-portal" href="http://nbn-resolving.org/<x:out select="$x" />"><x:out select="$x" /></a></p>
			  </x:forEach>
			  <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='doi']">
			  	<p><a class="ir-link-portal" href="https://doi.org/<x:out select="$x" />">https://doi.org/<br class="visible-md-inline"/><x:out select="$x" /></a></p>
              </x:forEach>
              </div>
              
              </div>
              </div>
        </x:if> 
       
       <%--Download Area --%>
     <div style="margin-bottom:30px;">
      <x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='fulltext']">
        <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='fulltext']/@xlink:href" /></c:set>
        <mcr:retrieveObject mcrid="${derid}" varDOM="derXML" />
        <c:set var="maindoc"><x:out select="$derXML/mycorederivate/derivate/internals/internal/@maindoc" /></c:set>
        <c:if test="${empty maindoc}">
          <c:set var="fulltext_href">${WebApplicationBaseURL}resolve/id/${mcrid}/file/fulltext</c:set>
        </c:if>
        <c:if test="${not(empty maindoc)}">
          <c:set var="fulltext_href">${WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}</c:set>
        </c:if>
        <a class="btn btn-default ir-button ir-button-download" href="${fulltext_href}" target="_blank">
          <img style="vertical-align:middle;height:30px;" src="${WebApplicationBaseURL}images/download_pdf.png" title = "<fmt:message key="Webpage.docdetails.pdfdownload" />" />
          <fmt:message key="Webpage.docdetails.pdfdownload" />
        </a>
      </x:if>
      <x:if select="$doc/mycoreobject[not(contains(@ID,'_bundle_'))]/structure/derobjects/derobject[@xlink:title='DV_METS' or @xlink:title='METS']">
        <c:set var="recordID"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier[@source='DE-28']" /></c:set>
        <c:if test="${not empty recordID}">
          <a class="btn btn-default ir-button ir-button-download"  
               href="${WebApplicationBaseURL}pdfdownload/recordIdentifier/${fn:replace(recordID, '/','_')}" target="_blank">
                <img style="vertical-align:middle;height:30px;" src="${WebApplicationBaseURL}images/download_pdf.png" title = "<fmt:message key="Webpage.docdetails.pdfdownload" />" />
              &nbsp;<fmt:message key="Webpage.docdetails.pdfdownload" />
            </a>
        </c:if>
      </x:if>
      <x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='DV_METS' or @xlink:title='METS']">
      <a class="btn btn-default ir-button ir-button-download"  
         href="${WebApplicationBaseURL}resolve/id/${mcrid}/dfgviewer" target="_blank">
          <img style="height: 24px; margin: 3px 0px;" src="${WebApplicationBaseURL}images/dfgviewerLogo.svg" title = "<fmt:message key="Webpage.docdetails.dfgviewer" />" />
       </a>
      </x:if>
       
       
       <x:forEach select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='data' or @xlink:title='documentation' ]">
        <c:set var="url">${WebApplicationBaseURL}api/v1/objects/<x:out select="/mycoreobject/@ID" />/derivates/<x:out select="./@xlink:href" />/contents</c:set>
            <c:import var="derXML" url="${url}"/>
            <x:parse xml="${derXML}" var="derDoc"/>
            <x:set var="derLink" select="$derDoc//children/child[1]" />
            <x:if select="$derLink">
             <a class="btn btn-default ir-button ir-button-download" style="text-align:left" title="MD5: <x:out select="$derLink/md5" />" 
             href="<x:out select="$derLink/@href" />" target="_blank">
            <x:choose>
              <x:when select="contains($derLink/@href, '.zip')">
                <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_zip.png" />  
              </x:when>
              <x:when select="contains($derLink/@href, '.pdf')">
                <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_pdf.png" />  
              </x:when>
              <x:otherwise>
                <img style="vertical-align:middle;height: 38px;margin-right:12px;float:left" src="${WebApplicationBaseURL}images/download_other.png" />
              </x:otherwise>
            </x:choose>
            <c:set var="mesKey">OMD.derivatedisplay.<x:out select="@xlink:title"/></c:set>
            <strong><fmt:message key="${mesKey}" /></strong><br />
            <span style="font-size: 85%">
              <x:out select="$derLink/name" />&nbsp;&nbsp;&nbsp;(<x:out select="round($derLink/size div 1024 div 1024 * 10) div 10" /> MB)<br />
            </span>
         </a>
            </x:if>
       </x:forEach>

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
     </div>
       
       
       <x:if select="not($doc/mycoreobject/service/servstates/servstate/@categid='deleted')">
       <div class="ir-box ir-box-bordered" style="margin-bottom:0px;padding-bottom:5px">
          <div class="row">
            <div class="col-xs-12">
   	
			<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='doctype']/@valueURI, '#histbest')">
        				<h3>Export</h3>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
									<c:set var="ppn"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=bibtex">BibTeX</a>
									<a class="ir-link-portal" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=endnote">EndNote</a>
									<a class="ir-link-portal" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=ris">RIS</a>
									<a class="ir-link-portal" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=dc">DublinCore</a>
									<a class="ir-link-portal" href="http://unapi.gbv.de/?id=opac-de-28:ppn:${ppn}&format=mods">MODS</a>
  								</x:forEach>
  		
  						<h3>Portale</h3>
  								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='PPN']">
									<c:set var="ppn"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="http://opac.lbs-rostock.gbv.de/DB=1/PPNSET?PPN=${ppn}">OPAC (UB Rostock)</a>
									<a class="ir-link-portal" href="https://gso.gbv.de/DB=2.1/PPNSET?PPN=${ppn}">OPAC (GBV)</a>
								</x:forEach>							
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd16']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="http://gateway-bayern.de/VD16+${fn:replace(vdnr,' ','+')}">VD16</a>
								</x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd17']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="https://kxp.k10plus.de/DB=1.28/CMD?ACT=SRCHA&IKT=8079&TRM=%27${vdnr}%27">VD17</a>
								</x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='vd18']">
									<c:set var="vdnr"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="https://kxp.k10plus.de/DB=1.65/CMD?ACT=SRCHA&IKT=8080&TRM=VD18${fn:replace(vdnr,' ','+')}&ADI_MAT=B&MATCFILTER=Y&MATCSET=Y">VD18</a>
								</x:forEach>
                                <x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='zdb']">
                                  <c:set var="zdbnr"><x:out select="$x" /></c:set>
                                  <a class="ir-link-portal" href="https://zdb-katalog.de/list.xhtml?key=cql&t=${zdbnr}">ZDB</a>
                                </x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject[contains(@ID,'_disshab_')]/@ID">
									<c:set var="id"><x:out select="$x" /></c:set>
								</x:forEach>
								<x:forEach var="x" select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='kalliope']">
									<c:set var="id"><x:out select="$x" /></c:set>
									<a class="ir-link-portal" href="http://kalliope-verbund.info/${id}">Kalliope-Verbundkatalog</a>
								</x:forEach>	
   					
   					           </x:if>
				
        

                                <h3>Teilen</h3>
                                <div class="shariff" data-url="${WebApplicationBaseURL}resolve/id/${param.id}"
                                     data-services="[&quot;twitter&quot;, &quot;facebook&quot;, &quot;googleplus&quot;, &quot;linkedin&quot;, &quot;xing&quot;, &quot;whatsapp&quot;, &quot;mail&quot;, &quot;info&quot;]"
                                     data-mail-url="mailto:" data-mail-subject="Dokument auf RosDok" data-mail-body="${WebApplicationBaseURL}resolve/id/${param.id}"
                                     data-orientation="horizontal" data-theme="standard">
                                </div> <%--data-theme=standard|grey|white --%>
                                <script src="${WebApplicationBaseURL}modules/shariff/shariff.min.js"></script>
                          
                </div>

                </div>
       </div>
      
     </x:if>
        
        
        <div class="ir-box" style="margin-bottom:30px;margin-right:-15px">
        <div class="clearfix">
        <div class="pull-right">
              <button type="button" class="btn btn-default btn-sm pull-right hidden-xs" style="border:none;color:#DEDEDE; background-color:white;" 
                    data-toggle="collapse" data-target="#hiddenTools" title="<fmt:message key="Webpage.tools.menu4experts" />">
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
              <x:if select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='REPOS_METS']">
                <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[@xlink:title='REPOS_METS']/@xlink:href" /></c:set>
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}api/v1/objects/${param.id}/derivates/${derid}/open" class="btn btn-default" title="<fmt:message key="Webpage.tools.showREPOS_METS" />">METS</a>
              </x:if>
              <x:if select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[contains(@valueURI, '#epub') or contains(@valueURI, '#data')]">
                <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}receive/${mcrid}?XSL.Transformer=rosdok_datacite" rel="nofollow">Datacite</a>
              </x:if>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:oai.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai/dnb-urn?verb=GetRecord&metadataPrefix=epicur&identifier=oai:oai-dnb-urn.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI:DNB_URN</a>
              <a class="btn btn-warning btn-sm ir-button-warning" style="margin:3px" target="_blank" 
                   href="${WebApplicationBaseURL}oai/dnb-epflicht?verb=GetRecord&metadataPrefix=xMetaDissPlus&identifier=oai:oai-dnb-epflicht.rosdok.uni-rostock.de:${mcrid}" rel="nofollow">OAI:DNB_EPFLICHT</a>
            </div>
          </div>
        </div>          
        </div>
        </div>
      </div>
	</stripes:layout-component>
</stripes:layout-render>
