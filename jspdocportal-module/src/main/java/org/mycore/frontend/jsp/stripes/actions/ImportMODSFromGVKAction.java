package org.mycore.frontend.jsp.stripes.actions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathFactory;
import org.mycore.activiti.MCRActivitiUtils;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.tools.gvkmods.GVKMODSImport;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/importMODSFromGVK.action")
public class ImportMODSFromGVKAction implements ActionBean {
    ForwardResolution fwdResolution = new ForwardResolution("/content/workspace/import/import-mods-from-gvk.jsp");
    private ActionBeanContext context;

    private String returnPath = "";
    private String mcrID = "";
    private String gvkPPN = "";
    private String modsXML = "";

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ImportMODSFromGVKAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        if (getContext().getRequest().getParameter("mcrid") != null) {
            mcrID = getContext().getRequest().getParameter("mcrid");
            if (gvkPPN.equals("")) {
                findGVKPPN();
            }
        }
        if (getContext().getRequest().getParameter("returnPath") != null) {
            returnPath = getContext().getRequest().getParameter("returnPath");
        }
    }

    @DefaultHandler
    public Resolution defaultRes() {
        return fwdResolution;
    }

    public Resolution doRetrieve() {
        if (gvkPPN != null) {
            Element eMODS = GVKMODSImport.retrieveMODS(gvkPPN);
            if (eMODS != null) {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                modsXML = outputter.outputString(eMODS);
            }
        }
        return fwdResolution;
    }

    public Resolution doSave() {
        if (!mcrID.equals("")) {
            try {
                Document docJdom = MCRActivitiUtils.getWorkflowObjectXML(MCRObjectID.getInstance(mcrID));
                Element eMeta = docJdom.getRootElement().getChild("metadata");
                if (eMeta != null) {
                    Element eDefMods = eMeta.getChild("def.modsContainer");
                    if (eDefMods == null) {
                        eDefMods = new Element("def.modsContainer");
                        eMeta.addContent(0, eDefMods);
                        eDefMods.setAttribute("class", "MCRMetaXML");
                    }
                    eDefMods.removeContent();
                    Element eMods = new Element("modsContainer");
                    eDefMods.addContent(eMods);

                    SAXBuilder sb = new SAXBuilder();
                    Element eModsData = sb.build(new StringReader(modsXML)).getRootElement();
                    eMods.addContent(eModsData.detach());
                }

                Path file = MCRActivitiUtils.getWorkflowObjectFile(MCRObjectID.getInstance(mcrID));
                try (BufferedWriter bw = Files.newBufferedWriter(file)) {
                    XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
                    xout.output(docJdom, bw);
                }
            } catch (JDOMException jdome) {
                // do nothing
            } catch (IOException e) {
                // do nothing
            }
        }

        return new ForwardResolution(returnPath);
    }

    public Resolution doCancel() {
        return new ForwardResolution(returnPath);
    }

    public String getMcrID() {
        return mcrID;
    }

    public void setMcrID(String mcrid) {
        this.mcrID = mcrid;
    }

    public String getGvkPPN() {
        return gvkPPN;
    }

    public void setGvkPPN(String gvkppn) {
        this.gvkPPN = gvkppn;
    }

    public String getModsXML() {
        return modsXML;
    }

    public void setModsXML(String modsXML) {
        this.modsXML = modsXML;
    }

    public String getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    private void findGVKPPN() {
        if (!mcrID.equals("")) {
            Document docJdom = MCRActivitiUtils.getWorkflowObjectXML(MCRObjectID.getInstance(mcrID));
            // <identifier type="gvk-ppn">721494285</identifier>>
            Element e = XPathFactory.instance()
                    .compile("/mods:identifier[@type='gvk-ppn']", Filters.element(), null, MCRConstants.MODS_NAMESPACE)
                    .evaluateFirst(docJdom);
            if (e != null) {
                setGvkPPN(e.getTextNormalize());
            }
        }
    }
}
