(function(){
	app = angular.module('bulibot-releasenotes', ['bulibot']);
	app.controller('ReleaseNotesController', ['$http', function($http) {
		var rc = this;
		
		// release notes
		rc.releaseNotes = [];
		rc.loadReleaseNotes = function() {
			$http.get('/assets/data/release-notes.json').success(function(releaseNotes) {
				rc.releaseNotes = releaseNotes;
			});
		};
		rc.loadReleaseNotes();
	}]);
})();