var onCanvasDrop;
var onCanvasItemDragStop;

function setupPalette() {
    $('.palette-item').draggable({
        cursor: 'move',
        helper: 'clone',
        scroll: false,
        appendTo: '#plumbContainer',
        start: function () {},
        stop: function (event, ui) {}
    });
}

function setupCanvas() {
    jsPlumb.setContainer($('#plumbContainer'));

    $('#plumbContainer').droppable({
        accept: '.palette-item',
        drop: function(event, ui) {
            onCanvasDrop(ui.position.left, ui.position.top);
        }
    });
}

function makeCanvasItemsDraggable(selector) {
    jsPlumb.draggable($(selector), {
        containment: 'parent',
        stop: function(params) {
            var index = $('#' + params.el.id).index('.canvas-item');
            onCanvasItemDragStop(index, params.pos[0], params.pos[1]);
        }
    });
}

function addSourceEndpoint(itemId) {
    var sourceEndpointOptions = {
        isSource:true,
        paintStyle : {
            fillStyle:'green'
        },
        connectorStyle : {
            strokeStyle:'green',
            lineWidth:8
        }
    };
    var sourceEndpoint = jsPlumb.addEndpoint(itemId, sourceEndpointOptions);
}

function addTargetEndpoint(itemId) {
    var targetEndpointOptions = {
        endpoint: 'Rectangle',
        anchor: 'TopCenter',
        isTarget:true,
        paintStyle : {
            fillStyle:'red'
        },
    };
    var targetEndpoint = jsPlumb.addEndpoint(itemId, targetEndpointOptions);
}

function init () {
    setupPalette();
    setupCanvas();
}


