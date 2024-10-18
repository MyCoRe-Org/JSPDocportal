<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ taglib prefix="browse" tagdir="/WEB-INF/tags/ir/browse"%>

<mcr:setNamespace prefix="mods" uri="http://www.loc.gov/mods/v3" />
<mcr:setNamespace prefix="xlink" uri="http://www.w3.org/1999/xlink" />
  
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

<fmt:message var="pageTitle" key="OMD.docdetails">
	<fmt:param>${mcrid}</fmt:param>
</fmt:message>

<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI, '#epub')">
  <c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_browse.epub_details</c:set>
</x:if>
<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI, '#data')">
  <c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_browse.epub_details</c:set>
</x:if>
<x:if select="contains($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI, '#histbest')">
  <c:set var="org.mycore.navigation.path" scope="request">left.histbest.histbest_browse.histbest_details</c:set>
</x:if>

<!doctype html>
<html>
<head>
  <link rel="canonical" href="${WebApplicationBaseURL}resolve/id/${mcrid}" />
  <%@ include file="fragments/html_head.jspf" %>

  <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/metatags_html.xsl" />
  <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}modules/shariff_3.2.1/shariff.min.css">
  <script>
  var resolveDOIMetadataPage = function(doi) {
     <%--
	 //retrieve DOI Registration Agency as JSON:
     //[{ "DOI": "10.29085/9781783304868",
     //    "RA": "Crossref" }]
     
     configured as URL in identifiers classification:
     <category ID="doi">
       <label xml:lang="x-portal-url" text="javascript:resolveDOIMetadataPage('{0}');"/>
     </category>
     --%>
     $.ajax({
	    url: "https://doi.org/doiRA/"+doi,
	  })
	  .done(function( json ) {
	  	if(json[0].RA ==='DataCite'){
	  	  window.location.assign("https://commons.datacite.org/doi.org/"+doi);
	  	}
	  	else if(json[0].RA =='Crossref'){
	  	  window.location.assign("https://search.crossref.org/?from_ui=yes&q="+doi);
	  	}
	  	else if(json[0].RA =='mEDRA'){
	  		window.location.assign("https://www.medra.org/servlet/view?doi="+doi);
	  	}
	  	else{
	  	  window.location.assign("https://doi.org/doiRA/"+doi);
	  	}
	  });
   }
   
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
  <div id="content_area">
    <div class="container">
      <div class="row d-block d-lg-none" style="padding: 0px 15px">
        <div class="col-12" style="padding-top:45px">
          <search:result-navigator mcrid="${mcrid}" />
        </div>
      </div>
      <div class="row">  
        <div class="col-12 col-md-8"><%--main area --%>
		  <div class="row">
            <div class="col">
			  <div class="ir-docdetails-header">
                <x:choose>
                  <x:when select="$doc/mycoreobject/service/servstates/servstate[@categid='deleted']">
                    <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/deleted_header_html.xsl" />
                  </x:when>
                  <x:otherwise>
                    <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/header_html.xsl" />
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
          <c:set var="class_structure_active">
            <x:if select="contains($doc/mycoreobject/@ID, '_bundle_')"> active </x:if>
          </c:set>
		  <div class="row">
		    <div id="content_viewer_area" class="col">
			  <div class="mb-3">
                 <ul id="nav_bar_root" class="nav nav-tabs ir-docdetails-tabs">
                   <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
					<li id="nav_item_fulltext" class="nav-item" role="presentation">
                      <a id="nav_tab_fulltext" class="nav-link active" data-toggle="tab" data-target="#nav_content_fulltext" href="#tab_fulltext"><fmt:message key="Browse.Tabs.viewer" /></a>
                    </li>
  				    </x:if>
                   
  				   <li id="nav_item_structure" class="nav-item d-none" role="presentation">
                      <a id="nav_tab_structure" class="nav-link ${class_structure_active}" data-toggle="tab" data-target="#nav_content_structure" href="#tab_structure"><fmt:message key="Browse.Tabs.structure" /></a>
                   </li>
                   <x:if select="$doc/mycoreobject/metadata//*[@displayLabel='doctype'][contains(@valueURI, '/doctype#data')]">
                      <li id="nav_item_data" class="nav-item" role="presentation">
                        <a id="nav_tab_data" class="nav-link active" data-toggle="tab" data-target="#nav_content_data" href="#tab_data"><fmt:message key="Browse.Tabs.data" /></a>
                      </li>
                   </x:if>
				   <li id="nav_item_metadata" class="nav-item" role="presentation">
                      <a id="nav_tab_metadata" class="nav-link" data-toggle="tab" data-target="#nav_content_metadata" href="#tab_metadata"><fmt:message key="Browse.Tabs.metadata" /></a>
                   </li>
				   <x:if select="$doc/mycoreobject/structure/derobjects/derobject">
					  <li class="nav-item" role="presentation">
                        <a id="nav_tab_files" class="nav-link" data-toggle="tab" data-target="#nav_content_files" href="#tab_files"><fmt:message key="Browse.Tabs.files" /></a>
                      </li>
				   </x:if>
				  </ul>
			  </div>
			
              <div id="nav_content_root" class="tab-content" style="padding-bottom:75px">
		          <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
			        <div id="nav_content_fulltext" class="tab-pane active" data-parent="#nav_content_root">
				       <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]">
                         <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext']]/@xlink:href" /></c:set>
					      <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                          <c:if test="${not hasAccess}">
                           <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                           <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                              <mcr:displayCategory valueURI="${valueURI}" showDescription="true"/>
                            </div>
                          </c:if>
                          <c:if test="${hasAccess}">
                            <div id="divMCRViewer" style="height:80vh; margin:0px 16px; position:relative;"></div>
                            <search:mcrviewer mcrid="${it.id}" recordIdentifier="${it.id}" doctype="pdf" id="divMCRViewer" />
                          </c:if> 
				       </x:if>
				       <x:if select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]">
					     <c:set var="recordidentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier" /></c:set>
                         <c:set var="derid"><x:out select="$doc/mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='MCRVIEWER_METS']]/@xlink:href" /></c:set>
                         <mcr:hasAccess var="hasAccess" permission="read" mcrid="${derid}" />
                         <c:if test="${not hasAccess}">
                           <c:set var="valueURI"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:classification[@displayLabel='accesscondition']/@valueURI" /></c:set>
                           <div class="ir-box ir-box-bordered-emph" style="margin-bottom:30px">
                             <mcr:displayCategory valueURI="${valueURI}" showDescription="true"/>
                           </div>
                         </c:if>
                         <c:if test="${hasAccess}">
                           <div id="divMCRViewer" style="height:80vh; margin:0px 16px; position:relative;"></div>
                           <search:mcrviewer mcrid="${it.id}" recordIdentifier="${recordidentifier}" doctype="mets" id="divMCRViewer" />
                         </c:if>
				       </x:if>
			        </div>
		          </x:if>
			        <div id="nav_content_structure" class="tab-pane d-none ${class_structure_active}" data-parent="#nav_content_root">
				      <div style="font-size: 85%;min-height:600px">
			    	    <c:set var="recordIdentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier"/></c:set>
                        <c:set var="doctype"><x:out select="substring-after($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI,'#')"/></c:set>
                        <c:set var="zdbid"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='zdb']"/></c:set>

                        <browse:docdetails-structure hostRecordIdentifier="${recordIdentifier}" hostMcrID="${it.id}" hostDoctype="${doctype}" hostZDBID="${zdbid}" />
				      </div>
			        </div>
                  <script type="text/javascript">
                    //show structure tab and structure content area if children are available
                    window.addEventListener('DOMContentLoaded',function(){
                      let elemNavContentStructure = document.getElementById("nav_content_structure");
                  	  if(elemNavContentStructure.querySelector('.ir-structure-has-children')){
                  	    let elemNavItemStructure = document.getElementById("nav_item_structure");
                  	    elemNavItemStructure.classList.remove("d-none");
                        elemNavContentStructure.classList.remove("d-none");
                  	  }
                    });
                  </script>

                  <x:if select="$doc/mycoreobject/metadata//*[@displayLabel='doctype'][contains(@valueURI, '/doctype#data')]">
                    <div id="nav_content_data" class="tab-pane active" data-parent="#nav_content_root">
                      <div style="font-size: 85%;min-height:600px">
                        <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/download_html.xsl" />                 
                      </div>
                    </div>
                  </x:if>
		          <div id="nav_content_metadata" class="tab-pane" data-parent="#nav_content_root">
			        <div class="ir-docdetails-data" style="min-height:600px">
				       <x:choose>
				         <x:when select="$doc/mycoreobject/service/servstates/servstate/@categid='deleted'">
				           <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/deleted_details_html.xsl" />
				         </x:when>
					     <x:otherwise>
						   <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/metadata_html.xsl" />
					     </x:otherwise>
			          </x:choose>
			        </div>
		          </div>
		          <x:if select="$doc/mycoreobject/structure/derobjects/derobject">
			        <div id="nav_content_files" class="tab-pane" data-parent="#nav_content_root">
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
               $(window).on('load', function(){
                 if(urlParam('_mcrviewer_start')){
                   //[0] get Javascript object from Jquery object
                   $("#content_viewer_area")[0].scrollIntoView();
                 }
               });
               
               $(function() {
                 $('#nav_tab_fulltext').on('shown.bs.tab', function() {
                   <%--refresh viewer --%>
                   $('#divMCRViewer').trigger('resize');
                 });
                 
                 var hash = window.location.hash;
                 if(hash.startsWith('#tab_')){
                   $('#nav_'+hash.substr(1)).tab('show');
                 }
               });
             </script>
          </div>
       </div>
    </div><%-- main area --%>
    <div class="col col-md-4"> <%-- right area --%>
      <div class="ir-right_side h-100">
        <div class="d-none d-lg-block">
          <search:result-navigator mcrid="${mcrid}" />
          <mcr:showEditMenu mcrid="${mcrid}" cssClass="text-right pb-3" />
          <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/rightside_html.xsl" />
        </div>
      </div>
    </div><%-- right area --%>
  </div><%--row --%>
</div>
</div>
<%@ include file="fragments/footer.jspf" %>
</body>
</html>
