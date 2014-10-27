<%@ page import="org.mycore.user.MCRUser,org.mycore.user.MCRUserContact,org.mycore.user.MCRUserMgr,
    org.mycore.common.xml.MCRXMLHelper,	
    org.jdom.filter.ElementFilter,
	java.text.SimpleDateFormat,
	java.text.DateFormat,
	java.util.ArrayList,
	java.util.Iterator,
	java.util.Collections"%>
<%@ page import="org.mycore.frontend.servlets.MCRServlet" %>
<%@ page import="java.io.File" %>
<%@page import="java.util.List"%>
<%	
    String WebApplicationBaseURL = MCRServlet.getBaseURL();
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	MCRUser user = null;
	MCRUserContact contact = null;
	List grouplist = MCRUserMgr.instance().getAllGroupIDs();
	Collections.sort(grouplist);
    String uid_orig = "";
    
	if (request.getParameter("id")==null){
	    // create from empty Editor
		user = new MCRUser(0,null, MCRServlet.getSession(request,"HttpJspBase").getCurrentUserID(), null,null, true, true, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null,null,null,null);
		contact = user.getUserContact();
	}else{
		if (request.getParameter("step") ==null) {
		    // update from database
			user = MCRUserMgr.instance().retrieveUser(request.getParameter("id"));
			uid_orig = user.getID();
		} else {
		    // create from registration file - no database entry 
			String filename = request.getParameter("filename");
			System.out.println("File is: " + filename);
			System.out.println("Step: >" + request.getParameter("step")+"<");
			
			if ( request.getParameter("step").equalsIgnoreCase("register") ) {				
			
	            org.jdom.Document doc = MCRXMLHelper.parseURI(new File(filename).toURI(),false);
           		Iterator it = doc.getDescendants(new ElementFilter("user"));
				if (it.hasNext()) {
					org.jdom.Element uElm =  (org.jdom.Element) it.next();
//org.jdom.output.XMLOutputter xmlout = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
//System.out.print(xmlout.outputString(uElm));
		            user = new MCRUser(uElm, true);

//		            String isFemale = uElm.getChildTextTrim("contact.gender");
//		            if(isFemale.equalsIgnoreCase("true"));{
//						String s = uElm.getChildTextTrim("contact.salutation");
//						if(s.length()>MCRUserContact.salutation_len-2){
//							s.substring(0,MCRUserContact.salutation_len-2);
//						}
//						s="f_"+s;
//					    user.getUserContact().setSalutation(s);		            	
//		            }
		            
		          
				}	            
	            
	        } else if ( request.getParameter("step").equalsIgnoreCase("delete" )) {
	        	// delete the Workflow file
				File file = new File(filename);
				System.out.println("File deleted from filesystem: " + filename);
				if ( file.exists())			file.delete();
				response.sendRedirect(WebApplicationBaseURL + "nav?path=admin.usermanagement.user_registration");
				return;
			}			
		}
		contact = user.getUserContact();
	}


%>


<SCRIPT TYPE="text/javascript">
	function validateOnSubmit() {
		var elem;
	    var errs=0;
        if (!validatePresent(document.forms.details.uid)) errs += 1;
		if (!validatePresent(document.forms.details.upass)) errs += 1;
		if (!validatePresent(document.forms.details.ufirstname)) errs += 1;
		if (!validatePresent(document.forms.details.uname)) errs += 1;
		if (!validatePresent(document.forms.details.uprimgroup)) errs += 1;

		if (errs > 0){
			alert("Bitte kontrollieren Sie die markierten Felder.");
			return false;
		}else{
			return true;
		}
	}
</SCRIPT>

<h4>Benutzer bearbeiten</h4>

<p><a href="<%= WebApplicationBaseURL %>nav?path=admin.usermanagement.user">zur �bersicht</a></p>

<form name="details" method="post" action="<%= WebApplicationBaseURL %>servlets/MCRUserValidateServlet" onSubmit="return validateOnSubmit()">
	
	<table class="access">
		<tr>
			<td>Benutzerkennung <sup class="required">*</sup>:</td>
			<td>
				<input type="text" name="uid" id="uid" value="<%=user.getID()%>" maxlength="20" size="30" style="width:209px" readonly="readonly" onchange="validatePresent(this);">
				<input type="hidden" name="uid_orig" value="<%=uid_orig%>">
			</td>
		</tr>
		<tr>
