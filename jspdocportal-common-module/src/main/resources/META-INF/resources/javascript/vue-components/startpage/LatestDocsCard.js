import { defineComponent, inject, provide } from 'vue';
import { useI18n } from 'vue-i18n';

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

export async function initLatestDocsCardComponent(app, solrResponse, baseUrl) {
  app.provide('solrResponse', solrResponse);
  const tplLatestDocsCard = await fetch(baseUrl + 'javascript/vue-components/startpage/LatestDocsCard.html').then(r => r.text());
  app.component("McrLatestDocsCard", createLatestDocsCard(tplLatestDocsCard));
};
