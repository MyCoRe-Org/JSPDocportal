package org.mycore.jspdocportal.common.controller.workspace;

import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.frontend.xeditor.MCREditorSession;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCREditorSessionStoreUtils;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

@javax.ws.rs.Path("/do/workspace/tasks")
public class MCRShowWorkspaceController {
    private static Logger LOGGER = LogManager.getLogger(MCRShowWorkspaceController.class);

    private MCRMODSCatalogService modsCatService = (MCRMODSCatalogService) MCRConfiguration2
            .getInstanceOf("MCR.Workflow.MODSCatalogService.class").orElse(null);

    @POST
    public Response submitForm(@FormParam("mode") @DefaultValue("") String mode,
        @Context HttpServletRequest request) {
        return defaultRes(mode, request);
    }
    
    @GET
    public Response defaultRes(@QueryParam("mode") @DefaultValue("") String mode,
        @Context HttpServletRequest request) {
        if (mode == null) {
            return Response.temporaryRedirect(URI.create("/")).build();
        }
        
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("mode", mode);
        
        List<String> messages = new ArrayList<String>();
        model.put("messages", messages);

        // open XEditor
        if (request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM) != null) {
            String xEditorStepID = request.getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
            String sessionID = xEditorStepID.split("-")[0];
            MCREditorSession session = MCREditorSessionStoreUtils.getSessionStore().getSession(sessionID);

            if (session == null) {
                LOGGER.error("Editor session invalid !!!");
                // ToDo - Forward to error page
                // String msg = getErrorI18N("xeditor.error", "noSession", sessionID);
                
                    //sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  "EditorSession not found: " + sessionID);
                return Response.serverError().build();
                
            }

            String mcrID = session.getEditedXML().getRootElement().getAttributeValue("ID");
            return editObject(mcrID, null, mode);
        }

        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            if (request.getSession(false) == null
                    || !MCRUserManager.getCurrentUser().isUserInRole("adminwf-" + mode)) {
                return Response.temporaryRedirect(URI.create("do/login")).build();
            }
        }

        for (Object o : request.getParameterMap().keySet()) {
            String s= o.toString();
            if (s.equals("doPublishAllTasks")) {
                publishAllTasks(mode, messages);
            }
            if (s.startsWith("doCreateNewTask-")) {
                String mcrBase = s.substring(s.indexOf("-") + 1);
                createNewTask(mcrBase, request, mode, messages);
            }
            if (s.startsWith("doAcceptTask-task_")) {
                String id = s.substring(s.indexOf("_") + 1);
                acceptTask(id);
            }
            if (s.startsWith("doReleaseTask-task_")) {
                String id = s.substring(s.indexOf("_") + 1);
                releaseTask(id);
            }
            // doFollowt-task_[ID]-[mcrObjID]
            if (s.startsWith("doGoto-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String transactionID = id.substring(id.indexOf("-") + 1);
                followTransaction(taskID, transactionID, messages);
            }

            // doEditObject-task_[ID]-[mcrObjID]
            if (s.startsWith("doEditObject-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);
                return editObject(mcrObjID, taskID, mode);
            }

            /*
             * GET-Request as URL in Front-end
             
            // doEditDerivates-task_[ID]-[mcrObjID]
            if (s.startsWith("doEditDerivates-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);
                return Response.temporaryRedirect(URI.create("do/workspace/derivates?taskid=" + taskID + "&mcrobjid=" + mcrObjID))
                    .entity(null)
                    .build();
            }
            */

            // doImportMODS-task_[ID]-[mcrObjID]
            if (s.startsWith("doImportMODS-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);

                importMODSFromGVK(mcrObjID, taskID);
            }
        }

        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            MCRUser user = MCRUserManager.getCurrentUser();

            TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
            List<Task> myTasks = ts.createTaskQuery().taskAssignee(user.getUserID())
                    .processVariableValueEquals(MCRBPMNMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();
            model.put("myTasks", myTasks);
            
            for (Task t : myTasks) {
                updateWFObjectMetadata(t.getId());
                updateWFDerivateList(t);
            }

            List<Task> availableTasks = ts.createTaskQuery().taskCandidateUser(user.getUserID())
                    .processVariableValueEquals(MCRBPMNMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();
            model.put("availableTasks", availableTasks);
        }

        model.put("newObjectBases",  MCRConfiguration2.getOrThrow("MCR.Workflow.NewObjectBases." + mode, MCRConfiguration2::splitValue)
                    .collect(Collectors.toList()));

        Viewable v = new Viewable("/workspace/workspace", model);
        return Response.ok(v).build();
    }

    public void createNewTask(String mcrBase, HttpServletRequest request, String mode, List<String> messages) {
        if (mcrBase != null) {
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                String projectID = mcrBase.substring(0, mcrBase.indexOf("_"));
                String objectType = mcrBase.substring(mcrBase.indexOf("_") + 1);
                if (request.getSession(false) != null
                        && MCRAccessManager.checkPermission("create-" + objectType)) {
                    Map<String, Object> variables = new HashMap<String, Object>();
                    variables.put(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, objectType);
                    variables.put(MCRBPMNMgr.WF_VAR_PROJECT_ID, projectID);
                    variables.put(MCRBPMNMgr.WF_VAR_MODE, mode);

                    RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
                    // ProcessInstance pi = rs.startProcessInstanceByKey("create_object_simple",
                    // variables);
                    ProcessInstance pi = rs.startProcessInstanceByMessage("start_create", variables);
                    TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
                    for (Task t : ts.createTaskQuery().processInstanceId(pi.getId()).list()) {
                        ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
                    }
                } else {
                    messages.add(MCRTranslation.translate("WF.messages.create.forbidden"));
                }
            }
        }
    }

    private void acceptTask(String taskId) {
        LOGGER.debug("Accepted Task" + taskId);
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        ts.setAssignee(taskId, MCRUserManager.getCurrentUser().getUserID());
    }

    private void releaseTask(String taskId) {
        LOGGER.debug("Release Task" + taskId);
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        ts.setAssignee(taskId, null);
    }

    private Response editObject(String mcrID, String taskID, String mode) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        String objectType = mcrObjID.getTypeId();
        if (objectType.equals("thesis")) {
            objectType = "disshab";
        }

        HashMap<String, Object> model = new HashMap<String, Object>();
        Viewable v = new Viewable("/workspace/fullpageEditor", model);
        
        Path wfFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        String sourceURI = wfFile.toUri().toString();
        model.put("sourceURI", sourceURI);        
        
        StringBuffer sbCancel = new StringBuffer(MCRFrontendUtil.getBaseURL() + "showWorkspace.action?");
        if (!mode.isEmpty()) {
            sbCancel.append("&mode=").append(mode);
        }
        if (taskID != null) {
            sbCancel.append("#task_").append(taskID);
        }
        String cancelURL = sbCancel.toString();
        model.put("cancelURL", cancelURL);
        
        String editorPath = "/editor/metadata/editor-" + objectType + "-default.xed";
        model.put("editorPath", editorPath);
        
        return Response.ok(v).build();
    }

    private void followTransaction(String taskId, String transactionID, List<String> messages) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        ts.setVariable(taskId, "goto", transactionID);

        if (transactionID.equals("edit_object.do_save")) {
            // Task t = ts.createTaskQuery().taskId(taskId).singleResult();
            updateWFObjectMetadata(taskId);
            String mcrid = String.valueOf(ts.getVariable(taskId, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
            String title = String.valueOf(ts.getVariable(taskId, MCRBPMNMgr.WF_VAR_DISPLAY_TITLE));
            String url = MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrid + "?_cache=clear";
            messages.add(MCRTranslation.translate("WF.messages.publish.completed", title, url, url));
        }
        ts.complete(taskId);
    }

    private void updateWFObjectMetadata(String taskId) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(MCRBPMNMgr.getWorfklowProcessEngine()
                .getTaskService().getVariable(taskId, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
        if (mcrObjID == null) {
            LOGGER.error("WFObject could not be read.");
        }

        // Title
        MCRObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);
        String txt = null;
        try {
            String xpTitle = MCRConfiguration2
                    .getString("MCR.Workflow.MCRObject.Display.Title.XPath." + mcrObjID.getBase())
                    .orElse(MCRConfiguration2
                            .getString("MCR.Workflow.MCRObject.Display.Title.XPath.default_" + mcrObjID.getTypeId())
                            .orElse("/mycoreobject/@ID"));
            XPathExpression<String> xpath = XPathFactory.instance().compile(xpTitle, Filters.fstring(), null,
                    MCRConstants.MODS_NAMESPACE);
            txt = xpath.evaluateFirst(mcrObj.createXML());
        } catch (Exception e) {
            LOGGER.error(e);
            txt = e.getMessage();
        }
        if (txt != null) {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_TITLE, txt);
        } else {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_TITLE, MCRTranslation.translate("Wf.common.newObject"));
        }

        // Description
        try {
            String xpDescr = MCRConfiguration2
                    .getString("MCR.Workflow.MCRObject.Display.Description.XPath." + mcrObjID.getBase())
                    .orElse(MCRConfiguration2
                            .getString(
                                    "MCR.Workflow.MCRObject.Display.Description.XPath.default_" + mcrObjID.getTypeId())
                            .orElse("/mycoreobject/@label"));
            XPathExpression<String> xpath = XPathFactory.instance().compile(xpDescr, Filters.fstring(), null,
                    MCRConstants.MODS_NAMESPACE);
            txt = xpath.evaluateFirst(mcrObj.createXML());
        } catch (Exception e) {
            LOGGER.error(e);
            txt = e.getMessage();
        }
        if (txt != null) {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_DESCRIPTION, txt);
        } else {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_DESCRIPTION, "");
        }

        // PersistentIdentifier
        try {
            String xpPI = MCRConfiguration2
                    .getString("MCR.Workflow.MCRObject.Display.PersistentIdentifier.XPath." + mcrObjID.getBase())
                    .orElse(MCRConfiguration2.getString(
                            "MCR.Workflow.MCRObject.Display.PersistentIdentifier.XPath.default_" + mcrObjID.getTypeId())
                            .orElse("/mycoreobject/@ID"));

            XPathExpression<String> xpath = XPathFactory.instance().compile(xpPI, Filters.fstring(), null,
                    MCRConstants.MODS_NAMESPACE);
            txt = xpath.evaluateFirst(mcrObj.createXML());
        } catch (Exception e) {
            LOGGER.error(e);
            txt = e.getMessage();
        }
        if (txt != null) {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER, txt);
        } else {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER, "");
        }

        // RecordIdentifier
        try {
            String xpPI = "concat(//mods:mods//mods:recordInfo/mods:recordIdentifier,'')";
            XPathExpression<String> xpath = XPathFactory.instance().compile(xpPI, Filters.fstring(), null,
                    MCRConstants.MODS_NAMESPACE);
            txt = xpath.evaluateFirst(mcrObj.createXML());
        } catch (Exception e) {
            LOGGER.error(e);
            txt = e.getMessage();
        }
        if (txt != null) {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_RECORD_IDENTIFIER, txt);
        } else {
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_RECORD_IDENTIFIER, "");
        }

        // LicenceInfo
        MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, "");
        String xpLic = "//mods:mods/mods:classification[contains(@valueURI, 'licenseinfo#work')]/@valueURI";
        XPathExpression<Attribute> xpathLic = XPathFactory.instance().compile(xpLic, Filters.attribute(), null,
                MCRConstants.MODS_NAMESPACE);
        try {
            Attribute attrLic = xpathLic.evaluateFirst(mcrObj.createXML());
            if (attrLic != null) {
                String licID = attrLic.getValue().substring(attrLic.getValue().indexOf("#") + 1);
                MCRCategory cat = MCRCategoryDAOFactory.getInstance()
                        .getCategory(MCRCategoryID.fromString("licenseinfo:" + licID), 0);
                if (cat != null) {
                    Optional<MCRLabel> optLabelIcon = cat.getLabel("x-icon");
                    Optional<MCRLabel> optLabelText = cat.getLabel("de");
                    StringBuffer sb = new StringBuffer();
                    sb.append("<table><tr><td colspan='3'>");
                    if (optLabelText.isPresent()) {
                        sb.append("<strong>").append(optLabelText.get().getText()).append("</strong>");
                    }
                    sb.append("</td></tr><tr><td>");
                    if (optLabelIcon.isPresent()) {
                        sb.append("<img src='" + MCRFrontendUtil.getBaseURL() + "images" + optLabelIcon.get().getText()
                                + "' />");
                    }
                    sb.append("</td><td>&nbsp;&nbsp;&nbsp;</td> <td style='text-align:justify'>");
                    if (optLabelText.isPresent()) {
                        sb.append(optLabelText.get().getDescription());
                    }
                    sb.append("</td></tr></table>");
                    MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                            MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, sb.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, e.getMessage());
        }

    }
    
    //TODO: -> JSP-Tag oder Model-Object + HTML im Frontend
    private void updateWFDerivateList(Task t) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(MCRBPMNMgr.getWorfklowProcessEngine()
                .getTaskService().getVariable(t.getId(), MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
        if (mcrObjID == null) {
            LOGGER.error("WFObject could not be read.");
        }

        MCRObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);
        StringWriter result = new StringWriter();
        if (mcrObj != null && mcrObj.getStructure().getDerivates().size() > 0) {
            Map<String, List<String>> derivateFiles = MCRBPMNUtils.getDerivateFiles(mcrObjID);
            for (MCRMetaLinkID derID : mcrObj.getStructure().getDerivates()) {
                result.append("<div class=\"row\">");
                result.append("\n  <div class=\"offset-1 col-3\">");
                result.append("<span class=\"badge badge-pill badge-secondary\">" + derID.getXLinkHref() + "</span>");
                result.append("\n  </div>");
                MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(mcrObjID,
                        derID.getXLinkHrefID());
                result.append("\n  <div class=\"col-8\">");
                if (!der.getDerivate().getClassifications().isEmpty()) {
                    result.append("\n    <strong>");
                    for (MCRMetaClassification c : der.getDerivate().getClassifications()) {
                        Optional<MCRLabel> oLabel = MCRCategoryDAOFactory.getInstance()
                                .getCategory(new MCRCategoryID(c.getClassId(), c.getCategId()), 0).getCurrentLabel();
                        if (oLabel.isPresent()) {
                            result.append("[").append(oLabel.get().getText()).append("] ");
                        }
                    }
                    result.append("</strong>");
                }
                for (MCRMetaLangText txt : der.getDerivate().getTitles()) {
                    result.append("<br />" + txt.getText());
                }
                result.append("\n    <ul style=\"list-style-type: none;\">");
                for (String fileName : derivateFiles.get(derID.getXLinkHref())) {
                    result.append("\n        <li>");
                    if (fileName.contains(".")) {
                        result.append("<i class=\"fa fa-file mr-3\"></i>");
                    } else {
                        result.append("<i class=\"fa fa-folder-open mr-3\"></i>");
                    }
                    result.append("<a href=\"" + MCRFrontendUtil.getBaseURL() + "do/wffile/" + mcrObjID.toString() + "/"
                            + der.getId().toString() + "/" + fileName + "\">" + fileName + "</a>");

                    if (fileName.equals(der.getDerivate().getInternals().getMainDoc())) {
                        result.append("<span class=\"ml-3 text-secondary\" class=\"fa fa-star\" title=\""
                                + MCRTranslation.translate("Editor.Common.derivate.maindoc") + "\"></span>");
                    }
                    result.append("\n    </li>");
                }
                result.append("\n    </ul>");
                result.append("\n  </div>"); // col
                result.append("\n</div>"); // row
            }
        }
        MCRBPMNMgr.getWorfklowProcessEngine().getTaskService().setVariable(t.getId(),
                MCRBPMNMgr.WF_VAR_DISPLAY_DERIVATELIST, result.toString());

    }

    private void publishAllTasks(String mode, List<String> messages) {
        MCRUser user = MCRUserManager.getCurrentUser();
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        List<Task> myTasks = ts.createTaskQuery().taskAssignee(user.getUserID())
                .processVariableValueEquals(MCRBPMNMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();

        for (Task t : myTasks) {
            followTransaction(t.getId(), "edit_object.do_save", messages);
        }
    }

    private void importMODSFromGVK(String mcrID, String taskId) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        Path mcrFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        Document docJdom = MCRBPMNUtils.getWorkflowObjectXML(mcrObjID);
        modsCatService.updateWorkflowFile(mcrFile, docJdom);
        updateWFObjectMetadata(taskId);
    }

}