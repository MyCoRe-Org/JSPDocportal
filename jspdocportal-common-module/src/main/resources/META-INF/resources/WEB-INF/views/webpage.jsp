<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ page import = "org.mycore.common.config.MCRConfiguration2" %>
<% 
    pageContext.setAttribute("navSide", MCRConfiguration2.getString("MCR.JSPDocportal.Navigation.Side").orElse("left"));
%>

<fmt:message var="pageTitle" key="Webpage.title.${fn:replace(it.path, '/', '.')}" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div class="container">
    <c:if test="${not empty it.info}">
      <div class="row">
        <div class="col col-md-8">
          <div class="ir-box">
            <mcr:includeWebcontent id="${fn:replace(it.path, '/', '.')}" file="${it.path}.html" />
          </div>
        </div>
        <div class="col col-md-4">
          <div class="row">
            <c:forEach var="id" items="${fn:split(it.info,',')}">
              <div class="col">
                <div class="ir-box ir-box-bordered">
                  <mcr:includeWebcontent id="${id}" file="${fn:replace(id, '.', '/')}.html" />
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </div>
    </c:if>
    <c:if test="${empty it.info}">
      <div class="row">
        <c:if test="${empty requestScope['org.mycore.navigation.side.path']}">
          <div id="main" class="col ir-content-main">
              <div class="ir-box">
                <mcr:includeWebcontent id="${fn:replace(it.path, '/', '.')}" file="${it.path}.html" />
              </div>
          </div>
       </c:if>
        <c:if test="${not empty requestScope['org.mycore.navigation.side.path']}">
         <c:if test="${pageScope.navSide == 'left'}">
           <div id="left-side-nav" class="col col-md-3 ir-content-left pt-3">
              <mcr:outputNavigation mode="side" id="${fn:substringBefore(requestScope['org.mycore.navigation.side.path'], '.')}"></mcr:outputNavigation>
              <c:if test="${not empty it.infoBox}">
                <mcr:includeWebcontent id="${fn:replace(it.infoBox, '/', '.')}" file="${it.infoBox}.html" />
              </c:if>
            </div>
          </c:if>
          <div id="main" class="col col-md-9 ir-content-main card">
              <div class="card-body">
                <mcr:includeWebcontent id="${fn:replace(it.path, '/', '.')}" file="${it.path}.html" />
              </div>
          </div>
          <c:if test="${pageScope.navSide == 'right'}">
            <div id="right-side-nav" class="col col-md-3 ir-content-right pt-3">
              <mcr:outputNavigation mode="side" id="${fn:substringBefore(requestScope['org.mycore.navigation.side.path'], '.')}"></mcr:outputNavigation>
              <c:if test="${not empty it.infoBox}">
                <mcr:includeWebcontent id="${fn:replace(it.infoBox, '/', '.')}" file="${it.infoBox}.html" />
              </c:if>
            </div>
          </c:if>
       </c:if>
      </div>
    </c:if>
  </div>
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>
