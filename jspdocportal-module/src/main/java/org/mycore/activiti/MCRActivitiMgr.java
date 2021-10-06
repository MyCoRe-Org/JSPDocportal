package org.mycore.activiti;

import java.io.File;
import java.io.FileInputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationDir;
import org.mycore.common.config.MCRConfigurationException;

public class MCRActivitiMgr {
	private static final Logger LOGGER = Logger.getLogger(MCRActivitiMgr.class);
	public static final String WF_VAR_PROJECT_ID = "projectID";
	public static final String WF_VAR_OBJECT_TYPE = "objectType";
	public static final String WF_VAR_MCR_OBJECT_ID = "mcrObjectID";
	public static final String WF_VAR_VALIDATION_RESULT = "validationResult";
	public static final String WF_VAR_VALIDATION_MESSAGE = "validationMessage";
	public static final String WF_VAR_DISPLAY_TITLE = "wfObjectDisplayTitle";
	public static final String WF_VAR_DISPLAY_DESCRIPTION = "wfObjectDisplayDescription";
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
		String projectID = String.valueOf(execution.getVariable(MCRActivitiMgr.WF_VAR_PROJECT_ID));
		String objectType = String.valueOf(execution.getVariable(MCRActivitiMgr.WF_VAR_OBJECT_TYPE));

		return getWorkflowMgr(projectID, objectType);
	}

	public static MCRWorkflowMgr getWorkflowMgr(String processInstanceId) {
		RuntimeService rs = MCRActivitiMgr.getWorfklowProcessEngine().getRuntimeService();

		String projectID = String.valueOf(rs.getVariable(processInstanceId, MCRActivitiMgr.WF_VAR_PROJECT_ID));
		String objectType = String.valueOf(rs.getVariable(processInstanceId, MCRActivitiMgr.WF_VAR_OBJECT_TYPE));

		// String objectType =
		// String.valueOf(processInstance.getProcessVariables().get(MCRActivitiMgr.WF_VAR_OBJECT_TYPE));

		return getWorkflowMgr(projectID, objectType);
	}

	private static MCRWorkflowMgr getWorkflowMgr(String projectID, String objectType) {
		MCRWorkflowMgr mgr = null;
		String prop = "";
		try {
			try {
				prop = "MCR.Activiti.WorkflowMgr.Class.create_object_simple." + projectID + "_" + objectType;
				mgr = (MCRWorkflowMgr) MCRConfiguration.instance().getInstanceOf(prop);
			} catch (MCRConfigurationException cnfe) {
				// use default;
				prop = "MCR.Activiti.WorkflowMgr.Class.create_object_simple.default_" + objectType;
				mgr = (MCRWorkflowMgr) MCRConfiguration.instance().getInstanceOf(prop);
			}
		} catch (ClassCastException cce) {
			throw new MCRException("Class Cast Exception - the specified MCRWorkflowMgr in property " + prop + " could not be casted to MCRWorkflowMgr", cce);
		}

		return mgr;
	}

	public static SimpleEmail createNewEmailFromConfig() {
		SimpleEmail email = new SimpleEmail();
		email.setCharset("UTF-8");
		MCRConfiguration config = MCRConfiguration.instance();

		String host = config.getString("MCR.Workflow.Email.MailServerHost");
		if (host == null) {
			LOGGER.error("Email is not configured!");
			return null;
		}
		email.setHostName(host);
		if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.MailServerPort"))) {
			try {
				email.setSmtpPort(config.getInt("MCR.Workflow.Email.MailServerPort"));
			} catch (NumberFormatException nfe) {
				LOGGER.debug(nfe);
			}
		}
		if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.MailServerUseSSL"))) {
			email.setSSL(config.getBoolean("MCR.Workflow.Email.MailServerUseSSL", false));
		}
		if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.MailServerUseTLS"))) {
			email.setTLS(config.getBoolean("MCR.Workflow.Email.MailServerUseTLS", false));
		}
		if (StringUtils.isNoneBlank(config.getString("MCR.Workflow.Email.MailServerUsername"), config.getString("MCR.Workflow.Email.MailServerPassword"))) {
			email.setAuthentication(config.getString("MCR.Workflow.Email.MailServerUsername"), config.getString("MCR.Workflow.Email.MailServerPassword"));
		}
		if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.MailServerUseTLS"))) {
			email.setTLS(config.getBoolean("MCR.Workflow.Email.MailServerUseTLS", false));
		}

		try {
			if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.From"))) {
				email.setFrom(config.getString("MCR.Workflow.Email.From"));
			}

			if (StringUtils.isNoneBlank(config.getString("MCR.Workflow.Email.From"), config.getString("MCR.Workflow.Email.Sender"))) {
				email.setFrom(config.getString("MCR.Workflow.Email.From"), config.getString("MCR.Workflow.Email.Sender"));
			}
			if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.CC"))) {
				for(String s: config.getString("MCR.Workflow.Email.CC").split(",")){
					email.addCc(s.trim());
				}
			}
			//add aditional property for tls (use recent SSL protocol)
			if(email.isTLS()) {
				email.getMailSession().getProperties().put("mail.smtp.starttls.required", "true");
				email.getMailSession().getProperties().put("mail.smtp.ssl.protocols", "TLSv1.2");
			}
		} catch (EmailException e) {
			LOGGER.error(e);
		}

		return email;
	}
}
