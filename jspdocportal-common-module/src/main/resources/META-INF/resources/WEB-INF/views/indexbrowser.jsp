<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<c:set var="org.mycore.navigation.path" scope="request">left.indexbrowser_${it.modus}</c:set>
<fmt:message var="pageTitle" key="Webpage.indexbrowser.${it.modus}.title" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div id="content_area">
  <div class="container indexbrowser">
    <div class="row">
      <div class="col-12 col-xl-9">
        <mcr:includeWebcontent id="indexbrowser_intro" file="indexbrowser/${it.modus}_intro.html" />
        <div class="card indexbrowser-card ir-result-card">
          <div class="card-header">
            <div class="navbar navbar-default indexbrowser-navbar-primary">
              <c:forEach var="x" items="${it.firstSelector}">
                <c:set var="active"></c:set>
                <c:if test="${fn:startsWith(it.select, x)}"><c:set var="active">active</c:set></c:if>
                <a href="${WebApplicationBaseURL}do/indexbrowser/${it.modus}?select=${x}"
                   class="btn btn-outline-secondary navbar-btn indexbrowser-btn ${active}" role="button">${x}</a>
              </c:forEach>
            </div>
            <c:if test="${not empty it.secondSelector}">
              <div class="navbar navbar-default indexbrowser-navbar-secondary">
                <c:forEach var="x" items="${it.secondSelector}">
                  <c:set var="active"></c:set>
                  <c:if test="${fn:startsWith(it.select, x.key)}"><c:set var="active">active</c:set></c:if>
                  <a href="${WebApplicationBaseURL}do/indexbrowser/${it.modus}?select=${x.key}"
                     class="btn btn-outline-secondary btn-sm indexbrowser-btn ${active}" role="button">${x.key} 
                  <span class="badge badge-pill badge-secondary ml-2">${x.value}</span></a>
                </c:forEach>
              </div>
            </c:if>
            <div class="navbar navbar-default indexbrowser-searchbar">
              <form action="${applicationScope.WebApplicationBaseURL}do/indexbrowser/${it.modus}"
                    id="indexbrowserForm" enctype="multipart/form-data" accept-charset="UTF-8" class="form-inline">
                <label for="txtSelect"><fmt:message key="Webpage.indexbrowser.form.label" />:</label>
                <input type="text" class="form-control form-control-sm ml-4 w-auto" name="select" value="${it.select}"/>
                <fmt:message var="output" key="Webpage.indexbrowser.form.button" />
                <button type="submit" name="doSearch" class="btn btn-sm btn-primary ml-4">${output}</button>
              </form>
            </div>
          </div>
               
          <c:forEach var="r" items="${it.result.entries}">
            <div class="card-body">
              <search:result-entry entry="${r}" url="${WebApplicationBaseURL}resolve/id/${r.mcrid}?_search=${it.result.id}" />
            </div>
          </c:forEach>
          
        </div>
      </div>
      <div class="col-3">
      </div>
    </div>
    </div>
    </div>
    <%@ include file="fragments/footer.jspf" %>
  </body>
</html>
