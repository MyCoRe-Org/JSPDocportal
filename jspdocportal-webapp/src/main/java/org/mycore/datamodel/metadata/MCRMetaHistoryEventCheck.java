package org.mycore.datamodel.metadata;

import static org.jdom.Namespace.XML_NAMESPACE;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.datamodel.metadata.validator.MCREditorMetadataValidator;
import org.mycore.datamodel.metadata.validator.MCREditorOutValidator;

public class MCRMetaHistoryEventCheck implements MCREditorMetadataValidator {
	  private static Logger LOGGER = Logger.getLogger(MCRMetaHistoryEventCheck.class);
      public String checkDataSubTag(Element datasubtag) {
            @SuppressWarnings("unchecked")
            List<Element> children = datasubtag.getChildren("text");
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                String text = child.getTextTrim();
                if (text == null || text.length() == 0) {
                    datasubtag.removeContent(child);
                    i--;
                    continue;
                }
                if (child.getAttribute("lang") != null) {
                    child.getAttribute("lang").setNamespace(XML_NAMESPACE);
                    LOGGER.warn("namespace add for xml:lang attribute in " + datasubtag.getName());
                }
            }
            if (children.size() == 0) {
                return "history date is empty";
            }
            Element classification = datasubtag.getChild("classification"); 
             if(classification!=null){
            	  String categid = classification.getAttributeValue("categid");
                  if (categid == null) {
                      return "Attribute categid is empty";
                  }
                  String checkResult = MCREditorOutValidator.checkMetaObject(classification, MCRMetaClassification.class, false);
                  if(checkResult!=null){
                	  return checkResult;
                  }
            }
            return MCREditorOutValidator.checkMetaObject(datasubtag, MCRMetaHistoryEvent.class, false);
        }
    
}
