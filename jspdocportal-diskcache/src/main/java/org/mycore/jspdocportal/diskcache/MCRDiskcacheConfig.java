package org.mycore.jspdocportal.diskcache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.annotation.MCRInstance;
import org.mycore.common.config.annotation.MCRPostConstruction;
import org.mycore.common.config.annotation.MCRProperty;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.jspdocportal.diskcache.disklru.DiskLruCache;
import org.mycore.jspdocportal.diskcache.disklru.DiskLruCache.Editor;
import org.mycore.jspdocportal.diskcache.disklru.DiskLruCache.Value;

public class MCRDiskcacheConfig {

    /** The logger */
    private static Logger LOGGER = LogManager.getLogger(MCRDiskcacheConfig.class);

    private static int DISK_LRUCACHE_VALUE_COUNT = 1;

    private String id;
    private Path baseDir;

    //TODO "defaultName" in MCRInstance
    @MCRInstance(name = "Generator", valueClass = BiConsumer.class)
    public BiConsumer<String, Path> generator;

    @MCRProperty(name = "FileName", defaultName = "MCR.Diskcache.Default.FileName")
    public String fileName;
    @MCRProperty(name = "MimeType", defaultName = "MCR.Diskcache.Default.MimeType")
    public String mimeType;

    private long livespanInMillis;
    private long maxSizeInBytes;

    //not directly supported, could be removed later
    private int maxCount;
    private int version;

    private DiskLruCache cache;

    @MCRProperty(name = "BaseDir", defaultName = "MCR.Diskcache.Default.BaseDir")
    public void setBaseDir(String dir) {
        baseDir = Paths.get(dir);
    }

    @MCRProperty(name = "LivespanInMillis", defaultName = "MCR.Diskcache.Default.LivespanInMillis")
    public void setLivespanInMillis(String sLivespanInMillis) {
        this.livespanInMillis = Long.parseLong(sLivespanInMillis);
    }

    @MCRProperty(name = "MaxSizeInBytes", defaultName = "MCR.Diskcache.Default.MaxSizeInBytes")
    public void setMaxSizeInBytes(String sMaxSizeInBytes) {
        this.maxSizeInBytes = Long.parseLong(sMaxSizeInBytes);
    }

    @MCRProperty(name = "MaxCount", defaultName = "MCR.Diskcache.Default.MaxCount")
    public void setMaxCount(String sMaxCount) {
        this.maxCount = Integer.parseInt(sMaxCount);
    }

    //Version des Caches -> beim Setzen einer neuen Nummer wird der Cache gelöscht
    @MCRProperty(name = "Version", defaultName = "MCR.Diskcache.Default.Version")
    public void setVersion(String sVersion) {
        this.version = Integer.parseInt(sVersion);
    }

    @MCRPostConstruction
    public void init(String property) {
        String p = property.endsWith(".Class") ? property.substring(0, property.length() - 6) : property;
        id = p.substring(p.lastIndexOf(".") + 1);
        try {
            Path cacheDir = baseDir.resolve(id);
            Files.createDirectories(cacheDir);
            cache = DiskLruCache.open(cacheDir, version, DISK_LRUCACHE_VALUE_COUNT, maxSizeInBytes);
            MCRShutdownHandler.getInstance().addCloseable(new MCRDiskLruCacheClosable(cache));
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public String getId() {
        return id;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getLivespanInMillis() {
        return livespanInMillis;
    }

    public long getMaxSizeInBytes() {
        return maxSizeInBytes;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public DiskLruCache getCache() {
        return cache;
    }

    private Value getFromCache(String key) {
        try {
            Value v = cache.get(key);
            if (v != null) {
                Path p = v.getFile(0);
                if (Files.getLastModifiedTime(p).toMillis() < System.currentTimeMillis() - livespanInMillis) {
                    cache.remove(key);
                    return null;
                }
            }
            return v;
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return null;
    }

    public synchronized Path retrieveCachedFile(String objectId) {
        Value v = getFromCache(objectId);
        if (v != null && v.getFile(0) != null) {
            return v.getFile(0);
        } else {
            //TODO Null-Check / Errorhandling
            return generateCachedFile(objectId);
        }
    }

    public Path generateCachedFile(String key) {
        Editor editor = null;
        Path p = null;
        try {
            editor = cache.edit(key);
            p = editor.getFile(0);
            generator.accept(key, p);
            editor.commit();
        } catch (IOException e) {
            if (editor != null) {
                editor.abortUnlessCommitted();
            }
        }
        return p;
    }

    public synchronized void removeCachedFile(String objectId) {
        try {
            cache.remove(objectId);
        } catch (IOException e) {
            LOGGER.error("Could not remove object " + objectId + " from cache " + getId(), e);
        }
    }

    class MCRDiskLruCacheClosable implements MCRShutdownHandler.Closeable {
        private DiskLruCache cache;

        public MCRDiskLruCacheClosable(DiskLruCache cache) {
            this.cache = cache;
        }

        @Override
        public void prepareClose() {
        }

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE;
        }

        @Override
        public void close() {
            String cacheId = cache.getDirectory().getFileName().toString();
            LOGGER.info("Shutting down DiskCache " + cacheId);
            try {
                cache.flush();
                cache.close();
            } catch (IOException e) {
                LOGGER.error("Error closing cache " + cacheId, e);
            }

        }
    }

}
