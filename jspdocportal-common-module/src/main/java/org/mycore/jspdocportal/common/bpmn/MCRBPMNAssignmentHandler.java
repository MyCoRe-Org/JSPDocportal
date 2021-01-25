package org.mycore.jspdocportal.common.bpmn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;

/**
 * MCRActivitiAssignmentHandler assigns the proper users and groups to the given task
 * by looking them up in MyCoRe properties
 * 
 * they are defined as follows:
 * groups:	
 * 		"MCR.Activiti.TaskAssignment.CandidateGroups."+taskID+"."+wfMode
 * 		e.g.: MCR.Activiti.TaskAssignment.CandidateGroups.edit_object.professorum=editProfessorum
 * 
 * users:
 * 		"MCR.Activiti.TaskAssignment.CandidateUsers."+taskID+"."+wfMode
 * 		e.g.: MCR.Activiti.TaskAssignment.CandidateUsers.edit_object.professorum=administrator
 * 
 * It is configured as TaskListener in BPMN model file:
 *  <userTask id="edit_object" name="Objekt bearbeiten">
 *    <extensionElements>
 *      <camunda:taskListener class="org.mycore.jspdocportal.common.bpmn.MCRBPMNAssignmentHandler" event="create" />
 *    </extensionElements>
 *    ...
 *  </userTask>  
 * 
 *  @author Robert Stephan
 *  
 */
public class MCRBPMNAssignmentHandler implements TaskListener {
    
    private static Logger LOGGER = LogManager.getLogger(MCRBPMNAssignmentHandler.class);

    public void notify(DelegateTask delegateTask) {
        String mode = String.valueOf(delegateTask.getVariable(MCRBPMNMgr.WF_VAR_MODE));
        
        String wfID = delegateTask.getProcessDefinitionId().split(":")[0];

        String propKeyGrp = "MCR.Activiti.TaskAssignment.CandidateGroups." + wfID + "." + mode;
        List<String> groups = MCRConfiguration2.getString(propKeyGrp).map(MCRConfiguration2::splitValue)
                .map(s -> s.collect(Collectors.toList())).orElse(Collections.emptyList());
        for (String g : groups) {
            delegateTask.addCandidateGroup(g.trim());
        }
        String propKeyUser = "MCR.Activiti.TaskAssignment.CandidateUsers." + wfID + "." + mode;
        List<String> users = MCRConfiguration2.getString(propKeyUser).map(MCRConfiguration2::splitValue)
                .map(s -> s.collect(Collectors.toList())).orElse(Collections.emptyList());
        for (String u : users) {
            delegateTask.addCandidateUser(u.trim());
        }
        
        if (groups.size()==0 && users.size()==0) {
            LOGGER.error("Please define candidate users or groups for the following workflow: "
                    + delegateTask.getProcessDefinitionId().split(":")[0]);
            LOGGER.error("Set at least one of the following properties: " + propKeyGrp + " or " + propKeyUser + ".");
        }
    }
}
