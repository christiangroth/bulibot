(function(){
	app = angular.module('bulibot-api', ['bulibot']);
	app.controller('ApiController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/system';
		var ac = this;
		
		// API routes
		ac.apiRoutes = null;
		ac.loadApiRoutes = function() {
			$http.get(apiBaseUrl + '/../help').success(function(apiRoutes) {
				ac.apiRoutes = apiRoutes;
			});
		};
		ac.loadApiRoutes();
	}]);
})();