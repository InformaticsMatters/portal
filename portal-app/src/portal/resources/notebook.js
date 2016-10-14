var onNotebookCanvasPaletteDrop;
var onNotebookCanvasItemDragged;
var onNotebookCanvasItemResized;
var onVersionTreeNodeSelection;

function applyNotebookCanvasPageLayout(cellsVisibility, canvasVisibility, nbListVisibility, versionTreeVisibility) {

    var nbListVisible = (nbListVisibility === 'true');
    var cellsVisible = (cellsVisibility === 'true');
    var canvasVisible = (canvasVisibility === 'true');
    var versionTreeVisible = (versionTreeVisibility === 'true');

    if (nbListVisible) {
        $('.notebookListPanel').show();
        $('.nbListMenu').show();
        $('.nbListToggle').addClass("active");
    } else if (!nbListVisible) {
        $('.notebookListPanel').hide();
        $('.nbListMenu').hide();
        $('.nbListToggle').removeClass("active");
    }

    if (cellsVisible) {
        $('.notebookCellsPanel').show();
        $('.cellsToggle').addClass("active");
    } else if (!cellsVisible) {
        $('.notebookCellsPanel').hide();
        $('.cellsToggle').removeClass("active");
    }

    if (canvasVisible) {
        $('.notebookContainer').show();
        $('.canvasToggle').addClass("active");
    } else if (!canvasVisible) {
        $('.notebookContainer').hide();
        $('.canvasToggle').removeClass("active");
    }

    if (versionTreeVisible) {
            $('.notebookVersionTreePanel').show();
            $('.versionTreeToggle').addClass("active");
        } else if (!versionTreeVisible) {
            $('.notebookVersionTreePanel').hide();
            $('.versionTreeToggle').removeClass("active");
        }

    if (!cellsVisible && !nbListVisible && !canvasVisible && !versionTreeVisible) {
        $('.left-column').attr('style','display: flex!important;');
        $('.right-column').attr('style','display:flex!important;');
    } else if ((cellsVisible || nbListVisible) && (canvasVisible || versionTreeVisible)) {
        $('.left-column').attr('style','display: flex!important;');
        $('.right-column').attr('style','display: flex!important;');
    } else if ((cellsVisible || nbListVisible) && (!canvasVisible && !versionTreeVisible)) {
        $('.left-column').attr('style','margin-right: 20px!important; border-right: 1px solid #ddd;');
        $('.right-column').attr('style','display:none!important;');
    } else if ((!cellsVisible && !nbListVisible) && (canvasVisible || versionTreeVisible)) {
        $('.left-column').attr('style','display:none!important;');
        $('.right-column').attr('style','margin-left: 20px!important; border-left: 1px solid #ddd;');
    }


}

function addCellsPaletteDragAndDropSupport() {
    var draggable = document.getElementById("cellsDraggablesContainer");
    draggable.addEventListener('dragstart', dragStart, false);
    draggable.addEventListener('dragend'  , dragEnd  , false);

    function dragStart(event) {
        event.dataTransfer.setData('draggableMarkupId', event.target.id);
        event.dataTransfer.setData('mouseOffsetX', event.layerX);
        event.dataTransfer.setData('mouseOffsetY', event.layerY);
        event.dataTransfer.dropEffect = "copy";
        event.dataTransfer.setData(event.target.getAttribute("dropdatatype"), "");
        event.dataTransfer.setData(event.target.getAttribute("dropdataid"), "");
    }

    function dragEnd(event) {
    }

    var droptarget = document.getElementById("plumbContainer");
    droptarget.addEventListener('dragenter', dragEnter, false);
    droptarget.addEventListener('dragover', dragOver, false);
    droptarget.addEventListener('dragleave', dragLeave, false);
    droptarget.addEventListener('drop', drop, false);

    function dragEnter(event) {
    }

    function dragOver(event) {
        event.preventDefault();
        return false;
    }

    function dragLeave(event) {
    }

    function drop(event) {
        var draggableMarkupId = event.dataTransfer.getData('draggableMarkupId');

        if (draggableMarkupId != "") {
            var mouseOffsetX = event.dataTransfer.getData('mouseOffsetX');
            var mouseOffsetY = event.dataTransfer.getData('mouseOffsetY');
            var el = document.getElementById(draggableMarkupId);
            var dropDataType = el.getAttribute("dropDataType");
            var dropDataId = el.getAttribute("dropDataId");

            var $el = $(el);
            var width = $el.width();
            var height = $el.height();

            var left = event.layerX - mouseOffsetX;
            var top = event.layerY - mouseOffsetY;

            if (top < 0) {
                top = 0
            }

            if (left < 0) {
                left = 0
            }
            event.preventDefault();
            onNotebookCanvasPaletteDrop(dropDataType, dropDataId, left, top);
        }

        return false;
    }
}

