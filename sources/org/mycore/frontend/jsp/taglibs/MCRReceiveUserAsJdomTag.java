package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserMgr;
public class MCRReceiveUserAsJdomTag extends SimpleTagSupport
{
	private static Logger logger = Logger.getLogger(MCRReceiveUserAsJdomTag.class);
	
	private String userID;
	private String var ;
	
	public void setVar (String var ){
		this.var  = var ; 
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	
	public void doTag() throws JspException, IOException {
    	try {
    		if ( userID == null || userID.length() == 0) {
    			userID = MCRSessionMgr.getCurrentSession().getCurrentUserID();
    		}
    		MCRUser u = new MCRUser(userID);
    		String sNoUser = "User with ID " + userID + " dos'nt exit";
    		try {
    			u = MCRUserMgr.instance().retrieveUser(userID);   		
			} catch (MCRException noUser) {
				logger.warn(sNoUser);
				u.setDescription(sNoUser);
			}
			
			Document jUser = u.toJDOMDocument();    	
			Element eUser = (Element)jUser.getRootElement().getChild("user").clone();
			PageContext pageContext = (PageContext) getJspContext();
	    	//org.w3c.dom.Document domDoc = null;
    		//domDoc =  new DOMOutputter().output( jUser);
    		//pageContext.setAttribute(var , domDoc);
    		pageContext.setAttribute(var , new Document(eUser));
    		
    	} catch (Exception e) {
    		logger.error("error in receiving user for jdom ", e);
    	}
	}	

}