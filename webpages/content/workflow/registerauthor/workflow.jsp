<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr" %>
<%@ page pageEncoding="UTF-8" %>

<c:set  var="baseURL" value="${applicationScope.WebApplicationBaseURL}"/>

<fmt:setLocale value="${requestScope.lang}" />
<fmt:setBundle basename='messages' />
<!--  debug handling -->
<c:choose>
   <c:when test="${!empty(param.debug)}">
      <c:set var="debug" value="true" />
   </c:when>
   <c:otherwise>
      <c:set var="debug" value="false" />
   </c:otherwise>
</c:choose>

<!--  handle task ending parameters -->
<c:if test="${!empty(param.endTask)}">
    <c:set var="endTask" scope="request" value="${param.endTask}" />
    <c:set var="processID" scope="request" value="${param.processID}" />
    <c:set var="transition" scope="request" value="${param.transition}" />
    <c:import url="/content/workflow/registerauthor/endTasks.jsp" />
</c:if>

<!--  task management part -->

<mcr:getWorkflowTaskBeanList var="myTaskList" mode="activeTasks" workflowTypes="registerauthor" 
	varTotalSize="total1" />
<mcr:getWorkflowTaskBeanList var="myProcessList" mode="initiatedProcesses" workflowTypes="registerauthor"
    varTotalSize="total2" />

<c:choose>
   <c:when test="${empty(myTaskList)&& empty(myProcessList)}">
	  <div class="headline"><fmt:message key="Nav.Workflow.registerauthor.info" /></div>
      <img title="" alt="" src="images/greenArrow.gif">
      <a target="_self" href="${baseURL}nav?path=~registerauthor-begin"><fmt:message key="WorkflowEngine.StartRegisterAuthorWorkflow" /></a>
      <br/>&nbsp;<br>
      <img title="" alt="" src="images/greenArrow.gif">
      <fmt:message key="WorkflowEngine.EmptyWorkflow" />   
      <br>&nbsp;<br>
      <mcr:checkAccess permission="administrate-registerauthor" var="curUserIsAdminUser" />
      <c:if test="${!curUserIsAdminUser}">
	      <c:import url="/content/workflow/registerauthor/information.jsp" />
    	  <br/>&nbsp;<br>
   	  </c:if>
   </c:when>
   <c:otherwise>
        <div class="headline"><fmt:message key="Nav.Workflow.registerauthor" /></div>
      <img title="" alt="" src="images/greenArrow.gif">
      <a target="_self" href="${baseURL}nav?path=~registerauthor-begin"><fmt:message key="WorkflowEngine.StartRegisterAuthorWorkflow" /></a>
        <br/>&nbsp;<br>
        <br>&nbsp;<br>

        <div class="headline"><fmt:message key="WorkflowEngine.MyTasks" /></div>   
        
        <table>       
	        <c:forEach var="task" items="${myTaskList}">
	        <tr><td class="task">
			   <table width="100%">				        
		           <c:set var="task" scope="request" value="${task}" />
		           <c:import url="/content/workflow/${task.workflowProcessType}/getTasks.jsp" />
		       </table>
		    </td></tr>       
	        </c:forEach>
        <c:if test="${empty(myTaskList)}">
           <font color="#00ff00"><fmt:message key="WorkflowEngine.NoTasks" /></font>
        </c:if>
        </table>
        
        <br>&nbsp;<br>
        
        <div class="headline"><fmt:message key="WorkflowEngine.MyInititiatedProcesses.registerauthor" /></div>
        
        <table>
        <c:forEach var="task" items="${myProcessList}">
           <c:set var="task" scope="request" value="${task}" />
           <c:import url="/content/workflow/${task.workflowProcessType}/getTasks.jsp" />
        </c:forEach>
        <c:if test="${empty(myProcessList)}">
           <font color="#00ff00"><fmt:message key="WorkflowEngine.NoTasks" /></font>
        </c:if>
        </table>   
   </c:otherwise>
</c:choose>        