(function(){
	app = angular.module('bulibot-users', ['bulibot']);
	app.controller('UserController', ['$http', '$timeout', function($http, $timeout) {
		var apiBaseUrl = '/api/v1/data';
		var apiSystemUrl = '/api/v1/system';
		var uc = this;
		
		// messages
		uc.messages = {};
		uc.messages["inviteNameMissing"] = "Bitte gib einen Namen an!";
		uc.messages["inviteEmailMissing"] = "Bitte gib eine E-Mail Adresse an!";
		uc.messages["registrationUserExists"] = "Die eingegebene E-Mail Adresse ist bereits registriert!";
		uc.messages["registrationEmailFailed"] = "Es konnte kein Code gesendet werden, bitte versuche es später nochmal!";
		uc.messages["userNotFound"] = "Die Benutzerdaten konnten nicht aktualisiert werden!";
		uc.messages["loginPasswordResetEmailFailed"] = "Das Passwort konnte nicht neu gesendet werden!";
		uc.clearMessageTimerBegin = function() {
			$timeout(function() {
				uc.usersError = null;
				uc.usersMessage = null;
			}, 3000);
		};

		// user
		uc.user = null;
		uc.loadUser = function() {
			$http.get(apiBaseUrl + '/user').success(function(user) {
				uc.user = user;
			});
		};
		uc.loadUser();
		
		// users
		uc.users = [];
		uc.loadUsers = function() {
			$http.get(apiBaseUrl + '/users').success(function(users) {
				uc.users = users;
			});
			uc.inUserAction = false;
			uc.selectedUser = null;
		};
		uc.loadUsers();

		// invite
		uc.inviteData = {};
		uc.inviteError = null;
		uc.inviteMessage = null;
		uc.invite = function() {
			$http.post(apiBaseUrl + '/user/invite', uc.inviteData).success(function(result) {
				if(result.valid) {
					uc.inviteData = {};
					uc.inviteError = null;
					uc.inviteMessage = uc.messages[result.message];
					uc.loadUsers();
				} else {
					uc.inviteError = uc.messages[result.message];
				}
				uc.clearMessageTimerBegin();
			});
		};
		
		// edit users
		uc.usersError = null;
		uc.usersMessage = null;
		uc.lock = function(user) {
			$http.post(apiBaseUrl + '/user/' + user.id + '/flags', uc.createUserFlags(true, user.admin)).success(function(result) {
				uc.handleUsersResult(result);
			});
		};
		uc.unlock = function(user) {
			$http.post(apiBaseUrl + '/user/' + user.id + '/flags', uc.createUserFlags(false, user.admin)).success(function(result) {
				uc.handleUsersResult(result);
			});
		};
		uc.admin = function(user) {
			$http.post(apiBaseUrl + '/user/' + user.id + '/flags', uc.createUserFlags(user.locked, true)).success(function(result) {
				uc.handleUsersResult(result);
			});
		};
		uc.noAdmin = function(user) {
			$http.post(apiBaseUrl + '/user/' + user.id + '/flags', uc.createUserFlags(user.locked, false)).success(function(result) {
				uc.handleUsersResult(result);
			});
		};
		uc.createUserFlags = function(locked, admin) {
			return {"locked" : locked, "admin" : admin};
		};
		
		uc.inUserAction = false;
		uc.userActionCode = null;
		uc.userActionText = null;
		uc.selectedUser = null;
		uc.beginUserAction = function(user, code, text){
			uc.inUserAction = true;
			uc.selectedUser = user;
			uc.userActionCode = code;
			uc.userActionText = text;
		};
		uc.applyUserAction = function(){
			if(uc.userActionCode == 'pwreset') {
				$http.get(apiBaseUrl + '/user/' + uc.selectedUser.id + '/pwreset').success(function(result) {
					uc.handleUsersResult(result);
				});
			} else if(uc.userActionCode == 'delete') {
				$http.delete(apiBaseUrl + '/user/' + uc.selectedUser.id).success(function(result) {
					uc.handleUsersResult(result);
				});
			}
		};
		uc.abortUserAction = function(){
			uc.userActionText = null;
			uc.userActionCode = null;
			uc.selectedUser = null;
			uc.inUserAction = false;
		};
		
		uc.handleUsersResult = function(result) {
			if(result.valid) {
				uc.usersMessage = uc.messages[result.message];
				uc.loadUsers();
			} else {
				uc.usersError = uc.messages[result.message];
			}
			uc.clearMessageTimerBegin();
		};
	}]);
})();