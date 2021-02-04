package org.mycore.jspdocportal.common.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.services.i18n.MCRTranslation;

/**
 * 
 * TODO cleanup which properties can be changed from form and which can be modified in form!
 * @author Robert Stephan
 *
 */
@Path("/do/feedback")
public class MCRSendFeedbackController{
    private static Logger LOGGER = LogManager.getLogger(MCRSendFeedbackController.class);

    private static final Pattern EMAIL_PATTERN = Pattern
        .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    //http://cpr.uni-rostock.de/feedback.action
    //?topicURL=http%3A%2F%2Fcpr.uni-rostock.de%2Fresolve%2Fid%2Fcpr_person_00003464
    //&topicHeader=Kr%C3%BCger%2C+Friedrich+Karl+von

    @GET
    public Response defaultRes(@Context HttpServletRequest request, 
        @QueryParam("topicURL") String topicURL, @QueryParam("topicHeader") String topicHeader,
        @QueryParam("returnURL") @DefaultValue("") String returnURL) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/feedback", model);

        String csrfToken = UUID.randomUUID().toString();
        request.getSession().setAttribute("feedbackFormCSRFToken", csrfToken);

        model.put("recipient", MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Recipient").orElse(""));
        model.put("subject", MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Subject").orElse(""));
        model.put("topicURL", topicURL);
        model.put("topicHeader", topicHeader);
        model.put("returnURL", returnURL);
        model.put("csrfToken",  csrfToken); 
       

        return Response.ok(v).build();
    }

    @POST
    public Response doSend(@Context HttpServletRequest request,
        @FormParam("fromName") String fromName,
        @FormParam("fromEmail") String fromEmail,
        @FormParam("topicURL") String topicURL,
        @FormParam("topicHeader") String topicHeader,
        @FormParam("message") String message,
        @FormParam("recipient") String recipient,
        @FormParam("subject") String subject,
        @FormParam("returnURL") String returnURL,
        @FormParam("csrfToken") String csrfToken) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/feedback", model);

        String sessionCSRFToken = String.valueOf(request.getSession().getAttribute("feedbackFormCSRFToken"));
        List<String> messages = new ArrayList<String>();
        model.put("messages", messages);

        if (!sessionCSRFToken.equals(csrfToken)) {
            return Response.status(Status.UNAUTHORIZED).entity(v).build();
        }

        SimpleEmail email = MCRBPMNMgr.createNewEmailFromConfig();
        try {
            if (StringUtils.isNotBlank(message)) {
                email.setMsg(message);
            } else {
                messages.add(MCRTranslation.translate("WF.messages.feedback.noMessage"));
            }
            boolean isEmailValid = true;
            if (StringUtils.isNotBlank(fromEmail)) {
                isEmailValid = EMAIL_PATTERN.matcher(fromEmail).matches();
                if (!isEmailValid) {
                    messages.add(MCRTranslation.translate("WF.messages.feedback.wrongEmailFormat"));
                }
            }

            if (StringUtils.isNotBlank(fromName)) {
                if (StringUtils.isNotBlank(fromEmail)) {
                    email.setCc(Arrays.asList(new InternetAddress[] { new InternetAddress(fromEmail, fromName) }));
                    email.setReplyTo(Arrays.asList(new InternetAddress[] { new InternetAddress(fromEmail) }));
                }
            } else {
                messages.add(MCRTranslation.translate("WF.messages.feedback.noName"));
            }

            if (StringUtils.isNoneBlank(message, fromName) && isEmailValid) {
                subject = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Subject")
                    .orElse("Feedbackformular zu {0}");
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

                recipient = MCRConfiguration2.getString("MCRWorkflow.Email.Feedback.Recipient").orElse("");
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
                    Response.temporaryRedirect(URI.create(returnURL)).build();
                }
            }
        } catch (EmailException e) {
            LOGGER.error(e);
            messages.add(e.getMessage());

        } catch (AddressException e) {
            LOGGER.error(e);
            messages.add(e.getMessage());
        } catch (UnsupportedEncodingException e) {

        }
        return Response.ok(v).build();
    }
}
