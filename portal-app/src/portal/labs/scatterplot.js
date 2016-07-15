/* colorModel values:
'none': no coloring
'categorical': discrete categories (needs integer or text values)
'blue-red', 'steelblue-brown': scales from min value to max values (needs continuous variables - float or integer)

*/

/** Class defintion that stores the key configuration properties and persists for the
lifetime of the page instance.
*/
var ScatterPlotConfig = function (xLabel, yLabel, colorModel, data) {

    this.xLabel = xLabel;
    this.yLabel = yLabel;
    this.colorModel = colorModel;
    this.xMin = d3.min(data, function(d) { return d.x; });
    this.xMax = d3.max(data, function(d) { return d.x; });
    this.yMin = d3.min(data, function(d) { return d.y; });
    this.yMax = d3.max(data, function(d) { return d.y; });
    this.margin = {top: 15, right: colorModel == 'none' ? 20 : 80, bottom: 25, left: 40};


    this.sizeScale = d3.scale.sqrt()
            .domain([d3.min(data, function(d) { return d.size; }), d3.max(data, function(d) { return d.size; })])
            .range([3, 20])
            .nice();

    this.brush = d3.svg.brush();

    switch(colorModel) {

            case 'categorical':
                this.colorsScale = d3.scale.category20();
                break;

            case 'steelblue-brown':
                var min = d3.min(data, function(d) { return d.color; });
                var max = d3.max(data, function(d) { return d.color; });
                this.colorsScale = d3.scale.linear()
                    .domain([min, max])
                    .range(["steelblue", "brown"])
                    .nice();
                break;

            case 'blue-red':
                var min = d3.min(data, function(d) { return d.color; });
                var max = d3.max(data, function(d) { return d.color; });
                this.colorsScale = d3.scale.linear()
                    .domain([min, max])
                    .range(["blue", "red"])
                    .nice();
                break;
        }
};

/**
* Method to rescale the plot configuration
*/
ScatterPlotConfig.prototype.setSize = function(outerWidth, outerHeight) {
    this.outerWidth = outerWidth;
    this.outerHeight = outerHeight;
    this.svgWidth = outerWidth - this.margin.left - this.margin.right;
    this.svgHeight = outerHeight - this.margin.top - this.margin.bottom;
    //console.log("Resizing to:    " + outerWidth + " " + outerHeight);
    //console.log("New dimensions: " + this.svgWidth + " " + this.svgHeight);

    this.xScale = d3.scale.linear()
        .domain([this.xMin, this.xMax])
        .range([0, this.svgWidth])
        .nice();

    this.yScale = d3.scale.linear()
        .domain([this.yMin, this.yMax])
        .range([this.svgHeight, 0])
        .nice();

    this.brush
        .x(this.xScale)
        .y(this.yScale);

};

