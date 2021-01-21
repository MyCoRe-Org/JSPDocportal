package org.mycore.frontend.jsp.stripes.actions;

import java.io.IOException;
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

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.output.DOMOutputter;
import org.mycore.activiti.MCRActivitiMgr;
import org.mycore.activiti.MCRActivitiUtils;
import org.mycore.activiti.workflows.create_object_simple.MCRWorkflowMgr;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.w3c.dom.Document;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesRequestWrapper;

@UrlBinding("/editDerivates.action")
public class EditDerivatesAction extends MCRAbstractStripesAction implements ActionBean {
    public static enum Direction {
        MOVE_UP, MOVE_DOWN
    };

    private static Logger LOGGER = LogManager.getLogger(EditDerivatesAction.class);
    ForwardResolution fwdResolution = new ForwardResolution("/content/workspace/edit-derivates.jsp");

    private List<String> messages = new ArrayList<String>();
    private String mcrobjid;
    private String taskid;
    private String mode = "";

    public EditDerivatesAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
        if (getContext().getRequest().getParameter("taskid") != null) {
            taskid = getContext().getRequest().getParameter("taskid");
        }
        if (getContext().getRequest().getParameter("mcrobjid") != null) {
            mcrobjid = getContext().getRequest().getParameter("mcrobjid");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
        for (String s : getContext().getRequest().getParameterMap().keySet()) {
            if (s.startsWith("doCreateNewDerivate-task_")) {
                taskid = s.substring(s.indexOf("_") + 1);
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                createNewDerivate();
            }
            //doMoveUpDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doMoveUpDerivate-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start);
                moveDerivate(taskid, derid, Direction.MOVE_UP);
            }

            //doMoveDownDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doMoveDownDerivate-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start);
                moveDerivate(taskid, derid, Direction.MOVE_DOWN);
            }

