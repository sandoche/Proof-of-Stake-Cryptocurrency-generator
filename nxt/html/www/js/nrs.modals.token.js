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
    var _password = null;
    var tokenModal = $("#token_modal");
    tokenModal.on("show.bs.modal", function(e) {
		$("#generate_token_output, #decode_token_output, #generate_token_output_qr_code").html("").hide();

		$("#token_modal_generate_token").show();
		$("#generate_token_button").show();
		$("#validate_token_button").hide();
		var $invoker = $(e.relatedTarget);
		var isOffline = !!$invoker.data("offline");
		if (isOffline) {
			$(this).find("ul.nav li").hide();
			$(this).find("ul.nav li:first").show();
            $(this).find(".mobile-offline").val("true");
		}
	});
	
	NRS.forms.decodeToken = function() {
		return {
			data: {
				"website": $("#decode_token_data").val(),
				"token": $("#decode_token_token").val()
			}
		};
	};
	
	NRS.forms.decodeTokenComplete = function(response) {
		$("#token_modal").find(".error_message").hide();

		if (response.valid) {
			$("#decode_token_output").html($.t("success_valid_token", {
				"account_link": NRS.getAccountLink(response, "account"),
				"timestamp": NRS.formatTimestamp(response.timestamp)
			})).addClass("callout-info").removeClass("callout-danger").show();
		} else {
			$("#decode_token_output").html($.t("error_invalid_token", {
				"account_link": NRS.getAccountLink(response, "account"),
				"timestamp": NRS.formatTimestamp(response.timestamp)
			})).addClass("callout-danger").removeClass("callout-info").show();
		}
	};

	NRS.forms.decodeTokenError = function() {
		$("#decode_token_output").hide();
	};

    tokenModal.find("ul.nav li").click(function(e) {
		e.preventDefault();
		var tab = $(this).data("tab");
		$(this).siblings().removeClass("active");
		$(this).addClass("active");
		$(".token_modal_content").hide();
		var content = $("#token_modal_" + tab);
		if (tab == "generate_token") {
			$("#generate_token_button").show();
			$("#validate_token_button").hide();
		} else {
            $("#generate_token_button").hide();
            $("#validate_token_button").show();
		}

		$("#token_modal").find(".error_message").hide();
		content.show();
	});

	tokenModal.on("hidden.bs.modal", function() {
		$(this).find(".token_modal_content").hide();
		$(this).find("ul.nav li.active").removeClass("active");
		$("#generate_token_nav").addClass("active");
	});

    $("#generate_token_button").click(function (e) {
		var data = NRS.getFormData($("#generate_token_form"));
        var website = data.website;
        var tokenOutput = $("#generate_token_output");
        var outputQrCodeContainer = $("#generate_token_output_qr_code");
        if (!website || website == "") {
            tokenOutput.html($.t("data_required_field"));
            tokenOutput.addClass("callout-danger").removeClass("callout-info").show();
            outputQrCodeContainer.hide();
            return;
        }
		var isOffline = !!$(this).find(".mobile-offline").val();
        var secretPhrase;
        if (!NRS.rememberPassword) {
            secretPhrase = data.secretPhrase;
			var accountId = NRS.getAccountId(secretPhrase);
			if (accountId != NRS.account && !isOffline) {
				tokenOutput.html($.t("error_incorrect_passphrase"));
				tokenOutput.addClass("callout-danger").removeClass("callout-info").show();
                outputQrCodeContainer.hide();
				return;
			}
		} else {
        	secretPhrase = _password;
		}
        var token = NRS.generateToken(website, secretPhrase);
        tokenOutput.html($.t("generated_token_is") + "<br/><br/><textarea readonly style='width:100%' rows='3'>" + token + "</textarea>");
        tokenOutput.addClass("callout-info").removeClass("callout-danger").show();
        NRS.generateQRCode("#generate_token_output_qr_code", token, 14);
        outputQrCodeContainer.show();
        e.preventDefault();
    });

    NRS.setTokenPassword = function(password) {
        _password = password;
    };

    return NRS;
}(NRS || {}, jQuery));