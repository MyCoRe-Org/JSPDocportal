<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr"%>

<%@page import="org.mycore.common.MCRConfiguration"%>
<c:set var="debug" value="${param.debug}" />

<c:set var="WebApplicationBaseURL"
	value="${applicationScope.WebApplicationBaseURL}" />
<fmt:setBundle basename="messages" />
<c:choose>
	<c:when test="${param.page > 1}">
		<c:set var="page" value="${param.page}" />
	</c:when>
	<c:otherwise>
		<c:set var="page" value="1" />
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${param.numPerPage > 1}">
		<c:set var="numPerPage" value="${param.numPerPage}" />
	</c:when>
	<c:otherwise>
		<c:set var="numPerPage" value="10" />
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${requestScope.host}">
		<c:set var="host" value="${requestScope.host}" />
	</c:when>
	<c:otherwise>
		<c:set var="host" value="local" />
	</c:otherwise>
</c:choose>
<c:set var="len" value="0" />
<c:set var="lang" value="${requestScope.lang}" />
<c:set var="navPath" value="${requestScope.path}" />
<c:set var="mcrresult" value="${requestScope.results}" />
<c:set var="query" value="${requestScope.query}" />

<!-- the result contains only the results from the begin of the selected page to page +numPerPage -->
<mcr:setResultList var="resultList" results="${mcrresult}"  objectType="oType" from="0"	 until="${numPerPage}" lang="${lang}" />

<c:choose>
	<c:when test="${fn:startsWith(resultlistType,'class')}">
		<c:set var="headlineKey"
			value="Webpage.searchresults.result-document-browse" />
	</c:when>
	<c:otherwise>
		<c:set var="headlineKey"
			value="Webpage.searchresults.result-document-search" />
	</c:otherwise>
</c:choose>

<x:forEach select="$resultList/mcr_results">
	<x:set var="resultid" select="string(./@id)" scope="page" />
	<x:set var="totalhits" select="string(./@total-hitsize)" scope="request" />
	<x:set var="mask" select="string(./@mask)" scope="page" />
	<div class="headline"><fmt:message key="${headlineKey}" /></div>
	<%-- disabled, because not working - enable after fixing <p><a
		href="${WebApplicationBaseURL}servlets/MCRJSPSearchServlet?mode=refine&mask=${mask}&id=${resultid}">
	<b>Suche verfeinern</b> </a><b> | </b> <a
		href="${WebApplicationBaseURL}servlets/MCRJSPSearchServlet?mode=renew&mask=${mask}">
	<b>neue Suche</b></a></p> --%>
	<jsp:include page="searchresult-resortform.jsp" />
	
	
	<x:if select="./mcr_result">
	<table cellpadding="0" cellspacing="0" width="100%">
		<tbody>
		<tr><td>
			<x:forEach select="./mcr_result/all-metavalues">
					<x:set var="resultlistLink" select="string(./metaname[1]/resultlistLink/@href)" />
					<x:set var="mcrID" select="string(@ID)" />
					<x:set var="docType" select="string(@docType)" />
					<!--  the number corresponds to the x. entry in resultlist-*.xml -->
					<x:set var="contentType"
							 select="string(./metaname[@name='OMD.class-types']/metavalues/metavalue/@categid)" />
					<x:set var="formatType"
							select="string(./metaname[@name='OMD.class-formats']/metavalues/metavalue/@categid|./metaname[@name='OMD.class-formats']/metavalues/metavalue/@text)" />
					<x:set var="data" select="." scope="request" />
					<table id="resultList" width="100%">
						<tbody>
							<tr valign="top" >
								<td width="30px" class="resultIcon" rowspan="2" >
									<jsp:include page="results-config/resultlistitems/resultlistitem-${docType}.jsp" >
							        	<jsp:param name="pageFragment" value="icon" />
								        <jsp:param name="contentType" value="${contentType}" />
   								        <jsp:param name="formatType" value="${formatType}" />
								    </jsp:include>
							    </td>
								<td class="resultTitle">
									<a href="${resultlistLink}&path=${navPath}.docdetail&resultid=${resultid}">
									<jsp:include page="results-config/resultlistitems/resultlistitem-${docType}.jsp" >
							        	<jsp:param name="pageFragment" value="headline" />
								        <jsp:param name="contentType" value="${contentType}" />
   								        <jsp:param name="formatType" value="${formatType}" />
								    </jsp:include>
									
