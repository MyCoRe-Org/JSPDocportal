<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<%@ taglib prefix="mcrdd" 	uri="http://www.mycore.org/jspdocportal/docdetails.tld"%>

<mcrdd:setnamespace prefix="mods" uri="http://www.loc.gov/mods/v3" />
<mcrdd:setnamespace prefix="xlink" uri="http://www.w3.org/1999/xlink" />
  
<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />
<c:set var="mcrid">
	<c:choose>
		<c:when test="${!empty(requestScope.id)}">${requestScope.id}</c:when>
		<c:otherwise>${it.id}</c:otherwise>
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

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
  <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/metatags_html.xsl" />
  <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}modules/shariff_3.2.1/shariff.min.css">
  <script>
   var urlParam = function(name){
		 var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
		 if(results){
			 return results[1] || 0;
         }
         return null;
	   }
  </script>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
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
			  <div class="ir-docdetails-header">
                <x:choose>
                  <x:when select="$doc/mycoreobject/service/servstates/servstate/@categid='deleted'">
                    <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/deleted_header_html.xsl" />
                  </x:when>
                  <x:otherwise>
                    <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/header_html.xsl" />
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
                            <search:mcrviewer mcrid="${it.id}" recordIdentifier="${it.id}" doctype="pdf" id="divMCRViewer_2" />
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
					       <search:mcrviewer mcrid="${it.id}" recordIdentifier="${recordidentifier}" doctype="mets" id="divMCRViewer_1" />
                           <div id="divMCRViewer_1" style="height:80vh; margin:0px 16px; position:relative;"></div>
                         </c:if>
                         <script type="text/javascript">

                      	   window.addEventListener("load", function(){
							 if(urlParam('_mcrviewer_start')){
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
					    <search:docdetails-structure hostRecordIdentifier="${recordIdentifier}" hostMcrID="${it.id}" />
				      </div>
			        </div>
		          </x:if>
		          <div id="nav_content_metadata" class="collapse" data-parent="#nav_content_root">
			        <div class="ir-docdetails-data" style="min-height:600px">
				       <x:choose>
				         <x:when select="$doc/mycoreobject/service/servstates/servstate/@categid='deleted'">
				           <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/deleted_details_html.xsl" />
				         </x:when>
					     <x:otherwise>
						   <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/metadata_html.xsl" />
					     </x:otherwise>
			          </x:choose>
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
		          $('#nav_content_root > div').on('shown.bs.collapse', function() {
		        	  $("a.nav-link[href='#"+ $(this).attr("id") +"']").addClass('active');
                  });
		
		         $('#nav_content_root > div').on('hidden.bs.collapse', function() {
		        	 $("a.nav-link[href='#"+ $(this).attr("id") +"']").removeClass("active");
		         });

		         if(urlParam('_tab')){
             		var tab = urlParam('_tab');
             		$('#nav_content_root > div#nav_content_'+tab).addClass('show');
		         	$('#nav_bar_root >  a#nav_tab_'+tab).addClass('active');
         		 }else{
		         	$('#nav_content_root > div:first-child').addClass('show');
		         	$('#nav_bar_root > li:first-child a').addClass('active');
         		 }
		       });
             </script>
          </div>
       </div>
       <%--
       <div class="row">
         <div class="col">
           <mcr:transformXSL dom="${doc}" xslt="xsl/xsl3example.xsl" />
         </div>
       </div>
       --%>
    </div><%-- main area --%>
    <div class="col-xs-12 col-md-4"> <%-- right area --%>
       <div class="ir-right_side h-100">
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
        
        <mcr:showEditMenu mcrid="${mcrid}" cssClass="text-right pb-3" />
        <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/rightside_html.xsl" />
       </div>
      </div>
    </div><%-- right area --%>
  </div><%--row --%>
</div>
<%@ include file="fragments/footer.jspf" %>
</body>
</html>
