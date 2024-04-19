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
    private static final long serialVersionUID = 1L;
    
    private MCRIDMapper mcrIdMapper = MCRConfiguration2
        .<MCRIDMapper>getInstanceOf(MCRIDMapper.MCR_PROPERTY_CLASS).get();

    @Override
    public void init() throws ServletException {

    }

    @Override
    protected FileServletData getFileData(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Map<String, MCRDiskcacheConfig> caches = MCRDiskcacheManager.instance().getCaches();
        Optional<Entry<String, MCRDiskcacheConfig>> oCache = caches.entrySet()
            .stream().filter(e -> pathInfo.endsWith(e.getValue().getFileName())).findFirst();
        if (oCache.isPresent()) {
            MCRDiskcacheConfig cache = oCache.get().getValue();
            String id = pathInfo.substring(1, pathInfo.length() - cache.getFileName().length());
            Optional<MCRObjectID> mcrObjId = mcrIdMapper.mapMCRObjectID(id.replace("rosdok_ppn", "rosdok/ppn")); 
            if(mcrObjId.isPresent()) {
                Path file = MCRDiskcacheManager.instance().retrieveCachedFile(cache.getId(), mcrObjId.get().toString());
                return new FileServletData(file, cache.getMimeType(), pathInfo.substring(pathInfo.lastIndexOf("/")+1));
            }
        }
        return null;
    }
}
