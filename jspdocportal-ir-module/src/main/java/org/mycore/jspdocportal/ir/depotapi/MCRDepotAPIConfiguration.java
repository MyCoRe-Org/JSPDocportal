package org.mycore.jspdocportal.ir.depotapi;

import java.nio.file.Path;

/**
 * Configurations for the DepotAPI must implement this interface.
 * The servlet config parameter "config.class" can be used to point to a class implementing this interface
 * which will the be loaded and used by the DepotAPIServlet.
 *  
 * @author Robert Stephan
 *
 */
public interface MCRDepotAPIConfiguration {

    /**
     * returns the file that should be delivered by the servlet
     * @param path - the extra path information from the request URL, 
     * following the servlet path 
     * 
     * @return the file to be delivered or null
     */
    public Path resolveFile(String path);

    /**
     * return how long the browser should cache request to the API.
     * The HTTP-Header "Expires" will be set with this value.
     * @return time in ms or -1 if no browser cache shall be used.
     */
    public long getMaxBrowserCacheAgeInMillis();

    /**
     * returns how long generated ETags should be valid
     * The HTTP Etag mechanism will be used. If the resource has not changed
     * the servlet will return code 304 "Not modified" instead of the resource itself.
     * 
     * @return time in ms or -1 if request shall not be cached
     */
    public long getMaxEtagAgeInMillis();

}
