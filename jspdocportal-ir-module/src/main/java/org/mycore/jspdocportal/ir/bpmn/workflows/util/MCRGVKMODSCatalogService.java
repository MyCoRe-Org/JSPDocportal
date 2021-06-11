package org.mycore.jspdocportal.ir.bpmn.workflows.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import org.mycore.jspdocportal.common.controller.workspace.MCRMODSCatalogService;

public abstract class MCRGVKMODSCatalogService implements MCRMODSCatalogService {
    
    private static Logger LOGGER = LogManager.getLogger(MCRGVKMODSCatalogService.class);
    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static XPathExpression<Element> XP_URN = XPathFactory.instance().compile(
        "//mods:mods/mods:identifier[@type='urn']",
        Filters.element(), null, MODS_NAMESPACE);

    private static XPathExpression<Element> XP_PPN = XPathFactory.instance().compile(
        "//mods:mods/mods:recordInfo/mods:recordInfoNote[@type='k10plus_ppn']",
        Filters.element(), null, MODS_NAMESPACE);

    private static XPathExpression<Element> XP_RECORD_ID = XPathFactory.instance().compile(
        "//mods:mods/mods:recordInfo/mods:recordIdentifier",
        Filters.element(), null, MODS_NAMESPACE);

    private static XPathExpression<Element> XP_MODS_ROOT = XPathFactory.instance().compile("//*[./mods:mods]",
        Filters.element(), null, MODS_NAMESPACE);
    
    public void updateWorkflowFile(Path mcrFile, Document docJdom) {
        try {
            
            Element eModsContainer = XP_MODS_ROOT.evaluateFirst(docJdom);

            Element eURN = XP_URN.evaluateFirst(docJdom);
            if (eModsContainer != null && eURN != null) {
                Element eMODS = retrieveMODSByURN(eURN.getTextTrim());
                if (eMODS != null) {
                    updateWorkflowMetadataFile(mcrFile, docJdom, eModsContainer, eMODS);
                    return;
                }
            }

            Element eRecordInfo = XP_RECORD_ID.evaluateFirst(docJdom);
            if (eModsContainer != null && eRecordInfo != null) {
                Element eMODS = retrieveMODSByPURL(eRecordInfo.getTextTrim());
                if (eMODS != null) {
                    updateWorkflowMetadataFile(mcrFile, docJdom, eModsContainer, eMODS);
                    return;
                }
            }

            Element ePPN = XP_PPN.evaluateFirst(docJdom);
            if (eModsContainer != null && ePPN != null) {
                Element eMODS = retrieveMODSByPPN(ePPN.getTextTrim());
                if (eMODS != null) {
                    updateWorkflowMetadataFile(mcrFile, docJdom, eModsContainer, eMODS);
                    return;
                }
            }

            if (eModsContainer != null) {
                Element eMODS = retrieveMODSByMyCoReID(docJdom.getRootElement().getAttributeValue("ID"));
                if (eMODS != null) {
                    updateWorkflowMetadataFile(mcrFile, docJdom, eModsContainer, eMODS);
                    return;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private void updateWorkflowMetadataFile(Path mcrFile, Document docJdom, Element eModsContainer, Element eMODS)
        throws IOException {
        eModsContainer.removeContent();
        eModsContainer.addContent(eMODS.detach());
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try (BufferedWriter bw = Files.newBufferedWriter(mcrFile)) {
            outputter.output(docJdom, bw);
        }
    }

    private Element retrieveMODSByURN(String urn) {
        String query = "pica.urn=" + urn;
        return retrieveMODSFromCatalogue(query);
    }

    private Element retrieveMODSByPURL(String recordIdentifer) {
        String query = "pica.url=purl*" + recordIdentifer.replace("/", "");
        return retrieveMODSFromCatalogue(query);
    }

    private Element retrieveMODSByMyCoReID(String mcrID) {
        String query = "pica.url="+getResolvingURLPrefix() + mcrID.replace("_", "");
        return retrieveMODSFromCatalogue(query);
    }

    private Element retrieveMODSByPPN(String ppn) {
        String query = "pica.ppn=" + ppn;
        return retrieveMODSFromCatalogue(query);
    }
    
    
    //RosDok: rosdok*resolveid
    //DBHSNB: digibib.hsnb.deresolveid
    public abstract String getResolvingURLPrefix();
        
        
    public abstract Element retrieveMODSFromCatalogue(String sruQuery);
}
