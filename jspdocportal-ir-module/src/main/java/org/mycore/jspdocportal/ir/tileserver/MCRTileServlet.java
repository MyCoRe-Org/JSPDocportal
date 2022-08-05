/*
 * $Id: MCRTileServlet.java 30904 2014-10-20 14:22:26Z mcrtchef $
 * $Revision: 30904 $ $Date: 2014-10-20 16:22:26 +0200 (Mo, 20 Okt 2014) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.jspdocportal.ir.tileserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.imagetiler.MCRTiledPictureProps;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Get a specific tile of an image.
 * @author Thomas Scheffler (yagee)
 *
 */
public class MCRTileServlet extends HttpServlet {
    /**
     * how long should a tile be cached by the client
     */
    static final int MAX_AGE = 60 * 60 * 24 * 365; // one year

    private static final long serialVersionUID = 3805114872438336791L;

    private static final Logger LOGGER = LogManager.getLogger(MCRTileServlet.class);

    private MCRTileFileProvider tfp = new MCRStandardTileFileProvider();

    /**
     * Extracts tile or image properties from iview2 file and transmits it.
     * 
     * Uses {@link HttpServletRequest#getPathInfo()} (see {@link #getTileInfo(String)}) to get tile attributes.
     * Also uses {@link #MAX_AGE} to tell the client how long it could cache the information.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final TileInfo tileInfo = getTileInfo(getPathInfo(req));
        Path iviewFile = getTileFile(tileInfo);
        if (iviewFile == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File does not exist - Invalid request!");
            return;
        }
        if (!Files.exists(iviewFile)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File does not exist: " + iviewFile.toString());
            return;
        }
        try (FileSystem iviewFS = getFileSystem(iviewFile)) {
            Path root = iviewFS.getRootDirectories().iterator().next();
            Path tilePath = root.resolve(tileInfo.tile);
            BasicFileAttributes fileAttributes;
            try {
                fileAttributes = Files.readAttributes(tilePath, BasicFileAttributes.class);
            } catch (IOException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Tile not found: " + tileInfo);
                return;
            }
            resp.setHeader("Cache-Control", "max-age=" + MAX_AGE);
            resp.setDateHeader("Last-Modified", fileAttributes.lastModifiedTime().toMillis());
            if (tileInfo.tile.endsWith("xml")) {
                resp.setContentType("text/xml");
            } else {
                resp.setContentType("image/jpeg");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Extracting " + tilePath + " size " + fileAttributes.size());
            }
            //size of a tile or imageinfo.xml file is always smaller than Integer.MAX_VALUE
            resp.setContentLength((int) fileAttributes.size());
            try (ServletOutputStream out = resp.getOutputStream()) {
                Files.copy(tilePath, out);
            }

        }
        LOGGER.debug("Ending MCRTileServlet");
    }

    /**
     * Returns at which time the specified tile (see {@link #doGet(HttpServletRequest, HttpServletResponse)} was last modified.
     */
    @Override
    protected long getLastModified(final HttpServletRequest req) {
        final TileInfo tileInfo = getTileInfo(getPathInfo(req));
        try {
            Path p = getTileFile(tileInfo);
            if (p!=null && Files.exists(p)) {
                return Files.getLastModifiedTime(p).toMillis();
            } else {
                return -1;
            }
        } catch (IOException e) {
            LOGGER.warn("Could not get lastmodified time.", e);
            return -1;
        }
    }

    /**
     * returns PathInfo from request including ";"
     * @param request
     * @return
     */
    private static String getPathInfo(final HttpServletRequest request) {
        return request.getPathInfo();
    }

    /**
     * returns a {@link TileInfo} for this <code>pathInfo</code>.
     * The format of <code>pathInfo</code> is
     * <code>/{derivateID}/{absoluteImagePath}/{tileCoordinate}</code>
     * where <code>tileCoordinate</code> is either {@value MCRTiledPictureProps#IMAGEINFO_XML} or <code>{z}/{y}/{x}</code> as zoomLevel and x-y-coordinates.
     * @param pathInfo of the described format
     * @return a {@link TileInfo} instance for <code>pathInfo</code>
     */
    static TileInfo getTileInfo(final String pathInfo) {
        LOGGER.debug("Starting MCRTileServlet: " + pathInfo);
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        String tile = "";
        String imagePath = "";
        String derivate = "";
        if (path.contains("/")) {
            derivate = path.substring(0, path.indexOf('/'));
            imagePath = path.substring(derivate.length());
            if (imagePath.endsWith(".xml")) {
                tile = imagePath.substring(imagePath.lastIndexOf('/') + 1);
                imagePath = imagePath.substring(0, imagePath.length() - tile.length() - 1);
            } else {
                int pos = imagePath.length();
                int cnt = 0;
                while (--pos > 0 && cnt < 3) {
                    switch (imagePath.charAt(pos)) {
                        case '/':
                            cnt++;
                            break;
                        default:
                    }
                }
                if (imagePath.length() > pos + 1) {
                    tile = imagePath.substring(pos + 2);
                    imagePath = imagePath.substring(0, pos + 1);
                }
            }
        }
        final TileInfo tileInfo = new TileInfo(derivate, imagePath, tile);
        return tileInfo;
    }

    private Path getTileFile(TileInfo tileInfo) {
        return tfp.getTileFile(tileInfo.derivate, tileInfo.imagePath);
        //move into MCRTileProvider Implementation: 
        //return MCRImage.getTiledFile(MCRIView2Tools.getTileDir(), tileInfo.derivate, tileInfo.imagePath);
    }

    /**
     * Holds all attributes for a specific tile.
     * @author Thomas Scheffler (yagee)
     *
     */
    static class TileInfo {
        String derivate, imagePath, tile;

        public TileInfo(final String derivate, final String imagePath, final String tile) {
            this.derivate = derivate;
            this.imagePath = imagePath;
            this.tile = tile;
        }

        /**
         * returns "TileInfo [derivate=" + derivate + ", imagePath=" + imagePath + ", tile=" + tile + "]"
         */
        @Override
        public String toString() {
            return "TileInfo [derivate=" + derivate + ", imagePath=" + imagePath + ", tile=" + tile + "]";
        }
    }

    public static FileSystem getFileSystem(Path iviewFile) throws IOException {
        URI uri = URI.create("jar:" + iviewFile.toUri().toString());
        try {
            return FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap(),
                MCRTileServlet.class.getClassLoader());
        } catch (FileSystemAlreadyExistsException exc) {
            // block until file system is closed
            try {
                FileSystem fileSystem = FileSystems.getFileSystem(uri);
                while (fileSystem.isOpen()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        // get out of here
                        throw new IOException(ie);
                    }
                }
            } catch (FileSystemNotFoundException fsnfe) {
                // seems closed now -> do nothing and try to return the file system again
                LOGGER.debug("Filesystem not found", fsnfe);
            }
            return getFileSystem(iviewFile);
        }
    }

    @Override
    public void init() throws ServletException {
        String clazz = getServletConfig().getInitParameter("tileFileProvider");
        if (clazz != null) {
            try {
                tfp = (MCRTileFileProvider) Class.forName(clazz).getDeclaredConstructor(new Class[] {}).newInstance();
            } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                //ignore;
            }
        }
        super.init();
    }

    class MCRStandardTileFileProvider implements MCRTileFileProvider {

        @Override
        public Path getTileFile(String derivate, String image) {
            Path tileDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tiles");
            return tileDir.resolve(derivate).resolve(image);
        }

    }

}
