/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.jspdocportal.ir.solr.index;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.solr.index.MCRSolrIndexHandler;
import org.mycore.solr.index.handlers.MCRSolrLazyInputDocumentHandlerFactory;

/**
 * This indexhandler factory supplies an AltoXML aware file index handler
 * This is necessary because word coordinates are to large to be send via URL parameter
 * Therefore they will be sent as 2nd Solr Atomic Update Request. 
 *  
 * @author Robert Stephan
 *
 */
public class MCRSolrAltoAwareIndexHandlerFactory extends MCRSolrLazyInputDocumentHandlerFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public MCRSolrIndexHandler getIndexHandler(Path file, BasicFileAttributes attrs) {
        return this.getIndexHandler(file, attrs, checkFile(file, attrs));
    }

    @Override
    public MCRSolrIndexHandler getIndexHandler(Path file, BasicFileAttributes attrs, boolean sendContent) {
        if (sendContent) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Solr: submitting file \"{} for indexing", file);
            }
            long start = System.currentTimeMillis();
            /* extract metadata with tika */
            MCRSolrAltoFileIndexHandler indexHandler = new MCRSolrAltoFileIndexHandler(file, attrs);
            long end = System.currentTimeMillis();
            indexHandler.getStatistic().addTime(end - start);
            return indexHandler;
        } else {
            return super.getIndexHandler(file, attrs, sendContent);
        }
    }
}
