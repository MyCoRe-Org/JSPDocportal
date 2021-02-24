package org.mycore.jspdocportal.common.controller;

import java.net.URI;
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
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
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
    public Response defaultRes(@QueryParam("mcrid") String mcrid, @Context HttpServletRequest request) {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            if (request.getSession(false)==null  || !MCRAccessManager.checkPermission(mcrid, "writedb")) {
                return Response.temporaryRedirect(URI.create("/do/login")).build();
            }

            LOGGER.debug("Document MCRID = " + mcrid);

            if (mcrid != null) {
                if (MCRAccessManager.checkPermission(mcrid, "writedb")) {
                    MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
                    MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                    if (mcrObj.getService().getState().getID().equals("published") || mcrObj.getService().getState().getID().equals("deleted")) {
                    	String mode = retrieveModeFromMetadata(mcrObj);
                    	
                        Map<String, Object> variables = new HashMap<String, Object>();
                        variables.put(MCRBPMNMgr.WF_VAR_OBJECT_TYPE, mcrObjID.getTypeId());
                        variables.put(MCRBPMNMgr.WF_VAR_PROJECT_ID, mcrObjID.getProjectId());
                        variables.put(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObjID.toString());
                        variables.put(MCRBPMNMgr.WF_VAR_MODE, mode);                        
                        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
                        //ProcessInstance pi = rs.startProcessInstanceByKey("create_object_simple", variables);
                        ProcessInstance pi = rs.startProcessInstanceByMessage("start_load", variables);
                        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
                        for (Task t : ts.createTaskQuery().processInstanceId(pi.getId()).list()) {
                            ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
                        }
                        return Response.temporaryRedirect(URI.create("/do/workspace/tasks")).build();
                    }
                   
                }
            }
        }
        //TODO redirect to ERROR-Page
        return Response.temporaryRedirect(URI.create("/")).build();
    }
    
    private static String retrieveModeFromMetadata(MCRObject mcrObj) {
    	Document doc = mcrObj.createXML();
    	Map<String, String>modeChecks = MCRConfiguration2.getSubPropertiesMap("MCR.Workflow.RetrieveMode.");
    	
    	XPathFactory xpathFactory = XPathFactory.instance();
    	for(Map.Entry<String, String> entry: modeChecks.entrySet()) {
    		XPathExpression<Object> xpCheck =  xpathFactory.compile(entry.getValue(), Filters.fpassthrough(), null, MCRConstants.MODS_NAMESPACE);
    		if(xpCheck.evaluateFirst(doc)!=null) {
    			return entry.getKey().substring(entry.getKey().lastIndexOf(".")+1);
    		}
    	}
    	throw new MCRException("Pleae provide a property \"MCR.Workflow.RetrieveMode.{mode}\" with an XPath, that maps the current MyCoRe object");
    }
}
