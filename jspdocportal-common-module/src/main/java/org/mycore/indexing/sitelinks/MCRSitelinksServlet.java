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
 * along with MyCoRe.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mycore.indexing.sitelinks;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.content.MCRContent;
import org.mycore.frontend.servlets.MCRContentServlet;
import org.mycore.indexing.sitelinks.dto.MCRSitelinksClusterPageDto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet for managing "Sitelinks" and their associated data.
 * <p>
 * Provides endpoints to display years, months, and publications based on the
 * requested path parameters. Supports pagination and XML content mapping.
 */
public class MCRSitelinksServlet extends MCRContentServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROP_PREFIX = "MCR.Sitelinks.";
    private static final String PATH_PAGE = "page";

    private MCRSitelinksService service;
    private MCRSitelinksPageMapper mapper;
    private int pageSize;

    /**
     * Default constructor for Servlet container.
     * <p>
     * Initialization is done in {@link #init()}.
     */
    public MCRSitelinksServlet() {
        // Leave empty, container will call init()
    }

    /**
     * Constructor for manual instantiation / unit tests.
     *
     * @param service the service responsible for managing object metadata
     * @param mapper the mapper used to transform pages into content representations
     * @param pageSize the maximum number of objects to display per page
     */
    protected MCRSitelinksServlet(MCRSitelinksService service, MCRSitelinksPageMapper mapper, int pageSize) {
        this.service = service;
        this.mapper = mapper;
        this.pageSize = pageSize;
    }

    /**
     * Initializes the servlet from configuration.
     *
     * @throws ServletException if initialization fails
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.service =
                MCRConfiguration2.getSingleInstanceOfOrThrow(MCRSitelinksService.class, PROP_PREFIX + "Service.Class");
            this.mapper =
                MCRConfiguration2.getSingleInstanceOfOrThrow(MCRSitelinksPageMapper.class, PROP_PREFIX + "Mapper.Class");
            this.pageSize = MCRConfiguration2.getString(PROP_PREFIX + "PageSize")
                .map(Integer::valueOf)
                .orElseThrow(
                    () -> new MCRConfigurationException("Please specify property: '" + PROP_PREFIX + "PageSize'"));
        } catch (MCRConfigurationException e) {
            throw new ServletException("Failed to initialize SitelinksServlet", e);
        }
    }

    @Override
    public MCRContent getContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /sitelinks -> list years
            try {
                return mapper.map(service.getRootPage());
            } catch (MCRSitelinksMappingException e) {
                LOGGER.error("Failed to map root page", e);
                return null;
            }
        }
        if (pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }
        String[] pathParts = pathInfo.substring(1).split("/");

        return parseCluster(pathParts[0])
            .flatMap(cluster -> {
                if (pathParts.length == 1) {
                    // GET /sitelinks/{cluster} -> list for cluster (page 1)
                    return getClusterPage(cluster, 1);
                } else if (pathParts.length == 3 && PATH_PAGE.equals(pathParts[1])) {
                    // GET /sitelinks/{cluster}/page/{page} -> list for cluster and page
                    return parsePage(pathParts[2]).flatMap(page -> getClusterPage(cluster, page));
                }
                return Optional.empty();
            }).orElseGet(() -> {
                LOGGER.error(() -> "Invalid or missing content for path" + request.getPathInfo());
                return null;
            });
    }

    private Optional<String> parseCluster(String clusterStr) {
            return Optional.of(clusterStr.trim());
    }

    private Optional<Integer> parsePage(String pageStr) {
        try {
            return Optional.of(Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<MCRContent> getClusterPage(String cluster, int page) {
        try {
            MCRSitelinksClusterPageDto resultPage = service.getClusterPage(cluster, page, pageSize);
            return Optional.of(mapper.map(resultPage));
        } catch (MCRSitelinksNotFoundException e) {
            LOGGER.debug("Sitelinks not found: {}", e.getMessage());
            return Optional.empty();
        } catch (MCRSitelinksMappingException e) {
            LOGGER.error("Mapping failed for cluster {} page {}", cluster, page, e);
            return Optional.empty();
        }
    }
}
