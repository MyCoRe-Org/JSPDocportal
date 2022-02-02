<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>


<fmt:message var="pageTitle" key="Webpage.editwebcontent.form.headline" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="fragments/html_head.jspf" %>
   <mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/ckeditor.js" />
   <mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/adapters/jquery.js" />
  
</head>
<body>
  <%@ include file="fragments/header.jspf" %>
  <div id="content_area">
  <div class="container">
    <div class="row">
		<div class="col ir-box">
			<h2><fmt:message key="Webpage.editwebcontent.form.headline" /></h2>
			<p><strong>Datei: ${it.file}</strong></p>
		<form id="editWebcontent_${it.id}" method="post" action="${applicationScope.WebApplicationBaseURL}do/save-webcontent" accept-charset="UTF-8">
		    <input type="hidden" name="file_${it.id}" value="${it.file}" />
		    <input type="hidden" name="referer_${it.id}" value="${it.referer}" />
		    <textarea  id="taedit_${it.id}" name="content_${it.id}" rows="10" cols="80">${it.content}</textarea>
		
		<c:set var="jsid" value="${fn:replace(it.id, '.', '\\\\\\\\.')}" />
		<script type="text/javascript">
		
		 	var config = {
		 		basicEntities:false,
		 	    entities_additional: 'gt,lt,amp', //remove &nbsp; from entities (not allowed in XHTML)
				entities:false,
				entities_latin:false,
				entities_greek:false,
				allowedContent:true,
				format_tags:'p;h1;h2;h3;h4;h5;h6;pre;address;div'
				
         	};
		 	CKEDITOR.dtd.$removeEmpty.span = false;
		 	CKEDITOR.dtd.$removeEmpty.i = false;

		    $(document).ready( function() {$('textarea#taedit_${jsid}').ckeditor(config); });
		</script>
		  <div class="card">  
		    <div class="card-body bg-warning">
		        <input type="submit"  name="doSave_${it.id}" class="btn btn-primary" 
		               title="<fmt:message key="Webpage.editwebcontent.save"/>" value="Speichern" /> 
		                 <%-- <i class="fa fa-floppy-o"></i> <fmt:message key="Webpage.editwebcontent.save" /> --%>
				

		        <input type="submit"  name="doCancel_${it.id}" class="btn btn-danger" 
		        	title="<fmt:message key="Webpage.editwebcontent.cancel" />" value="Abbrechen" />
		        		<%-- <i class="fa fa-times"></i> <fmt:message key="Webpage.editwebcontent.cancel" /> --%>
		    </div>
		  </div>
		</form>
		</div>
    </div>
    </div>
    </div>
  <%@ include file="fragments/footer.jspf" %>
  </body>
</html>
    