function makeCanvasItemPlumbDraggable(selector) {
    jsPlumb.draggable($(selector), {
        containment: 'parent',
        filter : ".titleBar, .titleBarContent, .tbCellName",
        filterExclude:false,
        drag: function(params) {
            var positionx = params.pos[0];
            var cellWidth = $('#' + params.el.id).outerWidth();
            var borderRight = positionx + cellWidth;

            var containerWidth = $('#plumbContainer').outerWidth();
            var newWidth = containerWidth + cellWidth;

            if(borderRight == containerWidth) {
               $('#plumbContainer').css("min-width", newWidth);
            }

            var positiony = params.pos[1];
            var cellHeight = $('#' + params.el.id).outerHeight();
            var borderBottom = positiony + cellHeight;

            var containerHeight = $('#plumbContainer').outerHeight();
            var newHeight = containerHeight + cellHeight;
            var outerContainerHeight = $('.notebookContainer').outerHeight();

            if(borderBottom == containerHeight) {
               $('.notebookContainer').css("max-height", outerContainerHeight);
               $('#plumbContainer').css("height", newHeight);
            }


        },

        stop: function(params) {
            var index = $('#' + params.el.id).index('.notebook-canvas-item');
            var $plumbContainer = $('#plumbContainer');
            var containerHeight = $plumbContainer.outerHeight();
            var containerWidth = $plumbContainer.outerWidth();
            onNotebookCanvasItemDragged(index, params.pos[0], params.pos[1], containerWidth, containerHeight);
        }
    });
}

function makeCanvasItemResizable(width, height, id, fitCallback) {
    $("#" + id).resizable({
        minWidth: width,
        minHeight: height,
        resize : function(event, ui) {
            jsPlumb.repaintEverything();
            fitCallback(id);
        },

        stop: function(event, ui) {
            $element = $(ui.element);
            var index = $element.parent().index('.notebook-canvas-item');
            onNotebookCanvasItemResized(index, ui.size.width, ui.size.height);
        }
    });
}

 /*
function fitScatterPlot(id) {
    var svg = d3.select(".main");
    var dim = Math.min(parseInt(d3.select(".chart").style("width")), parseInt(d3.select(".chart").style("height")));
    var width = dim - 80;
    var height = dim - 80;

    var x = d3.scale.linear();

         var y = d3.scale.linear();

    var r = d3.scale.linear();

        var xAxis = d3.svg.axis();

        var yAxis = d3.svg.axis();

      x.range([0, width]);
      y.range([height, 0]);
      // Update the axis and text with the new scale
      svg.select('.x.axis')
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

      svg.select('.x.axis').select('.label')
            .attr("x",width);

        svg.select('.y.axis')
          .call(yAxis);

    // Update the tick marks
      xAxis.ticks(dim / 75);
      yAxis.ticks(dim / 75);

      // Update the circles
      r.range([dim / 90, dim / 35])

      var g =  d3.select(".main");

          g.selectAll("scatter-dots")
            .append("svg:circle")
                .attr("cx", function (d,i) { return x(d.x); } )
                .attr("cy", function (d) { return y(d.y); } )
                .attr("r", 5);
}
*/

