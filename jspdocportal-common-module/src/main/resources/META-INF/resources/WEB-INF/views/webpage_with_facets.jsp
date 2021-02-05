<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean" %>
<%@ page import="org.mycore.common.config.MCRConfiguration2" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

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
     <c:if test="${fn:contains(WebApplicationBaseURL, 'rosdok')}">
     <div class="row">
       <div class="col-12 col-md-8 my-3 pr-5">
          <mcr:includeWebcontent id="${fn:replace(it.path, '/', '.')}" file="${it.path}.html" />
      </div>
      <div class="col-md-4 d-none d-md-block">
         <%--epub or histbest --%>
        <fmt:message var="img" key="Webpage.browse.${it.path}.image">
        	<fmt:param>${WebApplicationBaseURL}</fmt:param>
        </fmt:message>
		<img src="${img}" style="width:100%">
      </div>
      </div>
      <div class="row my-5">
      	<div class="col-12 col-md-8">
      		<search:main-searchmask mode="${it.path}" />
         </div>
      </div>
      </c:if>
      
      <c:if test="${fn:contains(WebApplicationBaseURL, 'dbhsnb') or fn:contains(WebApplicationBaseURL, 'hs-nb')}">
      	<div class="row">
      		<div class="col-12 col-md-8 my-3" style="padding:0 90px 0 30px">
          		<h2>Digitale Bibliothek Neubrandenburg</h2>
          		<div class="mt-5">
         			<search:main-searchmask mode="${it.path}" />
         		</div>
         		<div style="margin-top:70px">
         		<mcr:includeWebcontent id="${fn:replace(it.path, '/', '.')}" file="${it.path}.html" />
         		</div>
      		</div>
      		
       		<div class="col-md-4 d-none d-md-block" style="padding:30px 0px 0px 0px">
        		<img src="${WebApplicationBaseURL}themes/hsnb/images/hsnb_building.jpg" style="width:100%;height:200px">
				<div class="text-center" style="margin:30px 0px 30px 0px;">
                	<a href="https://www.hs-nb.de/bibliothek/hauptmenue/informieren/infos/abschlussarbeit-archivieren/" class="btn btn-primary">
  						Meine Abschlussarbeit<br>archivieren oder veröffentlichen
					</a>
        		</div>
        	</div>
      	</div>
      </c:if>
     
      <c:if test="${(it.path eq 'epub') or (it.path eq 'histbest') }">
            <script type="text/javascript">
				function changeFacetIncludeURL(key, value, mask) {
					window.location=$("meta[name='mcr:baseurl']").attr("content")
							 	       + "browse/"+mask+"?"
							           + "&_add-filter="
							       + encodeURIComponent("+" + key +":"+ value.replace('epoch:',''));
					}
				
				
				</script>
				
				  <c:set var="mask" value="${it.path}" />
            <%
			        MCRSearchResultDataBean result = new MCRSearchResultDataBean();
		    	    result = new MCRSearchResultDataBean();
		        	result.setQuery(MCRConfiguration2.getString("MCR.Browse." + pageContext.getAttribute("mask") + ".Query").orElse("*:*"));
		        	result.setMask((String)pageContext.getAttribute("mask"));
		        	result.setAction("browse/" + pageContext.getAttribute("mask"));
		        	result.getFacetFields().clear();
		        	for (String ff : MCRConfiguration2.getString("MCR.Browse." + pageContext.getAttribute("mask") + ".FacetFields").orElse("").split(",")) {
		            	if (ff.trim().length() > 0) {
			                result.getFacetFields().add(ff.trim());
			            }
			        }
			        result.setRows(20);
			        MCRSearchResultDataBean.addSearchresultToSession(request, result);
			        result.doSearch();
					pageContext.setAttribute("result", result);					
				%>

            <%-- key=$("input[name='filterField']:checked").val(); value=$('#filterValue').val()); --%>

          </c:if>
		</div>
		

