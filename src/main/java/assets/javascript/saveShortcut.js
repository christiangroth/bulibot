function saveShortcut(id) {
	$(window).bind('keydown', function(event) {
	    if (event.ctrlKey || event.metaKey) {
	        switch (String.fromCharCode(event.which).toLowerCase()) {
	        case 's':
	            event.preventDefault();
	            $(id).click();
	            break;
	        }
	    }
	});
}
