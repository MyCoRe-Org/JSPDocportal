package org.mycore.datamodel.metadata;

import static org.jdom2.Namespace.XML_NAMESPACE;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.datamodel.metadata.validator.MCREditorMetadataValidator;
import org.mycore.datamodel.metadata.validator.MCREditorOutValidator;

public class MCRMetaHistoryEventCheck implements MCREditorMetadataValidator {
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    @Override
    public String checkDataSubTag(Element datasubtag) {
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
                LOGGER.warn("namespace add for xml:lang attribute in {}", datasubtag::getName);
            }
        }
        if (children.isEmpty()) {
            return "history date is empty";
        }
        Element classification = datasubtag.getChild("classification");
        if (classification != null) {
            String categid = classification.getAttributeValue("categid");
            if (categid == null) {
                return "Attribute categid is empty";
            }
            String checkResult = MCREditorOutValidator.checkMetaObject(classification, MCRMetaClassification.class,
                    false);
            if (checkResult != null) {
                return checkResult;
            }
        }
        return MCREditorOutValidator.checkMetaObject(datasubtag, MCRMetaHistoryEvent.class, false);
    }

}
