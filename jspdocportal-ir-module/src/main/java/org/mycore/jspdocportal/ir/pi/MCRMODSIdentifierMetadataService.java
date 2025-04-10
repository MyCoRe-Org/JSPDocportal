package org.mycore.jspdocportal.ir.pi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.identifier.MCRAbstractMODSMetadataService;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRMODSIdentifierMetadataService extends MCRAbstractMODSMetadataService {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void insertIdentifier(MCRPersistentIdentifier identifier, MCRBase base, String additional)
        throws MCRPersistentIdentifierException {
        MCRObject object = checkObject(base);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(object);

        Element identifierElement = wrapper.getElement(getXPath());
        final String type = getIdentifierType();

        if (identifierElement != null) {
            if (!identifier.asString().equals(identifierElement.getText())) {
                LOGGER.warn(
                    type + " with prefix " + getProperties().get(PREFIX_PROPERTY_KEY) + " already exist with value "
                        + identifierElement.getText() + "! - It will be replaced with " + identifier.asString());
                identifierElement.setText(identifier.asString());
            }
        }
        else {
            identifierElement = new Element("identifier", MCRConstants.MODS_NAMESPACE);
            identifierElement.setAttribute("type", type);
            identifierElement.setText(identifier.asString());
            wrapper.addElement(identifierElement);
        }
    }
}
