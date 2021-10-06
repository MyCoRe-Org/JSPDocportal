package org.mycore.activiti;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationDir;

public class MCRActivitiMgr {
	private static final Logger LOGGER = LogManager.getLogger(MCRActivitiMgr.class);
	public static final String WF_VAR_MODE = "wfMode";
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

	@Deprecated
	public static final String ACTIVITI_CONFIG_FILE = "/config/workflow/activiti.cfg.xml";
	public static final String MCR_ACTIVITI_CONFIG_FILE = "activiti.cfg.xml";

	private static ProcessEngine activitiProcessEngine;

	public static ProcessEngineConfiguration getWorkflowProcessEngineConfiguration() {
		File configFile = MCRConfigurationDir.getConfigFile(MCR_ACTIVITI_CONFIG_FILE);
		if (configFile != null && configFile.canRead()) {
			try (FileInputStream fis = new FileInputStream(configFile)) {
				return ProcessEngineConfiguration.createProcessEngineConfigurationFromInputStream(fis);
			} catch (Exception e) {
				LOGGER.warn("Error while loading activiti configuration for: " + configFile, e);
			}
		}

		//fallback - TODO cleanup
		return ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(ACTIVITI_CONFIG_FILE);
	}

	// Workflow Engine
	public static synchronized ProcessEngine getWorfklowProcessEngine() {
		if (activitiProcessEngine == null) {

			activitiProcessEngine = getWorkflowProcessEngineConfiguration().buildProcessEngine();
		}
		return activitiProcessEngine;
	}

	public static MCRWorkflowMgr getWorkflowMgr(DelegateExecution execution) {
		String mode = String.valueOf(execution.getVariable(MCRActivitiMgr.WF_VAR_MODE));
		return getWorkflowMgrForMode(mode);
	}

	public static MCRWorkflowMgr getWorkflowMgr(String processInstanceId) {
		RuntimeService rs = MCRActivitiMgr.getWorfklowProcessEngine().getRuntimeService();

		String mode = String.valueOf(rs.getVariable(processInstanceId, MCRActivitiMgr.WF_VAR_MODE));
		return getWorkflowMgrForMode(mode);
	}

	private static MCRWorkflowMgr getWorkflowMgrForMode(String mode) {
		MCRWorkflowMgr mgr = null;
		String prop = "";
		try {
			prop = "MCR.Activiti.WorkflowMgr.Class.create_object_simple." + mode;
			mgr = (MCRWorkflowMgr) MCRConfiguration2.getInstanceOf(prop).orElseThrow();
		} catch (Exception cce) {
			throw new MCRException("Class Cast Exception - the specified MCRWorkflowMgr in property " + prop
					+ " could not be casted to MCRWorkflowMgr", cce);
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
		if(user.isPresent() && pw.isPresent()) {
		    email.setAuthentication(user.get(), pw.get());
		}

		try {
		    Optional<String> emailFrom = MCRConfiguration2.getString("MCR.Workflow.Email.From");
		    if(emailFrom.isPresent()) {
		        Optional<String> emailSender = MCRConfiguration2.getString("MCR.Workflow.Email.Sender");
		        
		        if(emailSender.isPresent()) {
		            email.setFrom(emailFrom.get(), emailSender.get());
		        }
		        else {
		            email.setFrom(emailFrom.get());
		        }
			}
		    Optional<String> emailCCs = MCRConfiguration2.getString("MCR.Workflow.Email.CC");
			if (emailCCs.isPresent()) {
				for (String s : emailCCs.get().split(",")) {
					email.addCc(s.trim());
				}
			}
			//add aditional property for tls (use recent SSL protocol)
			if(email.isStartTLSEnabled()) {
				email.getMailSession().getProperties().put("mail.smtp.ssl.protocols", "TLSv1.2");
			}
		} catch (EmailException e) {
			LOGGER.error(e);
		}

		return email;
	}
}
