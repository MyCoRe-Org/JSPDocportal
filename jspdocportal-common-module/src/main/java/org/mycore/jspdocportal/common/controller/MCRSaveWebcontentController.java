package org.mycore.jspdocportal.common.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.resource.MCRResourceHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/do/save-webcontent")
public class MCRSaveWebcontentController {
    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    public Response post(@Context HttpServletRequest request) {
        String referer = null;
        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            if (MCRAccessManager.checkPermission("administrate-webcontent")) {
                for (Object o : request.getParameterMap().keySet()) {
                    String s = o.toString();
                    if (s.startsWith("doSave_")) {
                        String id = s.substring(s.indexOf('_') + 1);
                        doSave(id, request);
                        break;
                    }
                    if (s.startsWith("doOpen_")) {
                        String id = s.substring(s.indexOf('_') + 1);
                        getOpenEditorsFromSession().add(id);
                        Map<String, Object> model = new HashMap<>();
                        model.put("id", id);
                        model.put("referer", request.getHeader("Referer"));
                        String file = request.getParameter("file_" + id);
                        model.put("file", file);
                        model.put("content", loadContent(file));

                        Viewable v = new Viewable("/webcontenteditor", model);
                        return Response.ok(v).build();

                    }
                    if (s.startsWith("doCancel_")) {
                        String id = s.substring(s.indexOf('_') + 1);
                        getOpenEditorsFromSession().remove(id);
                        break;
                    }
                }
            }
        }
        if (referer == null) {
            referer = MCRFrontendUtil.getBaseURL();
        }
        return Response.temporaryRedirect(URI.create(referer)).build();
    }

    private void doSave(String id, HttpServletRequest request) {
        getOpenEditorsFromSession().remove(id);
        String file = request.getParameter("file_" + id);
        String content = request.getParameter("content_" + id);
        java.nio.file.Path saveDir =
            Paths.get(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow())
                .resolve(MCRSessionMgr.getCurrentSession().getCurrentLanguage());
        try {
            Files.createDirectories(saveDir);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        try (BufferedWriter bw = Files.newBufferedWriter(saveDir.resolve(file))) {
            bw.append(content);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private Set<String> getOpenEditorsFromSession() {
        @SuppressWarnings("unchecked")
        Set<String> openEditors = (Set<String>) MCRSessionMgr.getCurrentSession().get("open_webcontent_editors");
        if (openEditors == null) {
            openEditors = new HashSet<>();
            MCRSessionMgr.getCurrentSession().put("open_webcontent_editors", openEditors);
        }
        return openEditors;
    }

    private String loadContent(String file) {
        StringWriter out = new StringWriter();
        String lang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        java.nio.file.Path dirSaveWebcontent =
            Paths.get(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow())
                .resolve(lang);
        java.nio.file.Path fText = dirSaveWebcontent.resolve(file);

        try (InputStream is = Files.exists(fText) ? Files.newInputStream(fText)
            : MCRResourceHelper.getResourceAsStream("/config/webcontent/" + lang + "/" + file)) {

            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line = br.readLine();
                    while (line != null) {
                        out.append("\n" + line);
                        line = br.readLine();
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
}
