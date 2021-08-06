<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@page import="org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr"%>
<%@page import="org.mycore.frontend.servlets.MCRServlet"%>
<%@page import="org.mycore.common.MCRSessionMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="mcrdd" uri="http://www.mycore.org/jspdocportal/docdetails.tld" %>

<fmt:message var="pageTitle" key="WF.derivates.headline" /> 
<mcrdd:setnamespace prefix="xlink" uri="http://www.w3.org/1999/xlink" />

<!doctype html>
<html>
<head>
  <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
  <%@ include file="../fragments/html_head.jspf" %>
    <link type="text/css" rel="stylesheet" href="${WebApplicationBaseURL}css/style_workspace.css" />
    <script type="text/javascript">
      function enableDerMetaEditing(derID){
        $('#btnEditDerMetaSave_'+derID).show();
        $('#btnEditDerMetaCancel_'+derID).show();
        $('#btnEditDerMetaEdit_'+derID).hide();
        $('#selectEditDerMetaLabel_'+derID).prop('disabled', false);
        $('#txtEditDerMetaTitle_'+derID).prop('disabled', false);
      }

      function disableDerMetaEditing(derID){
        $('#btnEditDerMetaSave_'+derID).hide();
        $('#btnEditDerMetaCancel_'+derID).hide();
        $('#btnEditDerMetaEdit_'+derID).show();

        $('#selectEditDerMetaLabel_'+derID).val($('#selectEditDerMetaLabel_'+derID).data('original-value'));
        $('#txtEditDerMetaTitle_'+derID).val($('#txtEditDerMetaTitle_'+derID).data('original-value'));
        $('#selectEditDerMetaLabel_'+derID).prop('disabled', true);
        $('#txtEditDerMetaTitle_'+derID).prop('disabled', true);
      }

      function renameFile(derid, filename){
        var x = prompt('Geben Sie einen neuen Dateinamen an: Alles wird gut:', filename);
        if(x!=null){
          document.getElementById('hiddenRenameFileNew_'+derid+'_'+filename).value = x;
          //setting value of hidden input seems to be buggy in jquery
          //$('#hiddenRenameFileNew_'+derid+'_'+filename).val(x)
          return true;
        }
        return false;
      }
        
    </script>
  
