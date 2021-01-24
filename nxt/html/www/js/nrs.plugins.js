/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $, undefined) {

	NRS.plugins = {};
    NRS.disablePluginsDuringSession = true;
	NRS.activePlugins = false;
    NRS.numRunningPlugins = 0;

	NRS.checkForPluginManifest = function(pluginId) {
		var manifest = undefined;
		jQuery.ajaxSetup({ async: false });
    	$.ajax({
    		url: 'plugins/' + pluginId + '/manifest.json',
            cache: false,
    		success: function(data){
    			manifest = data;
    		}
		});
    	jQuery.ajaxSetup({ async: true });
    	return manifest;
	};

	NRS.checkPluginValidity = function(pluginId, manifest) {
		var plugin = NRS.plugins[pluginId];
		if (!manifest.pluginVersion) {
    		plugin['validity'] = NRS.constants.PV_UNKNOWN_MANIFEST_VERSION;
    		plugin['validity_msg'] = $.t('pv_unknown_manifest_version_msg', 'Unknown plugin manifest version');
    		return false;
    	}
    	if (manifest.pluginVersion != NRS.constants.PLUGIN_VERSION) {
    		plugin['validity'] = NRS.constants.PV_INCOMPATIBLE_MANIFEST_VERSION;
    		plugin['validity_msg'] = $.t('pv_incompatible_manifest_version_msg', 'Incompatible plugin manifest version');
    		return false;
    	}

    	var invalidManifestFileMsg = $.t('pv_invalid_manifest_file_msg', 'Invalid plugin manifest file');
    	var mandatoryManifestVars = ["name", "myVersion", "shortDescription", "infoUrl", "startPage", "nrsVersion"];
    	for (var i=0; i<mandatoryManifestVars.length; i++) {
    		var mvv = mandatoryManifestVars[i];
    		if (!manifest[mvv] || (manifest[mvv] && manifest[mvv].length == 0)) {
    			plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    			plugin['validity_msg'] = invalidManifestFileMsg;
    			console.log("Attribute '" + mvv + "' missing for '" + pluginId + "' plugin manifest file.");
    			return false;
    		}
    	}

    	var lengthRestrictions = [
    		["name", 20],
    		["myVersion", 16],
    		["shortDescription", 200],
    		["infoUrl", 200],
    		["startPage", 50],
    		["nrsVersion", 20]
    	];
    	for (i=0; i<lengthRestrictions.length; i++) {
    		if (manifest[lengthRestrictions[i][0]].length > lengthRestrictions[i][1]) {
    			plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    			plugin['validity_msg'] = invalidManifestFileMsg;
    			console.log("'" + lengthRestrictions[i][0] + "' attribute too long in '" + pluginId + "' plugin manifest file.");
    			return false;
    		}
    	}

    	if (!(manifest["infoUrl"].substr(0, 7) == 'http://' || manifest["infoUrl"].substr(0, 8) == 'https://')) {
    		plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    		plugin['validity_msg'] = invalidManifestFileMsg;
    		console.log("'infoUrl' attribute in '" + pluginId + "' plugin manifest file is not a valid URL.");
    		return false;
    	}

    	if (manifest["nrsVersion"].split('.').length != 3 || !(/^[\d\.]+$/.test(manifest["nrsVersion"]))) {
    		plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    		plugin['validity_msg'] = invalidManifestFileMsg;
    		console.log("'nrsVersion' attribute in '" + pluginId + "' plugin manifest file is not in correct format ('x.y.z', no additions).");
    		return false;
    	}

    	if (manifest["deactivated"] != undefined && typeof(manifest["deactivated"]) != "boolean") {
    		plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    		plugin['validity_msg'] = invalidManifestFileMsg;
    		console.log("'deactivated' attribute in '" + pluginId + "' plugin manifest file must be boolean type.");
    		return false;
    	}

    	if (manifest["sidebarOptOut"] != undefined && typeof(manifest["sidebarOptOut"]) != "boolean") {
    		plugin['validity'] = NRS.constants.PV_INVALID_MANIFEST_FILE;
    		plugin['validity_msg'] = invalidManifestFileMsg;
    		console.log("'sidebarOptOut' attribute in '" + pluginId + "' plugin manifest file must be boolean type.");
    		return false;
    	}

    	var pluginPath = 'plugins/' + pluginId + '/';
    	var notFound = undefined;
    	var mandatoryFiles = [
    		pluginPath + 'html/pages/' + pluginId + '.html',
    		pluginPath + 'html/modals/' + pluginId + '.html',
    		pluginPath + 'js/nrs.' + pluginId + '.js',
    		pluginPath + 'css/' + pluginId + '.css'
    	];
    	jQuery.ajaxSetup({ async: false });
    	for (i=0; i<mandatoryFiles.length; i++) {
			$.ajax({
    			url: mandatoryFiles[i],
    			type: 'GET',
                cache: false,
    			success: function(data) {
    				//nothing to do
    			},
    			error: function(data) {
                    NRS.logConsole(data.statusText + " error loading plugin file " + this.url);
    				notFound = this.url;
    			}
			});
    	}
    	jQuery.ajaxSetup({ async: true });

    	if (notFound) {
    		plugin['validity'] = NRS.constants.PV_INVALID_MISSING_FILES;
    		plugin['validity_msg'] = $.t('pv_invalid_missing_files_msg', 'Missing plugin files');
    		console.log("File '" + notFound + "' of plugin '" + pluginId + "' missing.");
    		return false;
    	}

    	plugin['validity'] = NRS.constants.PV_VALID;
    	plugin['validity_msg'] = $.t('pv_valid_msg', 'Plugin is valid');
    	return true;
	};

	NRS.checkPluginNRSCompatibility = function(pluginId) {
		var plugin = NRS.plugins[pluginId];
		var pluginNRSVersion = plugin.manifest["nrsVersion"];
		var pvList = pluginNRSVersion.split('.');
		var currentNRSVersion = NRS.appVersion.replace(/[a-zA-Z]/g,'');
		var cvList = currentNRSVersion.split('.');
        var versionCompare = NRS.versionCompare(pluginNRSVersion, currentNRSVersion);
		if (versionCompare == 0) {
        	plugin['nrs_compatibility'] = NRS.constants.PNC_COMPATIBLE;
    		plugin['nrs_compatibility_msg'] = $.t('pnc_compatible_msg', 'Plugin compatible with NRS version');
        } else {
            if (versionCompare == 1) {
				plugin['nrs_compatibility'] = NRS.constants.PNC_COMPATIBILITY_CLIENT_VERSION_TOO_OLD;
                plugin['nrs_compatibility_msg'] = $.t('pnc_compatibility_build_for_newer_client_msg', 'Plugin build for newer client version');
			} else {
                if (pvList[0] == cvList[0] && pvList[1] == cvList[1]) {
                    plugin['nrs_compatibility'] = NRS.constants.PNC_COMPATIBILITY_MINOR_RELEASE_DIFF;
                    plugin['nrs_compatibility_msg'] = $.t('pnc_compatibility_minor_release_diff_msg', 'Plugin build for another minor release version');
                } else {
                    plugin['nrs_compatibility'] = NRS.constants.PNC_COMPATIBILITY_MAJOR_RELEASE_DIFF;
                    plugin['nrs_compatibility_msg'] = $.t('pnc_compatibility_minor_release_diff_msg', 'Plugin build for another major release version');      
                }
            }
		}
	};

	NRS.determinePluginLaunchStatus = function(pluginId) {
		var plugin = NRS.plugins[pluginId];
		if (!((300 <= plugin['validity'] && plugin['validity'] < 400) || (300 <= plugin['nrs_compatibility'] && plugin['validity'] < 400))) {
			if (plugin['manifest']['deactivated']) {
				plugin['launch_status'] = NRS.constants.PL_DEACTIVATED;
				plugin['launch_status_msg'] = $.t('plugin_deactivated', 'Deactivated');
			} else {
				plugin['launch_status'] = NRS.constants.PL_PAUSED;
				plugin['launch_status_msg'] = $.t('plugin_paused', 'Paused');
				NRS.activePlugins = true;
			}
		}
	};

	NRS.initializePlugins = function() {
		NRS.sendRequest("getPlugins", {}, function (response) {
			if(response.plugins && response.plugins.length >= 0) {
				for (var i=0; i<response.plugins.length; i++) {
					var manifest = NRS.checkForPluginManifest(response.plugins[i]);
					if (manifest) {
						NRS.plugins[response.plugins[i]] = {
							'validity': NRS.constants.PV_NOT_VALID,
							'validity_msg': $.t('pv_not_valid_msg', 'Plugin invalid'),
							'nrs_compatibility': NRS.constants.PNC_COMPATIBILITY_UNKNOWN,
							'nrs_compatibility_msg': $.t('pnc_compatible_unknown_msg', 'Plugin compatibility with NRS version unknown'),
							'launch_status': NRS.constants.PL_HALTED,
							'launch_status_msg': $.t('plugin_halted', 'Halted'),
							'manifest': undefined
						};
						if (NRS.checkPluginValidity(response.plugins[i], manifest)) {
							NRS.plugins[response.plugins[i]]['manifest'] = manifest;
							NRS.checkPluginNRSCompatibility(response.plugins[i]);
							NRS.determinePluginLaunchStatus(response.plugins[i]);
						}
					}
				}
			}
            NRS.initPluginWarning();
		});
	};

    NRS.pages.plugins = function() {
        var msg;
        if (NRS.numRunningPlugins == 1) {
            msg = $.t('one_plugin_active_and_running_msg');
        } else {
            msg = $.t('plugins_active_and_running_msg', {
                'num': String(NRS.numRunningPlugins)
            });
        }
        $('#plugins_page_msg').html(msg);
        var pluginsDisabledWarning = $('#plugins_disabled_warning');
        if (NRS.disablePluginsDuringSession) {
            pluginsDisabledWarning.html($.t('plugins_disabled_for_session_msg'));
            pluginsDisabledWarning.show();
        } else if (NRS.settings["enable_plugins"] == "0") {
            pluginsDisabledWarning.html($.t('plugins_disabled_for_account_msg'));
            pluginsDisabledWarning.show();
        } else {
            pluginsDisabledWarning.hide();
        }
        NRS.dataLoaded();
    };

    NRS.loadPlugin = function(pluginId) {
        var plugin = NRS.plugins[pluginId];
        var manifest = NRS.plugins[pluginId]['manifest'];
        var pluginPath = 'plugins/' + pluginId + '/';
        async.series([
            function(callback){
                NRS.asyncLoadPageHTML(pluginPath + 'html/pages/' + pluginId + '.html');
                callback(null);
            },
            function(callback){
                NRS.asyncLoadPageHTML(pluginPath + 'html/modals/' + pluginId + '.html');
                callback(null);
            },
            function(callback){
                $.getScript(pluginPath + 'js/nrs.' + pluginId + '.js').done(function() {
                    if (!manifest['sidebarOptOut']) {
                        var sidebarId = 'sidebar_plugins';
                        var options = {
                            "titleHTML": manifest['name'].escapeHTML(),
                            "type": 'PAGE',
                            "page": manifest['startPage']
                        };
                        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
                        $(".sidebar .treeview").tree();
                    }
                    var cssURL = pluginPath + 'css/' + pluginId + '.css';
                    if (document.createStyleSheet) {
                        document.createStyleSheet(cssURL);
                    } else {
                        $('<link rel="stylesheet" type="text/css" href="' + cssURL + '" />').appendTo('head');
                    }
                    plugin['launch_status'] = NRS.constants.PL_RUNNING;
                    plugin['launch_status_msg'] = $.t('plugin_running', 'Running');
                    if(manifest['startPage'] && manifest['startPage'] in NRS.setup) {
                        NRS.setup[manifest['startPage']]();
                    }
                    NRS.numRunningPlugins += 1;
                    callback(null);
                }).fail(function() {
                    plugin['launch_status'] = NRS.constants.PL_HALTED;
                    plugin['launch_status_msg'] = $.t('plugin_halted', 'Halted');
                    plugin['validity'] = NRS.constants.PV_INVALID_JAVASCRIPT_FILE;
                    plugin['validity_msg'] = $.t('plugin_invalid_javascript_file', 'Invalid javascript file');
                    callback(null);
                })
            }
        ])
    };

    NRS.loadPlugins = function() {
        NRS.logConsole("Loading Plugins");
        var sidebarId = 'sidebar_plugins';
        if ($('#' + sidebarId).length == 0) {
            var options = {
                "id": sidebarId,
                "titleHTML": '<i class="fa fa-plug"></i> <span data-i18n="plugins">Plugins</span>',
                "page": 'plugins',
                "desiredPosition": 110,
                "depends": { apis: [ NRS.constants.REQUEST_TYPES.getPlugins ] }
            };
            NRS.addTreeviewSidebarMenuItem(options);
        }

        $.each(NRS.plugins, function(pluginId, pluginDict) {
            NRS.logConsole("Iterating over plugins");
            if ((NRS.settings["enable_plugins"] == "0" || NRS.disablePluginsDuringSession) && pluginDict['launch_status'] == NRS.constants.PL_PAUSED) {
                pluginDict['launch_status'] = NRS.constants.PL_DEACTIVATED;
                pluginDict['launch_status_msg'] = $.t('plugin_deactivated', 'Deactivated');
            }
            if (pluginDict['launch_status'] == NRS.constants.PL_PAUSED) {
                NRS.loadPlugin(pluginId);
            }
        });
        var growlOptions = {
            type: "warning",
            position: {
                from: "bottom",
                align: "right"
            },
            offset: 20,
            offsetRight: 20
        };
        if (!NRS.disablePluginsDuringSession && !NRS.settings["enable_plugins"] == "0" && NRS.numRunningPlugins > 0) {
            var msg;
            if (NRS.numRunningPlugins == 1) {
                msg = $.t('one_plugin_active_and_running_msg');
            } else {
                msg = $.t('plugins_active_and_running_msg', {
                    'num': String(NRS.numRunningPlugins)
                });
            }
            $.growl(msg, growlOptions);
        }

        NRS.loadPageHTMLTemplates();
        NRS.loadModalHTMLTemplates();
        NRS.logConsole("Plugins loaded");
    };

    NRS.getPluginRowHTML = function(pluginId) {
        var plugin = NRS.plugins[pluginId];
        var manifest = plugin['manifest'];

        var html = "";
        html += "<tr>";
        var nameHTML;
        if (manifest) {
            nameHTML = String(manifest['name']).escapeHTML();
        } else {
            nameHTML = String(pluginId).escapeHTML();
        }
        html += "<td>" + nameHTML + "</td>";
        html += "<td>" + ((manifest) ? String(manifest['myVersion']).escapeHTML() : "&nbsp;") + "</td>";

        var websiteHTML = "&nbsp;";
        if (manifest) {
            var uri = encodeURI(String(manifest['infoUrl']));
            websiteHTML = "<a href='" + uri + "' target='_blank'><span>" + $.t('website') + "</span></a>";
        }
        html += "<td>" + websiteHTML + "</td>";

        var validityPopoverHTML = "data-content='" + plugin['validity_msg'].escapeHTML() + "' data-placement='top'";
        var validityText;
        var validityHTML;
        if (100 <= plugin['validity'] && plugin['validity'] < 200) {
            validityText = $.t('valid', 'Valid');
            validityHTML = "<span class='label label-success show_popover' " + validityPopoverHTML + " style='display:inline-block;min-width:85px;'>";
            validityHTML += validityText + " <i class='fa fa-check'></i></span>";
        } else {
            validityText = $.t('invalid', 'Invalid');
            validityHTML = "<span class='label label-danger show_popover' " + validityPopoverHTML + " style='display:inline-block;min-width:85px;'>";
            validityHTML += validityText + " <i class='fa fa-times-circle'></i></span>";
        }
        html += "<td style='text-align:center;'>" + validityHTML + "</td>";

        var compatibilityHTML;
        if (manifest) {
            var compatibilityPopoverHTML = "data-content='" + plugin['nrs_compatibility_msg'].escapeHTML() + "' data-placement='top'";
            var compatibilityText = manifest['nrsVersion'].escapeHTML();
            if (100 <= plugin['nrs_compatibility'] && plugin['nrs_compatibility'] < 200) {
                compatibilityHTML = "<span class='label label-success show_popover' " + compatibilityPopoverHTML + " style='display:inline-block;min-width:70px;'>";
                compatibilityHTML += compatibilityText + "</span>";
            } else if (200 <= plugin['nrs_compatibility'] && plugin['nrs_compatibility'] < 300) {
                compatibilityHTML = "<span class='label label-warning show_popover' " + compatibilityPopoverHTML + " style='display:inline-block;min-width:70px;'>";
                compatibilityHTML += compatibilityText + "</span>";
            } else {
                compatibilityHTML = "<span class='label label-danger show_popover' " + compatibilityPopoverHTML + " style='display:inline-block;min-width:70px;'>";
                compatibilityHTML += compatibilityText + "</span>";
            }
        } else {
            compatibilityHTML = "&nbsp;";
        }
        html += "<td style='text-align:center;'>" + compatibilityHTML + "</td>";

        var launchStatusText = plugin['launch_status_msg'].escapeHTML();
        var launchStatusType = "default";
        if (plugin['launch_status'] == NRS.constants.PL_RUNNING) {
            launchStatusType = "success";
        }
        if (plugin['launch_status'] == NRS.constants.PL_PAUSED || plugin['launch_status'] == NRS.constants.PL_DEACTIVATED) {
            launchStatusType = "warning";
        }
        if (plugin['launch_status'] == NRS.constants.PL_HALTED) {
            launchStatusType = "danger";
        }
        html += "<td style='text-align:center;'><span class='label label-" + launchStatusType + "' style='display:inline-block;min-width:95px;'>";
        html += launchStatusText + "</span></td>";

        html += "</tr>";

        return html;
    };

    NRS.pages.plugins_overview = function() {
        var rows = "";
        
        $.each(NRS.plugins, function(pluginId) {
            rows += NRS.getPluginRowHTML(pluginId);
        });
        NRS.dataLoaded(rows);
    };


	return NRS;
}(NRS || {}, jQuery));