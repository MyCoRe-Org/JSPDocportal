package org.mycore.jspdocportal.ir.pi;

import org.mycore.common.events.MCREvent;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.pi.MCRPICreationEventHandler;
import org.mycore.pi.MCRPIService;

public class MCRJSPPICreationEventhandler extends MCRPICreationEventHandler {

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        if(obj.isImportMode()) {
            MCRPIService.updateFlagsInDatabase(obj);
        }
        super.handleObjectCreated(evt, obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if(obj.isImportMode()) {
            MCRPIService.updateFlagsInDatabase(obj);
        }
        super.handleObjectUpdated(evt, obj);
    }
}
