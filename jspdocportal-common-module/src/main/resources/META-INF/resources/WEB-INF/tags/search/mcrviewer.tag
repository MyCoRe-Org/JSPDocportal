<%@tag import="org.mycore.datamodel.metadata.MCRExpandedObject"%>
<%@tag import="org.jdom2.Content"%>
<%@tag import="org.jdom2.Element"%>
<%@tag import="org.jdom2.filter.Filters"%>
<%@tag import="org.jdom2.xpath.XPathFactory"%>
<%@tag import="org.jdom2.xpath.XPathExpression"%>
<%@tag import="org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper"%>
<%@tag import="org.mycore.datamodel.metadata.MCRDerivate"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObjectID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetadataManager"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObject"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="mcrid" required="true" type="java.lang.String" %>
<%@ attribute name="recordIdentifier" required="true" type="java.lang.String" %>
<%@ attribute name="doctype" required="true" type="java.lang.String" %>

<c:set var="iviewBaseURL" value="${applicationScope.WebApplicationBaseURL}modules/iview2/" />
<mcr:session var="lang" info="language" />

    <link href="${iviewBaseURL}css/default.css" type="text/css" rel="stylesheet">
    <script type="module" src="${iviewBaseURL}js/iview-client-base.es.js"></script>
    <script type="module" src="${iviewBaseURL}js/iview-client-frame.es.js"></script>
    
    
<c:if test="${doctype eq 'pdf' }">
    <script type="module" src="${iviewBaseURL}js/iview-client-pdf.es.js"></script>
	<style type="text/css">
		.mycoreViewer .navbar{
			position: absolute; left: 0px; right: 0px; top: 0px;
		}
		
		.mycoreViewer div[data-id='SidebarControllGroup']{
			display:none;
		}
	</style>
	
	<%
	try(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()){
		MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(MCRObjectID.getInstance(String.valueOf(jspContext.getAttribute("mcrid"))));
		String derLabel = "fulltext";
		for(MCRMetaEnrichedLinkID derLink: mcrObj.getStructure().getDerivates()){
		    String maindoc=null;
		    boolean isFulltext = false;
		    //use derLink.createXML to create the XML-Element -> continue with e.getchild("maindoc")
		    for(Content c : derLink.getContentList()){
		        if(c instanceof Element && ((Element)c).getName().equals("maindoc")){
		            maindoc = ((Element)c).getText();
		        }
		    	if(c instanceof Element && ((Element)c).getName().equals("classification")){
	            	isFulltext = isFulltext || ("derivate_types".equals(((Element)c).getAttributeValue("classid")) && "fulltext".equals(((Element)c).getAttributeValue("categid")));
	        	}
		    }
			if(isFulltext && maindoc!=null){
		    	jspContext.setAttribute("maindoc", maindoc);
		    	jspContext.setAttribute("derid", derLink.getXLinkHref());
			}
		}
	}
	catch(Exception e){
	    //do nothing
	}
%>

    <script type="module">
      import {MyCoReViewer} from "${iviewBaseURL}js/iview-client-base.es.js";

      window.addEventListener("load", function(){
            window["viewer"] = new MyCoReViewer(jQuery("#${id}"), {
                mobile: false,
                pdfProviderURL: "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
                derivate: "${mcrid}",
                filePath: "${maindoc}",
                doctype: "${doctype}",
                startImage: "1",
                i18nURL: "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
                lang: "${lang}",
                webApplicationBaseURL: "${applicationScope.WebApplicationBaseURL}",
                pdfWorkerURL: "${iviewBaseURL}js/lib/pdf.worker.min.js",
                canvas: {
                	startup:{
                		fitWidth: true
                	},
                	overview:{
                		enabled: true,
                		minVisibleSize: 9999
                	}
                },
                chapter: {
                	showOnStart: false,
                    enabled: false
                },
                imageOverview: {
                	enabled: false
                },
                permalink: {
                    enabled: true,
                    updateHistory: false,
                    viewerLocationPattern:"{baseURL}/mcrviewer/id/{derivate}/{file}"
                }
            });
        });
    </script>
</c:if>


<c:if test="${doctype eq 'mets' }">
    <script type="module" src="${iviewBaseURL}js/iview-client-mets.es.js"></script>
	<style type="text/css">
		.mycoreViewer .navbar{
			position: absolute; left: 0px; right: 0px; top: 0px;
		}
	</style>
<%
	try(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()){
		MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(MCRObjectID.getInstance(String.valueOf(jspContext.getAttribute("mcrid"))));
		String derLabel = "MCRVIEWER_METS";
		for(MCRMetaEnrichedLinkID derLink: mcrObj.getStructure().getDerivates()){
		
		
		String maindoc=null;
	    boolean isMets = false;
	    for(Content c : derLink.getContentList()){
	        if(c instanceof Element && ((Element)c).getName().equals("maindoc")){
	            maindoc = ((Element)c).getText();
	        }
	    	if(c instanceof Element && ((Element)c).getName().equals("classification")){
	    	    isMets = isMets || ("derivate_types".equals(((Element)c).getAttributeValue("classid")) && "MCRVIEWER_METS".equals(((Element)c).getAttributeValue("categid")));
        	}
	    }
		if(isMets && maindoc!=null){
	    	jspContext.setAttribute("maindoc", maindoc);
	    	jspContext.setAttribute("derid", derLink.getXLinkHref());
		}
		
		jspContext.setAttribute("startImage", "phys_0001");
        if(request.getParameter("_mcrviewer_start")!=null){
            jspContext.setAttribute("startImage", request.getParameter("_mcrviewer_start"));
        }
        else{
		  XPathExpression<Element> xpCoverImage = XPathFactory.instance().compile("//irControl/map/entry[@key='start_image']", Filters.element());
		  for(Element e : xpCoverImage.evaluate(mcrObj.createXML())){
		    jspContext.setAttribute("startImage", e.getTextTrim());
		  }
        }
		}
	}
	catch(Exception e){
	    //do nothing
	}
%>

    <script type="module">
      import {MyCoReViewer} from "${iviewBaseURL}js/iview-client-base.es.js";
      window.addEventListener("load", function(){
        window["viewer"] = new MyCoReViewer(jQuery("#${id}"), {
                mobile: false,
                doctype: "mets",
                metsURL: "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
                imageXmlPath: "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(recordIdentifier,'/','_')}/",
                tileProviderPath: "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(recordIdentifier,'/','_')}/",
                filePath: "iview2/${startImage}.iview2",
                derivate: "${derid}",
                i18nURL: "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
                lang: "${lang}",
                metadataURL: "",
                derivateURL: "${applicationScope.WebApplicationBaseURL}depot/${fn:replace(recordIdentifier,'/','_')}/",
                objId: "",
                webApplicationBaseURL: "${applicationScope.WebApplicationBaseURL}",
                canvas: {
                	startup:{
                		fitWidth: true
                	},
                	overview:{
                		enabled:true,
                		minVisibleSize: 9999
                	}
                },
                imageOverview : {
                    enabled: true
                },
                chapter: {
                    enabled: true,
                    showOnStart: false
                },
                permalink: {
                    enabled: true,
                    updateHistory: false,
                    viewerLocationPattern:"{baseURL}/mcrviewer/recordIdentifier/${fn:replace(recordIdentifier,'/','_')}/{file}"
                },
                text: {
                	enabled: false
                }
            });
	   });
    </script>
</c:if>

