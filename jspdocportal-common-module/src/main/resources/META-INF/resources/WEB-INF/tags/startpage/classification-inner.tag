<%@ tag language="java" pageEncoding="UTF-8"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="startpage" tagdir="/WEB-INF/tags/startpage"%>

<%@ attribute name="category" required="true" type="org.mycore.datamodel.classifications2.MCRCategory" %>
<%@ attribute name="facetField" required="false" type="java.lang.String" %>
<%@ attribute name="mask" required="false" type="java.lang.String" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>

<c:if test="${category.hasChildren()}">
  <ul class="list-group list-group-flush">
    <c:forEach var="c" items="${category.children}">
      <li class="list-group-item ir-facets-btn btn-sm px-2 py-1"
          data-mcr-facet-value="${c.id.rootID}:${c.id.id}"
          x-data="{countPos : counts.indexOf('${c.id.rootID}:${c.id.id}')}" x-show="countPos >= 0"
          x-on:click="window.location=baseurl + 'do/browse/' + mask
              + '?_add-filter=' + encodeURIComponent('+${facetField}:${c.id.rootID}:${c.id.id}')"
          x-effect="updateHiddenStateForParents($el)">
        <startpage:classification-label category="${c}" lang="${lang}" />
        <span class="ir-facets-btn-count mcr-facet-count"
              data-mcr-facet-field="${facetField}" data-mcr-facet-value="${c.id.rootID}:${c.id.id}"
              x-show="countPos >= 0 && counts[countPos + 1] > 0"
              x-text="countPos >= 0 ? counts[countPos + 1] : '-'"></span>
      </li>
      <c:if test="${c.hasChildren()}">
        <li class="list-group-item ir-facets-sublist py-0 pe-0"
            x-data="{countPos : counts.indexOf('${c.id.rootID}:${c.id.id}')}"
            x-bind:data-mcr-facet-count="countPos >= 0 ? counts[countPos + 1] : 0">
          <startpage:classification-inner category="${c}" facetField="${facetField}" mask="${mask}" lang="${lang}"/>
        </li>
      </c:if>
    </c:forEach>
  </ul>
</c:if>
