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
		onkeypress="if (event.keyCode == 13) { changeFilterIncludeURL($('input[name=\'filterField\']:checked').val(), $('#filterValue').val(), $('#filterValue').parent().data('ir-mode'));}" />
	<div class="input-group-append">
		<button id="filterInclude" class="btn btn-primary" type="button"
			onclick="changeFilterIncludeURL($('input[name=\'filterField\']:checked').val(), $('#filterValue').val(), $('#filterValue').parent().data('ir-mode'));">
			<i class="fa fa-search"></i>
		</button>
	</div>
  </div>

  <div class="custom-control custom-control-inline float-right mr-0">
    <a class="btn btn-outline-secondary btn-sm" href="${WebApplicationBaseURL}browse/${mode}">
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
	  window.location=$("meta[name='mcr:baseurl']").attr("content")
		+ "browse/"+mask+"?"
    	+ "&_add-filter="
    	+ encodeURIComponent("+" + key+":"+value);
    }
  </script>
