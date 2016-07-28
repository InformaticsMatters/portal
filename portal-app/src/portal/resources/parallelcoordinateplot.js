function resizeParallelCoordinatePanel(id) {
    var div = d3.select("#" + id);
    var plot = div.select(".svg-container");
    var title = div.select(".titleBar");
    var wouter = div.style("width").replace("px", "");
    var w = wouter - 2;
    var houter = div.style("height").replace("px", "");
    var h = houter - title.style("height").replace("px", "") - 5;
    console.log("Resizing parallelCoordinatePlot : outer width= " + wouter + " width=" + w + " outer height=" + houter + " inner height=" + h);
    plot.style("width", w + "px");
    plot.style("height", h + "px");
    return {width:w, height:h};
}


function buildParallelCoordinatePlot(id, config, data) {

    resizeParallelCoordinatePanel(id);

    var selector = "#" + id + " .svg-container";
    var plot = d3.select(selector);

    var chart = createParallelCoordinatePlot(selector, config, data);
    plot.node().chart = chart;

    return chart;
}

function fitParallelCoordinatePlot(id) {

    var plot = d3.select("#" + id + " .svg-container");

    var dims = resizeParallelCoordinatePanel(id);

    var chart = plot.node().chart;

    console.log("Resizing PCP to " + dims.width + " " +  dims.height);
    chart.width(dims.width).height(dims.height).render();

    // this is a hack to add back the event that allows the data to be re-coloured
    // as this get's lost during the resize event
    chart.svg.selectAll(".dimension").on("click", chart.change_color);
}

function createParallelCoordinatePlot(selection, config, data) {

    d3.selectAll(selection).selectAll("*").remove();

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





//
//function buildParallelCoordinatePlot(id, data) {
//
//    var svgSelection = d3.select("#" + id + " .svg-container");
//    svgSelection.node().data = data;
//
//    displayParallelCoordinatePlot(id)
//}
//
//function displayParallelCoordinatePlot(id) {
//
//    var svgSelector = "#" + id + " .svg-container";
//    var svgSelection = d3.select(svgSelector);
//    svgSelection.selectAll("*").remove();
//
//    var data = svgSelection.node().data;
//
//    if (data == null || data.length == 0 || data[0] == null) {
//        //console.log("No data - clearing and returning");
//        return;
//    }
//
//    //console.log("Num Data items:" + data.length)
//
//    function generateColorScale(data, dimension) {
//        var min = d3.min(data, function(d) { return d[dimension]; });
//        var max = d3.max(data, function(d) { return d[dimension]; });
//        //console.log("Dimension: " + dimension + " Range: " + min + " - " + max);
//        return d3.scale.linear()
//            .domain([min, max])
//            .range(["steelblue", "brown"])
//            .interpolate(d3.interpolateLab);
//    };
//
//    var scale = generateColorScale(data, 'idx');
//
//    var parcoords = d3.parcoords()(svgSelector)
//        .color(function(d) { return scale(d.idx); })
//        .alpha(0.4)
//        .data(data)
//        .hideAxis(["uuid"])
//        .composite("darker")
//        .render()
//        .shadows()
//        .reorderable()
//        .brushMode("1D-axes")  // enable brushing
//        .on("brush", brushed);
//
//    // click label to activate coloring
//    parcoords.svg.selectAll(".dimension")
//        .on("click", change_color);
//
//    // update color
//    function change_color(dimension) {
//        parcoords.svg.selectAll(".dimension")
//            .style("font-weight", "normal")
//            .filter(function(d) { return d == dimension; })
//            .style("font-weight", "bold");
//
//        scale = generateColorScale(parcoords.data(), dimension);
//        parcoords.color(function(d) { return scale(d[dimension]); })
//            .render();
//    }
//
//    function brushed(data) {
//        //console.log("Brushed: " + data.length);
//        var ids = [];
//        for (i = 0; i < data.length; i++) {
//            ids.push(data[i].uuid);
//        }
//        updateSelection(ids);
//    }
//
//    function updateSelection(ids) {
////        if (ids == null) {
////            d3.select("#selection").text('-- all --');
////        } else {
////            if (ids.length == 0) {
////                d3.select("#selection").text("-- none --");
////            } else {
////                d3.select("#selection").text(ids.join(","));
////            }
////        }
//    }
//
//}
//
//function fitParallelCoordinatePlot(id) {
//
//    /* TODO: this resizing is not optimal as it relies on storing the data and regenerating the entire plot on each resize
//    * instead should regenerate the plot content without needing to rebind the data
//    */
//
//    var idSelection = d3.select('#' + id);
//    var plotContent = idSelection.select('.svg-container');  // .boxPlotContent'
//    var plotNode = plotContent.node();
//    var timerId = plotNode.timerId;
//    if (timerId != null) {
//        clearTimeout(timerId);
//    }
//
//    // apply resize using a timer to ensure it doesn't redraw too often
//    plotNode.timerId = setTimeout(function () {
//        // redrawing code
//        plotNode.timerId = null;
//        displayParallelCoordinatePlot(id);
//    }, 200);
//}
