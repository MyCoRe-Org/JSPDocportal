package org.mycore.frontend.jsp.taglibs.docdetails.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class UnibibliographieHRO {
	private static final String BEACON_URL="http://web10.ub.uni-rostock.de/uploads/uni_bibliographie/daten/ubhro_bibliographie_pnd_beacon.txt";
	private static UnibibliographieHRO instance;
	
	private String biblioPNDBeaconEtag;
	private TreeMap<String, Integer> biblioPNDMap;

	
	//Singleton Pattern
	public static synchronized UnibibliographieHRO getInstance(){
		if(instance==null){
			instance = new UnibibliographieHRO();
		}
		return instance;
	}
	
	public UnibibliographieHRO(){
		biblioPNDBeaconEtag = "";			
		biblioPNDMap = new TreeMap<String, Integer>();

	}
	
	/**
	 * returns the number of entries in Rostock University Bibliography
	 * @param pnd - the PND Number
	 * @return the number of hits
	 */
	public int getHitCount(String pnd){
		try {
			//retrieve PND-IDs and their number of occurence from pnd beacon file and store them in local map
			URL url = new URL(BEACON_URL);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestProperty("If-None-Match", biblioPNDBeaconEtag);
			
			//uses ETag (hash code for file content, generated by web server)
			//returns 200 if a newer file is available
			//returns 304 if a file with the same content already exists
			int rsp = urlConnection.getResponseCode();
			if(rsp == 200){
				biblioPNDMap.clear();
				BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				 for (String line; (line = br.readLine()) != null;) {
					 if(line.startsWith("#")){
						 continue;
					 }
					 String[] data = line.split("\\|");
					 if(data.length==2){
						 int i = 0;
						 try{
						 	i = Integer.parseInt(data[1]);
						 }
						 catch(NumberFormatException nfe){
							 //ignore;
						 }
						 biblioPNDMap.put(data[0], i);
					 }
			     }
			}
			biblioPNDBeaconEtag = urlConnection.getHeaderField("ETag");
		
		}
		catch (Exception e) {
			Logger.getLogger("unibliographie.jsp").error(e.getMessage(), e);
		}
		Integer result =  biblioPNDMap.get(pnd);
		if(result==null){
			return 0;
		}
		else{
			return result.intValue();
		}
	}
	
	/**
	 * returns the message which could be displayed as link 
	 * @param pnd the PND number
	 * @return the message
	 */
	public String getMessage(String pnd){
		int count = getHitCount(pnd);
		if(count==1){
			return "1 Nachweis in der Universitätsbibliographie Rostock";
		}
		else{
			return Integer.toString(count)+" Nachweise in der Universitätsbibliographie Rostock";
		}
	}

	/**
	 * returns the Request URL for catalog
	 * @param pnd
	 * @return
	 */
	public String getURL(String pnd){
		return "http://katalog.ub.uni-rostock.de/DB=4/CMD?ACT=SRCHA&SRT=YOP&IKT=5047&TRM="+pnd;
	}
}