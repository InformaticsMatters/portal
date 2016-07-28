var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = 560 - margin.left - margin.right,
    height = 550 - margin.top - margin.bottom;

var randomX = d3.random.normal(width / 2, 50),
    randomY = d3.random.normal(height / 2, 50),
    points = d3.range(2000).map(function() { return [randomX(), randomY()]; });

var hexbin = d3.hexbin()
    .size([width, height])
    .radius(10);

var data = hexbin(points);
var max = d3.max(data, function(d) { return d.length });
console.log("Max bin size: " + max);

var color = d3.scale.linear()
    //.domain([0, max/2.0])
    //.range(["white", "steelblue"])
    .domain([0, max])
    .range(["#eee", "#111"])
    .interpolate(d3.interpolateLab);

var x = d3.scale.identity()
    .domain([0, width]);

var y = d3.scale.linear()
    .domain([0, height])
    .range([height, 0]);

var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom")
    .tickSize(6, -height);

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .tickSize(6, -width);

var svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

svg.append("clipPath")
    .attr("id", "clip")
  .append("rect")
    .attr("class", "mesh")
    .attr("width", width)
    .attr("height", height);

svg.append("g")
    .attr("clip-path", "url(#clip)")
  .selectAll(".hexagon")
    .data(data)
  .enter().append("path")
    .attr("class", "hexagon")
    .attr("d", hexbin.hexagon())
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    .style("fill", function(d) { return color(d.length); });

svg.append("g")
    .attr("class", "y axis")
    .call(yAxis);

svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);
