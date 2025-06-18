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
<c:set var="org.mycore.navigation.main.path" scope="request">main.search</c:set>
<c:set var="org.mycore.navigation.search.path" scope="request">search.search_${it.mask}</c:set>

<!doctype html>
<html>
<head>
  <fmt:message var="pageTitle" key="Webpage.search.title.${it.mask}" />
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
  <meta name="mcr:search.id" content="${it.result.id}" />
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div id="content_area">
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
					<button id="buttonCollapseSearchmask" class="btn btn-secondary float-end" type="button"
						    data-bs-toggle="collapse" data-bs-target="#searchmask" aria-expanded="false" aria-controls="searchmask">
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
          document.addEventListener('DOMContentLoaded', function() {
            const searchmask = document.getElementById('searchmask');
            if (searchmask) {
              searchmask.addEventListener('show.bs.collapse', function(event) {
                document.getElementById('buttonCollapseSearchmask')?.setAttribute('hidden', '');
                document.getElementById('buttonCollapseSearchmask2')?.setAttribute('hidden', '');
              });

              searchmask.addEventListener('shown.bs.collapse', function(event) {
                event.target.scrollIntoView();
              });
            }
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
				  <button id="buttonCollapseSearchmask2" class="btn btn-secondary float-end mt-3" type="button"
					      data-bs-toggle="collapse" data-bs-target="#searchmask" aria-expanded="false" aria-controls="searchmask">
					  <fmt:message key="Webpage.Searchresult.redefine" />
				  </button>
               </div>
             </div>
		   </c:if>
		</c:if>
		
    <script type="text/javascript">
      document.addEventListener('DOMContentLoaded', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const field = urlParams.get('searchField');
        const value = urlParams.get('searchValue');
      
        // Test 'truthy-ness': false = {null | undefined | NaN | empty | 0 | false}
        if (field && value) {
          // Punkte in IDs müssen im QuerySelector via CSS.escape() mit '\.' ersetzt werden
          // Input-Element mit ID suchen und Wert setzen (falls Element existiert)
          document.querySelector('input#' + CSS.escape(field))?.value = decodeURIComponent(value);
         }
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
     </div>
  <%@ include file="../fragments/footer.jspf" %>
  </body>
</html>
