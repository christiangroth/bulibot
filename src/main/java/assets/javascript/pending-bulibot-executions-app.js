(function(){
	app = angular.module('bulibot-executions', ['bulibot']);
	app.controller('PendingBulibotExecutionsController', ['$http', '$timeout', function($http, $timeout) {
		var apiBaseUrl = '/api/v1/data';
		var apiSystemUrl = '/api/v1/system';
		var pbec = this;
		
		// current season
		pbec.season = null;
		pbec.loadSeason = function() {
			$http.get(apiBaseUrl + '/../currentSeason').success(function(season) {
				pbec.season = season; 
				pbec.loadMatchesWithPendingExecutions();
			});
		}
		pbec.loadSeason();

		// matches with pending executions
		pbec.matchesWithPendingExecutions = [];
		pbec.loadMatchesWithPendingExecutions = function() {
			$http.get(apiSystemUrl + '/pendingBulibotExecutions/' + pbec.season).success(function(matchesWithPendingExecutions) {
				pbec.matchesWithPendingExecutions = matchesWithPendingExecutions; 
			});
		}
		
		// deleting action
		pbec.inUserAction = false;
		pbec.userActionCode = null;
		pbec.userActionText = null;
		pbec.selectedUser = null;
		pbec.beginUserAction = function(match, code, text){
			pbec.inUserAction = true;
			pbec.selectedMatch = match;
			pbec.userActionCode = code;
			pbec.userActionText = text;
		};
		pbec.applyUserAction = function() {
			if(pbec.userActionCode == 'delete') {
				$http.delete(apiSystemUrl + '/pendingBulibotExecutions/' + pbec.season + '/' + pbec.selectedMatch.id).success(function(result) {
					pbec.loadMatchesWithPendingExecutions();
				});
				pbec.abortUserAction();
			}
		};
		pbec.abortUserAction = function(){
			pbec.userActionText = null;
			pbec.userActionCode = null;
			pbec.selectedMatch = null;
			pbec.inUserAction = false;
		};
	}]);
})();