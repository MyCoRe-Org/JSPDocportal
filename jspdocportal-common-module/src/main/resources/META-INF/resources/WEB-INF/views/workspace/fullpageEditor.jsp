<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="org.mycore.frontend.servlets.MCRServlet"%>
<%@ page import="org.mycore.common.MCRSessionMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>

<fmt:message var="pageTitle" key="WF.Headline" /> 

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
  <mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/ckeditor.js" />
  <mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/adapters/jquery.js" />
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div class="container">
      <div class="row">
        <div class="col">
		  <mcr:includeXEditor editorPath="${it.editorPath}" cancelURL="${it.cancelURL}" sourceURI="${it.sourceURI}" />
        </div>
      </div>
  </div>
  <%@ include file="../fragments/footer.jspf" %>
</body>
</html>

