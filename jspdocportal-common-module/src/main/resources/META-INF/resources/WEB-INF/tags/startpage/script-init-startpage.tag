<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<fmt:message var="lblSearchPlaceholder" key="Browse.Search.placeholder.count" />
<fmt:message var="lblSearchEmpty" key="Browse.Search.placeholder" />
<mcr:session var="lang" info="language" />

<script type="module">
    import { createApp } from 'vue';
    import { createI18n } from 'vue-i18n';
    import {initClassCardComponent} from 'mcr-class-card';
    import {initLatestDocsCardComponent} from 'mcr-latest-docs-card';
    import {initSearchboxCardComponent} from 'mcr-searchbox-card';
    const app = createApp();

    const baseUrl = document.querySelector('meta[name="mcr:baseurl"]').content;
    app.provide('baseUrl', baseUrl);

    const mask = document.querySelector('meta[name="mcr:mask"]').content;
    let facetElements = Array.from(document.getElementsByTagName('mcr-class-card'));
    let facetFieldParams = facetElements.map(x => '&facet.field=' + x.getAttribute('facet-field')).join("");
    let latestDocsElements = Array.from(document.getElementsByTagName('mcr-latest-docs-card'));
    let solrFields = latestDocsElements.map(x => x.getAttribute('solr-fields')).join(",");
 
    //const solrUrl = 'https://...de/servlets/solr/select?q=category%3A%22doctype%3Ahistbest%22&fq=state%3Apublished&sort=created+DESC&rows=5&wt=json&indent=true&facet=true&facet.field=ir.material_class.facet&facet.field=ir.epoch_class.facet&facet.field=ir.collection_class.facet&facet.field=ir.provider_class.facet&fl=id,created,ir.cover_url,ir.creator.result,ir.title.result,ir.doctype.result,ir.doctype_en.result,ir.originInfo.result&wt=json';
    //const solrData = await fetch("https://corsproxy.io/?url="+ encodeURIComponent(solrUrl)).then(r => r.json());
    const solrUrl = baseUrl + "api/solr/main/select"
          + "?q=category%3A%22doctype%3A"+mask+"%22"
          + "&fq=state%3Apublished"
          + "&sort=created%20DESC"
          + "&rows=5&wt=json&wt=json&indent=true"
          + "&facet=true"
          + facetFieldParams
          + "&fl="+solrFields;
    const solrData = await fetch(solrUrl).then(r => r.json());
  
    const currentLang = document.querySelector('meta[name="mcr:current_lang"]').content;
    // Messages asynchron laden (top-level await in Modulen erlaubt)
    const messages = await fetch(baseUrl +"api/v1/messages?filter=Webpage.startpage.browse.&format=vue-i18n&lang=de;en").then(r => r.json());
    const i18n = createI18n({
      legacy: false,        // Composition API aktivieren
      locale: currentLang,  // Standardsprache
      fallbackLocale: 'en', // Fallback falls eine Übersetzung fehlt
      messages,
    })
    app.use(i18n);

    await initClassCardComponent(app, solrData.facet_counts.facet_fields, baseUrl);
    await initLatestDocsCardComponent(app, solrData.response, baseUrl);
    await initSearchboxCardComponent(app, solrData.response.numFound, baseUrl);

    app.mount(document.getElementById('app'));
</script>
