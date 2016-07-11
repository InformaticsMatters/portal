/* colorModel values:
'none': no coloring
'categorical': discrete categories (needs integer or text values)
'blue-red': scale from blue for min value to red for max value (needs continuous variables - float or integer)

*/

function buildScatterPlot(id, totalWidth, totalHeight, data, xLabel, yLabel, colorModel) {
    // this prevents #13, caused by server side generating
    // data with invalid (null) values
    if (data[0] == null) {
        data = [];
    }

    var id = d3.select('#' + id);
    var plotContent = id.select('.svg-container');

    plotContent.select("svg").remove()

    var margin = {top: 15, right: colorModel == 'none' ? 20 : 60, bottom: 25, left: 40};
    var width = totalWidth - margin.left - margin.right;
    var height = totalHeight - margin.top - margin.bottom;

    var colors = null
    switch(colorModel) {

        case 'categorical':
            colors = d3.scale.category20();
            break;

        case 'blue-red':
            var min = d3.min(data, function(d) { return d.color; });
            var max = d3.max(data, function(d) { return d.color; });
            colors = d3.scale.linear()
                .domain([min, max])
                .range(["blue", "red"])
                .nice();
            break;

        case 'steelblue-brown':
            var min = d3.min(data, function(d) { return d.color; });
            var max = d3.max(data, function(d) { return d.color; });
            colors = d3.scale.linear()
                .domain([min, max])
                .range(["steelblue", "brown"])
                .nice();
            break;
    }

    var x = d3.scale.linear()
        .domain([d3.min(data, function(d) { return d.x; }), d3.max(data, function(d) { return d.x; })])
        .range([0, width])
        .nice();

    var y = d3.scale.linear()
        .domain([d3.min(data, function(d) { return d.y; }), d3.max(data, function(d) { return d.y; })])
        .range([height, 0])
        .nice();

    var size = d3.scale.sqrt()
        .domain([d3.min(data, function(d) { return d.size; }), d3.max(data, function(d) { return d.size; })])
        .range([3, 20])
        .nice();

    var chart = plotContent
        .append('svg:svg')
        .attr('width', width + margin.right + margin.left)
        .attr('height', height + margin.top + margin.bottom)
        .attr('class', 'chart');

    var main = chart.append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
        .attr('width', width)
        .attr('height', height)
        .attr('class', 'main');

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
      .data(data, function(d) { return d.uuid; })
      .enter()
      .append("svg:circle")
          .attr("cx", function (d,i) { return x(d.x); } )
          .attr("cy", function (d) { return y(d.y); } )
          .attr("r", function (d) { return d.size; } )
          .style("fill", function(d) { return colors == null ? null : colors(d.color); });

    switch(colorModel) {

        case 'categorical':

            var legendG = chart.append("g")
                 .attr("class", "legend")
                 .attr("transform", "translate(" + (width + 50) + ",10)");

            var legend = d3.legend.color()
                .labelOffset(4)
                .shapePadding(1)
                .scale(colors);

            legendG.call(legend);
            break;

        case 'steelblue-brown':
        case 'blue-red':

           var legendG = chart.append("g")
             .attr("class", "legend")
             .attr("transform", "translate(" + (width + 60) + ",10)");

           var legend = d3.legend.color()
               .cells(10)
               .labelOffset(4)
               .shapePadding(0)
               .scale(colors);

           legendG.call(legend);
           break;
    }
}


