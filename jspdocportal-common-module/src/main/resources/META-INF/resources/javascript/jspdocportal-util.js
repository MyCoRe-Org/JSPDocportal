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
}