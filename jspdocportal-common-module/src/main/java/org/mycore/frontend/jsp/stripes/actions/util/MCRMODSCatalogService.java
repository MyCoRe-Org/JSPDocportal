package org.mycore.frontend.jsp.stripes.actions.util;

import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;

public interface MCRMODSCatalogService {
    public void updateWorkflowFile(Path mcrFile, Document docJdom);
    public Element retrieveMODSFromCatalogue(String sruQuery);
}
