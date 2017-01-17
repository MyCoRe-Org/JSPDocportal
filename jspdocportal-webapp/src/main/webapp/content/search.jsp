<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>

<mcr:session var="sessionID" method="get" type="ID" />

<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />

<div class="headline"><fmt:message key="Nav.Search" /></div>

<p><fmt:message key="Webpage.intro.search.Possibilities" /> </p>
<%--<c:url var="url" value="${WebApplicationBaseURL}editor/searchmasks/SearchMask_AllMetadataFields.xml">
	    <c:param name="XSL.editor.source.new" value="true" />
	    <c:param name="XSL.editor.cancel.url" value="${WebApplicationBaseURL}" />
	    <c:param name="lang" value="${requestScope.lang}" />
	    <c:param name="MCRSessionID" value="${sessionID}"/>
	  </c:url>
	<c:import url="${url}" /> --%>
	<mcr:includeEditor editorPath="editor/searchmasks/SearchMask_AllMetadataFields.xml"/>
<p><c:import url="/content/node.jsp" /></p>
<mcr:includeWebContent file="search_introtext.html"/>