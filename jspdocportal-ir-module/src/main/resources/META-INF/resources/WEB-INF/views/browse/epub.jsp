<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<%@ taglib prefix="browse" tagdir="/WEB-INF/tags/ir/browse"%>
<c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_browse</c:set>
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
          $(function(){
          	$('#facetInfo').on('hidden.bs.collapse', function () {
          		$("#btnToogleFilterTextOn").addClass('d-none');
          		$("#btnToogleFilterTextOff").removeClass('d-none');
        	});
        	$('#facetInfo').on('shown.bs.collapse', function () {
        		$("#btnToogleFilterTextOn").removeClass('d-none');
           		$("#btnToogleFilterTextOff").addClass('d-none');
        	});
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
						  window.location=$("meta[name='mcr:baseurl']").attr("content")
										    + "browse/epub?_search="
							       			+ $("meta[name='mcr:search.id']").attr("content")
							    			+ "&_add-filter="
							    			+ encodeURIComponent("+" + $("input[name='filterField']:checked").val()+":"+$("#filterValue").val());
						}
						function changeFilterExcludeURL() {
							window.location=$("meta[name='mcr:baseurl']").attr("content")
						    		   + "browse/epub?_search="
						   	   	       + $("meta[name='mcr:search.id']").attr("content")
						   	   		   + "&_add-filter="
						   	   		   + encodeURIComponent("-" + $("input[name='filterField']:checked").val()+":"+$("#filterValue").val());
						}
                    </script>
                  
                    <fmt:message var="lblTerm" key="Browse.Filter.term" />
                    <input class="form-control border-secondary" id="filterValue" name="filterValue" placeholder="${lblTerm}"
                           type="text" onkeypress="if (event.keyCode == 13) { changeFilterIncludeURL();}">
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
                          <input id="filterField1" type="radio" class="form-check-input" name="filterField" value="content">
                          <label for="filterField1" class="form-check-label" "><fmt:message key="Browse.Filter.epub.content" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField2" type="radio" class="form-check-input" name="filterField" value="allMeta" checked="checked">
                          <label for="filterField2" class="form-check-label" ><fmt:message key="Browse.Filter.epub.allMeta" /></label>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField3" type="radio" class="form-check-input" name="filterField" value="ir.title_all" >
                          <label for="filterField3" class="form-check-label"><fmt:message key="Browse.Filter.epub.ir.title_all" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField4" type="radio" class="form-check-input" name="filterField" value="ir.pubyear_start">
                          <label for="filterField4" class="form-check-label"><fmt:message key="Browse.Filter.epub.ir.pubyear_start" /></label>
                        </div>
                      <td>
                    </tr>
                    <tr>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField5"  type="radio"class="form-check-input" name="filterField" value="ir.creator_all">
                          <label for="filterField5" class="form-check-label"><fmt:message key="Browse.Filter.epub.ir.creator_all" /></label>
                        </div>
                      </td>
                      <td>
                        <div class="form-check form-control-sm">
                          <input id="filterField6" type="radio" class="form-check-input" name="filterField" value="ir.pubyear_end">
                          <label for="filterField6" class="form-check-label"><fmt:message key="Browse.Filter.epub.ir.pubyear_end" /></label>
                        </div>
                      <td>
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
                  <c:url var="url" value="${WebApplicationBaseURL}browse/epub">
                    <c:param name="_search" value="${it.result.id}" />
                    <c:param name="_remove-filter" value="${fq}" />
                  </c:url>
                  <c:set var="c"><fmt:message key="Browse.Filter.epub.${fn:substringBefore(fn:substring(fq, 1, -1),':')}" />: ${it.util.calcFacetOutputString(fn:substringBefore(fn:substring(fq, 1, -1),':'), fn:substringAfter(fn:substring(fq, 1, -1),':'))}</c:set>
                   <button class="btn btn-sm ir-filter-btn active mb-2" 
                       onclick="window.location.href='${url}'">
                      <i class="fas fa-times" style="position: absolute; top: 5px; right: 5px; color: darkred;"></i>
                      <span>${c}</span>
                  </button>
                </c:if>
              </c:forEach>
            </div>
          </div>

          <search:result-facets result="${it.result}" mask="epub" top="5" />
          </div>
        </div>
      </div>
      
      <div class="col-xs-12 col-md-9">
        <search:result-sorter result="${it.result}" mode="browse"
                              fields="score,ir.pubyear_start,modified,ir.creator.sort,ir.title.result" mask="epub" />
                              
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
