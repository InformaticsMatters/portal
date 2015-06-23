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

function applyWorkflowPageLayout(jobsCheckBoxValue, visualizersCheckBoxValue) {
 if(jobsCheckBoxValue && visualizersCheckBoxValue) {
            $('#centralColumn').attr('class','ten wide column');
        }else if(!jobsCheckBoxValue && !visualizersCheckBoxValue) {
                $('#centralColumn').attr('class','sixteen wide column');
            } else if(jobsCheckBoxValue && !visualizersCheckBoxValue) {
                    $('#centralColumn').attr('class','thirteen wide column');
                }else if(!jobsCheckBoxValue && visualizersCheckBoxValue) {
                        $('#centralColumn').attr('class','thirteen wide column');
                    }
}