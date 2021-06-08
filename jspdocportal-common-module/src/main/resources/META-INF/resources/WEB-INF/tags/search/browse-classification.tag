<%@tag import="org.mycore.jspdocportal.common.controller.MCRClassBrowserController"%>
<%@tag import="org.mycore.datamodel.classifications2.impl.MCRCategoryImpl"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryID"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryDAOFactory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="categid" required="true" type="java.lang.String" %>
<%@ attribute name="mask" required="true" type="java.lang.String" %>
<%@ attribute name="facetField" required="false" type="java.lang.String" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>
<%@ attribute name="flatten" required="false" type="java.lang.String" %>

<% MCRCategory rootCateg = MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.fromString(categid), -1);
   jspContext.setAttribute("rootCateg", rootCateg);
   
   if(Boolean.valueOf((String)jspContext.getAttribute("flatten"))){
     jspContext.setAttribute("categChildren", MCRClassBrowserController.flattenClassification(rootCateg));
   }
   else{
       jspContext.setAttribute("categChildren", null);
   }
%>
  <div class="mcr-facet card ir-browse-classification-card h-100" data-mcr-facet-field="${facetField}">
    <div class="card-header p-2">
      <h4 class="mb-0">
        <search:browse-classification-label category="${rootCateg}" lang="${lang}" />
      </h4>
    </div>
    <c:choose>
      <c:when test="${not empty categChildren}">
        <ul class="list-group list-group-flush">
          <c:forEach var="c" items="${categChildren}">
            <li class="list-group-item ir-facets-btn btn-sm px-2 py-1" style="display:block" 
                onclick="changeFacetIncludeURL('${facetField}','${c.id.getRootID()}:${c.id.ID}', '${mask}', '${result.id}');">
              <search:browse-classification-label category="${c}" lang="${lang}" />
              <span class="ir-facets-btn-count mcr-facet-count" data-mcr-facet-field="${facetField}" data-mcr-facet-value="${c.id.getRootID()}:${c.id.ID}"></span>
            </li>
          </c:forEach>
        </ul>
      </c:when>
      <c:otherwise>
        <search:browse-classification-inner category="${rootCateg}" facetField="${facetField}" mask="${mask}" lang="${lang}"/>
      </c:otherwise>
    </c:choose>
  </div>
  