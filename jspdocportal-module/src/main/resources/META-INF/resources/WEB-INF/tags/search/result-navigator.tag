<%@tag import="org.mycore.frontend.jsp.search.MCRSearchResultDataBean" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ attribute name="mcrid" required="true" type="java.lang.String" %>

<%
	String searchID = request.getParameter("_search");
	if(searchID!=null){
		MCRSearchResultDataBean result = MCRSearchResultDataBean.retrieveSearchresultFromSession(request, searchID);
		jspContext.setAttribute("result", result);
		if(result!=null){
			int pos = result.findEntryPosition(mcrid); 
			if(pos >= 0){
				result.setCurrent(result.getStart()+pos);
			}
		}
	}
%>

<c:if test="${not empty result}">
	<!-- Searchresult PageNavigation -->
	<div id="searchdetail-navigation" class="navbar navbar-default">
		<c:set var="numHits" value="${result.numFound}" />
		<div style="padding:6px; text-align:center;">
			<fmt:message key="Webpage.Searchresult.hitXofY">
				<fmt:param>${result.current + 1}</fmt:param>
				<fmt:param>${numHits}</fmt:param>	
			</fmt:message>
		</div>
		<div style="padding:6px;">
			<c:set var="backURL" value="${pageContext.request.contextPath}/${result.action}?_search=${result.id}" />
			<c:if test="${fn:contains(result.backURL, 'indexbrowser')}">
				<c:set var="backURL" value="${result.backURL}" />	
			</c:if>

			<div class="btn-group" style="width:101%;">
			
				<a style="font-size:1.5em;width:33.333%;" class="btn btn-default btn-xs ${result.current == 0 ? 'disabled invisible' :''}" 
			   	   href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_hit=${result.current-1}"
			       title="<fmt:message key="Webpage.Searchresult.prevPage.hint" />"><span class="glyphicon glyphicon-chevron-left"></span></a>
		
			    <a style="font-size:1.5em;width:33.333%;" class="btn btn-default btn-xs" 
			       href="${backURL}"
			       title="<fmt:message key="Webpage.Searchresult.back.hint" />"><span class="glyphicon glyphicon-chevron-up"></span></a>
			
				<a style="font-size:1.5em;width:33.333%;" class="btn btn-default btn-xs ${result.current == numHits - 1 ? 'disabled invisible' : ''}" 
				   href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_hit=${result.current+1}"
				   title="<fmt:message key="Webpage.Searchresult.nextPage.hint" />"><span class="glyphicon glyphicon-chevron-right"></span></a>
			</div>
		</div>	
	</div>
</c:if>
