<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<% 
    String messageKey = (String) request.getAttribute("messageKey");
    if (messageKey == null) {
        messageKey = request.getParameter("messageKey");
    }
    if (messageKey == null) {
        messageKey = new StringBuffer("MessageKey.")
            .append((String)request.getAttribute("path")).toString();
    }
    pageContext.setAttribute("messageKey",messageKey);
%>
<h2><fmt:message key="MessageHeadline" /></h2>
<font color="#ff0000"><fmt:message key="${pageScope.messageKey}" /></font>