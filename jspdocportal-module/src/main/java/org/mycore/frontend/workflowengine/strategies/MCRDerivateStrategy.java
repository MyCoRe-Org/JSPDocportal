package org.mycore.frontend.workflowengine.strategies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.mycore.common.JSPUtils;
import org.mycore.common.MCRDerivateFileFilter;
import org.mycore.common.MCRException;
import org.mycore.common.MCRUtils;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRFileContent;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaIFS;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowUtils;
import org.xml.sax.SAXParseException;

public abstract class MCRDerivateStrategy {
	private static Logger logger = Logger.getLogger(MCRDefaultDerivateStrategy.class.getName());
	private static String XLINK_URL = "http://www.w3.org/1999/xlink";
	
	protected static String SEPARATOR = "/";
	
	// pattern for the stringpart after the last [/\]
	protected static Pattern filenamePattern = Pattern.compile("([^\\\\/]+)\\z");
	//	 pattern for the file extension
	protected static Pattern fileextensionPattern = Pattern.compile(".([^\\\\/.]+)\\z");	

	/**
	 * deletes a derivate from the workflow 
	 * @param documentType
	 * 			String like "disshab" or "document"
	 * @param metadataObjectId
	 * 			String of the objID the derivate belongs to
	 * @param derivateObjectId
	 * 			String of the derID, that will bedeleted
	 * @return
	 */
	public abstract boolean deleteDerivateObject(ContextInstance ctxI, String derivateDirectory, String backupDirectory, String metadataObjectId, String derivateObjectId, boolean mustWorkflowVarBeUpdated);
	
	
	/**
	 * deletes a file from a derivate from the workflow 
	 * @param documentType
	 * 			String like "disshab" or "document"
	 * @param metadataObjectId
	 * 			String of the objID the derivate belongs to
	 * @param derivateObjectId
	 * 			String of the derID, that will bedeleted
	 * @param filename
	 * 			String the filename with its derivatepath
	 * @return
	 */
	public abstract boolean deleteDerivateFile(ContextInstance ctxI, String derivateDirectory, String backupDirectory, String metadataObjectId, String derivateObjectId, boolean mustWorkflowVarBeUpdated, String filename);
	
	/**
	 * adds a new derivate to an workflow object
	 * @param metadataObjectId
	 * @param derivateDirectory
	 * @return
	 * TODO check, why is here a userid required???
	 */
	public String addNewDerivateToWorkflowObject(String derivateDirectory, String metadataObjectId){
		File derivateParentDir = new File(derivateDirectory);
	    MCRObjectID IDMax = setNextFreeDerivateID(derivateParentDir);
		
		logger.debug("New derivate ID " + IDMax.toString());

		// create a new directory
		File dir = new File(derivateParentDir, IDMax.toString());
		dir.mkdir();
		logger.debug("Directory " + dir.getAbsolutePath() + " created.");

		// build the derivate XML file
		MCRDerivate der = new MCRDerivate();
		der.setId(IDMax);
		der.setLabel(MCRConfiguration.instance().getString("MCR.Derivates.Labels.default", "dataobject_from_" + IDMax.toString()));
		der.setSchema("datamodel-derivate.xsd");
		MCRMetaLinkID link = new MCRMetaLinkID("linkmeta", 0);
		link.setReference(metadataObjectId, null, null);
		der.getDerivate().setLinkMeta(link);
		MCRMetaIFS internal = new MCRMetaIFS("internal", dir.getAbsolutePath());
		internal.setMainDoc("#####");
		der.getDerivate().setInternals(internal);
		
		JSPUtils.saveDirect( der.createXML(), dir.getAbsolutePath() + ".xml");
		logger.info("Derivate " + IDMax.toString() + " stored under " + dir.getAbsolutePath() + ".xml");
		return IDMax.toString();
	}

    final public static synchronized MCRObjectID setNextFreeDerivateID(File parentDir) {
        int maxwf = 0;
        String base = MCRConfiguration.instance().getString("MCR.SWF.Project.ID", "DocPortal") + "_derivate";
        List<String> allDerivateFileNames = new ArrayList<String>();
        HashMap directoryMap = MCRWorkflowDirectoryManager.getEditWorkflowDirectories();
        for (Iterator it = directoryMap.keySet().iterator(); it.hasNext();) {
            File workDir = new File((String) directoryMap.get(it.next()));
            if (workDir.isDirectory()) {
                Iterator<File> it2 = Arrays.asList(workDir.listFiles(new MCRDerivateFileFilter())).iterator();
                while (it2.hasNext()) {
                    File ff = (File) it2.next();
                    if ((ff.isFile())) {
                        allDerivateFileNames.add(ff.getName());
                    }
                }
            }
        }

        if (allDerivateFileNames.size() == 0) {
            maxwf = 0;
        } else {
            Collections.sort(allDerivateFileNames, Collections.reverseOrder());
            String maxFilename = (String) allDerivateFileNames.get(0);
            MCRObjectID IDinWF = MCRObjectID.getInstance(maxFilename.substring(0, maxFilename.length() - 4));
            maxwf = IDinWF.getNumberAsInteger();
        }

        MCRObjectID retID = MCRObjectID.getNextFreeId(base, maxwf);
        //create dummy in workflow
        File f = new File(parentDir, retID.toString() + ".xml");
        try {
            f.createNewFile();
        } catch (Exception e) {
            logger.error("could not create new file", e);
        }

        return retID;
    }
	
