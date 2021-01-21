/*
 * $RCSfile$
 * $Revision$ $Date$
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

package org.mycore.frontend.servlets;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;

/**
 * This servlet delivers the Webcontent of the application as a zipfile
 * All webfile, that can be changed by the webcontent editor will be saved
 * 
 * call the Servlet via Browser:
 * BaseURL/zipwebcontent
 * 
 * @author Robert Stephan
 * 
 */
public class MCRZipWebcontentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = LogManager.getLogger(MCRZipWebcontentServlet.class);

    /**
     * Handles the HTTP request
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            byte[] buffer = new byte[18024];
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            String zipfilename = "webcontent_" + formatter.format(new Date()) + ".zip";
            ZipOutputStream out = buildZipOutputStream(response, zipfilename);
            String foldername = "content/" + MCRConfiguration2.getString("MCR.WebContent.Folder").orElseThrow();
            File webcontentDir = new File(getServletContext().getRealPath(foldername));
            int rootPathLength = webcontentDir.getParent().length();

            ArrayList<File> files = new ArrayList<File>();
            collectAllFiles(webcontentDir, files);
            Iterator<?> it = files.iterator();
            while (it.hasNext()) {
                File f = (File) it.next();
                FileInputStream in = new FileInputStream(f);
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(f.getPath().substring(rootPathLength, f.getPath().length())));
                //write file into zip
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                // Close the current entry
                out.closeEntry();

                // Close the current file input stream
                in.close();
            }
            out.close();

        } catch (Exception e) {
            String msg = "Das Zip-File konnte nicht ordnungsgemäss erstellt werden, "
                    + "Bitte überprüfen Sie die eingegebenen Parameter";
            response.reset();
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            } catch (Exception e2) {
                LOGGER.error("Fehler", e2);
            }
        }
    }

    private void collectAllFiles(File file, List<File> l) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                collectAllFiles(subFiles[i], l);
            }
        }
        if (file.isFile()) {
            l.add(file);
        }
    }

    /**
     * buildZipOutputStream sets the contenttype and name of the zip-file
     * Returns the ZipOutputStream
     * 
     * @param filename the name of the zipfile
     */
    protected ZipOutputStream buildZipOutputStream(HttpServletResponse res, String filename) throws IOException {
        res.setContentType("multipart/x-zip");
        res.addHeader("Content-Disposition", "atachment; filename=\"" + filename + "\"");

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(res.getOutputStream()));
        out.setLevel(Deflater.BEST_COMPRESSION);

        return out;
    }
}
