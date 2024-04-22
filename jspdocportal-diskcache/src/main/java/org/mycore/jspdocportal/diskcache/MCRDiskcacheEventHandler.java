package org.mycore.jspdocportal.diskcache;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;

public class MCRDiskcacheEventHandler extends MCREventHandlerBase {

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
            .forEach((id, cache) -> {
                cache.removeCachedFile(obj.getId().toString());

            });
    }
}
