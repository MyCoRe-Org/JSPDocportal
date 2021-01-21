package org.mycore.frontend.jsp.query;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;

public class MCRDerivateComparator implements Comparator<Element> {
    public int compare(Element child1, Element child2) {
        try {
            int firstOrder = child1.getAttributeValue("type").compareTo(child2.getAttributeValue("type"));
            if (firstOrder != 0)
                return firstOrder;

            if (child1.getChildText("contentType") != null && child2.getChildText("contentType") != null) {
                int secondOrder = child1.getChildText("contentType").compareTo(child2.getChildText("contentType"));
                if (secondOrder != 0)
                    return secondOrder;
            }

            int thirdOrder = child1.getChildText("name").compareTo(child2.getChildText("name"));
            return thirdOrder;
        } catch (Exception e) {
            LogManager.getLogger(MCRDerivateComparator.class)
                    .debug("no sorting possible, error is catched, but must be checked", e);
            return 0;
        }
    }
}
