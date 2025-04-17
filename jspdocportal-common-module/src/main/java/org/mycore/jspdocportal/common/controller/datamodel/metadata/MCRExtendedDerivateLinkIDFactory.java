package org.mycore.jspdocportal.common.controller.datamodel.metadata;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.datamodel.metadata.MCRDefaultEnrichedDerivateLinkIDFactory;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCREditableMetaEnrichedLinkID;
import org.mycore.datamodel.niofs.MCRFileAttributes;
import org.mycore.datamodel.niofs.MCRPath;

public class MCRExtendedDerivateLinkIDFactory extends MCRDefaultEnrichedDerivateLinkIDFactory {
    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public MCREditableMetaEnrichedLinkID getDerivateLink(MCRDerivate der) {
        MCREditableMetaEnrichedLinkID derivateLinkID = super.getDerivateLink(der);
        final String mainDoc = derivateLinkID.getMainDoc();
        if (!StringUtils.isEmpty(mainDoc)) {
            MCRPath mcrPath = MCRPath.getPath(der.getId().toString(), mainDoc);

            if (Files.exists(mcrPath)) {
                try {
                    @SuppressWarnings("rawtypes")
                    MCRFileAttributes attrs = Files.readAttributes(mcrPath, MCRFileAttributes.class);
                    derivateLinkID.setOrCreateElement("maindoc_size", Long.toString(attrs.size()));
                    derivateLinkID.getContentList()
                        .add(new Element("maindoc_" + attrs.digest().getAlgorithm().toLowerCase())
                            .setText(attrs.digest().toHexString()));
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.error("Error - maindoc '{}' does not exist for {}", () -> mainDoc, der::getId);
            }
        }

        return derivateLinkID;
    }
}
