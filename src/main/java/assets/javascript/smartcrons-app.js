(function(){
	app = angular.module('bulibot-smartcrons', ['bulibot']);
	app.controller('SmartcronsController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/system';
		var sc = this;
		
		sc.configureHighcharts = function() {
			Highcharts.setOptions({
			    global : {
			        useUTC : false
			    }
			});
		};
		sc.configureHighcharts();
		
		sc.showCharts = false;
		sc.toggleShowCharts = function(){
			sc.showCharts = !sc.showCharts;
		};
		
		// smartcrons
		sc.smartcron = null;
		sc.selectSmartcron = function(smartcron) {
			sc.smartcron = smartcron;
			sc.showCharts = false;
			if(sc.smartcron != null) {
				sc.buildSmartcronChart();
			}
		}
		
		sc.buildSmartcronChart = function() {
			
			// prepare data
			durationSeriesData = [];
			for(execution of sc.smartcron.history) {
				started = moment(
					execution.started.year + "-" + (execution.started.monthValue < 10 ? '0' : '') + execution.started.monthValue + "-" + execution.started.dayOfMonth + ' ' +
					(execution.started.hour < 10 ? '0' : '') + execution.started.hour + ':' + (execution.started.minute < 10 ? '0' : '') + execution.started.minute + ':' + (execution.started.second < 10 ? '0' : '') + execution.started.second);
				durationSeriesData.push({x: started.toDate().getTime(), y: execution.duration});
			}
			sortFunc = function(a, b) { return a.x - b.x };
			durationSeriesData = durationSeriesData.sort(sortFunc);
			
			// create chart
			$('#smartcronChart').highcharts({
			    
				title: {
					text: 'Laufzeiten'
				},
				
				xAxis: {
			        type: 'datetime'
			    },
			    
			    yAxis: { 
			    	title: { text: 'ms' }
			    },
			        
				series: [{
			        type: 'areaspline',
			        name: 'Laufzeit',
			        data: durationSeriesData
			    }],
			    
			    chart: { width: 832 },
			    exporting: { enabled: false }
			});
		};
		
		sc.smartcrons = [];
		sc.loadSmartcrons = function() {
			$http.get(apiBaseUrl + '/smartcrons').success(function(smartcrons) {
				sc.smartcrons = smartcrons;
			});
		};
		sc.loadSmartcrons();
		
		sc.executeSmartcron = function(smartcron) {
			$http.post(apiBaseUrl + '/smartcrons/' + smartcron.name, {'execute' : true}).success(function(smartcrons) {
				sc.loadSmartcrons();
			});
		};
		
		sc.toggleSmartcron = function(smartcron) {
			$http.post(apiBaseUrl + '/smartcrons/' + smartcron.name, {'enable' : !smartcron.active}).success(function(smartcrons) {
				sc.loadSmartcrons();
			});
		};
	}]);
})();