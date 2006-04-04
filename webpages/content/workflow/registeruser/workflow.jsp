<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr"%>
<c:set var="baseURL" value="${applicationScope.WebApplicationBaseURL}" />
<fmt:setLocale value="${requestScope.lang}" />
<fmt:setBundle basename='messages' />
<c:choose>
	<c:when test="${!empty(param.debug)}">
		<c:set var="debug" value="true" />
	</c:when>
	<c:otherwise>
		<c:set var="debug" value="false" />
	</c:otherwise>
</c:choose>

<mcr:checkAccess var="adminuser" permission="administrate-user" />

<c:choose>
	<c:when test="${adminuser}">
		<div class="headline"><fmt:message key="Nav.WorkflowRegisteruser" /></div>
		<br>&nbsp;<br>
		<div class="headline"><fmt:message key="WorkflowEngine.MyTasks" /></div>
		<mcr:getWorkflowTaskBeanList var="myTaskList" mode="activeTasks" workflowTypes="registeruser" varTotalSize="total1" />
		<table >
			<c:forEach var="task" items="${myTaskList}">
				<c:set var="task" scope="request" value="${task}" />
				<c:import url="/content/workflow/${task.workflowProcessType}/getTasks.jsp" />
			</c:forEach>
		</table>
	</c:when>
	<c:otherwise>
		<fmt:message key="Admin.PrivilegesError" />
	</c:otherwise>
</c:choose>
