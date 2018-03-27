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
	NRS.forms.leaseBalanceComplete = function(response, data) {
		NRS.getAccountInfo();
	};

    function setLeaseBalanceHelp(period) {
        var days = Math.round(period / 1440);
        $("#lease_balance_help").html($.t("lease_balance_help_var", {
            "blocks": String(period).escapeHTML(),
            "days": String(Math.round(days)).escapeHTML()
        }));
    }

	$("#lease_balance_modal").on("show.bs.modal", function() {
        var leaseBalancePeriod = $("#lease_balance_period");
        leaseBalancePeriod.attr('min', 1440);
        leaseBalancePeriod.attr('max', NRS.constants.MAX_UNSIGNED_SHORT_JAVA);
		setLeaseBalanceHelp(NRS.constants.MAX_UNSIGNED_SHORT_JAVA);
	});

    $("#lease_balance_period").on("change", function() {
		if (this.value > NRS.constants.MAX_UNSIGNED_SHORT_JAVA) {
			$("#lease_balance_help").html($.t("error_lease_balance_period"));
		} else {
            setLeaseBalanceHelp(this.value);
        }
	});

	return NRS;
}(NRS || {}, jQuery));