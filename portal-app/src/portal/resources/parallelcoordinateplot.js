

function buildParallelCoordinatePlot(id, data) {

        var svgSelector = "#" + id + " .svg-container";
        d3.select(svgSelector).selectAll("*").remove();

        if (data.length == 0 || data[0] == null) {
            return;
        }

        //console.log("id: " +id);
        //console.log("Num Data items:" + data.length);

        var generateColorScale = function(dimension) {
            return d3.scale.linear()
                .domain([d3.min(data, function(d) { return d[dimension]; }), d3.max(data, function(d) { return d[dimension]; })])
                .range(["steelblue", "brown"])
                .interpolate(d3.interpolateLab);
        };

        // quantitative color scale
        var scale = generateColorScale('idx');

        var color = function(d) { return scale(d.idx); };

        var parcoords = d3.parcoords()(svgSelector)
            .color(color)
            .alpha(0.4)
            .data(data)
            .hideAxis(["uuid"])
            .composite("darker")
            .render()
            .shadows()
            .reorderable()
            .brushMode("1D-axes");  // enable brushing

//            // click label to activate coloring
//            parcoords.svg.selectAll(".dimension")
//                .on("click", change_color)
//                .selectAll(".label")
//                .style("font-size", "12px");
//
//            // update color
//            function change_color(dimension) {
//              console.log("Clicked " + dimension);
//              parcoords.svg.selectAll(".dimension")
//                .style("font-weight", "normal")
//                .filter(function(d) { return d == dimension; })
//                .style("font-weight", "bold");
//
//              colorByAxis = dimension;
//              console.log("Coloring by " + dimension);
//
//              parcoords.color(generateColorScale(dimension)).render();
//            }

}
