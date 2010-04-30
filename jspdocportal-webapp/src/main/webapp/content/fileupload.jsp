<%@ page import="org.mycore.common.MCRConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn" %>
<% 
    String jSessionID = MCRConfiguration.instance().getString("MCR.session.param", ";jsessionid=");
    String sessionID = jSessionID + session.getId();
%>

<div class="headline"><fmt:message key="Editor.FileuploadPageTitle" /></div>
Sie k�nnen einzelne Dateien bis 50 MB Gr��e direkt mittels des folgenden Formulares hochladen.
<c:url var="url" value="${applicationScope.WebApplicationBaseURL}editor/workflow/editor-author-addfile.xml">
	<c:param name="XSL.UploadID" value="${param['XSL.UploadID']}" />
	<c:param name="XSL.editor.source.new" value="${param['XSL.editor.source.new']}" />
	<c:param name="XSL.editor.cancel.url" value="${param['XSL.editor.cancel.url']}" />
	<c:param name="mcrid" value="${param.mcrid}" />
	<c:param name="remcrid" value="${param.remcrid}" />
	<c:param name="type" value="${param.type}" />
	<c:param name="step" value="${param.step}" />	
	<c:param name="HttpSessionID" value="<%= sessionID %>" />	
	<c:param name="JSessionID" value="<%= sessionID %>" />			
</c:url>
<c:import url="${url}" />