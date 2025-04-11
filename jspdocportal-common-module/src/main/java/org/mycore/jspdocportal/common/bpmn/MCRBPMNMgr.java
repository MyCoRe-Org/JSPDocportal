package org.mycore.jspdocportal.common.bpmn;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.identity.MCRMyCoReIDMPlugin;
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

    public static final String WF_PROCESS_ENGINE_PROPERTIES_PREFIX = "MCR.Workflow.ProcessEngine.";

    private static ProcessEngine processEngine;

    /**
     * initializes Camunda Workflow processEngine from MyCoRe properties
     * see https://docs.camunda.org/manual/7.21/user-guide/spring-boot-integration/configuration/
     * for documentation, default values and other options
     * 
     * @return the configuration object
     */
    public static ProcessEngineConfiguration getWorkflowProcessEngineConfiguration() {
        try {
            Map<String, String> props = MCRConfiguration2.getSubPropertiesMap(WF_PROCESS_ENGINE_PROPERTIES_PREFIX);
            StandaloneProcessEngineConfiguration peConf
                = (StandaloneProcessEngineConfiguration) ProcessEngineConfiguration
                    .createStandaloneProcessEngineConfiguration();
            if (props.containsKey("JdbcDriver")) {
                peConf.setJdbcDriver(props.get("JdbcDriver"));
            }
            if (props.containsKey("JdbcUrl")) {
                peConf.setJdbcUrl(props.get("JdbcUrl"));
            }
            if (props.containsKey("JdbcUsername")) {
                peConf.setJdbcUsername(props.get("JdbcUsername"));
            }
            if (props.containsKey("JdbcPassword")) {
                peConf.setJdbcPassword(props.get("JdbcPassword"));
            }
            if (props.containsKey("DatabaseSchema")) {
                peConf.setDatabaseSchema(props.get("DatabaseSchema"));
                // databaseTablePrefix = databaseSchema + '.' 
                peConf.setDatabaseTablePrefix(props.get("DatabaseSchema") + ".");
            }

            // default values, currently no support to change via MyCoRe properties
            peConf.setHistoryTimeToLive(props.getOrDefault("HistoryTimeToLive", "P7D"));
            peConf.setDatabaseSchemaUpdate(props.getOrDefault("DatabaseSchemaUpdate", "true"));
            peConf.setJobExecutorActivate(false);

            peConf.getProcessEnginePlugins().add(new MCRMyCoReIDMPlugin());
            return peConf;
        } catch (Exception e) {
            throw new MCRException("Workflow Engine could not be configured", e);
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
            mgr = MCRConfiguration2.getInstanceOf(MCRWorkflowMgr.class, prop).orElseThrow();
        } catch (Exception e) {
            throw new MCRException("Could not instantiate MCRWorkflowMgr for " + prop, e);
        }

        return mgr;
    }

    public static void sendMail(List<InternetAddress> to, String subject, String body, List<InternetAddress> cc,
        List<InternetAddress> replyTo) {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        MCRConfiguration2.getString("MCR.Workflow.Email.MailServerSslProtocols")
            .ifPresent(p -> props.put("mail.smtp.ssl.protocols", p));

        props.put("mail.smtp.host", MCRConfiguration2.getString("MCR.Workflow.Email.MailServerHost").get());
        props.put("mail.smtp.port", MCRConfiguration2.getInt("MCR.Workflow.Email.MailServerPort").get());

        Authenticator auth = null;
        String user = MCRConfiguration2.getString("MCR.Workflow.Email.MailServerUsername").orElse("");
        String password = MCRConfiguration2.getString("MCR.Workflow.Email.MailServerPassword").orElse("");
        if (!user.isBlank() && !password.isBlank()) {
            props.put("mail.smtp.auth", "true");
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
        }
        //create the Session object
        Session session = Session.getInstance(props, auth);
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
