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
<mcr:webjarLocator htmlElement="script" project="jquery" file="jquery.min.js" />
<mcr:webjarLocator htmlElement="script" project="bootstrap" file="js/bootstrap.min.js" />

<script type="text/javascript" src="${iviewBaseURL}js/iview-client-base.js"></script>
<script type="text/javascript" src="${iviewBaseURL}js/iview-client-desktop.js"></script>
<script type="text/javascript" src="${iviewBaseURL}js/iview-client-logo.js"></script>
<script type="text/javascript" src="${iviewBaseURL}js/iview-client-toolbar-extender.js"></script>

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

<c:if test="${it.doctype eq 'pdf'}">
	<script type="text/javascript" src="${iviewBaseURL}js/iview-client-pdf.js"></script>
	<script type="text/javascript" src="${iviewBaseURL}js/lib/pdf.js"></script>
	<script type="text/javascript" src="${iviewBaseURL}js/iview-client-metadata.js"></script>
	<style type="text/css">
      button[data-id='ShareButton']{
        border-radius:4px !important;
      }
      
      button[data-id='PdfDownloadButton']{
        display:none;
      }
      
    </style>
	<script>
		window.onload = function() {
			var config = {
				logoURL:"${applicationScope.WebApplicationBaseURL}images/mcrviewer/mcrviewer.png",
				"mobile" : false,
				pdfProviderURL : "${applicationScope.WebApplicationBaseURL}${it.pdfProviderURL}",
				derivate : "${it.recordIdentifier}",
				filePath : "${it.filePath}",
				doctype : "pdf",
				startImage : "1",
				i18nURL : "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
				lang : "de",
				webApplicationBaseURL : "${applicationScope.WebApplicationBaseURL}",
				pdfWorkerURL : "${iviewBaseURL}js/lib/pdf.worker.js",
				"canvas.startup.fitWidth" : true,
				"canvas.overview.enabled" : false,
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
								href : "${applicationScope.WebApplicationBaseURL}${it.pdfProviderURL}",
								icon: "fa-download",
								inGroup: "addOns"
							} ]
			};
			new mycore.viewer.MyCoReViewer(jQuery("body"), config);
		};
	</script>
</c:if>
<c:if test="${it.doctype eq 'mets'}">
	<c:set var="mcrid">${it.mcrid}</c:set>
	<script type="text/javascript" src="${iviewBaseURL}js/iview-client-mets.js"></script>
	<script type="text/javascript" src="${iviewBaseURL}js/iview-client-metadata.js"></script>
	
	<%
	   

		MCRSessionMgr.unlock();
	 	try 
		(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
			MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(
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

	<script>
		window.onload = function() {
			var config = {
				mobile : false,
				doctype : "mets",

				//derivate: "${fn:replace(it.recordIdentifier,'/','%252F')}",
				derivate : "${derid}",
				filePath : "${it.filePath}",
				metsURL : "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
				imageXmlPath : "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(it.recordIdentifier,'/','_')}/",
				tileProviderPath : "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(it.recordIdentifier,'/','_')}/",

				i18nURL : "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
				lang : "de",
				webApplicationBaseURL : "${applicationScope.WebApplicationBaseURL}",
				// derivateURL : "${applicationScope.WebApplicationBaseURL}depot/${fn:replace(it.recordIdentifier,'/','%25252F')}/",
				derivateURL : "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/",
				"canvas.startup.fitWidth" : true,
				"canvas.overview.enabled" : false,
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
	                    	   id : "btn_pdf_download",
	                    	   type : "button",
	                    	   label : "buttons.pdf_download",
	                    	   href : "${applicationScope.WebApplicationBaseURL}pdfdownload/recordIdentifier/${fn:replace(it.recordIdentifier,'/','_')}",
	                    	   icon: "fa-download",
	                    	   inGroup: "addOns"
				           },
		                   {
		                       id : "btn_page_download",
		                       type : "button",
		                       label : "buttons.page_download",
		                    
		                       action : function(obj, evt){
		                    	   <%-- Alternativ müsste man die Komponenten Liste des Viewer nach der MyCoReImageScrollComponent suchen. 
                                        Diese hat das Property _currentImage --%>
		                    	   			var s = $("div[data-id='ImageChangeControllGroup'] > select > option:selected");
		                    	   			window.location.href="${applicationScope.WebApplicationBaseURL}depot/${fn:replace(it.recordIdentifier,'/','_')}/images/"+s.data('id')+".jpg";
		                       },
		                       icon: "fa-download",
		                       inGroup: "addOns"
		                   	}],
	                   	  
				objId : ""

			};
			new mycore.viewer.MyCoReViewer(jQuery("body"), config);
		};
	</script>
</c:if>
</head>

<body>

</body>
</html>
