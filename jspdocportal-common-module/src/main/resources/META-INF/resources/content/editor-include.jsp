<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="WF.editor.ValidatorError.Headline" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "Editor" layout="1column">
	<stripes:layout-component name="main_part">
<c:choose>
   <c:when test="${not empty param.processid}">
      <c:set  var="processid" value="${param.processid}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="processid" value="${requestScope.processid}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.mcrid}">
      <c:set  var="mcrid" value="${param.mcrid}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="mcrid" value="${requestScope.mcrid}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.isNewEditorSource}">
      <c:set  var="isNewEditorSource" value="${param.isNewEditorSource}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="isNewEditorSource" value="${requestScope.isNewEditorSource}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.type}">
      <c:set  var="type" value="${param.type}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="type" value="${requestScope.type}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.workflowType}">
      <c:set  var="workflowType" value="${param.workflowType}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="workflowType" value="${requestScope.workflowType}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.publicationType}">
      <c:set  var="publicationType" value="${param.publicationType}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="publicationType" value="${requestScope.publicationType}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.step}">
      <c:set  var="step" value="${param.step}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="step" value="${requestScope.step}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.nextPath}">
      <c:set  var="nextPath" value="${param.nextPath}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="nextPath" value="${requestScope.nextPath}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.uploadID}">
      <c:set  var="uploadID" value="${param.uploadID}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="uploadID" value="${requestScope.uploadID}"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.mcrid2}">
      <c:set  var="mcrid2" value="${param.mcrid2}"/>
   </c:when>
   <c:otherwise>
      <c:set  var="mcrid2" value="${requestScope.mcrid2}"/>
   </c:otherwise>
</c:choose>

<c:set  var="editorSessionID" value="${param['XSL.editor.session.id']}"/>
<c:if test="${empty editorSessionID}">
	<c:set  var="editorSessionID" value="${param.XSL.editor.session.id}"/>
</c:if>

<c:choose>
   <c:when test="${not empty param.target}">
      <c:set  var="target" value="${param.target}"/>
   </c:when>
   <c:when test="${not empty requestScope.target}">
      <c:set  var="target" value="${requestScope.target}"/>
   </c:when>   
   <c:otherwise>
      <c:set  var="target" value="MCRCheckMetadataServlet"/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.editorPath}">
      <c:set  var="editorPath" value="${param.editorPath}"/>
   </c:when>
   <c:when test="${not empty requestScope.editorPath}">
      <c:set  var="editorPath" value="${requestScope.editorPath}"/>
   </c:when>   
   <c:otherwise>
      <c:set  var="editorPath" value=""/>
   </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${not empty param.editorSource}">
      <c:set  var="editorSource" value="${param.editorSource}"/>
   </c:when>
   <c:when test="${not empty requestScope.editorSource}">
      <c:set  var="editorSource" value="${requestScope.editorSource}"/>
   </c:when>   
   <c:otherwise>
      <c:set  var="editorSource" value=""/>
   </c:otherwise>
</c:choose>


<hr/>

<mcr:hasAccess var="modifyAllowed" permission="writedb" mcrid="${mcrid}" />
<%-- TODO in subselect übergeben von mcrid--%>
<c:set var="modifyAllowed" value="true" />
<c:choose>
    <c:when test="${modifyAllowed}">
        <mcr:includeEditorInWorkflow 
          editorSessionID="${editorSessionID}"  
          isNewEditorSource="${isNewEditorSource}" 
          mcrid2="${mcrid2}"  
          uploadID="${uploadID}"    
          mcrid="${mcrid}" 
          type="${type}" 
          processid="${processid}" 
          workflowType="${workflowType}"
          publicationType="${publicationType}" 
          step="${step}" 
          target="${target}" 
          nextPath="${nextPath}" 
          editorPath="${editorPath}" 
          editorSource="${editorSource}"/>        
    </c:when>
    <c:otherwise>
        <span class="error"><fmt:message key="WF.common.PrivilegesError" /></span>
    </c:otherwise>
</c:choose>

       
<hr/>	
</stripes:layout-component>
</stripes:layout-render>