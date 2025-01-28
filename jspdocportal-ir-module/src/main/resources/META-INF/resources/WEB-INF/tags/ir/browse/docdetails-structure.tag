<%@tag import="java.nio.charset.StandardCharsets"%>
<%@tag import="java.net.URLEncoder"%>
<%@tag import="org.apache.commons.lang3.StringUtils"%>
<%@tag import="org.mycore.frontend.MCRFrontendUtil"%>
<%@tag import="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<%@ taglib prefix="browse" tagdir="/WEB-INF/tags/ir/browse"%>

<%@ attribute name="hostRecordIdentifier" required="true" type="java.lang.String" %>
<%@ attribute name="hostMcrID" required="true" type="java.lang.String" %>
<%@ attribute name="hostDoctype" required="true" type="java.lang.String" %>
<%@ attribute name="hostZDBID" required="false" type="java.lang.String" %>

<%--make variables available in body: 
<%@ variable name-given="mcrid" %>
<%@ variable name-given="url" %>
<%@ variable name-given="entry" %>
 --%>
<% 
	MCRSearchResultDataBean result = new MCRSearchResultDataBean();
    if(StringUtils.isEmpty(hostRecordIdentifier)){
        result.setQuery("ir.host.recordIdentifier:"+hostMcrID);
    }
    else {
        result.setQuery("ir.host.recordIdentifier:"+hostRecordIdentifier
            +" OR ir.host.recordIdentifier:"+hostMcrID.replaceFirst("/", "_")
            +" OR ir.host.recordIdentifier:"+hostMcrID.replaceFirst("_", "/")
            +" OR ir.host.recordIdentifier:"+hostMcrID);
    }
	String sortOrder = hostMcrID.contains("_document_") ? "desc" : "asc";
	result.setSort("ir.sortstring " + sortOrder);
    result.setRows(999);
	StringBuffer sb = new StringBuffer(MCRFrontendUtil.getBaseURL());
    sb.append("resolve/id/" + hostMcrID + "?");
	if(request.getParameter("_search")!=null){sb.append("&_search="+URLEncoder.encode(request.getParameter("_search"), StandardCharsets.UTF_8));}
	if(request.getParameter("_hit")!=null){sb.append("&_hit="+ URLEncoder.encode(request.getParameter("_hit"), StandardCharsets.UTF_8));}
	result.setBackURL(sb.toString());
	result.doSearch();
	MCRSearchResultDataBean.addSearchresultToSession(request, result);

 	jspContext.setAttribute("result", result);
%>
<c:set var="numHits" value="${result.numFound}" />

<div class="panel panel-default ir-searchresult-panel">
  <c:if test="${numHits > 0}">
    <ul class="list-group ir-structure-has-children">
      <c:forEach var="entry" items="${result.entries}">
        <c:set var="mcrid" value="${entry.mcrid}" />
        <c:set var="entry" value="${entry}" />
        <c:set var="url"   value="${pageContext.request.contextPath}/resolve/id/${entry.mcrid}?_search=${result.id}&_hit=${entry.pos}" />
        <li class="list-group-item">
          <div class="ir-result-card">
            <browse:result-entry entry="${entry}" url="${url}" mode="${hostDoctype}" />
            <div style="clear:both"></div>
		  </div>
		</li>				 
	  </c:forEach>
           
      <c:if test="${(hostDoctype eq 'histbest.print.journal') and not(empty(hostZDBID))}">
        <li class="list-group-item">
          <div class="ir-result-card">
            <fmt:message var="outZDB" key="OMD.ir.docdetails.structure.zdb_hint">
              <fmt:param value="${hostZDBID}" />
            </fmt:message>
            <c:out value="${outZDB}" escapeXml="false" />
          </div>
        </li>
      </c:if>        
    </ul>
  </c:if>
</div>