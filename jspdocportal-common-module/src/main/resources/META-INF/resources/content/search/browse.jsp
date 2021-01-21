<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<fmt:message var="pageTitle" key="Webpage.browse.title.${actionBean.result.mask}" />
<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="1column">
	<stripes:layout-component name="html_head">
	</stripes:layout-component>
	<stripes:layout-component name="main_part">
		<c:if test="${not empty actionBean.result.mask}">
			<div class="ur-box ur-text">
				<c:set var="classCollapse" value="" />
				<c:if test="${not actionBean.showMask and actionBean.result.numFound>0}">
					<button id="buttonCollapseSearchmask" class="btn btn-secondary float-right" type="button"
						    data-toggle="collapse" data-target="#searchmask" aria-expanded="false" aria-controls="searchmask">
						<fmt:message key="Webpage.Searchresult.redefine" />
					</button>
					<c:set var="classCollapse">collapse</c:set> 
				</c:if>
			
				<h2>${pageTitle}</h2>

				<div class="${classCollapse}" id="searchmask">
					<c:out value="${actionBean.xeditorHtml}" escapeXml="false" />
				</div>
				<script type="text/javascript">
              		$('#searchmask').on('show.bs.collapse', function () {
            			$('#buttonCollapseSearchmask').hide();
        			})
            	 </script>
			</div>
		</c:if>
		<c:if test="${actionBean.showResults}">
			<div class="ur-box">
			  	<search:result-browser result="${actionBean.result}">
			  		<c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
						<search:result-entry entry="${entry}" url="${url}" />
						<div style="clear:both"></div>
			  	</search:result-browser>
			</div>
		</c:if>
	</stripes:layout-component>
</stripes:layout-render>
