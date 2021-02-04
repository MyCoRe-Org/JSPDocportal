<%@page import="org.mycore.frontend.servlets.MCRServlet"%>
<%@page import="org.mycore.common.MCRSessionMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ page pageEncoding="UTF-8" %>

<%--Parameter: objectType --%>

<fmt:message var="pageTitle" key="WF.${param.objectType}" /> 
<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div class="container">
  <div class="row">
  <div class="col">
    <c:forEach var="m" items="${it.messages}">
        <div class="alert">
          <c:out value="${m}" />
        </div>
        </c:forEach>    

		<form action="${applicationScope.WebApplicationBaseURL}do/workspace/administration" accept-charset="UTF-8">
				<h2>Administration der WorkflowProzesse</h2>
				<input type="hidden" name="projectID" value="${it.projectID}"  />
				<input type="hidden" name="objectType" value="${it.objectType}" />


				<%-- load first time from request parameter "returnPath --%>

				<div>
					ProjektID: <c:out value="${it.projectID}" /> <br />
					ObjectType:<c:out value="${it.objectType}" />
				</div>	
				
				<h3>Gestartete Prozesse</h3>
				<c:forEach var="pi" items="${it.runningProcesses}" >
					<div>
						ProcessInstance: <c:out value="${pi.processInstanceId}" />
						<c:if test="${not empty it.objectType}">
							
						</c:if>
						<button class="btn btn-danger" name="doDeleteProcess_${pi.processInstanceId}">Prozess beenden</button>
					</div>
				</c:forEach>
			</form>
</div>
</div>
</div>
  <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>

