
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

<% MCRCategory rootCateg = MCRCategoryDAOFactory.getInstance().getCategory(MCRCategoryID.fromString(categid), -1); 
	jspContext.setAttribute("rootCateg", rootCateg);
%>
   <div class="mcr-facet card ir-browse-classification-card h-100" data-mcr-facet-field="${facetField}">
     <div class="card-header p-2">
       <h4 class="mb-0">
      <c:if test="${not empty lang}">
        <c:choose>
          <c:when test="${not rootCateg.getLabel('x-'.concat(lang).concat('-short')).isEmpty() }">
            ${rootCateg.getLabel('x-'.concat(lang).concat('-short')).get().text}
          </c:when> 
          <c:otherwise>
            ${rootCateg.getLabel(lang).get().text}
          </c:otherwise>
        </c:choose>
      </c:if>
      <c:if test="${empty lang}">
        ${rootCateg.currentLabel.get().text}
      </c:if>
      </h4>
     </div>
      
    <search:browse-classification-inner category="${rootCateg}" facetField="${facetField}" mask="${mask}" lang="${lang}"/>
  </div>