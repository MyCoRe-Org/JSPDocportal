package org.mycore.jspdocportal.common.bpmn;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MCRBPMNMgr {
    @SuppressWarnings("unused")
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

    public static void sendMail(List<InternetAddress> to, String subject, String body, List<InternetAddress> cc,
        List<InternetAddress> replyTo) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        MCRConfiguration2.getString("MCR.Workflow.Email.MailServerSslProtocols")
            .ifPresent(p -> props.put("mail.smtp.ssl.protocols", p));

        props.put("mail.smtp.host", MCRConfiguration2.getString("MCR.Workflow.Email.MailServerHost").get());
        props.put("mail.smtp.port", MCRConfiguration2.getInt("MCR.Workflow.Email.MailServerPort").get());

        //create the Session object
        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        MCRConfiguration2.getString("MCR.Workflow.Email.MailServerUsername").get(),
                        MCRConfiguration2.getString("MCR.Workflow.Email.MailServerPassword").get());
                }
            });

        try {
            //create a MimeMessage object
            Message message = new MimeMessage(session);

            //set From:
            InternetAddress addrFrom = new InternetAddress();
            MCRConfiguration2.getString("MCR.Workflow.Email.From")
                .ifPresent(from -> addrFrom.setAddress(from));
            MCRConfiguration2.getString("MCR.Workflow.Email.Sender")
                .ifPresent(name -> {
                    try {
                        addrFrom.setPersonal(name, StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        // does not happen
                    }
                });
            message.setFrom(addrFrom);

            //set To:
            message.setRecipients(Message.RecipientType.TO, to.toArray(InternetAddress[]::new));

            //set CC:
            if (!cc.isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, cc.toArray(InternetAddress[]::new));
            }
            if (replyTo.isEmpty()) {
                message.setReplyTo(replyTo.toArray(InternetAddress[]::new));
            }

            //set email subject field
            message.setSubject(subject);

            //set the content of the email message
            message.setText(body);

            //send the email message
            Transport.send(message);

        } catch (MessagingException e) {
            throw new MCRException("Email could not be sent!", e);
        }
    }
}
