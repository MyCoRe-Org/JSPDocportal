<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Nav.Sitemap" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="main_part">

		<h2><fmt:message key="Nav.Sitemap" /></h2>
		<table class="sitemap">
			<tr width="100%">
				<th class="sitemap"><fmt:message key="Nav.MainmenueLeft" /></th>
				<th class="sitemap"><fmt:message key="Nav.MenuAbove" /></th>
			</tr>	
			<tr valign="top">
				<td>
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='left']"/>
					<c:import url="/content/sitemap_items_rec.jsp" />
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='publish']"/>
					<c:import url="/content/sitemap_items_rec.jsp" />
				</td>
				<td>
					<x:set scope="session" var="recNavPath" select="$applicationScope:navDom//*[local-name()='navigation' and @id='top']"/>
					<c:import url="/content/sitemap_items_rec.jsp" />
				</td>
			</tr>
		</table>
		
	</stripes:layout-component>
</stripes:layout-render>    