<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="result" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean" %>

<%@ variable name-given="mcrid" %>
<%@ variable name-given="url" %>
<%@ variable name-given="entry" %>
 
<c:set var="numHits" value="${result.numFound}" />

<div class="card ir-result-card w-100">
	<c:if test="${numHits >= 0}">	
		<c:set var="pageNavi">
			<%-- // 36.168 Treffer                   Erste Seite | 11-20 | 21-30 | 31-40 | 41-50 | Letzte Seite --%>
		  <nav class="float-end">
            <ul class="pagination ir-result-pagination mb-0">
			<c:if test="${result.numPages> 1}">
				<c:set var="page"><%= Math.round(Math.floor((double) result.getStart() / result.getRows()) + 1) %></c:set>
				<c:set var="start">0</c:set>
				<li class="page-item"><a class="page-link h-100" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}"><fmt:message key="Webpage.Searchresult.firstPage" /></a></li>
			
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
				<li class="page-item"><a class="page-link h-100" href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_start=${start}"><fmt:message key="Webpage.Searchresult.lastPage" /></a></li>
		  </c:if>
		  </ul>
        </nav>
        <div class="ir-result-pagination">
          <c:if test="${(numHits gt 0) and (not empty result.csvDownloadFields)}">
            <c:set var="sort_param"><%= ((org.apache.solr.common.util.NamedList<Object>)result.getSolrQueryResponse().getHeader().get("params")).get("sort") %></c:set>
            <c:url var="csv_url" value="${WebApplicationBaseURL}api/v1/search">
              <c:param name="q"><%= ((org.apache.solr.common.util.NamedList<Object>)result.getSolrQueryResponse().getHeader().get("params")).get("q") %></c:param>
              <%
                //pageContext is not available in JSP tags -> use jspContext instead
                jspContext.setAttribute("filterQueries", ((org.apache.solr.common.util.NamedList<Object>)result.getSolrQueryResponse().getHeader().get("params")).get("fq"));
              %>
              <c:forEach var="fq" items="${pageScope.filterQueries}">
                <c:param name="fq">${fq}</c:param>
              </c:forEach>  
              <c:if test="${sort_param}">
                <c:param name="sort">${sort_param}</c:param>
              </c:if>
              <c:param name="rows">${result.csvDownloadRows}</c:param>
              <c:param name="wt">csv</c:param>
              <c:param name="fl">${result.csvDownloadFields}</c:param>
            </c:url>
            <c:set var="i18n_download"><fmt:message key="Webpage.Searchresult.download" /></c:set>
            <c:set var="i18n_download_csv_title"><fmt:message key="Webpage.Searchresult.csvDownload.title" /></c:set>
            
            <div id="ir-drownload-dropdown" class="dropdown float-start">
              <button class="btn btn-outline-primary page-item mr-3 dropdown-toggle" type="button" id="dropdownMenuDownload" 
                 title="${i18n_download}" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                 <i class="fas fa-download"></i>
               </button>
               <div class="dropdown-menu" aria-labelledby="dropdownMenuDownload">
                <a class="dropdown-item" rel="nofollow" href="${csv_url}" download="${result.csvDownloadFilename}" title="${i18n_download_csv_title}"><fmt:message key="Webpage.Searchresult.csvDownload" /></a>
              </div>
            </div>
          </c:if>

		<c:if test="${fn:length(result.backURL) >0}">
			<a class="btn btn-outline-primary page-item mr-3"
			   href="${result.backURL}" ><fmt:message key="Webpage.searchresults.back" /></a>
		</c:if>
		<span class="ir-result-pagination-numfound btn">${result.numFound} <fmt:message key="Webpage.Searchresult.numHits" /></span>
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
		<ul class="list-group list-group-flush">
			<c:forEach var="entry" items="${result.entries}">
				<c:set var="mcrid" value="${entry.mcrid}" />
				<c:set var="entry" value="${entry}" />
				<c:set var="url"   value="${pageContext.request.contextPath}/resolve/id/${entry.mcrid}?_search=${result.id}&_hit=${entry.pos}" /> 
                <li class="list-group-item">
					<jsp:doBody />
				</li>				 
			</c:forEach>
   		</ul>

		<div class="card-footer bg-light w-100">
			<c:out value="${pageNavi}" escapeXml="false"/>
		</div>			
	</c:if>
</c:if>			
</div>