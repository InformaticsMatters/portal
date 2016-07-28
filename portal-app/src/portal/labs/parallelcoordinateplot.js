function createParallelCoordinatePlot(selection, config, data) {

    var brushExtents = config.brushExtents // something like: {"mol_weight":[200.0,400.0]};
        selectionHandler = config.selectionHandler;

    function generateColorScale(data, dimension) {
        var min = d3.min(data, function(d) { return d[dimension]; });
        var max = d3.max(data, function(d) { return d[dimension]; });
        //console.log("Dimension: " + dimension + " Range: " + min + " - " + max);
        return d3.scale.linear()
            .domain([min, max])
            .range(["steelblue", "brown"])
            .interpolate(d3.interpolateLab);
    };


    function updateSelection(ids) {
        if (selectionHandler) {
            selectionHandler(ids);
        }
    }

    var scale = generateColorScale(data, 'idx');

    var chart = d3.parcoords()(selection)
        .color(function(d) { return scale(d.idx); })
        .margin({top: 18, right: 0, bottom: 22, left: 0})
        .alpha(0.4)
        .data(data)
        .hideAxis(["uuid"])
        .composite("darker")
        .render()
        .createAxes()
        .shadows()
        .reorderable()
        .brushMode("1D-axes")  // enable brushing
        .on("brushend", brushended);

    if (brushExtents) chart.brushExtents(brushExtents);

    chart.state.colorDimension = 'idx';


    // update color
    chart.change_color = function(dimension) {
        //console.log("Setting color dimesnion to " + dimension);
        chart.state.colorDimension = dimension;
        chart.svg.selectAll(".dimension")
            .style("font-weight", "normal")
            .filter(function(d) { return d == dimension; })
            .style("font-weight", "bold");

        scale = generateColorScale(chart.data(), dimension);
        chart.color(function(d) { return scale(d[dimension]); })
            .render();
    }

    // click label to activate coloring
    chart.svg.selectAll(".dimension")
        .on("click", chart.change_color);


    function brushended(data) {
        //console.log("Brushed: " + data.length);
        var ids = [];
        for (i = 0; i < data.length; i++) {
            ids.push(data[i].uuid);
        }
        updateSelection(ids);
        var extents = chart.brushExtents();
        //console.log("Brush extents: " + JSON.stringify(extents));

    }
    // example of how to set the extents
    //chart.brushExtents({"hbd_count":[1.490,2.33],"hba_count":[1.0,3.60]});

    chart.persistentState = function(_) {
        // TODO - also need to handle the column order and the colour column
        if (!arguments.length) {
            var state = {};
            var extents = chart.brushExtents();
            if (extents.length > 0) {
                state.brushExtents = extents;
            }
            return state;
        }
        // TODO - implement setting? Or does this only happen via config when creating?
        return chart;
    }

    return chart;
}

