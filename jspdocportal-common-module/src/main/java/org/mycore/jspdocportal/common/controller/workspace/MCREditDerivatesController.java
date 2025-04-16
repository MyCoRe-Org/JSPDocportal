package org.mycore.jspdocportal.common.controller.workspace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jdom2.output.DOMOutputter;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCREditableMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;
import org.w3c.dom.Document;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/do/workspace/derivates")
public class MCREditDerivatesController {
    private static final String CLASSID__DERIVATE_TYPES = "derivate_types";
    private static final String PREFIX_DERIVATE = "-derivate_";
    private static final String PREFIX_TASK = "-task_";

    public enum Direction {
        MOVE_UP, MOVE_DOWN
    }

    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response submit(@Context HttpServletRequest request, FormDataMultiPart multiPart,
        @FormDataParam("taskid") String taskid, @FormDataParam("mcrobjid") String mcrobjid) {

        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
        // classic request/form parameter:
        //for (Object o : request.getParameterMap().keySet()) {
        for (BodyPart p : multiPart.getBodyParts()) {

            String s = ((FormDataContentDisposition) p.getContentDisposition()).getName();
            if (s.startsWith("doCreateNewDerivate" + PREFIX_TASK)) {
                taskid = s.substring(s.indexOf('_') + 1);
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                createNewDerivate(taskid, mcrobjid, multiPart);
            }
            //doMoveUpDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doMoveUpDerivate")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start);
                moveDerivate(mcrobjid, derid, Direction.MOVE_UP);
            }

            //doMoveDownDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doMoveDownDerivate")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start);
                moveDerivate(mcrobjid, derid, Direction.MOVE_DOWN);
            }

