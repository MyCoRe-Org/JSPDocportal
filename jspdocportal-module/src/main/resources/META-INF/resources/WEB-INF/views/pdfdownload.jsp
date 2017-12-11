<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrdd" uri="http://www.mycore.org/jspdocportal/docdetails.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />

<c:set var="pageTitle"><fmt:message key="PDF.download.pageTitle" /></c:set> 
<stripes:layout-render name="../../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="html_header">
		<title>${pageTitle} @ <fmt:message key="Webpage.title" /></title>
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_docdetails.css">
	</stripes:layout-component>
	<stripes:layout-component name="main_part">
    <div class="row" style="margin-bottom:30px;">
      <div class="col-xs-12">
        <h2><fmt:message key="PDF.download.headline.download" /></h2>   
      </div>
    </div>
    
    <div class="row">
      <div class="col-xs-12">
          <c:forEach var="msg" items="${actionBean.errorMessages}">
	 	     <p style="font-size:125%; color:darkred"><c:out value="${msg}" escapeXml="false" /></p>
	       </c:forEach>
      </div>
    </div>
   
	 
	 <c:if test="${empty actionBean.errorMessages}">
		<c:import url="${WebApplicationBaseURL}api/v1/objects/recordIdentifier:${actionBean.recordIdentifier}" var="docXML" charEncoding="UTF-8"/>
		<x:parse var="doc" xml="${docXML}" />
		<mcrdd:setnamespace prefix="mods" uri="http://www.loc.gov/mods/v3" />
		<x:choose>
   		<x:when select="$doc/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:recordInfo">
            <div class="row">
    			<c:set var="mcrid"><x:out select="$doc/mycoreobject/@ID" /></c:set>
                <div class="col-xs-8">
                  <mcr:transformXSL xml="${doc}" xslt="xsl/docdetails/document2header_html.xsl" />      
                </div>
                <div class="col-xs-2 col-xs-offset-1">
                  <search:derivate-image mcrid="${mcrid}" width="100%" labelContains="cover" />
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
				<c:when test="${actionBean.ready}">
					<div class="ir-box" style="margin:-15px">
                       <c:url var="url" value="/pdfdownload/recordIdentifier/${actionBean.recordIdentifier}/${actionBean.filename}" />
					   <a href="${url}" class="btn btn-default ir-button ir-button-download" style="font-size:150%;padding:15px;">
                          <img src="${imgIconUrl}" style="height:60px;"/>&nbsp;&nbsp;${actionBean.filename} &nbsp;&nbsp;&nbsp; <small>(${actionBean.filesize})</small>
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
					<c:set var="progress" value="${actionBean.progress}" />
					<c:choose>
						<c:when test="${progress < 0}">
					     	<c:url var="url" value="/pdfdownload/recordIdentifier/${actionBean.recordIdentifier}/${actionBean.filename}" />
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
 		<c:if test="${progress >= 0 or fn:endsWith(actionBean.requestURL, actionBean.filename)}">
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

 	</stripes:layout-component>
	
</stripes:layout-render>
