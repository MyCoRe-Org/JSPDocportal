package org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRExpandedObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public interface MCRWorkflowMgr {
    MCRExpandedObject createMCRObject(DelegateExecution execution);

    MCRExpandedObject loadMCRObject(DelegateExecution execution);

    MCRExpandedObject dropMCRObject(DelegateExecution execution);

    MCRDerivate createMCRDerivate(MCRObjectID owner, String label, String title);

    boolean deleteProcessInstance(String processInstanceId);

    boolean commitMCRObject(DelegateExecution execution);

    boolean rollbackMCRObject(DelegateExecution execution);

    boolean validateMCRObject(DelegateExecution execution);

    boolean cleanupWorkflow(DelegateExecution execution);
}
