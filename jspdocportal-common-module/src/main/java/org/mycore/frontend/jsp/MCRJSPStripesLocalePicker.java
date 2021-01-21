package org.mycore.frontend.jsp;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;

import org.mycore.common.MCRSessionMgr;

import net.sourceforge.stripes.localization.DefaultLocalePicker;

/**
 * This class follows the advice on the Stripes Homepage:
 * http://www.stripesframework.org/display/stripes/Localization 
 * 
 * to implement my own Locale Picker and store the locale in session. 
 * 
 * This makes it possible for the user to select a new locale by supplying the requests parameter "lang".
 * 
 * The available locales have to be configured in web.xml as described in the documentation.
 * By providing a default encoding (UTF-8) in the configuration we don't need an extra UTF8StripesFilter class. 
 * 
 * The set locale is also available in JSTL <fmt:message> tags. 
 * This really simplifies our code, because there is no need to set the locale explicitly by <fmt:setLocale>. 
 *  
 * 
 * @author Robert Stephan
 *
 */
public class MCRJSPStripesLocalePicker extends DefaultLocalePicker {

    @Override
    public Locale pickLocale(HttpServletRequest request) {
        String lang = request.getParameter("lang");
        if (lang != null) {
            Locale loc = new Locale(lang);
            if (locales.contains(loc)) {
                request.getSession().setAttribute("stripes_locale", loc);
            } else {
                request.getSession().setAttribute("stripes_locale", locales.get(0));
            }
            MCRSessionMgr.getCurrentSession().setCurrentLanguage(lang);
            Config.set(request.getSession(), Config.FMT_LOCALE, loc);
        }
        if (request.getSession().getAttribute("stripes_locale") == null) {
            request.getSession().setAttribute("stripes_locale", locales.get(0));
        }
        return (Locale) request.getSession().getAttribute("stripes_locale");
    }
}
