package org.mycore.activiti.workflows.create_object_simple;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public interface MCRWorkflowMgr {
    public MCRObject createMCRObject(DelegateExecution execution);

    public MCRObject loadMCRObject(DelegateExecution execution);

    public MCRObject dropMCRObject(DelegateExecution execution);

    public MCRDerivate createMCRDerivate(MCRObjectID owner, String label, String title);

    public boolean deleteProcessInstance(String processInstanceId);

    public boolean commitMCRObject(DelegateExecution execution);

    public boolean rollbackMCRObject(DelegateExecution execution);

    public boolean validateMCRObject(DelegateExecution execution);

    public boolean cleanupWorkflow(DelegateExecution execution);
}
