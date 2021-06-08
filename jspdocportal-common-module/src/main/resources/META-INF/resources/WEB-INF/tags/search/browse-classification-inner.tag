<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="category" required="true" type="org.mycore.datamodel.classifications2.MCRCategory" %>
<%@ attribute name="facetField" required="false" type="java.lang.String" %>
<%@ attribute name="mask" required="false" type="java.lang.String" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>

<c:if test="${category.hasChildren()}">
  <ul class="list-group list-group-flush">
    <c:forEach var="c" items="${category.children}">
      <li class="list-group-item ir-facets-btn btn-sm px-2 py-1 d-block"
          onclick="changeFacetIncludeURL('${facetField}','${c.id.getRootID()}:${c.id.ID}', '${mask}', '${result.id}');">
        <search:browse-classification-label category="${c}" lang="${lang}" />
        <span class="ir-facets-btn-count mcr-facet-count" data-mcr-facet-field="${facetField}" data-mcr-facet-value="${c.id.getRootID()}:${c.id.ID}"></span>
      </li>
      <c:if test="${c.hasChildren()}">
        <li class="list-group-item ir-facets-sublist py-0">
          <search:browse-classification-inner category="${c}" facetField="${facetField}" mask="${mask}" lang="${lang}"/>
        </li>
      </c:if>
    </c:forEach>
  </ul>
</c:if>
