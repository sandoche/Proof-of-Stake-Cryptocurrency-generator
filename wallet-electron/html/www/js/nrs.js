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
 * @depends {3rdparty/jquery-2.1.0.js}
 * @depends {3rdparty/bootstrap.js}
 * @depends {3rdparty/big.js}
 * @depends {3rdparty/jsbn.js}
 * @depends {3rdparty/jsbn2.js}
 * @depends {3rdparty/pako.js}
 * @depends {3rdparty/webdb.js}
 * @depends {3rdparty/growl.js}
 * @depends {crypto/curve25519.js}
 * @depends {crypto/curve25519_.js}
 * @depends {crypto/passphrasegenerator.js}
 * @depends {crypto/sha256worker.js}
 * @depends {crypto/3rdparty/cryptojs/aes.js}
 * @depends {crypto/3rdparty/cryptojs/sha256.js}
 * @depends {crypto/3rdparty/jssha256.js}
 * @depends {util/converters.js}
 * @depends {util/extensions.js}
 * @depends {util/nxtaddress.js}
 */
var NRS = (function(NRS, $, undefined) {
	"use strict";

	NRS.state = {};
	NRS.blocks = [];
	NRS.account = "";
	NRS.accountRS = "";
	NRS.publicKey = "";
	NRS.accountInfo = {};

	NRS.database = null;
	NRS.databaseSupport = false;
	NRS.databaseFirstStart = false;

	// Legacy database, don't use this for data storage
	NRS.legacyDatabase = null;
	NRS.legacyDatabaseWithData = false;

	NRS.serverConnect = false;
	NRS.peerConnect = false;

	NRS.settings = {};
	NRS.mobileSettings = {
	    is_check_remember_me: false,
		is_store_remembered_passphrase: (window["cordova"] !== undefined), // too early to use feature detection
	    is_simulate_app: false,
        is_testnet: false,
        remote_node_address: "",
        remote_node_port: 7876,
        is_remote_node_ssl: false,
        validators_count: 3,
        bootstrap_nodes_count: 5
    };
	NRS.contacts = {};

	NRS.isTestNet = NRS.isTestNet ? NRS.isTestNet : false;
	NRS.forgingStatus = NRS.constants.UNKNOWN;
	NRS.isAccountForging = false;
	NRS.isLeased = false;
	NRS.needsAdminPassword = true;
    NRS.upnpExternalAddress = null;
	NRS.ledgerTrimKeep = 0;

	NRS.lastBlockHeight = 0;
	NRS.lastLocalBlockHeight = 0;
	NRS.downloadingBlockchain = false;

	NRS.rememberPassword = false;
	NRS.selectedContext = null;

	NRS.currentPage = "dashboard";
	NRS.currentSubPage = "";
	NRS.pageNumber = 1;
	//NRS.itemsPerPage = 50;  /* Now set in nrs.settings.js */

	NRS.pages = {};
	NRS.incoming = {};
	NRS.setup = {};

	NRS.appVersion = "";
	NRS.appPlatform = "";
	NRS.assetTableKeys = [];

	NRS.lastProxyBlock = 0;
	NRS.lastProxyBlockHeight = 0;
    NRS.spinner = null;

    var stateInterval;
	var stateIntervalSeconds = 30;
	var isScanning = false;

	NRS.loadMobileSettings = function () {
		if (!window["localStorage"]) {
			return;
		}
		var mobileSettings = NRS.getJSONItem("mobile_settings");
		if (mobileSettings) {
            for (var setting in mobileSettings) {
                if (!mobileSettings.hasOwnProperty(setting)) {
                    continue;
                }
                NRS.mobileSettings[setting] = mobileSettings[setting];
            }
		}
        for (setting in NRS.mobileSettings) {
            if (!NRS.mobileSettings.hasOwnProperty(setting)) {
                continue;
            }
            NRS.logConsole("NRS.mobileSettings." + setting + " = " + NRS.mobileSettings[setting]);
        }
	};

	function initSpinner() {
        var opts = {
            lines: 13 // The number of lines to draw
            , length: 10 // The length of each line
            , width: 4 // The line thickness
            , radius: 20 // The radius of the inner circle
            , scale: 1 // Scales overall size of the spinner
            , corners: 1 // Corner roundness (0..1)
            , color: '#ffffff' // #rgb or #rrggbb or array of colors
            , opacity: 0.25 // Opacity of the lines
            , rotate: 0 // The rotation offset
            , direction: 1 // 1: clockwise, -1: counterclockwise
            , speed: 1 // Rounds per second
            , trail: 60 // Afterglow percentage
            , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
            , zIndex: 2e9 // The z-index (defaults to 2000000000)
            , className: 'spinner' // The CSS class to assign to the spinner
            , top: '50%' // Top position relative to parent
            , left: '50%' // Left position relative to parent
            , shadow: false // Whether to render a shadow
            , hwaccel: false // Whether to use hardware acceleration
            , position: 'absolute' // Element positioning
        };
        NRS.spinner = new Spinner(opts);
		console.log("Spinner initialized");
    }

    NRS.init = function() {
        i18next.use(i18nextXHRBackend)
            .use(i18nextLocalStorageCache)
            .use(i18nextBrowserLanguageDetector)
            .use(i18nextSprintfPostProcessor)
            .init({
                fallbackLng: "en",
                fallbackOnEmpty: true,
                lowerCaseLng: true,
                detectLngFromLocalStorage: true,
                resGetPath: "locales/__lng__/translation.json",
                compatibilityJSON: 'v1',
                compatibilityAPI: 'v1',
                debug: true
            }, function() {
                NRS.initSettings();

                jqueryI18next.init(i18next, $, {
                    handleName: "i18n"
                });

                initSpinner();
                NRS.spinner.spin($("#center")[0]);
                NRS.loadMobileSettings();
                if (NRS.isMobileApp()) {
                    $('body').css('overflow-x', 'auto');
                    initMobile();
                } else {
                    initImpl();
                }

                $("[data-i18n]").i18n();
                NRS.initClipboard();
                hljs.initHighlightingOnLoad();
            });
    };

    function initMobile() {
        var promise = new Promise(function(resolve, reject) {
            NRS.initRemoteNodesMgr(NRS.mobileSettings.is_testnet, resolve, reject);
        });
        promise.then(function() {
            NRS.remoteNodesMgr.findMoreNodes(true);
            initImpl();
        }).catch(function() {
            var msg = $.t("cannot_find_remote_nodes");
            console.log(msg);
            $.growl(msg);
			var loadConstantsPromise = new Promise(function(resolve) {
				console.log("load server constants");
				NRS.loadServerConstants(resolve);
			});
			loadConstantsPromise.then(function() {
				var mobileSettingsModal = $("#mobile_settings_modal");
				mobileSettingsModal.find("input[name=is_offline]").val("true");
				mobileSettingsModal.modal("show");
			});
        })
    }

    function applyBranding(constants) {
        document.title = constants.PROJECT_NAME;
        $("#nrs_version_info").text(constants.PROJECT_NAME + " " + $.t("version"));
        $(".help-about").text($.t("about") + " " + constants.PROJECT_NAME);
        $(".modal-title-info").text(constants.PROJECT_NAME + " " + $.t("info"));
        if (constants.PROJECT_NAME != "NXT") {
            $(".branding-message").html("<p>" + constants.PROJECT_NAME + " " + $.t("branding_message") + "<p>");
		}
	}

    function initImpl() {
		var loadConstantsPromise = new Promise(function(resolve) {
			console.log("load server constants");
			NRS.loadServerConstants(resolve);
		});
		loadConstantsPromise.then(function() {
            applyBranding(NRS.constants);
			var getStatePromise = new Promise(function(resolve) {
				console.log("calling getState");
				NRS.sendRequest("getState", {
					"includeCounts": "false"
				}, function (response) {
					console.log("getState response received");
					var isTestnet = false;
					var isOffline = false;
                    var customLoginWarning;
					var peerPort = 0;
					for (var key in response) {
						if (!response.hasOwnProperty(key)) {
							continue;
						}
						if (key == "isTestnet") {
							isTestnet = response[key];
						}
						if (key == "isOffline") {
							isOffline = response[key];
						}
						if (key == "customLoginWarning") {
                            customLoginWarning = response[key];
						}
						if (key == "peerPort") {
							peerPort = response[key];
						}
						if (key == "needsAdminPassword") {
							NRS.needsAdminPassword = response[key];
						}
						if (key == "upnpExternalAddress") {
							NRS.upnpExternalAddress = response[key];
						}
						if (key == "version") {
							NRS.appVersion = response[key];
						}
					}

					if (!isTestnet) {
						$(".testnet_only").hide();
					} else {
						NRS.isTestNet = true;
						var testnetWarningDiv = $("#testnet_warning");
						var warningText = testnetWarningDiv.text() + " The testnet peer port is " + peerPort + (isOffline ? ", the peer is working offline." : ".");
						NRS.logConsole(warningText);
						testnetWarningDiv.text(warningText);
						$(".testnet_only, #testnet_login, #testnet_warning").show();
					}
                    var customLoginWarningDiv = $(".custom_login_warning");
                    if (customLoginWarning) {
                        customLoginWarningDiv.text(customLoginWarning);
                        customLoginWarningDiv.show();
					} else {
						customLoginWarningDiv.hide();
					}

					if (NRS.isInitializePlugins()) {
						NRS.initializePlugins();
					}
					NRS.printEnvInfo();
					NRS.spinner.stop();
					console.log("getState response processed");
					resolve();
				});
			});

			getStatePromise.then(function() {
				console.log("continue initialization");
				var hasLocalStorage = false;
				try {
					//noinspection BadExpressionStatementJS
					window.localStorage && localStorage;
					hasLocalStorage = checkLocalStorage();
				} catch (err) {
					NRS.logConsole("localStorage is disabled, error " + err.message);
					hasLocalStorage = false;
				}

				if (!hasLocalStorage) {
					NRS.logConsole("localStorage is disabled, cannot load wallet");
					// TODO add visible warning
					return; // do not load client if local storage is disabled
				}

				if (!(navigator.userAgent.indexOf('Safari') != -1 &&
					navigator.userAgent.indexOf('Chrome') == -1) &&
					navigator.userAgent.indexOf('JavaFX') == -1) {
					// Don't use account based DB in Safari due to a buggy indexedDB implementation (2015-02-24)
					NRS.createLegacyDatabase();
				}

				if (NRS.mobileSettings.is_check_remember_me) {
					$("#remember_me").prop("checked", true);
				}
				NRS.getSettings(false);

				NRS.getState(function () {
					setTimeout(function () {
						NRS.checkAliasVersions();
					}, 5000);
				});

				$("body").popover({
					"selector": ".show_popover",
					"html": true,
					"trigger": "hover"
				});

				var savedPassphrase = NRS.getStrItem("savedPassphrase");
				if (!savedPassphrase) {
					NRS.showLockscreen();
				}
				NRS.setStateInterval(30);

				setInterval(NRS.checkAliasVersions, 1000 * 60 * 60);

				NRS.allowLoginViaEnter();
				NRS.automaticallyCheckRecipient();

				$("#dashboard_table, #transactions_table").on("mouseenter", "td.confirmations", function () {
					$(this).popover("show");
				}).on("mouseleave", "td.confirmations", function () {
					$(this).popover("destroy");
					$(".popover").remove();
				});
                $(".coin-symbol-separator").html(" " + $.t("per") + " ");

				_fix();

				$(window).on("resize", function () {
					_fix();

					if (NRS.currentPage == "asset_exchange") {
						NRS.positionAssetSidebar();
					}
				});
				// Enable all static tooltip components
				// tooltip components generated dynamically (for tables cells for example)
				// has to be enabled by activating this code on the specific widget
				$("[data-toggle='tooltip']").tooltip();

				$("#dgs_search_account_center").mask(NRS.getAccountMask("*"));
				console.log("done initialization");
				if (NRS.getUrlParameter("account")) {
					NRS.login(false, NRS.getUrlParameter("account"));
				} else if (savedPassphrase) {
					$("#remember_me").prop("checked", true);
					NRS.login(true, savedPassphrase, null, { isSavedPassphrase: true });
				}
			});
		});
	}

    NRS.initClipboard = function() {
        var clipboard = new Clipboard('#copy_account_id');
        function onCopySuccess(e) {
            $.growl($.t("success_clipboard_copy"), {
                "type": "success"
            });
            e.clearSelection();
        }
        clipboard.on('success', onCopySuccess);
        clipboard.on('error', function(e) {
            if (window.java) {
                if (window.java.copyText(e.text)) {
                    onCopySuccess(e);
                    return;
                }
            }
            NRS.logConsole('Copy failed. Action: ' + e.action + '; Text: ' + e.text);

        });
    };

	function _fix() {
		var height = $(window).height() - $("body > .header").height();
		var content = $(".wrapper").height();

		$(".content.content-stretch:visible").width($(".page:visible").width());
		if (content > height) {
			$(".left-side, html, body").css("min-height", content + "px");
		} else {
			$(".left-side, html, body").css("min-height", height + "px");
		}
	}

	NRS.setStateInterval = function(seconds) {
		if (!NRS.isPollGetState()) {
			return;
		}
		if (seconds == stateIntervalSeconds && stateInterval) {
			return;
		}
		if (stateInterval) {
			clearInterval(stateInterval);
		}
		stateIntervalSeconds = seconds;
		stateInterval = setInterval(function() {
			NRS.getState(null);
			NRS.updateForgingStatus();
		}, 1000 * seconds);
	};

	var _firstTimeAfterLoginRun = false;
	var _prevLastProxyBlock = "0";

	NRS.getLastBlock = function() {
		return NRS.state.apiProxy ? NRS.lastProxyBlock : NRS.state.lastBlock;
	};

	NRS.handleBlockchainStatus = function(response, callback) {
		var firstTime = !("lastBlock" in NRS.state);
		var previousLastBlock = (firstTime ? "0" : NRS.state.lastBlock);

		NRS.state = response;
		var lastBlock = NRS.state.lastBlock;
		var height = response.apiProxy ? NRS.lastProxyBlockHeight : NRS.state.numberOfBlocks - 1;

		NRS.serverConnect = true;
		NRS.ledgerTrimKeep = response.ledgerTrimKeep;
		$("#sidebar_block_link").html(NRS.getBlockLink(height));
		if (firstTime) {
			$("#nrs_version").html(NRS.state.version).removeClass("loading_dots");
			NRS.getBlock(lastBlock, NRS.handleInitialBlocks);
		} else if (NRS.state.isScanning) {
			//do nothing but reset NRS.state so that when isScanning is done, everything is reset.
			isScanning = true;
		} else if (isScanning) {
			//rescan is done, now we must reset everything...
			isScanning = false;
			NRS.blocks = [];
			NRS.tempBlocks = [];
			NRS.getBlock(lastBlock, NRS.handleInitialBlocks);
			if (NRS.account) {
				NRS.getInitialTransactions();
				NRS.getAccountInfo();
			}
		} else if (previousLastBlock != lastBlock) {
			NRS.tempBlocks = [];
			if (NRS.account) {
				NRS.getAccountInfo();
			}
			NRS.getBlock(lastBlock, NRS.handleNewBlocks);
			if (NRS.account) {
				NRS.getNewTransactions();
				NRS.updateApprovalRequests();
			}
		} else {
			if (NRS.account) {
				NRS.getUnconfirmedTransactions(function(unconfirmedTransactions) {
					NRS.handleIncomingTransactions(unconfirmedTransactions, false);
				});
			}
		}
		if (NRS.account && !_firstTimeAfterLoginRun) {
			//Executed ~30 secs after login, can be used for tasks needing this condition state
			_firstTimeAfterLoginRun = true;
		}

		if (callback) {
			callback();
		}
	};

    NRS.connectionError = function(errorDescription, errorCode) {
        if (errorCode != 19) {
            NRS.serverConnect = false;
        }

        var msg = $.t("error_server_connect", {url: NRS.getRequestPath()}) +
            (errorDescription ? " " + NRS.escapeRespStr(errorDescription) : "");
        $.growl(msg, {
            "type": "danger",
            "offset": 10
        });
        NRS.logConsole(msg);
    };

    NRS.getState = function(callback, msg) {
		if (msg) {
			NRS.logConsole("getState event " + msg);
		}
		NRS.sendRequest("getBlockchainStatus", {}, function(response) {
			if (response.errorCode) {
                NRS.connectionError(response.errorDescription, response.errorCode);
			} else {
				var clientOptionsLink = $("#header_client_options_link");
                if (NRS.isMobileApp()) {
                    clientOptionsLink.html($.t("mobile_client"));
                }
				if (response.apiProxy) {
                    if (!NRS.isMobileApp()) {
                        if (response.isLightClient) {
                            clientOptionsLink.html($.t("light_client"));
                        } else {
                            clientOptionsLink.html($.t("roaming_client"));
                        }
                    }
					NRS.sendRequest("getBlocks", {
						"firstIndex": 0, "lastIndex": 0
					}, function(proxyBlocksResponse) {
						if (proxyBlocksResponse.errorCode) {
                            NRS.connectionError(proxyBlocksResponse.errorDescription, proxyBlocksResponse.errorCode);
						} else {
							_prevLastProxyBlock = NRS.lastProxyBlock;
							var prevHeight = NRS.lastProxyBlockHeight;
							NRS.lastProxyBlock = proxyBlocksResponse.blocks[0].block;
							NRS.lastProxyBlockHeight = proxyBlocksResponse.blocks[0].height;
							NRS.lastBlockHeight = NRS.lastProxyBlockHeight;
							NRS.incoming.updateDashboardBlocks(NRS.lastProxyBlockHeight - prevHeight);
							NRS.updateDashboardLastBlock(proxyBlocksResponse.blocks[0]);
							NRS.handleBlockchainStatus(response, callback);
                            NRS.updateDashboardMessage();
						}
					}, { isAsync: false });
					if (!NRS.isMobileApp()) {
						console.log("look for remote confirmation nodes");
						NRS.initRemoteNodesMgr(NRS.isTestnet);
					}
				} else {
					NRS.handleBlockchainStatus(response, callback);
				}
                var clientOptions = $(".client_options");
                if (NRS.isShowClientOptionsLink()) {
                    clientOptions.show();
                } else {
                    clientOptions.hide();
                }
				if (NRS.isShowRemoteWarning()) {
					$(".remote_warning").show();
				}
			}
			/* Checks if the client is connected to active peers */
			NRS.checkConnected();
			//only done so that download progress meter updates correctly based on lastFeederHeight
			if (NRS.downloadingBlockchain) {
				NRS.updateBlockchainDownloadProgress();
			}
		});
	};

	$("#logo, .sidebar-menu").on("click", "a", function(e, data) {
		if ($(this).hasClass("ignore")) {
			$(this).removeClass("ignore");
			return;
		}

		e.preventDefault();

		if ($(this).data("toggle") == "modal") {
			return;
		}

		var page = $(this).data("page");

		if (page == NRS.currentPage) {
			if (data && data.callback) {
				data.callback();
			}
			return;
		}

		$(".page").hide();

		$(document.documentElement).scrollTop(0);

		$("#" + page + "_page").show();

		$(".content-header h1").find(".loading_dots").remove();

        var $newActiveA;
        if ($(this).attr("id") && $(this).attr("id") == "logo") {
            $newActiveA = $("#dashboard_link").find("a");
		} else {
			$newActiveA = $(this);
		}
		var $newActivePageLi = $newActiveA.closest("li.treeview");

		$("ul.sidebar-menu > li.active").each(function(key, elem) {
			if ($newActivePageLi.attr("id") != $(elem).attr("id")) {
				$(elem).children("a").first().addClass("ignore").click();
			}
		});

		$("ul.sidebar-menu > li.sm_simple").removeClass("active");
		if ($newActiveA.parent("li").hasClass("sm_simple")) {
			$newActiveA.parent("li").addClass("active");
		}

		$("ul.sidebar-menu li.sm_treeview_submenu").removeClass("active");
		if($(this).parent("li").hasClass("sm_treeview_submenu")) {
			$(this).closest("li").addClass("active");
		}

		if (NRS.currentPage != "messages") {
			$("#inline_message_password").val("");
		}

		//NRS.previousPage = NRS.currentPage;
		NRS.currentPage = page;
		NRS.currentSubPage = "";
		NRS.pageNumber = 1;
		NRS.showPageNumbers = false;

		if (NRS.pages[page]) {
			NRS.pageLoading();
			NRS.resetNotificationState(page);
            var callback;
            if (data) {
				if (data.callback) {
					callback = data.callback;
				} else {
					callback = data;
				}
			} else {
				callback = undefined;
			}
            var subpage;
            if (data && data.subpage) {
                subpage = data.subpage;
			} else {
				subpage = undefined;
			}
			NRS.pages[page](callback, subpage);
		}
	});

	$("button.goto-page, a.goto-page").click(function(event) {
		event.preventDefault();
		NRS.goToPage($(this).data("page"), undefined, $(this).data("subpage"));
	});

	NRS.loadPage = function(page, callback, subpage) {
		NRS.pageLoading();
		NRS.pages[page](callback, subpage);
	};

	NRS.goToPage = function(page, callback, subpage) {
		var $link = $("ul.sidebar-menu a[data-page=" + page + "]");

		if ($link.length > 1) {
			if ($link.last().is(":visible")) {
				$link = $link.last();
			} else {
				$link = $link.first();
			}
		}

		if ($link.length == 1) {
			$link.trigger("click", [{
				"callback": callback,
				"subpage": subpage
			}]);
			NRS.resetNotificationState(page);
		} else {
			NRS.currentPage = page;
			NRS.currentSubPage = "";
			NRS.pageNumber = 1;
			NRS.showPageNumbers = false;

			$("ul.sidebar-menu a.active").removeClass("active");
			$(".page").hide();
			$("#" + page + "_page").show();
			if (NRS.pages[page]) {
				NRS.pageLoading();
				NRS.resetNotificationState(page);
				NRS.pages[page](callback, subpage);
			}
		}
	};

	NRS.pageLoading = function() {
		NRS.hasMorePages = false;

		var $pageHeader = $("#" + NRS.currentPage + "_page .content-header h1");
		$pageHeader.find(".loading_dots").remove();
		$pageHeader.append("<span class='loading_dots'><span>.</span><span>.</span><span>.</span></span>");
	};

	NRS.pageLoaded = function(callback) {
		var $currentPage = $("#" + NRS.currentPage + "_page");

		$currentPage.find(".content-header h1 .loading_dots").remove();

		if ($currentPage.hasClass("paginated")) {
			NRS.addPagination();
		}

		if (callback) {
			try {
                callback();
            } catch(e) {
				NRS.logException(e);
			}
		}
	};

	NRS.addPagination = function () {
        var firstStartNr = 1;
		var firstEndNr = NRS.itemsPerPage;
		var currentStartNr = (NRS.pageNumber-1) * NRS.itemsPerPage + 1;
		var currentEndNr = NRS.pageNumber * NRS.itemsPerPage;

		var prevHTML = '<span style="display:inline-block;width:48px;text-align:right;">';
		var firstHTML = '<span style="display:inline-block;min-width:48px;text-align:right;vertical-align:top;margin-top:4px;">';
		var currentHTML = '<span style="display:inline-block;min-width:48px;text-align:left;vertical-align:top;margin-top:4px;">';
		var nextHTML = '<span style="display:inline-block;width:48px;text-align:left;">';

		if (NRS.pageNumber > 1) {
			prevHTML += "<a href='#' data-page='" + (NRS.pageNumber - 1) + "' title='" + $.t("previous") + "' style='font-size:20px;'>";
			prevHTML += "<i class='fa fa-arrow-circle-left'></i></a>";
		} else {
			prevHTML += '&nbsp;';
		}

		if (NRS.hasMorePages) {
			currentHTML += currentStartNr + "-" + currentEndNr + "&nbsp;";
			nextHTML += "<a href='#' data-page='" + (NRS.pageNumber + 1) + "' title='" + $.t("next") + "' style='font-size:20px;'>";
			nextHTML += "<i class='fa fa-arrow-circle-right'></i></a>";
		} else {
			if (NRS.pageNumber > 1) {
				currentHTML += currentStartNr + "+";
			} else {
				currentHTML += "&nbsp;";
			}
			nextHTML += "&nbsp;";
		}
		if (NRS.pageNumber > 1) {
			firstHTML += "&nbsp;<a href='#' data-page='1'>" + firstStartNr + "-" + firstEndNr + "</a>&nbsp;|&nbsp;";
		} else {
			firstHTML += "&nbsp;";
		}

		prevHTML += '</span>';
		firstHTML += '</span>';
		currentHTML += '</span>';
		nextHTML += '</span>';

		var output = prevHTML + firstHTML + currentHTML + nextHTML;
		var $paginationContainer = $("#" + NRS.currentPage + "_page .data-pagination");

		if ($paginationContainer.length) {
			$paginationContainer.html(output);
		}
	};

	$(document).on("click", ".data-pagination a", function(e) {
		e.preventDefault();
		NRS.goToPageNumber($(this).data("page"));
	});

	NRS.goToPageNumber = function(pageNumber) {
		/*if (!pageLoaded) {
			return;
		}*/
		NRS.pageNumber = pageNumber;

		NRS.pageLoading();

		NRS.pages[NRS.currentPage]();
	};

	function initUserDB() {
		NRS.storageSelect("data", [{
			"id": "asset_exchange_version"
		}], function(error, result) {
			if (!result || !result.length) {
				NRS.storageDelete("assets", [], function(error) {
					if (!error) {
						NRS.storageInsert("data", "id", {
							"id": "asset_exchange_version",
							"contents": 2
						});
					}
				});
			}
		});

		NRS.storageSelect("data", [{
			"id": "closed_groups"
		}], function(error, result) {
			if (result && result.length) {
				NRS.setClosedGroups(result[0].contents.split("#"));
			} else {
				NRS.storageInsert("data", "id", {
					id: "closed_groups",
					contents: ""
				});
			}
		});
		NRS.loadContacts();
		NRS.getSettings(true);
		NRS.updateNotifications();
		NRS.setUnconfirmedNotifications();
		NRS.setPhasingNotifications();
        NRS.setShufflingNotifications();
		var page = NRS.getUrlParameter("page");
		if (page) {
			page = page.escapeHTML();
			if (NRS.pages[page]) {
				NRS.goToPage(page);
			} else {
				$.growl($.t("page") + " " + page + " " + $.t("does_not_exist"), {
					"type": "danger",
					"offset": 50
				});
			}
		}
		if (NRS.getUrlParameter("modal")) {
			var urlParams = [];
			if (window.location.search && window.location.search.length > 1) {
				urlParams = window.location.search.substring(1).split('&');
			}
			var modalId = "#" + NRS.getUrlParameter("modal").escapeHTML();
			var modal = $(modalId);
			var attributes = {};
			if (modal[0]) {
				var isValidParams = true;
				for (var i = 0; i < urlParams.length; i++) {
					var paramKeyValue = urlParams[i].split('=');
					if (paramKeyValue.length != 2) {
						continue;
					}
					var key = paramKeyValue[0].escapeHTML();
					if (key == "account" || key == "modal") {
						continue;
					}
					var value = paramKeyValue[1].escapeHTML();
                    var input = modal.find("input[name=" + key + "]");
                    if (input[0]) {
						if (input.attr("type") == "text") {
							input.val(value);
						} else if (input.attr("type") == "checkbox") {
							var isChecked = false;
							if (value != "true" && value != "false") {
								isValidParams = false;
								$.growl($.t("value") + " " + value + " " + $.t("must_be_true_or_false") + " " + $.t("for") + " " + key, {
									"type": "danger",
									"offset": 50
								});
							} else if (value == "true") {
								isChecked = true;
							}
							if (isValidParams) {
								input.prop('checked', isChecked);
							}
						}
					} else if (modal.find("textarea[name=" + key + "]")[0]) {
						modal.find("textarea[name=" + key + "]").val(decodeURI(value));
					} else {
						attributes["data-" + key.toLowerCase().escapeHTML()] = String(value).escapeHTML();
					}
				}
				if (isValidParams) {
					var a = $('<a />');
					a.attr('href', '#');
					a.attr('data-toggle', 'modal');
					a.attr('data-target', modalId);
					Object.keys(attributes).forEach(function (key) {
						a.attr(key, attributes[key]);
					});
					$('body').append(a);
					a.click();
				}
			} else {
				$.growl($.t("modal") + " " + modalId + " " + $.t("does_not_exist"), {
					"type": "danger",
					"offset": 50
				});
			}
		}
	}

	NRS.initUserDBSuccess = function() {
		NRS.databaseSupport = true;
		initUserDB();
        NRS.logConsole("IndexedDB initialized");
    };

	NRS.initUserDBWithLegacyData = function() {
		var legacyTables = ["contacts", "assets", "data"];
		$.each(legacyTables, function(key, table) {
			NRS.legacyDatabase.select(table, null, function(error, results) {
				if (!error && results && results.length >= 0) {
					NRS.database.insert(table, results, function(error, inserts) {});
				}
			});
		});
		setTimeout(function(){ NRS.initUserDBSuccess(); }, 1000);
	};

	NRS.initLocalStorage = function() {
		NRS.database = null;
		NRS.databaseSupport = false;
		initUserDB();
        NRS.logConsole("local storage initialized");
    };

	NRS.createLegacyDatabase = function() {
		var schema = {};
		var versionLegacyDB = 2;

		// Legacy DB before switching to account based DBs, leave schema as is
		schema["contacts"] = {
			id: {
				"primary": true,
				"autoincrement": true,
				"type": "NUMBER"
			},
			name: "VARCHAR(100) COLLATE NOCASE",
			email: "VARCHAR(200)",
			account: "VARCHAR(25)",
			accountRS: "VARCHAR(25)",
			description: "TEXT"
		};
		schema["assets"] = {
			account: "VARCHAR(25)",
			accountRS: "VARCHAR(25)",
			asset: {
				"primary": true,
				"type": "VARCHAR(25)"
			},
			description: "TEXT",
			name: "VARCHAR(10)",
			decimals: "NUMBER",
			quantityQNT: "VARCHAR(15)",
			groupName: "VARCHAR(30) COLLATE NOCASE"
		};
		schema["data"] = {
			id: {
				"primary": true,
				"type": "VARCHAR(40)"
			},
			contents: "TEXT"
		};
		if (versionLegacyDB == NRS.constants.DB_VERSION) {
			try {
				NRS.legacyDatabase = new WebDB("NRS_USER_DB", schema, versionLegacyDB, 4, function(error) {
					if (!error) {
						NRS.legacyDatabase.select("data", [{
							"id": "settings"
						}], function(error, result) {
							if (result && result.length > 0) {
								NRS.legacyDatabaseWithData = true;
							}
						});
					}
				});
			} catch (err) {
                NRS.logConsole("error creating database " + err.message);
			}
		}
	};

	function createSchema(){
		var schema = {};

		schema["contacts"] = {
			id: {
				"primary": true,
				"autoincrement": true,
				"type": "NUMBER"
			},
			name: "VARCHAR(100) COLLATE NOCASE",
			email: "VARCHAR(200)",
			account: "VARCHAR(25)",
			accountRS: "VARCHAR(25)",
			description: "TEXT"
		};
		schema["assets"] = {
			account: "VARCHAR(25)",
			accountRS: "VARCHAR(25)",
			asset: {
				"primary": true,
				"type": "VARCHAR(25)"
			},
			description: "TEXT",
			name: "VARCHAR(10)",
			decimals: "NUMBER",
			quantityQNT: "VARCHAR(15)",
			groupName: "VARCHAR(30) COLLATE NOCASE"
		};
		schema["polls"] = {
			account: "VARCHAR(25)",
			accountRS: "VARCHAR(25)",
			name: "VARCHAR(100)",
			description: "TEXT",
			poll: "VARCHAR(25)",
			finishHeight: "VARCHAR(25)"
		};
		schema["data"] = {
			id: {
				"primary": true,
				"type": "VARCHAR(40)"
			},
			contents: "TEXT"
		};
		return schema;
	}

	function initUserDb(){
		NRS.logConsole("Database is open");
		NRS.database.select("data", [{
			"id": "settings"
		}], function(error, result) {
			if (result && result.length > 0) {
				NRS.logConsole("Settings already exist");
				NRS.databaseFirstStart = false;
				NRS.initUserDBSuccess();
			} else {
				NRS.logConsole("Settings not found");
				NRS.databaseFirstStart = true;
				if (NRS.legacyDatabaseWithData) {
					NRS.initUserDBWithLegacyData();
				} else {
					NRS.initUserDBSuccess();
				}
			}
		});
	}

	NRS.createDatabase = function (dbName) {
		if (!NRS.isIndexedDBSupported()) {
			NRS.logConsole("IndexedDB not supported by the rendering engine, using localStorage instead");
			NRS.initLocalStorage();
			return;
		}
		var schema = createSchema();
		NRS.assetTableKeys = ["account", "accountRS", "asset", "description", "name", "position", "decimals", "quantityQNT", "groupName"];
		NRS.pollsTableKeys = ["account", "accountRS", "poll", "description", "name", "finishHeight"];
		try {
			NRS.logConsole("Opening database " + dbName);
            NRS.database = new WebDB(dbName, schema, NRS.constants.DB_VERSION, 4, function(error, db) {
                if (!error) {
                    NRS.indexedDB = db;
                    initUserDb();
                } else {
                    NRS.logConsole("Error opening database " + error);
                    NRS.initLocalStorage();
                }
            });
            NRS.logConsole("Opening database " + NRS.database);
		} catch (e) {
			NRS.logConsole("Exception opening database " + e.message);
			NRS.initLocalStorage();
		}
	};

	/* Display connected state in Sidebar */
	NRS.checkConnected = function() {
		NRS.sendRequest("getPeers+", {
			"state": "CONNECTED"
		}, function(response) {
            var connectedIndicator = $("#connected_indicator");
            if (response.peers && response.peers.length) {
				NRS.peerConnect = true;
				connectedIndicator.addClass("connected");
                connectedIndicator.find("span").html($.t("Connected")).attr("data-i18n", "connected");
				connectedIndicator.show();
			} else {
				NRS.peerConnect = false;
				connectedIndicator.removeClass("connected");
				connectedIndicator.find("span").html($.t("Not Connected")).attr("data-i18n", "not_connected");
				connectedIndicator.show();
			}
		});
	};

	NRS.getRequestPath = function (noProxy) {
		var url = NRS.getRemoteNodeUrl();
		if (!NRS.state.apiProxy || noProxy) {
			return url + "/nxt";
		} else {
			return url + "/nxt-proxy";
		}
	};

	NRS.getAccountInfo = function(firstRun, callback, isAccountSwitch) {
		NRS.sendRequest("getAccount", {
			"account": NRS.account,
			"includeAssets": true,
			"includeCurrencies": true,
			"includeLessors": true,
			"includeEffectiveBalance": true
		}, function(response) {
			var previousAccountInfo = NRS.accountInfo;
			NRS.accountInfo = response;
			if (response.errorCode) {
				NRS.logConsole("Get account info error (" + response.errorCode + ") " + response.errorDescription);
				$("#account_balance, #account_balance_sidebar, #account_currencies_balance, #account_nr_currencies, #account_purchase_count, #account_pending_sale_count, #account_completed_sale_count, #account_message_count, #account_alias_count").html("0");
                NRS.updateDashboardMessage();
			} else {
				if (NRS.accountRS && NRS.accountInfo.accountRS != NRS.accountRS) {
					$.growl("Generated Reed Solomon address different from the one in the blockchain!", {
						"type": "danger"
					});
					NRS.accountRS = NRS.accountInfo.accountRS;
				}
                NRS.updateDashboardMessage();
                $("#account_balance, #account_balance_sidebar").html(NRS.formatStyledAmount(response.unconfirmedBalanceNQT));
                $("#account_forged_balance").html(NRS.formatStyledAmount(response.forgedBalanceNQT));

                if (NRS.isDisplayOptionalDashboardTiles()) {
                    // only show if happened within last week and not during account switch
                    var showAssetDifference = !isAccountSwitch &&
                        ((!NRS.downloadingBlockchain || (NRS.blocks && NRS.blocks[0] && NRS.state && NRS.state.time - NRS.blocks[0].timestamp < 60 * 60 * 24 * 7)));

                    // When switching account this query returns error
                    if (!isAccountSwitch) {
                        NRS.storageSelect("data", [{
                            "id": "asset_balances"
                        }], function (error, asset_balance) {
                            if (asset_balance && asset_balance.length) {
                                var previous_balances = asset_balance[0].contents;
                                if (!NRS.accountInfo.assetBalances) {
                                    NRS.accountInfo.assetBalances = [];
                                }
                                var current_balances = JSON.stringify(NRS.accountInfo.assetBalances);
                                if (previous_balances != current_balances) {
                                    if (previous_balances != "undefined" && typeof previous_balances != "undefined") {
                                        previous_balances = JSON.parse(previous_balances);
                                    } else {
                                        previous_balances = [];
                                    }
                                    NRS.storageUpdate("data", {
                                        contents: current_balances
                                    }, [{
                                        id: "asset_balances"
                                    }]);
                                    if (showAssetDifference) {
                                        NRS.checkAssetDifferences(NRS.accountInfo.assetBalances, previous_balances);
                                    }
                                }
                            } else {
                                NRS.storageInsert("data", "id", {
                                    id: "asset_balances",
                                    contents: JSON.stringify(NRS.accountInfo.assetBalances)
                                });
                            }
                        });
                    }

                    var i;
                    if ((firstRun || isAccountSwitch) && response.assetBalances) {
                        var assets = [];
                        var assetBalances = response.assetBalances;
                        var assetBalancesMap = {};
                        for (i = 0; i < assetBalances.length; i++) {
                            if (assetBalances[i].balanceQNT != "0") {
                                assets.push(assetBalances[i].asset);
                                assetBalancesMap[assetBalances[i].asset] = assetBalances[i].balanceQNT;
                            }
                        }
                        NRS.sendRequest("getLastTrades", {
                            "assets": assets
                        }, function (response) {
                            if (response.trades && response.trades.length) {
                                var assetTotal = 0;
                                for (i = 0; i < response.trades.length; i++) {
                                    var trade = response.trades[i];
                                    assetTotal += assetBalancesMap[trade.asset] * trade.priceNQT / 100000000;
                                }
                                $("#account_assets_balance").html(NRS.formatStyledAmount(new Big(assetTotal).toFixed(8)));
                                $("#account_nr_assets").html(response.trades.length);
                            } else {
                                $("#account_assets_balance").html(0);
                                $("#account_nr_assets").html(0);
                            }
                        });
                    } else {
                        if (!response.assetBalances) {
                            $("#account_assets_balance").html(0);
                            $("#account_nr_assets").html(0);
                        }
                    }

                    if (response.accountCurrencies) {
                        var currencies = [];
                        var currencyBalances = response.accountCurrencies;
                        var numberOfCurrencies = currencyBalances.length;
                        $("#account_nr_currencies").html(numberOfCurrencies);
                        var currencyBalancesMap = {};
                        for (i = 0; i < numberOfCurrencies; i++) {
                            if (currencyBalances[i].units != "0") {
                                currencies.push(currencyBalances[i].currency);
                                currencyBalancesMap[currencyBalances[i].currency] = currencyBalances[i].units;
                            }
                        }
                        NRS.sendRequest("getLastExchanges", {
                            "currencies": currencies
                        }, function (response) {
                            if (response.exchanges && response.exchanges.length) {
                                var currencyTotal = 0;
                                for (i = 0; i < response.exchanges.length; i++) {
                                    var exchange = response.exchanges[i];
                                    currencyTotal += currencyBalancesMap[exchange.currency] * exchange.rateNQT / 100000000;
                                }
                                $("#account_currencies_balance").html(NRS.formatStyledAmount(new Big(currencyTotal).toFixed(8)));
                            } else {
                                $("#account_currencies_balance").html(0);
                            }
                        });
                    } else {
                        $("#account_currencies_balance").html(0);
                        $("#account_nr_currencies").html(0);
                    }

                    /* Display message count in top and limit to 100 for now because of possible performance issues*/
                    NRS.sendRequest("getBlockchainTransactions+", {
                        "account": NRS.account,
                        "type": 1,
                        "subtype": 0,
                        "firstIndex": 0,
                        "lastIndex": 99
                    }, function (response) {
                        if (response.transactions && response.transactions.length) {
                            if (response.transactions.length > 99)
                                $("#account_message_count").empty().append("99+");
                            else
                                $("#account_message_count").empty().append(response.transactions.length);
                        } else {
                            $("#account_message_count").empty().append("0");
                        }
                    });

                    NRS.sendRequest("getAliasCount+", {
                        "account": NRS.account
                    }, function (response) {
                        if (response.numberOfAliases != null) {
                            $("#account_alias_count").empty().append(response.numberOfAliases);
                        }
                    });

                    NRS.sendRequest("getDGSPurchaseCount+", {
                        "buyer": NRS.account
                    }, function (response) {
                        if (response.numberOfPurchases != null) {
                            $("#account_purchase_count").empty().append(response.numberOfPurchases);
                        }
                    });

                    NRS.sendRequest("getDGSPendingPurchases+", {
                        "seller": NRS.account
                    }, function (response) {
                        if (response.purchases && response.purchases.length) {
                            $("#account_pending_sale_count").empty().append(response.purchases.length);
                        } else {
                            $("#account_pending_sale_count").empty().append("0");
                        }
                    });

                    NRS.sendRequest("getDGSPurchaseCount+", {
                        "seller": NRS.account,
                        "completed": true
                    }, function (response) {
                        if (response.numberOfPurchases != null) {
                            $("#account_completed_sale_count").empty().append(response.numberOfPurchases);
                        }
                    });
                    $(".optional_dashboard_tile").show();
                } else {
                    // Hide the optional tiles and move the block info tile to the first row
                    $(".optional_dashboard_tile").hide();
                    var blockInfoTile = $(".block_info_dashboard_tile").detach();
                    blockInfoTile.appendTo($(".dashboard_first_row"));
                }

                var leasingChange = false;
				if (NRS.lastBlockHeight) {
					var isLeased = NRS.lastBlockHeight >= NRS.accountInfo.currentLeasingHeightFrom;
					if (isLeased != NRS.IsLeased) {
						leasingChange = true;
						NRS.isLeased = isLeased;
					}
				}

				if (leasingChange ||
					(response.currentLeasingHeightFrom != previousAccountInfo.currentLeasingHeightFrom) ||
					(response.lessors && !previousAccountInfo.lessors) ||
					(!response.lessors && previousAccountInfo.lessors) ||
					(response.lessors && previousAccountInfo.lessors && response.lessors.sort().toString() != previousAccountInfo.lessors.sort().toString())) {
					NRS.updateAccountLeasingStatus();
				}

				NRS.updateAccountControlStatus();

				if (response.name) {
					$("#account_name").html(NRS.addEllipsis(NRS.escapeRespStr(response.name), 17)).removeAttr("data-i18n");
				} else {
					$("#account_name").html($.t("set_account_info"));
				}
			}

			if (firstRun) {
				$("#account_balance, #account_balance_sidebar, #account_assets_balance, #account_nr_assets, #account_currencies_balance, #account_nr_currencies, #account_purchase_count, #account_pending_sale_count, #account_completed_sale_count, #account_message_count, #account_alias_count").removeClass("loading_dots");
			}

			if (callback) {
				callback();
			}
		});
	};

    NRS.updateDashboardMessage = function() {
        if (NRS.accountInfo.errorCode) {
            if (NRS.accountInfo.errorCode == 5) {
                if (NRS.downloadingBlockchain && !(NRS.state && NRS.state.apiProxy) && !NRS.state.isLightClient) {
                    if (NRS.newlyCreatedAccount) {
                        $("#dashboard_message").addClass("alert-success").removeClass("alert-danger").html($.t("status_new_account", {
                                "account_id": NRS.escapeRespStr(NRS.accountRS),
                                "public_key": NRS.escapeRespStr(NRS.publicKey)
                            }) + NRS.getPassphraseValidationLink(true) +
							"<br/><br/>" + NRS.blockchainDownloadingMessage() +
                            "<br/><br/>" + NRS.getFundAccountLink()).show();
                    } else {
                        $("#dashboard_message").addClass("alert-success").removeClass("alert-danger").html(NRS.blockchainDownloadingMessage()).show();
                    }
                } else if (NRS.state && NRS.state.isScanning) {
                    $("#dashboard_message").addClass("alert-danger").removeClass("alert-success").html($.t("status_blockchain_rescanning")).show();
                } else {
                    var message;
                    if (NRS.publicKey == "") {
                        message = $.t("status_new_account_no_pk_v3", {
                            "account_id": NRS.escapeRespStr(NRS.accountRS)
                        });
                        message += NRS.getPassphraseValidationLink(false);
                        if (NRS.downloadingBlockchain) {
                            message += "<br/><br/>" + NRS.blockchainDownloadingMessage();
                        }
                    } else {
                        message = $.t("status_new_account", {
                            "account_id": NRS.escapeRespStr(NRS.accountRS),
                            "public_key": NRS.escapeRespStr(NRS.publicKey)
                        });
                        message += NRS.getPassphraseValidationLink(true);
                        if (NRS.downloadingBlockchain) {
                            message += "<br/><br/>" + NRS.blockchainDownloadingMessage();
                        }
                        message += "<br/><br/>" + NRS.getFundAccountLink();
                    }
                    $("#dashboard_message").addClass("alert-success").removeClass("alert-danger").html(message).show();
                }
            } else {
                var errorMessage;
                if (NRS.accountInfo.errorCode == 19) {
                    errorMessage = $.t("no_open_api_peers");
                } else {
                    errorMessage = NRS.accountInfo.errorDescription ? NRS.escapeRespStr(NRS.accountInfo.errorDescription) : $.t("error_unknown");
                }


                $("#dashboard_message").addClass("alert-danger").removeClass("alert-success").html(errorMessage).show();
            }
        } else {
            if (NRS.downloadingBlockchain) {
                $("#dashboard_message").addClass("alert-success").removeClass("alert-danger").html(NRS.blockchainDownloadingMessage()).show();
            } else if (NRS.state && NRS.state.isScanning) {
                $("#dashboard_message").addClass("alert-danger").removeClass("alert-success").html($.t("status_blockchain_rescanning")).show();
            } else if (!NRS.accountInfo.publicKey) {
                var warning = NRS.publicKey != 'undefined' ? $.t("public_key_not_announced_warning", { "public_key": NRS.publicKey }) : $.t("no_public_key_warning");
                $("#dashboard_message").addClass("alert-danger").removeClass("alert-success").html(warning + " " + $.t("public_key_actions")).show();
            } else if (NRS.state.isLightClient) {
                $("#dashboard_message").addClass("alert-success").removeClass("alert-danger").html(NRS.blockchainDownloadingMessage()).show();
            } else {
                $("#dashboard_message").hide();
            }
        }
    };

	NRS.updateAccountLeasingStatus = function() {
		var accountLeasingLabel = "";
		var accountLeasingStatus = "";
		var nextLesseeStatus = "";
		if (NRS.accountInfo.nextLeasingHeightFrom < NRS.constants.MAX_INT_JAVA) {
			nextLesseeStatus = $.t("next_lessee_status", {
				"start": NRS.escapeRespStr(NRS.accountInfo.nextLeasingHeightFrom),
				"end": NRS.escapeRespStr(NRS.accountInfo.nextLeasingHeightTo),
				"account": String(NRS.convertNumericToRSAccountFormat(NRS.accountInfo.nextLessee)).escapeHTML()
			})
		}

		if (NRS.lastBlockHeight >= NRS.accountInfo.currentLeasingHeightFrom) {
			accountLeasingLabel = $.t("leased_out");
			accountLeasingStatus = $.t("balance_is_leased_out", {
				"blocks": String(NRS.accountInfo.currentLeasingHeightTo - NRS.lastBlockHeight).escapeHTML(),
				"end": NRS.escapeRespStr(NRS.accountInfo.currentLeasingHeightTo),
				"account": NRS.escapeRespStr(NRS.accountInfo.currentLesseeRS)
			});
			$("#lease_balance_message").html($.t("balance_leased_out_help"));
		} else if (NRS.lastBlockHeight < NRS.accountInfo.currentLeasingHeightTo) {
			accountLeasingLabel = $.t("leased_soon");
			accountLeasingStatus = $.t("balance_will_be_leased_out", {
				"blocks": String(NRS.accountInfo.currentLeasingHeightFrom - NRS.lastBlockHeight).escapeHTML(),
				"start": NRS.escapeRespStr(NRS.accountInfo.currentLeasingHeightFrom),
				"end": NRS.escapeRespStr(NRS.accountInfo.currentLeasingHeightTo),
				"account": NRS.escapeRespStr(NRS.accountInfo.currentLesseeRS)
			});
			$("#lease_balance_message").html($.t("balance_leased_out_help"));
		} else {
			accountLeasingStatus = $.t("balance_not_leased_out");
			$("#lease_balance_message").html($.t("balance_leasing_help"));
		}
		if (nextLesseeStatus != "") {
			accountLeasingStatus += "<br>" + nextLesseeStatus;
		}

		//no reed solomon available? do it myself? todo
        var accountLessorTable = $("#account_lessor_table");
        if (NRS.accountInfo.lessors) {
			if (accountLeasingLabel) {
				accountLeasingLabel += ", ";
				accountLeasingStatus += "<br /><br />";
			}

			accountLeasingLabel += $.t("x_lessor", {
				"count": NRS.accountInfo.lessors.length
			});
			accountLeasingStatus += $.t("x_lessor_lease", {
				"count": NRS.accountInfo.lessors.length
			});

			var rows = "";

			for (var i = 0; i < NRS.accountInfo.lessorsRS.length; i++) {
				var lessor = NRS.accountInfo.lessorsRS[i];
				var lessorInfo = NRS.accountInfo.lessorsInfo[i];
				var blocksLeft = lessorInfo.currentHeightTo - NRS.lastBlockHeight;
				var blocksLeftTooltip = "From block " + lessorInfo.currentHeightFrom + " to block " + lessorInfo.currentHeightTo;
				var nextLessee = "Not set";
				var nextTooltip = "Next lessee not set";
				if (lessorInfo.nextLesseeRS == NRS.accountRS) {
					nextLessee = "You";
					nextTooltip = "From block " + lessorInfo.nextHeightFrom + " to block " + lessorInfo.nextHeightTo;
				} else if (lessorInfo.nextHeightFrom < NRS.constants.MAX_INT_JAVA) {
					nextLessee = "Not you";
					nextTooltip = "Account " + NRS.getAccountTitle(lessorInfo.nextLesseeRS) +" from block " + lessorInfo.nextHeightFrom + " to block " + lessorInfo.nextHeightTo;
				}
				rows += "<tr>" +
					"<td>" + NRS.getAccountLink({ lessorRS: lessor }, "lessor") + "</td>" +
					"<td>" + NRS.escapeRespStr(lessorInfo.effectiveBalanceNXT) + "</td>" +
					"<td><label>" + String(blocksLeft).escapeHTML() + " <i class='fa fa-question-circle show_popover' data-toggle='tooltip' title='" + blocksLeftTooltip + "' data-placement='right' style='color:#4CAA6E'></i></label></td>" +
					"<td><label>" + String(nextLessee).escapeHTML() + " <i class='fa fa-question-circle show_popover' data-toggle='tooltip' title='" + nextTooltip + "' data-placement='right' style='color:#4CAA6E'></i></label></td>" +
				"</tr>";
			}

			accountLessorTable.find("tbody").empty().append(rows);
			$("#account_lessor_container").show();
			accountLessorTable.find("[data-toggle='tooltip']").tooltip();
		} else {
			accountLessorTable.find("tbody").empty();
			$("#account_lessor_container").hide();
		}

		if (accountLeasingLabel) {
			$("#account_leasing").html(accountLeasingLabel).show();
		} else {
			$("#account_leasing").hide();
		}

		if (accountLeasingStatus) {
			$("#account_leasing_status").html(accountLeasingStatus).show();
		} else {
			$("#account_leasing_status").hide();
		}
	};

	NRS.updateAccountControlStatus = function() {
		var onNoPhasingOnly = function() {
			$("#setup_mandatory_approval").show();
			$("#mandatory_approval_details").hide();
			delete NRS.accountInfo.phasingOnly;
		};
		if (NRS.accountInfo.accountControls && $.inArray('PHASING_ONLY', NRS.accountInfo.accountControls) > -1) {
			NRS.sendRequest("getPhasingOnlyControl", {
				"account": NRS.account
			}, function (response) {
				if (response && response.votingModel >= 0) {
					$("#setup_mandatory_approval").hide();
					$("#mandatory_approval_details").show();

					NRS.accountInfo.phasingOnly = response;
					var infoTable = $("#mandatory_approval_info_table");
					infoTable.find("tbody").empty();
					var data = {};
					var params = NRS.phasingControlObjectToPhasingParams(response);
					params.phasingWhitelist = params.phasingWhitelisted;
					NRS.getPhasingDetails(data, params);
					delete data.full_hash_formatted_html;
					if (response.minDuration) {
						data.minimum_duration_short = response.minDuration;
					}
					if (response.maxDuration) {
						data.maximum_duration_short = response.maxDuration;
					}
					if (response.maxFees) {
						data.maximum_fees = NRS.convertToNXT(response.maxFees);
					}
					infoTable.find("tbody").append(NRS.createInfoTable(data));
					infoTable.show();
				} else {
					onNoPhasingOnly();
				}
			});
		} else {
			onNoPhasingOnly();
		}
	};

	NRS.checkAssetDifferences = function(current_balances, previous_balances) {
		var current_balances_ = {};
		var previous_balances_ = {};

		if (previous_balances && previous_balances.length) {
			for (var k in previous_balances) {
                if (!previous_balances.hasOwnProperty(k)) {
                    continue;
                }
				previous_balances_[previous_balances[k].asset] = previous_balances[k].balanceQNT;
			}
		}

		if (current_balances && current_balances.length) {
			for (k in current_balances) {
                if (!current_balances.hasOwnProperty(k)) {
                    continue;
                }
				current_balances_[current_balances[k].asset] = current_balances[k].balanceQNT;
			}
		}

		var diff = {};

		for (k in previous_balances_) {
            if (!previous_balances_.hasOwnProperty(k)) {
                continue;
            }
			if (!(k in current_balances_)) {
				diff[k] = "-" + previous_balances_[k];
			} else if (previous_balances_[k] !== current_balances_[k]) {
                diff[k] = (new BigInteger(current_balances_[k]).subtract(new BigInteger(previous_balances_[k]))).toString();
			}
		}

		for (k in current_balances_) {
            if (!current_balances_.hasOwnProperty(k)) {
                continue;
            }
			if (!(k in previous_balances_)) {
				diff[k] = current_balances_[k]; // property is new
			}
		}

		var nr = Object.keys(diff).length;
		if (nr == 0) {
        } else if (nr <= 3) {
			for (k in diff) {
                if (!diff.hasOwnProperty(k)) {
                    continue;
                }
				NRS.sendRequest("getAsset", {
					"asset": k,
					"_extra": {
						"asset": k,
						"difference": diff[k]
					}
				}, function(asset, input) {
					if (asset.errorCode) {
						return;
					}
					asset.difference = input["_extra"].difference;
					asset.asset = input["_extra"].asset;
                    var quantity;
					if (asset.difference.charAt(0) != "-") {
						quantity = NRS.formatQuantity(asset.difference, asset.decimals);

						if (quantity != "0") {
							if (parseInt(quantity) == 1) {
								$.growl($.t("you_received_assets", {
									"name": NRS.escapeRespStr(asset.name)
								}), {
									"type": "success"
								});
							} else {
								$.growl($.t("you_received_assets_plural", {
									"name": NRS.escapeRespStr(asset.name),
									"count": quantity
								}), {
									"type": "success"
								});
							}
							NRS.loadAssetExchangeSidebar();
						}
					} else {
						asset.difference = asset.difference.substring(1);
						quantity = NRS.formatQuantity(asset.difference, asset.decimals);
						if (quantity != "0") {
							if (parseInt(quantity) == 1) {
								$.growl($.t("you_sold_assets", {
									"name": NRS.escapeRespStr(asset.name)
								}), {
									"type": "success"
								});
							} else {
								$.growl($.t("you_sold_assets_plural", {
									"name": NRS.escapeRespStr(asset.name),
									"count": quantity
								}), {
									"type": "success"
								});
							}
							NRS.loadAssetExchangeSidebar();
						}
					}
				});
			}
		} else {
			$.growl($.t("multiple_assets_differences"), {
				"type": "success"
			});
		}
	};

	NRS.updateBlockchainDownloadProgress = function() {
		var lastNumBlocks = 5000;
        var downloadingBlockchain = $('#downloading_blockchain');
        downloadingBlockchain.find('.last_num_blocks').html($.t('last_num_blocks', { "blocks": lastNumBlocks }));

		if (NRS.state.isLightClient) {
			downloadingBlockchain.hide();
		} else if (!NRS.serverConnect || !NRS.peerConnect) {
			downloadingBlockchain.show();
			downloadingBlockchain.find(".db_active").hide();
			downloadingBlockchain.find(".db_halted").show();
		} else {
			downloadingBlockchain.show();
			downloadingBlockchain.find(".db_halted").hide();
			downloadingBlockchain.find(".db_active").show();

			var percentageTotal = 0;
			var blocksLeft;
			var percentageLast = 0;
			if (NRS.state.lastBlockchainFeederHeight && NRS.state.numberOfBlocks <= NRS.state.lastBlockchainFeederHeight) {
				percentageTotal = parseInt(Math.round((NRS.state.numberOfBlocks / NRS.state.lastBlockchainFeederHeight) * 100), 10);
				blocksLeft = NRS.state.lastBlockchainFeederHeight - NRS.state.numberOfBlocks;
				if (blocksLeft <= lastNumBlocks && NRS.state.lastBlockchainFeederHeight > lastNumBlocks) {
					percentageLast = parseInt(Math.round(((lastNumBlocks - blocksLeft) / lastNumBlocks) * 100), 10);
				}
			}
			if (!blocksLeft || blocksLeft < parseInt(lastNumBlocks / 2)) {
				downloadingBlockchain.find(".db_progress_total").hide();
			} else {
				downloadingBlockchain.find(".db_progress_total").show();
				downloadingBlockchain.find(".db_progress_total .progress-bar").css("width", percentageTotal + "%");
				downloadingBlockchain.find(".db_progress_total .sr-only").html($.t("percent_complete", {
					"percent": percentageTotal
				}));
			}
			if (!blocksLeft || blocksLeft >= (lastNumBlocks * 2) || NRS.state.lastBlockchainFeederHeight <= lastNumBlocks) {
				downloadingBlockchain.find(".db_progress_last").hide();
			} else {
				downloadingBlockchain.find(".db_progress_last").show();
				downloadingBlockchain.find(".db_progress_last .progress-bar").css("width", percentageLast + "%");
				downloadingBlockchain.find(".db_progress_last .sr-only").html($.t("percent_complete", {
					"percent": percentageLast
				}));
			}
			if (blocksLeft) {
				downloadingBlockchain.find(".blocks_left_outer").show();
				downloadingBlockchain.find(".blocks_left").html($.t("blocks_left", { "numBlocks": blocksLeft }));
			}
		}
	};

	NRS.checkIfOnAFork = function() {
		if (!NRS.downloadingBlockchain) {
			var isForgingAllBlocks = true;
			if (NRS.blocks && NRS.blocks.length >= 10) {
				for (var i = 0; i < 10; i++) {
					if (NRS.blocks[i].generator != NRS.account) {
						isForgingAllBlocks = false;
						break;
					}
				}
			} else {
				isForgingAllBlocks = false;
			}

			if (isForgingAllBlocks) {
				$.growl($.t("fork_warning"), {
					"type": "danger"
				});
			}

            if (NRS.blocks && NRS.blocks.length > 0 && NRS.baseTargetPercent(NRS.blocks[0]) > 1500 && !NRS.isTestNet) {
                $.growl($.t("fork_warning_base_target"), {
                    "type": "danger"
                });
            }
		}
	};

    NRS.printEnvInfo = function() {
        NRS.logProperty("navigator.userAgent");
        NRS.logProperty("navigator.platform");
        NRS.logProperty("navigator.appVersion");
        NRS.logProperty("navigator.appName");
        NRS.logProperty("navigator.appCodeName");
        NRS.logProperty("navigator.hardwareConcurrency");
        NRS.logProperty("navigator.maxTouchPoints");
        NRS.logProperty("navigator.languages");
        NRS.logProperty("navigator.language");
        NRS.logProperty("navigator.userLanguage");
        NRS.logProperty("navigator.cookieEnabled");
        NRS.logProperty("navigator.onLine");
		if (window["cordova"]) {
			NRS.logProperty("device.model");
			NRS.logProperty("device.platform");
			NRS.logProperty("device.version");
		}
        NRS.logProperty("NRS.isTestNet");
        NRS.logProperty("NRS.needsAdminPassword");
    };

	$("#id_search").on("submit", function(e) {
		e.preventDefault();

		var id = $.trim($("#id_search").find("input[name=q]").val());

		if (NRS.isRsAccount(id)) {
			NRS.sendRequest("getAccount", {
				"account": id
			}, function(response, input) {
				if (!response.errorCode) {
					response.account = input.account;
					NRS.showAccountModal(response);
				} else {
					$.growl($.t("error_search_no_results"), {
						"type": "danger"
					});
				}
			});
		} else {
			if (!NRS.isNumericAccount(id)) {
				$.growl($.t("error_search_invalid"), {
					"type": "danger"
				});
				return;
			}
			NRS.sendRequest("getTransaction", {
				"transaction": id
			}, function(response, input) {
				if (!response.errorCode) {
					response.transaction = input.transaction;
					NRS.showTransactionModal(response);
				} else {
					NRS.sendRequest("getAccount", {
						"account": id
					}, function(response, input) {
						if (!response.errorCode) {
							response.account = input.account;
							NRS.showAccountModal(response);
						} else {
							NRS.sendRequest("getBlock", {
								"block": id,
                                "includeTransactions": "true",
								"includeExecutedPhased": "true"
							}, function(response) {
								if (!response.errorCode) {
									NRS.showBlockModal(response);
								} else {
                                    NRS.sendRequest("getBlock", {
                                        "height": id,
                                        "includeTransactions": "true",
                                        "includeExecutedPhased": "true"
                                    }, function(response) {
                                        if (!response.errorCode) {
                                            NRS.showBlockModal(response);
                                        } else {
                                            $.growl($.t("error_search_no_results"), {
                                                "type": "danger"
                                            });
                                        }
                                    });
								}
							});
						}
					});
				}
			});
		}
	});

	function checkLocalStorage() {
	    var storage;
	    var fail;
	    var uid;
	    try {
	        uid = String(new Date());
	        (storage = window.localStorage).setItem(uid, uid);
	        fail = storage.getItem(uid) != uid;
	        storage.removeItem(uid);
	        fail && (storage = false);
	    } catch (exception) {
	        NRS.logConsole("checkLocalStorage " + exception.message)
	    }
	    return storage;
	}

	return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
} else {
    $(document).ready(function() {
        console.log("document.ready");
        NRS.init();
    });
}
