(function(){
	app = angular.module('bulibot-matches', ['bulibot']);
	app.controller('MatchesController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/data';
		var mc = this;
		
		mc.seasons = [];
		mc.loadSeasons = function() {
			$http.get(apiBaseUrl + '/seasons').success(function(seasons) {
				mc.seasons = seasons; 
			});
		};
		mc.loadSeasons();
		
		mc.season = null;
		mc.currentSeason = null;
		mc.matchday = null;
		mc.currentMatchday = null;
		mc.loadCurrentData = function() {
			$http.get(apiBaseUrl + '/../currentSeason').success(function(currentSeason) {
				
				// init season
				mc.season = currentSeason;
				mc.currentSeason = currentSeason; 
				
				$http.get(apiBaseUrl + '/../currentMatchday').success(function(currentMatchday) {
					
					// init matchday
					mc.matchday = currentMatchday;
					mc.currentMatchday = currentMatchday;
					
					// load data
					mc.loadMatchAndStatisticData();
				});
			});
		};
		mc.loadCurrentData();
		
		mc.teams = {};
		mc.loadTeams = function() {
			$http.get(apiBaseUrl + '/teams').success(function(teams) {
				for (team of teams) {
					mc.teams[team.id] = team; 
				}
			});
		};
		mc.loadTeams();
		
		mc.selectedMatch = null;
		mc.selectMatch = function(match) {
			mc.selectedMatch = match;
		};
		
		mc.switchSeason = function(season) {
			
			// determine matchday to load
			if(!season) {
				matchdayToLoad = null;
			} else if(season == mc.currentSeason) {
				matchdayToLoad = mc.currentMatchday;
			} else if(season < mc.currentSeason) {
				matchdayToLoad = 34;
			} else {
				matchdayToLoad = 1;
			}
			
			// set data
			mc.season = season;
			mc.matchday = matchdayToLoad;
			
			// load data
			mc.loadMatchAndStatisticData();
		};
		
		mc.previousMatchday = function() {
			mc.matchday = mc.matchday - 1;
			mc.loadMatchAndStatisticData();
		};

		mc.nextMatchday = function() {
			mc.matchday = mc.matchday + 1;
			mc.loadMatchAndStatisticData();
		};

		mc.tab = "matches";
		mc.setTab = function(value) {
			mc.tab = value;
		};
		
		mc.matches = [];
		mc.statistics = null;
		mc.loadMatchAndStatisticData = function() {

			// load concrete season and matchday data
			if(mc.season && mc.matchday) {
				$http.get(apiBaseUrl + '/matches/' + mc.season + '/' + mc.matchday).success(function(matches) {
					mc.matches = matches; 
				});
				$http.get(apiBaseUrl + '/statistics' + '/' + mc.season + '/' + mc.matchday).success(function(statistics) {
					mc.statistics = statistics; 
				});
			} 
			
			// load overall data
			else if(!mc.season && !mc.matchday) {
				mc.matches = []; 
				if(mc.tab == "matches") {
					mc.tab = "ranking";
				}
				$http.get(apiBaseUrl + '/statistics').success(function(statistics) {
					mc.statistics = statistics; 
				});
			}
			
			// no data
			else {
				mc.matches = [];
				mc.statistics = null;
			}
		};
	}]);
})();