<%--Facetten --%>
	 		<c:if test="${fn:contains(WebApplicationBaseURL, 'rosdok')}">
            	<div class="bg-light">
	        		<div class="container">	
    		      		<div class="row">
	 		
            			<c:if test="${it.path eq 'histbest' }">
                <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                <%-- <search:browse-classification categid="doctype:histbest" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                <search:browse-classification categid="collection:Materialart" mask="${mask}" lang="x-de-short"
                  facetField="ir.collection_class.facet" />
              </div>
              <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.collection_class.facet" /> --%>
                <search:browse-classification categid="collection:Projekte" mask="${mask}"
                  facetField="ir.collection_class.facet" />
              </div>
              <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.epoch_msg.facet" /> --%>
                <search:browse-classification categid="epoch" mask="${mask}" facetField="ir.epoch_msg.facet" />
              </div>
              <div class="col-md-3 col-12 bg-dark">
         			<div class="ir-latestdocs">
           				<h4><a href="${WebApplicationBaseURL}browse/${it.path}"></a><fmt:message key="Browse.latestdocs" /></a></h4>       
           				<div id="latest_documents" data-ir-mode="${it.path}"></div>
          				<a href="${WebApplicationBaseURL}browse/${it.path}" class="ir-latestdocs-more-button btn btn-sm btn-primary float-right mt-3">mehr ...</a>			
	    			</div>
      			</div>
            
          </c:if>

            <c:if test="${it.path eq 'epub' }">
             
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                  <search:browse-classification categid="doctype:epub" mask="${mask}" facetField="ir.doctype_class.facet" />
                </div>
              	<div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.sdnb_class.facet" /> --%>
                  <search:browse-classification categid="SDNB" mask="${mask}" facetField="ir.sdnb_class.facet" />
              	</div>
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.institution_class.facet" /> --%>
                  <search:browse-classification categid="institution" mask="${mask}" facetField="ir.institution_class.facet" />
                </div>
                <div class="col-md-3 col-12 bg-dark">
         <div class="ir-latestdocs">
           
         	<h4><a href="/browse/${it.path}"><fmt:message key="Browse.latestdocs" /></a></h4>
 
                  
           <div id="latest_documents" data-ir-mode="${it.path}">
            <%--
            <div class="card ir-latestdocs-card">
              <div class="card-body">
                <p class="card-text">Meinhardt, Jennifer</p>
                <h4 class="card-title">
                  <a class="card-link" href="#">Das Konnektom des Cortex cerebri der Ratte</a>
                </h4>
                <table>
                  <tr>
                    <td style="vertical-align: top; width: 67%;">
                      <p class="card-text">Neubrandenburg : Hochschule , 2016</p>
                      <p class="card-text text-secondary font-weight-bold">Bachelorarbeit</p>
                      <p class="card-text text-secondary">14.12.2016</p>
                    </td>
                    <td style="vertical-align: bottom; width: 33%; padding-left: 15px;">
                      <a href="../resolve/id/dbhsnb_thesis_0000001540">
                        <img class="ir-latestdocs-cover" style="max-width: 100%; max-height: 180px; object-fit: contain;" src="http://rosdok.uni-rostock.de/file/rosdok_document_0000012807/rosdok_derivate_0000044495/ppn102519165X.cover.jpg">
                      </a>
                    </td>
                  </tr>
                </table>
              </div>
		    </div> --%>
          </div>
          <a href="${WebApplicationBaseURL}browse/${it.path}" class="ir-latestdocs-more-button btn btn-sm btn-primary float-right mt-3">mehr ...</a>			
	    </div>
	    </div>
	    </c:if>
      </div>
      </div>
      </div>
      <div class="bg-light" style="height: 75px;">
      <div class="container h-100">
        <div class="row h-100">
          <div class="col-9 bg-light"></div>
          <div class="col-3 bg-dark"></div>
        </div>
      </div>
    </div>
    </c:if>
    
    <c:if test="${fn:contains(WebApplicationBaseURL, 'dbhsnb') or fn:contains(WebApplicationBaseURL, 'hs-nb')}">
        <div class="bg-light">
	       <div class="container">	
    		<div class="row">
                <div class="col-md-4 col-12" style="padding-bottom:60px">
                	<div class="h-25 pb-md-2">
                	  <search:browse-classification categid="doctype:epub" mask="${mask}" facetField="ir.doctype_class.facet" />
                  </div>
                  <div class="h-75 pt-md-2">
                      <search:browse-classification categid="institution:HSNB" mask="${mask}" facetField="ir.institution_class.facet" />
                  </div>
                </div>
              	<div class="col-md-4 col-12" style="padding-right:30px;padding-bottom:45px">
                  <search:browse-classification categid="SDNB" mask="${mask}"  facetField="ir.sdnb_class.facet" />
              	</div>
              	
                <div class="col-md-4 col-12 bg-dark" style="padding-bottom:30px;">
         			<div class="ir-latestdocs">
           				<h4 style="padding-top:"><fmt:message key="Browse.latestdocs" /></h4>       
           				<div id="latest_documents" data-ir-mode="${it.path}"></div>
          				<a href="${WebApplicationBaseURL}browse/${it.path}" class="ir-latestdocs-more-button btn btn-sm btn-primary float-right mt-3">mehr ...</a>			
	    			</div>
      			</div>
      			</div>
      			</div>
      			</div>
    </c:if>

    <%@ include file="fragments/startpage/js_latestdocs.jspf" %> 		

    <%@ include file="fragments/footer.jspf" %>
  </body>
</html>

