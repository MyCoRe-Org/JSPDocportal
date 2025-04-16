package org.mycore.jspdocportal.common.bpmn.workflows.servlets;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jdom2.Document;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Stores the Metadata (JDOM Document) from XEditor into the MCRObject file in workflow directory
 * 
 * TODO rewrite as Jersey-Controller?
 * 
 * @author Robert Stephan
 */
public class MCRStoreMetadataServlet extends HttpServlet {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * This method overrides doGetPost of MCRServlet. <br />
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Document xml = (Document) (req.getAttribute("MCRXEditorSubmission"));
        String mcrID = xml.getRootElement().getAttributeValue("ID");
        MCRObjectID mcrObjID = MCRObjectID.getInstance(mcrID);
        Path wfFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        MCRJDOMContent content = new MCRJDOMContent(xml);
        content.sendTo(wfFile, StandardCopyOption.REPLACE_EXISTING);

        resp.sendRedirect(MCRFrontendUtil.getBaseURL() + "do/workspace/tasks");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
