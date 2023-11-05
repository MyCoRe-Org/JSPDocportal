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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * This class will add namespace declarations (prefix-uri pairs) to the XPathUtil class
 * which is used by the JSTL XML Tag Library to process XPath expressions.
 * This allows us to use any namespace prefix in XPath Expressions processed by this JSTL.
 * 
 * Uses the Java Reflection Framework to modify private fields
 * 
 * @author Robert Stephan
 *
 */
public class MCRSetNamespaceTag extends SimpleTagSupport {
    private static Logger LOGGER = LogManager.getLogger(MCRSetNamespaceTag.class);

    private String prefix = "";

    private String uri = "";

    /**
     * the prefix
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * the uri 
     * @param uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /* Eclipse EE4J implementation  */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void doTag() throws JspException, IOException {
        try {
            Class c_XPathUtil = Class.forName("org.apache.taglibs.standard.tag.common.xml.XPathUtil");
            // XPathUtil
            // private static JSTLXPathNamespaceContext jstlXPathNamespaceContext = null;
            Field field = c_XPathUtil.getDeclaredField("JSTL_XPATH_NS_CTX");
            field.setAccessible(true);
            Object nsContext = field.get(null); // param null for static fields

            // //should not happen, because the class should be properly initialized 
            // if (nsContext == null) {
            //    nsContext = new JSTLXPathNamespaceContext();
            //    field.set(null, nsContext);
            // }

            // JSTLXPathNamespaceContext
            // protected void addNamespace(String prefix, String uri ) {
            Class c_JSTLXPathNamespaceContext = Class
                .forName("org.apache.taglibs.standard.tag.common.xml.JSTLXPathNamespaceContext");
            Method m_addNamespace = c_JSTLXPathNamespaceContext.getDeclaredMethod("addNamespace", String.class,
                String.class);
            m_addNamespace.setAccessible(true);
            m_addNamespace.invoke(nsContext, prefix, uri);
        } catch (Exception e) {
            LOGGER.error("Something went wrong adding the namespace", e);
        }
    }

    /* OLD Apache Tomcat Taglib implementation  */
    /*
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
    	try
    	{
    		Class c_XPathUtil = Class.forName("org.apache.taglibs.standard.tag.common.xml.XPathUtil");
    		// XPathUtil
    		// private static JSTLXPathNamespaceContext jstlXPathNamespaceContext = null;
    		Field field = c_XPathUtil.getDeclaredField("jstlPrefixResolver");
    		field.setAccessible( true );
    		JSTLPrefixResolver prefixResolver = (JSTLPrefixResolver)field.get(null);
    		if(prefixResolver == null){
    			prefixResolver = new JSTLPrefixResolver();
    			field.set(null, prefixResolver);
    		}
    		
    		// JSTLXPathNamespaceContext
    		// protected void addNamespace(String prefix, String uri ) {
    		Class c_JSTLPrefixResolver = Class.forName("org.apache.taglibs.standard.tag.common.xml.JSTLPrefixResolver");
    		Method m_addNamespace=  c_JSTLPrefixResolver.getDeclaredMethod("addNamespace", String.class, String.class);
    		m_addNamespace.setAccessible(true);
    		m_addNamespace.invoke(prefixResolver, prefix, uri);
    	}
    	catch( Exception e )
    	{
    		Logger.getLogger(MCRDocDetailsSetNamespaceTag.class).error("Something went wrong adding the namespace", e);
    	}
    }
    */

}
