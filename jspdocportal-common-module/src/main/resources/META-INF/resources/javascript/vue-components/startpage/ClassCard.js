import { ref, defineComponent, onMounted, inject, provide } from 'vue';
import createClassCardItem from './ClassCardItem.js';

const createClassCard = (template) => defineComponent({
  name: 'ClassCard',
  template,

  props: {
    facetField: { type: String, required: true },
    classid: { type: String, required: true },
    mask: { type: String, required: true },
    root: { type: String, required: false },
    lang: { type: String, required: true }
  },

  setup(props) {
    const baseUrl = inject('baseUrl');
    const facetFields = inject('facetFields');
    const classification = ref({});
    const classid = ref("");
    const sumCounts = ref(new Map());
    const facetCounts = ref(new Map());

    provide("facetField", props.facetField);
    provide("sumCounts", sumCounts);
    provide("facetCounts", facetCounts);
    provide("browseUrl", baseUrl + "do/browse/" + props.mask + "?_add-filter=" + encodeURIComponent("+" + props.facetField + ":" + props.classid + ":"))

    async function fetchClassification() {
      // MyCoRe Rest v1 unterstützt RootId-Filter für Teilbäume
      const langFilter = props.lang ? "lang:"+props.lang : "";
      const rootFilter = props.root ? "root:"+props.root : "";
      const classUrl =  baseUrl + "api/v1/classifications/"+props.classid+"?format=json&filter="+langFilter+";"+rootFilter;
      //const response = await fetch("https://corsproxy.io/?url=" + encodeURIComponent(classUrl));
      const response = await fetch(classUrl);
      classification.value = await response.json();
    };

    function createFacetCounts() {
      const map = new Map();
      let arr = facetFields[props.facetField];
      for(let i = 0; i < arr.length; i = i + 2) {
        map.set(arr[i].substring(arr[i].indexOf(':')+1), arr[i+1]);
      }
      facetCounts.value = map;
    }

    function retrieveCounts(id) {
      let counts = facetCounts[props.facetField];
      let key = props.classid + ':' + id;
      for(let c = 0; c < counts?.length; c++) {
        if(counts[c] == key) {
          return counts[c+1];
        }
      }
      return 0;
    };

    function createSumCounts() {
      const map = new Map();
      fillSumCounts(map, classification.value, []);
      sumCounts.value = map;
    }

    function fillSumCounts(map, categ, anchestors) {
      categ.categories?.forEach((c) => {
        let val = facetCounts.value.get(c.ID);
        if(val == undefined) {
          val = 0;
        }
        anchestors.push(c.ID);
        anchestors.forEach((a) => {
          if(map.has(a)) {
            map.set(a, map.get(a) + val);
          } else {
            map.set(a, val);
          }
        });
        fillSumCounts(map, c, anchestors);
        anchestors.pop();
      });
    }

    onMounted(async () => {
      classid.value = props.classid;
      await fetchClassification();
      createFacetCounts();
      createSumCounts();
    });

    return {props, classification, retrieveLabelText}
  }
});

export function gotoPage(browseUrl, cId) {
  window.location = browseUrl + cId;
};

export function retrieveLabelText(c, lang) {
  //in RestAPI v1 heißt das Label-Array am Root-Element nur label in v2: labels
  const labels = c?.labels ? c?.labels : c?.label;
  if(labels && labels.length > 0 ){
    if(!lang || lang.trim() === ""){
      return labels[0].text;
    }
    for(let lbl of labels){
      if(lbl.lang === "x-"+ lang + "-short"){
        return lbl.text;
      }
    }
    for(let lbl of labels){
      if(lbl.lang == lang){
        return lbl.text;
      }
    }
  }
  return "??? "+ c?.ID + "@" + lang + " ???";
};

export async function initClassCardComponent(app, facetFieldsData, baseUrl) {
  // Alle Templates parallel laden
  const [tplClassCard, tplClassCardItem] = await Promise.all([
    fetch(baseUrl + 'javascript/vue-components/startpage/ClassCard.html').then(r => r.text()),
    fetch(baseUrl + 'javascript/vue-components/startpage/ClassCardItem.html').then(r => r.text())
  ]);

  app.provide('facetFields', facetFieldsData);
  app.component("McrClassCard", createClassCard(tplClassCard));
  app.component("McrClassCardItem", createClassCardItem(tplClassCardItem));
};
