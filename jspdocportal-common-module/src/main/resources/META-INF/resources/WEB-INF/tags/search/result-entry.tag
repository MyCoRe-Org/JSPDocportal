<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="url" required="true" type="java.lang.String"%>
<%@ attribute name="entry" required="true" type="org.mycore.frontend.jsp.search.MCRSearchResultEntry"%>
<%@ attribute name="protectDownload" type="java.lang.Boolean"  %>

<div class="row">
	<div class="col-sm-9">
		<search:show-edit-button mcrid="${entry.mcrid}" cssClass="btn btn-primary ir-edit-btn pull-right" /> 
		<h4 class="card-title">
			<a href="${url}">${entry.label}</a>
		</h4>
		<table class="ir-result-table card-text"
			style="border-spacing: 4px; border-collapse: separate; font-size: 100%">
			<c:forEach var="d" items="${entry.data}">
				<tr class="ir-result-entry-${fn:replace(d.key,'.','_')}">
					<th><fmt:message
							key="Webpage.searchresult.${entry.objectType}.label.${d.key}" />:&#160;</th>
					<c:choose>
						<c:when test="${fn:endsWith(d.key, '_msg')}">
							<td><fmt:message key="${d.value}" /></td>
						</c:when>
						<c:when test="${fn:endsWith(d.key, '_class')}">
							<td><mcr:displayClassificationCategory
									classid="${fn:substringBefore(d.value,':')}"
									categid="${fn:substringAfter(d.value,':')}" lang="de" /></td>
						</c:when>
						<c:otherwise>
							<td><c:out value="${fn:replace(d.value, '|', '<br />')}"
									escapeXml="false" /></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
		</table>
	</div>
	<c:if test="${not empty entry.coverURL}">
		<div class="col-md-3 d-none d-md-block">
			<div class="img-thumbnail pull-right ir-result-image">
				<div style="position:relative">
   					<c:if test="${protectDownload}">
   						<img style="opacity:0.001;position:absolute;top:0px;left:0px;width:100%;height:100%;z-index:1" src="${pageContext.request.contextPath}/images/image_terms_of_use.png"/>
	   				</c:if>
   					<img style="position:relative;top:0px;left:0px;width:98%;padding:1%;display:block;" src="${pageContext.request.contextPath}/${entry.coverURL}" border="0" />
				</div>
			</div>
		</div>
	</c:if>
</div>