</head>
<body>
  <%@ include file="../fragments/header.jspf" %>
  <div class="container">
        <div class="row">
          <div class="col">
		 	<h2><fmt:message key="WF.derivates.headline" /></h2>
			<c:set  var="baseURL" value="${applicationScope.WebApplicationBaseURL}" />
			
			<form method="post" action="${applicationScope.WebApplicationBaseURL}do/workspace/derivates"
				id="workspaceForm"  accept-charset="UTF-8" enctype="multipart/form-data">
            <input type="hidden" name="taskid" value="${it.taskid}" />
            <input type="hidden" name="mcrobjid" value="${it.mcrobjid}" />

				<%-- load first time from request parameter "returnPath --%>
				<div class="card border my-3">
  					<div class="card-header bg-dark" style="min-height:54px">
                      <div class="row">
                        <div class="col-2">
                          <span class="badge badge-pill badge-secondary">${it.currentVariables.mcrObjectID}</span>
                        </div>
                        <div class="col-8">
                        	<h3>
                            <c:set var="shortTitle" value="${fn:substring(it.currentVariables.wfObjectDisplayTitle, 0, 50)}..." />
					  		${fn:length(it.currentVariables.wfObjectDisplayTitle)<50 ? it.currentVariables.wfObjectDisplayTitle : shortTitle}
					  	</h3>
                      </div>
                      <div class="col-2">
  						<a class="btn btn-secondary float-right" href="${baseURL}do/workspace/tasks?mode=${it.mode}">
  						  <i class="fas fa-power-off"></i> <fmt:message key="WF.derivates.back" />
  						</a>
					  	</div>
					  
                      </div>
  					</div>
    				<div class="card-body p-0">
    					<c:set var="doc" value="${it.mcrobjXML}" />
    					<c:set var="objID"><x:out select="$doc/mycoreobject/@ID" /></c:set>
    					<x:forEach var="x" select="$doc/mycoreobject/structure/derobjects/derobject">
    						<c:set var="derID"><x:out select="$x/@xlink:href" /></c:set>
    						<c:set var="derDoc" value="${it.derivateXMLs[derID]}" />
    						<c:set var="maindoc"><x:out select="$derDoc/mycorederivate/derivate/internals/internal/@maindoc" /></c:set>
    						<div class="card border border-primary m-3">
  							   <div class="card-header bg-light">
                                  <div class="row">
                                      <div class="col-2">
                                          <span class="badge badge-pill badge-secondary" style="margin-right:24px; margin-top:6px;">${derID}</span>
                                      </div>
                                      <div class="col-5">
                                        <h4>
                                          <c:set var="derLabel"><x:out select="$derDoc/mycorederivate/derivate/classifications/classification[@classid='derivate_types']/@categid" /></c:set>
                                          <select id="selectEditDerMetaLabel_${derID}" name="saveDerivateMeta_label-task_${it.taskid}-derivate_${derID}" 
                                                  class="form-control" disabled="disabled"  data-original-value="${derLabel}">
                                            <c:forEach var="entry" items="${it.derivateLabels}">
                                              <c:if test="${entry.key eq derLabel}">
                                                <option value="${entry.key}" selected="selected">${entry.value}</option>
                                              </c:if>
                                              <c:if test="${not(entry.key eq derLabel)}">
                                                  <option value="${entry.key}">${entry.value}</option>
                                              </c:if>
                                            </c:forEach>
                                        </select>
                                        <c:if test="${fn:contains(objID,'_person_')}">
                                            <c:set var="derTitle"><x:out select="$derDoc/mycorederivate/derivate/titles/title/text()" /></c:set>
                                            <input id="txtEditDerMetaTitle_${derID}" name="saveDerivateMeta_title-task_${it.taskid}-derivate_${derID}" type="text" class="form-control" disabled="disabled" value="${derTitle}" data-original-value="${derTitle}" />
                                        </c:if>
                                        </h4> 
                                    </div>
                                    <div class="col-4">
  										 <button id="btnEditDerMetaSave_${derID}" name="doSaveDerivateMeta-task_${it.taskid}-derivate_${derID}" style="display:none;" class="btn btn-primary"><i class="fas fa-save"></i> <fmt:message key="WF.derivates.button.save"/></button>
  										 <button id="btnEditDerMetaCancel_${derID}" type="button" style="display:none;" class="btn btn-secondary" onclick="disableDerMetaEditing('${derID}')"><i class="fas fa-times"></i> <fmt:message key="WF.derivates.button.cancel"/></button>
  										 <button id="btnEditDerMetaEdit_${derID}" type="button" class="btn btn-secondary" onclick="enableDerMetaEditing('${derID}')"><i class="fas fa-pencil-alt"></i> <fmt:message key="WF.derivates.button.edit"/></button>
                                         <x:if select="$doc/mycoreobject/structure/derobjects/derobject[1]/@xlink:href != $x/@xlink:href">
                                           <fmt:message key="WF.derivates.button.move_up" var="titleMoveUp"/>
                                           <button id="btnEditDerMoveUp_${derID}" name="doMoveUpDerivate-task_${it.taskid}-derivate_${derID}" class="btn btn-secondary float-right ml-2" title="${titleMoveUp} }">
                                              <i class="fa fa-arrow-up"></i>
                                            </button>
                                         </x:if>
                                         <x:if select="$doc/mycoreobject/structure/derobjects/derobject[last()]/@xlink:href != $x/@xlink:href">
                                            <fmt:message key="WF.derivates.button.move_down" var="titleMoveDown"/>
                                            <button id="btnEditDerMoveUp_${derID}" name="doMoveDownDerivate-task_${it.taskid}-derivate_${derID}" class="btn btn-secondary float-right" title="${titleMoveDown}">
                                                <i class="fa fa-arrow-down"></i>
                                             </button>
                                         </x:if>
                                         
                                    </div>
                                    <div class="col-1">   
                                      <fmt:message key="WF.derivates.delete" var="titleDelete"/>
                                      <button title="${titleDelete}" data-toggle="collapse" data-target="#deleteDerivate_${derID}"
                                        class="btn btn-danger pull-right" type="button"><i class="fas fa-trash"></i></button>
  									</div>
                                  </div>
                                </div>
                      			<div id="deleteDerivate_${derID}" class="collapse">
                                    <div class="card-body border-top border-secondary bg-warning">
  											<fmt:message key="WF.derivates.delete" var="titleDelete"/>
  											<button id="btnDeleteDerivate_${derID}_${f}" title="${titleDelete}" name="doDeleteDerivate-task_${it.taskid}-derivate_${derID}" 
    										        class="btn btn-danger btn-sm"><i class="fas fa-trash"></i><fmt:message key="WF.workspace.button.delete" /></button>
    										<label class="ml-3"><fmt:message key="WF.derivates.delete.message" /></label>
  									</div>
  							   </div>
    							<div class="card-body border-top border-bottom border-secondary">
    									<ul class="ir-derivate-list pb-0">
    										<c:forEach var="f" items="${it.derivateFiles[derID]}">
    											<li>
                                                <div class="row">
                                                  <div class="col-8">
    											   <c:choose>
    												<c:when test="${fn:endsWith(fn:toLowerCase(f), '.xml')}">
    													<img src="${WebApplicationBaseURL}images/fileicons/xml.png" style="height:48px" />		
    												</c:when>
    												<c:when test="${fn:endsWith(fn:toLowerCase(f), '.htm') or fn:endsWith(fn:toLowerCase(f), '.html')}">
    													<img src="${WebApplicationBaseURL}images/fileicons/html.png"  style="height:48px"  />		
    												</c:when>
    												<c:when test="${fn:endsWith(fn:toLowerCase(f), '.pdf')}">
    													<img src="${WebApplicationBaseURL}images/fileicons/pdf.png" style="height:48px" />		
    												</c:when>
    												<c:when test="${fn:endsWith(fn:toLowerCase(f), '.zip')}">
    													<img src="${WebApplicationBaseURL}images/fileicons/compressed.png" style="height:48px" />		
    												</c:when>
    												<c:when test="${fn:endsWith(fn:toLowerCase(f), '.tif') or fn:endsWith(fn:toLowerCase(f), '.tiff') or fn:endsWith(fn:toLowerCase(f), '.jpg') or fn:endsWith(fn:toLowerCase(f), '.jpeg') or fn:endsWith(fn:toLowerCase(f), '.png')}">
    													<img src="${WebApplicationBaseURL}images/fileicons/image.png" style="height:48px" />		
    												</c:when>
    												<c:otherwise>
    													<img src="${WebApplicationBaseURL}images/fileicons/fileicon_bg.png" style="height:48px" />
    												</c:otherwise>
    											  </c:choose>
    											  <a href="${WebApplicationBaseURL}do/wffile/${it.currentVariables.mcrObjectID}/${derID}/${f}">${f}</a>
    											  <c:if test="${maindoc eq f}">
    												<c:set var="info"><fmt:message key="Editor.Common.derivate.maindoc" /></c:set>
    												<i class="fas fa-star text-secondary ml-3" title="${info}"></i>
    											  </c:if>
                                                </div>
                                                <div class="col-4">
    											<input type="hidden" id="hiddenRenameFileNew_${derID}_${f}" name="renameFile_new-task_${it.taskid}-derivate_${derID}-file_${f}" value="${f}" />
    											<fmt:message key="WF.derivates.rename_file" var="fileRename"/>
                                                <button id="btnRenameFile_${derID}_${f}" title="${fileRename}" name="doRenameFile-task_${it.taskid}-derivate_${derID}-file_${f}" 
                                                         onclick="return renameFile('${derID}', '${f}');" class="btn btn-sm btn-secondary" style="border:1px solid darkgrey">
                                                         <i class="fas fa-pencil-alt"></i>
                                                </button>
                                                <fmt:message key="WF.derivates.delete_file" var="fileDelete"/>
    											<fmt:message key="WF.derivates.delete_file.message" var="messageDeleteFile"/>
    											<button class="btn btn-sm btn-danger" data-toggle="collapse" data-target="#deleteFile_${derID}_${fn:replace(f, '.', '_')}" 
    													type="button">
    											        <i class="fas fa-trash"></i>
    											</button>
    											
  												 </div>
                                                </div>
                                                 <div class="row">
                                                  <div class="col">
  												<div id="deleteFile_${derID}_${fn:replace(f, '.', '_')}" class="collapse">
  													<div class="card-body border border-secondary bg-warning mt-1 mb-3">
  														<fmt:message key="WF.derivates.delete_file" var="fileDelete"/>
    													<fmt:message key="WF.derivates.delete_file.message" var="messageDeleteFile"/>
    													<button id="btnDeleteFile_${derID}_${f}" title="${fileDelete}" name="doDeleteFile-task_${it.taskid}-derivate_${derID}-file_${f}" 
    										       				class="btn btn-danger btn-sm ml-3">
    										        			<i class="fas fa-trash"></i><fmt:message key="WF.workspace.button.delete" />
    													</button>
  														<label class=ml-3><fmt:message key="WF.derivates.delete_file.message"><fmt:param>${f}</fmt:param></fmt:message></label>
  													</div>
  												</div>
                                                </div>
                                                </div>
  												</li>
    										</c:forEach>
    									</ul>
    								</div>
    								<div class="card-footer">
    									<div class="form-horizontal" role="form">
    										<div class="row">
    											<label for="inputAddFile_${derID}" class="col-2 col-form-label font-weight-bold"><fmt:message key="WF.derivates.label.upload_file"/></label>
    											<div class="col-8">
    												<fmt:message key="WF.derivates.file" var="file"/>
      												<input type="file" name="addFile_file-task_${it.taskid}-derivate_${derID}" class="form-control" style="height:auto" id="inputAddFile_${derID}" placeholder="${file}"></input>
    											</div>
    											<div class="col-2">
    												<fmt:message key="WF.derivates.upload" var="upload"/>
													<button type="submit" class="btn btn-primary" name="doAddFile-task_${it.taskid}-derivate_${derID}">${upload}</button>
												</div>
  											</div>
										</div>
  									</div>
  								</div>
    					</x:forEach>
    				</div>
    				<div class="card-footer">
    					<div class="row">
  							<div class="col-2" style="margin-top:0px"><h4><fmt:message key="WF.derivates.new_derivate"/></h4></div>
  							<div class="col-8">
  									<div class="form-group row">
    									<label for="inputLabel" class="col-sm-2 col-form-label text-right"><fmt:message key="WF.derivates.label"/></label>
   										<div class="col-8">
      										<select class="form-control" name="newDerivate_label-task_${it.taskid}">
  												<c:forEach var="entry" items="${it.derivateLabels}">
                                              	  <c:if test="${entry.key eq derLabel}">
                                                    <option value="${entry.key}" selected="selected">${entry.value}</option>
                                                  </c:if>
                                                  <c:if test="${not(entry.key eq derLabel)}">
                                                    <option value="${entry.key}">${entry.value}</option>
                                                </c:if>
                                            </c:forEach>
  											</select>
    									</div>
                                        <div class="col-2">
                                          <button class="btn btn-primary" name="doCreateNewDerivate-task_${it.taskid}">Erstellen</button>
                                       </div>
  									</div>
  									<c:if test="${fn:contains(objID,'_person_')}">
  									   <div class="form-group row">
    									 <label for="inputTitle" class="col-2 col-form-label text-right"><fmt:message key="WF.derivates.title"/></label>
    									 <div class="col-8">
    										<fmt:message key="WF.derivates.title" var="title"/>
      										<input type="text" name="newDerivate_title-task_${it.taskid}" class="form-control" id="inputTitle" placeholder="${title}"></input>
    									 </div>
  									   </div>
  									</c:if>
  									<div class="form-group row">
    									<label for="inputFile" class="col-2 col-form-label text-right"><fmt:message key="WF.derivates.file"/></label>
    									<div class="col-8">
    										<fmt:message key="WF.derivates.title" var="file"/>
      										<input type="file" name="newDerivate_file-task_${it.taskid}" class="form-control" style="height:auto" id="inputFile" placeholder="${file}"></input>
    									</div>
  									</div>
  								</div>
							</div>
    				</div>
				</div>
			</form>
		 </div>
       </div>
    </div>
    <div style="height: 75px;">&nbsp;</div>
  <%@ include file="../fragments/footer.jspf" %>
</body>
</html>
