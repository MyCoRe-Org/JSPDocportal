import { defineComponent, inject, provide } from 'vue';
import { createI18n, useI18n } from 'vue-i18n';

const createLatestDocsCard = (template) => defineComponent({
  name: 'LatestDocsCard',
  template,
    props: {
      mask: { type: String, required: true }
    },
    setup() {
      const {t, locale} = useI18n();
      const solrResponse = inject('solrResponse');
      const baseUrl = inject('baseUrl');
      return {solrResponse, baseUrl, t, locale}
    }
});

export async function initLatestDocsCardComponent(app, lang, solrResponse, baseUrl) {
  // Messages asynchron laden (top-level await in Modulen erlaubt)
  const messages = await fetch(baseUrl+'javascript/vue-components/startpage/messages.json').then(r => r.json());
  const tplLatestDocsCard = await fetch(baseUrl + 'javascript/vue-components/startpage/LatestDocsCard.html').then(r => r.text())

  const i18n = createI18n({
    legacy: false,        // Composition API aktivieren
    locale: lang,         // Standardsprache
    fallbackLocale: 'en', // Fallback falls eine Übersetzung fehlt
    messages,
  })

  app.use(i18n);
  app.provide('solrResponse', solrResponse);
  app.provide('baseUrl', baseUrl);
  app.component("McrLatestDocsCard", createLatestDocsCard(tplLatestDocsCard));
};
