<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<fmt:message var="lblPlaceholder" key="Browse.Search.placeholder.count" />
<mcr:session var="lang" info="language" />

<mcr:webjarLocator htmlElement="script" project="axios" file="axios.min.js" />
<mcr:webjarLocator htmlElement="script" project="alpinejs" file="cdn.min.js" attribute="defer" />

<script>
  document.addEventListener('alpine:init', () => {
    Alpine.data('startpage', () => ({
        baseurl: document.querySelector('meta[name="mcr:baseurl"]').content,
        mask: document.querySelector('meta[name="mcr:mask"]').content,
        solrResponse: null,
        solrFacetCounts: null,
        search_placeholder: '${lblPlaceholder}'.replace('[x]', 0),
        search_term: '',
        search_field: 'allMeta',
        
        init() {
          var that = this;
          var facetElements = Array.from(document.getElementsByClassName('mcr-facet'));
          var facetFieldParams 
                = facetElements.map(x => '&facet.field=' + x.getAttribute('data-mcr-facet-field')).join("");
          var url = this.baseurl + "servlets/solr/select"
                + "?q=category%3A%22doctype%3A"+this.mask+"%22"
                + "&fq=state%3Apublished"
                + "&sort=created+DESC"
                + "&rows=5&wt=json&indent=true"
                + "&facet=true"
                + facetFieldParams
                + "&fl=id,created,ir.cover_url,ir.creator.result,ir.title.result,"
                +   "ir.doctype.result,ir.doctype_en.result,ir.originInfo.result"
                +"&wt=json";
          
          console.log(url);
          			           
          axios.get(url)
              .then(function (resp) {
                  let numFound = parseInt(resp.data.response.numFound) || 0; //default 0 for NaN
                  that.search_placeholder = '${lblPlaceholder}'.replace('[x]', numFound.toLocaleString())
                  that.solrResponse = resp.data.response;
                  that.solrFacetCounts = resp.data.facet_counts;
                  
                  //test remove count for unirostock to provoke a disabled facet entry
                  //var pos = that.solrFacetCounts.facet_fields['ir.institution_class.facet'].indexOf('institution:unirostock')
                  //that.solrFacetCounts.facet_fields['ir.institution_class.facet'].splice(pos,2)
                  //console.log(that.solrFacetCounts);
                  
              })
              .catch(function (error) {
                  console.log(error);
              });
        },
        
        doSearch() {
          window.location = this.baseurl + "browse/" + this.mask
            +"?_add-filter=" + encodeURIComponent("+" + this.search_field+":"+this.search_term);
        },
        
        updateHiddenStateForParents(el){
            <%-- hide <li> elements in a classification that contain sub lists where all entries are hidden.
                 former jQuery expression: $("li.ir-facets-sublist:not(:has(ul > li.d-block))").addClass('d-none');
            --%>
            var e = el.parentElement;
            do {
                if(e.tagName === 'LI'){
                    this.hideIfAllChildrenAreHidden(e);
                }
                e = e.parentElement;
            } while (e!=null && !e.classList.contains('mcr-facet'));
        },
        
        hideIfAllChildrenAreHidden(el){
            <%-- utility method to update the visibility of categories dependend on the state of their children
              a) hide the sublist if all children are hidden (with style 'display:none')
              b) if there are visible children and the entry has no linked entries
                 then disalbe the entry and set the class 'text-muted'
              former jQuerye expression:
              if("${mask}"=="epub" && $.inArray($(el).attr('data-mcr-facet-value'), ["institution:unirostock"])!=-1){
                  $(el).parent().attr('disabled', 'disabled');
                  $(el).prev().addClass('text-muted');
              }
            --%>
            if(Array.from(el.querySelectorAll(':scope > ul li'))
                    .filter(ele => window.getComputedStyle(ele).getPropertyValue('display')!='none')
                    .length) {
                el.style.display='block';
                if(parseInt(el.dataset.mcrFacetCount)==0){
                  el.previousElementSibling.classList.add('text-muted');
                  el.previousElementSibling.classList.add('disabled');
                  el.previousElementSibling.style.display='block';
                } else {
                  el.previousElementSibling.classList.remove('text-muted');
                  el.previousElementSibling.classList.remove('disabled');
                }
            } else { 
                el.style.display='none';
            }
        }
    }));
  });
</script>
