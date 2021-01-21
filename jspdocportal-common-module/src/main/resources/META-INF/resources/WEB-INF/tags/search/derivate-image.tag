<%@tag pageEncoding="UTF-8"%> 
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="x"   uri="http://java.sun.com/jsp/jstl/xml" %>

<%@ attribute name="mcrobj" required="true" type="org.w3c.dom.Document" %>
<%@ attribute name="category" type="java.lang.String" %>
<%@ attribute name="width" type="java.lang.String" %>
<%@ attribute name="showFooter" type="java.lang.Boolean"  %>
<%@ attribute name="protectDownload" type="java.lang.Boolean"  %>
<%-- to change the image used for copyright and download protection, override: /images/image_terms_of_use.png --%>

<c:if test="${empty width}">
	<c:set var="width" value="100%" />
</c:if>

<x:set var="mcrid" select="string($mcrobj/mycoreobject/@ID)" />
<x:forEach select="$mcrobj/mycoreobject/structure/derobjects/derobject">
	<x:if select="./classification[@categid = $category]">
		<%--instead of string(./@xlink:href) to avoid  XPathStylesheetDOM3Exception: Prefix must resolve to a namespace: xlink --%>
		<x:set var="derid" select="string(./@*[local-name()='href'])"/>	
		<div class="docdetails-image" style="width:${width}">
			<div style="position:relative">
   				<c:if test="${protectDownload}">
   					<img style="opacity:0.001;position:absolute;top:0px;left:0px;width:100%;height:100%;z-index:1" src="${pageContext.request.contextPath}/images/image_terms_of_use.png"/>
	   			</c:if>
	   			<x:set var="maindoc" select="string(./maindoc)" />
   				<img style="position:relative;top:0px;left:0px;width:98%;padding:1%;display:block;" src="${pageContext.request.contextPath}/file/${mcrid}/${derid}/${maindoc}" border="0" width="${width}" alt="${maindoc}" />
			</div>
			<c:if test="${showFooter}">
  				<div class="docdetails-image-footer" style="padding:1%">
    				<x:out select="./title[1]" />
				</div>
			</c:if>
		</div>		
	</x:if>
</x:forEach>
