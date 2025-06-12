class JSPDocportalUtil {
  
  /* ein erweiterte Bootstrap Popover-System mit gemischten Triggern (Hover + Click) und benutzerdefinierten Schließ-Buttons.
     hover wird als Basis-Trigger verwendet, aber durch zusätzliche Click-Event-Listener erweitert.
     Das ermöglicht folgendes Verhalten: Hover zeigt Popover, Click macht ihn "sticky".
  */   
  static initClickablePopovers() {
    document.querySelectorAll('[id^="btn_ir_popover_"]').forEach(function(popoverTriggerEl) {
      let popover = new bootstrap.Popover(popoverTriggerEl, {
        trigger: 'hover',
        delay: {
          "show": 0,
          "hide": 3000
        },
        html: true,
        content: function() {
          const templateId = popoverTriggerEl.dataset.irPopoverBodyTemplate.replace('#', '');
          const bodyTemplate = document.getElementById(templateId);
          const closeBtn = '<button type="button" class="btn-close float-end ps-1 pb-1" aria-label="Close" data-ir-popover-trigger="#' + popoverTriggerEl.id + '"></button>';
          return '<div>' + closeBtn + bodyTemplate.innerHTML + '</div>';
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
}
