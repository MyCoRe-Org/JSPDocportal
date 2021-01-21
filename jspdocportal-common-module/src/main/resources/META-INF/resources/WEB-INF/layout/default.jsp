<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrdd" uri="http://www.mycore.org/jspdocportal/docdetails.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<stripes:layout-definition>
	<%-- Parameters: heading1: The overall heading of the page, usually hidden by CSS
	                 (useful for screen readers) 
	                 
	                 layout: "1column", "2columns", "3columns" (=default)
	--%>
	<!DOCTYPE html>
	<c:if test="${empty layout}">
		<c:set var="layout" value="3columns" />
	</c:if>
	<mcrdd:setnamespace prefix="nav" uri="http://www.mycore.org/jspdocportal/navigation" />
	<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />
	<c:set var="path" value="${requestScope.path}" />
	<%-- set the current language --%>
	<mcr:session var="lang" information="langauage" />
	<fmt:setLocale value="${lang}" scope="request" />

	<html>
	<head>
		<meta charset="UTF-8" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_reset.css" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_layout.css" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_general.css" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_navigation.css" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_content.css" />
		<link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_editor.css" />
		<link rel="shortcut icon" href="${WebApplicationBaseURL}images/mycore_favicon.ico" />
		<stripes:layout-component name="html_head">
			<%-- any additional HTML header content --%>
		</stripes:layout-component>		
	</head>
	<body>
		<fmt:message var="gotoContents" key="Webpage.gotoContents" />  
		<div class="none">
			<h1>${heading1}</h1>
			<p><a href="#contents" title="${gotoContents}">${gotoContents}</a></p>
		</div>
		<div id="wrapper">
			<div id="header">
				<div class="top_navigation">
					<mcr:outputNavigation id="top" mode="top" separatorString="|"/>
					<ol>
						<li class="separator">|</li> 
						<li class="userinfo">
							<mcr:session var="username" info="userID" />
							<c:if test="${not username eq 'guest'}">
								<span class="label"><fmt:message key="Webpage.user" />:&#160;</span>
								<span class="username">							 
									<a href="${WebApplicationBaseURL}nav?path=~userdetail">${username}</a>
								</span>
								[<span class="action">
									<a href="${WebApplicationBaseURL}nav?path=~logout">
										<fmt:message key="Nav.Logout" />
									</a>
								</span>]							
							</c:if>							
							<c:if test="${username eq 'guest'}">
								[<span class="action">
									<a href="${WebApplicationBaseURL}login.action">
											<fmt:message key="Nav.Login" />
									</a>
								</span>]
							</c:if>			
						</li>
					</ol>
					<ol>
						<li class="separator">|</li> 
						<li>
							<mcr:outputLanguageSelector languages="de,en" separatorString="&#160;&#160;|&#160;&#160;" />
						</li>
					</ol>		
				</div>
				<div class="logo">
					<a href="${WebApplicationBaseURL}">
		  					<span style="font-size:36px; font-family:Verdana; color: darkblue;"><fmt:message key="Webpage.application.title" /></span>
					</a>
				</div>
			</div> <!-- END OF header -->
			<div id="left_col" class="left_col_${layout}">
				<div class="base_box">
					<div class="main_navigation">
						<mcr:outputNavigation id="left" expanded="false" mode="left" />
					</div>
					<div style="padding-top:32px;padding-bottom:32px; text-align: center;">
						<a href="http://www.mycore.org">
							<img alt="powered by MyCoRe 2.2"
								 src="${WebApplicationBaseURL}images/poweredByMyCoRe2.png"
							 	 style="border:0;text-align:center; width:90%">
						</a>
					</div>
					<div class="main_navigation">
						<mcr:outputNavigation id="edit" expanded="false" mode="left" />
					</div>
					<div class="main_navigation">
						<mcr:outputNavigation id="admin" expanded="false" mode="left" />
					</div>
				</div>
			</div><!-- END OF left_col -->
			<div id="center_col" class="center_col_${layout}">
				<div class="base_box breadcrumbs">
					<mcr:outputNavigation id="left" mode="breadcrumbs" />				
				</div>
				
				<div id="contents" class="base_content text">
					<stripes:layout-component name="main_part">
						<%--<div>This is the main page ...</div>--%>
					</stripes:layout-component>			
				</div>
			</div><!-- END of content -->
			<div id="right_col" class="right_col_${layout}">
				<stripes:layout-component name="right_side">
				
				</stripes:layout-component>
				
			</div> <!--  END OF right-->
		<div id="footer" >
	        <a href="${WebApplicationBaseURL}nav?path=~impressum">Impressum</a>
	  	</div>
	  </div>

	</body>
	</html>
</stripes:layout-definition>