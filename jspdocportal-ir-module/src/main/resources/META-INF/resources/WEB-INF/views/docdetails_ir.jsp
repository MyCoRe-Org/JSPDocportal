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
  <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}modules/socializer_f794acd/css/socializer.min.css" />

  <script src="${WebApplicationBaseURL}javascript/jspdocportal-util.js"></script>

  <script type="text/javascript">
    document.addEventListener("DOMContentLoaded", () => {
      JSPDocportalUtil.initPopovers();
    });
    
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
          <div class="row">
            <div id="content_viewer_area" class="col">
              <div class="mb-3">
                <ul id="tabbar_root" class="nav nav-tabs ir-docdetails-tabs">
                  <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
                    <li class="nav-item" role="presentation">
                      <a id="tab_fulltext" class="nav-link" data-bs-toggle="tab" href="#tabcontent_fulltext"><fmt:message key="Browse.Tabs.viewer" /></a>
                    </li>
                  </x:if>
                  <li class="nav-item" role="presentation">
                    <a id="tab_structure" class="nav-link d-none" data-bs-toggle="tab" href="#tabcontent_structure"><fmt:message key="Browse.Tabs.structure" /></a>
                  </li>
                  <x:if select="$doc/mycoreobject/metadata//*[@displayLabel='doctype'][contains(@valueURI, '/doctype#data')]">
                    <li class="nav-item" role="presentation">
                      <a id="tab_data" class="nav-link active" data-bs-toggle="tab" href="#tabcontent_data"><fmt:message key="Browse.Tabs.data" /></a>
                    </li>
                  </x:if>
                  <li class="nav-item" role="presentation">
                    <a id="tab_metadata" class="nav-link" data-bs-toggle="tab" href="#tabcontent_metadata"><fmt:message key="Browse.Tabs.metadata" /></a>
                  </li>
                  <x:if select="$doc/mycoreobject/structure/derobjects/derobject">
                    <li class="nav-item" role="presentation">
                      <a id="nav_tab_files" class="nav-link" data-bs-toggle="tab" href="#tabcontent_files"><fmt:message key="Browse.Tabs.files" /></a>
                    </li>
                  </x:if>
                </ul>
             </div>

              <div id="tabcontent_root" class="tab-content" style="padding-bottom:75px">
                <x:if select="$doc/mycoreobject[not(contains(@ID, '_bundle_'))]/structure/derobjects/derobject[classification[@classid='derivate_types'][@categid='fulltext' or @categid='MCRVIEWER_METS']]">
                  <div id="tabcontent_fulltext" class="tab-pane fade" aria-labelledby="tab_fulltext">
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
                <div id="tabcontent_structure" class="tab-pane fade" aria-labelledby="tab_structure">
                  <div style="font-size: 85%;min-height:600px">
                    <c:set var="recordIdentifier"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo/mods:recordIdentifier"/></c:set>
                    <c:set var="doctype"><x:out select="substring-after($doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:genre[@displayLabel='doctype']/@valueURI,'#')"/></c:set>
                    <c:set var="zdbid"><x:out select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier[@type='zdb']"/></c:set>
                   <browse:docdetails-structure hostRecordIdentifier="${recordIdentifier}" hostMcrID="${it.id}" hostDoctype="${doctype}" hostZDBID="${zdbid}" />
                  </div>
                </div>
                <x:if select="$doc/mycoreobject/metadata//*[@displayLabel='doctype'][contains(@valueURI, '/doctype#data')]">
                  <div id="tabcontent_data" class="tab-pane fade" aria-labelledby="tab_fulltext">
                    <div style="font-size: 85%;min-height:600px">
                      <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/download_html.xsl" />
                    </div>
                  </div>
                </x:if>
                <div id="tabcontent_metadata" class="tab-pane fade" aria-labelledby="tab_metadata">
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
                  <div id="tabcontent_files" class="tab-pane fade" aria-labelledby="tab_files">
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
              </div><%--END: tabcontent_root --%>

              <script type="text/javascript">
                document.addEventListener("DOMContentLoaded", () => {
                  // 1) show structure tab (if present)
                  const elemTabStructure = document.getElementById("tab_structure");
                  const elemTabContentStructure = document.getElementById("tabcontent_structure");
                  if(elemTabContentStructure.querySelector('.ir-structure-has-children')){
                    elemTabStructure.classList.remove("d-none");
                    bootstrap.Tab.getOrCreateInstance(elemTabStructure).show();
                  }
                  
                  // 2) show fulltext (viewer) and update MyCoReViewer (on resize-event)
                  const elemTabFulltext = document.getElementById('tab_fulltext');
                  if(elemTabFulltext) {
                    elemTabFulltext.addEventListener('shown.bs.tab', event => {
                      document.getElementById('divMCRViewer').dispatchEvent(new Event('resize'));
                    });
                    const urlParams = new URLSearchParams(window.location.search);
                    if(urlParams.has('_mcrviewer_start')) {
                      bootstrap.Tab.getOrCreateInstance(elemTabFulltext).show();
                      document.getElementById('content_viewer_area').scrollIntoView();
                      return;
                    }
                  }
                  
                  //if URL contains anchor with tab name - open it.
                  const hash = window.location.hash;
                  if(hash && hash.startsWith('#tab_')) {
                    const elemForHash = document.querySelector(hash);
                    if(elemForHash) {
                      bootstrap.Tab.getOrCreateInstance(elemForHash).show();
                      return;
                    }
                  }
                  
                  //4) fallback: open first tab in tabbar
                  const elemTabFirst = document.querySelector('#tabbar_root a[data-bs-toggle="tab"]:not(.d-none)');
                  bootstrap.Tab.getOrCreateInstance(elemTabFirst).show();
                });
              </script>
            </div>
          </div>
        </div><%-- main area --%>
        <div class="col col-md-4"> <%-- right area --%>
          <div class="ir-right_side h-100">
            <div class="d-none d-lg-block">
              <search:result-navigator mcrid="${mcrid}" />
              <mcr:showEditMenu mcrid="${mcrid}" cssClass="text-end pb-3" />
              <mcr:transformXSL dom="${doc}" xslt="xslt/docdetails/rightside_html.xsl" />
            </div>
          </div>
        </div><%-- right area --%>
      </div>
    </div>
  </div>
  <%@ include file="fragments/footer.jspf" %>
</body>
</html>
