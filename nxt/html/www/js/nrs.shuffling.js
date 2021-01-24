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
    function isErrorResponse(response) {
        return response.errorCode || response.errorDescription || response.errorMessage || response.error;
    }

    NRS.jsondata = NRS.jsondata||{};

    NRS.jsondata.shuffling = function (response, shufflers, amountDecimals) {
        var isShufflerActive = false;
        var recipient;
        var state;
        var error;
        if (shufflers && shufflers.shufflers) {
            for (var i = 0; i < shufflers.shufflers.length; i++) {
                var shuffler = shufflers.shufflers[i];
                if (response.shufflingFullHash == shuffler.shufflingFullHash) {
                    isShufflerActive = true;
                    recipient = shuffler.recipientRS;
                    if (shuffler.participantState != undefined) {
                        state = $.t(NRS.getShufflingParticipantState(shuffler.participantState).toLowerCase());
                    }
                    if (shuffler.failureCause) {
                        error = NRS.escapeRespStr(response.failureCause)
                    }
                    break;
                }
            }
        }
        var shufflerStatus = $.t("unknown");
        var shufflerColor = "gray";

        if (shufflers && shufflers.shufflers) {
            if (isShufflerActive) {
                if (error) {
                    shufflerStatus = error;
                    shufflerColor = "red";
                } else {
                    shufflerStatus = $.t("active");
                    shufflerColor = "green";
                }
            } else {
                shufflerStatus = $.t("inactive");
                shufflerColor = "red";
            }
        }

        var shufflerIndicatorFormatted = "";
        var startShufflerLinkFormatted = "";
        var shufflerStage = "";
        if (response.stage == 4) {
            if (response.participantCount != response.registrantCount) {
                shufflerStage = $.t("expired");
            } else {
                shufflerStage = $.t("failed");
            }
        } else {
            shufflerStage = $.t(NRS.getShufflingStage(response.stage).toLowerCase())
        }
        if (response.stage < 4) {
            shufflerIndicatorFormatted = "<i class='fa fa-circle' style='color:" + shufflerColor + ";'></i>";
            if (!isShufflerActive) {
                startShufflerLinkFormatted = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#m_shuffler_start_modal' " +
                    "data-shuffling='" + response.shuffling + "' " +
                    "data-shufflingfullhash='" + response.shufflingFullHash + "'>" + $.t("start") + "</a>";
            }
        } else {
            shufflerStatus = "";
        }
        return {
            status:
                (function () {
                    if (response.stage > 0) {
                        return "<span>" + $.t("in_progress") + "</span>";
                    }
                    if (!isShufflerActive) {
                        return "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' " +
                            "data-target='#m_shuffler_start_modal' " +
                            "data-shuffling='" + response.shuffling + "' " +
                            "data-shufflingfullhash='" + response.shufflingFullHash + "'>" + $.t("join") + "</a>";
                    }
                    return "<span>" + $.t("already_joined") + "</span>";
                })(),
            shufflingFormatted: NRS.getTransactionLink(response.shuffling),
            stageLabel: shufflerStage,
            shufflerStatus: shufflerStatus,
            shufflerIndicatorFormatted: shufflerIndicatorFormatted,
            startShufflerLinkFormatted: startShufflerLinkFormatted,
            recipientFormatted: recipient,
            stateLabel: state,
            assigneeFormatted: NRS.getAccountLink(response, "assignee"),
            issuerFormatted: NRS.getAccountLink(response, "issuer"),
            amountFormatted: (function () {
                switch (response.holdingType) {
                    case 0: return NRS.formatAmount(response.amount, false, false, amountDecimals);
                    case 1:
                    case 2: return NRS.formatQuantity(response.amount, response.holdingInfo.decimals, false, amountDecimals);
                }
            })(),
            holdingFormatted: (function () {
                switch (response.holdingType) {
                    case 0: return 'NXT';
                    case 1: return NRS.getTransactionLink(response.holding) + " (" + $.t('asset') + ")";
                    case 2: return NRS.getTransactionLink(response.holding, response.holdingInfo.code)  + " (" + $.t('currency') + ")";
                }
            })(),
            participants: NRS.escapeRespStr(response.registrantCount) + " / " + NRS.escapeRespStr(response.participantCount),
            blocks: response.blocksRemaining,
            shuffling: response.shuffling,
            shufflingFullHash: response.shufflingFullHash
        };
    };

    NRS.pages.shuffling = function () {};

    NRS.setup.shuffling = function() {
        if (!NRS.isShufflingSupported()) {
            return;
        }
        var sidebarId = 'sidebar_shuffling';
        NRS.addTreeviewSidebarMenuItem({
            "id": sidebarId,
            "titleHTML": '<i class="fa fa-random"></i> <span data-i18n="shuffling">Shuffling</span>',
            "page": 'active_shufflings',
            "desiredPosition": 80,
            "depends": { tags: [ NRS.constants.API_TAGS.SHUFFLING ] }
        });
        NRS.appendMenuItemToTSMenuItem(sidebarId, {
            "titleHTML": '<span data-i18n="active_shufflings">Active Shufflings</span>',
            "type": 'PAGE',
            "page": 'active_shufflings'
        });
        NRS.appendMenuItemToTSMenuItem(sidebarId, {
            "titleHTML": '<span data-i18n="my_shufflings">My Shufflings</span>',
            "type": 'PAGE',
            "page": 'my_shufflings'
        });
        NRS.appendMenuItemToTSMenuItem(sidebarId, {
            "titleHTML": '<span data-i18n="create_shuffling">Create Shuffling</span>',
            "type": 'MODAL',
            "modalId": 'm_shuffling_create_modal'
        });

        $('#m_shuffling_create_holding_type').change();
    };

    /**
     * Create shuffling modal holding type onchange listener.
     * Hides holding field unless type is asset or currency.
     */
    $('#m_shuffling_create_holding_type').change(function () {
        var holdingType = $("#m_shuffling_create_holding_type");
        if(holdingType.val() == "0") {
            $("#shuffling_asset_id_group").css("display", "none");
            $("#shuffling_ms_currency_group").css("display", "none");
            $('#m_shuffling_create_unit').html($.t('amount'));
            $('#m_shuffling_create_amount').attr('name', 'shufflingAmountNXT');
        } if(holdingType.val() == "1") {
			$("#shuffling_asset_id_group").css("display", "inline");
			$("#shuffling_ms_currency_group").css("display", "none");
            $('#m_shuffling_create_unit').html($.t('quantity'));
            $('#m_shuffling_create_amount').attr('name', 'amountQNTf');
		} else if(holdingType.val() == "2") {
			$("#shuffling_asset_id_group").css("display", "none");
			$("#shuffling_ms_currency_group").css("display", "inline");
            $('#m_shuffling_create_unit').html($.t('units'));
            $('#m_shuffling_create_amount').attr('name', 'amountQNTf');
		}
    });

    NRS.forms.shufflingCreate = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        switch (data.holdingType) {
            case '0':
                delete data.holding;
                break;
            case '1':
                break;
            case '2':
                break;
        }
        if (data.finishHeight) {
            data.registrationPeriod = parseInt(data.finishHeight) - NRS.lastBlockHeight;
            delete data.finishHeight;
        }
        return {
            "data": data
        }
    };

    NRS.incoming.active_shufflings = function() {
        NRS.loadPage("active_shufflings");
    };

    NRS.incoming.my_shufflings = function() {
        NRS.loadPage("my_shufflings");
    };

    function getShufflers(callback) {
        NRS.sendRequest("getShufflers", {"account": NRS.account, "adminPassword": NRS.getAdminPassword(), "includeParticipantState": true},
            function (shufflers) {
                if (isErrorResponse(shufflers)) {
                    $.growl($.t("cannot_check_shufflers_status") + " " + shufflers.errorDescription.escapeHTML());
                    callback(null, undefined);
                } else {
                    callback(null, shufflers);
                }
            }
        )
    }

    NRS.pages.finished_shufflings = function() {
        NRS.finished_shufflings("finished_shufflings_full", true);
    };

    NRS.pages.active_shufflings = function () {
        NRS.finished_shufflings("finished_shufflings",false);
        async.waterfall([
            function(callback) {
                getShufflers(callback);
            },
            function(shufflers, callback) {
                NRS.hasMorePages = false;
                var view = NRS.simpleview.get('active_shufflings', {
                    errorMessage: null,
                    isLoading: true,
                    isEmpty: false,
                    shufflings: []
                });
                var params = {
                    "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                    "lastIndex": NRS.pageNumber * NRS.itemsPerPage,
                    "includeHoldingInfo": "true"
                };
                NRS.sendRequest("getAllShufflings", params,
                    function (response) {
                        if (isErrorResponse(response)) {
                            view.render({
                                errorMessage: NRS.getErrorMessage(response),
                                isLoading: false,
                                isEmpty: false
                            });
                            return;
                        }
                        if (response.shufflings.length > NRS.itemsPerPage) {
                            NRS.hasMorePages = true;
                            response.shufflings.pop();
                        }
                        view.shufflings.length = 0;
                        var amountDecimals = NRS.getNumberOfDecimals(response.shufflings, "amount", function(shuffling) {
                            switch (shuffling.holdingType) {
                                case 0: return NRS.formatAmount(shuffling.amount);
                                case 1:
                                case 2: return NRS.formatQuantity(shuffling.amount, shuffling.holdingInfo.decimals);
                                default: return "";
                            }
                        });
                        response.shufflings.forEach(
                            function (shufflingJson) {
                                view.shufflings.push(NRS.jsondata.shuffling(shufflingJson, shufflers, amountDecimals))
                            }
                        );
                        view.render({
                            isLoading: false,
                            isEmpty: view.shufflings.length == 0
                        });
                        NRS.pageLoaded();
                        callback(null);
                    }
                );
            }
        ], function (err, result) {});
    };

    NRS.pages.my_shufflings = function () {
        async.waterfall([
            function(callback) {
                getShufflers(callback);
            },
            function(shufflers, callback) {
                NRS.hasMorePages = false;
                var view = NRS.simpleview.get('my_shufflings_page', {
                    errorMessage: null,
                    isLoading: true,
                    isEmpty: false,
                    shufflings: []
                });
                var params = {
                    "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                    "lastIndex": NRS.pageNumber * NRS.itemsPerPage,
                    "account": NRS.account,
                    "includeFinished": "true",
                    "includeHoldingInfo": "true"
                };
                NRS.sendRequest("getAccountShufflings", params,
                    function(response) {
                        if (isErrorResponse(response)) {
                            view.render({
                                errorMessage: NRS.getErrorMessage(response),
                                isLoading: false,
                                isEmpty: false
                            });
                            return;
                        }
                        if (response.shufflings.length > NRS.itemsPerPage) {
                            NRS.hasMorePages = true;
                            response.shufflings.pop();
                        }
                        view.shufflings.length = 0;
                        var amountDecimals = NRS.getNumberOfDecimals(response.shufflings, "amount", function(shuffling) {
                            switch (shuffling.holdingType) {
                                case 0: return NRS.formatAmount(shuffling.amount);
                                case 1:
                                case 2: return NRS.formatQuantity(shuffling.amount, shuffling.holdingInfo.decimals);
                                default: return "";
                            }
                        });
                        response.shufflings.forEach(
                            function (shufflingJson) {
                                view.shufflings.push( NRS.jsondata.shuffling(shufflingJson, shufflers, amountDecimals) );
                            }
                        );
                        view.render({
                            isLoading: false,
                            isEmpty: view.shufflings.length == 0
                        });
                        NRS.pageLoaded();
                        callback(null);
                    }
                );
            }
        ], function (err, result) {});
    };

    $("#m_shuffling_create_modal").on("show.bs.modal", function() {
   		var context = {
   			labelText: "Currency",
   			labelI18n: "currency",
   			inputCodeName: "shuffling_ms_code",
   			inputIdName: "holding",
   			inputDecimalsName: "shuffling_ms_decimals",
   			helpI18n: "add_currency_modal_help"
   		};
   		NRS.initModalUIElement($(this), '.shuffling_holding_currency', 'add_currency_modal_ui_element', context);

   		context = {
   			labelText: "Asset",
   			labelI18n: "asset",
   			inputIdName: "holding",
   			inputDecimalsName: "shuffling_asset_decimals",
   			helpI18n: "add_asset_modal_help"
   		};
   		NRS.initModalUIElement($(this), '.shuffling_holding_asset', 'add_asset_modal_ui_element', context);

   		context = {
   			labelText: "Registration Finish",
   			labelI18n: "registration_finish",
   			helpI18n: "shuffling_registration_height_help",
   			inputName: "finishHeight",
   			initBlockHeight: NRS.lastBlockHeight + 1440,
   			changeHeightBlocks: 500
   		};
   		NRS.initModalUIElement($(this), '.shuffling_finish_height', 'block_height_modal_ui_element', context);
        // Activating context help popovers - from some reason this code is activated
        // after the same event in nrs.modals.js which doesn't happen for create pool thus it's necessary
        // to explicitly enable the popover here. strange ...
		$(function () {
            $("[data-toggle='popover']").popover({
            	"html": true
            });
        });

   	});

    var shufflerStartModal = $("#m_shuffler_start_modal");
    shufflerStartModal.on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var shufflingId = $invoker.data("shuffling");
        if (shufflingId) {
            $("#shuffler_start_shuffling_id").html(shufflingId);
        }
        var shufflingFullHash = $invoker.data("shufflingfullhash");
        if (shufflingFullHash) {
            $("#shuffler_start_shuffling_full_hash").val(shufflingFullHash);
        }
    });

    $('#m_shuffler_start_recipient_secretphrase').on("change", function () {
        var secretPhraseValue = $('#m_shuffler_start_recipient_secretphrase').val();
        var recipientAccount = $('#m_shuffler_start_recipient_account');
        if (secretPhraseValue == "") {
            recipientAccount.val("");
            return;
        }
        recipientAccount.val(NRS.getAccountId(secretPhraseValue, true));
    });

    NRS.forms.startShuffler = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        if (data.recipientSecretPhrase) {
            data.recipientPublicKey = NRS.getPublicKey(converters.stringToHexString(data.recipientSecretPhrase));
            delete data.recipientSecretPhrase;
        }
        return {
            "data": data
        };
    };

    NRS.forms.shufflingCreateComplete = function(response) {
        $.growl($.t("shuffling_created"));
        // After shuffling created we show the start shuffler modal
        $("#shuffler_start_shuffling_id").html(response.transaction);
        $("#shuffler_start_shuffling_full_hash").val(response.fullHash);
        $('#m_shuffler_start_modal').modal("show");
    };

    NRS.forms.startShufflerComplete = function() {
        $.growl($.t("shuffler_started"));
        NRS.loadPage(NRS.currentPage);
    };

    NRS.finished_shufflings = function (table,full) {
        var finishedShufflingsTable = $("#" + table + "_table");
        finishedShufflingsTable.find("tbody").empty();
        finishedShufflingsTable.parent().addClass("data-loading").removeClass("data-empty");
        async.waterfall([
            function(callback) {
                getShufflers(callback);
            },
            function(shufflers, callback) {
                NRS.hasMorePages = false;
                var view = NRS.simpleview.get(table, {
                    errorMessage: null,
                    isLoading: true,
                    isEmpty: false,
                    data: []
                });
                var params = {
                    "account": NRS.account,
                    "finishedOnly": "true",
                    "includeHoldingInfo": "true"
                };
                if (full) {
                    params["firstIndex"] = NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage;
                    params["lastIndex"] = NRS.pageNumber * NRS.itemsPerPage;
                } else {
                    params["firstIndex"] = 0;
                    params["lastIndex"] = 9;
                }
                NRS.sendRequest("getAllShufflings", params,
                    function (response) {
                        if (isErrorResponse(response)) {
                            view.render({
                                errorMessage: NRS.getErrorMessage(response),
                                isLoading: false,
                                isEmpty: false
                            });
                            return;
                        }
                        if (response.shufflings.length > NRS.itemsPerPage) {
                            NRS.hasMorePages = true;
                            response.shufflings.pop();
                        }
                        view.data.length = 0;
                        var amountDecimals = NRS.getNumberOfDecimals(response.shufflings, "amount", function(shuffling) {
                            switch (shuffling.holdingType) {
                                case 0: return NRS.formatAmount(shuffling.amount);
                                case 1:
                                case 2: return NRS.formatQuantity(shuffling.amount, shuffling.holdingInfo.decimals);
                                default: return "";
                            }
                        });
                        response.shufflings.forEach(
                            function (shufflingJson) {
                                view.data.push(NRS.jsondata.shuffling(shufflingJson, shufflers, amountDecimals))
                            }
                        );
                        view.render({
                            isLoading: false,
                            isEmpty: view.data.length == 0
                        });
                        NRS.pageLoaded();
                        callback(null);
                    }
                );
            }
        ], function (err, result) {});
    };

    return NRS;

}(NRS || {}, jQuery));