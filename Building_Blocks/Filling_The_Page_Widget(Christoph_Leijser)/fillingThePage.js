var fillingThePage = (function(){
  "use strict";

  // default function to create states out of a svg file.
  // all elements having an attribute matching the supplied
  // attributeName will be selected.
  // it is assumed the the attribute value is an integer defining the
  // state on which that element is to be shown.
  //
  // all elements are grouped by their value, so that multiple elements
  // with the same value with be shown/hidden at the same time.
  //
  // this function returns an array of arrays. each outer array represents
  // a single defined state and the inner array is a collection of all elements
  // belonging to that specific state
  var stateExtract = function(svg, attributeName) {
    var states = [];
    var tmpStates = [];

    // select all matching elements
    var stateElementNodes = svg.querySelectorAll("[" + attributeName + "]");
    var stateElements;

    // process elements (if found)
    if(stateElementNodes.length > 0) {
      stateElements = Array.prototype.slice.call(stateElementNodes);
      stateElements.forEach(function(stateElement) {
        // extract value from attribute...
        var stateValue = stateElement.getAttribute(attributeName);
        if(tmpStates[stateValue] === undefined) {
          tmpStates[stateValue] = []; // create new array, if no array exists
        }
        // ... and place in corresponding array position
        tmpStates[stateValue].push(stateElement);
      });

      // remove empty fields if a sparse array was created
      states = tmpStates.filter(function() {
        return true;
      });
    }

    return states;
  };

  // this function returns an object to realize the filling the page functionality.
  // it returns an object that contains the different states that can be displayed
  // and offers functions to display the first and last state as well as functions
  // to go forward or backward n states
  //
  // each state can consist of several svg elements which will be shown or hidden
  // when that state is selected.
  //
  // the fillingThePage function can be called with an configuration object.
  // this allows to alter the attribute name of svg elements which will be used
  // for grouping. it also offers the posibility to supply another function to
  // generate the states.
  // 
  //
  // function calling:
  //   without parameters (elements with "filling-state" attribute will be used):
  //     var ftp = fillingThePage(svgElement);
  //   state navigation:
  //     ftp.firstState();
  //     ftp.lastState();
  //     ftp.increaseState();
  //     ftp.increaseState(3);
  //     ftp.decreaseState();
  //     ftp.decreaseState(5);
  //
  //  with different attribute name:
  //    var ftp = fillingThePage(svgElement, {attributeName: "alternativeAttributeName"});

  var fillingThePage = function(svg, conf) {
    var ftpObject = {};
    var states;         
    var minState = 0;
    var currentState = 0;
    var maxState;

    conf = conf || {};
    conf.attributeName = conf.attributeName || "filling-state";
    conf.stateExtract = conf.stateExtract || stateExtract;

    states = conf.stateExtract(svg, conf.attributeName);

    maxState = states.length;

    // hide all elements
    var hideAllElements = function() {
      states.forEach(function(state) {
        state.forEach(function(stateElement) {
          stateElement.style.visibility = "hidden";
        });
      });
    };

    // hide all elements, then draw all elements that are visible in the current state
    var drawCurrentState = function() {
      hideAllElements();
      states.slice(0, currentState).forEach(function(state) {
        state.forEach(function(stateElement) {
          stateElement.style.visibility = "visible";
        });
      });
    };


    // display the 'step' next step on svg
    var increaseState = function(step) {
      if(step === undefined) {
        step = 1;
      }
      currentState = Math.min(maxState, currentState + step);
      drawCurrentState();
    };

    // remove 'step' steps from svg
    var decreaseState = function(step) {
      if(step === undefined) {
        step = 1;
      }
      currentState = Math.max(minState, currentState - step);
      drawCurrentState();
    };

    // display first state, that is hiding all elements
    var firstState = function() {
      currentState = minState;
      drawCurrentState();
    }

    // display all elements
    var lastState = function() {
      currentState = maxState;
      drawCurrentState();
    }


    // initialize component, which is displaying the current step with all elements hidden
    drawCurrentState();

    // add navigation methods to return object
    ftpObject.increaseState = increaseState;
    ftpObject.decreaseState = decreaseState;
    ftpObject.firstState = firstState;
    ftpObject.lastState = lastState;

    return ftpObject;
  };

  return fillingThePage;
  
})();
