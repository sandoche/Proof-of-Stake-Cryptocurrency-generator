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
 * @depends {nrs.modals.js}
 */
var NRS = (function(NRS, $) {
	NRS.forms.startForgingComplete = function(response, data) {
		if ("deadline" in response) {
            setForgingIndicatorStatus(NRS.constants.FORGING);
			forgingIndicator.find("span").html($.t(NRS.constants.FORGING)).attr("data-i18n", "forging");
			NRS.forgingStatus = NRS.constants.FORGING;
            NRS.isAccountForging = true;
			$.growl($.t("success_start_forging"), {
				type: "success"
			});
		} else {
            NRS.isAccountForging = false;
			$.growl($.t("error_start_forging"), {
				type: 'danger'
			});
		}
	};

	NRS.forms.stopForgingComplete = function(response, data) {
		if ($("#stop_forging_modal").find(".show_logout").css("display") == "inline") {
			NRS.logout();
			return;
		}
        if (response.foundAndStopped || (response.stopped && response.stopped > 0)) {
            NRS.isAccountForging = false;
            if (!response.forgersCount || response.forgersCount == 0) {
                setForgingIndicatorStatus(NRS.constants.NOT_FORGING);
                forgingIndicator.find("span").html($.t(NRS.constants.NOT_FORGING)).attr("data-i18n", "forging");
            }
            $.growl($.t("success_stop_forging"), {
				type: 'success'
			});
		} else {
			$.growl($.t("error_stop_forging"), {
				type: 'danger'
			});
		}
	};

	var forgingIndicator = $("#forging_indicator");
	forgingIndicator.click(function(e) {
		e.preventDefault();

        if (NRS.state.isLightClient) {
            $.growl($.t("error_forging_light_client"), {
                "type": "danger"
            });
        } else if (NRS.downloadingBlockchain) {
			$.growl($.t("error_forging_blockchain_downloading"), {
				"type": "danger"
			});
		} else if (NRS.state.isScanning) {
			$.growl($.t("error_forging_blockchain_rescanning"), {
				"type": "danger"
			});
		} else if (!NRS.accountInfo.publicKey) {
			$.growl($.t("error_forging_no_public_key"), {
				"type": "danger"
			});
		} else if (NRS.accountInfo.effectiveBalanceNXT == 0) {
			if (NRS.lastBlockHeight >= NRS.accountInfo.currentLeasingHeightFrom && NRS.lastBlockHeight <= NRS.accountInfo.currentLeasingHeightTo) {
				$.growl($.t("error_forging_lease"), {
					"type": "danger"
				});
			} else {
				$.growl($.t("error_forging_effective_balance"), {
					"type": "danger"
				});
			}
		} else if (NRS.isAccountForging) {
			$("#stop_forging_modal").modal("show");
		} else {
			$("#start_forging_modal").modal("show");
		}
	});

	forgingIndicator.hover(
		function() {
            NRS.updateForgingStatus();
        }
	);

    NRS.getForgingTooltip = function(data) {
        if (!data || data.account == NRS.accountInfo.account) {
            NRS.isAccountForging = true;
            return $.t("forging_tooltip", {"balance": NRS.accountInfo.effectiveBalanceNXT});
        }
        return $.t("forging_another_account_tooltip", {"accountRS": data.accountRS });
    };

    NRS.updateForgingTooltip = function(tooltip) {
        $("#forging_indicator").attr('title', tooltip).tooltip('fixTitle');
    };

    function setForgingIndicatorStatus(status) {
        var forgingIndicator = $("#forging_indicator");
        forgingIndicator.removeClass(NRS.constants.FORGING);
        forgingIndicator.removeClass(NRS.constants.NOT_FORGING);
        forgingIndicator.removeClass(NRS.constants.UNKNOWN);
        forgingIndicator.addClass(status);
    }

    NRS.updateForgingStatus = function(secretPhrase) {
        var forgingIndicator = $("#forging_indicator");
        if (!NRS.isForgingSupported()) {
            forgingIndicator.hide();
            return;
        }
        var status = NRS.forgingStatus;
        var tooltip = forgingIndicator.attr('title');
        if (NRS.state.isLightClient) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_light_client");
        } else if (!NRS.accountInfo.publicKey) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_no_public_key");
        } else if (NRS.isLeased) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_lease");
        } else if (NRS.accountInfo.effectiveBalanceNXT == 0) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_effective_balance");
        } else if (NRS.downloadingBlockchain) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_blockchain_downloading");
        } else if (NRS.state.isScanning) {
            status = NRS.constants.NOT_FORGING;
            tooltip = $.t("error_forging_blockchain_rescanning");
        } else if (NRS.needsAdminPassword && NRS.getAdminPassword() == "" && (!secretPhrase || !NRS.isForgingSafe())) {
            // do not change forging status
        } else {
            var params = {};
            if (NRS.needsAdminPassword && NRS.getAdminPassword() != "") {
                params["adminPassword"] = NRS.getAdminPassword();
            }
            if (secretPhrase && NRS.needsAdminPassword && NRS.getAdminPassword() == "") {
                params["secretPhrase"] = secretPhrase;
            }
            NRS.sendRequest("getForging", params, function (response) {
                NRS.isAccountForging = false;
                if ("account" in response) {
                    status = NRS.constants.FORGING;
                    tooltip = NRS.getForgingTooltip(response);
                    NRS.isAccountForging = true;
                } else if ("generators" in response) {
                    if (response.generators.length == 0) {
                        status = NRS.constants.NOT_FORGING;
                        tooltip = $.t("not_forging_not_started_tooltip");
                    } else {
                        status = NRS.constants.FORGING;
                        if (response.generators.length == 1) {
                            tooltip = NRS.getForgingTooltip(response.generators[0]);
                        } else {
                            tooltip = $.t("forging_more_than_one_tooltip", { "generators": response.generators.length });
                            for (var i=0; i< response.generators.length; i++) {
                                if (response.generators[i].account == NRS.accountInfo.account) {
                                    NRS.isAccountForging = true;
                                }
                            }
                            if (NRS.isAccountForging) {
                                tooltip += ", " + $.t("forging_current_account_true");
                            } else {
                                tooltip += ", " + $.t("forging_current_account_false");
                            }
                        }
                    }
                } else {
                    status = NRS.constants.UNKNOWN;
                    tooltip = NRS.escapeRespStr(response.errorDescription);
                }
            }, { isAsync: false });
        }
        setForgingIndicatorStatus(status);
        if (status == NRS.constants.NOT_FORGING) {
            NRS.isAccountForging = false;
        }
        forgingIndicator.find("span").html($.t(status)).attr("data-i18n", status);
        forgingIndicator.show();
        NRS.forgingStatus = status;
        NRS.updateForgingTooltip(tooltip);
    };

	return NRS;
}(NRS || {}, jQuery));