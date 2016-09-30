<%@tag import="org.mycore.frontend.MCRFrontendUtil"%>
<%@tag import="org.mycore.frontend.jsp.search.MCRSearchResultDataBean"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="recordIdentifier" required="true" type="java.lang.String" %>

<%--make variables available in body: 
<%@ variable name-given="mcrid" %>
<%@ variable name-given="url" %>
<%@ variable name-given="entry" %>
 --%>
<% 
	MCRSearchResultDataBean result = new MCRSearchResultDataBean();
	result.setQuery("ir.host.recordIdentifier:"+recordIdentifier);
	result.setSort("ir.sortstring asc");
	StringBuffer sb = new StringBuffer(MCRFrontendUtil.getBaseURL());
	sb.append("resolve/recordIdentifier/"+recordIdentifier.replace("/", "%252F")+"?");
	if(request.getParameter("_search")!=null){sb.append("&_search="+request.getParameter("_search"));}
	if(request.getParameter("_hit")!=null){sb.append("&_hit="+request.getParameter("_hit"));}
	result.setBackURL(sb.toString());
	result.doSearch();
	MCRSearchResultDataBean.addSearchresultToSession(request, result);

 	jspContext.setAttribute("result", result);
%>
<c:set var="numHits" value="${result.numFound}" />

<div class="panel panel-default ir-searchresult-panel">
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
				<li class="list-group-item">
					<div class="ir-resultentry-panel">
						<c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
						<c:choose>
							<c:when test="${(doctype eq 'disshab') or (doctype eq 'thesis')}">
								<search:result-entry-disshab entry="${entry}" url="${url}" />
							</c:when>
							<c:when test="${(doctype eq 'document') or (doctype eq 'bundle')}">
								<search:result-entry-document entry="${entry}" url="${url}" />
							</c:when>
							<c:otherwise>
								<search:result-entry entry="${entry}" url="${url}" />
							</c:otherwise>
						</c:choose>
						<div style="clear:both"></div>
					</div>
				</li>				 
			</c:forEach>
   		</ul>
	</c:if>
</div>