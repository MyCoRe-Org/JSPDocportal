package org.mycore.jspdocportal.common.bpmn;

import java.util.Optional;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;

public class MCRBPMNMgr {
    private static final Logger LOGGER = LogManager.getLogger(MCRBPMNMgr.class);

    public static final String WF_VAR_MODE = "wfMode";
    
    public static final String WF_VAR_HEADLINE = "wfHeadline";

    public static final String WF_VAR_PROJECT_ID = "projectID";

    public static final String WF_VAR_OBJECT_TYPE = "objectType";

    public static final String WF_VAR_MCR_OBJECT_ID = "mcrObjectID";

    public static final String WF_VAR_VALIDATION_RESULT = "validationResult";

    public static final String WF_VAR_VALIDATION_MESSAGE = "validationMessage";

    public static final String WF_VAR_DISPLAY_TITLE = "wfObjectDisplayTitle";

    public static final String WF_VAR_DISPLAY_LICENCE_HTML = "wfObjectLicenceHTML";

    public static final String WF_VAR_DISPLAY_DESCRIPTION = "wfObjectDisplayDescription";

    public static final String WF_VAR_DISPLAY_PERSISTENT_IDENTIFIER = "wfObjectDisplayPersistentIdentifier";

    public static final String WF_VAR_DISPLAY_RECORD_IDENTIFIER = "wfObjectDisplayRecordIdentifier";

    public static final String WF_VAR_DISPLAY_DERIVATELIST = "wfObjectDisplayDerivateList";

    public static final String MCR_BPMN_CONFIG_FILE = "camunda.cfg.xml";

    private static ProcessEngine processEngine;

    public static ProcessEngineConfiguration getWorkflowProcessEngineConfiguration() {
        try {
            return ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(MCR_BPMN_CONFIG_FILE);

        } catch (Exception e) {
            throw new MCRException("Workflow Engine configuration not found", e);
        }
    }

    // Workflow Engine
    public static synchronized ProcessEngine getWorfklowProcessEngine() {
        if (processEngine == null) {
            ProcessEngineConfiguration cfg = getWorkflowProcessEngineConfiguration();
            processEngine = cfg.buildProcessEngine();
        }
        return processEngine;
    }

    public static MCRWorkflowMgr getWorkflowMgr(DelegateExecution execution) {
        String mode = String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MODE));
        return getWorkflowMgrForMode(mode);
    }

    public static MCRWorkflowMgr getWorkflowMgr(String processInstanceId) {
        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();

        String mode = String.valueOf(rs.getVariable(processInstanceId, MCRBPMNMgr.WF_VAR_MODE));
        return getWorkflowMgrForMode(mode);
    }

    private static MCRWorkflowMgr getWorkflowMgrForMode(String mode) {
        MCRWorkflowMgr mgr = null;
        String prop = "";
        try {
            prop = "MCR.Workflow.WorkflowMgr.Class.create_object_simple." + mode;
            mgr = (MCRWorkflowMgr) MCRConfiguration2.getInstanceOf(prop).orElseThrow();
        } catch (Exception e) {
            throw new MCRException("Could not instantiate MCRWorkflowMgr for " + prop, e);
        }

        return mgr;
    }

    public static SimpleEmail createNewEmailFromConfig() {
        SimpleEmail email = new SimpleEmail();
        email.setCharset("UTF-8");

        Optional<String> host = MCRConfiguration2.getString("MCR.Workflow.Email.MailServerHost");
        if (host.isEmpty()) {
            LOGGER.error("Email is not configured!");
            return null;
        }
        email.setHostName(host.get());
        Optional<Integer> port = MCRConfiguration2.getInt("MCR.Workflow.Email.MailServerPort");
        if (port.isPresent()) {
            try {
                email.setSmtpPort(port.get());
            } catch (NumberFormatException nfe) {
                LOGGER.debug(nfe);
            }
        }
        email.setSSLOnConnect(MCRConfiguration2.getBoolean("MCR.Workflow.Email.MailServerUseSSL").orElse(false));
        email.setStartTLSEnabled(MCRConfiguration2.getBoolean("MCR.Workflow.Email.MailServerUseTLS").orElse(false));
        email.setStartTLSRequired(MCRConfiguration2.getBoolean("MCR.Workflow.Email.MailServerUseTLS").orElse(false));
        
        Optional<String> user = MCRConfiguration2.getString("MCR.Workflow.Email.MailServerUsername");
        Optional<String> pw = MCRConfiguration2.getString("MailServerPassword");
        if (user.isPresent() && pw.isPresent()) {
            email.setAuthentication(user.get(), pw.get());
        }

        try {
            Optional<String> emailFrom = MCRConfiguration2.getString("MCR.Workflow.Email.From");
            if (emailFrom.isPresent()) {
                Optional<String> emailSender = MCRConfiguration2.getString("MCR.Workflow.Email.Sender");

                if (emailSender.isPresent()) {
                    email.setFrom(emailFrom.get(), emailSender.get());
                } else {
                    email.setFrom(emailFrom.get());
                }
            }
            Optional<String> emailCCs = MCRConfiguration2.getString("MCR.Workflow.Email.CC");
            if (emailCCs.isPresent()) {
                for (String s : emailCCs.get().split(",")) {
                    email.addCc(s.trim());
                }
            }
    		if(email.isStartTLSEnabled()) {
				email.getMailSession().getProperties().put("mail.smtp.ssl.protocols", "TLSv1.2");
			}
        } catch (EmailException e) {
            LOGGER.error(e);
        }

        return email;
    }
}
