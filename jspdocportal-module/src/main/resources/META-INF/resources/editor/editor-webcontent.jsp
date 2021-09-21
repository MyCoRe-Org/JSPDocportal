<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>


<fmt:message var="pageTitle" key="Webpage.editwebcontent.form.headline" />
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}" layout="1column">
	<stripes:layout-component name="html_head">
		<%-- Jquery from Layout: <script src="//code.jquery.com/jquery-1.11.1.min.js"></script> --%>
   		<%-- Bootstrap from Layout: <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script> --%>
   		
   		<mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/ckeditor.js" />
   		<mcr:webjarLocator htmlElement="script" project="ckeditor" file="standard/adapters/jquery.js" />
	</stripes:layout-component>
	
	<stripes:layout-component name="main_part">
		<div class="ir-box">
			<h2><fmt:message key="Webpage.editwebcontent.form.headline" /></h2>
			<p><strong>Datei: ${actionBean.file}</strong></p>
		<form id="editWebcontent_${actionBean.id}" method="post" action="saveWebcontent.action" accept-charset="UTF-8">
		    <input type="hidden" name="file_${actionBean.id}" value="${actionBean.file}" />
		    <input type="hidden" name="referer_${actionBean.id}" value="${actionBean.referer}" />
		    <textarea  id="taedit_${actionBean.id}" name="content_${actionBean.id}" rows="10" cols="80">${actionBean.content}</textarea>
		
		<c:set var="jsid" value="${fn:replace(actionBean.id, '.', '\\\\\\\\.')}" />
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
		        <input type="submit"  name="doSave_${actionBean.id}" class="btn btn-primary" 
		               title="<fmt:message key="Webpage.editwebcontent.save"/>" value="Speichern" /> 
		                 <%-- <i class="fa fa-floppy-o"></i> <fmt:message key="Webpage.editwebcontent.save" /> --%>
				

		        <input type="submit"  name="doCancel_${actionBean.id}" class="btn btn-danger" 
		        	title="<fmt:message key="Webpage.editwebcontent.cancel" />" value="Abbrechen" />
		        		<%-- <i class="fa fa-times"></i> <fmt:message key="Webpage.editwebcontent.cancel" /> --%>
		    </div>
		  </div>
		</form>
		</div>
	</stripes:layout-component>
</stripes:layout-render>   