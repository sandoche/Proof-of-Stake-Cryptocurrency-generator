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
	var _tagsPerPage = 34;
	var _goodsToShow;
	var _currentSearch = {
		"page": "",
		"searchStr": ""
	};
    var missingImage = "img/no_image_available.png";

	NRS.getMarketplaceItemHTML = function(good) {
		var html = "";
		var id = 'good_'+ NRS.escapeRespStr(good.goods);
        var image = NRS.dgs_get_picture(good);
		html += '<div id="' + id +'" style="border:1px solid #ccc;padding:12px;margin-top:12px;margin-bottom:12px;">';
			html += "<table width='100%' style='table-layout:fixed;'>";
				html += "<tr>";
					html += "<td rowspan =20 style='vertical-align:top;width:100px;height:100px;'>";
						html += image;
					html += "</td>";
					html += "<td style='width:10px'>&nbsp;</td>";
					html += "<td>";
						html += "<div style='float:right;color: #999999;background:white;padding:5px;border:1px solid #ccc;border-radius:3px'>";
							html += "<strong>" + $.t("seller") + '</strong>: <span><a href="#" onclick="event.preventDefault();NRS.dgs_search_seller(\'' + NRS.getAccountFormatted(good, "seller") + '\')">' + NRS.getAccountTitle(good, "seller") + "</a></span> ";
							html += "(<a href='#' data-user='" + NRS.getAccountFormatted(good, "seller") + "' class='show_account_modal_action user_info'>" + $.t('info') + "</a>)<br>";
							html += "<strong>" + $.t("product_id") + "</strong>: &nbsp;<a href='#'' data-toggle='modal' data-target='#dgs_product_modal' data-goods='" + NRS.escapeRespStr(good.goods) + "'>" + NRS.escapeRespStr(good.goods) + "</a><br>";
							html += "<strong>" + $.t("timestamp_listing") + "</strong>: " + NRS.formatTimestamp(good.timestamp);
        				html += "</div>";
						html += "<div><h3 class='title'><a href='#' data-goods='" + NRS.escapeRespStr(good.goods) + "' data-toggle='modal' data-target='#dgs_purchase_modal'>" + NRS.escapeRespStr(good.name) + "</a></h3></div>";
						html += "<div class='price'><strong>" + NRS.formatAmount(good.priceNQT) + " NXT</strong></div>";
						html += "<div class='showmore'><div class='moreblock description'>" + String(good.description).autoLink().nl2br() + "</div>";
							if (good.numberOfPublicFeedbacks > 0) {
								html += "<span style='float:right;clear:right;'><a href='#' class='feedback' data-goods='" + NRS.escapeRespStr(good.goods) + "' data-toggle='modal' data-target='#dgs_show_feedback_modal'>" + $.t('show_feedback', 'Show Feedback') + "</a></span>";
							}
						html += "</div>";
						html += "<div>";
							html += "<span class='quantity'><strong>" + $.t("quantity") + "</strong>: " + NRS.format(good.quantity) + "</span>&nbsp;&nbsp; ";
							html += "<span class='tags' style='display:inline-block;'><strong>" + $.t("tags") + "</strong>: ";

							var tags = good.parsedTags;
							for (var i=0; i<tags.length; i++) {
								html += '<span style="display:inline-block;background-color:#fff;padding:2px 5px 2px 5px;border:1px solid #f2f2f2;">';
								html += '<a href="#" class="tags" onclick="event.preventDefault(); NRS.dgs_search_tag(\'' + NRS.escapeRespStr(tags[i]) + '\');">';
								html += NRS.escapeRespStr(tags[i]) + '</a>';
								html += '</span>';
							}

							html += "</span>";

							if (good.numberOfPurchases>0) {
								html +=	"<span class='purchases' style='float:right;clear:right;'><strong>" + $.t("purchases", "Purchases") + "</strong>: " + NRS.format(good.numberOfPurchases) + "</span>";
							}

						html += '</div>';
					html += "</td>";
				html += "</tr>";
			html += "</table>";
        html += '</div>';
		return html;
	};

	NRS.getMarketplacePurchaseHTML = function(purchase, showBuyer) {
		var status, statusHTML, modal;

		if (purchase.unconfirmed) {
			status = $.t("tentative");
		} else if (purchase.pending) {
			status = $.t("pending");
			statusHTML = "<span class='label label-warning'>" + $.t("pending") + "</span>";
		} else if (purchase.refundNQT) {
			status = $.t("refunded");
			modal = "#dgs_view_refund_modal";
		} else if (!purchase.goodsData) {
            if (purchase.deliveryDeadlineTimestamp < NRS.toEpochTime()) {
				status = $.t("not_delivered_in_time");
			} else {
				status = $.t("failed");
			}
		} else {
			status = $.t("complete");
		}

		return "<div data-purchase='" + NRS.escapeRespStr(purchase.purchase) + "' " + (purchase.unconfirmed ? "class='tentative'" : "") + "><div style='float:right;color: #999999;background:white;padding:5px;border:1px solid #ccc;border-radius:3px'>" +
			(showBuyer ? "<strong>" + $.t("buyer") + "</strong>: <span>" + NRS.getAccountLink(purchase, "buyer") + "</span><br>" :
			"<strong>" + $.t("seller") + "</strong>: <span>" + NRS.getAccountLink(purchase, "seller") + "</span><br>") +
			"<strong>" + $.t("product_id") + "</strong>: &nbsp;<a href='#'' data-toggle='modal' data-target='#dgs_product_modal' data-goods='" + NRS.escapeRespStr(purchase.goods) + "'>" + NRS.escapeRespStr(purchase.goods) + "</a>" +
			"</div>" +
			"<h3 class='title'><a href='#' data-purchase='" + NRS.escapeRespStr(purchase.purchase) + "' data-toggle='modal' data-target='" + (modal ? modal : "#dgs_view_delivery_modal") + "'>" + NRS.escapeRespStr(purchase.name) + "</a></h3>" +
			"<table>" +
			"<tr><td style='width:150px'><strong>" + $.t("order_date") + "</strong>:</td><td>" + NRS.formatTimestamp(purchase.timestamp) + "</td></tr>" +
			"<tr><td><strong>" + $.t("order_status") + "</strong>:</td><td><span class='order_status'>" + (statusHTML ? statusHTML : status) + "</span></td></tr>" +
			(purchase.pending ? "<tr><td><strong>" + $.t("delivery_deadline") + "</strong>:</td><td>" + NRS.formatTimestamp(purchase.deliveryDeadlineTimestamp) + "</td></tr>" : "") +
			"<tr><td><strong>" + $.t("price") + "</strong>:</td><td>" + NRS.formatAmount(purchase.priceNQT) + " NXT</td></tr>" +
			"<tr><td><strong>" + $.t("quantity") + "</strong>:</td><td>" + NRS.format(purchase.quantity) + "</td></tr>" +
			(purchase.seller == NRS.account && purchase.feedbackNote ? "<tr><td><strong>" + $.t("feedback") + "</strong>:</td><td>" + $.t("includes_feedback") + "</td></tr>" : "") +
			"</table></div>" +
			"<hr />";
	};

	NRS.getMarketplacePendingOrderHTML = function(purchase) {
      return "<div data-purchase='" + NRS.escapeRespStr(purchase.purchase) + "'><div style='float:right;color: #999999;background:white;padding:5px;border:1px solid #ccc;border-radius:3px'>" +
			"<strong>" + $.t("buyer") + "</strong>: <span>" + NRS.getAccountLink(purchase, "buyer") + "</span><br>" +
			"<strong>" + $.t("product_id") + "</strong>: &nbsp;<a href='#'' data-toggle='modal' data-target='#dgs_product_modal' data-goods='" + NRS.escapeRespStr(purchase.goods) + "'>" + NRS.escapeRespStr(purchase.goods) + "</a>" +
			"</div>" +
			"<h3 class='title'><a href='#' data-purchase='" + NRS.escapeRespStr(purchase.purchase) + "' data-toggle='modal' data-target='#dgs_view_purchase_modal'>" + NRS.escapeRespStr(purchase.name) + "</a></h3>" +
			"<table class='purchase' style='margin-bottom:5px'>" +
			"<tr><td style='width:150px'><strong>Order Date</strong>:</td><td>" + NRS.formatTimestamp(purchase.timestamp) + "</td></tr>" +
			"<tr><td><strong>" + $.t("delivery_deadline") + "</strong>:</td><td>" + NRS.formatTimestamp(purchase.deliveryDeadlineTimestamp) + "</td></tr>" +
			"<tr><td><strong>" + $.t("price") + "</strong>:</td><td>" + NRS.formatAmount(purchase.priceNQT) + " NXT</td></tr>" +
			"<tr><td><strong>" + $.t("quantity") + "</strong>:</td><td>" + NRS.format(purchase.quantity) + "</td></tr>" +
			"</table>" +
			"<span class='delivery'><button type='button' class='btn btn-default btn-deliver' data-toggle='modal' data-target='#dgs_delivery_modal' data-purchase='" + NRS.escapeRespStr(purchase.purchase) + "'>" + $.t("deliver_goods") + "</button></span>" +
			"</div><hr />";
	};

	NRS.dgs_show_results = function(response) {
		var content = "";

		$("#dgs_search_contents").empty();
		$("#dgs_search_results").show();
		$("#dgs_search_center").hide();
		$("#dgs_listings").hide();
		$("#dgs_search_top").show();

		if (response.goods && response.goods.length) {
			if (response.goods.length > NRS.itemsPerPage) {
				NRS.hasMorePages = true;
				response.goods.pop();
			} else {
				NRS.hasMorePages = false;
			}
			for (var i = 0; i < response.goods.length; i++) {
				content += NRS.getMarketplaceItemHTML(response.goods[i]);
			}
		}

		NRS.dataLoaded(content);
		NRS.showMore();
	};

	NRS.dgs_load_tags = function() {
		$('#dgs_tag_list').empty();
		NRS.sendRequest("getDGSTags+", {
				"firstIndex": NRS.pageNumber * _tagsPerPage - _tagsPerPage,
				"lastIndex": NRS.pageNumber * _tagsPerPage
			}, function(response) {
				var content = "";
				if (response.tags && response.tags.length) {
					NRS.hasMorePages = response.tags.length > _tagsPerPage;
					for (var i=0; i<response.tags.length; i++) {
						content += '<div style="padding:5px 24px 5px 24px;text-align:center;background-color:#fff;font-size:16px;';
						content += 'width:220px;display:inline-block;margin:2px;border:1px solid #f2f2f2;">';
						content += '<a href="#" onclick="event.preventDefault(); NRS.dgs_search_tag(\'' +response.tags[i].tag + '\');">';
						content += NRS.escapeRespStr(response.tags[i].tag) + ' [' + response.tags[i].inStockCount + ']</a>';
						content += '</div>';
					}
				}
				$('#dgs_tag_list').html(content);
				NRS.pageLoaded();
			}
        );
	};

	NRS.dgs_search_seller = function(seller) {
		if (seller == null) {
			seller = _currentSearch["searchStr"];
		} else {
			_currentSearch = {
				"page": "seller",
				"searchStr": seller
			};
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		$(".dgs_search_pageheader_addon").hide();
		$(".dgs_search_pageheader_addon_seller_text").text(seller);
		$(".dgs_search_pageheader_addon_seller").show();
		NRS.sendRequest("getDGSGoods+", {
			"seller": seller,
			"includeCounts": true,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.dgs_show_results(response);
		});
	};

	NRS.dgs_search_fulltext = function(query) {
		if (query == null) {
			query = _currentSearch["searchStr"];
		} else {
			_currentSearch = {
				"page": "fulltext",
				"searchStr": query
			};
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		$(".dgs_search_pageheader_addon").hide();
		$(".dgs_search_pageheader_addon_fulltext_text").text('"' + query + '"');
		$(".dgs_search_pageheader_addon_fulltext").show();
		NRS.sendRequest("searchDGSGoods+", {
			"query": query,
            "includeCounts": true,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.dgs_show_results(response);
		});
	};

	NRS.dgs_search_tag = function(tag) {
		if (tag == null) {
			tag = _currentSearch["searchStr"];
		} else {
			_currentSearch = {
				"page": "tag",
				"searchStr": tag
			};
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		$(".dgs_search_pageheader_addon").hide();
		$(".dgs_search_pageheader_addon_tag_text").text('"' + tag + '"');
		$(".dgs_search_pageheader_addon_tag").show();
		NRS.sendRequest("searchDGSGoods+", {
			"tag": tag,
            "includeCounts": true,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.dgs_show_results(response);
		});
	};

	NRS.dgs_search_main = function(callback) {
		if (_currentSearch["page"] != "main") {
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		_currentSearch = {
			"page": "main",
			"searchStr": ""
		};
		$(".dgs_search input[name=q]").val("").trigger("unmask").mask("NXT-****-****-****-*****");
		$(".dgs_fulltext_search input[name=fs_q]").val("");
		$(".dgs_search_pageheader_addon").hide();
		$("#dgs_search_contents").empty();
		NRS.dgs_load_tags();

		NRS.sendRequest("getDGSPurchases+", {
			"buyer": NRS.account
		}, function(response) {
			if (response.purchases && response.purchases.length != null) {
				$("#dgs_user_purchase_count").html(response.purchases.length).removeClass("loading_dots");
			}
		});

		NRS.sendRequest("getDGSGoodsCount+", {
		}, function(response) {
			if (response.numberOfGoods) {
				$("#dgs_product_count").html(response.numberOfGoods).removeClass("loading_dots");
			}
		});
		NRS.sendRequest("getDGSPurchaseCount+", {
		}, function(response) {
			if (response.numberOfPurchases) {
				$("#dgs_total_purchase_count").html(response.numberOfPurchases).removeClass("loading_dots");
			}
		});
		NRS.sendRequest("getDGSTagCount+", {
		}, function(response) {
			if (response.numberOfTags) {
				$("#dgs_tag_count").html(response.numberOfTags).removeClass("loading_dots");
			}
		});

        NRS.dgs_listings("recent_listings","getDGSGoods+");
        NRS.dgs_listings("recent_purchases","getDGSPurchases+");

		$("#dgs_search_center").show();
		$("#dgs_listings").show();
		$("#dgs_search_top").hide();
		$("#dgs_search_results").hide();

		if (callback) {
			callback();
		}
	};

	NRS.pages.dgs_search = function(callback) {
        var dgsDisabled = $("#dgs_disabled");
        var topSection = $("#dgs_top");
        var searchCenter = $("#dgs_search_center");
        var pagination = $("#dgs_pagination");
        if (NRS.settings.marketplace != "1") {
			dgsDisabled.show();
			topSection.hide();
			searchCenter.hide();
			pagination.hide();
			$("#dgs_listings").hide();
            $("#dgs_search_results").hide();
            return;
		}
		dgsDisabled.hide();
		topSection.show();
		searchCenter.show();
		pagination.show();

		if (_currentSearch["page"] == "seller") {
			NRS.dgs_search_seller();
		} else if (_currentSearch["page"] == "fulltext") {
			NRS.dgs_search_fulltext();
		} else if (_currentSearch["page"] == "tag") {
			NRS.dgs_search_tag();
		} else {
			NRS.dgs_search_main(callback);
		}
	};

	NRS.pages.purchased_dgs = function() {
		NRS.sendRequest("getDGSPurchases+", {
			"buyer": NRS.account,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
            var content = "";
			if (response.purchases && response.purchases.length) {
				if (response.purchases.length > NRS.itemsPerPage) {
					NRS.hasMorePages = true;
					response.purchases.pop();
				}
				for (var i = 0; i < response.purchases.length; i++) {
					content += NRS.getMarketplacePurchaseHTML(response.purchases[i]);
				}
			}
			NRS.dataLoaded(content);
		});
	};

	NRS.setup.dgs_search = function() {
		var sidebarId = 'sidebar_dgs_buyer';
		var options = {
			"id": sidebarId,
			"titleHTML": '<i class="fa fa-shopping-cart"></i><span data-i18n="marketplace">Marketplace</span>',
			"page": 'dgs_search',
			"desiredPosition": 60,
			"depends": { tags: [ NRS.constants.API_TAGS.DGS ] }
		};
		NRS.addTreeviewSidebarMenuItem(options);
		options = {
			"titleHTML": '<span data-i18n="marketplace">Marketplace</span></a>',
			"type": 'PAGE',
			"page": 'dgs_search'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="purchased_products">Purchased Products</span>',
			"type": 'PAGE',
			"page": 'purchased_dgs'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_store">My Store</span>'
		};
		NRS.appendSubHeaderToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_products_for_sale">My Products For Sale</span>',
			"type": 'PAGE',
			"page": 'my_dgs_listings'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_pending_orders">My Pending Orders</span>',
			"type": 'PAGE',
			"page": 'pending_orders_dgs'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_completed_orders">My Completed Orders</span>',
			"type": 'PAGE',
			"page": 'completed_orders_dgs'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="list_product_for_sale">List Product For Sale</span>',
			"type": 'MODAL',
			"modalId": 'dgs_listing_modal'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
	};

	NRS.incoming.purchased_dgs = function(transactions) {
		if (NRS.hasTransactionUpdates(transactions)) {
			NRS.loadPage("purchased_dgs");
		}
	};

	NRS.pages.completed_orders_dgs = function() {
		NRS.sendRequest("getDGSPurchases+", {
			"seller": NRS.account,
			"completed": true,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			var content = "";

			if (response.purchases && response.purchases.length) {
				if (response.purchases.length > NRS.itemsPerPage) {
					NRS.hasMorePages = true;
					response.purchases.pop();
				}

				for (var i = 0; i < response.purchases.length; i++) {
					content += NRS.getMarketplacePurchaseHTML(response.purchases[i], true);
				}
			}

			NRS.dataLoaded(content);
		});
	};

    NRS.pages.recent_listings =function() {
		NRS.dgs_listings("recent_listings_full","getDGSGoods+",true);
	};

	NRS.pages.recent_purchases =function() {
		NRS.dgs_listings("recent_purchases_full","getDGSPurchases+",true);
	};

	NRS.incoming.completed_orders_dgs = function() {
		NRS.loadPage("completed_orders_dgs");
	};

	NRS.pages.pending_orders_dgs = function() {
		NRS.sendRequest("getDGSPendingPurchases+", {
			"seller": NRS.account,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			var content = "";
			if (response.purchases && response.purchases.length) {
				if (response.purchases.length > NRS.itemsPerPage) {
					NRS.hasMorePages = true;
					response.purchases.pop();
				}
				for (var i = 0; i < response.purchases.length; i++) {
					content += NRS.getMarketplacePendingOrderHTML(response.purchases[i]);
				}
			}
			NRS.dataLoaded(content);
		});
	};

	NRS.incoming.pending_orders_dgs = function() {
		NRS.loadPage("pending_orders_dgs");
	};

	NRS.pages.my_dgs_listings = function() {
		NRS.sendRequest("getDGSGoods+", {
			"seller": NRS.account,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage,
			"inStockOnly": "false",
			"hideDelisted": "true"
		}, function(response) {
         var rows = "";
			if (response.goods && response.goods.length) {
				if (response.goods.length > NRS.itemsPerPage) {
					NRS.hasMorePages = true;
					response.goods.pop();
				}
				var quantityDecimals = NRS.getNumberOfDecimals(response.goods, "quantity", function(val) {
					return NRS.format(val.quantity);
				});
				var priceDecimals = NRS.getNumberOfDecimals(response.goods, "priceNQT", function(val) {
					return NRS.formatAmount(val.priceNQT);
				});
				for (var i = 0; i < response.goods.length; i++) {
               		var good = response.goods[i];
               		rows += "<tr class='' data-goods='" + NRS.escapeRespStr(good.goods) + "'><td><a href='#' data-toggle='modal' data-target='#dgs_product_modal' data-goods='" + NRS.escapeRespStr(good.goods) + "'>" + NRS.escapeRespStr(good.name) + "</a></td>";
					rows += "<td class='quantity numeric'>" + NRS.format(good.quantity, false, quantityDecimals) + "</td>";
					rows += "<td class='price numeric'>" + NRS.formatAmount(good.priceNQT, false, false, priceDecimals) + " NXT</td>";
					rows += "<td style='white-space:nowrap'><a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_price_change_modal' data-goods='" + NRS.escapeRespStr(good.goods) + "'>" + $.t("change_price") + "</a> <a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_quantity_change_modal' data-goods='" + NRS.escapeRespStr(good.goods) + "'>" + $.t("change_qty") + "</a> <a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_delisting_modal' data-goods='" + NRS.escapeRespStr(good.goods) + "'>" + $.t("delete") + "</a></td></tr>";
				}
			}
			NRS.dataLoaded(rows);
		});
	};

	NRS.incoming.my_dgs_listings = function() {
		NRS.loadPage("my_dgs_listings");
	};

    NRS.dgs_makebase64 = function (modal) {
        var example = $("#dgs_product_picture_example");
        example.attr("src", missingImage);
        var input = $("#" + modal).find("input[name=image]");
        var reader = new FileReader();
        var image = input[0].files[0];
        var img = new Image();
        if (NRS.isFileReaderSupported()) {
            img.src = window.URL.createObjectURL(image);
        } else {
            NRS.logConsole("Image not displayed when using desktop application");
            example.hide();
            return;
        }
        img.onload = function() {
            var reader = new FileReader();
            if (reader) {
                reader.onload = function(output){
                    example.attr("src", output.target.result);
                };
                reader.readAsDataURL(image);
            }
        }
    };

	NRS.forms.dgsListing = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		$.each(data, function(key, value) {
        			data[key] = $.trim(value);
        });

		if (!data.description) {
			return {
				"error": $.t("error_description_required")
			};
		}

		if (data.tags) {
			data.tags = data.tags.toLowerCase();

			var tags = data.tags.split(",");

			if (tags.length > 3) {
				return {
					"error": $.t("error_max_tags", {
						"nr": 3
					})
				};
			} else {
				var clean_tags = [];

				for (var i = 0; i < tags.length; i++) {
					var tag = $.trim(tags[i]);

					if (tag.length < 3 || tag.length > 20) {
						return {
							"error": $.t("error_incorrect_tag_length", {
								"min": 3,
								"max": 20
							})
						};
					} else if (!tag.match(/^[a-z]+$/i)) {
						return {
							"error": $.t("error_incorrect_tag_alpha")
						};
					} else if (clean_tags.indexOf(tag) > -1) {
						return {
							"error": $.t("error_duplicate_tags")
						};
					} else {
						clean_tags.push(tag);
					}
				}

				data.tags = clean_tags.join(",")
			}
		}

		return {
			"data": data
		};
	};

	NRS.forms.dgsListingComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}
		if (NRS.currentPage == "my_dgs_listings") {
         var rowToAdd = "<tr class='tentative' data-goods='" + NRS.escapeRespStr(response.transaction) + "'><td><a href='#' data-toggle='modal' data-target='#dgs_listing_modal' data-goods='" + NRS.escapeRespStr(response.transaction) + "'>" + NRS.escapeRespStr(data.name) + "</a></td><td class='quantity'>" + NRS.format(data.quantity) + "</td><td class='price'>" + NRS.formatAmount(data.priceNQT) + " NXT</td><td style='white-space:nowrap'><a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_price_change_modal' data-goods='" + NRS.escapeRespStr(response.transaction) + "'>" + $.t("change_price") + "</a> <a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_quantity_change_modal' data-goods='" + NRS.escapeRespStr(response.transaction) + "'>" + $.t("change_qty") + "</a> <a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#dgs_delisting_modal' data-goods='" + NRS.escapeRespStr(response.transaction) + "'>" + $.t("delete") + "</a></td></tr>";
         var listingsTable = $("#my_dgs_listings_table");
         listingsTable.find("tbody").prepend(rowToAdd);
			if (listingsTable.parent().hasClass("data-empty")) {
				listingsTable.parent().removeClass("data-empty");
			}
		}
	};

	NRS.forms.dgsDelistingComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}

		$("#my_dgs_listings_table").find("tr[data-goods=" + String(data.goods).escapeHTML() + "]").addClass("tentative tentative-crossed");
	};

	NRS.forms.dgsFeedback = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		NRS.sendRequest("getDGSPurchase", {
			"purchase": data.purchase
		}, function(response) {
			if (response.errorCode) {
				return {
					"error": $.t("error_purchase")
				};
			} else {
				data.seller = response.seller;
			}
		}, { isAsync: false });

		data.add_message = true;
		data.encrypt_message = data.feedback_type != "public";

		delete data.seller;
		delete data.feedback_type;

		return {
			"data": data
		};
	};

	NRS.forms.dgsPurchase = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));

		NRS.sendRequest("getDGSGood", {
			"goods": data.goods
		}, function(response) {
			if (response.errorCode) {
				return {
					"error": $.t("error_goods")
				};
			} else {
				data.seller = response.seller;
			}
		}, { isAsync: false });

        if (NRS.isShowFakeWarning()) {
            return NRS.composeFakeWarning($.t("goods"), data.goods);
        }

		data.deliveryDeadlineTimestamp = String(NRS.toEpochTime() + 60 * 60 * data.deliveryDeadlineTimestamp);

		delete data.seller;

		return {
			"data": data
		};
	};

	NRS.forms.dgsRefund = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));

		NRS.sendRequest("getDGSPurchase", {
			"purchase": data.purchase
		}, function(response) {
			if (!response.errorCode) {
				data.buyer = response.buyer;
			} else {
				data.buyer = false;
			}
		}, { isAsync: false });

		if (data.buyer === false) {
			return {
				"error": $.t("error_purchase")
			};
		}

		if (data.message) {
			data.add_message = true;
			data.encrypt_message = true;
		}

		delete data.buyer;

		return {
			"data": data
		};
	};

	NRS.forms.dgsDelivery = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));

		NRS.sendRequest("getDGSPurchase", {
			"purchase": data.purchase
		}, function(response) {
			if (!response.errorCode) {
				data.buyer = response.buyer;
			} else {
				data.buyer = false;
			}
		}, { isAsync: false });

		if (data.buyer === false) {
			return {
				"error": $.t("error_purchase")
			};
		}

		if (!data.data) {
			return {
				"error": $.t("error_not_specified", {
					"name": $.t("data").toLowerCase()
				}).capitalize()
			};
		}

		try {
			if (data.doNotSign) {
                data.goodsToEncrypt = data.data;
            } else {
                var encrypted = NRS.encryptNote(data.data, {
                    "account": data.buyer
                }, data.secretPhrase);

                data.goodsData = encrypted.message;
                data.goodsNonce = encrypted.nonce;
            }
			data.goodsIsText = "true";
		} catch (err) {
			return {
				"error": err.message
			};
		}

		delete data.buyer;
		delete data.data;

		return {
			"data": data
		};
	};

	NRS.forms.dgsQuantityChange = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));

		NRS.sendRequest("getDGSGood", {
			"goods": data.goods
		}, function(response) {
			if (!response.errorCode) {
				if (data.quantity == response.quantity) {
					data.deltaQuantity = "0";
				} else {
					var quantityA = new BigInteger(String(data.quantity));
					var quantityB = new BigInteger(String(response.quantity));

					if (quantityA.compareTo(quantityB) > 0) {
						data.deltaQuantity = quantityA.subtract(quantityB).toString();
					} else {
						data.deltaQuantity = "-" + quantityB.subtract(quantityA).toString();
					}
				}
			} else {
				data.deltaQuantity = false;
			}
		}, { isAsync: false });

		if (data.deltaQuantity === false) {
			return {
				"error": $.t("error_goods")
			};
		}

		if (data.deltaQuantity == "0") {
			return {
				"error": $.t("error_quantity_no_change")
			};
		}

		delete data.quantity;

		return {
			"data": data
		};
	};

	NRS.forms.dgsQuantityChangeComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}

		var quantityField = $("#my_dgs_listings_table").find("tr[data-goods=" + String(data.goods).escapeHTML() + "]").addClass("tentative").find(".quantity");
		quantityField.html(quantityField.html() + (String(data.deltaQuantity).charAt(0) != "-" ? "+" : "") + NRS.format(data.deltaQuantity));
	};

	NRS.forms.dgsPriceChangeComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}

		$("#my_dgs_listings_table").find("tr[data-goods=" + String(data.goods).escapeHTML() + "]").addClass("tentative").find(".price").html(NRS.formatAmount(data.priceNQT) + " NXT");
	};

	NRS.forms.dgsRefundComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}

		if (NRS.currentPage == "completed_orders_dgs") {
			var $row = $("#completed_orders_dgs_contents").find("div[data-purchase=" + String(data.purchase).escapeHTML() + "]");
			if ($row.length) {
				$row.addClass("tentative");
				$row.find("span.order_status").html($.t("refunded"));
			}
		}
	};

	NRS.forms.dgsDeliveryComplete = function(response, data) {
		if (response.alreadyProcessed) {
			return;
		}

		if (NRS.currentPage == "pending_orders_dgs") {
			$("#pending_orders_dgs_contents").find("div[data-purchase=" + String(data.purchase).escapeHTML() + "]").addClass("tentative").find("span.delivery").html($.t("delivered"));
		}
	};

	$("#dgs_listing_modal").on("show.bs.modal", function() {
		$("#dgs_product_picture_example").attr("src", missingImage);
	});

	var dgsShowModalBacklink = $("#dgs_show_picture_modal, #dgs_show_feedback_modal");
	dgsShowModalBacklink.on("click", ".dgs_show_modal_action_product, .dgs_show_modal_action_purchase", function () {
		_goodsToShow = $(this).data("data-goods");
		if ($(this).hasClass("dgs_show_modal_action_product")) {
			$("#dgs_product_modal").modal();
		} else if ($(this).hasClass("dgs_show_modal_action_purchase")) {
			$("#dgs_purchase_modal").modal();
		}
	});

	$("#dgs_refund_modal, #dgs_delivery_modal, #dgs_feedback_modal, #dgs_view_purchase_modal, #dgs_view_delivery_modal, #dgs_view_refund_modal").on("show.bs.modal", function(e) {
		var $modal = $(this);
		var $invoker = $(e.relatedTarget);

		var type = $modal.attr("id");

		var purchase = $invoker.data("purchase");

        if (NRS.getUrlParameter("purchase") && purchase == null) {
            purchase = String(NRS.getUrlParameter("purchase")).escapeHTML();
        }

		$modal.find("input[name=purchase]").val(purchase);

		NRS.sendRequest("getDGSPurchase", {
			"purchase": purchase
		}, function(response) {
			if (response.errorCode) {
				e.preventDefault();
				$.growl($.t("error_purchase"), {
					"type": "danger"
				});
			} else {
				NRS.sendRequest("getDGSGood", {
					"goods": response.goods
				}, function(good) {
					if (good.errorCode) {
						e.preventDefault();
						$.growl($.t("error_products"), {
							"type": "danger"
						});
					} else {
						var output = "<table>";
						var image = NRS.dgs_get_picture(good);
						output += "<tr><th style='width:85px;'><strong>" + $.t("product") + "</strong>:</th><td>" + NRS.escapeRespStr(good.name) + '</td><td rowspan = 20 height="100" width="100">' + image + '</td></tr>';
						output += "<tr><th><strong>" + $.t("price") + "</strong>:</th><td>" + NRS.formatAmount(response.priceNQT) + " NXT</td></tr>";
						output += "<tr><th><strong>" + $.t("quantity") + "</strong>:</th><td>" + NRS.format(response.quantity) + "</td></tr>";
						if (good.delisted) {
							output += "<tr><th><strong>" + $.t("status") + "</strong>:</th><td>" + $.t("no_longer_for_sale") + "</td></tr>";
						}

						if (type == "dgs_refund_modal" || type == "dgs_delivery_modal" || type == "dgs_feedback_modal") {
							if (response.seller == NRS.account) {
								$modal.find("input[name=recipient]").val(response.buyerRS);
							} else {
								$modal.find("input[name=recipient]").val(response.sellerRS);
							}
							if (response.quantity != "1") {
								var orderTotal = NRS.formatAmount(new BigInteger(String(response.quantity)).multiply(new BigInteger(String(response.priceNQT))));
								output += "<tr><th><strong>" + $.t("total") + "</strong>:</th><td>" + orderTotal + " NXT</td></tr>";
							}
							if (response.discountNQT && (type == "dgs_refund_modal" || type == "dgs_feedback_modal")) {
								output += "<tr><th><strong>" + $.t("discount") + "</strong>:</th><td>" + NRS.formatAmount(response.discountNQT) + " NXT</td></tr>";
							}
						}

						if (response.seller == NRS.account) {
							output += "<tr><th><strong>" + $.t("buyer") + "</strong>:</th><td>" + NRS.getAccountLink(response, "buyer") + "</td></tr>";
						} else {
							output += "<tr><th><strong>" + $.t("seller") + "</strong>:</th><td>" + NRS.getAccountLink(response, "seller") + "</td></tr>";
						}

						if (type == "dgs_view_refund_modal") {
							output += "<tr><th><strong>" + $.t("refund_price") + "</strong>:</th><td>" + NRS.formatAmount(response.refundNQT) + " NXT</td></tr>";
						}

						if (response.note && (type == "dgs_view_purchase_modal" || type == "dgs_delivery_modal")) {
							output += "<tr><th><strong>" + $.t("note") + "</strong>:</th><td id='" + type + "_note'></td></tr>";
						}

						output += "</table>";

						$modal.find(".purchase_info").html(output);

						if (response.note && (type == "dgs_view_purchase_modal" || type == "dgs_delivery_modal")) {
							try {
								NRS.tryToDecrypt(response, {
									"note": ""
								}, NRS.getAccountForDecryption(response, "buyer", "seller"), {
									"identifier": "purchase",
									"formEl": "#" + type + "_note",
									"outputEl": "#" + type + "_note",
									"showFormOnClick": true
								});
							} catch (err) {
								response.note = String(err.message);
							}
						}

						if (type == "dgs_refund_modal") {
							var orderTotalBeforeDiscount = new BigInteger(String(response.quantity)).multiply(new BigInteger(String(response.priceNQT)));
							var refund = orderTotalBeforeDiscount.subtract(new BigInteger(String(response.discountNQT)));

							$("#dgs_refund_purchase").val(response.purchase);
							$("#dgs_refund_refund").val(NRS.convertToNXT(refund));
						} else if (type == "dgs_view_purchase_modal") {
							var primaryButton = $modal.find("button.btn-primary");
							primaryButton.data("purchase", response.purchase);
						} else if (type == "dgs_view_refund_modal") {
							NRS.tryToDecrypt(response, {
								"refundNote": $.t("Refund Note")
							}, NRS.getAccountForDecryption(response, "buyer", "seller"), {
								"identifier": "purchase",
								"noPadding": true,
								"formEl": "#dgs_view_refund_output",
								"outputEl": "#dgs_view_refund_output"
							});
						} else if (type == "dgs_view_delivery_modal") {
							if (response.pending) {
								e.preventDefault();
								$.growl($.t("error_goods_not_yet_delivered"), {
									"type": "warning"
								});
								return;
							}

							var fieldsToDecrypt = {
								"goodsData": "Data"
							};

							if (response.feedbackNote) {
								fieldsToDecrypt["feedbackNote"] = $.t("feedback_given");
							}

							if (response.refundNote) {
								fieldsToDecrypt["refundNote"] = $.t("refund_note");
							}

							NRS.tryToDecrypt(response, fieldsToDecrypt, NRS.getAccountForDecryption(response, "buyer", "seller"), {
								"identifier": "purchase",
								"noPadding": true,
								"formEl": "#dgs_view_delivery_output",
								"outputEl": "#dgs_view_delivery_output"
							});

							if (type == "dgs_view_delivery_modal") {
								if (!response.pending && !response.goodsData) {
                                    if (response.deliveryDeadlineTimestamp < NRS.toEpochTime()) {
										$("#dgs_view_delivery_output").append("<div class='callout callout-danger' style='margin-bottom:0'>" + $.t("purchase_not_delivered_in_time") + "</div>");
									} else {
										$("#dgs_view_delivery_output").append("<div class='callout callout-danger' style='margin-bottom:0'>" + $.t("purchase_failed") + "</div>");
									}
								}
							}

							var $btn = $modal.find("button.btn-primary:not([data-ignore=true])");

							if (!response.feedbackNote) {
								if (NRS.account == response.buyer) {
									$btn.data("purchase", response.purchase);
									$btn.attr("data-target", "#dgs_feedback_modal");
									$btn.html($.t("give_feedback"));
									$btn.show();
								} else {
									$btn.hide();
								}
							} else {
								$btn.hide();
							}

							if (!response.refundNote && NRS.account == response.seller) {
								$btn.data("purchase", response.purchase);
								$btn.attr("data-target", "#dgs_refund_modal");
								$btn.html($.t("refund_purchase"));
								$btn.show();
							}
						}
					}
				}, { isAsync: false });
			}
		}, { isAsync: false });
	}).on("hidden.bs.modal", function() {
		var type = $(this).attr("id");
		NRS.removeDecryptionForm($(this));

		$(this).find(".purchase_info").html($.t("loading"));

		if (type == "dgs_refund_modal") {
			$("#dgs_refund_purchase").val("");
		} else if (type == "dgs_view_delivery_modal") {
			$("#dgs_delivery_purchase").val("");
			$("#dgs_view_delivery_output").empty();
			$(this).find("button.btn-primary").data("purchase", "");
		}
	});

	$("#dgs_product_modal, #dgs_delisting_modal, #dgs_quantity_change_modal, #dgs_price_change_modal, #dgs_purchase_modal").on("show.bs.modal", function(e) {
		var $modal = $(this);
		var $invoker = $(e.relatedTarget);

		var type = $modal.attr("id");
        var goods;

		if (!$invoker.length) {
			goods = _goodsToShow;
			_goodsToShow = 0;
		} else {
			goods = $invoker.data("goods");
		}

        if (NRS.getUrlParameter("goods") && goods == null) {
            goods = String(NRS.getUrlParameter("goods")).escapeHTML();
        }

		$modal.find("input[name=goods]").val(goods);

		NRS.sendRequest("getDGSGood", {
			"goods": goods
		}, function(response) {
			if (response.errorCode) {
				e.preventDefault();
				$.growl($.t("error_goods"), {
					"type": "danger"
				});
			} else {

				var output = "<table>";
				var image = NRS.dgs_get_picture(response);

				NRS.sendRequest("getDGSGoodsPurchases+", {
					"goods": response.goods,
					"withPublicFeedbacksOnly": true
				}, function(publicFeedback) {
					output += "<tr><th style='width:85px'><strong>" + $.t("product") + "</strong>:</th><td>" + NRS.escapeRespStr(response.name);
					if (publicFeedback.purchases.length && publicFeedback.purchases.length > 0) {
						output += " (<a href='#' class='feedback' data-goods='" + NRS.escapeRespStr(response.goods) + "' data-toggle='modal' data-target='#dgs_show_feedback_modal'>" + $.t('show_feedback', 'Show Feedback') + "</a>)";
					}
					output += '</td><td rowspan = 20 height="100" width="100">' + image + '</td></tr>';
					output += "<tr><th><strong>" + $.t("date") + "</strong>:</th><td>" + NRS.formatTimestamp(response.timestamp) + "</td></tr>";
					output += "<tr><th><strong>" + $.t("price") + "</strong>:</th><td>" + NRS.formatAmount(response.priceNQT) + " NXT</td></tr>";
					output += "<tr><th><strong>" + $.t("seller") + "</strong>:</th><td>" + NRS.getAccountLink(response, "seller") + " (" + '<a href="#" data-goto-seller="' + response.sellerRS + '">View Store' + "</a>)</td></tr>";
					if (response.delisted) {
						output += "<tr><th><strong>" + $.t("status") + "</strong>:</th><td>" + $.t("no_longer_for_sale") + "</td></tr>";
					} else {
						output += "<tr><th><strong>" + $.t("quantity") + "</strong>:</th><td>" + NRS.format(response.quantity) + "</td></tr>";
					}
					if (type == "dgs_purchase_modal") {
						NRS.modalStack.push({class: "dgs_show_modal_action_purchase", key: "data-goods", value: goods});
					} else if (type == "dgs_product_modal") {
						NRS.modalStack.push({class: "dgs_show_modal_action_product", key: "data-goods", value: goods});
					}
					if (type == "dgs_purchase_modal" || type == "dgs_product_modal") {
						output += "<tr><td colspan='2'><div style='max-height:150px;overflow:auto;'>" + String(response.description).autoLink().nl2br() + "</div></td></tr>";
					}
					output += "</table>";
					$modal.find(".goods_info").html(output);

					if (type == "dgs_quantity_change_modal") {
						$("#dgs_quantity_change_current_quantity, #dgs_quantity_change_quantity").val(NRS.escapeRespStr(response.quantity));
					} else if (type == "dgs_price_change_modal") {
						$("#dgs_price_change_current_price, #dgs_price_change_price").val(NRS.convertToNXT(response.priceNQT).escapeHTML());
					} else if (type == "dgs_purchase_modal") {
						$modal.find("input[name=recipient]").val(response.sellerRS);

						$("#dgs_purchase_price").val(NRS.escapeRespStr(response.priceNQT));
						$("#dgs_total_purchase_price").html(NRS.formatAmount(response.priceNQT) + " NXT");

						$("#dgs_purchase_quantity").on("change", function() {
							var totalNQT = new BigInteger(response.priceNQT).multiply(new BigInteger(String($(this).val()))).toString();
							$("#dgs_total_purchase_price").html(NRS.formatAmount(totalNQT) + " NXT");
						});
					}
				});
			}
		}, { isAsync: false });
	}).on("hidden.bs.modal", function() {
		$("#dgs_purchase_quantity").off("change");

		NRS.removeDecryptionForm($(this));

		$(this).find(".goods_info").html($.t("loading"));
		$("#dgs_quantity_change_current_quantity, #dgs_price_change_current_price, #dgs_quantity_change_quantity, #dgs_price_change_price").val("0");
	});

	$("#dgs_show_feedback_modal").on("show.bs.modal", function(e) {
		NRS.setBackLink();
		var $modal = $(this);
		var $invoker = $(e.relatedTarget);
		var goods = $invoker.data("goods");

		if (NRS.getUrlParameter("goods") && goods == null) {
            goods = String(NRS.getUrlParameter("goods")).escapeHTML();
        }

		$modal.find(".modal_content table").empty();
		NRS.sendRequest("getDGSGoodsPurchases+", {
			"goods": goods,
			"withPublicFeedbacksOnly": true
		}, function(response) {
			if (response.purchases.length && response.purchases.length > 0) {
				for (var i=0; i<response.purchases.length; i++) {
					var purchase = response.purchases[i];
					if (purchase.publicFeedbacks.length && purchase.publicFeedbacks.length > 0) {
						$modal.find(".modal_content table").append('<tr><td>' + NRS.escapeRespStr(purchase.publicFeedbacks[0]) + '</td></tr>');
					}
				}
			}
		});
	});

	$(".dgs_my_purchases_link").click(function(e) {
		e.preventDefault();
		$("#sidebar_dgs_buyer").find("a[data-page=purchased_dgs]").addClass("active").trigger("click");
	});

	$(".dgs_search").on("submit", function(e) {
		e.preventDefault();

		var seller = $.trim($(this).find("input[name=q]").val());

		$(".dgs_search input[name=q]").val(seller);

		if (seller == "") {
			NRS.pages.dgs_search();
		} else if (/^(NXT\-)/i.test(seller)) {
			var address = new NxtAddress();

			if (!address.set(seller)) {
				$.growl($.t("error_invalid_seller"), {
					"type": "danger"
				});
			} else {
				NRS.dgs_search_seller(seller);
			}
		} else {
            NRS.dgs_search_seller(seller);
		}
	});

	$(".dgs_fulltext_search").on("submit", function(e) {
		e.preventDefault();

		var query = $.trim($(this).find("input[name=fs_q]").val());

		if (query != "") {
			NRS.dgs_search_fulltext(query);
		}
	});

	$("#dgs_clear_results").on("click", function(e) {
		e.preventDefault();
		NRS.dgs_search_main();
	});

	$("#accept_dgs_link").on("click", function(e) {
		e.preventDefault();
		NRS.updateSettings("marketplace", "1");
		NRS.pages.dgs_search();
	});

	$("#user_info_modal").on("click", "a[data-goto-goods]", function(e) {
		e.preventDefault();

		var $visible_modal = $(".modal.in");

		if ($visible_modal.length) {
            $visible_modal.modal("hide");
		}

		NRS.goToGoods($(this).data("seller"), $(this).data("goto-goods"));
	});

	$("#dgs_purchase_modal, #dgs_product_modal, #dgs_recent_listings_full, #dgs_listings").on("click", "a[data-goto-seller]", function(e) {
		e.preventDefault();

		var $visible_modal = $(".modal.in");

		if ($visible_modal.length) {
			$visible_modal.modal("hide");
		}
		NRS.goToPage("dgs_search", function() {});
		NRS.dgs_search_seller($(this).data("goto-seller"));
	});

	NRS.goToGoods = function(seller, goods) {
		$(".dgs_search input[name=q]").val(seller);

		NRS.goToPage("dgs_search", function() {
			_goodsToShow = goods;
			$("#dgs_purchase_modal").modal("show");
		});
	};
	var dgsShowPictureModal = $("#dgs_show_picture_modal");
    dgsShowPictureModal.on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
		var goods;

		if (!$invoker.length) {
			goods = _goodsToShow;
			_goodsToShow = 0;
		} else {
			goods = $invoker.data("goods");
		}

		if (NRS.getUrlParameter("goods") && goods == null) {
			goods = String(NRS.getUrlParameter("goods")).escapeHTML();
		}
		NRS.setBackLink();
		NRS.sendRequest("getDGSGood+", {
			"goods": goods
		}, function(response) {
			var image;
			if (!response.hasImage) {
				image = missingImage;
			} else {
				image = NRS.getRequestPath() + "?requestType=downloadPrunableMessage&transaction=" + response.goods + "&retrieve=true";
			}
			$("#dgs_product_picture_modal").attr("src", image);
			$("#dgs_product_picture_modal_goods_name").html(NRS.escapeRespStr(response.name));
		});
    });

    NRS.dgs_get_picture = function(input) {
        var picture = new Image();
        var image = "";
        if (!input.hasImage) {
            picture.src = missingImage;
            image = '<img style="max-height:100%;max-width:100%;" id="dgs_product_picture" src="'+ picture.src + '"/>';
        } else {
            picture.src = NRS.getRequestPath() + "?requestType=downloadPrunableMessage&transaction=" + input.goods + "&retrieve=true";
            image = '<a href="#" data-toggle="modal" data-target="#dgs_show_picture_modal" data-goods="' + input.goods + '"><img style="max-height:100%;max-width:100%;" id="dgs_product_picture" src="'+ picture.src + '"/></a>';
        }
        return image;
    };

	NRS.dgs_listings = function (table, api, full) {
    		var listingsTable = $("#" + table + "_table");
    		listingsTable.find("tbody").empty();
    		listingsTable.parent().addClass("data-loading").removeClass("data-empty");
    		var params;
    		if (full) {
    			params = {
    				"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
    				"lastIndex": NRS.pageNumber * NRS.itemsPerPage,
					"completed": true
    			};
    		} else {
    			params = {
    				"firstIndex": 0,
    				"lastIndex": 9,
					"completed": true
    			};
    		}
    		var view = NRS.simpleview.get(table, {
    			errorMessage: null,
    			isLoading: true,
    			isEmpty: false,
    			data: []
    		});
    		NRS.sendRequest(api, params, function (response) {
    			var accountKey;
    			if (api == "getDGSGoods+") {
    				response = response.goods;
    				accountKey = "seller";
    			} else if (api == "getDGSPurchases+") {
    				response = response.purchases;
    				accountKey = "buyer";
    			}
    			if (response.length > NRS.itemsPerPage) {
    				NRS.hasMorePages = true;
    				response.pop();
    			}
				var priceDecimals = NRS.getNumberOfDecimals(response, "priceNQT", function(val) {
					return NRS.formatAmount(val.priceNQT);
				});
    			for (var i = 0; i < response.length; i++) {
    				var item = response[i];
    				var name = NRS.escapeRespStr(item.name);
    				var image = NRS.dgs_get_picture(item);
                    if (name.length > 45) {
    					name = name.substring(0, 45) + "...";
    				}
					var good = '<a href="#" data-goods="' + item.goods + '" data-toggle="modal" data-target="#dgs_purchase_modal">' + name + '</a>';
    				var account;
    				if (accountKey == "seller") {
						account = '<a href="#" data-goto-seller="' + item.sellerRS + '">' + item.sellerRS + '</a>';
    				} else if (accountKey == "buyer") {
    					account = NRS.getAccountLink(item, accountKey)
    				}
    				view.data.push({
    					"timestamp": NRS.formatTimestamp(item.timestamp),
    					"good": good,
    					"price": NRS.formatAmount(item.priceNQT, NRS.decimals, false, priceDecimals),
    					"account": account,
    					"image": image
    				})
    			}
    			view.render({
    				isLoading: false,
    				isEmpty: view.data.length == 0
    			});
    			NRS.pageLoaded();
    		});
    	};

	return NRS;
}(NRS || {}, jQuery));