<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="mode" required="true" type="java.lang.String"%>


  <fmt:message var="lblAllMeta" key="Browse.Filter.${mode}.allMeta" />
  <fmt:message var="lblContent" key="Browse.Filter.${mode}.content"  />
  <fmt:message var="lblSearch" key="Browse.Search.placeholder"  />

  <div class="input-group mb-3" data-ir-mode="${mode}">
	<input type="text" class="form-control ir-form-control"
		id="filterValue" name="filterValue" placeholder="${lblSearch} "
		onkeypress="if (event.key === 'Enter') { 
                  changeFilterIncludeURL(
                    document.querySelector('input[name=\'filterField\']:checked')?.value,
                    document.getElementById('filterValue')?.value,
                    document.getElementById('filterValue')?.parentElement?.dataset?.irMode);
                }" />
	<div class="input-group-append">
		<button id="filterInclude" class="btn btn-primary" type="button"
			      onclick="changeFilterIncludeURL(
              document.querySelector('input[name=\'filterField\']:checked')?.value,
              document.getElementById('filterValue')?.value,
              document.getElementById('filterValue')?.parentElement?.dataset?.irMode);"
			<i class="fa fa-search"></i>
		</button>
	</div>
  </div>

  <div class="custom-control custom-control-inline float-end me-0">
    <a class="btn btn-outline-secondary btn-sm" href="${WebApplicationBaseURL}do/browse/${mode}">
      <fmt:message key="Browse.Search.alldocuments" />
    </a>
  </div>
 
  <div class="custom-control custom-radio custom-control-inline">
	<input type="radio" checked="checked" id="filterField1"
		name="filterField" value="allMeta" class="custom-control-input">
	<label class="custom-control-label" for="filterField1">
	<c:out escapeXml="false" value="${fn:replace(lblAllMeta,'<br />', ' ')}" /></label>
  </div>

  <div class="custom-control custom-radio custom-control-inline">
	<input type="radio" id="filterField2" name="filterField"
		value="content" class="custom-control-input"> <label
		class="custom-control-label" for="filterField2">
		<c:out escapeXml="false" value="${fn:replace(lblContent,'<br />', ' ')}" /></label>
  </div>
  <script>
  function changeFilterIncludeURL(key, value, mask) {
    const baseUrl = document.querySelector("meta[name='mcr:baseurl']")?.content;
    if (baseUrl) {
      const url = new URL("do/browse/" + mask), baseUrl);
      url.searchParams.set("_add-filter", "+" + key + ":" + value);
      window.location.href = url.toString();
    }
}
  </script>
