package org.mycore.jspdocportal.common.controller.login;

/**
* This Java bean class represents a link object.
* It contains the attributes url and label.
* 
* @author Robert Stephan
*
*/
public class MCRLoginNextStep {
    private String url;
    private String label;

    public MCRLoginNextStep(String url, String label) {
        super();
        this.url = url;
        this.label = label;
    }

    /**
    * 
    * @return the url or empty string
    */
    public String getUrl() {
        return url;
    }

    /**
    * 
    * @return the label
    */
    public String getLabel() {
        return label;
    }
}
