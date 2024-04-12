package org.mycore.jspdocportal.diskcache;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.mycore.jspdocportal.diskcache.servlet.FileServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet implementation class FileDownloadTestServlet
 */

public class MCRDiskcacheDownloadServlet extends FileServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {

    }

    //TODO null Handling
    @Override
    protected Path getFile(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Map<String, MCRDiskcacheConfig> caches = MCRDiskcacheManager.instance().getCaches();
        Optional<Entry<String, MCRDiskcacheConfig>> oCache = caches.entrySet()
            .stream().filter(e -> pathInfo.endsWith(e.getValue().getFileName())).findFirst();
        if (oCache.isPresent()) {
            String objectId = pathInfo.substring(1, pathInfo.length() - oCache.get().getValue().getFileName().length());
            Path file = MCRDiskcacheManager.instance().retrieveCachedFile(oCache.get().getValue().getId(), objectId);
            return file;
        }
        return null;
    }

}
