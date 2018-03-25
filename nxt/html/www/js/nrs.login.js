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
var NRS = (function(NRS, $, undefined) {
	NRS.newlyCreatedAccount = false;

	NRS.allowLoginViaEnter = function() {
		$("#login_account_other").keypress(function(e) {
			if (e.which == '13') {
				e.preventDefault();
				var account = $("#login_account_other").val();
				NRS.login(false,account);
			}
		});
		$("#login_password").keypress(function(e) {
			if (e.which == '13') {
				e.preventDefault();
				var password = $("#login_password").val();
				NRS.login(true,password, { isPreventLoginToNewAccount: true });
			}
		});
	};

	NRS.showLoginOrWelcomeScreen = function() {
		if (localStorage.getItem("logged_in")) {
			NRS.showLoginScreen();
		} else {
			NRS.showWelcomeScreen();
		}
	};

	NRS.showLoginScreen = function() {
		$("#account_phrase_custom_panel, #account_phrase_generator_panel, #welcome_panel, #custom_passphrase_link").hide();
		$("#account_phrase_custom_panel").find(":input:not(:button):not([type=submit])").val("");
		$("#account_phrase_generator_panel").find(":input:not(:button):not([type=submit])").val("");
        $("#login_account_other").mask("NXT-****-****-****-*****");
		if (NRS.isMobileApp()) {
            $(".mobile-only").show();
        }
        $("#login_panel").show();
	};

	NRS.showWelcomeScreen = function() {
		$("#login_panel, #account_phrase_generator_panel, #account_phrase_custom_panel, #welcome_panel, #custom_passphrase_link").hide();
        if (NRS.isMobileApp()) {
            $(".mobile-only").show();
        }
		$("#welcome_panel").show();
	};

    NRS.createPassphraseToConfirmPassphrase = function() {
        if ($("#confirm_passphrase_warning").is(":checked")) {
            $('.step_2').hide();$('.step_3').show();
        } else {
            $("#confirm_passphrase_warning_container").css("background-color", "red");
		}
    };

	NRS.registerUserDefinedAccount = function() {
		$("#account_phrase_generator_panel, #login_panel, #welcome_panel, #custom_passphrase_link").hide();
		$("#account_phrase_generator_panel").find(":input:not(:button):not([type=submit])").val("");
		var accountPhraseCustomPanel = $("#account_phrase_custom_panel");
        accountPhraseCustomPanel.find(":input:not(:button):not([type=submit])").val("");
		accountPhraseCustomPanel.show();
		$("#registration_password").focus();
	};

	NRS.registerAccount = function() {
		$("#login_panel, #welcome_panel").hide();
		var accountPhraseGeneratorPanel = $("#account_phrase_generator_panel");
        accountPhraseGeneratorPanel.show();
		accountPhraseGeneratorPanel.find(".step_3 .callout").hide();

		var $loading = $("#account_phrase_generator_loading");
		var $loaded = $("#account_phrase_generator_loaded");
		if (NRS.isWindowPrintSupported()) {
            $(".paper-wallet-link-container").show();
		}

		//noinspection JSUnresolvedVariable
		if (window.crypto || window.msCrypto) {
			$loading.find("span.loading_text").html($.t("generating_passphrase_wait"));
		}

		$loading.show();
		$loaded.hide();

		if (typeof PassPhraseGenerator == "undefined") {
			$.when(
				$.getScript("js/crypto/passphrasegenerator.js")
			).done(function() {
				$loading.hide();
				$loaded.show();

				PassPhraseGenerator.generatePassPhrase("#account_phrase_generator_panel");
			}).fail(function() {
				alert($.t("error_word_list"));
			});
		} else {
			$loading.hide();
			$loaded.show();

			PassPhraseGenerator.generatePassPhrase("#account_phrase_generator_panel");
		}
	};

    $("#generator_paper_wallet_link").click(function(e) {
    	e.preventDefault();
        NRS.printPaperWallet($("#account_phrase_generator_panel").find(".step_2 textarea").val());
    });

	NRS.verifyGeneratedPassphrase = function() {
		var accountPhraseGeneratorPanel = $("#account_phrase_generator_panel");
        var password = $.trim(accountPhraseGeneratorPanel.find(".step_3 textarea").val());

		if (password != PassPhraseGenerator.passPhrase) {
			accountPhraseGeneratorPanel.find(".step_3 .callout").show();
		} else {
			NRS.newlyCreatedAccount = true;
			NRS.login(true,password);
			PassPhraseGenerator.reset();
			accountPhraseGeneratorPanel.find("textarea").val("");
			accountPhraseGeneratorPanel.find(".step_3 .callout").hide();
		}
	};

	$("#account_phrase_custom_panel").find("form").submit(function(event) {
		event.preventDefault();

		var password = $("#registration_password").val();
		var repeat = $("#registration_password_repeat").val();

		var error = "";

		if (password.length < 35) {
			error = $.t("error_passphrase_length");
		} else if (password.length < 50 && (!password.match(/[A-Z]/) || !password.match(/[0-9]/))) {
			error = $.t("error_passphrase_strength");
		} else if (password != repeat) {
			error = $.t("error_passphrase_match");
		}

		if (error) {
			$("#account_phrase_custom_panel").find(".callout").first().removeClass("callout-info").addClass("callout-danger").html(error);
		} else {
			$("#registration_password, #registration_password_repeat").val("");
			NRS.login(true,password);
		}
	});

	NRS.listAccounts = function() {
		var loginAccount = $('#login_account');
        loginAccount.empty();
		if (NRS.getStrItem("savedNxtAccounts") && NRS.getStrItem("savedNxtAccounts") != ""){
			$('#login_account_container').show();
			$('#login_account_container_other').hide();
			var accounts = NRS.getStrItem("savedNxtAccounts").split(";");
			$.each(accounts, function(index, account) {
				if (account != ''){
					$('#login_account')
					.append($("<li></li>")
						.append($("<a></a>")
							.attr("href","#")
							.attr("onClick","NRS.login(false,'"+account+"')")
							.text(account))
						.append($('<button data-dismiss="modal" class="close" type="button">×</button>')
							.attr("onClick","NRS.removeAccount('"+account+"')"))
					);
				}
			});
			var otherHTML = "<li><a href='#' data-i18n='other'>Other</a></li>";
			var $otherHTML = $(otherHTML);
			$otherHTML.click(function() {
				$('#login_account_container').hide();
				$('#login_account_container_other').show();
			});
			$otherHTML.appendTo(loginAccount);
		}
		else{
			$('#login_account_container').hide();
			$('#login_account_container_other').show();
		}
	};

	NRS.switchAccount = function(account) {
		// Reset security related state
		NRS.resetEncryptionState();
		NRS.setServerPassword(null);
		NRS.setAccountDetailsPassword(null);
		NRS.rememberPassword = false;
		NRS.account = "";
		NRS.accountRS = "";
		NRS.publicKey = "";
		NRS.accountInfo = {};

		// Reset other functional state
		$("#account_balance, #account_balance_sidebar, #account_nr_assets, #account_assets_balance, #account_currencies_balance, #account_nr_currencies, #account_purchase_count, #account_pending_sale_count, #account_completed_sale_count, #account_message_count, #account_alias_count").html("0");
		$("#id_search").find("input[name=q]").val("");
		NRS.resetAssetExchangeState();
		NRS.resetPollsState();
		NRS.resetMessagesState();
		NRS.forgingStatus = NRS.constants.UNKNOWN;
		NRS.isAccountForging = false;
		NRS.selectedContext = null;

		// Reset plugins state
		NRS.activePlugins = false;
		NRS.numRunningPlugins = 0;
		$.each(NRS.plugins, function(pluginId) {
			NRS.determinePluginLaunchStatus(pluginId);
		});

		// Return to the dashboard and notify the user
		NRS.goToPage("dashboard");
        NRS.login(false, account, function() {
            $.growl($.t("switched_to_account", { account: account }))
        }, { isAccountSwitch: true } );
	};

    $("#loginButtons").find(".btn").click(function (e) {
        e.preventDefault();
        var type = $(this).data("login-type");
        var readerId = $(this).data("reader");
        var reader = $("#" + readerId);
        if (reader.is(':visible') && type != "scan") {
            NRS.scanQRCode(readerId, function() {}); // turn off scanning
        }
        if (type == "account") {
            NRS.listAccounts();
            $('#login_password').parent().hide();
        } else if (type == "password") {
            $('#login_account_container').hide();
            $('#login_account_container_other').hide();
            $('#login_password').parent().show();
        } else if (type == "scan" && !reader.is(':visible')) {
            NRS.scanQRCode(readerId, function(text) {
                var nxtAddress = new NxtAddress();
                if (nxtAddress.set(text)) {
                    if ($("#remember_me").is(":checked")) {
                        rememberAccount(text);
                    }
                    NRS.login(false, text);
                } else {
                    NRS.login(true, text);
                }
            });
        }
    });

	NRS.removeAccount = function(account) {
		var accounts = NRS.getStrItem("savedNxtAccounts").replace(account+';','');
		if (accounts == '') {
			NRS.removeItem('savedNxtAccounts');
		} else {
			NRS.setStrItem("savedNxtAccounts", accounts);
		}
		NRS.listAccounts();
	};

    function rememberAccount(account) {
        var accountsStr = NRS.getStrItem("savedNxtAccounts");
        if (!accountsStr) {
            NRS.setStrItem("savedNxtAccounts", account + ";");
            return;
        }
        var accounts = accountsStr.split(";");
        if (accounts.indexOf(account) >= 0) {
            return;
        }
        NRS.setStrItem("savedNxtAccounts", accountsStr + account + ";");
    }

    // id can be either account id or passphrase
    NRS.login = function(isPassphraseLogin, id, callback, options) {
        if (!options) {
            options = {};
        }
        NRS.logConsole("login isPassphraseLogin = " + isPassphraseLogin +
            ", isAccountSwitch = " + options.isAccountSwitch +
            ", isSavedPassphrase = " + options.isSavedPassphrase +
            ", isPreventLoginToNewAccount = " + options.isPreventLoginToNewAccount);
        NRS.spinner.spin($("#center")[0]);
        if (isPassphraseLogin && !options.isSavedPassphrase){
			var loginCheckPasswordLength = $("#login_check_password_length");
			if (!id.length) {
				$.growl($.t("error_passphrase_required_login"), {
					"type": "danger",
					"offset": 10
				});
                NRS.spinner.stop();
				return;
			} else if (!NRS.isTestNet && id.length < 12 && loginCheckPasswordLength.val() == 1) {
				loginCheckPasswordLength.val(0);
				var loginError = $("#login_error");
				loginError.find(".callout").html($.t("error_passphrase_login_length"));
				loginError.show();
                NRS.spinner.stop();
				return;
			}

			$("#login_password, #registration_password, #registration_password_repeat").val("");
			loginCheckPasswordLength.val(1);
		}

		console.log("login calling getBlockchainStatus");
		NRS.sendRequest("getBlockchainStatus", {}, function(response) {
			if (response.errorCode) {
			    NRS.connectionError(response.errorDescription, response.errorCode);
                NRS.spinner.stop();
				console.log("getBlockchainStatus returned error");
				return;
			}
			console.log("getBlockchainStatus response received");
			NRS.state = response;
			var accountRequest;
			var requestVariable;
			if (isPassphraseLogin) {
				accountRequest = "getAccountId"; // Processed locally, not submitted to server
				requestVariable = {secretPhrase: id};
			} else {
				accountRequest = "getAccount";
				requestVariable = {account: id};
			}
			console.log("calling " + accountRequest);
			NRS.sendRequest(accountRequest, requestVariable, function(response, data) {
				console.log(accountRequest + " response received");
				if (!response.errorCode) {
					NRS.account = NRS.escapeRespStr(response.account);
					NRS.accountRS = NRS.escapeRespStr(response.accountRS);
					if (isPassphraseLogin) {
                        NRS.publicKey = NRS.getPublicKey(converters.stringToHexString(id));
                    } else {
                        NRS.publicKey = NRS.escapeRespStr(response.publicKey);
                    }
				}
                if (response.errorCode == 5) {
                    if (isPassphraseLogin && options.isPreventLoginToNewAccount) {
                        var accountRS = NRS.getAccountId(id, true);
                        $.growl($.t("passphrase_login_to_new_account", { account: accountRS }), {
                            "type": "danger", "delay": "10000"
                        });
                        NRS.spinner.stop();
                        return;
                    } else {
                        NRS.account = NRS.escapeRespStr(response.account);
                        NRS.accountRS = NRS.escapeRespStr(response.accountRS);
                        if (isPassphraseLogin) {
                            NRS.publicKey = NRS.getPublicKey(converters.stringToHexString(id));
                        }
                    }
                }
				if (response.errorCode == 19 || response.errorCode == 21) {
                    $.growl($.t("light_client_connecting_to_network"), {
                                    "type": "danger",
                                    "offset": 10
                                });
                    NRS.spinner.stop();
                    return;
				}
				if (!NRS.account) {
					$.growl($.t("error_find_account_id", { accountRS: (data && data.account ? String(data.account).escapeHTML() : "") }), {
						"type": "danger",
						"offset": 10
					});
                    NRS.spinner.stop();
					return;
				} else if (!NRS.accountRS) {
					$.growl($.t("error_generate_account_id"), {
						"type": "danger",
						"offset": 10
					});
                    NRS.spinner.stop();
					return;
				}

				NRS.sendRequest("getAccountPublicKey", {
					"account": NRS.account
				}, function(response) {
					if (response && response.publicKey && response.publicKey != NRS.generatePublicKey(id) && isPassphraseLogin) {
						$.growl($.t("error_account_taken"), {
							"type": "danger",
							"offset": 10
						});
                        NRS.spinner.stop();
						return;
					}

					var rememberMe = $("#remember_me");
					if (rememberMe.is(":checked") && isPassphraseLogin) {
						NRS.rememberPassword = true;
						NRS.setPassword(id);
						$(".secret_phrase, .show_secret_phrase").hide();
						$(".hide_secret_phrase").show();
					} else {
                        NRS.rememberPassword = false;
                        NRS.setPassword("");
                        $(".secret_phrase, .show_secret_phrase").show();
                        $(".hide_secret_phrase").hide();
                    }
					NRS.disablePluginsDuringSession = $("#disable_all_plugins").is(":checked");
					$("#sidebar_account_id").html(String(NRS.accountRS).escapeHTML());
					$("#sidebar_account_link").html(NRS.getAccountLink(NRS, "account", NRS.accountRS, "details", false, "btn btn-default btn-xs"));
					if (NRS.lastBlockHeight == 0 && NRS.state.numberOfBlocks) {
						NRS.checkBlockHeight(NRS.state.numberOfBlocks - 1);
					}
					if (NRS.lastBlockHeight == 0 && NRS.lastProxyBlockHeight) {
						NRS.checkBlockHeight(NRS.lastProxyBlockHeight);
					}
                    $("#sidebar_block_link").html(NRS.getBlockLink(NRS.lastBlockHeight));

					var passwordNotice = "";

					if (id.length < 35 && isPassphraseLogin) {
						passwordNotice = $.t("error_passphrase_length_secure");
					} else if (isPassphraseLogin && id.length < 50 && (!id.match(/[A-Z]/) || !id.match(/[0-9]/))) {
						passwordNotice = $.t("error_passphrase_strength_secure");
					}

					if (passwordNotice) {
						$.growl("<strong>" + $.t("warning") + "</strong>: " + passwordNotice, {
							"type": "danger"
						});
					}
					NRS.getAccountInfo(true, function() {
						if (NRS.accountInfo.currentLeasingHeightFrom) {
							NRS.isLeased = (NRS.lastBlockHeight >= NRS.accountInfo.currentLeasingHeightFrom && NRS.lastBlockHeight <= NRS.accountInfo.currentLeasingHeightTo);
						} else {
							NRS.isLeased = false;
						}
						NRS.updateForgingTooltip($.t("forging_unknown_tooltip"));
						NRS.updateForgingStatus(isPassphraseLogin ? id : null);
						if (NRS.isForgingSafe() && isPassphraseLogin) {
							var forgingIndicator = $("#forging_indicator");
							NRS.sendRequest("startForging", {
								"secretPhrase": id
							}, function (response) {
								if ("deadline" in response) {
									forgingIndicator.addClass("forging");
									forgingIndicator.find("span").html($.t("forging")).attr("data-i18n", "forging");
									NRS.forgingStatus = NRS.constants.FORGING;
									NRS.updateForgingTooltip(NRS.getForgingTooltip);
								} else {
									forgingIndicator.removeClass("forging");
									forgingIndicator.find("span").html($.t("not_forging")).attr("data-i18n", "not_forging");
									NRS.forgingStatus = NRS.constants.NOT_FORGING;
									NRS.updateForgingTooltip(response.errorDescription);
								}
								forgingIndicator.show();
							});
						}
					}, options.isAccountSwitch);
					NRS.initSidebarMenu();
					NRS.unlock();

					if (NRS.isOutdated) {
						$.growl($.t("nrs_update_available"), {
							"type": "danger"
						});
					}

					if (!NRS.downloadingBlockchain) {
						NRS.checkIfOnAFork();
					}
					NRS.logConsole("User Agent: " + String(navigator.userAgent));
					if (navigator.userAgent.indexOf('Safari') != -1 &&
						navigator.userAgent.indexOf('Chrome') == -1 &&
						navigator.userAgent.indexOf('JavaFX') == -1) {
						// Don't use account based DB in Safari due to a buggy indexedDB implementation (2015-02-24)
						NRS.createDatabase("NRS_USER_DB");
						$.growl($.t("nrs_safari_no_account_based_db"), {
							"type": "danger"
						});
					} else {
						NRS.createDatabase("NRS_USER_DB_" + String(NRS.account));
					}
					if (callback) {
						callback();
					}

					$.each(NRS.pages, function(key) {
						if(key in NRS.setup) {
							NRS.setup[key]();
						}
					});

					$(".sidebar .treeview").tree();
					$('#dashboard_link').find('a').addClass("ignore").click();

					var accounts;
					if (rememberMe.is(":checked") || NRS.newlyCreatedAccount) {
						rememberAccount(NRS.accountRS);
					}

					$("[data-i18n]").i18n();

					/* Add accounts to dropdown for quick switching */
					var accountIdDropdown = $("#account_id_dropdown");
					accountIdDropdown.find(".dropdown-menu .switchAccount").remove();
					if (NRS.getStrItem("savedNxtAccounts") && NRS.getStrItem("savedNxtAccounts")!=""){
						accountIdDropdown.show();
						accounts = NRS.getStrItem("savedNxtAccounts").split(";");
						$.each(accounts, function(index, account) {
							if (account != ''){
								$('#account_id_dropdown').find('.dropdown-menu')
								.append($("<li class='switchAccount'></li>")
									.append($("<a></a>")
										.attr("href","#")
										.attr("style","font-size: 85%;")
										.attr("onClick","NRS.switchAccount('"+account+"')")
										.text(account))
								);
							}
						});
					} else {
						accountIdDropdown.hide();
					}

					NRS.updateApprovalRequests();
				});
			});
		});
	};

	$("#logout_button_container").on("show.bs.dropdown", function() {
		if (NRS.forgingStatus != NRS.constants.FORGING) {
			$(this).find("[data-i18n='logout_stop_forging']").hide();
		}
	});

	NRS.initPluginWarning = function() {
		if (NRS.activePlugins) {
			var html = "";
			html += "<div style='font-size:13px;'>";
			html += "<div style='background-color:#e6e6e6;padding:12px;'>";
			html += "<span data-i18n='following_plugins_detected'>";
			html += "The following active plugins have been detected:</span>";
			html += "</div>";
			html += "<ul class='list-unstyled' style='padding:11px;border:1px solid #e0e0e0;margin-top:8px;'>";
			$.each(NRS.plugins, function(pluginId, pluginDict) {
				if (pluginDict["launch_status"] == NRS.constants.PL_PAUSED) {
					html += "<li style='font-weight:bold;'>" + pluginDict["manifest"]["name"] + "</li>";
				}
			});
			html += "</ul>";
			html += "</div>";

			$('#lockscreen_active_plugins_overview').popover({
				"html": true,
				"content": html,
				"trigger": "hover"
			});

			html = "";
			html += "<div style='font-size:13px;padding:5px;'>";
			html += "<p data-i18n='plugin_security_notice_full_access'>";
			html += "Plugins are not sandboxed or restricted in any way and have full accesss to your client system including your Nxt passphrase.";
			html += "</p>";
			html += "<p data-i18n='plugin_security_notice_trusted_sources'>";
			html += "Make sure to only run plugins downloaded from trusted sources, otherwise ";
			html += "you can loose your NXT! In doubt don't run plugins with accounts ";
			html += "used to store larger amounts of NXT now or in the future.";
			html += "</p>";
			html += "</div>";

			$('#lockscreen_active_plugins_security').popover({
				"html": true,
				"content": html,
				"trigger": "hover"
			});

			$("#lockscreen_active_plugins_warning").show();
		} else {
			$("#lockscreen_active_plugins_warning").hide();
		}
	};

	NRS.showLockscreen = function() {
		NRS.listAccounts();
		if (localStorage.getItem("logged_in")) {
			NRS.showLoginScreen();
		} else {
			NRS.showWelcomeScreen();
		}

		$("#center").show();
		if (!NRS.isShowDummyCheckbox) {
			$("#dummyCheckbox").hide();
		}
	};

	NRS.unlock = function() {
		if (!localStorage.getItem("logged_in")) {
			localStorage.setItem("logged_in", true);
		}
		$("#lockscreen").hide();
		$("body, html").removeClass("lockscreen");
		$("#login_error").html("").hide();
		$(document.documentElement).scrollTop = 0;
        NRS.spinner.stop();
    };

	NRS.logout = function(stopForging) {
		if (stopForging && NRS.forgingStatus == NRS.constants.FORGING) {
			var stopForgingModal = $("#stop_forging_modal");
            stopForgingModal.find(".show_logout").show();
			stopForgingModal.modal("show");
		} else {
			NRS.setDecryptionPassword("");
			NRS.setPassword("");
			//window.location.reload();
			window.location.href = window.location.pathname;
		}
	};

	$("#logout_clear_user_data_confirm_btn").click(function(e) {
		e.preventDefault();
		if (NRS.database) {
			//noinspection JSUnresolvedFunction
			indexedDB.deleteDatabase(NRS.database.name);
		}
		if (NRS.legacyDatabase) {
			//noinspection JSUnresolvedFunction
			indexedDB.deleteDatabase(NRS.legacyDatabase.name);
		}
		NRS.removeItem("logged_in");
		NRS.removeItem("savedNxtAccounts");
		NRS.removeItem("language");
        NRS.removeItem("savedPassphrase");
		NRS.localStorageDrop("data");
		NRS.localStorageDrop("polls");
		NRS.localStorageDrop("contacts");
		NRS.localStorageDrop("assets");
		NRS.logout();
	});

    NRS.setPassword = function(password) {
		NRS.setEncryptionPassword(password);
		NRS.setServerPassword(password);
        NRS.setAccountDetailsPassword(password);
        NRS.setAdvancedModalPassword(password);
        NRS.setTokenPassword(password);
		if (NRS.mobileSettings.is_store_remembered_passphrase) {
			NRS.setStrItem("savedPassphrase", password);
		} else {
			NRS.setStrItem("savedPassphrase", "");
		}
	};
	return NRS;
}(NRS || {}, jQuery));