	/**
	 * saves a list of files in a workflow directory, 
	 * 		when the requirements of the specific workflow-type 
	 * 		cannot be fulfilled, an exception is thrown
	 * @param files
	 * @param dirname
	 * @param wfp
	 * @throws MCRException
	 * TODO a better javadoc
	 */	
	public abstract void saveFiles(List files, String dirname, ContextInstance ctxI, String newLabel, String newTitle) throws MCRException ;
	
	public Element getDerivateData(String derivateDirectory, String docID, String derivateID) {
		String fileName = new StringBuffer(derivateDirectory)
			.append(SEPARATOR).append(derivateID)
			.append(".xml").toString();
		Element derivate = getDerivateMetaData(fileName);
		if ( docID.equalsIgnoreCase(derivate.getAttributeValue("href"))) {
			// this is our convention
			String derivatePath = derivate.getAttributeValue("ID");
			File dir = new File(derivateDirectory, derivatePath);
			logger.debug("Derivate under " + dir.getName());
			if (dir.isDirectory()) {
				ArrayList dirlist = MCRUtils.getAllFileNames(dir);
				for (int k = 0; k < dirlist.size(); k++) {
					org.jdom2.Element file = new org.jdom2.Element("file");
					file.setText(derivatePath + "/" + (String) dirlist.get(k));
					File thisfile = new File(dir, (String) dirlist.get(k));
					file.setAttribute("size", String.valueOf(thisfile.length()));
					file.setAttribute("main", "false");
					if (derivate.getAttributeValue("maindoc").equals((String) dirlist.get(k))) {
						file.setAttribute("main", "true");
					}
					derivate.addContent(file);
				}
			}
		}
		return derivate;
	}

		/**
		 * returns relevant information of certain derivate for a certain document as jdom Element
		 * @param docID
		 * @param derivateID
		 * @return
		 * 	the derivate as JDOM-Element
		 * <br>
		 * output format<br>
		 * &lt;derivate id="derivateID" label="Label of Derivate" &rt;
		 *    &lt;file type="maindoc" name="filename" path="fullpath without /filename" /&gt;
		 *    &lt;file type="standard" name="filename" path="fullpath without /filename" /&gt;
		 *    &lt;file type="standard" name="filename" path="fullpath without /filename" /&gt;
		 * &lt;/derivate&rt;
		 */
		protected final Element getDerivateMetaData( String filename){
			Element derivateData = new Element("derivate");
			try {
				Document derDoc = new MCRFileContent(new File(filename)).asXML(); 
				Element derivate = derDoc.getRootElement();				
				derivateData.setAttribute("label", derivate.getAttributeValue("label") );
				derivateData.setAttribute("ID", derivate.getAttributeValue("ID") );
				
				Iterator it = derivate.getDescendants(new ElementFilter("linkmeta"));
				if ( it.hasNext() ) {
			      Element el = (Element) it.next();
			      String href = el.getAttributeValue("href",org.jdom2.Namespace.getNamespace("xlink",XLINK_URL));
			      if ( href==null)  	href = "";      
		          derivateData.setAttribute("href", href);
			    } 
				
				derivateData.setAttribute("title", "");
				it = derivate.getDescendants(new ElementFilter("servflag"));
				while( it.hasNext() ) {
			      Element el = (Element) it.next();
			      if("title".equals(el.getAttributeValue("type"))){
			    	  derivateData.setAttribute("title", el.getText());
			      }		          
			    } 
				
				it = derivate.getDescendants(new ElementFilter("internal"));		
			    if ( it.hasNext() )	    {
			      Element el = (Element) it.next();
			      String maindoc = el.getAttributeValue("maindoc");
			      if ( maindoc==null)  	maindoc = "####";
			      derivateData.setAttribute("maindoc", maindoc );          
			    }
			} catch ( Exception ignore) {	;	}
			
		    return derivateData;		
		}

