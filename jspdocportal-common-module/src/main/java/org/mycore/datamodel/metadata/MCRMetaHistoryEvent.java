package org.mycore.datamodel.metadata;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRException;

public class MCRMetaHistoryEvent extends MCRMetaHistoryDate {
    private static final Logger LOGGER = LogManager.getLogger(MCRMetaHistoryEvent.class);
    private static final String SUBTAG_CLASSIFICATION = "classification";
    public static final int MCRHISTORYEVENT_MAX_EVENT = 1024;
    private String event;
    private MCRMetaClassification classification;

    public MCRMetaHistoryEvent() {
        // TODO Auto-generated constructor stub
        super();
        event = "";
        classification = new MCRMetaClassification();
        classification.setSubTag(SUBTAG_CLASSIFICATION);

    }

    public MCRMetaHistoryEvent(String setSubtag, String setType, int setInherted) throws MCRException {
        super(setSubtag, setType, setInherted);
        event = "";
        classification = new MCRMetaClassification();
        classification.setSubTag(SUBTAG_CLASSIFICATION);

    }

    /**
    * The method set the text value.
    */
    public final void setEvent(String set) {
        if (set == null) {
            event = "";
            return;
        }
        if (set.length() <= MCRHISTORYEVENT_MAX_EVENT) {
            event = set.trim();
        } else {
            event = set.substring(0, MCRHISTORYEVENT_MAX_EVENT);
        }
    }

    public final void setClassification(MCRMetaClassification set) {
        classification = set;
    }

    public final String getEvent() {
        return event;
    }

    public final MCRMetaClassification getClassification() {
        return classification;
    }

    /**
     * This method reads the XML input stream part from a DOM part for the
     * metadata of the document.
     * 
     * @param element
     *            a relevant JDOM element for the metadata
     */
    @Override
    public void setFromDOM(org.jdom2.Element element) {
        if (element.getChild("von") == null) {
            element.addContent(new Element("von"));
        }
        if (element.getChild("bis") == null) {
            element.addContent(new Element("bis"));
        }
        Iterator<org.jdom2.Element> textchild = element.getChildren("text").iterator();
        while (textchild.hasNext()) {
            Element elmt = textchild.next();
            Attribute attr = elmt.getAttribute("lang");
            if (attr != null) {
                attr.setNamespace(Namespace.XML_NAMESPACE);
            }
        }

        super.setFromDOM(element);
        setEvent(element.getChildTextTrim("event"));
        setCalendar(element.getChildTextTrim("calendar"));
        Element eClassi = element.getChild(SUBTAG_CLASSIFICATION);
        if (eClassi != null) {
            if (classification == null) {
                classification = new MCRMetaClassification();
                classification.setSubTag(SUBTAG_CLASSIFICATION);
            }
            classification.setFromDOM(eClassi);
        } else {
            classification = null;
        }

    }

    /**
     * This method creates a XML stream for all data in this class, defined by
     * the MyCoRe XML MCRMetaHistoryDate definition for the given subtag.
     * 
     * @exception MCRException
     *                if the content of this class is not valid
     * @return a JDOM Element with the XML MCRMetaHistoryDate part
     */
    @Override
    public org.jdom2.Element createXML() throws MCRException {
        if (!isValid()) {
            debug();
            throw new MCRException("The content of MCRMetaHistoryEvent '" + getSubTag() + "' is not valid.");
        }

        org.jdom2.Element elm = super.createXML();
        if (!event.isBlank()) {
            elm.addContent(new org.jdom2.Element("event").addContent(event));
        }

        //        if(classification!=null && !classification.isValid()){
        //        	debug();
        //        	throw new MCRException("The content of MCRMetaHistoryEvent's classification is not valid.");
        //        
        //        }
        if (classification != null && classification.isValid()) {
            elm.addContent(classification.createXML());
        }

        return elm;
    }

    /**
     * This method checks the validation of the content of this class. The
     * method returns <em>false</em> if
     * <ul>
     * <li>the text is null or</li>
     * <li>the von is null or</li>
     * <li>the bis is null</li>
     * <li>the event is null</li>
     * <li>the classification is null</li>
     * </ul>
     * otherwise the method returns <em>true</em>.
     * 
     * @return a boolean value
     */
    @Override
    public boolean isValid() {
        boolean b = super.isValid() && event != null;
        if (classification != null) {
            b &= classification.isValid();
        }
        return b;
    }

    /**
     * This method make a clone of this class.
     */
    @Override
    public MCRMetaHistoryEvent clone() {
        MCRMetaHistoryEvent out = new MCRMetaHistoryEvent(subtag, type, inherited);
        out.setText(getText("de").getText(), "de");
        out.setVonDate(getVon());
        out.setBisDate(getBis());
        out.setCalendar(getCalendar());
        if (event != null) {
            out.setEvent(event);
        }
        if (classification != null) {
            out.setClassification(classification.clone());
        } else {
            out.setClassification(null);
        }
        return out;
    }

    /**
     * This method put debug data to the logger (for the debug mode).
     */
    @Override
    public void debug() {
        LOGGER.debug("Start Class : MCRMetaHistoryEvent");
        super.debugDefault();
        LOGGER.debug("Text               = {}", () -> getText("de"));
        LOGGER.debug("Calendar           = {}", this::getCalendar);
        LOGGER.debug("Von (String)       = {}", this::getVonToString);
        LOGGER.debug("Von (JulianDay)    = {}", this::getIvon);
        LOGGER.debug("Bis (String)       = {}", this::getBisToString);
        LOGGER.debug("Bis (JulianDay)    = {}", this::getIbis);
        LOGGER.debug("Event              = {}", event);
        if (classification != null) {
            classification.debug();
        }
        LOGGER.debug("Stop");
        LOGGER.debug("");
    }
}
