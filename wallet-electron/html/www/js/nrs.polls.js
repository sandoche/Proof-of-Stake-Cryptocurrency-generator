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
	var _voteCache;
	var requestedPoll;
	var viewingPollBookmark;

	NRS.resetPollsState = function () {
		_voteCache = {};
		requestedPoll = "";
		viewingPollBookmark = "";
	};
	NRS.resetPollsState();

	function _setFollowButtonStates() {
		NRS.storageSelect("polls", null, function (error, polls) {
			$.each(polls, function (index, poll) {
				$('.follow_button:visible[data-follow="' + poll.poll + '"]').attr('disabled', true);
			});
		});
	}

	function _setVoteButtonStates() {
		$('.vote_button:visible[data-poll]').each(function(index, btn) {
			var pollID = $(btn).data('poll');
			if (pollID in _voteCache) {
				$(btn).attr('disabled', true);
			} else {
				NRS.sendRequest("getPollVote", {
					"account": NRS.account,
					"poll": pollID
				}, function(response) {
					if (response && response.voterRS) {
						$(btn).attr('disabled', true);
						_voteCache[pollID] = response;
					} else {
						$(btn).attr('disabled', false);
					}
				});
			}
		});
	}

	NRS.pages.finished_polls = function () {
		NRS.finished_polls("finished_polls_full",true);
	};

	NRS.pages.polls = function() {
		NRS.sendRequest("getPolls+", {
			"firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
			"lastIndex": NRS.pageNumber * NRS.itemsPerPage,
			"includeFinished": false
		}, function(response) {
			if (response.polls && response.polls.length) {
				var polls = {};
				var nrPolls = 0;

				if (response.polls.length > NRS.itemsPerPage) {
					NRS.hasMorePages = true;
					response.polls.pop();
				}

				for (var i = 0; i < response.polls.length; i++) {
					NRS.sendRequest("getTransaction+", {
						"transaction": response.polls[i].poll
					}, function(poll, input) {
						if (NRS.currentPage != "polls") {
							polls = {};
							return;
						}

						if (!poll.errorCode) {
							polls[input.transaction] = poll;
						}

						nrPolls++;
						if (nrPolls == response.polls.length) {
							var rows = "";
							for (var i = 0; i < nrPolls; i++) {
								poll = polls[response.polls[i].poll];
								if (!poll) {
									continue;
								}

								var pollDescription = String(poll.attachment.description);
								if (pollDescription.length > 100) {
									pollDescription = pollDescription.substring(0, 100) + "...";
								}
								rows += "<tr>";
								rows += "<td>" + NRS.getTransactionLink(poll.transaction, poll.attachment.name) + "</td>";
								rows += "<td>" + NRS.escapeRespStr(pollDescription) + "</td>";
								rows += "<td>" + NRS.getAccountLink(poll, "sender") + "</td>";
								rows += "<td>" + NRS.formatTimestamp(poll.timestamp) + "</td>";
								rows += "<td style='text-align:center;'>" + String(poll.attachment.finishHeight - NRS.lastBlockHeight) + "</td>";
								rows += "<td style='text-align:center;'><nobr><a href='#' class='vote_button btn btn-xs btn-default' data-poll='" + poll.transaction +"'>" + $.t('vote_btn_short') + "</a> ";
								rows += "<a href='#' class='follow_button btn btn-xs btn-default' data-follow='" + poll.transaction + "'>" + $.t('vote_follow_btn_short') + "</a> ";
								rows += "<a href='#' class='view_button btn btn-xs btn-default' data-view='" + poll.transaction + "'>" + $.t('view') + "</a></nobr></td>";
								rows += "</tr>";
							}
							NRS.dataLoaded(rows);
							_setFollowButtonStates();
							_setVoteButtonStates();
						}
					});
				}
			} else {
				NRS.dataLoaded();
			}
		});
		NRS.finished_polls("finished_polls",false);
	};
 
	NRS.incoming.polls = function() {
		NRS.loadPage("polls");
	};

	NRS.pages.my_polls = function() {
		NRS.sendRequest("getPolls+", {
			"account": NRS.account,
			"includeFinished": true
		}, function(response) {
			if (response.polls && response.polls.length) {
				var polls = {};
				var nrPolls = 0;

				for (var i = 0; i < response.polls.length; i++) {
					NRS.sendRequest("getTransaction+", {
						"transaction": response.polls[i].poll
					}, function(poll, input) {
						if (NRS.currentPage != "my_polls") {
							polls = {};
							return;
						}
						if (!poll.errorCode) {
							polls[input.transaction] = poll;
						}

						nrPolls++;
						if (nrPolls == response.polls.length) {
							var rows = "";
							for (var i = 0; i < nrPolls; i++) {
								poll = polls[response.polls[i].poll];
								if (!poll) {
									continue;
								}
								var pollDescription = String(poll.attachment.description);
								if (pollDescription.length > 100) {
									pollDescription = pollDescription.substring(0, 100) + "...";
								}
								rows += "<tr>";
								rows += "<td>" + NRS.getTransactionLink(poll.transaction, poll.attachment.name) + "</td>";
								rows += "<td>" + NRS.escapeRespStr(pollDescription) + "</td>";
								rows += "<td>" + NRS.getAccountLink(poll, "sender") + "</td>";
								rows += "<td>" + NRS.formatTimestamp(poll.timestamp) + "</td>";
								if(poll.attachment.finishHeight > NRS.lastBlockHeight) {
									rows += "<td style='text-align:center;'>" + String(poll.attachment.finishHeight - NRS.lastBlockHeight) + "</td>";
									rows += "<td style='text-align:center;'><a href='#' class='vote_button btn btn-xs btn-default' data-poll='" + poll.transaction +"'>" + $.t('vote_btn_short') + "</a> ";
								} else {
									rows += "<td style='text-align:center;'>" + $.t('complete') + "</td>";
									rows += "<td style='text-align:center;'><a href='#' class='results_button btn btn-xs btn-default' data-results='" + poll.transaction +"'>" + $.t('vote_results_btn_short') + "</a> ";
								}
								rows += "<a href='#' class='follow_button btn btn-xs btn-default' data-follow='" + poll.transaction + "'>" + $.t('vote_follow_btn_short') + "</a></td>";
								rows += "</tr>";
							}
							NRS.dataLoaded(rows);
							_setFollowButtonStates();
							_setVoteButtonStates();
						}
					});
				}
			} else {
				NRS.dataLoaded();
			}
		});
	};

	NRS.incoming.my_polls = function() {
		NRS.loadPage("my_polls");
	};

	NRS.pages.voted_polls = function() {
		NRS.sendRequest("getBlockchainTransactions",{"account": NRS.accountRS, "type": 1, "subtype": 3}, function(response) {
			if (response.transactions && response.transactions.length > 0) {
				var polls = {};
				var nrPolls = 0;

				for (var i = 0; i < response.transactions.length; i++) {
					NRS.sendRequest("getTransaction", {
						"transaction": response.transactions[i].attachment.poll
					}, function(poll, input) {
						if (NRS.currentPage != "voted_polls") {
							polls = {};
							return;
						}
						if (!poll.errorCode) {
							polls[input.transaction] = poll;
						}

						nrPolls++;
						if (nrPolls == response.transactions.length) {
							var rows = "";
							for (var i = 0; i < nrPolls; i++) {
								poll = polls[response.transactions[i].attachment.poll];
								if (!poll) {
									continue;
								}
								var pollDescription = String(poll.attachment.description);
								if (pollDescription.length > 100) {
									pollDescription = pollDescription.substring(0, 100) + "...";
								}
								rows += "<tr>";
								rows += "<td>" + NRS.getTransactionLink(poll.transaction, poll.attachment.name) + "</td>";
								rows += "<td>" + NRS.escapeRespStr(pollDescription) + "</td>";
								rows += "<td>" + NRS.getAccountLink(poll, "sender") + "</td>";
								rows += "<td>" + NRS.formatTimestamp(poll.timestamp) + "</td>";
								if(poll.attachment.finishHeight > NRS.lastBlockHeight) {
									rows += "<td style='text-align:center;'>" + String(poll.attachment.finishHeight - NRS.lastBlockHeight) + "</td>";
									rows += "<td style='text-align:center;'>-</td>";
								} else {
									rows += "<td style='text-align:center;'>" + $.t('complete') + "</td>";
									rows += "<td style='text-align:center;'><a href='#' class='results_button btn btn-xs btn-default' data-results='" + poll.transaction +"'>" + $.t('vote_results_btn_short') + "</a></td>";
								}
								rows += "<td style='text-align:center;'><a href='#' class='follow_button btn btn-xs btn-default' data-follow='" + poll.transaction + "'>" + $.t('vote_follow_btn_short') + "</a></td>";
								rows += "</tr>";
							}
							NRS.dataLoaded(rows);
							_setFollowButtonStates();
						}
					});
				}
			} else {
				NRS.dataLoaded();
			}
		});
	};

	NRS.incoming.voted_polls = function() {
		NRS.loadPage("voted_polls");
	};

	NRS.setup.polls = function() {
		var sidebarId = 'sidebar_voting_system';
		var options = {
			"id": sidebarId,
			"titleHTML": '<i class="fa fa-check-square-o"></i><span data-i18n="voting_system">Voting</span>',
			"page": 'polls',
			"desiredPosition": 50,
			"depends": { tags: [ NRS.constants.API_TAGS.VS ] }
		};
		NRS.addTreeviewSidebarMenuItem(options);
		options = {
			"titleHTML": '<span data-i18n="active_polls">Active Polls</span>',
			"type": 'PAGE',
			"page": 'polls'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="followed_polls">Followed Polls</span>',
			"type": 'PAGE',
			"page": 'followed_polls'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_votes">My Votes</span>',
			"type": 'PAGE',
			"page": 'voted_polls'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="my_polls">My Polls</span>',
			"type": 'PAGE',
			"page": 'my_polls'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
		options = {
			"titleHTML": '<span data-i18n="create_poll">Create Poll</span>',
			"type": 'MODAL',
			"modalId": 'create_poll_modal'
		};
		NRS.appendMenuItemToTSMenuItem(sidebarId, options);
	};

	$("#create_poll_answers").on("click", "button.btn.remove_answer", function(e) {
		e.preventDefault();
		if ($("#create_poll_answers").find("> .form-group").length == 1) {
			return;
		}
		$(this).closest("div.form-group").remove();
	});

	$("#create_poll_answers_add").click(function() {
		var $clone = $("#create_poll_answers").find("> .form-group").first().clone();
		$clone.find("input").val("");
		$clone.appendTo("#create_poll_answers");
	});

	function _setMinBalanceForm() {
		var pollType = parseInt($("#create_poll_type").val());
		var mbType = parseInt($("input[name=minBalanceType]:radio:checked").val());
		if (pollType == 0 && mbType == 0) {
			$('#min_voting_balance_label_unit').html($.t('none'));
			$('#create_poll_min_balance').attr('disabled', true);
		} else {
			$('#create_poll_min_balance').attr('disabled', false);
		}
		if ((pollType == 0 && mbType == 1) || pollType == 1) {
			$('#min_voting_balance_label_unit').html(NRS.constants.COIN_SYMBOL);
			$('#create_poll_min_balance').attr('name', 'minBalanceNXT');
		}
		if ((pollType == 0 && mbType == 2) || pollType == 2) {
			$('#min_voting_balance_label_unit').html($.t('asset'));
			$('#create_poll_min_balance').attr('name', 'minBalanceQNTf');
		}
		if ((pollType == 0 && mbType == 3) || pollType == 3) {
			$('#min_voting_balance_label_unit').html($.t('currency'));
			$('#create_poll_min_balance').attr('name', 'minBalanceQNTf');
		}
	}

	$("#create_poll_type").change(function() {
		// poll type changed, lets see if we have to include/remove the asset id
		var pollType = $("#create_poll_type").val();
        if (pollType == "2") {
			$("#create_poll_asset_id_group").css("display", "inline");
			$("#create_poll_ms_currency_group").css("display", "none");
		} else if(pollType == "3") {
			$("#create_poll_asset_id_group").css("display", "none");
			$("#create_poll_ms_currency_group").css("display", "inline");
		} else {
			$("#create_poll_asset_id_group").css("display", "none");
			$("#create_poll_ms_currency_group").css("display", "none");
		}

		if(pollType == "0") {
			$("#create_poll_min_balance_type_group").css("display", "block");
		} else {
			$("#create_poll_min_balance_type_group").css("display", "none");
		}
		_setMinBalanceForm();
	});

	$("input[name=minBalanceType]:radio").change(function () {
		var value = $(this).val();
		if(value == "2") {
			$("#create_poll_asset_id_group").css("display", "block");
			$("#create_poll_ms_currency_group").css("display", "none");
		} else if(value == "3") {
			$("#create_poll_asset_id_group").css("display", "none");
			$("#create_poll_ms_currency_group").css("display", "block");
		} else {
			$("#create_poll_asset_id_group").css("display", "none");
			$("#create_poll_ms_currency_group").css("display", "none");
		}
		_setMinBalanceForm();
	});

	var body = $("body");
    body.on("click", ".vote_button[data-poll]", function(e) {
		e.preventDefault();
		var transactionId = $(this).data("poll");
		NRS.sendRequest("getTransaction", {
			"transaction": transactionId
		}, function(response, input) {
			$("#cast_vote_poll_name").text(response.attachment.name);
			$("#cast_vote_poll_description").text(response.attachment.description);
			var castVoteAnswersEntry = $("#cast_vote_answers_entry");
            castVoteAnswersEntry.text("");
			var selectText;
			if(response.attachment.minNumberOfOptions != response.attachment.maxNumberOfOptions) {
				selectText = $.t('poll_select_min_max_options', {
					'min': response.attachment.minNumberOfOptions,
					'max': response.attachment.maxNumberOfOptions
				});
				$("#cast_vote_range").text(selectText);
			} else if (response.attachment.minNumberOfOptions != 1) {
				selectText = $.t('poll_select_num_options', {
					'num': response.attachment.minNumberOfOptions
				});
				$("#cast_vote_range").text(selectText);
			} else {
				selectText = $.t('poll_select_one_option');
				$("#cast_vote_range").text(selectText);
			}

			$("#cast_vote_poll").val(response.transaction);
			if(response.attachment.maxRangeValue != 1) {
				for (var b=0; b<response.attachment.options.length; b++) {
					var html = "<div class='answer_slider' style='padding:6px;background-color:#f9f9f9;border:1px solid #ddd;margin-bottom:4px;'>";
					html += "<label>"+NRS.escapeRespStr(response.attachment.options[b])+"</label> &nbsp;&nbsp;";
					html += "<span class='cast_vote_value label label-default' style='float:right;'>"+response.attachment.minRangeValue+"</span><br/>";
					html += "<input class='form-control' step='1' value='"+response.attachment.minRangeValue+"' max='"+response.attachment.maxRangeValue+"' min='"+response.attachment.minRangeValue+"' type='range'/>";
					html += "</div>";
					castVoteAnswersEntry.append(html);
				}
			} else {
				for (b=0; b<response.attachment.options.length; b++) {
					castVoteAnswersEntry.append("<div class='answer_boxes'><label><input type='checkbox'/>&nbsp;&nbsp;"+NRS.escapeRespStr(response.attachment.options[b])+"</label></div>");
				}
			}
			$("#cast_vote_modal").modal();
			$("input[type='range']").on("change mousemove", function() {
				var $label = $(this).parent().children(".cast_vote_value.label");
				if ($(this).val() > 0) {
					$label.removeClass("label-default");
					$label.addClass("label-primary");
				} else {
					$label.removeClass("label-primary");
					$label.addClass("label-default");
				}
				$label.text($(this).val());
			});
		});
	});

    function layoutPollResults(resultsdata, polldata) {
        var results = resultsdata.results;
        var options = polldata.options;
        if (!results) {
            results = [];
        }
        var rows = "";
        if (results.length) {
            for (var i = 0; i < results.length; i++) {
                var result = results[i];
                rows += "<tr>";
                rows += "<td>" + NRS.escapeRespStr(options[i]) + "</td>";
                var resultStr = "";
                var weightStr = "";
                if (polldata.votingModel == 0) {
                	resultStr = result.result;
                	weightStr = result.weight;
                } else if (polldata.votingModel == 1) {
                	resultStr = NRS.formatAmount(result.result);
                	weightStr = NRS.formatAmount(result.weight);
                } else if (resultsdata.holding) {
                	resultStr = NRS.formatQuantity(result.result, resultsdata.decimals);
                	weightStr = NRS.formatQuantity(result.weight, resultsdata.decimals);
                }
                rows += "<td style='text-align:right;'>" + resultStr + "</td>";
                rows += "<td style='text-align:right;'>" + weightStr + "</td>";
                rows += "</tr>";
            }
        }
        return rows;
    }

    function layoutPollChart(resultsdata) {
    	$('#followed_polls_poll_chart').empty();
    	if (!resultsdata.results) {
    		return;
    	}
    	var color = d3.scale.category20();
    	var content = [];
    	for(var i=0; i<resultsdata.results.length; i++) {
    		var result = resultsdata.results[i].result;
			if (result == "") {
				continue;
			}
    		content.push({
    			"label": resultsdata.options[i],
    			"value": parseInt(result),
    			"color": color(i)
    		});
    	}
		if (content.length == 0) {
			return;
		}

		//noinspection JSPotentiallyInvalidConstructorUsage
        new d3pie("followed_polls_poll_chart", {
            "header": {
                "title": {
                    "fontSize": 24,
                    "font": "open sans"
                },
                "subtitle": {
                    "color": "#999999",
                    "fontSize": 12,
                    "font": "open sans"
                },
                "titleSubtitlePadding": 0
            },
            "footer": {
                "color": "#999999",
                "fontSize": 10,
                "font": "open sans",
                "location": "bottom-left"
            },
            "size": {
                "canvasHeight": 340,
                "canvasWidth": 350
            },
            "data": {
                "sortOrder": "value-desc",
                "smallSegmentGrouping": {
                    "enabled": true
                },
                "content": content
            },
            "labels": {
                "outer": {
                    "pieDistance": 18
                },
                "inner": {
                    "hideWhenLessThanPercentage": 3
                },
                "mainLabel": {
                    "fontSize": 11
                },
                "percentage": {
                    "color": "#ffffff",
                    "decimalPlaces": 0
                },
                "value": {
                    "color": "#adadad",
                    "fontSize": 11
                },
                "lines": {
                    "enabled": true
                }
            },
            "effects": {
                "load": {
                    "effect": "none"
                },
                "pullOutSegmentOnClick": {
                    "effect": "none",
                    "speed": 400,
                    "size": 8
                }
            }
        });
    }

    $("#my_polls_table, #voted_polls_table").on("click", "a[data-results]", function(e) {
		e.preventDefault();
		var transactionId = $(this).data("results");

		NRS.sendRequest("getPollResult", {"poll": transactionId, "req":"getPollResult"}, voteModal);
		NRS.sendRequest("getPollVotes", {"poll": transactionId, "req":"getPollVotes"}, voteModal);
		NRS.sendRequest("getPoll", {"poll": transactionId, "req": "getPoll"}, voteModal);

		var resultsdata, votesdata, polldata;
		function voteModal(data, input) {
			if (input.req=="getPollResult") {
                resultsdata = data;
            }
			if (input.req=="getPollVotes") {
                votesdata = data;
            }
			if (input.req=="getPoll") {
                polldata = data;
            }
			if (resultsdata !== undefined && votesdata !== undefined && polldata !== undefined) {
				var resultsOptions = $("#poll_results_options");
                resultsOptions.append("<tr><td style='font-weight: bold;width:180px;'><span data-i18n='poll_name'>Poll Name</span>:</td><td><span id='poll_results_poll_name'>"+NRS.escapeRespStr(polldata.name)+"</span></td></tr>");
				resultsOptions.append("<tr><td style='font-weight: bold;width:180px;'><span data-i18n='poll_id'>Poll Id</span>:</td><td><span id='poll_results_poll_id'>"+polldata.poll+"</span></td></tr>");

				$("#poll_results_poll_name").text(NRS.escapeRespStr(polldata.name));
				$("#poll_results_poll_id").text(polldata.poll);
				$("#poll_results_modal").modal();
                var rows = layoutPollResults(resultsdata, polldata);
                $("#poll_results_table").find("tbody").empty().append(rows);

				var votes = votesdata.votes;
				if (!votes) {
					votes = [];
				}

				var pollVotersTable = $("#poll_voters_table");
                if (votes.length) {
					var head = "";
					head += "<tr>";
					head += "<th data-i18n=\"voter\">Voter</th>";

					for (var b=0; b<polldata.options.length; b++) {
						head += "<th>"+String(polldata.options[b].escapeHTML()) + "</th>";
					}
					head += "</tr>";

					pollVotersTable.find("thead").empty().append(head);
					rows = "";
					for (var i = 0; i < votes.length; i++) {
						rows += "<tr>";
						var vote = votes[i];
						rows += "<td><a href='#' class='user_info' data-user='" + NRS.getAccountFormatted(vote, "voter") + "'>" + NRS.getAccountTitle(vote, "voter") + "</td>";
						for(var a=0;a<vote.votes.length;a++) {
							rows += "<td>" + vote.votes[a] + "</td>";
						}
					}
					pollVotersTable.find("tbody").empty().append(rows);
				} else {
					pollVotersTable.find("tbody").empty();
				}
			}
		}
	});

	body.on("click", ".follow_button[data-follow]", function(e) {
		e.preventDefault();
		var $btn = $(this);
		var pollId = $(this).data("follow");

		NRS.sendRequest("getPoll", {"poll": pollId}, function(response) {
			if (response.errorCode) {
                $.growl($.t("no_poll_found"));
			} else {
				NRS.saveFollowedPolls(new Array(response), NRS.forms.addFollowedPollsComplete);
			}
			$btn.attr('disabled', true);
		});
	});

	$("#create_poll_modal").on("show.bs.modal", function() {
		$('#create_poll_min_balance_type_group').show();
		$('#create_poll_min_balance_type_0').click();

		context = {
			labelText: "Currency",
			labelI18n: "currency",
			inputCodeName: "create_poll_ms_code",
			inputIdName: "create_poll_ms_id",
			inputDecimalsName: "create_poll_ms_decimals",
			helpI18n: "add_currency_modal_help"
		};
		NRS.initModalUIElement($(this), '.poll_holding_currency', 'add_currency_modal_ui_element', context);

		context = {
			labelText: "Asset",
			labelI18n: "asset",
			inputIdName: "create_poll_asset_id",
			inputDecimalsName: "create_poll_asset_decimals",
			helpI18n: "add_asset_modal_help"
		};
		NRS.initModalUIElement($(this), '.poll_holding_asset', 'add_asset_modal_ui_element', context);

		var context = {
			labelText: "Finish Height",
			labelI18n: "finish_height",
			helpI18n: "create_poll_finish_height_help",
			inputName: "finishHeight",
			initBlockHeight: NRS.lastBlockHeight + 7000,
			changeHeightBlocks: 500
		};
		NRS.initModalUIElement($(this), '.create_poll_finish_height', 'block_height_modal_ui_element', context);
	});


	var resultsModal = $("#poll_results_modal");
    resultsModal.on("show.bs.modal", function() {
		$("#poll_results_modal_statistics").show();
		// now lets put the data in the correct place...
	});

	resultsModal.find("ul.nav li").click(function(e) {
		e.preventDefault();

		var tab = $(this).data("tab");
		$(this).siblings().removeClass("active");
		$(this).addClass("active");
		$(".poll_results_modal_content").hide();

		var content = $("#poll_results_modal_" + tab);
		content.show();
	});

	resultsModal.on("hidden.bs.modal", function() {
		$(this).find(".poll_results_modal_content").hide();
		$(this).find("ul.nav li.active").removeClass("active");
		$("#poll_results_statistics_nav").addClass("active");
		$("#poll_results_options").text("");
	});	

	NRS.forms.createPoll = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		var options = [];
		$("#create_poll_answers").find("input.create_poll_answers").each(function() {
			var option = $.trim($(this).val());
			if (option) {
				options.push(option);
			}
		});

        var pollType = $("#create_poll_type");
        if(pollType.val() == "0") {
			data["votingModel"] = 0;
			var minBalanceModel = parseInt($('input:radio[name=minBalanceType]:checked').val());
			data["minBalanceModel"] = minBalanceModel;
			if(minBalanceModel == 2) {
                data["holding"] = $("input[name='create_poll_asset_id']").val();
            } else if(minBalanceModel == 3) {
                data["holding"] = $("input[name='create_poll_ms_id']").val();
            }
		} else if(pollType.val() == "1") {
			data["votingModel"] = 1;
			data["minBalanceModel"] = 1;
		} else if(pollType.val() == "2") {
			data["votingModel"] = 2;
			data["holding"] = $("input[name='create_poll_asset_id']").val();
			data["minBalanceModel"] = 2;
		} else if(pollType.val() == "3") {
			data["votingModel"] = 3;
			data["holding"] = $("input[name='create_poll_ms_id']").val();
			data["minBalanceModel"] = 3;
		}

		for (var i = 0; i < options.length; i++) {
			var number;
			if (i < 10) {
                number = "0" + i;
            } else {
                number = i;
            }
			data["option" + (number)] = options[i];
		}

		return {
			"requestType": "createPoll",
			"data": data
		};
	};

	NRS.forms.castVote = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		var options = [];
        var castVoteAnswersEntry = $("#cast_vote_answers_entry");
        castVoteAnswersEntry.find("div.answer_slider input").each(function() {
			var option = $.trim($(this).val());
			if(option == 0) {
                option = -128;
            }
			if (option) {
				options.push(option);
			}
		});

		castVoteAnswersEntry.find("div.answer_boxes input").each(function() {
			var option = $(this).is(':checked') ? 1 : -128;
			options.push(option);
		});

		data["poll"] = $("#cast_vote_poll").val();
		data["deadline"] = $("#cast_vote_deadline").val();
		data["secretPhrase"] =  $("#cast_vote_password").val();
		for (var i = 0; i < options.length; i++) {
			data["vote" + (i < 10 ? "0" + i : i)] = options[i];
		}
		return {
			"requestType": "castVote",
			"data": data
		};
	};

	NRS.forms.castVoteComplete = function(response, data) {
		if (data.poll) {
			$('.vote_button[data-poll="' + data.poll + '"]').attr('disabled', true);
		}
	};

	// a lot of stuff in followed polls, lets put that here
	NRS.followedPolls = [];
	NRS.followedPollIds = [];
	NRS.currentPoll = {};

	NRS.pages.followed_polls = function(callback) {
		$(".content.content-stretch:visible").width($(".page:visible").width());
		NRS.followedPolls = [];
		NRS.followedPollIds = [];
		NRS.storageSelect("polls", null, function (error, polls) {
			//select already bookmarked polls
			$.each(polls, function (index, poll) {
				NRS.cachePoll(poll);
			});
			NRS.loadFollowedPollsSidebar(callback);
		});
		if (NRS.getUrlParameter("poll") && requestedPoll == "") {
			requestedPoll = NRS.getUrlParameter("poll").escapeHTML();
		}
		if (requestedPoll != "") {
		    NRS.sendRequest("getPoll", {
                "poll": requestedPoll
            }, function(response) {
                NRS.loadPoll(response);
            });
	    }
	};

	NRS.cachePoll = function(poll) {
		if (NRS.followedPollIds.indexOf(poll.poll) != -1) {
			return;
		}

		NRS.followedPollIds.push(poll.poll);
		poll.groupName = "";
		var cachedPoll = {
			"poll": String(poll.poll),
			"name": String(poll.name).toLowerCase(),
			"description": String(poll.description),
			"account": String(poll.account),
			"accountRS": String(poll.accountRS),
			"finishHeight": String(poll.finishHeight)
		};
		NRS.followedPolls.push(cachedPoll);
	};

	NRS.forms.addFollowedPoll = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		data.id = $.trim(data.id);
		if (!data.id) {
			return {
				"error": $.t("error_poll_id_required")
			};
		}

		if (!NRS.isNumericAccount(data.id) && !NRS.isRsAccount(data.id)) {
			return {
				"error": $.t("error_poll_id_invalid")
			};
		} else {
			if (data.id == viewingPollBookmark) {
				$("#followed_polls_bookmark_poll").hide();
			}
			NRS.sendRequest("getPoll", {
				"poll": data.id
			}, function(response) {
				if (response.errorCode) {
					NRS.showModalError($.t("no_poll_found"), $modal);
				} else {
					NRS.saveFollowedPolls(new Array(response), NRS.forms.addFollowedPollsComplete);
				}
			});
		}
	};

	NRS.forms.addFollowedPollsComplete = function(newPolls, submittedPolls) {
		NRS.pollSearch = false;

		var followedPollsSidebar = $("#followed_polls_sidebar");
		if (newPolls.length == 0) {
			NRS.closeModal();
			$.growl($.t("error_poll_already_bookmarked", {
				"count": submittedPolls.length
			}), {
				"type": "danger"
			});
            followedPollsSidebar.find("a.active").removeClass("active");
			followedPollsSidebar.find("a[data-poll=" + submittedPolls[0].poll + "]").addClass("active").trigger("click");
		} else {
			NRS.closeModal();

			var message = "";
			if (newPolls.length == 1) {
				message += $.t("success_poll_followed_one");
			} else {
				message += $.t("success_poll_followed", { "count": newPolls.length });
			}

			$.growl(message, {
				"type": "success"
			});

			NRS.loadFollowedPollsSidebar(function() {
				followedPollsSidebar.find("a.active").removeClass("active");
				followedPollsSidebar.find("a[data-asset=" + newPolls[0].poll + "]").addClass("active").trigger("click");
			});
		}
	};

	NRS.saveFollowedPolls = function(polls, callback) {
		var newPollIds = [];
		var newPolls = [];

		$.each(polls, function(key, poll) {
			var newPoll = {
				"poll": String(poll.poll),
				"name": String(poll.name),
				"account": String(poll.account),
				"accountRS": String(poll.accountRS),
				"description": String(poll.description),
				"finishHeight": String(poll.finishHeight)
			};

			newPolls.push(newPoll);
			newPollIds.push({
				"poll": String(poll.poll)
			});
		});

		NRS.storageSelect("polls", newPollIds, function (error, existingPolls) {
			var existingIds = [];
			if (existingPolls.length) {
				$.each(existingPolls, function (index, poll) {
					existingIds.push(poll.poll);
				});
				newPoll = $.grep(newPolls, function (v) {
					return (existingIds.indexOf(v.poll) === -1);
				});
			}

			if (newPolls.length == 0) {
				if (callback) {
					callback([], polls);
				}
			} else {
				NRS.storageInsert("polls", "poll", newPolls, function () {
					$.each(newPolls, function (key, poll) {
						poll.name = poll.name.toLowerCase();
						NRS.followedPollIds.push(poll.poll);
						NRS.followedPolls.push(poll);
					});

					if (callback) {
						//for some reason we need to wait a little or DB won't be able to fetch inserted record yet..
						setTimeout(function () {
							callback(newPolls, polls);
						}, 50);
					}
				});
			}
		});
	};

	NRS.positionFollowedPollsSidebar = function() {
		var followedPollsSidebar = $("#followed_polls_sidebar");
        followedPollsSidebar.parent().css("position", "relative");
		followedPollsSidebar.parent().css("padding-bottom", "5px");
		followedPollsSidebar.height($(window).height() - 120);
	};

	//called on opening the page and automatic refresh
	NRS.loadFollowedPollsSidebar = function(callback) {
		var followedPollsPage = $("#followed_polls_page");
		var followedPollsSidebarContent = $("#followed_polls_sidebar_content");
        if (!NRS.followedPolls.length && requestedPoll == "") {
			NRS.pageLoaded(callback);
			followedPollsSidebarContent.empty();
			$("#no_poll_selected, #loading_poll_data, #no_poll_search_results").hide();
			$("#no_polls_available").show();
			followedPollsPage.addClass("no_polls");
			return;
		}

		var rows = "";
		followedPollsPage.removeClass("no_polls");
		NRS.positionFollowedPollsSidebar();
		NRS.followedPolls.sort(function(a, b) {
			if (a.name > b.name) {
				return 1;
			} else if (a.name < b.name) {
				return -1;
			} else {
				return 0;
			}
		});

		for (var i = 0; i < NRS.followedPolls.length; i++) {
			var poll = NRS.followedPolls[i];

			rows += "<a href='#' class='list-group-item list-group-item-ungrouped not_owns_asset" + "' ";
			rows += "data-cache='" + i + "' ";
			rows += "data-poll='" + NRS.escapeRespStr(poll.poll) + "' ";
			rows += "data-closed='false'>";
			rows += "<h4 class='list-group-item-heading'>" + poll.name.escapeHTML() + "</h4>";

			if(NRS.lastBlockHeight > parseInt(poll.finishHeight)) {
				rows += "<p class='list-group-item-text'><span data-i18n=\"completed\">Completed</span></p>";
			} else {
				rows += "<p class='list-group-item-text'><span data-i18n=\"blocks_left\">Blocks Left</span>: " + (parseInt(poll.finishHeight)-NRS.lastBlockHeight) + "</p>";
			}
			rows += "</a>";
		}

		var followedPollsSidebar = $("#followed_polls_sidebar");
        var active = followedPollsSidebar.find("a.active");

		if (active.length) {
			active = active.data("poll");
		} else {
			active = false;
		}
		followedPollsSidebarContent.empty().append(rows);

		if (active) {
			followedPollsSidebar.find("a[data-poll=" + active + "]").addClass("active");
		}
		$("#followed_polls_sidebar_search").hide();
		NRS.pageLoaded(callback);
	};

	NRS.incoming.followed_polls = function() {
		if (!NRS.viewingPoll) {
			//refresh active poll
			var $active = $("#followed_polls_sidebar").find("a.active");

			if ($active.length) {
				$active.trigger("click", [{
					"refresh": true
				}]);
			}
		} else {
			NRS.loadPoll(NRS.viewingPoll, true);
		}
	};

	$("#followed_polls_sidebar").on("click", "a", function(e, data) {
		e.preventDefault();
		//refresh is true if data is refreshed automatically by the system (when a new block arrives)
		var refresh = !!(data && data.refresh);
        var currentPollID = String($(this).data("poll")).escapeHTML();
        if (!currentPollID) {
            return;
        }

		NRS.storageSelect("polls", [{
			"poll": currentPollID
		}], function (error, poll) {
			if (poll && poll.length && poll[0].poll == currentPollID) {
				NRS.loadPoll(poll[0], refresh);
			}
		});
	});

	$("#followed_polls_sidebar_context").on("click", "a", function(e) {
		e.preventDefault();
		var pollId = NRS.selectedContext.data("poll");
		var option = $(this).data("option");
		NRS.closeContextMenu();
		if (option == "remove_from_bookmarks") {
			NRS.storageDelete("polls", [{
				"poll": pollId
			}], function () {
				setTimeout(function () {
					NRS.loadPage("followed_polls");
					$.growl($.t("success_poll_bookmark_removal"), {
						"type": "success"
					});
				}, 50);
			});
		}
	});

	NRS.loadPoll = function(poll, refresh) {
		var pollId = poll.poll;
		NRS.currentPoll = poll;
		NRS.currentSubPage = pollId;
		var pollLink = $("#followed_polls_sidebar").find("a[data-poll=" + pollId + "]");
		if (pollLink.length) {
			$("#followed_polls_bookmark_poll").hide();
		} else {
			$("#followed_polls_bookmark_poll").show();
			viewingPollBookmark = pollId;
		}
		requestedPoll = "";
		if (!refresh) {
            var followedPollsSidebar = $("#followed_polls_sidebar");
            followedPollsSidebar.find("a.active").removeClass("active");
			followedPollsSidebar.find("a[data-poll=" + pollId + "]").addClass("active");

			$("#no_poll_selected, #loading_poll_data, #no_polls_available, #no_poll_search_results").hide();
			$("#poll_details").show().parent().animate({
				"scrollTop": 0
			}, 0);

			$("#poll_account").html(NRS.getAccountLink(poll, "account"));
			$("#poll_id").html(NRS.getTransactionLink(pollId));

			$("#followed_polls_poll_name").html(NRS.escapeRespStr(poll.name));
			$("#poll_description").html(String(poll.description).autoLink());
			$(".poll_name").html(NRS.escapeRespStr(poll.name));
            var votePollLink = $("#vote_poll_link");
            votePollLink.find(".vote_button").data("poll", pollId);

			if(poll.finishHeight > NRS.lastBlockHeight) {
				votePollLink.show();
			} else {
				votePollLink.hide();
			}

            var followedPollsPollResults = $("#followed_polls_poll_results");
            followedPollsPollResults.find("tbody").empty();
            var followedPollsVotesCast = $("#followed_polls_votes_cast");
            followedPollsVotesCast.find("tbody").empty();
			followedPollsPollResults.parent().addClass("data-loading").removeClass("data-empty");
			followedPollsVotesCast.parent().addClass("data-loading").removeClass("data-empty");

			$(".data-loading img.loading").hide();

			setTimeout(function() {
				$(".data-loading img.loading").fadeIn(200);
			}, 200);

			NRS.sendRequest("getPoll", {
				"poll": pollId
			}, function(response) {
				if (!response.errorCode) {
					if (response.poll != poll.poll || response.account != poll.account || response.accountRS != poll.accountRS || response.description != poll.description || response.name != poll.name) {
						NRS.storageDelete("polls", [{
							"poll": poll.poll
						}], function() {
							setTimeout(function() {
								NRS.loadPage("followed_polls");
								$.growl("Invalid poll.", {
									"type": "danger"
								});
							}, 50);
						});
					}
				}
			});

			if (poll.viewingPoll) {
				$("#followed_polls_bookmark_this_poll").show();
				NRS.viewingPoll = poll;
			} else {
				$("#followed_polls_bookmark_this_poll").hide();
				NRS.viewingPoll = false;
			}
		}

		NRS.loadPollResults(pollId, refresh);
		NRS.loadPollVotes(pollId, refresh);
		_setVoteButtonStates();
	};

	NRS.loadPollResults = function(pollId, refresh) {
		NRS.sendRequest("getPoll+" + pollId, {
			"poll": pollId
		}, function(polldata) {
			NRS.sendRequest("getPollResult+" + pollId, {
				"poll": pollId
			}, function(response) {
                var rows = layoutPollResults(response, polldata);
                layoutPollChart(response, polldata);
                var followedPollsPollResults = $("#followed_polls_poll_results");
                followedPollsPollResults.find("tbody").empty().append(rows);
				NRS.dataLoadFinished(followedPollsPollResults, !refresh);
			});
		});
	};

	NRS.loadPollVotes = function(pollId, refresh) {
		NRS.sendRequest("getPoll+" + pollId, {
			"poll": pollId
		}, function(polldata) {
			var maxVotes = 50;
			NRS.sendRequest("getPollVotes+" + pollId, {
				"poll": pollId,
				"firstIndex": 0,
				"lastIndex": maxVotes
			}, function(votesdata) {
				var votes = votesdata.votes;
				if (!votes) {
					votes = [];
				}
                var followedPollsVotesCast = $("#followed_polls_votes_cast");
                if (votes.length) {
					var head = "";
					head += "<tr>";
					head += "<th data-i18n=\"voter\">Voter</th>";
					for(var b=0; b<polldata.options.length; b++) {
						head += "<th>"+String(NRS.escapeRespStr(polldata.options[b])) + "</th>";
					}
					head += "</tr>";
					followedPollsVotesCast.find("thead").empty().append(head);
                    var lengthStr;
					if (votes.length > maxVotes) {
						lengthStr = String(maxVotes) + "+";
						votes.pop();
					} else {
						lengthStr = String(votes.length);
					}
					$("#votes_cast_count").html("(" + lengthStr + ")");

					var rows = "";
					for (var i = 0; i < votes.length; i++) {
						rows += "<tr>";
						var vote = votes[i];
						rows += "<td><a href='#' class='user_info' data-user='" + NRS.getAccountFormatted(vote, "voter") + "'>" + NRS.getAccountTitle(vote, "voter") + "</td>";
						for (var a=0;a<vote.votes.length;a++) {
							rows += "<td>" + vote.votes[a] + "</td>";
						}
					}
					followedPollsVotesCast.find("tbody").empty().append(rows);
				} else {
					followedPollsVotesCast.find("tbody").empty();
					$("#votes_cast_count").html("(0)");
				}
				NRS.dataLoadFinished(followedPollsVotesCast, !refresh);
			});
		})
	};

	body.on("click", ".view_button[data-view]", function() {
	    requestedPoll = $(this).data("view");
		NRS.goToPage("followed_polls");
    });

	$("#followed_polls_bookmark_poll").on("click", function () {
		var $btn = $(this);
		NRS.sendRequest("getPoll", {"poll": viewingPollBookmark}, function(response) {
			NRS.saveFollowedPolls(new Array(response), NRS.forms.addFollowedPollsComplete);
			$btn.hide();
		});
	});

	NRS.finished_polls = function (table,full) {
		var finishedPollsTable = $("#" + table + "_table");
		finishedPollsTable.find("tbody").empty();
		finishedPollsTable.parent().addClass("data-loading").removeClass("data-empty");
		var params = {
			"finishedOnly": "true"
		};
		if (full) {
			params["firstIndex"] = NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage;
			params["lastIndex"] = NRS.pageNumber * NRS.itemsPerPage;
		} else {
			params["firstIndex"] = 0;
			params["lastIndex"] = 9;
		}
		var view = NRS.simpleview.get(table, {
			errorMessage: null,
			isLoading: true,
			isEmpty: false,
			data: []
		});
		NRS.sendRequest("getPolls", params, function (response) {
			var polls = response.polls;
			if (polls.length > NRS.itemsPerPage) {
				NRS.hasMorePages = true;
				polls.pop();
			}
			for (var i = 0; i < polls.length; i++) {
				var poll = polls[i];
				var description = NRS.escapeRespStr(poll.description);
				if (description.length > 100) {
					description = description.substring(0, 100) + "...";
				}
				var actions = '<a class="view_button btn btn-xs btn-default" href="#" data-view="' + poll.poll + '">' + $.t('view') + '</a>';
				view.data.push({
					"title": NRS.getTransactionLink(poll.poll, NRS.escapeRespStr(poll.name), true),
					"description": description,
					"sender": NRS.getAccountLink(poll, "account"),
					"timestamp": NRS.formatTimestamp(poll.timestamp),
					"actions": actions
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

