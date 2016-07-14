

function buildParallelCoordinatePlot(id, data) {

    var svgSelector = "#" + id + " .svg-container"
    d3.select(svgSelector).selectAll("*").remove();

    if (data[0] == null) {
        //console.log("No data - clearing and returning");
        return;
    }

    //console.log("Num Data items:" + data.length)

    function generateColorScale(data, dimension) {
        var min = d3.min(data, function(d) { return d[dimension]; });
        var max = d3.max(data, function(d) { return d[dimension]; });
        //console.log("Dimension: " + dimension + " Range: " + min + " - " + max);
        return d3.scale.linear()
            .domain([min, max])
            .range(["steelblue", "brown"])
            .interpolate(d3.interpolateLab);
    };

    var scale = generateColorScale(data, 'idx');

    var parcoords = d3.parcoords()(svgSelector)
        .color(function(d) { return scale(d.idx); })
        .alpha(0.4)
        .data(data)
        .hideAxis(["uuid"])
        .composite("darker")
        .render()
        .shadows()
        .reorderable()
        .brushMode("1D-axes");  // enable brushing

    // click label to activate coloring
    parcoords.svg.selectAll(".dimension")
        .on("click", change_color);

    // update color
    function change_color(dimension) {
        parcoords.svg.selectAll(".dimension")
            .style("font-weight", "normal")
            .filter(function(d) { return d == dimension; })
            .style("font-weight", "bold");

        scale = generateColorScale(parcoords.data(), dimension);
        parcoords.color(function(d) { return scale(d[dimension]); })
            .render();
    }

}
