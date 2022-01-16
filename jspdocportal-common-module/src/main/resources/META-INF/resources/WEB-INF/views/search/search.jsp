<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ page import = "org.mycore.common.config.MCRConfiguration2" %>
<% 
    pageContext.setAttribute("navSide", MCRConfiguration2.getString("MCR.JSPDocportal.Navigation.Side").orElse("left"));
%>
<c:set var="org.mycore.navigation.path" scope="request">left.search._${it.mask}</c:set>

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
        <c:if test="${pageScope.navSide == 'left'}">
            <div id="search_nav" class="col-3">
                <mcr:outputNavigation mode="side" id="search" expanded="true"></mcr:outputNavigation>
            </div>
        </c:if>
		<div id="search_content" class="col">
		<c:if test="${not empty it.result.mask}">
	      <div class="row">
             <div class="col">
				<c:set var="classCollapse" value="" />
				<c:if test="${not it.showMask and it.result.numFound>0}">
					<button id="buttonCollapseSearchmask" class="btn btn-secondary float-right" type="button"
						    data-toggle="collapse" data-target="#searchmask" aria-expanded="false" aria-controls="searchmask">
						<fmt:message key="Webpage.Searchresult.redefine" />
					</button>
					<c:set var="classCollapse">collapse</c:set> 
				</c:if>
			
				<div>
					<mcr:includeWebcontent id="search_intro" file="search/${it.result.mask}_intro.html" />
				</div>

				<div class="searchmask ${classCollapse}" id="searchmask">
					<c:out value="${it.xeditorHtml}" escapeXml="false" />
				</div>
				<script type="text/javascript">
              		$('#searchmask').on('show.bs.collapse', function (event) {
              			$('#buttonCollapseSearchmask').hide();
            			$('#buttonCollapseSearchmask2').hide();
        			});
              		$('#searchmask').on('shown.bs.collapse', function (event) {
              			event.target.scrollIntoView();
              		});
            	 </script>
             </div>
           </div>
		</c:if>
        <c:if test="${it.showResults}">
				<c:if test="${not empty it.result.sortfields}">
        			<search:result-sorter result="${it.result}"
                    	 fields="${it.result.sortfields}" mode="search" mask="${it.result.mask}" />
				</c:if>
           <div class="row">
              <div class="col">
			  	<search:result-browser result="${it.result}">
			  		<c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" /> 
						<search:result-entry entry="${entry}" url="${url}" />
						<div style="clear:both"></div>
			  	</search:result-browser>
              </div>
           </div>   
			  	<%--2nd redefine search button requested by CPB --%>
		   <c:if test="${not it.showMask and it.result.numFound>0}">
             <div class="row">
               <div class="col">
				  <button id="buttonCollapseSearchmask2" class="btn btn-secondary float-right mt-3" type="button"
					      data-toggle="collapse" data-target="#searchmask" aria-expanded="false" aria-controls="searchmask">
					  <fmt:message key="Webpage.Searchresult.redefine" />
				  </button>
               </div>
             </div>
		   </c:if>
		</c:if>
		
		<script>
		$.urlParam = function(name){
		    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
		    if (results==null){
		       return null;
		    }
		    else{
		       return results[1] || 0;
		    }
		}
		<%-- $(function(){   = document.ready() --%>
		$(function(){
			var field = $.urlParam('searchField');
			var value = $.urlParam('searchValue');
			// test 'truthy-ness": false = {null | undefined > NAN | empty | 0 | false}
			if(field && value){
				$('input#'+field.replace('.', '\\.')).val(decodeURIComponent(value));
			}
		});
		</script>
		</div>
        <c:if test="${pageScope.navSide == 'right'}">
            <div id="search_nav" class="col-3">
                <mcr:outputNavigation mode="side" id="search" expanded="true"></mcr:outputNavigation>
            </div>
        </c:if>
		</div>
	 </div>
  <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>
