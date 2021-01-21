<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>
<%--Parameter: mcrid --%>

<c:set var="mcrid" value="${param.mcrid}" />

<mcr:retrieveObject mcrid="${mcrid}" varJDOM="jdom" varDOM="dom" cache="true"/>
<c:if test="${not empty jdom}" >
	<c:set var="type"><mcr:simpleXpath jdom="${jdom}" xpath="/mycoreobject/metadata/types/type/@categid" /></c:set>
	<c:set var="format"><mcr:simpleXpath jdom="${jdom}" xpath="/mycoreobject/metadata/formats/format/@categid"/></c:set>
	<c:set var="title"><mcr:simpleXpath jdom="${jdom}" xpath="/mycoreobject/metadata/titles/title[@type='short']" /></c:set>
	<c:if test="${empty title}">
		<c:set var="title"><mcr:simpleXpath jdom="${jdom}" xpath="/mycoreobject/metadata/titles/title[1]" /></c:set>
	</c:if>
	<jsp:include page="/content/resultdetails/fragments/document_icon.jsp">
		<jsp:param name="contentType" value="${type}" />
		<jsp:param name="formatType" value="${format}" />
		<jsp:param name="docType" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
	</jsp:include>		
	<b><c:out value='${title}' /></b>
	<c:if test="${not empty param.volume}" >;&#160;<c:out value="${param.volume}" /></c:if>	
	:<br />
</c:if>
<c:if test="${empty jdom}" ><br />Could not load document</c:if>
