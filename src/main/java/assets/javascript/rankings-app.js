(function(){
	app = angular.module('bulibot-rankings', ['bulibot']);
	app.controller('RankingsController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/data';
		var rc = this;
		
		rc.userNames = [];
		rc.loadUserNames = function() {
			$http.get(apiBaseUrl + '/userNames').success(function(userNames) {
				rc.userNames = userNames;
			});
		};
		rc.loadUserNames();
		
		rc.seasons = [];
		rc.currentSeason = null;
		rc.currentMatchday = null;
		rc.loadSeasons = function() {
			$http.get(apiBaseUrl + '/rankingSeasons').success(function(seasons) {
				rc.seasons = seasons;
			});
		};
		rc.loadSeasons();

		rc.teams = {};
		rc.loadTeams = function() {
			$http.get(apiBaseUrl + '/teams').success(function(teams) {
				for (team of teams) {
					rc.teams[team.id] = team; 
				}
			});
		};
		rc.loadTeams();
		
		rc.loadCurrentSeason = function() {
			$http.get(apiBaseUrl + '/../currentSeason').success(function(currentSeason) {
				rc.season = currentSeason; 
				rc.currentSeason = currentSeason; 
				rc.loadCurrentMatchday();
				rc.loadSeasonChartData(false);
			});
		}
		rc.loadCurrentMatchday = function() {
			$http.get(apiBaseUrl + '/../currentMatchday').success(function(currentMatchday) {
				rc.matchday = currentMatchday; 
				rc.currentMatchday = currentMatchday; 
				rc.loadRankingData();
			});
		};
		rc.loadCurrentSeason();
		
		rc.user = null;
		rc.loadUser = function() {
			$http.get(apiBaseUrl + '/user').success(function(user) {
				rc.user = user;
			});
		};
		rc.loadUser();
		
		rc.season = null;
		rc.matchday = null;
		rc.rankingData = null;
		rc.switchSeason = function(season) {
			
			// determine matchday to load
			matchdayToLoad = 1;
			if(season == rc.currentSeason) {
				matchdayToLoad = rc.currentMatchday;
			} else if(season < rc.currentSeason) {
				matchdayToLoad = 34;
			}

			// set data
			rc.season = season;
			rc.matchday = matchdayToLoad;
			
			// load data
			rc.loadRankingData();

			// season charts
			if(rc.showSeasonCharts) {
				rc.loadSeasonChartData(true);
			}
		};
		
		rc.previousMatchday = function() {
			rc.matchday = rc.matchday - 1;
			rc.loadRankingData();
		};

		rc.nextMatchday = function() {
			rc.matchday = rc.matchday + 1;
			rc.loadRankingData();
		};
		
		rc.loadRankingData = function() {

			// set current data
			$http.get(apiBaseUrl + '/rankingData/' + rc.season + '/' + rc.matchday).success(function(rankingData) {
				rc.rankingData = rankingData;
			});
		};
		
		rc.showSeasonCharts = false;
		rc.toggleSeasonCharts = function() {
			rc.showSeasonCharts = !rc.showSeasonCharts;
			if(rc.showSeasonCharts) {
				rc.buildSeasonCharts();
			}
		};
		
		rc.seasonRankingData = null;
		rc.loadSeasonChartData = function(buildCharts) {
			$http.get(apiBaseUrl + '/rankingData/' + rc.season).success(function(rankingData) {
				rc.seasonRankingData = rankingData;
				
				if(buildCharts) {
					rc.buildSeasonCharts();
				}
			});
		};
		
		rc.buildSeasonCharts = function() {
			
			// prepare data
			matchday = 0;
			totalRanks = 0;
			seasonChartData = {};
			for(matchdayRankingData of rc.seasonRankingData) {

				// check total ranks
				if(totalRanks < matchdayRankingData.ranks.length) {
					totalRanks = matchdayRankingData.ranks.length; 
				}
				
				// next matchday
				matchday += 1;

				// check ranks
				for(matchdayRank of matchdayRankingData.ranks) {

					// future matchday data has a rank but no matchday rank
					if(matchdayRank.matchdayRank == null) {
						continue;
					}
					
					// ensure user data
					userData = seasonChartData[matchdayRank.rank.userId];
					if(!userData) {
						
						// create new user data
						userData = {};
						userData.id = matchdayRank.rank.userId;
						userData.name = rc.userNames[userData.id].name;
						userData.points = [];
						userData.pointsPerMatchday = [];
						userData.rank = [];
						userData.rankPerMatchday = [];
						
						// prepend empty values
						for(i = 0; i < matchday; i++) {
							userData.points.push(0);
							userData.pointsPerMatchday.push(0);
							userData.rank.push(0);
							userData.rankPerMatchday.push(0);
						}
						
						// add to cache
						seasonChartData[matchdayRank.rank.userId] = userData;
					}

					// add data from rankings
					userData.points.push(matchdayRank.rank.points);
					userData.pointsPerMatchday.push(matchdayRank.matchdayRank.points);
					userData.rank.push(matchdayRank.rank.position);
					userData.rankPerMatchday.push(matchdayRank.matchdayRank.position);
				}
			}
			
			// build charts
			rc.buildSeasonPointsChart(seasonChartData);
			rc.buildSeasonPointsPerMatchdayChart(seasonChartData);
			rc.buildSeasonRanksChart(seasonChartData, totalRanks);
			rc.buildSeasonRanksPerMatchdayChart(seasonChartData, totalRanks);
		};
		
		rc.buildSeasonPointsChart = function(seasonChartData) {
			
			// prepare chart data
			seriesData = [];
			for(userId in seasonChartData) {
				seriesData.push({
					type: 'spline',
					name: seasonChartData[userId].name,
					data: seasonChartData[userId].points
				});
			}
			
			// create chart
			$('#seasonChartPoints').highcharts({
			    
				title: {
					text: 'Punkte im Saisonverlauf'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'},
					min: 1
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Punkte'}
				},
			    
			    tooltip: {
			        shared: true,
			        headerFormat: '<span style="font-size: 10px">{point.key}. Spieltag</span><br/>'
			    },
			    
			    series: seriesData,

			    responsive: {
                   rules: [{
                       condition: {
                           maxWidth: 1024
                       }
                   }]
                },

			    exporting: { enabled: false }
			});
		};
		
		rc.buildSeasonPointsPerMatchdayChart = function(seasonChartData) {
			
			// prepare chart data
			seriesData = [];
			for(userId in seasonChartData) {
				seriesData.push({
					type: 'column',
					name: seasonChartData[userId].name,
					data: seasonChartData[userId].pointsPerMatchday
				});
			}
			
			// create chart
			$('#seasonChartPointsPerMatchday').highcharts({
				
				title: {
					text: 'Punkte pro Spieltag'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'},
					min: 1
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Punkte'}
				},
			    
			    tooltip: {
			        shared: true,
			        headerFormat: '<span style="font-size: 10px">{point.key}. Spieltag</span><br/>'
			    },
				
				series: seriesData,

			    responsive: {
                   rules: [{
                       condition: {
                           maxWidth: 1024
                       }
                   }]
                },

				exporting: { enabled: false }
			});
		};
		
		rc.buildSeasonRanksChart = function(seasonChartData, ranks) {
			
			// prepare chart data
			seriesData = [];
			for(userId in seasonChartData) {
				seriesData.push({
					type: 'areaspline',
					name: seasonChartData[userId].name,
					data: seasonChartData[userId].rank
				});
			}
			
			// create chart
			$('#seasonChartRanks').highcharts({
				
				title: {
					text: 'Platzierung im Saisonverlauf'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'},
					min: 1
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Platzierung'},
					min: 1,
					max: ranks,
					reversed: true
				},
			    
			    tooltip: {
			        shared: true,
			        headerFormat: '<span style="font-size: 10px">{point.key}. Spieltag</span><br/>'
			    },
				
				series: seriesData,

			    responsive: {
                   rules: [{
                       condition: {
                           maxWidth: 1024
                       }
                   }]
                },
				
				chart: { height: 208 + (ranks * 24) },
				exporting: { enabled: false }
			});
		}
			
		rc.buildSeasonRanksPerMatchdayChart = function(seasonChartData, ranks) {
			
			// prepare chart data
			seriesData = [];
			for(userId in seasonChartData) {
				seriesData.push({
					type: 'spline',
					name: seasonChartData[userId].name,
					data: seasonChartData[userId].rankPerMatchday
				});
			}
			
			// create chart
			$('#seasonChartRanksPerMatchday').highcharts({
				
				title: {
					text: 'Platzierung pro Spieltag'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'},
					min: 1
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Platzierung'},
					min: 1,
					max: ranks,
					reversed: true
				},
			    
			    tooltip: {
			        shared: true,
			        headerFormat: '<span style="font-size: 10px">{point.key}. Spieltag</span><br/>'
			    },
				
				series: seriesData,

			    responsive: {
                   rules: [{
                       condition: {
                           maxWidth: 1024
                       }
                   }]
                },
				
				chart: { height: 208 + (ranks * 24) },
				exporting: { enabled: false }
			});
		};
		
		rc.showStatisticDetails = false;
		rc.toggleStatisticDetails = function() {
			rc.showStatisticDetails = !rc.showStatisticDetails;
		};
		
		rc.selectedRank = null;
		rc.selectRank = function(rank) {
			rc.selectedRank = rank;
			if(rc.selectedRank != null) {
				rc.buildChartsForRank()
			}
		};
		
		rc.configureHighcharts = function() {

			// Radialize the colors
			Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, function (color) {
			    return {
			        radialGradient: {
			            cx: 0.5,
			            cy: 0.3,
			            r: 0.7
			        },
			        stops: [
			            [0, color],
			            [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
			        ]
			    };
			});
		};
		rc.configureHighcharts();
		
		rc.buildChartsForRank = function() {

			// create charts - team distribution
			rc.createPieChart("#chartBulibotTeamDistribution", "Bulibot Tendenzen", [
				{ name: 'Heim: ', y: rc.selectedRank.rank.resultsHomeTeam },
				{ name: 'Gast',   y: rc.selectedRank.rank.resultsAwayTeam },
				{ name: 'Remis',  y: rc.selectedRank.rank.resultsDraw },
				{ name: 'Fehler', y: rc.selectedRank.rank.resultsWithError }
            ]);
			rc.createPieChart("#chartMatchTeamDistribution", "Bundesliga Tendenzen", [
				{ name: 'Heim: ', y: rc.rankingData.resultsHomeTeam },
				{ name: 'Gast',   y: rc.rankingData.resultsAwayTeam },
				{ name: 'Remis',  y: rc.rankingData.resultsDraw }
			]);
			rc.createPieChart("#chartPointsTeamDistribution", "Punkte nach Tendenz", [
   				{ name: 'Heim: ', y: rc.selectedRank.rank.pointsWithHomeTeam },
   				{ name: 'Gast',   y: rc.selectedRank.rank.pointsWithAwayTeam },
   				{ name: 'Remis',  y: rc.selectedRank.rank.pointsWithDraw }
   			]);

			// create charts - result distribution
			rc.createPieChart("#chartResultDistribution", "Bulibot Ergebnisse", [
 				{ name: 'Ergebnis: ',   y: rc.selectedRank.rank.resultsWithExactHit },
 				{ name: 'Tordifferenz', y: rc.selectedRank.rank.resultsWithRelativeHit },
 				{ name: 'Tendenz',      y: rc.selectedRank.rank.resultsWithWinnerHit },
 				{ name: 'Falsch',       y: rc.selectedRank.rank.resultsWithNoHit },
 				{ name: 'Fehler',       y: rc.selectedRank.rank.resultsWithError }
 			]);
			rc.createPieChart("#chartPointsResultDistribution", "Punkte nach Ergebnis", [
 				{ name: 'Ergebnis: ',   y: rc.selectedRank.rank.pointsWithExactHit },
 				{ name: 'Tordifferenz', y: rc.selectedRank.rank.pointsWithRelativeHit },
 				{ name: 'Tendenz',      y: rc.selectedRank.rank.pointsWithWinnerHit }
 			]);
		};
		
		rc.createPieChart = function(target, title, data) {
			$(target).highcharts({
			    
				title: {
					text: title
				},
				
				series: [{
			        type: 'pie',
			        data: data
			    }],
			    
			    plotOptions: {
			        pie: {
			            dataLabels: {
			                enabled: true,
			                formatter: function() { 
			                	return this.y && this.y > 0 ? '<b>' + this.point.name + '</b>: ' + parseFloat(Math.round(this.point.percentage * 100) / 100).toFixed(2) + '%' : null;
			                },
			                style: {
			                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
			                },
			                connectorColor: 'silver'
			            },
			            tooltip: {
			            	pointFormat: '{point.y}'
			            }
			        }
			    },

			    responsive: {
                   rules: [{
                       condition: {
                           maxWidth: 1024
                       }
                   }]
                },

			    exporting: { enabled: false }
			});
		};
		
		rc.selectedBulibotExecution = null;
		rc.selectBulibotExecution = function(bulibotExecution) {
			rc.selectedBulibotExecution = bulibotExecution;
		};
	}]);
})();