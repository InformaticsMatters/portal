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
        var status = document.getElementById('status');
        drop = document.getElementById('drop');
        var list = document.getElementById('list');

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
            } // stops the browser from redirecting off to the image.

            var dt = e.dataTransfer;
            var files = dt.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                var reader = new FileReader();

                //attach event handlers here...

                reader.readAsDataURL(file);
                addEventHandler(reader, 'loadend', function (e, file) {
                    var bin = this.result;

                    var newFile = document.createElement('div');
                    newFile.innerHTML = file.name;
                    newFile.className = "card palette-item";
                    drop.appendChild(newFile);


                }.bindToEventHandler(file));
            }
            return false;
        });
        Function.prototype.bindToEventHandler = function bindToEventHandler() {
            var handler = this;
            var boundParameters = Array.prototype.slice.call(arguments);
            //create closure
            return function (e) {
                e = e || window.event; // get window.event if e argument missing (in IE)
                boundParameters.unshift(e);
                handler.apply(this, boundParameters);
            }
        };
    });
} else {
    document.getElementById('status').innerHTML = 'Your browser does not support the HTML5 FileReader.';
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
