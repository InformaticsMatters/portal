var onNotebookCanvasPaletteDrop;
var onNotebookCanvasItemDragged;
var onNotebookCanvasItemResized;

function applyNotebookCanvasPageLayout(cellsVisibility, canvasVisibility, nbListVisibility) {

    var nbListVisible = (nbListVisibility === 'true');
    var cellsVisible = (cellsVisibility === 'true');
    var canvasVisible = (canvasVisibility === 'true');

    if (nbListVisible) {
            $('.notebookListPanel').show();
        } else if (!nbListVisible) {
            $('.notebookListPanel').hide();
        }

    if (cellsVisible) {
        $('.notebookCellsPanel').show();
    } else if (!cellsVisible) {
        $('.notebookCellsPanel').hide();
    }

    if (canvasVisible) {
        $('.plumbContainer').show();
    } else if (!canvasVisible) {
        $('.plumbContainer').hide();
    }

    if (!cellsVisible && !canvasVisible) {
        $('.left-column').attr('style','display: flex!important;');
        $('.right-column').attr('style','display:flex!important;');
    } else if (cellsVisible && canvasVisible) {
        $('.left-column').attr('style','display: flex!important;');
        $('.right-column').attr('style','display: flex!important;');
    } else if (cellsVisible && !canvasVisible) {
        $('.left-column').attr('style','margin-right: 20px!important; border-right: 1px solid #ddd;');
        $('.right-column').attr('style','display:none!important;');
    } else if (!cellsVisible && canvasVisible) {
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
            event.preventDefault();
            onNotebookCanvasPaletteDrop(dropDataType, dropDataId, left, top);
        }

        return false;
    }
}

function makeCanvasItemPlumbDraggable(selector) {
    jsPlumb.draggable($(selector), {
        containment: 'parent',
        filter : ".ui-resizable-handle.ui-resizable-se.ui-icon.ui-icon-gripsmall-diagonal-se",
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
            onNotebookCanvasItemDragged(index, params.pos[0], params.pos[1]);
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
            var index = $element.parent().index();
            onNotebookCanvasItemResized(index, ui.size.width, ui.size.height);
        }
    });
}

function fitTableDisplayGrid(id) {
    var $id = $('#' + id);
    var containerh = $id.outerHeight();

    var $grid = $id.find(".imxt-vista .imxt-body-container1");
    var gridTop = $grid.position().top;

    var h = containerh - gridTop - 12;
    $grid.css("height", h);
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

    if (top != 0) {
        $parent = $id.parent();
        $parent.css('top', top + 'px');
        $parent.css('left', left + 'px');
    }
}

var sourceEndpointOptions = {
    anchor: ["Continuous", {shape: "Rectangle", faces:["bottom"]}],
    isSource: true,
    maxConnections: -1,
    paintStyle: {
        fillStyle: "#7AB02C",
        radius: 9
    },
    connectorStyle: {
        lineWidth: 2,
        strokeStyle: "#61B7CF"
    }
};

var targetEndpointOptions = {
    endpoint: 'Dot',
    anchor: ["Continuous", {shape: "Rectangle", faces:["top"]}],
    maxConnections: -1,
    isTarget: true,
    paintStyle: {
        strokeStyle: "#7AB02C",
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

