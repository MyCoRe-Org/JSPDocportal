<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="doctype" required="true" type="java.lang.String"%>
<%@ attribute name="facetFields" required="true" type="java.lang.String"%>

<c:set var="facetParam" value="" />
	<c:forEach var="x" items="${fn:split(facetFields,',')}">
		<c:set var="facetURLParam">${facetURLParam}&facet.field=${x}</c:set>	
	</c:forEach>

<%--
   facetFields="ir.doctype_class.facet,ir.institution_class.facet,ir.collection_class.facet"
   --> &facet.field=ir.doctype_class.facet&facet.field=ir.institution_class.facet&facet.field=ir.collection_class.facet
--%>

<%-- called in <browse-classification-inner> tag --%>
	<script>
		function changeFacetIncludeURL(key, value, mask) {
			window.location=$("meta[name='mcr:baseurl']").attr("content")
						 	       + "browse/"+mask+"?"
						           + "&_add-filter="
							       + encodeURIComponent("+" + key +":"+ value.replace('epoch:',''));
		}
	</script>
   <%--TODO use SOLR-Parameter "&facet.mincount=1" --%>
    <script>
		$( document ).ready(function() {
			$.ajax({
				type : "GET",
				url : $("meta[name='mcr:baseurl']").attr("content")
				        //api/v1/search
				       +"servlets/solr/select?q=category%3A%22doctype%3A${doctype}%22&rows=1&wt=json&indent=true&facet=true${facetURLParam}&json.wrf=?",
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
					    	if("${doctype}"=="histbest" && $.inArray($(el).attr('data-mcr-facet-value'), ["doctype:histbest.print", "doctype:histbest.manuscript"])!=-1){
					    		$(el).parent().attr('disabled', 'disabled');
					    	}
					    	else{
					    		$(el).parent().addClass('d-none');
					    	}
					    }
					    else{
					    	var c = fvalues[idx + 1];
					    	if(c>0){
								$(el).text(c) ;
					    	}
					    	else{
					    		$(el).parent().addClass('d-none');
					    	}
					 	}
					});
				 } <%--outer success --%>
            });
			
					
	  
		});
     </script>