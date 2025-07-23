package org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.jdom2.Document;
import org.jdom2.JDOMException;
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
import org.mycore.datamodel.metadata.MCRExpandedObject;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkIDFactory;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.datamodel.niofs.utils.MCRTreeCopier;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNMgr;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.user2.MCRUserManager;

public abstract class MCRAbstractWorkflowMgr implements MCRWorkflowMgr {
    private static final String STATE_REVIEW = "review";
    private static final String FLAG_DELETE_DOCTYPE = "mcr-delete:doctype";
    private static final String FLAG_DELETE_NOTE = "mcr-delete:note";
    private static final String FLAG_DELETE_DATE = "mcr-delete:date";
    private static final String FLAG_EDITEDBY = "editedby";
    private static final String STATE_RESERVED = "reserved";
    private static final String STATE_PUBLISHED = "published";
    private static final String STATE_DELETED = "deleted";
    private static final String STATE_NEW = "new";
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public MCRExpandedObject createMCRObject(DelegateExecution execution) {
        String base = execution.getVariable(MCRBPMNMgr.WF_VAR_PROJECT_ID).toString() + "_"
            + execution.getVariable(MCRBPMNMgr.WF_VAR_OBJECT_TYPE).toString();
        MCRExpandedObject mcrObj = new MCRExpandedObject();

        mcrObj.setSchema("datamodel-" + execution.getVariable(MCRBPMNMgr.WF_VAR_OBJECT_TYPE).toString() + ".xsd");
        mcrObj.setId(MCRMetadataManager.getMCRObjectIDGenerator().getNextFreeId(base));
        mcrObj.setLabel(mcrObj.getId().toString());
        mcrObj.setVersion(MCRConfiguration2.getString("MCR.SWF.MCR.Version").orElse("2.0"));
        MCRObjectMetadata defaultMetadata = getDefaultMetadata(base);
        if (defaultMetadata != null) {
            mcrObj.getMetadata().appendMetadata(defaultMetadata);
        }
        mcrObj.getService().setState(createStateCategory(STATE_NEW));
        mcrObj.getService().addFlag(FLAG_EDITEDBY, MCRUserManager.getCurrentUser().getUserID());
        mcrObj.getStructure();
        try {
            MCRMetadataManager.create(mcrObj);
            MCRObjectID mcrObjId = mcrObj.getId();
            execution.setVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObjId.toString());

            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(MCRMetadataManager.retrieveMCRExpandedObject(mcrObjId));
        } catch (MCRAccessException e) {
            LOGGER.error(e);
        }
        return mcrObj;
    }

    @Override
    public MCRExpandedObject loadMCRObject(DelegateExecution execution) {
        
        MCRObjectID mcrObjId = MCRObjectID.getInstance(String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
        MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjId);
        try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
            mcrObj.getService().removeFlags(FLAG_EDITEDBY);
            mcrObj.getService().addFlag(FLAG_EDITEDBY, MCRUserManager.getCurrentUser().getUserID());

            MCRMetadataManager.update(mcrObj);
            //Fix: MCRMetadaManager.update() deletes structure (derivates + children)
            // therefore we need to get a new MCRExpandedObject from MetadataManager before it an be further processed
            //MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
            mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjId);
            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
            processDerivatesOnLoad(mcrObj);
        } catch (MCRAccessException e) {
            LOGGER.error(e);
        }

        execution.setVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID, mcrObj.getId().toString());
        return mcrObj;
    }

    @Override
    public MCRExpandedObject dropMCRObject(DelegateExecution execution) {
        MCRExpandedObject mcrObj = null;
        MCRObjectID mcrObjID = MCRObjectID
            .getInstance(String.valueOf(execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID)));
        if (MCRMetadataManager.exists(mcrObjID)) {
            MCRBPMNUtils.saveMCRObjectToWorkflowDirectory(mcrObj);
            mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);
            try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
                mcrObj.getService().setState(createStateCategory(STATE_DELETED));
                mcrObj.getService().removeFlags(FLAG_EDITEDBY);
                MCRMetadataManager.delete(mcrObj);
            } catch (MCRActiveLinkException | MCRAccessException e) {
                LOGGER.error(e);
            }
        }
        return mcrObj;
    }

    @Override
    public MCRDerivate createMCRDerivate(MCRObjectID owner, String label, String title) {
        MCRDerivate der = new MCRDerivate();
        der.setId(MCRObjectID.getInstance(owner.getProjectId() + "_derivate_0"));
        der.setSchema("datamodel-derivate.xsd");
        der.getDerivate().setLinkMeta(new MCRMetaLinkID("linkmeta", owner, null, null));

        der.getDerivate().setInternals(new MCRMetaIFS("internal", null));
        der.getService().setState(createStateCategory(STATE_NEW));

        if (!StringUtils.isBlank(title)) {
            der.getDerivate().getTitles().add(new MCRMetaLangText("title", "de", null, 0, "plain", title));
        }
        if (!StringUtils.isBlank(label)) {
            if (MCRCategoryDAOFactory.obtainInstance().exist(new MCRCategoryID("derivate_types", label))) {
                der.getDerivate().getClassifications()
                    .add(new MCRMetaClassification("classification", 0, null, "derivate_types", label));
            } else {
                LOGGER.warn("Classification 'derivate_types' does not contain a category with ID: {}", label);
            }
        }

        if (MCRAccessManager.checkPermission("create-" + owner.getBase())
            || MCRAccessManager.checkPermission("create-" + owner.getTypeId())) {
            MCRExpandedObject mcrObj = MCRBPMNUtils.loadMCRObjectFromWorkflowDirectory(owner);
            if (der.getId().getNumberAsInteger() == 0) {
                MCRObjectID newDerID = MCRMetadataManager.getMCRObjectIDGenerator()
                    .getNextFreeId(der.getId().getBase());
                der.setId(newDerID);
                try {
                    MCRMetadataManager.create(der);
                } catch (MCRAccessException e) {
                    LOGGER.error(e);
                }
                MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);
            }
            der.setOrder(mcrObj.getStructure().getDerivates().size() + 1);
            mcrObj.getStructure().addDerivate(MCRMetaEnrichedLinkIDFactory.obtainInstance().getDerivateLink(der));
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
        Object id = rs.getVariable(processInstanceId, MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
        rs.deleteProcessInstance(processInstanceId, "Deletion requested by admin");
        if (id != null) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(id));
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
        Object id = execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
        if (id != null) {
            if (execution.hasVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE)) {
                execution.removeVariable(MCRBPMNMgr.WF_VAR_VALIDATION_MESSAGE);
            }
            MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(id));
            try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
                MCRExpandedObject mcrWFObj = MCRBPMNUtils.getWorkflowObject(mcrObjID);
                MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);
                processDerivatesOnCommit(mcrObj, mcrWFObj);

                //reload mcrObject, because it was change in processDerivatesOnCommit
                mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);
                MCRObjectMetadata mcrObjMeta = mcrObj.getMetadata();
                mcrObjMeta.removeInheritedMetadata();
                while (mcrObjMeta.size() > 0) {
                    mcrObjMeta.removeMetadataElement(0);
                }

                mcrObjMeta.appendMetadata(mcrWFObj.getMetadata());

                StringValue mode = execution.getVariableTyped(MCRBPMNMgr.WF_VAR_MODE);
                writeService(mode.getValue(), mcrWFObj, mcrObj);

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

    private void writeService(String mode, MCRExpandedObject mcrWFObj, MCRExpandedObject mcrObj) {

        if (mode.startsWith("wf_edit_")) {
            mcrObj.getService().setState(createStateCategory(STATE_PUBLISHED));
        }
        if (mode.startsWith("wf_register_")) {
            mcrObj.getService().setState(createStateCategory(STATE_RESERVED));
        }

        // set/update delete information from <service>
        mcrObj.getService().removeDate(FLAG_DELETE_DATE);
        if (mcrWFObj.getService().getDate(FLAG_DELETE_DATE) != null) {
            mcrObj.getService().setDate(FLAG_DELETE_DATE, mcrWFObj.getService().getDate(FLAG_DELETE_DATE));
        }
        mcrObj.getService().removeFlags(FLAG_DELETE_NOTE);
        for (String flag : mcrWFObj.getService().getFlags(FLAG_DELETE_NOTE)) {
            mcrObj.getService().addFlag(FLAG_DELETE_NOTE, flag);
        }
        mcrObj.getService().removeFlags(FLAG_DELETE_DOCTYPE);
        for (String flag : mcrWFObj.getService().getFlags(FLAG_DELETE_DOCTYPE)) {
            mcrObj.getService().addFlag(FLAG_DELETE_DOCTYPE, flag);
        }
        if (STATE_DELETED.equals(mcrWFObj.getService().getState().getId())) {
            mcrObj.getService().setState(createStateCategory(STATE_DELETED));
        }

        mcrObj.getService().removeFlags(FLAG_EDITEDBY);
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
        Object id = execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
        if (id != null) {
            MCRObjectID mcrObjID = MCRObjectID.getInstance(String.valueOf(id));
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
            MCRExpandedObject mcrWFObj = new MCRExpandedObject(wfFile.toUri());
        } catch (JDOMException e) {
            return "XML Error: " + e.getMessage();
        } catch (IOException e) {
            return "I/O-Error: " + e.getMessage();
        }
        return null;

    }

    private boolean resetMetadataAndCleanupWorkflowDir(MCRObjectID mcrObjID) {
        boolean result = true;
        if (MCRMetadataManager.exists(mcrObjID)) {
            try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {
                MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);
                for (MCRMetaLinkID metaID : new ArrayList<MCRMetaLinkID>(mcrObj.getStructure().getDerivates())) {
                    MCRObjectID derID = metaID.getXLinkHrefID();
                    MCRDerivate derObj = null;
                    try {
                        derObj = MCRMetadataManager.retrieveMCRDerivate(derID);
                    } catch (MCRPersistenceException mpe) {
                        LOGGER.error(mpe);
                    }
                    if (derObj != null && derObj.getService().getState() != null) {
                        String state = derObj.getService().getState().getId();
                        if (state.equals(STATE_NEW)) {
                            MCRMetadataManager.delete(derObj);
                        } else {
                            derObj.getService().removeFlags(FLAG_EDITEDBY);
                            derObj.getDerivate().getInternals().setSourcePath(null);
                            try {
                                MCRMetadataManager.update(derObj);
                            } catch (MCRAccessException e) {
                                LOGGER.error(e);
                                result = false;
                            }
                        }

                    }
                }

                mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(mcrObjID);
                if (mcrObj.getService().getState() != null &&
                    STATE_NEW.equals(mcrObj.getService().getState().getId())) {
                    MCRMetadataManager.delete(mcrObj);
                } else {
                    mcrObj.getService().removeFlags(FLAG_EDITEDBY);
                    MCRMetadataManager.update(mcrObj);
                }

            } catch (MCRActiveLinkException | MCRAccessException e) {
                LOGGER.error(e);
                result = false;
            }
        }
        MCRBPMNUtils.cleanUpWorkflowDirForObject(mcrObjID);

        return result;
    }

    // stores changes on Derivates in Workflow into the MyCoRe Object
    private void processDerivatesOnLoad(MCRExpandedObject mcrObj) {
        // delete derivates if necessary

        for (MCRMetaLinkID metalinkID : mcrObj.getStructure().getDerivates()) {
            MCRObjectID mcrDerID = metalinkID.getXLinkHrefID();
            if (mcrDerID != null && MCRMetadataManager.exists(mcrDerID)) {
                MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrDerID);
                if (mcrDer.getService().getState() == null
                    || List.of(STATE_NEW, STATE_PUBLISHED).contains(mcrDer.getService().getState().getId())) {
                    mcrDer.getService().removeFlags(FLAG_EDITEDBY);
                    mcrDer.getService().addFlag(FLAG_EDITEDBY, MCRUserManager.getCurrentUser().getUserID());
                }
                try {
                    MCRMetadataManager.update(mcrDer);
                } catch (MCRAccessException e) {
                    LOGGER.error(e);
                }
                MCRBPMNUtils.cleanupWorkflowDirForDerivate(mcrObj.getId(), mcrDer.getId());
                MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(mcrDer);
                MCRPath rootPath = MCRPath.getRootPath(mcrDerID.toString());
                try {
                    Files.walkFileTree(rootPath, new MCRTreeCopier(rootPath,
                        MCRBPMNUtils.getWorkflowDerivateDir(mcrObj.getId(), mcrDer.getId())));
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    // stores changes on Derivates in Workflow into the MyCoRe Object
    private void processDerivatesOnCommit(MCRExpandedObject mcrObj, MCRExpandedObject mcrWFObj) {
        // delete derivates if necessary
        List<String> wfDerivateIDs = new ArrayList<>();
        for (MCRMetaLinkID derID : mcrWFObj.getStructure().getDerivates()) {
            wfDerivateIDs.add(derID.getXLinkHref());
        }
        Set<MCRObjectID> derIDsToDelete = new HashSet<>();
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
            processDerivate(mcrObj, wfDerivateIDs.indexOf(derID) + 1, derID);
        }
    }

    private void processDerivate(MCRExpandedObject mcrObj, int order, String derID) {
        MCRDerivate der = MCRBPMNUtils.loadMCRDerivateFromWorkflowDirectory(mcrObj.getId(),
            MCRObjectID.getInstance(derID));
        der.setOrder(order);
        if (STATE_DELETED.equals(mcrObj.getService().getState().getId())) {
            der.getService().setState(createStateCategory(STATE_DELETED));
        } else if (der.getService().getState() == null
            || List.of(STATE_NEW, STATE_REVIEW).contains(der.getService().getState().getId())) {
            der.getService().setState(createStateCategory(STATE_PUBLISHED));
        }
        der.getService().removeFlags(FLAG_EDITEDBY);
        MCRBPMNUtils.saveMCRDerivateToWorkflowDirectory(der);

        String filename = MCRBPMNUtils.getWorkflowDerivateFile(mcrObj.getId(), MCRObjectID.getInstance(derID))
            .toString();
        try {
            MCRObjectID derIDObj = MCRObjectID.getInstance(derID);
            if (MCRMetadataManager.exists(derIDObj)) {
                MCRBPMNUtils.deleteDirectoryContent(MCRPath.getRootPath(derID));
                MCRDerivateCommands.updateFromFile(filename, false);
            } else {
                MCRDerivateCommands.loadFromFile(filename, false);
            }
        } catch (JDOMException | IOException | MCRAccessException e) {
            LOGGER.error(e);
        }
    }

    private MCRCategoryID createStateCategory(String state) {
        return new MCRCategoryID(
            MCRConfiguration2.getString("MCR.Metadata.Service.State.Classification.ID").orElse("state"), state);
    }

    @Override
    public boolean cleanupWorkflow(DelegateExecution execution) {
        Object id = execution.getVariable(MCRBPMNMgr.WF_VAR_MCR_OBJECT_ID);
        if (id != null) {
            return resetMetadataAndCleanupWorkflowDir(MCRObjectID.getInstance(String.valueOf(id)));
        }
        return false;
    }
}
