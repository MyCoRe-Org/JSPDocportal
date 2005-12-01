<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr" %>
<%@ page import="org.apache.log4j.Logger" %>
<c:catch var="e">
<fmt:setLocale value='${requestScope.lang}'/>
<fmt:setBundle basename='messages'/>
<c:set var="WebApplicationBaseURL" value="${requestScope.WebApplicationBaseURL}" />
<c:set var="mcrid" value="${param.id}" /> 
<c:set var="debug" value="${param.debug}" />
<mcr:receiveMcrObjAsJdom var="mycoreobject" mcrid="${mcrid}" />
<c:choose>
   <c:when test="${requestScope.host}">
      <c:set var="host" value="${requestScope.host}" />
   </c:when>
   <c:otherwise>
      <c:set var="host" value="local" />   
   </c:otherwise>
</c:choose>
<c:choose>
   <c:when test="${param.offset > 0}">
      <c:set var="offset" value="${param.offset}" />
   </c:when>
   <c:otherwise>
      <c:set var="offset" value="0" />
   </c:otherwise>
</c:choose>
<c:choose>
   <c:when test="${param.size > 0}">
      <c:set var="size" value="${param.size}" />
   </c:when>
   <c:otherwise>
     <c:set var="size" value="0" />
   </c:otherwise>
</c:choose>

<table cellspacing="0" cellpadding="0" id="metaHeading">
   <tr>
      <td class="titles">
         <mcr:simpleXpath jdom="${mycoreobject}" xpath="/mycoreobject/metadata/titles/title[@xml:lang='${requestScope.lang}']" />
      </td>
      <td class="titles">
         <mcr:browseCtrl results="${sessionScope.lastMCRResults}" offset="${offset}" >
            <c:if test="${!empty(lastHitID)}">
                <a href="${WebApplicationBaseURL}nav?path=~docdetail&id=${lastHitID}&offset=${offset -1}">&lt;&lt;</a>&#160;&#160;
            </c:if>
            <a href="${WebApplicationBaseURL}nav?path=${sessionScope.lastSearchListPath}">^</a>&#160;&#160;
            <c:if test="${!empty(nextHitID)}">
                <a href="${WebApplicationBaseURL}nav?path=~docdetail&id=${nextHitID}&offset=${offset +1}">&gt;&gt;</a>                        
            </c:if>
         </mcr:browseCtrl>
      </td>
   </tr>
</table>


<table cellspacing="0" cellpadding="0" id="metaData">
    <mcr:docDetails var="docDetails" mcrObj="${mycoreobject}" lang="${requestScope.lang}" />
    <x:forEach select="$docDetails//metaname">
      <x:choose>
         <x:when select="./@type = 'space'">
            <tr>
               <td colspan="2" class="metanone">&nbsp;</td>
            </tr>                
         </x:when>
         <x:when select="./@type = 'standard'">
            <x:set var="nameKey" select="string(./@name)" />
            <tr>
               <td class="metaname"><fmt:message key="${nameKey}" />:</td>
               <td class="metavalue">
                  <x:forEach select="./metavalues">
                     <x:set var="separator" select="./@separator" />
                     <x:set var="terminator" select="./@terminator" />
                     <x:if select="string-length(./@introkey) > 0" >
                        <x:set var="introkey" select="string(./@introkey)" />
                        <fmt:message key="${introkey}" />
                     </x:if>
                     <x:forEach select="./metavalue">
                        <x:if select="generate-id(../metavalue[position() = 1]) != generate-id(.)">
                           <x:out select="$separator" escapeXml="false" />
                        </x:if>
                        <x:choose>
                           <x:when select="../@type = 'BooleanValues'">
                              <x:set var="booleanKey" select="concat(./@type,'-',./@text)" />
                              <fmt:message key="${booleanKey}" />
                           </x:when>
                           <x:when select="../@type = 'AuthorJoin'">
                              <x:set var="authorjoinKey" select="concat(./@type,'-',./@text)" />
                                 <a href="<x:out select="./@href" />" target="<x:out select="./@target" />"><fmt:message key="${authorjoinKey}" /></a>
                           </x:when>                                     
                           <x:when select="./@href != ''">
                              <a href="<x:out select="./@href" />" target="<x:out select="./@target" />"><x:out select="./@text" /></a>
                           </x:when>
                           <x:otherwise>
                              <x:out select="./@text" escapeXml="./@escapeXml" />
                           </x:otherwise>
                        </x:choose>
                     </x:forEach>
                     <x:if select="generate-id(../metavalues[position() = last()]) != generate-id(.)">
                        <x:out select="$terminator" escapeXml="false" />
                     </x:if>                               
                  </x:forEach>
                  <x:forEach select="./digitalobjects">
                     <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tbody>
                        <x:forEach select="./digitalobject">
                           <tr>
                              <td align="left" valign="top">
                                 <div class="derivateBox">
                                    <div class="derivateHeading"><x:out select="./@derivlabel" /></div>
                                    <div class="derivate">
                                       <x:set var="mainFileURL" select="concat($WebApplicationBaseURL,'file/',./@derivid,'/',./@derivmain,'?hosts=',$host)" />
                                       <x:set var="contentType" select="string(./@contentType)" />
                                       <table>
                                          <tr>
                                             <td><a href="<x:out select="$mainFileURL" />" target="_self"><x:out select="./@derivmain" /></a>&#160;
                                                 (<x:out select="./@size mod 1024" /> kB)&#160;&#160;
                                             </td>
                                             <td>
                                                <a href="<x:out select="concat($WebApplicationBaseURL,'zip?id=',./@derivid)" />" class="linkButton" ><fmt:message key="zipgenerate" /></a>&#160;&#160;
                                             </td>
                                             <td>
                                                <a href="<x:out select="concat($WebApplicationBaseURL,'nav?path=~derivatedetails&derID=',./@derivid,'&docID=',$mcrid,'&hosts=',$host)" />" target="_self"><fmt:message key="details" />&gt;&gt;</a>&#160;&#160; 
                                             </td>
                                             <c:if test="${fn:contains('gif-jpeg-png', contentType)}">
                                                <td class="imageInResult"><a href="${mainFileURL}"><img src="${mainFileURL}" width="100"></a></td>
                                             </c:if>                                                 
                                          </tr>
                                       </table>
                                    </div>
                                 </div>
                              </td>
                           </tr>
                        </x:forEach>
                        </tbody>
                     </table>                               
                  </x:forEach>
               </td>
            </tr>        
         </x:when>
      </x:choose>
    </x:forEach>
</table>
</c:catch>
<c:if test="${e!=null}">
An error occured, hava a look in the logFiles!
<% 
  Logger.getLogger("test.jsp").error("error", (Throwable) pageContext.getAttribute("e"));   
%>
</c:if>