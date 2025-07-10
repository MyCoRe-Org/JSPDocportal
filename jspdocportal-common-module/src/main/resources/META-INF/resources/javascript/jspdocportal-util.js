class JSPDocportalUtil {

  static initPopovers() {
    //init normal popovers
    document.querySelectorAll('[id^="btn_ir_popover_"]').forEach(function(popoverTriggerEl) {
      let popover = new bootstrap.Popover(popoverTriggerEl, {
        delay: {
          "show": 100,
          "hide": 1500
        }
      });
    });
    
    //init clickable popovers
    /* ein erweiterte Bootstrap Popover-System mit gemischten Triggern (Hover + Click) und benutzerdefinierten Schließ-Buttons.
       hover wird als Basis-Trigger verwendet, aber durch zusätzliche Click-Event-Listener erweitert.
       Das ermöglicht folgendes Verhalten: Hover zeigt Popover, Click macht ihn "sticky".
    */
    document.querySelectorAll('[id^="btn_ir_click_popover_"]').forEach(function(popoverTriggerEl) {
      const closeBtn = '<button type="button" class="btn-close float-end" aria-label="Close"'
                       +' data-ir-popover-trigger="#' + popoverTriggerEl.id + '"></button>';
      let popover = new bootstrap.Popover(popoverTriggerEl, {
        trigger: 'hover',
        customClass: 'ir-popover',
        delay: {
          "show": 100,
          "hide": 3000
        },
        html: true,
        content: function() {
          const titleTemplateId = popoverTriggerEl.dataset.irPopoverTitleTemplate?.replace('#', '');
          const bodyTemplateId = popoverTriggerEl.dataset.irPopoverBodyTemplate?.replace('#', '');
          if(bodyTemplateId) {
            const bodyTemplate = document.getElementById(bodyTemplateId);
            if(!titleTemplateId) {
              return '<div>' + closeBtn + bodyTemplate.innerHTML + '</div>';
            }
            return '<div>' + bodyTemplate.innerHTML + '</div>';
          }
        },
        title: function() {
          const titleTemplateId = popoverTriggerEl.dataset.irPopoverTitleTemplate?.replace('#', '');
          if(titleTemplateId) {
            const titleTemplate = document.getElementById(titleTemplateId);
            return '<div>' + closeBtn + titleTemplate.innerHTML + '</div>';
          }
          return '';
        },
        container: 'body',
        sanitize: false
      });

      popoverTriggerEl.addEventListener('click', (event) => {
        const popover = bootstrap.Popover.getOrCreateInstance(event.target)
        if (popoverTriggerEl.dataset.irPopoverClicked == "true") {
          delete popoverTriggerEl.dataset.irPopoverClicked;
          popoverTriggerEl.classList.remove("active");
          popover.hide();
        } else {
          popoverTriggerEl.dataset.irPopoverClicked = "true";
          popoverTriggerEl.classList.add("active");
          popover.show();
        }
      });

      popoverTriggerEl.addEventListener('hide.bs.popover', (event) => {
        if (popoverTriggerEl.dataset.irPopoverClicked == "true") {
          event.preventDefault();
        }
      });
    });

    //globales click event für alle close buttons
    document.addEventListener('click', (eventClick) => {
      const targetEl = eventClick.target;
      const idTriggerEl = targetEl.dataset.irPopoverTrigger;
      if (targetEl.classList.contains("btn-close") && idTriggerEl) {
        const triggerEl = document.getElementById(idTriggerEl.replace("#", ""));
        delete triggerEl.dataset.irPopoverClicked;
        triggerEl.classList.remove("active");
        const popover = bootstrap.Popover.getOrCreateInstance(triggerEl);
        popover.hide();
      }
    });
  }
  
  /**
   * checks for a given DOI to which DOI agency it belongs (via https://doi.org/doiRA/)
   * and redirect the browser to its coresponding metadata page.
   * 
   * The JSON response from https://doi.org/doiRA is:
   * [{ "DOI": "10.29085/9781783304868",
        "RA":  "Crossref" }]
   * 
   * The method call is configured in identifier classification:
   * <category ID="doi">
   *   <label xml:lang="x-portal-url" text="javascript:JSPDocportalUtil.gotoDOIMetadataPage('{0}');" />
   * </category>
   */
  static gotoDOIMetadataPage(doi) {
    fetch('https://doi.org/doiRA/' + doi)
      .then(response => {
        response.json()
          .then(data => {
            if (data[0].RA === 'DataCite') {
              window.location.assign("https://commons.datacite.org/doi.org/" + doi);
            } else if (data[0].RA === 'Crossref') {
              window.location.assign("https://search.crossref.org/?from_ui=yes&q=" + doi);
            } else if (data[0].RA === 'mEDRA') {
              window.location.assign("https://www.medra.org/servlet/view?doi=" + doi);
            } else {
              window.location.assign("https://doi.org/doiRA/" + doi);
            }
          })
      })
  }

  /** 
   * apply the currently set include filter to the given search page
   * by generating a new url with parameters
   * (used on browsing pages)
   */
  static applyIncludeSearchFilter(mask) {
    const url = new URL("do/browse/" + mask, document.querySelector("meta[name='mcr:baseurl']").content);
    url.searchParams.set("_search", document.querySelector("meta[name='mcr:search.id']").content);
    url.searchParams.set("_add-filter", "+" + document.querySelector("input[name='filterField']:checked").value
                                         +":" + document.getElementById("filterValue").value);
    window.location.href = url.toString();
  }

  /** 
   * apply the currently set include filter to the given search page
   * by generating a new url with parameters
   * (used on browsing pages)
   */  
  static applyExcludeSearchFilter(mask) {
    const url = new URL("do/browse/" + mask, document.querySelector("meta[name='mcr:baseurl']").content);
    url.searchParams.set("_search", document.querySelector("meta[name='mcr:search.id']").content);
    url.searchParams.set("_add-filter", "-" + document.querySelector("input[name='filterField']:checked").value
                                         + ":" + document.getElementById("filterValue").value);
    window.location.href = url.toString();
  }
}
