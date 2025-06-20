package org.mycore.jspdocportal.common.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * 
 * TODO cleanup which properties can be changed from form and which can be modified in form!
 * @author Robert Stephan
 *
 */
@Path("/do/feedback")
public class MCRSendFeedbackController {
    private static final Logger LOGGER = LogManager.getLogger();

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

        model.put("recipient", MCRConfiguration2.getString("MCR.Workflow.Email.Feedback.Recipient").orElse(""));
        model.put("subject", MCRConfiguration2.getString("MCR.Workflow.Email.Feedback.Subject").orElse(""));
        model.put("topicURL", topicURL);
        model.put("topicHeader", topicHeader);
        model.put("returnURL", returnURL);
        model.put("csrfToken", csrfToken);

        return Response.ok(v).build();
    }

    @SuppressWarnings({"PMD.NcssCount", "PMD.NPathComplexity"})
    @POST
    public Response doSend(@Context HttpServletRequest request,
        @FormParam("fromName") @DefaultValue("") String fromName,
        @FormParam("fromEmail") String fromEmail,
        @FormParam("topicURL") String topicURL,
        @FormParam("topicHeader") String topicHeader,
        @FormParam("message") String message,
        @FormParam("recipient") String recipient,
        @FormParam("returnURL") String returnURL,
        @FormParam("csrfToken") String csrfToken) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/feedback", model);

        String sessionCSRFToken = String.valueOf(request.getSession().getAttribute("feedbackFormCSRFToken"));
        List<String> messages = new ArrayList<>();
        model.put("messages", messages);

        if (!sessionCSRFToken.equals(csrfToken)) {
            return Response.status(Status.UNAUTHORIZED).entity(v).build();
        }

        try {
            if (StringUtils.isBlank(message)) {
                messages.add(MCRTranslation.translate("WF.messages.feedback.noMessage"));
            }
            boolean isEmailValid = true;
            if (StringUtils.isNotBlank(fromEmail)) {
                isEmailValid = EMAIL_PATTERN.matcher(fromEmail).matches();
                if (!isEmailValid) {
                    messages.add(MCRTranslation.translate("WF.messages.feedback.wrongEmailFormat"));
                }
            }

            List<InternetAddress> cc = new ArrayList<>();
            List<InternetAddress> replyTo = new ArrayList<>();

            if (StringUtils.isNotBlank(fromName) && StringUtils.isNotBlank(fromEmail)) {
                cc.add(new InternetAddress(fromEmail, fromName));
                replyTo.add(new InternetAddress(fromEmail, fromName));
            } else {
                messages.add(MCRTranslation.translate("WF.messages.feedback.noName"));
            }

            if (StringUtils.isNoneBlank(message, fromName) && isEmailValid) {
                String subject = MCRConfiguration2.getString("MCR.Workflow.Email.Feedback.Subject")
                    .orElse("Feedbackformular zu {0}");
                subject = subject.replace("{0}", topicHeader);

                StringBuilder sbMailBody = new StringBuilder();
                sbMailBody.append(subject);
                sbMailBody.append('\n').append(StringUtils.repeat('=', subject.length()));
                sbMailBody.append('\n');
                sbMailBody.append("\nAngaben zu:");
                sbMailBody.append("\n-----------");
                sbMailBody.append('\n').append(topicHeader);
                sbMailBody.append("\n(").append(topicURL).append(')');
                sbMailBody.append('\n');
                sbMailBody.append("\nAbsender:");
                sbMailBody.append("\n---------");
                sbMailBody.append('\n').append(fromName);
                if (StringUtils.isNotBlank(fromEmail)) {
                    sbMailBody.append(" (").append(fromEmail).append(')');
                }
                sbMailBody.append('\n');
                sbMailBody.append("\nNachricht:");
                sbMailBody.append("\n----------");
                sbMailBody.append('\n').append(message);

                List<InternetAddress> receiver = List
                    .of(new InternetAddress(
                        MCRConfiguration2.getString("MCR.Workflow.Email.Feedback.Recipient").get()));
                String[] ccProp = MCRConfiguration2.getString("MCR.Workflow.Email.CC").orElse("").split(",");
                for (String s : ccProp) {
                    s = s.trim();
                    if (StringUtils.isNotBlank(s)) {
                        cc.add(new InternetAddress(s.trim()));
                    }
                }
                if (StringUtils.isNotBlank(fromEmail)) {
                    cc.add(new InternetAddress(fromEmail, fromName));
                }

                MCRBPMNMgr.sendMail(receiver, subject, sbMailBody.toString(), replyTo, cc);

                //see other redirect POST request to GET response
                if (StringUtils.isNotBlank(returnURL)) {
                    return Response.seeOther(URI.create(returnURL)).build();
                } else if (StringUtils.isNotBlank(topicURL)) {
                    return Response.seeOther(URI.create(topicURL)).build();
                }
            }
        } catch (UnsupportedEncodingException e) {
            // do nothing
        } catch (Exception e) {
            LOGGER.error(e);
            messages.add(e.getMessage());
        }
        return Response.ok(v).build();
    }
}
