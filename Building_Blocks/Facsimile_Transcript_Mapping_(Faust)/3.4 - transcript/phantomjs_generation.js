var page = require("webpage").create();
var fs = require("fs");

page.onConsoleMessage = function(msg) {
  console.log(msg);
};

page.onError = function (msg, trace) {
    console.log(msg);
    trace.forEach(function(item) {
            console.log('  ', item.file, ':', item.line);
        });
};

page.viewportSize = { width: 1920, height: 1080 };

var svgString = fs.read("0003.svg");
var json = JSON.parse(fs.read("0003.json"));

function evaluate(page, func) {
    var args = [].slice.call(arguments, 2);
    var fn = "function() { return (" + func.toString() + ").apply(this, " + JSON.stringify(args) + ");}";
    return page.evaluate(fn);
}

page.open("file://" + fs.absolute("phantomjs_generation.html"), function(status) {
  if(status === "success") {

/*    var callback = function(json) {
      return function() {
        createDiplomaticSvg(json, function(diplomaticSvg) {
          console.log(diplomaticSvg);
        });
      };
    };
    page.evaluate(callback(json));
*/

setTimeout(function() {
  evaluate(page, function(json, svgString) {
    createDiplomaticSvg(json, function(diplomaticSvg) {
      var div = document.createElement("div");
      div.appendChild(diplomaticSvg);
//      console.log(div.innerHTML);
      var overlaySvg = createFacsimileOverlaySvg(diplomaticSvg, svgString);
      var oDiv = document.createElement("div");
      oDiv.appendChild(overlaySvg);
      console.log(oDiv.innerHTML);
    });
  }, json, svgString);
//  console.log(a);
}, 2000);

//    setTimeout(function() {
//      page.render('example.jpeg', {format: 'jpeg', quality: '100'});
//      setTimeout(function(){phantom.exit();}, 1000);
//    }, 10000);
//    console.log(1234);
  }
  //phantom.exit();
});
