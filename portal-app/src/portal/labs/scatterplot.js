/* colorModel values:
'none': no coloring
'categorical': discrete categories (needs integer or text values)
'blue-red', 'steelblue-brown': scales from min value to max values (needs continuous variables - float or integer)

*/

function buildScatterPlot(id, xLabel, yLabel, colorModel, data) {

    var svgSelector = "#" + id + " .svg-container"
    var plotContent = d3.select(svgSelector);
    plotContent.selectAll("*").remove();

    if (data == null || data.length == 0 || data[0] == null) {
        //console.log("No data - clearing and returning");
        return;
    }

    var svgWidth = plotContent.style("width").replace("px", "");
    var svgHeight = plotContent.style("height").replace("px", "");
    //console.log("Canvas width,height: " + svgWidth + "," + svgHeight);

    var margin = {top: 15, right: colorModel == 'none' ? 20 : 80, bottom: 25, left: 40};
    var width = svgWidth - margin.left - margin.right;
    var height = svgHeight - margin.top - margin.bottom;

    var colors = null
    switch(colorModel) {

        case 'categorical':
            colors = d3.scale.category20();
            break;

        case 'steelblue-brown':
            var min = d3.min(data, function(d) { return d.color; });
            var max = d3.max(data, function(d) { return d.color; });
            colors = d3.scale.linear()
                .domain([min, max])
                .range(["steelblue", "brown"])
                .nice();
            break;

        case 'blue-red':
            var min = d3.min(data, function(d) { return d.color; });
            var max = d3.max(data, function(d) { return d.color; });
            colors = d3.scale.linear()
                .domain([min, max])
                .range(["blue", "red"])
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

    g.selectAll("circle")
      .data(data, function(d) { return d.uuid; })
      .enter()
      .append("circle")
          .attr("cx", function (d,i) { return x(d.x); } )
          .attr("cy", function (d) { return y(d.y); } )
          .attr("r", function (d) { return size(d.size); } )
          .style("fill", function(d) { return colors == null ? null : colors(d.color); })
          .style("hidden", "false");


    var brush = d3.svg.brush()
        .x(x)
        .y(y)
        .on("brush", brushed)
        .on("brushend", brushended);

    g.append("g")
        .attr("class", "brush")
        .call(brush);

    updateSelection()

    function brushed() {
        var e = brush.extent();
        g.selectAll("circle").classed("hidden", function(d) {
            return e[0][0] > d.x || d.x > e[1][0]
                  || e[0][1] > d.y || d.y > e[1][1];
        });
    }

    function brushended() {
        if (!d3.event.sourceEvent) return; // only transition after input
        if (brush.empty()) {
            g.selectAll(".hidden").classed("hidden", false);
            updateSelection();
        } else {

            var ids = [];
            g.selectAll("circle:not(.hidden)").each(function(d) {
                ids.push(d.uuid);
            })
            //console.log(ids.length + " items selected: " + ids);
            updateSelection(ids);
        }
    }

    function updateSelection(ids) {
        if (ids == null) {
            d3.select("#selection").text('-- all --');
        } else {
            if (ids.length == 0) {
                d3.select("#selection").text("-- none --");
            } else {
                d3.select("#selection").text(ids.join(","));
            }
        }
    }

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

        default:

           var legendG = chart.append("g")
             .attr("class", "legend")
             .attr("transform", "translate(" + (width + 50) + ",10)");

           var legend = d3.legend.color()
               .cells(10)
               .labelOffset(4)
               .shapePadding(0)
               .scale(colors);

           legendG.call(legend);
           break;
    }
}


