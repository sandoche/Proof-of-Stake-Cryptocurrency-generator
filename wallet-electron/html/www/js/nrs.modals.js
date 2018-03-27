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
	NRS.fetchingModalData = false;
	NRS.modalStack = [];
	var isFakeWarningDisplayed;

	// save the original function object
	var _superModal = $.fn.modal;

	// add locked as a new option
	$.extend(_superModal.Constructor.DEFAULTS, {
		locked: false
	});

	// capture the original hide
	var _hide = _superModal.Constructor.prototype.hide;

	// add the lock, unlock and override the hide of modal
	$.extend(_superModal.Constructor.prototype, {
		// locks the dialog so that it cannot be hidden
		lock: function() {
			this.options.locked = true;
			this.$element.addClass("locked");
		}
		// unlocks the dialog so that it can be hidden by 'esc' or clicking on the backdrop (if not static)
		,
		unlock: function() {
			this.options.locked = false;
			this.$element.removeClass("locked");
		},
		// override the original hide so that the original is only called if the modal is unlocked
		hide: function() {
			if (this.options.locked) {
                return;
            }
			_hide.apply(this, arguments);
		}
	});

	//Reset scroll position of tab when shown.
	$('a[data-toggle="tab"]').on("shown.bs.tab", function(e) {
		var target = $(e.target).attr("href");
		$(target).scrollTop(0);
	});

	$(".add_message").on("change", function() {
		if ($(this).is(":checked")) {
			$(this).closest("form").find(".optional_message").fadeIn();
			$(this).closest(".form-group").css("margin-bottom", "5px");
		} else {
			$(this).closest("form").find(".optional_message").hide();
			$(this).closest(".form-group").css("margin-bottom", "");
		}
	});

	$(".add_note_to_self").on("change", function() {
		if ($(this).is(":checked")) {
			$(this).closest("form").find(".optional_note").fadeIn();
		} else {
			$(this).closest("form").find(".optional_note").hide();
		}
	});

	$(".do_not_broadcast").on("change", function() {
		if ($(this).is(":checked")) {
			$(this).closest("form").find(".optional_do_not_sign").fadeIn();
		} else {
			$(this).closest("form").find(".optional_do_not_sign").hide();
			$(this).closest("form").find(".optional_public_key").hide();
            $(this).closest("form").find(".optional_do_not_sign input").prop("checked", false);
            $(this).closest("form").find(".secret_phrase input").prop("disabled", false);
		}
	});

	$(".do_not_sign").on("change", function() {
        var passphrase = $(this).closest("form").find(".secret_phrase input");
        if ($(this).is(":checked")) {
            passphrase.val("");
            passphrase.prop("disabled", true);
            if (!NRS.accountInfo || !NRS.accountInfo.publicKey) {
                $(this).closest("form").find(".optional_public_key").fadeIn();
            }
        } else {
            passphrase.prop("disabled", false);
            $(this).closest("form").find(".optional_public_key").hide();
		}
	});

	//hide modal when another one is activated.
    var modal = $(".modal");
    modal.on("show.bs.modal", function() {
		var $inputFields = $(this).find("input[name=recipient], input[name=account_id], input[name=phasingWhitelisted]").not("[type=hidden]");
		$.each($inputFields, function() {
			if ($(this).hasClass("noMask")) {
				$(this).unmask();
				$(this).removeClass("noMask");
			} else {
				$(this).mask(NRS.getAccountMask("*"));
			}
		});

		var $visible_modal = $(".modal.in");
		if ($visible_modal.length) {
			if ($visible_modal.hasClass("locked")) {
				var $btn = $visible_modal.find("button.btn-primary:not([data-dismiss=modal])");
				NRS.unlockForm($visible_modal, $btn, true);
			} else {
				$visible_modal.modal("hide");
			}
		}

		$(this).find(".form-group").css("margin-bottom", "");
		$(this).find('.approve_tab_list a:first').click();
		NRS.initAdvancedModalFormValues($(this));
		$(this).find(".pas_contact_info").text(" ");
		// Activating context help popovers
		$(function () {
            $("[data-toggle='popover']").popover({
            	"html": true
            });
        });
        $(this).find("input[name=secretPhrase]").prop("disabled", false);
        var name = $(this).attr('id').replace('_modal', '');
	});

	modal.on("shown.bs.modal", function() {
		$(this).find("input[type=text]:first, textarea:first, input[type=password]:first").not("[readonly]").first().focus();
		$(this).find("input[name=converted_account_id]").val("");
		NRS.showedFormWarning = false; //maybe not the best place... we assume forms are only in modals?
		isFakeWarningDisplayed = false;
	});

	modal.on("hide.bs.modal", function() {
        // Turn off scanner when cancelling the modal during scan
        $(this).find(".scan-qr-code-reader").each(function() {
            var id = $(this)[0].id;
            var reader = $("#" + id);
            if (reader.is(':visible')) {
                NRS.scanQRCode(id, function() {});
            }
        });
    });

    //Reset form to initial state when modal is closed
    modal.on("hidden.bs.modal", function() {
		if(this.id === 'raw_transaction_modal') {
			var reader = $('#raw_transaction_modal_signature_reader');
			if (reader.data('stream')) {
                reader.html5_qrcode_stop();
            }
		}
		$(this).find("input[name=recipient], input[name=account_id]").not("[type=hidden]").trigger("unmask");
		$(this).find(":input:not(button)").each(function() {
			var defaultValue = $(this).data("default");
			var type = $(this).attr("type");
			var tag = $(this).prop("tagName").toLowerCase();
			if (type == "checkbox") {
				if (defaultValue == "checked") {
					$(this).prop("checked", true);
				} else {
					$(this).prop("checked", false);
				}
			} else if (type == "hidden") {
				if (defaultValue !== undefined) {
					$(this).val(defaultValue);
				}
			} else if (tag == "select") {
				if (defaultValue !== undefined) {
					$(this).val(defaultValue);
				} else {
					$(this).find("option:selected").prop("selected", false);
					$(this).find("option:first").prop("selected", "selected");
				}
			} else {
				if (defaultValue !== undefined) {
					$(this).val(defaultValue);
				} else {
					$(this).val("");
				}
			}
		});

		//Hidden form field
		$(this).find("input[name=converted_account_id]").val("");

		//Hide/Reset any possible error messages
		$(this).find(".callout-danger:not(.never_hide, .remote_warning), .error_message, .account_info").html("").hide();
		$(this).find(".advanced").hide();
		$(this).find(".recipient_public_key").hide();
		$(this).find(".optional_message, .optional_note, .optional_do_not_sign, .optional_public_key").hide();
		$(this).find(".advanced_info a").text($.t("advanced"));
		$(this).find(".advanced_extend").each(function(index, obj) {
			var normalSize = $(obj).data("normal");
			var advancedSize = $(obj).data("advanced");
			$(obj).removeClass("col-xs-" + advancedSize + " col-sm-" + advancedSize + " col-md-" + advancedSize).addClass("col-xs-" + normalSize + " col-sm-" + normalSize + " col-md-" + normalSize);
		});

		$("#create_poll_asset_id_group").css("display", "none");
		$("#create_poll_ms_currency_group").css("display", "none");
		$("#shuffling_asset_id_group").css("display", "none");
		$("#shuffling_ms_currency_group").css("display", "none");
        var pollTypeGroup = $("#create_poll_type_group");
        pollTypeGroup.removeClass("col-xs-6").addClass("col-xs-12");
		pollTypeGroup.removeClass("col-sm-6").addClass("col-sm-12");
		pollTypeGroup.removeClass("col-md-6").addClass("col-md-12");

		$(this).find(".tx-modal-approve").empty();
		NRS.showedFormWarning = false;
		isFakeWarningDisplayed = false;
        var isOffline = !!$(this).find(".mobile-offline").val();
        if (isOffline) {
            $("#mobile_settings_modal").modal();
		}
	});

	NRS.showModalError = function(errorMessage, $modal) {
		var $btn = $modal.find("button.btn-primary:not([data-dismiss=modal], .ignore)");
		$modal.find("button").prop("disabled", false);
		$modal.find(".error_message").html(NRS.escapeRespStr(errorMessage)).show();
		$btn.button("reset");
		$modal.modal("unlock");
	};

	NRS.closeModal = function($modal) {
		if (!$modal) {
			$modal = $("div.modal.in:first");
		}
		$modal.find("button").prop("disabled", false);
		var $btn = $modal.find("button.btn-primary:not([data-dismiss=modal], .ignore)");
		$btn.button("reset");
		$modal.modal("unlock");
		$modal.modal("hide");
	};

	$("button[data-dismiss='modal']").on("click", function() {
		NRS.modalStack = [];
	});

	$(".advanced_info a").on("click", function(e) {
		e.preventDefault();
		var $modal = $(this).closest(".modal");
		var text = $(this).text().toLowerCase();
		if (text == $.t("advanced").toLowerCase()) {
			var not = ".optional_note, .optional_do_not_sign, .optional_public_key";
			var requestType = $modal.find('input[name="request_type"]').val();
			if (requestType != "approveTransaction"
				&& NRS.accountInfo.accountControls && $.inArray('PHASING_ONLY', NRS.accountInfo.accountControls) > -1) {
				not += ", .approve_modal";
			}
			$modal.find(".advanced").not(not).fadeIn();
		} else {
			$modal.find(".advanced").hide();
		}

		$modal.find(".advanced_extend").each(function(index, obj) {
			var normalSize = $(obj).data("normal");
			var advancedSize = $(obj).data("advanced");
			if (text == "advanced") {
				$(obj).addClass("col-xs-" + advancedSize + " col-sm-" + advancedSize + " col-md-" + advancedSize).removeClass("col-xs-" + normalSize + " col-sm-" + normalSize + " col-md-" + normalSize);
			} else {
				$(obj).removeClass("col-xs-" + advancedSize + " col-sm-" + advancedSize + " col-md-" + advancedSize).addClass("col-xs-" + normalSize + " col-sm-" + normalSize + " col-md-" + normalSize);
			}
		});

		if (text == $.t("advanced").toLowerCase()) {
			$(this).text($.t("basic"));
		} else {
			$(this).text($.t("advanced"));
		}
		// Close accidentally triggered popovers
		$(".show_popover").popover("hide");
	});

	NRS.isShowFakeWarning = function() {
		if (NRS.settings.fake_entity_warning != "1") {
			return false;
		}
		return !isFakeWarningDisplayed;
    };

	NRS.composeFakeWarning = function (entity, id) {
		isFakeWarningDisplayed = true;
        return {
            "error": $.t("fake_warning", {
                entity: entity,
                id: id
            }) + " " + $.t("click_submit_again")
        };
    };

	return NRS;
}(NRS || {}, jQuery));