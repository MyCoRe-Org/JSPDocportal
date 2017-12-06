package org.mycore.tools.dissonline.formimport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.config.MCRConfiguration;
import org.xml.sax.InputSource;

public class DissOnlineFormImport {
    private static String XSLT_FILE="/xsl/ubr_form2mcr4disshab.xsl";
    private static final Logger LOGGER = LogManager.getLogger(DissOnlineFormImport.class);
    
    public static String[] retrieveMetadataVersions(String folderName) {
        ArrayList<String> result = new ArrayList<String>();
        MCRConfiguration config = MCRConfiguration.instance();
        
        FTPClient ftp = new FTPClient();
        int reply;
        try {
            
            ftp.connect(config.getString("dissonline.upload.ftp.url"));
            ftp.login(config.getString("dissonline.upload.ftp.user"), config.getString("dissonline.upload.ftp.password"));
            reply = ftp.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                ftp.changeWorkingDirectory(config.getString("dissonline.upload.ftp.basedir"));
                ftp.changeWorkingDirectory(folderName);
                for (FTPFile f: ftp.listDirectories()){
                    if(f.getName().startsWith("Metadaten")){
                    result.add(f.getName());
                    }
                }
            }
            
        } catch (SocketException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        finally{
            try{
                ftp.disconnect();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
   
        return result.toArray(new String[]{});
    }

    //can return null
    public static Document retrieveMetadataContent(String folderName, String metadataVersion) {
        Document doc = null;
            MCRConfiguration config = MCRConfiguration.instance();
            
            FTPClient ftp = new FTPClient();
            InputStream is = null;
            int reply;
            try {
                
                ftp.connect(config.getString("dissonline.upload.ftp.url"));
                ftp.login(config.getString("dissonline.upload.ftp.user"), config.getString("dissonline.upload.ftp.password"));
                reply = ftp.getReplyCode();
                if (FTPReply.isPositiveCompletion(reply)) {
                    ftp.changeWorkingDirectory(config.getString("dissonline.upload.ftp.basedir"));
                    ftp.changeWorkingDirectory(folderName);
                    ftp.changeWorkingDirectory(metadataVersion);
                    String filename=null;
                    for(FTPFile file: ftp.listFiles()){
                        if(file.getName().endsWith(".xml")){
                            filename = file.getName();
                        }
                    }
                    if(filename!=null){
                        is = ftp.retrieveFileStream(filename);
                        doc = new SAXBuilder().build(is, "UTF-8");
                    }
                }
                
            } catch (SocketException ex) {
                LOGGER.error(ex);
            } catch (IOException ex) {
                LOGGER.error(ex);
            } catch (JDOMException ex) {
                LOGGER.error(ex);
            }
            finally{
                try{
                    if(is!=null){
                        is.close();
                    }
                    ftp.disconnect();
                    
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
       
            return doc;
      }
    
    public static void loadFormDataIntoMCRObject(String content, Path mcrFile){
        try{
            StreamSource xsltSource = new StreamSource(DissOnlineFormImport.class.getResourceAsStream(XSLT_FILE)); 
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
        
            
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new InputSource(new StringReader(content)));
            

            transformer.setParameter("formData", doc.getChildNodes());
            transformer.setParameter("currentYear", String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("CET"), Locale.GERMANY).get(Calendar.YEAR)));
            
            Path mcrOutFile = mcrFile;
            //debugging: 
            //File mcrOutFile = new File(mcrFile.getPath().replace(".xml", ".out.xml"));
            
            DOMResult domResult = null;
            try(BufferedReader br = Files.newBufferedReader(mcrFile)){
            	StreamSource xmlSource = new StreamSource(br);
            	domResult = new DOMResult();
            	transformer.transform(xmlSource, domResult);
            }
            //output pretty print
            DOMSource domSource = new DOMSource(domResult.getNode());
            try(BufferedWriter bw = Files.newBufferedWriter(mcrOutFile)){
            StreamResult streamResult = new StreamResult(bw);
            Transformer transformerOut = TransformerFactory.newInstance().newTransformer();
            transformerOut.setOutputProperty(OutputKeys.INDENT, "yes");
            transformerOut.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformerOut.transform(domSource,  streamResult);
            }
            
        } catch (Exception e) {
            LOGGER.error("Error processing formdata", e);
        }
    
    }
}
