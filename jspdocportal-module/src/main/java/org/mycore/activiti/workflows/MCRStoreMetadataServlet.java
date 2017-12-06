package org.mycore.activiti.workflows;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.mycore.activiti.MCRActivitiUtils;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;


/**
 * Stores the Metadata (JDOM Document) from XEditor into the MCRObject file in workflow directory
 * 
 * @author Robert Stephan
 */
public class MCRStoreMetadataServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;

	/**
	 * This method overrides doGetPost of MCRServlet. <br />
	 */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Document xml = (org.jdom2.Document) (req.getAttribute("MCRXEditorSubmission"));
		String mcrID = xml.getRootElement().getAttributeValue("ID");
		MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
		Path wfFile = MCRActivitiUtils.getWorkflowObjectFile(mcrObjID);
		MCRJDOMContent content = new MCRJDOMContent(xml);
		content.sendTo(wfFile);
		
		resp.sendRedirect(MCRFrontendUtil.getBaseURL()+"showWorkspace.action?mcr_base="+mcrObjID.getBase());
	}
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
