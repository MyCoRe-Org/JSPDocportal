<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>

<%-- parameter: mcrid --%>

 <mcr:checkAccess var="modifyAllowed" permission="writedb" key="${param.mcrid}" />
 <c:if test="${modifyAllowed}">
 	<mcr:isObjectNotLocked var="bhasAccess" mcrObjectID="${param.mcrid}" />
 	<c:choose>
 		<c:when test="${bhasAccess}">
 			<!--  Editbutton -->
 			<a class="btn btn-primary btn-lg pull-right" style="padding:6px" 
				href="${WebApplicationBaseURL}startedit.action?mcrid=${mcrid}" title="<fmt:message key="WF.common.object.EditObject" />">
		   		<span class="glyphicon glyphicon-pencil"></span>
		   	</a> 
		</c:when>
		<c:otherwise>
			<button class="btn btn-default btn-lg pull-right" style="padding:6px" disabled="disabled" 
           			title="<fmt:message key="WF.common.object.EditObjectIsLocked" />">
		   			<span class="glyphicon glyphicon-ban-circle"></span>
           	</button>
		</c:otherwise>
	</c:choose>         
</c:if>   