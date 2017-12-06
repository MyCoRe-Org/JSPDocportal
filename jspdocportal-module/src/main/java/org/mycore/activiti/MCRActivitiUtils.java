package org.mycore.activiti;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.utils.MCRRecursiveDeleter;
import org.xml.sax.SAXParseException;

/**
 * provides some static utility methods
 * 
 * @author Robert Stephan
 * 
 */
public class MCRActivitiUtils {
	private static Logger LOGGER = LogManager.getLogger(MCRActivitiUtils.class);
	/**
	 * saves a given MCR object into the workflow directory
	 * @param MCRObject
	 */
	public static void saveMCRObjectToWorkflowDirectory(MCRObject mcrObj) {
		Path wfObjFile = getWorkflowObjectFile(mcrObj.getId());
		try (BufferedWriter bw = Files.newBufferedWriter(wfObjFile)) {
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(mcrObj.createXML(), bw);
		} catch (Exception ex) {
			throw new MCRException("Cant save MCR Object "
					+ mcrObj.getId().toString() + " as file "
					+ wfObjFile.toString());
		}
	}
	
	public static MCRObject loadMCRObjectFromWorkflowDirectory(MCRObjectID mcrObjID){
		MCRObject mcrObj= null;
		try{
			mcrObj = MCRActivitiUtils.getWorkflowObject(mcrObjID);
		}
		catch(Exception e){
			LOGGER.error(e);
		}
		return mcrObj;
	}
	
	/**
	 * saves a given MCR object into the workflow directory
	 * @param MCRObject
	 */
	public static void saveMCRDerivateToWorkflowDirectory(MCRDerivate mcrDer) {
		try (BufferedWriter bw = Files.newBufferedWriter(getWorkflowDerivateFile(mcrDer.getOwnerID(), mcrDer.getId()), StandardOpenOption.CREATE)) {
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(mcrDer.createXML(), bw);
		} catch (Exception ex) {
			throw new MCRException("Could not save MCR Derivate "
					+ mcrDer.getId().toString() + " into workfow directory.", ex);
		}
	}
	
	public static MCRDerivate loadMCRDerivateFromWorkflowDirectory(MCRObjectID owner, MCRObjectID mcrderid){
		try{
			Path wfFile = getWorkflowDerivateFile(owner, mcrderid);
			if(Files.exists(wfFile)){
				return new MCRDerivate(wfFile.toUri());
			}
		}
		catch(Exception e){
			LOGGER.error(e);
		}
		return null;
	}
	
	private static Path getWorkflowDirectory(MCRObjectID mcrObjID) {
		String s = MCRConfiguration.instance().getString("MCR.Workflow.WorkflowDirectory");
		Path p = Paths.get(s).resolve(mcrObjID.getTypeId());
		if (!Files.exists(p)) {
			try {
				Files.createDirectories(p);
			}
			catch(IOException e) {
				LOGGER.error(e);
			}
		}
		return p;
	}
		
	public static MCRObject getWorkflowObject(MCRObjectID mcrObjID){
		MCRObject o = null;
		try {
			o = new MCRObject(getWorkflowObjectFile(mcrObjID).toUri());
		} catch (SAXParseException | IOException e) {
			LOGGER.error(e);
		}
		return o;
	}
	
	public static Path getWorkflowObjectFile(MCRObjectID mcrObjID){
		Path p = getWorkflowDirectory(mcrObjID).resolve(mcrObjID.toString() +".xml");
		return p;
	}
	
	public static Document getWorkflowObjectXML(MCRObjectID mcrObjID){
		Document doc = null;
		Path wfFile = MCRActivitiUtils.getWorkflowObjectFile(mcrObjID);
		MCRPathContent mpc = new MCRPathContent(wfFile);
     	try {
			doc = mpc.asXML();
		} catch (Exception e) {
			LOGGER.error(e);
		}     	
     	return doc;
	}
	
	public static Path getWorkflowObjectDir(MCRObjectID mcrObjID){
		Path p = getWorkflowDirectory(mcrObjID).resolve(mcrObjID.toString());
		if (!Files.exists(p)) {
			try {
				Files.createDirectories(p);
			}
			catch(IOException e) {
				LOGGER.error(e);
			}
		}
		return p;
	}
		
	public static Path getWorkflowDerivateFile(MCRObjectID mcrObjID, MCRObjectID mcrDerID){
		Path wfFile = getWorkflowObjectDir(mcrObjID).resolve(mcrDerID.toString() +".xml");
		return wfFile;
	}
	
