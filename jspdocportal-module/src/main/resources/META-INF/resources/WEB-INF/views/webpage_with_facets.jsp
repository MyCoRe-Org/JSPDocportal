<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="org.mycore.frontend.jsp.search.MCRSearchResultDataBean" %>
<%@ page import="org.mycore.common.config.MCRConfiguration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<fmt:message var="pageTitle" key="Webpage.title.${fn:replace(actionBean.path, '/', '.')}" />
<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="main_part">
    
     <div class="container">
     <div class="row">
      <div class="col-12 col-md-8 my-3">
          <mcr:includeWebcontent id="${fn:replace(actionBean.path, '/', '.')}" file="${actionBean.path}.html" />
      </div>
      <div class="col-md-1 d-none d-md-block">
        &nbsp;
      </div>
       		<div class="col-md-3 d-none d-md-block" style="padding-top:3em">
        	 <%--epub or histbest --%>
        	<fmt:message var="img" key="Webpage.browse.${actionBean.path}.image"/>
			<img src="${img}" style="width:100%">
        </div>
      </div>
     
      <c:if test="${(actionBean.path eq 'epub') or (actionBean.path eq 'histbest') }">
            <script type="text/javascript">
				function changeFacetIncludeURL(key, value, mask) {
					window.location=$("meta[name='mcr:baseurl']").attr("content")
							 	       + "browse/"+mask+"?"
							           + "&_add-filter="
							       + encodeURIComponent("+" + key +":"+ value.replace('epoch:',''));
					}
				
				function changeFilterIncludeURL(key, value, mask) {
					window.location=$("meta[name='mcr:baseurl']").attr("content")
			 				    + "browse/"+mask+"?"
				    			+ "&_add-filter="
				    			+ encodeURIComponent("+" + key+":"+value);
				}
										
				</script>
            <c:set var="mask" value="${actionBean.path}" />
            <%
			        MCRSearchResultDataBean result = new MCRSearchResultDataBean();
		    	    result = new MCRSearchResultDataBean();
		        	result.setQuery(MCRConfiguration.instance().getString("MCR.Browse." + pageContext.getAttribute("mask") + ".Query", "*:*"));
		        	result.setMask((String)pageContext.getAttribute("mask"));
		        	result.setAction("browse/" + pageContext.getAttribute("mask"));
		        	result.getFacetFields().clear();
		        	for (String ff : MCRConfiguration.instance().getString("MCR.Browse." + pageContext.getAttribute("mask") + ".FacetFields", "").split(",")) {
		            	if (ff.trim().length() > 0) {
			                result.getFacetFields().add(ff.trim());
			            }
			        }
			        result.setRows(20);
			        MCRSearchResultDataBean.addSearchresultToSession(request, result);
			        result.doSearch();
					pageContext.setAttribute("result", result);					
				%>

            <%-- key=$("input[name='filterField']:checked").val(); value=$('#filterValue').val()); --%>
            <div class="row my-5">
              <div class="col-12 col-md-8">
                <div class="input-group mb-3" data-ir-mode="${actionBean.path}">
                  <input type="text" class="form-control ir-form-control" id="filterValue" name="filterValue" placeholder="Suche "
                         onkeypress="if (event.keyCode == 13) { changeFilterIncludeURL($('input[name=\'filterField\']:checked').val(), $('#filterValue').val(), $('#filterValue').parent().data('ir-mode'));}" />
                   <div class="input-group-append">
                      <button id="filterInclude" class="btn btn-primary" type="button"
                              onclick="changeFilterIncludeURL($('input[name=\'filterField\']:checked').val(), $('#filterValue').val(), $('#filterValue').parent().data('ir-mode'));">
                        <i class="fa fa-search"></i>
                      </button>   
                   </div>
                </div>
                
                <fmt:message key="Browse.Filter.${actionBean.path}.allMeta" var="lblAllMeta"/>
                <fmt:message key="Browse.Filter.${actionBean.path}.content" var="lblContent"/>
                
                <div class="custom-control custom-radio custom-control-inline">
                  <input type="radio" checked="checked" id="filterField1" name="filterField" value="allMeta" class="custom-control-input">
                  <label class="custom-control-label" for="filterField1"><c:out escapeXml="false" value="${fn:replace(lblAllMeta,'<br />', ' ')}" /></label>
                </div>
                <div class="custom-control custom-radio custom-control-inline">
                  <input type="radio" id="filterField2" name="filterField" value="content" class="custom-control-input">
                  <label class="custom-control-label" for="filterField2"><c:out escapeXml="false" value="${fn:replace(lblContent,'<br />', ' ')}" /></label>
                </div>
              </div>
            </div>
          </c:if>
		</div>
		

