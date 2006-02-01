<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr" %>
<%@ page import="org.mycore.frontend.servlets.MCRServlet" %>

<fmt:setLocale value="${requestScope.lang}" />
<fmt:setBundle basename='messages' />


<table width="100%">
	<tr>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>
			<div class="headline"><fmt:message key="Admin.Text1" /></div>
			<h3>zur Zeit bietet es folgende Funktionen:</h3>
		
			<ul>
				<li><a href="${applicationScope.WebApplicationBaseURL}admin?path=rules">Regeleditor</a> zur Erstellung der Zugriffsregeln</li>
				<li><a href="${applicationScope.WebApplicationBaseURL}admin?path=access">Regelzuweisung</a>, um Daten-Objekten Regeln zuweisen zu k�nnen</li>
				<li><a href="${applicationScope.WebApplicationBaseURL}admin?path=usergroup">Benutzergruppenverwaltung</a> zur Erstellung der Benutzergruppen</li>
				<li><a href="${applicationScope.WebApplicationBaseURL}admin?path=user">Benutzerverwaltung</a> zur Verwaltung Nutzer</li>
				<li><a href="${applicationScope.WebApplicationBaseURL}admin?path=user">Neue Nutzerregistration</a> zur Erstanmeldung von Nutzern</li>
			</ul>
			
		</td>
	</tr>
</table>
