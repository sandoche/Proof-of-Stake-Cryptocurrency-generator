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
    var EXCHANGE_NAME = "shape_shift";
    var DEPOSIT_ADDRESSES_KEY = "shapeshift.depositAddresses.";
    var SUPPORTED_COINS = {};

    var coinToPair = function (op, coin) {
        return (op == "buy") ? "NXT_" + coin : coin + "_NXT";
    };

    var pairToCoin = function (pair) {
        if (pair.indexOf("NXT_") == 0) {
            return pair.substring("NXT_".length);
        }
        if (pair.indexOf("_NXT") == pair.length - "_NXT".length) {
            return pair.substring(0, pair.indexOf("_NXT"));
        }
        throw "illegal pair " + pair;
    };

    var reversePair = function (pair) {
        var pairParts = pair.split('_');
        return pairParts[1] + '_' + pairParts[0];
    };

    var addDepositAddress = function(address, pair) {
        var pairParts = pair.split('_');
        NRS.addDepositAddress(address, pairParts[0], pairParts[1], DEPOSIT_ADDRESSES_KEY + NRS.accountRS);
    };

    var apiCall = function(action, requestData, method, doneCallback, ignoreError, modal) {
        NRS.logConsole("api call action: " + action + " ,data: " + JSON.stringify(requestData) + " ,method: " + method +
            (ignoreError ? " ignore " + ignoreError : "") + (modal ? " modal " + modal : ""));
        $.ajax({
            url: NRS.getShapeShiftUrl() + action,
            crossDomain: true,
            dataType: "json",
            type: method,
            timeout: 30000,
            async: true,
            data: requestData
        }).done(function(response, status) {
            if (status != "success") {
                NRS.logConsole(action + ' status ' + status);
                if (modal) {
                    NRS.showModalError(status, modal);
                }
            }
            if (response.error) {
                var error = response.error;
                var msg;
                if (error.code) {
                    msg = ' code ' + error.code + ' errno ' + error.errno + ' syscall ' + error.syscall;
                    NRS.logConsole(action + msg);
                } else {
                    msg = error;
                    NRS.logConsole(action + ' error ' + error);
                }
                if (ignoreError === false) {
                    return;
                }
                if (modal) {
                    NRS.showModalError(msg, modal);
                }
                if (action.indexOf("txStat/") != 0 && action.indexOf("cancelpending") != 0) {
                    $("#shape_shift_status").html($.t("error"));
                }
            }
            doneCallback(response);
        }).fail(function (xhr, textStatus, error) {
            var message = "Request failed, action " + action + " method " + method + " status " + textStatus + " error " + error;
            NRS.logConsole(message);
            throw message;
        })
    };

    var renderExchangeTable = function (op) {
        var coins = NRS.getCoins(EXCHANGE_NAME);
        var tasks = [];
        for (var i = 0; i < coins.length; i++) {
            tasks.push((function (i) {
                return function (callback) {
                    NRS.logConsole("marketinfo iteration " + i);
                    var pair = coinToPair(op, coins[i]);
                    var counterPair = reversePair(pair);
                    NRS.logConsole("counterPair " + counterPair);
                    async.waterfall([
                        function(callback) {
                            apiCall("marketinfo/" + pair, {}, "GET", function(data) {
                                callback(data.error, data);
                            })
                        },
                        function(marketInfoData, callback) {
                            var amount = 100;
                            if (op == "buy") {
                                amount = amount * marketInfoData.rate;
                            }
                            apiCall("sendamount", { "amount": amount, "pair": pair}, "POST", function(data) {
                                if (data.success && data.success.quotedRate) {
                                    marketInfoData.quotedRate = data.success.quotedRate;
                                } else {
                                    marketInfoData.quotedRate = 0;
                                }
                                callback(null, marketInfoData);
                            })
                        }
                    ], function(err, data){
                        if (err) {
                            callback(err, err);
                            return;
                        }
                        var row = "";
                        row += "<tr>";
                        row += "<td>" + SUPPORTED_COINS[coins[i]].name + " " +
                            "<img src='" + SUPPORTED_COINS[coins[i]].image + "' width='16px' height='16px'/>" +
                        "</td>";
                        row += "<td>" + coins[i] + "</td>";
                        var rate, quotedRate, diff;
                        if (op == "sell") {
                            if (parseFloat(data.rate) == 0) {
                                rate = "N/A";
                            } else {
                                rate = NRS.invert(data.rate);
                            }
                            if (parseFloat(data.quotedRate) == 0) {
                                quotedRate = "N/A";
                                diff = "N/A";
                            } else {
                                quotedRate = NRS.invert(data.quotedRate);
                                diff = -100 * (quotedRate - rate) / rate;
                            }
                        } else {
                            rate = data.rate;
                            if (parseFloat(data.quotedRate) == 0) {
                                quotedRate = "N/A";
                                diff = "N/A";
                            } else {
                                quotedRate = data.quotedRate;
                                diff = 100 * (quotedRate - rate) / rate;
                            }
                        }
                        row += "<td>" + String(rate).escapeHTML() + "</td>";
                        row += "<td>" + String(quotedRate).escapeHTML() + "</td>";
                        row += "<td>" + NRS.formatAmount(diff, 2) + "%</td>";
                        row += "<td><a href='#' class='btn btn-xs btn-default' data-18n='shift' data-toggle='modal' data-target='#shape_shift_" + op + "_modal' " +
                            "data-pair='" + pair + "' data-rate='" + data.rate + "' data-min='" + data.minimum + "' data-max='" + data.limit +
                            "' data-fee='" + data.minerFee + "'>Shift</a>";
                        row += "<a href='#' class='btn btn-xs btn-default' data-18n='send_amount' data-toggle='modal' data-target='#m_send_amount_" + op + "_modal' " +
                            "data-pair='" + pair + "' data-rate='" + data.rate + "' data-min='" + data.minimum + "' data-max='" + data.limit +
                            "' data-fee='" + data.minerFee + "'>Send Amount</a></td>";
                        row += "</tr>";
                        NRS.logConsole(row);
                        callback(null, row);
                    });
                }
            })(i));
        }
        NRS.logConsole(tasks.length + " tasks ready to run");
        async.series(tasks, function (err, results) {
            var table = $("#p_shape_shift_" + op + "_nxt");
            if (err) {
                NRS.logConsole("Err: ", err, "\nResults:", results);
                table.find("tbody").empty();
                NRS.dataLoadFinished(table);
                return;
            }
            NRS.logConsole("results", results);
            var rows = "";
            for (i = 0; i < results.length; i++) {
                rows += results[i];
            }
            NRS.logConsole("rows " + rows);
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        });
    };

    var renderMyExchangesTable = function () {
        var depositAddressesJSON = localStorage[DEPOSIT_ADDRESSES_KEY + NRS.accountRS];
        var depositAddresses = [];
        if (depositAddressesJSON) {
            depositAddresses = JSON.parse(depositAddressesJSON);
        }
        var tasks = [];
        var empty = "<td></td>";
        for (var i = 0; i < depositAddresses.length; i++) {
            tasks.push((function (i) {
                return function (callback) {
                    NRS.logConsole("txStat iteration " + i);
                    apiCall("txStat/" + depositAddresses[i].address, {}, "GET", function(data) {
                        var row = "";
                        row += "<tr>";
                        row += "<td>" + NRS.formatTimestamp(depositAddresses[i].time, false, true) + "</td>";
                        row += "<td>" + data.status + "</td>";
                        if (data.status == "failed") {
                            row += "<td>" + data.error + "</td>";
                            row += empty + empty + empty + empty + empty + empty;
                            NRS.logConsole(row);
                            callback(null, row);
                            return;
                        }
                        if (depositAddresses[i].pair) {
                            // To protect against old data already stored in local storage
                            var pairTokens = depositAddresses[i].pair.split("_");
                            depositAddresses[i].from = pairTokens[0];
                            depositAddresses[i].to = pairTokens[1];
                        }
                        row += "<td>" + NRS.getExchangeAddressLink(data.address, depositAddresses[i].from) + "</td>";
                        if (data.status == "no_deposits") {
                            row += empty + empty + empty + empty + empty + empty;
                            NRS.logConsole(row);
                            callback(null, row);
                            return;
                        }
                        row += "<td>" + data.incomingCoin + "</td>";
                        row += "<td>" + data.incomingType + "</td>";
                        if (data.status == "received") {
                            row += empty + empty + empty + empty;
                            NRS.logConsole(row);
                            callback(null, row);
                            return;
                        }
                        row += "<td>" + NRS.getExchangeAddressLink(data.withdraw, depositAddresses[i].to) + "</td>";
                        row += "<td>" + data.outgoingCoin + "</td>";
                        row += "<td>" + data.outgoingType + "</td>";
                        row += "<td>" + NRS.getExchangeTransactionLink(data.transaction, depositAddresses[i].to) + "</td>";
                        NRS.logConsole(row);
                        callback(null, row);
                    }, true);
                }
            })(i));
        }
        NRS.logConsole(tasks.length + " tasks ready to run");
        var table = $("#p_shape_shift_my_table");
        if (tasks.length == 0) {
            table.find("tbody").empty();
            NRS.dataLoadFinished(table);
        }
        async.series(tasks, function (err, results) {
            if (err) {
                NRS.logConsole("Err: ", err, "\nResults:", results);
                table.find("tbody").empty();
                NRS.dataLoadFinished(table);
                return;
            }
            NRS.logConsole("results", results);
            var rows = "";
            for (i = 0; i < results.length; i++) {
                rows += results[i];
            }
            NRS.logConsole("rows " + rows);
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        });
    };

    function renderRecentTable() {
        apiCall('recenttx/50', {}, 'GET', function (data) {
            NRS.logConsole("recent");
            var rows = "";
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    var transaction = data[i];
                    if (String(transaction.curIn).escapeHTML() != "NXT" && String(transaction.curOut).escapeHTML() != "NXT") {
                        continue;
                    }
                    rows += "<tr>";
                    rows += "<td>" + String(transaction.curIn).escapeHTML() + "</td>";
                    rows += "<td>" + String(transaction.curOut).escapeHTML() + "</td>";
                    rows += "<td>" + NRS.formatTimestamp(1000 * transaction.timestamp, false, true) + "</td>";
                    rows += "<td>" + transaction.amount + "</td>";
                    rows += "</tr>";
                }
            }
            NRS.logConsole("recent rows " + rows);
            var table = $("#p_shape_shift_table");
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        });
    }

    function renderNxtLimit() {
        apiCall('limit/nxt_btc', {}, 'GET', function (data) {
            NRS.logConsole("limit1 " + data.limit);
            if (data.limit) {
                $('#shape_shift_status').html('ok');
                $('#shape_shift_nxt_avail').html(String(data.limit).escapeHTML());
            }
        });
    }

    NRS.shapeShiftSelectCoins = function(inputFields, selectedCoins) {
        apiCall('getcoins', {}, 'GET', function (data) {
            SUPPORTED_COINS = data;
            for (var i = 0; i < inputFields.length; i++) {
                inputFields[i].empty();
                var isSelectionAvailable = false;
                $.each(data, function (code, coin) {
                    if (code != 'NXT' && coin['status'] == 'available') {
                        inputFields[i].append('<option value="' + code + '">' + coin['name'] + ' [' + code + ']</option>');
                        SUPPORTED_COINS[code] = coin;
                    }
                    if (selectedCoins[i] == code) {
                        isSelectionAvailable = true;
                    }
                });
                if (isSelectionAvailable) {
                    inputFields[i].val(selectedCoins[i]);
                }
            }
        });
    };

    function loadCoins() {
        var coin0 = EXCHANGE_NAME + "_coin0";
        var coin1 = EXCHANGE_NAME + "_coin1";
        var coin2 = EXCHANGE_NAME + "_coin2";
        var inputFields = [];
        inputFields.push($('#' + coin0));
        inputFields.push($('#' + coin1));
        inputFields.push($('#' + coin2));
        var selectedCoins = [];
        selectedCoins.push(NRS.settings[coin0]);
        selectedCoins.push(NRS.settings[coin1]);
        selectedCoins.push(NRS.settings[coin2]);
        NRS.shapeShiftSelectCoins(inputFields, selectedCoins);
    }

    NRS.pages.exchange_shape_shift = function() {
        var exchangeDisabled = $(".exchange_disabled");
        var exchangePageHeader = $(".exchange_page_header");
        var exchangePageContent = $(".exchange_page_content");
        if (NRS.settings.exchange != "1") {
			exchangeDisabled.show();
            exchangePageHeader.hide();
            exchangePageContent.hide();
            return;
		}
        exchangeDisabled.hide();
        exchangePageHeader.show();
        exchangePageContent.show();
        NRS.pageLoading();
        loadCoins();
        renderNxtLimit();
        renderExchangeTable("buy");
        renderExchangeTable("sell");
        renderMyExchangesTable();
        renderRecentTable();
        NRS.pageLoaded();
        setTimeout(refreshPage, 60000);
    };

    refreshPage = function() {
        if (NRS.currentPage == "exchange_shape_shift") {
            NRS.pages.exchange_shape_shift();
        }
    };

    $("#shape_shift_accept_exchange_link").on("click", function(e) {
   		e.preventDefault();
   		NRS.updateSettings("exchange", "1");
        NRS.pages.exchange_shape_shift();
   	});

    $("#shape_shift_clear_my_exchanges").on("click", function(e) {
   		e.preventDefault();
   		localStorage.removeItem(DEPOSIT_ADDRESSES_KEY + NRS.accountRS);
        renderMyExchangesTable();
   	});

    NRS.getFundAccountLink = function() {
        return "<div class='callout callout-danger'>" +
            "<span>" + $.t("fund_account_warning_1") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_2") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_3") + "</span><br>" +
            "</div>" +
            "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#m_send_amount_sell_modal' " +
            "data-pair='BTC_NXT'>" + $.t("fund_account_message") + "</a>";
    };

    $('.coin-select.shape-shift ').change(function() {
        var id = $(this).attr('id');
        var coins = NRS.getCoins(EXCHANGE_NAME);
        coins[parseInt(id.slice(-1))] = $(this).val();
        NRS.setCoins(EXCHANGE_NAME, coins);
        renderExchangeTable('buy');
        renderExchangeTable('sell');
    });

	NRS.setup.exchange = function() {
        // Do not implement connection to a 3rd party site here to prevent privacy leak
    };

    $("#shape_shift_buy_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var pair = invoker.data("pair");
        $("#shape_shift_buy_pair").val(pair);
        var coin = pairToCoin(pair);
        NRS.logConsole("modal invoked pair " + pair + " coin " + coin);
        $("#shape_shift_buy_title").html($.t("exchange_nxt_to_coin_shift", { coin: coin }));
        $("#shape_shift_buy_min").val(invoker.data("min"));
        $("#shape_shift_buy_min_coin").html("NXT");
        $("#shape_shift_buy_max").val(invoker.data("max"));
        $("#shape_shift_buy_max_coin").html("NXT");
        $("#shape_shift_buy_rate").val(invoker.data("rate"));
        $("#shape_shift_buy_rate_text").html("NXT/" + coin);
        $("#shape_shift_withdrawal_address_coin").html(coin);
        $("#shape_shift_buy_fee").val(invoker.data("fee"));
        $("#shape_shift_buy_fee_coin").html(coin);
    });

    $("#shape_shift_buy_submit").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var amountNQT = NRS.convertToNQT($("#shape_shift_buy_amount").val());
        var withdrawal = $("#shape_shift_buy_withdrawal_address").val();
        var pair = $("#shape_shift_buy_pair").val();
        NRS.logConsole('shift withdrawal ' + withdrawal + " pair " + pair);
        apiCall('shift', {
            withdrawal: withdrawal,
            pair: pair,
            returnAddress: NRS.accountRS,
            apiKey: NRS.settings.shape_shift_api_key
        }, 'POST', function (data) {
            NRS.logConsole("shift response");
            var msg;
            if (data.error) {
                return;
            }
            if (data.depositType != "NXT") {
                msg = "incorrect deposit coin " + data.depositType;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            if (data.withdrawalType != pairToCoin(pair)) {
                msg = "incorrect withdrawal coin " + data.withdrawalType;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            if (data.withdrawal != withdrawal) {
                msg = "incorrect withdrawal address " + data.withdrawal;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            NRS.logConsole("shift request done, deposit address " + data.deposit);
            NRS.sendRequest("sendMoney", {
                "recipient": data.deposit,
                "amountNQT": amountNQT,
                "secretPhrase": $("#shape_shift_buy_password").val(),
                "deadline": "1440",
                "feeNQT": NRS.convertToNQT(1)
            }, function (response) {
                if (response.errorCode) {
                    NRS.logConsole("sendMoney response " + response.errorCode + " " + response.errorDescription.escapeHTML());
                    NRS.showModalError(NRS.translateServerError(response), $modal);
                    return;
                }
                addDepositAddress(data.deposit, pair);
                renderMyExchangesTable();
                $("#shape_shift_buy_passpharse").val("");
                NRS.unlockForm($modal, $btn, true);
            })
        }, true, $modal);
    });

    $("#m_send_amount_buy_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var pair = invoker.data("pair");
        var coin = pairToCoin(pair);
        NRS.logConsole("modal invoked pair " + pair + " coin " + coin);
        $("#m_send_amount_buy_title").html($.t("exchange_nxt_to_coin_send_amount", { coin: coin }));
        $("#m_send_amount_buy_withdrawal_amount_coin").html(coin);
        $("#m_send_amount_buy_rate_text").html("NXT/" + coin);
        $("#m_send_amount_withdrawal_address_coin").html(coin + " address");
        $("#m_send_amount_buy_fee_coin").html(coin);
        $("#m_send_amount_buy_pair").val(pair);
        $("#m_send_amount_buy_submit").prop('disabled', true);
    });

    $('#m_send_amount_buy_withdrawal_amount, #m_send_amount_buy_withdrawal_address').change(function () {
        var modal = $(this).closest(".modal");
        var amount = $('#m_send_amount_buy_withdrawal_amount').val();
        var withdrawal = $('#m_send_amount_buy_withdrawal_address').val();
        var pair = $("#m_send_amount_buy_pair").val();
        var buySubmit = $("#m_send_amount_buy_submit");
        buySubmit.prop('disabled', true);
        if (amount == "" || withdrawal == "") {
            return;
        }
        modal.css('cursor','wait');
        apiCall('sendamount', {
            amount: amount,
            withdrawal: withdrawal,
            pair: pair,
            returnAddress: NRS.accountRS,
            apiKey: NRS.settings.shape_shift_api_key
        }, "POST", function(data) {
            try {
                var rate = $("#m_send_amount_buy_rate");
                var fee = $("#m_send_amount_buy_fee");
                var depositAmount = $("#m_send_amount_buy_deposit_amount");
                var depositAddress = $("#m_send_amount_buy_deposit_address");
                var expiration = $("#m_send_amount_buy_expiration");
                if (data.error) {
                    rate.val("");
                    fee.val("");
                    depositAmount.val("");
                    depositAddress.val("");
                    expiration.val("");
                    buySubmit.prop('disabled', true);
                    return;
                }
                if (amount != data.success.withdrawalAmount) {
                    NRS.showModalError("amount returned from shapeshift " + data.success.withdrawalAmount +
                    " differs from requested amount " + amount, modal);
                    return;
                }
                if (withdrawal != data.success.withdrawal) {
                    NRS.showModalError("withdrawal address returned from shapeshift " + data.success.withdrawal +
                    " differs from requested address " + withdrawal, modal);
                    return;
                }
                modal.find(".error_message").html("").hide();
                rate.val(data.success.quotedRate);
                fee.val(data.success.minerFee);
                // add 1 NXT fee to make sure the net amount is what requested by shape shift
                depositAmount.val(parseFloat(data.success.depositAmount) + 1);
                depositAddress.val(data.success.deposit);
                expiration.val(NRS.formatTimestamp(data.success.expiration, false, true));
                buySubmit.prop('disabled', false);
            } finally {
                modal.css('cursor', 'default');
            }
        }, true, modal)
    });

    $("#m_send_amount_buy_submit").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var pair = $("#m_send_amount_buy_pair").val();
        var depositAddress = $("#m_send_amount_buy_deposit_address").val();
        NRS.logConsole("pay request submitted, deposit address " + depositAddress);
        var amountNQT = NRS.convertToNQT($("#m_send_amount_buy_deposit_amount").val());
        NRS.sendRequest("sendMoney", {
            "recipient": depositAddress,
            "amountNQT": amountNQT,
            "secretPhrase": $("#m_send_amount_buy_password").val(),
            "deadline": "1440",
            "feeNQT": NRS.convertToNQT(1)
        }, function (response) {
            if (response.errorCode) {
                NRS.logConsole('sendMoney error ' + response.errorDescription.escapeHTML());
                NRS.showModalError(response.errorDescription.escapeHTML(), $modal);
                return;
            }
            addDepositAddress(depositAddress, pair);
            renderMyExchangesTable();
            $("#m_send_amount_buy_passpharse").val("");
            NRS.unlockForm($modal, $btn, true);
        });
    });

    $("#shape_shift_sell_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var modal = $(this).closest(".modal");
        var pair = invoker.data("pair");
        var coin = pairToCoin(pair);
        NRS.logConsole("modal invoked pair " + pair + " coin " + coin);
        $("#shape_shift_sell_title").html($.t("exchange_coin_to_nxt_shift", { coin: coin }));
        $("#shape_shift_sell_qr_code").html("");
        var data = invoker.data;
        modal.css('cursor','wait');
        async.waterfall([
            function(callback) {
                if (data.rate) {
                    callback(null);
                } else {
                    apiCall("marketinfo/" + pair, {}, "GET", function(response) {
                        data.rate = response.rate;
                        data.min = response.minimum;
                        data.max = response.limit;
                        data.fee = response.minerFee;
                        callback(null);
                    })
                }
            },
            function(callback) {
                $("#shape_shift_sell_min").val(data.min);
                $("#shape_shift_sell_min_coin").html(coin);
                $("#shape_shift_sell_max").val(data.max);
                $("#shape_shift_sell_max_coin").html(coin);
                $("#shape_shift_sell_rate").val(data.rate);
                $("#shape_shift_sell_rate_text").html(coin + "/NXT");
                $("#shape_shift_sell_fee").val(data.fee);
                $("#shape_shift_sell_fee_coin").html("NXT");
                $("#shape_shift_sell_pair").val(pair);
                var publicKey = NRS.publicKey;
                if (publicKey == "" && NRS.accountInfo) {
                    publicKey = NRS.accountInfo.publicKey;
                }
                if (!publicKey || publicKey == "") {
                    NRS.showModalError("Account has no public key, please login using your passphrase", modal);
                    return;
                }
                apiCall('shift', {
                    withdrawal: NRS.accountRS,
                    rsAddress: publicKey,
                    pair: pair,
                    apiKey: NRS.settings.shape_shift_api_key
                }, "POST", function (data) {
                    NRS.logConsole("shift request done");
                    var msg;
                    if (data.depositType != coin) {
                        msg = "incorrect deposit coin " + data.depositType;
                        NRS.logConsole(msg);
                        NRS.showModalError(msg, modal);
                        callback(null);
                        return;
                    }
                    if (data.withdrawalType != "NXT") {
                        msg = "incorrect withdrawal coin " + data.withdrawalType;
                        NRS.logConsole(msg);
                        NRS.showModalError(msg, modal);
                        callback(null);
                        return;
                    }
                    if (data.withdrawal != NRS.accountRS) {
                        msg = "incorrect withdrawal address " + data.withdrawal;
                        NRS.logConsole(msg);
                        NRS.showModalError(msg, modal);
                        callback(null);
                        return;
                    }
                    NRS.logConsole("shift request done, deposit address " + data.deposit);
                    $("#shape_shift_sell_deposit_address").html(data.deposit);
                    NRS.generateQRCode("#shape_shift_sell_qr_code", data.deposit);
                    callback(null);
                })
            }
        ], function (err, result) {
            modal.css('cursor', 'default');
        })
    });

    $("#shape_shift_sell_done").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var pair = $("#shape_shift_sell_pair").val();
        var deposit = $("#shape_shift_sell_deposit_address").html();
        if (deposit != "") {
            addDepositAddress(deposit, pair);
            renderMyExchangesTable();
        }
        NRS.unlockForm($modal, $btn, true);
    });

    $("#shape_shift_sell_cancel").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var deposit = $("#shape_shift_sell_deposit_address").html();
        if (deposit != "") {
            apiCall('cancelpending', { address: deposit }, 'POST', function(data) {
                var msg = data.success ? data.success : data.err;
                NRS.logConsole("sell cancelled response: " + msg);
                NRS.unlockForm($modal, $btn, true);
            })
        } else {
            NRS.unlockForm($modal, $btn, true);
        }
    });

    $("#m_send_amount_sell_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var modal = $(this).closest(".modal");
        var pair = invoker.data("pair");
        var coin = pairToCoin(pair);
        NRS.logConsole("modal invoked pair " + pair + " coin " + coin);
        $("#m_send_amount_sell_title").html($.t("exchange_coin_to_nxt_send_amount", { coin: coin }));
        $("#m_send_amount_sell_rate_text").html("NXT/" + coin);
        $("#m_send_amount_sell_fee_coin").html("NXT");
        $("#m_send_amount_sell_withdrawal_amount_coin").html("NXT");
        $("#m_send_amount_sell_deposit_amount_coin").html(coin);
        $("#m_send_amount_sell_deposit_address").html("");
        $("#m_send_amount_sell_qr_code").html("<span style='color: blue'>" + $.t("please_enter_withdrawal_amount") + "</span>");
        $("#m_send_amount_sell_pair").val(pair);
        $("#m_send_amount_sell_done").prop('disabled', true);
    });

    $('#m_send_amount_sell_withdrawal_amount').change(function () {
        var modal = $(this).closest(".modal");
        var amount = $('#m_send_amount_sell_withdrawal_amount').val();
        var pair = $('#m_send_amount_sell_pair').val();
        $("#m_send_amount_sell_done").prop('disabled', true);
        var publicKey = NRS.publicKey;
        if (publicKey == "" && NRS.accountInfo) {
            publicKey = NRS.accountInfo.publicKey;
        }
        if (!publicKey || publicKey == "") {
            NRS.showModalError("Account has no public key, please login using your passphrase", modal);
            return;
        }
        $("#m_send_amount_sell_qr_code").html("<span style='color: blue'>" + $.t("please_enter_withdrawal_amount") + "</span>");
        modal.css('cursor','wait');
        apiCall('sendamount', { amount: amount, withdrawal: NRS.accountRS, pubKey: publicKey, pair: pair, apiKey: NRS.settings.shape_shift_api_key },
                "POST", function (data) {
            try {
                var rate = $("#m_send_amount_sell_rate");
                var fee = $("#m_send_amount_sell_fee");
                var depositAmount = $("#m_send_amount_sell_deposit_amount");
                var depositAddress = $("#m_send_amount_sell_deposit_address");
                var expiration = $("#m_send_amount_sell_expiration");
                if (data.error) {
                    rate.val("");
                    fee.val("");
                    depositAmount.val("");
                    depositAddress.html("");
                    expiration.val("");
                    return;
                }
                if (amount != data.success.withdrawalAmount) {
                    NRS.showModalError("amount returned from shapeshift " + data.success.withdrawalAmount +
                    " differs from requested amount " + amount, modal);
                    return;
                }
                if (NRS.accountRS != data.success.withdrawal) {
                    NRS.showModalError("withdrawal address returned from shapeshift " + data.success.withdrawal +
                    " differs from requested address " + NRS.accountRS, modal);
                    return;
                }
                modal.find(".error_message").html("").hide();
                rate.val(NRS.invert(data.success.quotedRate));
                fee.val(data.success.minerFee);
                depositAmount.val(parseFloat(data.success.depositAmount));
                depositAddress.html(data.success.deposit);
                expiration.val(NRS.formatTimestamp(data.success.expiration, false, true));
                NRS.logConsole("sendamount request done, deposit address " + data.success.deposit);
                NRS.generateQRCode("#m_send_amount_sell_qr_code", "bitcoin:" + data.success.deposit + "?amount=" + data.success.depositAmount);
                $("#m_send_amount_sell_done").prop('disabled', false);
            } finally {
                modal.css('cursor', 'default');
            }
        }, true, modal)
    });

    $("#m_send_amount_sell_done").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var pair = $("#m_send_amount_sell_pair").val();
        var deposit = $("#m_send_amount_sell_deposit_address").html();
        if (deposit != "") {
            addDepositAddress(deposit, pair);
            renderMyExchangesTable();
        }
        NRS.unlockForm($modal, $btn, true);
    });

    $("#m_send_amount_sell_cancel").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var deposit = $("#m_send_amount_sell_deposit_address").html();
        if (deposit != "") {
            apiCall('cancelpending', { address: deposit }, 'POST', function(data) {
                var msg = data.success ? data.success : data.err;
                NRS.logConsole("sell cancelled response: " + msg);
                NRS.unlockForm($modal, $btn, true);
            })
        } else {
            NRS.unlockForm($modal, $btn, true);
        }
    });

	return NRS;
}(NRS || {}, jQuery));