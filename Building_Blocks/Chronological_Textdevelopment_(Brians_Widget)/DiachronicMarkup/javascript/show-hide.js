// wait until the document has loaded
$(document).ready(function () {
   
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