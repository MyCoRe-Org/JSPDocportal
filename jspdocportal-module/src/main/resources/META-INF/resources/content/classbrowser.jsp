<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Nav.ClassificationsSearch" />
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="2columns">
	<stripes:layout-component name="html_header">
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_classification-browser.css" />
	</stripes:layout-component>
    <stripes:layout-component name="contents">
    	<div class="ur-box ur-text">
			<h2><fmt:message key="Webpage.classbrowser.${actionBean.modus}.title" /></h2>
			<mcr:includeWebcontent id="classbrowser_${actionBean.modus}" file="classbrowser_${actionBean.modus}_intro.html" />

			<mcr:classificationBrowser modus="${actionBean.modus}"/>
			<div style="min-height:100px">&#160;</div>
		</div>
	</stripes:layout-component>
</stripes:layout-render>