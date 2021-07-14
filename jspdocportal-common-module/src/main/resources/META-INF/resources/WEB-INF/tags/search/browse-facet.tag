<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="result" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean"%>
<%@ attribute name="top" required="false" type="java.lang.Integer" %>
<%@ attribute name="facetField" required="true" type="java.lang.String" %>
<%@ attribute name="mask" required="true" type="java.lang.String" %>
<%@ attribute name="sort" required="false" type="java.lang.String" %>

<fmt:message var="btnMore" key="Browse.Filter.Buttons.more" />
<fmt:message var="btnLess" key="Browse.Filter.Buttons.less" />
		
<c:set var="facets" value="${result.facetResult}" />
<c:if test="${facets.get(facetField).size() gt 0}">
	<h5><fmt:message key="Browse.Filter.${mask}.${facetField}" /></h5>
	<c:forEach var="countsKey" items="${facets.get(facetField).keySet()}" varStatus="status">
		<c:set var="key">+${facetField}:${countsKey}</c:set>
		<c:set var="facetID" value="${fn:replace(facetField, '.', '_')}" />
		<c:if test="${status.index == top}">
			<div id="moreFacets_div_${facetID}" class="collapse">
		</c:if>

		<button class="btn btn-sm btn-default ir-facets-btn" style="border:none; display:block;text-align:left;white-space:normal;width:100%" 
		        onclick="changeFacetIncludeURL('${facetField}','${countsKey}', '${mask}', '${result.id}');">
			<span class="ir-facets-btn-label">
				${actionBean.calcFacetOutputString(facetField, countsKey)}
			</span>
			<span class="ir-facets-btn-count">
				<span class="badge ir-badge">${facets.get(facetField).get(countsKey)}</span>
			</span>
		</button>

		<c:if test="${status.index >= top and status.last}">
			</div>
			<button id="moreFacets_btn_${facetID}" class="btn btn-default btn-xs ir-form-control pull-right" 
                    data-toggle="collapse" data-target="#moreFacets_div_${facetID}"  >${btnMore}</button>
			<script type="text/javascript">
				$('#moreFacets_div_${facetID}').on('shown.bs.collapse', function () {
					$('#moreFacets_btn_${facetID}').text('${btnMore}');
				});
				$('#moreFacets_div_${facetID}').on('hidden.bs.collapse', function () {
					$('#moreFacets_btn_${facetID}').text('${btnLess}')
				});
			</script>
		</c:if>
	</c:forEach>
</c:if>
