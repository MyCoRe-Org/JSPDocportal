/*
 * $RCSfile$
 * $Revision: 29729 $ $Date: 2014-04-23 11:28:51 +0200 (Mi, 23 Apr 2014) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 */
package org.mycore.jspdocportal.common.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.MCRHibernateTransactionWrapper;
import org.mycore.jspdocportal.common.controller.login.MCRLoginNextStep;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * This class handles the Login into the system.
 * 
 * The actionBean context exposes the following variables:
 * 
 * userID - the userID; loginOK - boolean, true if successfully logged-in.
 * loginStatus - result of the login-process as string userName - the full name
 * of logged-in user nextSteps - list of MCRLoginNextStep (fields: url, label
 * for next actions ...)
 * 
 * @author Robert Stephan
 *
 */
@Path("/do/login")
public class MCRLoginController {

    private static final String MODEL_LOGIN_OK = "loginOK";

    private static final String MODEL_LOGIN_STATUS = "loginStatus";

    public static final String SESSION_ATTR_MCR_USER = "mcr.jspdocportal.current_user";

    private static final Logger LOGGER = LogManager.getLogger();

    @GET
    public Response defaultRes(@QueryParam("logout") @DefaultValue("") String logout,
        @Context HttpServletRequest request) {

        if ("true".equals(logout)) {
            return doLogout(request);
        } else {
            Map<String, Object> model = new HashMap<>();
            MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
            MCRUserInformation mcrUserInfo = mcrSession.getUserInformation();
            if (mcrUserInfo != null && !mcrUserInfo.getUserID().equals("guest")) {
                model.put(MODEL_LOGIN_STATUS, "user.welcome");
                model.put(MODEL_LOGIN_OK, true);

                try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
                    updateData(mcrSession, model);
                }
            }
            Viewable v = new Viewable("/login", model);
            return Response.ok(v).build();
        }
    }

    public Response doLogout(HttpServletRequest request) {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String uid = session.getUserInformation().getUserID();
        LOGGER.debug("Log out user {}", uid);
        session.setUserInformation(MCRSystemUserInformation.GUEST);
        request.getSession().removeAttribute(SESSION_ATTR_MCR_USER);

        Map<String, Object> model = new HashMap<>();
        Viewable v = new Viewable("/login", model);
        return Response.ok(v).build();

    }

    @POST
    public Response doLogin(@FormParam("userID") String userID, @FormParam("password") String password,
        @Context HttpServletRequest request) {
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            Map<String, Object> model = new HashMap<>();
            Viewable v = new Viewable("/login", model);
            Response r = Response.ok(v).build();
            String oldUserID = mcrSession.getUserInformation().getUserID();

            userID = (userID == null || userID.isBlank()) ? null : userID.trim();
            password = (password == null || password.isBlank()) ? null : password.trim();

            if (userID == null && password == null && !"guest".equals(oldUserID)) {
                model.put(MODEL_LOGIN_OK, true);
                model.put(MODEL_LOGIN_STATUS, "user.incomplete");
                model.put("userID", userID);

                updateData(mcrSession, model);
                return r;
            } else if (userID == null || password == null) {
                model.put(MODEL_LOGIN_OK, false);
                model.put(MODEL_LOGIN_STATUS, "user.incomplete");
                return r;
            }

            LOGGER.debug("Trying to log in user {}", userID);
            if (oldUserID.equals(userID)) {
                LOGGER.debug("User " /*+ userName */ + " with ID {} is allready logged in", userID);
                model.put(MODEL_LOGIN_OK, true);
                model.put(MODEL_LOGIN_STATUS, "user.exists");
                updateData(mcrSession, model);
                return r;
            }
            return doLogin(request, mcrSession, model, r, userID, password);
        }
    }

    private Response doLogin(HttpServletRequest request, MCRSession mcrSession, Map<String, Object> model,
        Response r, String userID, String password) {
        boolean mcrLoginOK = loginInMyCore(userID, password, mcrSession, request, model);
        if (mcrLoginOK) {
            model.put(MODEL_LOGIN_OK, true);
            model.put(MODEL_LOGIN_STATUS, "user.welcome");
            updateData(mcrSession, model);
            return Response.temporaryRedirect(URI.create(request.getContextPath() + "/do/workspace/tasks")).build();
        } else {
            // the user is not allowed
            model.put(MODEL_LOGIN_OK, false);
            model.put(MODEL_LOGIN_STATUS, "user.unknown");
        }

        return r;
    }

    private boolean loginInMyCore(String mcrUserID, String mcrPassword, MCRSession mcrSession,
        HttpServletRequest request, Map<String, Object> model) {
        boolean result = false;
        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            MCRUser mcrUser = MCRUserManager.login(mcrUserID, mcrPassword);
            if (mcrUser != null) {
                result = true;
                mcrSession.setUserInformation(mcrUser);
                request.getSession().setAttribute(SESSION_ATTR_MCR_USER, mcrUser);
                model.put(MODEL_LOGIN_STATUS, "user.welcome");
                LOGGER.debug("user {} logged in ", mcrUserID);
                updateData(mcrSession, model);
            } else {
                if (mcrUserID != null) {
                    model.put(MODEL_LOGIN_STATUS, "user.invalid_password");
                }
            }

        } catch (MCRException e) {
            result = false;
            if (e.getMessage().equals("user can't be found in the database")) {
                model.put(MODEL_LOGIN_STATUS, "user.unknown");
            } else if (e.getMessage().equals("Login denied. User is disabled.")) {
                model.put(MODEL_LOGIN_STATUS, "user.disabled");
            } else {
                model.put(MODEL_LOGIN_STATUS, "user.unkwnown_error");
                LOGGER.debug("user.unkwnown_error", e);
            }
        }
        LOGGER.info(() -> model.get(MODEL_LOGIN_STATUS));
        return result;
    }

    /**
     * sets userName and nextSteps variables
     * 
     * @param mcrSession
     */
    private void updateData(MCRSession mcrSession, Map<String, Object> model) {
        try (MCRHibernateTransactionWrapper tw = new MCRHibernateTransactionWrapper()) {
            List<MCRLoginNextStep> nextSteps = new ArrayList<>();

            StringBuffer name = new StringBuffer();
            ResourceBundle messages = MCRTranslation.getResourceBundle("messages",
                Locale.of(mcrSession.getCurrentLanguage()));
            MCRUser mcrUser = MCRUserManager.getCurrentUser();
            if ("female".equals(mcrUser.getUserAttribute("sex"))) {
                // Frau
                name.append(messages.getString("Webpage.login.user.salutation.female"));
            } else {
                // Herr
                name.append(messages.getString("Webpage.login.user.salutation.male"));
            }
            name.append(' ');
            name.append(mcrUser.getRealName());
            model.put("userName", name.toString());
            model.put("userID", mcrUser.getUserID());

            for (String groupID : mcrUser.getSystemRoleIDs()) {
                MCRRole mcrgroup = MCRRoleManager.getRole(groupID);
                String link =
                    MCRConfiguration2.getString("MCR.Application.Login.StartLink." + groupID).orElse("").trim();
                if (link.length() > 0) {
                    nextSteps.add(new MCRLoginNextStep(MCRFrontendUtil.getBaseURL() + link,
                        mcrgroup.getLabel().getText() + " (" + mcrgroup.getName() + ")"));
                }
            }
            model.put("nextSteps", nextSteps);
        }
    }
}
