<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>

<%@ taglib prefix="jspdp-ui" tagdir="/WEB-INF/tags/ui" %>

<fmt:message var="pageTitle" key="Nav.Browsing.EPub" />


<stripes:layout-render name="../../WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="1column">

<stripes:layout-component name="html_head">
     <jspdp-ui:init-dojo/>
     <link type="text/css" rel="stylesheet" href="css/style_searchresult.css" />
     <link type="text/css" rel="stylesheet" href="css/style_docdetails.css">
     <script>
     	function toogleTechButton(btn){
     		require(["dojo/query", "dojo/dom", "dojo/dom-style", "dojo/dom-class", "dojo/on", "dojo/domReady!"], 
      			function(query, dom, domStyle, domClass, on ) {
     				if(domClass.contains(btn, "btn-tools-techdata-checked")){
     					query(".btn-tools-techdata").forEach(function(no){
     						domClass.remove(no, "btn-tools-techdata-checked");
     					});
     					query(".div-technical-data").forEach(function(no){
     						domStyle.set(no, {display: "none"});
     					});
     				}
     				else{
     					query(".btn-tools-techdata").forEach(function(no){
     						domClass.add(no, "btn-tools-techdata-checked");
     					});
     					//query(".div-technical-data").style({display: ""});
     					query(".div-technical-data").forEach(function(no){
     						domStyle.set(no, {display: ""});
     					});
     				}
        	});
     	}
      </script>
</stripes:layout-component>

<stripes:layout-component name="main_part">

<stripes:form beanclass="de.uni_rostock.ub.rosdok.stripes.actions.BrowseEPubSearchActionBean"
      id="form_detailsEPub" enctype="multipart/form-data" acceptcharset="UTF-8">


   <%--<stripes:form action="nav?path=~results-epub"
      id="form_detailsEPub" enctype="multipart/form-data" acceptcharset="UTF-8"> --%> 
      <stripes:hidden name="searcher.id" />
      <stripes:hidden name="searcher.classType" />
      <stripes:hidden name="searcher.classOrigin" />
      <stripes:hidden name="searcher.classDnbSG" />
    
    
      <div style="width:240px;float:left;margin-top:0px">
      
      <div class="base_box" style="margin-bottom:30px">
          <div class="main_navigation">
            <mcr:outputNavigation id="left" expanded="false" mode="left" />
          </div>
                    <div class="main_navigation">
  <%--TODO                  <mcr:outputNavigation id="publish" expanded="false" mode="left" /> --%>   
                    </div>
          <div style="padding-top:16px;padding-bottom:16px; text-align: center;">
            <a href="http://www.mycore.org">
              <img alt="powered by MyCoRe 2.2"
                 src="${WebApplicationBaseURL}images/mycore_logo_powered_129x34_knopf_hell.png"
                style="border:0;text-align:center;">
            </a>
          </div>
       </div>
       <%--  <div class="base_box infobox" > </div>  --%>
      </div>
      
      <div style="float:left; width:750px">

      <div class="base_box breadcrumbs">
        <mcr:outputNavigation id="left" mode="breadcrumbs" />       
      </div>	 
      
      <div id="contents" class="base_content text">
        <div id="top"></div>

         <c:if test="${not empty actionBean.searcher.result}">
             <c:set var="result" value="${actionBean.searcher.result}" />
             <c:set var="mcrid" value="${result.mcrIDs[(result.current mod 25)]}" />
            <jspdp-ui:details-pagination numFound="${result.numFound}" current="${result.current}" mcrid="${mcrid}"/>

        <c:choose>
 							<c:when test="${fn:contains(mcrid,'codice')}">
     							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_codice.jsp?id=${mcrid}" />
 							</c:when>
   
 							<c:when test="${fn:contains(mcrid,'thesis')}">
 							     <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_thesis.jsp?id=${mcrid}" />
  							</c:when>
 
  							<c:when test="${fn:contains(mcrid,'disshab')}">
 							     <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_disshab.jsp?id=${mcrid}" />
  							</c:when>
 
  							<c:when test="${fn:contains(mcrid,'document')}">
      							 <c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_document.jsp?id=${mcrid}" />
 							 </c:when>
   							<c:when test="${fn:contains(mcrid,'_bundle_')}">
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_bundle.jsp?id=${mcrid}" />
 							 </c:when>
    						
  							<c:otherwise>
      							<c:import url="${WebApplicationBaseURL}content/docdetails/docdetails_document.jsp?id=${mcrid}" />
  							</c:otherwise>
  						</c:choose>
            <c:if test="${result.numFound>0}">
              <jspdp-ui:details-pagination numFound="${result.numFound}" current="${result.current}" mcrid="${mcrid}"/>
            </c:if>
      </c:if>
    
   </div>
	   </div>
     
	 </stripes:form>
  </stripes:layout-component>
</stripes:layout-render>
