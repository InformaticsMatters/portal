function makeMenuButtonActive(itemId) {
    $('.button').removeClass("active");
    $('#' + itemId).addClass("active");
}

function makeMenuItemActive(itemId) {
    $('.item').removeClass("active");
    $('#' + itemId).addClass("active");
}

function makeVerticalItemActive(itemId) {
  //  $('.verticalItem').removeClass("active");
    $('#' + itemId).toggleClass("active");
}


function leftSideBarToggle(){
    $('.ui.left.sidebar')
        .sidebar('setting', 'transition', 'push')
        .sidebar('toggle')
    ;
}

function rightSideBarToggle(){
    $('.ui.right.sidebar')
        .sidebar('setting', 'transition', 'push')
        .sidebar('toggle')
    ;
}

$( document ).ready(function() {
    $('.ui.dropdown')
      .dropdown()
    ;


     var draggable = document.getElementById("datasetsDraggablesContainer");
     draggable.addEventListener('dragstart', dragStart, false);
     draggable.addEventListener('dragend'  , dragEnd  , false);

     draggable = document.getElementById("servicesDraggablesContainer");
     draggable.addEventListener('dragstart', dragStart, false);
     draggable.addEventListener('dragend'  , dragEnd  , false);


     var droptarget = document.getElementById("plumbContainer");
     droptarget.addEventListener('dragenter', dragEnter, false);
     droptarget.addEventListener('dragover', dragOver, false);
     droptarget.addEventListener('dragleave', dragLeave, false);
     droptarget.addEventListener('drop', drop, false);

     function dragStart(event) {
         event.dataTransfer.setData('draggableMarkupId', event.target.id);
         event.dataTransfer.setData('mouseOffsetX', event.layerX);
         event.dataTransfer.setData('mouseOffsetY', event.layerY);
         event.dataTransfer.dropEffect = "copy";
         event.dataTransfer.setData(event.target.getAttribute("dropdatatype"), "");
     }

     function dragEnd(event) {
     }

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
         var mouseOffsetX = event.dataTransfer.getData('mouseOffsetX');
         var mouseOffsetY = event.dataTransfer.getData('mouseOffsetY');
         var el = document.getElementById(draggableMarkupId);
         var dropDataType = el.getAttribute("dropDataType");
         var dropDataId = el.getAttribute("dropDataId");

        var $el = $(el);
        var width = $el.width();
        var height = $el.height();

        var left = event.offsetX - mouseOffsetX;
        var top = event.offsetY - mouseOffsetY;
         event.preventDefault();
         onCanvasDrop(dropDataType, dropDataId, left, top, draggableMarkupId);
         return false;
     }



     var $allCards = $('.card');
     $allCards.each(function(){
         this.addEventListener("dragenter", handleDataItemsBoxDragEnter, false);
         this.addEventListener("dragleave", handleDragLeave, false);
         this.addEventListener("dragover", preventDefaultEventHandling, false);
         this.addEventListener("drop", dropOntoDataItem, false);
     });

     var dragging = 0;

     function handleDataItemsBoxDragEnter(event) {
                 dragging++;
                 var dropType = this.getAttribute("dropdatatype");
                 if (event.dataTransfer.types.indexOf('dataset') > -1 && dropType == "service")  {
                     this.classList.add('over');
                 } else if (event.dataTransfer.types.indexOf('service') > -1 && dropType == "dataset")  {
                     this.classList.add('over');
                 }
                 event.stopPropagation();
                 event.preventDefault();
                 return false;
             }

     function preventDefaultEventHandling(event) {
             event.stopPropagation();
             event.preventDefault() ;
             return false;
      }

     function handleDragLeave(event) {
                 dragging--;
                 if (dragging === 0) {
                     this.classList.remove('over');
                 }
                 event.stopPropagation();
                 event.preventDefault();
                 return false;
             }

      function dropOntoDataItem(event) {
                 dragging--;
                 event.stopPropagation();
                 event.preventDefault();
                 this.classList.remove('over');
                 return false;

                }


});

function tabularMenu() {
    $('.tabular.menu .item').tab();
}



function applyWorkflowPageLayout(datasetsVisibility, servicesVisibility, canvasVisibility, jobsVisibility, visualizersVisibility) {

var datasetsVisible = (datasetsVisibility === 'true');
var servicesVisible = (servicesVisibility === 'true');
var canvasVisible = (canvasVisibility === 'true');
var jobsVisible = (jobsVisibility === 'true');
var visualizersVisible = (visualizersVisibility === 'true');

if(datasetsVisible) {
        $('.datasetsPanel').show();
    } else if (!datasetsVisible) {
            $('.datasetsPanel').hide();
        }
if(servicesVisible) {
    $('.servicesPanel').show();
        } else if (!servicesVisible) {
            $('.servicesPanel').hide();
            }
if(jobsVisible) {
      $('.jobsPanel').show();
  } else if (!jobsVisible) {
          $('.jobsPanel').hide();
      }
if(canvasVisible) {
      $('.plumbContainer').show();
  } else if (!canvasVisible) {
          $('.plumbContainer').hide();
      }
if(visualizersVisible) {
     $('.visualizersPanel').show();
 } else if (!visualizersVisible) {
         $('.visualizersPanel').hide();
     }

 if(!datasetsVisible && !servicesVisible && !canvasVisible && !jobsVisible && !visualizersVisible) {
        $('.left-column').attr('style','display: flex!important;');
        $('.right-column').attr('style','display:flex!important;');
    } else if ((datasetsVisible || servicesVisible) && (canvasVisible || jobsVisible || visualizersVisible)) {
            $('.left-column').attr('style','display: flex!important;');
            $('.right-column').attr('style','display: flex!important;');
        } else if ((datasetsVisible || servicesVisible) && (!canvasVisible && !jobsVisible && !visualizersVisible)) {
                $('.left-column').attr('style','margin-right: 20px!important; border-right: 1px solid #ddd;');
                $('.right-column').attr('style','display:none!important;');
           } else if ((!datasetsVisible && !servicesVisible) && (canvasVisible || jobsVisible || visualizersVisible)) {
                     $('.left-column').attr('style','display:none!important;');
                     $('.right-column').attr('style','margin-left: 20px!important; border-left: 1px solid #ddd;');
                }
}



if (window.FileReader) {
    var drop;
    addEventHandler(window, 'load', function () {
        drop = document.getElementById('drop');

        function cancel(e) {
            if (e.preventDefault) {
                e.preventDefault();
            }
            return false;
        }

        // Tells the browser that we *can* drop on this target
        addEventHandler(drop, 'dragover', cancel);
        addEventHandler(drop, 'dragenter', cancel);

        addEventHandler(drop, 'drop', function (e) {
            e = e || window.event; // get window.event if e argument missing (in IE)
            if (e.preventDefault) {
                e.preventDefault();
            }

            var dt = e.dataTransfer;
            var files = dt.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                 console.log(file);
                 $('.modal').modal('show');
            }

            return false;
        });

    });
}


function addEventHandler(obj, evt, handler) {
    if (obj.addEventListener) {
        // W3C method
        obj.addEventListener(evt, handler, false);
    } else if (obj.attachEvent) {
        // IE method.
        obj.attachEvent('on' + evt, handler);
    } else {
        // Old school method.
        obj['on' + evt] = handler;
    }
}
