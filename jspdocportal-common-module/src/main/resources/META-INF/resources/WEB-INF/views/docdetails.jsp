<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

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

<!doctype html>
<html>
<head>
  <title>${mcrid} @ <fmt:message key="Nav.Application" /></title>
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
  <div id="content_area">
    <div class="container">
      <div class="row d-block d-lg-none" style="padding: 0px 15px">
        <div class="col-12" style="padding-top:45px">
		  <div class="ir-nav-search-back ir-nav-search ir-box text-right" style="padding:0px 0px 30px 0px">
             <a class="btn btn-primary" href="${WebApplicationBaseURL}/do/browse/epub" class="btn btn-primary btn-sm">
			    <i class="fas fa-search"></i>
				<fmt:message key="Webpage.docdetails.newsearch" />
			</a>
 		  </div>
          <search:result-navigator mcrid="${mcrid}" />
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
          
    </div><%-- main area --%>
    <div class="col-xs-12 col-md-4"> <%-- right area --%>
       <div class="ir-right_side h-100">
         <div class="d-none d-lg-block">
     	    <search:result-navigator mcrid="${mcrid}" />
            <mcr:showEditMenu mcrid="${mcrid}" cssClass="text-right pb-3" />
            <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/rightside_html.xsl" />
         </div>
      </div>
    </div><%-- right area --%>
  </div><%--row --%>
  </div>
  </div>
<%@ include file="fragments/footer.jspf" %>
</body>
</html>
