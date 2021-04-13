package org.mycore.jspdocportal.common.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.user2.MCRUserManager;

@Path("/do/startedit")
public class MCRStartEditController  {
    private static Logger LOGGER = LogManager.getLogger(MCRStartEditController.class);

    @GET
    public Response defaultRes(@QueryParam("mcrid") String mcrid, @QueryParam("mode") String mode, @Context HttpServletRequest request) {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            if (request.getSession(false)==null  || !MCRAccessManager.checkPermission(mcrid, "writedb")) {
                return Response.temporaryRedirect(URI.create(request.getContextPath() + "/do/login")).build();
            }

            LOGGER.debug("Document MCRID = " + mcrid);

            if (mcrid != null) {
                if (MCRAccessManager.checkPermission(mcrid, "writedb")) {
                    MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
                    MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                    if (Arrays.asList("published", "deleted", "reserved").contains(mcrObj.getService().getState().getID())) {
                    	//String mode = retrieveModeFromMetadata(mcrObj);
                    	//TODO validate mode
                        Map<String, Object> variables = new HashMap<String, Object>();
                        variables.put(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, mcrObjID.getTypeId());
                        variables.put(MCRBPMNMgr.WF_VAR_PROJECT_ID, mcrObjID.getProjectId());
                        variables.put(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObjID.toString());
                        variables.put(MCRBPMNMgr.WF_VAR_MODE, mode);
                        String role = mode + "-" + mcrObjID.getTypeId();
                        variables.put(MCRBPMNMgr.WF_VAR_HEADLINE,
                            MCRCategoryDAOFactory.getInstance().getCategory(new MCRCategoryID("mcr-roles", role), 0)
                                .getCurrentLabel()
                                .orElse(new MCRLabel(MCRSessionMgr.getCurrentSession().getLocale().getLanguage(),
                                    "??" + role + "??", ""))
                                .getText());
                        
                        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
                        //ProcessInstance pi = rs.startProcessInstanceByKey("create_object_simple", variables);
                        ProcessInstance pi = rs.startProcessInstanceByMessage("start_load", variables);
                        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
                        for (Task t : ts.createTaskQuery().processInstanceId(pi.getId()).list()) {
                            ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
                        }
                        return Response.temporaryRedirect(URI.create(request.getContextPath() + "/do/workspace/tasks")).build();
                    }
                   
                }
            }
        }
        //TODO redirect to ERROR-Page
        return Response.temporaryRedirect(URI.create(request.getContextPath())).build();
    }
    
    //DEPRECATED: mode as input param
//    private static String retrieveModeFromMetadata(MCRObject mcrObj) {
//    	Document doc = mcrObj.createXML();
//    	Map<String, String>modeChecks = MCRConfiguration2.getSubPropertiesMap("MCR.Workflow.RetrieveMode.edit.");
//    	
//    	XPathFactory xpathFactory = XPathFactory.instance();
//    	for(Map.Entry<String, String> entry: modeChecks.entrySet()) {
//    		XPathExpression<Object> xpCheck =  xpathFactory.compile(entry.getValue(), Filters.fpassthrough(), null, MCRConstants.MODS_NAMESPACE);
//    		if(xpCheck.evaluateFirst(doc)!=null) {
//    			return entry.getKey().substring(entry.getKey().lastIndexOf(".")+1);
//    		}
//    	}
//    	throw new MCRException("Pleae provide a property \"MCR.Workflow.RetrieveMode.{mode}\" with an XPath, that maps the current MyCoRe object");
//    }
}
