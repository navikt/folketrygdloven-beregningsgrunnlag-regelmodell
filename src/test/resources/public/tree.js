'use strict';

// ************** Generate the tree diagram	 *****************
var margin = {top: 50, right: 120, bottom: 10, left: 150},
   width = 2000 - margin.right - margin.left,
   height = 1400 - margin.top - margin.bottom;

var i = 0;

var tree = d3.layout.tree()
   .size([height, width]);

var diagonal = d3.svg.diagonal()
   .projection(function (d) {
      return [d.y, d.x];
   });

var svg = d3.select("body").append("svg")
   .style("background", "#212121")
   .attr("width", width + margin.right + margin.left)
   .attr("height", height + margin.top + margin.bottom)
   .append("g")
   .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// load the external data
d3.json("/doc", function (error, root) {
   update(root);
});

var tip = d3.tip()
   .attr('class', 'd3-tip')
   .offset([-10, 0])
   .html(function(d) {
      return "<div> ID: <span style='color:lightblue'>"+ d.ruleId+"</span></div> " +
         "<div> Beskrivelse: <span style='color:lightblue'>"+ d.ruleDescription+"</span></span> " +
         "<div> Operator: <span style='color:lightblue'>"+ d.operator+"</span></div>";
   })

svg.call(tip);

function update(source) {

   // Compute the new tree layout.
   var nodes = tree.nodes(source).reverse(),
      links = tree.links(nodes);

   // Normalize for fixed-depth.
   nodes.forEach(function (d) {
      d.y = d.depth * 150;
   });

   // Declare the nodes…
   var node = svg.selectAll("g.node")
      .data(nodes, function (d) {
         return d.id || (d.id = ++i);
      });

   // Enter the nodes.
   var nodeEnter = node.enter().append("g")
      .attr("class", "node")
      .attr("transform", function (d) {
         return "translate(" + d.y + "," + d.x + ")";
      });


   function getType(d) {
      switch (d.operator) {
         case "OG" :
            return "square";
         case "ELLER" :
            return "cross";
         case "IKKE" :
            return "diamond";
         case "COMPUTATIONAL_IF" :
            return "diamond";
          case "SEQUENCE" :
            return "cross";
         default:
            return "circle";
      }
   }

   nodeEnter.append("path")
      .attr("d", d3.svg.symbol()
         .size( function(d) { return 30*30 })
         .type( function(d) { return getType(d) }))
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide)
      .attr("class", function(d){
         switch (d.operator){
            case "JA": return "nodeYes"
            case "NEI" : return "nodeNo"
            case "KANSKJE": return "nodeMaybe"
            case "COMPUTATIONAL_IF": return "nodeMaybe"
            case "SEQUENCE": return "nodeNo"
            default: return "nodeYes"
         }
      })

   nodeEnter.append("text")
      .attr("y", function (d) {
         return -10;
      })
     .attr("x", function (d) {
       return d.children ? -20 : 20;
     })
      .attr("dy", ".35em")
      .attr("text-anchor", function (d) {
         return d.children  ? "end" : "start";
      })
      .text(function (d) {
         if (d.operator === "COMPUTATIONAL_IF") {
           return d.ruleDescription;
         }
        if (d.operator === "SEQUENCE") {
          return d.ruleId;
        }
         return d.children ? "" : d.ruleId;
      })
      .style("fill", "#BEBEBE");

   nodeEnter.append("text")
      .attr("dy", ".35em")
      .attr("text-anchor", "middle")
      .text(function (d) {
         switch (d.operator){
            case "COMPUTATIONAL_IF" : return "?"
           case "SINGLE": return "✔️"
            default: return "️"
         }
      })
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide)
      .style("fill", "#212121")
      .style("font-weight","bold")

   // Declare the links…
   var link = svg.selectAll("path.link")
      .data(links, function (d) {
         return d.target.id;
      });

   // Enter the links.
   link.enter().insert("path", "g")
      .attr("class", function(d){
         switch (d.target.operator){
            case "COMPUTATIONAL_IF" : return "linkIf"
           default: return "link"
         }
      })
      .attr("d", diagonal);


}
