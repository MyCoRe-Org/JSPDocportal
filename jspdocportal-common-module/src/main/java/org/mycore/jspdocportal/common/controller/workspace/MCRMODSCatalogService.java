package org.mycore.jspdocportal.common.controller.workspace;

import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;

public interface MCRMODSCatalogService {
    void updateWorkflowFile(Path mcrFile, Document docJdom) throws Exception;
    Element retrieveMODSFromCatalogue(String sruQuery);
}
