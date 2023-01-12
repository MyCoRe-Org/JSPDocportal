<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<fmt:message var="pageTitle" key="Webpage.error.title" />
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
			<div class="row">
				<div class="col-12">
					<h2><c:out value="${pageTitle}" /></h2>
					<c:if test="${not empty it.errorInfo.message or not empty it.errorInfo.exception}">
						<fmt:message var="showDetails" key="Webpage.error.button.details" />
						<button type="button" class="btn btn-default btn-sm float-right d-none d-sm-block" style="border:none;color:#DEDEDE;" data-toggle="collapse" data-target="#errorInfoPanel" title="${showDetails}">
	    					<span class="fas fa-wrench"></span>
	        			</button>
	        		</c:if>
					<c:if test="${not empty it.errorInfo.headline}">
						<h4>${it.errorInfo.headline}</h4>
					</c:if>
				</div>
			</div>
			<div class="row">
				<c:if test="${not empty it.errorInfo.message or not empty it.errorInfo.exception}">
					<div id="errorInfoPanel" class="collapse col-12 col-md-11 offset-md-1">
						<div class="card mt-3 border border-danger ">
							<c:if test="${not empty it.errorInfo.message}">
								<div class="card-header bg-danger text-white"><c:out value="${it.errorInfo.message}" /></div>
							</c:if>
							<c:if test="${not empty it.errorInfo.exception}">
								<div class="card-body">
									<pre><code><strong><c:out value="${it.errorInfo.exception.localizedMessage}" /></strong></code></pre>
									<pre><samp style="white-space:normal;">
										<c:forEach var="ste" items="${it.errorInfo.exception.stackTrace}">
											<c:out value="${ste}" />
											<br />
										</c:forEach>
									</samp></pre>
								</div>
							</c:if>
						</div>
					</div>
				</c:if>
			</div>
		 </div>
  </div>   
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>
