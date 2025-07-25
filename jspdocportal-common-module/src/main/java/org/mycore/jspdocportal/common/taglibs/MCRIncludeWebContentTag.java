package org.mycore.jspdocportal.common.taglibs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.resource.MCRResourceHelper;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Tag, that displays the specified file in the proper language
 * by adding a "language-suffix" to the filename
 * If the current user has the permission a button to edit this file will be displayed.
 * If the button was clicked the OpenSource CKeditor would be opened to edit the file.
 * 
 * @author Robert Stephan
 */
public class MCRIncludeWebContentTag extends SimpleTagSupport {
    private String file;

    private String id;

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();
        out.flush();

        try (MCRHibernateTransactionWrapper unusedTw = new MCRHibernateTransactionWrapper()) {

            if (!isGuestUser() && MCRAccessManager.checkPermission("administrate-webcontent")) {
                if (getOpenEditorsFromSession().contains(id)) {
                    showEditor(out);
                } else {
                    showEditorButton(out);
                    showText(out);
                }
            } else {
                showText(out);
            }
        }
    }

    private boolean isGuestUser() {
        String userID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
        return (userID.equals(MCRSystemUserInformation.GUEST.getUserID()));
    }

    /**
     * opens the CKEditor
     * by including the editor as described in:
     * http://docs.ckeditor.com/#!/guide/dev_installation
     * 
     */
    private void showEditor(JspWriter out) throws IOException {
        //until we know why the CKEditor cannot be integrated here, we use an external form on a new jsp page;
        showEditorButton(out);
        showText(out);
        /* <script type="text/javascript" src="/ckeditor/ckeditor.js"></script>
        <form method="post">
        	<p><textarea name="editor1">
        	    &lt;p&gt;Initial value.&lt;/p&gt;</textarea>
        		<script type="text/javascript">
        			CKEDITOR.replace( 'editor1', 
        			{customConfig : '/custom/ckeditor_config.js'});
        		</script>
        	</p>
        </form>	*/

        //out.write("\n<script src=\""+MCRFrontendUtil.getBaseURL()+"ckeditor/ckeditor.js\"></script>");
        /*
        out.write("\n<form id=\"editWebcontent_"+id+"\" method=\"post\" action=\"saveWebcontent.action\">");
        out.write("\n    <input type=\"hidden\" name=\"file_"+id+"\" value=\""+file+"\" />"); 
        out.write("\n    <textarea  id=\"taedit_"+id+"\" name=\"content_"+id+"\" rows=\"10\" cols=\"80\">");
        //showText(out);
        out.write("\n    </textarea>");
        out.write("\n<script type=\"text/javascript\">");
        out.write("\n    §(document).ready( function() {§('textarea#taedit_"+id+"').ckeditor(); alert('CKEditor loaded!'); });");
        //out.write("\n    CKEDITOR.replace( 'taedit_"+id+"');");
        //out.write("\n        ,{customConfig : '"+MCRFrontendUtil.getBaseURL() +"admin/ckeditor_config.js'}");
        //out.write("\n    );");
        out.write("\n</script>");
        out.write("\n    <div class=\"panel-body text-bg-warning\">");
        out.write("\n        <input type=\"submit\"  name=\"doSave_"+id+"\" class=\"btn btn-primary\" title=\""
        		+MCRTranslation.translate("Webpage.editwebcontent.save")+"\"><i class=\"fa fa-floppy-o\"></i> "+MCRTranslation.translate("Webpage.editwebcontent.save")+"</input>");
        
        out.write("\n        <input type=\"submit\"  name=\"doCancel_"+id+"\" class=\"btn btn-danger\" title=\""
        		+MCRTranslation.translate("Webpage.editwebcontent.cancel")+"\"><i class=\"fa fa-times\"></i> "+MCRTranslation.translate("Webpage.editwebcontent.cancel")+"</input>");
        
        out.write("\n    </div>");
        out.write("\n</form>");
        */
    }

    private void showEditorButton(JspWriter out) throws IOException {
        String baseurl = getJspContext().getAttribute("WebApplicationBaseURL", PageContext.APPLICATION_SCOPE)
            .toString();
        out.write("\n<div class=\"float-end\">");
        out.write("\n    <form id=\"editWebcontent_" + id + "\" method=\"post\" action=\"" + baseurl
            + "do/save-webcontent\">");
        out.write("\n        <input type=\"hidden\" name=\"file_" + id + "\" value=\"" + file + "\" />");
        out.write("\n        <input type=\"submit\"  name=\"doOpen_" + id
            + "\" value=\"&#9997;\" style=\"font-size:200%;padding:0px 6px;position:relative;z-index:100;\" class=\"btn btn-success\" title=\""
            + MCRTranslation.translate("Webpage.editwebcontent") + "\" />");
        out.write("\n    </form>");
        out.write("\n</div>");
    }

    private void showText(JspWriter out) throws IOException {
        String lang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        Path dirSaveWebcontent = Paths.get(MCRConfiguration2.getString("MCR.WebContent.SaveFolder").orElseThrow())
            .resolve(lang);
        Path fText = dirSaveWebcontent.resolve(file);
        String path = fText.toAbsolutePath().toString();

        try (InputStream is = Files.exists(fText)
            ? Files.newInputStream(fText)
            : MCRResourceHelper.getResourceAsStream("/config/webcontent/" + lang + "/" + file)) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line = br.readLine();
                    while (line != null) {
                        out.println(line);
                        line = br.readLine();
                    }
                } catch (UnsupportedEncodingException | FileNotFoundException e) {
                    //do nothing
                }
            } else {
                out.println("<p class=\"panel-body text-bg-warning\">");
                String dataDir = Paths.get(MCRConfiguration2.getString("MCR.datadir")
                    .orElseThrow()).toAbsolutePath().toString();
                out.println(MCRTranslation.translate("Webpage.editwebcontent.nofile",
                    path.replace(dataDir, "%MCR.datadir% ")));
                out.println("</p>");
            }
        }
    }

    private Set<String> getOpenEditorsFromSession() {
        @SuppressWarnings("unchecked")
        //Set<String> openEditors = (Set<String>)((PageContext) getJspContext()).getSession().getAttribute("open_webcontent_editors");
        Set<String> openEditors = (Set<String>) MCRSessionMgr.getCurrentSession().get("open_webcontent_editors");

        if (openEditors == null) {
            openEditors = new HashSet<>();
        }
        return openEditors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