<%--Facetten --%>
      <div class="bg-light">
        <div class="container">	
          <div class="row">
            <c:if test="${actionBean.path eq 'histbest' }">
                <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                <%-- <search:browse-classification categid="doctype:histbest" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                <search:browse-classification categid="collection:Materialart" mask="${mask}" lang="x-de-short"
                  facetField="ir.collection_class.facet" />
              </div>
              <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.collection_class.facet" /> --%>
                <search:browse-classification categid="collection:Projekte" mask="${mask}"
                  facetField="ir.collection_class.facet" />
              </div>
              <div class="col-md-3 col-12">
                <%-- <search:browse-facet result="${result}" mask="${mask}" facetField="ir.epoch_msg.facet" /> --%>
                <search:browse-classification categid="epoch" mask="${mask}" facetField="ir.epoch_msg.facet" />
              </div>
          </c:if>

            <c:if test="${actionBean.path eq 'epub' }">
              <c:if test="${fn:contains(WebApplicationBaseURL, 'rosdok')}">
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                  <search:browse-classification categid="doctype:epub" mask="${mask}" facetField="ir.doctype_class.facet" />
                </div>
              	<div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.sdnb_class.facet" /> --%>
                  <search:browse-classification categid="SDNB" mask="${mask}" facetField="ir.sdnb_class.facet" />
              	</div>
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.institution_class.facet" /> --%>
                  <search:browse-classification categid="institution" mask="${mask}" facetField="ir.institution_class.facet" />
                </div>
              </c:if>
              <c:if test="${fn:contains(WebApplicationBaseURL, 'dbhsnb') or fn:contains(WebApplicationBaseURL, 'hs-nb')}">
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.doctype_class.facet" /> --%>
                  <search:browse-classification categid="doctype:epub" mask="${mask}" facetField="ir.doctype_class.facet" />
                </div>
              	<div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.sdnb_class.facet" /> --%>
                  <search:browse-classification categid="ghb" mask="${mask}" facetField="ir.ghb_class.facet" />
              	</div>
                <div class="col-md-3 col-12">
                  <%--<search:browse-facet result="${result}" mask="${mask}" facetField="ir.institution_class.facet" /> --%>
                  <search:browse-classification categid="institution:HS.NB.S" mask="${mask}" facetField="ir.institution_class.facet" />
                </div>
              </c:if>
          </c:if>
 
      <div class="col-md-3 col-12 bg-dark">
         <div class="ir-latestdocs">
           <h4 style="padding-top:"><fmt:message key="Browse.latestdocs" /></h4>       
           <div id="latest_documents" data-ir-mode="${actionBean.path}">
            <%--
            <div class="card ir-latestdocs-card">
              <div class="card-body">
                <p class="card-text">Meinhardt, Jennifer</p>
                <h4 class="card-title">
                  <a class="card-link" href="#">Das Konnektom des Cortex cerebri der Ratte</a>
                </h4>
                <table>
                  <tr>
                    <td style="vertical-align: top; width: 67%;">
                      <p class="card-text">Neubrandenburg : Hochschule , 2016</p>
                      <p class="card-text text-secondary font-weight-bold">Bachelorarbeit</p>
                      <p class="card-text text-secondary">14.12.2016</p>
                    </td>
                    <td style="vertical-align: bottom; width: 33%; padding-left: 15px;">
                      <a href="../resolve/id/dbhsnb_thesis_0000001540">
                        <img class="ir-latestdocs-cover" style="max-width: 100%; max-height: 180px; object-fit: contain;" src="http://rosdok.uni-rostock.de/file/rosdok_document_0000012807/rosdok_derivate_0000044495/ppn102519165X.cover.jpg">
                      </a>
                    </td>
                  </tr>
                </table>
              </div>
		    </div> --%>
          </div>
          <a href="/browse/${actionBean.path}" class="ir-latestdocs-more-button btn btn-sm btn-primary float-right mt-3">mehr ...</a>			
	    </div>
      </div>
    </div>
    </div>
    </div>
    
    <div class="bg-light" style="height: 75px;">
      <div class="container h-100">
        <div class="row h-100">
          <div class="col-9 bg-light"></div>
          <div class="col-3 bg-dark"></div>
        </div>
      </div>
    </div>
    <%--TODO use SOLR-Parameter "&facet.mincount=1" --%>
    <script>
		$( document ).ready(function() {
			$.ajax({
				type : "GET",
				url : "${WebApplicationBaseURL}api/v1/search?q=category%3A%22doctype%3A${mask}%22&rows=1&wt=json&indent=true&facet=true&facet.field=ir.doctype_class.facet&facet.field=ir.institution_class.facet&facet.field=ir.collection_class.facet&facet.field=ir.epoch_msg.facet&facet.field=ir.sdnb_class.facet&facet.field=ir.ghb_class.facet&json.wrf=?",
				dataType : "jsonp",
				success : function(data) {
					<%-- num found --%>
            		var x = data.response.numFound;
            		$('#filterValue').attr('placeholder', 'Suche in ' + x.toLocaleString() + ' Dokumenten');
                
            		<%-- facets --%>
					var fc = data.facet_counts.facet_fields;
					$('.mcr-facet-count').each(function(index, el){
						 var fvalues = fc[$(el).attr('data-mcr-facet-field')];
						 //TODO remove tempory fix replace("epoch:", ...)
						 var idx = $.inArray($(el).attr('data-mcr-facet-value').replace("epoch:", ""), fvalues);
					    if(idx == -1){
					    	if("${mask}"=="histbest" && $.inArray($(el).attr('data-mcr-facet-value'), ["doctype:histbest.print", "doctype:histbest.manuscript"])!=-1){
					    		$(el).parent().parent().attr('disabled', 'disabled');
					    	}
					    	else{
					    		$(el).parent().parent().addClass('d-none');
					    	}
					    }
					    else{
					    	var c = fvalues[idx + 1];
					    	if(c>0){
								$(el).text(c) ;
					    	}
					    	else{
					    		$(el).parent().parent().addClass('d-none');
					    	}
					 	}
					});
				 } <%--outer success --%>
            });
			
					
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
	            	 type: "GET",
	            	 url: "../api/v1/search?q=category:%22doctype:" + $('#filterValue').parent().data('ir-mode') + "%22%20-objectType:bundle&sort=created+DESC&rows=5&fl=id,created,ir.cover_url,ir.creator.result,ir.title.result,ir.doctype.result,ir.originInfo.result&wt=json&json.wrf=?",
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
	            				cardBody.append($("<h4></h4>").addClass("card-title").append($("<a></a>").addClass("card-link").attr("href", "../resolve/id/"+entry["id"]).text(title)));
	            			}
	            			
	            			var cardBodyTR = $("<tr></tr>").appendTo($("<table></table>").appendTo(cardBody));
	            			var cardBodyTDData = $("<td></td>").css("vertical-align","top").appendTo(cardBodyTR);
	            			
	            			if(entry.hasOwnProperty("ir.cover_url")){
	            			  cardBodyTDData.css("width", "67%")
	            			  var coverImg = $("<a></a>").attr("href", "../resolve/id/"+entry["id"]).append(
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

  </stripes:layout-component>	
</stripes:layout-render>
