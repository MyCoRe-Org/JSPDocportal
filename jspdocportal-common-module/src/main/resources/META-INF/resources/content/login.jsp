<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.login.ChangeUserID" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}" layout="2columns">
  <stripes:layout-component name="main_part">
    <div class="container">
    <!-- available user status:  
	 	  actionBean.loginStatus = { user.login, user.invalid_password, user.welcome, user.disabled, user.unknown, user.unkwnown_error
    -->
	<div class="row">
      <div class="col">
        <h2><fmt:message key="Webpage.login.ChangeUserID" /></h2>
        <p><fmt:message key="Webpage.login.info" /></p>
      </div>
    </div> 
       
	<stripes:form class="form-horizontal"
 				 beanclass="org.mycore.frontend.jsp.stripes.actions.MCRLoginAction"
				 id="loginForm" enctype="multipart/form-data" acceptcharset="UTF-8">
				
      <div class="stripesinfo">
        <stripes:errors />
		<stripes:messages />
      </div>
    
      <div class="row">
        <div class="col">
          <c:if test="${actionBean.loginOK}">
		    <p><fmt:message key="Webpage.login.YouAreLoggedInAs" />:&#160;	<strong><c:out value="${actionBean.userID}"></c:out></strong></p>
		  </c:if>
		  <c:if test="${not empty actionBean.loginStatus}">
		    <div class="alert alert-secondary" role="alert"><fmt:message key="Webpage.login.status.${actionBean.loginStatus}" >
			  <fmt:param value="${actionBean.userName}" /></fmt:message></div>
		  </c:if>
        </div>
      </div>
      <div class="row">
        <div class="col offset-sm-3 col-sm-6 form-horizontal">
          <div class="row">  
		    <label for="inputUserID" class="col-sm-4 control-label"><fmt:message key="Webpage.login.UserLogin" />:</label>
		    <div class="col-sm-8">
			  <input type="text" id="inputUserID" name="userID" placeholder="User ID"  class="form-control" />
		    </div>
		  </div>
		  <div class="row mt-3">
		    <label for="inputPassword" class="col-sm-4 control-label"><fmt:message key="Webpage.login.Password" />:</label>
		    <div class="col-sm-8">
			  <input type="password" id="inputPassword" name="password" placeholder="Passwort" class="form-control" />
		    </div>
	      </div>

          <div class="row mt-3">
		    <div class="offset-sm-4 col-sm-4 text-center">
  			  <input name="doLogin" class="btn btn-primary" value="<fmt:message key="Webpage.login.Login" />" type="submit" /> 
		    </div>
		    <c:if test="${actionBean.loginOK}">
			  <div class="col-sm-4 text-center">
			     <input name="doLogout" class="btn btn-secondary" value="<fmt:message key="Webpage.login.Logout" />" type="submit" /> 
			  </div>
		    </c:if>
		  </div>
	    </div>
      </div>
	  
      <c:if test="${not empty actionBean.nextSteps}">
        <div class="row">
          <div class="offset-sm-2 col-sm-8">
		    <div class="card border border-primary mt-5">
              <div class="card-header bg-light"><strong><fmt:message key="Webpage.login.your_options" /></strong></div>
  			  <div class="card-body">
  			    <ul>
    			  <c:forEach var="nextStep" items="${actionBean.nextSteps}">
    			    <c:set var="href"><c:out escapeXml="true" value="${nextStep.url}"/></c:set>
				    <li><a href="${href}">${nextStep.label}</a></li>
			      </c:forEach>
			    </ul>
			  </div>
		    </div>
          </div>
        </div>
      </c:if>
    </stripes:form>
	</div>
    <div style="height: 75px;">&nbsp;</div>
  </stripes:layout-component>
</stripes:layout-render>
