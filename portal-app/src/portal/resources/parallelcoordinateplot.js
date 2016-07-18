function fitParallelCoordinatePlot(id) {

    var divContent = d3.select("#" + id);
    var divWidth = divContent.style("width").replace("px", "");
    var divHeight = divContent.style("height").replace("px", "");

    console.log("New size: " + divWidth + " x " + divHeight);

    var svgContent = d3.select("#" + id + " .svg-container");
    var statusContent = d3.select("#" + id + " .extra.content");

    svgContent.style("width", divWidth + "px");
    svgContent.style("height", (divHeight - 40) + "px");

//    d3.parcoords()("#" + id + " .svg-container")
//        .width((divWidth - 20) + "px")
//        .height((divHeight - 90) + "px")
//        .render()
//        .updateAxes();
}



function buildParallelCoordinatePlot(id, data) {

    var svgSelector = "#" + id + " .svg-container"
    d3.select(svgSelector).selectAll("*").remove();
    fitParallelCoordinatePlot(id);

    if (data[0] == null) {
        //console.log("No data - clearing and returning");
        return;
    }
    var plotContent = d3.select(svgSelector);

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
        .margin({
          top: 20,
          left: 20,
          right: 20,
          bottom: 20
        })
        .color(function(d) { return scale(d.idx); })
        .alpha(0.4)
        .data(data)
        .hideAxis(["uuid"])
        .composite("darker")
        .render()
        .shadows()
        .reorderable()
        .brushMode("1D-axes")  // enable brushing
        .on("resize", function(dim) {
            console.log("resized " + dim.width + " " + dim.height + " " + dim.margin);
        });

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
