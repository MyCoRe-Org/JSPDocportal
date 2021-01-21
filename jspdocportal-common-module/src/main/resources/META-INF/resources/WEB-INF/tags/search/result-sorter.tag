<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="fields" required="true" type="java.lang.String"%>
<%@ attribute name="result" required="true" type="org.mycore.frontend.jsp.search.MCRSearchResultDataBean"%>
<%@ attribute name="mask" required="true" type="java.lang.String"%>
<!-- values are 'browse' or 'search' -->
<%@ attribute name="mode" required="true" type="java.lang.String"%>

<div class="row">
	<div class="col-sm-12 text-right" style="margin-bottom:12px">
		<script type="text/javascript">
			function changeSortURL(value) {
				window.location = $("meta[name='mcr:baseurl']").attr("content")
						+ "${mode}/${mask}?_search="
						+ $("meta[name='mcr:search.id']").attr("content")
						+ "&_sort="
						+ encodeURIComponent($("#sortField option:selected").val() + " " + value);
			}
		</script>
		<fmt:message key="Webpage.Searchresult.resort-label" />
		  <select id="sortField" class="form-control ir-form-control form-control-sm" onchange="changeSortURL('asc')" style="width: auto; display: inline; margin: 0px 12px">
			<c:forEach var="f" items="${fn:split(fields,',')}">
				<option value="${f}" ${fn:startsWith(result.sort,f.concat(' ')) ? 'selected="selected"' : ''}><fmt:message key="Browse.Sort.${f}" /></option>
			</c:forEach>
		  </select>
    
		  <button class="btn btn-sm ${fn:endsWith(result.sort,' asc') ? 'active' : 'btn-secondary'}" ${fn:endsWith(result.sort,' asc') ? 'disabled': ''} role="button" onclick="changeSortURL('asc')">
            <i class="fas fa-sort-amount-down"></i> A-Z
		  </button>
		  <button class="btn btn-sm ${fn:endsWith(result.sort,' desc') ? 'active' : 'btn-secondary'}" ${fn:endsWith(result.sort,' desc') ? 'disabled': ''} role="button" onclick="changeSortURL('desc')">
		    <i class="fas fa-sort-amount-up" onclick="changeSortURL('desc')"></i> Z-A
		  </button>
	</div>
</div>