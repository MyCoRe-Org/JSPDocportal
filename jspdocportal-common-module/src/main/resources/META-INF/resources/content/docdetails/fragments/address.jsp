<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://www.mycore.org/jspdocportal/base.tld" prefix="mcr"%>
<!-- $data as xml element representing mcrlink object --->

<c:set var="street"><x:out select="$data/street/text()" />&#160;<x:out select="$data/number/text()" /></c:set>
<c:set var="city"><x:out select="$data/zipcode/text()" />&#160;<x:out select="$data/city/text()" /></c:set>
<c:set var="country"><x:out select="$data/state/text()" />&#160;<x:out select="$data/country/text()" /></c:set>

<c:if test="${fn:length(street)>8}"><c:out value="${street}" escapeXml="false" /><br /></c:if>
<c:if test="${fn:length(city)>8}"><c:out value="${city}" escapeXml="false" /><br /></c:if>
<c:if test="${fn:length(country)>8}"><c:out value="${country}" escapeXml="false" /></c:if>
					    