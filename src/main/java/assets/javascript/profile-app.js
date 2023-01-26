(function(){
	app = angular.module('bulibot-profile', ['bulibot']);
	app.controller('ProfileController', ['$http', '$timeout', function($http, $timeout) {
		var apiBaseUrl = '/api/v1/data';
		var pc = this;
		
		// messages
		pc.messages = {};
		pc.messages["exportExcutionsChanged"] = "Die Export-Einstellungen wurden geändert.";
		pc.messages["exportExcutionsMalformedUrl"] = "Die Export-URL ist ungültig.";
		pc.messages["exportExcutionsTestSuccessful"] = "Die Testdaten wurden erfolgreich gesendet.";
		pc.messages["nameChanged"] = "Der Benutzername wurde geändert.";
		pc.messages["bulibotNameChanged"] = "Der Bulibot Name wurde geändert.";
		pc.messages["passwordInvalid"] = "Das aktuelle Passwort konnte nicht verifiziert werden!";
		pc.messages["passwordsMismatch"] = "Das neue Passwort und die Wiederholung stimmen nicht überein!";
		pc.messages["passwordChanged"] = "Das Passwort wurde geändert.";
		pc.clearMessageTimerBegin = function() {
			$timeout(function() {
				pc.changeJsonExportExecutionsError = null;
				pc.changeJsonExportExecutionsMessage = null;
				pc.changeSlackExportExecutionsError = null;
				pc.changeSlackExportExecutionsMessage = null;
				pc.changeNameError = null;
				pc.changeNameMessage = null;
				pc.changeBulibotNameError = null;
				pc.changeBulibotNameMessage = null;
				pc.changePasswordError = null;
				pc.changePasswordMessage = null;
				pc.deleteUserError = null;
			}, 3000);
		};
		
		// change json executions export
		pc.changeJsonExportExecutionsEnabled = false;
		pc.changeJsonExportExecutionsData = {};
		pc.testJsonExportExecutionsData = null;
		pc.changeJsonExportExecutionsError = null;
		pc.changeJsonExportExecutionsMessage = null;
		pc.toggleJsonExportResults = function() {
			pc.changeJsonExportExecutionsData.enabled = !pc.changeJsonExportExecutionsData.enabled;
		};
		pc.changeJsonExportExecutions = function() {
			pc.changeJsonExportExecutionsWithCallback(null);
		};
		pc.testJsonExportExecutions = function() {
			testCallback = function() {
				$http.get(apiBaseUrl + '/user/jsonExportExecutions').success(function(result) {
					pc.changeJsonExportExecutionsError = null;
					pc.changeJsonExportExecutionsMessage = null;
					pc.testJsonExportExecutionsData = result.payload;
					if(result.valid) {
						pc.changeJsonExportExecutionsMessage = pc.messages[result.message];
					} else {
						pc.changeJsonExportExecutionsError = result.message;
					}
					pc.clearMessageTimerBegin();
				});				
			};
			pc.changeJsonExportExecutionsWithCallback(testCallback);
		};
		pc.changeJsonExportExecutionsWithCallback = function(saveCallback) {
			pc.testJsonExportExecutionsData = null;
			$http.post(apiBaseUrl + '/user/jsonExportExecutions', pc.changeJsonExportExecutionsData).success(function(result) {
				if(result.valid) {
					pc.user.jsonExportBulibotResultsUrlEnabled = pc.changeJsonExportExecutionsData.enabled;
					pc.user.jsonExportBulibotResultsUrl = pc.changeJsonExportExecutionsData.url;
					pc.changeJsonExportExecutionsError = null;
					pc.changeJsonExportExecutionsMessage = pc.messages[result.message];
					if(saveCallback != null) {
						saveCallback();
					}
					pc.changeJsonExportExecutionsEnabled = false;
				} else {
					pc.changeJsonExportExecutionsError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		pc.dropTestJsonExportExecutionsData = function() {
			pc.testJsonExportExecutionsData = null;
		}
		pc.toggleChangeJsonExportExecutions = function() {
			pc.changeJsonExportExecutionsEnabled = !pc.changeJsonExportExecutionsEnabled;
		}
		
		// change slack executions export
		pc.changeSlackExportExecutionsEnabled = false;
		pc.changeSlackExportExecutionsData = {};
		pc.testSlackExportExecutionsData = null;
		pc.changeSlackExportExecutionsError = null;
		pc.changeSlackExportExecutionsMessage = null;
		pc.toggleSlackExportResults = function() {
			pc.changeSlackExportExecutionsData.enabled = !pc.changeSlackExportExecutionsData.enabled;
		};
		pc.changeSlackExportExecutions = function() {
			pc.changeSlackExportExecutionsWithCallback(null);
		};
		pc.testSlackExportExecutions = function() {
			testCallback = function() {
				$http.get(apiBaseUrl + '/user/slackExportExecutions').success(function(result) {
					pc.changeSlackExportExecutionsError = null;
					pc.changeSlackExportExecutionsMessage = null;
					pc.testSlackExportExecutionsData = result.payload;
					if(result.valid) {
						pc.changeSlackExportExecutionsMessage = pc.messages[result.message];
					} else {
						pc.changeSlackExportExecutionsError = result.message;
					}
					pc.clearMessageTimerBegin();
				});				
			};
			pc.changeSlackExportExecutionsWithCallback(testCallback);
		};
		pc.changeSlackExportExecutionsWithCallback = function(saveCallback) {
			pc.testSlackExportExecutionsData = null;
			$http.post(apiBaseUrl + '/user/slackExportExecutions', pc.changeSlackExportExecutionsData).success(function(result) {
				if(result.valid) {
					pc.user.slackExportBulibotResultsUrlEnabled = pc.changeSlackExportExecutionsData.enabled;
					pc.user.slackExportBulibotResultsUrl = pc.changeSlackExportExecutionsData.url;
					pc.changeSlackExportExecutionsError = null;
					pc.changeSlackExportExecutionsMessage = pc.messages[result.message];
					if(saveCallback != null) {
						saveCallback();
					}
					pc.changeSlackExportExecutionsEnabled = false;
				} else {
					pc.changeSlackExportExecutionsError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		pc.dropTestSlackExportExecutionsData = function() {
			pc.testSlackExportExecutionsData = null;
		}
		pc.toggleChangeSlackExportExecutions = function() {
			pc.changeSlackExportExecutionsEnabled = !pc.changeSlackExportExecutionsEnabled;
		}
		
		// change user name
		pc.changeNameEnabled = false;
		pc.changeNameData = {};
		pc.changeNameError = null;
		pc.changeNameMessage = null;
		pc.changeName = function() {
			$http.post(apiBaseUrl + '/user/name', pc.changeNameData).success(function(result) {
				if(result.valid) {
					pc.user.name = pc.changeNameData.name;
					pc.changeNameError = null;
					pc.changeNameMessage = pc.messages[result.message];
					pc.changeNameEnabled = false;
				} else {
					pc.changeNameError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		pc.toggleChangeName = function() {
			pc.changeNameEnabled = !pc.changeNameEnabled;
			if(!pc.changeNameEnabled) {
				pc.changeNameData.name = pc.user.name;
			}
		}
		
		// change bulibot name
		pc.changeBulibotNameEnabled = false;
		pc.changeBulibotNameData = {};
		pc.changeBulibotNameError = null;
		pc.changeBulibotNameMessage = null;
		pc.changeBulibotName = function() {
			$http.post(apiBaseUrl + '/user/bulibotName', pc.changeBulibotNameData).success(function(result) {
				if(result.valid) {
					pc.user.bulibotName = pc.changeBulibotNameData.bulibotName;
					pc.changeBulibotNameError = null;
					pc.changeBulibotNameMessage = pc.messages[result.message];
					pc.changeBulibotNameEnabled = false;
				} else {
					pc.changeBulibotNameError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		pc.toggleChangeBulibotName = function() {
			pc.changeBulibotNameEnabled = !pc.changeBulibotNameEnabled;
			if(!pc.changeBulibotNameEnabled) {
				pc.changeBulibotNameData.bulibotName = pc.user.bulibotName;
			}
		}
		
		// change password
		pc.changePasswordEnabled = false;
		pc.changePasswordData = {};
		pc.changePasswordError = null;
		pc.changePasswordMessage = null;
		pc.changePassword = function() {
			$http.post(apiBaseUrl + '/user/password', pc.changePasswordData).success(function(result) {
				if(result.valid) {
					pc.changePasswordData = {};
					pc.changePasswordError = null;
					pc.changePasswordMessage = pc.messages[result.message];
				} else {
					pc.changePasswordError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		pc.toggleChangePassword = function() {
			pc.changePasswordEnabled = !pc.changePasswordEnabled;
		}
		
		// delete user
		pc.deleteUserData = {};
		pc.deleteUserError = null;
		pc.deleteUser = function() {
			$http.post(apiBaseUrl + '/user/delete', pc.deleteUserData).success(function(result) {
				if(result.valid) {
					pc.deleteUserData = {};
					pc.deleteUserError = null;
					location.reload(); 
				} else {
					pc.deleteUserError = pc.messages[result.message];
				}
				pc.clearMessageTimerBegin();
			});
		};
		
		// user
		pc.user = null;
		pc.loadUser = function() {
			$http.get(apiBaseUrl + '/user').success(function(user) {
				pc.user = user;
				pc.changeNameData.name = pc.user.name;
				pc.changeBulibotNameData.bulibotName = pc.user.bulibotName;
				pc.changeJsonExportExecutionsData.enabled = pc.user.jsonExportBulibotResultsUrlEnabled;
				pc.changeJsonExportExecutionsData.url = pc.user.jsonExportBulibotResultsUrl;
				pc.changeSlackExportExecutionsData.enabled = pc.user.slackExportBulibotResultsUrlEnabled;
				pc.changeSlackExportExecutionsData.url = pc.user.slackExportBulibotResultsUrl;
			});
		};
		pc.loadUser();
	}]);
})();