<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>

<%-- parameter: mcrid --%>

 <mcr:hasAccess var="modifyAllowed" permission="writedb" mcrid="${param.mcrid}" />
 <c:if test="${modifyAllowed}">
 	<mcr:isLocked var="locked" mcrid="${param.mcrid}" />
 	<c:choose>
 		<c:when test="${not locked}">
 			<!--  Editbutton -->
 			<a class="btn btn-primary btn-lg pull-right" style="padding:6px" 
				href="${WebApplicationBaseURL}do/startedit?mcrid=${mcrid}" title="<fmt:message key="WF.common.object.EditObject" />">
		   		<i class="fas fa-pencil-alt"></i>
		   	</a> 
		</c:when>
		<c:otherwise>
			<button class="btn btn-default btn-lg pull-right" style="padding:6px" disabled="disabled" 
           			title="<fmt:message key="WF.common.object.EditObjectIsLocked" />">
		   			<i class="fa fa-ban"></i>
           	</button>
		</c:otherwise>
	</c:choose>         
</c:if>   