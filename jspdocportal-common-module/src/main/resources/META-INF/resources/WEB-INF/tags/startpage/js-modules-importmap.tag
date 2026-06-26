<%@tag language="java" body-content="empty" pageEncoding="UTF-8"%>
<%@tag import="java.util.TreeMap"%>
<%@tag import="java.util.Map"%>
<%@tag import="org.mycore.common.config.MCRConfiguration2"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="id" required="false" type="java.lang.String" %>
<%
   final String CONFIG_PREFIX = "MCR.JSPDocportal.JS.ImportMap.";
   Map<String, String> importMap = new TreeMap<>();
   String prefix = CONFIG_PREFIX + id + ".";
   MCRConfiguration2.getPropertiesMap()
            .entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .forEach(e -> {
                String key = e.getKey().substring(prefix.length());
                importMap.put(key, e.getValue());
            });
    jspContext.setAttribute("_importMap", importMap);
%>
    <script type="importmap">
      { "imports": {
           <c:forEach var="entry" items="${_importMap}" varStatus="status">"${entry.key}": "${entry.value}"<c:if test="${!status.last}">,
           </c:if></c:forEach>
        }
      }
    </script>
