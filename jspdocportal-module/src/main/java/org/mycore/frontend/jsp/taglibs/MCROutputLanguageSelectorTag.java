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

package org.mycore.frontend.jsp.taglibs;

import static javax.servlet.jsp.PageContext.PAGE_SCOPE;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;

import org.mycore.frontend.MCRFrontendUtil;

/**
 * <p>Tag that supports rendering the language selector. See below for two usage examples</p>
 * 
 * <p>It receives the following attributes:</p>
 * <ul>
 * <li>languages: A comma separated list of languages, that should be used.
 * For example "de,en"</li>
 * <li>var: The name of the variable that should be bound to the PAGE_SCOPE</p>
 * </ul>
 * 
 * <p>You may use the following page variables within the JSP body (if you set var="lang"):</p>
 * <ul>
 * <li> lang.currentlang - the language currently used in the application</li>
 * <li> lang.first - the first language in the list of available languages</li>
 * <li> lang.lang - the current language while iterating through the list of languages</li>
 * <li> lang.href - the link to be displayed for the current language</li>
 * <li> lang.imageURL - the URL to the image to be displayed for the current language</li>
 * </ul>
 *  
 *  <p>Sample Usage (simple):</p>
 *  
 *  <mcr:outputLanguageSelector languages="de,en" var="lang">
 *    <c:if test="${lang.currentLang != lang.lang}">
 *       <a href="${lang.href}">
 *       <img src="${lang.imageURL}"
 *           style="border-style: none; width: 24px; height: 12px; vertical-align: bottom;" />
 *       </a>
 *    </c:if>
 *  </mcr:outputLanguageSelector>
 *  
 *  Sample Usage (advanced):
 *  
 *  <mcr:outputLanguageSelector languages="de,en" var="lang">
 *   <c:if test="${!lang.first}">&#160;|&#160;</c:if>                     
 *   <c:choose>
 *      <c:when test="${lang_lang == lang.currentLang }">
 *          <span class="lang_link_active">${lang.label}</span>
 *      </c:when>
 *      <c:otherwise>
 *         <a href="${lang.href}" title="${lang.title}" class="lang_link">${lang.label}</a>
 *      </c:otherwise>
 *   </c:choose>                     
 * </mcr:outputLanguageSelector>
 *
 *  
 *  
 * @author Robert Stephan
 *
 */
public class MCROutputLanguageSelectorTag extends MCRAbstractTag {
    private String languages;
    private String var;

    public void doTag() throws JspException, IOException {

        init();

        HttpServletRequest request = (HttpServletRequest) ((PageContext) getJspContext()).getRequest();
        StringBuffer url = request.getRequestURL();
        if(!url.toString().contains("?")) {
            url.append("?");
        }

        JspWriter out = getJspContext().getOut();
        JspContext context = getJspContext();
        JspFragment body = getJspBody();

        Enumeration<String> pnames = (Enumeration<String>) request.getParameterNames();

        while (pnames.hasMoreElements()) {
            String pName = pnames.nextElement();
            if (pName.equals("lang")) {
                continue;
            }

            url.append(pName).append("=").append(request.getParameter(pName));

        }

        boolean first = true;
        for (String l : languages.split(",")) {
            LanguageVariables lv = new LanguageVariables();
            lv.setCurrentLang(lang);
            lv.setFirst(first);
            lv.setLang(l);
            lv.setHref(url.toString() + "&lang=" + l);
            lv.setLabel(getLabel(l));
            lv.setTitle(getTitle(l));
            StringBuilder imageURL = new StringBuilder(MCRFrontendUtil.getBaseURL());
            lv.setImageURL(imageURL.append("images/lang-").append(l).append(".png").toString());
            l = l.trim();
            if (first) {
                first = false;
            }

            context.setAttribute(var, lv, PAGE_SCOPE);
            body.invoke(out);
        }
    }

    /**
     * set the available languages (use comma separated list)
     * @param should look like "de, en".
     */
    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public void setVar(String var) {
        this.var = var;
    }

    protected String getLabel(String lang) {
        StringBuilder sb = new StringBuilder("Webpage.lang.");
        sb.append(lang);
        return retrieveI18N(sb.toString());
    }

    protected String getTitle(String lang) {
        StringBuilder sb = new StringBuilder("Webpage.lang.");
        sb.append(lang).append(".title");

        return retrieveI18N(sb.toString());
    }

    /**
     * 
     * A JavaBean to put into the scope when the {@link MCROutputLanguageSelectorTag} is called.
     * It contains (hopefully) all information you need to create your language selector.
     *
     */
    public static class LanguageVariables {
        private String lang;
        private String href;
        private boolean first;
        private String currentLang;
        private String imageURL;
        private String label;
        private String title;

        /**
         * The abbreviation of the language.
         * @return 'de', 'en' and so on...
         */
        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        /**
         * The URL that points to the very same resource the user requested the last time.
         * Just in a different language
         * @return
         */
        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        /**
         * Allows to check, if this language item is the first.
         * @return
         */
        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        /**
         * The language the user is using right now. Also just as an abbreviation
         * @return 'de', 'en' and so on...
         */
        public String getCurrentLang() {
            return currentLang;
        }

        public void setCurrentLang(String currentLang) {
            this.currentLang = currentLang;
        }

        /**
         * A URL that points to an image with a flag, that represents this language
         * @return
         */
        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public String getTitle() {
            return this.title;

        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLabel() {
            return this.label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }

}
