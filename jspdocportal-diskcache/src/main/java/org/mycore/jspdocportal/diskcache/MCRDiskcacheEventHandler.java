package org.mycore.jspdocportal.diskcache;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

public class MCRDiskcacheEventHandler extends MCREventHandlerBase{
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        handleObjectUpdated(evt, obj);
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        handleObjectUpdated(evt, obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRDiskcacheManager.instance().getCaches()
        .forEach((c,a) -> {
           a.removeCachedFile(obj.getId().toString());
           
        });
    }
}
