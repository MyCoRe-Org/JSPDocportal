package org.mycore.frontend.jsp.stripes.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.mycore.access.MCRAccessManager;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/adminWorkflowProcesses.action")
public class AdminWorkflowProcessesAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(AdminWorkflowProcessesAction.class);

    ForwardResolution fwdResolution = new ForwardResolution("/content/workspace/adminWorkflowProcesses.jsp");

    private List<String> messages = new ArrayList<String>();

    private String objectType = "";

    private String projectID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID");

    private List<ProcessInstance> runningProcesses = new ArrayList<ProcessInstance>();

    public AdminWorkflowProcessesAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("objectType") != null) {
            String type = getContext().getRequest().getParameter("objectType");
            if (MCRObjectID.isValidType(type)) {
                objectType = type;
            }
        }
        if (getContext().getRequest().getParameter("projectID") != null) {
            projectID = getContext().getRequest().getParameter("projectID");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            for (String s : getContext().getRequest().getParameterMap().keySet()) {
                if (s.startsWith("doDeleteProcess_")) {
                    String id = s.substring(s.indexOf("_") + 1);
                    deleteProcessInstance(id);
                }
            }

            if (MCRAccessManager.checkPermission("administrate-" + objectType)) {
                RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
                runningProcesses = rs.createProcessInstanceQuery()
                        .variableValueEquals(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, objectType)
                        .variableValueEquals(MCRBPMNMgr.WF_VAR_PROJECT_ID, projectID).orderByProcessInstanceId()
                        .desc().list();
            } else {
                messages.add("You don't have the Permission to delete a process instance");
            }
        }

        return fwdResolution;
    }

    private void deleteProcessInstance(String processInstanceId) {
        LOGGER.debug("Delete Process " + processInstanceId);
        MCRWorkflowMgr wfMgr = MCRBPMNMgr.getWorkflowMgr(processInstanceId);
        wfMgr.deleteProcessInstance(processInstanceId);
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public List<ProcessInstance> getRunningProcesses() {
        return runningProcesses;
    }
}
