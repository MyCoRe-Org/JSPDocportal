import { defineComponent, inject, computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const createSearchboxCard = (template) => defineComponent({
  name: 'SearchboxCard',
  template,
  
  props: {
    mask: { type: Object, required: true }, //lazy loading
  },
  
  setup(props) {
    const {t, locale} = useI18n();
    const numFound = inject('numFound');
    const placeholder = computed(() => { 
      return t("Webpage.startpage.browse.searchbox.placeholder").replace("%0%", numFound.toLocaleString());
    })
    const baseUrl = inject('baseUrl');
    const searchTerm = ref('');
    const searchField = ref('allMeta');
    
    function doSearch() {
      window.location = baseUrl + "do/browse/" + props.mask
        +"?_add-filter=" + encodeURIComponent("+" + searchField.value+":"+searchTerm.value);
    }

    return {props, placeholder, baseUrl, searchTerm, searchField, doSearch, t, locale};
  }
});

export async function initSearchboxCardComponent(app, numFound, baseUrl) {
  app.provide('numFound', numFound);
  const tplSearchboxCard = await fetch(baseUrl + 'javascript/vue-components/startpage/SearchboxCard.html').then(r => r.text());
  app.component("McrSearchboxCard", createSearchboxCard(tplSearchboxCard));
}
