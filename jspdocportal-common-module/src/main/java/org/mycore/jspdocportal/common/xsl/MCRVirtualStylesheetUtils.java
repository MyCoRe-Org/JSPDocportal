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

package org.mycore.jspdocportal.common.xsl;

/**
 * Utility methods for building synthetic ("virtual") XSL stylesheets that exist only
 * in memory rather than as a real classpath or filesystem resource.
 */
public class MCRVirtualStylesheetUtils {

    private MCRVirtualStylesheetUtils() {
    }

    /**
     * Builds a minimal XSL stylesheet that imports the given stylesheet via the
     * {@code xslImport:} URI scheme.
     *
     * @param importName the name of the stylesheet to import, resolved via {@code xslImport:<importName>}
     * @param outputMethod the XSLT output method (e.g. {@code html}, {@code xml}, {@code text})
     * @return the generated XSL stylesheet source
     */
    public static String createVirtualStylesheet(String importName, String outputMethod) {
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <xsl:stylesheet version="3.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

            <xsl:import href="xslImport:%s"/>
            <xsl:output method="%s" indent="yes" standalone="no" encoding="UTF-8" />

        </xsl:stylesheet>
        """.formatted(importName, outputMethod);
    }
}
