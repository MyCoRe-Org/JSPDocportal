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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
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
			mgr = (MCRWorkflowMgr) MCRConfiguration.instance().getInstanceOf(prop);
		} catch (ClassCastException cce) {
			throw new MCRException("Class Cast Exception - the specified MCRWorkflowMgr in property " + prop
					+ " could not be casted to MCRWorkflowMgr", cce);
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
			email.setSSLOnConnect(config.getBoolean("MCR.Workflow.Email.MailServerUseSSL", false));
		}
		if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.MailServerUseTLS"))) {
			email.setStartTLSEnabled(config.getBoolean("MCR.Workflow.Email.MailServerUseTLS", false));
		}
		if (StringUtils.isNoneBlank(config.getString("MCR.Workflow.Email.MailServerUsername"),
				config.getString("MCR.Workflow.Email.MailServerPassword"))) {
			email.setAuthentication(config.getString("MCR.Workflow.Email.MailServerUsername"),
					config.getString("MCR.Workflow.Email.MailServerPassword"));
		}

		try {
			if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.From"))) {
				email.setFrom(config.getString("MCR.Workflow.Email.From"));
			}

			if (StringUtils.isNoneBlank(config.getString("MCR.Workflow.Email.From"),
					config.getString("MCR.Workflow.Email.Sender"))) {
				email.setFrom(config.getString("MCR.Workflow.Email.From"),
						config.getString("MCR.Workflow.Email.Sender"));
			}
			if (StringUtils.isNotBlank(config.getString("MCR.Workflow.Email.CC"))) {
				for (String s : config.getString("MCR.Workflow.Email.CC").split(",")) {
					email.addCc(s.trim());
				}
			}
			//add aditional property for tls (use recent SSL protocol)
			if(email.isStartTLSEnabled()) {
				email.setStartTLSRequired(true);
				email.getMailSession().getProperties().put("mail.smtp.ssl.protocols", "TLSv1.2");
			}
		} catch (EmailException e) {
			LOGGER.error(e);
		}

		return email;
	}
}
