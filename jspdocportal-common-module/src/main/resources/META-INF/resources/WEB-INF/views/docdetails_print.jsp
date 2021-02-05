<%@page import="org.hibernate.resource.transaction.spi.TransactionStatus"%>
<%@page import="org.mycore.backend.hibernate.MCRHIBConnection"%>
<%@page import="org.mycore.common.MCRException"%>
<%@page import="org.hibernate.Transaction"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%--
     Make the JSTL-core and the JSTL-fmt taglibs available
     within this page. JSTL is the Java Standard Tag Library,
     which defines a set of commonly used elements
     The prefix to be used for core-tags is "c"
     The prefix to be used for fmt-tags is "fmt"
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:set var="from"  value="${param.from}" />

<html>
<head><title>Print Details</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="shortcut icon" href="${applicationScope.WebApplicationBaseURL}images/icon_cpr.ico" />
	<link type="text/css" rel="stylesheet" href="${applicationScope.WebApplicationBaseURL}css/style_reset.css" />
	<link type="text/css" rel="stylesheet" href="${applicationScope.WebApplicationBaseURL}css/style_layout.css" />
	<link type="text/css" rel="stylesheet" href="${applicationScope.WebApplicationBaseURL}css/style_content.css" />
	<link type="text/css" rel="stylesheet" href="${applicationScope.WebApplicationBaseURL}css/style_docdetails_headlines.css" />
	<style type="text/css">
		body{
		background: white;
		}
	</style>
</head>
<body bgcolor="#FFFFFF">
<%
    Transaction tx  = MCRHIBConnection.instance().getSession().beginTransaction();
	try{
%>
<div class="base_content text">

<c:set var="mcrid" value="${param.id}" />

<c:choose>
 <c:when test="${fn:contains(mcrid,'codice')}">
     <c:import url="docdetails/docdetails-codice.jsp" />
 </c:when>
 
 <c:when test="${fn:contains(mcrid,'thesis')}">
     <c:import url="docdetails/docdetails_thesis.jsp" />
 </c:when>
 
 <c:when test="${fn:contains(mcrid,'disshab')}">
     <c:import url="docdetails/docdetails_disshab.jsp" />
 </c:when>
 
 
 <c:when test="${fn:contains(mcrid,'person')}">
     <c:import url="docdetails/docdetails_person.jsp" />
 </c:when>
 
  <c:when test="${fn:contains(mcrid,'institution')}">
     <c:import url="docdetails/docdetails_institution.jsp" />
 </c:when> 
 
  <c:when test="${fn:contains(mcrid,'document')}">
     <c:import url="docdetails/docdetails_document.jsp" />
 </c:when>

 <c:when test="${fn:contains(mcrid,'_bundle_')}">
     <c:import url="docdetails/docdetails_bundle.jsp" />
 </c:when>
 
 <c:otherwise>
     <c:import url="docdetails/docdetails-document.jsp" />
 </c:otherwise>
 </c:choose>
 </div>
<% }	
	catch(MCRException e){
		pageContext.getOut().append(e.getMessage());
	}
	finally{
		if (!(tx.getStatus() == TransactionStatus.COMMITTED)){
			tx.commit();
		}
	}
	%>
</body>
</html>