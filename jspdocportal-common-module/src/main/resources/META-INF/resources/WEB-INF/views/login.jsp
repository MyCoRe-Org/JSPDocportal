<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>

<fmt:message var="pageTitle" key="Webpage.login.ChangeUserID" />


<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div id="content_area">
  <div class="container">
      <!-- available user status:  
	 	  it.loginStatus = { user.login, user.invalid_password, user.welcome, user.disabled, user.unknown, user.unkwnown_error }
    -->
	<div class="row">
      <div class="col">
        <h2><fmt:message key="Webpage.login.ChangeUserID" /></h2>
        <div class="alert"><fmt:message key="Webpage.login.info" /></div>
      </div>
    </div> 
       
	<form method="post" class="form-horizontal" action="${WebApplicationBaseURL}do/login"
 	      id="loginForm" accept-charset="UTF-8">

      <div class="row">
        <div class="col">
          <c:if test="${it.loginOK}">
		    <div class="alert"><fmt:message key="Webpage.login.YouAreLoggedInAs" />:&#160;	<strong><c:out value="${it.userID}"></c:out></strong></div>
		  </c:if>
		  <c:if test="${not empty it.loginStatus}">
		    <div class="alert alert-secondary" role="alert"><fmt:message key="Webpage.login.status.${it.loginStatus}" >
			  <fmt:param value="${it.userName}" /></fmt:message></div>
		  </c:if>
        </div>
      </div>
      <div class="row">
        <div class="col offset-sm-3 col-sm-6 form-horizontal">
          <div class="row">  
		    <label for="inputUserID" class="col-sm-4 form-label"><fmt:message key="Webpage.login.UserLogin" />:</label>
		    <div class="col-sm-8">
              <fmt:message var="userPlaceholder" key="Webpage.login.placeholder.UserLogin" />
			  <input type="text" id="inputUserID" name="userID" placeholder="${userPlaceholder}"  class="form-control" />
		    </div>
		  </div>
		  <div class="row mt-3">
		    <label for="inputPassword" class="col-sm-4 form-label"><fmt:message key="Webpage.login.Password" />:</label>
		    <div class="col-sm-8">
              <fmt:message var="passwordPlaceholder" key="Webpage.login.placeholder.Password" />
			  <input type="password" id="inputPassword" name="password" placeholder="${passwordPlaceholder}" class="form-control" />
		    </div>
	      </div>

          <div class="row mt-3">
		    <div class="offset-sm-4 col-sm-4 text-center">
  			  <input name="doLogin" class="btn btn-primary" value="<fmt:message key="Webpage.login.Login" />" type="submit" /> 
		    </div>
		    <c:if test="${it.loginOK}">
			  <div class="col-sm-4 text-center">
			     <input name="doLogout" class="btn btn-secondary" value="<fmt:message key="Webpage.login.Logout" />" type="submit" /> 
			  </div>
		    </c:if>
		  </div>
	    </div>
      </div>
	  
      <c:if test="${not empty it.nextSteps}">
        <div class="row">
          <div class="offset-sm-2 col-sm-8">
		    <div class="card border border-primary mt-5">
              <div class="card-header text-bg-light"><strong><fmt:message key="Webpage.login.your_options" /></strong></div>
  			  <div class="card-body">
  			    <ul>
    			  <c:forEach var="nextStep" items="${it.nextSteps}">
    			    <c:set var="href"><c:out escapeXml="true" value="${nextStep.url}"/></c:set>
				    <li><a href="${href}">${nextStep.label}</a></li>
			      </c:forEach>
			    </ul>
			  </div>
		    </div>
          </div>
        </div>
      </c:if>
    </form>
	</div>
   </div>
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>