package org.mycore.jspdocportal.ir.bpmn.workflows.create_object_simple;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.jspdocportal.common.bpmn.MCRBPMNUtils;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRAbstractWorkflowMgr;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;
import org.xml.sax.SAXParseException;

public class MCRWorkflowMgrEditData extends MCRAbstractWorkflowMgr implements MCRWorkflowMgr {
    private static final String DEFAULT_METADATA_XML = ""
        + "<metadata>"
        + "  <def.modsContainer class='MCRMetaXML'>"
        + "    <modsContainer inherited='0' type='imported'>"
        + "      <mods:mods xmlns:mods='http://www.loc.gov/mods/v3' version='3.7'>"
        + "        <mods:titleInfo xml:lang='de' usage='primary'>"
        + "           <mods:title>Neues DataObject</mods:title>"
        + "        </mods:titleInfo>"
        + "            <mods:classification displayLabel='doctype' valueURI='#epub' />"
        + "      </mods:mods>"
        + "    </modsContainer>"
        + "  </def.modsContainer>"
        + "</metadata>";

    @Override
    public MCRObjectMetadata getDefaultMetadata(String mcrBase) {
        SAXBuilder sax = new SAXBuilder();
        try {
            Document doc = sax.build(new StringReader(DEFAULT_METADATA_XML));
            MCRObjectMetadata mcrOMD = new MCRObjectMetadata();
            mcrOMD.setFromDOM(doc.getRootElement());
            return mcrOMD;
        } catch (Exception e) {
            throw new MCRException("Could not create default metadata", e);
        }
    }

    /**
     * 
     * @param mcrObjID
     * @return null if correct, error message otherwise
     */
    @Override
    public String validate(MCRObjectID mcrObjID) {
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

}
