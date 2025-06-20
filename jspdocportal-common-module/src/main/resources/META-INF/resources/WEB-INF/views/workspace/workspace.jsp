<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@page import="org.mycore.common.config.MCRConfiguration2"%>
<%@page import="org.camunda.bpm.engine.task.Task"%>
<%@page import="org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr"%>
<%@page import="org.mycore.frontend.servlets.MCRServlet"%>
<%@page import="org.mycore.common.MCRSessionMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>

<fmt:message var="pageTitle" key="WF.workspace" />

<!doctype html>
<html>
  <head>
    <title>${pageTitle} @ <fmt:message key="Nav.Application" /></title>
    <%@ include file="../fragments/html_head.jspf"%>
  </head>
  <body>
    <%@ include file="../fragments/header.jspf"%>
    <form method="post" id="workspaceForm" accept-charset="UTF-8"
          action="${applicationScope.WebApplicationBaseURL}do/workspace/tasks">
    <div id="content_area">
      <div id="workspace" class="container">
        <div class="row">
          <div class="col workspace-content">
            <div class="float-end">
              <div class="dropdown">
                <button class="btn btn-primary dropdown-toggle" type="button" id="dropdownNewActionsMenu"
                        data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                  <fmt:message key="WF.workspace.info.menu.new_task" />
                </button>
                <div class="dropdown-menu dropdown-menu-right" aria-labelledby="dropdownNewActionsMenu">
                  <c:forEach var="base" items="${it.newActions}">
                    <button class="dropdown-item" type="submit" name="doCreateNewTask-${base.key}" value="doit">
                      <fmt:message key="WF.menu.new.action">
                        <fmt:param value="${base.value}"/>
                      </fmt:message>
                    </button>
                  </c:forEach>
                </div>
              </div>
            </div>
            
            <h2">
              <fmt:message key="WF.workspace.headline" />
            </h2>
          </div>
        </div>

        <div class="row">
          <div class="col">
            <c:set var="baseURL" value="${applicationScope.WebApplicationBaseURL}" />
            <c:forEach var="msg" items="${it.messages}">
              <div class="alert alert-warning ir-workflow-message">
                <c:out value="${msg}" escapeXml="false" />
              </div>
            </c:forEach>
            <div class="card border border-dark my-3 w-100">
              <div class="card-header text-bg-dark">
                <button class="btn btn-sm float-end btn-secondary mt-1" type="button" data-bs-toggle="collapse"
                        data-bs-target="#publish-dialog-task_${currentTask.executionId}">
                  <fmt:message key="WF.workspace.button.publish_all_objects" />
                </button>
                <h3 class="text-light my-1"><fmt:message key="WF.workspace.info.headline.claimed_tasks" /></h3>
              </div>
              <div id="publish-dialog-task_${currentTask.executionId}" class="collapse">
                <div class="card-body border border-secondary text-bg-warning">
                  <button name="doPublishAllTasks" value="" class="btn btn-primary btn-sm" type="submit">
                    <i class="fa fa-check-square-o"></i>
                    <fmt:message key="WF.workspace.button.publish_all" />
                  </button>
                  <label class="ms-3"><fmt:message key="WF.workspace.label.publish_all" /></label>
                </div>
              </div>
              <div class="card-body p-0">
                <c:forEach var="currentTask" items="${it.myTasks}">
                  <c:set var="currentVariables" value="${it.myVariables[currentTask.executionId]}" />
                  <c:choose>
                    <c:when test="${currentTask.name eq 'Objekt bearbeiten'}">
                      <div class="card border border-secondary m-3" id="task_${currentTask.executionId}" style="border-width:2px !important">
                        <div class="card-header">
                          <div class="row">
                            <div class="col-8">
                              <h5>
                                <span class="badge rounded-pill text-bg-secondary me-3" style="height: auto">${currentTask.executionId}</span>
                                ${currentVariables.wfHeadline} 
                                <span class="badge rounded-pill text-bg-primary ms-3" style="height: auto">${currentVariables.mcrObjectID}</span>
                                <c:if test="${not empty currentVariables.wfObjectDisplayRecordIdentifier}">
                                  <span class="badge rounded-pill text-bg-info ms-3" style="height: auto">${currentVariables.wfObjectDisplayRecordIdentifier}</span>
                                </c:if>
                              </h5>
                            </div>
                            <div class="col-4">
                              <button class="btn btn-sm btn-secondary float-end" name="doReleaseTask-task_${currentTask.executionId}">
                                <fmt:message key="WF.workspace.submit.task" />
                              </button>
                              <small class="text-start me-3 float-end">
                                <strong><fmt:message key="WF.workspace.start" /> </strong><br />
                                <fmt:formatDate pattern="dd.MM.yyyy HH:mm" value="${currentTask.createTime}" />
                              </small>
                            </div>
                          </div>
                        </div>

                        <div class="card-body  border-top border-bottom border-secondary">
                          <c:if test="${not empty currentVariables.validationMessage}">
                            <div class="alert alert-danger" role="alert">${currentVariables.validationMessage}</div>
                          </c:if>
                          <div class="row">
                            <div class="col-9">
                              <h4 class="mt-0">${currentVariables.wfObjectDisplayTitle}</h4>
                              <c:out value="${currentVariables.wfObjectDisplayDescription}" escapeXml="false" />
                            </div>
                            <div class="col-3 text-end">
                              <a href="${WebApplicationBaseURL}resolve/id/${currentVariables.mcrObjectID}?fromWF=true&anchor=task_${currentTask.executionId}"
                                class="btn btn-sm btn-outline-secondary"> 
                                <i class="far fa-newspaper"></i> <fmt:message key="WF.workspace.preview" />
                              </a>
                            </div>
                          </div>
                          <c:if test="${not empty currentVariables.wfObjectLicenceHTML}">
                            <div class="row">
                              <div class="col">
                                <c:out value="${currentVariables.wfObjectLicenceHTML}" escapeXml="false" />
                              </div>
                            </div>
                          </c:if>
                          <div class="row">
                            <div class="col">
                              <c:if test="${not empty currentVariables.wfObjectDisplayDerivateList}">
                                <c:out value="${currentVariables.wfObjectDisplayDerivateList}" escapeXml="false" />
                              </c:if>
                            </div>
                          </div>
                          <div class="row mt-3">
                            <div class="col">
                              <c:if test="${not fn:contains(currentVariables.mcrObjectID,'_person_')}">
                                <% pageContext.setAttribute("pica3URL", MCRConfiguration2.getString("MCR.Workflow.Pica3Import.URL").orElse("")); %>
                                <a id="workspace_button_pica3_import"
                                   href="${pica3URL}?urn=${currentVariables.wfObjectDisplayPersistentIdentifier}&recordIdentifier=${currentVariables.wfObjectDisplayRecordIdentifier}&mcrid=${currentVariables.mcrObjectID}"
                                   class="btn btn-sm btn-outline-secondary" target="_blank">
                                   <i class="fas fa-book"></i>
                                   <fmt:message key="WF.workspace.button.pica3" />
                                </a>
                                <button id="workspace_button_mods_from_opac" class="btn btn-sm btn-outline-secondary"
                                        type="button" data-bs-toggle="collapse"
                                        data-bs-target="#import_mods-dialog-task_${currentTask.executionId}">
                                  <i class="fas fa-download"></i>
                                  <fmt:message key="WF.workspace.button.mods_from_opac" />
                                </button>
                                <button id="workspace_button_edit_reservation"
                                  name="doEditReservation-task_${currentTask.executionId}-${currentVariables.mcrObjectID}"
                                  value="" class="btn btn-sm btn-outline-secondary" type="submit">
                                  <i class="fas fa-anchor"></i>
                                  <fmt:message key="WF.workspace.button.edit_reservation" />
                                </button>
                              </c:if>
                              <button id="workspace_button_edit_metadata"
                                name="doEditObject-task_${currentTask.executionId}-${currentVariables.mcrObjectID}"
                                value="" class="btn btn-sm btn-outline-secondary" type="submit">
                                <i class="fas fa-tag"></i>
                                <fmt:message key="WF.workspace.button.edit_metadata" />
                              </button>
                            
                              <a id="workspace_button_edit_derivate"
                                 href="${applicationScope.WebApplicationBaseURL}do/workspace/derivates?taskid=${currentTask.executionId}&mcrobjid=${currentVariables.mcrObjectID}"
                                 class="btn btn-sm btn-outline-secondary"> 
                                 <i class="fas fa-file"></i> 
                                 <fmt:message key="WF.workspace.button.edit_derivate" />
                              </a>
                            </div>
                          </div>
                        </div>
                        <div id="import_mods-dialog-task_${currentTask.executionId}" class="collapse">
                          <div class="card-body border-bottom border-secondary text-bg-warning">
                            <button name="doImportMODS-task_${currentTask.executionId}-${currentVariables.mcrObjectID}"
                                    value="" class="btn btn-primary btn-sm" type="submit">
                              <i class="fa fa-download"></i>
                              <fmt:message key="WF.workspace.button.import" />
                            </button>
                            <label class="ms-3"><fmt:message key="WF.workspace.label.import" /></label>
                          </div>
                        </div>
                        <div class="card-footer">
                          <button name="doGoto-task_${currentTask.executionId}-edit_object.do_save" value=""
                                  class="btn btn-sm btn-primary" type="submit">
                            <i class="fa fa-check"></i>
                            <c:choose>
                              <c:when test="${fn:startsWith(currentVariables.wfMode, 'wf_register')}">
                                <fmt:message key="WF.workspace.button.save_reservation" />
                              </c:when>
                              <c:otherwise>
                                <fmt:message key="WF.workspace.button.publish" />
                              </c:otherwise>
                            </c:choose>                            
                          </button>
                          <button name="doGoto-task_${currentTask.executionId}-edit_object.do_cancel" value=""
                                  class="btn btn-sm btn-secondary" type="submit">
                            <i class="fa fa-times"></i>
                            <fmt:message key="WF.workspace.button.cancel" />
                          </button>
                          <button class="btn btn-danger btn-sm float-end" type="button" 
                                  data-bs-toggle="collapse" data-bs-target="#delete-dialog-task_${currentTask.executionId}">
                            <i class="fas fa-trash"></i>
                            <fmt:message key="WF.workspace.button.delete_object" />
                          </button>
                        </div>
                        <div id="delete-dialog-task_${currentTask.executionId}" class="collapse">
                          <div class="card-footer border border-primary">
                            <button name="doGoto-task_${currentTask.executionId}-edit_object.do_drop" value=""
                              class="btn btn-danger btn-sm" type="submit">
                              <i class="fas fa-trash"></i>
                              <fmt:message key="WF.workspace.button.delete" />
                            </button>
                            <label class="ms-3"><fmt:message key="WF.workspace.label.delete" /></label>
                          </div>
                        </div>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <p>Nothing ToDo for TASK: = ${task.name}</p>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
              </div>
            </div>

            <div class="card border border-dark my-3 w-100">
              <div class="card-header text-bg-dark">
                <h3 class="mb-0">
                  <fmt:message key="WF.workspace.info.headline.available_tasks" />
                </h3>
              </div>
              <div class="card-body p-0">
                <c:forEach var="currentTask" items="${it.availableTasks}">
                  <c:set var="currentVariables" value="${it.availableVariables[currentTask.executionId]}" />
                  <div class="card border border-secondary m-3" id="available_task_${currentTask.executionId}">
                    <div class="card-header">
                      <div class="row">
                        <div class="col-8">
                          <h5>
                             <span class="badge rounded-pill text-bg-secondary me-3" style="height: auto">${currentTask.executionId}</span>
                             ${currentVariables.wfHeadline} 
                             <span class="badge rounded-pill text-bg-primary ms-3" style="height: auto">${currentVariables.mcrObjectID}</span>
                          </h5>
                        </div>
                        <div class="col-4">
                          <button class="btn btn-secondary btn-sm float-end"
                                  name="doAcceptTask-task_${currentTask.executionId}">
                            <fmt:message key="WF.workspace.submit.accept_task" />
                          </button>
                          <small class="text-start me-3 float-end">
                            <strong><fmt:message key="WF.workspace.start" /> </strong><br />
                            <fmt:formatDate pattern="dd.MM.yyyy HH:mm" value="${currentTask.createTime}" />
                          </small>
                        </div>
                      </div>
                    </div>

                    <div class="card-body border-top border-secondary">
                      <div class="row">
                        <div class="col">
                          <c:if test="${not empty currentVariables.validationMessage}">
                            <div class="alert alert-danger" role="alert">${currentVariables.validationMessage}</div>
                          </c:if>
                          <h4 style="margin-top: 0px">${currentVariables.wfObjectDisplayTitle}</h4>
                          <c:out value="${currentVariables.wfObjectDisplayDescription}" escapeXml="false" />
                        </div>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>  
    </form>
    <%@ include file="../fragments/footer.jspf"%>
  </body>
</html>
