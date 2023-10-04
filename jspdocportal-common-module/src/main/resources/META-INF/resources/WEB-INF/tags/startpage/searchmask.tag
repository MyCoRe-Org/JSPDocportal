<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="mode" required="true" type="java.lang.String"%>

<fmt:message var="lblAllMeta" key="Browse.Filter.${mode}.allMeta" />
<fmt:message var="lblContent" key="Browse.Filter.${mode}.content" />
<fmt:message var="lblSearch" key="Browse.Search.placeholder" />

  <div class="input-group mb-3">
    <input type="text" class="form-control ir-form-control" 
      placeholder="${lblSearch}" x-bind:placeholder="search_placeholder"
      x-model='search_term' x-on:keyup.enter="doSearch()" />
    <div class="input-group-append">
      <button class="btn btn-primary" type="button" x-on:click="doSearch()">
        <i class="fa fa-search"></i>
      </button>
    </div>
  </div>

  <div class="custom-control custom-control-inline float-right mr-0">
    <a class="btn btn-outline-secondary btn-sm" href="${WebApplicationBaseURL}do/browse/${mode}"> 
      <fmt:message key="Browse.Search.alldocuments" />
    </a>
  </div>

  <div class="custom-control custom-radio custom-control-inline">
    <input type="radio" class="custom-control-input" id="filterField1" 
           x-model="search_field" value="allMeta"> 
    <label class="custom-control-label" for="filterField1"> 
      <c:out escapeXml="false" value="${fn:replace(lblAllMeta,'<br />', ' ')}" />
    </label>
  </div>

  <div class="custom-control custom-radio custom-control-inline">
    <input type="radio" class="custom-control-input" id="filterField2" 
           x-model="search_field" value="content">
    <label class="custom-control-label" for="filterField2"> 
      <c:out escapeXml="false" value="${fn:replace(lblContent,'<br />', ' ')}" />
    </label>
  </div>
