<%@ page language="java"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr"%>


<%-- creates the sitempa navigation menu by recursively calling itself--%>
<fmt:setLocale value="${lang}" />
<fmt:setBundle basename='messages' />
<x:out select="./@level"/>
<x:forEach select="$sessionScope:recNavPath/navitem[not(@hidden = 'true')]">
	<x:set var="href" select="string(./@path)" />
	<x:set var="labelKey" select="string(./@label)" />
    <x:set var="right" select="string(./@right)" /> 
    <c:if test="${right!=''}">
		<mcr:checkAccess var="canDo" permission="${right}" key="" /> 
    </c:if>
	<c:if test="${right=='' or canDo}">						
		<div class="sitemap-item">
			<a target="_self" href='${href}'><fmt:message key="${labelKey}" /></a>
			<x:if select="not(./navitem/@hidden='true')">
				<x:set scope="session" var="recNavPath" select="./navitem"/>
				<c:import url="/content/sitemap_items_rec.jsp" />
			</x:if>
		</div>
	</c:if>
</x:forEach>