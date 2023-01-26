(function(){
	app = angular.module('bulibot-persistence', ['bulibot']);
	app.controller('PersistenceController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/system';
		var pc = this;

		pc.configureHighcharts = function() {

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
		pc.configureHighcharts();
		
		pc.showCharts = false;
		pc.toggleShowCharts = function(){
			pc.showCharts = !pc.showCharts;
		};
		
		pc.buildStorageChart = function() {
			
			// prepare data
			sizeSeriesData = [];
			itemsSeriesData = [];
			for(metric in pc.storage.metrics) {
				metricData = pc.storage.metrics[metric];
				sizeSeriesData.push({name: metricData.type, y: metricData.fileSize});
				itemsSeriesData .push({name: metricData.type, y: metricData.itemCount});
			}
			
			// create charts
			pc.createPieChart('#storageChartFileSize', 'Speicher', sizeSeriesData);
			pc.createPieChart('#storageChartItems', 'Objekte', itemsSeriesData);
		};
		pc.createPieChart = function(target, title, seriesData) {
			$(target).highcharts({
			    
				title: {
					text: title
				},
				
			    series: [{ type: 'pie', data: seriesData }],
			    
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

			    chart: { width: 1024 },
			    exporting: { enabled: false }
			});
		};
		
		// storage
		pc.storage = null;
		pc.loadStorage = function() {
			$http.get(apiBaseUrl + '/storage').success(function(storage) {
				pc.storage = storage;
				pc.buildStorageChart();
			});
		};
		pc.loadStorage();
	}]);
})();