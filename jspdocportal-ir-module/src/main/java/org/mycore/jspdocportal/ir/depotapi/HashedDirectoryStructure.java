/*
 * $RCSfile$
 * $Revision: 25918 $ $Date: 2013-01-24 13:20:01 +0100 (Do, 24 Jan 2013) $
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

package org.mycore.jspdocportal.ir.depotapi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class produces a hashed directory structure for a given recordIdentifier.
 * 
 * The recordIdentifier may have the following syntax: 
 * {project_id} + "/" + {local_id}
 * 
 * The project_id is optional. The local_id could be a UUID or of the structure:
 * {prefix} + {number}
 * 
 * The resulting path will create subdirectories for the projectID and chunks of
 * the first 2 and after that the first 5 numbers of the local_id. A prefix may
 * be ignored.
 * 
 * Look at the following examples: 
 *   12345678            -> \12\12345\12345678 
 *   pref12345678        -> \pref12\pref12345\pref12345678 
 *   rosdok/id12345678   -> \rosdok\id12\id12345\id12345678 
 *   rosdok/ppn12345678X -> \rosdok\ppn12\ppn12345\ppn12345678X 
 *   darl/rlbtext02      -> \darl\rlbtext02\rlbtext02 
 *   darl/3dfab9ef-e90b-4fa6-8f02-4b34f0206c25 -> \darl\3d\3dfab\3dfab9ef-e90b-4fa6-8f02-4b34f0206c25
 * 
 * @author Robert Stephan
 *
 */
public class HashedDirectoryStructure {
    public static final Pattern UUID_PATTERN = Pattern
            .compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

    //ppn30 - ppn299 - TODO better to start splitting by counting from endfrom back
    public static final Pattern ID_START_SPLITT_PATTERN = Pattern.compile("^([a-zA-Z]*[1-2]?[0-9]{2})");


    /**
     * 
     * @param baseDir
     *            - the root directory
     * @param recordIdentifier
     *            the record identifier
     * @return
     */
    public static Path createOutputDirectory(Path baseDir, String recordIdentifier) {
        if(!recordIdentifier.contains("/")) {
            recordIdentifier = recordIdentifier.replaceFirst("_", "/");
        }
        Path currentDir = baseDir;
        for (String s : recordIdentifier.split("\\/")) {
            currentDir = currentDir.resolve(s);
        }

        Path result = null;
        Matcher m = ID_START_SPLITT_PATTERN.matcher(currentDir.getFileName().toString());
        if (!UUID_PATTERN.matcher(currentDir.getFileName().toString()).matches() && m.find()) {
            String start = m.group();
            Path parentDir = currentDir;
            Path grandParentDir = parentDir.getParent();
            String parentDirName = parentDir.getFileName().toString();
            Path upperParentDir = grandParentDir;
            if (parentDirName.length() >= start.length()) {
                upperParentDir = grandParentDir.resolve(parentDirName.substring(0, start.length()));
            }
            if (parentDirName.length() > start.length() + 3) {
                upperParentDir = upperParentDir.resolve(parentDirName.substring(0, start.length() + 3));
            }
            result = upperParentDir.resolve(parentDir.getFileName());
        } else {
            Path parentDir = currentDir;
            Path grandParentDir = parentDir.getParent();
            String parentDirName = parentDir.getFileName().toString();
            Path upperParentDir = grandParentDir.resolve(parentDirName);
            if (parentDirName.length() >= 2) {
                upperParentDir = grandParentDir.resolve(parentDirName.substring(0, 2));
            }
            if (parentDirName.length() > 5) {
                upperParentDir = upperParentDir.resolve(parentDirName.substring(0, 5));
            }

            result = upperParentDir.resolve(currentDir.getFileName());
        }
        return result;
    }

    /**
     * Some examples and test of the algorithm ...
     * 
     * @param args
     *            - none
     */
    public static void main(String[] args) {
        // testing ...
        String r = "rosdok/id123456789";
        Path p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(
                r + " : " + p.toString() + " -> " + p.equals(Paths.get("/depot/rosdok/id12/id12345/id12345678")));
        r = "rosdok/ppn102345678X";
        p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(
                r + " : " + p.toString() + " -> " + p.equals(Paths.get("/depot/rosdok/ppn12/ppn12345/ppn12345678X")));
        r = "darl/rlbtext02";
        p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(r + " : " + p.toString() + " -> " + p.equals(Paths.get("/depot/darl/rlbtext02")));
        r = "darl/3dfab9ef-e90b-4fa6-8f02-4b34f0206c25";
        p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(r + " : " + p.toString() + " -> "
                + p.equals(Paths.get("/depot/darl/3d/3dfab/3dfab9ef-e90b-4fa6-8f02-4b34f0206c25")));
        r = "12345678";
        p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(r + " : " + p.toString() + " -> " + p.equals(Paths.get("/depot/12/12345/12345678")));
        r = "prefix12345678";
        p = createOutputDirectory(Paths.get("/depot"), r);
        System.out.println(
                r + " : " + p.toString() + " -> " + p.equals(Paths.get("/depot/prefix12/prefix12345/prefix12345678")));
    }
}
