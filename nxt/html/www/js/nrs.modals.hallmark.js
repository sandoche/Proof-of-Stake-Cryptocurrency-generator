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
    var hallmarkModal = $("#hallmark_modal");
    hallmarkModal.on("show.bs.modal", function() {
		$("#generate_hallmark_output, #parse_hallmark_output").html("").hide();

		$("#hallmark_modal_generate_hallmark").show();
        if (NRS.upnpExternalAddress) {
            $("#generate_hallmark_host").val(NRS.upnpExternalAddress);
        }
		$("#hallmark_modal_button").text($.t("generate")).data("form", "generate_hallmark_form");
	});

	NRS.forms.markHost = function() {
        return {};
	};

	NRS.forms.markHostComplete = function(response) {
        var hallmarkModal = $("#hallmark_modal");
        hallmarkModal.find(".error_message").hide();

		if (response.hallmark) {
			$("#generate_hallmark_output").html($.t("generated_hallmark_is") + "<br /><br /><textarea readonly style='width:100%' rows='4'>" + NRS.escapeRespStr(response.hallmark) + "</textarea>").show();
		} else {
			$.growl($.t("error_generate_hallmark"), {
				"type": "danger"
			});
            hallmarkModal.modal("hide");
		}
	};

	NRS.forms.markHostError = function() {
		$("#generate_hallmark_output").hide();
	};

	NRS.forms.decodeHallmarkComplete = function(response) {
		$("#hallmark_modal").find(".error_message").hide();

		if (!response.errorCode) {
			$("#parse_hallmark_output").html($.t("success_hallmark_parsing")).
                addClass("callout-info").removeClass("callout-danger").show();
            $("#parse_hallmark_account").val(NRS.convertNumericToRSAccountFormat(response.account));
            $("#parse_hallmark_host").val(response.host);
            $("#parse_hallmark_port").val(response.port);
            $("#parse_hallmark_weight").val(response.weight);
            $("#parse_hallmark_date").val(response.date);
            $("#parse_hallmark_valid").val(response.valid);
		} else {
			$("#parse_hallmark_output").html($.t("error_invalid_hallmark", {
				"error": NRS.escapeRespStr(response.errorDescription)
            })).addClass("callout-danger").removeClass("callout-info").show();
            $("#parse_hallmark_host").val("");
            $("#parse_hallmark_port").val("");
            $("#parse_hallmark_weight").val("");
            $("#parse_hallmark_date").val("");
            $("#parse_hallmark_valid").val("");
		}
	};

	NRS.forms.decodeHallmarkError = function() {
		$("#parse_hallmark_output").hide();
        $("#parse_hallmark_host").val("");
        $("#parse_hallmark_port").val("");
        $("#parse_hallmark_weight").val("");
        $("#parse_hallmark_date").val("");
        $("#parse_hallmark_valid").val("");
	};

    hallmarkModal.find("ul.nav li").click(function(e) {
		e.preventDefault();

		var tab = $(this).data("tab");

		$(this).siblings().removeClass("active");
		$(this).addClass("active");

		$(".hallmark_modal_content").hide();

		var content = $("#hallmark_modal_" + tab);

		if (tab == "generate_hallmark") {
			$("#hallmark_modal_button").text($.t("generate")).data("form", "generate_hallmark_form");
		} else {
			$("#hallmark_modal_button").text($.t("parse")).data("form", "parse_hallmark_form");
		}

		$("#hallmark_modal").find(".error_message").hide();
		content.show();
	});

	hallmarkModal.on("hidden.bs.modal", function() {
		$(this).find(".hallmark_modal_content").hide();
		$(this).find("ul.nav li.active").removeClass("active");
		$("#generate_hallmark_nav").addClass("active");
	});

	return NRS;
}(NRS || {}, jQuery));