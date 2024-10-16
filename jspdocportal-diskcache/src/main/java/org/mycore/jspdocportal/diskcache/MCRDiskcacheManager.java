package org.mycore.jspdocportal.diskcache;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.diskcache.disklru.DiskLruCache;

public class MCRDiskcacheManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final MCRDiskcacheManager SINGLETON = new MCRDiskcacheManager();
    private static final String MCR_PROPERTY_CONFIG_PREFIX = "MCR.Diskcache.Cache.";

    private Map<String, MCRDiskcacheConfig> caches = new HashMap<>();

    private MCRDiskcacheManager() {
        MCRConfiguration2.getString("MCR.Diskcache.EnabledCaches").ifPresent(x -> {
            Arrays.asList(x.split(",")).forEach(c -> {
                caches.put(c,
                    MCRConfiguration2.getInstanceOf(MCRDiskcacheConfig.class, MCR_PROPERTY_CONFIG_PREFIX + c + ".Class")
                        .orElseThrow(() -> MCRConfiguration2
                            .createConfigurationException(MCR_PROPERTY_CONFIG_PREFIX + c + ".Class")));
            });
            LOGGER.warn("Info: DiskCacheConfiguration loaded");
            LOGGER.warn("-----------------------------------");
            for (Entry<String, MCRDiskcacheConfig> e : caches.entrySet()) {
                DiskLruCache c = e.getValue().getCache();
                String size = c == null ? "null" : Long.toString(c.size());
                LOGGER.warn(e.getKey() + ":: urlSuffix: " + e.getValue().getURLSuffix()
                    + " / cacheObject: " + c
                    + " / currentCacheSize: " + size);
            }
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
