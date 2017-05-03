function resizeParallelCoordinatePanel(id) {
    var div = d3.select("#" + id);
    var plot = div.select(".svg-container");
    var headers = div.select(".headers");
    var status = div.select(".extra.content.line");
    var wouter = div.style("width").replace("px", "");
    var w = wouter - 2;
    var houter = div.style("height").replace("px", "");
    var headersH = headers.style("height").replace("px", "");
    var statusH = status.style("height").replace("px", "");
    var h = houter - headersH - statusH;
    //console.log("Resizing parallelCoordinatePlot : outer width= " + wouter + " width=" + w + " outer height=" + houter + " inner height=" + h);
    plot.style("width", w + "px");
    plot.style("height", h + "px");
    return {width:w, height:h};
}


function buildParallelCoordinatePlot(id, config, data) {

    resizeParallelCoordinatePanel(id);

    var selector = "#" + id;
    var outerDiv = d3.selectAll(selector);
    var plot = outerDiv.select(".svg-container");
    var form = outerDiv.select('#' + id + ' form.parallelcoordinateplot');

    var selectionHandler = function() { }

    selectionHandler.readAxes = function() {
        var json = form.select('.axes').attr('value');
        //console.log("Axes: " + json);
        if (json == null || json.length == 0) {
            return null;
        } else {
            return JSON.parse(json);
        }
    }

    selectionHandler.updateAxes = function(dimensions, colorDimension) {
        var json = (dimensions == null ? null : JSON.stringify(dimensions));
        form.select('.axes').attr('value', json);
        form.select('.colorDimension').attr('value', colorDimension);
        // submit
        form.select('.updateAxes').node().click();
     }

    selectionHandler.readExtents = function() {
        var json = form.select('.extents').attr('value');
        //console.log("Extents: " + json);
        if (json == null || json.length == 0) {
            return null;
        } else {
            return JSON.parse(json);
        }
    }

    selectionHandler.readColorDimension = function() {
            var value = form.select('.colorDimension').attr('value');
            if (value == null || value.length == 0) {
                return null;
            } else {
                return value;
            }
        }

    selectionHandler.updateSelection = function(extents, selected) {
        var extentsJson = (selected == null ? null : JSON.stringify(extents));
        var selectedJson = (selected == null ? null : JSON.stringify(selected));
        console.log("Selection: " + selectedJson);

        form.select('.extents').attr('value', extentsJson);
        form.select('.selection').attr('value', selectedJson);
        // submit
        form.select('.updateSelection').node().click();
    }

    var chart = createParallelCoordinatePlot(selector + " .svg-container", config, selectionHandler, data);
    plot.node().chart = chart;

    return chart;
}

function fitParallelCoordinatePlot(id) {

    var plot = d3.select("#" + id + " .svg-container");

    var dims = resizeParallelCoordinatePanel(id);

    var chart = plot.node().chart;

    //console.log("Resizing PCP to " + dims.width + " " +  dims.height);
    chart.width(dims.width).height(dims.height).render();

    // this is a hack to add back the event that allows the data to be re-coloured
    // as this get's lost during the resize event
    chart.svg.selectAll(".dimension").on("click", chart.change_color);
}

function createParallelCoordinatePlot(selection, config, selectionHandler, data) {

    function generateColorScale(data, dimension) {
        var min = d3.min(data, function(d) { return d[dimension]; });
        var max = d3.max(data, function(d) { return d[dimension]; });
        //console.log("Dimension: " + dimension + " Range: " + min + " - " + max);
        return d3.scale.linear()
            .domain([min, max])
            .range(["steelblue", "brown"])
            .interpolate(d3.interpolateLab);
    };


    function updateSelection(extents, ids) {
        if (selectionHandler) {
            selectionHandler.updateSelection(extents, ids);
        }
    }

    function saveAxes() {
        var dims = chart.dimensions();
        //console.log("Axes reordered: " + JSON.stringify(dims));
        if (selectionHandler) {
            selectionHandler.updateAxes(dims, chart.state.colorDimension);
        }
    }

    function doChangeColor(dimension) {
        //console.log("Setting color dimension to " + dimension);
        chart.state.colorDimension = dimension;
        chart.svg.selectAll(".dimension")
            .style("font-weight", "normal")
            .filter(function(d) { return d == dimension; })
            .style("font-weight", "bold");

        scale = generateColorScale(chart.data(), dimension);
        chart.color(function(d) { return scale(d[dimension]); })
            .render();
    }

    function brushended(data) {
        var extents = chart.brushExtents();
        if (Object.keys(extents).length === 0) {
            updateSelection(null, null);
        } else {
            var ids = [];
            for (i = 0; i < data.length; i++) {
                ids.push(data[i].uuid);
            }
            updateSelection(extents, ids);
        }
    }

    try {

        var outerDiv = d3.selectAll(selection);

        outerDiv.selectAll("*").remove();

        var brushExtents = (selectionHandler ? selectionHandler.readExtents() : null) // something like: {"mol_weight":[200.0,400.0]};
        var axes = (selectionHandler ? selectionHandler.readAxes() : null);
        var colorDimension = (selectionHandler ? selectionHandler.readColorDimension() : null);
        if (colorDimension == null) colorDimension = 'idx';

        var scale = generateColorScale(data, colorDimension);

        var chart = d3.parcoords(config)(selection)
            //.color(function(d) { return scale(d[colorDimension]); })
            .margin({top: 18, right: 0, bottom: 22, left: 0})
            .alpha(0.4)
            .data(data)
            .composite("darker")
            .dimensions(axes ? axes : {})
            .hideAxis(["uuid"])
            .render()
            .createAxes()
            .shadows()
            .reorderable()
            .brushMode("1D-axes")  // enable brushing
            .brushExtents(brushExtents ? brushExtents : {})
            .on("brushend", brushended);


        // update color
        chart.change_color = function(dimension) {
            doChangeColor(dimension);
                saveAxes();
            }

        // set the initial coloring
        doChangeColor(colorDimension);

        // click label to activate coloring
        chart.svg.selectAll(".dimension")
            .on("click", chart.change_color);

        chart.on("axesreorder.settings", saveAxes);

        // example of how to set the extents
        //chart.brushExtents({"hbd_count":[1.490,2.33],"hba_count":[1.0,3.60]});


    } catch(err) {
        console.log("FAILED TO CREATE PARALLELCOORDINATEPLOT: " + err.message);
        return;
    }


    return chart;
}
