function makeMenuButtonActive(itemId) {
    $('.button').removeClass("active");
    $('#' + itemId).addClass("active");
}

function makeMenuItemActive(itemId) {
    $('.item').removeClass("active");
    $('#' + itemId).addClass("active");
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

function onClickCardPopup() {
    $('.card')
      .popup({
        popup: '.clickCardPopup.popup',
        on    : 'click',
        position: 'right center'
      })
    ;
}

function onHoverCardPopup() {
    $('.card .content')
      .popup({
        popup: '.hoverCardPopup.popup',
        on    : 'hover',
        position: 'left center'
      })
    ;
}