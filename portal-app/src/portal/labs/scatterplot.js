/*
Start definition of reusable scatterplot.
based on this topic in the D3 google group:
https://groups.google.com/forum/#!topic/d3-js/ADH20I8DG9I

colorModel values:
'none': no coloring
'categorical': discrete categories (needs integer or text values)
'blue-red', 'steelblue-brown': scales from min value to max values (needs continuous variables - float or integer)
*/
scatterPlot = function(config) {

  // We can simply maintain the values within the
  // function closure. This ensures they will remain private,
  // and the external environment will only be able to change them
  // in the accessors from now on.
  var xLabel = config.xLabel,
      yLabel = config.yLabel,
      colorModel = config.colorModel ? config.colorModel : 'none',
      displayLegend = config.displayLegend,
      outerWidth = config.width,
      outerHeight = config.height,
      mark,
      selectionHandler = config.selectionHandler,
      brushExtent = selectionHandler ? selectionHandler.readExtents() : [[0,0,],[0,0]];

      //console.log("Initialized with extents: " + brushExtent);


  // This is the chart function that will be returned when
  // we first call the containing "plot" object. Note, we
  // are returning a FUNCTION, not an object. Luckily,
  // in javascript, functions ARE objects, so we can bind
  // methods to this function, while still exposing a
  // functional API as well.
  //
  // Again, note, the first time "plot(...options)" is called
  // we return this function "chart".
  function chart(selection) {

    //var start = new Date().getTime();

    selection.each(function(data, i) {

      var xMin = data.length ? d3.min(data, function(d) { return d.xl == null ? d.x : d.xl; }) : 0,
          xMax = data.length ? d3.max(data, function(d) { return d.xu == null ? d.x : d.xu; }) : 0,
          yMin = data.length ? d3.min(data, function(d) { return d.yl == null ? d.y : d.yl; }) : 0,
          yMax = data.length ? d3.max(data, function(d) { return d.yu == null ? d.y : d.yu; }) : 0,
          margin = {top: 15, right: 30, bottom: 25, left: 40},

          sizeScale = d3.scale.sqrt()
                .domain([d3.min(data, function(d) { return d.size; }), d3.max(data, function(d) { return d.size; })])
                .range([3, 20])
                .nice(),
          colorScale = createColorScale(colorModel, data),
          legend = createLegend(colorModel, colorScale),
          selectionHandler = config.selectionHandler;

        // create the svg
        var svg = d3.select(this).selectAll('.chart')
            .data([true]); // This 1-element array acts as a null object or null data.
        // This enables us to use d3's enter, update, and exit methods,
        // since a new enter selection will be created on the first run.

      svg.transition().duration(10)
        .attr('width', outerWidth)
        .attr('height', outerHeight);

      // Enter SVG element
      svg.enter()
      // Only append the first time our chart function runs.
        .append('svg:svg')
        .attr('width', outerWidth)
        .attr('height', outerHeight)
        .attr('class', 'chart');

        svg.selectAll('.legend').remove();
        // first we need the legend as its width needs to be determined.
        if (displayLegend && legend != null && data.length) {
              // for some reason legend doesn't behave well if the data([true]) pattern is used
              // so we delete the current one and create a new one

              var l = svg.append('g')
                  .attr("class", "legend")
                  .attr("visibility", "hidden")
                  .call(legend);

              var legendWidth = Math.ceil(l.node().getBoundingClientRect().width);
              if (!legendWidth) {
                // TODO - resolve this - happens on Firefox
                console.log("Legend width could not be determined, Using default");
                legendWidth = 45;
              }
              //console.log("Legend width: " + legendWidth + " " + l.node().getBoundingClientRect().width);
              margin.right = margin.right + legendWidth;
              l.attr("transform", "translate(" + (outerWidth - legendWidth - 10) + ",10)")
                .attr("visibility", "visible");
        }

        // now we can adjust the margins
        var width = outerWidth - margin.left - margin.right;
        var height = outerHeight - margin.top - margin.bottom;

        //console.log(width + " " + height + " " + xMin + " " + xMax + " " + yMin + " " + yMax);

        var xScale = d3.scale.linear()
                    .domain([xMin, xMax])
                    .range([0, width])
                    .nice();
        var yScale = d3.scale.linear()
                    .domain([yMin, yMax])
                    .range([height, 0])
                    .nice();
      // "d3.svg.brush" here returns a function. Until we
      // call this function and pass in a selection,
      // it will not create anything for us.
      // Even calling accessor/setter methods will
      // not automatically update it. We must call the
      // function this returns ("brush") later to
      // initialise it.
      var brush = d3.svg.brush()
        .x(xScale)
        .y(yScale)
        .extent(brushExtent);

      // create the main scatterplot container group
      var main = svg.selectAll('.main')
        .data([true]);

      main
          .transition().duration(10)
          .attr('width', width)
          .attr('height', height);

      main.enter()
          .append('g')
          .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
          .attr('width', width)
          .attr('height', height)
          .attr('class', 'main');

      // create x and y axes
      var xAxis = d3.svg.axis()
          .scale(xScale)
          .orient('bottom');

      var yAxis = d3.svg.axis()
          .scale(yScale)
          .orient('left');

      // add the axes
      var xaxis = main.selectAll('.xaxis')
          .data([true]);

      // Update X Axis
      xaxis
      .transition().duration(10)
      .attr('transform', 'translate(0,' + height + ')')
      .call(xAxis)
      .selectAll('.label')
      .attr('x', width);

      // Append the new X Axis if Needed
      xaxis.enter()
        .append('g')
        .attr('transform', 'translate(0,' + height + ')')
        .attr('class', 'main axis xaxis')
        .call(xAxis)
        .append("text")
            .attr("class", "label")
            .attr("x", width)
            .attr("y", -6)
            .style("text-anchor", "end")
            .text(xLabel);

      var yaxis = main.selectAll('.yaxis')
          .data([true]);

      // Update Y Axis
      yaxis
        .transition().duration(10)
        .call(yAxis);

      // Append the new Y Axis if Needed
      yaxis.enter()
        .append('g')
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

      var g = main.selectAll(".points")
        .data([true]);

      g.enter()
          .append("g")
          .attr("class", "points");

      var elements = g.selectAll(".element")
        .data(
            data, function(d) { return d.uuid; } // the ID mapper
        );

        elements
            .enter()
            .append("g")
            .attr("class", "element");

         elements
            .exit() // The exit selection lets us remove dead data.
            .style('opacity', 0)
            .remove();

        // X error bars
        elements
            .filter(function(d) {
                return d.xl != null && d.xu != null;
            })
            .append("line")
            .attr("class", "xerror")
            .call(drawXErrorBars);

        // Y error bars
        elements
            .filter(function(d) {
                return d.yl != null && d.yu != null;
            })
            .append("line")
            .attr("class", "yerror")
            .call(drawYErrorBars);

        // the points - draw after the error bars so that they are on top
        elements
            .append("circle")
            .call(drawCircle);

        elements.selectAll("line.xerror")
            .call(drawXErrorBars);

        elements.selectAll("line.yerror")
            .call(drawYErrorBars);

        elements.selectAll("circle")
            .call(drawCircle);

      var brushRect = main.selectAll('.brush')
          .data([true]);

      // Update the existing brush element.
      brushRect
        .transition().duration(10)
        .each("end", brushed)
        .call(brush);

      brushed();

      brushRect
        .enter()
          .append("g")
          .attr("class", "brush")
          .call(brush)
          .call(function() {
              brush.on('brush', brushed);
              brush.on("brushend", brushended);
          });
        // That was the crucial moment for the brush. What is
        // happening here is you are calling the brush function that was
        // returned when we initialised the brush, but by using .call(), you
        // are passing in very specific arguments to brush(). Namely, you are
        // passing in the selection ("g") on which you have used .call().
        // The d3 brush component expects this, and it is by passing this
        // SELECTION that you are actually DRAWING the brush. Anytime the brush is
        // drawn, it will need a selection to act on.
        // The syntax may seem strange, but it is equivalent to calling:
        // brush(d3.selectAll('g.brush'));
        // See Mike's article on the reusable chart pattern to understand why that is.
        // Also, since we are calling the brush event binding only on the first
        // run, we will not have memory leaks from rebinding events.


        // draw the circle and its error bars
        function drawCircle(circle) {
            circle.attr("cx", function (d,i) { return xScale(d.x); } )
                .attr("cy", function (d) { return yScale(d.y); } )
                .attr("r", function (d) { return sizeScale(d.size); } )
                .attr("class", function(d) { return  mark == null ? null : (mark.has(d.uuid) ? "marked" : null); })
                .style("fill", function(d) { return colorScale == null ? "steelblue" : colorScale(d.color); });

            return circle;
        }


        function drawXErrorBars(line) {

            line
                .attr("x1", function(d) { return xScale(d.xl); })
                .attr("y1", function(d) { return yScale(d.y); })
                .attr("x2", function(d) { return xScale(d.xu); })
                .attr("y2", function(d) { return yScale(d.y); });

            return line;
        }



        function drawYErrorBars(line) {

            line
                .attr("x1", function(d) { return xScale(d.x); })
                .attr("y1", function(d) { return yScale(d.yl); })
                .attr("x2", function(d) { return xScale(d.x); })
                .attr("y2", function(d) { return yScale(d.yu); });

            return line;
        }

      // Because "brushed" is inside
      // the chart function closure, we can access them
      // in the chart function. Since we no longer have a separate
      // draw and redraw function, the functions can access elements for resizing,
      // whereas we could not call the brushed function from the
      // former draw function from within the former redraw function.
      function brushed() {
        brushExtent = brush.extent();
        if (brush.empty()) {
            // empty brush so we select all
            g.selectAll("circle").classed("deselected", false);

        } else {
            g.selectAll("circle").classed("deselected", function(d) {
                return brushExtent[0][0] > d.x ||
                    d.x > brushExtent[1][0] ||
                    brushExtent[0][1] > d.y ||
                    d.y > brushExtent[1][1];
            });
        }
      }

      function brushended() {
        if (!d3.event.sourceEvent) return; // only transition after input
        updateSelections();
      }

      function getSelections() {
        var selected;
        var selectedAndMarked;
        if (!brush.empty()) {
            selected = [];
            selectedAndMarked = [];
            g.selectAll("circle:not(.deselected)").each(function(d) {
                selected.push(d.uuid);
                if (mark && mark.has(d.uuid)) {
                    selectedAndMarked.push(d.uuid);
                }
            });
        }
        return [selected, selectedAndMarked];
      }

      function updateSelections() {

            if (selectionHandler) {
                if (brush.empty()) {
                    selectionHandler.writeSelections(null, null, null);
                } else {
                    var selections = getSelections();
                    //console.log("updating selections " + selections[0].length + " " + selections[1].length);
                    selectionHandler.writeSelections(brush.extent(), selections[0], selections[1]);
                }
            }
      }

    });

    function createLegend(colorModel, colorScale) {

        if (colorScale == null) {
            return null;
        }

        switch(colorModel) {

            case 'none':
                return null;

            case 'categorical':
                return d3.legend.color()
                    .labelOffset(4)
                    .shapePadding(1)
                    .scale(colorScale);

            default:
                return d3.legend.color()
                   .cells(10)
                   .labelOffset(4)
                   .shapePadding(0)
                   .scale(colorScale);
        }
    }

      function createColorScale(colorModel, data) {
             switch(colorModel) {

                  case 'categorical':
                      //console.log("Using category20 color scale");
                      return d3.scale.category20();

                  case 'steelblue-brown':
                      var min = d3.min(data, function(d) { return d.color; });
                      var max = d3.max(data, function(d) { return d.color; });
                      //console.log("Using steelblue-brown color scale")
                      return d3.scale.linear()
                          .domain([min, max])
                          .range(["steelblue", "brown"])
                          .nice();

                  case 'blue-red':
                      var min = d3.min(data, function(d) { return d.color; });
                      var max = d3.max(data, function(d) { return d.color; });
                      //console.log("Using default color scale");
                      return d3.scale.linear()
                          .domain([min, max])
                          .range(["blue", "red"])
                          .nice();

                  default:
                    return null;
                }
      }

      //var end = new Date().getTime();
      //console.log("chart took " + (end - start));
  }

  chart.width = function(_) {
    if (!arguments.length) return outerWidth;
    outerWidth = _;
    return chart;
  };

  chart.height = function(_) {
    if (!arguments.length) return outerHeight;
    outerHeight = _;
    return chart;
  };

  chart.data = function(_) {
    if (!arguments.length) return data;
    data = _;
    return chart;
  };

  chart.mark = function(_) {
    if (!arguments.length) return mark;
    mark = _;
    return chart;
  }

   chart.brush = function(_) {
         if (!arguments.length) return brushExtent;
         brushExtent = _;
         return chart;
      }

  return chart;

};
