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
    <c:if test="${fn:endsWith(mode,'series')}">
      <c:if test="${not empty entry.data['ir.seriesNumber.result']}">
        <div style="position:absolute;top:-5px;left:0px" class="border border-dark px-2" title="${mode}">
          <strong>${entry.data['ir.seriesNumber.result']}</strong>
        </div>
        <p class="card-text">&nbsp;</p>
      </c:if>
    </c:if>
    <p class="card-text">${entry.data['ir.creator.result']}</p>
    <c:choose>
      <c:when test="${empty(entry.data['ir.partNumber.result']) and empty(entry.data['ir.partName.result'])}">
        <h4 class="card-title"><a href="${url}">${entry.label}</a></h4>
      </c:when>
      <c:otherwise>
        <c:if test="${not (fn:endsWith(mode,'journal') or fn:endsWith(mode,'multipart'))}">
          <h5 class="card-title"><a href="${url}">${entry.label}</a></h5>
        </c:if>
        <h4 class="card-title"><a href="${url}">
          <c:if test="${not empty entry.data['ir.partNumber.result']}">${entry.data['ir.partNumber.result']}</c:if>
          <c:if test="${(not empty entry.data['ir.partNumber.result']) and not empty entry.data['ir.partName.result']}"><br /></c:if>
          <c:if test="${not empty entry.data['ir.partName.result']}">${entry.data['ir.partName.result']}</c:if>
        </a></h4>
      </c:otherwise>
    </c:choose>
    <c:if test="${not empty(entry.data['ir.host.title.result'])}">
      <p class="card-text"><span class="display-label"><fmt:message key="OMD.ir.docdetails.header.label.appears_in" /></span> ${entry.data['ir.host.title.result']}
        <c:if test="${not empty(entry.data['ir.host.part.result'])}">
          ${entry.data['ir.host.part.result']}
        </c:if>
      </p>
    </c:if>
    <p class="card-text">${entry.data['ir.originInfo.result']}</p>
    <c:choose>
      <c:when test="${entry.data['category'].contains('doctype:histbest')}">
        <c:if test="${not empty(entry.data['purl'])}">
          <p class="card-text">${fn:replace(entry.data['purl'], 'http://purl.uni-rostock.de','https://purl.uni-rostock.de')}</p>
        </c:if>
      </c:when>
      <c:otherwise>
        <c:if test="${not empty(entry.data['doi'])}">
          <p class="card-text">https://doi.org/${entry.data['doi']}</p>
        </c:if>
      </c:otherwise>  
    </c:choose>
    <p class="card-text" style="font-size: 80%; text-align:justify">${entry.data['ir.abstract300.result']}</p>
    
    <p class="card-text">
      <mcr:session var="lang" info="language" />
      <span class="badge ir-badge badge-secondary">
        <c:choose>
          <c:when test="${lang eq 'en'}">${entry.data['ir.doctype_en.result']}</c:when>
          <c:otherwise>${entry.data['ir.doctype.result']}</c:otherwise>
        </c:choose>
      </span>
      <c:choose>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'restrictedaccess')}">
          <span class="badge ir-badge ir-badge-restrictedaccess">
            <fmt:message key="OMD.ir.docdetails.header.access.restricted" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />            
          </span>
        </c:when>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'closedaccess')}">
          <span class="badge ir-badge ir-badge-closedaccess">
            <fmt:message key="OMD.ir.docdetails.header.access.closed" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Closed_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />
          </span>
        </c:when>
        <c:when test="${fn:contains(entry.data['ir.accesscondition_class.facet'], 'openaccess')}">
          <span class="badge ir-badge ir-badge-openaccess">
            <fmt:message key="OMD.ir.docdetails.header.access.open" /> <img style="height:1em;padding:0 .25em" src="${WebApplicationBaseURL}images/logo_Open_Access.png"/> <fmt:message key="OMD.ir.docdetails.header.access" />
          </span>
        </c:when>
      </c:choose>
      
      <c:if test="${fn:contains(entry.data['ir.contains_msg.facet'], 'ocr')}">
        <span class="badge ir-badge ir-badge-ocr">
          <fmt:message key="OMD.ir.docdetails.header.label.ocr" />
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
    <div class="img-thumbnail float-end ir-result-image">
      <div style="position:relative;text-align:center">
          <c:choose>
            <c:when test="${entry.data['category'].contains('derivate_types:cover') or entry.data['category'].contains('derivate_types:fulltext')}">
              <a href="${url}" id="thumbnail_${entry.mcrid}" style="display:inline-block;min-height:2em"></a>
              <script>
     	        let image_${entry.mcrid} = new Image();
     	        image_${entry.mcrid}.onload = function() {
       	          image_${entry.mcrid}.style.width = "128px";
                  image_${entry.mcrid}.classList.add("border");
                  image_${entry.mcrid}.classList.add("border-secondary");
                  document.getElementById("thumbnail_${entry.mcrid}").appendChild(image_${entry.mcrid});
                }
                image_${entry.mcrid}.onerror = function() {
                  // image did not load - show default image
                  let err_${entry.mcrid} = new Image();
                  err_${entry.mcrid}.style.width = "128px";
                  err_${entry.mcrid}.src = "${WebApplicationBaseURL}images/filetypeicons/empty.png";
                  document.getElementById("thumbnail_${entry.mcrid}").appendChild(err_${entry.mcrid});
                }
                image_${entry.mcrid}.src = "${WebApplicationBaseURL}api/iiif/image/v2/thumbnail/${entry.mcrid}/full/!512,512/0/default.jpg";
              </script>
          </c:when>
          <c:when test="${fn:contains(entry.mcrid, '_bundle_')}">
            <a href="${url}">
              <img style="width:128px" src="${WebApplicationBaseURL}images/filetypeicons/bundle.png" />
            </a>
          </c:when>
          <c:when test="${entry.data['category'].contains('doctype:data')}">
            <a href="${url}">
              <img style="width:128px" src="${WebApplicationBaseURL}images/filetypeicons/data.png" />
            </a>
          </c:when>
          <c:otherwise>
            <a href="${url}">
              <img style="width:128px" src="${WebApplicationBaseURL}images/filetypeicons/document.png" />
            </a>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>


