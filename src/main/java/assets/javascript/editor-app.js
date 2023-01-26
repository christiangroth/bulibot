(function(){
	app = angular.module('bulibot-editor', ['bulibot']);
	app.controller('EditorController', ['$http', '$timeout', function($http, $timeout) {
		var apiBaseUrl = '/api/v1/data';
		var ec = this;
		
		// get configured bulibot execution threshold
		ec.bulibotConfig = {};
		ec.loadBulibotConfiguration = function() {
			$http.get(apiBaseUrl + '/bulibotConfig').success(function(result) {
				ec.bulibotConfig = result;
			});
		};
		ec.loadBulibotConfiguration();
		
		// auto save timer handling
		ec.bublibotEditorAutoSave = false;
		ec.bublibotEditorAutoSaveTimer = 0;
		ec.bublibotEditorAutoSaveTimerTicks = 0;
		ec.autoSaveTimerTickSchedule = function() {
			ec.bublibotEditorAutoSaveTimer = $timeout(function() {
				ec.autoSaveTimerAbort();
				ec.autoSaveTimerTick();
			}, 1000);
		};
		ec.autoSaveTimerBegin = function() {
			if(ec.bublibotEditorAutoSave) {
				ec.bublibotEditorAutoSaveTimerTicks = 30;
				ec.autoSaveTimerTickSchedule();
			}
		};
		ec.autoSaveTimerTick = function() {
			ec.bublibotEditorAutoSaveTimerTicks--;
			if(!ec.bublibotEditorAutoSave) {
				ec.autoSaveTimerEnd();
			} else {
				if(ec.bublibotEditorAutoSaveTimerTicks < 1) {
					ec.save();
				} else {
					ec.autoSaveTimerTickSchedule();
				}
			}
		};
		ec.autoSaveTimerAbort = function() {
			$timeout.cancel(ec.bublibotEditorAutoSaveTimer);
		};
		ec.autoSaveTimerEnd = function() {
			ec.autoSaveTimerAbort();
			ec.bublibotEditorAutoSaveTimerTicks = 0;
		};
		
		// messages
		ec.error = null;
		ec.message = null;
		ec.messages = {};
		ec.messages["strategy.lastMatchday"] = "Letzter Spieltag";
		ec.messages["strategy.lastThreeMatchdays"] = "Letzte 3 Spieltage";
		ec.messages["strategy.lastFiveMatchdays"] = "Letzte 5 Spieltage";
		ec.messages["strategy.lastTenMatchdays"] = "Letzte 10 Spieltage";
		ec.messages["strategy.currentSeason"] = "Aktuelle Saison";
		ec.messages["strategy.lastSeason"] = "Letzte Saison";
		ec.messages["strategy.seasonBeforeLast"] = "Vorletzte Saison";
		ec.messages["bulibotSaved"] = "Gespeichert.";
		ec.messages["bulibotSetLive"] = "Live-Version gespeichert.";
		ec.messages["bulibotDeleted"] = "Gelöscht.";
		ec.messages["bulibotNotFound"] = "Fehlgeschlagen!";
		ec.messages["bulibotNameAlreadyExists"] = "Der Name ist bereits vergeben!";
		ec.messages["bulibotVersionCanNotBeDeleted"] = "Die Version konnte nicht gelöscht werden!";
		ec.messages["bulibotLiveVersionMustNotBeDeleted"] = "Die Live-Version darf nicht gelöscht werden!";
		ec.clearMessageTimerBegin = function() {
			$timeout(function() {
				ec.error = null;
				ec.message = null;
			}, 3000);
		};
		
		// set live
		ec.setLive = function() {
			$http.get(apiBaseUrl + '/bulibot/' + ec.bulibot.name + '/setLive').success(function(result) {
				if(result.valid) {
					ec.error = null;
					ec.message = ec.messages[result.message];
					for(bulibot of ec.bulibots) {
						bulibot.live = false;
					}					
					ec.bulibot.live = true;
				} else {
					ec.error = ec.messages[result.message];
				}
				ec.clearMessageTimerBegin();
			});
		};
		
		// delete bulibot
		ec.deleting = false;
		ec.startDelete = function() {
			ec.deleting = true;
		};
		ec.abortDelete = function() {
			ec.deleting = false;
		};
		ec.saveDelete = function() {
			$http.delete(apiBaseUrl + '/bulibot/' + ec.bulibot.name, ec.nameData).success(function(result) {
				if(result.valid) {
					ec.error = null;
					ec.message = ec.messages[result.message];
					ec.updateBulibots();
				} else {
					ec.error = ec.messages[result.message];
					ec.deleting = false;
				}
				ec.clearMessageTimerBegin();
			});
		};
		
		// rename & duplicate bulibot
		ec.nameData = {};
		ec.renaming = false;
		ec.duplicating = false;
		ec.startRename = function() {
			ec.nameData.name = ec.bulibot.name;
			ec.renaming = true;
		};
		ec.abortRename = function() {
			ec.renaming = false;
		};
		ec.saveRename = function() {
			$http.post(apiBaseUrl + '/bulibot/' + ec.bulibot.name +'/rename', ec.nameData).success(function(result) {
				if(result.valid) {
					ec.error = null;
					ec.message = ec.messages[result.message];
					ec.updateBulibots();
				} else {
					ec.error = ec.messages[result.message];
					ec.renaming = false;
				}
				ec.clearMessageTimerBegin();
			});
		};
		ec.startDuplicate = function() {
			ec.nameData.name = ec.bulibot.name;
			ec.duplicating = true;
		};
		ec.abortDuplicate = function() {
			ec.duplicating = false;
		};
		ec.saveDuplicate = function() {
			$http.post(apiBaseUrl + '/bulibot/' + ec.bulibot.name +'/copy', ec.nameData).success(function(result) {
				if(result.valid) {
					ec.error = null;
					ec.message = ec.messages[result.message];
					ec.updateBulibots();
				} else {
					ec.error = ec.messages[result.message];
					ec.duplicating = false;
				}
				ec.clearMessageTimerBegin();
			});
		};
		ec.updateBulibots = function() {
			$http.get(apiBaseUrl + '/bulibots').success(function(bulibots) {
				ec.bulibots = bulibots;
				if(ec.renaming || ec.duplicating) {
					ec.selectBulibot(ec.nameData.name);
					ec.renaming = false;
					ec.duplicating = false;
				} else if(ec.deleting) {
					ec.selectBulibot(null);
					ec.deleting = false;
				}
			});
		};
		ec.selectBulibot = function(name) {
			selectLiveVersion = name == null;
			for(bulibot of ec.bulibots) {
				if((selectLiveVersion && bulibot.live) || (!selectLiveVersion && name == bulibot.name)) {
					ec.bulibot = bulibot;
					autoSaveEnabled = ec.bublibotEditorAutoSave;
					if(autoSaveEnabled) {
						ec.bublibotEditorAutoSave = false;
					}
					ec.editorSourceUpdate(ec.bulibot.source);
					if(autoSaveEnabled) {
						ec.bublibotEditorAutoSave = true;
					}
				}
			}
		};
		
		// code mirror integration
		ec.editorHide = function() {
			$(bublibotEditor.getWrapperElement()).hide();
		};
		ec.editorShow = function() {
			$(bublibotEditor.getWrapperElement()).show();
		};
		ec.editorSource = function() {
			return bublibotEditor.getDoc().getValue();
		};
		ec.editorSourceUpdate = function(source) {
			bublibotEditor.getDoc().setValue(source);
		};
		ec.editorCallback = function(event, callback) {
			bublibotEditor.on(event, callback);
		};
		
		// test bulibot
		ec.testing = false;
		ec.testStrategy = null;
		ec.testResult = null;
		ec.showTestResultCharts = false;
		ec.testResultBulibotExecution = null
		ec.saveAndTest = function(strategy) {
			ec.testStrategy = strategy;
			ec.testing = true;
			ec.editorHide();
			testCallback = function() {
				$http.get(apiBaseUrl + '/bulibot/' + ec.bulibot.name + '/test/' + ec.testStrategy).success(function(result) {
					if(result.valid) {
						ec.error = null;
						if(result.message != null) {
							ec.message = ec.messages[result.message];
						}
						ec.testResult = result.payload;
						ec.buildTestResultCharts();
					} else {
						ec.error = tc.messages[result.message];
					}
				});				
			};
			ec.saveWithCallback(testCallback);
		};
		ec.buildTestResultCharts = function() {
			
			// ensure data
			if(!ec.testResult || !ec.testResult.matchdayResults || ec.testResult.matchdayResults.length < 1) {
				return;
			}
			
			// prepare data
			points = 0;
			pointsSeriesData = [];
			pointsPerMatchdaySeriesData = [];
			exact = 0;
			exactSeriesData = [];
			exactPerMatchdaySeriesData = [];
			relative = 0;
			relativeSeriesData = [];
			relativePerMatchdaySeriesData = [];
			winner = 0;
			winnerSeriesData = [];
			winnerPerMatchdaySeriesData = [];
			wrong = 0;
			wrongSeriesData = [];
			wrongPerMatchdaySeriesData = [];
			error = 0;
			errorSeriesData = [];
			errorPerMatchdaySeriesData = [];
			for(i = ec.testResult.matchdayResults.length - 1; i >= 0; i--) {
				matchdayResult = ec.testResult.matchdayResults[i];
				
				// points
				points += matchdayResult.points;
				pointsSeriesData.push({x: matchdayResult.matchday, y: points});
				pointsPerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.points});

				// exact
				exact += matchdayResult.resultsWithExactHit;
				exactSeriesData.push({x: matchdayResult.matchday, y: exact});
				exactPerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.resultsWithExactHit});
				
				// relative
				relative += matchdayResult.resultsWithRelativeHit;
				relativeSeriesData.push({x: matchdayResult.matchday, y: relative});
				relativePerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.resultsWithRelativeHit});
				
				// winner
				winner += matchdayResult.resultsWithWinnerHit;
				winnerSeriesData.push({x: matchdayResult.matchday, y: winner});
				winnerPerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.resultsWithWinnerHit});
				
				// wrong
				wrong += matchdayResult.resultsWithNoHit;
				wrongSeriesData.push({x: matchdayResult.matchday, y: wrong});
				wrongPerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.resultsWithNoHit});
				
				// error
				error += matchdayResult.resultsWithError;
				errorSeriesData.push({x: matchdayResult.matchday, y: error});
				errorPerMatchdaySeriesData.push({x: matchdayResult.matchday, y: matchdayResult.resultsWithError});
			}
			
			// build charts
			ec.buildTestResultChart(pointsSeriesData, exactSeriesData, relativeSeriesData, winnerSeriesData, wrongSeriesData, errorSeriesData);
			ec.buildTestResultChartPerMatchday(pointsPerMatchdaySeriesData, exactPerMatchdaySeriesData, relativePerMatchdaySeriesData, winnerPerMatchdaySeriesData, wrongPerMatchdaySeriesData, errorPerMatchdaySeriesData);
		};
		ec.buildTestResultChart = function(pointsSeriesData, exactSeriesData, relativeSeriesData, winnerSeriesData, wrongSeriesData, errorSeriesData) {
			
			// prepare chart data
			seriesData = [];
			seriesData.push({
				type: 'spline',
				name: 'Punkte',
				data: pointsSeriesData
			});
			seriesData.push({
				type: 'spline',
				name: 'Ergebnis',
				data: exactSeriesData
			});
			seriesData.push({
				type: 'spline',
				name: 'Tordifferenz',
				data: relativeSeriesData
			});
			seriesData.push({
				type: 'spline',
				name: 'Tendenz',
				data: winnerSeriesData
			});
			seriesData.push({
				type: 'spline',
				name: 'Falsch',
				data: wrongSeriesData
			});
			seriesData.push({
				type: 'spline',
				name: 'Fehler',
				data: errorSeriesData
			});
			
			// create chart
			$('#testResultChart').highcharts({
			    
				title: {
					text: 'Verlauf'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'}
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Punkte / Anzahl'}
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
		ec.buildTestResultChartPerMatchday = function(pointsSeriesData, exactSeriesData, relativeSeriesData, winnerSeriesData, wrongSeriesData, errorSeriesData) {
			
			// prepare chart data
			seriesData = [];
			seriesData.push({
				type: 'column',
				name: 'Punkte',
				data: pointsSeriesData
			});
			seriesData.push({
				type: 'column',
				name: 'Ergebnis',
				data: exactSeriesData
			});
			seriesData.push({
				type: 'column',
				name: 'Tordifferenz',
				data: relativeSeriesData
			});
			seriesData.push({
				type: 'column',
				name: 'Tendenz',
				data: winnerSeriesData
			});
			seriesData.push({
				type: 'column',
				name: 'Falsch',
				data: wrongSeriesData
			});
			seriesData.push({
				type: 'column',
				name: 'Fehler',
				data: errorSeriesData
			});
			
			// create chart
			$('#testResultChartPerMatchday').highcharts({
				
				title: {
					text: 'pro Spieltag'
				},
				
				xAxis: {
					allowDecimals: false,
					title: {text: 'Spieltag'}
				},
				
				yAxis: {
					allowDecimals: false,
					title: {text: 'Punkte / Anzahl'}
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
		ec.selectBulibotExecution = function(testResultBulibotExecution) {
			ec.testResultBulibotExecution = testResultBulibotExecution;
		};
		ec.toggleTestResultCharts = function(){
			ec.showTestResultCharts = !ec.showTestResultCharts;
		};
		ec.endTesting = function() {
			ec.testResultBulibotExecution = null
			ec.testResult = null;
			ec.testStrategy = null;
			ec.testing = false;
			ec.editorShow();
		};
		
		// save bulibot
		ec.save = function() {
			ec.saveWithCallback(null);
		};
		ec.saveWithCallback = function(saveCallback) {
			ec.autoSaveTimerEnd();
			ec.bulibot.source = ec.editorSource();
			$http.post(apiBaseUrl + '/bulibot/' + ec.bulibot.name, ec.bulibot).success(function(result) {
				if(result.valid) {
					ec.error = null;
					if(saveCallback != null) {
						saveCallback();
					} else {
						ec.message = ec.messages[result.message];
					}
				} else {
					ec.error = ec.messages[result.message];
					if(saveCallback != null){
						ec.editorShow();
					}
				}
				if(saveCallback == null) {
					ec.clearMessageTimerBegin();
				}
			});
		};
		
		// load teams
		ec.teams = {};
		ec.loadTeams = function() {
			$http.get(apiBaseUrl + '/teams').success(function(teams) {
				for (team of teams) {
					ec.teams[team.id] = team; 
				}
			});
		};
		ec.loadTeams();

		// load bulibot data
		ec.bulibot = null;
		ec.bulibots = [];
		ec.loadBulibots = function() {
			$http.get(apiBaseUrl + '/bulibots').success(function(bulibots) {
				ec.bulibots = bulibots;
				for(bulibot of ec.bulibots) {
					if(bulibot.live) {
						ec.bulibot = bulibot;
					}
				}
				if(ec.bulibot != null) {
					ec.editorSourceUpdate(ec.bulibot.source);
				}
				ec.editorCallback('change', function(editor) {
					ec.autoSaveTimerBegin();
				});
			});
		};
		ec.loadBulibots();
		
		// testdata strategies
		ec.strategies = [];
		ec.loadStrategies = function() {
			$http.get(apiBaseUrl + '/bulibot/test').success(function(result) {
				if(result.valid) {
					ec.error = null;
					if(result.message != null) {
						ec.message = ec.messages[result.message];
					}
					ec.strategies = result.payload;
				} else {
					ec.error = ec.messages[result.message];
				}
			});
		};
		ec.loadStrategies();
	}]);
})();