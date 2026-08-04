<%@ tag description="Renders the workspace object header, with fallback for new objects" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ attribute name="mcrObjectID" required="true" rtexprvalue="true" %>

<c:choose>
  <c:when test="${empty mcrObjectID}">
    <h4 class="mt-0"><fmt:message key="WF.common.newObject" /></h4>
  </c:when>
  <c:otherwise>
    <mcr:retrieveObject
      mcrid="${mcrObjectID}"
      varDOM="mcrobj"
      cache="true"
      fromWorkflow="false" />
    <mcr:transformXSL
      dom="${mcrobj}"
      xslImports="workspace-object-header" />
  </c:otherwise>
</c:choose>
