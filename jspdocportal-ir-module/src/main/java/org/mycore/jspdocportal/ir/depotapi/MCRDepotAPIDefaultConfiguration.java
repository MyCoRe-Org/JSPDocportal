/* $Revision: $ $Date: $
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
package org.mycore.jspdocportal.ir.depotapi;

import java.nio.file.Path;

/**
 * This class contains the default configuration for the DepotAPI.
 * All options are dissabled.
 * 
 * @author Robert Stephan
 * @version $Revision: $ $Date: $
 */
public class MCRDepotAPIDefaultConfiguration implements MCRDepotAPIConfiguration {

    /**
     * @see org.mycore.frontend.depotapi.MCRDepotAPIConfiguration#resolveFile(java.lang.String)
     */
    @Override
    public Path resolveFile(String path) {
        return null;
    }

    /**
     * @see org.mycore.frontend.depotapi.MCRDepotAPIConfiguration#getMaxBrowserCacheAgeInMillis()
     */
    @Override
    public long getMaxBrowserCacheAgeInMillis() {
        return -1;
    }

    /**
     * @see org.mycore.frontend.depotapi.MCRDepotAPIConfiguration#getMaxEtagAgeInMillis()
     */
    @Override
    public long getMaxEtagAgeInMillis() {
        return -1;
    }
}
