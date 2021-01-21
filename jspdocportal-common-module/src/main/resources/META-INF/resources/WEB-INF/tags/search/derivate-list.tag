<%@tag pageEncoding="UTF-8"%> 

<%@tag import="java.nio.file.Files" %>
<%@tag import="java.nio.file.DirectoryStream" %>
<%@tag import="java.nio.file.Path" %>
<%@tag import="java.util.Locale"%>
<%@tag import="org.mycore.access.MCRAccessManager"%>
<%@tag import="org.mycore.common.MCRUtils"%>
<%@tag import="org.mycore.common.MCRPersistenceException"%>
<%@tag import="org.mycore.services.i18n.MCRTranslation"%>
<%@tag import="org.mycore.frontend.MCRFrontendUtil"%>
<%@tag import="org.mycore.datamodel.niofs.MCRPath" %>
<%@tag import="org.mycore.datamodel.niofs.MCRFileAttributes"%>
<%@tag import="org.mycore.datamodel.metadata.MCRDerivate"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetaLinkID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObjectID"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetadataManager"%>
<%@tag import="org.mycore.datamodel.metadata.MCRObject"%>

<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryDAOFactory"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRLabel"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategoryID"%>
<%@tag import="org.mycore.datamodel.classifications2.MCRCategory"%>
<%@tag import="org.mycore.datamodel.metadata.MCRMetaClassification"%>


<%@tag import="org.mycore.frontend.jsp.MCRHibernateTransactionWrapper"%>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="derid" required="true" type="java.lang.String" %>
<%@ attribute name="showSize" required="false" type="java.lang.Boolean" %>

<% 
	MCRDerivate mcrDerivate = null;
	try{
		mcrDerivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derid));
	}
	catch(MCRPersistenceException e){
	    //do nothing
	}
	if(mcrDerivate==null){
		out.write("<b>No derivate found for id: "+derid+".</b>");
	}
	else{
	    try (MCRHibernateTransactionWrapper hib = new MCRHibernateTransactionWrapper()) {
            Path root = MCRPath.getPath(derid.toString(), "/");
            if (root != null) {
				String mainDoc = mcrDerivate.getDerivate().getInternals().getMainDoc();
				String url = MCRFrontendUtil.getBaseURL()+"file/"+mcrDerivate.getOwnerID().toString()+"/"+mcrDerivate.getId().toString()+"/";
				String displayLabel="";
				for(MCRMetaClassification c: mcrDerivate.getDerivate().getClassifications()){
					if("derivate_types".equals(c.getClassId())){
						MCRCategory categ = MCRCategoryDAOFactory.getInstance().getCategory(new MCRCategoryID(c.getClassId(), c.getCategId()),0);
						displayLabel = categ.getCurrentLabel().orElse(new MCRLabel("de", "Dateibereich", null)).getText();
					}
				}
				
                boolean accessAllowed = MCRAccessManager.checkPermission(derid, "read");
                out.write("    <tr class=\"ir-derivate-list-row\">");
                out.write("      <th>"+ displayLabel +"</th>");
                out.write("      <td>");
                out.write("        <ul class=\"ir-derivate-list-files\">");

                try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
                    for (Path theFile: ds) {
                      String fileName =  theFile.getFileName().toString();
                      out.write("      <li>");
                      if (accessAllowed) {
                          String fURL = url + fileName;
                          String fontAwesomeName = "fa fa-file-o";
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".pdf")) {
                        	fontAwesomeName = "far fa-file-pdf";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".jpg")
                            || fileName.toLowerCase(Locale.GERMAN).endsWith(".jpeg")) {
                        	fontAwesomeName = "far fa-file-image";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".doc")
                            || fileName.toLowerCase(Locale.GERMAN).endsWith(".txt")) {
                        	fontAwesomeName = "far fa-file-alt";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".xml")) {
                        	fontAwesomeName = "far fa-file-code";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".mp3")) {
                        	fontAwesomeName = "far fa-file-audio";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".zip")) {
                        	fontAwesomeName = "far fa-file-archive";
                        }
                        if (fileName.toLowerCase(Locale.GERMAN).endsWith(".mp4")
                        	|| fileName.toLowerCase(Locale.GERMAN).endsWith(".mpeg")
                        	|| fileName.toLowerCase(Locale.GERMAN).endsWith(".mpg")) {
                        	fontAwesomeName = "fa fa-file-video-o";
                        }
                        if (Files.isDirectory(theFile)) {
                        	fontAwesomeName = "fa fa-files-o";
                        }
                        out.write("<i class=\"" + fontAwesomeName + "\"></i>&nbsp;&nbsp;");
                        out.write("    <a href=\"" + fURL + "\" target=\"_blank\">");
                        out.write(fileName);
                        out.write("    </a>");
                        if (showSize) { 
                          String md5 = "";
                          if(Files.isRegularFile(theFile)) {
                              @SuppressWarnings("rawtypes")
                              MCRFileAttributes attrs = Files.readAttributes(theFile, MCRFileAttributes.class);
                               md5 = "; MD5: " + attrs.md5sum(); 
                              out.write("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<small>(" +  MCRUtils.getSizeFormatted(attrs.size()).replace(" ","&#160;") + md5 + ")</small>");
                          }
                        }
                        out.write("  </li>");
                    } else {
                    	out.write("  <li>");
                        out.write(fileName.replace(".mets.xml", " .mets.xml"));
                        if (showSize) {
                            out.write(" (" + MCRUtils.getSizeFormatted(Files.size(theFile)).replace(" ","&#160;") + ")<br />");
                        }
                        out.write("&#160;---&#160;"
                            + MCRTranslation.translate("OMD.fileaccess.denied"));
                        out.write("  </li>");
                    }
                }
                out.write("      </ul>");
                out.write("    </td>");
                out.write("  </tr>");
			}
        }
	}
}
%>

