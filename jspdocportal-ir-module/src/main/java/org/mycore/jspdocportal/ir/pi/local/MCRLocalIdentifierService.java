package org.mycore.jspdocportal.ir.pi.local;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRLocalIdentifierService extends MCRPIService<MCRLocalID> {
    public static final String DEFAULT_ID = "MCRLocalID";
    
    public MCRLocalIdentifierService() {
        super(MCRLocalID.TYPE);
    }

    @Override
    protected void registerIdentifier(MCRBase obj, String additional, MCRLocalID pi)
        throws MCRPersistentIdentifierException {
        Optional<MCRPersistentIdentifier> oPI = getMetadataService().getIdentifier(obj, additional);
        if (oPI.isPresent() && oPI.get() instanceof MCRLocalID) {
            //do nothing
        } else {
            getNewIdentifier(obj, additional);
        }
    }

    @Override
    protected void update(MCRLocalID identifier, MCRBase obj, String additional)
        throws MCRPersistentIdentifierException {
        //do nothing

    }

    @Override
    protected void delete(MCRLocalID identifier, MCRBase obj, String additional)
        throws MCRPersistentIdentifierException {
        // do nothing

    }

}
