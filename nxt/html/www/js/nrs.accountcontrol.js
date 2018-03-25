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

    NRS.forms.setPhasingOnlyControlComplete = function(response, data) {
        //NRS.getAccountInfo();
    };

	NRS.pages.account_control = function(callback) {
		
	}
	
	NRS.setup.account_control = function() {

	};

	$("#set_mandatory_approval_modal").on("show.bs.modal", function(e) {
		var $modal = $(this);

		$(".phasing_only_number_accounts_group").find("input[name=controlQuorum]").val(1);

		var context = {
			labelText: "Amount NXT",
			labelI18n: "amount_nxt",
			helpI18n: "approve_transaction_amount_help",
			inputName: "controlQuorumNXT",
			addonText: "NXT",
			addonI18n: "nxt_unit"
		}
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_amount_nxt', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		var context = {
			labelText: "Asset Quantity",
			labelI18n: "asset_quantity",
			helpI18n: "approve_transaction_amount_help",
			inputName: "controlQuorumQNTf",
			addonText: "Quantity",
			addonI18n: "quantity"
		}
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_asset_quantity', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		var context = {
			labelText: "Currency Units",
			labelI18n: "currency_units",
			helpI18n: "approve_transaction_amount_help",
			inputName: "controlQuorumQNTf",
			addonText: "Units",
			addonI18n: "units"
		}
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_currency_units', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		var context = {
			labelText: "Accounts (Whitelist)",
			labelI18n: "accounts_whitelist",
			helpI18n: "approve_transaction_accounts_requested_help",
			inputName: "controlWhitelisted"
		}
		var $elems = NRS.initModalUIElement($modal, '.add_phasing_only_whitelist_group', 'multi_accounts_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);

		var context = {
			labelText: "Min Balance Type",
			labelI18n: "min_balance_type",
			helpI18n: "approve_transaction_min_balance_type_help",
			selectName: "controlMinBalanceModel"
		}
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_min_balance_model_group', 'min_balance_model_modal_ui_element', context);
		$elems.find('select').prop("disabled", true);

		$elems.each(function(e){
			var $mbGroup = $(this).closest('div.phasing_only_min_balance_model_group');
			if ($mbGroup.hasClass("approve_mb_balance")) {
				$mbGroup.find('option[value="1"], option[value="2"], option[value="3"]').remove();
			}
			if ($mbGroup.hasClass("approve_mb_asset")) {
				$mbGroup.find('option[value="1"], option[value="3"]').remove();
			}
			if ($mbGroup.hasClass("approve_mb_currency")) {
				$mbGroup.find('option[value="1"], option[value="2"]').remove();
			}
		});

		var context = {
			labelText: "Min Balance",
			labelI18n: "min_balance",
			helpI18n: "approve_transaction_min_balance_help",
			inputName: "",
			addonText: "",
			addonI18n: ""
		}
		context['inputName'] = 'controlMinBalanceNXT';
		context['addonText'] = 'NXT';
		context['addonI18n'] = 'nxt_unit';
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_min_balance_nxt', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context['inputName'] = 'controlMinBalanceQNTf';
		context['addonText'] = 'Quantity';
		context['addonI18n'] = 'quantity';
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_min_balance_asset_quantity', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context['inputName'] = 'controlMinBalanceQNTf';
		context['addonText'] = 'Units';
		context['addonI18n'] = 'units';
		var $elems = NRS.initModalUIElement($modal, '.phasing_only_min_balance_currency_units', 'simple_input_with_addon_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context = {
			labelText: "Asset",
			labelI18n: "asset",
			inputIdName: "controlHolding",
			inputDecimalsName: "controlHoldingDecimals",
			helpI18n: "add_asset_modal_help"
		}
		$elems = NRS.initModalUIElement($modal, '.phasing_only_holding_asset', 'add_asset_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems = NRS.initModalUIElement($modal, '.phasing_only_holding_asset_optional', 'add_asset_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		context = {
			labelText: "Currency",
			labelI18n: "currency",
			inputCodeName: "controlHoldingCurrencyCode",
			inputIdName: "controlHolding",
			inputDecimalsName: "controlHoldingDecimals",
			helpI18n: "add_currency_modal_help"
		}
		$elems = NRS.initModalUIElement($modal, '.phasing_only_holding_currency', 'add_currency_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems = NRS.initModalUIElement($modal, '.phasing_only_holding_currency_optional', 'add_currency_modal_ui_element', context);
		$elems.find('input').prop("disabled", true);
		$elems.hide();

		$(this).find('.mandatory_approve_tab_list a:first').click();

		$(this).find('.phasing_duration_and_min_fees').hide();

		$(".show_popover").popover("hide");
	});

	$('.mandatory_approve_tab_list a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
		var $am = $(this).closest('.phasing_only_modal');
		$am.find('.tab-pane input, .tab-pane select').prop('disabled', true);
		$am.find('.tab-pane.active input, .tab-pane.active select').prop('disabled', false);
		if ($am.find('.tab-pane:first').hasClass("active")) {
			$am.find('.phasing_duration_and_min_fees').hide();
		} else {
			$am.find('.phasing_duration_and_min_fees').show();
		}

		$('.modal .phasing_only_modal .phasing_only_min_balance_model_group:visible select').trigger('change');
	});

	$('body').on('change', '.modal .phasing_only_modal .phasing_only_min_balance_model_group select', function(e) {
		var $tabPane = $(this).closest('div.tab_pane_approve');
		var mbModelId = $(this).val();
		for(var id=0; id<=3; id++) {
			$tabPane.find('.approve_mb_model_' + String(id) + ' input').attr('disabled', true);
			$tabPane.find('.approve_mb_model_' + String(id)).hide();
		}
		$tabPane.find('.approve_mb_model_' + String(mbModelId) + ' input').attr('disabled', false);
		$tabPane.find('.approve_mb_model_' + String(mbModelId)).show();
	});

	return NRS;
}(NRS || {}, jQuery));