function fitTableDisplayGrid(id) {
    var $id = $('#' + id);
    var containerh = $id.outerHeight();

    var $grid = $id.find(".imxt-vista .imxt-body-container1");
    var gridTop = $grid.position().top;

    var $status = $id.find(".extra.content");
    var $statush = $id.find(".extra.content").outerHeight();
    var statuspos = containerh - $statush - gridTop + 5;

    var h = containerh - gridTop - $statush;
    $grid.css("height", h);
}

function fit3DViewer(id) {
    var $id = $('#' + id);
    var containerh = $id.outerHeight();

    var $viewer = $id.find(".gldiv");
    var $viewerTop = $viewer.position().top;

    var $status = $id.find(".extra.content");
    var $statush = $id.find(".extra.content").outerHeight();
    var statuspos = containerh - $statush - $viewerTop + 5;

    var h = containerh - $viewerTop - $statush;
    $viewer.css("height", h);
    $status.css("margin-top", statuspos);
}

function fitScriptTextArea(id) {
    var $id = $('#' + id);
    var containerh = $id.outerHeight();
    var containerOffset = $id.offset().top;

    var $textarea = $id.find("textarea");
    var textareaOffset = $id.find("textarea").offset().top;
    var textareaPosition = textareaOffset - containerOffset;

    var h = containerh - textareaPosition - 12;
    $textarea.css("height", h);
}

function fitDefinitionsArea(id) {
    var $id = $('#' + id);
    var containerh = $id.outerHeight();
    var containerOffset = $id.offset().top;

    var $textarea = $id.find("textarea");
    var textareaOffset = $id.find("textarea").offset().top;
    var textareaPosition = textareaOffset - containerOffset;

    var h = containerh - textareaPosition - 12;
    $textarea.css("height", h);
}

function initCellSizeAndPosition(id, left, top, width, height) {
    $id = $('#' + id);
    if (width != 0) {
        $id.width(width);
    }
    if (height != 0) {
        $id.height(height);
    }
    if ((top != 0) || (left != 0)) {
        $parent = $id.parent();
        $parent.css('top', top + 'px');
        $parent.css('left', left + 'px');
    }
}

var needsRepaintFix = true;

var sourceEndpointOptions = {
    endpoint: 'Dot',
    anchor: ["Continuous", {faces:["bottom", "right"]}],
    maxConnections: -1,
    isSource: true,
    scope: "data",
    paintStyle: {
        fillStyle: "#7AB02C",
        radius: 10
    },
    connectorStyle: {
        lineWidth: 2,
        strokeStyle: "#CEEBA3"
    },
     dragOptions:{
         start: function(e, ui) {
                     needsRepaintFix = true;
                 },
         drag: function(e, ui) {
             if(needsRepaintFix){
                 jsPlumb.repaintEverything();
                 needsRepaintFix = false;
             }
         }
     }
};

var targetEndpointOptions = {
    endpoint: 'Dot',
    anchor:["Continuous", {faces:["top", "left"]}],
    maxConnections: -1,
    isTarget: true,
    scope: "data",
    paintStyle: {
        strokeStyle: "#7AB02C",
        radius: 9,
        lineWidth: 3
    }
};

var optionSourceEndpointOptions = {
    endpoint: 'Dot',
    anchor: ["Continuous", {faces:["bottom", "right"]}],
    maxConnections: -1,
    isSource: true,
    scope: "options",
    paintStyle: {
        fillStyle: "#FC8A02",
        radius: 10
    },
    connectorStyle: {
        lineWidth: 2,
        strokeStyle: "#FFCA60"
    },
      dragOptions:{
          start: function(e, ui) {
                      needsRepaintFix = true;
                  },
          drag: function(e, ui) {
              if(needsRepaintFix){
                  jsPlumb.repaintEverything();
                  needsRepaintFix = false;
              }
          }
      }
};

var optionTargetEndpointOptions = {
    endpoint: 'Dot',
    anchor:["Continuous", {faces:["top", "left"]}],
    maxConnections: -1,
    isTarget: true,
    scope: "options",
    paintStyle: {
        strokeStyle: "#FC8A02",
        radius: 9,
        lineWidth: 3
    }
};

