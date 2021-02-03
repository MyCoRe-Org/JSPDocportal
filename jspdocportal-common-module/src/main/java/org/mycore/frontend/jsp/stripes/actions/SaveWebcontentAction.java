package org.mycore.frontend.jsp.stripes.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

@UrlBinding("/saveWebcontent.action")
public class SaveWebcontentAction extends MCRAbstractStripesAction implements ActionBean {
    private static Logger LOGGER = LogManager.getLogger(SaveWebcontentAction.class);

    private String file = "";

    private String content = "";

    private String referer = null;

    private String id = null;

    public SaveWebcontentAction() {

    }

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void rehydrate() {
        super.rehydrate();
    }

    @DefaultHandler
    public Resolution defaultRes() {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            if (MCRAccessManager.checkPermission("administrate-webcontent")) {
                for (Object o : getContext().getRequest().getParameterMap().keySet()) {
                    String s = o.toString();
                    if (s.startsWith("doSave_")) {
                        id = s.substring(s.indexOf("_") + 1);
                        doSave(id);
                        break;
                    }
                    if (s.startsWith("doOpen_")) {
                        id = s.substring(s.indexOf("_") + 1);
                        getOpenEditorsFromSession().add(id);
                        referer = getContext().getRequest().getHeader("Referer");
                        file = getContext().getRequest().getParameter("file_" + id);
                        content = loadContent();
                        return new ForwardResolution("/editor/editor-webcontent.jsp");
                    }
                    if (s.startsWith("doCancel_")) {
                        id = s.substring(s.indexOf("_") + 1);
                        getOpenEditorsFromSession().remove(id);
                        referer = getContext().getRequest().getHeader("Referer");
                        break;
                    }
                }
            }
        }
        if (referer == null) {
            referer = MCRFrontendUtil.getBaseURL();
        }
        //return new RedirectResolution(getContext().getRequest().getHeader("Referer"), false);
        return new RedirectResolution(referer, false);
    }

    private void doSave(String id) {
        getOpenEditorsFromSession().remove(id);
        file = getContext().getRequest().getParameter("file_" + id);
        content = getContext().getRequest().getParameter("content_" + id);
        referer = getContext().getRequest().getParameter("referer_" + id);
        File saveDir = new File(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow());
        saveDir = new File(saveDir, MCRSessionMgr.getCurrentSession().getCurrentLanguage());
        File saveFile = new File(saveDir, file);
        saveFile.getParentFile().mkdirs();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), "UTF-8"));
            bw.append(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private Set<String> getOpenEditorsFromSession() {
        @SuppressWarnings("unchecked")
        Set<String> openEditors = (Set<String>) MCRSessionMgr.getCurrentSession().get("open_webcontent_editors");
        if (openEditors == null) {
            openEditors = new HashSet<String>();
            MCRSessionMgr.getCurrentSession().put("open_webcontent_editors", openEditors);
        }
        return openEditors;
    }

    private String loadContent() {
        StringWriter out = new StringWriter();
        String lang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        File dirSaveWebcontent = new File(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow());
        dirSaveWebcontent = new File(dirSaveWebcontent, lang);
        File fText = new File(dirSaveWebcontent, file);

        try {
            InputStream is = null;
            if (fText.exists()) {
                is = new FileInputStream(fText);
            } else {
                is = getClass().getResourceAsStream("/config/webcontent/" + lang + "/" + file);
            }
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        out.append("\n" + line);
                    }
                } catch (UnsupportedEncodingException | FileNotFoundException e) {
                    // do nothing
                }

            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return out.toString();
    }

    public String getFile() {
        return file;
    }

    public String getContent() {
        return content;
    }

    public String getReferer() {
        return referer;
    }

    public String getId() {
        return id;
    }
}
