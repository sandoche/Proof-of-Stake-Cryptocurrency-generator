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
	$("body").on("click", ".show_block_modal_action", function(event) {
		event.preventDefault();
		if (NRS.fetchingModalData) {
			return;
		}
		NRS.fetchingModalData = true;
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // The current modal
        }
		var block = $(this).data("block");
        var isBlockId = $(this).data("id");
        var params = {
            "includeTransactions": "true",
            "includeExecutedPhased": "true"
        };
        if (isBlockId) {
            params["block"] = block;
        } else {
            params["height"] = block;
        }
        NRS.sendRequest("getBlock+", params, function(response) {
			NRS.showBlockModal(response);
		});
	});

	NRS.showBlockModal = function(block) {
        NRS.setBackLink();
        NRS.modalStack.push({ class: "show_block_modal_action", key: "block", value: block.height });
        try {
            $("#block_info_modal_block").html(NRS.escapeRespStr(block.block));
            $("#block_info_transactions_tab_link").tab("show");

            var blockDetails = $.extend({}, block);
            delete blockDetails.transactions;
            blockDetails.generator_formatted_html = NRS.getAccountLink(blockDetails, "generator");
            delete blockDetails.generator;
            delete blockDetails.generatorRS;
            if (blockDetails.previousBlock) {
                blockDetails.previous_block_formatted_html = NRS.getBlockLink(blockDetails.height - 1, blockDetails.previousBlock);
                delete blockDetails.previousBlock;
            }
            if (blockDetails.nextBlock) {
                blockDetails.next_block_formatted_html = NRS.getBlockLink(blockDetails.height + 1, blockDetails.nextBlock);
                delete blockDetails.nextBlock;
            }
            if (blockDetails.timestamp) {
                blockDetails.blockGenerationTime = NRS.formatTimestamp(blockDetails.timestamp);
            }
            var detailsTable = $("#block_info_details_table");
            detailsTable.find("tbody").empty().append(NRS.createInfoTable(blockDetails));
            detailsTable.show();
            var transactionsTable = $("#block_info_transactions_table");
            if (block.transactions.length) {
                $("#block_info_transactions_none").hide();
                transactionsTable.show();
                var rows = "";
                for (var i = 0; i < block.transactions.length; i++) {
                    var transaction = block.transactions[i];
                    if (transaction.amountNQT) {
                        transaction.amount = new BigInteger(transaction.amountNQT);
                        transaction.fee = new BigInteger(transaction.feeNQT);
                        rows += "<tr>" +
                        "<td>" + transaction.transactionIndex + (transaction.phased ? "&nbsp<i class='fa fa-gavel' title='" + $.t("phased") + "'></i>" : "") + "</td>" +
                        "<td>" + NRS.getTransactionLink(transaction.transaction, NRS.formatTimestamp(transaction.timestamp)) + "</td>" +
                        "<td>" + NRS.getTransactionIconHTML(transaction.type, transaction.subtype) + "</td>" +
                        "<td>" + NRS.formatAmount(transaction.amount) + "</td>" +
                        "<td>" + NRS.formatAmount(transaction.fee) + "</td>" +
                        "<td>" + NRS.getAccountLink(transaction, "sender") + "</td>" +
                        "<td>" + NRS.getAccountLink(transaction, "recipient") + "</td>" +
                        "</tr>";
                    }
                }
                transactionsTable.find("tbody").empty().append(rows);
            } else {
                $("#block_info_transactions_none").show();
                transactionsTable.hide();
            }
            var executedPhasedTable = $("#block_info_executed_phased_table");
            if (block.executedPhasedTransactions.length) {
                $("#block_info_executed_phased_none").hide();
                executedPhasedTable.show();
                rows = "";
                for (i = 0; i < block.executedPhasedTransactions.length; i++) {
                    transaction = block.executedPhasedTransactions[i];
                    rows += "<tr>" +
                        "<td>" + NRS.getTransactionLink(transaction.transaction, NRS.formatTimestamp(transaction.timestamp)) + "</td>" +
                        "<td>" + NRS.getTransactionIconHTML(transaction.type, transaction.subtype) + "</td>" +
                        "<td>" + NRS.getBlockLink(transaction.height) + "</td>" +
                        "<td>" + (transaction.attachment.phasingFinishHeight == block.height ? $.t("finished") : $.t("approved")) + "</td>";
                }
                executedPhasedTable.find("tbody").empty().append(rows);
            } else {
                $("#block_info_executed_phased_none").show();
                executedPhasedTable.hide();
            }
            var blockInfoModal = $('#block_info_modal');
            if (!blockInfoModal.data('bs.modal') || !blockInfoModal.data('bs.modal').isShown) {
                blockInfoModal.modal("show");
            }
        } finally {
            NRS.fetchingModalData = false;
        }
	};

	return NRS;
}(NRS || {}, jQuery));