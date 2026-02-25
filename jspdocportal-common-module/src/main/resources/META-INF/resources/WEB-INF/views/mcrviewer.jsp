<%@page import="org.mycore.datamodel.metadata.MCRExpandedObject"%>
<%@page import="org.mycore.common.MCRSessionMgr"%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@page import="org.apache.logging.log4j.core.Logger"%>
<%@page import="org.jdom2.Element"%>
<%@page import="org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID"%>
<%@page import="org.mycore.datamodel.metadata.MCRObject"%>
<%@page import="org.mycore.datamodel.metadata.MCRObjectID"%>
<%@page import="org.mycore.datamodel.metadata.MCRMetadataManager"%>
<%@page import="org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<c:set var="iviewBaseURL" value="${applicationScope.WebApplicationBaseURL}modules/iview2/" />
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<mcr:webjarLocator htmlElement="stylesheet" project="bootstrap" file="css/bootstrap.min.css" />
<mcr:webjarLocator htmlElement="stylesheet" project="font-awesome" file="css/all.min.css" />

<link rel="stylesheet" type="text/css" href="${applicationScope.WebApplicationBaseURL}modules/mcrviewer/mcrviewer.css" />
<link rel="stylesheet" type="text/css" href="${iviewBaseURL}css/default.css" />
<mcr:webjarLocator htmlElement="script" project="bootstrap" file="js/bootstrap.bundle.min.js" />

<script type="module" src="${iviewBaseURL}js/iview-client-base.es.js"></script>
<script type="module" src="${iviewBaseURL}js/iview-client-desktop.es.js"></script>
<script type="module" src="${iviewBaseURL}js/iview-client-logo.es.js"></script>
<script type="module" src="${iviewBaseURL}js/iview-client-toolbar-extender.es.js"></script>

<style type="text/css">
      div.mcrviewer_html{
        border: 1px solid lightgrey;
        padding: 5px;
      }

      .mycoreViewer .navbar .navbar-right {
        margin-right: 20px;
      }

      .mycoreViewer .navbar .navbar-left {
        margin-left: 20px;
      }
</style>

<mcr:session var="lang" info="language" />

<c:if test="${it.doctype eq 'pdf'}">
	<script type="module" src="${iviewBaseURL}js/iview-client-pdf.es.js"></script>
	<script type="module" src="${iviewBaseURL}js/iview-client-metadata.es.js"></script>
	<style type="text/css">
      button[data-id='ShareButton']{
        border-radius:4px !important;
      }
      
      button[data-id='PdfDownloadButton']{
        display:none;
      }
      
      div[data-id='ActionControlGroup']{
        display:none;
      }
      
    </style>
	<script type="module">
		import {MyCoReViewer} from "${iviewBaseURL}js/iview-client-base.es.js";

		window.onload = function() {
			var config = {
				logoURL:"${applicationScope.WebApplicationBaseURL}images/mcrviewer/mcrviewer.png",
				mobile: false,
				pdfProviderURL : "${applicationScope.WebApplicationBaseURL}${it.pdfProviderURL}",
				derivate : "${it.recordIdentifier}",
				filePath : "${it.filePath}",
				doctype : "pdf",
				startImage : "1",
				i18nURL : "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
				lang : "${lang}",
				webApplicationBaseURL : "${applicationScope.WebApplicationBaseURL}",
				pdfWorkerURL : "${iviewBaseURL}js/lib/pdf.worker.min.js",
				canvas: {
                	startup:{
                		fitWidth: true
                	},
                	overview:{
                		enabled:true,
                		minVisibleSize: 600
                	}
                },
				permalink : {
					enabled : true,
					updateHistory : true,
					viewerLocationPattern : "{baseURL}/mcrviewer/recordIdentifier/${fn:replace(it.recordIdentifier,'/','_')}/{file}"
				},
				onClose : function() {
					window.history.back();
					setTimeout(function() {
						window.close();
					}, 500);
				},
				
				toolbar : [ {
								id: "addOns",
								type: "group"
							},
							{
								id : "pdf_download",
								type : "button",
								label : "buttons.pdf_download",
								tooltip : "buttons.pdf_download.tooltip",
								action : function(obj, evt){
                    	   			window.open("${applicationScope.WebApplicationBaseURL}${it.pdfProviderURL}", '_blank');
                                },
								
								icon: "download",
								inGroup: "addOns"
							} ]
			};
			window["viewer"] = new MyCoReViewer(document.body, config);
		};
	</script>
