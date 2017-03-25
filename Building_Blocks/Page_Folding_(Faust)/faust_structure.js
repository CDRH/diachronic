var documentStructure = (function() {
  "use strict";

  // function to create a configured element in svg namespace
  var createSvgElement = (function(){
    return function(conf) {
      var element;

      // an element can only be created, if a tag name is given
      if( (conf !== undefined) && (conf.name !== undefined) ) {
        // create element
        element = document.createElementNS("http://www.w3.org/2000/svg", conf.name);

        // if a class name is given, it can't be applied like with html elements (setting element.className).
        // so if a class name is given copy it to the attributes array with an attribute name of "class"
        if(conf.class !== undefined) {
          // find out if an attributes array already exists. if not create one
          if(conf.attributes === undefined) {
            conf.attributes = [];
          }
          // append class name to attributes array
          conf.attributes.push(["class", conf.class]);
          // remove class from config object
          delete conf.class;
        }
        // configure svg element
        configureElement(element, conf);
      }

      return element;
    };
  })();



  var event = (function(){
    var event = {};

    // event handling object. events can be triggered by calling events.triggerEvent(eventName, [returnObj]).
    // event handlers are added by calling events.addEventListener(eventName, callback).
    event.createEventQueue = function(){
      // create object with functions to add event handlers and to trigger events
      var events = {};
      var i;

      // create object that will hold all appended listeners
      events.listeners = {};

      // function to call if an event occurred. all listeners attached
      // listening to the same name that was triggered will be called.
      events.triggerEvent = (function(){
        return function(eventName, obj){
          if(events.listeners[eventName] !== undefined) {
            // iterate through every listener
            for(i = 0; i < events.listeners[eventName].length; i++) {
              // if a result exists
              if(obj) {
                // ...pass it through to callback
                events.listeners[eventName][i](obj);
              } else {
                // ...else call callback without parameters
                events.listeners[eventName][i]();
              }
            }
            return true;
          } else {
            return false;
          }
        };
      })();

      // function to add listeners for an event. an event name
      // as well as a callback must be provided
      events.addEventListener = (function(){
        return function(eventName, callback){
          // select appropriate event listener queue
          if(events.listeners[eventName] === undefined) {
            events.listeners[eventName] = [];
          }
          // ... and add listener to queue
          events.listeners[eventName].push(callback);
          return true;
        };
      })();

      return events;
    };

    return event;
  })();





  var configureElement = (function(){
    return function(element, conf) {

      // element can only be configured if a configuration is given
      if (conf !== undefined) {

        // configure id and class(es)
        if(conf.id !== undefined) {
          element.id = conf.id;
        }

        if(conf.class !== undefined) {
          element.className = conf.class;
        }

        // configure ancestors and successors
        if(conf.parent !== undefined) {
          conf.parent.appendChild(element);
        }

        if( (conf.children !== undefined) && (conf.children instanceof Array) ) {
          conf.children.forEach(function(child) {
            element.appendChild(child);
          });
        }

        // configure attributes
        if( (conf.attributes !== undefined ) && (conf.attributes instanceof Array) ) {
          conf.attributes.forEach(function(attribute) {
            element.setAttribute(attribute[0], attribute[1]);
          });
        }

        // configure event listeners
        if( (conf.listeners !== undefined) && (conf.listeners instanceof Array) ) {
          conf.listeners.forEach(function(listener) {
            element.addEventListener(listener[0], listener[1]);
          });
        }

        // configure element object properties
        if( (conf.properties !== undefined) && (conf.properties instanceof Array) ) {
          conf.properties.forEach(function(property) {
            element[property[0]] = property[1];
          });
        }
      }

      // return configured element
      return element;
    };
  })();







  var documentStructure = {};

  var horizontalIndentation = 15;
  var verticalDistance = 15;
  var horizontalLineLength = 200;

  var appendPagesGroup = function(parent, className, events, leftPageNum, rightPageNum) {
    var group = createSvgElement({name: "g", parent: parent, class: "pages-group", properties: [["pages", {"left": leftPageNum, "right": rightPageNum}]]});

    // add event listeners
    ["mouseenter", "mousemove", "mouseleave", "click"].forEach(function(eventName){
      group.addEventListener(eventName, function(){
        events.triggerEvent(eventName, group.pages);
      });
    });

    return group;
  };

  documentStructure.createFromXml = function(xmlDocument) {
    
    var currentY = 0;
    var currentPage = 1;

    var events = event.createEventQueue();

    // create basic dom structure
    // create outmost svg element
    var svg = createSvgElement({name: "svg"});

    // create group that will contain all rects that fill the space between the lines in lineGroup and have
    // event listeners to react to user interactions
    var rectGroup = createSvgElement({name: "g", parent: svg, id: "rectGroup", attributes: [["transform", "translate(" + (2 * horizontalIndentation) + "," + verticalDistance + ")"]]});

    // add event to notify when mouse leaves rect group
    rectGroup.addEventListener("mouseleave", function(){
      events.triggerEvent("rectGroupLeave");
    });

    // create group that will contain all lines that sketch the sheets and pages
    var lineGroup = createSvgElement({name: "g", parent: svg, id: "lineGroup", attributes: [["transform", "translate(" + (2 * horizontalIndentation) + "," + verticalDistance + ")"]]});

    var pageNumTextGroup = createSvgElement({name: "g", parent: svg, id: "pageNumTextGroup", attributes: [["transform", "translate(" + (horizontalLineLength + 2 * horizontalIndentation + 5) + ", 20)"]]});
    
    var setLockedGroup = (function() {
      var lockedGroup;

      return function(pageNum) {
        Array.prototype.slice.call(rectGroup.childNodes).forEach(function(pageGroup) {
          // when a group is clicked, it is locked. that is it will be highlighted until
          // another group is clicked. so if a previous group was clicked remove the 
          // class "pages-locked"
          if(pageGroup.pages.left === pageNum || pageGroup.pages.right === pageNum) {
            if(lockedGroup !== undefined) {
              Faust.dom.removeClassFromElement(lockedGroup, "pages-locked");
            }
            // store new clicked group and assign "pages-locked" css class
            lockedGroup = pageGroup;
            Faust.dom.addClassToElement(pageGroup, "pages-locked");
            events.triggerEvent("structureLockedGroupChange", pageGroup.pages);
          }
        });
      };
    
    })();


    // add pages to svg. there are two things beeing done. first there will be elements added to the lineGroup which
    // show the structure of the document. second there will be elements added to the rectGroup which will allow
    // user interaction with the drawn structure. there will be a group inside the rectGroup containing (in most cases)
    // two rects that represent single pages.
    var addPage = function(nodeDepth) {
      var pagesGroup;

      // special case: generally group inside rectGroup contains two pages. The first and the last page of a document
      // are special since they reside solitary in their parent group. this is caught here. since there is only one page
      // inside the created group, the left hand side gets assigned "undefined" value
      if(currentPage === 1) {
        pagesGroup = appendPagesGroup(rectGroup, "pages-group", events, undefined, currentPage++, setLockedGroup);
      }

      // write the page number of the upper page next to the line representing the leaf the page is on
      var pageNumText = createSvgElement({name: "text",
                                          class: "structure-page-num",
                                          parent: pageNumTextGroup,
                                          attributes: [
                                            ["x", 0],
                                            ["y", currentY],
                                            ["text-anchor", "start"]
                                          ]
      });
      pageNumText.appendChild(document.createTextNode(currentPage - 1));

      // draw rects for user interaction. the problem is, that the last page of the current leaf must be put in a group with the first page
      // of the next group (like when opening a book one sees the bottom side of one page and the upper side of the next page).
      // To achive this we first put our first page in the group that was created before (by the previously processed leaf) and than create
      // a new group to append the second page to. the next processed page will be put into this group, too.
      createSvgElement({name: "rect",
                        class: "structure-rect",
                        parent: rectGroup.lastChild,
                        attributes: [
                          ["x", 0 + (horizontalIndentation * nodeDepth)],
                          ["y", currentY - verticalDistance/2],
                          ["width", horizontalLineLength - (horizontalIndentation * nodeDepth)],
                          ["height", verticalDistance/2]
                        ]
      });

      pagesGroup = appendPagesGroup(rectGroup, "pages-group", events, currentPage++, currentPage++, setLockedGroup);

      createSvgElement({name: "rect",
                        class: "structure-rect",
                        parent: rectGroup.lastChild,
                        attributes: [
                          ["x", 0 + (horizontalIndentation * nodeDepth)],
                          ["y", currentY],
                          ["width", horizontalLineLength - (horizontalIndentation * nodeDepth)],
                          ["height", verticalDistance/2]
                        ]
      });

      // draw the line that symbolises the document structure
      createSvgElement({name: "line",
                        class: "structure-line",
                        parent: lineGroup,
                        attributes: [
                          ["x1", 0 + (horizontalIndentation * nodeDepth)],
                          ["x2", horizontalLineLength],
                          ["y1", currentY],
                          ["y2", currentY]
                        ]
      });

      // assign new y position
      currentY = currentY + verticalDistance;
    };


    var processXmlNode = function(xmlNode, nodeDepth) {
      var sheetYPosBegin;

      // try to get first element child of xmlNode
      var childNode = xmlNode.firstElementChild;

      // check if a child was found and iterate through all children
      if(childNode !== null) {
        do {

          if(childNode.nodeName === "sheet") {
            // store current y position to draw vertical line when finished processing children
            sheetYPosBegin = currentY;

            // add first leaf of sheet to svg
            addPage(nodeDepth);

            // process child elements wrapped into sheet (if exist)
            processXmlNode(childNode, nodeDepth + 1);

            // add last leaf of sheet to svg
            addPage(nodeDepth);

            // draw a line connecting both sheat leafs
            createSvgElement({name: "line", class: "structure-sheet-connector", parent: lineGroup, attributes: [["x1", 0 + (horizontalIndentation * nodeDepth)], ["x2", 0 + (horizontalIndentation * nodeDepth)], ["y1", sheetYPosBegin], ["y2", currentY - verticalDistance]]});

          } else if(childNode.nodeName === "disjunctLeaf") {
            // add leaf to svg
            addPage(nodeDepth);
            processXmlNode(childNode, nodeDepth);
          }

        } while ( (childNode = childNode.nextElementSibling) );
      }
    };


    processXmlNode(xmlDocument.firstChild, 0);
    // since there is only one page in the last rectGroup / pagesGroup the second page is empty. we have assigned it a page
    // number before, so remove it
    rectGroup.lastChild.pages.right = undefined;

    // draw vertical line on left hand side
    createSvgElement({name: "line", class: "structure-vertical-line", parent: svg, attributes: [["x1", horizontalIndentation], ["x2", horizontalIndentation], ["y1", verticalDistance], ["y2", currentY]]});

    // adjust width and height of svg
    // left has a padding of "horizontalIndentation" and there is a gap of the same size between vertical line and horizontal lines.
    // space is added on right side for page numbers and right side padding
    svg.setAttribute("width", horizontalLineLength + (2 * horizontalIndentation) + 30);
    svg.setAttribute("height", currentY + verticalDistance);

    // add  addEventListener function
    svg.addStructureEventListener = events.addEventListener;
    svg.setLockedGroup = setLockedGroup;

    return svg;
  };

  return documentStructure;
  
})();
