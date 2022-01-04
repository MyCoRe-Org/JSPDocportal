<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="url" required="true" type="java.lang.String"%>
<%@ attribute name="entry" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultEntry"%>
<%@ attribute name="mode" required="false" type="java.lang.String"%>

<div class="row">
  <div class="col-md-9" style="position:relative">
    <h4 class="card-title"><a href="${url}">${entry.label}</a></h4>
    <table>
      <c:forEach items="${entry.data}" var="field">
        <tr>
          <th><fmt:message key="Webpage.Searchresult.${entry.objectType}.Label.${field.key}" /></th>
          <td><c:out value="${fn:replace(field.value, '|', '<br />')}" escapeXml="false" /></td>
        </tr>
      </c:forEach>
    </table>
  </div>
  <div class="col-md-3 d-none d-md-block">
    <c:if test="${not empty entry.internal['ir.cover_url']}">
      <div class="img-thumbnail ir-result-image">
          <a href="${url}" id="thumbnail_${entry.mcrid}" style="display:inline-block;min-height:2em">
            <img style="width:128px" src="${WebApplicationBaseURL}${entry.internal['ir.cover_url']}" />
          </a>
      </div>
    </c:if>
  </div>
</div>
