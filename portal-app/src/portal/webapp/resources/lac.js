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



function applyWorkflowPageLayout(datasetsVisibility, servicesVisibility) {
var datasetsVisible = (datasetsVisibility === 'true');
var servicesVisible = (servicesVisibility === 'true');

 if(!datasetsVisible && !servicesVisible) {
        $('.left-column').attr('style','display: none!important;');
        $('.right-column').attr('style','margin-left: 20px!important; border-left: 1px solid #ddd;');
    } else if (datasetsVisible && servicesVisible) {
            $('.left-column').attr('style','display: flex!important;');
            $('.right-column').attr('style','border: 0; margin-left: 0px!important;');
        } else if (!datasetsVisible && servicesVisible) {
                  $('.left-column').attr('style','display: flex!important;');
                  $('.right-column').attr('style','border: 0; margin-left: 0px!important;');
              } else if (datasetsVisible && !servicesVisible) {
                        $('.left-column').attr('style','display: flex!important;');
                        $('.right-column').attr('style','border: 0; margin-left: 0px!important;');
                   }
}