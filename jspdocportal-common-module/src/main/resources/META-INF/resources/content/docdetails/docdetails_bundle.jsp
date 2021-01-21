<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrdd" uri="http://www.mycore.org/jspdocportal/docdetails.tld" %>
<%-- Parameter: id - the MCR Object ID--%>
<%-- Parameter: fromWF - from Workflow or database --%>


<mcrdd:docdetails mcrID="${param.id}" lang="de" fromWorkflow="${param.fromWF}" outputStyle="table"> 
	<mcrdd:setnamespace prefix="xlink" uri="http://www.w3.org/1999/xlink" />
	<mcrdd:row select="/mycoreobject/structure/parents/parent" labelkey="" showInfo="false">
			<mcrdd:outputitem select="." var="xml" >
			<x:forEach select="$xml">
				<c:set var="idparam"><x:out select="./@*[local-name()='href']" /></c:set>
  					<span class="button"><a href="${WebApplicationBaseURL}resolve/id/${idparam}"><fmt:message key="Webpage.docdetails.gotoParent" /></a></span>
  				</x:forEach>
		</mcrdd:outputitem>     
	</mcrdd:row>

    <mcrdd:row select="/mycoreobject/metadata/titles/title[@type='original-main']" labelkey="OMD.maintitle" showInfo="false">
		<mcrdd:item select="./text()"  styleName="docdetails-value-title" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/titles/title[@type='original-sub']" labelkey="OMD.subtitle" showInfo="false">
		<mcrdd:item select="./text()" styleName="docdetails-value-title" />              
    </mcrdd:row>
    	
    <mcrdd:row select="/mycoreobject/metadata/participationlinks/participationlink | /mycoreobject/metadata/participants/participant" 
               labelkey="OMD.participants" showInfo="false">
    	<mcrdd:item labelkeyPrefix="OMD.typeOfParticipation." select="./@*[local-name()='label' or local-name()='type']" />
    	<mcrdd:linkitem select="." />
    </mcrdd:row>
    
     <mcrdd:row select="/mycoreobject/metadata/dates/date[@type='create']" labelkey="OMD.Date.publishingyear" showInfo="false">
		<mcrdd:item select="./text()" datePattern="yyyy" />              
    </mcrdd:row>


	<mcrdd:separator showLine="true" />
	
	
    <mcrdd:row select="/mycoreobject/metadata/descriptions/description" labelkey="OMD.descriptions" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/keywords/keyword" labelkey="OMD.keywords" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/subjects/subject" labelkey="OMD.class-subjects" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:separator showLine="true"/>
    
        <mcrdd:row select="/mycoreobject/metadata/origins/origin" labelkey="OMD.class-origins" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/notes/note" labelkey="OMD.notes" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/bkls/bkl" labelkey="OMD.bkl" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:separator showLine="true"/>  
    
    <mcrdd:row select="/mycoreobject/metadata/dates/date/text" labelkey="OMD.Date.publishingyear" showInfo="false">
		<mcrdd:item select="." />              
    </mcrdd:row>
	
	<mcrdd:row select="/mycoreobject/metadata/types/type" labelkey="OMD.class-types" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/formats/format" labelkey="OMD.class-formats" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/sizes/size" labelkey="OMD.sizes" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/places/place" labelkey="OMD.places" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
  	<mcrdd:separator showLine="true"/>
    
    <mcrdd:row select="/mycoreobject/metadata/notes/note" labelkey="OMD.notes" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:separator showLine="true"/>
    
      <mcrdd:row select="/mycoreobject/structure/children/child" labelkey="OMD.children" showInfo="false">
		<mcrdd:outputitem select="." var="xml" >
			<ul style="line-height:1.5em; list-style:none;list-style-position: inside;margin: 0px;padding: 0px;">
					<x:forEach select="$xml">
					      <x:set var="childID"  select="string(./@*[local-name()='href'])" />							
								<jsp:include page="fragments/childdocs.jsp" flush="true" >
				    				<jsp:param name="mcrid" value="${childID}" />
								</jsp:include>						     
					</x:forEach>
			</ul>
		</mcrdd:outputitem>              
    </mcrdd:row>
  
    <mcrdd:separator showLine="true"/>
 	
 	<mcrdd:row select="/mycoreobject/service/servdates/servdate[@type='createdate']" labelkey="OMD.created" showInfo="false">
		<mcrdd:item select="./text()" datePattern="dd. MMMM yyyy" />              
    </mcrdd:row>
 	
 	<mcrdd:row select="/mycoreobject/service/servdates/servdate[@type='modifydate']" labelkey="OMD.changed" showInfo="false">
		<mcrdd:item select="./text()" datePattern="dd. MMMM yyyy" />              
    </mcrdd:row>
 	
   <mcrdd:row select="/mycoreobject" labelkey="OMD.selflink" showInfo="false" >
   		<mcrdd:outputitem select="./@ID" var="current">
   		   <jsp:element name="a">
   		   		<jsp:attribute name="href">${applicationScope.WebApplicationBaseURL}resolve/id/<x:out select="string($current)"/></jsp:attribute>
   		   		<jsp:body>${applicationScope.WebApplicationBaseURL}resolve/id/<x:out select="string($current)"/></jsp:body>
   		   </jsp:element>
   		</mcrdd:outputitem> 
   	</mcrdd:row>  

</mcrdd:docdetails>