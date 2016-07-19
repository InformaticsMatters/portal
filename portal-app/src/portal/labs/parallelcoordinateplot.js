


function buildParallelCoordinatePlot(id, data) {

    var svgSelection = d3.select("#" + id + " .svg-container");
    svgSelection.node().data = data;

    displayParallelCoordinatePlot(id)
}

function displayParallelCoordinatePlot(id) {

    var svgSelector = "#" + id + " .svg-container";
    var svgSelection = d3.select(svgSelector);
    svgSelection.selectAll("*").remove();

    var data = svgSelection.node().data;

    var width = svgSelection.style("width").replace("px", "");
    var height = svgSelection.style("height").replace("px", "");

    if (data == null || data.length == 0 || data[0] == null) {
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
        .brushMode("1D-axes")  // enable brushing
        .on("brush", brushed);

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

    function brushed(data) {
        //console.log("Brushed: " + data.length);
        var ids = [];
        for (i = 0; i < data.length; i++) {
            ids.push(data[i].uuid);
        }
        updateSelection(ids);
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

}

function fitParallelCoordinatePlot(id) {

    /* TODO: this resizing is not optimal as it relies on storing the data and regenerating the entire plot on each resize
    * instead should regenerate the plot content without needing to rebind the data
    */

    var idSelection = d3.select('#' + id);
    var plotContent = idSelection.select('.svg-container');  // .boxPlotContent'
    var plotNode = plotContent.node();
    var timerId = plotNode.timerId;
    if (timerId != null) {
        clearTimeout(timerId);
    }

    // apply resize using a timer to ensure it doesn't redraw too often
    plotNode.timerId = setTimeout(function () {
        // redrawing code
        plotNode.timerId = null;
        displayParallelCoordinatePlot(id);
    }, 200);
}
