<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<%@ page import = "org.mycore.common.config.MCRConfiguration2" %>
<% 
    pageContext.setAttribute("navSide", MCRConfiguration2.getString("MCR.JSPDocportal.Navigation.Side").orElse("left"));
%>

<fmt:message var="pageTitle" key="Nav.ClassificationsSearch" />
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="2columns">
	<stripes:layout-component name="html_head">
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_classification-browser.css" />
	</stripes:layout-component>
    <stripes:layout-component name="main_part">
	<div class="row">
       <c:if test="${pageScope.navSide == 'left'}">
		     <div id="classbrowser_nav" class="col-3">
			    <mcr:outputNavigation mode="side" id="search" expanded="true" />
		    </div>
        </c:if>
		<div id="classbrowser_content" class="col">
    		<div>
				<mcr:includeWebcontent id="classbrowser_${actionBean.modus}" file="classbrowser/${actionBean.modus}_intro.html" />
			</div>
			<mcr:classificationBrowser modus="${actionBean.modus}"/>
			<div style="min-height:100px">&#160;</div>
		</div>
		<c:if test="${pageScope.navSide == 'right'}">
            <div id="search_nav" class="col-3">
                <mcr:outputNavigation mode="side" id="search" expanded="true" />
            </div>
        </c:if>
	</div>
	</stripes:layout-component>
</stripes:layout-render>