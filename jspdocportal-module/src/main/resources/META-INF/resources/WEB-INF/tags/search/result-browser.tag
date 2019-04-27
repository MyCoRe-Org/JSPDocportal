<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="result" required="true" type="org.mycore.frontend.jsp.search.MCRSearchResultDataBean" %>

<%@ variable name-given="mcrid" %>
<%@ variable name-given="url" %>
<%@ variable name-given="entry" %>
 
<c:set var="numHits" value="${result.numFound}" />

<div class="card ir-result-card w-100">
	<c:if test="${numHits >= 0}">	
		<c:set var="pageNavi">
			<%-- // 36.168 Treffer                   Erste Seite | 11-20 | 21-30 | 31-40 | 41-50 | Letzte Seite --%>
		  <nav class="float-right">
            <ul class="pagination ir-result-pagination">
			<c:if test="${result.numPages> 1}">
				<c:set var="page"><%= Math.round(Math.floor((double) result.getStart() / result.getRows()) + 1) %></c:set>
				<c:set var="start">0</c:set>
				<li class="page-item"><a class="page-link" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}"><fmt:message key="Webpage.Searchresult.firstPage" /></a></li>
			
				<c:if test="${page - 2 > 0}">
					<c:set var="start">${result.start - result.rows - result.rows}</c:set>
					<li class="page-item"><a class="page-link"  href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}">${start + 1}-${start + result.rows}</a></li>
				</c:if>
				<c:if test="${page - 1 > 0}">
					<c:set var="start">${result.start - result.rows}</c:set>
					<li class="page-item"><a class="page-link"  href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}">${start + 1}-${start + result.rows}</a></li>
				</c:if>

				<c:set var="start">${result.start}</c:set>
				<li class="page-item"><a  class="page-link active" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}">${start + 1}-<%=Math.min(Integer.parseInt(jspContext.getAttribute("start").toString()) + result.getRows(), result.getNumFound())%></a></li>

				<c:if test="${page + 1 <= result.numPages}">
					<c:set var="start">${result.start + result.rows}</c:set>
					<li class="page-item"><a  class="page-link" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}">${start + 1}-<%=Math.min(Integer.parseInt(jspContext.getAttribute("start").toString()) + result.getRows(), result.getNumFound())%></a></li>
				</c:if>
				<c:if test="${page + 2 <= result.numPages}">
					<c:set var="start">${result.start + result.rows + result.rows}</c:set>
					<li class="page-item"><a  class="page-link" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}">${start + 1}-<%=Math.min(Integer.parseInt(jspContext.getAttribute("start").toString()) + result.getRows(), result.getNumFound())%></a></li>
				</c:if>
			
				<c:set var="start"><%= Math.round((result.getNumPages() - 1) * result.getRows()) %></c:set>
				<li class="page-item"><a class="page-link" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}"><fmt:message key="Webpage.Searchresult.lastPage" /></a></li>
		  </c:if>
		  </ul>
        </nav>
        <div class="ir-result-pagination">
		<c:if test="${fn:length(result.backURL) >0}">
			<a class="btn btn-primary page-item mr-3"
			   href="${result.backURL}" ><fmt:message key="Webpage.searchresults.back" /></a>
		</c:if>
		<span class="ir-result-pagination-numfound">${result.numFound} <fmt:message key="Webpage.Searchresult.numHits" /></span>
		</div>
	</c:set>
  
	<div class="card-header bg-light w-100">
		<c:out value="${pageNavi}" escapeXml="false"/>
	</div>
	<c:if test="${numHits eq 0}">
		<div class="panel-body">
			<fmt:message key="Webpage.Searchresult.empty"/>
		</div>
	</c:if>
	
	<c:if test="${numHits > 0}">	
		<ul class="list-group">
			<c:forEach var="entry" items="${result.entries}">
				<c:set var="mcrid" value="${entry.mcrid}" />
				<c:set var="entry" value="${entry}" />
				<c:set var="url"   value="${pageContext.request.contextPath}/resolve/id/${entry.mcrid}?_search=${result.id}&_hit=${entry.pos}" /> 
				
                <div class="card-body">
					<div class="container">
						<jsp:doBody />
					</div>
				</div>				 
			</c:forEach>
   		</ul>

		<div class="card-footer bg-light w-100">
			<c:out value="${pageNavi}" escapeXml="false"/>
		</div>			
	</c:if>
</c:if>			
</div>