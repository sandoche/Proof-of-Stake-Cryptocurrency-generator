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
 * @depends {nrs.modals.js}
 */
var NRS = (function(NRS, $) {

    NRS.forms.dividendPayment = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        data.asset = NRS.getCurrentAsset().asset;
        resetCallout();
        var decimals = 8;
        switch (data.holdingType) {
            case '0':
                delete data.holding;
                break;
            case '1':
                decimals = data.dividend_payment_asset_decimals;
                break;
            case '2':
                decimals = data.dividend_payment_ms_decimals;
                break;
            default:
                throw "Unknown holding type";
        }

        if (isNaN(decimals)) {
            return {
                "error": $.t("select_holding")
            };
        }

        if (!data.amountPerShare) {
            return {
                "error": $.t("error_amount_per_share_required")
            };
        }

        var conversion = convertAmountPerShare($modal, decimals);
        if (conversion.error) {
            return conversion;
        }
        data.amountNQTPerQNT = conversion.amountNQTPerQNT;

        if (!/^\d+$/.test(data.height)) {
            return {
                "error": $.t("error_invalid_dividend_height")
            };
        }
        var isDividendHeightBeforeAssetHeight;
        NRS.sendRequest("getTransaction", { transaction: data.asset }, function(response) {
            if (response.height > data.height) {
                isDividendHeightBeforeAssetHeight = true;
            }
        }, { isAsync: false });
        if (isDividendHeightBeforeAssetHeight) {
            return {
                "error": $.t("dividend_height_asset_height")
            };
        }
        delete data.amountPerShare;
        delete data.holdingDecimals;
        delete data.dividend_payment_ms_code;
        delete data.dividend_payment_ms_decimals;
        delete data.dividend_payment_asset_decimals;

        return {
            "data": data
        };
    };

    /**
     * Dividend payment modal holding type onchange listener.
     * Hides holding field unless type is asset or currency.
     */
    $('#dividend_payment_holding_type').change(function () {
        var holdingType = $("#dividend_payment_holding_type");
        if(holdingType.val() == "0") {
            $("#dividend_payment_asset_id_group").css("display", "none");
            $("#dividend_payment_ms_currency_group").css("display", "none");
        } if(holdingType.val() == "1") {
            $("#dividend_payment_asset_id_group").css("display", "inline");
            $("#dividend_payment_ms_currency_group").css("display", "none");
        } else if(holdingType.val() == "2") {
            $("#dividend_payment_asset_id_group").css("display", "none");
            $("#dividend_payment_ms_currency_group").css("display", "inline");
        }
        resetCallout();
    });

    var $dividendPaymentModal = $("#dividend_payment_modal");
    $dividendPaymentModal.on("show.bs.modal", function() {
        $('#dividend_payment_holding_type').change();
        var context = {
            labelText: "Currency",
            labelI18n: "currency",
            inputCodeName: "dividend_payment_ms_code",
            inputIdName: "holding",
            inputDecimalsName: "dividend_payment_ms_decimals",
            helpI18n: "add_currency_modal_help"
        };
        NRS.initModalUIElement($(this), '.dividend_payment_holding_currency', 'add_currency_modal_ui_element', context);

        context = {
            labelText: "Asset",
            labelI18n: "asset",
            inputIdName: "holding",
            inputDecimalsName: "dividend_payment_asset_decimals",
            helpI18n: "add_asset_modal_help"
        };
        NRS.initModalUIElement($(this), '.dividend_payment_holding_asset', 'add_asset_modal_ui_element', context);

        $(function () {
            $("[data-toggle='popover']").popover({
                "html": true
            });
        });
    });
    
    $dividendPaymentModal.on("hidden.bs.modal", function() {
        resetCallout();
    });

    $dividendPaymentModal.find("label[for='dividend_payment_amount_per_share'] i.show_popover").data("content", amountPerShareHelpPopoverContent);

    // prevent entering invalid characters on "amount per share" input, also checks for number of decimals allowed
    $("#dividend_payment_amount_per_share").keydown(function (e) {
        // noinspection JSDeprecatedSymbols
        var charCode = !e.charCode ? e.which : e.charCode;
        if (NRS.isControlKey(charCode) || e.ctrlKey || e.metaKey) {
            return;
        }
        var holdingInfo = getHoldingInfo();
        var holdingDecimals = holdingInfo.holdingName === "" ? 8 : holdingInfo.holdingDecimals;
        var maxFractionLength = Math.max(0, holdingDecimals - NRS.getCurrentAsset().decimals);
        NRS.validateDecimals(maxFractionLength, charCode, $(this).val(), e);
    });

    $("#dividend_payment_amount_per_share, #dividend_payment_height").on("blur", function() {
        var $modal = $(this).closest(".modal");
        var height = $modal.find("#dividend_payment_height").val();
        resetCallout();
        if ($modal.find("#dividend_payment_amount_per_share").val() === '') {
            return;
        }
        var holdingInfo = getHoldingInfo();
        if (holdingInfo.holdingName === "") {
            return;
        }
        var holdingDecimals = holdingInfo.holdingDecimals;
        var conversion = convertAmountPerShare($modal, holdingDecimals);
        if (conversion.error) {
            showCallout(conversion.error, "callout-danger");
            return;
        }
        if (conversion.amountNQTPerQNT !== null && /^\d+$/.test(height)) {
            NRS.getAssetAccounts(NRS.getCurrentAsset().asset, height,
                function (response) {
                    var accountAssets = response.accountAssets;
                    var qualifiedDividendRecipients = accountAssets.filter(
                        function(accountAsset) {
                            return accountAsset.accountRS !== NRS.getCurrentAsset().accountRS
                                && accountAsset.accountRS !== NRS.constants.GENESIS_RS;
                        });
                    var totalQuantityQNT = new BigInteger("0");
                    qualifiedDividendRecipients.forEach(
                        function (accountAsset) {
                            totalQuantityQNT = totalQuantityQNT.add(new BigInteger(accountAsset.quantityQNT));
                        }
                    );
                    var totalNQT = totalQuantityQNT.multiply(new BigInteger(conversion.amountNQTPerQNT));
                    showCallout($.t("dividend_payment_info_preview_success",
                        {
                            "amount": NRS.formatQuantity(totalNQT, holdingDecimals) + " " + holdingInfo.holdingName + " " + holdingInfo.holdingUnitName,
                            "totalQuantity": NRS.formatQuantity(totalQuantityQNT, NRS.getCurrentAsset().decimals),
                            "recipientCount": qualifiedDividendRecipients.length
                        }), "callout-info");
                },
                function (response) {
                    var displayString;
                    if (response.errorCode == 4 || response.errorCode == 8) {
                        displayString = $.t("error_invalid_dividend_height");
                    } else {
                        displayString = $.t("dividend_payment_info_preview_error", {"errorCode": response.errorCode});
                    }
                    showCallout(displayString, "callout-warning");
                }
            );
        }
    });

    function convertAmountPerShare($modal, holdingDecimals) {
        try {
            var amountPerShare = $modal.find("#dividend_payment_amount_per_share").val();
            if (amountPerShare === '') {
                return { error: $.t("error_amount_per_share_required") };
            }
            var amountNQT = NRS.convertToQNT(amountPerShare, holdingDecimals);
            if (amountNQT === "0") {
                return { error: $.t("error_zero_field", {field: $.t("amount_per_share")}) };
            }
            return { amountNQTPerQNT: NRS.calculatePricePerWholeQNT(amountNQT, NRS.getCurrentAsset().decimals) };
        } catch (e) {
            NRS.logException(e);
            return { error: $.t("error_invalid_input") + " " + amountPerShareHelpPopoverContent() };
        }
    }

    function amountPerShareHelpPopoverContent() {
        var holdingInfo = getHoldingInfo();
        if (holdingInfo.holdingName === "") {
            return $.t("select_holding");
        }
        var assetDecimals = NRS.getCurrentAsset().decimals;
        var holdingDecimals = holdingInfo.holdingDecimals;
        var md = holdingDecimals - assetDecimals;
        var msgParams = {
            assetDecimals: assetDecimals,
            holdingDecimals: holdingDecimals
        };
        if (md > 0 ) {
            msgParams.maxDecimals = md;
            return $.t("amount_per_share_decs", msgParams);
        } else if (md < 0) {
            msgParams.minValue = NRS.formatQuantity(Math.pow(10, -md));
            msgParams.zeros = -md;
            return $.t("amount_per_share_zeros", msgParams);
        } else {
            return $.t("amount_per_share_integer", msgParams);
        }
    }

    function showCallout(msg, cssClass) {
        $dividendPaymentModal.find(".callout.error_message")
            .first() // callout
            .text(msg)
            .removeClass("callout-info callout-danger callout-warning")
            .addClass(cssClass)
            .show();
    }

    function resetCallout() {
        $dividendPaymentModal.find(".callout.error_message")
            .first()
            .removeClass("callout-info callout-warning")
            .addClass('callout-danger')
            .hide();
    }

    function getHoldingInfo() {
        var holdingType = $dividendPaymentModal.find("#dividend_payment_holding_type").val();
        switch (holdingType) {
            case "0":
                return {
                    holdingType    : "0",
                    holdingDecimals: 8,
                    holdingName    : "NXT",
                    holdingUnitName: " "
                };
            case "1":
                var assetDecimals = $dividendPaymentModal.find('.aam_ue_asset_decimals_input').val();
                return {
                    holdingType    : "1",
                    holdingDecimals: parseInt(assetDecimals),
                    holdingName    : assetDecimals === "" ? "" : $.t('asset_name_and_id', {
                        'name': $dividendPaymentModal.find('.aam_ue_asset_name_input').val(),
                        'id': $dividendPaymentModal.find('.aam_ue_asset_id_input').val()
                    }),
                    holdingUnitName: $.t("shares")
                };
            case "2":
                var currencyDecimals = $dividendPaymentModal.find('.acm_ue_currency_decimals_input').val();
                return {
                    holdingType    : "2",
                    holdingDecimals: parseInt(currencyDecimals),
                    holdingName    : $dividendPaymentModal.find('.acm_ue_currency_code_input').val(),
                    holdingUnitName: $.t("units")
                };
            default:
                throw "Unknown holding type";
        }
    }

    return NRS;
}(NRS || {}, jQuery));
