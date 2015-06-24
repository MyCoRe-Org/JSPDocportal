<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrb" uri="http://www.mycore.org/jspdocportal/browsing.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="jspdp-ui" tagdir="/WEB-INF/tags/ui" %>

<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />
<c:set var="mcrid">
   <c:choose>
      <c:when test="${!empty(requestScope.id)}">${requestScope.id}</c:when>
      <c:otherwise>${param.id}</c:otherwise>
   </c:choose>
</c:set>
<c:set var="from"  value="${param.fromWF}" /> 
<c:set var="debug" value="${param.debug}" />
<c:set var="style" value="${param.style}" />
<c:set var="type"  value="${fn:split(mcrid,'_')[1]}" />

<fmt:message var="pageTitle" key="OMD.headline">
	<fmt:param>${mcrid}</fmt:param>
</fmt:message> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}" layout="2columns_wide">
	<stripes:layout-component name="html_header">
		<title>${pageTitle} @ <fmt:message key="Webpage.title" /></title>
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_docdetails.css">
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_searchresult.css">
	</stripes:layout-component>
	<stripes:layout-component name="contents">
	<h2><fmt:message key="OMD.docdetails" /></h2>
	<div class="menu" style="float:right; min-width:30px;min-height:1px">
        <jspdp-ui:toolbar-edit mcrid="${mcrid}" />
    </div>

	<c:catch var="e">
			<c:choose>
 				<c:when test="${from}" >
     				<c:set var="layout" value="preview" />
 				</c:when>
 				<c:otherwise>
     				<c:set var="layout" value="normal" />
 				</c:otherwise> 
			</c:choose>
						<c:choose>
 							<c:when test="${fn:contains(mcrid,'codice')}">
     							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_codice.jsp?id=${mcrid}&fromWF=${from}" />
 							</c:when>
   
 							<c:when test="${fn:contains(mcrid,'thesis')}">
 							     <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_thesis.jsp?id=${mcrid}&fromWF=${from}" />
  							</c:when>
 
  							<c:when test="${fn:contains(mcrid,'disshab')}">
 							     <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_disshab.jsp?id=${mcrid}&fromWF=${from}" />
  							</c:when>
 
  							<c:when test="${fn:contains(mcrid,'person')}">
 							     <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_person.jsp?id=${mcrid}&fromWF=${from}" />
  							</c:when>
 
   							<c:when test="${fn:contains(mcrid,'institution')}">
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_institution.jsp?id=${mcrid}&fromWF=${from}" />
  							</c:when> 
 
   							<c:when test="${fn:contains(mcrid,'document')}">
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_document.jsp?id=${mcrid}&fromWF=${from}" />
 							 </c:when>
   							<c:when test="${fn:contains(mcrid,'_bundle_')}">
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_bundle.jsp?id=${mcrid}&fromWF=${from}" />
 							 </c:when>
  
  							<c:otherwise>
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails-document.jsp?id=${mcrid}&fromWF=${from}" />
  							</c:otherwise>
  						</c:choose>
		</c:catch>
		<c:if test="${e!=null}">
			An error occured, hava a look in the logFiles!
			<% 
  				Logger.getLogger("test.jsp").error("error", (Throwable) pageContext.getAttribute("e"));   
			%>
		</c:if>
	</stripes:layout-component>
	 <stripes:layout-component name="right_side">
		<div class="base_box infobox">
			<div class="docdetails-toolbar">
				<div class="docdetails-toolbar-item">
					<mcrb:searchDetailBrowser/>
				</div>
				<div style="clear: both;"></div>
			</div>
    		<div class="docdetails-toolbar">
    			<c:if test="${empty(param.print) and !fn:contains(style,'user')}">
    				<div class="docdetails-toolbar-item">
		     			<a href="${WebApplicationBaseURL}content/print_details.jsp?id=${param.id}&fromWF=${param.fromWF}" target="_blank">
	          				<img src="${WebApplicationBaseURL}images/workflow_print.gif" border="0" alt="<fmt:message key="WF.common.printdetails" />"  class="imagebutton" height="30"/>
	         			</a>
	         		</div>
     			</c:if>
   				<c:if test="${(not from) && !fn:contains(style,'user')}" > 
 					<mcr:checkAccess var="modifyAllowed" permission="writedb" key="${mcrid}" />
    				<mcr:isObjectNotLocked var="bhasAccess" objectid="${mcrid}" />
    				<c:if test="${modifyAllowed}">
      					<c:choose>
    						<c:when test="${bhasAccess}"> 
	         					<!--  Editbutton -->
	         					<div class="docdetails-toolbar-item">
	         						<form method="get" action="${WebApplicationBaseURL}StartEdit" class="resort">                 
	            						<input name="mcrid" value="${mcrid}" type="hidden"/>
										<input title="<fmt:message key="WF.common.object.EditObject" />" src="${WebApplicationBaseURL}images/workflow1.gif" type="image"  class="imagebutton" height="30" />
	         						</form>
	         					</div>
         					</c:when>
         					<c:otherwise>
         						<div class="docdetails-toolbar-item">
         	  						<img title="<fmt:message key="WF.common.object.EditObjectIsLocked" />" border="0" src="${WebApplicationBaseURL}images/workflow_locked.gif" height="30" />
         	  					</div>
         					</c:otherwise>
        				</c:choose>  
        			</c:if>
    			</c:if>
   				<div style="clear:both;"></div>
			</div>
		</div>
	</stripes:layout-component>
</stripes:layout-render>