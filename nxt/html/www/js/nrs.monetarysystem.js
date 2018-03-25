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

//noinspection JSUnusedLocalSymbols
/**
 * @depends {nrs.js}
 */
var NRS = (function (NRS, $, undefined) {

    NRS.isExchangeable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["EXCHANGEABLE"];
    };

    NRS.isControllable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["CONTROLLABLE"];
    };

    NRS.isReservable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["RESERVABLE"];
    };

    NRS.isClaimable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["CLAIMABLE"];
    };

    NRS.isMintable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["MINTABLE"];
    };

    NRS.isNonShuffleable = function (type) {
        return type & NRS.constants.CURRENCY_TYPES["NON_SHUFFLEABLE"];
    };

    /* MONETARY SYSTEM PAGE */
    /* Monetary System Page Search capitalization */
    var search = $("#currency_search");
    search.find("input[name=q]").blur(function () {
        if (this.value && this.value != this.value.toLocaleUpperCase()) {
            this.value = this.value.toLocaleUpperCase();
        }
    });

    search.find("input[name=q]").keyup(function () {
        if (this.value && this.value != this.value.toLocaleUpperCase()) {
            this.value = this.value.toLocaleUpperCase();
        }
    });

    search.on("submit", function (e, data) {
        e.preventDefault();
        //refresh is true if data is refreshed automatically by the system (when a new block arrives)
        var refresh = false;
        if (data && data.refresh) {
            refresh = true;
        }
        NRS.pageNumber = 1;
        var currencyCode = $.trim($("#currency_search").find("input[name=q]").val());
        $("#buy_currency_with_nxt").html($.t("buy_currency_param", { currency: currencyCode}));
        $("#buy_currency_offers").html($.t("offers_to_buy_currency_param", { currency: currencyCode}));
        $("#sell_currency_with_nxt").html($.t("sell_currency_param", { currency: currencyCode}));
        $("#sell_currency_offers").html($.t("offers_to_sell_currency_param", { currency: currencyCode}));
        $(".currency_code").html(NRS.escapeRespStr(currencyCode));

        var currencyId = 0;
        async.waterfall([
            function(callback) {
                NRS.sendRequest("getCurrency+", {
                    "code": currencyCode
                }, function (response) {
                    if (response && !response.errorDescription) {
                        $("#MSnoCode").hide();
                        $("#MScode").show();
                        $("#currency_account").html(NRS.getAccountLink(response, "account"));
                        currencyId = response.currency;
                        $("#currency_id").html(NRS.getTransactionLink(currencyId));
                        $("#currency_name").html(NRS.escapeRespStr(response.name));
                        $("#currency_code").html(NRS.escapeRespStr(response.code));
                        $("#currency_current_supply").html(NRS.convertToQNTf(response.currentSupply, response.decimals).escapeHTML());
                        $("#currency_max_supply").html(NRS.convertToQNTf(response.maxSupply, response.decimals).escapeHTML());
                        $("#currency_decimals").html(NRS.escapeRespStr(response.decimals));
                        $("#currency_description").html(String(response.description).autoLink());
                        var buyCurrencyButton = $("#buy_currency_button");
                        buyCurrencyButton.data("currency", currencyId);
                        buyCurrencyButton.data("decimals", response.decimals);
                        var sellCurrencyButton = $("#sell_currency_button");
                        sellCurrencyButton.data("currency", currencyId);
                        sellCurrencyButton.data("decimals", response.decimals);
                        if (!refresh) {
                            var msLinksCallout = $("#ms_links_callout");
                            msLinksCallout.html("");
                            msLinksCallout.append("<a href='#' data-toggle='modal' data-target='#transfer_currency_modal' data-currency='" + NRS.escapeRespStr(response.currency) + "' data-code='" + response.code + "' data-decimals='" + response.decimals + "'>" + $.t("transfer") + "</a>");
                            msLinksCallout.append(" | ");
                            msLinksCallout.append("<a href='#' data-toggle='modal' data-target='#publish_exchange_offer_modal' data-currency='" + NRS.escapeRespStr(response.currency) + "' data-code='" + response.code + "' data-decimals='" + response.decimals + "'>" + $.t("offer") + "</a>");
                        }
                    } else {
                        $("#MSnoCode").show();
                        $("#MScode").hide();
                        $.growl(NRS.escapeRespStr(response.errorDescription), {
                            "type": "danger"
                        });
                    }
                    callback(null);
                });
            },
            function(callback) {
                NRS.sendRequest("getAccountCurrencies+", {
                    "account": NRS.accountRS,
                    "currency": currencyId,
                    "includeCurrencyInfo": true
                }, function (response) {
                    if (response.unconfirmedUnits) {
                        $("#your_currency_balance").html(NRS.formatQuantity(response.unconfirmedUnits, response.decimals));
                    } else {
                        $("#your_currency_balance").html(0);
                    }
                    callback(null);
                });
            },
            function(callback) {
                NRS.loadCurrencyOffers("buy", currencyId, refresh);
                NRS.loadCurrencyOffers("sell", currencyId, refresh);
                NRS.getExchangeRequests(currencyId, refresh);
                NRS.getExchangeHistory(currencyId, refresh);
                if (NRS.accountInfo.unconfirmedBalanceNQT == "0") {
                    $("#ms_your_nxt_balance").html("0");
                } else {
                    $("#ms_your_nxt_balance").html(NRS.formatAmount(NRS.accountInfo.unconfirmedBalanceNQT));
                }
                NRS.pageLoaded();
                callback(null);
            }
        ], function (err, result) {});
    });

    /* Search on Currencies Page */
    $("#currencies_search").on("submit", function (e) {
        e.preventDefault();
        NRS.pageNumber = 1;
        var params = {
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        var requestType;
        var query = $.trim($("#currencies_search").find("input[name=searchquery]").val());
        if (query == "") {
            if (NRS.currenciesPageType == "my_currencies") {
                requestType = "getAccountCurrencies+";
                params["account"] = NRS.accountRS;
                params["includeCurrencyInfo"] = true;
            } else {
                requestType = "getAllCurrencies+";
            }
        } else {
            requestType = "searchCurrencies+";
            params["query"] = query;
        }
        NRS.sendRequest(requestType, params, function (response) {
            NRS.hasMorePages = false;
            if (response.currencies && response.currencies.length || response.accountCurrencies && response.accountCurrencies.length) {
                if (response.currencies && response.currencies.length) {
                    if (response.currencies.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.currencies.pop();
                    }
                } else {
                    if (response.accountCurrencies.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.accountCurrencies.pop();
                    }
                }
                var rows;
                if (NRS.currenciesPageType == "my_currencies") {
                    if (requestType == "searchCurrencies+") {
                        // The response is in currency format but we need accountCurrency format
                        NRS.sendRequest("getAccountCurrencies+", {
                            account: NRS.accountRS,
                            currency: response.currencies[0].currency,
                            includeCurrencyInfo: true
                        }, function (response) {
                            // Now we have a single currency but we need an array of accountCurrencies
                            if (response.currency) {
                                response["accountCurrencies"] = [];
                                response["accountCurrencies"].push(response);
                                rows = getAccountCurrenciesRows(response);
                            }
                        }, { isAsync: false });
                    } else {
                        rows = getAccountCurrenciesRows(response);
                    }
                } else {
                    rows = getAllCurrenciesRows(response);
                }
                NRS.dataLoaded(rows);
            } else {
                NRS.dataLoaded();
            }
        }, { isAsync: false });
    });

    function getAllCurrenciesRows(response) {
        var rows = "";
        var currentSupplyDecimals = NRS.getNumberOfDecimals(response.currencies, "currentSupply", function(currency) {
            return NRS.formatQuantity(currency.currentSupply, currency.decimals);
        });
        var maxSupplyDecimals = NRS.getNumberOfDecimals(response.currencies, "maxSupply", function(currency) {
            return NRS.formatQuantity(currency.maxSupply, currency.decimals);
        });
        for (var i = 0; i < response.currencies.length; i++) {
            var currency = response.currencies[i];
            var name = NRS.escapeRespStr(currency.name);
            var currencyId = NRS.escapeRespStr(currency.currency);
            var code = NRS.escapeRespStr(currency.code);
            var resSupply = NRS.convertToQNTf(currency.reserveSupply, currency.decimals);
            var decimals = NRS.escapeRespStr(currency.decimals);
            var minReserve = NRS.escapeRespStr(currency.minReservePerUnitNQT);
            var typeIcons = NRS.getTypeIcons(currency.type);
            rows += "<tr>" +
            "<td>" + NRS.getTransactionLink(currencyId, code) + "</td>" +
            "<td>" + name + "</td>" +
            "<td>" + typeIcons + "</td>" +
            "<td class = 'numeric'>" + NRS.formatQuantity(currency.currentSupply, currency.decimals, false, currentSupplyDecimals) + "</td>" +
            "<td class = 'numeric'>" + NRS.formatQuantity(currency.maxSupply, currency.decimals, false, maxSupplyDecimals) + "</td>" +
            "<td>";
            //noinspection BadExpressionStatementJS
            rows += "<a href='#' class='btn btn-xs btn-default' onClick='NRS.goToCurrency(&quot;" + code + "&quot;)' " + (!NRS.isExchangeable(currency.type) ? "disabled" : "") + ">" + $.t("exchange") + "</a> ";
            rows += "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#reserve_currency_modal' data-currency='" + currencyId + "' data-name='" + name + "' data-code='" + code + "' data-ressupply='" + resSupply + "' data-decimals='" + decimals + "' data-minreserve='" + minReserve + "' " + (currency.issuanceHeight > NRS.lastBlockHeight && NRS.isReservable(currency.type) ? "" : "disabled") + " >" + $.t("reserve") + "</a> ";
            rows += "</td></tr>";
        }
        var currenciesTable = $('#currencies_table');
        currenciesTable.find('[data-i18n="type"]').show();
        currenciesTable.find('[data-i18n="supply"]').show();
        currenciesTable.find('[data-i18n="max_supply"]').show();
        currenciesTable.find('[data-i18n="units"]').hide();
        return rows;
    }

    function getAccountCurrenciesRows(response) {
        var rows = "";
        var unitsDecimals = NRS.getNumberOfDecimals(response.accountCurrencies, "unconfirmedUnits", function (accountCurrency) {
            return NRS.formatQuantity(accountCurrency.unconfirmedUnits, accountCurrency.decimals);
        });
        for (var i = 0; i < response.accountCurrencies.length; i++) {
            var currency = response.accountCurrencies[i];
            var currencyId = NRS.escapeRespStr(currency.currency);
            var code = NRS.escapeRespStr(currency.code);
            var name = NRS.escapeRespStr(currency.name);
            var decimals = NRS.escapeRespStr(currency.decimals);
            var typeIcons = NRS.getTypeIcons(currency.type);
            var isOfferEnabled = NRS.isExchangeable(currency.type) && (!NRS.isControllable(currency.type) || NRS.account == currency.issuerAccount);
            //noinspection HtmlUnknownAttribute,BadExpressionStatementJS
            rows += "<tr>" +
                "<td>" + NRS.getTransactionLink(currencyId, code) + "</td>" +
                "<td>" + currency.name + "</td>" +
                "<td>" + typeIcons + "</td>" +
                "<td class = 'numeric'>" + NRS.formatQuantity(currency.unconfirmedUnits, currency.decimals, false, unitsDecimals) + "</td>" +
                "<td>" +
                "<a href='#' class='btn btn-xs btn-default' onClick='NRS.goToCurrency(&quot;" + code + "&quot;)' " + (!NRS.isExchangeable(currency.type) ? "disabled" : "") + ">" + $.t("exchange") + "</a> " +
                "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#transfer_currency_modal' data-currency='" + NRS.escapeRespStr(currency.currency) + "' data-code='" + code + "' data-decimals='" + decimals + "'>" + $.t("transfer") + "</a> " +
                "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#publish_exchange_offer_modal' data-currency='" + NRS.escapeRespStr(currency.currency) + "' data-code='" + code + "' data-decimals='" + decimals + "' " + (isOfferEnabled ? "" : "disabled") + " >" + $.t("offer") + "</a> " +
                "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#claim_currency_modal' data-currency='" + currencyId + "' data-name='" + name + "' data-code='" + code + "' data-decimals='" + decimals + "' " + (currency.issuanceHeight <= NRS.lastBlockHeight && NRS.isClaimable(currency.type) ? "" : "disabled") + " >" + $.t("claim") + "</a> " +
                "</td>" +
                "</tr>";
        }
        var currenciesTable = $('#currencies_table');
        currenciesTable.find('[data-i18n="type"]').show();
        currenciesTable.find('[data-i18n="supply"]').hide();
        currenciesTable.find('[data-i18n="max_supply"]').hide();
        currenciesTable.find('[data-i18n="units"]').show();
        return rows;
    }

    NRS.getTypeIcons = function (type) {
        var typeIcons = "";
        if (NRS.isExchangeable(type)) {
            typeIcons += "<i title='" + $.t('exchangeable') + "' class='fa fa-exchange'></i> ";
        }
        if (NRS.isControllable(type)) {
            typeIcons += "<i title='" + $.t('controllable') + "' class='fa fa-sliders'></i> ";
        }
        if (NRS.isReservable(type)) {
            typeIcons += "<i title='" + $.t('reservable') + "' class='fa fa-university'></i> ";
        }
        if (NRS.isClaimable(type)) {
            typeIcons += "<i title='" + $.t('claimable') + "' class='ion-android-archive'></i> ";
        }
        if (NRS.isMintable(type)) {
            typeIcons += "<i title='" + $.t('mintable') + "' class='fa fa-money'></i> ";
        }
        return typeIcons;
    };

    NRS.currenciesTableLayout = function () {
        var currenciesTable = $('#currencies_table');
        currenciesTable.find('[data-i18n="type"]').show();
        currenciesTable.find('[data-i18n="supply"]').show();
        currenciesTable.find('[data-i18n="max_supply"]').show();
        currenciesTable.find('[data-i18n="units"]').hide();
    };

    function processOffers(offers, type, refresh) {
        if (offers && offers.length > NRS.itemsPerPage) {
            NRS.hasMorePages = true;
            offers.pop();
        }
        var offersTable = $("#ms_open_" + type + "_orders_table");
        var entity = "currency";
        var rate = $("#" + (type == "sell" ? "buy" : "sell") + "_" + entity + "_rate");
        var supply = $("#" + type + "_" + entity + "_supply");
        var decimals = parseInt($("#currency_decimals").text(), 10);
        if (offers && offers.length) {
            var rows = "";
            var supplyDecimals = NRS.getNumberOfDecimals(offers, "supply", function(offer) {
                return NRS.convertToQNTf(offer.supply, decimals);
            });
            var limitDecimals = NRS.getNumberOfDecimals(offers, "limit", function(offer) {
                return NRS.convertToQNTf(offer.limit, decimals);
            });
            var rateNQTDecimals = NRS.getNumberOfDecimals(offers, "rateNQT", function(offer) {
                return NRS.formatOrderPricePerWholeQNT(offer.rateNQT, decimals);
            });
            for (i = 0; i < offers.length; i++) {
                var offer = offers[i];
                var rateNQT = offer.rateNQT;
                if (i == 0 && !refresh) {
                    rate.val(NRS.calculateOrderPricePerWholeQNT(rateNQT, decimals));
                    supply.text(NRS.formatQuantity(offer.supply, decimals));
                }
                rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(offer.offer, NRS.getTransactionStatusIcon(offer), true) + "</td>" +
                    "<td>" + NRS.getAccountLink(offer, "account") + "</td>" +
                    "<td class='numeric'>" + NRS.formatQuantity(offer.supply, decimals, false, supplyDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatQuantity(offer.limit, decimals, false, limitDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatOrderPricePerWholeQNT(rateNQT, decimals, rateNQTDecimals) + "</td>" +
                    "</tr>";
            }
            offersTable.find("tbody").empty().append(rows);
        } else {
            rate.val("0");
            supply.text("0");
            offersTable.find("tbody").empty();
        }
        NRS.dataLoadFinished(offersTable, !refresh);
    }

    NRS.loadCurrencyOffers = function (type, currencyId, refresh) {
        async.parallel([
            function(callback) {
                NRS.sendRequest("get" + type.capitalize() + "Offers+", {
                    "currency": currencyId, "availableOnly": "true",
                    "firstIndex": 0,
                    "lastIndex": NRS.itemsPerPage
                }, function (response) {
                    var offers = response["offers"];
                    if (!offers) {
                        offers = [];
                    }
                    callback(null, offers);
                })
            },
            function(callback) {
                NRS.sendRequest("getExpected" + type.capitalize() + "Offers", {
                    "currency": currencyId
                }, function (response) {
                    var offers = response["offers"];
                    if (!offers) {
                        offers = [];
                    }
                    callback(null, offers);
                })
            }
        ],
        // invoked when both requests above has completed
        // the results array contains both offer lists
        function(err, results) {
            if (err) {
                NRS.logConsole(err);
                return;
            }
            var offers = results[0].concat(results[1]);
            offers.sort(function (a, b) {
                if (type == "sell") {
                    return a.rateNQT - b.rateNQT;
                } else {
                    return b.rateNQT - a.rateNQT;
                }
            });
            processOffers(offers, type, refresh);
        });
    };

    NRS.incoming.monetary_system = function () {
        search.trigger("submit", [{"refresh": true}]);
    };

    /* CURRENCY FOUNDERS MODEL */
    var foundersModal = $("#currency_founders_modal");
    foundersModal.on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);

        var currencyId = $invoker.data("currency");
        var issueHeight = $invoker.data("issueheight");
        if (issueHeight > NRS.lastBlockHeight) {
            $("#founders_blocks_active").html(issueHeight - NRS.lastBlockHeight);
        } else {
            $("#founders_blocks_active").html("Active");
        }
        $("#founders_currency_code").html(String($invoker.data("code")).escapeHTML());

        NRS.sendRequest("getCurrencyFounders", {
            "currency": currencyId
        }, function (response) {
            var rows = "";
            var decimals = $invoker.data("decimals"); // has to be numeric not string
            var minReservePerUnitNQT = new BigInteger(String($invoker.data("minreserve"))).multiply(new BigInteger("" + Math.pow(10, decimals)));
            var initialSupply = new BigInteger(String($invoker.data("initialsupply")));
            var resSupply = new BigInteger(String($invoker.data("ressupply")));
            var totalAmountReserved = BigInteger.ZERO;
            $("#founders_reserve_units").html(NRS.formatQuantity(resSupply, decimals));
            $("#founders_issuer_units").html(NRS.formatQuantity(initialSupply, decimals));
            if (response.founders && response.founders.length) {
                var amountPerUnitNQT = BigInteger.ZERO;
                for (var i = 0; i < response.founders.length; i++) {
                    amountPerUnitNQT = new BigInteger(response.founders[i].amountPerUnitNQT).multiply(new BigInteger("" + Math.pow(10, decimals)));
                    totalAmountReserved = totalAmountReserved.add(amountPerUnitNQT);
                }
                for (i = 0; i < response.founders.length; i++) {
                    var account = response.founders[i].accountRS;
                    amountPerUnitNQT = new BigInteger(response.founders[i].amountPerUnitNQT).multiply(new BigInteger("" + Math.pow(10, decimals)));
                    var percentage = NRS.calculatePercentage(amountPerUnitNQT, minReservePerUnitNQT);
                    rows += "<tr>" +
                    "<td>" + NRS.getAccountLink(response.founders[i], "account")+ "</td>" +
                    "<td>" + NRS.convertToNXT(amountPerUnitNQT) + "</td>" +
                    "<td>" + NRS.convertToNXT(amountPerUnitNQT.multiply(new BigInteger(NRS.convertToQNTf(resSupply, decimals)))) + "</td>" +
                    "<td>" + NRS.formatQuantity(resSupply.subtract(initialSupply).multiply(amountPerUnitNQT).divide(totalAmountReserved), decimals) + "</td>" +
                    "<td>" + percentage + "</td>" +
                    "</tr>";
                }
            } else {
                rows = "<tr><td colspan='5'>None</td></tr>";
            }
            rows += "<tr>" +
            "<td><b>Totals</b></td>" +
            "<td>" + NRS.convertToNXT(totalAmountReserved) + "</td>" +
            "<td>" + NRS.convertToNXT(totalAmountReserved.multiply(new BigInteger(NRS.convertToQNTf(resSupply, decimals)))) + "</td>" +
            "<td>" + NRS.formatQuantity(resSupply.subtract(initialSupply), decimals) + "</td>" +
            "<td>" + NRS.calculatePercentage(totalAmountReserved, minReservePerUnitNQT) + "</td>" +
            "</tr>";
            var foundersTable = $("#currency_founders_table");
            foundersTable.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(foundersTable);
        });
    });

    foundersModal.on("hidden.bs.modal", function () {
        var foundersTable = $("#currency_founders_table");
        foundersTable.find("tbody").empty();
        foundersTable.parent().addClass("data-loading");
    });

    NRS.getExchangeHistory = function (currencyId, refresh, tableName) {
        var historyTable;
        if (tableName) {
            historyTable = $("#" + tableName);
        } else {
            historyTable = $("#ms_exchanges_history_table");
        }
        if (NRS.currenciesTradeHistoryType == "my_exchanges" && !tableName) {
            NRS.sendRequest("getExchanges+", {
                "currency": currencyId,
                "account": NRS.accountRS,
                "includeCurrencyInfo": true,
                "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                "lastIndex": NRS.pageNumber * NRS.itemsPerPage
            }, function (response) {
                if (response.exchanges && response.exchanges.length) {
                    if (response.exchanges.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.exchanges.pop();
                    }
                    var rows = "";
                    var decimals = parseInt($("#currency_decimals").text(), 10);
                    var quantityDecimals = NRS.getNumberOfDecimals(response.exchanges, "units", function(exchange) {
                        return NRS.convertToQNTf(exchange.units, decimals);
                    });
                    var rateNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "rateNQT", function(exchange) {
                        return NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, decimals);
                    });
                    var totalNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "totalNQT", function(exchange) {
                        return NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT));
                    });
                    for (var i = 0; i < response.exchanges.length; i++) {
                        var exchange = response.exchanges[i];
                        rows += "<tr>" +
                            "<td>" + NRS.getTransactionLink(exchange.transaction, NRS.formatTimestamp(exchange.timestamp)) + "</td>" +
                            "<td>" + NRS.getAccountLink(exchange, "seller") + "</td>" +
                            "<td>" + NRS.getAccountLink(exchange, "buyer") + "</td>" +
                            "<td class='numeric'>" + NRS.formatQuantity(exchange.units, exchange.decimals, false, quantityDecimals) + "</td>" +
                            "<td class='numeric'>" + NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals, rateNQTDecimals) + "</td>" +
                            "<td class='numeric'>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT), false, false, totalNQTDecimals) + "</td>" +
                        "</tr>";
                    }
                    historyTable.find("tbody").empty().append(rows);
                } else {
                    historyTable.find("tbody").empty();
                }
                NRS.dataLoadFinished(historyTable, !refresh);
            });
        } else {
            NRS.sendRequest("getExchanges+", {
                "currency": currencyId,
                "includeCurrencyInfo": true,
                "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                "lastIndex": NRS.pageNumber * NRS.itemsPerPage
            }, function (response) {
                if (response.exchanges && response.exchanges.length) {
                    if (response.exchanges.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.exchanges.pop();
                    }
                    var rows = "";
                    var decimals = parseInt($("#currency_decimals").text(), 10);
                    var quantityDecimals = NRS.getNumberOfDecimals(response.exchanges, "units", function(exchange) {
                        return NRS.formatQuantity(exchange.units, decimals);
                    });
                    var rateNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "rateNQT", function(exchange) {
                        return NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, decimals);
                    });
                    var totalNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "totalNQT", function(exchange) {
                        return NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT));
                    });
                    for (var i = 0; i < response.exchanges.length; i++) {
                        var exchange = response.exchanges[i];
                        rows += "<tr>" +
                            "<td>" + NRS.getTransactionLink(exchange.transaction, NRS.formatTimestamp(exchange.timestamp)) + "</td>";
                            if (!tableName) {
                                rows += "<td>" + NRS.getAccountLink(exchange, "seller") + "</td>";
                            }
                            rows += "<td>" + NRS.getAccountLink(exchange, "buyer") + "</td>" +
                            "<td class='numeric'>" + NRS.formatQuantity(exchange.units, exchange.decimals, false, quantityDecimals) + "</td>" +
                            "<td class='numeric'>" + NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals, rateNQTDecimals) + "</td>" +
                            "<td class='numeric'>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT), false, false, totalNQTDecimals) + "</td>" +
                        "</tr>";
                    }
                    if (tableName) {
                        NRS.dataLoaded(rows);
                    } else {
                        historyTable.find("tbody").empty().append(rows);
                    }
                } else {
                    if (tableName) {
                        NRS.dataLoaded();
                    } else {
                        historyTable.find("tbody").empty();
                    }
                }
                NRS.dataLoadFinished(historyTable, !refresh);
            });
        }
    };

    function processExchangeRequests(exchangeRequests, refresh) {
        var requestTable = $("#ms_exchange_requests_table");
        if (exchangeRequests && exchangeRequests.length) {
            if (exchangeRequests.length > NRS.itemsPerPage) {
                NRS.hasMorePages = true;
                exchangeRequests.pop();
            }
            var rows = "";
            var decimals = parseInt($("#currency_decimals").text(), 10);
            var quantityDecimals = NRS.getNumberOfDecimals(exchangeRequests, "units", function(exchangeRequest) {
                return NRS.formatQuantity(exchangeRequest.units, decimals);
            });
            var rateNQTDecimals = NRS.getNumberOfDecimals(exchangeRequests, "rateNQT", function(exchangeRequest) {
                return NRS.formatOrderPricePerWholeQNT(exchangeRequest.rateNQT, decimals);
            });
            var totalNQTDecimals = NRS.getNumberOfDecimals(exchangeRequests, "totalNQT", function(exchangeRequest) {
                return NRS.formatAmount(NRS.calculateOrderTotalNQT(exchangeRequest.units, exchangeRequest.rateNQT));
            });
            for (i = 0; i < exchangeRequests.length; i++) {
                var exchangeRequest = exchangeRequests[i];
                var type = exchangeRequest.subtype == 5 ? "buy" : "sell";
                rows += "<tr class=confirmed>" +
                    "<td>" + NRS.getTransactionLink(exchangeRequest.transaction, NRS.getTransactionStatusIcon(exchangeRequest), true) + "</td>" +
                    "<td>" + NRS.getBlockLink(exchangeRequest.height) + "</td>" +
                    "<td>" + type + "</td>" +
                    "<td class='numeric'>" + NRS.formatQuantity(exchangeRequest.units, decimals, false, quantityDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatOrderPricePerWholeQNT(exchangeRequest.rateNQT, decimals, rateNQTDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchangeRequest.units, exchangeRequest.rateNQT), false, false, totalNQTDecimals) + "</td>" +
                    "</tr>";
            }
            requestTable.find("tbody").empty().append(rows);
        } else {
            requestTable.find("tbody").empty();
        }
        NRS.dataLoadFinished(requestTable, !refresh);
    }

    NRS.getExchangeRequests = function (currencyId, refresh) {
        async.parallel([
            function(callback) {
                NRS.sendRequest("getAccountExchangeRequests+", {
                    "currency": currencyId,
                    "account": NRS.accountRS,
                    "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                    "lastIndex": NRS.pageNumber * NRS.itemsPerPage
                }, function (response) {
                    var requests = response["exchangeRequests"];
                    if (!requests) {
                        requests = [];
                    }
                    callback(null, requests);
                });
            },
            function(callback) {
                NRS.sendRequest("getExpectedExchangeRequests", {
                    "currency": currencyId,
                    "account": NRS.accountRS
                }, function (response) {
                    var requests = response["exchangeRequests"];
                    if (!requests) {
                        requests = [];
                    }
                    callback(null, requests);
                });
            }
        ],
        // invoked when both the requests above has completed
        // the results array contains both requests lists
        function(err, results) {
            if (err) {
                NRS.logConsole(err);
                return;
            }
            var exchangeRequests = results[0].concat(results[1]);
            exchangeRequests.sort(function (a, b) {
                return b.height - a.height;
            });
            processExchangeRequests(exchangeRequests, refresh);
        });
    };

    /* Monetary System Buy/Sell boxes */
    $("#buy_currency_box .box-header, #sell_currency_box .box-header").click(function (e) {
        e.preventDefault();
        //Find the box parent
        var box = $(this).parents(".box").first();
        //Find the body and the footer
        var bf = box.find(".box-body, .box-footer");
        if (!box.hasClass("collapsed-box")) {
            box.addClass("collapsed-box");
            $(this).find(".btn i.fa").removeClass("fa-minus").addClass("fa-plus");
            bf.slideUp();
        } else {
            box.removeClass("collapsed-box");
            bf.slideDown();
            $(this).find(".btn i.fa").removeClass("fa-plus").addClass("fa-minus");
        }
    });

    /* Currency Order Model */
    $("#currency_order_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);
        var exchangeType = $invoker.data("type");
        var currencyId = $invoker.data("currency");
        var currencyCode = $invoker.data("code") || $("#currency_code").html();
        var currencyDecimals = parseInt($invoker.data("decimals"), 10);
        var unitsQNT = $invoker.data("units");
        var units = String($("#" + exchangeType + "_currency_units").val());
        var rateNQT = $invoker.data("rateNQT");
        var effectiveRate = $invoker.data("effectiveRate");
        var isScheduled = $invoker.data("scheduled");
        var totalNQT = $invoker.data("totalNQT");
        var totalNXT = NRS.formatAmount(totalNQT, false, true);
        var submitButton = $("#currency_order_modal_button");
        submitButton.html($.t(exchangeType + "_currency")).data("resetText", $.t(exchangeType + "_currency"));
        submitButton.prop('disabled', false);

        if (rateNQT == "0" || unitsQNT == "0") {
            $.growl($.t("error_unit_rate_required"), {
                "type": "danger"
            });
            return e.preventDefault();
        }

        var description;
        var tooltipTitle;

        if (exchangeType == "buy") {
            description = $.t("buy_currency_description", {
                "total": totalNXT,
                "quantity": NRS.formatQuantity(unitsQNT, currencyDecimals, true),
                "currency_code": currencyCode.escapeHTML(),
                "rate": effectiveRate
            });
            tooltipTitle = $.t("buy_currency_description_help", {
                "rate": effectiveRate,
                "total_nxt": totalNXT
            });
        } else {
            description = $.t("sell_currency_description", {
                "total": totalNXT,
                "quantity": NRS.formatQuantity(unitsQNT, currencyDecimals, true),
                "currency_code": currencyCode.escapeHTML(),
                "rate": effectiveRate
            });
            tooltipTitle = $.t("sell_currency_description_help", {
                "rate": effectiveRate,
                "total_nxt": totalNXT
            });
        }

        $("#currency_order_description").html(description);
        $("#currency_order_total").html(totalNXT + " NXT");

        var totalTooltip = $("#currency_order_total_tooltip");
        if (units != "1") {
            totalTooltip.show();
            totalTooltip.popover("destroy");
            totalTooltip.data("content", tooltipTitle);
            totalTooltip.popover({
                "content": tooltipTitle,
                "trigger": "hover"
            });
        } else {
            totalTooltip.hide();
        }

        if (isScheduled && exchangeType == "buy") {
            $("#currency_order_type").val("scheduleCurrencyBuy");
        } else {
            $("#currency_order_type").val((exchangeType == "buy" ? "currencyBuy" : "currencySell"));
        }
        $("#currency_order_currency").val(currencyId);
        $("#currency_order_currency_code").val(currencyCode.escapeHTML());
        $("#currency_order_units").val(unitsQNT);
        $("#currency_order_rate").val(rateNQT);
    });

    NRS.forms.orderCurrency = function () {
        var orderType = $("#currency_order_type").val();
        var currencyCode = $("#currency_order_currency_code").val();

        if (orderType == "currencyBuy" && currencyCode != "JLRDA" && NRS.isShowFakeWarning()) {
            return NRS.composeFakeWarning($.t("currency"), currencyCode);
        }

        if (orderType == "scheduleCurrencyBuy") {
            $("#currency_order_no_broadcast").val("true");
            $("#currency_order_currency_issuer").val($("#buy_ignis_currency_issuer").val());
        } else {
            $("#currency_order_no_broadcast").val("");
        }
        return {
            "requestType": orderType,
            "successMessage": (orderType == "currencySell" ? $.t("success_sell_order_currency") : $.t("success_buy_order_currency")),
            "errorMessage": $.t("error_order_currency")
        };
    };

    $("#buy_currency_units_initial, #buy_currency_units_total, #buy_currency_offer_rate, #sell_currency_units_initial, #sell_currency_units_total, #sell_currency_offer_rate").keydown(function (e) {
        var decimals = parseInt($("#publish_exchange_offer_decimals").val(), 10);

        var charCode = !e.charCode ? e.which : e.charCode;
        if (NRS.isControlKey(charCode) || e.ctrlKey || e.metaKey) {
            return;
        }
        var isUnitsField = /_units/i.test($(this).attr("id"));
        var maxFractionLength = (isUnitsField ? decimals : 8 - decimals);
        return NRS.validateDecimals(maxFractionLength, charCode, $(this).val(), e);
    });

    var unitFields = $(".currency_units");
    unitFields.keydown(function (e) {
        var decimals = parseInt($("#currency_decimals").html(), 10);

        var charCode = !e.charCode ? e.which : e.charCode;
        if (NRS.isControlKey(charCode) || e.ctrlKey || e.metaKey) {
            return;
        }
        var isUnitsField = /_units/i.test($(this).attr("id"));
        var maxFractionLength = (isUnitsField ? decimals : 8 - decimals);
        return NRS.validateDecimals(maxFractionLength, charCode, $(this).val(), e);
    });

    unitFields.on("change", function() {
        var decimals = parseInt($("#currency_decimals").text(), 10);
        var type = $(this).data("type").toLowerCase();
        var orderType = type;
        var entity = "currency";
        var units_field = "#" + orderType + "_" + entity + "_units";
        var units = NRS.convertToQNT(String($(units_field).val()), decimals);
        NRS.sendRequest("getAvailableTo" + NRS.initialCaps(orderType), {
            "currency": $("#currency_id").text(),
            "units": units
        }, function (response) {
            var submitButton = $("#" + orderType + "_" + entity + "_button");
            var unitsField = $("#" + orderType + "_" + entity + "_units");
            var rateField = $("#" + orderType + "_" + entity + "_rate");
            var totalField = $("#" + orderType + "_" + entity + "_total");
            var effectiveRateField = $("#" + orderType + "_" + entity + "_effective_rate");
            if (response.errorCode) {
                unitsField.val("0");
                rateField.val("0");
                totalField.val("0");
                effectiveRateField.val("0");
                submitButton.prop('disabled', true);
                return;
            }
            var rate = NRS.calculateOrderPricePerWholeQNT(response.rateNQT, decimals);
            rateField.val(rate);
            var units = NRS.convertToQNTf(response.units, decimals);
            unitsField.val(units);
            var amount = NRS.convertToNXT(response.amountNQT);
            totalField.val(amount);
            NRS.sendRequest("getBalance", {
                "account": NRS.accountRS
            }, function (balance) {
                if (parseInt(response.amountNQT) > parseInt(balance.unconfirmedBalanceNQT - 100000000)) {
                    totalField.css("background-color", "red");
                    submitButton.prop('disabled', true);
                } else {
                    totalField.css("background-color", "");
                    submitButton.prop('disabled', false);
                }
            });
            var effectiveRate = units == "0" ? "0" : NRS.amountToPrecision(amount / units, 8 - decimals);
            effectiveRateField.val(effectiveRate);
            submitButton.data("units", response.units);
            submitButton.data("rateNQT", response.rateNQT);
            submitButton.data("effectiveRate", effectiveRate);
            submitButton.data("totalNQT", response.amountNQT);
            submitButton.prop('disabled', false);
        })
   	});

    NRS.pages.currencies = function () {
        if (NRS.currenciesPageType == "my_currencies") {
            NRS.sendRequest("getAccountCurrencies+", {
                "account": NRS.accountRS,
                "includeCurrencyInfo": true,
                "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                "lastIndex": NRS.pageNumber * NRS.itemsPerPage
            }, function (response) {
                if (response.accountCurrencies && response.accountCurrencies.length) {
                    if (response.accountCurrencies.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.accountCurrencies.pop();
                    }
                    var rows = getAccountCurrenciesRows(response);
                    NRS.dataLoaded(rows);
                } else {
                    NRS.dataLoaded();
                }
            });
        } else {
            NRS.sendRequest("getAllCurrencies+", {
                "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                "lastIndex": NRS.pageNumber * NRS.itemsPerPage
            }, function (response) {
                if (response.currencies && response.currencies.length) {
                    if (response.currencies.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.currencies.pop();
                    }
                    var rows = getAllCurrenciesRows(response);
                    NRS.dataLoaded(rows);
                } else {
                    NRS.dataLoaded();
                }
            });
        }
    };

    $("#currencies_page_type").find(".btn").click(function (e) {
        e.preventDefault();
        NRS.currenciesPageType = $(this).data("type");

        var currenciesTable = $("#currencies_table");
        currenciesTable.find("tbody").empty();
        currenciesTable.parent().addClass("data-loading").removeClass("data-empty");
        NRS.loadPage("currencies");
    });

    $("#ms_exchange_history_type").find(".btn").click(function (e) {
        e.preventDefault();
        NRS.currenciesTradeHistoryType = $(this).data("type");

        var exchangeHistoryTable = $("#ms_exchanges_history_table");
        exchangeHistoryTable.find("tbody").empty();
        exchangeHistoryTable.parent().addClass("data-loading").removeClass("data-empty");
        NRS.getExchangeHistory($("#currency_id").text(), false);
    });

    $("body").on("click", "a[data-goto-currency]", function (e) {
        e.preventDefault();

        var $visible_modal = $(".modal.in");

        if ($visible_modal.length) {
            $visible_modal.modal("hide");
        }

        NRS.goToCurrency($(this).data("goto-currency"));
    });

    NRS.goToCurrency = function (currency) {
        var currencySearch = $("#currency_search");
        currencySearch.find("input[name=q]").val(currency);
        currencySearch.submit();
        NRS.goToPage("monetary_system");
    };

    /* Transfer Currency Model */
    var currencyCodeField = $("#transfer_currency_code");
    $("#transfer_currency_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);

        var currency = $invoker.data("currency");
        var currencyCode = $invoker.data("code");
        var decimals = $invoker.data("decimals");

        $("#transfer_currency_currency").val(currency);
        $("#transfer_currency_decimals").val(decimals);
        if (currencyCode) {
            currencyCodeField.val(currencyCode);
            currencyCodeField.prop("readonly", true);
            $("#transfer_currency_units_code").html(String(currencyCode).escapeHTML());
            $("#transfer_currency_modal").find(".modal-title").html($.t("Transfer Currency"));
        } else {
            currencyCodeField.val('');
            currencyCodeField.prop("readonly", false);
            $("#transfer_currency_units_code").html('');
            $("#transfer_currency_modal").find(".modal-title").html($.t("Send Currency"));
        }
        $("#transfer_currency_available").html('');

        if (currency) {
            NRS.updateAvailableCurrency(currency);
        }
    });

    currencyCodeField.blur(function () {
        if (!currencyCodeField.val() || currencyCodeField.val() == '') {
            return;
        }
        currencyCodeField.val(currencyCodeField.val().toUpperCase());
        NRS.sendRequest("getCurrency", {
            "code": currencyCodeField.val()
        }, function (response) {
            var transferCurrencyModal = $("#transfer_currency_modal");
            if (response && !response.errorCode) {
                $("#transfer_currency_currency").val(response.currency);
                $("#transfer_currency_decimals").val(response.decimals);
                NRS.updateAvailableCurrency(response.currency);
                $("#transfer_currency_units_code").html(NRS.escapeRespStr(response.code));
                transferCurrencyModal.find(".error_message").hide();
            } else if (response.errorCode) {
                transferCurrencyModal.find(".error_message").html(NRS.escapeRespStr(response.errorDescription));
                transferCurrencyModal.find(".error_message").show();
            }
        })
    });

    NRS.updateAvailableCurrency = function (currency) {
        NRS.sendRequest("getAccountCurrencies", {
            "currency": currency,
            "account": NRS.accountRS,
            "includeCurrencyInfo": true
        }, function (response) {
            var availableCurrencyMessage = "None Available for Transfer";
            if (response.unconfirmedUnits && response.unconfirmedUnits != "0") {
                availableCurrencyMessage = $.t("available_units") + " " + NRS.formatQuantity(response.unconfirmedUnits, response.decimals);
            }
            $("#transfer_currency_available").html(availableCurrencyMessage);
        })
    };

    /* Publish Exchange Offer Model */
    $("#publish_exchange_offer_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);

        $("#publish_exchange_offer_currency_id").val($invoker.data("currency"));
        $("#publish_exchange_offer_decimals_id").val($invoker.data("decimals"));
        $(".currency_code").html(String($invoker.data("code")).escapeHTML());

        context = {
            labelText: "Expiration Height",
            labelI18n: "expiration_height",
            inputName: "expirationHeight",
            helpI18n: "expiration_height_help",
            initBlockHeight: NRS.lastBlockHeight + 14 * 1440,
            changeHeightBlocks: 100
        };
        NRS.initModalUIElement($(this), '.exchange_offer_expiration_height', 'block_height_modal_ui_element', context);

        NRS.sendRequest("getAccountCurrencies", {
            "currency": $invoker.data("currency"),
            "account": NRS.accountRS,
            "includeCurrencyInfo": true
        }, function (response) {
            var availableCurrencyMessage = " - None Available";
            if (response.unconfirmedUnits && response.unconfirmedUnits != "0") {
                availableCurrencyMessage = " - " + $.t("available_units") + " " + NRS.formatQuantity(response.unconfirmedUnits, response.decimals);
            }
            $("#publish_exchange_available").html(availableCurrencyMessage);
        })

    });

    /* EXCHANGE HISTORY PAGE */
    NRS.pages.exchange_history = function () {
        NRS.sendRequest("getExchanges+", {
            "account": NRS.accountRS,
            "includeCurrencyInfo": true,
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        }, function (response) {
            if (response.exchanges && response.exchanges.length) {
                if (response.exchanges.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.exchanges.pop();
                }
                var quantityDecimals = NRS.getNumberOfDecimals(response.exchanges, "units", function(exchange) {
                    return NRS.formatQuantity(exchange.units, exchange.decimals);
                });
                var rateNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "rateNQT", function(exchange) {
                    return NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals);
                });
                var totalNQTDecimals = NRS.getNumberOfDecimals(response.exchanges, "totalNQT", function(exchange) {
                    return NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT));
                });
                var rows = "";
                for (var i = 0; i < response.exchanges.length; i++) {
                    var exchange = response.exchanges[i];
                    rows += "<tr>" +
                    "<td>" + NRS.formatTimestamp(exchange.timestamp) + "</td>" +
                    "<td>" + NRS.getTransactionLink(exchange.transaction) + "</td>" +
                    "<td>" + NRS.getTransactionLink(exchange.offer) + "</td>" +
                    "<td>" + NRS.getTransactionLink(exchange.currency, exchange.code) + "</td>" +
                    "<td>" + NRS.getAccountLink(exchange, "seller") + "</td>" +
                    "<td>" + NRS.getAccountLink(exchange, "buyer") + "</td>" +
                    "<td class='numeric'>" + NRS.formatQuantity(exchange.units, exchange.decimals, false, quantityDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals, rateNQTDecimals) + "</td>" +
                    "<td class='numeric'>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT, exchange.decimals),false,false,totalNQTDecimals) + "</td>" +
                    "</tr>";
                }
                NRS.dataLoaded(rows);
            } else {
                NRS.dataLoaded();
            }
        });
    };

    NRS.pages.currency_transfer_history = function () {
        NRS.sendRequest("getCurrencyTransfers+", {
            "account": NRS.accountRS,
            "includeCurrencyInfo": true,
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        }, function (response) {
            if (response.transfers && response.transfers.length) {
                if (response.transfers.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.transfers.pop();
                }
                var transfers = response.transfers;
                var quantityDecimals = NRS.getNumberOfDecimals(transfers, "units", function(transfer) {
                    return NRS.formatQuantity(transfer.units, transfer.decimals);
                });
                var rows = "";
                for (var i = 0; i < transfers.length; i++) {
                    transfers[i].units = new BigInteger(transfers[i].units);
                    var type = (transfers[i].recipientRS == NRS.accountRS ? "receive" : "send");
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(transfers[i].transfer) + "</td>" +
                    "<td><a href='#' data-goto-currency='" + NRS.escapeRespStr(transfers[i].code) + "'>" + NRS.escapeRespStr(transfers[i].name) + "</a></td>" +
                    "<td>" + NRS.formatTimestamp(transfers[i].timestamp) + "</td>" +
                    "<td style='" + (type == "receive" ? "color:green" : "color:red") + "' class='numeric'>" + NRS.formatQuantity(transfers[i].units, transfers[i].decimals, false, quantityDecimals) + "</td>" +
                    "<td>" + NRS.getAccountLink(transfers[i], "recipient") + "</td>" +
                    "<td>" + NRS.getAccountLink(transfers[i], "sender") + "</td>" +
                    "</tr>";
                }
                NRS.dataLoaded(rows);
            } else {
                NRS.dataLoaded();
            }
        });
    };

    var _selectedApprovalCurrency = "";

    NRS.buildApprovalRequestCurrencyNavi = function () {
        var $select = $('#approve_currency_select');
        $select.empty();

        var currencySelected = false;
        var $noneOption = $('<option value=""></option>');

        NRS.sendRequest("getAccountCurrencies", {
            "account": NRS.accountRS,
            "includeCurrencyInfo": true
        }, function (response) {
            if (response.accountCurrencies) {
                if (response.accountCurrencies.length > 0) {
                    $noneOption.html($.t('no_currency_selected', 'No Currency Selected'));
                    $.each(response.accountCurrencies, function (key, ac) {
                        var idString = String(ac.currency);
                        var $option = $('<option value="' + idString + '">' + String(ac.code) + '</option>');
                        if (idString == _selectedApprovalCurrency) {
                            $option.attr('selected', true);
                            currencySelected = true;
                        }
                        $option.appendTo($select);
                    });
                } else {
                    $noneOption.html($.t('account_has_no_currencies', 'Account has no currencies'));
                }
            } else {
                $noneOption.html($.t('no_connection'));
            }
            if (!_selectedApprovalCurrency || !currencySelected) {
                $noneOption.attr('selected', true);
            }
            $noneOption.prependTo($select);
        });
    };

    NRS.pages.approval_requests_currency = function () {
        NRS.buildApprovalRequestCurrencyNavi();

        if (_selectedApprovalCurrency != "") {
            var params = {
                "currency": _selectedApprovalCurrency,
                "withoutWhitelist": true,
                "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
                "lastIndex": NRS.pageNumber * NRS.itemsPerPage
            };
            NRS.sendRequest("getCurrencyPhasedTransactions", params, function (response) {
                var rows = "";

                if (response.transactions && response.transactions.length > 0) {
                    if (response.transactions.length > NRS.itemsPerPage) {
                        NRS.hasMorePages = true;
                        response.transactions.pop();
                    }
                    var decimals = NRS.getTransactionsAmountDecimals(response.transactions);
                    for (var i = 0; i < response.transactions.length; i++) {
                        var t = response.transactions[i];
                        t.confirmed = true;
                        rows += NRS.getTransactionRowHTML(t, ['approve'], decimals);
                    }
                } else {
                    $('#ar_currency_no_entries').html($.t('no_current_approval_requests', 'No current approval requests'));
                }
                NRS.dataLoaded(rows);
                NRS.addPhasingInfoToTransactionRows(response.transactions);
            });
        } else {
            $('#ar_currency_no_entries').html($.t('please_select_currency', 'Please select a currency'));
            NRS.dataLoaded();
        }
    };

    $('#approve_currency_select').on('change', function () {
        _selectedApprovalCurrency = $(this).find('option:selected').val();
        NRS.loadPage("approval_requests_currency");
    });

    NRS.setup.currencies = function () {
        var sidebarId = 'sidebar_monetary_system';
        var options = {
            "id": sidebarId,
            "titleHTML": '<i class="fa fa-bank"></i><span data-i18n="monetary_system">Monetary System</span>',
            "page": 'currencies',
            "desiredPosition": 40,
            "depends": { tags: [ NRS.constants.API_TAGS.MS ] }
        };
        NRS.addTreeviewSidebarMenuItem(options);
        options = {
            "titleHTML": '<span data-i18n="currencies">Currencies</span>',
            "type": 'PAGE',
            "page": 'currencies'
        };
        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
        options = {
            "titleHTML": '<span data-i18n="exchange_history">Exchange History</span>',
            "type": 'PAGE',
            "page": 'exchange_history'
        };
        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
        options = {
            "titleHTML": '<span data-i18n="transfer_history">Transfer History</span>',
            "type": 'PAGE',
            "page": 'currency_transfer_history'
        };
        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
        options = {
            "titleHTML": '<span data-i18n="approval_requests">Approval Requests</span>',
            "type": 'PAGE',
            "page": 'approval_requests_currency'
        };
        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
        options = {
            "titleHTML": '<span data-i18n="issue_currency">Issue Currency</span></a>',
            "type": 'MODAL',
            "modalId": 'issue_currency_modal'
        };
        NRS.appendMenuItemToTSMenuItem(sidebarId, options);
    };

    /* ISSUE CURRENCY FORM */
    NRS.forms.issueCurrency = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));

        data.description = $.trim(data.description);
        if (data.minReservePerUnitNQT) {
            data.minReservePerUnitNQT = NRS.convertToNQT(data.minReservePerUnitNQT);
            data.minReservePerUnitNQT = NRS.convertToQNTf(data.minReservePerUnitNQT, data.decimals);
        }
        if (!data.initialSupply || data.initialSupply == "") {
            data.initialSupply = "0";
        }
        if (!data.reserveSupply || data.reserveSupply == "") {
            data.reserveSupply = "0";
        }
        if (!data.issuanceHeight) {
            data.issuanceHeight = "0";
        }
        if (!data.ruleset || data.ruleset == "") {
            data.ruleset = "0";
        }
        if (!data.algorithm || data.algorithm == "") {
            data.algorithm = "0";
        }
        if (!data.decimals || data.decimals == "") {
            data.decimals = "0";
        }

        data.type = 0;
        $("[name='type']:checked").each(function () {
            data.type += parseInt($(this).val(), 10);
        });

        if (!data.description) {
            return {
                "error": $.t("error_description_required")
            };
        } else if (!data.name) {
            return {
                "error": $.t("error_name_required")
            };
        } else if (!data.code || data.code.length < 3) {
            return {
                "error": $.t("error_code_required")
            };
        } else if (!data.maxSupply || data.maxSupply < 1) {
            return {
                "error": $.t("error_type_supply")
            };
        } else if (!/^\d+$/.test(data.maxSupply) || !/^\d+$/.test(data.initialSupply) || !/^\d+$/.test(data.reserveSupply)) {
            return {
                "error": $.t("error_whole_units")
            };
        } else {
            try {
                data.maxSupply = NRS.convertToQNT(data.maxSupply, data.decimals);
                data.initialSupply = NRS.convertToQNT(data.initialSupply, data.decimals);
                data.reserveSupply = NRS.convertToQNT(data.reserveSupply, data.decimals);
            } catch (e) {
                return {
                    "error": $.t("error_whole_units")
                };
            }
            return {
                "data": data
            };
        }
    };

    var distributionTable = $("#currency_distribution_table");
    var distributionModal = $("#currency_distribution_modal");
    distributionModal.on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);

        var code = $invoker.data("code");
        var currency = null;
        NRS.sendRequest("getCurrency", {
            "code": code
        }, function (response) {
            currency = response;
        }, { isAsync: false });
        NRS.sendRequest("getCurrencyAccounts", {
            "currency": currency.currency,
            "lastIndex": NRS.state.maxAPIRecords
        }, function (response) {
            var rows = "";

            if (response.accountCurrencies) {
                response.accountCurrencies.sort(function (a, b) {
                    return new BigInteger(b.units).compareTo(new BigInteger(a.units));
                });

                for (var i = 0; i < response.accountCurrencies.length; i++) {
                    var account = response.accountCurrencies[i];
                    var percentageCurrency = NRS.calculatePercentage(account.units, currency.currentSupply);
                    rows += "<tr><td>" + NRS.getAccountLink(account, "account", currency.account, "Currency Issuer") + "</td><td>" + NRS.formatQuantity(account.units, currency.decimals) + "</td><td>" + percentageCurrency + "%</td></tr>";
                }
            }

            distributionTable.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(distributionTable);
        });
    });

    distributionModal.on("hidden.bs.modal", function () {
        distributionTable.find("tbody").empty();
        distributionTable.parent().addClass("data-loading");
    });

    /* TRANSFER CURRENCY FORM */
    NRS.forms.transferCurrency = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var decimals = parseInt(data.decimals, 10);
        if (!data.units) {
            return {
                "error": $.t("error_not_specified", {
                    "name": NRS.getTranslatedFieldName("units").toLowerCase()
                }).capitalize()
            };
        }

        if (!NRS.showedFormWarning) {
            if (NRS.settings["currency_transfer_warning"] && NRS.settings["currency_transfer_warning"] != 0) {
                if (new Big(data.units).cmp(new Big(NRS.settings["currency_transfer_warning"])) > 0) {
                    NRS.showedFormWarning = true;
                    return {
                        "error": $.t("error_max_currency_transfer_warning", {
                            "qty": String(NRS.settings["currency_transfer_warning"]).escapeHTML()
                        })
                    };
                }
            }
        }

        try {
            data.units = NRS.convertToQNT(data.units, decimals);
        } catch (e) {
            return {
                "error": $.t("error_incorrect_units_plus", {
                    "err": e.escapeHTML()
                })
            };
        }

        delete data.decimals;

        if (!data.add_message) {
            delete data.add_message;
            delete data.message;
            delete data.encrypt_message;
            delete data.permanent_message;
        }

        return {
            "data": data
        };
    };

    $('#issue_currency_reservable').change(function () {
        var issuanceHeight = $("#issue_currency_issuance_height");
        if ($(this).is(":checked")) {
            $(".optional_reserve").show();
            issuanceHeight.val("");
            issuanceHeight.prop("disabled", false);
            $(".optional_reserve input").prop("disabled", false);
        } else {
            $(".optional_reserve").hide();
            $(".optional_reserve input").prop("disabled", true);
            issuanceHeight.val(0);
            issuanceHeight.prop("disabled", true);
        }
    });

    $('#issue_currency_claimable').change(function () {
        if ($(this).is(":checked")) {
            $("#issue_currency_initial_supply").val(0);
            $("#issue_currency_issuance_height").prop("disabled", false);
            $(".optional_reserve").show();
            $('#issue_currency_reservable').prop('checked', true);
            $("#issue_currency_min_reserve").prop("disabled", false);
            $("#issue_currency_min_reserve_supply").prop("disabled", false);
        } else {
            $("#issue_currency_initial_supply").val($("#issue_currency_max_supply").val());
        }
    });

    $(".issue_currency_reservable").on("change", function() {
   		if ($(this).is(":checked")) {
   			$(this).closest("form").find(".optional_reserve").fadeIn();
   		} else {
   			$(this).closest("form").find(".optional_reserve").hide();
   		}
   	});

    $('#issue_currency_mintable').change(function () {
        if ($(this).is(":checked")) {
            $(".optional_mint").fadeIn();
            $(".optional_mint input").prop("disabled", false);
        } else {
            $(".optional_mint").hide();
            $(".optional_mint input").prop("disabled", true);
        }
    });

    /* PUBLISH EXCHANGE OFFER MODEL */
    NRS.forms.publishExchangeOffer = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var decimals = parseInt(data.decimals, 10);
        data.initialBuySupply = NRS.convertToQNT(data.initialBuySupply, decimals);
        data.totalBuyLimit = NRS.convertToQNT(data.totalBuyLimit, decimals);
        data.buyRateNQT = NRS.calculatePricePerWholeQNT(NRS.convertToNQT(data.buyRateNQT), decimals);
        data.initialSellSupply = NRS.convertToQNT(data.initialSellSupply, decimals);
        data.totalSellLimit = NRS.convertToQNT(data.totalSellLimit, decimals);
        data.sellRateNQT = NRS.calculatePricePerWholeQNT(NRS.convertToNQT(data.sellRateNQT), decimals);
        return {
            "data": data
        };
    };

    /* RESERVE CURRENCY MODEL */
    $("#reserve_currency_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);
        var currency = $invoker.data("currency");
        var currencyCode = $invoker.data("code");
        NRS.sendRequest("getCurrency+", {
            "currency": currency
        }, function (response) {
            if (response && !response.errorDescription) {
                var currency = response.currency;
                var decimals = response.decimals;
                var minReserve = response.minReservePerUnitNQT;
                var currentReserve = response.currentReservePerUnitNQT;
                var resSupply = response.reserveSupply;
                var initialSupply = response.initialSupply;

                $("#reserve_currency_code").html(String(currencyCode).escapeHTML());
                $("#reserve_currency_currency").val(currency);
                $("#reserve_currency_decimals").val(decimals);
                $("#reserve_currency_minReserve").val(minReserve);
                var minReservePerUnitNQT = new BigInteger(minReserve).multiply(new BigInteger("" + Math.pow(10, decimals)));
                $("#reserve_currency_minReserve_text").html(NRS.formatQuantity(NRS.convertToNXT(minReservePerUnitNQT.multiply(new BigInteger(resSupply))), decimals));
                $("#reserve_currency_currentReserve").val(currentReserve);
                var currentReservePerUnitNQT = new BigInteger(currentReserve).multiply(new BigInteger("" + Math.pow(10, decimals)));
                $("#reserve_currency_currentReserve_text").html(NRS.formatQuantity(NRS.convertToNXT(currentReservePerUnitNQT.multiply(new BigInteger(resSupply))), decimals));
                $("#reserve_currency_resSupply").val(resSupply);
                $("#reserve_currency_resSupply_text").html(NRS.formatQuantity(resSupply, decimals));
                $("#reserve_currency_initialSupply_text").html(NRS.formatQuantity(initialSupply, decimals));
            }
        })
    });

    var reserveCurrencyAmount = $("#reserve_currency_amount");
    reserveCurrencyAmount.keydown(function (e) {
        var decimals = parseInt($("#reserve_currency_decimals").val(), 10);

        var charCode = !e.charCode ? e.which : e.charCode;
        if (NRS.isControlKey(charCode) || e.ctrlKey || e.metaKey) {
            return;
        }
        return NRS.validateDecimals(8 - decimals, charCode, $(this).val(), e);
    });

    reserveCurrencyAmount.blur(function () {
        var decimals = parseInt($("#reserve_currency_decimals").val());
        var resSupply = NRS.convertToQNTf($("#reserve_currency_resSupply").val(), decimals);
        var amountNQT = NRS.convertToNQT(this.value);
        var unitAmountNQT = new BigInteger(amountNQT).divide(new BigInteger(resSupply));
        var roundUnitAmountNQT = NRS.convertToNQT(NRS.amountToPrecision(NRS.convertToNXT(unitAmountNQT), decimals));
        $("#reserve_currency_total").val(NRS.convertToNXT(roundUnitAmountNQT));
        reserveCurrencyAmount.val(NRS.convertToNXT(new BigInteger(roundUnitAmountNQT).multiply(new BigInteger(resSupply)).toString()));
    });

    NRS.forms.currencyReserveIncrease = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var decimals = parseInt(data.decimals, 10);
        data.amountPerUnitNQT = NRS.calculatePricePerWholeQNT(NRS.convertToNQT(data.amountPerUnitNQT), decimals);

        return {
            "data": data
        };
    };

    /* CLAIM CURRENCY MODEL */
    $("#claim_currency_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);

        var currency = $invoker.data("currency");
        var currencyCode = $invoker.data("code");

        NRS.sendRequest("getAccountCurrencies", {
            "currency": currency,
            "account": NRS.accountRS,
            "includeCurrencyInfo": true
        }, function (response) {
            var availableUnits = "0";
            if (response.units) {
                availableUnits = NRS.formatQuantity(response.units, response.decimals);
            }
            $("#claimAvailable").html(availableUnits);
        });

        NRS.sendRequest("getCurrency", {
            "currency": currency
        }, function (response) {
            var currentReservePerUnitNQT = new BigInteger(response.currentReservePerUnitNQT).multiply(new BigInteger("" + Math.pow(10, response.decimals)));
            $("#claimRate").html(NRS.formatAmount(currentReservePerUnitNQT) + " [NXT/" + currencyCode + "]");
        });

        $("#claim_currency_decimals").val($invoker.data("decimals"));
        $("#claim_currency_currency").val(currency);
        $("#claim_currency_code").html(String(currencyCode).escapeHTML());

    });

    $("#claim_currency_amount").keydown(function (e) {
        var decimals = parseInt($("#claim_currency_decimals").val(), 10);

        var charCode = !e.charCode ? e.which : e.charCode;
        if (NRS.isControlKey(charCode) || e.ctrlKey || e.metaKey) {
            return;
        }
        return NRS.validateDecimals(decimals, charCode, $(this).val(), e);
    });

    /* Respect decimal positions on claiming a currency */
    NRS.forms.currencyReserveClaim = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var decimals = parseInt(data.decimals, 10);
        data.units = NRS.convertToQNT(data.units, decimals);

        return {
            "data": data
        };
    };

    return NRS;
}(NRS || {}, jQuery));