package org.mycore.frontend.workflowengine.jbpm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jbpm.graph.exe.ExecutionContext;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * @author mcradmin
 *
 */
public class MCRJbpmSendmail{

	protected static Logger logger = Logger.getLogger(MCRJbpmSendmail.class);
	private static Calendar cal = new GregorianCalendar( TimeZone.getTimeZone("ECT"), Locale.US);
	protected static String workflowAdminEmail = MCRConfiguration.instance().getString("MCR.WorkflowEngine.Administrator.Email", "admin@mycore.de");
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void sendMail(String from,
			String to, String replyTo, String bcc, 
			String subject, String body, String mode,
			String jbpmVariableName, String dateOfSubmissionVariable, 
			ExecutionContext executionContext) throws MCRException{

		if(to == null || to.equals("")){
			String errMsg = "no recipient was given";
			logger.error(errMsg);
			throw new MCRException(errMsg);			
		}
		
		if(subject == null || subject.equals("")){
			subject = MCRTranslation.translate("WorkflowEngine.Mails.DefaultSubject", new Locale("de"));
		}
		subject = replaceMessageKeys(subject);
		subject += " (Bearbeitungsnummer: " + executionContext.getProcessInstance().getId() + ")";
		if(body == null){
			body = "";
		}
		body = replaceMessageKeys(body);
		if(jbpmVariableName != null && !jbpmVariableName.equals("")){
			body += executionContext.getVariable(jbpmVariableName);  
		}
		try {
			String id = (String)executionContext.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			String title = (String)executionContext.getVariable(MCRWorkflowConstants.WFM_VAR_WFOBJECT_TITLE);
			if ( title != null)
				body += "\nTitel: '" + title + "'";
			if ( id != null)
				body += " ID: " + id;
		} catch ( Exception all) {
			;
		}
		
		if(body.equals("")){
			String errMsg = "no body for mail was given";
			logger.error(errMsg);
			throw new MCRException(errMsg);
		}
		try{
			List listTo = getEmailAddressesFromStringList(to, executionContext);						
			List listReplyTo = getEmailAddressesFromStringList(replyTo, executionContext);
			List listBcc = getEmailAddressesFromStringList(bcc, executionContext);
			
			// if no correct email is set
			if (listTo.size() + listReplyTo.size() + listBcc.size() < 1)	{
				
				logger.error("could not send email, but the workflow goes on");
				logger.error("main recipients are empty" );
				return;
			}
			if (listTo.size() < 1) {
				if (listReplyTo.size() > 0)
					  listTo = listReplyTo;
				else  listTo = listBcc;
			}			

			String fromAddress = (String)getEmailAddressesFromStringList(from, executionContext).iterator().next();
			MCRMailer.send(fromAddress, listReplyTo, listTo, listBcc, subject, body, null);
			if(dateOfSubmissionVariable != null && !dateOfSubmissionVariable.equals("")) {
				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			    executionContext.setVariable(dateOfSubmissionVariable, formater.format( cal.getTime() ) );	
			}
		}catch(Exception e){
			logger.error("could not send email, but the workflow goes on");
			logger.error("mail subject: " + subject);
			logger.error("mail body: " + body);
			logger.error("mail main recipients: " + to);
		}		
	}
	
	private static List<String> getEmailAddressesFromStringList(String addresses,
			ExecutionContext executionContext) {
		List<String> ret = new ArrayList<String>();
		if (addresses == null || addresses.equals(""))
			return ret;
		String[] array = addresses.split(";");
		for (int i = 0; i < array.length; i++) {
			if (array[i].indexOf("@") >= 0) {
				ret.add(array[i]);
			} else if (array[i].trim().equals("initiator")) {
				String email = getUserEmailAddress((String) executionContext
						.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR));
				if (email == null || email.equals("")) {
					email = (String) executionContext
							.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOREMAIL);
				}
				if (email == null || email.equals("")) {
					email = getUserEmailAddress("administrator");
				}
				ret.add(email);
			} else if (array[i].trim().equals("user")){
				MCRUser user = MCRUserManager.getCurrentUser();
				String email = user.getEMailAddress();
				if(email!=null && !email.equals("")){
					ret.add(email);
				}
			} else if (array[i].trim().equals("administrator")){
				String email  = getUserEmailAddress("administrator");
				if(email==null || email.equals("")){
					email = workflowAdminEmail;
				}
				ret.add(email);
				
			}			
			else {
				ret.addAll(getGroupMembersEmailAddresses(array[i]));
			}
		}
		return ret;
	}
	
	private static String getUserEmailAddress(String userid){
		if ( MCRUserManager.exists(userid) ) {
			MCRUser user = MCRUserManager.getUser(userid);
			return user.getEMailAddress();
		}
		return null;
	}
	
	private static List<String> getGroupMembersEmailAddresses(String groupid){
		List<String> ret = new ArrayList<String>();
		MCRRole group = MCRRoleManager.getRole(groupid);
		if (group !=null) {
			
			for (Iterator<String> it = MCRRoleManager.listUserIDs(group).iterator(); it.hasNext();) {
				String email = getUserEmailAddress(it.next());
				if(email != null && email.indexOf("@") > -1){
					ret.add(email);
				}
			}
		}
		if(ret.size() == 0){
			logger.error("no group member of [" + "] has a known e-mail-address");
			ret.add(workflowAdminEmail);
		}
		return ret;
	}
	
	
	
	/**
	 * replaces all occurrences of ${variable} 
	 * with the proper entry from the message property files
	 * @param text the input, that should be translated
	 * 
	 * @return the translated string
	 * 
	 * @author Robert Stephan
	 */
	public static String replaceMessageKeys(String text){
		ResourceBundle rb = MCRTranslation.getResourceBundle("messages", new Locale("de"));
		Pattern p = Pattern.compile("\\$\\{[^\\}]*\\}");
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String key = m.group();
			key = key.substring(2,key.length()-1);
			String replacement="???"+key+"???";
			try{
				replacement = rb.getString(key);
			} catch(MissingResourceException mre){
				
			}
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		return sb.toString();
}
}