</c:if>
<c:if test="${it.doctype eq 'mets'}">
	<c:set var="mcrid">${it.mcrid}</c:set>
	<script type="module" src="${iviewBaseURL}js/iview-client-mets.es.js"></script>
	<script type="module" src="${iviewBaseURL}js/iview-client-metadata.es.js"></script>
	
	<%
	   

		MCRSessionMgr.unlock();
	 	try 
		(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
			MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(
			MCRObjectID.getInstance(String.valueOf(pageContext.getAttribute("mcrid"))));
			String derLabel = "MCRVIEWER_METS";
			for (MCRMetaEnrichedLinkID derLink : mcrObj.getStructure().getDerivates()) {
				Element e = derLink.createXML();
				if (e.getChild("classification")!=null && derLabel.equals(e.getChild("classification").getAttributeValue("categid"))) {
					pageContext.setAttribute("maindoc", e.getChildTextTrim("maindoc"));
					pageContext.setAttribute("derid", derLink.getXLinkHref());
				}
			}
		} catch (Exception e) {
		    LogManager.getLogger("mcrviewer.jsp").error("Problem in mcrviewer.jsp", e);
		}
	%>

	<script type="module">
		import {MyCoReViewer} from "${iviewBaseURL}js/iview-client-base.es.js";

		window.onload = function() {
			var config = {
				mobile : false,
				doctype : "mets",

				derivate : "${derid}",
				filePath : "${it.filePath}",
				metsURL : "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
				imageXmlPath : "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(it.recordIdentifier,'/','_')}/",
				tileProviderPath : "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(it.recordIdentifier,'/','_')}/",

				i18nURL : "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
				lang : "${lang}",
				webApplicationBaseURL : "${applicationScope.WebApplicationBaseURL}",
				derivateURL : "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/",
				canvas: {
                	startup:{
                		fitWidth: true
                	},
                	overview:{
                		enabled:true,
                		minVisibleSize: 600
                	}
                },
				permalink : {
					enabled : true,
					updateHistory : true,
					viewerLocationPattern : "{baseURL}/mcrviewer/recordIdentifier/${fn:replace(it.recordIdentifier,'/','_')}/{file}"
				},
				imageOverview : {
					enabled : true
				},
				chapter : {
					enabled : true,
					showOnStart : true
				},
				text : {
					enabled : true
				},

				onClose : function() {
					window.history.back();
					setTimeout(function() {
						window.close();
					}, 500);
				},
				
				logoURL:"${applicationScope.WebApplicationBaseURL}images/mcrviewer/mcrviewer.png",
				
				toolbar : [{
				        	   id: "addOns",
		                       type: "group"
	                       },
	                       {
				        	   id: "addOns2",
		                       type: "group"
	                       },
	                     <c:if test="${not(fn:contains(mcrid, '_object_'))}">
				           {
	                    	   id : "btn_pdf_download",
	                    	   type : "button",
	                    	   label : "buttons.pdf_download",
	                    	   tooltip : "buttons.pdf_download.tooltip",
	                    	   action : function(obj, evt){
		                    	   			window.open("${applicationScope.WebApplicationBaseURL}do/pdfdownload/recordIdentifier/${fn:replace(it.recordIdentifier,'/','_')}", '_blank');
		                       },
	                       	   icon: "download",
	                    	   inGroup: "addOns"
				           },
				         </c:if>
		                   {
		                       id : "btn_page_download",
		                       type : "button",
		                       label : "buttons.page_download",
		                       tooltip : "buttons.page_download.tooltip",
		                    
		                       action : function(obj, evt){
		                    	   <%-- Alternativ müsste man die Komponenten Liste des Viewer nach der MyCoReImageScrollComponent suchen. 
                                        Diese hat das Property _currentImage --%>
		                    	   			var s = document.querySelector("div[data-id='ImageChangeControllGroup'] > select > option:selected");
		                    	   			window.open("${applicationScope.WebApplicationBaseURL}depot/${fn:replace(it.recordIdentifier,'/','_')}/images/"+s.dataset.id + ".jpg", '_blank');
		                       },
		                       icon: "download",
		                       inGroup: "addOns2"
		                   	}],
	                   	  
				objId : ""

			};
			window["viewer"] = new MyCoReViewer(document.body, config);
		};
	</script>
</c:if>
</head>

<body>

</body>
</html>
