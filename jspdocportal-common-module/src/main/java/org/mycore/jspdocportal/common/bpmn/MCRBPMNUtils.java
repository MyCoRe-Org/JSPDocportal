package org.mycore.jspdocportal.common.bpmn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.utils.MCRRecursiveDeleter;

/**
 * provides some static utility methods
 * 
 * @author Robert Stephan
 * 
 */
public class MCRBPMNUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * saves a given MCR object into the workflow directory
     * @param mcrObj - the MyCoRe object
     */
    public static void saveMCRObjectToWorkflowDirectory(MCRObject mcrObj) {
        Path wfObjFile = getWorkflowObjectFile(mcrObj.getId());
        try (BufferedWriter bw = Files.newBufferedWriter(wfObjFile, StandardCharsets.UTF_8)) {
            XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
            xmlOut.output(mcrObj.createXML(), bw);
        } catch (Exception ex) {
            throw new MCRException(
                "Cant save MCR Object " + mcrObj.getId().toString() + " as file " + wfObjFile.toString(), ex);
        }
    }

    public static MCRObject loadMCRObjectFromWorkflowDirectory(MCRObjectID mcrObjID) {
        MCRObject mcrObj = null;
        try {
            mcrObj = getWorkflowObject(mcrObjID);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return mcrObj;
    }

    /**
     * saves a given MCR object into the workflow directory
     * @param mcrDer - the MyCoRe derivate
     */
    public static void saveMCRDerivateToWorkflowDirectory(MCRDerivate mcrDer) {
        try (BufferedWriter bw = Files.newBufferedWriter(getWorkflowDerivateFile(mcrDer.getOwnerID(), mcrDer.getId()),
            StandardCharsets.UTF_8)) {
            XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
            xmlOut.output(mcrDer.createXML(), bw);
        } catch (Exception ex) {
            throw new MCRException(
                "Could not save MCR Derivate " + mcrDer.getId().toString() + " into workfow directory.", ex);
        }
    }

    public static MCRDerivate loadMCRDerivateFromWorkflowDirectory(MCRObjectID owner, MCRObjectID mcrderid) {
        try {
            Path wfFile = getWorkflowDerivateFile(owner, mcrderid);
            if (Files.exists(wfFile)) {
                return new MCRDerivate(wfFile.toUri());
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    private static Path getWorkflowDirectory(MCRObjectID mcrObjID) {
        String s = MCRConfiguration2.getString("MCR.Workflow.WorkflowDirectory").orElseThrow();
        Path p = Paths.get(s).resolve(mcrObjID.getTypeId());
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return p;
    }

    public static MCRObject getWorkflowObject(MCRObjectID mcrObjID) {
        try {
            return new MCRObject(getWorkflowObjectFile(mcrObjID).toUri());
        } catch (JDOMException | IOException e) {
            LOGGER.error(e);
            return null;
        }
    }

    public static Path getWorkflowObjectFile(MCRObjectID mcrObjID) {
        return getWorkflowDirectory(mcrObjID).resolve(mcrObjID.toString() + ".xml");
    }

    public static Document getWorkflowObjectXML(MCRObjectID mcrObjID) {
        Document doc = null;
        Path wfFile = getWorkflowObjectFile(mcrObjID);
        MCRPathContent mpc = new MCRPathContent(wfFile);
        try {
            doc = mpc.asXML();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return doc;
    }

    public static Path getWorkflowObjectDir(MCRObjectID mcrObjID) {
        Path p = getWorkflowDirectory(mcrObjID).resolve(mcrObjID.toString());
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return p;
    }

    public static Path getWorkflowDerivateFile(MCRObjectID mcrObjID, MCRObjectID mcrDerID) {
        return getWorkflowObjectDir(mcrObjID).resolve(mcrDerID.toString() + ".xml");
    }

    public static Document getWorkflowDerivateXML(MCRObjectID mcrObjID, MCRObjectID mcrDerID) {
        Document doc = null;
        Path wfFile = getWorkflowDerivateFile(mcrObjID, mcrDerID);
        MCRPathContent mpc = new MCRPathContent(wfFile);
        try {
            doc = mpc.asXML();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return doc;
    }

    public static Path getWorkflowDerivateDir(MCRObjectID mcrObjID, MCRObjectID mcrDerID) {
        Path p = getWorkflowObjectDir(mcrObjID).resolve(mcrDerID.toString());
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return p;
    }

    public static void cleanUpWorkflowDirForObject(MCRObjectID mcrObjID) {
        Path wfObjDir = getWorkflowObjectDir(mcrObjID);
        if (Files.exists(wfObjDir)) {
            try {
                Files.walkFileTree(wfObjDir, new MCRRecursiveDeleter());
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        Path wfObjFile = getWorkflowObjectFile(mcrObjID);

        if (Files.exists(wfObjFile)) {
            try {
                Files.delete(wfObjFile);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    public static void cleanupWorkflowDirForDerivate(MCRObjectID mcrObjID, MCRObjectID mcrDerID) {
        try {
            Path wfDerDir = getWorkflowDerivateDir(mcrObjID, mcrDerID);
            if (Files.exists(wfDerDir)) {
                Files.walkFileTree(wfDerDir, new MCRRecursiveDeleter());
            }
            Path wfDerFile = getWorkflowDerivateFile(mcrObjID, mcrDerID);
            if (Files.exists(wfDerFile)) {
                try {
                    Files.delete(wfDerFile);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public static Map<String, List<String>> getDerivateFiles(MCRObjectID mcrObjID) {
        Map<String, List<String>> result = new HashMap<>();
        Path baseDir = getWorkflowObjectDir(mcrObjID);
        MCRObject obj = loadMCRObjectFromWorkflowDirectory(mcrObjID);
        try {
            for (MCRMetaLinkID derID : obj.getStructure().getDerivates()) {
                String id = derID.getXLinkHref();
                List<String> fileNames = new ArrayList<>();
                try {
                    Path derDir = baseDir.resolve(id);

                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(derDir)) {
                        for (Path file : stream) {
                            fileNames.add(file.getFileName().toString());
                        }
                    } catch (IOException | DirectoryIteratorException e) {
                        LOGGER.error(e);
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
                result.put(id, fileNames);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return result;
    }

    public static void deleteDirectoryContent(Path path) {
        try {
            final Path rootPath = path;
            Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    if (!rootPath.equals(dir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
