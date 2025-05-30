package org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;

public class MCRBPMNEndEventDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        MCRWorkflowMgr wfm = MCRBPMNMgr.getWorkflowMgr(execution);
        wfm.cleanupWorkflow(execution);
    }

}
