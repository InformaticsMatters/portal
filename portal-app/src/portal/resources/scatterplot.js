/* colorModel values:
null: no coloring
'categorical': discrete categories (needs integer or text values)
'blue-red': scale from blue for min value to red for max value (needs continuous variables - float or integer)

*/

function buildScatterPlot(id, data, xLabel, yLabel, colorModel) {
    // this prevents #13, caused by server side generating
    // data with invalid (null) values
    if (data[0] == null) {
        data = [];
    }

    $id = $('#' + id);
    $plotContent = $id.find('.svg-container');

    $plotContent.empty();

    var margin = {top: 5, right: 25, bottom: 18, left: 25};
    var width = 440 - margin.left - margin.right;
    var height = 220 - margin.top - margin.bottom;

    var colors = null
    switch(colorModel) {

        case 'categorical':
            console.log("Using categorical color model");
            colors = d3.scale.category10();
            break;

        case 'blue-red':
            var min = d3.min(data, function(d) { return d.color; });
            var max = d3.max(data, function(d) { return d.color; });
            console.log("Blue-Red color model: Min: " + min + " Max: " + max)
            colors = d3.scale.linear().domain([min, max]).range(["blue","red"]);
            break;
    }

    var x = d3.scale.linear()
        .domain([d3.min(data, function(d) { return d.x; }), d3.max(data, function(d) { return d.x; })])
        .range([0, width]);

    var y = d3.scale.linear()
        .domain([d3.min(data, function(d) { return d.y; }), d3.max(data, function(d) { return d.y; })])
        .range([height, 0]);

    var chart = d3.select($plotContent[0])
        .append('svg:svg')
        .attr('width', width + margin.right + margin.left)
        .attr('height', height + margin.top + margin.bottom)
        .attr('class', 'chart');

    var main = chart.append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
        .attr('width', width)
        .attr('height', height)
        .attr('class', 'main') ;

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient('bottom');

    main.append('g')
        .attr('transform', 'translate(0,' + height + ')')
        .attr('class', 'main axis date')
        .call(xAxis)
        .append("text")
            .attr("class", "label")
            .attr("x", width)
            .attr("y", -6)
            .style("text-anchor", "end")
            .text(xLabel);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient('left');

    main.append('g')
        .attr('transform', 'translate(0,0)')
        .attr('class', 'main axis date')
        .call(yAxis)
        .append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yLabel);

    var g = main.append("svg:g");

    g.selectAll("scatter-dots")
      .data(data)
      .enter()
      .append("svg:circle")
          .attr("cx", function (d,i) { return x(d.x); } )
          .attr("cy", function (d) { return y(d.y); } )
          .attr("r", function (d) { return d.size; } )
          .style("fill", function(d) { return colors(d.color); });

    // TODO - handle legend for blue-red scale
    if (colorModel == 1) {
        var legend = g.selectAll(".legend")
            .data(colors.domain())
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

        legend.append("rect")
            .attr("x", width - 18)
            .attr("width", 18)
            .attr("height", 18)
            .style("fill", colors);

        legend.append("text")
            .attr("x", width - 24)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function(d) { return d;})
    }
}


