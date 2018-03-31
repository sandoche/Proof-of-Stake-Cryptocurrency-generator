/**
 *  Diagnostic plugin for Android
 *
 *  Copyright (c) 2015 Working Edge Ltd.
 *  Copyright (c) 2012 AVANTIC ESTUDIO DE INGENIEROS
 **/
var Diagnostic = (function(){

	/***********************
	 *
	 * Internal properties
	 *
	 *********************/
	var Diagnostic = {};

	var runtimeStoragePrefix = "__diag_rtm_";

	var runtimeGroupsMap;

	// Indicates if a runtime permissions request is in progress
	var requestInProgress = false;

	/********************
	 *
	 * Public properties
	 *
	 ********************/

	// Placeholder listeners
	Diagnostic._onBluetoothStateChange =
	Diagnostic._onLocationStateChange =
	Diagnostic._onPermissionRequestComplete = function(){};


	/**
	 * "Dangerous" permissions that need to be requested at run-time (Android 6.0/API 23 and above)
	 * See http://developer.android.com/guide/topics/security/permissions.html#perm-groups
	 * @type {Object}
	 */
	Diagnostic.runtimePermission = // deprecated
	Diagnostic.permission = {
		"READ_CALENDAR": "READ_CALENDAR",
		"WRITE_CALENDAR": "WRITE_CALENDAR",
		"CAMERA": "CAMERA",
		"READ_CONTACTS": "READ_CONTACTS",
		"WRITE_CONTACTS": "WRITE_CONTACTS",
		"GET_ACCOUNTS": "GET_ACCOUNTS",
		"ACCESS_FINE_LOCATION": "ACCESS_FINE_LOCATION",
		"ACCESS_COARSE_LOCATION": "ACCESS_COARSE_LOCATION",
		"RECORD_AUDIO": "RECORD_AUDIO",
		"READ_PHONE_STATE": "READ_PHONE_STATE",
		"CALL_PHONE": "CALL_PHONE",
		"ADD_VOICEMAIL": "ADD_VOICEMAIL",
		"USE_SIP": "USE_SIP",
		"PROCESS_OUTGOING_CALLS": "PROCESS_OUTGOING_CALLS",
		"READ_CALL_LOG": "READ_CALL_LOG",
		"WRITE_CALL_LOG": "WRITE_CALL_LOG",
		"SEND_SMS": "SEND_SMS",
		"RECEIVE_SMS": "RECEIVE_SMS",
		"READ_SMS": "READ_SMS",
		"RECEIVE_WAP_PUSH": "RECEIVE_WAP_PUSH",
		"RECEIVE_MMS": "RECEIVE_MMS",
		"WRITE_EXTERNAL_STORAGE": "WRITE_EXTERNAL_STORAGE",
		"READ_EXTERNAL_STORAGE": "READ_EXTERNAL_STORAGE",
		"BODY_SENSORS": "BODY_SENSORS"
	};

	/**
	 * Permission groups indicate which associated permissions will also be requested if a given permission is requested.
	 * See http://developer.android.com/guide/topics/security/permissions.html#perm-groups
	 * @type {Object}
	 */
	Diagnostic.runtimePermissionGroups = // deprecated
	Diagnostic.permissionGroups = {
		"CALENDAR": ["READ_CALENDAR", "WRITE_CALENDAR"],
		"CAMERA": ["CAMERA"],
		"CONTACTS": ["READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS"],
		"LOCATION": ["ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION"],
		"MICROPHONE": ["RECORD_AUDIO"],
		"PHONE": ["READ_PHONE_STATE", "CALL_PHONE", "ADD_VOICEMAIL", "USE_SIP", "PROCESS_OUTGOING_CALLS", "READ_CALL_LOG", "WRITE_CALL_LOG"],
		"SENSORS": ["BODY_SENSORS"],
		"SMS": ["SEND_SMS", "RECEIVE_SMS", "READ_SMS", "RECEIVE_WAP_PUSH", "RECEIVE_MMS"],
		"STORAGE": ["READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"]
	};

	Diagnostic.runtimePermissionStatus = // deprecated
	Diagnostic.permissionStatus = {
		"GRANTED": "GRANTED", //  User granted access to this permission, the device is running Android 5.x or below, or the app is built with API 22 or below.
		"DENIED": "DENIED", // User denied access to this permission
		"NOT_REQUESTED": "NOT_REQUESTED", // App has not yet requested access to this permission.
		"DENIED_ALWAYS": "DENIED_ALWAYS" // User denied access to this permission and checked "Never Ask Again" box.
	};

	Diagnostic.locationMode = {
		"HIGH_ACCURACY": "high_accuracy",
		"DEVICE_ONLY": "device_only",
		"BATTERY_SAVING": "battery_saving",
		"LOCATION_OFF": "location_off"
	};

	Diagnostic.locationAuthorizationMode = {}; // Empty object to enable easy cross-platform compatibility with iOS


	Diagnostic.firstRequestedPermissions;

	Diagnostic.bluetoothState = {
		"UNKNOWN": "unknown",
		"POWERED_OFF": "powered_off",
		"POWERED_ON": "powered_on",
		"POWERING_OFF": "powering_off",
		"POWERING_ON": "powering_on"
	};


	/********************
	 *
	 * Internal functions
	 *
	 ********************/

	function checkForInvalidPermissions(permissions, errorCallback){
		if(typeof(permissions) !== "object") permissions = [permissions];
		var valid = true, invalidPermissions = [];
		permissions.forEach(function(permission){
			if(!Diagnostic.permission[permission]){
				invalidPermissions.push(permission);
			}
		});
		if(invalidPermissions.length > 0){
			errorCallback("Invalid permissions specified: "+invalidPermissions.join(", "));
			valid = false;
		}
		return valid;
	}

	/**
	 * Maintains a locally persisted list of which permissions have been requested in order to resolve the returned status of STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS to either NOT_REQUESTED or DENIED_ALWAYS.
	 * Since requesting a given permission implicitly requests all other permissions in the same group (e.g. requesting READ_CALENDAR will also grant/deny WRITE_CALENDAR),
	 * flag every permission in the groups that were requested.
	 * @param {Array} permissions - list of requested permissions
	 */
	function updateFirstRequestedPermissions(permissions){
		var groups = {};

		permissions.forEach(function(permission){
			groups[runtimeGroupsMap[permission]] = 1;
		});


		for(var group in groups){
			Diagnostic.permissionGroups[group].forEach(function(permission){
				if(!Diagnostic.firstRequestedPermissions[permission]){
					setPermissionFirstRequested(permission);
				}
			});
		}
	}

	function setPermissionFirstRequested(permission){
		localStorage.setItem(runtimeStoragePrefix+permission, 1);
		getFirstRequestedPermissions();
	}

	function getFirstRequestedPermissions(){
		if(!runtimeGroupsMap){
			buildRuntimeGroupsMap();
		}
		Diagnostic.firstRequestedPermissions = {};
		for(var permission in Diagnostic.permission){
			if(localStorage.getItem(runtimeStoragePrefix+permission) == 1){
				Diagnostic.firstRequestedPermissions[permission] = 1;
			}
		}
		return Diagnostic.firstRequestedPermissions;
	}

	function resolveStatus(permission, status){
		if(status == "STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS"){
			status = Diagnostic.firstRequestedPermissions[permission] ? Diagnostic.permissionStatus.DENIED_ALWAYS : Diagnostic.permissionStatus.NOT_REQUESTED;
		}
		return status;
	}

	function buildRuntimeGroupsMap(){
		runtimeGroupsMap = {};
		for(var group in Diagnostic.permissionGroups){
			var permissions = Diagnostic.permissionGroups[group];
			for(var i=0; i<permissions.length; i++){
				runtimeGroupsMap[permissions[i]] = group;
			}
		}
	}

	function combineLocationStatuses(statuses){
		var coarseStatus = statuses[Diagnostic.permission.ACCESS_COARSE_LOCATION],
			fineStatus = statuses[Diagnostic.permission.ACCESS_FINE_LOCATION],
			status;

		if(coarseStatus == Diagnostic.permissionStatus.DENIED_ALWAYS || fineStatus == Diagnostic.permissionStatus.DENIED_ALWAYS){
			status = Diagnostic.permissionStatus.DENIED_ALWAYS;
		}else if(coarseStatus == Diagnostic.permissionStatus.DENIED || fineStatus == Diagnostic.permissionStatus.DENIED){
			status = Diagnostic.permissionStatus.DENIED;
		}else if(coarseStatus == Diagnostic.permissionStatus.NOT_REQUESTED || fineStatus == Diagnostic.permissionStatus.NOT_REQUESTED){
			status = Diagnostic.permissionStatus.NOT_REQUESTED;
		}else{
			status = Diagnostic.permissionStatus.GRANTED;
		}
		return status;
	}

	function combineCameraStatuses(statuses){
		var cameraStatus = statuses[Diagnostic.permission.CAMERA],
			mediaStatus = statuses[Diagnostic.permission.READ_EXTERNAL_STORAGE],
			status;

		if(cameraStatus == Diagnostic.permissionStatus.DENIED_ALWAYS || mediaStatus == Diagnostic.permissionStatus.DENIED_ALWAYS){
			status = Diagnostic.permissionStatus.DENIED_ALWAYS;
		}else if(cameraStatus == Diagnostic.permissionStatus.DENIED || mediaStatus == Diagnostic.permissionStatus.DENIED){
			status = Diagnostic.permissionStatus.DENIED;
		}else if(cameraStatus == Diagnostic.permissionStatus.NOT_REQUESTED || mediaStatus == Diagnostic.permissionStatus.NOT_REQUESTED){
			status = Diagnostic.permissionStatus.NOT_REQUESTED;
		}else{
			status = Diagnostic.permissionStatus.GRANTED;
		}
		return status;
	}

	function ensureBoolean(callback){
		return function(result){
			callback(!!result);
		}
	}


	/**********************
	 *
	 * Public API functions
	 *
	 **********************/


	/***********
	 * General
	 ***********/

	/**
	 * Opens settings page for this app.
	 *
	 * @param {Function} successCallback - The callback which will be called when switch to settings is successful.
	 * @param {Function} errorCallback - The callback which will be called when switch to settings encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.switchToSettings = function(successCallback, errorCallback) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'switchToSettings',
			[]);
	};

	/**
	 * Returns the current authorisation status for a given permission.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return GRANTED status as permissions are already granted at installation time.
	 *
	 * @param {Function} successCallback - function to call on successful retrieval of status.
	 * This callback function is passed a single string parameter which defines the current authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to retrieve authorisation status.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {String} permission - permission to request authorisation status for, defined as a value in cordova.plugins.diagnostic.permission
	 */
	Diagnostic.getPermissionAuthorizationStatus = function(successCallback, errorCallback, permission){
		if(!checkForInvalidPermissions(permission, errorCallback)) return;

		function onSuccess(status){
			successCallback(resolveStatus(permission, status));
		}

		return cordova.exec(
			onSuccess,
			errorCallback,
			'Diagnostic',
			'getPermissionAuthorizationStatus',
			[permission]);
	};

	/**
	 * Returns the current authorisation status for multiple permissions.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return GRANTED status as permissions are already granted at installation time.
	 *
	 * @param {Function} successCallback - function to call on successful retrieval of status.
	 * This callback function is passed a single object parameter which defines a key/value map, where the key is the requested permission defined as a value in cordova.plugins.diagnostic.permission, and the value is the current authorisation status of that permission as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to retrieve authorisation statuses.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {Array} permissions - list of permissions to request authorisation statuses for, defined as values in cordova.plugins.diagnostic.permission
	 */
	Diagnostic.getPermissionsAuthorizationStatus = function(successCallback, errorCallback, permissions){
		if(!checkForInvalidPermissions(permissions, errorCallback)) return;

		function onSuccess(statuses){
			for(var permission in statuses){
				statuses[permission] = resolveStatus(permission, statuses[permission]);
			}
			successCallback(statuses);
		}

		return cordova.exec(
			onSuccess,
			errorCallback,
			'Diagnostic',
			'getPermissionsAuthorizationStatus',
			[permissions]);
	};


	/**
	 * Requests app to be granted authorisation for a runtime permission.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will have no effect as the permissions are already granted at installation time.
	 *
	 * @param {Function} successCallback - function to call on successful request for runtime permission.
	 * This callback function is passed a single string parameter which defines the resulting authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {String} permission - permission to request authorisation for, defined as a value in cordova.plugins.diagnostic.permission
	 */
	Diagnostic.requestRuntimePermission = function(successCallback, errorCallback, permission) {
		if(!checkForInvalidPermissions(permission, errorCallback)) return;

		if(requestInProgress){
			return onError("A runtime permissions request is already in progress");
		}

		function onSuccess(status){
			requestInProgress = false;
			var status = resolveStatus(permission, status[permission]);
			successCallback(status);
			var statuses = {};
			statuses[permission] = status;
			Diagnostic._onPermissionRequestComplete(statuses);
			updateFirstRequestedPermissions([permission]);
		}

		function onError(error){
			requestInProgress = false;
			errorCallback(error);
		}

		requestInProgress = true;
		return cordova.exec(
			onSuccess,
			onError,
			'Diagnostic',
			'requestRuntimePermission',
			[permission]);
	};

	/**
	 * Requests app to be granted authorisation for multiple runtime permissions.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will have no effect as the permissions are already granted at installation time.
	 *
	 * @param {Function} successCallback - function to call on successful request for runtime permissions.
	 * This callback function is passed a single object parameter which defines a key/value map, where the key is the permission to request defined as a value in cordova.plugins.diagnostic.permission, and the value is the resulting authorisation status of that permission as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {Array} permissions - permissions to request authorisation for, defined as values in cordova.plugins.diagnostic.permission
	 */
	Diagnostic.requestRuntimePermissions = function(successCallback, errorCallback, permissions){
		if(!checkForInvalidPermissions(permissions, errorCallback)) return;

		if(requestInProgress){
			return onError("A runtime permissions request is already in progress");
		}

		function onSuccess(statuses){
			requestInProgress = false;
			for(var permission in statuses){
				statuses[permission] = resolveStatus(permission, statuses[permission]);
			}
			successCallback(statuses);
			Diagnostic._onPermissionRequestComplete(statuses);
			updateFirstRequestedPermissions(permissions);
		}

		function onError(error){
			requestInProgress = false;
			errorCallback(error);
		}

		requestInProgress = true;
		return cordova.exec(
			onSuccess,
			onError,
			'Diagnostic',
			'requestRuntimePermissions',
			[permissions]);

	};

	/**
	 * Indicates if the plugin is currently requesting a runtime permission via the native API.
	 * Note that only one request can be made concurrently because the native API cannot handle concurrent requests,
	 * so the plugin will invoke the error callback if attempting to make more than one simultaneous request.
	 * Multiple permission requests should be grouped into a single call since the native API is setup to handle batch requests of multiple permission groups.
	 *
	 * @return {boolean} true if a permission request is currently in progress.
	 */
	Diagnostic.isRequestingPermission = function(){
		return requestInProgress;
	};

	/**
	 * Registers a function to be called when a runtime permission request has completed.
	 * Pass in a falsey value to de-register the currently registered function.
	 *
	 * @param {Function} successCallback -  The callback which will be called when a runtime permission request has completed.
	 * This callback function is passed a single object parameter which defines a key/value map, where the key is the permission requested (defined as a value in cordova.plugins.diagnostic.permission) and the value is the resulting authorisation status of that permission as a value in cordova.plugins.diagnostic.permissionStatus.
	 */
	Diagnostic.registerPermissionRequestCompleteHandler = function(successCallback) {
		Diagnostic._onPermissionRequestComplete = successCallback || function(){};
	};


	/************
	 * Location *
	 ************/

	/**
	 * Checks if location is available for use by the app.
	 * On Android, this returns true if Location Mode is enabled and any mode is selected (e.g. Battery saving, Device only, High accuracy)
	 * AND if the app is authorised to use location.
	 *
	 * @param {Function} successCallback - The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if location is available for use.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isLocationAvailable = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isLocationAvailable',
			[]);
	};

	/**
	 * Checks if the device location setting is enabled.
	 * On Android, this returns true if Location Mode is enabled and any mode is selected (e.g. Battery saving, Device only, High accuracy)
	 *
	 * @param {Function} successCallback - The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if location setting is enabled.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isLocationEnabled = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isLocationEnabled',
			[]);
	};

	/**
	 * Checks if high-accuracy locations are available to the app from GPS hardware.
	 * Returns true if Location mode is enabled and is set to "Device only" or "High accuracy"
	 * AND if the app is authorised to use location.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if high-accuracy GPS-based location is available.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isGpsLocationAvailable = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isGpsLocationAvailable',
			[]);
	};

	/**
	 * Checks if the device location setting is set to return high-accuracy locations from GPS hardware.
	 * Returns true if Location mode is enabled and is set to either:
	 * Device only = GPS hardware only (high accuracy)
	 * High accuracy = GPS hardware, network triangulation and Wifi network IDs (high and low accuracy)
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if device setting is set to return high-accuracy GPS-based location.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isGpsLocationEnabled = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isGpsLocationEnabled',
			[]);
	};

	/**
	 * Checks if low-accuracy locations are available to the app from network triangulation/WiFi access points.
	 * Returns true if Location mode is enabled and is set to "Battery saving" or "High accuracy"
	 * AND if the app is authorised to use location.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if low-accuracy network-based location is available.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isNetworkLocationAvailable = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isNetworkLocationAvailable',
			[]);
	};

	/**
	 * Checks if the device location setting is set to return low-accuracy locations from network triangulation/WiFi access points.
	 * Returns true if Location mode is enabled and is set to either:
	 * Battery saving = network triangulation and Wifi network IDs (low accuracy)
	 * High accuracy = GPS hardware, network triangulation and Wifi network IDs (high and low accuracy)
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if device setting is set to return low-accuracy network-based location.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isNetworkLocationEnabled = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isNetworkLocationEnabled',
			[]);
	};

	/**
	 * Returns the current location mode setting for the device.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single string parameter defined as a constant in `cordova.plugins.diagnostic.locationMode`.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.getLocationMode = function(successCallback, errorCallback) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'getLocationMode',
			[]);
	};

	/**
	 * Switches to the Location page in the Settings app
	 */
	Diagnostic.switchToLocationSettings = function() {
		return cordova.exec(null,
			null,
			'Diagnostic',
			'switchToLocationSettings',
			[]);
	};

	/**
	 * Requests location authorization for the application.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will have no effect as the permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions.
	 * This callback function is passed a single string parameter which defines the resulting authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation.
	 */
	Diagnostic.requestLocationAuthorization = function(successCallback, errorCallback){
		function onSuccess(statuses){
			successCallback(combineLocationStatuses(statuses));
		}
		Diagnostic.requestRuntimePermissions(onSuccess, errorCallback, [
			Diagnostic.permission.ACCESS_COARSE_LOCATION,
			Diagnostic.permission.ACCESS_FINE_LOCATION
		]);
	};

	/**
	 * Returns the location authorization status for the application.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return GRANTED status as permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions status.
	 * This callback function is passed a single string parameter which defines the current authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation status.
	 */
	Diagnostic.getLocationAuthorizationStatus = function(successCallback, errorCallback){
		function onSuccess(statuses){
			successCallback(combineLocationStatuses(statuses));
		}
		Diagnostic.getPermissionsAuthorizationStatus(onSuccess, errorCallback, [
			Diagnostic.permission.ACCESS_COARSE_LOCATION,
			Diagnostic.permission.ACCESS_FINE_LOCATION
		]);
	};

	/**
	 * Checks if the application is authorized to use location.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return TRUE as permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions status.
	 * This callback function is passed a single boolean parameter which is TRUE if the app currently has runtime authorisation to use location.
	 * @param {Function} errorCallback - function to call on failure to request authorisation status.
	 */
	Diagnostic.isLocationAuthorized = function(successCallback, errorCallback){
		function onSuccess(status){
			successCallback(status == Diagnostic.permissionStatus.GRANTED);
		}
		Diagnostic.getLocationAuthorizationStatus(onSuccess, errorCallback);
	};

	/**
	 * Registers a function to be called when a change in Location state occurs.
	 * On Android, this occurs when the Location Mode is changed.
	 * Pass in a falsey value to de-register the currently registered function.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the Location state changes.
	 * This callback function is passed a single string parameter defined as a constant in `cordova.plugins.diagnostic.locationMode`.
	 */
	Diagnostic.registerLocationStateChangeHandler = function(successCallback) {
		Diagnostic._onLocationStateChange = successCallback || function(){};
	};

	/************
	 * WiFi     *
	 ************/

	/**
	 * Checks if Wifi is connected/enabled.
	 * On Android this returns true if the WiFi setting is set to enabled.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if device is connected by WiFi.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isWifiAvailable = Diagnostic.isWifiEnabled = function(successCallback, errorCallback) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'isWifiAvailable',
			[]);
	};

	/**
	 * Switches to the WiFi page in the Settings app
	 */
	Diagnostic.switchToWifiSettings = function() {
		return cordova.exec(null,
			null,
			'Diagnostic',
			'switchToWifiSettings',
			[]);
	};

	/**
	 * Enables/disables WiFi on the device.
	 *
	 * @param {Function} successCallback - function to call on successful setting of WiFi state
	 * @param {Function} errorCallback - function to call on failure to set WiFi state.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {Boolean} state - WiFi state to set: TRUE for enabled, FALSE for disabled.
	 */
	Diagnostic.setWifiState = function(successCallback, errorCallback, state) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'setWifiState',
			[state]);
	};

	/************
	 * Camera   *
	 ************/

	/**
	 * Checks if camera is usable: both present and authorised for use.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if camera is present and authorized for use.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isCameraAvailable = function(successCallback, errorCallback) {
		Diagnostic.isCameraPresent(function(isPresent){
			if(isPresent){
				Diagnostic.isCameraAuthorized(successCallback, errorCallback);
			}else{
				successCallback(!!isPresent);
			}
		},errorCallback);
	};

	/**
	 * Checks if camera hardware is present on device.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if camera is present
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isCameraPresent = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isCameraPresent',
			[]);
	};

	/**
	 * Requests authorisation for runtime permissions to use the camera.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will have no effect as the permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions.
	 * This callback function is passed a single string parameter which defines the resulting authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation.
	 */
	Diagnostic.requestCameraAuthorization = function(successCallback, errorCallback){
		function onSuccess(statuses){
			successCallback(combineCameraStatuses(statuses));
		}
		Diagnostic.requestRuntimePermissions(onSuccess, errorCallback, [
			Diagnostic.permission.CAMERA,
			Diagnostic.permission.READ_EXTERNAL_STORAGE
		]);
	};

	/**
	 * Returns the authorisation status for runtime permissions to use the camera.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return GRANTED status as permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions status.
	 * This callback function is passed a single string parameter which defines the current authorisation status as a value in cordova.plugins.diagnostic.permissionStatus.
	 * @param {Function} errorCallback - function to call on failure to request authorisation status.
	 */
	Diagnostic.getCameraAuthorizationStatus = function(successCallback, errorCallback){
		function onSuccess(statuses){
			successCallback(combineCameraStatuses(statuses));
		}
		Diagnostic.getPermissionsAuthorizationStatus(onSuccess, errorCallback, [
			Diagnostic.permission.CAMERA,
			Diagnostic.permission.READ_EXTERNAL_STORAGE
		]);
	};

	/**
	 * Checks if the application is authorized to use the camera.
	 * Note: this is intended for Android 6 / API 23 and above. Calling on Android 5 / API 22 and below will always return TRUE as permissions are already granted at installation time.
	 * @param {Function} successCallback - function to call on successful request for runtime permissions status.
	 * This callback function is passed a single boolean parameter which is TRUE if the app currently has runtime authorisation to use location.
	 * @param {Function} errorCallback - function to call on failure to request authorisation status.
	 */
	Diagnostic.isCameraAuthorized = function(successCallback, errorCallback){
		function onSuccess(status){
			successCallback(status == Diagnostic.permissionStatus.GRANTED);
		}
		Diagnostic.getCameraAuthorizationStatus(onSuccess, errorCallback);
	};

	/***************
	 * Bluetooth   *
	 ***************/

	/**
	 * Checks if Bluetooth is available to the app.
	 * Returns true if the device has Bluetooth capabilities and if so that Bluetooth is switched on
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if Bluetooth is available.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isBluetoothAvailable = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isBluetoothAvailable',
			[]);
	};

	/**
	 * Checks if the device setting for Bluetooth is switched on.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if Bluetooth is switched on.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isBluetoothEnabled = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'isBluetoothEnabled',
			[]);
	};

	/**
	 * Enables/disables Bluetooth on the device.
	 *
	 * @param {Function} successCallback - function to call on successful setting of Bluetooth state
	 * @param {Function} errorCallback - function to call on failure to set Bluetooth state.
	 * This callback function is passed a single string parameter containing the error message.
	 * @param {Boolean} state - Bluetooth state to set: TRUE for enabled, FALSE for disabled.
	 */
	Diagnostic.setBluetoothState = function(successCallback, errorCallback, state) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'setBluetoothState',
			[state]);
	};

	/**
	 * Returns current state of Bluetooth hardware on the device.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single string parameter defined as a constant in `cordova.plugins.diagnostic.bluetoothState`.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.getBluetoothState = function(successCallback, errorCallback) {
		return cordova.exec(successCallback,
			errorCallback,
			'Diagnostic',
			'getBluetoothState',
			[]);
	};

	/**
	 * Registers a listener function to call when the state of Bluetooth hardware changes.
	 * Pass in a falsey value to de-register the currently registered function.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the state of Bluetooth hardware changes.
	 * This callback function is passed a single string parameter defined as a constant in `cordova.plugins.diagnostic.bluetoothState`.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.registerBluetoothStateChangeHandler = function(successCallback, errorCallback) {
		cordova.exec(
			function(){
				Diagnostic._onBluetoothStateChange = successCallback || function(){};
			},
			errorCallback,
			'Diagnostic',
			'initializeBluetoothListener',
			[]
		);
	};


	/**
	 * Checks if the device has Bluetooth capabilities.
	 * See http://developer.android.com/guide/topics/connectivity/bluetooth.html.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if device has Bluetooth capabilities.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.hasBluetoothSupport = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'hasBluetoothSupport', []);
	};

	/**
	 * Checks if the device has Bluetooth Low Energy (LE) capabilities.
	 * See http://developer.android.com/guide/topics/connectivity/bluetooth-le.html.
	 *
	 * @param {Function} successCallback -  The callback which will be called when the operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if device has Bluetooth LE capabilities.
	 * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
	 *  This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.hasBluetoothLESupport = function(successCallback, errorCallback) {
		return cordova.exec(ensureBoolean(successCallback),
			errorCallback,
			'Diagnostic',
			'hasBluetoothLESupport', []);
	};

    /**
     * Checks if the device has Bluetooth Low Energy (LE) capabilities.
	 * See http://developer.android.com/guide/topics/connectivity/bluetooth-le.html.
     *
     * @param {Function} successCallback -  The callback which will be called when the operation is successful.
     * This callback function is passed a single boolean parameter which is TRUE if device has Bluetooth LE capabilities.
     * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
     *  This callback function is passed a single string parameter containing the error message.
     */
    Diagnostic.hasBluetoothLESupport = function(successCallback, errorCallback) {
        return cordova.exec(ensureBoolean(successCallback),
            errorCallback,
            'Diagnostic',
            'hasBluetoothLESupport', []);
    };

    /**
     * Checks if the device has Bluetooth Low Energy (LE) peripheral capabilities.
	 * See http://developer.android.com/guide/topics/connectivity/bluetooth-le.html#roles.
     *
     * @param {Function} successCallback -  The callback which will be called when the operation is successful.
     * This callback function is passed a single boolean parameter which is TRUE if device has Bluetooth LE peripheral capabilities.
     * @param {Function} errorCallback -  The callback which will be called when the operation encounters an error.
     *  This callback function is passed a single string parameter containing the error message.
     */
    Diagnostic.hasBluetoothLEPeripheralSupport = function(successCallback, errorCallback) {
        return cordova.exec(ensureBoolean(successCallback),
            errorCallback,
            'Diagnostic',
            'hasBluetoothLEPeripheralSupport', []);
    };

	/**
	 * Switches to the Bluetooth page in the Settings app
	 */
	Diagnostic.switchToBluetoothSettings = function() {
		return cordova.exec(null,
			null,
			'Diagnostic',
			'switchToBluetoothSettings',
			[]);
	};


	/*************
	 * Mobile Data
	 *************/

	/**
	 * Switches to the Mobile Data page in the Settings app
	 */
	Diagnostic.switchToMobileDataSettings = function() {
		return cordova.exec(null,
			null,
			'Diagnostic',
			'switchToMobileDataSettings',
			[]);
	};


	/***************************
	 * Microphone / Record Audio
	 ***************************/

	/**
	 * Checks if the application is authorized to use the microphone for recording audio.
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if access to microphone is authorized.
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isMicrophoneAuthorized = function(successCallback, errorCallback) {
		function onSuccess(status){
			successCallback(status == Diagnostic.permissionStatus.GRANTED);
		}
		Diagnostic.getMicrophoneAuthorizationStatus(onSuccess, errorCallback);
	};

	/**
	 * Returns the authorization status for the application to use the microphone for recording audio.
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single string parameter which indicates the authorization status.
	 * Possible values are: "unknown", "denied", "not_determined", "authorized"
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.getMicrophoneAuthorizationStatus = function(successCallback, errorCallback) {
		Diagnostic.getPermissionAuthorizationStatus(successCallback, errorCallback, Diagnostic.permission.RECORD_AUDIO);
	};

	/**
	 * Requests access to microphone if authorization was never granted nor denied, will only return access status otherwise.
	 *
	 * @param {Function} successCallback - The callback which will be called when authorization request is successful.
	 * @param {Function} errorCallback - The callback which will be called when an error occurs.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.requestMicrophoneAuthorization = function(successCallback, errorCallback) {
		Diagnostic.requestRuntimePermission(successCallback, errorCallback, Diagnostic.permission.RECORD_AUDIO);
	};

	/*************
	 * Contacts
	 *************/

	/**
	 *Checks if the application is authorized to use contacts (address book).
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if access to microphone is authorized.
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isContactsAuthorized = function(successCallback, errorCallback) {
		function onSuccess(status){
			successCallback(status == Diagnostic.permissionStatus.GRANTED);
		}
		Diagnostic.getContactsAuthorizationStatus(onSuccess, errorCallback);
	};

	/**
	 * Returns the contacts (address book) authorization status for the application.
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single string parameter which indicates the authorization status.
	 * Possible values are: "unknown", "denied", "not_determined", "authorized"
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.getContactsAuthorizationStatus = function(successCallback, errorCallback) {
		Diagnostic.getPermissionAuthorizationStatus(successCallback, errorCallback, Diagnostic.permission.READ_CONTACTS);
	};

	/**
	 *  Requests contacts (address book) authorization for the application.
	 *  Should only be called if authorization status is NOT_REQUESTED. Calling it when in any other state will have no effect.
	 *
	 * @param {Function} successCallback - The callback which will be called when authorization request is successful.
	 * @param {Function} errorCallback - The callback which will be called when an error occurs.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.requestContactsAuthorization = function(successCallback, errorCallback) {
		Diagnostic.requestRuntimePermission(successCallback, errorCallback, Diagnostic.permission.READ_CONTACTS);
	};

	/*************
	 * Calendar
	 *************/

	/**
	 *Checks if the application is authorized to use calendar.
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single boolean parameter which is TRUE if access to microphone is authorized.
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.isCalendarAuthorized = function(successCallback, errorCallback) {
		function onSuccess(status){
			successCallback(status == Diagnostic.permissionStatus.GRANTED);
		}
		Diagnostic.getCalendarAuthorizationStatus(onSuccess, errorCallback);
	};

	/**
	 * Returns the calendar authorization status for the application.
	 *
	 * @param {Function} successCallback - The callback which will be called when operation is successful.
	 * This callback function is passed a single string parameter which indicates the authorization status.
	 * Possible values are: "unknown", "denied", "not_determined", "authorized"
	 * @param {Function} errorCallback -  The callback which will be called when operation encounters an error.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.getCalendarAuthorizationStatus = function(successCallback, errorCallback) {
		Diagnostic.getPermissionAuthorizationStatus(successCallback, errorCallback, Diagnostic.permission.READ_CALENDAR);
	};

	/**
	 *  Requests calendar authorization for the application.
	 *  Should only be called if authorization status is NOT_REQUESTED. Calling it when in any other state will have no effect.
	 *
	 * @param {Function} successCallback - The callback which will be called when authorization request is successful.
	 * @param {Function} errorCallback - The callback which will be called when an error occurs.
	 * This callback function is passed a single string parameter containing the error message.
	 */
	Diagnostic.requestCalendarAuthorization = function(successCallback, errorCallback) {
		Diagnostic.requestRuntimePermission(successCallback, errorCallback, Diagnostic.permission.READ_CALENDAR);
	};


	/**************
	 * Constructor
	 **************/
	getFirstRequestedPermissions();

	return Diagnostic;
});

module.exports = new Diagnostic();

