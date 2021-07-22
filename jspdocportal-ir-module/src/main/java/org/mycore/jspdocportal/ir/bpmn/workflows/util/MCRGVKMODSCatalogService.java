package org.mycore.jspdocportal.ir.bpmn.workflows.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.jspdocportal.common.controller.workspace.MCRMODSCatalogService;

public abstract class MCRGVKMODSCatalogService implements MCRMODSCatalogService {

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static XPathExpression<Element> XP_PPN = XPathFactory.instance().compile(
        "//mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']",
        Filters.element(), null, MODS_NAMESPACE);

    private static XPathExpression<Element> XP_RECORD_ID = XPathFactory.instance().compile(
        "//mods:mods/mods:recordInfo/mods:recordIdentifier",
        Filters.element(), null, MODS_NAMESPACE);

    private static XPathExpression<Element> XP_MODS_ROOT = XPathFactory.instance().compile(
        "//modsContainer[@type='created' or @type='imported'][./mods:mods]",
        Filters.element(), null, MODS_NAMESPACE);

    public void updateWorkflowFile(Path mcrFile, Document docJdom) throws Exception {
        Element eModsContainer = XP_MODS_ROOT.evaluateFirst(docJdom);
        Element eNewMODS = retrieveNewMODS(docJdom);
        if (eNewMODS == null) {
            throw new MCRException("Could not retrieve MODS for " + docJdom.getRootElement().getAttributeValue("ID"));
        }
        if (eNewMODS.getName().equals("error")) {
            if(eNewMODS.getContentSize()>0) {
                throw new MCRException(eNewMODS.getContent(0).getValue());
            }
            else {
                throw new MCRException("Could not retrieve MODS for " + docJdom.getRootElement().getAttributeValue("ID"));
            }
        }
        
        updateWorkflowMetadataFile(mcrFile, docJdom, eModsContainer, eNewMODS);
    }

    private Element retrieveNewMODS(Document docJdom) throws Exception {
        Element eModsContainer = XP_MODS_ROOT.evaluateFirst(docJdom);
        Element ePPN = XP_PPN.evaluateFirst(docJdom);
        if (eModsContainer != null && ePPN != null) {
            //retrieve MODS by PPN
            String query = "pica.ppn=" + ePPN.getTextTrim();
            return retrieveMODSFromCatalogue(query);
        }

        Element eRecordInfo = XP_RECORD_ID.evaluateFirst(docJdom);
        if (eModsContainer != null && eRecordInfo != null) {
            // retrieve MODS by PURL
            String query = "pica.url=purl*" + eRecordInfo.getTextTrim().replace("/", "") + " and pica.abr=\""
                + getABLPrefix()
                + " doctype\"";
            return retrieveMODSFromCatalogue(query);
        }

        if (eModsContainer != null) {
            //retrieve MODS by MyCoRe ID
            String mcrID = docJdom.getRootElement().getAttributeValue("ID");
            String query = "pica.url=" + getResolvingURLPrefix() + mcrID.replace("_", "");
            return retrieveMODSFromCatalogue(query);
        }
        return null;
    }

    private void updateWorkflowMetadataFile(Path mcrFile, Document docJdom, Element eModsContainer, Element eMODS)
        throws IOException {
        eModsContainer.removeContent();
        eModsContainer.addContent(eMODS.detach());
        eModsContainer.setAttribute("type", "imported");
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try (BufferedWriter bw = Files.newBufferedWriter(mcrFile)) {
            outputter.output(docJdom, bw);
        }
    }

    //RosDok: rosdok*resolveid
    //DBHSNB: digibib.hsnb.deresolveid
    public abstract String getResolvingURLPrefix();

    //RosDok: ROSDOK
    //DBHSNB: DBHSNB
    public abstract String getABLPrefix();

    //RosDOK: opac-de-28
    //DBHSNB: opac-de-519
    public abstract String getCatalogID();

    public abstract Element retrieveMODSFromCatalogue(String sruQuery);
}
