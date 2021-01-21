/**
 * checks the gnd via lobid.org used in xeditor
 */

function checkGND(self) {
	var gnd = $(self).val().replace('http://d-nb.info/gnd/', '');
	if (gnd.length > 0) {
		$.ajax({
			type : 'GET',
			url : 'https://lobid.org/person?format=short.preferredNameForThePerson&id='+ gnd,
			cache : false,
			dataType: 'jsonp',
			success : function(data) {
				if ($(data).size() > 1) {
					$(self).attr('title', 'Name in GND (via lobid.org): '+data[1]);
				}
				else{
					$(self).attr('title', 'Name in GND (via lobid.org): NOT FOUND');
				}
			},
			error : function(){
				$(self).attr('title', 'Name in GND (via lobid.org): ERROR');
			}
		});
	}
}
