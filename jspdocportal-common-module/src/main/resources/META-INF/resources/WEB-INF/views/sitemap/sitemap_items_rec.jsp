<%@ page language="java"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr"%>



<%-- creates the sitempa navigation menu by recursively calling itself--%>
<x:out select="./@level"/>
<x:forEach select="$sessionScope:recNavPath/*[(local-name()='navitem' or local-name()='navigation') and not(@hidden = 'true')]">
	<x:set var="href" select="concat('../', ./@href)" />
	<x:set var="labelKey" select="string(./@i18n)" />
    <x:set var="right" select="string(./@permission)" /> 
    <c:if test="${right!=''}">
		<mcr:hasAccess var="canDo" permission="${right}" /> 
    </c:if>
	<c:if test="${right=='' or canDo or true}">						
		<div class="sitemap-item" style="margin:6px 24px 6px 24px;  ">
			<a target="_self" href='${href}'><fmt:message key="${labelKey}" /></a>
			<x:set scope="session" var="recNavPath" select="./*[local-name()='navitem' or local-name()='navigation']"/>
			<c:import url="sitemap_items_rec.jsp" />
		</div>
	</c:if>
</x:forEach>