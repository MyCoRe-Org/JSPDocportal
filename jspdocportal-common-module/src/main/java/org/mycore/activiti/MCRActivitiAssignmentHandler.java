package org.mycore.activiti;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;

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
 * @author Robert Stephan
 *
 */
public class MCRActivitiAssignmentHandler implements TaskListener {
    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = LogManager.getLogger(MCRActivitiAssignmentHandler.class);

    public void notify(DelegateTask delegateTask) {
        String mode = String.valueOf(delegateTask.getVariable(MCRActivitiMgr.WF_VAR_MODE));
        
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
