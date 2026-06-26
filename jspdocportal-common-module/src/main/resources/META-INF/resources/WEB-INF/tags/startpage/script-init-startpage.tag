<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<fmt:message var="lblSearchPlaceholder" key="Browse.Search.placeholder.count" />
<fmt:message var="lblSearchEmpty" key="Browse.Search.placeholder" />
<mcr:session var="lang" info="language" />

<script type="module">
    import { createApp } from 'vue';
    import {initClassCardComponent} from 'mcr-class-card';
    import {initLatestDocsCardComponent} from 'mcr-latest-docs-card';

    const baseUrl = document.querySelector('meta[name="mcr:baseurl"]').content;
    const mask = document.querySelector('meta[name="mcr:mask"]').content;
    const currentLang = document.querySelector('meta[name="mcr:currentlang"]').content;

    let facetElements = Array.from(document.getElementsByTagName('mcr-class-card'));
    let facetFieldParams = facetElements.map(x => '&facet.field=' + x.getAttribute('facet-field')).join("");
    let latestDocsElements = Array.from(document.getElementsByTagName('mcr-latest-docs-card'));
    let solrFields = latestDocsElements.map(x => x.getAttribute('solr-fields')).join(",");
 
    //const solrUrl = 'https://...de/servlets/solr/select?q=category%3A%22doctype%3Ahistbest%22&fq=state%3Apublished&sort=created+DESC&rows=5&wt=json&indent=true&facet=true&facet.field=ir.material_class.facet&facet.field=ir.epoch_class.facet&facet.field=ir.collection_class.facet&facet.field=ir.provider_class.facet&fl=id,created,ir.cover_url,ir.creator.result,ir.title.result,ir.doctype.result,ir.doctype_en.result,ir.originInfo.result&wt=json';
    //const solrData = await fetch("https://corsproxy.io/?url="+ encodeURIComponent(solrUrl)).then(r => r.json());
    const solrUrl = baseUrl + "servlets/solr/select"
          + "?q=category%3A%22doctype%3A"+mask+"%22"
          + "&fq=state%3Apublished"
          + "&sort=created%20DESC"
          + "&rows=5&wt=json&wt=json&indent=true"
          + "&facet=true"
          + facetFieldParams
          + "&fl="+solrFields;
    const solrData = await fetch(solrUrl).then(r => r.json());
  
    const app = createApp();
    await initClassCardComponent(app, solrData.facet_counts.facet_fields, baseUrl);
    await initLatestDocsCardComponent(app, currentLang, solrData.response, baseUrl);

    //TODO: SearchboxCard (Placeholder + I18N in Suchschlitz mit Trefferzahl aktualisieren (i18n + parameter))

    app.mount(document.getElementById('app'));
</script>
