
<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="category" required="true" type="org.mycore.datamodel.classifications2.MCRCategory" %>
<%@ attribute name="facetField" required="false" type="java.lang.String" %>
<%@ attribute name="mask" required="false" type="java.lang.String" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>

<c:if test="${category.hasChildren()}">
	<ul class="list-group list-group-flush">
		<c:forEach var="c" items="${category.children}">
			<%--
			<li>${c.currentLabel.get().text}
				<c:if test="${not empty facetField}">
					<span class="mcr-facet-count" data-mcr-facet-field="${facetField}" data-mcr-facet-value="${c.id.getRootID()}:${c.id.ID}"></span>
				</c:if>
				<search:browse-classification-inner category="${c}" facetField="${facetField}" mask="${mask}" />
			</li> --%>
			<li class="list-group-item ir-facets-btn" style="display:block" 
					    onclick="changeFacetIncludeURL('${facetField}','${c.id.getRootID()}:${c.id.ID}', '${mask}', '${result.id}');">
					<span class="ir-facets-btn-label">
                      <c:if test="${not empty lang}">
							${c.getLabel(lang).get().text}
                      </c:if>
                      <c:if test="${empty lang}">
                          ${c.currentLabel.get().text}
                      </c:if>
					</span>
    				<span class="ir-facets-btn-count mcr-facet-count" data-mcr-facet-field="${facetField}" data-mcr-facet-value="${c.id.getRootID()}:${c.id.ID}"></span>
	                
                    
			</li>
            <c:if test="${c.hasChildren()}">
              <li class="list-group-item py-0">
                <search:browse-classification-inner category="${c}" facetField="${facetField}" mask="${mask}" />
              </li>
            </c:if>
		</c:forEach>
	</ul>
</c:if>
