package org.mycore.frontend.jsp.stripes.actions;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.activiti.MCRActivitiMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.services.i18n.MCRTranslation;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/feedback.action")
public class SendFeedbackAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(SendFeedbackAction.class);

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    ForwardResolution fwdResolution = new ForwardResolution("/content/feedback.jsp");

    private String fromName;
    private String fromEmail;
    private String topicURL;
    private String topicHeader;
    private String message;
    private String recipient;
    private String subject;
    private String returnURL;
    private String csrfToken;

    public SendFeedbackAction() {
        recipient = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Recipient").orElse("");
        subject = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Subject").orElse("");
    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("csrfToken") != null) {
        	csrfToken = getContext().getRequest().getParameter("csrfToken");
        }
        if (getContext().getRequest().getParameter("message") != null) {
            message = getContext().getRequest().getParameter("message");
        }
        if (getContext().getRequest().getParameter("topicURL") != null) {
            topicURL = getContext().getRequest().getParameter("topicURL");
        }
        if (getContext().getRequest().getParameter("topicHeader") != null) {
            topicHeader = getContext().getRequest().getParameter("topicHeader");
        }
        if (getContext().getRequest().getParameter("fromName") != null) {
            fromName = getContext().getRequest().getParameter("fromName");
        }
        if (getContext().getRequest().getParameter("fromEmail") != null) {
            fromEmail = getContext().getRequest().getParameter("fromEmail");
        }
        if (getContext().getRequest().getParameter("recipient") != null) {
            recipient = getContext().getRequest().getParameter("recipient");
        }
        if (getContext().getRequest().getParameter("subject") != null) {
            subject = getContext().getRequest().getParameter("subject");
        }
        if (getContext().getRequest().getParameter("returnURL") != null) {
            returnURL = getContext().getRequest().getParameter("returnURL");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        if (StringUtils.isBlank(returnURL)) {
            returnURL = getContext().getRequest().getHeader("Referer");
        }
        csrfToken = UUID.randomUUID().toString();
        getContext().getRequest().getSession().setAttribute("feedbackFormCSRFToken", csrfToken);
        return fwdResolution;
    }

    public Resolution doSend() {
    	String sessionCSRFToken = String.valueOf(getContext().getRequest().getSession().getAttribute("feedbackFormCSRFToken"));
    	if(!sessionCSRFToken.equals(csrfToken)) {
    		return fwdResolution;
    	}
        SimpleEmail email = MCRActivitiMgr.createNewEmailFromConfig();
        try {
            if (StringUtils.isNotBlank(message)) {
                email.setMsg(message);
            } else {
                getContext().getMessages()
                        .add(new SimpleMessage(MCRTranslation.translate("WF.messages.feedback.noMessage")));
            }
            boolean isEmailValid = true;
            if (StringUtils.isNotBlank(fromEmail)) {
                isEmailValid = EMAIL_PATTERN.matcher(fromEmail).matches();
                if (!isEmailValid) {
                    getContext().getMessages()
                            .add(new SimpleMessage(MCRTranslation.translate("WF.messages.feedback.wrongEmailFormat")));
                }
            }

            if (StringUtils.isNotBlank(fromName)) {
                if (StringUtils.isNotBlank(fromEmail)) {
                    email.setCc(Arrays.asList(new InternetAddress[] { new InternetAddress(fromEmail, fromName) }));
                    email.setReplyTo(Arrays.asList(new InternetAddress[] { new InternetAddress(fromEmail) }));
                }
            } else {
                getContext().getMessages()
                        .add(new SimpleMessage(MCRTranslation.translate("WF.messages.feedback.noName")));
            }

            if (StringUtils.isNoneBlank(message, fromName) && isEmailValid) {
                String subject = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Subject").orElse("Feedbackformular zu {0}");
                subject = subject.replace("{0}", topicHeader);

                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(subject);
                sbMsg.append("\n" + StringUtils.repeat("=", subject.length()));
                sbMsg.append("\n");
                sbMsg.append("\nAngaben zu:");
                sbMsg.append("\n-----------");
                sbMsg.append("\n" + topicHeader);
                sbMsg.append("\n(" + topicURL + ")");
                sbMsg.append("\n");
                sbMsg.append("\nAbsender:");
                sbMsg.append("\n---------");
                sbMsg.append("\n" + fromName);
                if (StringUtils.isNotBlank(fromEmail)) {
                    sbMsg.append(" (" + fromEmail + ")");
                }
                sbMsg.append("\n");
                sbMsg.append("\nNachricht:");
                sbMsg.append("\n----------");
                sbMsg.append("\n" + message);

                String recipient = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Recipient").orElse("");
                email.getToAddresses().add(new InternetAddress(recipient));
                String[] cc = MCRConfiguration2.getString("MCR.Workflow.Email.CC").orElse("").split(",");
                for (String s : cc) {
                    s = s.trim();
                    if (StringUtils.isNotBlank(s)) {
                        email.getCcAddresses().add(new InternetAddress(s.trim()));
                    }
                }
                if (StringUtils.isNotBlank(fromEmail)) {
                    email.getCcAddresses().add(new InternetAddress(fromEmail));
                }
                email.setMsg(sbMsg.toString());
                email.setSubject(subject);

                email.send();

                if (StringUtils.isNotBlank(returnURL)) {
                    return new RedirectResolution(returnURL, false);
                }
            }
        } catch (EmailException e) {
            LOGGER.error(e);
            getContext().getMessages().add(new SimpleMessage(e.getMessage()));

        } catch (AddressException e) {
            LOGGER.error(e);
            getContext().getMessages().add(new SimpleMessage(e.getMessage()));
        } catch (UnsupportedEncodingException e) {

        }
        return fwdResolution;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getTopicURL() {
        return topicURL;
    }

    public void setTopicURL(String topicURL) {
        this.topicURL = topicURL;
    }

    public String getTopicHeader() {
        return topicHeader;
    }

    public void setTopicHeader(String topicHeader) {
        this.topicHeader = topicHeader;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }
    
    public String getCsrfToken() {
		return csrfToken;
	}
}