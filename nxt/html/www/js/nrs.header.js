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
var NRS = (function(NRS, $) {

    function widgetVisibility(widget, depends) {
        if (NRS.isApiEnabled(depends)) {
            widget.show();
        } else {
            widget.hide();
        }
    }

    $(window).on('load', function() {
        widgetVisibility($("#header_send_money"), { apis: [NRS.constants.REQUEST_TYPES.sendMoney] });
        widgetVisibility($("#header_transfer_currency"), { apis: [NRS.constants.REQUEST_TYPES.transferCurrency] });
        widgetVisibility($("#header_send_message"), { apis: [NRS.constants.REQUEST_TYPES.sendMessage] });
        if (!NRS.isFundingMonitorSupported()) {
            $("#funding_monitor_menu_item").hide();
        }
        if (!NRS.isExternalLinkVisible()) {
            $("#api_console_li").hide();
            $("#database_shell_li").hide();
        }
        if (!NRS.isWebWalletLinkVisible()) {
            $("#web_wallet_li").remove();
        }
    });

    $("#refreshSearchIndex").on("click", function() {
        NRS.sendRequest("luceneReindex", {
            adminPassword: NRS.getAdminPassword()
        }, function (response) {
            if (response.errorCode) {
                $.growl(NRS.escapeRespStr(response.errorDescription));
            } else {
                $.growl($.t("search_index_refreshed"));
            }
        })
    });

    $("#header_open_web_wallet").on("click", function() {
        if (java) {
            java.openBrowser(NRS.accountRS);
        }
    });

    $("#client_status_modal").on("show.bs.modal", function() {
        if (NRS.isMobileApp()) {
            $("#client_status_description").text($.t("mobile_client_description", { url: NRS.getRemoteNodeUrl() }));
            $("#client_status_set_peer").hide();
            $("#client_status_remote_peer_container").hide();
            $("#client_status_blacklist_peer").hide();
            return;
        } else if (NRS.state.isLightClient) {
            $("#client_status_description").text($.t("light_client_description"));
        } else {
            $("#client_status_description").text($.t("api_proxy_description"));
        }
        if (NRS.state.apiProxyPeer) {
            $("#client_status_remote_peer").val(String(NRS.state.apiProxyPeer).escapeHTML());
            $("#client_status_set_peer").prop('disabled', true);
            $("#client_status_blacklist_peer").prop('disabled', false);
        } else {
            $("#client_status_remote_peer").val("");
            $("#client_status_set_peer").prop('disabled', false);
            $("#client_status_blacklist_peer").prop('disabled', true);
        }
        NRS.updateConfirmationsTable();
    });

    $("#client_status_remote_peer").keydown(function() {
        if ($(this).val() == NRS.state.apiProxyPeer) {
            $("#client_status_set_peer").prop('disabled', true);
            $("#client_status_blacklist_peer").prop('disabled', false);
        } else {
            $("#client_status_set_peer").prop('disabled', false);
            $("#client_status_blacklist_peer").prop('disabled', true);
        }
    });

    NRS.forms.setAPIProxyPeer = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        data.adminPassword = NRS.getAdminPassword();
        return {
            "data": data
        };
    };

    NRS.forms.setAPIProxyPeerComplete = function(response) {
        var announcedAddress = response.announcedAddress;
        if (announcedAddress) {
            NRS.state.apiProxyPeer = announcedAddress;
            $.growl($.t("remote_peer_updated", { peer: String(announcedAddress).escapeHTML() }));
        } else {
            $.growl($.t("remote_peer_selected_by_server"));
        }
        NRS.updateDashboardMessage();
    };

    NRS.forms.blacklistAPIProxyPeer = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        data.adminPassword = NRS.getAdminPassword();
        return {
            "data": data
        };
    };

    NRS.forms.blacklistAPIProxyPeerComplete = function(response) {
        if (response.done) {
            NRS.state.apiProxyPeer = null;
            $.growl($.t("remote_peer_blacklisted"));
        }
        NRS.updateDashboardMessage();
    };

    $(".external-link").on('click', function(e) {
        if (!NRS.isMobileApp()) {
            return;
        }
        e.preventDefault();
        window.open($(this).attr('href'), '_system');
        return false;
    });

    $("#passphrase_validation_modal").on("show.bs.modal", function() {
        $("#passphrae_validation_account").val(NRS.accountRS);
    });

    NRS.forms.validatePassphrase = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var secretPhrase = data.secretPhrase;
        var account = data.account;
        var calculatedAccount = NRS.getAccountId(secretPhrase, true);
        if (account == calculatedAccount) {
            $(".btn-passphrase-validation").removeClass("btn-danger").addClass("btn-success");
            var publicKey = NRS.getPublicKey(converters.stringToHexString(secretPhrase));
            $("#passphrae_validation_public_key").val(publicKey);
            return {
                "successMessage": $.t("correct_passphrase"),
                "stop": true,
                "keepOpen": true
            };
        } else {
            $("#passphrae_validation_public_key").val("");
            return {
                "error": $.t("wrong_passphrase"),
                "stop": true,
                "keepOpen": true
            };
        }
    };

    NRS.getPassphraseValidationLink = function(isPassphraseLogin) {
        var label;
        if (isPassphraseLogin) {
            label = $.t("validate_passphrase");
        } else {
            label = $.t("account_public_key");
        }
        return "<br/><a href='#' class='btn btn-xs btn-danger btn-passphrase-validation' data-toggle='modal' data-target='#passphrase_validation_modal'>" + label + "</a>";
    };

    return NRS;
}(NRS || {}, jQuery));