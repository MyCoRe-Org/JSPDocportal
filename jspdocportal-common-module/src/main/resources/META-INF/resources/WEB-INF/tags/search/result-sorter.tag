<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="fields" required="true" type="java.lang.String"%>
<%@ attribute name="result" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultDataBean"%>
<%@ attribute name="mask" required="true" type="java.lang.String"%>
<!-- values are 'browse' or 'search' -->
<%@ attribute name="mode" required="true" type="java.lang.String"%>

<div class="row">
  <div class="col mt-3 mb-2">
    <div class="float-end">
      <script type="text/javascript">
        function changeSortURL(value) {
          const baseUrl = document.querySelector("meta[name='mcr:baseurl']")?.content || "";
          const searchId = document.querySelector("meta[name='mcr:search.id']")?.content || "";
          const sortField = document.getElementById("sortField");
          const selectedValue = sortField?.options[sortField.selectedIndex]?.value || "";

          const url = new URL("do/${mode}/${mask}", baseUrl);
          url.searchParams.set("_search", searchId);
          url.searchParams.set("_sort", selectedValue + " " + value);
          window.location.href = url.toString();
        }
      </script>

		<span class="pe-2"><fmt:message key="Webpage.Searchresult.resort-label" /></span>
        <br class="d-sm-none" />
		  <select id="sortField" class="form-control ir-form-control form-control-sm me-2 d-inline w-auto" onchange="changeSortURL('asc')">
			<c:forEach var="f" items="${fn:split(fields,',')}">
				<option value="${f}" ${fn:startsWith(result.sort,f.concat(' ')) ? 'selected="selected"' : ''}><fmt:message key="Webpage.Searchresult.Sort.Label.${f}" /></option>
			</c:forEach>
		  </select>
    
		  <button class="btn btn-sm ${fn:endsWith(result.sort,' asc') ?  'btn-secondary' : 'btn-link active'}" ${fn:endsWith(result.sort,' asc') ? 'disabled': ''} title="<fmt:message key="Webpage.Searchresult.order.asc" />" role="button" onclick="changeSortURL('asc')">
            <i class="fas fa-sort-amount-down"></i> A-Z
		  </button>
		  <button class="btn btn-sm ${fn:endsWith(result.sort,' desc') ? 'btn-secondary' : 'btn-link active'}" ${fn:endsWith(result.sort,' desc') ? 'disabled': ''} title="<fmt:message key="Webpage.Searchresult.order.desc" />" role="button" onclick="changeSortURL('desc')">
		    <i class="fas fa-sort-amount-up" onclick="changeSortURL('desc')"></i> Z-A
		  </button>
      </div>
	</div>
</div>