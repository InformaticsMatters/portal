var onCanvasDrop;
var onCanvasItemDragStop;
var onCanvasNewConnection;

function makeCanvasItemsDraggable(selector) {
    jsPlumb.draggable($(selector), {
        containment: 'parent',
        stop: function(params) {
            var index = $('#' + params.el.id).index('.canvas-item');
            onCanvasItemDragStop(index, params.pos[0], params.pos[1]);
        }
    });
}

function addCanvasItem(plumbContainerId, itemId) {
    $('#' + plumbContainerId).append("<div class='canvas-item' id='" + itemId + "'></div>");
}

function removeCanvasItem(itemId) {
    $item = $('#' + itemId);
    jsPlumb.detachAllConnections($item);
    jsPlumb.removeAllEndpoints($item)
    $item.remove();
}

function addSourceEndpoint(itemId) {
    var sourceEndpointOptions = {
        anchor: 'BottomCenter',
        isSource:true,
        paintStyle: {
            fillStyle: "#7AB02C",
            radius: 10
        },
        connectorStyle : {
            lineWidth: 4,
            strokeStyle: "#61B7CF",
            joinstyle: "round"
        }
    };
    var sourceEndpoint = jsPlumb.addEndpoint(itemId, sourceEndpointOptions);
}

function addTargetEndpoint(itemId) {
    var targetEndpointOptions = {
        endpoint: 'Dot',
        anchor: 'TopCenter',
        isTarget:true,
        paintStyle : {
            strokeStyle: "#7AB02C",
            radius: 9,
            lineWidth: 3
        },
    };
    var targetEndpoint = jsPlumb.addEndpoint(itemId, targetEndpointOptions);
}

function init () {
    jsPlumb.setContainer($('#plumbContainer'));

    jsPlumb.bind("connection", function (i, c) {
        var sourceId = i.connection.sourceId;
        var targetId = i.connection.targetId;
        onCanvasNewConnection(sourceId, targetId);
    });
}
