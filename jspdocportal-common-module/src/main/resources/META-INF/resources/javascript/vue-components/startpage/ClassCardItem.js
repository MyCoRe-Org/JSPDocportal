import { defineComponent, inject } from 'vue';
import {retrieveLabelText, gotoPage} from 'mcr-class-card';

export default (template) => defineComponent({
  name: 'ClassCardItem',
  template,
  props: {
    lang: { type: String, required: true },
    category: { type: Object, required: true }, //lazy loading
  },
  setup(props) {
    let sumCounts = inject('sumCounts');
    let facetCounts = inject('facetCounts');
    let browseUrl = inject('browseUrl');
    return {props, sumCounts, facetCounts, browseUrl, retrieveLabelText, gotoPage};
  }
});