<!-- 								<x:choose>
									<x:when select="contains('codice',$docType)">
										<x:out select="./metaname[1]/metavalues[2]/metavalue/@text" />
										<x:out select="./metaname[@name='OMD.title']/metavalues[3]/metavalue/@text" />
									</x:when>
									<x:when select="contains('professor',$docType)">
										<x:out select="./metaname[1]/metavalues/metavalue/@text" />,
										<x:out select="./metaname[1]/metavalues[2]/metavalue/@text" />
									</x:when>
									<x:otherwise>
										<x:out select="./metaname[1]/metavalues/metavalue/@text" />
									</x:otherwise> 
								</x:choose>-->
								
								 </a></td>

								<td class="id" align="right">[<c:out value="${mcrID}" />]</td>
							</tr>
							<tr>
								<td class="description">
								 <x:forEach select="./metaname[position() > 1]" >
								  <x:if select="./@type != 'hidden' ">
								   <x:if select="./@name != '' ">
										<x:set var="name" select="string(./@name)" />
										<fmt:message key="${name}" />:
								   </x:if>							 
	 							   <x:forEach select="./metavalues" >
									<x:set var="separator" select="string(./@separator)" />
									<x:set var="introkey" select="string(./@introkey)" />
									
									<x:if select="generate-id(../metavalues[position() > 1]) = generate-id(.)">
			                           <x:out select="$separator" escapeXml="false" />
                        			</x:if>									
 							        <x:forEach select="./metavalue[position() = 1]" >
										<x:set var="text" select="string(./@text)" />
										<x:set var="href" select="string(./@href)" />
											 							   
										<c:if test="${fn:length(introkey) > 0 }">
											<x:choose>
			                                <x:when select="../@type = 'messagekey'">
				                               <x:set var="val" select="string(./@text)" />
				                               <c:set var="messagekey" value="${introkey}.${val }" />
			      							   <fmt:message key="${messagekey}" />
											</x:when>
											<x:otherwise>
												<fmt:message key="${introkey}" />
											</x:otherwise>   
											</x:choose>
										</c:if>
										<x:choose>                                                               
			                               <x:when select="../@type = 'messagekey'">
			                                  <!-- ist schon im introkey behandelt -->
			                               </x:when>
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
			                                  <x:out select="./@text" escapeXml="false" /> 
			                               </x:otherwise>
			                            </x:choose>                           
									</x:forEach>
								  </x:forEach>	
								  <br/>
							     </x:if>									
								 </x:forEach>
								</td>
								<td align="right" >
								  <c:set var="type" value="${fn:split(mcrid,'_')[1]}"/>								     
							      <mcr:checkAccess var="modifyAllowed" permission="writedb" key="${mcrID}" />
							      <c:if test="${modifyAllowed}">
							        <mcr:isObjectNotLocked var="bhasAccess" objectid="${mcrID}" />
							        <c:choose>
							         <c:when test="${bhasAccess}"> 
								         <!--  Editbutton -->
								         <form method="get" action="${WebApplicationBaseURL}StartEdit" class="resort">                 
								            <input name="page" value="nav?path=~workflowEditor-${type}"  type="hidden">                                       
								            <input name="mcrid" value="${mcrID}" type="hidden"/>
												<input title="<fmt:message key="WF.common.object.EditObject" />" border="0" src="${WebApplicationBaseURL}images/workflow1.gif" type="image"  class="imagebutton" height="30" />
								         </form> 
							         </c:when>
							         <c:otherwise>
							            <img title="<fmt:message key="WF.common.object.EditObjectIsLocked" />" border="0" src="${WebApplicationBaseURL}images/workflow_locked.gif" height="30" />
							         </c:otherwise>
							        </c:choose>         
							      </c:if>      
								</td>
							</tr>
						</tbody>
					</table>
				</x:forEach>
		</td>
		</tr>		
	</tbody>
	</table>			
	       
      		<div id="resultListFooter">
      		<mcr:browsePageCtrl	var="browseControl" totalhits="${totalhits}"numPerPage="${numPerPage}" currentPage="${page}"
					maxDisplayedPages="10" resultid="${resultid}" /> 
			<x:forEach select="$browseControl/mcr_resultpage">
					<x:if select="generate-id(../mcr_resultpage[1]) = generate-id(.)">
						<fmt:message key="Webpage.searchresults.hitlists" />
					</x:if>
					<x:choose>
						<x:when
							select="( (contains(../@cutted-left,'true')) and (generate-id(../mcr_resultpage[1]) = generate-id(.)) )">
							<a href="${WebApplicationBaseURL}<x:out select="./@href" />">&lt;&lt;&lt;</a>&#160;
				        </x:when>
						<x:when
							select="( (contains(../@cutted-right,'true')) and (generate-id(../mcr_resultpage[last()]) = generate-id(.)) )">
							<a href="${WebApplicationBaseURL}<x:out select="./@href" />">&gt;&gt;&gt;</a>&#160;
	        			</x:when>
						<x:otherwise>
							<x:choose>
								<x:when select="contains(./@current,'true')">[<x:out select="./@pageNr" />]
			       				</x:when>
								<x:otherwise>
									<!-- /servlets/MCRSearchServlet?mode=results&id=-1xm6zxm7vxrojerkdk1sv&page=2&numPerPage=10	-->									
   	        					        [<a
										href="${WebApplicationBaseURL}<x:out select="./@href" />"><x:out
										select="./@pageNr" /></a>]                
				                </x:otherwise>
							</x:choose>
						</x:otherwise>
					</x:choose>
				</x:forEach>
			</div>
		<br />	
	 </x:if>
   </x:forEach>
				