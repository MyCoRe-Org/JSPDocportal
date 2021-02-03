<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="url" required="true" type="java.lang.String"%>
<%@ attribute name="entry" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultEntry"%>

<div class="row">
	<div class="col-sm-9">
		<p class="card-text">${entry.data['ir.creator.result']}</p>
		<h4 class="card-title">	<a href="${url}">${entry.label}</a></h4>
		<p class="card-text">${entry.data['ir.originInfo.result']}</p>
		<p class="card-text">${entry.data['purl']}</p>
        <c:if test="${empty entry.data['purl']}">
          <p class="card-text">${WebApplicationBaseURL}resolve/id/${entry.mcrid}</p>
        </c:if>
		<p class="card-text" style="font-size: 80%; text-align:justify">${entry.data['ir.abstract300.result']}</p>
	</div>
	<c:if test="${not empty entry.coverURL}">
		<div class="col-md-3 d-none d-md-block">
			<div class="img-thumbnail pull-right ir-result-image">
				<div style="position:relative">
   					<c:if test="${protectDownload}">
   						<img style="opacity:0.001;position:absolute;top:0px;left:0px;width:100%;height:100%;z-index:1" src="${pageContext.request.contextPath}/images/image_terms_of_use.png"/>
	   				</c:if>
                    <a href="${url}">
   					  <img style="position:relative;top:0px;left:0px;width:98%;padding:1%;display:block;" src="${pageContext.request.contextPath}/${entry.coverURL}" border="0" />
                    </a>
				</div>
			</div>
		</div>
	</c:if>
</div>