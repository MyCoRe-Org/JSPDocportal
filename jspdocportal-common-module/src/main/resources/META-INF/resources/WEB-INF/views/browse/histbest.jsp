<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<c:set var="org.mycore.navigation.path" scope="request">left.histbest.histbest_recherche</c:set>
<fmt:message var="pageTitle" key="Webpage.browse.title.${it.result.mask}" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
  <meta name="mcr:search.id" content="${it.result.id}" />
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div class="container">
    <div class="row">
      <div class="col-xs-12">
        <h2>${pageTitle}</h2>
      </div>
    </div>
    <div class="row">
      <div class="col-xs-12 col-md-3">
        <div class="ir-box ir-box-bordered" style="margin-top:42px; margin-bottom:45px;position:relative">
          <h3><fmt:message key="Browse.Filter.headline" /></h3>
          <script type="text/javascript">
          $(function(){
          	$('#facetInfo').on('hidden.bs.collapse', function () {
          		$("#btnToogleFilterTextOn").addClass('hidden');
          		$("#btnToogleFilterTextOff").removeClass('hidden');
        	});
        	$('#facetInfo').on('shown.bs.collapse', function () {
        		$("#btnToogleFilterTextOn").removeClass('hidden');
           		$("#btnToogleFilterTextOff").addClass('hidden');
        	});
          });
          </script>
           <div style="position:absolute;top:0px;right:0px" class="visible-xs-block visible-sm-block">
                <button id="btnToogleFilter" class="btn btn-lg btn-link" data-toggle="collapse" data-target="#facetInfo">
                      <i id="btnToogleFilterTextOn" class="fa fa-toggle-on" style="color:#004a99;"></i>
                      <i id="btnToogleFilterTextOff" class="fa fa-toggle-off hidden" style="color: #FFA100;"></i>
                </button>
            </div>
          <div id="facetInfo" class="collapse in">
          <form class="form-horizontal" onsubmit="return false;">
            <div class="form-group">
              <div class="col-sm-12">
              <%--
                 <select id="filterField" name="filterField" class="form-control input-sm" style="width:12em;border-radius:0px;background-color:#777777;color:white;margin-bottom:-1px">
                    <option value="allMeta"><fmt:message key="Browse.Filter.histbest.allMeta" /></option>
                    <option value="content"><fmt:message key="Browse.Filter.histbest.content" /></option>
                    <option value="ir.title_all"><fmt:message key="Browse.Filter.histbest.ir.title_all" /></option>
                    <option value="ir.creator_all"><fmt:message key="Browse.Filter.histbest.ir.creator_all" /></option>
                    <option value="ir.pubyear_start"><fmt:message key="Browse.Filter.histbest.ir.pubyear_start" /></option>
                    <option value="ir.pubyear_end"><fmt:message key="Browse.Filter.histbest.ir.pubyear_end" /></option>
                  </select>
                --%>
                <div class="input-group input-group-sm">
                  <script type="text/javascript">
					function changeFilterIncludeURL() {
						window.location=$("meta[name='mcr:baseurl']").attr("content")
				 				    + "browse/histbest?_search="
				        			+ $("meta[name='mcr:search.id']").attr("content")
					    			+ "&_add-filter="
					    			+ encodeURIComponent("+" + $("input[name='filterField']:checked").val()+":"+$("#filterValue").val());
					}
					function changeFilterExcludeURL() {
						window.location=$("meta[name='mcr:baseurl']").attr("content")
					    		   + "browse/histbest?_search="
				      	   	       + $("meta[name='mcr:search.id']").attr("content")
					   	   		   + "&_add-filter="
					   	   		   + encodeURIComponent("-" + $("input[name='filterField']:checked").val()+":"+$("#filterValue").val());
					}
										<%-- for select box use: $("#filterField option:selected").val() --%>
				</script>

                  <input class="form-control" id="filterValue" name="filterValue" style="width: 100%" placeholder="Wert"
                    type="text" onkeypress="if (event.keyCode == 13) { changeFilterIncludeURL();}"> <span
                    class="input-group-btn">
                    <button id="filterInclude" class="btn btn-primary ir-facets-btn-plus" onclick="changeFilterIncludeURL();">
                      <i class="fa fa-plus"></i>
                    </button>
                  </span>
                </div>
              </div>

              <div class="col-sm-12">
                <table>
                  <tr>
                    <td class="radio input-sm"><label> <input name="filterField" value="allMeta"
                        type="radio" checked="checked" /> <fmt:message key="Browse.Filter.histbest.allMeta" />
                    </label>
                    <td>
                    <td class="radio input-sm"><label> <input name="filterField" value="content"
                        type="radio" /> <fmt:message key="Browse.Filter.histbest.content" />
                    </label>
                    <td>
                  </tr>
                  <tr>
                    <td class="radio input-sm"><label> <input name="filterField" value="ir.title_all"
                        type="radio" /> <fmt:message key="Browse.Filter.histbest.ir.title_all" />
                    </label>
                    <td>
                    <td class="radio input-sm"><label> <input name="filterField" value="ir.pubyear_start"
                        type="radio" /> <fmt:message key="Browse.Filter.histbest.ir.pubyear_start" />
                    </label>
                    <td>
                  </tr>
                  <tr>
                    <td class="radio input-sm"><label> <input name="filterField" value="ir.creator_all"
                        type="radio" /> <fmt:message key="Browse.Filter.histbest.ir.creator_all" />
                    </label>
                    <td>
                    <td class="radio input-sm"><label> <input name="filterField" value="ir.pubyear_end"
                        type="radio" /> <fmt:message key="Browse.Filter.histbest.ir.pubyear_end" />
                    </label>
                    <td>
                  </tr>
                </table>
              </div>
            </div>
          </form>

          <div class="row" style="margin-bottom: 10px;">
            <div class="col-sm-12">
              <c:forEach var="fq" items="${it.result.filterQueries}">
                <c:if test="${not fn:contains(fq, '.facet:')}">
                  <c:url var="url" value="${WebApplicationBaseURL}browse/histbest">
                    <c:param name="_search" value="${it.result.id}" />
                    <c:param name="_remove-filter" value="${fq}" />
                  </c:url>
                  <c:set var="c">
                    <fmt:message key="Browse.Filter.histbest.${fn:substringBefore(fn:substring(fq, 1, -1),':')}" />: ${it.util.calcFacetOutputString(fn:substringBefore(fn:substring(fq, 1, -1),':'), fn:substringAfter(fn:substring(fq, 1, -1),':'))}</c:set>
                  <a class="btn btn-sm btn-default ir-facets-btn"
                    style="display: block; text-align: left; white-space: normal; margin-bottom: 3px; color: black; width: 100%"
                    href="${url}"> <i class="fa fa-times" style="float:right; position:relative; right:-6px; color: darkred;"></i>
                    ${c}
                  </a>
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
                              fields="score,ir.pubyear_start,modified,ir.creator.result,ir.title.result" mask="histbest" />
        
        <search:result-browser result="${it.result}">
          <c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
          <c:choose>
            <c:when test="${(doctype eq 'document') or (doctype eq 'bundle')}">
              <search:result-entry-document entry="${entry}" url="${url}" />
            </c:when>
            <c:otherwise>
              <search:result-entry entry="${entry}" url="${url}" />
            </c:otherwise>
          </c:choose>
          <div class="row">
            <div class="col-xs-12 col-md-12 mt-3">
            <p class="card-text">
              <search:show-edit-button mcrid="${mcrid}" cssClass="btn btn-sm btn-primary ir-edit-btn" />
              <span class="badge badge-secondary">${entry.data['ir.doctype.result']}</span>
              <c:choose>
                <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'restrictedaccess')}">
                  <span class="badge ir-badge-restrictedaccess">
                    Restricted <img style="height:1.5em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/>  Access            
                  </span>
                </c:when>
               <c:otherwise>
                <span class="badge ir-badge-openaccess">
                    Open <img style="height:1.5em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Open_Access.png"/>  Access            
                  </span>
               </c:otherwise>
             </c:choose>
             </p>
            </div>
          </div>
        </search:result-browser>
      </div>
    </div>
    </div>
        <div class="bg-white" style="height: 75px;">
      <div class="container h-100">
        <div class="row h-100">
          <div class="col-3 bg-dark"></div>
          <div class="col-9 bg-white"></div>
        </div>
      </div>
    </div>
  <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>

