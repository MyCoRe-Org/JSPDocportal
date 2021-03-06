<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.admin.DocumentManagement" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="main_part">
		<h2><fmt:message key="Webpage.admin.DocumentManagement" /></h2>
 		<div class="textblock2">
        	<div>
        		<mcr:outputNavigation mode="toc" id="publish"/>
        	</div>
        	<div style="padding-bottom: 50px;">
            	<mcr:includeWebcontent id="dms" file="documentmanagement_introtext.html"/>
           	</div>
        </div>        
	 </stripes:layout-component>
</stripes:layout-render>
