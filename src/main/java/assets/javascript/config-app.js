(function(){
	app = angular.module('bulibot-config', ['bulibot']);
	app.controller('ConfigController', ['$http', function($http) {
		var apiBaseUrl = '/api/v1/system';
		var cc = this;
		
		// messages
		cc.messages = {};
		cc.messages["unknownGroup"] = "Gruppe unbekannt!";
		cc.messages["unknownProperty"] = "Property unbekannt!";
		cc.messages["valueMandatory"] = "Wert notwendig!";
		cc.messages["valueInvalid"] = "Wert ungültig!";
		
		// config
		cc.config = null;
		cc.loadConfig = function() {
			$http.get(apiBaseUrl + '/config').success(function(config) {
				cc.config = config;
			});
		};
		cc.loadConfig();
		
		cc.selectedPropertyGroup = null;
		cc.selectedPropertyKey = null;
		cc.selectedPropertyValue = null;
		cc.selectedPropertyError = null;
		cc.selectProperty = function(group, property) {
			cc.selectedPropertyError = null;
			cc.selectedPropertyGroup = group;
			cc.selectedPropertyValue = property.value;
			cc.selectedPropertyKey = property.key;
		};
		
		cc.saveSelectedProperty = function() {
			$http.post(apiBaseUrl + '/config/' + cc.selectedPropertyGroup + '/' + cc.selectedPropertyKey, cc.selectedPropertyValue).success(function(result) {
				if(result.valid){
					cc.selectedPropertyError = null;
					cc.discardSelectedProperty();
					cc.loadConfig();
				} else {
					cc.selectedPropertyError = cc.messages[result.message];
				}
			});
		}
		
		cc.discardSelectedProperty = function() {
			cc.selectedPropertyError = null;
			cc.selectedPropertyGroup = null;
			cc.selectedPropertyKey = null;
			cc.selectedPropertyValue = null;
		}
	}]);
})();