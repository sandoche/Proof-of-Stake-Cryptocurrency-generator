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
	var _currentSearch = {
		"page": "",
		"searchStr": ""
	};

    NRS.jsondata.data = function(response) {
        return {
            nameFormatted: NRS.getTransactionLink(response.transaction, NRS.addEllipsis(NRS.unescapeRespStr(response.name), 20)),
            accountFormatted: NRS.getAccountLink(response, "account"),
            type: NRS.addEllipsis(NRS.unescapeRespStr(response.type), 20),
            channel: NRS.addEllipsis(NRS.unescapeRespStr(response.channel), 20),
            filename: NRS.addEllipsis(NRS.unescapeRespStr(response.filename), 20),
            dataFormatted: NRS.getTaggedDataLink(response.transaction, response.isText)
        };
    };

    NRS.getTaggedDataLink = function(transaction, isText) {
        if (isText) {
            return "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' " +
                "data-target='#tagged_data_view_modal' " +
                "data-transaction='" + NRS.escapeRespStr(transaction) + "'>" + $.t("view") + "</a>";
        } else {
            return NRS.getDownloadLink(
                NRS.getRequestPath() + "?requestType=downloadTaggedData&transaction=" + NRS.escapeRespStr(transaction) + "&retrieve=true");
        }
    };

	NRS.tagged_data_show_results = function(response) {
		$("#tagged_data_search_contents").empty();
		$("#tagged_data_search_results").show();
		$("#tagged_data_search_center").hide();
		$("#tagged_data_reset").show();

        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('tagged_data_search_results_section', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            data: []
        });
        if (response.data.length > NRS.itemsPerPage) {
            NRS.hasMorePages = true;
            response.data.pop();
        }
        view.data.length = 0;
        response.data.forEach(
            function (dataJson) {
                view.data.push( NRS.jsondata.data(dataJson) );
            }
        );
        view.render({
            isLoading: false,
            isEmpty: view.data.length == 0
        });
        NRS.pageLoaded();
    };

	NRS.tagged_data_load_tags = function() {
		$('#tagged_data_tag_list').empty();
		NRS.sendRequest("getDataTags+", {
			"firstIndex": NRS.pageNumber * _tagsPerPage - _tagsPerPage,
			"lastIndex": NRS.pageNumber * _tagsPerPage
		}, function(response) {
			var content = "";
			if (response.tags && response.tags.length) {
				NRS.hasMorePages = response.tags.length > _tagsPerPage;
				for (var i=0; i<response.tags.length; i++) {
					content += '<div style="padding:5px 24px 5px 24px;text-align:center;background-color:#fff;font-size:16px;';
					content += 'width:220px;display:inline-block;margin:2px;border:1px solid #f2f2f2;">';
					content += '<a href="#" onclick="event.preventDefault(); NRS.tagged_data_search_tag(\'' +response.tags[i].tag + '\');">';
					content += response.tags[i].tag.escapeHTML() + ' [' + response.tags[i].count + ']</a>';
					content += '</div>';
				}
			}
			$('#tagged_data_tag_list').html(content);
			NRS.pageLoaded();
		});
	};

	NRS.tagged_data_search_account = function(account) {
		if (account == null) {
			account = _currentSearch["searchStr"];
		} else {
			_currentSearch = {
				"page": "account",
				"searchStr": account
			};
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		$(".tagged_data_search_pageheader_addon").hide();
		$(".tagged_data_search_pageheader_addon_account_text").text(account);
		$(".tagged_data_search_pageheader_addon_account").show();
		NRS.sendRequest("getAccountTaggedData+", {
			"account": account,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.tagged_data_show_results(response);
		});
	};

	NRS.tagged_data_search_fulltext = function(query) {
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
		$(".tagged_data_search_pageheader_addon").hide();
		$(".tagged_data_search_pageheader_addon_fulltext_text").text('"' + query + '"');
		$(".tagged_data_search_pageheader_addon_fulltext").show();
		NRS.sendRequest("searchTaggedData+", {
			"query": query,
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.tagged_data_show_results(response);
		});
	};

	NRS.tagged_data_search_tag = function(tag) {
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
		$(".tagged_data_search_pageheader_addon").hide();
		$(".tagged_data_search_pageheader_addon_tag_text").text('"' + tag + '"');
		$(".tagged_data_search_pageheader_addon_tag").show();
		NRS.sendRequest("searchTaggedData+", {
			"tag": tag,
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage
		}, function(response) {
			NRS.tagged_data_show_results(response);
		});
	};

	NRS.tagged_data_search_main = function(callback) {
		if (_currentSearch["page"] != "main") {
			NRS.pageNumber = 1;
			NRS.hasMorePages = false;
		}
		_currentSearch = {
			"page": "main",
			"searchStr": ""
		};
		$(".tagged_data_search input[name=q]").val("").trigger("unmask").mask(NRS.getAccountMask("*"));
		$(".tagged_data_fulltext_search input[name=fs_q]").val("");
		$(".tagged_data_search_pageheader_addon").hide();
		$("#tagged_data_search_contents").empty();
		NRS.tagged_data_load_tags();

		$("#tagged_data_search_center").show();
		$("#tagged_data_reset").hide();
		$("#tagged_data_search_results").hide();
        NRS.sendRequest("getAllTaggedData+", {
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        }, function (response) {
            NRS.tagged_data_show_results(response);
        });

		if (callback) {
			callback();
		}
	};

	NRS.pages.tagged_data_search = function(callback) {
		$("#tagged_data_top").show();
		$("#tagged_data_search_center").show();
		if (_currentSearch["page"] == "account") {
			NRS.tagged_data_search_account();
		} else if (_currentSearch["page"] == "fulltext") {
			NRS.tagged_data_search_fulltext();
		} else if (_currentSearch["page"] == "tag") {
			NRS.tagged_data_search_tag();
		} else {
			NRS.tagged_data_search_main(callback);
		}
	};

	NRS.setup.tagged_data_search = function() {
		var sidebarId = 'sidebar_tagged_data';
		var options = {
			"id": sidebarId,
			"titleHTML": '<i class="fa fa-database"></i><span data-i18n="data_cloud">Data Cloud</span>',
			"page": 'tagged_data_search',
			"desiredPosition": 60,
			"depends": { tags: [ NRS.constants.API_TAGS.DATA ] }
		};
		NRS.addTreeviewSidebarMenuItem(options);
		options = {
			"titleHTML": '<span data-i18n="search">Search</span></a>',
			"type": 'PAGE',
			"page": 'tagged_data_search'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="upload_file">File Upload</span></a>',
			"type": 'MODAL',
			"modalId": 'upload_data_modal'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
	};

	$(".tagged_data_search").on("submit", function(e) {
		e.preventDefault();
		var account = $.trim($(this).find("input[name=q]").val());
		$(".tagged_data_search input[name=q]").val(account);

		if (account == "") {
			NRS.pages.tagged_data_search();
		} else if (NRS.isRsAccount(account)) {
			var address = new NxtAddress();
			if (!address.set(account)) {
				$.growl($.t("error_invalid_account"), {
					"type": "danger"
				});
			} else {
				NRS.tagged_data_search_account(account);
			}
		} else {
            NRS.tagged_data_search_account(account);
		}
	});

	$(".tagged_data_fulltext_search").on("submit", function(e) {
		e.preventDefault();
		var query = $.trim($(this).find("input[name=fs_q]").val());
		if (query != "") {
			NRS.tagged_data_search_fulltext(query);
		}
	});

	$("#tagged_data_reset").on("click", function(e) {
		e.preventDefault();
		NRS.tagged_data_search_main();
	});

	$("#tagged_data_upload").on("click", function(e) {
		e.preventDefault();
        $('#upload_data_modal').modal("show");
	});

    $("#tagged_data_view_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var transaction = $invoker.data("transaction");
        NRS.sendRequest("getTaggedData", {
			"transaction": transaction,
			"retrieve": "true"
		}, function (response) {
			if (response.errorCode) {
                $("#tagged_data_content").val(NRS.unescapeRespStr(response.errorDescription));
			} else {
                $("#tagged_data_content").val(NRS.unescapeRespStr(response.data));
			}
		}, { isAsync: false });
		NRS.getDownloadLink(NRS.getRequestPath() + "?requestType=downloadTaggedData&transaction=" + transaction + "&retrieve=true", $("#tagged_data_download"));
    });

    $("#extend_data_modal").on("show.bs.modal", function (e) {
        var $invoker = $(e.relatedTarget);
        var transaction = $invoker.data("transaction");
        $("#extend_data_transaction").val(transaction);
        NRS.sendRequest("getTransaction", {
            "transaction": transaction
        }, function (response) {
            var fee = NRS.convertToNXT(NRS.escapeRespStr(response.feeNQT));
            $('#extend_data_fee').val(fee);
            $('#extend_data_fee_label').html(String(fee) + " " + NRS.constants.COIN_SYMBOL);
        })
    });

	return NRS;
}(NRS || {}, jQuery));