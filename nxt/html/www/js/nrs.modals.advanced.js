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
var NRS = (function(NRS, $, undefined) {
	var _password;

	NRS.setAdvancedModalPassword = function (password) {
		_password = password;
	};

	NRS.showRawTransactionModal = function(transaction) {
        if (transaction.unsignedTransactionBytes && !transaction.transactionBytes) {
            $("#raw_transaction_modal_unsigned_transaction_bytes").val(transaction.unsignedTransactionBytes);
            NRS.generateQRCode("#raw_transaction_modal_unsigned_bytes_qr_code", transaction.unsignedTransactionBytes, 14);
            $("#raw_transaction_modal_unsigned_transaction_bytes_container").show();
            $("#raw_transaction_modal_unsigned_bytes_qr_code_container").show();
            $("#raw_transaction_broadcast").show();
        } else {
            $("#raw_transaction_modal_unsigned_transaction_bytes_container").hide();
            $("#raw_transaction_modal_unsigned_bytes_qr_code_container").hide();
            $("#raw_transaction_broadcast").hide();
        }

        if (transaction.transactionJSON) {
            var namePrefix;
            if (transaction.transactionBytes) {
                $("#raw_transaction_modal_transaction_json_label").html($.t("signed_transaction_json"));
                namePrefix = "signed";
            } else {
                $("#raw_transaction_modal_transaction_json_label").html($.t("unsigned_transaction_json"));
                namePrefix = "unsigned";
            }
            var unsignedTransactionJson = $("#raw_transaction_modal_transaction_json");
            var jsonStr = JSON.stringify(transaction.transactionJSON);
            unsignedTransactionJson.val(jsonStr);
            var downloadLink = $("#raw_transaction_modal_transaction_json_download");
            if (window.URL && NRS.isFileReaderSupported()) {
                var jsonAsBlob = new Blob([jsonStr], {type: 'text/plain'});
                downloadLink.prop('download', namePrefix + '.transaction.' + transaction.transactionJSON.timestamp + '.json');
                try {
                    downloadLink.prop('href', window.URL.createObjectURL(jsonAsBlob));
				} catch(e) {
                    NRS.logConsole("Desktop Application in Java 8 does not support createObjectURL");
                    downloadLink.hide();
				}
            } else {
                downloadLink.hide();
            }
        }

        if (transaction.unsignedTransactionBytes && !transaction.transactionBytes) {
            $('#raw_transaction_modal_signature_reader').hide();
            $("#raw_transaction_modal_signature_container").show();
        } else {
            $("#raw_transaction_modal_signature").val("");
            $("#raw_transaction_modal_signature_container").hide();
        }

		if (transaction.transactionBytes) {
            $("#raw_transaction_modal_transaction_bytes").val(transaction.transactionBytes);
            $("#raw_transaction_modal_transaction_bytes_container").show();
        } else {
            $("#raw_transaction_modal_transaction_bytes_container").hide();
        }

		if (transaction.fullHash) {
			$("#raw_transaction_modal_full_hash").val(transaction.fullHash);
			$("#raw_transaction_modal_full_hash_container").show();
		} else {
			$("#raw_transaction_modal_full_hash_container").hide();
		}

		if (transaction.signatureHash) {
			$("#raw_transaction_modal_signature_hash").val(transaction.signatureHash);
			$("#raw_transaction_modal_signature_hash_container").show();
		} else {
			$("#raw_transaction_modal_signature_hash_container").hide();
		}

		$("#raw_transaction_modal").modal("show");
	};

    $(".qr_code_reader_link").click(function(e) {
        e.preventDefault();
        var id = $(this).attr("id");
        var readerId = id.substring(0, id.lastIndexOf("_"));
        var outputId = readerId.substring(0, readerId.lastIndexOf("_"));
        NRS.scanQRCode(readerId, function(data) {
            $("#" + outputId).val(data);
        });
    });

    $("#broadcast_transaction_json_file, #unsigned_transaction_json_file").change(function(e) {
        e.preventDefault();
        var fileInputId = $(this).attr('id');
        var textAreaId = fileInputId.substring(0, fileInputId.lastIndexOf("_"));
        var fileInput = document.getElementById(fileInputId);
        var file = fileInput.files[0];
        if (!file) {
            $.growl($.t("select_file_to_upload"));
            return;
        }
        var fileReader = new FileReader();
        fileReader.onload = function(fileLoadedEvent) {
            var textFromFile = fileLoadedEvent.target.result;
            $("#" + textAreaId).val(textFromFile);
        };
        fileReader.readAsText(file, "UTF-8");
    });

    NRS.forms.broadcastTransaction = function(modal) {
        // The problem is that broadcastTransaction is invoked by different modals
        // We need to find the correct form in case the modal has more than one
        var data;
        if (modal.attr('id') == "transaction_json_modal") {
            data = NRS.getFormData($("#broadcast_json_form"));
        } else {
            data = NRS.getFormData(modal.find("form:first"));
        }
        if (data.transactionJSON) {
            var signature = data.signature;
            try {
                var transactionJSON = JSON.parse(data.transactionJSON);
            } catch (e) {
                return { errorMessage: "Invalid Transaction JSON" }
            }
            if (!transactionJSON.signature) {
                transactionJSON.signature = signature;
            }
            data.transactionJSON = JSON.stringify(transactionJSON);
            delete data.signature;
        }
        return { data: data };
    };

	NRS.initAdvancedModalFormValues = function($modal) {
		$(".phasing_number_accounts_group").find("input[name=phasingQuorum]").val(1);

		var type = $modal.data('transactionType');
		var subType = $modal.data('transactionSubtype');
		if (type != undefined && subType != undefined) {
			if (NRS.transactionTypes[type]["subTypes"][subType]["serverConstants"]["isPhasingSafe"] == true) {
				$modal.find('.phasing_safe_alert').hide();
			} else {
				$modal.find('.phasing_safe_alert').show();
			}
		}

		var minDuration = 0;
		var maxDuration = 0;

		if (NRS.accountInfo.phasingOnly) {
			minDuration = NRS.accountInfo.phasingOnly.minDuration;
			maxDuration = NRS.accountInfo.phasingOnly.maxDuration;
		}

		if (maxDuration == 0) {
			maxDuration = NRS.constants.SERVER.maxPhasingDuration;
		}

		var context = {
			labelText: "Finish Height",
			labelI18n: "finish_height",
			helpI18n: "approve_transaction_finish_height_help",
			inputName: "phasingFinishHeight",
			initBlockHeight: NRS.lastBlockHeight + Math.round(minDuration + (maxDuration - minDuration) / 2),
			changeHeightBlocks: 500
		};
		var $elems = NRS.initModalUIElement($modal, '.phasing_finish_height_group', 'block_height_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		$elems = NRS.initModalUIElement($modal, '.mandatory_approval_finish_height_group', 'block_height_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		context = {
			labelText: "Amount NXT",
			labelI18n: "amount_nxt",
			helpI18n: "approve_transaction_amount_help",
			inputName: "phasingQuorumNXT",
			addonText: "NXT",
			addonI18n: "nxt_unit"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_transaction_amount_nxt', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		context = {
			labelText: "Asset Quantity",
			labelI18n: "asset_quantity",
			helpI18n: "approve_transaction_amount_help",
			inputName: "phasingQuorumQNTf",
			addonText: "Quantity",
			addonI18n: "quantity"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_transaction_asset_quantity', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		context = {
			labelText: "Currency Units",
			labelI18n: "currency_units",
			helpI18n: "approve_transaction_amount_help",
			inputName: "phasingQuorumQNTf",
			addonText: "Units",
			addonI18n: "units"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_transaction_currency_units', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		context = {
			labelText: "Accounts (Whitelist)",
			labelI18n: "accounts_whitelist",
			helpI18n: "approve_transaction_accounts_requested_help",
			inputName: "phasingWhitelisted"
		};
		$elems = NRS.initModalUIElement($modal, '.add_approval_whitelist_group', 'multi_accounts_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		context = {
			labelText: "Min Balance Type",
			labelI18n: "min_balance_type",
			helpI18n: "approve_transaction_min_balance_type_help",
			selectName: "phasingMinBalanceModel"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_min_balance_model_group', 'min_balance_model_modal_ui_element', context);
		$elems.find('select').prop("disabled", true);

		$elems.each(function() {
			var $mbGroup = $(this).closest('div.approve_min_balance_model_group');
			if ($mbGroup.hasClass("approve_mb_balance")) {
				$mbGroup.find('option[value="2"], option[value="3"]').remove();
			}
			if ($mbGroup.hasClass("approve_mb_asset")) {
				$mbGroup.find('option[value="1"], option[value="3"]').remove();
			}
			if ($mbGroup.hasClass("approve_mb_currency")) {
				$mbGroup.find('option[value="1"], option[value="2"]').remove();
			}
		});

		context = {
			labelText: "Min Balance",
			labelI18n: "min_balance",
			helpI18n: "approve_transaction_min_balance_help",
			inputName: "",
			addonText: "",
			addonI18n: ""
		};
		context['inputName'] = 'phasingMinBalanceNXT';
		context['addonText'] = 'NXT';
		context['addonI18n'] = 'nxt_unit';
		$elems = NRS.initModalUIElement($modal, '.approve_min_balance_nxt', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context['inputName'] = 'phasingMinBalanceQNTf';
		context['addonText'] = 'Quantity';
		context['addonI18n'] = 'quantity';
		$elems = NRS.initModalUIElement($modal, '.approve_min_balance_asset_quantity', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context['inputName'] = 'phasingMinBalanceQNTf';
		context['addonText'] = 'Units';
		context['addonI18n'] = 'units';
		$elems = NRS.initModalUIElement($modal, '.approve_min_balance_currency_units', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context = {
			labelText: "Asset",
			labelI18n: "asset",
			inputIdName: "phasingHolding",
			inputDecimalsName: "phasingHoldingDecimals",
			helpI18n: "add_asset_modal_help"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_holding_asset', 'add_asset_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems = NRS.initModalUIElement($modal, '.approve_holding_asset_optional', 'add_asset_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context = {
			labelText: "Currency",
			labelI18n: "currency",
			inputCodeName: "phasingHoldingCurrencyCode",
			inputIdName: "phasingHolding",
			inputDecimalsName: "phasingHoldingDecimals",
			helpI18n: "add_currency_modal_help"
		};
		$elems = NRS.initModalUIElement($modal, '.approve_holding_currency', 'add_currency_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems = NRS.initModalUIElement($modal, '.approve_holding_currency_optional', 'add_currency_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		var selectName = $modal.attr('id') == "hash_modal" ? "hashAlgorithm" : "phasingHashedSecretAlgorithm";
		context = {
			labelText: "HASH ALGORITHM",
			labelI18n: "hash_algorithm",
			selectName: selectName
		};
		NRS.initModalUIElement($modal, '.hash_algorithm_model_group', 'hash_algorithm_model_modal_ui_element', context);

		_setMandatoryApproval($modal);
	};

	function _setMandatoryApproval($modal) {
		$modal.one('shown.bs.modal', function() {
			var requestType = $modal.find('input[name="request_type"]').val();

			if (requestType != "approveTransaction"
				&& NRS.accountInfo.accountControls && $.inArray('PHASING_ONLY', NRS.accountInfo.accountControls) > -1
				&& NRS.accountInfo.phasingOnly
				&& NRS.accountInfo.phasingOnly.votingModel >= 0) {

				$modal.find('.advanced_mandatory_approval input').prop('disabled', false);
				$modal.find('.advanced_mandatory_approval').show();

			} else {
				$modal.find('.advanced_mandatory_approval').hide();
			}
		});
	}

	$('.approve_tab_list a[data-toggle="tab"]').on('shown.bs.tab', function () {
        var $am = $(this).closest('.approve_modal');
        $am.find('.tab-pane input, .tab-pane select').prop('disabled', true);
        $am.find('.tab-pane.active input, .tab-pane.active select').prop('disabled', false);
        if ($(this).hasClass("at_no_approval")) {
			$am.find('.approve_whitelist_accounts').hide();
        	$am.find('.approve_whitelist_accounts input').prop('disabled', true);
        } else {
        	$am.find('.approve_whitelist_accounts input').prop('disabled', false);
        	$am.find('.approve_whitelist_accounts').show();
        }
        $('.modal .approve_modal .approve_min_balance_model_group:visible select').trigger('change');
    });

	$('body').on('change', '.modal .approve_modal .approve_min_balance_model_group select', function() {
		var $tabPane = $(this).closest('div.tab_pane_approve');
		var mbModelId = $(this).val();
		for(var id=0; id<=3; id++) {
			$tabPane.find('.approve_mb_model_' + String(id) + ' input').attr('disabled', true);
			$tabPane.find('.approve_mb_model_' + String(id)).hide();
		}
		$tabPane.find('.approve_mb_model_' + String(mbModelId) + ' input').attr('disabled', false);
		$tabPane.find('.approve_mb_model_' + String(mbModelId)).show();
	});

    var transactionJSONModal = $("#transaction_json_modal");
    transactionJSONModal.on("show.bs.modal", function(e) {
		$(this).find(".output").hide();
        $(this).find(".upload_container").hide();
		$(this).find("#unsigned_transaction_bytes_reader").hide();
		$(this).find(".tab_content:first").show();
        $("#transaction_json_modal_button").text($.t("sign_transaction")).data("resetText", $.t("sign_transaction")).data("form", "sign_transaction_form");
		var $invoker = $(e.relatedTarget);
		var isOffline = !!$invoker.data("offline");
		if (isOffline) {
			$(this).find("ul.nav li").hide();
			$(this).find("ul.nav li:first").show();
			$("#validate_transaction").prop("disabled", "true");
			$(".mobile-offline").val("true");
		}
	});

    transactionJSONModal.on("hidden.bs.modal", function() {
		var reader = $('#unsigned_transaction_bytes_reader');
		if (reader.data('stream')) {
		    reader.html5_qrcode_stop();
        }
		$(this).find(".tab_content").hide();
		$(this).find("ul.nav li.active").removeClass("active");
		$(this).find("ul.nav li:first").addClass("active");
		$(this).find(".output").hide();
	});

    transactionJSONModal.find("ul.nav li").click(function(e) {
		e.preventDefault();
		var tab = $(this).data("tab");
		$(this).siblings().removeClass("active");
		$(this).addClass("active");
		$(this).closest(".modal").find(".tab_content").hide();
		if (tab == "broadcast_json") {
			$("#transaction_json_modal_button").text($.t("broadcast")).data("resetText", $.t("broadcast")).data("form", "broadcast_json_form");
		} else if(tab == "parse_transaction") {
			$("#transaction_json_modal_button").text($.t("parse_transaction")).data("resetText", $.t("parse_transaction")).data("form", "parse_transaction_form");
		} else if(tab == "calculate_full_hash") {
			$("#transaction_json_modal_button").text($.t("calculate_full_hash")).data("resetText", $.t("calculate_full_hash")).data("form", "calculate_full_hash_form");
		} else {
			$("#transaction_json_modal_button").text($.t("sign_transaction")).data("resetText", $.t("sign_transaction")).data("form", "sign_transaction_form");
		}
		$("#transaction_json_modal_" + tab).show();
	});

	NRS.forms.broadcastTransactionComplete = function() {
		$("#parse_transaction_form").find(".error_message").hide();
        $("#transaction_json_modal").modal("hide");
	};

	NRS.forms.parseTransactionComplete = function(response) {
		$("#parse_transaction_form").find(".error_message").hide();
        var details = $.extend({}, response);
        if (response.attachment) {
            delete details.attachment;
        }
        $("#parse_transaction_output_table").find("tbody").empty().append(NRS.createInfoTable(details, true));
		$("#parse_transaction_output").show();
	};

	NRS.forms.parseTransactionError = function() {
		$("#parse_transaction_output_table").find("tbody").empty();
		$("#parse_transaction_output").hide();
	};

	NRS.forms.calculateFullHashComplete = function(response) {
		$("#calculate_full_hash_form").find(".error_message").hide();
		$("#calculate_full_hash_output_table").find("tbody").empty().append(NRS.createInfoTable(response, true));
		$("#calculate_full_hash_output").show();
	};

	NRS.forms.calculateFullHashError = function() {
		$("#calculate_full_hash_output_table").find("tbody").empty();
		$("#calculate_full_hash_output").hide();
	};

    NRS.forms.broadcastTransactionComplete = function() {
   		$("#parse_transaction_form").find(".error_message").hide();
   		$("#transaction_json_modal").modal("hide");
   	};

	function updateSignature(signature) {
		$("#transaction_signature").val(signature);
		NRS.generateQRCode("#transaction_signature_qr_code", signature, 8);
		$("#signature_output").show();
	}

	NRS.forms.signTransactionComplete = function(response) {
        $("#sign_transaction_form").find(".error_message").hide();
        var signedTransactionJson = $("#signed_transaction_json");
        var jsonStr = JSON.stringify(response.transactionJSON);
        signedTransactionJson.val(jsonStr);
        var downloadLink = $("#signed_transaction_json_download");
        if (window.URL && NRS.isFileReaderSupported()) {
            var jsonAsBlob = new Blob([jsonStr], {type: 'text/plain'});
            downloadLink.prop('download', 'signed.transaction.' + response.transactionJSON.timestamp + '.json');
            try {
                downloadLink.prop('href', window.URL.createObjectURL(jsonAsBlob));
			} catch(e) {
            	NRS.logConsole("Desktop Application in Java 8 does not support createObjectURL");
                downloadLink.hide();
			}
        } else {
            downloadLink.hide();
        }
        $("#signed_json_output").show();
		updateSignature(response.transactionJSON.signature);
    };

    NRS.forms.signTransaction = function() {
        var data = NRS.getFormData($("#sign_transaction_form"));
		if (data.unsignedTransactionBytes && !data.validate) {
			NRS.logConsole("Sign transaction locally");
			var output = {};
			var secretPhrase = (NRS.rememberPassword ? _password : data.secretPhrase);
			var isOffline = $(".mobile-offline").val();
			if (NRS.getAccountId(secretPhrase) == NRS.account || isOffline) {
				try {
					var signature = NRS.signBytes(data.unsignedTransactionBytes, converters.stringToHexString(secretPhrase));
					updateSignature(signature);
				} catch (e) {
					output.errorMessage = e.message;
				}
			} else {
				output.errorMessage = $.t("error_passphrase_incorrect");
			}
			output.stop = true;
			output.keepOpen = true;
			return output;
		}
        data.validate = (data.validate ? "true" : "false");
        return { data: data };
    };

	return NRS;
}(NRS || {}, jQuery));
