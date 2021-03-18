package org.mycore.jspdocportal.common.controller.datamodel.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRDefaultEnrichedDerivateLinkIDFactory;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCREditableMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaLangText;
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
                     List<MCRMetaLangText> titles = derivateLinkID.getTitle();
                    titles.add(new MCRMetaLangText("title", "zxx", "maindoc_size", 0, "", Long.toString(attrs.size())));
                    titles.add(new MCRMetaLangText("title", "zxx", "maindoc_md5", 0, "", attrs.md5sum()));
                    derivateLinkID.setTitles(titles);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
            else {
                LOGGER.error("Error - maindoc '" + mainDoc + "' does not exist for " + der.getId().toString());
            }
        }

        return derivateLinkID;
    }
}
