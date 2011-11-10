<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x"uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.admin.Process" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="contents">
<div class="headline"><fmt:message key="Webpage.admin.Process" /></div>

<c:set var="debug" value="false" />

<c:set var="pid" value="${param.pid}" />
<c:set var="type" value="${param.workflowProcessType}" />


<c:if test="${not empty pid}">
	<mcr:deleteProcess result="result" pid="${pid}"
		workflowProcessType="${type}" />
	<table class="access" cellspacing="1" cellpadding="0">
		<tr>
			<td>zu löschender Prozess: <c:out value="${pid}" /></td>
		</tr>
		<tr>
			<td>Typ: <c:out value="${type}" /></td>
		</tr>
		<tr>
			<td colspan="2">Status: <fmt:message key="${result}" /></td>
		</tr>
	</table>
	<hr />
</c:if>


<mcr:listWorkflowProcess var="processlist" workflowProcessType="${type}" />

<table class="access" cellspacing="0" cellpadding="3" >
	<x:forEach select="$processlist/processlist">
		<x:set var="type" select="string(./@type)" />
		<tr >
			<th colspan="2">Prozessdaten vom Typ: ${type}</th>
			<th>Löschen</th>
		</tr>
		<x:forEach select="./process">
			<x:set var="pid" select="string(./@pid)" />
			<x:set var="status" select="string(./@status)" />
			<tr>
				<td>
				<table cellpadding="1" cellspacing="0">
					<tr>
						<td valign="top"><b>Prozess Nr: <x:out select="./@pid" /></b></td>
						<td width="30px">&nbsp;</td>
						<td valign="top"><b>Status: <x:out select="./@status" /></b></td>
					</tr>
					<x:forEach select="./variable">
						<tr valign="top">
								<td><x:out select="./@name" /></td>
								<td width="30px">&nbsp;</td>
								<td><x:out select="./@value" /></td>
						</tr>
					</x:forEach>
				 </table>
				</td>
				<td width="50"></td>
				<td align="center">
				<form method="get"
					action="${applicationScope.WebApplicationBaseURL}nav"><input
					type=hidden name="path" value="~process-${type}" /> <input
					type=hidden name="pid" value="${pid}" /> <input type="image"
					title="Prozess löschen"
					src="${applicationScope.WebApplicationBaseURL}admin/images/delete.gif"
					onClick="return questionDel()"></form>
				</td>
			</tr>
		 <tr><th colspan="3" height="1px" ></th></tr>
		</x:forEach>
	</x:forEach>
</table>
	</stripes:layout-component>
</stripes:layout-render>    
