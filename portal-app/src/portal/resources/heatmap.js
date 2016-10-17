function fitHeatmap(id) {

      resizeHeatmap(d3.select("#" + id));

}

function buildHeatmap(id, config, data) {

    var colorScale = config.colorScale ? config.colorScale :
            d3.scale.linear()
                .domain(d3.extent(data, function(d) { return d.value}))
                .range(['white', 'brown']),
        margin = config.margin ? config.margin : { top: 75, right: 20, bottom: 20, left: 75 },
        cellSize = config.cellSize ? config.cellSize : 12, // size of cells
        rows = config.rowNames.map(function(n) { return {name:n, sum:0, count:0}; }), // details of the rows
        cols = config.colNames.map(function(n) { return {name:n, sum:0, count:0}; }), // ... and columns
        width = cellSize * cols.length,
        height = cellSize * rows.length,
        transition = config.transition != null ? config.transition : 1000 // speed of transition when sorting
        rowIndexes = d3.range(rows.length), // row indexes allow the correct row to be located after sorting
        colIndexes = d3.range(cols.length); // ... and for columns


    // gather sums and counts
    data.forEach(function(d) {
        rows[d.row].count++;
        rows[d.row].sum += d.value;
        cols[d.col].count++;
        cols[d.col].sum += d.value;
    });

    // sort function
    function averageSorter(a, b) {
            if (a.val !== null && b.val !== null) return a.val - b.val;
            if (a.val === null && b.val === null) return 0;
            if (a.val === null) return -1;
            if (b.val === null) return 1;
    }

    // function to generate sort by average value
    function sortByAverages(elements) {
        elements.forEach(function(element, i) {
            var c = element.count;
            if (c > 0) {
                element.avg = element.sum / c;
                //console.log("Sorted: " + i + " " + element.name + " " + element.avg);
            }
        });
        var sortedElements = elements.map(function(d,i) { return {idx:i, val:d.avg}}).sort(averageSorter);
        sortedElements.forEach(function(e,i) { elements[e.idx].sortedByAvg = i});
    }

    function sortByNames(elements) {
        var sortedElements = elements.map(function(d,i) { return {idx:i, name:d.name}})
            .sort(function(a,b) {
                if (a.name == b.name) {
                    var r = 0
                } else if (a.name > b.name) {
                    var r = 1;
                } else {
                    var r = -1;
                }
                return r;
            });
        sortedElements.forEach(function(e,i) {
            elements[e.idx].sortedByName = i;
        });
    }

    // generate the sort indices
    sortByAverages(rows);
    sortByAverages(cols);
    sortByNames(rows);
    sortByNames(cols);

  var selection = d3.select("#" + id);

  resizeHeatmap(selection);

  var heatmap = selection.select(".svg-container");
  heatmap.selectAll("*").remove();
  var svg = heatmap.append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  var rowSortOrder=false;
  var colSortOrder=false;
  var rowLabels = svg.append("g")
      .selectAll(".rowLabelg")
      .data(rows.map(function(d) { return d.name }))
      .enter()
      .append("text")
      .text(function (d) { return d; })
      .attr("x", 0)
      .attr("y", function (d, i) { return i * cellSize; })
      .style("text-anchor", "end")
      .attr("transform", "translate(-6," + cellSize / 1.5 + ")")
      .attr("class", function (d,i) { return "rowLabel mono r"+i;} )
      .on("mouseover", function(d) {d3.select(this).classed("text-hover",true);})
      .on("mouseout" , function(d) {d3.select(this).classed("text-hover",false);})
      .on("click", function(d,i) {rowSortOrder=!rowSortOrder; sortbylabel("r",i,rowSortOrder);});

  var colLabels = svg.append("g")
      .selectAll(".colLabelg")
      .data(cols.map(function(d) { return d.name }))
      .enter()
      .append("text")
      .text(function (d) { return d; })
      .attr("x", 0)
      .attr("y", function (d, i) { return i * cellSize; })
      .style("text-anchor", "left")
      .attr("transform", "translate("+cellSize/2 + ",-6) rotate (-90)")
      .attr("class",  function (d,i) { return "colLabel mono c"+i;} )
      .on("mouseover", function(d) {d3.select(this).classed("text-hover",true);})
      .on("mouseout" , function(d) {d3.select(this).classed("text-hover",false);})
      .on("click", function(d,i) {colSortOrder=!colSortOrder;  sortbylabel("c",i,colSortOrder);});

  var heatMap = svg.append("g").attr("class","g3")
        .selectAll(".cellg")
        .data(data,function(d){return d.row+":"+d.col;})
        .enter()
        .append("rect")
        .attr("x", function(d) { return d.col * cellSize; })
        .attr("y", function(d) { return d.row * cellSize; })
        .attr("class", function(d){return "cell cell-border cr"+(d.row)+" cc"+(d.col);})
        .attr("width", cellSize)
        .attr("height", cellSize)
        .style("fill", function(d) { return colorScale(d.value); })
        .on("mouseover", function(d){
               //highlight text
               d3.select(this).classed("cell-hover",true);
               d3.selectAll(".rowLabel").classed("text-highlight",function(r,ri){ return ri==(d.row);});
               d3.selectAll(".colLabel").classed("text-highlight",function(c,ci){ return ci==(d.col);});


               //Update the tooltip position and value
               var m = d3.mouse(heatmap.node()); // mouse position relative to the heatmap svg

               selection.select(".tooltip")
                 .style("left", (m[0]+20) + "px")
                 .style("top", (m[1]+40) + "px")
                 .select(".value")
                 .html("<b>labels</b>: " + rows[d.row].name + ", " + cols[d.col].name +
                    "<br><b>value</b>: "+d.value+
                    "<br><b>row,col</b>: "+d.col+", "+d.row+
                    "<br><b>cell x,y</b>: "+this.x.baseVal.value+", "+this.y.baseVal.value)
                 ;

               selection.select(".tooltip").classed("hidden", false)
        })
        .on("mouseout", function(){
               d3.select(this).classed("cell-hover",false);
               selection.selectAll(".rowLabel").classed("text-highlight",false);
               selection.selectAll(".colLabel").classed("text-highlight",false);
               selection.select(".tooltip").classed("hidden", true);
        });

// Change ordering of cells

    selection.select(".row_sorter").on("change",function(){
        var option = this.value;
        console.log('row sort: ' + option);
        if (option == 'row_default') {
            orderByRowDefault();
        } else if (option == 'row_name_asc') {
            orderByRowName(true);
        } else if (option == 'row_name_desc') {
            orderByRowName(false);
        } else if (option == 'row_avg_asc') {
            orderByRowAvg(true);
        } else if (option == 'row_avg_desc') {
            orderByRowAvg(false);
        }
    });
    selection.select(".col_sorter").on("change",function(){
        var option = this.value;
        console.log('col sort: ' + this.value);
        if (option == 'col_default') {
            orderByColDefault();
        } else if (option == 'col_name_asc') {
            orderByColName(true);
        } else if (option == 'col_name_desc') {
            orderByColName(false);
        } else if (option == 'col_avg_asc') {
            orderByColAvg(true);
        } else if (option == 'col_avg_desc') {
            orderByColAvg(false);
        }
    });

    function orderByRowDefault() {
        var t = svg.transition().duration(transition);
        t.selectAll(".cell")
            .attr("y", function(d) { return (d.row) * cellSize; });
        t.selectAll(".rowLabel")
            .attr("y", function (d, i) { return i * cellSize; });
        rowIndexes = d3.range(rows.length);
    }

    function orderByColDefault() {
        var t = svg.transition().duration(transition);
        t.selectAll(".cell")
            .attr("x", function(d) { return (d.col) * cellSize; });
        t.selectAll(".colLabel")
            .attr("y", function (d, i) { return i * cellSize; });
        rowIndexes = d3.range(cols.length);
    }

    function orderByRowName(desc) {
        var t = svg.transition().duration(transition);
        t.selectAll(".cell")
            .attr("y", function(d) {
                var idx = rows[d.row].sortedByName;
                return (desc ? idx : rows.length - idx) * cellSize;
            });
        t.selectAll(".rowLabel")
            .attr("y", function (d, i) {
                var idx = rows[i].sortedByName;
                return (desc ? idx : rows.length - idx) * cellSize;
            });

        var indexes = Array(rows.length);
        rows.forEach(function(d,i) {
            indexes[desc ? d.sortedByName : rows.length - d.sortedByName - 1] =  i;
        });
        rowIndexes = indexes;
    }

    function orderByColName(desc) {
        var t = svg.transition().duration(transition);
        t.selectAll(".cell")
            .attr("x", function(d) {
                var idx = cols[d.col].sortedByName;
                return (desc ? idx : cols.length - idx) * cellSize;
            });
        t.selectAll(".colLabel")
            .attr("y", function (d, i) {
                var idx = cols[i].sortedByName;
                return (desc ? idx : cols.length - idx) * cellSize;
            });

        var indexes = Array(cols.length);
        cols.forEach(function(d,i) {
            indexes[desc ? d.sortedByName : cols.length - d.sortedByName - 1] =  i;
        });
        colIndexes = indexes;
    }

    function orderByRowAvg(desc) {
        var t = svg.transition().duration(transition);

        t.selectAll(".cell")
            .attr("y", function(d) {
                var idx = rows[d.row].sortedByAvg;
                return (desc ? idx : rows.length - idx) * cellSize;
            });

        t.selectAll(".rowLabel")
            .attr("y", function (d, i) {
                var idx = rows[i].sortedByAvg;
                return (desc ? idx : rows.length - idx) * cellSize;
            });

        var indexes = Array(rows.length);
        rows.forEach(function(d,i) {
            indexes[desc ? d.sortedByAvg : rows.length - d.sortedByAvg - 1] =  i;
        });
        rowIndexes = indexes;
    }

    function orderByColAvg(desc) {
        var t = svg.transition().duration(transition);
        t.selectAll(".cell")
            .attr("x", function(d) {
                var idx = cols[d.col].sortedByAvg;
                return (desc ? idx : cols.length - idx) * cellSize;
            });

        t.selectAll(".colLabel")
            .attr("y", function (d, i) {
                var idx = cols[i].sortedByAvg;
                return (desc ? idx : cols.length - idx) * cellSize;
            });

        var indexes = Array(cols.length);
        cols.forEach(function(c,i) {
            indexes[desc ? c.sortedByAvg : cols.length - c.sortedByAvg - 1] =  i;
        });
        colIndexes = indexes;
    }

    function sortbylabel(rORc,i,sortOrder){
            var t = svg.transition().duration(transition);
            var values=[];
            var sorted; // sorted is zero-based index
            d3.selectAll(".c"+rORc+i)
                .filter(function(ce) { values.push(ce.value); });
            if (rORc=="r") { // sort by row
                sorted=d3.range(cols.length).sort(function(a,b){ if (sortOrder) { return values[b]-values[a];} else { return values[a]-values[b];}});
                t.selectAll(".cell")
                    .attr("x", function(d) { return sorted.indexOf(d.col) * cellSize; });
                t.selectAll(".colLabel")
                    .attr("y", function (d, i) { return sorted.indexOf(i) * cellSize; });
                rowIndexes = sorted;
                //console.log("Rows: " + JSON.stringify(sorted));
           } else { // sort by column
                sorted=d3.range(rows.length).sort(function(a,b){if (sortOrder) { return values[b]-values[a];} else { return values[a]-values[b];}});
                t.selectAll(".cell")
                    .attr("y", function(d) { return sorted.indexOf(d.row) * cellSize; });
                t.selectAll(".rowLabel")
                    .attr("y", function (d, i) { return sorted.indexOf(i) * cellSize; });
                colIndexes = sorted;
                //console.log("Cols: " + JSON.stringify(sorted));
           }
    }


  var sa=d3.select(".g3")
      .on("mousedown", function() {
          if( !d3.event.altKey) {
             d3.selectAll(".cell-selected").classed("cell-selected",false);
             d3.selectAll(".rowLabel").classed("text-selected",false);
             d3.selectAll(".colLabel").classed("text-selected",false);
          }
         var p = d3.mouse(this);
         sa.append("rect")
         .attr({
             rx      : 0,
             ry      : 0,
             class   : "selection",
             x       : p[0],
             y       : p[1],
             width   : 1,
             height  : 1
         })
      })
      .on("mousemove", function() {
         var s = sa.select("rect.selection");

         if(!s.empty()) {
             var p = d3.mouse(this),
                 d = {
                     x       : parseInt(s.attr("x"), 10),
                     y       : parseInt(s.attr("y"), 10),
                     width   : parseInt(s.attr("width"), 10),
                     height  : parseInt(s.attr("height"), 10)
                 },
                 move = {
                     x : p[0] - d.x,
                     y : p[1] - d.y
                 };

             if (move.x < 1 || (move.x*2<d.width)) {
                 d.x = p[0];
                 d.width -= move.x;
             } else {
                 d.width = move.x;
             }

             if (move.y < 1 || (move.y*2<d.height)) {
                 d.y = p[1];
                 d.height -= move.y;
             } else {
                 d.height = move.y;
             }
             s.attr(d);

             // deselect all temporary selected state objects
             d3.selectAll('.cell-selection.cell-selected').classed("cell-selected", false);
             d3.selectAll(".text-selection.text-selected").classed("text-selected",false);

             d3.selectAll('.cell').filter(function(cell_d, i) {
                 if(
                     !d3.select(this).classed("cell-selected") &&
                         // inner circle inside selection frame
                     (this.x.baseVal.value)+cellSize >= d.x && (this.x.baseVal.value)<=d.x+d.width &&
                     (this.y.baseVal.value)+cellSize >= d.y && (this.y.baseVal.value)<=d.y+d.height
                 ) {

                     d3.select(this)
                     .classed("cell-selection", true)
                     .classed("cell-selected", true);

                     d3.select(".r"+(cell_d.row))
                     .classed("text-selection",true)
                     .classed("text-selected",true);

                     d3.select(".c"+(cell_d.col))
                     .classed("text-selection",true)
                     .classed("text-selected",true);
                 }
             });
         }
      })
      .on("mouseup", function() {
         // remove selection frame
         sa.selectAll("rect.selection").remove();

         // remove temporary selection marker class
         d3.selectAll('.cell-selection').classed("cell-selection", false);
         d3.selectAll(".text-selection").classed("text-selection",false);
      })
      .on("mouseout", function() {
         if(d3.event.relatedTarget && d3.event.relatedTarget.tagName=='html') {
                 // remove selection frame
             sa.selectAll("rect.selection").remove();
                 // remove temporary selection marker class
             d3.selectAll('.cell-selection').classed("cell-selection", false);
             d3.selectAll(".rowLabel").classed("text-selected",false);
             d3.selectAll(".colLabel").classed("text-selected",false);
         }
      });
}

function resizeHeatmap(selection) {

    var divW = +selection.style("width").replace("px", ""),
        divH = +selection.style("height").replace("px", ""),
        title = selection.select(".titleBar"),
        titleH = title.style("height").replace("px", ""),
        chartH = divH - titleH - 20,
        chartW = divW - 10;

    //console.log("Resizing heatmap : div width=" + divW + " div height=" + divH + " chart width=" + chartW+ " chart height=" + chartH);


    selection.select(".svg-container")
        .style("width", divW + "px")
        .style("height", chartH + "px");
}