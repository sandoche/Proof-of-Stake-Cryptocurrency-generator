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
 * @depends {3rdparty/jquery-2.1.0.js}
 */
var NRS = (function(NRS, $, undefined) {

	var _delay = (function(){
  		var timer = 0;
  		return function(callback, ms){
    		clearTimeout (timer);
    		timer = setTimeout(callback, ms);
  		};
	})();

    function _updateBlockHeightEstimates($bhmElem) {
		var $input = $bhmElem.find(' .bhm_ue_time_input');
		var blockHeight = $input.val();
		var output = "<i class='fa fa-clock-o'></i> " + NRS.getBlockHeightTimeEstimate(blockHeight) + " ";
		$bhmElem.find(".bhm_ue_time_estimate").html(output);
	}

	function _changeBlockHeightFromButton($btn, add) {
		var $bhmElem = $btn.closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		var numBlocks = parseInt($btn.data('bhmUeNumBlocks'));
		var $input = $bhmElem.find(' .bhm_ue_time_input');
		var blockHeight = parseInt($input.val());
		if(add) {
			$input.val(String(blockHeight + numBlocks));
		} else {
			$input.val(String(blockHeight - numBlocks));
		}
	}

	$("body").on("show.bs.modal", '.modal', function(e) {
		var $bhmElems = $(this).find('div[data-modal-ui-element="block_height_modal_ui_element"]');
		$bhmElems.each(function(key, bhmElem) {
			_updateBlockHeightEstimates($(bhmElem));
			$(bhmElem).find(".bhm_ue_current_block_height").html(String(NRS.lastBlockHeight));
			$(bhmElem).find(".bhm_ue_use_current_block_height").data('CurrentBlockHeight', NRS.lastBlockHeight);
		});
		var $algorithm = $(this).find('div[data-modal-ui-element="hash_algorithm_model_modal_ui_element"]');
		var $algoSelect = $algorithm.find('select');
		NRS.loadAlgorithmList($algoSelect, ($(this).attr('id') != 'hash_modal'));
	});
	$('body').on('keyup', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_time_input', function(e) {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_updateBlockHeightEstimates($bhmElem);
	});

	$('body').on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_use_current_block_height', function(e) {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		$bhmElem.find('.bhm_ue_time_input').val($(this).data('CurrentBlockHeight'));
		_updateBlockHeightEstimates($bhmElem);
	});

	$('body').on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_reduce_height_btn', function(e) {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_changeBlockHeightFromButton($(this), false);
		_updateBlockHeightEstimates($bhmElem);
	});

	$('body').on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_add_height_btn', function(e) {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_changeBlockHeightFromButton($(this), true);
		_updateBlockHeightEstimates($bhmElem);
	});


	//add_currency_modal_ui_element
	_currencyCode = null;
	_acmElem = null;
	_setAssetInfoNotExisting = function() {
		$(_acmElem).find('.acm_ue_currency_id').html($.t('not_existing', 'Not existing'));
		$(_acmElem).find('.acm_ue_currency_id_input').val("");
		$(_acmElem).find('.acm_ue_currency_id_input').prop("disabled", true);
		$(_acmElem).find('.acm_ue_currency_decimals_input').val("");
		$(_acmElem).find('.acm_ue_currency_decimals_input').prop("disabled", true);
	}

	_loadCurrencyInfoForCode = function() {
		if (_currencyCode && _currencyCode.length >= 3) {
			NRS.sendRequest("getCurrency", {
				"code": _currencyCode
			}, function(response) {
				if (response && response.currency) {
					var idString = String(response.currency) + "&nbsp; (" + $.t('decimals', 'Decimals') + ": " + String(response.decimals) + ")";
					$(_acmElem).find('.acm_ue_currency_id').html(idString);
					$(_acmElem).find('.acm_ue_currency_id_input').val(String(response.currency));
					$(_acmElem).find('.acm_ue_currency_id_input').prop("disabled", false);
					$(_acmElem).find('.acm_ue_currency_decimals').html(String(response.decimals));
					$(_acmElem).find('.acm_ue_currency_decimals_input').val(String(response.decimals));
					$(_acmElem).find('.acm_ue_currency_decimals_input').prop("disabled", false);
				} else {
					_setAssetInfoNotExisting();
				}
			});
		} else {
			_setAssetInfoNotExisting();
		}
	}

	$('body').on('keyup', '.modal div[data-modal-ui-element="add_currency_modal_ui_element"] .acm_ue_currency_code_input', function(e) {
		_acmElem = $(this).closest('div[data-modal-ui-element="add_currency_modal_ui_element"]');
		_currencyCode = $(this).val();
		_delay(_loadCurrencyInfoForCode, 1000 );
	});

	//add_asset_modal_ui_element
	_assetId = null;
	_aamElem = null;
	_setAssetInfoNotExisting = function() {
		$(_aamElem).find('.aam_ue_asset_name').html($.t('not_existing', 'Not existing'));
		$(_aamElem).find('.aam_ue_asset_decimals_input').val("");
		$(_aamElem).find('.aam_ue_asset_decimals_input').prop("disabled", true);
	}

	_loadAssetInfoForId = function() {
		if (_assetId && _assetId.length > 0) {
			NRS.sendRequest("getAsset", {
				"asset": _assetId
			}, function(response) {
				if (response && response.asset) {
					var nameString = String(response.name) + "&nbsp; (" + $.t('decimals', 'Decimals') + ": " + String(response.decimals) + ")";
					$(_aamElem).find('.aam_ue_asset_name').html(nameString);
					$(_aamElem).find('.aam_ue_asset_decimals_input').val(String(response.decimals));
					$(_aamElem).find('.aam_ue_asset_decimals_input').prop("disabled", false);
					$(_aamElem).find('.aam_ue_asset_name_input').val(response.name);
				} else {
					_setAssetInfoNotExisting();
				}
			});
		} else {
			_setAssetInfoNotExisting();
		}
	}

	$('body').on('keyup', '.modal div[data-modal-ui-element="add_asset_modal_ui_element"] .aam_ue_asset_id_input', function(e) {
		_aamElem = $(this).closest('div[data-modal-ui-element="add_asset_modal_ui_element"]');
		_assetId = $(this).val();
		_delay(_loadAssetInfoForId, 1000 );
	});

	//multi_accounts_modal_ui_element
	$('body').on('click', '.modal div[data-modal-ui-element="multi_accounts_modal_ui_element"] .add_account_btn', function(e) {
    	var $accountBox = $(this).closest('div[data-modal-ui-element="multi_accounts_modal_ui_element"]');
        var $clone = $accountBox.find(".form_group_multi_accounts_ue").first().clone();
        $clone.find("input").val("");
        $clone.find(".pas_contact_info").text("");
        $accountBox.find(".multi_accounts_ue_added_account_list").append($clone);
    });

    $('body').on('click', '.modal div[data-modal-ui-element="multi_accounts_modal_ui_element"] .remove_account_btn', function(e) {
    	e.preventDefault();
    	var $accountBox = $(this).closest('div[data-modal-ui-element="multi_accounts_modal_ui_element"]');
    	if ($accountBox.find(".form_group_multi_accounts_ue").length == 1) {
            return;
        }
        $(this).closest(".form_group_multi_accounts_ue").remove();
    });



	return NRS;
}(NRS || {}, jQuery));