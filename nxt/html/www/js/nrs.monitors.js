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
    var currentMonitor;

    function isErrorResponse(response) {
        return response.errorCode || response.errorDescription || response.errorMessage || response.error;
    }

    function getErrorMessage(response) {
        return response.errorDescription || response.errorMessage || response.error;
    } 

    NRS.jsondata = NRS.jsondata||{};

    NRS.jsondata.monitors = function (response) {
        return {
            accountFormatted: NRS.getAccountLink(response, "account"),
            property: NRS.escapeRespStr(response.property),
            amountFormatted: NRS.formatAmount(response.amount),
            thresholdFormatted: NRS.formatAmount(response.threshold),
            interval: NRS.escapeRespStr(response.interval),
            statusLinkFormatted: "<a href='#' class='btn btn-xs' " +
                        "onclick='NRS.goToMonitor(" + JSON.stringify(response) + ");'>" +
                         $.t("status") + "</a>",
            stopLinkFormatted: "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#stop_funding_monitor_modal' " +
                        "data-account='" + NRS.escapeRespStr(response.accountRS) + "' " +
                        "data-property='" + NRS.escapeRespStr(response.property) + "'>" + $.t("stop") + "</a>"
        };
    };

    NRS.jsondata.monitoredAccount = function (response) {
        try {
            var value = JSON.parse(response.value);
        } catch (e) {
            NRS.logConsole(e.message);
        }
        return {
            accountFormatted: NRS.getAccountLink(response, "recipient"),
            property: NRS.escapeRespStr(response.property),
            amountFormatted: (value && value.amount) ? "<b>" + NRS.formatAmount(value.amount) : NRS.formatAmount(currentMonitor.amount),
            thresholdFormatted: (value && value.threshold) ? "<b>" + NRS.formatAmount(value.threshold) : NRS.formatAmount(currentMonitor.threshold),
            intervalFormatted: (value && value.interval) ? "<b>" + NRS.escapeRespStr(value.interval) : NRS.escapeRespStr(currentMonitor.interval),
            removeLinkFormatted: "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#remove_monitored_account_modal' " +
                        "data-recipient='" + NRS.escapeRespStr(response.recipientRS) + "' " +
                        "data-property='" + NRS.escapeRespStr(response.property) + "' " +
                        "data-value='" + NRS.normalizePropertyValue(response.value) + "'>" + $.t("remove") + "</a>"
        };
    };

    NRS.incoming.funding_monitors = function() {
        NRS.loadPage("funding_monitors");
    };

    NRS.pages.funding_monitors = function () {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('funding_monitors_page', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            monitors: []
        });
        var params = {
            "account": NRS.accountRS,
            "adminPassword": NRS.getAdminPassword(),
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        NRS.sendRequest("getFundingMonitor", params,
            function (response) {
                if (isErrorResponse(response)) {
                    view.render({
                        errorMessage: getErrorMessage(response),
                        isLoading: false,
                        isEmpty: false
                    });
                    return;
                }
                if (response.monitors.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.monitors.pop();
                }
                view.monitors.length = 0;
                response.monitors.forEach(
                    function (monitorJson) {
                        view.monitors.push(NRS.jsondata.monitors(monitorJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.monitors.length == 0
                });
                NRS.pageLoaded();
            }
        )
    };

    NRS.forms.startFundingMonitorComplete = function() {
        $.growl($.t("monitor_started"));
        NRS.loadPage("funding_monitors");
    };

    $("#stop_funding_monitor_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var account = $invoker.data("account");
        if (account) {
            $("#stop_monitor_account").val(account);
        }
        var property = $invoker.data("property");
        if (property) {
            $("#stop_monitor_property").val(property);
        }
        if (NRS.getAdminPassword()) {
            $("#stop_monitor_admin_password").val(NRS.getAdminPassword());
        }
    });

    NRS.forms.stopFundingMonitorComplete = function() {
        $.growl($.t("monitor_stopped"));
        NRS.loadPage("funding_monitors");
    };

    NRS.goToMonitor = function(monitor) {
   		NRS.goToPage("funding_monitor_status", function() {
            return monitor;
        });
   	};

    NRS.incoming.funding_monitors_status = function() {
        NRS.loadPage("funding_monitor_status");
    };

    NRS.pages.funding_monitor_status = function (callback) {
        currentMonitor = callback();
        $("#monitor_funding_account").html(NRS.escapeRespStr(currentMonitor.account));
        $("#monitor_control_property").html(NRS.escapeRespStr(currentMonitor.property));
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('funding_monitor_status_page', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            monitoredAccount: []
        });
        var params = {
            "setter": currentMonitor.account,
            "property": currentMonitor.property,
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        NRS.sendRequest("getAccountProperties", params,
            function (response) {
                if (isErrorResponse(response)) {
                    view.render({
                        errorMessage: getErrorMessage(response),
                        isLoading: false,
                        isEmpty: false
                    });
                    return;
                }
                if (response.properties.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.properties.pop();
                }
                view.monitoredAccount.length = 0;
                response.properties.forEach(
                    function (propertiesJson) {
                        view.monitoredAccount.push(NRS.jsondata.monitoredAccount(propertiesJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.monitoredAccount.length == 0,
                    fundingAccountFormatted: NRS.getAccountLink(currentMonitor, "account"),
                    controlProperty: currentMonitor.property
                });
                NRS.pageLoaded();
            }
        )
    };

    $("#add_monitored_account_modal").on("show.bs.modal", function() {
        $("#add_monitored_account_property").val(currentMonitor.property);
        $("#add_monitored_account_amount").val(NRS.convertToNXT(currentMonitor.amount));
        $("#add_monitored_account_threshold").val(NRS.convertToNXT(currentMonitor.threshold));
        $("#add_monitored_account_interval").val(currentMonitor.interval);
        $("#add_monitored_account_value").val("");
    });

    $(".add_monitored_account_value").on('change', function() {
        if (!currentMonitor) {
            return;
        }
        var value = {};
        var amount = NRS.convertToNQT($("#add_monitored_account_amount").val());
        if (currentMonitor.amount != amount) {
            value.amount = amount;
        }
        var threshold = NRS.convertToNQT($("#add_monitored_account_threshold").val());
        if (currentMonitor.threshold != threshold) {
            value.threshold = threshold;
        }
        var interval = $("#add_monitored_account_interval").val();
        if (currentMonitor.interval != interval) {
            value.interval = interval;
        }
        if (jQuery.isEmptyObject(value)) {
            value = "";
        } else {
            value = JSON.stringify(value);
        }
        $("#add_monitored_account_value").val(value);
    });

    $("#remove_monitored_account_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        $("#remove_monitored_account_recipient").val($invoker.data("recipient"));
        $("#remove_monitored_account_property").val($invoker.data("property"));
        $("#remove_monitored_account_value").val(NRS.normalizePropertyValue($invoker.data("value")));
    });

    return NRS;

}(NRS || {}, jQuery));