<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>
<c:set var="org.mycore.navigation.path" scope="request">left.epub.epub_recherche</c:set>
<fmt:message var="pageTitle" key="Webpage.browse.title.${actionBean.result.mask}" />
<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle="${pageTitle}">
  <stripes:layout-component name="html_head">
    <meta name="mcr:search.id" content="${actionBean.result.id}" />
  </stripes:layout-component>
  <stripes:layout-component name="main_part">
    <div class="container">
    <div class="row">
      <div class="col">
        <h2>${pageTitle}</h2>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-md-3 ">
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
           <div style="position:absolute;top:0px;right:15px" class="d-block d-md-none">
                <button id="btnToogleFilter" class="btn btn-lg btn-link" data-toggle="collapse" data-target="#facetInfo">
                      <i id="btnToogleFilterTextOn" class="fa fa-toggle-on" style="color:#004a99;"></i>
                      <i id="btnToogleFilterTextOff" class="fa fa-toggle-off d-none" style="color: #FFA100;"></i>
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
                  <%--
                  <span class="input-group-addon">
 					<select id="filterField" name="filterField" style="height:99%">
					  <option value="ir.creator_all"><fmt:message key="Browse.Filter.histbest.ir.creator_all"/></option>
					  <option value="ir.title_all"><fmt:message key="Browse.Filter.histbest.ir.title_all"/></option>
					  <option value="ir.pubyear_start"><fmt:message key="Browse.Filter.histbest.ir.pubyear_start"/></option>
					  <option value="ir.pubyear_end"><fmt:message key="Browse.Filter.histbest.ir.pubyear_end" /></option>
					</select>
				  </span>
                  --%>
                  <input class="form-control border-secondary" id="filterValue" name="filterValue" placeholder="Wert"
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
                        <input type="radio" id="filterField2" name="filterField" value="content" class="form-check-input">
                        <label class="form-check-label" for="filterField2"><fmt:message key="Browse.Filter.epub.content" /></label>
                      </div>
                    <td>
                    <td>
                      <div class="form-check form-control-sm">
                        <input type="radio" id="filterField1" name="filterField" value="allMeta" class="form-check-input" checked="checked">
                        <label class="form-check-label" for="filterField1"><fmt:message key="Browse.Filter.epub.allMeta" /></label>
                      </div>
                    <td>
                  </tr>
                  <tr>
                    <td>
                      <div class="form-check form-control-sm">
                        <input type="radio" id="filterField3" name="filterField"  value="ir.title_all" class="form-check-input">
                        <label class="form-check-label" for="filterField3"><fmt:message key="Browse.Filter.epub.ir.title_all" /></label>
                      </div>
                    <td>
                    <td>
                      <div class="form-check form-control-sm">
                        <input type="radio" id="filterField4" name="filterField" value="ir.pubyear_start" class="form-check-input">
                        <label class="form-check-label" for="filterField4"><fmt:message key="Browse.Filter.epub.ir.pubyear_start" /></label>
                      </div>
                    <td>
                  </tr>
                   <tr>
                    <td>
                      <div class="form-check form-control-sm">
                        <input type="radio" id="filterField5" name="filterField"  value="ir.creator_all" class="form-check-input">
                        <label class="form-check-label" for="filterField5"><fmt:message key="Browse.Filter.epub.ir.creator_all" /></label>
                      </div>
                    <td>
                    <td>
                      <div class="form-check form-control-sm">
                        <input type="radio" id="filterField6" name="filterField"  value="ir.pubyear_end" class="form-check-input">
                        <label class="form-check-label" for="filterField6"><fmt:message key="Browse.Filter.epub.ir.pubyear_end" /></label>
                      </div>
                    <td>
                  </tr>
                </table>
              </div>
            </div>
            </div>
          </form>

          <div class="row" style="margin-bottom: 10px;">
            <div class="col">
              <c:forEach var="fq" items="${actionBean.result.filterQueries}">
                <c:if test="${not fn:contains(fq, '.facet:')}">
                  <c:url var="url" value="${WebApplicationBaseURL}browse/epub">
                    <c:param name="_search" value="${actionBean.result.id}" />
                    <c:param name="_remove-filter" value="${fq}" />
                  </c:url>
                  <c:set var="c"><fmt:message key="Browse.Filter.epub.${fn:substringBefore(fn:substring(fq, 1, -1),':')}" />: ${actionBean.calcFacetOutputString(fn:substringBefore(fn:substring(fq, 1, -1),':'), fn:substringAfter(fn:substring(fq, 1, -1),':'))}</c:set>
                   <button class="btn btn-sm ir-filter-btn active" 
                       onclick="window.location.href='${url}'">
                      <i class="fas fa-times" style="position: absolute; top: 5px; right: 5px; color: darkred;"></i>
                      <span>${c}</span>
                  </button>
                </c:if>
              </c:forEach>
            </div>
          </div>

          <search:result-facets result="${actionBean.result}" mask="epub" top="5" />
          </div>
        </div>
      </div>
      <div class="col-xs-12 col-md-9">
        <search:result-sorter result="${actionBean.result}"
                              fields="score,ir.pubyear_start,modified,ir.creator.result,ir.title.result" mask="epub" />
                              
        <search:result-browser result="${actionBean.result}">
          <c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
          <c:choose>
            <c:when test="${(doctype eq 'disshab') or (doctype eq 'thesis')}">
              <search:result-entry-disshab entry="${entry}" url="${url}" />
            </c:when>
            <c:when test="${(doctype eq 'document') or (doctype eq 'bundle')}">
              <search:result-entry-document entry="${entry}" url="${url}" />
            </c:when>
            <c:otherwise>
              <search:result-entry entry="${entry}" url="${url}" />
            </c:otherwise>
          </c:choose>
          <%--<div style="clear:both"></div> --%>
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
          <div class="col-3">
              <div class="h-100 bg-dark"></div>
          </div>
          <div class="col-9 bg-white"></div>
        </div>
      </div>
    </div>
  </stripes:layout-component>
</stripes:layout-render>
