package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * checks whether a query-result was sorted by a given field 
 * or a given order. an example is given below. (needed for sort form)
 *
 */
public class MCRIfSortedTag extends SimpleTagSupport {
    private Document query;
    private int sortorder;
    private String attributeName;
    private String attributeValue;

    public void setQuery(Document inputQuery) {
        query = inputQuery;
        return;
    }

    public void setSortorder(int inputSortorder) {
        sortorder = inputSortorder;
        return;
    }

    public void setAttributeName(String inputAttributeName) {
        attributeName = inputAttributeName;
    }

    public void setAttributeValue(String inputAttributeValue) {
        attributeValue = inputAttributeValue;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();
        Element sortField = null;

        int sortprio = (sortorder == 0) ? 1 : sortorder;
        try {
            sortField = (Element) query.getRootElement().getChild("sortBy").getChildren("field").get(sortprio - 1);
            if (sortField != null) {
                if (sortField.getAttributeValue(attributeName) != null
                        && sortField.getAttributeValue(attributeName).equals(attributeValue)) {
                    JspFragment body = getJspBody();
                    StringWriter stringWriter = new StringWriter();
                    body.invoke(stringWriter);
                    out.println(stringWriter);
                }
            }
        } catch (Exception allE) {
            //No sortField in query -
            ;
        }
        return;
    }

}
