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
<table class="table searchresult-table">
	<tr>
		<td class="searchresult-table-icon" rowspan="10">
			<img src="${WebApplicationBaseURL}images/pubtype/${type}.gif" alt="${type}">
		</td> 
	
		<td class="searchresult-table-value" colspan="2">
			<x:forEach select="$xml/mycoreobject/metadata/creators/creator" varStatus="status" var="here">
            	<x:out select="$here//*[local-name()='foreName']"/>
            	<x:out select="$here//*[local-name()='surName']"/>
            	<x:if select="$here//*[local-name()='academicTitle']">
            		(<x:out select="$here//*[local-name()='academicTitle']" />)
            	</x:if>
            	<c:if test="${not status.last}">,</c:if>                            
            </x:forEach>
		
	</td>
	<td class="searchresult-table-id" rowspan="10">
			<jsp:include page="fragments/showeditbutton.jsp">
				<jsp:param name="mcrid" value="${param.id}" />
			</jsp:include>
		</td>
	</tr>
	
	<tr>
		<td class="searchresult-table-header" colspan="2">
			<c:set var="title"><x:out select="$xml/mycoreobject/metadata/titles/title[1]" /></c:set>
			<a href="${param.url}"><b>${title}</b></a>
		</td>
	</tr>
	<x:set var="data2" select="$xml/mycoreobject/metadata/types/type" /> 
	<x:if select="string-length($data2/@categid)>0">
		<tr>
			<td><fmt:message key="OMD.class-types" />:&#160;</td>
			<td class="searchresult-table-value">
 				<x:set var="classid" select="string($data2/@classid)" />
			    <x:set var="categid" select="string($data2/@categid)" />
				<mcr:displayClassificationCategory lang="de" classid="${classid}" categid="${categid}"/>
			</td>
		</tr>
	</x:if>
	
	<x:set var="data3" select="$xml/mycoreobject/metadata/dates/date[@type='accepted']" /> 
	<x:if select="string-length($data3)>0">
		<tr>
			<td><fmt:message key="OMD.yearofpublication" />:&#160; </td>
			<td class="searchresult-table-value">
				<x:set var="text" select="string($data3)" />
				<c:out value="${fn:substring(text,0,4)}"/>
			</td>
		</tr>
	</x:if>
	
	
	<x:set var="data4" select="$xml/mycoreobject/metadata/descriptions/description" /> 
	<x:if select="string-length($data4)>0">
		<c:set var="text"><x:out select="$data4" /></c:set>
		<c:if test="${fn:length(text)>255}">
			<c:set var="text"><c:out value="${fn:substring(text,0,250)}"/>...</c:set>
		</c:if>
		<tr>
			<td><fmt:message key="OMD.descriptions" />:&#160; </td>
			<td class="searchresult-table-value">
				<c:out value="${text}"/>
			</td>
		</tr>
	</x:if>
	
	<x:set var="data5" select="$xml/mycoreobject/service/servdates/servdate[@type='modifydate']" />
	<x:if select="string-length($data5)>0">
		<tr>
			<td><fmt:message key="OMD.id" />:&#160;</td>
			<td class="searchresult-table-value">
			    <c:set var="x"><x:out select="$data5" /></c:set>
			    <fmt:parseDate value="${x}" pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" var="moddate" />
			    <x:out select="$xml/mycoreobject/@ID" />&#160;
			    <em>(<fmt:message key="OMD.changed-at" />: <fmt:formatDate value="${moddate}" dateStyle="medium" />)</em>
			</td>
		</tr>
	</x:if>
 </table>
