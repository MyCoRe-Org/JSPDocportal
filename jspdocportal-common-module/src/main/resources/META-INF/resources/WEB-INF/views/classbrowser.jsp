<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>

<fmt:message var="pageTitle" key="Nav.ClassificationsSearch" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
      <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_classification-browser.css" />
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div class="container">
	<div class="row">
		<div class="col-3">
			<mcr:outputNavigation mode="side" id="search" expanded="true"></mcr:outputNavigation>
			
		</div>
		<div class="col">
    		<div>
				<mcr:includeWebcontent id="classbrowser_${it.modus}" file="classbrowser/${it.modus}_intro.html" />
			</div>
			<mcr:classificationBrowser modus="${it.modus}"/>
			<div style="min-height:100px">&#160;</div>
		</div>
	</div>
  </div>
  <%@ include file="fragments/footer.jspf" %>
</body>
</html>
