<%@tag import="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean" pageEncoding="UTF-8"%>
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
	
	<div id="searchdetail-navigation" class="navbar navbar-default ir-nav-search ir-box ir-box-emph d-flex">
		<c:set var="numHits" value="${result.numFound}" />
		<c:choose>
		  <c:when test="${mode eq 'one_line'}">
            <div class="d-flex justify-content-start">
               <a style="font-size:1.25em;margin:5px;" class="btn btn-primary btn-sm" 
			      href="${backURL}"
			      title="<fmt:message key="Webpage.Searchresult.back.hint" />"><i class="fa fa-chevron-up"></i></a>
		    </div>
            <div style="padding:0.66em 5px 5px 5px;" class="d-flex justify-content-between">
              <fmt:message key="Webpage.Searchresult.hitXofY">
                <fmt:param>${result.current + 1}</fmt:param>
                <fmt:param>${numHits}</fmt:param> 
              </fmt:message>
            </div>
		    <div class="d-flex justify-content-end">
		     <div class="btn-group" style="width:101%; margin:5px">
			   <a style="font-size:1.25em" class="btn btn-primary btn-sm ${result.current == 0 ? 'disabled invisible' :''}" 
			      href="${pageContext.request.contextPath}/do/search?_search=${result.id}&amp;_hit=${result.current-1}"
			      title="<fmt:message key="Webpage.Searchresult.prevPage.hint" />"><i class="fa fa-chevron-left"></i></a>
			   <a style="font-size:1.25em;margin-left:5px" class="btn btn-primary btn-sm ${result.current == numHits - 1 ? 'disabled invisible' : ''}" 
				  href="${pageContext.request.contextPath}/do/search?_search=${result.id}&amp;_hit=${result.current+1}"
				  title="<fmt:message key="Webpage.Searchresult.nextPage.hint" />"><i class="fa fa-chevron-right"></i></a>
			 </div>
		    </div>
    	</c:when>
		<c:otherwise>
		  <div style="padding:5px; text-align:center;">
			<fmt:message key="Webpage.Searchresult.hitXofY">
				<fmt:param>${result.current + 1}</fmt:param>
				<fmt:param>${numHits}</fmt:param>	
			</fmt:message>
		  </div>
		  <div style="padding:5px;">
			<div class="btn-group" style="width:101%;">
				<a style="font-size:1.5em;width:33.333%;" class="btn btn-primary btn-sm ${result.current == 0 ? 'disabled invisible' :''}" 
			   	   href="${pageContext.request.contextPath}/do/search?_search=${result.id}&amp;_hit=${result.current-1}"
			       title="<fmt:message key="Webpage.Searchresult.prevPage.hint" />"><i class="fa fa-chevron-left"></i></a>
		
			    <a style="font-size:1.5em;width:33.333%;" class="btn btn-primary btn-sm" 
			       href="${backURL}"
			       title="<fmt:message key="Webpage.Searchresult.back.hint" />"><i class="fa fa-chevron-up"></i></a>
			
				<a style="font-size:1.5em;width:33.333%;" class="btn btn-primary btn-sm ${result.current == numHits - 1 ? 'disabled invisible' : ''}" 
				   href="${pageContext.request.contextPath}/do/search?_search=${result.id}&amp;_hit=${result.current+1}"
				   title="<fmt:message key="Webpage.Searchresult.nextPage.hint" />"><i class="fa fa-chevron-right"></i></a>
			</div>
		  </div>
		</c:otherwise>
	  </c:choose>	
	</div>
</c:if>
