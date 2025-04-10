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

        final Element identifierElement = wrapper.getElement(getXPath());
        final String type = getIdentifierType();

        if (identifierElement != null) {
            if (!identifier.asString().equals(identifierElement.getText())) {
                LOGGER.warn("{} with prefix {} already exist with value {}! - It will be replaced with {}",
                    () -> type, () -> getProperties().get(PREFIX_PROPERTY_KEY),
                    identifierElement::getText, identifier::asString);
                identifierElement.setText(identifier.asString());
            }
        } else {
            Element idElem = new Element("identifier", MCRConstants.MODS_NAMESPACE);
            idElem.setAttribute("type", type);
            idElem.setText(identifier.asString());
            wrapper.addElement(idElem);
        }
    }
}
