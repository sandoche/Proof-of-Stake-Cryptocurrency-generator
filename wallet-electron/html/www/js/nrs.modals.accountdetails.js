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
var NRS = (function(NRS, $, undefined) {
    var _password = null;
    var accountDetailsModal = $("#account_details_modal");

    accountDetailsModal.on("show.bs.modal", function(e) {
        if (_password) {
            $("#account_details_modal_account_display").show();
            $("#account_details_modal_passphrase_display").show();
            if (NRS.isWindowPrintSupported()) {
                $("#account_details_modal_paper_wallet_link").prop("disabled", false);
            }
        } else {
            NRS.generateQRCode("#account_details_modal_account_qr_code", NRS.accountRS);
            $("#account_details_modal_account_display").hide();
            $("#account_details_modal_passphrase_display").hide();
            $("#account_details_modal_passphrase_qr_code").html($.t("passphrase_not_available"));
            $("#account_details_modal_paper_wallet_na").html($.t("passphrase_not_available"));
        }
		$("#account_details_modal_balance").show();

        var accountBalanceWarning = $("#account_balance_warning");
        if (NRS.accountInfo.errorCode && NRS.accountInfo.errorCode != 5) {
			$("#account_balance_table").hide();
			accountBalanceWarning.html(NRS.escapeRespStr(NRS.accountInfo.errorDescription)).show();
		} else {
			accountBalanceWarning.hide();
            var accountBalancePublicKey = $("#account_balance_public_key");
            if (NRS.accountInfo.errorCode && NRS.accountInfo.errorCode == 5) {
				$("#account_balance_balance, #account_balance_unconfirmed_balance, #account_balance_effective_balance, #account_balance_guaranteed_balance, #account_balance_forged_balance").html("0 " + NRS.constants.COIN_SYMBOL);
				accountBalancePublicKey.html(NRS.escapeRespStr(NRS.publicKey));
				$("#account_balance_account_rs").html(NRS.getAccountLink(NRS, "account", undefined, undefined, true));
				$("#account_balance_account").html(NRS.escapeRespStr(NRS.account));
			} else {
				$("#account_balance_balance").html(NRS.formatAmount(new BigInteger(NRS.accountInfo.balanceNQT)) + " " + NRS.constants.COIN_SYMBOL);
				$("#account_balance_unconfirmed_balance").html(NRS.formatAmount(new BigInteger(NRS.accountInfo.unconfirmedBalanceNQT)) + " " + NRS.constants.COIN_SYMBOL);
				$("#account_balance_effective_balance").html(NRS.formatAmount(NRS.accountInfo.effectiveBalanceNXT) + " " + NRS.constants.COIN_SYMBOL);
				$("#account_balance_guaranteed_balance").html(NRS.formatAmount(new BigInteger(NRS.accountInfo.guaranteedBalanceNQT)) + " " + NRS.constants.COIN_SYMBOL);
				$("#account_balance_forged_balance").html(NRS.formatAmount(new BigInteger(NRS.accountInfo.forgedBalanceNQT)) + " " + NRS.constants.COIN_SYMBOL);

				accountBalancePublicKey.html(NRS.escapeRespStr(NRS.accountInfo.publicKey));
				$("#account_balance_account_rs").html(NRS.getAccountLink(NRS.accountInfo, "account", undefined, undefined, true));
				$("#account_balance_account").html(NRS.escapeRespStr(NRS.account));

				if (!NRS.accountInfo.publicKey) {
					accountBalancePublicKey.html("/");
                    var warning = NRS.publicKey != 'undefined' ? $.t("public_key_not_announced_warning", { "public_key": NRS.publicKey }) : $.t("no_public_key_warning");
					accountBalanceWarning.html(warning + " " + $.t("public_key_actions")).show();
				}
			}
		}

		var $invoker = $(e.relatedTarget);
		var tab = $invoker.data("detailstab");
		if (tab) {
			_showTab(tab)
		}
	});

	function _showTab(tab){
		var tabListItem = $("#account_details_modal").find("li[data-tab=" + tab + "]");
		tabListItem.siblings().removeClass("active");
		tabListItem.addClass("active");
		$(".account_details_modal_content").hide();
		var content = $("#account_details_modal_" + tab);
		content.show();
	}

	accountDetailsModal.find("ul.nav li").click(function(e) {
		e.preventDefault();
		var tab = $(this).data("tab");
		_showTab(tab);
	});

	accountDetailsModal.on("hidden.bs.modal", function() {
		$(this).find(".account_details_modal_content").hide();
		$(this).find("ul.nav li.active").removeClass("active");
		$("#account_details_balance_nav").addClass("active");
		$("#account_details_modal_account_qr_code").empty();
		$("#account_details_modal_passphrase_qr_code").empty();
	});

    NRS.setAccountDetailsPassword = function(password) {
        _password = password;
    };

    $("#account_details_modal_account_display").on("click", function() {
        $("#account_details_modal_account_display").hide();
        $("#account_details_modal_passphrase_display").show();
        $("#account_details_modal_passphrase_qr_code").empty();
        NRS.generateQRCode("#account_details_modal_account_qr_code", NRS.accountRS);
    });

    $("#account_details_modal_passphrase_display").on("click", function() {
        $("#account_details_modal_passphrase_display").hide();
        $("#account_details_modal_account_display").show();
        $("#account_details_modal_account_qr_code").empty();
        NRS.generateQRCode("#account_details_modal_passphrase_qr_code", _password);
    });

    $("#account_details_modal_paper_wallet_link").on("click", function() {
		NRS.printPaperWallet(_password);
    });

    return NRS;
}(NRS || {}, jQuery));