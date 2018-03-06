function disableShowSearch(searchButton) {
	var btn = $(searchButton).find('.glyphicon');
	btn.addClass('glyphicon-refresh');
	btn.addClass('glyphicon-refresh-animate');
	$(searchButton).attr('disabled', 'disabled');
}

function enableHideSearch(searchButton) {
	var btn = $(searchButton).find('.glyphicon');
	btn.removeClass('glyphicon-refresh');
	btn.removeClass('glyphicon-refresh-animate');
	$(searchButton).removeAttr('disabled');
}