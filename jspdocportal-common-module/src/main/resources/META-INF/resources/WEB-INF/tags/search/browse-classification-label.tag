<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="category" required="true" type="org.mycore.datamodel.classifications2.MCRCategory" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>

    <span class="ir-facets-btn-label">
      <c:choose>
        <c:when test="${empty lang}">
          ${category.currentLabel.get().text}
        </c:when>
        <c:when test="${category.getLabel('x-'.concat(lang).concat('-short')).isPresent() }">
          ${category.getLabel('x-'.concat(lang).concat('-short')).get().text}
        </c:when>
        <c:when test="${category.getLabel(lang).isPresent() }">
          ${category.getLabel(lang).get().text}
        </c:when>
        <c:otherwise>
          ???${category.getId()}@${lang}???
        </c:otherwise>
      </c:choose>
	</span>
