package org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkIDFactory;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXParseException;

public abstract class MCRAbstractWorkflowMgr implements MCRWorkflowMgr {
    private static Logger LOGGER = LogManager.getLogger(MCRAbstractWorkflowMgr.class);

    @Override
    public MCRObject createMCRObject(DelegateExecution execution) {
        String base = execution.getVariable(MCRBPMNMgr.WF_VAR_PROJECT_ID).toString() + "_"
            + execution.getVariable(MCRBPMNMgr.WF_VAR_OBJECT_TYPE).toString();
        MCRObject mcrObj = new MCRObject();

        mcrObj.setSchema("datamodel-" + execution.getVariable(MCRBPMNMgr.WF_VAR_OBJECT_TYPE).toString() + ".xsd");
        mcrObj.setId(MCRObjectID.getNextFreeId(base));
        mcrObj.setLabel(mcrObj.getId().toString());
        mcrObj.setVersion(MCRConfiguration2.getString("MCR.SWF.MCR.Version").orElse("2.0"));
        MCRObjectMetadata defaultMetadata = getDefaultMetadata(base);
        if (defaultMetadata != null) {
            mcrObj.getMetadata().appendMetadata(defaultMetadata);
        }
        mcrObj.getService().setState(new MCRCategoryID(
            MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"), "new"));
        mcrObj.getService().addFlag("editedby", MCRUserManager.getCurrentUser().getUserID());
        mcrObj.getStructure();
        try {
            MCRMetadataManager.create(mcrObj);
            execution.setVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObj.getId().toString());

            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
        } catch (MCRAccessException e) {
            LOGGER.error(e);
        }
        return mcrObj;
    }

    @Override
    public MCRObject loadMCRObject(DelegateExecution execution) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(
            MCRObjectID.getInstance(String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID))));
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            mcrObj.getService().removeFlags("editedby");
            mcrObj.getService().addFlag("editedby", MCRUserManager.getCurrentUser().getUserID());

            MCRMetadataManager.update(mcrObj);

            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
            processDerivatesOnLoad(mcrObj);
        } catch (MCRAccessException e) {
            LOGGER.error(e);
        }

        execution.setVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObj.getId().toString());
        return mcrObj;
    }

    @Override
    public MCRObject dropMCRObject(DelegateExecution execution) {
        MCRObject mcrObj = null;
        MCRObjectID mcrObjID = MCRObjectID
            .getInstance(String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
        if (MCRMetadataManager.exists(mcrObjID)) {
            mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                mcrObj.getService().setState(new MCRCategoryID(
                    MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                    "deleted"));
                mcrObj.getService().removeFlags("editedby");
                MCRMetadataManager.delete(mcrObj);
            } catch (MCRActiveLinkException | MCRAccessException e) {
                LOGGER.error(e);
            }
        }
        MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
        return mcrObj;
    }

    @Override
    public MCRDerivate createMCRDerivate(MCRObjectID owner, String label, String title) {
        MCRDerivate der = new MCRDerivate();
        der.setId(MCRObjectID.getInstance(owner.getProjectId() + "_derivate_0"));
        der.setSchema("datamodel-derivate.xsd");
        der.getDerivate().setLinkMeta(new MCRMetaLinkID("linkmeta", owner, null, null));

        der.getDerivate().setInternals(new MCRMetaIFS("internal", null));
        der.getService().setState(new MCRCategoryID(
            MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"), "new"));

        if (!StringUtils.isBlank(title)) {
            der.getDerivate().getTitles().add(new MCRMetaLangText("title", "de", null, 0, "plain", title));
        }
        if (!StringUtils.isBlank(label)) {
            if (MCRCategoryDAOFactory.getInstance().exist(new MCRCategoryID("derivate_types", label))) {
                der.getDerivate().getClassifications()
                    .add(new MCRMetaClassification("classification", 0, null, "derivate_types", label));
            } else {
                LOGGER.warn("Classification 'derivate_types' does not contain a category with ID: " + label);
            }
        }

        if (MCRAccessManager.checkPermission("create-" + owner.getBase())
            || MCRAccessManager.checkPermission("create-" + owner.getTypeId())) {
            MCRObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(owner);
            if (der.getId().getNumberAsInteger() == 0) {
                MCRObjectID newDerID = MCRObjectID.getNextFreeId(der.getId().getBase());
                der.setId(newDerID);
                try {
                    MCRMetadataManager.create(der);
                } catch (MCRAccessException e) {
                    LOGGER.error(e);
                }
                MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
            }
            der.setOrder(mcrObj.getStructure().getDerivates().size() + 1);
            mcrObj.getStructure().addDerivate(MCRMetaEnrichedLinkIDFactory.getInstance().getDerivateLink(der));
            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);

        } else {
            throw new MCRPersistenceException(
                "You do not have \"create\" permission on " + der.getId().getTypeId() + ".");
        }
        return der;
    }

    @Override
    public boolean deleteProcessInstance(String processInstanceId) {
        RuntimeService rs = MCRBPMNMgr.getWorfklowProcessEngine().getRuntimeService();
        String id = String.valueOf(rs.getVariable(processInstanceId, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
        rs.deleteProcessInstance(processInstanceId, "Deletion requested by admin");
        if (!id.equals("null")) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(id);
            return resetMetadataAndCleanupWorkflowDir(mcrObjID);
        }
        return false;
    }

    public MCRObjectMetadata getDefaultMetadata(String mcrBase) {
        SAXBuilder sax = new SAXBuilder();
        try {
            String xml = getDefaultMetadataXML(mcrBase);
            Document doc = sax.build(new StringReader(xml));
            MCRObjectMetadata mcrOMD = new MCRObjectMetadata();
            mcrOMD.setFromDOM(doc.getRootElement());
            return mcrOMD;
        } catch (Exception e) {
            throw new MCRException("Could not create default metadata", e);
        }
    }

    protected abstract String getDefaultMetadataXML(String mcrBase);

    @Override
    public boolean commitMCRObject(DelegateExecution execution) {
        String id = String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
        if (!id.equals("null")) {
            if (execution.hasVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE)) {
                execution.removeVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE);
            }
            MCRObjectID mcrObjID = MCRObjectID.getInstance(id);
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                MCRObject mcrWFObj = MCRBPMNUtils.getWorkflowObject(mcrObjID);
                MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                processDerivatesOnCommit(mcrObj, mcrWFObj);

                MCRObjectMetadata mcrObjMeta = mcrObj.getMetadata();
                mcrObjMeta.removeInheritedMetadata();
                while (mcrObjMeta.size() > 0) {
                    mcrObjMeta.removeMetadataElement(0);
                }

                mcrObjMeta.appendMetadata(mcrWFObj.getMetadata());

                StringValue mode = execution.getVariableTyped(MCRBPMNMgr.WF_VAR_MODE);
                if (mode.getValue().startsWith("wf_edit_")) {
                    mcrObj.getService().setState(new MCRCategoryID(
                        MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                        "published"));
                }
                if (mode.getValue().startsWith("wf_register_")) {
                    mcrObj.getService().setState(new MCRCategoryID(
                        MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                        "reserved"));
                }

                // set/update delete information from <service>
                removeServDate(mcrObj, "mcr-delete:date");
                if (mcrWFObj.getService().getDate("mcr-delete:date") != null) {
                    mcrObj.getService().setDate("mcr-delete:date", mcrWFObj.getService().getDate("mcr-delete:date"));
                }
                mcrObj.getService().removeFlags("mcr-delete:note");
                for (String flag : mcrWFObj.getService().getFlags("mcr-delete:note")) {
                    mcrObj.getService().addFlag("mcr-delete:note", flag);
                }
                mcrObj.getService().removeFlags("mcr-delete:doctype");
                for (String flag : mcrWFObj.getService().getFlags("mcr-delete:doctype")) {
                    mcrObj.getService().addFlag("mcr-delete:doctype", flag);
                }
                if ("deleted".equals(mcrWFObj.getService().getState().getID())) {
                    mcrObj.getService().setState(new MCRCategoryID(
                        MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                        "deleted"));
                }

                mcrObj.getService().removeFlags("editedby");
                MCRMetadataManager.update(mcrObj);
            } catch (MCRAccessException | MCRException e) {
                LOGGER.error(e);
                StringBuffer msg = new StringBuffer(e.getMessage());
                if (e.getCause() != null) {
                    Throwable t1 = e.getCause();
                    msg.append("\ncaused by: ").append(t1.getMessage());
                    if (t1.getCause() != null) {
                        Throwable t2 = t1.getCause();
                        msg.append("\ncaused by: ").append(t2.getMessage());
                    }
                }
                //TODO: Display error / exception in workflow
                execution.setVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE, msg.toString());
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean rollbackMCRObject(DelegateExecution execution) {
        // String id =
        // String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
        // do nothing - cleanup done on workflow endState
        return true;
    }

    @Override
    public boolean validateMCRObject(DelegateExecution execution) {
        String id = String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
        if (!id.equals("null")) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(id);
            String result = validate(mcrObjID);
            if (result == null) {
                execution.setVariable(MCRBPMNMgr.WF_VAR_VALIDATION_RESULT, true);
                if (execution.hasVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE)) {
                    execution.removeVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE);
                }
                return true;
            } else {
                execution.setVariable(MCRBPMNMgr.WF_VAR_VALIDATION_RESULT, false);
                execution.setVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE, result);
                return false;
            }
        }
        return false;
    }

    /**
     * validation for the MyCoRe Object
     * Subclasses may override this method for enhanced validation
     * @param mcrObjID
     * @return null if correct, error message otherwise
     */

    protected String validate(MCRObjectID mcrObjID) {
        Path wfFile = MCRBPMNUtils.getWorkflowObjectFile(mcrObjID);
        try {
            @SuppressWarnings("unused")
            MCRObject mcrWFObj = new MCRObject(wfFile.toUri());
        } catch (SAXParseException e) {
            return "XML Error: " + e.getMessage();
        } catch (IOException e) {
            return "I/O-Error: " + e.getMessage();
        }
        return null;

    }

    private boolean resetMetadataAndCleanupWorkflowDir(MCRObjectID mcrObjID) {
        if (MCRMetadataManager.exists(mcrObjID)) {
            try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                for (MCRMetaLinkID metaID : new ArrayList<MCRMetaLinkID>(mcrObj.getStructure().getDerivates())) {
                    MCRObjectID derID = metaID.getXLinkHrefID();
                    MCRDerivate derObj = null;
                    try {
                        derObj = MCRMetadataManager.retrieveMCRDerivate(derID);
                    } catch (MCRPersistenceException mpe) {
                        LOGGER.error(mpe);
                    }
                    if (derObj != null && derObj.getService().getState() != null) {
                        String state = derObj.getService().getState().getID();
                        if (state.equals("new")) {
                            MCRMetadataManager.delete(derObj);
                        }
                        derObj.getService().removeFlags("editedby");
                        try {
                            MCRMetadataManager.update(derObj);
                        } catch (MCRAccessException e) {
                            LOGGER.error(e);
                        }

                    }
                }

                mcrObj = MCRMetadataManager.retrieveMCRObject(mcrObjID);
                if (mcrObj.getService().getState() != null &&
                    "new".equals(mcrObj.getService().getState().getID())) {
                    MCRMetadataManager.delete(mcrObj);
                } else {
                    mcrObj.getService().removeFlags("editedby");
                    MCRMetadataManager.update(mcrObj);
                }

            } catch (MCRActiveLinkException | MCRAccessException e) {
                LOGGER.error(e);
            }
        }
        MCRBPMNUtils.cleanUpWorkflowDirForObject(mcrObjID);

        return false;
    }

    // stores changes on Derivates in Workflow into the MyCoRe Object
    private void processDerivatesOnLoad(MCRObject mcrObj) {
        // delete derivates if necessary

        for (MCRMetaLinkID metalinkID : mcrObj.getStructure().getDerivates()) {
            MCRObjectID mcrDerID = metalinkID.getXLinkHrefID();
            if (mcrDerID != null && MCRMetadataManager.exists(mcrDerID)) {
                MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrDerID);
                if (mcrDer.getService().getState() == null
                    || "new|published".contains(mcrDer.getService().getState().getID())) {
                    mcrDer.getService().removeFlags("editedby");
                    mcrDer.getService().addFlag("editedby", MCRUserManager.getCurrentUser().getUserID());
                }
                try {
                    MCRMetadataManager.update(mcrDer);
                } catch (MCRAccessException e) {
                    LOGGER.error(e);
                }
                MCRBPMNUtils.cleanupWorkflowDirForDerivate(mcrObj.getId(), mcrDerID);
                MCRDerivateCommands.show(mcrDerID.toString(),
                    MCRBPMNUtils.getWorkflowObjectDir(mcrObj.getId()).toString());
            }
        }
    }

    // stores changes on Derivates in Workflow into the MyCoRe Object
    private void processDerivatesOnCommit(MCRObject mcrObj, MCRObject mcrWFObj) {
        // delete derivates if necessary
        List<String> wfDerivateIDs = new ArrayList<String>();
        for (MCRMetaLinkID derID : mcrWFObj.getStructure().getDerivates()) {
            wfDerivateIDs.add(derID.getXLinkHref());
        }
        Set<MCRObjectID> derIDsToDelete = new HashSet<MCRObjectID>();
        for (MCRMetaLinkID derID : mcrObj.getStructure().getDerivates()) {
            if (!wfDerivateIDs.contains(derID.getXLinkHref())) {
                derIDsToDelete.add(derID.getXLinkHrefID());
            }
        }
        for (MCRObjectID derID : derIDsToDelete) {
            try {
                MCRMetadataManager.deleteMCRDerivate(derID);
            } catch (MCRAccessException e) {
                LOGGER.error(e);
            }
        }
        // update derivates in MyCoRe
        for (String derID : wfDerivateIDs) {
            MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(mcrObj.getId(),
                MCRObjectID.getInstance(derID));
            if ("deleted".equals(mcrObj.getService().getState().getID())) {
                der.getService().setState(new MCRCategoryID(
                    MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                    "deleted"));
            } else if (der.getService().getState() == null
                || "new|review".contains(der.getService().getState().getID())) {
                der.getService().setState(new MCRCategoryID(
                    MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"),
                    "published"));
            }
            der.getService().removeFlags("editedby");
            MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);

            String filename = MCRBPMNUtils.getWorkflowDerivateFile(mcrObj.getId(), MCRObjectID.getInstance(derID))
                .toString();
            Map<String, Element> ruleMap = null;
            try {
                MCRObjectID derIDObj = MCRObjectID.getInstance(derID);
                if (MCRMetadataManager.exists(derIDObj)) {
                    ruleMap = MCRBPMNUtils.getAccessRulesMap(derID);
                    MCRBPMNUtils.deleteDirectoryContent(MCRPath.getPath(derID, "/"));
                    MCRDerivateCommands.updateFromFile(filename, false);
                } else {
                    MCRDerivateCommands.loadFromFile(filename, false);
                }
                /*
                if (MCRMetadataManager.exists(derIDObj)) {
                	ruleMap = MCRBPMNUtils.getAccessRulesMap(derID);
                	MCRDerivateCommands.delete(derID);
                }
                MCRDerivateCommands.loadFromFile(filename, false);
                */
            } catch (SAXParseException | IOException | MCRAccessException e) {
                LOGGER.error(e);
            }
            if (ruleMap != null) {
                MCRBPMNUtils.setAccessRulesMap(derID, ruleMap);
            }
        }

        // update order of derivates and labels in mcrobject
        mcrObj.getStructure().getDerivates().clear();
        mcrObj.getStructure().getDerivates().addAll(mcrWFObj.getStructure().getDerivates());
    }

    @Override
    public boolean cleanupWorkflow(DelegateExecution execution) {
        String id = String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID));
        if (!id.equals("null")) {
            return resetMetadataAndCleanupWorkflowDir(MCRObjectID.getInstance(id));
        }
        return false;
    }

    //use reflection to delete a servdate from MyCoRe-Object
    //remove in LTS 2019 (use new method removeDate(String type)
    private void removeServDate(MCRObject mcrObj, String type) {
        if (mcrObj != null && mcrObj.getService() != null) {
            Method methodGetDates;
            try {
                methodGetDates = MCRObjectService.class.getDeclaredMethod("getDates");
                methodGetDates.setAccessible(true);

                @SuppressWarnings("unchecked")
                ArrayList<MCRMetaISO8601Date> servDates = (ArrayList<MCRMetaISO8601Date>) methodGetDates
                    .invoke(mcrObj.getService());
                Iterator<MCRMetaISO8601Date> it = servDates.iterator();
                while (it.hasNext()) {
                    if (it.next().getType().equals(type)) {
                        it.remove();
                    }
                }

            } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                LOGGER.error(e);
            }
        }
    }
}
