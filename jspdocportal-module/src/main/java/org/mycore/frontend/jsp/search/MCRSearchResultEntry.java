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
package org.mycore.frontend.jsp.search;

import java.util.LinkedHashMap;

import org.apache.solr.common.SolrDocument;
import org.mycore.common.config.MCRConfiguration;

/**
 * This Java bean class represents a indexbrowser result data object.
 * 
 * @author Robert Stephan
 *
 */
public class MCRSearchResultEntry {
	private String mcrid;
	private String label;
	private String coverURL;

	private LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

	private static MCRConfiguration CONFIG = MCRConfiguration.instance();

	public MCRSearchResultEntry(SolrDocument solrDoc) {
		String objectType = String.valueOf(solrDoc.getFirstValue("objectType"));
		String labelfield = CONFIG.getString("MCR.SearchResult." + objectType + ".Headerfield");
		String[] datafields = CONFIG.getString("MCR.SearchResult." + objectType + ".Datafields").split(",");

		this.mcrid = String.valueOf(solrDoc.getFirstValue("returnId"));
		this.label = String.valueOf(solrDoc.getFirstValue(labelfield));
		for (String df : datafields) {
			Object o = solrDoc.getFirstValue(df);
			if (o != null) {
				addData(df, String.valueOf(o));
			}
		}
		Object o = solrDoc.getFirstValue("cover_url");
		if (o != null) {
			setCoverURL(String.valueOf(o));
		} else {
			String coverField = CONFIG.getString("MCR.SearchResult." + objectType + ".DefaultCoverfield", "");
			if(solrDoc.getFirstValue(coverField)==null){
				setCoverURL("images/cover/default.jpg");
			}
			else{
				setCoverURL("images/cover/default_"	+ String.valueOf(solrDoc.getFirstValue(coverField)) + ".jpg");
			}
		}
	}

	public void addData(String key, String value) {
		data.put(key, value);
	}

	public String getMcrid() {
		return mcrid;
	}
	
	public String getObjectType(){
		return mcrid.split("_")[1];
	}

	public String getLabel() {
		return label;
	}

	public LinkedHashMap<String, String> getData() {
		return data;
	}

	public String getCoverURL() {
		return coverURL;
	}

	public void setCoverURL(String coverURL) {
		this.coverURL = coverURL;
	}

}