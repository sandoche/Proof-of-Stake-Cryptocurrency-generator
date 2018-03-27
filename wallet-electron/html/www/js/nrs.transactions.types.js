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
    // If you add new mandatory attributes, please make sure to add them to
    // NRS.loadTransactionTypeConstants as well (below)
    NRS.transactionTypes = {
        0: {
            'title': "Payment",
            'i18nKeyTitle': 'payment',
            'iconHTML': "<i class='ion-calculator'></i>",
            'subTypes': {
                0: {
                    'title': "Ordinary Payment",
                    'i18nKeyTitle': 'ordinary_payment',
                    'iconHTML': "<i class='fa fa-money'></i>",
                    'receiverPage': 'transactions'
                }
            }
        },
        1: {
            'title': "Messaging/Voting/Aliases",
            'i18nKeyTitle': 'messaging_voting_aliases',
            'iconHTML': "<i class='fa fa-envelope-square'></i>",
            'subTypes': {
                0: {
                    'title': "Arbitrary Message",
                    'i18nKeyTitle': 'arbitrary_message',
                    'iconHTML': "<i class='fa fa-envelope-o'></i>",
                    'receiverPage': 'messages'
                },
                1: {
                    'title': "Alias Assignment",
                    'i18nKeyTitle': 'alias_assignment',
                    'iconHTML': "<i class='fa fa-bookmark'></i>"
                },
                2: {
                    'title': "Poll Creation",
                    'i18nKeyTitle': 'poll_creation',
                    'iconHTML': "<i class='fa fa-check-square-o'></i>"
                },
                3: {
                    'title': "Vote Casting",
                    'i18nKeyTitle': 'vote_casting',
                    'iconHTML': "<i class='fa fa-check'></i>"
                },
                4: {
                    'title': "Hub Announcement",
                    'i18nKeyTitle': 'hub_announcement',
                    'iconHTML': "<i class='ion-radio-waves'></i>"
                },
                5: {
                    'title': "Account Info",
                    'i18nKeyTitle': 'account_info',
                    'iconHTML': "<i class='fa fa-info'></i>"
                },
                6: {
                    'title': "Alias Sale/Transfer",
                    'i18nKeyTitle': 'alias_sale_transfer',
                    'iconHTML': "<i class='fa fa-tag'></i>",
                    'receiverPage': "aliases"
                },
                7: {
                    'title': "Alias Buy",
                    'i18nKeyTitle': 'alias_buy',
                    'iconHTML': "<i class='fa fa-money'></i>",
                    'receiverPage': "aliases"
                },
                8: {
                    'title': "Alias Deletion",
                    'i18nKeyTitle': 'alias_deletion',
                    'iconHTML': "<i class='fa fa-times'></i>"
                },
                9: {
                    'title': "Transaction Approval",
                    'i18nKeyTitle': 'transaction_approval',
                    'iconHTML': "<i class='fa fa-gavel'></i>",
                    'receiverPage': "transactions"
                },
                10: {
                    'title': "Account Property",
                    'i18nKeyTitle': 'account_property',
                    'iconHTML': "<i class='fa fa-gavel'></i>",
                    'receiverPage': "transactions"
                },
                11: {
                    'title': "AccountPropertyDelete",
                    'i18nKeyTitle': 'account_property_delete',
                    'iconHTML': "<i class='fa fa-question'></i>",
                    'receiverPage': "transactions"
                }
            }
        },
        2: {
            'title': "Asset Exchange",
            'i18nKeyTitle': 'asset_exchange',
            'iconHTML': '<i class="fa fa-signal"></i>',
            'subTypes': {
                0: {
                    'title': "Asset Issuance",
                    'i18nKeyTitle': 'asset_issuance',
                    'iconHTML': '<i class="fa fa-bullhorn"></i>'
                },
                1: {
                    'title': "Asset Transfer",
                    'i18nKeyTitle': 'asset_transfer',
                    'iconHTML': '<i class="ion-arrow-swap"></i>',
                    'receiverPage': "transfer_history"
                },
                2: {
                    'title': "Ask Order Placement",
                    'i18nKeyTitle': 'ask_order_placement',
                    'iconHTML': '<i class="ion-arrow-graph-down-right"></i>',
                    'receiverPage': "open_orders"
                },
                3: {
                    'title': "Bid Order Placement",
                    'i18nKeyTitle': 'bid_order_placement',
                    'iconHTML': '<i class="ion-arrow-graph-up-right"></i>',
                    'receiverPage': "open_orders"
                },
                4: {
                    'title': "Ask Order Cancellation",
                    'i18nKeyTitle': 'ask_order_cancellation',
                    'iconHTML': '<i class="fa fa-times"></i>',
                    'receiverPage': "open_orders"
                },
                5: {
                    'title': "Bid Order Cancellation",
                    'i18nKeyTitle': 'bid_order_cancellation',
                    'iconHTML': '<i class="fa fa-times"></i>',
                    'receiverPage': "open_orders"
                },
                6: {
                    'title': "Dividend Payment",
                    'i18nKeyTitle': 'dividend_payment',
                    'iconHTML': '<i class="fa fa-gift"></i>',
                    'receiverPage': "transactions"
                },
                7: {
                    'title': "Delete Asset Shares",
                    'i18nKeyTitle': 'delete_asset_shares',
                    'iconHTML': '<i class="fa fa-remove"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        3: {
            'title': "Marketplace",
            'i18nKeyTitle': 'marketplace',
            'iconHTML': '<i class="fa fa-shopping-cart"></i>',
            'subTypes': {
                0: {
                    'title': "Marketplace Listing",
                    'i18nKeyTitle': 'marketplace_listing',
                    'iconHTML': '<i class="fa fa-bullhorn"></i>'
                },
                1: {
                    'title': "Marketplace Removal",
                    'i18nKeyTitle': 'marketplace_removal',
                    'iconHTML': '<i class="fa fa-times"></i>'
                },
                2: {
                    'title': "Marketplace Price Change",
                    'i18nKeyTitle': 'marketplace_price_change',
                    'iconHTML': '<i class="fa fa-line-chart"></i>'
                },
                3: {
                    'title': "Marketplace Quantity Change",
                    'i18nKeyTitle': 'marketplace_quantity_change',
                    'iconHTML': '<i class="fa fa-sort"></i>'
                },
                4: {
                    'title': "Marketplace Purchase",
                    'i18nKeyTitle': 'marketplace_purchase',
                    'iconHTML': '<i class="fa fa-money"></i>',
                    'receiverPage': "pending_orders_dgs"
                },
                5: {
                    'title': "Marketplace Delivery",
                    'i18nKeyTitle': 'marketplace_delivery',
                    'iconHTML': '<i class="fa fa-cube"></i>',
                    'receiverPage': "purchased_dgs"
                },
                6: {
                    'title': "Marketplace Feedback",
                    'i18nKeyTitle': 'marketplace_feedback',
                    'iconHTML': '<i class="ion-android-social"></i>',
                    'receiverPage': "completed_orders_dgs"
                },
                7: {
                    'title': "Marketplace Refund",
                    'i18nKeyTitle': 'marketplace_refund',
                    'iconHTML': '<i class="fa fa-reply"></i>',
                    'receiverPage': "purchased_dgs"
                }
            }
        },
        4: {
            'title': "Account Control",
            'i18nKeyTitle': 'account_control',
            'iconHTML': '<i class="ion-locked"></i>',
            'subTypes': {
                0: {
                    'title': "Balance Leasing",
                    'i18nKeyTitle': 'balance_leasing',
                    'iconHTML': '<i class="fa fa-arrow-circle-o-right"></i>',
                    'receiverPage': "transactions"
                },
                1: {
                    'title': "Mandatory Approval",
                    'i18nKeyTitle': 'phasing_only',
                    'iconHTML': '<i class="fa fa-gavel"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        5: {
            'title': "Monetary System",
            'i18nKeyTitle': 'monetary_system',
            'iconHTML': '<i class="fa fa-bank"></i>',
            'subTypes': {
                0: {
                    'title': "Issue Currency",
                    'i18nKeyTitle': 'issue_currency',
                    'iconHTML': '<i class="fa fa-bullhorn"></i>'
                },
                1: {
                    'title': "Reserve Increase",
                    'i18nKeyTitle': 'reserve_increase',
                    'iconHTML': '<i class="fa fa-cubes"></i>'
                },
                2: {
                    'title': "Reserve Claim",
                    'i18nKeyTitle': 'reserve_claim',
                    'iconHTML': '<i class="fa fa-truck"></i>',
                    'receiverPage': "currencies"
                },
                3: {
                    'title': "Currency Transfer",
                    'i18nKeyTitle': 'currency_transfer',
                    'iconHTML': '<i class="ion-arrow-swap"></i>',
                    'receiverPage': "currencies"
                },
                4: {
                    'title': "Publish Exchange Offer",
                    'i18nKeyTitle': 'publish_exchange_offer',
                    'iconHTML': '<i class="fa fa-list-alt "></i>'
                },
                5: {
                    'title': "Buy Currency",
                    'i18nKeyTitle': 'currency_buy',
                    'iconHTML': '<i class="ion-arrow-graph-up-right"></i>',
                    'receiverPage': "currencies"
                },
                6: {
                    'title': "Sell Currency",
                    'i18nKeyTitle': 'currency_sell',
                    'iconHTML': '<i class="ion-arrow-graph-down-right"></i>',
                    'receiverPage': "currencies"
                },
                7: {
                    'title': "Mint Currency",
                    'i18nKeyTitle': 'mint_currency',
                    'iconHTML': '<i class="fa fa-money"></i>',
                    'receiverPage': "currencies"
                },
                8: {
                    'title': "Delete Currency",
                    'i18nKeyTitle': 'delete_currency',
                    'iconHTML': '<i class="fa fa-times"></i>'
                }
            }
        },
        6: {
            'title': "Data Cloud",
            'i18nKeyTitle': 'tagged_data',
            'iconHTML': '<i class="fa fa-dashboard"></i>',
            'subTypes': {
                0: {
                    'title': "Upload Data",
                    'i18nKeyTitle': 'upload_tagged_data',
                    'iconHTML': '<i class="fa fa-upload"></i>'
                },
                1: {
                    'title': "Extend Data Lifetime",
                    'i18nKeyTitle': 'extend_tagged_data',
                    'iconHTML': '<i class="fa fa-expand"></i>'
                }
            }
        },
        7: {
            'title': "Shuffling",
            'i18nKeyTitle': 'shuffling',
            'iconHTML': '<i class="fa fa-random"></i>',
            'subTypes': {
                0: {
                    'title': "Shuffling Creation",
                    'i18nKeyTitle': 'shuffling_creation',
                    'iconHTML': '<i class="fa fa-plus"></i>'
                },
                1: {
                    'title': "Shuffling Registration",
                    'i18nKeyTitle': 'shuffling_registration',
                    'iconHTML': '<i class="fa fa-link"></i>'
                },
                2: {
                    'title': "Shuffling Processing",
                    'i18nKeyTitle': 'shuffling_processing',
                    'iconHTML': '<i class="fa fa-cog"></i>'
                },
                3: {
                    'title': "Shuffling Recipients",
                    'i18nKeyTitle': 'shuffling_recipients',
                    'iconHTML': '<i class="fa fa-spoon"></i>'
                },
                4: {
                    'title': "Shuffling Verification",
                    'i18nKeyTitle': 'shuffling_verification',
                    'iconHTML': '<i class="fa fa-check-square"></i>'
                },
                5: {
                    'title': "Shuffling Cancellation",
                    'i18nKeyTitle': 'shuffling_cancellation',
                    'iconHTML': '<i class="fa fa-thumbs-down"></i>'
                }
            }
        }
    };

    NRS.subtype = {};

    NRS.loadTransactionTypeConstants = function(response) {
        if (response.genesisAccountId) {
            $.each(response.transactionTypes, function(typeIndex, type) {
                if (!(typeIndex in NRS.transactionTypes)) {
                    NRS.transactionTypes[typeIndex] = {
                        'title': "Unknown",
                        'i18nKeyTitle': 'unknown',
                        'iconHTML': '<i class="fa fa-question-circle"></i>',
                        'subTypes': {}
                    }
                }
                $.each(type.subtypes, function(subTypeIndex, subType) {
                    if (!(subTypeIndex in NRS.transactionTypes[typeIndex]["subTypes"])) {
                        NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex] = {
                            'title': "Unknown",
                            'i18nKeyTitle': 'unknown',
                            'iconHTML': '<i class="fa fa-question-circle"></i>'
                        }
                    }
                    NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex]["serverConstants"] = subType;
                });
            });
            NRS.subtype = response.transactionSubTypes;
        }
    };

    NRS.isOfType = function(transaction, type_str) {
        if (!NRS.subtype[type_str]) {
            $.growl($.t("unsupported_transaction_type"));
            return;
        }
        return transaction.type == NRS.subtype[type_str].type && transaction.subtype == NRS.subtype[type_str].subtype;
    };
    
    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}