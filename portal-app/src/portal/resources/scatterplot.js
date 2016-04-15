function buildScatterPlot(id, data, xLabel, yLabel) {
    $id = $('#' + id);
    $plotContent = $id.find('.scatterPlotContent');

    $plotContent.empty();

    var margin = {top: 16, right: 16, bottom: 32, left: 32};
    var width = 480 - margin.left - margin.right;
    var height = 240 - margin.top - margin.bottom;
    var colors = d3.scale.category10();

    var x = d3.scale.linear()
        .domain([0, d3.max(data, function(d) { return d.x; })])
        .range([0, width]);

    var y = d3.scale.linear()
        .domain([0, d3.max(data, function(d) { return d.y; })])
        .range([height, 0]);

    var chart = d3.select($plotContent[0])
        .append('svg:svg')
        .attr('width', width + margin.right + margin.left)
        .attr('height', height + margin.top + margin.bottom)
        .attr('class', 'chart')

    var main = chart.append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
        .attr('width', width)
        .attr('height', height)
        .attr('class', 'main')

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
          .attr("r", 5)
          .style("fill", function(d) { return colors(d.color); });
}


