<%@page import="org.mycore.frontend.servlets.MCRServlet"%>
<%@page import="org.mycore.common.MCRSessionMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ page pageEncoding="UTF-8" %>

<%--Parameter: objectType --%>

<fmt:message var="pageTitle" key="WF.${param.objectType}" /> 
<stripes:layout-render name="../../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}" layout="2columns">
<stripes:layout-component name="html_head">
	<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_workspace.css" />
</stripes:layout-component>
	<stripes:layout-component name="main_part">
		<c:set  var="baseURL" value="${applicationScope.WebApplicationBaseURL}"/>
			<stripes:form
				beanclass="org.mycore.frontend.jsp.stripes.actions.AdminWorkflowProcessesAction"
				id="workspaceForm" enctype="multipart/form-data" acceptcharset="UTF-8">
				<h2>Administration der WorkflowProzesse</h2>
				<div class="stripesinfo">
					<stripes:errors />
					<stripes:messages />
				</div>
				<stripes:hidden name="projectID" />
				<stripes:hidden name="objectType" />


				<%-- load first time from request parameter "returnPath --%>

				<div>
					ProjektID: ${actionBean.projectID} <br />
					ObjectType: ${actionBean.objectType}
				</div>	
				
				<h3>Gestartete Prozesse</h3>
				<c:forEach var="pi" items="${actionBean.runningProcesses}" >
					<div>
						ProcessInstance: <c:out value="${pi.processInstanceId}" />
						<c:if test="${not empty actionBean.objectType}">
							
						</c:if>
						<stripes:submit name="doDeleteProcess_${pi.processInstanceId}">Prozess beenden</stripes:submit>
					
					</div>
				</c:forEach>
				
				
			</stripes:form>
</stripes:layout-component>
</stripes:layout-render>
