(function(){
	app = angular.module('bulibot-authentication', ['bulibot']);
	app.controller('AuthenticationController', ['$http', '$timeout', function($http, $timeout) {
		var apiBaseUrl = '/api/v1';
		var ac = this;
		
		// messages
		ac.messages = {};
		ac.messages["registrationDisabled"] = "Weitere Anmeldungen sind aktuell nicht möglch!";
		ac.messages["registrationNameMissing"] = "Bitte gib einen Namen an!";
		ac.messages["registrationPasswordMismatch"] = "Die eingegebenen Passwörter stimmen nicht überein!";
		ac.messages["registrationUserExists"] = "Die eingegebene E-Mail Adresse ist bereits vergeben!";
		ac.messages["registrationSuccess"] = "Die Registrierung war erfolgreich. Bitte bestätige deine E-Mail Adresse mit dem dir in der E-Mail zugesandten Link.";
		ac.messages["registrationEmailFailed"] = "Es konnte kein Code gesendet werden, bitte versuche es später nochmal!";
		ac.messages["registrationResendEmailFailed"] = "Es konnte kein neuer Code gesendet werden!";
		ac.messages["registrationResendEmail"] = "Es wurde ein neuer Code an deine E-Mail Adresse gesendet.";
		ac.messages["verificationFailed"] = "Deine E-Mail Adresse konnte nicht verifiziert werden!";
		ac.messages["verificationSuccess"] = "Deine E-Mail Adresse wurde erfolgreich verifiziert, du kannst dich nun einloggen.";
		ac.messages["loginNotVerified"] = "Deine E-Mail Adresse ist noch nicht verifiziert, bitte bestätige diese zunächst!";
		ac.messages["loginLocked"] = "Dein Login ist aktuell gesperrt!";
		ac.messages["loginFailed"] = "Die Kombination aus E-Mail Adresse und Passwort ist nicht korrekt!";
		ac.messages["loginPasswordResetFailed"] = "Das Passwort konnte nicht zurückgesetzt werden!";
		ac.messages["loginPasswordResetEmailFailed"] = "Das Passwort konnte nicht neu gesendet werden!";
		ac.messages["loginPasswordReset"] = "Das neue Passwort wurde an deine E-Mail Adresse gesendet.";
		ac.clearMessageTimerBegin = function() {
			$timeout(function() {
				ac.loginError = null;
				ac.resetPasswordError = null;
				ac.resetPasswordMessage = null;
				ac.registrationError = null;
				ac.registrationMessage = null;
				ac.resendVerificationError = null;
				ac.resendVerificationMessage = null;
				ac.verificationError = null;
				ac.verificationMessage = null;
			}, 3000);
		};
		
		// load config
		ac.config = {};
		ac.loadConfig = function() {
			$http.get(apiBaseUrl + '/config').then(function success(result) {
				ac.config = result.data;
			}, function error() {
			});
		};
		ac.loadConfig();
		
		
		// login
		ac.loginData = {};
		ac.loginError = null;
		ac.login = function() {
			$http.post(apiBaseUrl + '/authenticate', ac.loginData).success(function(result) {
				if(result.valid) {
					ac.loginData = {};
					ac.loginError = null;
					location.reload();
				} else {
					ac.loginData.password = null;
					ac.loginError = ac.messages[result.message];
					ac.clearMessageTimerBegin();
				}
			});
		};

		// password reset
		ac.resetPasswordData = {};
		ac.resetPasswordError = null;
		ac.resetPasswordMessage = null;
		ac.resetPassword = function() {
			$http.post(apiBaseUrl + '/resetPassword', ac.resetPasswordData).success(function(result) {
				if(result.valid) {
					ac.resetPasswordData = {};
					ac.resetPasswordError = null;
					ac.resetPasswordMessage = ac.messages[result.message];
				} else {
					ac.resetPasswordError = ac.messages[result.message];
				}
				ac.clearMessageTimerBegin();
			});
		};

		// registration
		ac.registrationData = {};
		ac.registrationError = null;
		ac.registrationMessage = null;
		ac.registration = function() {
			$http.post(apiBaseUrl + '/registration', ac.registrationData).success(function(result) {
				if(result.valid) {
					ac.registrationData = {};
					ac.registrationError = null;
					ac.registrationMessage = ac.messages[result.message];
				} else {
					ac.registrationData.password = null;
					ac.registrationData.passwordAgain = null;
					ac.registrationError = ac.messages[result.message];
				}
				ac.clearMessageTimerBegin();
			});
		};
		
		// email verification
		ac.resendVerificationData = {};
		ac.resendVerificationError = null;
		ac.resendVerificationMessage = null;
		ac.resendVerification = function() {
			$http.post(apiBaseUrl + '/resendVerification', ac.resendVerificationData).success(function(result) {
				if(result.valid) {
					ac.resendVerificationData = {};
					ac.resendVerificationError = null;
					ac.resendVerificationMessage = ac.messages[result.message];
				} else {
					ac.resendVerificationError = ac.messages[result.message];
				}
				ac.clearMessageTimerBegin();
			});
		};
		
		ac.verificationError = null;
		ac.verificationMessage = null;
		ac.verification = function(valid) {
			if(valid) {
				ac.verificationMessage = ac.messages["verificationSuccess"];
			} else {
				ac.verificationError = ac.messages["verificationFailed"];
			}
			ac.clearMessageTimerBegin();
		};
	}]);
})();