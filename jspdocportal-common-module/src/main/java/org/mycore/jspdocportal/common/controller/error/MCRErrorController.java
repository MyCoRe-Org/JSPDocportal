package org.mycore.jspdocportal.common.controller.error;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.MCRSessionMgr;
import org.mycore.services.i18n.MCRTranslation;

@Path("/do/error")
public class MCRErrorController {
    
    @GET
    public Response defaultRes(@Context HttpServletRequest request) {
        MCRJSPErrorInfo errorInfo = new MCRJSPErrorInfo();
        int status = 500;
        try {
        String msg = (String) request.getAttribute("javax.servlet.error.message");
        if(msg!=null) {
            errorInfo.setMessage(msg);
        }
        
        String i18nKey = request.getParameter("i18n");
        if(i18nKey!=null) {
            MCRSessionMgr.unlock();
            errorInfo.setHeadline(MCRTranslation.translate(i18nKey));
        }
        
        //try to use default:    Throwable exception = (Throwable) req.getAttribute("javax.servlet.error.exception");
        Throwable thr = (Throwable) request.getAttribute("mcr_exception");
        if (thr != null) {
            if(errorInfo.getMessage()==null) {
                errorInfo.setMessage("Exeption:");
            }
            errorInfo.setException(thr);
            request.removeAttribute("mcr_exception");
        }
        
        if(request.getParameter("status")!=null) {
            try {
                status = Integer.parseInt(request.getParameter("status"));
                errorInfo.setStatus(status);
            }
            catch(NumberFormatException nfe) {
                // do nothing, use default
            }
        }
        if(status==404 || status == 500 || status == 410) {
            MCRSessionMgr.unlock();
            errorInfo.setHeadline(MCRTranslation.translate("Resolver.error.code."+status));
        }
        }
        catch(Exception e) {
            errorInfo.setMessage("Error processing an error: "+ e.getMessage() + "\nThe original error message was: "+ errorInfo.getMessage());
            errorInfo.setException(e);
            errorInfo.setStatus(500);
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("errorInfo", errorInfo);
        Viewable v = new Viewable("/error", model);

        return Response.status(status).entity(v).build();
        
    }
}
