package org.mycore.frontend.jsp.stripes.actions;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.activiti.MCRActivitiMgr;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.user2.MCRUserManager;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/startedit.action")
public class StartEditAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(StartEditAction.class);

    private String mcrid = null;

    public StartEditAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("mcrid") != null) {
            mcrid = getContext().getRequest().getParameter("mcrid");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            if (getContext().getRequest().getSession(false)==null  || !MCRAccessManager.checkPermission(mcrid, "writedb")) {
                return new RedirectResolution("/login.action");
            }

            LOGGER.debug("Document MCRID = " + mcrid);

            if (mcrid != null) {
                if (MCRAccessManager.checkPermission(mcrid, "writedb")) {
                    MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrid);
                    MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                    if (mcrObj.getService().getState().getID().equals("published")) {
                        Map<String, Object> variables = new HashMap<String, Object>();
                        variables.put(MCRActivitiMgr.WF_VAR_OBJECT_TYPE, mcrObjID.getTypeId());
                        variables.put(MCRActivitiMgr.WF_VAR_PROJECT_ID, mcrObjID.getProjectId());
                        variables.put(MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, mcrObjID.toString());

                        RuntimeService rs = MCRActivitiMgr.getWorfklowProcessEngine().getRuntimeService();
                        //ProcessInstance pi = rs.startProcessInstanceByKey("create_object_simple", variables);
                        ProcessInstance pi = rs.startProcessInstanceByMessage("start_load", variables);
                        TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
                        for (Task t : ts.createTaskQuery().processInstanceId(pi.getId()).list()) {
                            ts.setAssignee(t.getId(), MCRUserManager.getCurrentUser().getUserID());
                        }
                    }
                    return new RedirectResolution("/showWorkspace.action?mcr_base=" + mcrObjID.getBase());
                }
            }
        }
        return null;
    }
}
