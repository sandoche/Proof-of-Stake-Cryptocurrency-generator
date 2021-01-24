/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
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

    var $genericSaveModal = $("#m_save_node_process_config_modal");
    var $saveNodeProcessPayload = $("#save_node_process_payload");
    const $forgingSaveModal = $('#m_save_forging_encrypted_modal');
    var accountPassphrases = {};
    const forgingAccounts = [];

    var addon_defaults = {
        save_modal            : "#m_save_node_process_config_modal",
        accountExtractor      : genericAccountExtractor,
        payloadCurry          : genericPayloadCurry
    };

    var addons = [
        {
            friendlyName      : "forging",
            requestType       : "Forging",
            dataParameter     : "forging",
            save_modal        : "#m_save_forging_encrypted_modal"
        },
        {
            friendlyName      : "funding_monitors",
            requestType       : "FundingMonitors",
            getDataRequestType: "getFundingMonitor",
            dataParameter     : "monitors",
            accountParameter  : "account"
        }
    ];

    // apply defaults
    addons = addons.map(function (addon) {
        return $.extend({}, addon_defaults, addon);
    });

    NRS.setup.node_processes_config = function () {
        // check which add-ons are enabled (using constants) to filter view
        addons.forEach(function (addon) {
            addon.showUI = NRS.constants.REQUEST_TYPES['save' + addon.requestType + 'Encrypted'] !== undefined;
        });
        setupCompleteCallbacks();

        // override hidden passphrase input when logged with passphrase remembered
        $genericSaveModal.find(".secret_phrase").show();
        $forgingSaveModal.find(".secret_phrase").show();
    };

    NRS.pages.node_processes_config = function() {
        NRS.simpleview.get('node_processes_config_page', {
            addons: addons,
            emptyUI: addons.every(function (addon) { return !addon.showUI; })
        });
    };

    /************************************** Save modal  **************************************/

    /**
     * Upon showing the modal we try to populate the data textarea with the current configuration.
     */
    $genericSaveModal.on("show.bs.modal", function(event) {
        var $invoker = $(event.relatedTarget);
        var addon = addons[$invoker.data("addon")];
        $genericSaveModal.data("addon", addon);
        $genericSaveModal.find(".modal-title").text($.t("save") + ": " + $.t(addon.friendlyName));
        $genericSaveModal.find(".callout-info").hide();
        accountPassphrases = {};
        $("#save_node_process_config_request_type").val("save" + addon.requestType + "Encrypted");
        if (addon.getDataRequestType !== undefined) {
            NRS.sendRequest(addon.getDataRequestType, {"adminPassword": NRS.getAdminPassword()},
                function (response) {
                    if (NRS.isErrorResponse(response)) {
                        const msg = $.t("cannot_load_current") + " " + addon.getDataRequestType + ": " + NRS.unescapeRespStr(NRS.getErrorMessage(response));
                        $genericSaveModal.find(".error_message").text(msg).show();
                        $genericSaveModal.find(".passphrasesPanel,.encryptionPanel").addClass("hidden");
                        $genericSaveModal.find(".modal-footer button.btn-primary").prop("disabled", true);
                        return;
                    }
                    delete response.requestProcessingTime;
                    $saveNodeProcessPayload.val(JSON.stringify(response, null, 2)).change();
                });
        }
    }).on("hidden.bs.modal", function() {
        accountPassphrases = {}; // we don't want passphrases in memory for longer than strictly needed
    });

    /**
     * On each change of the data textarea we update the set and state of account passphrases remaining.
     */
    $saveNodeProcessPayload.on('change', function () {
        var addon = $genericSaveModal.data("addon");

        // remove pending accounts (we want to keep already entered passphrases)
        Object.getOwnPropertyNames(accountPassphrases).forEach(function (account) {
            if (accountPassphrases[account] === null) {
                delete accountPassphrases[account];
            }
        });

        try {
            $genericSaveModal.find(".error_message").hide();
            var payloadAccounts = addon.accountExtractor(addon);
        } catch (e) {
            var msg = $.t("cannot_parse_json") + ": " + (e.message || e);
            $genericSaveModal.find(".error_message").text(msg).show();
            return;
        }
        payloadAccounts.forEach(function (account) {
            if (!accountPassphrases.hasOwnProperty(account)) {
                accountPassphrases[account] = null;
            }
        });
        updatePassphrasesStatus();
    });

    function genericAccountExtractor(addon) {
        var payload = JSON.parse($saveNodeProcessPayload.val());
        var dataArray = payload[addon.dataParameter];
        if (dataArray === undefined) {
            throw $.t('error_invalid_field', {field: addon.dataParameter});
        }
        return dataArray.map(function (o) {
            return o[addon.accountParameter];
        });
    }

    /**
     * This method controls the remaining passphrases required to the user and paints the list if necessary.
     * Otherwise it shows the encryption password inputs.
     *
     * @returns {boolean} do we have all required passphrases?
     */
    function updatePassphrasesStatus() {
        // retrieve list of remaining passphrases required
        var pendingAccounts = Object.getOwnPropertyNames(accountPassphrases).filter(function (account) {
            return accountPassphrases[account] === null;
        }).map(function (account) {
            return NRS.convertNumericToRSAccountFormat(account);
        });

        if (pendingAccounts.length === 0) {
            // if empty, then hide passphrases panel, show encryption passphrase panel and enable "Save" button
            $genericSaveModal.find(".passphrasesPanel").addClass("hidden");
            $genericSaveModal.find(".encryptionPanel").removeClass("hidden");
            $genericSaveModal.find(".modal-footer button.btn-primary").prop("disabled", false);
            return true;
        } else {
            // if not empty, then show passphrases panel, update content list, hide encryption passphrase panel and disable "Save" button
            $genericSaveModal.find(".passphrasesPanel").removeClass("hidden").find("ul").empty().append(
                pendingAccounts.map(function (accountRS) {
                    return $('<li>').text(accountRS);
                })
            );
            $genericSaveModal.find(".encryptionPanel").addClass("hidden");
            $genericSaveModal.find(".modal-footer button.btn-primary").prop("disabled", true);
            return false;
        }
    }

    /**
     * Add passphrase button controller.
     */
    $genericSaveModal.find(".passphrasesPanel button.btn-primary").on("click", function () {
        var $passphraseInput = $("#m_save_node_process_config_password");
        var passphrase = $passphraseInput.val();
        if (passphrase === "") {
            $.growl($.t("empty_passphrase"), {type:'warning'});
        } else {
            $passphraseInput.val('');
            var account = NRS.getAccountId(passphrase, false);
            if (accountPassphrases[account] !== null) {
                $.growl($.t("passphrase_not_one_of_remaining"), {type:'warning'});
            }
            accountPassphrases[account] = passphrase;
            updatePassphrasesStatus();
        }
    });

    /////////// Forging save modal

    $forgingSaveModal.on('show.bs.modal', function () {
        forgingAccounts.length = 0;
        renderForgingAccountsTable();
    }).on("hidden.bs.modal", function() {
        forgingAccounts.length = 0; // we don't want passphrases in memory for longer than strictly needed
    });

    $forgingSaveModal.find('.addForgerButton').on('click', function () {
        const $passphraseInput = $('#m_save_forging_encrypted_password');
        const passphrase = $passphraseInput.val();
        if (passphrase === "") {
            $.growl($.t("empty_passphrase"), {type:'warning'});
        } else {
            $passphraseInput.val('');
            $("#m_save_forging_encrypted_is_shared_secret").prop("checked", false).change();
            const accountId = NRS.getAccountId(passphrase, false);
            const forgingAccount = {
                account: accountId,
                accountRS: NRS.convertNumericToRSAccountFormat(accountId),
                passphrase: passphrase,
                effectiveBalance: null
            };
            forgingAccounts.push(forgingAccount);
            renderForgingAccountsTable();
            NRS.sendRequest('getBalance', {account: accountId, includeEffectiveBalance: true}, function (response) {
                if (response.effectiveBalanceNXT !== undefined) {
                    forgingAccount.effectiveBalance = response.effectiveBalanceNXT;
                    renderForgingAccountsTable();
                }
            });
        }
    });

    $forgingSaveModal.on('click', 'span.forgetForgingAccount', function () {
        forgingAccounts.splice($(this).data('index'), 1);
        renderForgingAccountsTable();
    });

    function renderForgingAccountsTable() {
        const $forgingAccountsList = $forgingSaveModal.find('dl.forgingAccounts');
        const html = forgingAccounts.map((account,index) => {
            return `<dt>${account.accountRS}<span class='forgetForgingAccount' data-index='${index}'>x</span></dt>
                    <dd>Effective balance: ${account.effectiveBalance === null ? '...' : NRS.format(account.effectiveBalance)}</dd>`;
        });
        $forgingAccountsList.html(html);
    }

    /************************************** Save operation  **************************************/

    function genericSaveEncrypted ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var addon = $modal.data("addon");

        if (!data.encryptionPassword || data.encryptionPassword.length < 10) {
            return {
                "error": $.t("configuration_password_short")
            };
        }

        if (data.encryptionPassword !== data.encryptionPassword2) {
            return {
                "error": $.t("configuration_password_match")
            };
        }
        delete data.encryptionPassword2;

        if (!updatePassphrasesStatus()) {
            return;
        }

        // add the corresponding secretphrase to each object in the payload array
        try {
            data.payload = JSON.parse(data.payload);
        } catch(e) {
            return {
                "error": $.t("cannot_parse_json") + ": " + (e.message || e)
            };
        }

        var secretPhraseAsEncryptionPassword = addon.payloadCurry(addon, data);
        delete data.secretPhrase;

        // warn if using one of the secret phrases as encryption passphrase
        if (secretPhraseAsEncryptionPassword) {
            return {
                "error": $.t("encryption_passphrase_is_secretphrase")
            };
        }

        // client-side encryption
        var encryptedPayload = NRS.aesEncrypt(JSON.stringify(data.payload), data.encryptionPassword);
        delete data.payload;
        delete data.encryptionPassword;
        data[addon.dataParameter] = converters.byteArrayToHexString(encryptedPayload);
        data.dataAlreadyEncrypted = true;

        data.adminPassword = NRS.getAdminPassword();

        return {
            "data": data
        };
    }

    function genericPayloadCurry(addon, data) {
        var secretPhraseAsEncryptionPassword = false;
        if (Array.isArray(data.payload[addon.dataParameter])) {
            var dataArray = data.payload[addon.dataParameter];
            dataArray.forEach(function (element) {
                var account = element[addon.accountParameter];
                element.secretPhrase = accountPassphrases[account];
                if (element.secretPhrase === data.encryptionPassword) {
                    secretPhraseAsEncryptionPassword = true;
                }
            });
        }
        return secretPhraseAsEncryptionPassword;
    }

    NRS.forms.saveFundingMonitorsEncrypted = genericSaveEncrypted;

    NRS.forms.saveForgingEncrypted = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        delete data.secretPhrase;

        if (!data.encryptionPassword || data.encryptionPassword.length < 10) {
            return {
                "error": $.t("configuration_password_short")
            };
        }

        if (data.encryptionPassword !== data.encryptionPassword2) {
            return {
                "error": $.t("configuration_password_match")
            };
        }
        delete data.encryptionPassword2;

        data.payload = forgingAccounts.map(account => account.passphrase).join('\n');

        // warn if using one of the secret phrases as encryption passphrase
        if (data.payload.includes(data.encryptionPassword)) {
            return {
                "error": $.t("encryption_passphrase_is_secretphrase")
            };
        }

        // client-side encryption
        var encryptedPayload = NRS.aesEncrypt(data.payload, data.encryptionPassword);
        delete data.payload;
        delete data.encryptionPassword;
        data.passphrases = converters.byteArrayToHexString(encryptedPayload);
        data.dataAlreadyEncrypted = true;

        data.adminPassword = NRS.getAdminPassword();

        return {
            "data": data
        };
    };

    /************************************** Start modal  **************************************/

    $("#m_start_node_process_config_modal").on("show.bs.modal", function(event) {
        var $invoker = $(event.relatedTarget);
        var addon = addons[$invoker.data("addon")];
        var $modal = $(this);
        $modal.find(".modal-title").text($.t("start") + ": " + $.t(addon.friendlyName));
        $("#start_node_process_config_request_type").val("start" + addon.requestType + "Encrypted");
    });

    /************************************** Start operation  **************************************/

    function genericStartEncrypted ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));

        data.adminPassword = NRS.getAdminPassword();

        return {
            "data": data
        };
    }

    NRS.forms.startForgingEncrypted = genericStartEncrypted;
    NRS.forms.startFundingMonitorsEncrypted = genericStartEncrypted;

    /************************************** Complete callbacks **************************************/

    function genericSaveEncryptedComplete(processName) {
        return function() {
            $.growl($.t("process_file_saved", {process: processName}));
        };
    }

    function setupCompleteCallbacks() {
        NRS.forms.saveForgingEncryptedComplete = genericSaveEncryptedComplete($.t("forging"));
        NRS.forms.saveFundingMonitorsEncryptedComplete = genericSaveEncryptedComplete($.t("funding_monitors"));
    }


    NRS.forms.startForgingEncryptedComplete = function (response) {
        $.growl($.t("forgers_started", {count: response.forgersStarted, balance: response.totalEffectiveBalance}));
    };

    NRS.forms.startFundingMonitorsEncryptedComplete = function (response) {
        if (Array.isArray(response.monitors)) {
            $.growl($.t("started_monitors", {count: response.monitors.length}));
        }
    };

    return NRS;
}(NRS || {}, jQuery));