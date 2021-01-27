<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="result" required="true" type="org.mycore.frontend.jsp.search.MCRSearchResultDataBean"%>
<%@ attribute name="mask" required="true" type="java.lang.String"%>
<%@ attribute name="top" required="false" type="java.lang.Integer" %>


<c:set var="top" value="${(empty top) ? 1000 : top}" />
<script type="text/javascript">
	function changeFacetIncludeURL(key, value) {
		window.location=$("meta[name='mcr:baseurl']").attr("content")
				 	       + "browse/${mask}?_search="
				           + $("meta[name='mcr:search.id']").attr("content")
					       + "&_add-filter="
				       + encodeURIComponent("+" + key +":"+ value);
		}
		function changeFacetExcludeURL(key,value) {
			window.location=$("meta[name='mcr:baseurl']").attr("content")
					       + "browse/${mask}?_search="
				           + $("meta[name='mcr:search.id']").attr("content")
					       + "&_add-filter="
					       + encodeURIComponent("-" + key +":"+ value);
		}
</script>
		
<c:set var="facets" value="${result.facetResult}" />
<c:forEach var="facetKey" items="${facets.keySet()}">
	<c:if test="${facets.get(facetKey).size() gt 0}">
        <c:set var="facetID" value="${fn:replace(facetKey, '.', '_')}" />
        <c:set var="toggleClass">toggle-${facetID}-top</c:set>
		<div class="card ir-facets-card">
				<div class="card-header"><h4><fmt:message key="Browse.Filter.${mask}.${facetKey}" /></h4></div>
                <div class="card-body">
                <div class="btn-group-vertical w-100">
                <c:forEach var="countsKey" items="${facets.get(facetKey).keySet()}" varStatus="status">
					<c:set var="key">+${facetKey}:${countsKey}</c:set>
					
					<c:if test="${status.index >= top}">
						<c:set var="toggleClass">collapse toggle-${facetID}-collapse</c:set>
					</c:if>
					<c:if test="${result.filterQueries.contains(key)}">
					  	<c:url var="url" value="${WebApplicationBaseURL}browse/${mask}">
							<c:param name="_search" value="${result.id}" />
							<c:param name="_remove-filter" value="${key}" />
						</c:url>
						<button class="btn btn-sm ir-facets-btn active" onclick="window.location.href='${url}'">
							<i class="fa fa-times" style="position:absolute; top:5px; right:5px; color:darkred;"></i>
							<span style="display:table-cell;vertical-align:middle;">
								${it.util.calcFacetOutputString(facetKey, countsKey)}
							</span>
							<span style="display:table-cell;vertical-align:middle;padding-left:12px">
								<span class="badge ir-facets-badge">${facets.get(facetKey).get(countsKey)}</span>
							</span>
						</button>
					</c:if>
					<c:if test="${not result.filterQueries.contains(key)}">
						<button class="btn btn-sm ir-facets-btn ${toggleClass}" 
						        onclick="changeFacetIncludeURL('${facetKey}','${countsKey}');">
							<span style="display:table-cell;vertical-align:middle;">
								${it.util.calcFacetOutputString(facetKey, countsKey)}
							</span>
							<span style="display:table-cell;vertical-align:middle;padding-left:12px;">
								<span class="badge ir-facets-badge">${facets.get(facetKey).get(countsKey)}</span>
							</span>
						</button>
					</c:if>
					<c:if test="${status.index >= top and status.last}">
						<button id="moreFacets_btn_${facetID}" class="btn btn-primary btn-sm" style="width:unset;align-self:end" 
                                data-toggle="collapse" data-target=".toggle-${facetID}-collapse"  >mehr ...</button>
						<script type="text/javascript">
						$('.toggle-${facetID}-collapse:first').on('shown.bs.collapse', function () {
							$('#moreFacets_btn_${facetID}').text('weniger ...');
						});
						$('.toggle-${facetID}-collapse:first').on('hidden.bs.collapse', function () {
							$('#moreFacets_btn_${facetID}').text('mehr ...')
						});
						</script>
					</c:if>
				</c:forEach>
                  </div>
                </div>
			</div>
	</c:if>
</c:forEach>