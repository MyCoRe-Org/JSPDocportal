<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>

<fmt:message var="pageTitle" key="Nav.Sitemap" /> 


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
		<h2><fmt:message key="Nav.Sitemap" /></h2>
		<table class="sitemap">
			<tr style="width:100%">
				<th class="sitemap"><fmt:message key="Nav.MainmenueLeft" /></th>
				<th class="sitemap"><fmt:message key="Nav.MenuAbove" /></th>
			</tr>	
			<tr valign="top">
				<td>
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='left']"/>
					<c:import url="sitemap_items_rec.jsp" />
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='publish']"/>
					<c:import url="sitemap_items_rec.jsp" />
				</td>
				<td>
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='top']"/>
					<c:import url="sitemap_items_rec.jsp" />
				</td>
			</tr>
		</table>
</div>
</div>
</div>
  <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>