            //doSaveDerivateMeta-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doSaveDerivateMeta")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start);
                saveDerivateMetadata(taskid, mcrobjid, derid, multiPart);
            }

            //doAddFile-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doAddFile")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start);
                addFileToDerivate(taskid, mcrobjid, derid, multiPart);
            }

            //doDeleteFile-task_${actionBean.taskid}-derivate_${derID}-file_${f}
            if (s.startsWith("doDeleteFile")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start, s.indexOf('-', start));
                start = s.indexOf("file_") + 5;
                String file = s.substring(start);
                deleteFileFromDerivate(mcrobjid, derid, file);
            }

            //doRenameFile-task_${actionBean.taskid}-derivate_${derID}-file_${f}
            if (s.startsWith("doRenameFile")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start, s.indexOf('-', start));
                start = s.indexOf("file_") + 5;
                String file = s.substring(start);
                renameFileInDerivate(taskid, mcrobjid, derid, file, multiPart);
            }

            //doDeleteDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doDeleteDerivate")) {
                int start = s.indexOf(PREFIX_TASK) + 6;
                taskid = s.substring(start, s.indexOf('-', start));
                StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
                mcrobjid = sv.getValue();
                start = s.indexOf(PREFIX_DERIVATE) + 10;
                String derid = s.substring(start);
                deleteDerivate(mcrobjid, derid);
            }
        }

        return defaultRes(request, taskid, mcrobjid);
    }

    @GET
    public Response defaultRes(@Context HttpServletRequest request,
        @QueryParam("taskid") String taskid, @QueryParam("mcrobjid") String mcrobjid) {
        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/workspace/edit-derivates", model);
        model.put("taskid", taskid);
        model.put("mcrobjid", mcrobjid);

        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
        String mode = null;
        if (taskid != null && mcrobjid != null) {
            StringValue sv = rs.getVariableTyped(taskid, MCRBPMNMgr.WF_VAR_MODE);
            mode = sv.getValue();
        }
        model.put("mode", mode);
        model.put("mcrobjXML", calcMcrobjXML(mcrobjid));
        model.put("derivateLabels", calcDerivateLabels(mode));
        model.put("derivateXMLs", calcDerivateXMLs(mcrobjid));
        model.put("derivateFiles", MCRBPMNUtils.getDerivateFiles(MCRObjectID.getInstance(mcrobjid)));
        model.put("currentVariables", rs.getVariables(taskid));

        return Response.ok(v).build();
    }

    //Request-Parameter:
    //Submit: doSaveDerivateMeta-task_${actionBean.taskid}-derivate_${derID}
    //Label: saveDerivateMeta_label-task_${actionBean.taskid}-derivate_${derID}
    //Title: saveDerivateMeta_title-task_${actionBean.taskid}-derivate_${derID}
    private void saveDerivateMetadata(String taskid, String mcrobjid, String derid, FormDataMultiPart multiPart) {
        String label = null;
        String title = null;

        FormDataBodyPart fdbpLabel = multiPart.getField("saveDerivateMeta_label"+ PREFIX_TASK + taskid + PREFIX_DERIVATE + derid);
        if (fdbpLabel != null) {
            label = StringUtils.trimToEmpty(fdbpLabel.getValue());
        }
        FormDataBodyPart fdbpTitle = multiPart.getField("saveDerivateMeta_title" + PREFIX_TASK + taskid + PREFIX_DERIVATE + derid);
        if (fdbpTitle != null) {
            title = StringUtils.trimToEmpty(fdbpTitle.getValue());
        }

        MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
            MCRObjectID.getInstance(derid));

        if (!StringUtils.isBlank(label)) {
            der.getDerivate().getClassifications().removeIf(x -> CLASSID__DERIVATE_TYPES.equals(x.getClassId()));
            if (MCRCategoryDAOFactory.obtainInstance().exist(new MCRCategoryID(CLASSID__DERIVATE_TYPES, label))) {
                der.getDerivate().getClassifications()
                    .add(new MCRMetaClassification("classification", 0, null, CLASSID__DERIVATE_TYPES, label));
            } else {
                LOGGER.warn("Classification 'derivate_types' does not contain a category with ID: {}", label);
            }

        }
        if (!StringUtils.isBlank(title)) {
            der.getDerivate().getTitles().clear();
            der.getDerivate().getTitles().add(new MCRMetaLangText("title", "de", null, 0, "plain", title));
        }

        Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        updateMainFile(der, derDir);
        MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
    }

    private void moveDerivate(String mcrobjid, String derid, Direction dir) {
        MCRObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
        List<MCRMetaEnrichedLinkID> derList = mcrObj.getStructure().getDerivates();
        for (int pos = 0; pos < derList.size(); pos++) {
            if (derList.get(pos).getXLinkHref().equals(derid)) {
                if (dir == Direction.MOVE_UP && pos > 0) {
                    Collections.swap(derList, pos, pos - 1);
                }
                if (dir == Direction.MOVE_DOWN && pos < derList.size() - 1) {
                    Collections.swap(derList, pos, pos + 1);
                }
                break;
            }
        }
        updateDerivateOrder(mcrObj);
        MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);

    }

    private void updateDerivateOrder(MCRObject mcrObj) {
        List<MCRMetaEnrichedLinkID> derList = mcrObj.getStructure().getDerivates();
        for (int pos = 0; pos < derList.size(); pos++) {
            MCRMetaEnrichedLinkID derLink = mcrObj.getStructure().getDerivates().get(pos);
            if (derLink instanceof MCREditableMetaEnrichedLinkID x) {
                x.setOrder(pos + 1);
            }

            MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(mcrObj.getId(),
                derLink.getXLinkHrefID());
            der.setOrder(pos + 1);
            MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
        }
    }

    //File: addFile_file-task_${actionBean.taskid}-derivate_${derID}
    private void deleteFileFromDerivate(String mcrobjid, String derid, String fileName) {
        MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
            MCRObjectID.getInstance(derid));
        Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        Path f = derDir.resolve(fileName);
        try {
            Files.delete(f);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        if (der.getDerivate().getInternals().getMainDoc().equals(fileName)) {
            updateMainFile(der, derDir);
        }
        MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
    }

    //File: renameFile_new-task_${actionBean.taskid}-derivate_${derID}-file_${f}
    private void renameFileInDerivate(String taskid, String mcrobjid, String derid, String fileName,
        FormDataMultiPart multiPart) {
        MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
            MCRObjectID.getInstance(derid));
        Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        Path f = derDir.resolve(fileName);
        String newName = multiPart
            .getField("renameFile_new" + PREFIX_TASK + taskid + PREFIX_DERIVATE + derid + "-file_" + fileName).getValue();

        if (!StringUtils.isBlank(newName)) {
            newName = cleanupFileName(newName);

            Path fNew = f.getParent().resolve(newName);
            try {
                Files.move(f, fNew, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error(e);
            }

            if (der.getDerivate().getInternals().getMainDoc().equals(fileName)) {
                der.getDerivate().getInternals().setMainDoc(newName);
                MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
            }
        }
    }

    private String cleanupFileName(String input) {
        String newName = input;
        newName = newName.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        newName = newName.replace("Ä", "AE").replace("Ö", "OE").replace("Ü", "OE");
        newName = newName.replace("\\", "/");
        newName = newName.replaceAll("[^a-zA-Z0-9_\\-\\.\\/]", "_");
        return newName;
    }

    //File: addFile_file-task_${actionBean.taskid}-derivate_${derID}
    private void deleteDerivate(String mcrobjid, String derid) {
        MCRObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
        MCRObjectID derID = MCRObjectID.getInstance(derid);
        mcrObj.getStructure().removeDerivate(derID);
        updateDerivateOrder(mcrObj);
        MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
        MCRBPMNUtils.cleanupWorkflowDirForDerivate(mcrObj.getId(), derID);
    }

    //File: addFile_file-task_${actionBean.taskid}-derivate_${derID}
    private void addFileToDerivate(String taskid, String mcrobjid, String derid, FormDataMultiPart multiPart) {
        String field = "addFile_file" + PREFIX_TASK + taskid + PREFIX_DERIVATE + derid;
        String fileName = multiPart.getField(field).getFormDataContentDisposition().getFileName();
        try (InputStream is = multiPart.getField(field).getValueAs(InputStream.class)) {
            MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                MCRObjectID.getInstance(derid));
            Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());

            Files.copy(is, derDir.resolve(cleanupFileName(fileName)),
                StandardCopyOption.REPLACE_EXISTING);
            der.getDerivate().getInternals().setSourcePath(derDir.toString());
            updateMainFile(der, derDir);
            MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private void updateMainFile(MCRDerivate der, Path derDir) {
        String mainFile = der.getDerivate().getInternals().getMainDoc();
        if ((mainFile == null) || mainFile.isBlank() || !(Files.exists(derDir.resolve(mainFile)))) {
            mainFile = getPathOfMainFile(derDir);
            if (mainFile.equals("")) {
                der.getDerivate().getInternals().setMainDoc("");
            } else {
                der.getDerivate().getInternals().setMainDoc(mainFile.substring(derDir.toString().length() + 1));
            }
        }
    }

    /** 
     * the first file in the directory becomes the main file of the derivate
     * @param parent - the derivate directory
     * @return
     */
    protected static String getPathOfMainFile(Path parent) {
        while (Files.isDirectory(parent)) {
            List<Path> children = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                for (Path p : stream) {
                    children.add(p);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }

            Collections.sort(children, new Comparator<>() {
                @Override
                public int compare(Path f0, Path f1) {
                    return f0.toString().compareTo(f1.toString());
                }
            });
            if (children.isEmpty()) {
                return "";
            }
            if (Files.isDirectory(children.get(0))) {
                parent = children.get(0);
            }
            for (Path element : children) {
                if (Files.isRegularFile(element)) {
                    return element.toString();
                }
            }
        }
        return "";
    }

    private void createNewDerivate(String taskid, String mcrobjid, FormDataMultiPart multiPart) {
        TaskService ts = MCRBPMNMgr.getWorfklowProcessEngine().getTaskService();
        MCRDerivate der;
        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            MCRWorkflowMgr wfm = MCRBPMNMgr
                .getWorkflowMgr(ts.createTaskQuery().executionId(taskid).singleResult().getProcessInstanceId());

            String label = multiPart.getField("newDerivate_label" + PREFIX_TASK + taskid) == null
                ? null : multiPart.getField("newDerivate_label" + PREFIX_TASK + taskid).getValue();
            String title = multiPart.getField("newDerivate_title" + PREFIX_TASK + taskid) == null
                ? null : multiPart.getField("newDerivate_title" + PREFIX_TASK + taskid).getValue();
            String fileName = multiPart.getField("newDerivate_file" + PREFIX_TASK + taskid).getFormDataContentDisposition()
                .getFileName();
            der = wfm.createMCRDerivate(MCRObjectID.getInstance(mcrobjid), label, title);

            try (InputStream is = multiPart.getField("newDerivate_file" + PREFIX_TASK + taskid).getValueAs(InputStream.class)) {
                Path derDir = MCRBPMNUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
                Files.createDirectories(derDir);
                Files.copy(is, derDir.resolve(cleanupFileName(fileName)),
                    StandardCopyOption.REPLACE_EXISTING);
                der.getDerivate().getInternals().setSourcePath(derDir.toString());
                updateMainFile(der, derDir);
                MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Document calcMcrobjXML(String mcrobjid) {
        org.jdom2.Document jdom = MCRBPMNUtils.getWorkflowObjectXML(MCRObjectID.getInstance(mcrobjid));
        DOMOutputter domOut = new DOMOutputter();
        Document doc = null;
        try {
            doc = domOut.output(jdom);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return doc;
    }

    public Map<String, String> calcDerivateLabels(String mode) {
        Map<String, String> result = new LinkedHashMap<>();
        for (MCRCategory c : MCRCategoryDAOFactory.obtainInstance().getChildren(MCRCategoryID.rootID(CLASSID__DERIVATE_TYPES))) {
            if (c.getCurrentLabel().isPresent()) {
                Optional<MCRLabel> lblMode = c.getLabel("x-usedfor");
                if (lblMode.isPresent()) {
                    List<String> modes = Arrays.asList(lblMode.get().getText().split("\\s+"));
                    if (modes.contains(mode)) {
                        result.put(c.getId().getId(), c.getCurrentLabel().get().getText());
                    }
                } else {
                    result.put(c.getId().getId(), c.getCurrentLabel().get().getText());
                }
            }
        }
        return result;
    }

    public Map<String, Document> calcDerivateXMLs(String mcrobjid) {
        Map<String, Document> result = new HashMap<>();
        MCRObject obj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
        DOMOutputter domOut = new DOMOutputter();
        try {
            for (MCRMetaLinkID derID : obj.getStructure().getDerivates()) {
                String id = derID.getXLinkHref();
                org.jdom2.Document jdom = MCRBPMNUtils.getWorkflowDerivateXML(MCRObjectID.getInstance(mcrobjid),
                    MCRObjectID.getInstance(id));
                Document doc;
                try {
                    doc = domOut.output(jdom);
                    result.put(id, doc);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return result;
    }

    public Map<String, List<String>> getDerivateFiles(String mcrobjid) {
        return MCRBPMNUtils.getDerivateFiles(MCRObjectID.getInstance(mcrobjid));
    }
}