	public static Document getWorkflowDerivateXML(MCRObjectID mcrObjID, MCRObjectID mcrDerID){
		Document doc = null;
		Path wfFile = MCRActivitiUtils.getWorkflowDerivateFile(mcrObjID, mcrDerID);
		MCRPathContent mpc = new MCRPathContent(wfFile);
     	try {
			doc = mpc.asXML();
		} catch (Exception e) {
			LOGGER.error(e);
		}     	
     	return doc;
	}
	
	public static Path getWorkflowDerivateDir(MCRObjectID mcrObjID, MCRObjectID mcrDerID){
		Path p = getWorkflowObjectDir(mcrObjID).resolve(mcrDerID.toString());
		if (!Files.exists(p)) {
			try {
				Files.createDirectories(p);
			}
			catch(IOException e) {
				LOGGER.error(e);
			}
		}
		return p;
	}
	
	   public static void cleanUpWorkflowDirForObject(MCRObjectID mcrObjID) {
		   Path wfObjDir = getWorkflowObjectDir(mcrObjID);
		   if(Files.exists(wfObjDir)) {
				try {
					Files.walkFileTree(wfObjDir, MCRRecursiveDeleter.instance());
				}
				catch(IOException e) {
					LOGGER.error(e);
				}
			}
		   
		   Path wfObjFile = getWorkflowObjectFile(mcrObjID);
		   
		   if(Files.exists(wfObjFile)) {
			try {
				Files.delete(wfObjFile);
			} catch (IOException e) {
				LOGGER.error(e);
			}
		   }
	   }
	
	public static void cleanupWorkflowDirForDerivate(MCRObjectID mcrObjID, MCRObjectID mcrDerID){
		try{
			Path wfDerDir = getWorkflowDerivateDir(mcrObjID, mcrDerID);
			if(Files.exists(wfDerDir)) {
				Files.walkFileTree(wfDerDir, MCRRecursiveDeleter.instance());
			}
			Path wfDerFile = getWorkflowDerivateFile(mcrObjID, mcrDerID);
			if(Files.exists(wfDerFile)) {
				try {
					Files.delete(wfDerFile);
				} catch (IOException e) {
					LOGGER.error(e);
				}
			   }
		}
		catch(Exception e){
			LOGGER.error(e);
		}
	}

	public static Map<String, List<String>> getDerivateFiles(MCRObjectID mcrObjID){
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		Path baseDir = getWorkflowObjectDir(mcrObjID);
		MCRObject obj = MCRActivitiUtils.loadMCRObjectFromWorkflowDirectory(mcrObjID);
		try{
			for(MCRMetaLinkID derID: obj.getStructure().getDerivates()){
				String id = derID.getXLinkHref();
				List<String> fileNames = new ArrayList<String>();
				try{
					Path derDir = baseDir.resolve(id);
					
					try (DirectoryStream<Path> stream = Files.newDirectoryStream(derDir)) {
					    for (Path file: stream) {
					        fileNames.add(file.getFileName().toString());
					    }
					} catch (IOException | DirectoryIteratorException e) {
					   LOGGER.error(e);
					}
				}
				catch(Exception e){
					LOGGER.error(e);
				}
				result.put(id, fileNames);
			}
		} catch(Exception e){
			LOGGER.error(e);
		}
		return result;
	}
	
   public static void deleteDirectoryContent(Path path) {
		try {
			final Path rootPath = path;
				Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null){
						throw exc;
					}
					if(!rootPath.equals(dir)) {
						Files.delete(dir);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Element> getAccessRulesMap(String objid) {
		Iterator<String> it = MCRAccessManager.getPermissionsForID(objid).iterator();        
        Map<String, Element> htRules = new Hashtable<String, Element>();
        while(it.hasNext()){
        	String s = it.next();
           	Element eRule = MCRAccessManager.getAccessImpl().getRule( objid, s);
           	htRules.put(s, eRule);
        }
        return htRules;
	}	

	public static void setAccessRulesMap(String objid, Map<String, Element> htRules ) {
		if ( htRules != null) {
			MCRAccessManager.getAccessImpl().removeAllRules(objid);
			for (String perm: htRules.keySet()) {
				MCRAccessManager.addRule(objid,perm,htRules.get(perm),"");
			}
		}
	}
}
