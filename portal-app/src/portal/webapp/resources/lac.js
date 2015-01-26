function makeMenuButtonActive(itemId) {
    $('.button').removeClass("active");
    $('#' + itemId).addClass("active");
}