<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x"uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.title" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="main_part">
		<h2><fmt:message key="Nav.Admin.EditWebcontent" /></h2>
		<span class="subtitle"><fmt:message key="Webpage.admin.webcontent.Backup" /></span>
		<p><fmt:message key="Webpage.admin.webcontent.Description" /> </p>
		<p>
			<a href="<x:out select="concat($WebApplicationBaseURL,'zipwebcontent')" />"
							class="linkButton"><fmt:message key="OMD.zipgenerate" /></a>&#160;&#160;
		</p>
	</stripes:layout-component>
</stripes:layout-render>   

