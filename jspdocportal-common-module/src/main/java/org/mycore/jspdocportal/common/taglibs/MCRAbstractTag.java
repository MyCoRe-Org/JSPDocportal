/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
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
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.mycore.jspdocportal.common.taglibs;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.services.i18n.MCRTranslation;

import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Even though this class extends the SimpleTagSupport class, it is not meant to use it as one.
 * 
 * It is just a superclass for real tags, but it provides methods and variables that can be useful
 * @author Robert Stephan, Christian Windolf
 *
 */
public class MCRAbstractTag extends SimpleTagSupport {

    protected MCRSession mcrSession;

    /**
     * contains all i18nized messages
     */
    protected ResourceBundle rbMessages;

    /**
     * the current language in this HTTP/MCR-session
     */
    protected String lang;

    protected void init() {
        mcrSession = MCRSessionMgr.getCurrentSession();
        lang = mcrSession.getCurrentLanguage();
        if (StringUtils.isEmpty(lang)) {
            lang = "de";
        }
        rbMessages = MCRTranslation.getResourceBundle("messages", new Locale(lang));
    }

    /**
     * Looks up for a matching key in the message_**.properties.
     * @param key A valid key
     * @return if the key is not found, it returns "???key???"
     */
    protected String retrieveI18N(String key) {
        if (key == null || key.equals("")) {
            return "";
        } else {
            if (rbMessages.containsKey(key)) {
                return rbMessages.getString(key);
            } else {
                return "???" + key + "???";
            }
        }
    }

}