<% if ( user.getPassword().length()> 1 ) { %>	
			<td>
			<input type="hidden" name="upass" id="upass" value="<%=user.getPassword()%>" ">
			</td>
<% } else { %>
			<td>Passwort <sup class="required">*</sup>:</td>
			<td>
			<input type="password" name="upass" id="upass" value="<%=user.getPassword()%>" maxlength="20" size="30" style="width:209px" onchange="validatePresent(this);"> 
			</td>
<% } %>		
	    </tr>
		<tr>
			<td>Beschreibung/Vorhaben:</td>
			<td>
				<textarea name="udescr" cols="40" rows="4" ><%=user.getDescription()%></textarea>
			</td>
		</tr>
		<tr>
			<td>&#160;</td>
			<td>
				<input type="checkbox" name="uenabled" value="true"
				<% if (user.isEnabled()) out.print("checked"); %>
				> Benutzerkennung aktiv
			</td>
		</tr>
		<tr>
			<td>&#160;</td>
			<td>
				<input type="checkbox" name="uupdate" value="true"
					<% if (user.isUpdateAllowed()) out.print("checked"); %>
				> Accountdaten �nderbar
			</td>
		</tr>
		<tr>
			<td>Prim�re Gruppe <sup class="required">*</sup>:</td>
			<td>
				<select style="width:215px" name="uprimgroup" onchange="validatePresent(this);">
					<option>(bitte ausw�hlen)</option>
				<%
					for (int i=0; i<grouplist.size(); i++){
						if (user.getPrimaryGroupID().equals((String)grouplist.get(i))){
							out.print("<option value=\"" + (String)grouplist.get(i) + "\" selected>" + (String)grouplist.get(i) + "</option>");
						}else{
							out.print("<option value=\"" + (String)grouplist.get(i) + "\">" + (String)grouplist.get(i) + "</option>");
						}
					}
				%>		
				</select>
			</td>
		</tr>
		<tr>
			<td valign="top">Weitere Gruppen:</td>
			<td>
				<select multiple size="5" style="width:215px" name="ugroups">
					<option>(Mehrfachauswahl)</option>
				<%
				
					List gl = new ArrayList();
					if(user.getGroupCount()>0){
						gl = user.getGroupIDs();
					}

					for (int i=0; i<grouplist.size(); i++){
						if(gl.contains((String)grouplist.get(i))){
							out.print("<option value=\"" + (String)grouplist.get(i) + "\" selected>" + (String)grouplist.get(i) + "</option>");
						}else{
							out.print("<option value=\"" + (String)grouplist.get(i) + "\">" + (String)grouplist.get(i) + "</option>");
						}
					}
				%>		
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center" height="20px"><b>Kontaktinformationen</b></td>
		</tr>
		<tr>
			<td>Anrede / Titel:</td>
			<td>
				<input type="text" name="usalutation" value="<%=contact.getSalutation()%>" maxlength="24" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Vorname <sup class="required">*</sup>:</td>
			<td>
				<input type="text" name="ufirstname" id="ufirstname" value="<%=contact.getFirstName()%>" maxlength="64" size="30" style="width:209px" ONCHANGE="validatePresent(this);">
			</td>
		</tr>
		<tr>
			<td>Nachname <sup class="required">*</sup>:</td>
			<td>
				<input type="text" name="uname" id="uname" value="<%=contact.getLastName()%>" maxlength="32" size="30" style="width:209px" ONCHANGE="validatePresent(this);">
			</td>
		</tr>
		<tr>
			<td>Adresse:</td>
			<td><input type="text" name="uaddress" value="<%=contact.getStreet()%>" maxlength="64" size="30" style="width:209px"></td>
		</tr>
		<tr>
			<td>PLZ:</td>
			<td>
				<input type="text" name="upostal" value="<%=contact.getPostalCode()%>" maxlength="32" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Stadt:</td>
			<td>
				<input type="text" name="ucity" value="<%=contact.getCity()%>" maxlength="32" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Land:</td>
			<td>
				<input type="text" name="ucountry" value="<%=contact.getCountry()%>" maxlength="32" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Institution:</td>
			<td>
				<input type="text" name="uinstitution" value="<%=contact.getInstitution()%>" maxlength="64" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Fakult�t/Fachbereich:</td>
			<td>
				<input type="text" name="ufaculty" value="<%=contact.getFaculty()%>" maxlength="64" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Abteilung:</td>
			<td>
				<input type="text" name="udept" value="<%=contact.getDepartment()%>" maxlength="64" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Institut/Einrichtung:</td>
			<td>
				<input type="text" name="uinstitute" value="<%=contact.getInstitute()%>" maxlength="64" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Email:</td>
			<td>
				<input type="text" name="uemail" value="<%=contact.getEmail()%>" maxlength="64" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Telefon:</td>
			<td><input type="text" name="utel" value="<%=contact.getTelephone()%>" maxlength="32" size="30" style="width:209px"></td>
		</tr>
		<tr>
			<td>Fax-Nummer:</td>
			<td>
				<input type="text" name="ufax" value="<%=contact.getFax()%>" maxlength="32" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>Mobiles-Telefon:</td>
			<td>
				<input type="text" name="umobile" value="<%=contact.getCellphone()%>" maxlength="32" size="30" style="width:209px">
			</td>
		</tr>
		<tr>
			<td>&#160;</td>
			<td><sup class="required">* Pflichtfeld</sup></td>
		</tr>
		<tr>
			<td>&#160;
				<input type="hidden" name="creator" value="<%=user.getCreator()%>">
				<input type="hidden" name="creationtime" value="<%=df.format(user.getCreationDate())%>">
				<input type="hidden" name="operation" value="edit">
			</td>
			<td>
				<small>
					<%=user.getCreator()%>,&#160;;
					<%=df.format(user.getCreationDate())%>					
				</small>
			</td>
		</tr>
		<tr>
			<td>&#160;</td>
			<td>
				<input type="reset">
				&#160;
				<input type="submit" onclick="return validateOnSubmit()"  value="Speichern">
			</td>
		</tr>
	</table>

</form>
<br>&#160;<br>