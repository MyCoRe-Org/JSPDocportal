package org.mycore.activiti.workflows.create_object_simple.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;

public class MCRActivitiEndEventDelegate implements JavaDelegate {
    
    public void execute(DelegateExecution execution) throws Exception {
        MCRWorkflowMgr wfm = MCRBPMNMgr.getWorkflowMgr(execution);
        wfm.cleanupWorkflow(execution);
    }

}