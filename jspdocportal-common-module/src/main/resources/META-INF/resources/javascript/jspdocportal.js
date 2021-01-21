//Umlaute und Sonderzeichen in Messageboxen müssen mit unescape() und folgenden 
//Ersetzungen erzeugt werden:
//Ä %C4 	Ö %D6 		Ü %DC 
//ä %E4 	ö %F6 		ü %FC 
//ß %DF 	? %u20AC 	$ %24 	% %25 

     function checkEmail()	{
	    var val = document.setvar.initiatorEmail.value;
    	if ( val.indexOf('@') > 0 ) {
    	    //alert("OK");
    		//document.setvar.submit.disabled=false;
    		return true;
    	}else {
    	    alert(unescape("Bitte geben Sie eine g%FCltige Emailadresse ein."));
    		return false;
		}    	
	  }

     function checkDate()	{
	    var val = document.setvar.endOfSuspension.value;
    	if ( /[0-9][0-9].[0-9][0-9].[0-9][0-9][0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]/.test(val)) {
    	    //alert("OK");
    		//document.setvar.submit.disabled=false;
    		return true;
    	}else {
    	    alert(unescape("Bitte geben Sie ein g%FCltiges Datum ein."));
    		return false;
		}    	
	  }


	  function checkText()	{	  
    	if ( document.setvar.tmpTaskMessage.value !=  'Sie müssen noch...'  ) {
    		return true;
    	}
   		return false;		    	
	  }	  
  
  function reallyDeletefromDB() {
	return confirm(unescape("Achtung! \n Das gesamte Dokument wird damit aus der Digitalen Bibliothek gel%F6scht!"));
  }

  function showHelp(ID) {
     Ereignis = window.event;
     var out = document.getElementById(ID);
     out.style.visibility = "visible";
  }
  
  function hideHelp(ID) {
     var out = document.getElementById(ID);
     out.style.visibility = "hidden";
  }
    
  function checkMyFormUser()	{
    var prefix='/mycoreuser/user/'
    var elAnz = document.forms[0].length;	
    var ret=true;
    for (var i = 0; i < elAnz; ++i){
    	var myElem = document.forms[0].elements[i];
    	var type = myElem.className;
    	if ( type=='mandatory' ){    
    	    if ( myElem.value==null || myElem.value==('') ) {
				myElem.style.backgroundColor='rgb(255,240,240)';
				ret=false;
			} else {
			 //reset if fails in former time
 	 			 myElem.style.backgroundColor='rgb(255,255,255)';
	  		}
    	}
    	
    }
    if ( !ret )	alert(unescape("Es fehlen einige Pflichtangaben. \n Bitte f%FCllen Sie die markierten Felder aus." ));      
	return ret;
  }

  function checkMyForm()	{
    var prefix="/mycoreobject/metadata/"
    var elAnz = document.forms[0].length;	
    var ret=true;
    for (var i = 0; i < elAnz; ++i){
    	var myElem = document.forms[0].elements[i];
    	var type = myElem.className;
    	if ( type=="mandatory" ){    
    	    if ( myElem.value==null || myElem.value=="" ) {
				myElem.style.backgroundColor='rgb(255,240,240)';
				ret=false;
			} else {
			 //reset if fails in former time
	  		   myElem.style.backgroundColor='rgb(255,255,255)';
	  		}
    	}
    	
    }
    if ( !ret )	alert(unescape("Es fehlen einige Pflichtangaben. \n Bitte f%FCllen Sie die markierten Felder aus." ));      
	return ret;
  }
  
  function checkMyFormDocument()	{
    var ret=true;
    ret = checkMyForm();
    return ret;
  }

  
  function fenster(source,title) {
	var win1=null;
	var attr;
	attr="width=450,height=700,scrollbars=yes,resizable=yes,menubar=no,left=100,top=10";
    win1=window.open(source,"Ansicht",attr);
    win1.focus();
	return false;
  }

  function checkMyFormDisshab()	{
    var ret=true;
    ret = checkMyForm();
    return ret;
  }

  function checkMyFormProfessorum()	{
    var ret=true;
    ret = checkMyForm();
    return ret;
  }

 
 