/** Build the scatter plot with the specified data
*/
function buildScatterPlot(id, xLabel, yLabel, colorModel, data) {

    var svgSelector = "#" + id + " .svg-container"
    var plotContent = d3.select(svgSelector);
    plotContent.selectAll("*").remove();

    if (data == null || data.length == 0 || data[0] == null) {
        //console.log("No data - clearing and returning");
        return;
    }


    var divWidth = plotContent.style("width").replace("px", "");
    var divHeight = plotContent.style("height").replace("px", "");
    //console.log("Canvas width,height: " + svgWidth + "," + svgHeight);

    var config = new ScatterPlotConfig(xLabel, yLabel, colorModel, data)
    config.setSize(divWidth, divHeight);

    plotContent.node().plotConfig = config;

    // create the svg
    var chart = plotContent
        .append('svg:svg')
        .attr('width', config.outerWidth)
        .attr('height', config.outerHeight)
        .attr('class', 'chart');

    // create the main plot are where the points are plotted
    var main = chart.append('g')
        .attr("id", id + "_main")
        .attr('transform', 'translate(' + config.margin.left + ',' + config.margin.top + ')')
        .attr('width', config.svgWidth)
        .attr('height', config.svgHeight)
        .attr('class', 'main');

    // create x and y aves
    var xAxis = d3.svg.axis()
        .scale(config.xScale)
        .orient('bottom');

    var yAxis = d3.svg.axis()
        .scale(config.yScale)
        .orient('left');

    // add the axes
    main.append('g')
        .attr('transform', 'translate(0,' + config.svgHeight + ')')
        .attr('class', 'main axis xaxis')
        .call(xAxis)
        .append("text")
            .attr("class", "label")
            .attr("x", config.svgWidth)
            .attr("y", -6)
            .style("text-anchor", "end")
            .text(xLabel);

    main.append('g')
        .attr('transform', 'translate(0,0)')
        .attr('class', 'main axis yaxis')
        .call(yAxis)
        .append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yLabel);

    var g = main.append("g")
        .attr("id", id + "_points");

    g.selectAll("circle")
      .data(data, function(d) { return d.uuid; })
      .enter()
      .append("circle")
          .attr("cx", function (d,i) { return config.xScale(d.x); } )
          .attr("cy", function (d) { return config.yScale(d.y); } )
          .attr("r", function (d) { return config.sizeScale(d.size); } )
          .style("fill", function(d) { return config.colorsScale == null ? "steelblue" : config.colorsScale(d.color); })
          .style("hidden", "false");

    config.brush
        .on("brush", brushed)
        .on("brushend", brushended);

    main.append("g")
        .attr("class", "brush")
        .call(config.brush);

    updateSelection();

    function brushed() {
        var e = config.brush.extent();
        g.selectAll("circle").classed("hidden", function(d) {
            return e[0][0] > d.x || d.x > e[1][0]
                  || e[0][1] > d.y || d.y > e[1][1];
        });
    }

    function brushended() {
        if (!d3.event.sourceEvent) return; // only transition after input
        if (config.brush.empty()) {
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
             .attr("transform", "translate(" + (config.svgWidth + 50) + ",10)");

           var legend = d3.legend.color()
               .cells(10)
               .labelOffset(4)
               .shapePadding(0)
               .scale(config.colorsScale);

           legendG.call(legend);
           break;
    }
}

/** Regenerate the scatter plot when its containing div has been resized
*/
function regenerateScatterPlot(id) {
     var svgSelector = "#" + id + " .svg-container"
     var plotContent = d3.select(svgSelector);
     var config = plotContent.node().plotConfig;
     if (config == null) {
        console.log("PlotConfig not found. Can't regenerate");
        return;
     }
     var divWidth = plotContent.style("width").replace("px", "");
     var divHeight = plotContent.style("height").replace("px", "");
     config.setSize(divWidth, divHeight);

    // resize plot area
    d3.select("#" + id + " .chart")
        .transition()
        .style("width", divWidth)
        .style("height", divHeight);

    // reposition x axis
    var xAxis = d3.svg.axis()
        .scale(config.xScale)
        .orient('bottom');
    d3.select("#" + id + " .xaxis")
        .call(xAxis)
        .transition()
        .attr('transform', 'translate(0,' + config.svgHeight + ')')

    // reposition y axis
    var yAxis = d3.svg.axis()
        .scale(config.yScale)
        .orient('left');
    d3.select("#" + id + " .yaxis")
        .transition()
        .call(yAxis);

    // reposition legend
    switch(config.colorModel) {
        case 'categorical':

            d3.select("#" + id + " .legend")
                .transition()
                .attr("transform", "translate(" + (config.svgWidth + 50) + ",10)");
            break;

        default:

            d3.select("#" + id + " .legend")
                .transition()
                .attr("transform", "translate(" + (config.svgWidth + 50) + ",10)");
            break;
    }

    // reposition the points
    var points = d3.select("#" + id + "_points");
    points.selectAll("circle")
           .attr("cx", function (d,i) { return config.xScale(d.x); } )
           .attr("cy", function (d) { return config.yScale(d.y); } )

    // reposition the brush
    // TODO - can't figure out how to do this yet

}


