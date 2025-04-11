package org.mycore.jspdocportal.diskcache;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.idmapper.MCRIDMapper;
import org.mycore.jspdocportal.diskcache.servlet.FileServlet;
import org.mycore.jspdocportal.diskcache.servlet.FileServletData;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet implementation class FileDownloadTestServlet
 */

public class MCRDiskcacheDownloadServlet extends FileServlet {
    
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private MCRIDMapper mcrIdMapper
        = MCRConfiguration2.getInstanceOf(MCRIDMapper.class, MCRIDMapper.MCR_PROPERTY_CLASS).get();

    @Override
    public void init() throws ServletException {

    }

    @Override
    protected FileServletData getFileData(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Map<String, MCRDiskcacheConfig> caches = MCRDiskcacheManager.getInstance().getCaches();
        Optional<Entry<String, MCRDiskcacheConfig>> oCache = caches.entrySet()
            .stream().filter(e -> pathInfo.endsWith(e.getValue().getURLSuffix())).findFirst();
        if (oCache.isPresent()) {
            MCRDiskcacheConfig cache = oCache.get().getValue();
            String id = pathInfo.substring(1, pathInfo.length() - cache.getURLSuffix().length());
            Optional<MCRObjectID> oMcrObjId = mcrIdMapper.mapMCRObjectID(id.replace("rosdok/ppn", "rosdok_ppn"));
            String objId = oMcrObjId.map(MCRObjectID::toString).orElse(id);
            Path file = MCRDiskcacheManager.getInstance().retrieveCachedFile(cache.getId(), objId);
            return new FileServletData(file, cache.getMimeType(), pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
        }
        return null;
    }
}
