var createChessboard = (function() {
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

  // configure a dom element
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

  // create a container to render svg in
  var createRenderContainer = (function() {

    return function() {
      var renderContainer = document.createElement("div");

      renderContainer.style.height = "0px";
      renderContainer.style.width = "0px";

      renderContainer.style.overflow = "hidden";

      renderContainer.style.position = "fixed";
      renderContainer.style.top = "0px";
      renderContainer.style.left = "0px";

      return renderContainer;
    };
  })();
  


  var createChessboard = function(data, conf) {

    conf = conf || {};

    conf.tile = conf.tile || {};
    conf.tile.height = conf.tile.height || 40;
    conf.tile.width = conf.tile.width || 22.5;
    conf.tile.verticalDistance = conf.tile.verticalDistance || 35;
    conf.tile.horizontalDistance = conf.tile.horizontalDistance || 2.5;

    conf.highlight = conf.highlight || {};
    conf.highlight.horizontalMargin = conf.highlight.horizontalMargin || 2;
    conf.highlight.verticalMargin = conf.highlight.verticalMargin || 2;

    conf.label = conf.label || {};
    conf.label.horizontal = conf.label.horizontal || {};
    conf.label.horizontal.width = conf.label.horizontal.width || 75;
    conf.label.horizontal.offset = conf.label.horizontal.offset || 27.5;
    conf.label.horizontal.gap = conf.label.horizontal.gap || 10;

    conf.label.vertical = conf.label.vertical || {};
    conf.label.vertical.height = conf.label.vertical.height || 75;
    conf.label.vertical.offset = conf.label.vertical.offset || 20;
    conf.label.vertical.gap = conf.label.vertical.gap || 10;

    var maxRowLength = 0;

    // create a hidden container to draw svg until finished
    var renderContainer = createRenderContainer();
    document.body.appendChild(renderContainer);
    // create svg element
    var svg = createSvgElement({name: "svg", class: "chessboard", parent: renderContainer,
                                attributes: [["version", "1.1"],["baseprofile", "full"],["xmlns", "http://www.w3.org/2000/svg"],["xmlns:xlink", "http://www.w3.org/1999/xlink"]]});

    // create groups
    var tilesGroup = createSvgElement({name: "g", id: "tilesGroup", class: "tiles-group", parent: svg,
                                       attributes: [["transform", "translate(" + (conf.label.horizontal.width + conf.label.horizontal.gap) + ", " + (conf.label.vertical.height + conf.label.vertical.gap) + ")"]] });
    var highlightGroup = createSvgElement({name: "g", id: "highlightGroup", class: "highlight-group", parent: svg,
                                           attributes: [["transform", "translate(-" + conf.highlight.horizontalMargin + ", -" + conf.highlight.verticalMargin + ")"]]});
    var rowTextGroup = createSvgElement({name: "g", id: "rowTextGroup", class: "row-text-group", parent: svg,
                                         attributes: [["transform", "translate(" + conf.label.horizontal.width + ", " + (conf.label.horizontal.offset + conf.label.vertical.height + conf.label.vertical.gap) + ")"]] });
    var columnTextGroup = createSvgElement({name: "g", id: "columnTextGroup", class: "column-text-group", parent: svg,
                                            attributes: [["transform", "translate(" + (conf.label.vertical.offset + conf.label.horizontal.width + conf.label.horizontal.gap) + ", " + conf.label.vertical.height + ")"]] });

    // draw labels for rows and columns
    if(data.rowLabels) {
      data.rowLabels.forEach(function(rowLabel, rowIndex) {
        var rowText = createSvgElement({name: "text", id: "rowText-" + rowIndex, class: "row-text", parent: rowTextGroup, children: [document.createTextNode(rowLabel)],
                                        attributes: [["x", "0"], ["y", (rowIndex * (conf.tile.height + conf.tile.verticalDistance)) ]]});
      });
    }

    if(data.columnLabels) {
      data.columnLabels.forEach(function(columnLabel, columnIndex) {
        var rowText = createSvgElement({name: "text", id: "columnText-" + columnIndex, class: "column-text", parent: columnTextGroup, children: [document.createTextNode(columnLabel)],
                                        attributes: [["x", "0"], ["y", (columnIndex * (conf.tile.width + conf.tile.horizontalDistance)) ], ["transform", "rotate(-90)"]]});
      });
    }

/*
var a = createSvgElement({name: "a", parent: highlightGroup});
a.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "http://beta.faustedition.net");

var rect = createSvgElement({name: "rect", class: "highlight show-tooltip", parent: a, attributes: [["x", "0"], ["y", "0"], ["width", "200"], ["height", "200"]]});
*/
    // process each row, then each tile in a row and draw the tile
    data.rows.forEach(function(rowData, rowIndex) {
      var rowGroup = createSvgElement({name: "g", id: "rowGroup-" + rowIndex, class: "rowGroup", parent: tilesGroup,
                                       attributes: [["transform", "translate(0," + (rowIndex * (conf.tile.height + conf.tile.verticalDistance)) + ")"]]});

      rowData.forEach(function(className, tileIndex) {
        var tileRect = createSvgElement({name: "rect", class: className + " tile", parent: rowGroup,
                                         attributes: [["x", (tileIndex * (conf.tile.width + conf.tile.horizontalDistance))], ["y", "0"], ["width", conf.tile.width], ["height", conf.tile.height]]});

        // count maximum of tiles / columns
        if(tileIndex >= maxRowLength) {
          maxRowLength = tileIndex + 1;
        }
      });
      
    });

    createSvgElement({name: "line", parent: svg, attributes: [["stroke", "black"], ["stroke-width", "1"], ["x1", "0"], ["x2", "1000"], ["y1", "0"], ["y2", "0"]]});
    createSvgElement({name: "line", parent: svg, attributes: [["stroke", "black"], ["stroke-width", "1"], ["x1", "0"], ["x2", "0"], ["y1", "0"], ["y2", "1000"]]});
    createSvgElement({name: "line", parent: svg, attributes: [["stroke", "black"], ["stroke-width", "1"], ["x1", (conf.label.horizontal.width + conf.label.horizontal.gap + (maxRowLength * (conf.tile.width + conf.tile.horizontalDistance) - conf.tile.horizontalDistance))], ["x2", (conf.label.horizontal.width + conf.label.horizontal.gap + (maxRowLength * (conf.tile.width + conf.tile.horizontalDistance) - conf.tile.horizontalDistance))], ["y1", "0"], ["y2", "1000"]]});
    createSvgElement({name: "line", parent: svg, attributes: [["stroke", "black"], ["stroke-width", "1"], ["x1", "0"], ["x2", "1000"], ["y1", (conf.label.vertical.height + conf.label.vertical.gap + (data.rows.length * (conf.tile.height + conf.tile.verticalDistance) - conf.tile.verticalDistance))], ["y2", (conf.label.vertical.height + conf.label.vertical.gap + (data.rows.length * (conf.tile.height + conf.tile.verticalDistance) - conf.tile.verticalDistance))]]});

    // calculate width and height for svg. a padding is added on right hand side and bottom so that highlights won't be cut off
    var svgHeight =(conf.label.vertical.height + conf.label.vertical.gap + (data.rows.length * (conf.tile.height + conf.tile.verticalDistance) - conf.tile.verticalDistance)) + conf.padding;
    var svgWidth = (conf.label.horizontal.width + conf.label.horizontal.gap + (maxRowLength * (conf.tile.width + conf.tile.horizontalDistance) - conf.tile.horizontalDistance)) + conf.padding;

    // set image dimensions and viewport. 
    svg.setAttribute("viewBox", "0 0 " + svgWidth + " " + svgHeight);
    svg.setAttribute("width", svgWidth);
    svg.setAttribute("height", svgHeight);
    svg.setAttribute("overflow", "hidden");

    document.body.removeChild(renderContainer);
    return svg;
  };

  return createChessboard;
})();
