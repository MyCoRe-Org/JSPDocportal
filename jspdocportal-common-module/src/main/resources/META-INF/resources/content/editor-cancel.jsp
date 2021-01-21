<%@ page import="org.mycore.frontend.jsp.NavServlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<% 
   response.sendRedirect(response.encodeRedirectURL(NavServlet.getNavigationBaseURL() + "nav?path=~mycore-message&messageKey=CancelEditor"));
%>