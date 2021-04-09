package org.mycore.frontend.jsp.stripes.actions;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.activiti.MCRActivitiMgr;
import org.mycore.activiti.MCRActivitiUtils;
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
import org.mycore.frontend.jsp.stripes.actions.util.MCRMODSCatalogService;
import org.mycore.frontend.xeditor.MCREditorSession;
import org.mycore.frontend.xeditor.MCREditorSessionStore;
import org.mycore.frontend.xeditor.MCREditorSessionStoreUtils;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/showWorkspace.action")
public class ShowWorkspaceAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(ShowWorkspaceAction.class);

    ForwardResolution fwdResolution = new ForwardResolution("/content/workspace/workspace.jsp");

    private MCRMODSCatalogService modsCatService = (MCRMODSCatalogService) MCRConfiguration2
            .getInstanceOf("MCR.Workflow.MODSCatalogService.class").orElse(null);

    private List<String> messages = new ArrayList<String>();

    private String mode = "";

    private List<Task> myTasks = new ArrayList<Task>();

    private List<Task> availableTasks = new ArrayList<Task>();

    private String editorPath;

    private String sourceURI;

    private String cancelURL;

    public ShowWorkspaceAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("mode") != null) {
            mode = getContext().getRequest().getParameter("mode");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        if (mode == null) {
            return new RedirectResolution("/");
        }

        // open XEditor
        if (getContext().getRequest().getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM) != null) {
            String xEditorStepID = getContext().getRequest().getParameter(MCREditorSessionStore.XEDITOR_SESSION_PARAM);
            String sessionID = xEditorStepID.split("-")[0];
            MCREditorSession session = MCREditorSessionStoreUtils.getSessionStore().getSession(sessionID);

            if (session == null) {
                LOGGER.error("Editor session invalid !!!");
                // ToDo - Forward to error page
                // String msg = getErrorI18N("xeditor.error", "noSession", sessionID);
                try {
                    getContext().getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "EditorSession not found: " + sessionID);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
                return null;
            }

            String mcrID = session.getEditedXML().getRootElement().getAttributeValue("ID");
            return editObject(mcrID, null);
        }

        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            if (getContext().getRequest().getSession(false) == null
                    || !MCRUserManager.getCurrentUser().isUserInRole("adminwf-" + mode)) {
                return new RedirectResolution("/login.action");
            }
        }

        for (String s : getContext().getRequest().getParameterMap().keySet()) {
            if (s.equals("doPublishAllTasks")) {
                publishAllTasks();
            }
            if (s.startsWith("doCreateNewTask-")) {
                String mcrBase = s.substring(s.indexOf("-") + 1);
                createNewTask(mcrBase);
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
                followTransaction(taskID, transactionID);
            }

            // doEditObject-task_[ID]-[mcrObjID]
            if (s.startsWith("doEditObject-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);
                return editObject(mcrObjID, taskID);
            }

            // doEditDerivates-task_[ID]-[mcrObjID]
            if (s.startsWith("doEditDerivates-task_")) {
                String id = s.substring(s.indexOf("-") + 1);
                String taskID = id.substring(0, id.indexOf("-"));
                taskID = taskID.substring(taskID.indexOf("_") + 1);
                String mcrObjID = id.substring(id.indexOf("-") + 1);
                ForwardResolution res = new ForwardResolution(
                        "/editDerivates.action?taskid=" + taskID + "&mcrobjid=" + mcrObjID);
                return res;
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

            TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
            myTasks = ts.createTaskQuery().taskAssignee(user.getUserID())
                    .processVariableValueEquals(MCRActivitiMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();

            for (Task t : myTasks) {
                updateWFObjectMetadata(t.getId());
                updateWFDerivateList(t);
            }

            availableTasks = ts.createTaskQuery().taskCandidateUser(user.getUserID())
                    .processVariableValueEquals(MCRActivitiMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();

        }
        return fwdResolution;
    }

    public void createNewTask(String mcrBase) {
        if (mcrBase != null) {
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                String projectID = mcrBase.substring(0, mcrBase.indexOf("_"));
                String objectType = mcrBase.substring(mcrBase.indexOf("_") + 1);
                if (getContext().getRequest().getSession(false) != null
                        && MCRAccessManager.checkPermission("create-" + objectType)) {
                    Map<String, Object> variables = new HashMap<String, Object>();
                    variables.put(MCRActivitiMgr.WF_VAR_OBJECT_TYPE, objectType);
                    variables.put(MCRActivitiMgr.WF_VAR_PROJECT_ID, projectID);
                    variables.put(MCRActivitiMgr.WF_VAR_MODE, mode);

                    RuntimeService rs = MCRActivitiMgr.getWorfklowProcessEngine().getRuntimeService();
                    // ProcessInstance pi = rs.startProcessInstanceByKey("create_object_simple",
                    // variables);
                    ProcessInstance pi = rs.startProcessInstanceByMessage("start_create", variables);
                    TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
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
        for(Task t: ts.createTaskQuery().executionId(taskId).list()) {
            //should be only one item
            ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
        }
    }

    private void releaseTask(String taskId) {
        LOGGER.debug("Release Task" + taskId);
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        for(Task t: ts.createTaskQuery().executionId(taskId).list()) {
            //should be only one item
            ts.setAssignee(t.getId(), null);
        }
    }

    private Resolution editObject(String mcrID, String taskID) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        String objectType = mcrObjID.getTypeId();
        if (objectType.equals("thesis")) {
            objectType = "disshab";
        }
        editorPath = "/editor/metadata/editor-" + objectType + "-default.xed";
        Path wfFile = MCRActivitiUtils.getWorkflowObjectFile(mcrObjID);
        sourceURI = wfFile.toUri().toString();
        ForwardResolution res = new ForwardResolution("/content/editor/fullpageEditor.jsp");
        StringBuffer sbCancel = new StringBuffer(MCRFrontendUtil.getBaseURL() + "showWorkspace.action?");
        if (!mode.isEmpty()) {
            sbCancel.append("&mode=").append(mode);
        }
        if (taskID != null) {
            sbCancel.append("#task_").append(taskID);
        }
        cancelURL = sbCancel.toString();

        return res;
    }

    private void followTransaction(String taskId, String transactionID) {
        TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
        ts.setVariable(taskId, "goto", transactionID);

        if (transactionID.equals("edit_object.do_save")) {
            // Task t = ts.createTaskQuery().taskId(taskId).singleResult();
            updateWFObjectMetadata(taskId);
            String mcrid = String.valueOf(ts.getVariable(taskId, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID));
            String title = String.valueOf(ts.getVariable(taskId, MCRActivitiMgr.WF_VAR_DISPLAY_TITLE));
            String url = MCRFrontendUtil.getBaseURL() + "resolve/id/" + mcrid + "?_cache=clear";
            messages.add(MCRTranslation.translate("WF.messages.publish.completed", title, url, url));
        }
        ts.complete(taskId);
    }

    private void updateWFObjectMetadata(String taskId) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(MCRActivitiMgr.getWorfklowProcessEngine()
                .getTaskService().getVariable(taskId, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID)));
        if (mcrObjID == null) {
            LOGGER.error("WFObject could not be read.");
        }

        // Title
        MCRObject mcrObj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);
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
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_TITLE, txt);
        } else {
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_TITLE, MCRTranslation.translate("WF.common.newObject"));
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
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_DESCRIPTION, txt);
        } else {
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_DESCRIPTION, "");
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
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER, txt);
        } else {
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER, "");
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
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_RECORD_IDENTIFIER, txt);
        } else {
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_RECORD_IDENTIFIER, "");
        }

        // LicenceInfo
        MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                MCRActivitiMgr.WF_VAR_DISPLAY_LICENCE_HTML, "");
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
                    MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                            MCRActivitiMgr.WF_VAR_DISPLAY_LICENCE_HTML, sb.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(taskId,
                    MCRActivitiMgr.WF_VAR_DISPLAY_LICENCE_HTML, e.getMessage());
        }

    }

    private void updateWFDerivateList(Task t) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(MCRActivitiMgr.getWorfklowProcessEngine()
                .getTaskService().getVariable(t.getId(), MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID)));
        if (mcrObjID == null) {
            LOGGER.error("WFObject could not be read.");
        }

        MCRObject mcrObj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);
        StringWriter result = new StringWriter();
        if (mcrObj != null && mcrObj.getStructure().getDerivates().size() > 0) {
            Map<String, List<String>> derivateFiles = MCRActivitiUtils.getDerivateFiles(mcrObjID);
            for (MCRMetaLinkID derID : mcrObj.getStructure().getDerivates()) {
                result.append("<div class=\"row\">");
                result.append("\n  <div class=\"offset-1 col-3\">");
                result.append("<span class=\"badge badge-pill badge-secondary\">" + derID.getXLinkHref() + "</span>");
                result.append("\n  </div>");
                MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(mcrObjID,
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
                    result.append("<a href=\"" + MCRFrontendUtil.getBaseURL() + "wffile/" + mcrObjID.toString() + "/"
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
        MCRActivitiMgr.getWorfklowProcessEngine().getTaskService().setVariable(t.getId(),
                MCRActivitiMgr.WF_VAR_DISPLAY_DERIVATELIST, result.toString());

    }

    private void publishAllTasks() {
        MCRUser user = MCRUserManager.getCurrentUser();
        TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
        myTasks = ts.createTaskQuery().taskAssignee(user.getUserID())
                .processVariableValueEquals(MCRActivitiMgr.WF_VAR_MODE, mode).orderByTaskCreateTime().desc().list();

        for (Task t : myTasks) {
            followTransaction(t.getId(), "edit_object.do_save");
        }
    }

    private void importMODSFromGVK(String mcrID, String taskId) {
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        Path mcrFile = MCRActivitiUtils.getWorkflowObjectFile(mcrObjID);
        Document docJdom = MCRActivitiUtils.getWorkflowObjectXML(mcrObjID);
        modsCatService.updateWorkflowFile(mcrFile, docJdom);
        updateWFObjectMetadata(taskId);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        ;
    }

    public List<String> getNewObjectBases() {
        return MCRConfiguration2.getOrThrow("MCR.Workflow.NewObjectBases." + mode, MCRConfiguration2::splitValue)
                .collect(Collectors.toList());
    }

    public List<Task> getMyTasks() {
        return myTasks;
    }

    public List<Task> getAvailableTasks() {
        return availableTasks;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public ForwardResolution getFwdResolution() {
        return fwdResolution;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getEditorPath() {
        return editorPath;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public String getCancelURL() {
        return cancelURL;
    }
}
