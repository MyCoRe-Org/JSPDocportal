package org.mycore.jspdocportal.diskcache;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mycore.common.config.MCRConfiguration2;

public class MCRDiskcacheManager {
    private static final MCRDiskcacheManager SINGLETON = new MCRDiskcacheManager();
    private static final String MCR_PROPERTY_CONFIG_PREFIX = "MCR.Diskcache.Cache.";

    private Map<String, MCRDiskcacheConfig> caches = new HashMap<>();

    private MCRDiskcacheManager() {
        MCRConfiguration2.getString("MCR.Diskcache.EnabledCaches").ifPresent(x -> {
            Arrays.asList(x.split(",")).forEach(c -> {
                caches.put(c,
                    MCRConfiguration2.<MCRDiskcacheConfig>getInstanceOf(MCR_PROPERTY_CONFIG_PREFIX + c + ".Class")
                        .orElseThrow(() -> MCRConfiguration2
                            .createConfigurationException(MCR_PROPERTY_CONFIG_PREFIX + c + ".Class")));
            });
        });
    }

    public static MCRDiskcacheManager instance() {
        return SINGLETON;
    }

    public Map<String, MCRDiskcacheConfig> getCaches() {
        return caches;
    }

    public Path retrieveCachedFile(String cacheId, String objectId) {
        Path p = caches.get(cacheId).retrieveCachedFile(objectId);
        return p;

    }

    public void removeCachedFile(String cacheId, String objectId) {
        caches.get(cacheId).removeCachedFile(objectId);
    }

}
