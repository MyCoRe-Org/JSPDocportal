<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrdd" uri="http://www.mycore.org/jspdocportal/docdetails.tld" %>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />

<c:set var="pageTitle"><fmt:message key="PDF.download.pageTitle" /></c:set>

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_docdetails.css">
  <%@ include file="fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div class="container">
     <div class="row" style="margin-bottom:30px;">
      <div class="col-xs-12">
        <h2><fmt:message key="PDF.download.headline.download" /></h2>   
      </div>
    </div>
    
    <div class="row">
      <div class="col-xs-12">
          <c:forEach var="msg" items="${it.errorMessages}">
	 	     <p style="font-size:125%; color:darkred"><c:out value="${msg}" escapeXml="false" /></p>
	       </c:forEach>
      </div>
    </div>
   
	 
	 <c:if test="${empty it.errorMessages}">
        <mcr:retrieveObject query="recordIdentifier:${fn:replace(it.recordIdentifier, 'rosdok_', 'rosdok/')}" varDOM="doc" />
		<mcrdd:setnamespace prefix="mods" uri="http://www.loc.gov/mods/v3" />
		<x:choose>
   		<x:when select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo">
            <div class="row">
    			<c:set var="mcrid"><x:out select="$doc/mycoreobject/@ID" /></c:set>
                <div class="col-xs-8">
                  <mcr:transformXSL dom="${doc}" xslt="xsl/docdetails/document2header_html.xsl" />      
                </div>
                <div class="col-xs-2 col-xs-offset-1">
                  <search:derivate-image mcrobj="${doc}" width="100%" category="cover" />
                </div>      
            </div>
     	</x:when>
		</x:choose>
		
     <div class="row">
      <div class="col-xs-12 ir-divider">
        <hr/>
      </div>
    </div>
     <div class="row">
      <div class="col-xs-8">
 
         	<c:url var="imgIconUrl" value="/images/download_pdf.png" />
			<c:choose>
				<c:when test="${it.ready}">
					<div class="ir-box" style="margin:-15px">
                       <c:url var="url" value="/pdfdownload/recordIdentifier/${it.recordIdentifier}/${it.filename}" />
					   <a href="${url}" class="btn btn-default ir-button-download" style="font-size:150%;padding:15px;">
                          <img src="${imgIconUrl}" style="height:60px;"/>&nbsp;&nbsp;${it.filename} &nbsp;&nbsp;&nbsp; <small>(${it.filesize})</small>
                       </a>
					</div>
                    <div class="ir-box-teaser">
					 <h3><fmt:message key="PDF.download.headline.hint" /></h3>
					   <ul style="padding-left:24px">
                         <fmt:message key="PDF.download.hint" />
					   </ul>
                    </div>
				</c:when>
				<c:otherwise>
					<c:set var="progress" value="${it.progress}" />
					<c:choose>
						<c:when test="${progress < 0}">
					     	<c:url var="url" value="/pdfdownload/recordIdentifier/${it.recordIdentifier}/${it.filename}" />
							<div class="ir-box" style="margin:-15px">
								<a href="${url}"><img src="${imgIconUrl}" style="vertical-align:middle;" />&nbsp;&nbsp;<fmt:message key="PDF.download.generate" /></a>
							</div>
							<div class="ir-box-teaser">
							<h3><fmt:message key="PDF.download.headline.hint" /></h3>
							<ul style="padding-left:30px">
								<li><fmt:message key="PDF.download.generate.hint" /></li>
								</ul>
                            </div>
						</c:when>
						<c:otherwise>
                          <div class="ir-box-teaser">
							<h3><fmt:message key="PDF.download.generate.file" /></h3>
							<progress style="width:100%" id="progressBar" max="100" value="${progress}"></progress>
						  </div>
                          <div class="ir-box-teaser">
							 <h3><fmt:message key="PDF.download.headline.hint" /></h3>
							 <ul style="padding-left:30px">
								    <li><fmt:message key="PDF.download.patient.hint" /></li>
							 </ul>
                          </div>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
            
            <div style="padding-bottom: 100px; text-align: center;"></div>
 		</div>
    </div>
 		<c:if test="${progress >= 0 or fn:endsWith(it.requestURL, it.filename)}">
			<script>	
				function refresh() {
					setTimeout(function () {
        				location.reload()
    					}, 3000);
					}
					window.onload=refresh;
			</script>	
		</c:if>
 	</c:if>

   </div>
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>