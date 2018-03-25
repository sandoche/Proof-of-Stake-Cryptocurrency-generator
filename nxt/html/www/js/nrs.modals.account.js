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
var NRS = (function(NRS, $) {
	NRS.userInfoModal = {
		"user": 0
	};

    var target = document.getElementById('user_info_modal_transactions_table');


	var body = $("body");
    body.on("click", ".show_account_modal_action, a[data-user].user_info", function(e) {
		e.preventDefault();
		var account = $(this).data("user");
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // The current modal
        }
		NRS.showAccountModal(account);
	});

	NRS.showAccountModal = function(account) {
		if (NRS.fetchingModalData) {
			return;
		}

		if (typeof account == "object") {
			NRS.userInfoModal.user = account.account;
		} else {
			NRS.userInfoModal.user = account;
			NRS.fetchingModalData = true;
		}
        NRS.setBackLink();
		NRS.modalStack.push({ class: "show_account_modal_action", key: "user", value: account});

		$("#user_info_modal_account").html(NRS.getAccountFormatted(NRS.userInfoModal.user));
		var accountButton;
		if (NRS.userInfoModal.user in NRS.contacts) {
			accountButton = NRS.contacts[NRS.userInfoModal.user].name.escapeHTML();
			$("#user_info_modal_add_as_contact").hide();
		} else {
			accountButton = NRS.userInfoModal.user;
			$("#user_info_modal_add_as_contact").show();
		}

		$("#user_info_modal_actions").find("button").data("account", accountButton);

		if (NRS.fetchingModalData) {
            NRS.spinner.spin(target);
			NRS.sendRequest("getAccount", {
				"account": NRS.userInfoModal.user
            }, function(response) {
				NRS.processAccountModalData(response);
				NRS.fetchingModalData = false;
			});
		} else {
			NRS.spinner.spin(target);
			NRS.processAccountModalData(account);
		}
		$("#user_info_modal_transactions").show();
		NRS.userInfoModal.transactions();
	};

	NRS.processAccountModalData = function(account) {
		if (account.unconfirmedBalanceNQT == "0") {
			$("#user_info_modal_account_balance").html("0");
		} else {
			$("#user_info_modal_account_balance").html(NRS.formatAmount(account.unconfirmedBalanceNQT) + " NXT");
		}

		if (account.name) {
			$("#user_info_modal_account_name").html(NRS.escapeRespStr(account.name));
			$("#user_info_modal_account_name_container").show();
		} else {
			$("#user_info_modal_account_name_container").hide();
		}

		if (account.description) {
			$("#user_info_description").show();
			$("#user_info_modal_description").html(NRS.escapeRespStr(account.description).nl2br());
		} else {
			$("#user_info_description").hide();
		}
		var switchAccount = $("#user_info_switch_account");
        if (NRS.accountRS != account.accountRS) {
			switchAccount.html("<a class='btn btn-info btn-xs switch-account' data-account='" + account.accountRS + "'>" + $.t("switch_account") + "</a>");
			switchAccount.show();
		} else {
			switchAccount.hide();
		}

        var userInfoModal = $("#user_info_modal");
        if (!userInfoModal.data('bs.modal') || !userInfoModal.data('bs.modal').isShown) {
            userInfoModal.modal("show");
        }
        NRS.spinner.stop(target);
	};

	body.on("click", ".switch-account", function() {
		var account = $(this).data("account");
		NRS.closeModal($("#user_info_modal"));
		NRS.switchAccount(account);
	});

	var userInfoModal = $("#user_info_modal");
    userInfoModal.on("hidden.bs.modal", function() {
		$(this).find(".user_info_modal_content").hide();
		$(this).find(".user_info_modal_content table tbody").empty();
		$(this).find(".user_info_modal_content:not(.data-loading,.data-never-loading)").addClass("data-loading");
		$(this).find("ul.nav li.active").removeClass("active");
		$("#user_info_transactions").addClass("active");
		NRS.userInfoModal.user = 0;
	});

	userInfoModal.find("ul.nav li").click(function(e) {
		e.preventDefault();
		var tab = $(this).data("tab");
		$(this).siblings().removeClass("active");
		$(this).addClass("active");
		$(".user_info_modal_content").hide();

		var content = $("#user_info_modal_" + tab);
		content.show();
		if (content.hasClass("data-loading")) {
			NRS.userInfoModal[tab]();
		}
	});

    function getTransactionType(transaction) {
        var transactionType = $.t(NRS.transactionTypes[transaction.type].subTypes[transaction.subtype].i18nKeyTitle);
        if (transaction.type == NRS.subtype.AliasSell.type && transaction.subtype == NRS.subtype.AliasSell.subtype) {
            if (transaction.attachment.priceNQT == "0") {
                if (transaction.sender == transaction.recipient) {
                    transactionType = $.t("alias_sale_cancellation");
                } else {
                    transactionType = $.t("alias_transfer");
                }
            } else {
                transactionType = $.t("alias_sale");
            }
        }
        return transactionType;
    }

    NRS.userInfoModal.transactions = function() {
        NRS.sendRequest("getBlockchainTransactions", {
			"account": NRS.userInfoModal.user,
			"firstIndex": 0,
			"lastIndex": 100
		}, function(response) {
            var infoModalTransactionsTable = $("#user_info_modal_transactions_table");
			if (response.transactions && response.transactions.length) {
				var rows = "";
				var amountDecimals = NRS.getNumberOfDecimals(response.transactions, "amountNQT", function(val) {
					return NRS.formatAmount(val.amountNQT);
				});
				var feeDecimals = NRS.getNumberOfDecimals(response.transactions, "fee", function(val) {
					return NRS.formatAmount(val.fee);
				});
				for (var i = 0; i < response.transactions.length; i++) {
					var transaction = response.transactions[i];
                    var transactionType = getTransactionType(transaction);
                    var receiving;
					if (/^NXT\-/i.test(String(NRS.userInfoModal.user))) {
						receiving = (transaction.recipientRS == NRS.userInfoModal.user);
					} else {
						receiving = (transaction.recipient == NRS.userInfoModal.user);
					}

					if (transaction.amountNQT) {
						transaction.amount = new BigInteger(transaction.amountNQT);
						transaction.fee = new BigInteger(transaction.feeNQT);
					}
					var account = (receiving ? "sender" : "recipient");
					rows += "<tr>" +
						"<td>" + NRS.getTransactionLink(transaction.transaction, NRS.formatTimestamp(transaction.timestamp)) + "</td>" +
						"<td>" + NRS.getTransactionIconHTML(transaction.type, transaction.subtype) + "&nbsp" + transactionType + "</td>" +
						"<td class='numeric'  " + (transaction.type == 0 && receiving ? " style='color:#006400;'" : (!receiving && transaction.amount > 0 ? " style='color:red'" : "")) + ">" + (!receiving && transaction.amount > 0 ? "-" : "")  + "" + NRS.formatAmount(transaction.amount, false, false, amountDecimals) + "</td>" +
						"<td class='numeric' " + (!receiving ? " style='color:red'" : "") + ">" + NRS.formatAmount(transaction.fee, false, false, feeDecimals) + "</td>" +
						"<td>" + NRS.getAccountLink(transaction, account) + "</td>" +
					"</tr>";
				}

				infoModalTransactionsTable.find("tbody").empty().append(rows);
				NRS.dataLoadFinished(infoModalTransactionsTable);
			} else {
				infoModalTransactionsTable.find("tbody").empty();
				NRS.dataLoadFinished(infoModalTransactionsTable);
			}
		});
	};

    NRS.userInfoModal.ledger = function() {
        NRS.sendRequest("getAccountLedger", {
            "account": NRS.userInfoModal.user,
            "includeHoldingInfo": true,
            "firstIndex": 0,
            "lastIndex": 100
        }, function (response) {
            var infoModalLedgerTable = $("#user_info_modal_ledger_table");
            if (response.entries && response.entries.length) {
                var rows = "";
				var decimalParams = NRS.getLedgerNumberOfDecimals(response.entries);
				for (var i = 0; i < response.entries.length; i++) {
                    var entry = response.entries[i];
                    rows += NRS.getLedgerEntryRow(entry, decimalParams);
                }
                infoModalLedgerTable.find("tbody").empty().append(rows);
                NRS.dataLoadFinished(infoModalLedgerTable);
            } else {
                infoModalLedgerTable.find("tbody").empty();
                NRS.dataLoadFinished(infoModalLedgerTable);
            }
        });
	};

	NRS.userInfoModal.aliases = function() {
		NRS.sendRequest("getAliases", {
			"account": NRS.userInfoModal.user,
			"firstIndex": 0,
			"lastIndex": 100
		}, function(response) {
			var rows = "";
			if (response.aliases && response.aliases.length) {
				var aliases = response.aliases;
				aliases.sort(function(a, b) {
					if (a.aliasName.toLowerCase() > b.aliasName.toLowerCase()) {
						return 1;
					} else if (a.aliasName.toLowerCase() < b.aliasName.toLowerCase()) {
						return -1;
					} else {
						return 0;
					}
				});
				for (var i = 0; i < aliases.length; i++) {
					var alias = aliases[i];
					rows += "<tr data-alias='" + NRS.escapeRespStr(String(alias.aliasName).toLowerCase()) + "'><td class='alias'>" + NRS.escapeRespStr(alias.aliasName) + "</td><td class='uri'>" + (alias.aliasURI.indexOf("http") === 0 ? "<a href='" + NRS.escapeRespStr(alias.aliasURI) + "' target='_blank'>" + NRS.escapeRespStr(alias.aliasURI) + "</a>" : NRS.escapeRespStr(alias.aliasURI)) + "</td></tr>";
				}
			}
            var infoModalAliasesTable = $("#user_info_modal_aliases_table");
            infoModalAliasesTable.find("tbody").empty().append(rows);
			NRS.dataLoadFinished(infoModalAliasesTable);
		});
	};

	NRS.userInfoModal.marketplace = function() {
		NRS.sendRequest("getDGSGoods", {
			"seller": NRS.userInfoModal.user,
			"firstIndex": 0,
			"lastIndex": 100
		}, function(response) {
			var rows = "";
			var quantityDecimals = NRS.getNumberOfDecimals(response.goods, "quantity", function(val) {
				return NRS.format(val.quantity);
			});
			var priceDecimals = NRS.getNumberOfDecimals(response.goods, "priceNQT", function(val) {
				return NRS.formatAmount(val.priceNQT);
			});
			if (response.goods && response.goods.length) {
				for (var i = 0; i < response.goods.length; i++) {
					var good = response.goods[i];
					if (good.name.length > 150) {
						good.name = good.name.substring(0, 150) + "...";
					}
					rows += "<tr><td><a href='#' data-goto-goods='" + NRS.escapeRespStr(good.goods) + "' data-seller='" + NRS.escapeRespStr(NRS.userInfoModal.user) + "'>" + NRS.escapeRespStr(good.name) + "</a></td><td class='numeric'>" + NRS.formatAmount(good.priceNQT, false, false, priceDecimals) + " NXT</td><td class='numeric'>" + NRS.format(good.quantity, false, quantityDecimals) + "</td></tr>";
				}
			}
            var infoModalMarketplaceTable = $("#user_info_modal_marketplace_table");
            infoModalMarketplaceTable.find("tbody").empty().append(rows);
			NRS.dataLoadFinished(infoModalMarketplaceTable);
		});
	};
	
	NRS.userInfoModal.currencies = function() {
		NRS.sendRequest("getAccountCurrencies+", {
			"account": NRS.userInfoModal.user,
			"includeCurrencyInfo": true
		}, function(response) {
			var rows = "";
			var unitsDecimals = NRS.getNumberOfDecimals(response.accountCurrencies, "unconfirmedUnits", function(val) {
				return NRS.formatQuantity(val.unconfirmedUnits, val.decimals);
			});
			if (response.accountCurrencies && response.accountCurrencies.length) {
				for (var i = 0; i < response.accountCurrencies.length; i++) {
					var currency = response.accountCurrencies[i];
					var code = NRS.escapeRespStr(currency.code);
					rows += "<tr>" +
						"<td>" + NRS.getTransactionLink(NRS.escapeRespStr(currency.currency), code) + "</td>" +
						"<td>" + currency.name + "</td>" +
						"<td class='numeric'>" + NRS.formatQuantity(currency.unconfirmedUnits, currency.decimals, false, unitsDecimals) + "</td>" +
					"</tr>";
				}
			}
            var infoModalCurrenciesTable = $("#user_info_modal_currencies_table");
            infoModalCurrenciesTable.find("tbody").empty().append(rows);
			NRS.dataLoadFinished(infoModalCurrenciesTable);
		});
	};

	NRS.userInfoModal.assets = function() {
		NRS.sendRequest("getAccount", {
			"account": NRS.userInfoModal.user,
            "includeAssets": true
        }, function(response) {
			if (response.assetBalances && response.assetBalances.length) {
				var assets = {};
				var nrAssets = 0;
				var ignoredAssets = 0; // Optimization to reduce number of getAsset calls
				for (var i = 0; i < response.assetBalances.length; i++) {
					if (response.assetBalances[i].balanceQNT == "0") {
						ignoredAssets++;
						if (nrAssets + ignoredAssets == response.assetBalances.length) {
							NRS.userInfoModal.addIssuedAssets(assets);
						}
						continue;
					}

					NRS.sendRequest("getAsset", {
						"asset": response.assetBalances[i].asset,
						"_extra": {
							"balanceQNT": response.assetBalances[i].balanceQNT
						}
					}, function(asset, input) {
						asset.asset = input.asset;
						asset.balanceQNT = input["_extra"].balanceQNT;
						assets[asset.asset] = asset;
						nrAssets++;
                        // This will work since eventually the condition below or in the previous
                        // if statement would be met
						//noinspection JSReferencingMutableVariableFromClosure
                        if (nrAssets + ignoredAssets == response.assetBalances.length) {
							NRS.userInfoModal.addIssuedAssets(assets);
						}
					});
				}
			} else {
				NRS.userInfoModal.addIssuedAssets({});
			}
		});
	};

	NRS.userInfoModal.trade_history = function() {
		NRS.sendRequest("getTrades", {
			"account": NRS.userInfoModal.user,
			"includeAssetInfo": true,
			"firstIndex": 0,
			"lastIndex": 100
		}, function(response) {
			var rows = "";
			var quantityDecimals = NRS.getNumberOfDecimals(response.trades, "quantityQNT", function(val) {
				return NRS.formatQuantity(val.quantityQNT, val.decimals);
			});
			var priceDecimals = NRS.getNumberOfDecimals(response.trades, "priceNQT", function(val) {
				return NRS.formatOrderPricePerWholeQNT(val.priceNQT, val.decimals);
			});
			var amountDecimals = NRS.getNumberOfDecimals(response.trades, "totalNQT", function(val) {
				return NRS.formatAmount(NRS.calculateOrderTotalNQT(val.quantityQNT, val.priceNQT));
			});
			if (response.trades && response.trades.length) {
				var trades = response.trades;
				for (var i = 0; i < trades.length; i++) {
					trades[i].priceNQT = new BigInteger(trades[i].priceNQT);
					trades[i].quantityQNT = new BigInteger(trades[i].quantityQNT);
					trades[i].totalNQT = new BigInteger(NRS.calculateOrderTotalNQT(trades[i].priceNQT, trades[i].quantityQNT));
					var type = (trades[i].buyerRS == NRS.userInfoModal.user ? "buy" : "sell");
					rows += "<tr><td><a href='#' data-goto-asset='" + NRS.escapeRespStr(trades[i].asset) + "'>" + NRS.escapeRespStr(trades[i].name) + "</a></td><td>" + NRS.formatTimestamp(trades[i].timestamp) + "</td><td style='color:" + (type == "buy" ? "green" : "red") + "'>" + $.t(type) + "</td><td class='numeric'>" + NRS.formatQuantity(trades[i].quantityQNT, trades[i].decimals, false, quantityDecimals) + "</td><td class='asset_price numeric'>" + NRS.formatOrderPricePerWholeQNT(trades[i].priceNQT, trades[i].decimals, priceDecimals) + "</td><td class='numeric' style='color:" + (type == "buy" ? "red" : "green") + "'>" + NRS.formatAmount(trades[i].totalNQT, false, false, amountDecimals) + "</td></tr>";
				}
			}
            var infoModalTradeHistoryTable = $("#user_info_modal_trade_history_table");
            infoModalTradeHistoryTable.find("tbody").empty().append(rows);
			NRS.dataLoadFinished(infoModalTradeHistoryTable);
		});
	};

	NRS.userInfoModal.addIssuedAssets = function(assets) {
		NRS.sendRequest("getAssetsByIssuer", {
			"account": NRS.userInfoModal.user
		}, function(response) {
			if (response.assets && response.assets[0] && response.assets[0].length) {
				$.each(response.assets[0], function(key, issuedAsset) {
					if (assets[issuedAsset.asset]) {
						assets[issuedAsset.asset].issued = true;
					} else {
						issuedAsset.balanceQNT = "0";
						issuedAsset.issued = true;
						assets[issuedAsset.asset] = issuedAsset;
					}
				});
				NRS.userInfoModal.assetsLoaded(assets);
			} else if (!$.isEmptyObject(assets)) {
				NRS.userInfoModal.assetsLoaded(assets);
			} else {
                var infoModalAssetsTable = $("#user_info_modal_assets_table");
                infoModalAssetsTable.find("tbody").empty();
				NRS.dataLoadFinished(infoModalAssetsTable);
			}
		});
	};

	NRS.userInfoModal.assetsLoaded = function(assets) {
		var assetArray = [];
		var rows = "";
		$.each(assets, function(key, asset) {
			assetArray.push(asset);
		});
		assetArray.sort(function(a, b) {
			if (a.issued && b.issued) {
				if (a.name.toLowerCase() > b.name.toLowerCase()) {
					return 1;
				} else if (a.name.toLowerCase() < b.name.toLowerCase()) {
					return -1;
				} else {
					return 0;
				}
			} else if (a.issued) {
				return -1;
			} else if (b.issued) {
				return 1;
			} else {
				if (a.name.toLowerCase() > b.name.toLowerCase()) {
					return 1;
				} else if (a.name.toLowerCase() < b.name.toLowerCase()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		var quantityDecimals = NRS.getNumberOfDecimals(assetArray, "balanceQNT", function(val) {
			return NRS.formatQuantity(val.balanceQNT, val.decimals);
		});
		var totalDecimals = NRS.getNumberOfDecimals(assetArray, "quantityQNT", function(val) {
			return NRS.formatQuantity(val.quantityQNT, val.decimals);
		});
		for (var i = 0; i < assetArray.length; i++) {
			var asset = assetArray[i];
			var percentageAsset = NRS.calculatePercentage(asset.balanceQNT, asset.quantityQNT);
			rows += "<tr" + (asset.issued ? " class='asset_owner'" : "") + "><td><a href='#' data-goto-asset='" + NRS.escapeRespStr(asset.asset) + "'" + (asset.issued ? " style='font-weight:bold'" : "") + ">" + NRS.escapeRespStr(asset.name) + "</a></td><td class='quantity numeric'>" + NRS.formatQuantity(asset.balanceQNT, asset.decimals, false, quantityDecimals) + "</td><td class='numeric'>" + NRS.formatQuantity(asset.quantityQNT, asset.decimals, false, totalDecimals) + "</td><td>" + percentageAsset + "%</td></tr>";
		}

        var infoModalAssetsTable = $("#user_info_modal_assets_table");
        infoModalAssetsTable.find("tbody").empty().append(rows);
		NRS.dataLoadFinished(infoModalAssetsTable);
	};

	return NRS;
}(NRS || {}, jQuery));