<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<%@ taglib prefix="browse" tagdir="/WEB-INF/tags/ir/browse"%>

<c:set var="org.mycore.navigation.path" scope="request">left.histbest.histbest_browse</c:set>
<fmt:message var="pageTitle" key="Browse.Title.${it.result.mask}" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
  <meta name="mcr:search.id" content="${it.result.id}" />
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div id="content_area">
  <div class="container">
    <div class="row">
      <div class="col">
        <h2>${pageTitle}</h2>
      </div>
    </div>
    
    <div class="row">
      <div class="col col-md-3 ">
        <div class="ir-facets h-100">
          <h3><fmt:message key="Browse.Filter.headline" /></h3>
          <script type="text/javascript">
            document.addEventListener('DOMContentLoaded', function() {
              const facetInfo = document.getElementById('facetInfo');
              if (facetInfo) {
                  facetInfo.addEventListener('hidden.bs.collapse', function() {
                      document.getElementById('btnToogleFilterTextOn')?.classList.add('d-none');
                      document.getElementById('btnToogleFilterTextOff')?.classList.remove('d-none');
                  });

                  facetInfo.addEventListener('shown.bs.collapse', function() {
                      document.getElementById('btnToogleFilterTextOn')?.classList.remove('d-none');
                      document.getElementById('btnToogleFilterTextOff')?.classList.add('d-none');
                  });
              }
            });
          </script>
          <div style="position:absolute;top:-15px; right:0px" class="d-block d-md-none">
             <button id="btnToogleFilter" class="btn btn-lg btn-link" data-toggle="collapse" data-target="#facetInfo">
               <i id="btnToogleFilterTextOn" class="fa fa-toggle-on text-primary"></i>
               <i id="btnToogleFilterTextOff" class="fa fa-toggle-off d-none text-warning"></i>
             </button>
          </div>
          <div id="facetInfo" class="collapse show">
          <form class="form-horizontal" onsubmit="return false;">
            <div class="form-group">
              <div class="form-row">
                <div class="col">
                  <div class="input-group input-group-sm">
                    <script type="text/javascript">
                      function changeFilterIncludeURL() {
                        window.location = document.querySelector("meta[name='mcr:baseurl']").content
                          + "do/browse/histbest?_search="
                          + document.querySelector("meta[name='mcr:search.id']").content
                          + "&_add-filter="
                          + encodeURIComponent(""
                              +"+" + document.querySelector("input[name='filterField']:checked").value
                              +":" + document.getElementById("filterValue").value);
                      }
                      function changeFilterExcludeURL() {
                        window.location = document.querySelector("meta[name='mcr:baseurl']").content
                         + "do/browse/histbest?_search="
                         + document.querySelector("meta[name='mcr:search.id']").content
                         + "&_add-filter="
                         + encodeURIComponent(""
                             + "-" + document.querySelector("input[name='filterField']:checked").value
                             + ":" + document.getElementById("filterValue").value);
                      }
                    </script>
                    
                    <fmt:message var="lblTerm" key="Browse.Filter.term" />
                      <input class="form-control border-secondary" id="filterValue" name="filterValue" placeholder="${lblTerm}"
                             type="text" onkeypress="if (event.key === 'Enter') { changeFilterIncludeURL();}">
                      <div class="input-group-prepend">
                        <button id="filterInclude" class="btn btn-primary" onclick="changeFilterIncludeURL();">
                          <i class="fas fa-plus"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              <div class="form-row">   
                <div class="col">
                  <table class="w-100">
                    <tr>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField1"  type="radio" class="form-check-input" name="filterField" value="allMeta" checked="checked">
                          <label for="filterField1" class="form-check-label" ><fmt:message key="Browse.Filter.histbest.allMeta" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField2"  type="radio" class="form-check-input" name="filterField" value="content" >
                          <label for="filterField2" class="form-check-label" style="line-height:1em;" ><fmt:message key="Browse.Filter.histbest.content" /></label>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField3"  type="radio" class="form-check-input" name="filterField" value="ir.title_all" >
                          <label for="filterField3" class="form-check-label" ><fmt:message key="Browse.Filter.histbest.ir.title_all" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField4"  type="radio" class="form-check-input" name="filterField" value="ir.pubyear_start" >
                          <label for="filterField4" class="form-check-label" ><fmt:message key="Browse.Filter.histbest.ir.pubyear_start" /></label>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField5"  type="radio" class="form-check-input" name="filterField" value="ir.creator_all" >
                          <label for="filterField5" class="form-check-label" ><fmt:message key="Browse.Filter.histbest.ir.creator_all" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField6"  type="radio" class="form-check-input" name="filterField" value="ir.pubyear_end" >
                          <label for="filterField6" class="form-check-label" ><fmt:message key="Browse.Filter.histbest.ir.pubyear_end" /></label>
                        </div>
                      </td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
          </form>
          
          <div class="row mb-3">
            <div class="col">
              <c:forEach var="fq" items="${it.result.filterQueries}">
                <c:if test="${not fn:contains(fq, '.facet:')}">
                  <c:url var="url" value="${WebApplicationBaseURL}do/browse/histbest">
                    <c:param name="_search" value="${it.result.id}" />
                    <c:param name="_remove-filter" value="${fq}" />
                  </c:url>
                  <c:set var="c"><fmt:message key="Browse.Filter.histbest.${fn:substringBefore(fn:substring(fq, 1, -1),':')}" />: ${it.util.calcFacetOutputString(fn:substringBefore(fn:substring(fq, 1, -1),':'), fn:substringAfter(fn:substring(fq, 1, -1),':'))}</c:set>
                   <button class="btn btn-sm ir-filter-btn active mb-2"
                       onclick="window.location.href='${url}'">
                      <i class="fas fa-times" style="position: absolute; top: 5px; right: 5px; color: darkred;"></i>
                      <span>${c}</span>
                  </button>
                </c:if>
              </c:forEach>
            </div>
          </div>
          
          <search:result-facets result="${it.result}" mask="histbest" top="5" />
        </div>
        </div>
      </div>

      <div class="col-xs-12 col-md-9">
        <search:result-sorter result="${it.result}" mode="browse"
                              fields="score,ir.pubyear_start,modified,ir.creator.sort,ir.title.result" mask="histbest" />
        
        <search:result-browser result="${it.result}">
          <browse:result-entry entry="${entry}" url="${url}" />
        </search:result-browser>
      </div>
    </div>
    </div>
    
    <div class="ir-footer-space" style="height: 75px;">
      <div class="container h-100">
        <div class="row h-100">
          <div class="col-3">
              <div class="h-100 ir-footer-space-left"></div>
          </div>
          <div class="col-9 ir-footer-space-right"></div>
        </div>
      </div>
    </div>
    </div>
   <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>
