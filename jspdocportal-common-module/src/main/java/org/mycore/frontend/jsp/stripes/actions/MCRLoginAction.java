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
package org.mycore.frontend.jsp.stripes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.frontend.jsp.stripes.actions.util.MCRLoginNextStep;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

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
@UrlBinding("/login.action")
public class MCRLoginAction extends MCRAbstractStripesAction implements ActionBean {
    public static String SESSION_ATTR_MCR_USER = "mcr.jspdocportal.current_user";
    
    private static Logger LOGGER = LogManager.getLogger(MCRLoginAction.class);

    ForwardResolution fwdResolution = new ForwardResolution("/content/login.jsp");

    private String userID;
    private String password;
    private boolean loginOK;
    private String loginStatus = "user.login";
    private String userName;
    private List<MCRLoginNextStep> nextSteps = new ArrayList<>();

    @DefaultHandler
    public Resolution defaultRes() {
        HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
        if ("true".equals(request.getParameter("logout"))) {
            return doLogout();
        } else {
            MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
            MCRUserInformation mcrUserInfo = mcrSession.getUserInformation();
            if (mcrUserInfo != null && !mcrUserInfo.getUserID().equals("guest")) {
                loginStatus = "user.welcome";
                loginOK = true;
                try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
                    updateData(mcrSession);
                }
            }
        }
        return fwdResolution;
    }

    public Resolution doLogout() {
        HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
        MCRSession session = MCRSessionMgr.getCurrentSession();
        String uid = session.getUserInformation().getUserID();
        LOGGER.debug("Log out user " + uid);
        session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
        request.getSession().removeAttribute(SESSION_ATTR_MCR_USER);
        return fwdResolution;
    }

    public Resolution doLogin() {
        boolean mcrLoginOK = false;

        HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
        MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            String oldUserID = mcrSession.getUserInformation().getUserID();

            if (userID != null) {
                userID = (userID.trim().length() == 0) ? null : userID.trim();
            }
            if (password != null) {
                password = (password.trim().length() == 0) ? password : password.trim();
            }

            if (userID == null && password == null && !"guest".equals(oldUserID)) {
                loginOK = true;
                loginStatus = "user.incomplete";
                userID = oldUserID;
                updateData(mcrSession);
                return fwdResolution;
            }

            if (userID == null || password == null) {
                loginOK = false;
                LOGGER.debug("ID or Password cannot be empty");
                loginStatus = "user.incomplete";
                return fwdResolution;
            }

            LOGGER.debug("Trying to log in user " + userID);
            if (oldUserID.equals(userID)) {
                LOGGER.debug("User " + userName + " with ID " + userID + " is allready logged in");
                loginOK = true;
                loginStatus = "user.exists";
                return fwdResolution;
            }

            mcrLoginOK = loginInMyCore(userID, password, mcrSession, request);
            // interprete the results
            if (mcrLoginOK) {
                loginOK = true;
                loginStatus = "user.welcome";
            } else {
                // the user is not allowed
                loginStatus = "user.unknown";
                loginOK = false;
            }

            updateData(mcrSession);
            return fwdResolution;
        }
    }

    private boolean loginInMyCore(String mcrUserID, String mcrPassword, MCRSession mcrSession, HttpServletRequest request) {
        boolean result = false;
        try {
            MCRUser mcrUser = MCRUserManager.login(mcrUserID, mcrPassword);
            if (mcrUser != null) {
                result = true;
                mcrSession.setUserInformation(mcrUser);
                request.getSession().setAttribute(SESSION_ATTR_MCR_USER, mcrUser);
                loginStatus = "user.welcome";
                LOGGER.debug("user " + userID + " logged in ");
            } else {
                if (userID != null) {
                    loginStatus = "user.invalid_password";
                }
            }
            updateData(mcrSession);
        } catch (MCRException e) {
            result = false;
            if (e.getMessage().equals("user can't be found in the database")) {
                loginStatus = "user.unknown";
            } else if (e.getMessage().equals("Login denied. User is disabled.")) {
                loginStatus = "user.disabled";
            } else {
                loginStatus = "user.unkwnown_error";
                LOGGER.debug("user.unkwnown_error" + e.getMessage());
            }
        }
        LOGGER.info(loginStatus);
        return result;
    }

    /**
     * sets userName and nextSteps variables
     * 
     * @param mcrSession
     */
    private void updateData(MCRSession mcrSession) {
        nextSteps.clear();
        userName = "";

        if (loginOK) {
            StringBuffer name = new StringBuffer();
            ResourceBundle messages = MCRTranslation.getResourceBundle("messages",
                    new Locale(mcrSession.getCurrentLanguage()));
            MCRUser mcrUser = MCRUserManager.getCurrentUser();
            userID = mcrUser.getUserID();
            if ("female".equals(mcrUser.getUserAttribute("sex"))) {
                // Frau
                name.append(messages.getString("Webpage.login.user.salutation.female"));
            } else {
                // Herr
                name.append(messages.getString("Webpage.login.user.salutation.male"));
            }
            name.append(" ");
            name.append(mcrUser.getRealName());
            userName = name.toString();

            for (String groupID : mcrUser.getSystemRoleIDs()) {
                MCRRole mcrgroup = MCRRoleManager.getRole(groupID);
                String link = MCRConfiguration2.getString("MCR.Application.Login.StartLink." + groupID).orElse("").trim();
                if(link.length()>0) {
                	nextSteps.add(new MCRLoginNextStep(MCRFrontendUtil.getBaseURL() + link,
                        mcrgroup.getLabel().getText() + " (" + mcrgroup.getName() + ")"));
                }
            }
        }
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoginOK() {
        return loginOK;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public String getUserName() {
        return userName;
    }

    public List<MCRLoginNextStep> getNextSteps() {
        return nextSteps;
    }
}