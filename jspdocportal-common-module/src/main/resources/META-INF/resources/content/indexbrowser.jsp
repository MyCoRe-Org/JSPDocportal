<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<fmt:message var="pageTitle"
	key="Webpage.indexbrowser.${actionBean.modus}.title" />
<stripes:layout-render name="../WEB-INF/layout/default.jsp"
	pageTitle="${pageTitle}" layout="3columns">
	<stripes:layout-component name="html_head">
	<style>
		.indexbrowser-btn {
			margin-top:0.40em;
			padding-left:0.40em;
			padding-right:0.40em;
		}	
	</style>

	</stripes:layout-component>
	<stripes:layout-component name="main_part">
	    <div class="row indexbrowser-row">
        <div class="col">
		<div>
			<mcr:includeWebcontent id="indexbrowser_intro" file="indexbrowser/${actionBean.modus}_intro.html" />
		</div>
		<div class="row">
		  <div class="col-12">
		    <div class="indexbrowser-navbar">
			  <div class="navbar navbar-default indexbrowser-navbar-primary">
				<c:forEach var="x" items="${actionBean.firstSelector}">
				  <c:set var="active"></c:set>
				  <c:if test="${fn:startsWith(actionBean.select, x)}"><c:set var="active">active</c:set></c:if>
			      <a href="${WebApplicationBaseURL}indexbrowser/${actionBean.modus}?select=${x}"
					 class="btn btn-outline-secondary navbar-btn indexbrowser-btn ${active}" role="button">${x}</a>
			    </c:forEach>
			  </div>
			  <c:if test="${not empty actionBean.secondSelector}">
				<div class="navbar navbar-default indexbrowser-navbar-secondary">
				  <c:forEach var="x" items="${actionBean.secondSelector}">
				    <c:set var="active"></c:set>
					  <c:if test="${fn:startsWith(actionBean.select, x.key)}"><c:set var="active">active</c:set></c:if>
						<a href="${WebApplicationBaseURL}indexbrowser/${actionBean.modus}?select=${x.key}"
						   class="btn btn-outline-secondary btn-sm indexbrowser-btn ${active}" role="button">${x.key} 
						   <span class="badge badge-pill badge-secondary" style="font-size:80%;margin-left:8px">${x.value}</span></a>
				  </c:forEach>
			    </div>
			  </c:if>
			</div>
	      </div>
		</div>
		<div class="row">
			<div class="col-12">
				<div class="card indexbrowser-searchbar">
					<div class="card-body">
						<stripes:form beanclass="org.mycore.frontend.jsp.stripes.actions.IndexBrowserAction"
			   	  			          id="indexbrowserForm" enctype="multipart/form-data" acceptcharset="UTF-8" class="form-inline">
							<stripes:hidden name="modus">${actionBean.modus}</stripes:hidden>
							<stripes:label for="txtSelect"><fmt:message key="Webpage.indexbrowser.form.label" />:</stripes:label>&#160;&#160;&#160;&#160;&#160;
							<stripes:text class="form-control form-control-sm" id="txtSelect" name="select" />
							<fmt:message var="output" key="Webpage.indexbrowser.form.button" />
							<stripes:submit name="doSearch" value="${output}" class="btn btn-sm btn-primary ml-4" />
						</stripes:form>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<div class="card ir-result-card">				
					<c:forEach var="r" items="${actionBean.result.entries}">
						<div class="card-body">
							<search:result-entry entry="${r}" url="${WebApplicationBaseURL}resolve/id/${r.mcrid}?_search=${actionBean.result.id}" protectDownload="true" />
						</div>
					</c:forEach>
				</div>
			</div>
		</div>
	</div>
    <div class="col-3">
    </div>
    </div>
  </stripes:layout-component>
</stripes:layout-render>
