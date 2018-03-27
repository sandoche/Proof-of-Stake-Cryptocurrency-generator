/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
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
var NRS = (function(NRS, $) {
	var DOWNLOAD_REPOSITORY_URL = "https://bitbucket.org/Jelurida/nxt/downloads/";
	var index = 0;
	var bundles = [
		{alias: "nrsVersion", status: "release", prefix: "nxt-client-", ext: "zip"},
		{alias: "nrsBetaVersion", status: "beta", prefix: "nxt-client-", ext: "zip"},
		{alias: "nrsVersionWin", status: "release", prefix: "nxt-client-", ext: "exe"},
		{alias: "nrsBetaVersionWin", status: "beta", prefix: "nxt-client-", ext: "exe"},
		{alias: "nrsVersionMac", status: "release", prefix: "nxt-installer-", ext: "dmg"},
		{alias: "nrsBetaVersionMac", status: "beta", prefix: "nxt-installer-", ext: "dmg"},
		{alias: "nrsVersionLinux", status: "release", prefix: "nxt-client-", ext: "sh"},
		{alias: "nrsBetaVersionLinux", status: "beta", prefix: "nxt-client-", ext: "sh"}
	];
	NRS.isOutdated = false;

	NRS.checkAliasVersions = function() {
		if (NRS.downloadingBlockchain && !(NRS.state && NRS.state.apiProxy)) {
			$("#nrs_update_explanation").find("span").hide();
			$("#nrs_update_explanation_blockchain_sync").show();
			return;
		}

        // Load all version aliases in parallel and call checkForNewVersion() at the end
		index = 0;
		var versionInfoCall = [];
		for (var i=0; i<bundles.length; i++) {
			versionInfoCall.push(function(callback) {
				getVersionInfo(callback);
			});
		}
        async.parallel(versionInfoCall, function(err, results) {
            if (err == null) {
                NRS.logConsole("Version aliases: " + JSON.stringify(results));
            } else {
                NRS.logConsole("Version aliases lookup error " + err);
            }
			checkForNewVersion();
        });
	};

	function checkForNewVersion() {
        var installVersusNormal, installVersusBeta;
        if (NRS.nrsVersion && NRS.nrsVersion.versionNr) {
			installVersusNormal = NRS.versionCompare(NRS.state.version, NRS.nrsVersion.versionNr);
            $(".nrs_new_version_nr").html(NRS.nrsVersion.versionNr).show();
		}
		if (NRS.nrsBetaVersion && NRS.nrsBetaVersion.versionNr) {
			installVersusBeta = NRS.versionCompare(NRS.state.version, NRS.nrsBetaVersion.versionNr);
            $(".nrs_beta_version_nr").html(NRS.nrsBetaVersion.versionNr).show();
		}

		$("#nrs_update_explanation").find("> span").hide();
		$("#nrs_update_explanation_wait").attr("style", "display: none !important");
		if (installVersusNormal == -1 && installVersusBeta == -1) {
			NRS.isOutdated = true;
			$("#nrs_update").html($.t("outdated")).show();
			$("#nrs_update_explanation_new_choice").show();
		} else if (installVersusBeta == -1) {
			NRS.isOutdated = false;
			$("#nrs_update").html($.t("new_beta")).show();
			$("#nrs_update_explanation_new_beta").show();
		} else if (installVersusNormal == -1) {
			NRS.isOutdated = true;
			$("#nrs_update").html($.t("outdated")).show();
			$("#nrs_update_explanation_new_release").show();
		} else {
			NRS.isOutdated = false;
			$("#nrs_update_explanation_up_to_date").show();
		}
	}

	function verifyClientUpdate(e) {
		e.stopPropagation();
		e.preventDefault();
		var files = null;
		if (e.originalEvent.target.files && e.originalEvent.target.files.length) {
			files = e.originalEvent.target.files;
		} else if (e.originalEvent.dataTransfer.files && e.originalEvent.dataTransfer.files.length) {
			files = e.originalEvent.dataTransfer.files;
		}
		if (!files) {
			return;
		}
        var updateHashProgress = $("#nrs_update_hash_progress");
        updateHashProgress.css("width", "0%");
		updateHashProgress.show();
		var worker = new Worker("js/crypto/sha256worker.js");
		worker.onmessage = function(e) {
			if (e.data.progress) {
				$("#nrs_update_hash_progress").css("width", e.data.progress + "%");
			} else {
				$("#nrs_update_hash_progress").hide();
				$("#nrs_update_drop_zone").hide();

                var nrsUpdateResult = $("#nrs_update_result");
                if (e.data.sha256 == NRS.downloadedVersion.hash) {
					nrsUpdateResult.html($.t("success_hash_verification")).attr("class", " ");
				} else {
					nrsUpdateResult.html($.t("error_hash_verification")).attr("class", "incorrect");
				}

				$("#nrs_update_hash_version").html(NRS.downloadedVersion.versionNr);
				$("#nrs_update_hash_download").html(e.data.sha256);
				$("#nrs_update_hash_official").html(NRS.downloadedVersion.hash);
				$("#nrs_update_hashes").show();
				nrsUpdateResult.show();
				NRS.downloadedVersion = {};
				$("body").off("dragover.nrs, drop.nrs");
			}
		};

		worker.postMessage({
			file: files[0]
		});
	}

	NRS.downloadClientUpdate = function(status, ext) {
		var bundle;
		for (var i=0; i<bundles.length; i++) {
			bundle = bundles[i];
            if (bundle.status == status && bundle.ext == ext) {
				NRS.downloadedVersion = NRS[bundle.alias];
				break;
			}
		}
        if (!NRS.downloadedVersion) {
            NRS.logConsole("Cannot determine download version for alias " + bundle.alias);
            return;
        }
        var filename = bundle.prefix + NRS.downloadedVersion.versionNr + "." + bundle.ext;
        var fileurl = DOWNLOAD_REPOSITORY_URL + filename;
        var nrsUpdateExplanation = $("#nrs_update_explanation");
        if (window.java !== undefined) {
            window.java.popupHandlerURLChange(fileurl);
            nrsUpdateExplanation.html($.t("download_verification", { url: fileurl, hash: NRS.downloadedVersion.hash }));
            return;
        } else {
            $("#nrs_update_iframe").attr("src", fileurl);
        }
        nrsUpdateExplanation.hide();
        var updateDropZone = $("#nrs_update_drop_zone");
        updateDropZone.html($.t("drop_update_v2", { filename: filename }));
        updateDropZone.show();

        var body = $("body");
        body.on("dragover.nrs", function(e) {
            e.preventDefault();
            e.stopPropagation();

            if (e.originalEvent && e.originalEvent.dataTransfer) {
                e.originalEvent.dataTransfer.dropEffect = "copy";
            }
        });

        body.on("drop.nrs", function(e) {
            verifyClientUpdate(e);
        });

        updateDropZone.on("click", function(e) {
            e.preventDefault();
            $("#nrs_update_file_select").trigger("click");
        });

        $("#nrs_update_file_select").on("change", function(e) {
            verifyClientUpdate(e);
        });

		return false;
	};
	
    // Get latest version number and hash of version specified by the alias
    function getVersionInfo(callback) {
		var aliasName = bundles[index].alias;
		index ++;
        NRS.sendRequest("getAlias", {
            "aliasName": aliasName
        }, function (response) {
            if (response.aliasURI) {
                var token = response.aliasURI.trim().split(" ");
                if (token.length != 2) {
                    NRS.logConsole("Invalid token " + response.aliasURI + " for alias " + aliasName);
                    callback(null, null);
                    return;
                }
                NRS[aliasName] = { versionNr: token[0], hash: token[1] };
                callback(null, NRS[aliasName]);
            } else {
                callback(null, null);
            }
        });
    }
	return NRS;
}(NRS || {}, jQuery));