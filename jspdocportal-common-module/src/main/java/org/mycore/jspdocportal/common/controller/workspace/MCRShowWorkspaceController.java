package org.mycore.jspdocportal.common.controller.workspace;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRClassTools;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
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
import org.mycore.frontend.xeditor.MCREditorSession;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCREditorSessionStoreUtils;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/do/workspace/tasks")
public class MCRShowWorkspaceController {
    private static Logger LOGGER = LogManager.getLogger(MCRShowWorkspaceController.class);

    private MCRMODSCatalogService modsCatService = (MCRMODSCatalogService) MCRConfiguration2
        .getInstanceOf("MCR.Workflow.MODSCatalogService.class").orElse(null);

    @POST
    public Response submitForm(@Context HttpServletRequest request) {
        return defaultRes(request);
    }

    @GET
    public Response defaultRes(@Context HttpServletRequest request) {

        HashMap<String, Object> model = new HashMap<String, Object>();
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
            return editObject(mcrID, null);
        }

        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            if (request.getSession(false) == null
                || !MCRUserManager.getCurrentUser().isUserInRole("edit")) {
                return Response.temporaryRedirect(URI.create(request.getContextPath() + "/do/login")).build();
            }
        }

        for (Object o : request.getParameterMap().keySet()) {
            String s = o.toString();
            if (s.equals("doPublishAllTasks")) {
                publishAllTasks(messages);
            }
            if (s.startsWith("doCreateNewTask-")) {
                String parts[] = s.split("-");
                createNewTask(parts[1], parts[2], request, messages);
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
                return editObject(mcrObjID, taskID);
            }

            // doEditReservation-task_[ID]-[mcrObjID]
            if (s.startsWith("doEditReservation-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);
                return editReservation(mcrObjID, taskID);
            }

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
                .orderByTaskCreateTime().desc().list();
            model.put("myTasks", myTasks);

            Map<String, Object> myVariables = new HashMap<>();
            for (Task t : myTasks) {
                updateWFObjectMetadata(t);
                updateWFDerivateList(t);
                myVariables.put(t.getExecutionId(),
                    MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService().getVariables(t.getExecutionId()));
            }
            model.put("myVariables", myVariables);

            List<Task> availableTasks = ts.createTaskQuery().taskCandidateUser(user.getUserID())
                .orderByTaskCreateTime().desc().list();
            model.put("availableTasks", availableTasks);

            Map<String, Object> availableVariables = new HashMap<>();
            for (Task t : availableTasks) {
                availableVariables.put(t.getExecutionId(),
                    MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService().getVariables(t.getExecutionId()));
            }
            model.put("availableVariables", availableVariables);
        }

        LinkedHashMap<String, String> newActions = new LinkedHashMap<>();
        Set<String> roles = new TreeSet<String>(MCRUserManager.getCurrentUser().getSystemRoleIDs());
        for (String role : roles) {
            if (role.startsWith("wf_")) {
                newActions.put(role,
                    MCRCategoryDAOFactory.getInstance().getCategory(new MCRCategoryID("mcr-roles", role), 0)
                        .getCurrentLabel()
                        .orElse(new MCRLabel(MCRSessionMgr.getCurrentSession().getLocale().getLanguage(),
                            "??" + role + "??", ""))
                        .getText());
            }
        }
        model.put("newActions", newActions);

        Viewable v = new Viewable("/workspace/workspace", model);
        return Response.ok(v).build();
    }

    private void createNewTask(String mode, String objectType, HttpServletRequest request, List<String> messages) {
        if (objectType != null) {
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                String projectID = MCRConfiguration2.getStringOrThrow("MCR.SWF.Project.ID");
                if (request.getSession(false) != null) {
                    Map<String, Object> variables = new HashMap<String, Object>();
                    variables.put(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, objectType);
                    variables.put(MCRBPMNMgr.WF_VAR_PROJECT_ID, projectID);
                    variables.put(MCRBPMNMgr.WF_VAR_MODE, mode);
                    String role = mode + "-" + objectType;
                    variables.put(MCRBPMNMgr.WF_VAR_HEADLINE,
                        MCRCategoryDAOFactory.getInstance().getCategory(new MCRCategoryID("mcr-roles", role), 0)
                            .getCurrentLabel()
                            .orElse(new MCRLabel(MCRSessionMgr.getCurrentSession().getLocale().getLanguage(),
                                "??" + role + "??", ""))
                            .getText());

                    RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
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
        for (Task t : ts.createTaskQuery().executionId(taskId).list()) {
            //should be only one item
            ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
        }
    }

    private void releaseTask(String taskId) {
        LOGGER.debug("Release Task" + taskId);
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        for (Task t : ts.createTaskQuery().executionId(taskId).list()) {
            //should be only one item
            ts.setAssignee(t.getId(), null);
        }
    }

    private Response editObject(String mcrID, String taskID) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);

        HashMap<String, Object> model = new HashMap<String, Object>();
        Viewable v = new Viewable("/workspace/fullpageEditor", model);

        Path wfFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        String sourceURI = wfFile.toUri().toString();
        //String preprocessor = MCRConfiguration2.getString("MCR.Workflow.Metadata.Editor.PreProcessorXSL.create_object_simple").orElse(null);
        //if(preprocessor!=null) {
        //    sourceURI = "xslStyle:"+preprocessor+":" + sourceURI;
        //}
        String preprocessor = MCRConfiguration2
            .getString("MCR.Workflow.Metadata.Editor.PreProcessorTransformer.create_object_simple").orElse(null);

        if (preprocessor != null) {
            sourceURI = "xslTransform:" + preprocessor + ":" + sourceURI;
        }
        model.put("sourceURI", sourceURI);

        StringBuffer sbCancel = new StringBuffer(MCRFrontendUtil.getBaseURL() + "do/workspace/tasks");
        if (taskID != null) {
            sbCancel.append("#task_").append(taskID);
        }
        model.put("cancelURL", sbCancel.toString());

        //MCR.Workflow.Meta.dataEditor.Path.create_object_simple.wf_register_data

        
        if(taskID==null) {
            TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
            Task t = ts.createTaskQuery().processVariableValueEquals(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrID).singleResult();
            taskID = t.getExecutionId();
        }
        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
        String mode = ((StringValue) rs.getVariableLocalTyped(taskID, MCRBPMNMgr.WF_VAR_MODE)).getValue();

        String propKey = "MCR.Workflow.Metadata.Editor.Path.create_object_simple." + mode;
        String editorPath = MCRConfiguration2.getStringOrThrow(propKey);
        model.put("editorPath", editorPath);

        return Response.ok(v).build();
    }

    private Response editReservation(String mcrID, String taskID) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);

        HashMap<String, Object> model = new HashMap<String, Object>();
        Viewable v = new Viewable("/workspace/fullpageEditor", model);

        Path wfFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        String sourceURI = wfFile.toUri().toString();
        model.put("sourceURI", sourceURI);

        StringBuffer sbCancel = new StringBuffer(MCRFrontendUtil.getBaseURL() + "do/workspace/tasks");
        if (taskID != null) {
            sbCancel.append("#task_").append(taskID);
        }
        model.put("cancelURL", sbCancel.toString());

        //MCR.Workflow.Reservation.Editor.Path.create_object_simple.wf_register_data

        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
        String mode = ((StringValue) rs.getVariableLocalTyped(taskID, MCRBPMNMgr.WF_VAR_MODE)).getValue();

        LOGGER.debug(
            "ID: " + MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService().getActivityInstance(taskID).getId());

        String propKey = "MCR.Workflow.Reservation.Editor.Path.create_object_simple." + mode;
        String editorPath = MCRConfiguration2.getStringOrThrow(propKey);
        model.put("editorPath", editorPath);

        return Response.ok(v).build();
    }

    private void followTransaction(String executionId, String transactionID, List<String> messages) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        Task t = ts.createTaskQuery().executionId(executionId).list().get(0);
        ts.setVariable(t.getId(), "goto", transactionID);
        if (transactionID.equals("edit_object.do_save")) {
            // Task t = ts.createTaskQuery().taskId(taskId).singleResult();
            updateWFObjectMetadata(t);
            String mcrid = String.valueOf(ts.getVariable(t.getId(), MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
            String title = String.valueOf(ts.getVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_TITLE));
            String url = MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrid + "?_cache=clear";
            messages.add(MCRTranslation.translate("WF.messages.publish.completed", title, url, url));
        }
        ts.complete(t.getId());
    }

    private void updateWFObjectMetadata(Task t) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        String txt = null;
        MCRObjectID mcrObjID = null;
        MCRObject mcrObj;
        try {
            mcrObjID = MCRObjectID
                .getInstance(String.valueOf(ts.getVariable(t.getId(), MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
            if (mcrObjID == null) {
                LOGGER.error("WFObject could not be read.");
            }

            // Title
            mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);

            Class<? extends TransformerFactory> tfClass = MCRClassTools.forName("net.sf.saxon.TransformerFactoryImpl");
            MCRXSLTransformer xsltTitle = MCRXSLTransformer.getInstance(tfClass,
                MCRConfiguration2.getString("MCR.Workflow.MCRObject.Display.Title.XSL").orElseThrow());
            ByteArrayOutputStream baosTitle = new ByteArrayOutputStream();
            xsltTitle.transform(new MCRJDOMContent(mcrObj.createXML()), baosTitle);
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_TITLE, baosTitle.toString());

            MCRXSLTransformer xsltDescription = MCRXSLTransformer.getInstance(tfClass,
                MCRConfiguration2.getString("MCR.Workflow.MCRObject.Display.Description.XSL").orElseThrow());
            ByteArrayOutputStream baosDescription = new ByteArrayOutputStream();
            xsltDescription.transform(new MCRJDOMContent(mcrObj.createXML()), baosDescription);
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_DESCRIPTION, baosDescription.toString());

        } catch (Exception e) {
            LOGGER.error(e);
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE, e.getMessage());
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_TITLE, MCRTranslation.translate("WF.common.newObject"));
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_DESCRIPTION, "");
            return;
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
        ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_RECORD_IDENTIFIER, StringUtils.defaultString(txt, ""));

        //Persistent Identifier (URN für Pica)
        try {
            String xpPI = "concat(//mods:mods/mods:identifier[@type='urn'],'')";
            XPathExpression<String> xpath = XPathFactory.instance().compile(xpPI, Filters.fstring(), null,
                MCRConstants.MODS_NAMESPACE);
            txt = xpath.evaluateFirst(mcrObj.createXML());
        } catch (Exception e) {
            LOGGER.error(e);
            txt = e.getMessage();
        }
        ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER, StringUtils.defaultString(txt, ""));

        // LicenceInfo ... TODO MOVE TO XSLT for Description
        ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, "");
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
                    ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, sb.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_LICENCE_HTML, e.getMessage());
        }

    }

    //TODO: -> JSP-Tag oder Model-Object + HTML im Frontend
    private void updateWFDerivateList(Task t) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        MCRObjectID mcrObjID = null;
        try {
            mcrObjID = MCRObjectID
                .getInstance(String.valueOf(ts.getVariable(t.getId(), MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
            if (mcrObjID == null) {
                LOGGER.error("WFObject could not be read.");
            }
        } catch (Exception e) {
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE, e.getMessage());
            return;
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
                if(der==null) {
                    result.append("\n  <div class=\"alert alert-danger\" role=\"alert\">");
                    result.append("\n    " + MCRTranslation.translate("Editor.Common.derivate.error"));
                    result.append("\n  </div>");
                } else {
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
                }
                result.append("\n  </div>"); // col
                result.append("\n</div>"); // row
            }
        }
        ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_DISPLAY_DERIVATELIST, result.toString());

    }

    private void publishAllTasks(List<String> messages) {
        MCRUser user = MCRUserManager.getCurrentUser();
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        List<Task> myTasks = ts.createTaskQuery().taskAssignee(user.getUserID())
            .orderByTaskCreateTime().desc().list();

        for (Task t : myTasks) {
            followTransaction(t.getId(), "edit_object.do_save", messages);
        }
    }

    private void importMODSFromGVK(String mcrID, String executionId) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        Task t = ts.createTaskQuery().executionId(executionId).list().get(0);
        ts.removeVariable(t.getId(), MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE);

        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        Path mcrFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        Document docJdom = MCRBPMNUtils.getWorkflowObjectXML(mcrObjID);
        try {
            modsCatService.updateWorkflowFile(mcrFile, docJdom);
        } catch (Exception e) {
            ts.setVariable(t.getId(), MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE, e.getMessage());
            LOGGER.error(e);
        }

        updateWFObjectMetadata(t);
    }

}
