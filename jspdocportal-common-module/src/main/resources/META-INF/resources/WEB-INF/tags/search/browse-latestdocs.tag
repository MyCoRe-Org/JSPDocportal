<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="mode" required="true" type="java.lang.String"%>

<div class="ir-latestdocs">
	<h4 style="padding-top:">
		<fmt:message key="Browse.latestdocs" />
	</h4>
	<div id="latest_documents" data-ir-mode="${mode}"></div>
	<a href="${WebApplicationBaseURL}browse/${mode}" class="ir-latestdocs-more-button btn btn-sm btn-primary float-right mt-3">
		<fmt:message key="Browse.LatestDocs.button.more" />
	</a>
	
	<script>
		$( document ).ready(function() {
			 <%-- //aktuelle Dokumente
	            //http://localhost:8080/rosdok/api/v1/search?q=category.top:%22doctype:histbest%22
	            //	docs":[ { "id":"rosdok_document_0000009190",
	            //            "created":"2018-04-19T21:53:08.915Z",
	            //            "ir.cover_url":"file/rosdok_document_0000009190/rosdok_derivate_0000033719/ppn642329060.cover.jpg",
	            //            "ir.creator.result":"Neumann, Ferdinand",
	            //            "ir.title.result":"Die Cultur der Georginen in Deutschland mit besonderer Rücksicht auf Erfurt : (Nebst einer lithographirten Tafel)",
	            //            "ir.doctype.result":"Monographie",
	            //            "ir.originInfo.result":"Weißensee : Großmann , 1841"}, {}, ...]
	       --%>
				
		    $.ajax({
		       	 type: "GET",                                         //api/v1/search
		       	 url: $("meta[name='mcr:baseurl']").attr("content") + "servlets/solr/select?q=category:%22doctype:" + $('#filterValue').parent().data('ir-mode') + "%22%20-objectType:bundle&sort=created+DESC&rows=5&fl=id,created,ir.cover_url,ir.creator.result,ir.title.result,ir.doctype.result,ir.originInfo.result&wt=json&json.wrf=?",
		       	 dataType: "jsonp",
		       	 success: function (data) {
		       		data.response.docs.forEach(function( entry ) {
		       			var card= $("<div></div>").addClass("card ir-latestdocs-card").appendTo("#latest_documents");
		       			var cardBody = $("<div></div>").addClass("card-body");
		       			card.append(cardBody);
		            			
		       			if(entry.hasOwnProperty("ir.creator.result")){
		       				cardBody.append($("<p></p>").addClass("card-text").text(entry["ir.creator.result"]));
		       			}
		       			if(entry.hasOwnProperty("ir.title.result")){
		       				var title = entry["ir.title.result"];
		       				if(title.length>120){
		       					title = title.substring(0,100) + "…";          
		       				}
		       				cardBody.append($("<h5></h5>").addClass("card-title").append($("<a></a>").addClass("card-link").attr("href",  $("meta[name='mcr:baseurl']").attr("content")+ "resolve/id/"+entry["id"]).text(title)));
		       			}
		       			
		       			var cardBodyTR = $("<tr></tr>").appendTo($("<table></table>").appendTo(cardBody));
		       			var cardBodyTDData = $("<td></td>").css("vertical-align","top").appendTo(cardBodyTR);
		       			
		       			if(entry.hasOwnProperty("ir.cover_url")){
		       			  cardBodyTDData.css("width", "67%")
		       			  var coverImg = $("<a></a>").attr("href",  $("meta[name='mcr:baseurl']").attr("content") + "resolve/id/"+entry["id"]).append(
	    						$("<img />").addClass("ir-latestdocs-cover").css("max-width", "100%").css("max-height","180px").css("object-fit","contain")
	    						.attr("src", "../"+entry["ir.cover_url"]));
	    				  cardBodyTR.append($("<td></td>").css("vertical-align","bottom").css("width", "33%").css("padding-left","15px").append(coverImg));
		       			}
		            			
		            	if(entry.hasOwnProperty("ir.originInfo.result")){
		            		cardBodyTDData.append($("<p></p>").addClass("card-text").text(entry["ir.originInfo.result"]));
		            	}
		            	if(entry.hasOwnProperty("ir.doctype.result")){
		            		cardBodyTDData.append($("<p></p>").addClass("card-text text-secondary font-weight-bold").text(entry["ir.doctype.result"]));
		            	}
		            			
		            	var datum = entry["created"];
		            	cardBodyTDData.append($("<p></p>").addClass("card-text text-secondary").text(
		            		datum.substring(8,10)+"."+datum.substring(5,7)+"."+datum.substring(0,4)		
		            	));
		            });
		        }
		    }); <%-- end ajax latest_document --%>
		});
	</script>
</div>