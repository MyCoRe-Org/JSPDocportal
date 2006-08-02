package org.mycore.frontend.workflowengine.strategies;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRDefaults;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRDefaultInstitutionStrategy implements MCRInstitutionStrategy {
	private static Logger logger = Logger.getLogger(MCRDefaultInstitutionStrategy.class.getName());


	public MCRObjectID createInstitution(MCRObjectID nextFreeInstitutionId, boolean inDatabase){
		MCRObject institution = null;
		institution = createInstitutionObject(nextFreeInstitutionId);
		try {
			if ( inDatabase) {
				institution.createInDatastore();
			} else {
				FileOutputStream fos = new FileOutputStream(
						MCRWorkflowDirectoryManager.getWorkflowDirectory("institution")	+ "/" + institution.getId().getId() + ".xml");
						(new XMLOutputter(Format.getPrettyFormat())).output(institution.createXML(),fos);
				fos.close();
			}
		} catch ( Exception ex){
			//TODO Fehlermeldung
			logger.warn("Could not Create institution object:  " + nextFreeInstitutionId.getId(), ex);
			return null;
		}
   	    return institution.getId();		
	}
	
	private MCRObject createInstitutionObject(MCRObjectID id) {
		MCRObject institution = new MCRObject();
		Element xmlElemInstitution = new Element("mycoreobject");
		xmlElemInstitution.addNamespaceDeclaration(org.jdom.Namespace.getNamespace(
				"xsi", MCRDefaults.XSI_URL));
		xmlElemInstitution.setAttribute("noNamespaceSchemaLocation",
				"datamodel-author.xsd", org.jdom.Namespace.getNamespace("xsi",
						MCRDefaults.XSI_URL));
		xmlElemInstitution.setAttribute("ID", id.toString());
		xmlElemInstitution.setAttribute("label", id.toString());

		Element structure = new Element("structure");
		Element metadata = new Element("metadata");
		Element service = new Element("service");

		//metadata needs dummy data otherwise  author.createXML() failes
		
		Element eInames = new Element("names");
		eInames.setAttribute("class", "MCRMetaInstitutionName");
		eInames.setAttribute("textsearch", "true");
		MCRMetaInstitutionName iname = new MCRMetaInstitutionName();
		iname.setSubTag("name");
		iname.setLang("de");
		iname.set("Neue Institution", "", "");
		eInames.addContent(iname.createXML());
						
		metadata.addContent(eInames);
		xmlElemInstitution.addContent(structure);
		xmlElemInstitution.addContent(metadata);
		xmlElemInstitution.addContent(service);

		Document institutiondoc = new Document(xmlElemInstitution);
		institution.setFromJDOM(institutiondoc);
		institution.setId(id);
			
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		Logger.getLogger(this.getClass()).info(xout.outputString(institution.createXML()));
		return institution;
	}		
}
