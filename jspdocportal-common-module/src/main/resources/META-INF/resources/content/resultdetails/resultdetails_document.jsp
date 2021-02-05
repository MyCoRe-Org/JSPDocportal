<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.mycore.org/jspdocportal/docdetails.tld" prefix="mcrdd" %>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%-- Parameter: id - the MCR Object ID--%>
<%-- Parameter: url - the MCR Object ID--%>

<mcr:retrieveObject mcrid="${param.id}" varDOM="xml"/>
<c:set var="type" value="${fn:substringBefore(fn:substringAfter(param.id, '_'),'_')}" />

<c:set var="formatType"><x:out select="$xml/mycoreobject/metadata/formats/format/@categid"/></c:set>
<c:set var="contentType"><x:out select="$xml/mycoreobject/metadata/types/type/@categid"/></c:set>

<table class="table searchresult-table">
	<tr>
		<td class="searchresult-table-icon" rowspan="10">
			<jsp:include page="fragments/document_icon.jsp">
				<jsp:param name="contentType" value="${contentType}" />
				<jsp:param name="formatType" value="${formatType}" />
			</jsp:include>		
		</td>
		<td class="searchresult-table-header">
			<c:set var="title"><x:out select="$xml/mycoreobject/metadata/titles/title[1]" /></c:set>
			<a href="${param.url}"><b>${title}</b></a>
		</td>
		<td class="searchresult-table-id" rowspan="10">
			[<x:out select="$xml/mycoreobject/@ID" />]
			<br /><br />
			<jsp:include page="fragments/showeditbutton.jsp">
				<jsp:param name="mcrid" value="${param.id}" />
			</jsp:include>
		</td>
	</tr>
 
	<x:set var="data1a" select="substring-before(concat($xml/metadata/creatorlinks/creatorlink[1]/@*[local-name()='title'],'; ',$xml/metadata/creatorlinks/creatorlink[2]/@*[local-name()='title'],'; ',$xml/creatorlinks/creatorlink[3]/@*[local-name()='title'],'; ,'),'; ;')" /> 
	<x:set var="data1b" select="substring-before(concat($xml/metadata/creators/creator[1],'; ',$xml/metadata/creators/creator[2],'; ',$xml/metadata/creators/creator[3],'; ,'),'; ;')" />
	<c:set var="data1"><x:out select="$data1a" /></c:set>
	<x:if select="string-length($data1b)>0">
		<c:if test="${fn:length(data1) > 0}">
			<c:set var="data1"><c:out value="${data1}" />;&#160;<x:out select="$data1b" /></c:set>
		</c:if>
		<c:if test="${fn:length(data1) eq 0}">
			<c:set var="data1"><x:out select="$data1b" /></c:set>
		</c:if>
	</x:if>
	<c:if test="${fn:length(data1) > 0}"> 
		<tr>
			<td class="searchresult-table-value">
				<fmt:message key="OMD.author" />:&#160; <c:out value="${data1}" escapeXml="false"/>
			</td>
		</tr>
	</c:if>
	<x:set var="data2" select="$xml/mycoreobject/metadata/types/type" /> 
	<x:if select="string-length($data2/@categid)>0">
		<tr>
			<td class="searchresult-table-value">
			    <c:set var="classid"><x:out select="$data2/@classid" /></c:set>
			    <c:set var="categid"><x:out select="$data2/@categid" /></c:set>
			    
				<fmt:message key="OMD.class-types" />:&#160; 
				<mcr:displayClassificationCategory lang="de" classid="${classid}" categid="${categid}"/>
			</td>
		</tr>
	</x:if>
	<x:set var="data3" select="$xml/mycoreobject/metadata/formats/format" /> 
	<x:if select="string-length($data3/@categid)>0">
		<tr>
			<td class="searchresult-table-value">
			    <c:set var="classid"><x:out select="$data3/@classid" /></c:set>
			    <c:set var="categid"><x:out select="$data3/@categid" /></c:set>
			    
				<fmt:message key="OMD.class-formats" />:&#160;
				<mcr:displayClassificationCategory lang="de" classid="${classid}" categid="${categid}"/>
			</td>
		</tr>
	</x:if>
	
	<x:set var="data4" select="$xml/mycoreobject/metadata/descriptions/description" /> 
	<x:if select="string-length($data4)>0">
		<c:set var="text"><x:out select="$data4" /></c:set>
		<c:if test="${fn:length(text)>300}">
			<c:set var="text"><c:out value="${fn:substring(text,0,300)}" escapeXml="false"/>...</c:set>
		</c:if>
		<tr>
			<td class="searchresult-table-value">
				<fmt:message key="OMD.descriptions" />:&#160; <c:out value="${text}" escapeXml="false"/>
			</td>
		</tr>
	</x:if>
	
	<x:set var="data5" select="$xml/mycoreobject/service/servdates/servdate[@type='modifydate']" />
	<x:if select="string-length($data5)>0">
		<tr>
			<td class="searchresult-table-value">
			    <c:set var="x"><x:out select="$data5" /></c:set>
			    <fmt:parseDate value="${x}" pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" var="moddate" />
			    <fmt:message key="OMD.changed" />:&#160; <fmt:formatDate value="${moddate}" dateStyle="full" />
			</td>
		</tr>
	</x:if>
 </table>
