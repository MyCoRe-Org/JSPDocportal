package org.mycore.jspdocportal.ir.pi.local;

import java.util.Objects;
import java.util.Optional;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.pi.MCRPIMetadataService;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRLocalIDMetadataService extends MCRPIMetadataService<MCRLocalID> {

    @Override
    public void insertIdentifier(MCRLocalID identifier, MCRBase base, String additional)
        throws MCRPersistentIdentifierException {
        MCRObject mcrObj = checkObject(base);

        MCRMetaXML mcrMODS = (MCRMetaXML) mcrObj.getMetadata().findFirst("def.modsContainer").get();
        Element eMods = (Element) mcrMODS.getContent().stream().filter(x -> x.getClass().equals(Element.class))
            .findFirst().get();

        Element eRecordInfo = eMods.getChild("recordInfo", MCRConstants.MODS_NAMESPACE);
        if (eRecordInfo == null) {
            eRecordInfo = new Element("recordInfo", MCRConstants.MODS_NAMESPACE);
            eMods.addContent(0, eRecordInfo);
        }

        eRecordInfo.removeChildren("recordIdentifier", MCRConstants.MODS_NAMESPACE);
        Element eRecordIdentifier = new Element("recordIdentifier", MCRConstants.MODS_NAMESPACE);
        if (getProperties().containsKey("Source")) {
            eRecordIdentifier.setAttribute("source", getProperties().get("Source"));
        }
        eRecordIdentifier.setText(identifier.asString());
        eRecordInfo.addContent(eRecordIdentifier);
    }

    private MCRObject checkObject(MCRBase base) throws MCRPersistentIdentifierException {
        if (!(base instanceof MCRObject)) {
            throw new MCRPersistentIdentifierException(getClass().getName() + " does only support MyCoReObjects!");
        }
        return (MCRObject) base;
    }

    @Override
    public void removeIdentifier(MCRLocalID identifier, MCRBase obj, String additional) {
        // not supported
    }

    @Override
    public Optional<MCRPersistentIdentifier> getIdentifier(MCRBase base, String additional)
        throws MCRPersistentIdentifierException {
        Element element = null;
        try {
            MCRObject mcrObj = checkObject(base);

            MCRMetaXML mcrMODS = (MCRMetaXML) mcrObj.getMetadata().findFirst("def.modsContainer").get();
            Element eMods = (Element) mcrMODS.getContent().stream().filter(x -> x.getClass().equals(Element.class))
                .findFirst().get();

            element = eMods.getChild("recordInfo", MCRConstants.MODS_NAMESPACE).getChild("recordIdentifier",
                MCRConstants.MODS_NAMESPACE);
        } catch (Exception e) {
            //ignore, use default=null;
        }
        if (element == null) {
            return Optional.empty();
        }

        String idText = element.getTextNormalize();

        return new MCRLocalIDParser()
            .parse(idText)
            .filter(Objects::nonNull)
            .map(MCRLocalID.class::cast);
    }
}
