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
var NRS = (function (NRS, $, undefined) {
    $('body').on("click", ".show_transaction_modal_action", function (e) {
        e.preventDefault();

        var transactionId = $(this).data("transaction");
        var sharedKey = $(this).data("sharedkey");
        var infoModal = $('#transaction_info_modal');
        var isModalVisible = false;
        if (infoModal && infoModal.data('bs.modal')) {
            isModalVisible = infoModal.data('bs.modal').isShown;
        }
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // the current modal
        }
        NRS.showTransactionModal(transactionId, isModalVisible, sharedKey);
    });

    NRS.showTransactionModal = function (transaction, isModalVisible, sharedKey) {
        if (NRS.fetchingModalData) {
            return;
        }

        NRS.fetchingModalData = true;

        $("#transaction_info_output_top, #transaction_info_output_bottom, #transaction_info_bottom").html("").hide();
        $("#transaction_info_callout").hide();
        var infoTable = $("#transaction_info_table");
        infoTable.hide();
        infoTable.find("tbody").empty();

        try {
            if (typeof transaction != "object") {
                NRS.sendRequest("getTransaction", {
                    "transaction": transaction,
                    "includePhasingResult": true
                }, function (response, input) {
                    response.transaction = input.transaction;
                    NRS.processTransactionModalData(response, isModalVisible, sharedKey);
                });
            } else {
                NRS.processTransactionModalData(transaction, isModalVisible, sharedKey);
            }
        } catch (e) {
            NRS.fetchingModalData = false;
            throw e;
        }
    };

    NRS.getPhasingDetails = function(phasingDetails, phasingParams) {
        var votingModel = NRS.getVotingModelName(parseInt(phasingParams.phasingVotingModel));
        phasingDetails.votingModel = $.t(votingModel);
        switch (votingModel) {
            case 'ASSET':
                NRS.sendRequest("getAsset", { "asset": phasingParams.phasingHolding }, function(response) {
                    phasingDetails.quorum = NRS.convertToQNTf(phasingParams.phasingQuorum, response.decimals);
                    phasingDetails.minBalance = NRS.convertToQNTf(phasingParams.phasingMinBalance, response.decimals);
                }, { isAsync: false });
                break;
            case 'CURRENCY':
                NRS.sendRequest("getCurrency", { "currency": phasingParams.phasingHolding }, function(response) {
                    phasingDetails.quorum = NRS.convertToQNTf(phasingParams.phasingQuorum, response.decimals);
                    phasingDetails.minBalance = NRS.convertToQNTf(phasingParams.phasingMinBalance, response.decimals);
                }, { isAsync: false });
                break;
            default:
                phasingDetails.quorum = phasingParams.phasingQuorum;
                phasingDetails.minBalance = phasingParams.phasingMinBalance;
        }
        var phasingTransactionLink = NRS.getTransactionLink(phasingParams.phasingHolding);
        if (NRS.constants.VOTING_MODELS[votingModel] == NRS.constants.VOTING_MODELS.ASSET) {
            phasingDetails.asset_formatted_html = phasingTransactionLink;
        } else if (NRS.constants.VOTING_MODELS[votingModel] == NRS.constants.VOTING_MODELS.CURRENCY) {
            phasingDetails.currency_formatted_html = phasingTransactionLink;
        }
        var minBalanceModel = NRS.getMinBalanceModelName(parseInt(phasingParams.phasingMinBalanceModel));
        phasingDetails.minBalanceModel = $.t(minBalanceModel);
        var rows = "";
        if (phasingParams.phasingWhitelist && phasingParams.phasingWhitelist.length > 0) {
            rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Account") + "</th>" +
                "</tr></thead><tbody>";
            for (var i = 0; i < phasingParams.phasingWhitelist.length; i++) {
                var account = NRS.convertNumericToRSAccountFormat(phasingParams.phasingWhitelist[i]);
                rows += "<tr><td><a href='#' data-user='" + NRS.escapeRespStr(account) + "' class='show_account_modal_action'>" + NRS.getAccountTitle(account) + "</a></td></tr>";
            }
            rows += "</tbody></table>";
        } else {
            rows = "-";
        }
        phasingDetails.whitelist_formatted_html = rows;
        if (phasingParams.phasingLinkedFullHashes && phasingParams.phasingLinkedFullHashes.length > 0) {
            rows = "<table class='table table-striped'><tbody>";
            for (i = 0; i < phasingParams.phasingLinkedFullHashes.length; i++) {
                rows += "<tr><td>" + phasingParams.phasingLinkedFullHashes[i] + "</td></tr>";
            }
            rows += "</tbody></table>";
        } else {
            rows = "-";
        }
        phasingDetails.full_hash_formatted_html = rows;
        if (phasingParams.phasingHashedSecret) {
            phasingDetails.hashedSecret = phasingParams.phasingHashedSecret;
            phasingDetails.hashAlgorithm = NRS.getHashAlgorithm(phasingParams.phasingHashedSecretAlgorithm);
        }
    };

    NRS.processTransactionModalData = function (transaction, isModalVisible, sharedKey) {
        NRS.setBackLink();
        NRS.modalStack.push({ class: "show_transaction_modal_action", key: "transaction", value: transaction.transaction });
        try {
            var async = false;

            var transactionDetails = $.extend({}, transaction);
            delete transactionDetails.attachment;
            if (transactionDetails.referencedTransaction == "0") {
                delete transactionDetails.referencedTransaction;
            }
            delete transactionDetails.transaction;

            if (!transactionDetails.confirmations) {
                transactionDetails.confirmations = "/";
            }
            if (!transactionDetails.block) {
                transactionDetails.block = "unconfirmed";
            }
            if (transactionDetails.timestamp) {
                transactionDetails.transactionTime = NRS.formatTimestamp(transactionDetails.timestamp);
            }
            if (transactionDetails.blockTimestamp) {
                transactionDetails.blockGenerationTime = NRS.formatTimestamp(transactionDetails.blockTimestamp);
            }
            if (transactionDetails.height == NRS.constants.MAX_INT_JAVA) {
                transactionDetails.height = "unknown";
            } else {
                transactionDetails.height_formatted_html = NRS.getBlockLink(transactionDetails.height);
                delete transactionDetails.height;
            }
            if (transactionDetails.executionHeight !== undefined) {
                transactionDetails.execution_height_formatted_html = NRS.getBlockLink(transactionDetails.executionHeight);
                delete transactionDetails.executionHeight;
            }
            $("#transaction_info_modal_transaction").html(NRS.escapeRespStr(transaction.transaction));

            $("#transaction_info_tab_link").tab("show");

            $("#transaction_info_details_table").find("tbody").empty().append(NRS.createInfoTable(transactionDetails, true));
            var infoTable = $("#transaction_info_table");
            infoTable.find("tbody").empty();

            var incorrect = false;
            if (transaction.senderRS == NRS.accountRS) {
                $("#transaction_info_modal_send_money").attr('disabled','disabled');
                $("#transaction_info_modal_transfer_currency").attr('disabled','disabled');
                $("#transaction_info_modal_send_message").attr('disabled','disabled');
            } else {
                $("#transaction_info_modal_send_money").removeAttr('disabled');
                $("#transaction_info_modal_transfer_currency").removeAttr('disabled');
                $("#transaction_info_modal_send_message").removeAttr('disabled');
            }
            var accountButton;
            if (transaction.senderRS in NRS.contacts) {
                accountButton = NRS.contacts[transaction.senderRS].name.escapeHTML();
                $("#transaction_info_modal_add_as_contact").attr('disabled','disabled');
            } else {
                accountButton = transaction.senderRS;
                $("#transaction_info_modal_add_as_contact").removeAttr('disabled');
            }
            var approveTransactionButton = $("#transaction_info_modal_approve_transaction");
            if (!transaction.attachment || !transaction.block ||
                !transaction.attachment.phasingFinishHeight ||
                transaction.attachment.phasingFinishHeight <= NRS.lastBlockHeight) {
                approveTransactionButton.attr('disabled', 'disabled');
            } else {
                approveTransactionButton.removeAttr('disabled');
                approveTransactionButton.data("transaction", transaction.transaction);
                approveTransactionButton.data("fullhash", transaction.fullHash);
                approveTransactionButton.data("timestamp", transaction.timestamp);
                approveTransactionButton.data("minBalanceFormatted", "");
                approveTransactionButton.data("votingmodel", transaction.attachment.phasingVotingModel);
            }
            var extendDataButton = $("#transaction_info_modal_extend_data");
            if (transaction.type == NRS.subtype.TaggedDataUpload.type && transaction.subtype == NRS.subtype.TaggedDataUpload.subtype) {
                extendDataButton.removeAttr('disabled');
                extendDataButton.data("transaction", transaction.transaction);
            } else {
                extendDataButton.attr('disabled','disabled');
            }

            $("#transaction_info_actions").show();
            $("#transaction_info_actions_tab").find("button").data("account", accountButton);

            if (transaction.attachment && transaction.attachment.phasingFinishHeight) {
                var finishHeight = transaction.attachment.phasingFinishHeight;
                var phasingDetails = {};
                phasingDetails.finishHeight = finishHeight;
                phasingDetails.finishIn = ((finishHeight - NRS.lastBlockHeight) > 0) ? (finishHeight - NRS.lastBlockHeight) + " " + $.t("blocks") : $.t("finished");
                NRS.getPhasingDetails(phasingDetails, transaction.attachment);
                $("#phasing_info_details_table").find("tbody").empty().append(NRS.createInfoTable(phasingDetails, true));
                $("#phasing_info_details_link").show();
            } else {
                $("#phasing_info_details_link").hide();
            }
            // TODO Someday I'd like to replace it with if (NRS.isOfType(transaction, "OrdinaryPayment"))
            var data;
            var message;
            var fieldsToDecrypt = {};
            var i;
            if (transaction.type == 0) {
                switch (transaction.subtype) {
                    case 0:
                        data = {
                            "type": $.t("ordinary_payment"),
                            "amount": transaction.amountNQT,
                            "fee": transaction.feeNQT,
                            "recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
                            "sender": transaction.senderRS ? transaction.senderRS : transaction.sender
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    default:
                        incorrect = true;
                        break;
                }
            } else if (transaction.type == 1) {
                switch (transaction.subtype) {
                    case 0:
                        var $output = $("#transaction_info_output_top");
                        if (transaction.attachment) {
                            if (transaction.attachment.message) {
                                if (!transaction.attachment["version.Message"] && !transaction.attachment["version.PrunablePlainMessage"]) {
                                    try {
                                        message = converters.hexStringToString(transaction.attachment.message);
                                    } catch (err) {
                                        //legacy
                                        if (transaction.attachment.message.indexOf("feff") === 0) {
                                            message = NRS.convertFromHex16(transaction.attachment.message);
                                        } else {
                                            message = NRS.convertFromHex8(transaction.attachment.message);
                                        }
                                    }
                                } else {
                                    if (transaction.attachment.messageIsText) {
                                        message = String(transaction.attachment.message);
                                    } else {
                                        message = $.t("binary_data");
                                    }
                                }
                                $output.html("<div style='color:#999999;padding-bottom:10px'><i class='fa fa-unlock'></i> " + $.t("public_message") + "</div><div style='padding-bottom:10px'>" + NRS.escapeRespStr(message).nl2br() + "</div>");
                            }

                            if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
                                $output.append("" +
                                    "<div id='transaction_info_decryption_form'></div>" +
                                    "<div id='transaction_info_decryption_output' style='display:none;padding-bottom:10px;'></div>"
                                );
                                if (transaction.attachment.encryptedMessage) {
                                    fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
                                }
                                if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
                                    fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
                                }
                                var options = {
                                    "noPadding": true,
                                    "formEl": "#transaction_info_decryption_form",
                                    "outputEl": "#transaction_info_decryption_output"
                                };
                                if (sharedKey) {
                                    options["sharedKey"] = sharedKey;
                                }
                                NRS.tryToDecrypt(transaction, fieldsToDecrypt, NRS.getAccountForDecryption(transaction), options);
                            }
                        } else {
                            $output.append("<div style='padding-bottom:10px'>" + $.t("message_empty") + "</div>");
                        }
                        var isCompressed = false;
                        if (transaction.attachment.encryptedMessage) {
                            isCompressed = transaction.attachment.encryptedMessage.isCompressed;
                        } else if (transaction.attachment.encryptToSelfMessage) {
                            isCompressed = transaction.attachment.encryptToSelfMessage.isCompressed;
                        }
                        var hash = transaction.attachment.messageHash || transaction.attachment.encryptedMessageHash;
                        var hashRow = hash ? ("<tr><td><strong>" + $.t("hash") + "</strong>:&nbsp;</td><td>" + hash + "</td></tr>") : "";
                        var downloadLink = "";
                        if (transaction.attachment.messageHash && !NRS.isTextMessage(transaction) && transaction.block) {
                            downloadLink = "<tr><td>" + NRS.getMessageDownloadLink(transaction.transaction, sharedKey) + "</td></tr>";
                        }
                        $output.append("<table>" +
                            "<tr><td><strong>" + $.t("from") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "sender") + "</td></tr>" +
                            "<tr><td><strong>" + $.t("to") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "recipient") + "</td></tr>" +
                            "<tr><td><strong>" + $.t("compressed") + "</strong>:&nbsp;</td><td>" + isCompressed + "</td></tr>" +
                            hashRow + downloadLink +
                        "</table>");
                        $output.show();
                        break;
                    case 1:
                        data = {
                            "type": $.t("alias_assignment"),
                            "alias": transaction.attachment.alias,
                            "data_formatted_html": transaction.attachment.uri.autoLink()
                        };
                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 2:
                        data = {
                            "type": $.t("poll_creation"),
                            "name": transaction.attachment.name,
                            "description": transaction.attachment.description,
                            "finish_height": transaction.attachment.finishHeight,
                            "min_number_of_options": transaction.attachment.minNumberOfOptions,
                            "max_number_of_options": transaction.attachment.maxNumberOfOptions,
                            "min_range_value": transaction.attachment.minRangeValue,
                            "max_range_value": transaction.attachment.maxRangeValue,
                            "min_balance": transaction.attachment.minBalance,
                            "min_balance_model": transaction.attachment.minBalanceModel
                        };

                        if (transaction.attachment.votingModel == -1) {
                            data["voting_model"] = $.t("vote_by_none");
                        } else if (transaction.attachment.votingModel == 0) {
                            data["voting_model"] = $.t("vote_by_account");
                        } else if (transaction.attachment.votingModel == 1) {
                            data["voting_model"] = $.t("vote_by_balance");
                        } else if (transaction.attachment.votingModel == 2) {
                            data["voting_model"] = $.t("vote_by_asset");
                            data["asset_id"] = transaction.attachment.holding;
                        } else if (transaction.attachment.votingModel == 3) {
                            data["voting_model"] = $.t("vote_by_currency");
                            data["currency_id"] = transaction.attachment.holding;
                        } else if (transaction.attachment.votingModel == 4) {
                            data["voting_model"] = $.t("vote_by_transaction");
                        } else if (transaction.attachment.votingModel == 5) {
                            data["voting_model"] = $.t("vote_by_hash");
                        } else {
                            data["voting_model"] = transaction.attachment.votingModel;
                        }


                        for (i = 0; i < transaction.attachment.options.length; i++) {
                            data["option_" + i] = transaction.attachment.options[i];
                        }

                        if (transaction.sender != NRS.account) {
                            data["sender"] = NRS.getAccountTitle(transaction, "sender");
                        }

                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 3:
                        var vote = "";
                        var votes = transaction.attachment.vote;
                        if (votes && votes.length > 0) {
                            for (i = 0; i < votes.length; i++) {
                                if (votes[i] == -128) {
                                    vote += "N/A";
                                } else {
                                    vote += votes[i];
                                }
                                if (i < votes.length - 1) {
                                    vote += " , ";
                                }
                            }
                        }
                        data = {
                            "type": $.t("vote_casting"),
                            "poll_formatted_html": NRS.getTransactionLink(transaction.attachment.poll),
                            "vote": vote
                        };
                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 4:
                        data = {
                            "type": $.t("hub_announcement")
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 5:
                        data = {
                            "type": $.t("account_info"),
                            "name": transaction.attachment.name,
                            "description": transaction.attachment.description
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 6:
                        var type = $.t("alias_sale");
                        if (transaction.attachment.priceNQT == "0") {
                            if (transaction.sender == transaction.recipient) {
                                type = $.t("alias_sale_cancellation");
                            } else {
                                type = $.t("alias_transfer");
                            }
                        }

                        data = {
                            "type": type,
                            "alias_name": transaction.attachment.alias
                        };

                        if (type == $.t("alias_sale")) {
                            data["price"] = transaction.attachment.priceNQT
                        }

                        if (type != $.t("alias_sale_cancellation")) {
                            data["recipient"] = transaction.recipientRS ? transaction.recipientRS : transaction.recipient;
                        }

                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;

                        if (type == $.t("alias_sale")) {
                            message = "";
                            var messageStyle = "info";

                            NRS.sendRequest("getAlias", {
                                "aliasName": transaction.attachment.alias
                            }, function (response) {
                                NRS.fetchingModalData = false;

                                if (!response.errorCode) {
                                    if (transaction.recipient != response.buyer || transaction.attachment.priceNQT != response.priceNQT) {
                                        message = $.t("alias_sale_info_outdated");
                                        messageStyle = "danger";
                                    } else if (transaction.recipient == NRS.account) {
                                        message = $.t("alias_sale_direct_offer", {
                                            "nxt": NRS.formatAmount(transaction.attachment.priceNQT)
                                        }) + " <a href='#' data-alias='" + NRS.escapeRespStr(transaction.attachment.alias) + "' data-toggle='modal' data-target='#buy_alias_modal'>" + $.t("buy_it_q") + "</a>";
                                    } else if (typeof transaction.recipient == "undefined") {
                                        message = $.t("alias_sale_indirect_offer", {
                                            "nxt": NRS.formatAmount(transaction.attachment.priceNQT)
                                        }) + " <a href='#' data-alias='" + NRS.escapeRespStr(transaction.attachment.alias) + "' data-toggle='modal' data-target='#buy_alias_modal'>" + $.t("buy_it_q") + "</a>";
                                    } else if (transaction.senderRS == NRS.accountRS) {
                                        if (transaction.attachment.priceNQT != "0") {
                                            message = $.t("your_alias_sale_offer") + " <a href='#' data-alias='" + NRS.escapeRespStr(transaction.attachment.alias) + "' data-toggle='modal' data-target='#cancel_alias_sale_modal'>" + $.t("cancel_sale_q") + "</a>";
                                        }
                                    } else {
                                        message = $.t("error_alias_sale_different_account");
                                    }
                                }
                            }, { isAsync: false });

                            if (message) {
                                $("#transaction_info_bottom").html("<div class='callout callout-bottom callout-" + messageStyle + "'>" + message + "</div>").show();
                            }
                        }

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 7:
                        data = {
                            "type": $.t("alias_buy"),
                            "alias_name": transaction.attachment.alias,
                            "price": transaction.amountNQT,
                            "recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
                            "sender": transaction.senderRS ? transaction.senderRS : transaction.sender
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 8:
                        data = {
                            "type": $.t("alias_deletion"),
                            "alias_name": transaction.attachment.alias,
                            "sender": transaction.senderRS ? transaction.senderRS : transaction.sender
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 9:
                        data = {
                            "type": $.t("transaction_approval")
                        };
                        for (i = 0; i < transaction.attachment.transactionFullHashes.length; i++) {
                            var transactionBytes = converters.hexStringToByteArray(transaction.attachment.transactionFullHashes[i]);
                            var transactionId = converters.byteArrayToBigInteger(transactionBytes, 0).toString().escapeHTML();
                            data["transaction" + (i + 1) + "_formatted_html"] =
                                NRS.getTransactionLink(transactionId);
                        }

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 10:
                        data = {
                            "type": $.t("set_account_property"),
                            "property": transaction.attachment.property,
                            "value": transaction.attachment.value
                        };
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 11:
                        data = {
                            "type": $.t("delete_account_property"),
                            "property_formatted_html": NRS.getTransactionLink(transaction.attachment.property)
                        };
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    default:
                        incorrect = true;
                        break;
                }
            } else if (transaction.type == 2) {
                switch (transaction.subtype) {
                    case 0:
                        NRS.sendRequest("getAsset", {
                            "asset": transaction.transaction
                        }, function (asset) {
                            data = {
                                "type": $.t("asset_issuance"),
                                "name": transaction.attachment.name,
                                "decimals": transaction.attachment.decimals,
                                "description": transaction.attachment.description
                            };
                            if (asset) {
                                data["initial_quantity"] = [asset.initialQuantityQNT, transaction.attachment.decimals];
                                data["quantity"] = [asset.quantityQNT, transaction.attachment.decimals];
                            }
                            data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                            $("#transaction_info_callout").html("<a href='#' data-goto-asset='" + NRS.escapeRespStr(transaction.transaction) + "'>Click here</a> to view this asset in the Asset Exchange.").show();

                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();
                        });

                        break;
                    case 1:
                        async = true;

                        NRS.sendRequest("getAsset", {
                            "asset": transaction.attachment.asset
                        }, function (asset) {
                            data = {
                                "type": $.t("asset_transfer"),
                                "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
                                "asset_name": asset.name,
                                "quantity": [transaction.attachment.quantityQNT, asset.decimals]
                            };

                            data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                            data["recipient"] = transaction.recipientRS ? transaction.recipientRS : transaction.recipient;
                            // Setting recipient to genesis to delete shares was allowed between v1.6 and v1.7
                            if (data.recipient == NRS.constants.GENESIS_RS) {
                                data.type = $.t("delete_asset_shares");
                            }
                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            $("#transaction_info_modal").modal("show");
                            NRS.fetchingModalData = false;
                        });

                        break;
                    case 2:
                    case 3:
                        async = true;
                        NRS.sendRequest("getAsset", {
                            "asset": transaction.attachment.asset
                        }, function (asset) {
                            NRS.formatAssetOrder(asset, transaction, isModalVisible)
                        });
                        break;
                    case 4:
                        async = true;

                        NRS.sendRequest("getTransaction", {
                            "transaction": transaction.attachment.order
                        }, function (transaction) {
                            if (transaction.attachment.asset) {
                                NRS.sendRequest("getAsset", {
                                    "asset": transaction.attachment.asset
                                }, function (asset) {
                                    data = {
                                        "type": $.t("ask_order_cancellation"),
                                        "order_formatted_html": NRS.getTransactionLink(transaction.transaction),
                                        "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
                                        "asset_name": asset.name,
                                        "quantity": [transaction.attachment.quantityQNT, asset.decimals],
                                        "price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " NXT",
                                        "total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " NXT"
                                    };
                                    data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                                    infoTable.find("tbody").append(NRS.createInfoTable(data));
                                    infoTable.show();
                                    $("#transaction_info_modal").modal("show");
                                    NRS.fetchingModalData = false;
                                });
                            } else {
                                NRS.fetchingModalData = false;
                            }
                        });
                        break;
                    case 5:
                        async = true;
                        NRS.sendRequest("getTransaction", {
                            "transaction": transaction.attachment.order
                        }, function (transaction) {
                            if (transaction.attachment.asset) {
                                NRS.sendRequest("getAsset", {
                                    "asset": transaction.attachment.asset
                                }, function (asset) {
                                    data = {
                                        "type": $.t("bid_order_cancellation"),
                                        "order_formatted_html": NRS.getTransactionLink(transaction.transaction),
                                        "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
                                        "asset_name": asset.name,
                                        "quantity": [transaction.attachment.quantityQNT, asset.decimals],
                                        "price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " NXT",
                                        "total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " NXT"
                                    };
                                    data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                                    infoTable.find("tbody").append(NRS.createInfoTable(data));
                                    infoTable.show();
                                    $("#transaction_info_modal").modal("show");
                                    NRS.fetchingModalData = false;
                                });
                            } else {
                                NRS.fetchingModalData = false;
                            }
                        });
                        break;
                    case 6:
                        async = true;

                        NRS.sendRequest("getTransaction", {
                            "transaction": transaction.transaction
                        }, function (transaction) {
                            if (transaction.attachment.asset) {
                                NRS.sendRequest("getAsset", {
                                    "asset": transaction.attachment.asset
                                }, function (asset) {
                                    data = {
                                        "type": $.t("dividend_payment"),
                                        "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
                                        "asset_name": asset.name,
                                        "amount_per_share": NRS.formatOrderPricePerWholeQNT(transaction.attachment.amountNQTPerQNT, asset.decimals) + " NXT",
                                        "height": transaction.attachment.height
                                    };
                                    data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                                    infoTable.find("tbody").append(NRS.createInfoTable(data));
                                    infoTable.show();

                                    $("#transaction_info_modal").modal("show");
                                    NRS.fetchingModalData = false;
                                });
                            } else {
                                NRS.fetchingModalData = false;
                            }
                        });
                        break;
                    case 7:
                        async = true;

                        NRS.sendRequest("getAsset", {
                            "asset": transaction.attachment.asset
                        }, function (asset) {
                            data = {
                                "type": $.t("delete_asset_shares"),
                                "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
                                "asset_name": asset.name,
                                "quantity": [transaction.attachment.quantityQNT, asset.decimals]
                            };

                            data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            $("#transaction_info_modal").modal("show");
                            NRS.fetchingModalData = false;
                        });
                        break;
                    default:
                        incorrect = true;
                        break;
                }
            } else if (transaction.type == 3) {
                switch (transaction.subtype) {
                    case 0:
                        data = {
                            "type": $.t("marketplace_listing"),
                            "name": transaction.attachment.name,
                            "description": transaction.attachment.description,
                            "price": transaction.attachment.priceNQT,
                            "quantity_formatted_html": NRS.format(transaction.attachment.quantity),
                            "seller": NRS.getAccountFormatted(transaction, "sender")
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        async = true;
                        NRS.sendRequest("getDGSGood", {
                            "goods": transaction.attachment.goods
                        }, function (goods) {
                            data = {
                                "type": $.t("marketplace_removal"),
                                "item_name": goods.name,
                                "seller": NRS.getAccountFormatted(goods, "seller")
                            };

                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            $("#transaction_info_modal").modal("show");
                            NRS.fetchingModalData = false;
                        });
                        break;
                    case 2:
                        async = true;
                        NRS.sendRequest("getDGSGood", {
                            "goods": transaction.attachment.goods
                        }, function (goods) {
                            data = {
                                "type": $.t("marketplace_item_price_change"),
                                "item_name": goods.name,
                                "new_price_formatted_html": NRS.formatAmount(transaction.attachment.priceNQT) + " NXT",
                                "seller": NRS.getAccountFormatted(goods, "seller")
                            };

                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            $("#transaction_info_modal").modal("show");
                            NRS.fetchingModalData = false;
                        });
                        break;
                    case 3:
                        async = true;
                        NRS.sendRequest("getDGSGood", {
                            "goods": transaction.attachment.goods
                        }, function (goods) {
                            data = {
                                "type": $.t("marketplace_item_quantity_change"),
                                "item_name": goods.name,
                                "delta_quantity": transaction.attachment.deltaQuantity,
                                "seller": NRS.getAccountFormatted(goods, "seller")
                            };

                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            $("#transaction_info_modal").modal("show");
                            NRS.fetchingModalData = false;
                        });
                        break;
                    case 4:
                        async = true;
                        NRS.sendRequest("getDGSGood", {
                            "goods": transaction.attachment.goods
                        }, function (goods) {
                            data = {
                                "type": $.t("marketplace_purchase"),
                                "item_name": goods.name,
                                "price": transaction.attachment.priceNQT,
                                "quantity_formatted_html": NRS.format(transaction.attachment.quantity),
                                "buyer": NRS.getAccountFormatted(transaction, "sender"),
                                "seller": NRS.getAccountFormatted(goods, "seller")
                            };

                            infoTable.find("tbody").append(NRS.createInfoTable(data));
                            infoTable.show();

                            NRS.sendRequest("getDGSPurchase", {
                                "purchase": transaction.transaction
                            }, function (purchase) {
                                var callout = "";
                                if (purchase.errorCode) {
                                    if (purchase.errorCode == 4) {
                                        if (transactionDetails.block == "unconfirmed") {
                                            callout = $.t("unconfirmed_transaction");
                                        } else {
                                            callout = $.t("incorrect_purchase");
                                        }
                                    } else {
                                        callout = NRS.escapeRespStr(purchase.errorDescription);
                                    }
                                } else {
                                    if (NRS.account == transaction.recipient || NRS.account == transaction.sender) {
                                        if (purchase.pending) {
                                            if (NRS.account == transaction.recipient) {
                                                callout = "<a href='#' data-toggle='modal' data-target='#dgs_delivery_modal' data-purchase='" + NRS.escapeRespStr(transaction.transaction) + "'>" + $.t("deliver_goods_q") + "</a>";
                                            } else {
                                                callout = $.t("waiting_on_seller");
                                            }
                                        } else {
                                            if (purchase.refundNQT) {
                                                callout = $.t("purchase_refunded");
                                            } else {
                                                callout = $.t("purchase_delivered");
                                            }
                                        }
                                    }
                                }
                                if (callout) {
                                    $("#transaction_info_bottom").html("<div class='callout " + (purchase.errorCode ? "callout-danger" : "callout-info") + " callout-bottom'>" + callout + "</div>").show();
                                }
                                $("#transaction_info_modal").modal("show");
                                NRS.fetchingModalData = false;
                            });
                        });
                        break;
                    case 5:
                        async = true;
                        NRS.sendRequest("getDGSPurchase", {
                            "purchase": transaction.attachment.purchase
                        }, function (purchase) {
                            NRS.sendRequest("getDGSGood", {
                                "goods": purchase.goods
                            }, function (goods) {
                                data = {
                                    "type": $.t("marketplace_delivery"),
                                    "item_name": goods.name,
                                    "price": purchase.priceNQT
                                };

                                data["quantity_formatted_html"] = NRS.format(purchase.quantity);

                                if (purchase.quantity != "1") {
                                    var orderTotal = NRS.formatAmount(new BigInteger(String(purchase.quantity)).multiply(new BigInteger(String(purchase.priceNQT))));
                                    data["total_formatted_html"] = orderTotal + " NXT";
                                }

                                if (transaction.attachment.discountNQT) {
                                    data["discount"] = transaction.attachment.discountNQT;
                                }

                                data["buyer"] = NRS.getAccountFormatted(purchase, "buyer");
                                data["seller"] = NRS.getAccountFormatted(purchase, "seller");

                                if (transaction.attachment.goodsData) {
                                    NRS.tryToDecrypt(transaction, {
                                        "goodsData": {
                                            "title": $.t("data"),
                                            "nonce": "goodsNonce"
                                        }
                                    }, NRS.getAccountForDecryption(purchase, "buyer", "seller"));
                                }

                                infoTable.find("tbody").append(NRS.createInfoTable(data));
                                infoTable.show();

                                var callout;

                                if (NRS.account == purchase.buyer) {
                                    if (purchase.refundNQT) {
                                        callout = $.t("purchase_refunded");
                                    } else if (!purchase.feedbackNote) {
                                        callout = $.t("goods_received") + " <a href='#' data-toggle='modal' data-target='#dgs_feedback_modal' data-purchase='" + NRS.escapeRespStr(transaction.attachment.purchase) + "'>" + $.t("give_feedback_q") + "</a>";
                                    }
                                } else if (NRS.account == purchase.seller && purchase.refundNQT) {
                                    callout = $.t("purchase_refunded");
                                }

                                if (callout) {
                                    $("#transaction_info_bottom").append("<div class='callout callout-info callout-bottom'>" + callout + "</div>").show();
                                }

                                $("#transaction_info_modal").modal("show");
                                NRS.fetchingModalData = false;
                            });
                        });
                        break;
                    case 6:
                        async = true;
                        NRS.sendRequest("getDGSPurchase", {
                            "purchase": transaction.attachment.purchase
                        }, function (purchase) {
                            NRS.sendRequest("getDGSGood", {
                                "goods": purchase.goods
                            }, function (goods) {
                                data = {
                                    "type": $.t("marketplace_feedback"),
                                    "item_name": goods.name,
                                    "buyer": NRS.getAccountFormatted(purchase, "buyer"),
                                    "seller": NRS.getAccountFormatted(purchase, "seller")
                                };

                                infoTable.find("tbody").append(NRS.createInfoTable(data));
                                infoTable.show();

                                if (purchase.seller == NRS.account || purchase.buyer == NRS.account) {
                                    NRS.sendRequest("getDGSPurchase", {
                                        "purchase": transaction.attachment.purchase
                                    }, function (purchase) {
                                        var callout;
                                        if (purchase.buyer == NRS.account) {
                                            if (purchase.refundNQT) {
                                                callout = $.t("purchase_refunded");
                                            }
                                        } else {
                                            if (!purchase.refundNQT) {
                                                callout = "<a href='#' data-toggle='modal' data-target='#dgs_refund_modal' data-purchase='" + NRS.escapeRespStr(transaction.attachment.purchase) + "'>" + $.t("refund_this_purchase_q") + "</a>";
                                            } else {
                                                callout = $.t("purchase_refunded");
                                            }
                                        }
                                        if (callout) {
                                            $("#transaction_info_bottom").append("<div class='callout callout-info callout-bottom'>" + callout + "</div>").show();
                                        }
                                        $("#transaction_info_modal").modal("show");
                                        NRS.fetchingModalData = false;
                                    });
                                } else {
                                    $("#transaction_info_modal").modal("show");
                                    NRS.fetchingModalData = false;
                                }
                            });
                        });
                        break;
                    case 7:
                        async = true;
                        NRS.sendRequest("getDGSPurchase", {
                            "purchase": transaction.attachment.purchase
                        }, function (purchase) {
                            NRS.sendRequest("getDGSGood", {
                                "goods": purchase.goods
                            }, function (goods) {
                                data = {
                                    "type": $.t("marketplace_refund"),
                                    "item_name": goods.name
                                };
                                var orderTotal = new BigInteger(String(purchase.quantity)).multiply(new BigInteger(String(purchase.priceNQT)));
                                data["order_total_formatted_html"] = NRS.formatAmount(orderTotal) + " NXT";
                                data["refund"] = transaction.attachment.refundNQT;
                                data["buyer"] = NRS.getAccountFormatted(purchase, "buyer");
                                data["seller"] = NRS.getAccountFormatted(purchase, "seller");
                                infoTable.find("tbody").append(NRS.createInfoTable(data));
                                infoTable.show();
                                $("#transaction_info_modal").modal("show");
                                NRS.fetchingModalData = false;
                            });
                        });
                        break;
                    default:
                        incorrect = true;
                        break
                }
            } else if (transaction.type == 4) {
                switch (transaction.subtype) {
                    case 0:
                        data = {
                            "type": $.t("balance_leasing"),
                            "period": transaction.attachment.period,
                            "lessee": transaction.recipientRS ? transaction.recipientRS : transaction.recipient
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        data = {
                            "type": $.t("phasing_only")
                        };

                        NRS.getPhasingDetails(data, transaction.attachment.phasingControlParams);

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;

                    default:
                        incorrect = true;
                        break;
                }
            }
            else if (transaction.type == 5) {
                async = true;
                var currency = null;
                var id = (transaction.subtype == 0 ? transaction.transaction : transaction.attachment.currency);
                NRS.sendRequest("getCurrency", {
                    "currency": id
                }, function (response) {
                    if (!response.errorCode) {
                        currency = response;
                    }
                }, { isAsync: false });

                switch (transaction.subtype) {
                    case 0:
                        var minReservePerUnitNQT = new BigInteger(transaction.attachment.minReservePerUnitNQT).multiply(new BigInteger("" + Math.pow(10, transaction.attachment.decimals)));
                        var currencyTypes = "";
                        for (var currencyType in NRS.constants.CURRENCY_TYPES) {
                            if (!NRS.constants.CURRENCY_TYPES.hasOwnProperty(currencyType)) {
                                continue;
                            }
                            //noinspection JSBitwiseOperatorUsage
                            if (transaction.attachment.type & NRS.constants.CURRENCY_TYPES[currencyType]) {
                                currencyTypes += currencyType + ",";
                            }
                        }
                        currencyTypes = transaction.attachment.type + ":" + currencyTypes.slice(0, -1);
                        var hashAlgorithm = transaction.attachment.algorithm;
                        for (var algorithm in NRS.constants.MINTING_HASH_ALGORITHMS) {
                            if (!NRS.constants.MINTING_HASH_ALGORITHMS.hasOwnProperty(algorithm)) {
                                continue;
                            }
                            if (transaction.attachment.algorithm == NRS.constants.MINTING_HASH_ALGORITHMS[algorithm]) {
                                hashAlgorithm = hashAlgorithm + ":" + algorithm;
                                break;
                            }
                        }

                        data = {
                            "type": $.t("currency_issuance"),
                            "name": transaction.attachment.name,
                            "code": transaction.attachment.code,
                            "currency_type": currencyTypes,
                            "description_formatted_html": transaction.attachment.description.autoLink(),
                            "initial_units": [transaction.attachment.initialSupply, transaction.attachment.decimals],
                            "reserve_units": [transaction.attachment.reserveSupply, transaction.attachment.decimals],
                            "max_units": [transaction.attachment.maxSupply, transaction.attachment.decimals],
                            "decimals": transaction.attachment.decimals,
                            "issuance_height": transaction.attachment.issuanceHeight,
                            "min_reserve_per_unit_formatted_html": NRS.formatAmount(minReservePerUnitNQT) + " NXT",
                            "minDifficulty": transaction.attachment.minDifficulty,
                            "maxDifficulty": transaction.attachment.maxDifficulty,
                            "algorithm": hashAlgorithm
                        };
                        if (currency) {
                            data["current_units"] = NRS.convertToQNTf(currency.currentSupply, currency.decimals);
                            var currentReservePerUnitNQT = new BigInteger(currency.currentReservePerUnitNQT).multiply(new BigInteger("" + Math.pow(10, currency.decimals)));
                            data["current_reserve_per_unit_formatted_html"] = NRS.formatAmount(currentReservePerUnitNQT) + " NXT";
                        } else {
                            data["status"] = "Currency Deleted or not Issued";
                        }
                        break;
                    case 1:
                        if (currency) {
                            var amountPerUnitNQT = new BigInteger(transaction.attachment.amountPerUnitNQT).multiply(new BigInteger("" + Math.pow(10, currency.decimals)));
                            var resSupply = currency.reserveSupply;
                            data = {
                                "type": $.t("reserve_increase"),
                                "code": currency.code,
                                "reserve_units": [resSupply, currency.decimals],
                                "amount_per_unit_formatted_html": NRS.formatAmount(amountPerUnitNQT) + " NXT",
                                "reserved_amount_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(amountPerUnitNQT, NRS.convertToQNTf(resSupply, currency.decimals))) + " NXT"
                            };
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 2:
                        if (currency) {
                            var reservePerUnitNQT = new BigInteger(currency.currentReservePerUnitNQT).multiply(new BigInteger("" + Math.pow(10, currency.decimals)));
                            data = {
                                "type": $.t("reserve_claim"),
                                "code": currency.code,
                                "units": [transaction.attachment.units, currency.decimals],
                                "claimed_amount_formatted_html": NRS.formatAmount(NRS.convertToQNTf(NRS.calculateOrderTotalNQT(reservePerUnitNQT, transaction.attachment.units), currency.decimals)) + " NXT"
                            };
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 3:
                        if (currency) {
                            data = {
                                "type": $.t("currency_transfer"),
                                "code": currency.code,
                                "units": [transaction.attachment.units, currency.decimals]
                            };
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 4:
                        if (currency) {
                            data = NRS.formatCurrencyOffer(currency, transaction);
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 5:
                        if (currency) {
                            data = NRS.formatCurrencyExchange(currency, transaction, "buy");
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 6:
                        if (currency) {
                            data = NRS.formatCurrencyExchange(currency, transaction, "sell");
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 7:
                        if (currency) {
                            data = {
                                "type": $.t("mint_currency"),
                                "code": currency.code,
                                "units": [transaction.attachment.units, currency.decimals],
                                "counter": transaction.attachment.counter,
                                "nonce": transaction.attachment.nonce
                            };
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    case 8:
                        if (currency) {
                            data = {
                                "type": $.t("delete_currency"),
                                "code": currency.code
                            };
                        } else {
                            data = NRS.getUnknownCurrencyData(transaction);
                        }
                        break;
                    default:
                        incorrect = true;
                        break;
                }
                if (!incorrect) {
                    if (transaction.sender != NRS.account) {
                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                    }
                    var infoCallout = $("#transaction_info_callout");
                    infoCallout.html("");
                    if (currency != null && NRS.isExchangeable(currency.type)) {
                        infoCallout.append("<a href='#' data-goto-currency='" + NRS.escapeRespStr(currency.code) + "'>" + $.t('exchange_booth') + "</a><br/>");
                    }
                    if (currency != null && NRS.isReservable(currency.type)) {
                        infoCallout.append("<a href='#' data-toggle='modal' data-target='#currency_founders_modal' data-currency='" + NRS.escapeRespStr(currency.currency) + "' data-name='" + NRS.escapeRespStr(currency.name) + "' data-code='" + NRS.escapeRespStr(currency.code) + "' data-ressupply='" + NRS.escapeRespStr(currency.reserveSupply) + "' data-initialsupply='" + NRS.escapeRespStr(currency.initialSupply) + "' data-decimals='" + NRS.escapeRespStr(currency.decimals) + "' data-minreserve='" + NRS.escapeRespStr(currency.minReservePerUnitNQT) + "' data-issueheight='" + NRS.escapeRespStr(currency.issuanceHeight) + "'>View Founders</a><br/>");
                    }
                    if (currency != null) {
                        infoCallout.append("<a href='#' data-toggle='modal' data-target='#currency_distribution_modal' data-code='" + NRS.escapeRespStr(currency.code) + "'  data-i18n='Currency Distribution'>Currency Distribution</a>");
                    }
                    infoCallout.show();

                    infoTable.find("tbody").append(NRS.createInfoTable(data));
                    infoTable.show();

                    if (!isModalVisible) {
                        $("#transaction_info_modal").modal("show");
                    }
                    NRS.fetchingModalData = false;
                }
            } else if (transaction.type == 6) {
                switch (transaction.subtype) {
                    case 0:
                        data = NRS.getTaggedData(transaction.attachment, 0, transaction);
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        data = NRS.getTaggedData(transaction.attachment, 1, transaction);
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;

                    default:
                        incorrect = true;
                        break;
                }
            } else if (NRS.isOfType(transaction, "ShufflingCreation")) {
                data = {
                    "type": $.t("shuffling_creation"),
                    "period": transaction.attachment.registrationPeriod,
                    "holdingType": transaction.attachment.holdingType
                };
                if (transaction.attachment.holding != "0") {
                    var requestType;
                    if (data.holdingType == 1) {
                        requestType = "getAsset";
                    } else if (data.holdingType == 2) {
                        requestType = "getCurrency";
                    }
                    NRS.sendRequest(requestType, {"currency": transaction.attachment.holding, "asset": transaction.attachment.holding}, function (response) {
                        data.holding_formatted_html = NRS.getTransactionLink(transaction.attachment.holding);
                        data.amount_formatted_html = NRS.convertToQNTf(transaction.attachment.amount, response.decimals);
                    }, { isAsync: false });
                } else {
                    data.amount = transaction.attachment.amount;
                }
                NRS.sendRequest("getShufflingParticipants", { "shuffling": transaction.transaction }, function (response) {
                    if (response.participants && response.participants.length > 0) {
                        var rows = "<table class='table table-striped'><thead><tr>" +
                        "<th>" + $.t("participant") + "</th>" +
                        "<th>" + $.t("state") + "</th>" +
                        "<tr></thead><tbody>";
                        for (i = 0; i < response.participants.length; i++) {
                            var participant = response.participants[i];
                            rows += "<tr>" +
                            "<td>" + NRS.getAccountLink(participant, "account") + "<td>" +
                            "<td>" + $.t(NRS.getShufflingParticipantState(participant.state).toLowerCase()) + "</td>" +
                            "</tr>";
                        }
                        rows += "</tbody></table>";
                        data["participants_formatted_html"] = rows;
                    } else {
                        data["participants"] = $.t("no_matching_participants");
                    }
                }, { isAsync: false });
                NRS.sendRequest("getShufflers", {
                    "shufflingFullHash": transaction.fullHash,
                    "account": NRS.accountRS,
                    "adminPassword": NRS.getAdminPassword()
                }, function (response) {
                    if (response.shufflers && response.shufflers.length > 0) {
                        var shuffler = response.shufflers[0];
                        data["shuffler"] = "running";
                        data["shufflerRecipient_formatted_html"] = NRS.getAccountLink(shuffler, "recipient");
                        if (shuffler.failedTransaction) {
                            data["failedTransaction_formatted_html"] = NRS.getTransactionLink(shuffler.recipient);
                            data["failureCause"] = shuffler.failureCause;
                        }
                    } else {
                        if (response.errorCode) {
                            data["shuffler"] = $.t("unknown");
                        } else {
                            data["shuffler"] = $.t("not_started");
                        }
                    }
                }, { isAsync: false });
                NRS.sendRequest("getShuffling", {
                    "shuffling": transaction.transaction
                }, function (response) {
                    if (response.shuffling) {
                        data["stage_formatted_html"] = NRS.getShufflingStage(response.stage);
                        data["count"] = response.registrantCount + " / " + response.participantCount;
                        data["blocksRemaining"] = response.blocksRemaining;
                        data["issuer_formatted_html"] = NRS.getAccountLink(response, "issuer");
                        if (response.assignee) {
                            data["assignee_formatted_html"] = NRS.getAccountLink(response, "assignee");
                        }
                        data["shufflingStateHash"] = response.shufflingStateHash;
                        if (response.recipientPublicKeys && response.recipientPublicKeys.length > 0) {
                            data["recipients_formatted_html"] = listPublicKeys(response.recipientPublicKeys);
                        }
                    }
                }, { isAsync: false });
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            } else if (NRS.isOfType(transaction, "ShufflingRegistration")) {
                data = { "type": $.t("shuffling_registration") };
                NRS.mergeMaps(transaction.attachment, data, { "version.ShufflingRegistration": true });
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            } else if (NRS.isOfType(transaction, "ShufflingProcessing")) {
                data = { "type": $.t("shuffling_processing") };
                NRS.mergeMaps(transaction.attachment, data, { "version.ShufflingProcessing": true });
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            } else if (NRS.isOfType(transaction, "ShufflingRecipients")) {
                data = {
                    "type": $.t("shuffling_recipients"),
                    "shuffling_state_hash": transaction.attachment.shufflingStateHash
                };
                data["shuffling_formatted_html"] = NRS.getTransactionLink(transaction.attachment.shuffling);
                data["recipients_formatted_html"] = listPublicKeys(transaction.attachment.recipientPublicKeys);
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            } else if (NRS.isOfType(transaction, "ShufflingVerification")) {
                data = { "type": $.t("shuffling_verification") };
                NRS.mergeMaps(transaction.attachment, data, { "version.ShufflingVerification": true });
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            } else if (NRS.isOfType(transaction, "ShufflingCancellation")) {
                data = { "type": $.t("shuffling_cancellation") };
                NRS.mergeMaps(transaction.attachment, data, { "version.ShufflingCancellation": true });
                infoTable.find("tbody").append(NRS.createInfoTable(data));
                infoTable.show();
            }
            if (!(transaction.type == 1 && transaction.subtype == 0)) {
                if (transaction.attachment) {
                    var transactionInfoOutputBottom = $("#transaction_info_output_bottom");
                    if (transaction.attachment.message) {
                        if (!transaction.attachment["version.Message"] && !transaction.attachment["version.PrunablePlainMessage"]) {
                            try {
                                message = converters.hexStringToString(transaction.attachment.message);
                            } catch (err) {
                                //legacy
                                if (transaction.attachment.message.indexOf("feff") === 0) {
                                    message = NRS.convertFromHex16(transaction.attachment.message);
                                } else {
                                    message = NRS.convertFromHex8(transaction.attachment.message);
                                }
                            }
                        } else {
                            if (NRS.isTextMessage(transaction)) {
                                message = String(transaction.attachment.message);
                            } else {
                                message = $.t("binary_data")
                            }
                        }

                        transactionInfoOutputBottom.append("<div style='padding-left:5px;'><label><i class='fa fa-unlock'></i> " + $.t("public_message") + "</label><div>" + NRS.escapeRespStr(message).nl2br() + "</div></div>");
                    }

                    if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
                        var account;
                        if (transaction.attachment.message) {
                            transactionInfoOutputBottom.append("<div style='height:5px'></div>");
                        }
                        if (transaction.attachment.encryptedMessage) {
                            fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
                            account = NRS.getAccountForDecryption(transaction);
                        }
                        if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
                            fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
                            if (!account) {
                                account = transaction.sender;
                            }
                        }
                        NRS.tryToDecrypt(transaction, fieldsToDecrypt, account, {
                            "formEl": "#transaction_info_output_bottom",
                            "outputEl": "#transaction_info_output_bottom"
                        });
                    }

                    transactionInfoOutputBottom.show();
                }
            }

            if (incorrect) {
                $.growl($.t("error_unknown_transaction_type"), {
                    "type": "danger"
                });

                NRS.fetchingModalData = false;
                return;
            }

            if (!async) {
                if (!isModalVisible) {
                    $("#transaction_info_modal").modal("show");
                }
                NRS.fetchingModalData = false;
            }
        } catch (e) {
            NRS.fetchingModalData = false;
            throw e;
        }
    };

    NRS.formatAssetOrder = function (asset, transaction, isModalVisible) {
        var data = {
            "type": (transaction.subtype == 2 ? $.t("ask_order_placement") : $.t("bid_order_placement")),
            "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
            "asset_name": asset.name,
            "quantity": [transaction.attachment.quantityQNT, asset.decimals],
            "price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " NXT",
            "total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " NXT"
        };
        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
        var rows = "";
        var params;
        if (transaction.subtype == 2) {
            params = {"askOrder": transaction.transaction};
        } else {
            params = {"bidOrder": transaction.transaction};
        }
        var transactionField = (transaction.subtype == 2 ? "bidOrder" : "askOrder");
        NRS.sendRequest("getOrderTrades", params, function (response) {
            var tradeQuantity = BigInteger.ZERO;
            var tradeTotal = BigInteger.ZERO;
            if (response.trades && response.trades.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Quantity") + "</th>" +
                "<th>" + $.t("Price") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.trades.length; i++) {
                    var trade = response.trades[i];
                    tradeQuantity = tradeQuantity.add(new BigInteger(trade.quantityQNT));
                    tradeTotal = tradeTotal.add(new BigInteger(trade.quantityQNT).multiply(new BigInteger(trade.priceNQT)));
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(trade[transactionField], NRS.formatTimestamp(trade.timestamp)) + "</td>" +
                    "<td>" + NRS.formatQuantity(trade.quantityQNT, asset.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(trade.priceNQT, asset.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(trade.quantityQNT, trade.priceNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["trades_formatted_html"] = rows;
            } else {
                data["trades"] = $.t("no_matching_trade");
            }
            data["quantity_traded"] = [tradeQuantity, asset.decimals];
            data["total_traded"] = NRS.formatAmount(tradeTotal, false, true) + " NXT";
        }, { isAsync: false });

        var infoTable = $("#transaction_info_table");
        infoTable.find("tbody").append(NRS.createInfoTable(data));
        infoTable.show();
        if (!isModalVisible) {
            $("#transaction_info_modal").modal("show");
        }
        NRS.fetchingModalData = false;
    };

    NRS.formatCurrencyExchange = function (currency, transaction, type) {
        var rateUnitsStr = " [NXT " + $.t("per") + " " + currency.code +"]";
        var data = {
            "type": type == "sell" ? $.t("sell_currency") : $.t("buy_currency"),
            "code": currency.code,
            "units": [transaction.attachment.units, currency.decimals],
            "rate": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.rateNQT, currency.decimals) + rateUnitsStr
        };
        var rows = "";
        NRS.sendRequest("getExchangesByExchangeRequest", {
            "transaction": transaction.transaction
        }, function (response) {
            var exchangedUnits = BigInteger.ZERO;
            var exchangedTotal = BigInteger.ZERO;
            if (response.exchanges && response.exchanges.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Units") + "</th>" +
                "<th>" + $.t("Rate") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.exchanges.length; i++) {
                    var exchange = response.exchanges[i];
                    exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
                    exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(exchange.offer, NRS.formatTimestamp(exchange.timestamp)) + "</td>" +
                    "<td>" + NRS.formatQuantity(exchange.units, currency.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, currency.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["exchanges_formatted_html"] = rows;
            } else {
                data["exchanges"] = $.t("no_matching_exchange_offer");
            }
            data["units_exchanged"] = [exchangedUnits, currency.decimals];
            data["total_exchanged"] = NRS.formatAmount(exchangedTotal, false, true) + " [NXT]";
        }, { isAsync: false });
        return data;
    };

    NRS.formatCurrencyOffer = function (currency, transaction) {
        var rateUnitsStr = " [NXT " + $.t("per") + " " + currency.code +"]";
        var buyOffer;
        var sellOffer;
        NRS.sendRequest("getOffer", {
            "offer": transaction.transaction
        }, function (response) {
            buyOffer = response.buyOffer;
            sellOffer = response.sellOffer;
        }, { isAsync: false });
        var data = {
            "type": $.t("exchange_offer"),
            "code": currency.code,
            "expiration_height": transaction.attachment.expirationHeight
        };
        if (buyOffer && sellOffer) {
            data["buy_supply_formatted_html"] = NRS.formatQuantity(buyOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialBuySupply, currency.decimals) + ")";
            data["buy_limit_formatted_html"] = NRS.formatQuantity(buyOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalBuyLimit, currency.decimals) + ")";
            data["buy_rate_formatted_html"] = NRS.calculateOrderPricePerWholeQNT(transaction.attachment.buyRateNQT, currency.decimals) + rateUnitsStr;
            data["sell_supply_formatted_html"] = NRS.formatQuantity(sellOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialSellSupply, currency.decimals) + ")";
            data["sell_limit_formatted_html"] = NRS.formatQuantity(sellOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalSellLimit, currency.decimals) + ")";
            data["sell_rate_formatted_html"] = NRS.calculateOrderPricePerWholeQNT(transaction.attachment.sellRateNQT, currency.decimals) + rateUnitsStr;
        }
        var rows = "";
        NRS.sendRequest("getExchangesByOffer", {
            "offer": transaction.transaction
        }, function (response) {
            var exchangedUnits = BigInteger.ZERO;
            var exchangedTotal = BigInteger.ZERO;
            if (response.exchanges && response.exchanges.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Type") + "</th>" +
                "<th>" + $.t("Units") + "</th>" +
                "<th>" + $.t("Rate") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.exchanges.length; i++) {
                    var exchange = response.exchanges[i];
                    exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
                    exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
                    var exchangeType = exchange.seller == transaction.sender ? "Buy" : "Sell";
                    if (exchange.seller == exchange.buyer) {
                        exchangeType = "Same";
                    }
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(exchange.transaction, NRS.formatTimestamp(exchange.timestamp)) + "</td>" +
                    "<td>" + exchangeType + "</td>" +
                    "<td>" + NRS.formatQuantity(exchange.units, currency.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, currency.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["exchanges_formatted_html"] = rows;
            } else {
                data["exchanges"] = $.t("no_matching_exchange_request");
            }
            data["units_exchanged"] = [exchangedUnits, currency.decimals];
            data["total_exchanged"] = NRS.formatAmount(exchangedTotal, false, true) + " [NXT]";
        }, { isAsync: false });
        return data;
    };

    NRS.getUnknownCurrencyData = function (transaction) {
        if (!transaction) {
            return {};
        }
        return {
            "status": "Currency Deleted or not Issued",
            "type": transaction.type,
            "subType": transaction.subtype
        };
    };

    NRS.getTaggedData = function (attachment, subtype, transaction) {
        var data = {
            "type": $.t(NRS.transactionTypes[6].subTypes[subtype].i18nKeyTitle)
        };
        if (attachment.hash) {
            data["hash"] = attachment.hash;
        }
        if (attachment.taggedData) {
            data["tagged_data_formatted_html"] = NRS.getTransactionLink(attachment.taggedData);
            transaction = attachment.taggedData;
        }
        if (attachment.data) {
            data["name"] = attachment.name;
            data["description"] = attachment.description;
            data["tags"] = attachment.tags;
            data["mime_type"] = attachment.type;
            data["channel"] = attachment.channel;
            data["is_text"] = attachment.isText;
            data["filename"] = attachment.filename;
            if (attachment.isText) {
                data["data_size"] = NRS.getUtf8Bytes(attachment.data).length;
            } else {
                data["data_size"] = converters.hexStringToByteArray(attachment.data).length;
            }
        }
        if (transaction.block) {
            data["link_formatted_html"] = NRS.getTaggedDataLink(transaction.transaction, attachment.isText);
        }
        return data;
    };

    function listPublicKeys(publicKeys) {
        var rows = "<table class='table table-striped'><tbody>";
        for (var i = 0; i < publicKeys.length; i++) {
            var recipientPublicKey = publicKeys[i];
            var recipientAccount = {accountRS: NRS.getAccountIdFromPublicKey(recipientPublicKey, true)};
            rows += "<tr>" +
                "<td>" + NRS.getAccountLink(recipientAccount, "account") + "<td>" +
                "</tr>";
        }
        rows += "</tbody></table>";
        return rows;
    }

    $(document).on("click", ".approve_transaction_btn", function (e) {
        e.preventDefault();
        var approveTransactionModal = $('#approve_transaction_modal');
        approveTransactionModal.find('.at_transaction_full_hash_display').text($(this).data("transaction"));
        approveTransactionModal.find('.at_transaction_timestamp').text(NRS.formatTimestamp($(this).data("timestamp")));
        $("#approve_transaction_button").data("transaction", $(this).data("transaction"));
        approveTransactionModal.find('#at_transaction_full_hash').val($(this).data("fullhash"));

        var mbFormatted = $(this).data("minBalanceFormatted");
        var minBalanceWarning = $('#at_min_balance_warning');
        if (mbFormatted && mbFormatted != "") {
            minBalanceWarning.find('.at_min_balance_amount').html(mbFormatted);
            minBalanceWarning.show();
        } else {
            minBalanceWarning.hide();
        }
        var revealSecretDiv = $("#at_revealed_secret_div");
        if ($(this).data("votingmodel") == NRS.constants.VOTING_MODELS.HASH) {
            revealSecretDiv.show();
        } else {
            revealSecretDiv.hide();
        }
    });

    $("#approve_transaction_button").on("click", function () {
        $('.tr_transaction_' + $(this).data("transaction") + ':visible .approve_transaction_btn').attr('disabled', true);
    });

    $("#transaction_info_modal").on("hide.bs.modal", function () {
        NRS.removeDecryptionForm($(this));
        $("#transaction_info_output_bottom, #transaction_info_output_top, #transaction_info_bottom").html("").hide();
    });

    return NRS;
}(NRS || {}, jQuery));
