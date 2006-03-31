<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr" %>
<fmt:setLocale value="${requestScope.lang}" />
<fmt:setBundle basename='messages'/>
<mcr:getWorkflowEngineVariable pid="${requestScope.pid}" var="authorID" workflowVar="authorID" /> 
<mcr:getWorkflowEngineVariable pid="${requestScope.pid}" var="urn" workflowVar="reservatedURN" /> 
<mcr:receiveMcrObjAsJdom varDom="authorobject" mcrid="${authorID}" />

<div class="headline">
   <fmt:message key="Nav.Workflow.xmetadiss.begin" />
</div>

<table cellspacing="3" cellpadding="3" >
<c:if test="${!empty(authorobject)}">
 <x:forEach select="$authorobject/mycoreobject" >
   <tr valign="top">
      <td class="metaname"><fmt:message key="SWF.Dissertation.Author" /> </td>
      <td class="metavalue">  
         <x:out select="./metadata/names/name/fullname" escapeXml="false" />
      </td>
   </tr>  
   <tr valign="top" >
      <td class="metaname"><fmt:message key="SWF.Dissertation.Author.ID" /> TODO LINK AUF WORKFLOW DER AUTOREN �NDERT</td>
      <td class="metavalue">  
         <x:out select="./@ID" />
      </td>
   </tr>  
   <tr valign="top">
        <td class="metaname"><fmt:message key="SWF.Dissertation.URN" /> </td>
        <td class="metavalue">            
         <x:set var="mcrid" select="string(./@ID)" />
         <b><c:out value="${urn}" /></b>
         <br/>
         <br/>
         <i><fmt:message key="SWF.Dissertation.URN.Hinweis" /></i>
      </td>
   </tr>     
 </x:forEach>
</c:if>
   <tr>
      <td colspan="2">
         <img title="" alt="" src="images/greenArrow.gif">
         <a href="${WebApplicationBaseURL}nav?path=~xmetadiss"><fmt:message key="WorkflowEngine.forwardToWorkflow" /></a>
      </td>
   </tr> 
   <tr><td colspan="2">
     <hr/>
     <p><fmt:message key="Dissertation.Service.Hinweis1" /></p>
     <p>
        <mcr:getConfigProperty var="mail" prop="MCR.WorkflowEngine.contactemail.xmetadiss" defaultValue="mycore@mycore.de" />
        <a href="mailto:${mail}">${mail}</a>
     </p>
     </td>
    </tr>
</table>
