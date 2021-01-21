package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.user2.MCRUserManager;

public class MCRGetFreeUserIDs extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRGetFreeUserIDs.class);

    private String var;
    private String userid;
    private int count = 0;

    private static String randomChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    public void setVar(String inputVar) {
        var = inputVar;
        return;
    }

    public void setCount(int inputCount) {
        count = inputCount;
    }

    public void setUserid(String inputUserid) {
        userid = inputUserid;
    }

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        Element newUserID = new Element("freeUserID");
        if (userid == null || userid.equals("")) {
            count = 0;
        }
        for (int childcnt = 0, forcnt = 0; childcnt < count; forcnt++) {
            String testID = getRandomID(childcnt, forcnt);
            if (!MCRUserManager.exists(testID)) {
                childcnt++;
                Element child = new Element("userid");
                child.setAttribute("ID", testID);
                newUserID.addContent(child);
            }
        }

        org.jdom2.Document newIDs = new org.jdom2.Document(newUserID);
        org.w3c.dom.Document domDoc = null;
        try {
            domDoc = new DOMOutputter().output(newIDs);
        } catch (JDOMException e) {
            LOGGER.error("Domoutput failed: ", e);
        }
        if (pageContext.getAttribute("debug") != null && pageContext.getAttribute("debug").equals("true")) {
            JspWriter out = pageContext.getOut();
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            StringBuffer debugSB = new StringBuffer("<textarea cols=\"80\" rows=\"30\">").append("found this IDs:\r\n")
                    .append(outputter.outputString(newIDs)).append("--------------------\r\nfor the ID\r\n")
                    .append("</textarea>");
            out.println(debugSB.toString());
        }
        pageContext.setAttribute(var, domDoc);

        return;
    }

    private String getRandomID(int len, int trys) {
        String testID = userid;
        if (len == 0)
            len = 1;
        if (trys > 3 * randomChar.length())
            len++;
        for (int i = 0; i < len; i++) {
            double pos = Math.random();
            long ri = Math.round(pos * 1000000) % randomChar.length();
            Integer intObj = Integer.valueOf(Long.toString(ri));
            int index = intObj.intValue();
            testID += randomChar.substring(index, index + 1);
        }
        return testID;
    }

}