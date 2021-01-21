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
	<mcrdd:setnamespace prefix="cc" uri="http://www.d-nb.de/standards/cc/" />
	<mcrdd:setnamespace prefix="pc" uri="http://www.d-nb.de/standards/pc/" />
	
	<mcrdd:row select="/mycoreobject/metadata/creators/creator" labelkey="OMD.author" showInfo="false">
    	<mcrdd:outputitem select="." var="current">
    		<x:out select="$current//pc:foreName" /> 
    		<x:if select="$current//pc:prefix">
    			<x:out select="$current//pc:prefix" />
    		</x:if>
    		<x:out select="$current//pc:surName" />
    		<x:if select="$current//pc:academicTitle">
    			(<x:out select="$current//pc:academicTitle" />)
    		</x:if>   		   
   		</mcrdd:outputitem> 
    </mcrdd:row>

    <mcrdd:row select="/mycoreobject/metadata/titles/title[@type='original-main']" labelkey="OMD.maintitle" showInfo="false">
		<mcrdd:item select="./text()" styleName="docdetails-value-title" />              
    </mcrdd:row>
    <mcrdd:row select="/mycoreobject/metadata/titles/title[@type='original-sub']" labelkey="OMD.subtitle" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
	<mcrdd:row select="/mycoreobject/metadata/types/type" labelkey="OMD.class-types" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>    
    

    <mcrdd:row select="/mycoreobject/metadata/origins/origin" labelkey="OMD.class-origins" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/institutions/institution" labelkey="OMD.institution" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>

	<mcrdd:row select="/mycoreobject/metadata/dates/date[@type='submitted']" labelkey="OMD.yearsubmitted" showInfo="false">
		<mcrdd:item select="./text()" datePattern="yyyy" />              
    </mcrdd:row>

	<mcrdd:row select="/mycoreobject/metadata/dates/date[@type='accepted']" labelkey="OMD.yearaccepted" showInfo="false">
		<mcrdd:item select="./text()" datePattern="yyyy" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/dates/date[@type='published']" labelkey="OMD.yearpublished" showInfo="false">
      <mcrdd:item select="./text()" datePattern="yyyy" />              
    </mcrdd:row>
        
    <mcrdd:separator showLine="true"/>  
     
    <mcrdd:row select="/mycoreobject/metadata/contributors/contributor" labelkey="OMD.referee" showInfo="false">
    	<mcrdd:outputitem select="." var="current">
    		<x:out select="$current//pc:foreName" /> 
    		<x:if select="$current//pc:prefix">
    			<x:out select="$current//pc:prefix" />
    		</x:if>
    		<x:out select="$current//pc:surName" />
    		<x:if select="$current//pc:academicTitle">
    			(<x:out select="$current//pc:academicTitle" />)
    		</x:if>
    		<x:if select="$current//pc:affiliation">
    			<br /><x:out select="$current//pc:affiliation//cc:name" />
    		</x:if>   		   
   		</mcrdd:outputitem>
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/descriptions/description" labelkey="OMD.descriptions" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/keywords/keyword" labelkey="OMD.keywords" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>

  	<mcrdd:separator showLine="false"/>
    
	<mcrdd:row select="/mycoreobject/metadata/subjects/subject" labelkey="OMD.class-subjects" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/bkls/bkl" labelkey="OMD.bkl" showInfo="false" >
	  	<mcrdd:classificationitem select="." />  
    </mcrdd:row>
    
    <mcrdd:separator showLine="true"/>
  
    <mcrdd:row select="/mycoreobject/metadata/sources/source" labelkey="OMD.sources" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:row select="/mycoreobject/metadata/coverages/coverage" labelkey="OMD.coverages" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>

    <mcrdd:row select="/mycoreobject/metadata/notes/note" labelkey="OMD.notes" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>

    <mcrdd:row select="/mycoreobject/metadata/sizes/size" labelkey="OMD.sizes" showInfo="false">
		<mcrdd:item select="./text()" />              
    </mcrdd:row>
    
    <mcrdd:separator showLine="true"/>
    
    <mcrdd:row select="/mycoreobject/metadata/urns/urn" labelkey="OMD.urns" showInfo="false" >
   		<mcrdd:outputitem select="." var="current">
   		   <jsp:element name="a">
   		   		<jsp:attribute name="href">http://nbn-resolving.de/<x:out select="string($current)"/></jsp:attribute>
   		   		<jsp:body><x:out select="string($current)"/></jsp:body>
   		   </jsp:element>
   		</mcrdd:outputitem> 
   	</mcrdd:row>  
    
    <mcrdd:separator showLine="true"/>
 	
 	<mcrdd:row select="/mycoreobject/service/servdates/servdate[@type='createdate']" labelkey="OMD.created" showInfo="false">
		<mcrdd:item select="./text()" datePattern="dd. MMMM yyyy" />              
    </mcrdd:row>
 	
 	<mcrdd:row select="/mycoreobject/service/servdates/servdate[@type='modifydate']" labelkey="OMD.changed" showInfo="false">
		<mcrdd:item select="./text()" datePattern="dd. MMMM yyyy" />              
    </mcrdd:row>
 	
   <mcrdd:row select="/mycoreobject/metadata/urns/urn" labelkey="OMD.selflink" showInfo="false" >
   		<mcrdd:outputitem select="." var="current">
   		   <jsp:element name="a">
   		   		<jsp:attribute name="href">${applicationScope.WebApplicationBaseURL}resolve/urn/<x:out select="string($current)"/></jsp:attribute>
   		   		<jsp:body>${applicationScope.WebApplicationBaseURL}resolve/urn/<x:out select="string($current)"/></jsp:body>
   		   </jsp:element>
   		</mcrdd:outputitem> 
   	</mcrdd:row>  

</mcrdd:docdetails>