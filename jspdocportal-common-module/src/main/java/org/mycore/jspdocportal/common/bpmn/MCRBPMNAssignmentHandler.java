package org.mycore.jspdocportal.common.bpmn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.user2.MCRUserManager;

/**
 * MCRBPMNAssignmentHandler assigns the proper users and groups to the given task
 * by looking them up in MyCoRe properties
 * 
 * they are defined as follows:
 * groups:
 * 	    groupID build as workflowMode + "-" + doctype if the current user belongs to that group
 * users:
 * 		"MCR.Workflow.TaskAssignment.CandidateUsers."+workflowID+"."+workflowMode
 * 		e.g.: MCR.Workflow.TaskAssignment.CandidateUsers.edit_object.professorum=administrator
 * 
 * It is configured as TaskListener in BPMN model file:
 *  &lt;userTask id="edit_object" name="Objekt bearbeiten"&gt;
 *    &lt;extensionElements&gt;
 *      &lt;camunda:taskListener class="org.mycore.jspdocportal.common.bpmn.MCRBPMNAssignmentHandler" event="create" /&gt;
 *    &lt;/extensionElements&gt;
 *    ...
 *  &lt;/userTask&gt;
 * 
 *  @author Robert Stephan
 *  
 */
public class MCRBPMNAssignmentHandler implements TaskListener {

    private static Logger LOGGER = LogManager.getLogger(MCRBPMNAssignmentHandler.class);

    public void notify(DelegateTask delegateTask) {
        StringValue mode = delegateTask.getVariableTyped(MCRBPMNMgr.WF_VAR_MODE);
        StringValue objectType = delegateTask.getVariableTyped(MCRBPMNMgr.WF_VAR_OBJECT_TYPE);

        String candidateRole = mode.getValue() + "-" + objectType.getValue();

        String wfID = delegateTask.getProcessDefinitionId().split(":")[0];
        List<String> groups = new ArrayList<String>();
        for (String role : MCRUserManager.getCurrentUser().getSystemRoleIDs()) {
            if (role.equals(candidateRole)) {
                groups.add(role);
                delegateTask.addCandidateGroup(role);
            }
        }

        String propKeyUser = "MCR.Workflow.TaskAssignment.CandidateUsers." + wfID + "." + mode.getValue();
        List<String> users = MCRConfiguration2.getString(propKeyUser).map(MCRConfiguration2::splitValue)
            .map(s -> s.collect(Collectors.toList())).orElse(Collections.emptyList());
        for (String u : users) {
            delegateTask.addCandidateUser(u.trim());
        }

        if (groups.size() == 0 && users.size() == 0) {
            LOGGER.error("Please define candidate users or groups for the following workflow: " + wfID);
            LOGGER.error("For candidate users you may set the following property: " + propKeyUser);
        }
    }
}
