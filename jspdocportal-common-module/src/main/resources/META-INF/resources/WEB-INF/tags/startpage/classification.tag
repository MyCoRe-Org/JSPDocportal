<%@ tag language="java" pageEncoding="UTF-8"%>
<%@tag import="org.mycore.jspdocportal.common.controller.MCRClassBrowserController"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryID"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryDAOFactory"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="startpage" tagdir="/WEB-INF/tags/startpage" %>

<%@ attribute name="categid" required="true" type="java.lang.String" %>
<%@ attribute name="mask" required="true" type="java.lang.String" %>
<%@ attribute name="facetField" required="false" type="java.lang.String" %>
<%@ attribute name="lang" required="false" type="java.lang.String" %>
<%@ attribute name="flatten" required="false" type="java.lang.String" %>

<% MCRCategory rootCateg = MCRCategoryDAOFactory.getInstance()
     .getCategory(MCRCategoryID.fromString(categid), -1);
   if(Boolean.valueOf((String)jspContext.getAttribute("flatten"))){
     MCRClassBrowserController.flattenChildren(rootCateg);
   }
   jspContext.setAttribute("rootCateg", rootCateg);
%>

  <div class="mcr-facet card ir-browse-classification-card h-100" data-mcr-facet-field="${facetField}">
    <div class="card-header p-2">
      <h4 class="mb-0">
        <startpage:classification-label category="${rootCateg}" lang="${lang}" />
      </h4>
    </div>
    <template x-if="solrFacetCounts">
      <div x-data="{ counts : solrFacetCounts.facet_fields['${facetField}'] }">
        <startpage:classification-inner category="${rootCateg}" facetField="${facetField}" mask="${mask}" lang="${lang}"/>
      </div>
    </template>
  </div>
