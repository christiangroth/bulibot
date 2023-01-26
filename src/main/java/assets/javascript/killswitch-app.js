(function(){
	app = angular.module('bulibot-killswitch', ['bulibot']);
	app.controller('KillswitchController', ['$http', '$timeout', function($http, $timeout) {
		var apiSystemUrl = '/api/v1/system';
		var kc = this;
		
		kc.trigger = function() {
		  $http.put(apiSystemUrl + '/killswitch')
        }
	}]);
})();