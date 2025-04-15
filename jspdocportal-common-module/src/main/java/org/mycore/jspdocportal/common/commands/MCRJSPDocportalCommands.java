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

package org.mycore.jspdocportal.common.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.MCRRuleAccessInterface;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.datamodel.niofs.utils.MCRRecursiveDeleter;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.metamodel.EntityType;

/**
 * This class provides a set of commands specific to JSPDocportal
 * 
 *  
 * @author Heiko Helmbrecht
 * @author Robert Stephan
 * 
 * @version $Revision$ $Date$
 */

@MCRCommandGroup(name = "JSPDocportal Commands")
public class MCRJSPDocportalCommands extends MCRAbstractCommands {
    /** The logger */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The command deletes a process instance of the workflow engine
     * 	if you've got to do this, you must restart your application server
     * 	for reinitializing your caches
     * @param strProcessID
     *        		String processId as String
     * @throws MCRException
     */

    /**
    * Backups all objects of given type and their derivates into the following structure:
    *  - MCR_OBJECT_0001
    *    - MCR_DERIVATE_0001
    *      - file0001.pdf
    *      - file0002.pdf
    *    - MCR_DERIVATE_0002
    *      - file0003.txt
    *    - mcr_derivate_0001.xml
    *    - mcr_derivate_0002.xml
    *   - MCR_OBJECT_0002
    *     - MCR_DERIVATE_0003
    *       - file004.pdf
    *     - mcr_derivate_0003.xml
    *   - mcr_object_0001.xml
    *   - mcr_object_0002.xml
    * @param type
    *            the object type
    * @param dirname
    *            the filename to store the object 
    */
    @MCRCommand(syntax = "backup all objects of type {0} to directory {1}",
        help = "The command backups all objects of type {0} into the directory {1} including all derivates")
    public static final List<String> backupAllObjects(String type, String dirname) {
        // check dirname
        List<String> commandList = new ArrayList<>();
        Path dir = Paths.get(dirname);
        if (!Files.exists(dir)) {
            if (Files.exists(dir.getParent())) {
                try {
                    Files.createDirectory(dir);
                } catch (IOException e) {
                    LOGGER.error("Could not create directory {}", dirname, e);
                }

            }
        }
        if (Files.isDirectory(dir)) {
            for (String id : MCRXMLMetadataManager.getInstance().listIDsOfType(type)) {
                commandList.add("backup object " + id + " to directory " + dirname);
            }
        } else {
            LOGGER.error("{} is not a directory.", dirname);
        }
        return commandList;
    }

    @MCRCommand(syntax = "backup object {0} to directory {1}",
        help = "The command backups a single object {0} into the directory {1} including all derivates")
    public static final void backupObject(String id, String dirname) {
        Path dir = Paths.get(dirname);
        if (!Files.isDirectory(dir)) {
            LOGGER.error("{} is not a directory.", dirname);
            return;
        }
        // check dirname
        try {
            // if object do'snt exist - no exception is catched!
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));

            //               add ACL's
            if (MCRAccessManager.getAccessImpl() instanceof MCRRuleAccessInterface) {
                Iterator<String> it = MCRAccessManager.getPermissionsForID(id).iterator();
                while (it.hasNext()) {
                    String s = it.next();

                    Element rule = ((MCRRuleAccessInterface) MCRAccessManager.getAccessImpl()).getRule(id, s);
                    mcrObj.getService().addRule(s, rule);
                }
            }

            // build JDOM
            Document xml = mcrObj.createXML();

            Path xmlOutput = dir.resolve(id + ".xml");
            try (OutputStream out = Files.newOutputStream(xmlOutput)) {
                new org.jdom2.output.XMLOutputter(Format.getPrettyFormat()).output(xml, out);
            }