		/**
		 * is removing all derivates of a special workflow process
		 * @param wfp
		 * @param directory
		 */
		public abstract boolean removeDerivates(ContextInstance ctxI, String saveDirectory, String backupDirectory);
		
		
		public abstract boolean moveDerivateObject(ContextInstance ctxI, String derivateObjectID, int direction);
		
		/**
		 * is publishing a derivate to the database
		 * @param derivateid
		 * @param directory
		 * @return
		 */		
		public boolean commitDerivateObject(String derivateid, String directory) {
			String filename = directory + SEPARATOR + derivateid + ".xml";
			return loadDerivate(derivateid, filename);
		}
		
		/**
		 * is publishing the deleting in the workflowprozess - makes the delete of the derivate in the database
		 * @param derivateid
		 * @return
		 */
		public boolean deleteDeletedDerivates(String derivateid) {	
			if(MCRMetadataManager.exists(MCRObjectID.getInstance(derivateid))){
				MCRMetadataManager.deleteMCRDerivate(MCRObjectID.getInstance(derivateid));
			}
			return true;
		}

		/**
		 * is publishing the deleting in the workflowprozess - makes the delete of single files of a derivate in the database
		 * @param filename
		 * @return
		 */
		public boolean deleteDeletedDerivateFile(String fileName) {			
			logger.debug("Delete File from Derivate: "+fileName);
			int split = fileName.indexOf(SEPARATOR);
			String derID = fileName.substring(0, split);
			MCRPath path = MCRPath.getPath(derID, fileName.substring(split));
			try{
				Files.delete(path);
			}
			catch(IOException e){
				logger.error(e);
			}
			return true;
		}
		
		
		
		protected boolean backupDerivateObject(String saveDirectory, String backupDir,
				String metadataObjectID, String derivateObjectID, long pid) {
			
			logger.debug("backup is uncommented");
			return true;
			/***
			try{
				String derivateDirectory = saveDirectory + SEPARATOR + derivateObjectID;
				String derivateFileName = derivateDirectory + ".xml" ;
				
				File inputDir = new File(derivateDirectory);
				File inputFile = new File(derivateFileName);
				
				if ( inputFile.exist() ){
					SimpleDateFormat fmt = new SimpleDateFormat();
				    fmt.applyPattern( "yyyyMMddhhmmss" );
				    GregorianCalendar cal = new GregorianCalendar();
				    File curBackupDir = null;
				    boolean dirCreated = false;
				    while(!dirCreated) {
				    	curBackupDir = new File(backupDir + "/" + "deleted_at_" + fmt.format(cal.getTime()));
				    	if(curBackupDir.mkdir()) dirCreated = true;
				    }
				    File outputDir = new File(curBackupDir.getAbsolutePath() + SEPARATOR + inputDir.getName());
					JSPUtils.recursiveCopy(inputDir, outputDir);
					FileInputStream fin = new FileInputStream(inputFile);
					FileOutputStream fout = new FileOutputStream(new File(curBackupDir.getAbsolutePath() + SEPARATOR + inputFile.getName()));
					MCRUtils.copyStream(fin, fout);
					
					fin.close();
					fout.flush();
					fout.close();
				}

			}catch(Exception ex){
				logger.error("problems in copying", ex);
				return false;
			}
			return true;
			**/		
		}		

		private boolean loadDerivate(String derivateid, String filename) {
	        Map ruleMap = null;
	        boolean result = false;
	        try{
			if (MCRMetadataManager.exists(MCRObjectID.getInstance(derivateid))) {
		        ruleMap = MCRWorkflowUtils.getAccessRulesMap(derivateid);
				result = MCRDerivateCommands.updateFromFile(filename, false);
			} else {
				result = MCRDerivateCommands.loadFromFile(filename, false);
			}
	        }
	        catch(SAXParseException e){
	        	logger.error(e);
	        } catch (IOException e) {
				e.printStackTrace();
			}
			
			if ( ruleMap != null ) 
				MCRWorkflowUtils.setAccessRulesMap(derivateid, ruleMap);
		
			logger.debug("Commit the derivate " + filename);

			return result;
		}	
		
		/**
		 * returns a list of all derivates of given directory
		 * @param directory
		 * @return
		 */		
		public List getAllDerivateDataFromWorkflow(String directory) {
			List<Element> workfiles = new ArrayList<Element>();
			if (!directory.equals(".")) {
				File dir = new File(directory);
				String[] dirl = null;
				if (dir.isDirectory()) {
					dirl = dir.list();
				}
				if (dirl != null) {
					for (int i = 0; i < dirl.length; i++) {
						if ((dirl[i].indexOf("_derivate_") != -1) && (dirl[i].endsWith(".xml"))) {
							Element derivateData = getDerivateMetaData(directory + SEPARATOR + dirl[i]);
							workfiles.add(derivateData);						
						}
					}
				}
			}
			return workfiles;
    }

}