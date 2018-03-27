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
	NRS.automaticallyCheckRecipient = function() {
        var $recipientFields = $("#send_money_recipient, #transfer_asset_recipient, #transfer_currency_recipient, " +
        "#send_message_recipient, #add_contact_account_id, #update_contact_account_id, #lease_balance_recipient, " +
        "#transfer_alias_recipient, #sell_alias_recipient, #set_account_property_recipient, #delete_account_property_recipient, " +
		"#add_monitored_account_recipient");

		$recipientFields.on("blur", function() {
			$(this).trigger("checkRecipient");
		});

		$recipientFields.on("checkRecipient", function() {
			var value = $(this).val();
			var modal = $(this).closest(".modal");

			if (value && value != NRS.getAccountMask("_")) {
				NRS.checkRecipient(value, modal);
			} else {
				modal.find(".account_info").hide();
			}
		});

		$recipientFields.on("oldRecipientPaste", function() {
			var modal = $(this).closest(".modal");

			var callout = modal.find(".account_info").first();

			callout.removeClass("callout-info callout-danger callout-warning").addClass("callout-danger").html($.t("error_numeric_ids_not_allowed")).show();
		});
	};

	$("#send_message_modal, #send_money_modal, #transfer_currency_modal, #add_contact_modal, #set_account_property_modal, #delete_account_property_modal").on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
		var account = $invoker.data("account");
		if (!account) {
			account = $invoker.data("contact");
		}
		if (account) {
			var $inputField = $(this).find("input[name=recipient], input[name=account_id]").not("[type=hidden]");
			if (!NRS.isRsAccount(account)) {
				$inputField.addClass("noMask");
			}
			$inputField.val(account).trigger("checkRecipient");
		}
	});

	//todo later: http://twitter.github.io/typeahead.js/
	var modal = $(".modal");
    modal.on("click", "span.recipient_selector button, span.plain_address_selector button", function(e) {
		if (!Object.keys(NRS.contacts).length) {
			e.preventDefault();
			e.stopPropagation();
			return;
		}

		var $list = $(this).parent().find("ul");

		$list.empty();

		for (var accountId in NRS.contacts) {
			if (!NRS.contacts.hasOwnProperty(accountId)) {
				continue;
			}
			$list.append("<li><a href='#' data-contact-id='" + accountId + "' data-contact='" + String(NRS.contacts[accountId].name).escapeHTML() + "'>" + String(NRS.contacts[accountId].name).escapeHTML() + "</a></li>");
		}
	});

	modal.on("click", "span.recipient_selector ul li a", function(e) {
		e.preventDefault();
		$(this).closest("form").find("input[name=converted_account_id]").val("");
		$(this).closest("form").find("input[name=recipient],input[name=account_id]").not("[type=hidden]").trigger("unmask").val($(this).data("contact")).trigger("blur");
	});

	modal.on("click", "span.plain_address_selector ul li a", function(e) {
		e.preventDefault();
		$(this).closest(".input-group").find("input.plain_address_selector_input").not("[type=hidden]").trigger("unmask").val($(this).data("contact-id")).trigger("blur");
	});

	modal.on("keyup blur show", ".plain_address_selector_input", function() {
		var currentValue = $(this).val();
		var contactInfo;
		if (NRS.contacts[currentValue]) {
			contactInfo = NRS.contacts[currentValue]['name'];
		} else {
			contactInfo = " ";
		}
		$(this).closest(".input-group").find(".pas_contact_info").text(contactInfo);
	});

	NRS.forms.sendMoneyComplete = function(response, data) {
		if (!(data["_extra"] && data["_extra"].convertedAccount) && !(data.recipient in NRS.contacts)) {
			$.growl($.t("success_send_money") + " <a href='#' data-account='" + NRS.getAccountFormatted(data, "recipient") + "' data-toggle='modal' data-target='#add_contact_modal' style='text-decoration:underline'>" + $.t("add_recipient_to_contacts_q") + "</a>", {
				"type": "success"
			});
		} else {
			$.growl($.t("send_money_submitted"), {
				"type": "success"
			});
		}
	};

	NRS.sendMoneyShowAccountInformation = function(accountId) {
		NRS.getAccountError(accountId, function(response) {
			if (response.type == "success") {
				$("#send_money_account_info").hide();
			} else {
				$("#send_money_account_info").html(response.message).show();

			}
		});
	};

	NRS.getAccountError = function(accountId, callback) {
		NRS.sendRequest("getAccount", {
			"account": accountId
		}, function(response) {
			var result;
			if (response.publicKey) {
				if (response.name){
					result = {
						"type": "info",
						"message": $.t("recipient_info_with_name", {
							"name" : NRS.unescapeRespStr(response.name),
							"amount": NRS.formatAmount(response.unconfirmedBalanceNQT, false, true),
                            "symbol": NRS.constants.COIN_SYMBOL
						}),
						"account": response
					};
				}
				else{
					result = {
						"type": "info",
						"message": $.t("recipient_info", {
							"amount": NRS.formatAmount(response.unconfirmedBalanceNQT, false, true),
                            "symbol": NRS.constants.COIN_SYMBOL
						}),
						"account": response
					};
				}
			} else {
				if (response.errorCode) {
					if (response.errorCode == 4) {
						result = {
							"type": "danger",
							"message": $.t("recipient_malformed") + (!NRS.isRsAccount(accountId) ? " " + $.t("recipient_alias_suggestion") : ""),
							"account": null
						};
					} else if (response.errorCode == 5) {
						result = {
							"type": "warning",
							"message": $.t("recipient_unknown_pka"),
							"account": null,
							"noPublicKey": true
						};
					} else {
						result = {
							"type": "danger",
							"message": $.t("recipient_problem") + " " + NRS.unescapeRespStr(response.errorDescription),
							"account": null
						};
					}
				} else {
					result = {
						"type": "warning",
						"message": $.t("recipient_no_public_key_pka", {
							"amount": NRS.formatAmount(response.unconfirmedBalanceNQT, false, true),
                            "symbol": NRS.constants.COIN_SYMBOL
						}),
						"account": response,
						"noPublicKey": true
					};
				}
			}
			result.message = result.message.escapeHTML();
			callback(result);
		});
	};

	NRS.correctAddressMistake = function(el) {
		$(el).closest(".modal-body").find("input[name=recipient],input[name=account_id]").val($(el).data("address")).trigger("blur");
	};

	NRS.checkRecipient = function(account, modal) {
		var classes = "callout-info callout-danger callout-warning";

		var callout = modal.find(".account_info").first();
		var accountInputField = modal.find("input[name=converted_account_id]");
		var merchantInfoField = modal.find("input[name=merchant_info]");

		accountInputField.val("");
		merchantInfoField.val("");

		account = $.trim(account);

		//solomon reed. Btw, this regex can be shortened..
		if (NRS.isRsAccount(account)) {
			var address = new NxtAddress();

			if (address.set(account)) {
				NRS.getAccountError(account, function(response) {
					if (response.noPublicKey && account!=NRS.accountRS) {
						modal.find(".recipient_public_key").show();
					} else {
						modal.find("input[name=recipientPublicKey]").val("");
						modal.find(".recipient_public_key").hide();
					}
					if (response.account && response.account.description) {
						checkForMerchant(response.account.description, modal);
					}
					
					if (account==NRS.accountRS)
						callout.removeClass(classes).addClass("callout-" + response.type).html("This is your account").show();
					else{
						callout.removeClass(classes).addClass("callout-" + response.type).html(response.message).show();
					}
				});
			} else {
				if (address.guess.length == 1) {
					callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed_suggestion", {
						"recipient": "<span class='malformed_address' data-address='" + NRS.escapeRespStr(address.guess[0]) + "' onclick='NRS.correctAddressMistake(this);'>" + address.format_guess(address.guess[0], account) + "</span>"
					})).show();
				} else if (address.guess.length > 1) {
					var html = $.t("recipient_malformed_suggestion", {
						"count": address.guess.length
					}) + "<ul>";
					for (var i = 0; i < address.guess.length; i++) {
						html += "<li><span class='malformed_address' data-address='" + NRS.escapeRespStr(address.guess[i]) + "' onclick='NRS.correctAddressMistake(this);'>" + address.format_guess(address.guess[i], account) + "</span></li>";
					}

					callout.removeClass(classes).addClass("callout-danger").html(html).show();
				} else {
					callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
				}
			}
		} else if (!NRS.isNumericAccount(account)) {
			if (account.charAt(0) != '@') {
				NRS.storageSelect("contacts", [{
					"name": account
				}], function(error, contact) {
					if (!error && contact.length) {
						contact = contact[0];
						NRS.getAccountError(contact.accountRS, function(response) {
							if (response.noPublicKey && account!=NRS.account) {
								modal.find(".recipient_public_key").show();
							} else {
								modal.find("input[name=recipientPublicKey]").val("");
								modal.find(".recipient_public_key").hide();
							}
							if (response.account && response.account.description) {
								checkForMerchant(response.account.description, modal);
							}

							callout.removeClass(classes).addClass("callout-" + response.type).html($.t("contact_account_link", {
								"account_id": NRS.getAccountFormatted(contact, "account")
							}) + " " + response.message).show();

							if (response.type == "info" || response.type == "warning") {
								accountInputField.val(contact.accountRS);
							}
						});
					} else if (/^[a-z0-9]+$/i.test(account)) {
						NRS.checkRecipientAlias(account, modal);
					} else {
						callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
					}
				});
			} else if (/^[a-z0-9@]+$/i.test(account)) {
				if (account.charAt(0) == '@') {
					account = account.substring(1);
					NRS.checkRecipientAlias(account, modal);
				}
			} else {
				callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
			}
		} else {
			callout.removeClass(classes).addClass("callout-danger").html($.t("error_numeric_ids_not_allowed")).show();
		}
	};

	NRS.checkRecipientAlias = function(account, modal) {
		var classes = "callout-info callout-danger callout-warning";
		var callout = modal.find(".account_info").first();
		var accountInputField = modal.find("input[name=converted_account_id]");

		accountInputField.val("");

		NRS.sendRequest("getAlias", {
			"aliasName": account
		}, function(response) {
			if (response.errorCode) {
				callout.removeClass(classes).addClass("callout-danger").html($.t("error_invalid_account_id")).show();
			} else {
				if (response.aliasURI) {
					var alias = String(response.aliasURI);
					var timestamp = response.timestamp;

					var regex_1 = /acct:(.*)@nxt/;
					var regex_2 = /nacc:(.*)/;

					var match = alias.match(regex_1);

					if (!match) {
						match = alias.match(regex_2);
					}

					if (match && match[1]) {
						match[1] = String(match[1]).toUpperCase();

						if (NRS.isNumericAccount(match[1])) {
							var address = new NxtAddress();

							if (address.set(match[1])) {
								match[1] = address.toString();
							} else {
								accountInputField.val("");
								callout.removeClass(classes).addClass("callout-danger").html($.t("error_invalid_account_id")).show();
								return;
							}
						}

						NRS.getAccountError(match[1], function(response) {
							if (response.noPublicKey) {
								modal.find(".recipient_public_key").show();
							} else {
								modal.find("input[name=recipientPublicKey]").val("");
								modal.find(".recipient_public_key").hide();
							}
							if (response.account && response.account.description) {
								checkForMerchant(response.account.description, modal);
							}

							callout.removeClass(classes).addClass("callout-" + response.type).html($.t("alias_account_link", {
								"account_id": NRS.escapeRespStr(match[1])
							}) + " " + response.message + " " + $.t("alias_last_adjusted", {
								"timestamp": NRS.formatTimestamp(timestamp)
							})).show();

							if (response.type == "info" || response.type == "warning") {
								accountInputField.val(NRS.escapeRespStr(match[1]));
							}
						});
					} else {
						callout.removeClass(classes).addClass("callout-danger").html($.t("alias_account_no_link") + (!alias ? $.t("error_uri_empty") : $.t("uri_is", {
							"uri": NRS.escapeRespStr(alias)
						}))).show();
					}
				} else if (response.aliasName) {
					callout.removeClass(classes).addClass("callout-danger").html($.t("error_alias_empty_uri")).show();
				} else {
					callout.removeClass(classes).addClass("callout-danger").html(response.errorDescription ? $.t("error") + ": " + NRS.escapeRespStr(response.errorDescription) : $.t("error_alias")).show();
				}
			}
		});
	};

	function checkForMerchant(accountInfo, modal) {
		var requestType = modal.find("input[name=request_type]").val(); // only works for single request type per modal
		if (requestType == "sendMoney" || requestType == "transferAsset") {
			if (accountInfo.match(/merchant/i)) {
				modal.find("input[name=merchant_info]").val(accountInfo);
				var checkbox = modal.find("input[name=add_message]");
				if (!checkbox.is(":checked")) {
					checkbox.prop("checked", true).trigger("change");
				}
			}
		}
	}

	return NRS;
}(NRS || {}, jQuery));