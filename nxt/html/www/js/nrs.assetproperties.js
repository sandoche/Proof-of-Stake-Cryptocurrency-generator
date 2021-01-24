/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
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

    NRS.pages.asset_properties = function() {
        var asset = NRS.getCurrentAsset().asset;
        $("#set_asset_property_btn").data("asset", asset);
        NRS.renderAssetProperties(asset);
    };

    NRS.renderAssetProperties = function(asset) {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('asset_properties_section', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            properties: []
        });
        var params = {
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        if (asset) {
            params.asset = asset;
        } else {
            params.setter = NRS.account;
        }
        NRS.sendRequest("getAssetProperties+", params,
            function(response) {
                if (response.properties.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.properties.pop();
                }
                view.properties.length = 0;
                response.properties.forEach(
                    function (propertiesJson) {
                        view.properties.push( NRS.jsondata.assetProperties(propertiesJson, asset) );
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.properties.length == 0,
                    header: asset ? $.t("asset") : $.t("setter")
                });
                NRS.pageLoaded();
            }
        );
    };

    NRS.normalizePropertyValue = function(value) {
        if (value == null) {
            return "";
        } else if (typeof value === "object") {
            return JSON.stringify(value);
        }
        return NRS.escapeRespStr(value);
    };

    NRS.jsondata.assetProperties = function (response, asset) {
        var updateAction = "";
        var deleteAction = "";
        var value = NRS.normalizePropertyValue(response.value);
        if (asset) {
            if (response.setterRS == NRS.accountRS) {
                deleteAction = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#delete_asset_property_modal' " +
                    "data-setter='" + response.setterRS + "' " +
                    "data-asset='" + asset + "' " +
                    "data-property='" + NRS.escapeRespStr(response.property) + "'>" + $.t("delete") + "</a>";

                updateAction = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#set_asset_property_modal' " +
                    "data-asset='" + asset + "' " +
                    "data-property='" + NRS.escapeRespStr(response.property) + "' " +
                    "data-value='" + value + "'>" + $.t("update") + "</a>";
            }
        } else {
            deleteAction = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#delete_asset_property_modal' " +
                "data-setter='" + NRS.accountRS + "' " +
                "data-asset='" + asset + "' " +
                "data-property='" + NRS.escapeRespStr(response.property) + "'>" + $.t("delete") + "</a>";

            updateAction = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#set_asset_property_modal' " +
                "data-asset='" + asset + "' " +
                "data-property='" + NRS.escapeRespStr(response.property) + "' " +
                "data-value='" + value + "'>" + $.t("update") + "</a>";
        }

        return {
            accountFormatted: asset ? NRS.getTransactionLink(asset) : NRS.getAccountLink(response, "setter"),
            property: NRS.escapeRespStr(response.property),
            value: value,
            action_update: updateAction,
            action_delete: deleteAction
        };
    };

    NRS.incoming.asset_properties = function() {
        NRS.loadPage("asset_properties");
    };

    $("#set_asset_property_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var asset = $invoker.data("asset");
        var assetInput = $("#set_asset_property_asset");
        if (asset) {
            assetInput.val(asset);
            assetInput.prop('readonly', true);
        } else {
            assetInput.prop('readonly', false);
        }
        var property = $invoker.data("property");
        var propertyInput = $("#set_asset_property_property");
        if (property) {
            propertyInput.val(property);
            propertyInput.prop('readonly', true);
        } else {
            propertyInput.prop('readonly', false);
        }
        $("#set_asset_property_value").val(NRS.normalizePropertyValue($invoker.data("value")));
    });

    $("#delete_asset_property_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var setter = $invoker.data("setter");
        if (setter) {
            var setterInput = $("#delete_asset_property_setter");
            setterInput.val(setter);
        }
        var asset = $invoker.data("asset");
        if (asset) {
            var assetInput = $("#delete_asset_property_asset");
            assetInput.val(asset);
        }
        var property = $invoker.data("property");
        if (property) {
            var propertyInput = $("#delete_asset_property_property");
            propertyInput.val(property);
        }
    });

    return NRS;
}(NRS || {}, jQuery));