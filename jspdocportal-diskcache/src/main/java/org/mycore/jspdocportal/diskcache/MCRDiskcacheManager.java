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

public final class MCRDiskcacheManager {
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
                String urlSuffix = e.getValue().getURLSuffix();
                String key = e.getKey();
                LOGGER.warn("{} :: urlSuffix: {} / cacheObject: {} / currentCacheSize: {}",
                    key, urlSuffix, c, size);
            }
        });
    }

    public static MCRDiskcacheManager getInstance() {
        return SINGLETON;
    }

    public Map<String, MCRDiskcacheConfig> getCaches() {
        return caches;
    }

    public Path retrieveCachedFile(String cacheId, String objectId) {
        return caches.get(cacheId).retrieveCachedFile(objectId);
    }

    public void removeCachedFile(String cacheId, String objectId) {
        caches.get(cacheId).removeCachedFile(objectId);
    }

}
