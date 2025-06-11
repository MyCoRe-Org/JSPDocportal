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
  <mcr:webjarLocator htmlElement="script" project="tinymce" file="tinymce.min.js" />
  <script src="${applicationScope.WebApplicationBaseURL}modules/tinymce-i18n/langs7/de.js"></script>
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div id="content_area">
    <div class="container">
      <div class="row">
        <div class="col">
		  <mcr:includeXEditor editorPath="${it.editorPath}" cancelURL="${it.cancelURL}" sourceURI="${it.sourceURI}" />
        </div>
      </div>
    </div>
  </div>
  <%@ include file="../fragments/footer.jspf" %>
</body>
</html>

