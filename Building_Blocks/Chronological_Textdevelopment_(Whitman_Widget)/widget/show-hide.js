// wait until the document has loaded
$(document).ready(function () {
   
   loadingFade();
   // start the fade immediately after load
   
   /* -------------- */
   /* Fade In Spans */
   /* -------------- */
   
   function loadingFade() {
      hideAllSpans();
      
      // sort the spans by change attribute in ascending order
      var sortedChanges = $("span[change]").sort(function (a, b) {
         var valA = parseInt($(a).attr('change'), 10);
         var valB = parseInt($(b).attr('change'), 10);
         return (valA < valB) ? -1: (valA > valB) ? 1: 0;
      });
      
      // fade the spans in one by one
      sortedChanges.each(function (index) {
         $(this).delay((index) * 800).fadeIn(1000);
      });
   };
   
   function hideAllSpans() {
      $("span[change]").hide();
   };
   
   var undoArray =[]; // will contain spans that the user clicked
   
   /* ------------- */
   /* Span Behavior */
   /* ------------- */
   function aboveInsertion(span) {
      span.toggleClass("above_insertion");
   };
   
   function addOverwriteNull(span) {
      span.toggleClass("add_overwrite_null");
   };
   
   function moveBackInline(span) {
      span.toggleClass("moveBackInline");
   };
   
   function showHide(span) {
      // toggles between display: none and normal display
      span.toggle();
   };
   
   /* ---------------- */
   /* Behavior Chooser */
   /* ---------------- */
   function elementToggler(span) {
      var spanId = span.attr("id") ? span.attr("id"): null;
      if (spanId) {
         if (span.hasClass("overstrike")) {
            showHide(span);
         } else if (span.hasClass("add")) {
            moveBackInline(span);
         } else if (span.hasClass("overwrite")) {
            showHide(span);
         } else if (span.hasClass("add_overwrite")) {
            addOverwriteNull(span);
            // TODO this removes its identifying class so I am using the title
            // for now but it would be better if it had a second class identifier
            // } else if (span.hasClass("above_insertion")) {
         } else if (span.attr("title").indexOf("add insertion") !== -1) {
            aboveInsertion(span);
         } else {
            console.log("This element does not yet have an action assigned to it");
         }
      }
   };
   
   /* ------------- */
   /*     Events    */
   /* ------------- */
   // add onclick event to all spans that will delegate to the correct functions
   $("span").on("click", function () {
      elementToggler($(this));
      undoArray.push($(this));
      // add this span to the list of past actions
      // TODO this assumes that all spans are clickable and have expected behavior
   });
   
   $("#undo").on("click", function () {
      // remove the last element of the undoArray and toggle it
      if (undoArray) {
         if (undoArray.length == 0) {
            alert("No remaining actions to undo.");
         } else {
            lastSpan = undoArray.pop();
            elementToggler(lastSpan);
         }
      }
   });
});
// ends jquery document ready

// TODO there is one span that has a class of "overwrite" but not "add_overwrite"
// so nothing is happening to it!

// TODO above_insertion see note above