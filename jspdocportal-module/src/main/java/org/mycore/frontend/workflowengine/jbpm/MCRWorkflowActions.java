/*
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 * 
 * 
 */

package org.mycore.frontend.workflowengine.jbpm;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.editor.MCRRequestParameters;
import org.mycore.frontend.jsp.MCRJSPDocportalCommands;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.frontend.workflowengine.strategies.MCRWorkflowDirectoryManager;

/**
 * This class is the superclass of servlets which checks the MCREditorServlet
 * output XML and store the XML in a file or if an error was occured start the
 * editor again.
 * 
 * @author Heiko Helmbrecht
 * @version $Revision$ $Date$
 * 
 */
public class MCRWorkflowActions extends MCRServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(MCRWorkflowActions.class);
	
	/**
     * This method overrides doGetPost of MCRServlet. <br />
     */
	
    public void doGetPost(MCRServletJob job) throws Exception {
    	   	
    	HttpServletRequest request = job.getRequest();
    	HttpServletResponse response = job.getResponse();
            	
        MCRRequestParameters parms = new MCRRequestParameters(request);
        long pid = Long.parseLong(parms.getParameter("processid"));
    	String returnPath ="";
        MCRWorkflowProcess wfp = MCRWorkflowProcessManager.getInstance().getWorkflowProcess(pid);
    	try{
     
        MCRWorkflowManager WFM = wfp.getCurrentWorkflowManager();
        
        String mcrid = wfp.getStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
        String userid = wfp.getStringVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR);
        String workflowType = wfp.getWorkflowProcessType();
        String documentType = wfp.getDocumentType();
                
        String derivateID = parms.getParameter("derivateID");
        String nextPath = parms.getParameter("nextPath");
        String filename = parms.getParameter("filename");
        
        if ( nextPath == null || nextPath.length() == 0)        	
        	 nextPath = "~workflow-" + workflowType;
        
        String todo = parms.getParameter("todo");
       
        wfp.getContextInstance().setVariable("varnameERROR", "");
        
        if ( "WFAddWorkflowObject".equals(todo) ) {
            if ( ! MCRAccessManager.checkPermission("create-"+documentType)) {
            	nextPath+="begin";
            	returnPath="/nav?path=" + nextPath;
            	return;            	
            }
        	// leeren Editor für das Object includieren
        	request.setAttribute("isNewEditorSource","false");
        	request.setAttribute("mcrid",mcrid);
        	request.setAttribute("processid",new Long(pid));        	
        	request.setAttribute("type",documentType);
        	request.setAttribute("workflowType",workflowType);
        	request.setAttribute("step","author");
        	request.setAttribute("nextPath",nextPath);
        	returnPath="/nav?path=~editor-include";
        	return;
        }
        if ( "WFEditWorkflowObject".equals(todo) ) {
            if ( ! MCRAccessManager.checkPermission("create-"+documentType)) {
            	nextPath+="begin";
            	returnPath="/nav?path=" + nextPath;
            	return;            	
            }
        	// befuellten Editor fuer das Object includieren
        	String publicationType = (String) wfp.getContextInstance().getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_PUBLICATIONTYPE);
        	if ( publicationType == null )  
        		 publicationType="";
        	
        	if ( publicationType.length() > 0	&& publicationType.indexOf(".") >0 )
        		 publicationType = publicationType.substring(0, publicationType.indexOf("."));
        	request.setAttribute("isNewEditorSource","false");
        	request.setAttribute("mcrid",mcrid);
        	request.setAttribute("processid",new Long(pid));        	
        	request.setAttribute("type",documentType);
        	request.setAttribute("workflowType",workflowType);
        	request.setAttribute("publicationType",publicationType);
        	request.setAttribute("step","author");
        	request.setAttribute("nextPath",nextPath);
        	returnPath="/nav?path=~editor-include";
        	return;
        	
        }
        if ( "WFCommitWorkflowObject".equals(todo) ) {
    		if (  ( MCRAccessManager.checkPermission(mcrid, "commitdb")
   	             && MCRAccessManager.checkPermission(derivateID,"deletedb")) ) {   			
    			WFM.commitWorkflowObject(wfp.getContextInstance());
    		}
    		returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFDeleteWorkflowObject".equals(todo) ) {
        	boolean bSuccess =false;
    		if ( MCRAccessManager.checkPermission(mcrid, "deletewf") ) {
    			bSuccess = WFM.removeWorkflowFiles(wfp.getContextInstance());
    		}
    		if (bSuccess) {
        		if (!MCRMetadataManager.exists(MCRObjectID.getInstance(mcrid))){
        			// delete unused Permissions from database
        			MCRAccessManager.getAccessImpl().removeAllRules(mcrid);
        		}
    			// gesamten Prozess löschen!!
    			wfp.close();
    			MCRJSPDocportalCommands.deleteProcess(String.valueOf(pid));
    		}

    		returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFDeleteObject".equals(todo) ) {
        	boolean bSuccess =false;
    		if ( MCRAccessManager.checkPermission(mcrid, "deletedb")) {
    	        bSuccess = WFM.removeDatabaseObjects(wfp.getContextInstance());
    	        if ( bSuccess )
    	        	bSuccess = WFM.removeWorkflowFiles(wfp.getContextInstance());
    		}
    		if (bSuccess) {
    			// gesamten Prozess löschen!!
    			wfp.close();
    			MCRJSPDocportalCommands.deleteProcess(String.valueOf(pid));
    		}
    		returnPath="/nav?path=" + nextPath;
        	return;
        }        
        if ( "WFAddNewDerivateToWorkflowObject".equals(todo) ) {
            if ( ! MCRAccessManager.checkPermission("create-"+documentType)) {
            	nextPath+="begin";
            	returnPath="/nav?path=" + nextPath;
            	return;            	
            }
        	derivateID = WFM.addDerivate(wfp.getContextInstance(), mcrid);
        	
    		WFM.setDefaultPermissions(derivateID, userid, wfp.getContextInstance());
    		
        	todo = "WFAddNewFileToDerivate";
        	// kein requestforward sondern in den WFAddNewFileToDerivate Zweig übergehen! 
        }
        if ( "WFEditDerivateFromWorkflowObject".equals(todo) ) {
        	//befüllten Editor für das Derivate includieren	um Label zu editieren		
            if ( ! MCRAccessManager.checkPermission("create-"+documentType)) {
            	nextPath+="begin";
            	returnPath="/nav?path=" + nextPath;
            	return;            	
            }
        	StringBuffer sb = new StringBuffer();        	
			sb.append(MCRWorkflowDirectoryManager.getWorkflowDirectory(documentType))
				.append("/")
				.append(derivateID).append(".xml");
				
			request.setAttribute("path","~editor-include");
        	request.setAttribute("isNewEditorSource","false");
        	request.setAttribute("editorSource",sb.toString());
			request.setAttribute("mcrid", derivateID);
			request.setAttribute("type", "derivate");
        	request.setAttribute("processid",new Long(pid));        	
			request.setAttribute("workflowType",workflowType);
			request.setAttribute("step", "author");
			request.setAttribute("mcrid2", mcrid);
			request.setAttribute("nextPath", nextPath);
			request.setAttribute("target", "MCRCheckDerivateServlet");
			returnPath="/nav?path=~editor-include";
        	return;
        }
       
        if ( "WFAddNewFileToDerivate".equals(todo) ) {
            if ( ! MCRAccessManager.checkPermission("create-"+documentType)) {
            	nextPath+="begin";
        		returnPath="/nav?path=" + nextPath;
            	return;            	
            }
        	//leeren upload Editor includieren
			String fuhid = new MCRWorkflowUploadHandler( mcrid, derivateID, "new", nextPath).getID();
			String base = getBaseURL() + "nav";
			Properties params = new Properties();
			params.put("path","~editor-include");
			params.put("uploadID", fuhid);
			params.put("isNewEditorSource", "true");
			params.put("mcrid2", mcrid);
			params.put("type", documentType);
			params.put("processid", String.valueOf(pid));
			params.put("workflowType",workflowType);
			params.put("nextPath", nextPath);
			params.put("step", "author");
			params.put("mcrid", derivateID);
			params.put("target", "MCRCheckDerivateServlet");

			returnPath="";
			wfp.close();
			response.sendRedirect(response.encodeRedirectURL(buildRedirectURL(base, params)));
			return;
       	}
     
        if ( "WFRemoveFileFromDerivate".equals(todo) ) {
    		if ( (  	MCRAccessManager.checkPermission(mcrid, "deletewf")
   	             && MCRAccessManager.checkPermission(derivateID,"deletewf")) ) {    			
   		   	 WFM.removeFileFromDerivate(wfp.getContextInstance(), mcrid, derivateID, filename);
    		}
    		returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFRemoveDerivateFromWorkflowObject".equals(todo) ) {
    		if ( (  	MCRAccessManager.checkPermission(mcrid, "deletewf")
    	             && MCRAccessManager.checkPermission(derivateID,"deletewf")) ) {    			
    		   	 WFM.removeDerivate(wfp.getContextInstance(), mcrid, derivateID);
    		}    		
            returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFMoveDerivateUp".equals(todo) ) {
    		if ( (  	MCRAccessManager.checkPermission(mcrid, "deletewf")
    	             && MCRAccessManager.checkPermission(derivateID,"deletewf")) ) {    			
    		   	 WFM.moveDerivate(wfp.getContextInstance(), mcrid, derivateID,-1);
    		}    		
            returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFMoveDerivateDown".equals(todo) ) {
    		if ( (  	MCRAccessManager.checkPermission(mcrid, "deletewf")
    	             && MCRAccessManager.checkPermission(derivateID,"deletewf")) ) {    			
    		   	 WFM.moveDerivate(wfp.getContextInstance(), mcrid, derivateID,+1);
    		}    		
            returnPath="/nav?path=" + nextPath;
        	return;
        }
        if ( "WFCreateURN".equals(todo) ){
        	WFM.createURN(wfp.getContextInstance());
        	returnPath="/nav?path=" + nextPath;
        	return;
        }
        
        
        
        
        
        }
        catch(Exception e){
        	LOGGER.error("Exception in MCRWorkflowActions", e);
        }
        finally{
        	if(!wfp.wasClosed()){
        		wfp.close();
        	}

        	if(returnPath.equals("")){
        		//do nothing
        	}
        	else{
        		request.getRequestDispatcher(returnPath).forward(request, response);
        	}
        }
    }
    
	/**
	 * Builds an url that can be used to redirect the client browser to another
	 * page, including http request parameters. The request parameters will be
	 * encoded as http get request.
	 * 
	 * @param baseURL
	 *            the base url of the target webpage
	 * @param parameters
	 *            the http request parameters
	 */
	protected String buildRedirectURL(String baseURL, Properties parameters) {
		StringBuffer redirectURL = new StringBuffer(baseURL);
		boolean first = true;

		for (Enumeration<Object> e = parameters.keys(); e.hasMoreElements();) {
			if (first) {
				redirectURL.append("?");
				first = false;
			} else
				redirectURL.append("&");

			String name = (String) (e.nextElement());
			String value = null;
			try {
				value = URLEncoder.encode(parameters.getProperty(name), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				value = parameters.getProperty(name);
			}

			redirectURL.append(name).append("=").append(value);
		}

		LOGGER.debug("Sending redirect to " + redirectURL.toString());
		return redirectURL.toString();
	}    
    


}

