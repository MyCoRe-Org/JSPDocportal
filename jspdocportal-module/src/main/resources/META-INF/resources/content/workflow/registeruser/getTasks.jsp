<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr" %>

<c:set  var="baseURL" value="${applicationScope.WebApplicationBaseURL}"/>
<c:set var="dom" value="${requestScope.task.variables}" />
<c:set var="userID">	<x:out select="$dom/variables/variable[@name = 'initiatorUserID']/@value" /></c:set>
<c:set var="name">	<x:out select="$dom/variables/variable[@name = 'initiatorName']/@value" /></c:set>


<!--  for all task  -->
     <tr>
       <td class="metaname"> 
         <b><fmt:message key="WF.common.Processnumber" /> ${requestScope.task.processID}</b>
         <br/>
         <b>Information:</b>
            <fmt:message key="WF.registeruser.title.${requestScope.task.taskName}" />
       </td>
      </tr>
 	 <tr valign="top">
		<td>
			<b>Benutzerkennzeichen: <c:out value="${userID}" /></b><br/>
			<c:out value="${name}" /> <br />
		</td>		
     </tr>   
<c:choose>
   <c:when test="${requestScope.task.taskName eq 'isUserAccountOK' || requestScope.task.taskName eq 'isEditedUserAccountOK' }">
      <tr> 
	   <td>       
        <table width="100%" >
	       	<c:import url="/content/workflow/registeruser/editorButtons.jsp" />	       
	       <tr><td>	       
	       <a href="${baseURL}nav?path=~workflow-registeruser&transition=go2canUserBeSubmitted&endTask=${requestScope.task.taskName}&processID=${requestScope.task.processID}">
	       	<fmt:message key="WF.registeruser.UserSubmitting"/></a>
	        &#160;
	        &#160;
	       <a href="${baseURL}nav?path=~workflow-registeruser&transition=go2canUserBeRejected&endTask=${requestScope.task.taskName}&processID=${requestScope.task.processID}">
	       	<fmt:message key="WF.registeruser.UserRejecting"/></a>
       	</td></tr>   
        </table>
	   </td>
	  </tr>	      
   </c:when>
   <c:when test="${requestScope.task.taskName eq 'taskAdminCheckRejectSuccessfull' }">
     <tr> 
	  <td>       
       <a href="${baseURL}nav?path=~workflow-registeruser&transition=go2sendRejectedMail&endTask=${requestScope.task.taskName}&processID=${requestScope.task.processID}">
         <fmt:message key="WF.registeruser.${requestScope.task.taskName}" />
	   </td>
	  </tr>	      
   </c:when>
   <c:when test="${requestScope.task.taskName eq 'taskSetUserDataComplete'}">
      <tr> 
	   <td>       
        <table width="100%" >
	     <tr><td>	       
	       <a href="${baseURL}nav?path=~workflow-registeruser&transition=go2canUserBeCommitted&endTask=${requestScope.task.taskName}&processID=${requestScope.task.processID}">
	         <fmt:message key="WF.registeruser.UserSubmitting" />
	       </a>  
	       </td><td>&#160;  </td><td>	       
	       <a href="${baseURL}nav?path=~workflow-registeruser&transition=go2canUserBeRejected&endTask=${requestScope.task.taskName}&processID=${requestScope.task.processID}">
	         <fmt:message key="WF.registeruser.UserRejecting" />
	       </a>  
       	</td></tr>    
        </table>
       	</td>
	  </tr>	      
   </c:when>
   <c:when test="${requestScope.task.taskName eq 'taskEnterMessageData'}">
      <tr> 
	   <td>       
	     <form action="${baseURL}setworkflowvariable" accept-charset="utf-8">
	   	     <input name="dispatcherForward" value="/nav?path=~workflow-registeruser" type="hidden" />
	         <input name="transition" value="" type="hidden" />
	         <input name="endTask" value="taskentermessagedata" type="hidden" />
	         <input name="processID" value="${requestScope.task.processID}" type="hidden" />
	         <input name="jbpmVariableNames" value="tmpTaskMessage" type="hidden" /> 
		     <textarea name="tmpTaskMessage" cols="50" rows="4">Ablehnungsgr�nde: </textarea>  
		     <br>&nbsp;<br>
	    	<input name=submit" type="submit" value="<fmt:message key="WF.common.SendTask" />"/>      
	      </form>
	   </td>
	  </tr>	      
   </c:when>
   <c:when test="${requestScope.task.taskName eq 'taskAdminCheckCommitmentNotSuccessfull' }">
      <tr> 
	   <td>       
	         <fmt:message key="WF.registeruser.taskAdminCheckCommitmentNotSuccessfull" />
	   </td>
	  </tr>	      
   
   </c:when>
   <c:otherwise>
      <tr>
       <td>
	    <p> what else? TASK = ${requestScope.task.taskName} </p>
	   </td>
	  </tr> 
   </c:otherwise>
</c:choose>
