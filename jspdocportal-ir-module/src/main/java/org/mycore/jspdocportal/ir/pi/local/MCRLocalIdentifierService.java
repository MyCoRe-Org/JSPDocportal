package org.mycore.jspdocportal.ir.pi.local;

import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.MCRPIServiceDates;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRLocalIdentifierService extends MCRPIService<MCRLocalID> {
    public static final String DEFAULT_ID = "MCRLocalID";
    
    public MCRLocalIdentifierService() {
        super(MCRLocalID.TYPE);
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

    @Override
    protected MCRPIServiceDates registerIdentifier(MCRBase obj, String additional, MCRLocalID pi)
        throws MCRPersistentIdentifierException {
        //do nothing
        return new MCRPIServiceDates(null, null);
    }

}
