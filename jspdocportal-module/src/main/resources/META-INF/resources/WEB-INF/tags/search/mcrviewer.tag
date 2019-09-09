<%@tag import="org.jdom2.Content"%>
<%@tag import="org.jdom2.Element"%>
<%@tag import="org.jdom2.filter.Filters"%>
<%@tag import="org.jdom2.xpath.XPathFactory"%>
<%@tag import="org.jdom2.xpath.XPathExpression"%>
<%@tag import="org.mycore.frontend.jsp.MCRHibernateTransactionWrapper"%>
<%@tag import="org.mycore.datamodel.metadata.MCRDerivate"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObjectID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetadataManager"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObject"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="mcrid" required="true" type="java.lang.String" %>
<%@ attribute name="recordIdentifier" required="true" type="java.lang.String" %>
<%@ attribute name="doctype" required="true" type="java.lang.String" %>

<c:set var="iviewBaseURL" value="${applicationScope.WebApplicationBaseURL}modules/iview2/" />

    <link href="${iviewBaseURL}css/default.css" type="text/css" rel="stylesheet">
    <script src="${iviewBaseURL}js/iview-client-base.js"></script>
    <script src="${iviewBaseURL}js/iview-client-frame.js"></script>
<c:if test="${doctype eq 'pdf' }">
    <script src="${iviewBaseURL}js/iview-client-pdf.js"></script>
    <script src="${iviewBaseURL}js/lib/pdf.js"></script>
	<style type="text/css">
		.mycoreViewer .navbar{
			position: absolute; left: 0px; right: 0px; top: 0px;
		}
	</style>
	
	<%
	try(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()){
		MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(String.valueOf(jspContext.getAttribute("mcrid"))));
		String derLabel = "fulltext";
		for(MCRMetaEnrichedLinkID derLink: mcrObj.getStructure().getDerivates()){
		    String maindoc=null;
		    boolean isFulltext = false;
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

    <script>
    window.addEventListener("load", function(){
            new mycore.viewer.MyCoReViewer(jQuery("#${id}"), {
                "mobile": false,
                pdfProviderURL: "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
                derivate: "${mcrid}",
                filePath: "${maindoc}",
                doctype: "${doctype}",
                startImage: "1",
                i18nURL: "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
                lang: "de",
                webApplicationBaseURL: "${applicationScope.WebApplicationBaseURL}",
                pdfWorkerURL: "${iviewBaseURL}js/lib/pdf.worker.js",
                "canvas.startup.fitWidth": false,
                "canvas.overview.enabled": false,
                "chapter.showOnStart": false,
                "chapter.enabled" : false,
                "imageOverview.enabled" : false,
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
    <script src="${iviewBaseURL}js/iview-client-mets.js"></script>
	<style type="text/css">
		.mycoreViewer .navbar{
			position: absolute; left: 0px; right: 0px; top: 0px;
		}
	</style>
<%
	try(MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()){
		MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(String.valueOf(jspContext.getAttribute("mcrid"))));
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

    <script>
    window.addEventListener("load", function(){
            new mycore.viewer.MyCoReViewer(jQuery("#${id}"),  {
            	"mobile": false,
                doctype: "mets",
                metsURL: "${applicationScope.WebApplicationBaseURL}file/${mcrid}/${derid}/${maindoc}",
                imageXmlPath: "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(recordIdentifier,'/','_')}/",
                tileProviderPath: "${applicationScope.WebApplicationBaseURL}tiles/${fn:replace(recordIdentifier,'/','_')}/",
                filePath: "iview2/${startImage}.iview2",
                derivate: "${derid}",
                i18nURL: "${applicationScope.WebApplicationBaseURL}rsc/locale/translate/{lang}/component.viewer.*",
                lang: "de",
                metadataURL: "",
                derivateURL : "${applicationScope.WebApplicationBaseURL}depot/${fn:replace(recordIdentifier,'/','%_')}/",
                objId: "",
                webApplicationBaseURL: "${applicationScope.WebApplicationBaseURL}",
                imageOverview : {
                    enabled: false
                },
                chapter: {
                    enabled: false,
                    showOnStart: false
                },
                permalink: {
                    enabled: true,
                    updateHistory: false,
                    viewerLocationPattern:"{baseURL}/mcrviewer/recordIdentifier/${fn:replace(recordIdentifier,'/','_')}/{file}"
                },
                canvas: {
                    overview: {
                        enabled: true
                    }
                },
                text: {
                	enabled: false
                }
            });
	   });
    </script>
</c:if>

