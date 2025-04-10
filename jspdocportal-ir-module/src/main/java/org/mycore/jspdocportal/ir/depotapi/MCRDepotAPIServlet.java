/* $Revision: $ $Date: $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 */

package org.mycore.jspdocportal.ir.depotapi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet delivers content from the depot directory
 * which contains additional files, that are not stored in MyCoRe derivates.
 * 
 * @author Robert Stephan
 * @version $Revision: $ $Date: $
 */
public class MCRDepotAPIServlet extends HttpServlet {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger();

    private MCRDepotAPIConfiguration depotAPIConf = new MCRDepotAPIDefaultConfiguration();

    /**
     * The initialization method of the servlet.
     * uses the servlet config parameter {@code config.class} 
     * 
     * @see jakarta.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            String clazz = getServletConfig().getInitParameter("config.class");
            if (clazz != null) {
                Class<?> c = Class.forName(clazz);
                if (MCRDepotAPIConfiguration.class.isAssignableFrom(c)) {
                    depotAPIConf = (MCRDepotAPIConfiguration) c.getDeclaredConstructor(new Class[] {}).newInstance();
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            //do nothing
        }
    }

    /**
     * This methods returns the resource from the depot directory
     * It makes use of the standard HTTP caching headers {@code Expires, Last-Modified, Etag}.
     * 
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        LOGGER.debug("contextPath={}", request::getContextPath);
        String path = request.getPathInfo();

        if (path != null) {
            try {
                Path file = depotAPIConf.resolveFile(path);
                if (file == null) {
                    try {
                        response.sendError(404, "Resource not found!");
                        return;
                    } catch (IOException e) {
                        LOGGER.error(e);
                    }
                }
                if (depotAPIConf.getMaxEtagAgeInMillis() > 0) {
                    String ifEtag = request.getHeader("If-None-Match");
                    long ifModifiedSince = request.getDateHeader("If-Modified-Since");

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String md5 = String.format(Locale.ENGLISH, "%032x",
                        new BigInteger(1, md.digest(path.getBytes(StandardCharsets.UTF_8))));

                    long lastModified = Files.getLastModifiedTime(file).toMillis();
                    long current = System.currentTimeMillis();
                    String etag = md5 + "_" + current;
                    if (ifEtag != null && ifModifiedSince > 0) {
                        if (validateEtag(md5, current, ifEtag) && lastModified < ifModifiedSince + 1000) {
                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            return;
                        }
                    }
                    response.setDateHeader("Last-Modified", lastModified);
                    response.setHeader("Etag", etag);
                }

                if (depotAPIConf.getMaxBrowserCacheAgeInMillis() > 0) {
                    response.setDateHeader("Expires",
                        System.currentTimeMillis() + depotAPIConf.getMaxBrowserCacheAgeInMillis());
                    response.setHeader("Cache-Control",
                        "max-age=" + depotAPIConf.getMaxBrowserCacheAgeInMillis() / 1000);
                }
                try (OutputStream out = new BufferedOutputStream(response.getOutputStream())) {
                    response.setContentType(getServletContext().getMimeType(file.getFileName().toString()));
                    response.setContentLength(Long.valueOf(Files.size(file)).intValue());
                    Files.copy(file, out);
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Validates the Etag
     * For the implementation our Etags contain the encoded creation time.
     * So it is possible to ignore Etags of a certain age.
     * @param md5 - the md5 sum of the resource that should be returned
     * @param currentTime - the current time stamp
     * @param checkEtag - the Etag to check 
     * @return
     */
    private boolean validateEtag(String md5, long currentTime, String checkEtag) {
        try {
            String[] data = checkEtag.split("_");
            if (data.length == 2) {
                long time = Long.parseLong(data[1]);
                if (md5.equals(data[0]) && time + depotAPIConf.getMaxEtagAgeInMillis() > currentTime) {
                    return true;
                }
            }
        } catch (Exception e) {
            //do nothing    
        }
        return false;
    }
}