            //doSaveDerivateMeta-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doSaveDerivateMeta-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start);
                saveDerivateMetadata(taskid, derid, getContext().getRequest());
            }

            //doAddFile-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doAddFile-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start);
                addFileToDerivate(taskid, derid, getContext().getRequest());
            }

            //doDeleteFile-task_${actionBean.taskid}-derivate_${derID}-file_${f}
            if (s.startsWith("doDeleteFile-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start, s.indexOf("-", start));
                start = s.indexOf("file_") + 5;
                String file = s.substring(start);
                deleteFileFromDerivate(taskid, derid, file);
            }

            //doRenameFile-task_${actionBean.taskid}-derivate_${derID}-file_${f}
            if (s.startsWith("doRenameFile-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start, s.indexOf("-", start));
                start = s.indexOf("file_") + 5;
                String file = s.substring(start);
                renameFileInDerivate(taskid, derid, file, getContext().getRequest());
            }

            //doDeleteDerivate-task_${actionBean.taskid}-derivate_${derID}
            if (s.startsWith("doDeleteDerivate-")) {
                int start = s.indexOf("task_") + 5;
                taskid = s.substring(start, s.indexOf("-", start));
                mcrobjid = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MCR_OBJECT_ID, String.class);
                start = s.indexOf("derivate_") + 9;
                String derid = s.substring(start);
                deleteDerivate(taskid, derid);
            }
        }

        if (taskid != null && mcrobjid != null) {
        	mode = ts.getVariable(taskid, MCRActivitiMgr.WF_VAR_MODE, String.class);
        } else {
            messages.add("URL Parameter taskid was not set!");
        }
        return fwdResolution;

    }

    //Request-Parameter:
    //Submit: doSaveDerivateMeta-task_${actionBean.taskid}-derivate_${derID}
    //Label: saveDerivateMeta_label-task_${actionBean.taskid}-derivate_${derID}
    //Title: saveDerivateMeta_title-task_${actionBean.taskid}-derivate_${derID}
    private void saveDerivateMetadata(String taskid, String derid, HttpServletRequest request) {
        String label = request.getParameter("saveDerivateMeta_label-task_" + taskid + "-derivate_" + derid);
        String title = request.getParameter("saveDerivateMeta_title-task_" + taskid + "-derivate_" + derid);

        MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                MCRObjectID.getInstance(derid));

        if (!StringUtils.isBlank(label)) {
            der.getDerivate().getClassifications().removeIf(x -> "derivate_types".equals(x.getClassId()));
            if (MCRCategoryDAOFactory.getInstance().exist(new MCRCategoryID("derivate_types", label))) {
                der.getDerivate().getClassifications()
                        .add(new MCRMetaClassification("classification", 0, null, "derivate_types", label));
            } else {
                LOGGER.warn("Classification 'derivate_types' does not contain a category with ID: " + label);
            }

        }
        if (!StringUtils.isBlank(title)) {
            der.getDerivate().getTitles().clear();
            der.getDerivate().getTitles().add(new MCRMetaLangText("title", "de", null, 0, "plain", title));
        }
        
        Path derDir = MCRActivitiUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        updateMainFile(der, derDir);

        MCRActivitiUtils.saveMCRDerivateToWorkflowDirectory(der);
    }

    private void moveDerivate(String taskid, String derid, Direction dir) {
        MCRObject mcrObj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
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
        MCRActivitiUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
        updateDerivateOrder(mcrObj);
    }

    private void updateDerivateOrder(MCRObject mcrObj) {
        List<MCRMetaEnrichedLinkID> derList = mcrObj.getStructure().getDerivates();
        for (int pos = 0; pos < derList.size(); pos++) {
            MCRMetaEnrichedLinkID derLink = mcrObj.getStructure().getDerivates().get(pos);
            MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                    derLink.getXLinkHrefID());
            der.setOrder(pos +1);
            MCRActivitiUtils.saveMCRDerivateToWorkflowDirectory(der);
        }
    }

    //File: addFile_file-task_${actionBean.taskid}-derivate_${derID}
    private void deleteFileFromDerivate(String taskid, String derid, String fileName) {
        MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                MCRObjectID.getInstance(derid));
        Path derDir = MCRActivitiUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        Path f = derDir.resolve(fileName);
        try {
            Files.delete(f);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        if (der.getDerivate().getInternals().getMainDoc().equals(fileName)) {
            updateMainFile(der, derDir);
        }
    }

    //File: renameFile_new-task_${actionBean.taskid}-derivate_${derID}-file_${f}
    private void renameFileInDerivate(String taskid, String derid, String fileName, HttpServletRequest request) {
        MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                MCRObjectID.getInstance(derid));
        Path derDir = MCRActivitiUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
        Path f = derDir.resolve(fileName);
        String newName = request
                .getParameter("renameFile_new-task_" + taskid + "-derivate_" + derid + "-file_" + fileName);
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
                MCRActivitiUtils.saveMCRDerivateToWorkflowDirectory(der);
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
    private void deleteDerivate(String taskid, String derid) {
        MCRObject mcrObj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
        MCRObjectID derID = MCRObjectID.getInstance(derid);
        mcrObj.getStructure().removeDerivate(derID);
        updateDerivateOrder(mcrObj);
        MCRActivitiUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
        MCRActivitiUtils.cleanupWorkflowDirForDerivate(mcrObj.getId(), derID);
    }

    //File: addFile_file-task_${actionBean.taskid}-derivate_${derID}
    private void addFileToDerivate(String taskid, String derid, HttpServletRequest request) {
        FileBean fb = ((StripesRequestWrapper) getContext().getRequest())
                .getFileParameterValue("addFile_file-task_" + taskid + "-derivate_" + derid);
        if (fb != null) {
            MCRDerivate der = MCRActivitiUtils.loadMCRDerivateFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid),
                    MCRObjectID.getInstance(derid));
            Path derDir = MCRActivitiUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
            try {
                Files.copy(fb.getInputStream(), derDir.resolve(cleanupFileName(fb.getFileName())), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error(e);
            }
            updateMainFile(der, derDir);
        }
    }

    private void updateMainFile(MCRDerivate der, Path derDir) {
        String mainFile = der.getDerivate().getInternals().getMainDoc();
        if ((mainFile == null) || mainFile.trim().isEmpty() || !(Files.exists(derDir.resolve(mainFile)))) {
            mainFile = getPathOfMainFile(derDir);
            if (mainFile.equals("")) {
                der.getDerivate().getInternals().setMainDoc("");
            } else {
                der.getDerivate().getInternals().setMainDoc(mainFile.substring(derDir.toString().length() + 1));
            }
            MCRActivitiUtils.saveMCRDerivateToWorkflowDirectory(der);
        }
    }

    /** 
     * the first file in the directory becomes the main file of the derivate
     * @param the derivate directory
     * @return
     */
    protected static String getPathOfMainFile(Path parent) {
        while (Files.isDirectory(parent)) {
            List<Path> children = new ArrayList<Path>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                for (Path p : stream) {
                    children.add(p);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Collections.sort(children, new Comparator<Path>() {
                @Override
                public int compare(Path f0, Path f1) {
                    // TODO Auto-generated method stub
                    return f0.toString().compareTo(f1.toString());
                }
            });
            if (children.size() == 0) {
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

    private void createNewDerivate() {
        TaskService ts = MCRActivitiMgr.getWorfklowProcessEngine().getTaskService();
        MCRDerivate der = null;
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            MCRWorkflowMgr wfm = MCRActivitiMgr
                    .getWorkflowMgr(ts.createTaskQuery().taskId(taskid).singleResult().getProcessInstanceId());
            FileBean fb = ((StripesRequestWrapper) getContext().getRequest())
                    .getFileParameterValue("newDerivate_file-task_" + taskid);

            der = wfm.createMCRDerivate(MCRObjectID.getInstance(mcrobjid),
                    getContext().getRequest().getParameter("newDerivate_label-task_" + taskid),
                    getContext().getRequest().getParameter("newDerivate_title-task_" + taskid));

            if (fb != null) {
                Path derDir = MCRActivitiUtils.getWorkflowDerivateDir(MCRObjectID.getInstance(mcrobjid), der.getId());
                Files.copy(fb.getInputStream(), derDir.resolve(cleanupFileName(fb.getFileName())), StandardCopyOption.REPLACE_EXISTING);

                der.getDerivate().getInternals().setSourcePath(derDir.toString());
                updateMainFile(der, derDir);
                MCRActivitiUtils.saveMCRDerivateToWorkflowDirectory(der);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public String getMode() {
        return mode;
    }

    public String getMcrobjid() {
        return mcrobjid;
    }

    public String getTaskid() {
        return taskid;
    }

    public Document getMcrobjXML() {
        org.jdom2.Document jdom = MCRActivitiUtils.getWorkflowObjectXML(MCRObjectID.getInstance(mcrobjid));
        DOMOutputter domOut = new DOMOutputter();
        Document doc = null;
        try {
            doc = domOut.output(jdom);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return doc;
    }
    
    public Map<String, String> getDerivateLabels(){
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (MCRCategory c: MCRCategoryDAOFactory.getInstance().getChildren(MCRCategoryID.rootID("derivate_types"))){
            if(c.getCurrentLabel().isPresent()) {
                Optional<MCRLabel> lblMode = c.getLabel("x-usedfor");
                if(lblMode.isPresent()) {
                    List<String> modes= Arrays.asList(lblMode.get().getText().split("\\s+"));
                    if(modes.contains(getMode())) {
                        result.put(c.getId().getID(), c.getCurrentLabel().get().getText());
                    }
                }
                else {
                    result.put(c.getId().getID(), c.getCurrentLabel().get().getText());
                }
            }
        }
        return result;
    }

    public Map<String, Document> getDerivateXMLs() {
        HashMap<String, Document> result = new HashMap<String, Document>();
        MCRObject obj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(MCRObjectID.getInstance(mcrobjid));
        DOMOutputter domOut = new DOMOutputter();
        try {
            for (MCRMetaLinkID derID : obj.getStructure().getDerivates()) {
                String id = derID.getXLinkHref();
                org.jdom2.Document jdom = MCRActivitiUtils.getWorkflowDerivateXML(MCRObjectID.getInstance(mcrobjid),
                        MCRObjectID.getInstance(id));
                Document doc = null;
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

    public Map<String, List<String>> getDerivateFiles() {
        return MCRActivitiUtils.getDerivateFiles(MCRObjectID.getInstance(mcrobjid));
    }
}