            MCRObjectStructure mcrStructure = mcrObj.getStructure();
            if (mcrStructure == null) {
                return;
            }
            for (MCRMetaLinkID derivate : mcrStructure.getDerivates()) {
                String derID = derivate.getXLinkHref();
                Path subdir = dir.resolve(mcrObj.getId().toString());
                Files.createDirectories(subdir);
                MCRDerivateCommands.exportWithStylesheet(derID, subdir.toAbsolutePath().toString(), null);
            }
            String canonicalPath = xmlOutput.toAbsolutePath().toString();
            LOGGER.info("Object {} saved to {}.", id, canonicalPath);
            LOGGER.info("");
        } catch (MCRException ex) {
            // ignore
        } catch (IOException ex) {
            LOGGER.error("Error writing file {}", id, ex);
        }
    }

    /**
     * Restore all MCRObject's from a directory with the following structure:
     *  - MCR_OBJECT_0001
     *    - MCR_DERIVATE_0001
     *      - file0001.pdf
     *      - file0002.pdf
     *    - MCR_DERIVATE_0002
     *      - file0003.txt
     *    - mcr_derivate_0001.xml
     *    - mcr_derivate_0002.xml
     *   - MCR_OBJECT_0002
     *     - MCR_DERIVATE_0003
     *       - file004.pdf
     *     - mcr_derivate_0003.xml
     *   - mcr_object_0001.xml
     *   - mcr_object_0002.xml
     *     
     *  @param dirname
     *            the directory name from where to restore the objects
     * 
     */
    @MCRCommand(syntax = "restore all objects from directory {0}",
        help = "The command restores all objects from directory {0} including all derivates")
    public static final List<String> restoreAllObjects(String dirname) {
        // check dirname
        List<String> commandList = new ArrayList<>();
        Path dir = Paths.get(dirname);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.sorted().filter(x -> !Files.isDirectory(x))
                    .forEach(f -> commandList.add("restore object from file " + f.toAbsolutePath().toString()));
            } catch (IOException e) {
                LOGGER.error(e);
            }
        } else {
            LOGGER.error("{} is not a directory.", dirname);
        }
        return commandList;
    }

    @MCRCommand(syntax = "restore object from file {0}",
        help = "The command restores a single object {0} including all derivates")
    public static final void restoreObject(String fileName) {
        //ignore directories
        Path objectFile = Paths.get(fileName);
        if (!Files.exists(objectFile) && !Files.isRegularFile(objectFile)) {
            LOGGER.error("{} is not a file.", fileName);
            return;
        }

        String fn = objectFile.getFileName().toString();
        String id = fn.substring(0, fn.lastIndexOf('.'));
        LOGGER.info(" ... processing object {}", id);
        try {
            MCRObject mcrObj = new MCRObject(objectFile.toUri());
            mcrObj.setImportMode(true); //true = servdates are taken from xml file;
            //clone derivateIDs
            List<MCRMetaLinkID> derivateIDs = new ArrayList<>(mcrObj.getStructure().getDerivates());
            mcrObj.getStructure().getDerivates().clear();
            MCRMetadataManager.update(mcrObj);

            //load derivates in the order specified in MCRObject
            Path objDir = objectFile.getParent().resolve(id);
            processDerivates(derivateIDs, objDir);

            //set AccessRules
            /*
            while (mcrObj.getService().getRulesSize() > 0) {
                MCRMetaAccessRule rule = mcrObj.getService().getRule(0);
                String permission = mcrObj.getService().getRulePermission(0);
            
                MCRAccessManager.updateRule(id, permission, rule.getCondition(), "");
                mcrObj.getService().removeRule(0);
            }
            */
            if (mcrObj.getService().getRulesSize() > 0) {
                LOGGER.warn("ACLS for {} ignored.", mcrObj::getId);
            }
        } catch (MCRAccessException | JDOMException | IOException e) {
            LOGGER.error(e);
        }
    }

    private static void processDerivates(List<MCRMetaLinkID> derivateIDs, Path objDir)
        throws MCRAccessException, IOException, JDOMException {
        if (Files.exists(objDir)) {
            for (MCRMetaLinkID derLinkID : derivateIDs) {
                MCRObjectID derID = derLinkID.getXLinkHrefID();
                LOGGER.info(" ... processing derivate {}", derID);
                if (MCRMetadataManager.exists(derID)) {
                    try {
                        MCRDerivateCommands.delete(derID.toString());
                    } catch (MCRPersistenceException mpe) {
                        LOGGER.error("Could not delete derivate {}", derID, mpe);
                    }
                }
                final Path f = objDir.resolve(derID.toString() + ".xml");
                LOGGER.info("Loading derivate {} : file exists?: {}",
                    f::toAbsolutePath, () -> Files.exists(f));

                MCRDerivate mcrDer = new MCRDerivate(f.toUri());
                mcrDer.setImportMode(true); //true = servdates are taken from xml file;

                // override creation dates with the information from the xml file
                Date dateCreated = mcrDer.getService().getDate("createdate");
                Files.walkFileTree(objDir.resolve(derID.toString()), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                        throws IOException {
                        if (attrs.creationTime().toMillis() > dateCreated.getTime()) {
                            BasicFileAttributeView basicView = Files.getFileAttributeView(dir,
                                BasicFileAttributeView.class);
                            basicView.setTimes(null, null, FileTime.fromMillis(dateCreated.getTime()));
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                        throws IOException {
                        LOGGER.info("Update create date of file: {}:{}", file::toString, attrs::creationTime);
                        if (attrs.creationTime().toMillis() > dateCreated.getTime()) {
                            BasicFileAttributeView basicView = Files.getFileAttributeView(file,
                                BasicFileAttributeView.class);
                            basicView.setTimes(null, null, FileTime.fromMillis(dateCreated.getTime()));
                        }
                        BasicFileAttributeView basicView = Files.getFileAttributeView(file,
                            BasicFileAttributeView.class);
                        FileTime creationTime = basicView.readAttributes().creationTime();
                        LOGGER.info("   -------------> {}", creationTime);
                        return FileVisitResult.CONTINUE;
                    }
                });

                if (MCRMetadataManager.exists(derID)) {
                    MCRDerivateCommands.updateFromFile(f.toAbsolutePath().toString());
                } else {
                    MCRDerivateCommands.loadFromFile(f.toAbsolutePath().toString());
                }

                /*
                //set ACLs
                while (mcrDer.getService().getRulesSize() > 0) {
                    MCRMetaAccessRule rule = mcrDer.getService().getRule(0);
                    String permission = mcrDer.getService().getRulePermission(0);
                    MCRAccessManager.updateRule(derID, permission, rule.getCondition(), "");
                    mcrDer.getService().removeRule(0);
                }
                */
                if (mcrDer.getService().getRulesSize() > 0) {
                    LOGGER.warn("ACLS for {} ignored.", mcrDer::getId);
                }
            }
        }
    }

    /**
     * Updates the URN Store by parsing all Metadata Objects
     * 
     */

    @MCRCommand(syntax = "repair urn store",
        help = "The command parses through all metadata objects and updates the urns in the URN store if necessary")
    public static final void repairURNStore() throws MCRException {
        //TODO URN -> PI
        /*
        try {
            for (String mcrid : MCRXMLMetadataManager.instance().listIDs()) {
                // if object do'snt exist - no exception is catched!
                MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
                MCRMetaElement me = mcrObj.getMetadata().getMetadataElement("urns");
                if (me != null) {
                    MCRMetaLangText mltUrn = (MCRMetaLangText) me.getElement(0);
                    String urnNew = mltUrn.getText();
                    if (MCRURNManager.hasURNAssigned(mcrid)) {
                        if (!(MCRURNManager.getURNforDocument(mcrid).equals(urnNew))) {
                            MCRURNManager.removeURNByObjectID(mcrid);
                            MCRURNManager.assignURN(urnNew, mcrid);
                        }
                    } else {
                        MCRURNManager.assignURN(urnNew, mcrid);
                    }
        
                }
            }
        } catch (Exception e) {
            throw new MCRException("Error while repairing URN Store", e);
        }
        */
    }

    @MCRCommand(syntax = "create directory {0}",
        help = "The command creates a directory. If MyCoRe Properties are specified as part of the path, they will be replaced.")
    public static final void createDirectory(String dirname) {
        while (dirname.contains("${")) {
            int start = dirname.indexOf("${");
            int end = dirname.indexOf('}', start);

            if (end > start && end < dirname.length()) {
                String prop = dirname.substring(start + 2, end);
                if (prop.length() > 0) {
                    String value = MCRConfiguration2.getString(prop).orElse("");
                    dirname = dirname.substring(0, start) + value + dirname.substring(end + 1);
                }
            }
        }
        // check dirname
        Path dir = Paths.get(dirname);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * This command empties your MyCoRe Repository.
     * USE with CARE !!!!
     * 
     * all files in metadata and content are deleted
     * all tables in database will be emptied.
     * 
     * After calling this command, you need to intialize your repository again
     * with default users, permissions and classifications.
     * 
     */
    @MCRCommand(syntax = "drop mycore content",
        help = "The command deletes all data from mycore directories and database on a lower level.")
    public static final void dropMyCoReContent() {
        Map<String, String> ifsProperties = MCRConfiguration2.getSubPropertiesMap("MCR.IFS.ContentStore");
        for (String key : ifsProperties.keySet()) {
            if (key.endsWith(".BaseDir")) {
                Path dir = Paths.get(String.valueOf(ifsProperties.get(key)));
                if (Files.exists(dir)) {
                    try {
                        Files.walkFileTree(dir, new MCRRecursiveDeleter());
                    } catch (IOException e) {
                        LOGGER.error(e);
                    }
                }
            }
        }
        Path dir = Paths.get(MCRConfiguration2.getString("MCR.Metadata.Store.BaseDir").orElseThrow());
        if (Files.exists(dir)) {
            try {
                Files.walkFileTree(dir, new MCRRecursiveDeleter());
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        try (EntityManager em = MCREntityManagerProvider.getEntityManagerFactory().createEntityManager();) {
            int count = 0;
            EntityTransaction t = em.getTransaction();
            for (EntityType<?> et : em.getMetamodel().getEntities()) {
                String entityName = et.getName();
                try {
                    t.begin();
                    String selectQuery = "SELECT x FROM " + entityName + " x WHERE 1=1";
                    List<?> toRemove = em.createQuery(selectQuery).getResultList();
                    LOGGER.info("deleting {} objects of {}", toRemove::size, () -> entityName);
                    count += toRemove.size();
                    for (Object o : toRemove) {
                        em.remove(o);
                    }
                    // count += em.createQuery("DELETE FROM "+entityName+" WHERE 1=1").executeUpdate();
                    t.commit();
                } catch (Exception e) {
                    LOGGER.error(e);
                    t.rollback();
                }
            }
            LOGGER.info("Deleted {} objects.", count);
        }
    }

    /**
     * This command is needed for JSPDocportal Migration 2016 
     * It will look for any parent-child-relationships between MyCoRE objects 
     * and migrate them to mods:relatedItems.
     * 
     * At the end the parent entry is deleted.
     * 
     */
    @MCRCommand(syntax = "migrate mycore parent to relateditem",
        help = "The command creates related items for parents and removes parent child relationships.")
    public static final void migrateParent2RelatedItem() {
        LOGGER.info("Please be patient, while collecting required MyCoRe objects.");
        List<String> check = new ArrayList<>();
        check.addAll(MCRXMLMetadataManager.getInstance().listIDsOfType("bundle"));
        check.addAll(MCRXMLMetadataManager.getInstance().listIDsOfType("document"));

        XPathExpression<Element> xpathRecordIdentifier = XPathFactory.instance().compile(
            ".//mods:mods/mods:recordInfo/mods:recordIdentifier", Filters.element(), null,
            MCRConstants.MODS_NAMESPACE);
        XPathExpression<Element> xpathMainTitle = XPathFactory.instance().compile(
            ".//mods:mods/mods:titleInfo/mods:title", Filters.element(), null, MCRConstants.MODS_NAMESPACE);

        for (String mcrid : check) {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
            if (mcrObj.getParent() != null) {
                MCRMetaXML mcrMODS = (MCRMetaXML) mcrObj.getMetadata().findFirst("def.modsContainer").get();
                Element eMeta = (Element) mcrMODS.getContent().stream().filter(x -> x.getClass().equals(Element.class))
                    .findFirst().get();
                Element eRelatedItem = eMeta.getChild("relatedItem", MCRConstants.MODS_NAMESPACE);
                if (eRelatedItem != null) {
                    LOGGER.info("Creating mods:relatedItem for MCRObject {}", mcrid);
                    MCRObject mcrParentObj = MCRMetadataManager.retrieveMCRObject(mcrObj.getParent());

                    Element eParentMeta = mcrParentObj.getMetadata().createXML();
                    String recordIdentifier = xpathRecordIdentifier.evaluateFirst(eParentMeta).getTextNormalize();
                    String title = xpathMainTitle.evaluateFirst(eParentMeta).getTextNormalize();

                    eRelatedItem.removeChildren("titleInfo", MCRConstants.MODS_NAMESPACE);
                    eRelatedItem.addContent(new Element("titleInfo", MCRConstants.MODS_NAMESPACE)
                        .setAttribute("type", "simple", MCRConstants.XLINK_NAMESPACE)
                        .addContent(new Element("title", MCRConstants.MODS_NAMESPACE).setText(title)));
                    eRelatedItem.removeChildren("recordInfo", MCRConstants.MODS_NAMESPACE);
                    eRelatedItem.addContent(new Element("recordInfo", MCRConstants.MODS_NAMESPACE)
                        .addContent(new Element("recordIdentifier", MCRConstants.MODS_NAMESPACE)
                            .setAttribute("source", "DE-28").setText(recordIdentifier)));

                    int pos = 0;
                    for (int i = 0; i < mcrParentObj.getStructure().getChildren().size(); i++) {
                        if (mcrParentObj.getStructure().getChildren().get(i).getXLinkHrefID().equals(mcrObj.getId())) {
                            pos = i;
                            break;
                        }
                    }
                    DecimalFormat df = new DecimalFormat("0000", DecimalFormatSymbols.getInstance(Locale.GERMANY));
                    String sortString = "_" + df.format(pos) + "-" + mcrParentObj.getId().toString();
                    eRelatedItem.getChild("part", MCRConstants.MODS_NAMESPACE).removeChildren("text",
                        MCRConstants.MODS_NAMESPACE);
                    eRelatedItem.getChild("part", MCRConstants.MODS_NAMESPACE)
                        .addContent(new Element("text", MCRConstants.MODS_NAMESPACE)
                            .setAttribute("type", "sortstring").setText(sortString));

                    try {
                        MCRMetadataManager.update(mcrObj);
                    } catch (MCRPersistenceException | MCRAccessException e) {
                        LOGGER.error(e);
                    }
                } else {
                    LOGGER.error("{}: Parent exists and mods related item not found !!!", mcrid);
                }
            }
        }

        //delete parent nodes
        for (String mcrid : check) {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
            if (mcrObj.getStructure().getParent() != null) {
                mcrObj.getStructure().setParent((MCRMetaLinkID) null);
                try {
                    MCRMetadataManager.update(mcrObj);
                } catch (MCRAccessException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    @MCRCommand(syntax = "send mail to {0} with subject {1} with body {2}", help = "Send an email via MCRMailer")
    public static final void sendMail(String to, String subject, String body) {
        String from = MCRConfiguration2.getString("MCR.Mail.Address").orElse("");
        MCRMailer.send("MyCoRe Mailer Test<" + from + ">", to, subject, body);
    }
}
