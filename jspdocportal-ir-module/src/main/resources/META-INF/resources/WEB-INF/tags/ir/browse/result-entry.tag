<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<%@ attribute name="url" required="true" type="java.lang.String"%>
<%@ attribute name="entry" required="true" type="org.mycore.jspdocportal.common.search.MCRSearchResultEntry"%>

<div class="row">
  <div class="col-sm-9">
    <p class="card-text">${entry.data['ir.creator.result']}</p>
    <c:choose>
      <c:when test="${empty(entry.data['ir.partTitle.result'])}">
        <h4 class="card-title"><a href="${url}">${entry.label}</a></h4>
      </c:when>
      <c:otherwise>
        <h5 class="card-title"><a href="${url}">${entry.label}</a></h5>
        <h4 class="card-title"><a href="${url}">${entry.data['ir.partTitle.result']}</a></h4>
      </c:otherwise>
    </c:choose>
    <c:if test="${not empty(entry.data['ir.host.title.result'])}">
      <p class="card-text"><span class="display-label">in:</span> ${entry.data['ir.host.title.result']}
        <c:if test="${not empty(entry.data['ir.host.part.result'])}">
          ${entry.data['ir.host.part.result']}
        </c:if>
      </p>
    </c:if>
    <p class="card-text">${entry.data['ir.originInfo.result']}</p>
    <p class="card-text">${entry.data['purl']}</p>
    <!-- temporary during migration: -->
    <c:if test="${empty entry.data['purl']}">
      <p class="card-text">${WebApplicationBaseURL}resolve/id/${entry.mcrid}</p>
    </c:if>
    <p class="card-text" style="font-size: 80%; text-align:justify">${entry.data['ir.abstract300.result']}</p>
    
    <p class="card-text">
      <span class="badge ir-badge badge-secondary">${entry.data['ir.doctype.result']}</span>
      <c:choose>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'restrictedaccess')}">
          <span class="badge ir-badge ir-badge-restrictedaccess">
            Beschränkter <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/>  Zugang            
          </span>
        </c:when>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'closedaccess')}">
          <span class="badge ir-badge ir-badge-closeddaccess">
            Kein <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/>  Zugang            
          </span>
        </c:when>
        <c:otherwise>
          <span class="badge ir-badge ir-badge-openaccess">
            Freier <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Open_Access.png"/>  Zugang            
          </span>
        </c:otherwise>
      </c:choose>
      
      <c:if test="${fn:contains(entry.data['ir.contains_msg.facet'], 'ocr')}">
        <span class="badge ir-badge ir-badge-ocr">
          OCR-Volltext            
        </span>
      </c:if>
      
      <mcr:hasAccess var="hasAccess" permission="edit" />
      <c:if test="${hasAccess}">
        <c:forEach var="s" items="${entry.data['ir.state_class.facet']}">
          <c:if test="${not fn:contains(s, 'state:published')}">
            <span class="badge ir-badge bg-warning">${fn:replace(fn:replace(fn:replace(s, ']',''), '[',''),'state:','')}</span>
          </c:if>
        </c:forEach>
      </c:if>
    </p>
  </div>
  <div class="col-md-3 d-none d-md-block">
    <div class="img-thumbnail pull-right ir-result-image">
      <div style="position:relative;text-align:center">
        <a href="${url}">
          <c:choose>
            <c:when test="${entry.data['category'].contains('derivate_types:cover') or entry.data['category'].contains('derivate_types:fulltext')}">
              <img style="width:98%;padding:1%;" src="${pageContext.request.contextPath}/api/iiif/image/v2/thumbnail/${entry.mcrid}/full/full/0/default.jpg" border="0" />
            </c:when>
            <c:when test="${fn:contains(entry.mcrid, '_bundle_')}">
              <img style="width:150px" src="${WebApplicationBaseURL}images/filetypeicons/bundle.png" />
            </c:when>
            <c:when test="${entry.data['category'].contains('doctype:data')}">
              <img style="width:150px" src="${WebApplicationBaseURL}images/filetypeicons/data.png" />
            </c:when>
            <c:otherwise>
              <img style="width:150px" src="${WebApplicationBaseURL}images/filetypeicons/document.png" />
            </c:otherwise>
          </c:choose>
        </a>
      </div>
    </div>
  </div>
</div>


