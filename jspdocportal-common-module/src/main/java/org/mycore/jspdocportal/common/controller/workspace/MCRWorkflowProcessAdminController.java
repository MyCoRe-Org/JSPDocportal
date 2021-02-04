package org.mycore.jspdocportal.common.controller.workspace;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;

@Path("/do/workspace/administration")
public class MCRWorkflowProcessAdminController {
    private static Logger LOGGER = LogManager.getLogger(MCRWorkflowProcessAdminController.class);

    @POST
    public Response defaultRes(@QueryParam("objectType") String objectType,
        @Context HttpServletRequest request) {
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            for (Object o : request.getParameterMap().keySet()) {
                String s = o.toString();
                if (s.startsWith("doDeleteProcess_")) {
                    String id = s.substring(s.indexOf("_") + 1);
                    deleteProcessInstance(id);
                }
            }
        }
        return get(objectType);
    }

    @GET
    public Response get(@QueryParam("objectType") String objectType) {
        HashMap<String, Object> model = new HashMap<>();
        String projectID = MCRConfiguration2.getString("MCR.SWF.Project.ID").orElse("");
        model.put("projectID", projectID);
        model.put("objectType", objectType);

        if (MCRAccessManager.checkPermission("administrate-" + objectType)) {
            RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
            List<ProcessInstance> runningProcesses = rs.createProcessInstanceQuery()
                .variableValueEquals(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, objectType)
                .variableValueEquals(MCRBPMNMgr.WF_VAR_PROJECT_ID, projectID).orderByProcessInstanceId()
                .desc().list();
            model.put("runningProcesses", runningProcesses);

        } else {
            model.put("messages", List.of("You don't have the Permission to delete a process instance"));
        }

        Viewable v = new Viewable("/workspace/administration", model);
        return Response.ok(v).build();
    }

    private void deleteProcessInstance(String processInstanceId) {
        LOGGER.debug("Delete Process " + processInstanceId);
        MCRWorkflowMgr wfMgr = MCRBPMNMgr.getWorkflowMgr(processInstanceId);
        wfMgr.deleteProcessInstance(processInstanceId);
    }

}
