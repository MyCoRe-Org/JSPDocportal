<%@tag import="org.activiti.engine.impl.cmd.GetHistoricIdentityLinksForTaskCmd"%>
<%@tag import="org.mycore.frontend.jsp.search.MCRSearchResultDataBean" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ attribute name="mcrid" required="true" type="java.lang.String" %>
<%@ attribute name="mode" required="false" type="java.lang.String" %>

<%
	String searchID = request.getParameter("_search");
	if(searchID!=null){
		MCRSearchResultDataBean result = MCRSearchResultDataBean.retrieveSearchresultFromSession(request, searchID);
		jspContext.setAttribute("result", result);
		if(result!=null){
		    String hitID = request.getParameter("_hit");
		    if(hitID!=null){
		        int hit=0;
		        try{
		            hit = Integer.parseInt(hitID);
		            result.getHit(hit); //reset hit
		        }
		        catch(Exception e){
		            //do nothing - keep what you have
		        }
		    }
		}
	}
%>

<c:if test="${not empty result}">
	<!-- Searchresult PageNavigation -->
	<c:set var="backURL" value="${pageContext.request.contextPath}/${result.action}?_search=${result.id}" />
	<c:if test="${fn:contains(result.backURL, 'indexbrowser') or empty result.action}">
		<c:set var="backURL" value="${result.backURL}" />	
	</c:if>
	
	<div id="searchdetail-navigation" class="navbar navbar-default">
		<c:set var="numHits" value="${result.numFound}" />
		<c:choose>
			<c:when test="${mode eq 'one_line'}">
		
		<div class="pull-left">
				    <a style="font-size:1.25em;margin:6px;" class="btn btn-default btn-xs" 
			       href="${backURL}"
			       title="<fmt:message key="Webpage.Searchresult.back.hint" />"><span class="glyphicon glyphicon-chevron-up"></span></a>
		</div>
		<div class="pull-right">
			<c:set var="backURL" value="${pageContext.request.contextPath}/${result.action}?_search=${result.id}" />
			<c:if test="${fn:contains(result.backURL, 'indexbrowser')}">
				<c:set var="backURL" value="${result.backURL}" />	
			</c:if>

			<div class="btn-group" style="width:101%;margin:6px">
			
				<a style="font-size:1.25em" class="btn btn-default btn-xs ${result.current == 0 ? 'disabled invisible' :''}" 
			   	   href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_hit=${result.current-1}"
			       title="<fmt:message key="Webpage.Searchresult.prevPage.hint" />"><span class="glyphicon glyphicon-chevron-left"></span></a>
		
			
				<a style="font-size:1.25em" class="btn btn-default btn-xs ${result.current == numHits - 1 ? 'disabled invisible' : ''}" 
				   href="${pageContext.request.contextPath}/${result.action}?_search=${result.id}&amp;_hit=${result.current+1}"
				   title="<fmt:message key="Webpage.Searchresult.nextPage.hint" />"><span class="glyphicon glyphicon-chevron-right"></span></a>
			</div>
		</div>

		<div style="padding:0.66em 6px 6px 6px; text-align:center">
			<fmt:message key="Webpage.Searchresult.hitXofY">
				<fmt:param>${result.current + 1}</fmt:param>
				<fmt:param>${numHits}</fmt:param>	
			</fmt:message>
		</div>
			</c:when>
			<c:otherwise>
		<div style="padding:6px; text-align:center;">
			<fmt:message key="Webpage.Searchresult.hitXofY">
				<fmt:param>${result.current + 1}</fmt:param>
				<fmt:param>${numHits}</fmt:param>	
			</fmt:message>
		</div>
		<div style="padding:6px;">
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
		</c:otherwise>
		</c:choose>	
	</div>
</c:if>
