package org.mycore.activiti.workflows.create_object_simple.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.mycore.activiti.MCRActivitiMgr;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;

public class MCRActivitiCreateObjectDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
        MCRWorkflowMgr wfm = MCRActivitiMgr.getWorkflowMgr(execution);
        wfm.createMCRObject(execution);
    }

}