function addSourceEndpoint(itemId, endpointId, labelText) {
    var sourceEndpoint = jsPlumb.addEndpoint(itemId, sourceEndpointOptions, {uuid: endpointId});

    sourceEndpoint.bind("mouseover", function(sourceEndpoint) {
        sourceEndpoint.addOverlay(["Label", {label: labelText, id: "label", location: [1.5, 1.5]}]);
    });
    sourceEndpoint.bind("mouseout", function(sourceEndpoint) {
        sourceEndpoint.removeOverlay("label");
    });
}

function addTargetEndpoint(itemId, endpointId, labelText) {
    var targetEndpoint = jsPlumb.addEndpoint(itemId, targetEndpointOptions, {uuid: endpointId});
    targetEndpoint.bind("mouseover", function(targetEndpoint) {
        targetEndpoint.addOverlay(["Label", {label: labelText, id: "label", location: [-0.5, -0.5]}]);
    });
    targetEndpoint.bind("mouseout", function(targetEndpoint) {
        targetEndpoint.removeOverlay("label");
    });
}

function addOptionSourceEndpoint(itemId, endpointId, labelText) {
    var optionSourceEndpoint = jsPlumb.addEndpoint(itemId, optionSourceEndpointOptions, {uuid: endpointId});
    optionSourceEndpoint.bind("mouseover", function(optionSourceEndpoint) {
        optionSourceEndpoint.addOverlay(["Label", {label: labelText, id: "label", location: [1.5, 1.5]}]);
    });
    optionSourceEndpoint.bind("mouseout", function(optionSourceEndpoint) {
        optionSourceEndpoint.removeOverlay("label");
    });
}

function addOptionTargetEndpoint(itemId, endpointId, labelText) {
    var optionTargetEndpoint = jsPlumb.addEndpoint(itemId, optionTargetEndpointOptions, {uuid: endpointId});
    optionTargetEndpoint.bind("mouseover", function(optionTargetEndpoint) {
        optionTargetEndpoint.addOverlay(["Label", {label: labelText, id: "label", location: [-0.5, -0.5]}]);
    });
    optionTargetEndpoint.bind("mouseout", function(optionTargetEndpoint) {
        optionTargetEndpoint.removeOverlay("label");
    });
}

function addConnection(sourceEndpointUuid, targetEndpointUuid) {
    jsPlumb.connect({
        uuids: [sourceEndpointUuid, targetEndpointUuid],
        connector: ["Flowchart", {gap: 10, cornerRadius: 5}],
        overlays: [
            ["Arrow", {location: 1, id: "arrow", length: 14, foldback: 0.8}]
        ]
    });
}

function initJsPlumb() {
    jsPlumb.setContainer($('#plumbContainer'));

    jsPlumb.bind("beforeDrop", function (i, c) {
        var sourceId = i.connection.endpoints[0].getUuid();
        var targetId = i.dropEndpoint.getUuid();
        onNotebookCanvasNewConnection(sourceId, targetId);
        return false;
    });
}

function makeNbTrActive(itemId) {
    $('.nbTr').removeClass("selected");
    $('#' + itemId).addClass("selected");
}

function applySavedCanvasSize(width, height) {
    $plumbContainer = $('#plumbContainer');
    $plumbContainer.css("min-width", width);
    $plumbContainer.css("height", height);
}

function applyCellMenuAction(id, value) {
    //console.log("Applying action " + value + " for #" + id);
    if (value == 'showContent' || value == 'showDescription')  {
        var checkbox = $('#' + id + ' form .' + value);
        var wasChecked = checkbox.prop('checked');
        checkbox.prop('checked', !wasChecked);
        var submit = $('#' + id + ' form .changeVisibilityButton');
        submit.trigger('click');
    } else if (value == 'editDescription') {
        var submit = $('#' + id + ' form .editDescriptionButton');
        submit.trigger('click');
    } else if (value == 'bindings') {
        var submit = $('#' + id + ' form .bindingsButton');
        submit.trigger('click');
    } else if (value == 'delete') {
        var submit = $('#' + id + ' form .deleteButton');
        submit.trigger('click');
    }
}


