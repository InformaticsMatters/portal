var onNotebookCanvasPaletteDrop;
var onNotebookCanvasItemDragged;

function applyNotebookCanvasPageLayout(cellsVisibility, canvasVisibility) {
    var cellsVisible = (cellsVisibility === 'true');
    var canvasVisible = (canvasVisibility === 'true');

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
        stop: function(params) {
            var index = $('#' + params.el.id).index('.notebook-canvas-item');
            onNotebookCanvasItemDragged(index, params.pos[0], params.pos[1]);
        }
    });
}

function initJsPlumb() {
    jsPlumb.setContainer($('#plumbContainer'));
}

