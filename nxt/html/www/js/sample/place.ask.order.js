/******************************************************************************
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

var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    const decimals = 2;
    var quantity = 2.5;
    var price = 1.3;
    var data = {
        asset: "6094526212840718212", // testnet Megasset
        quantityQNT: NRS.convertToQNT(quantity, decimals),
        priceNQT: NRS.calculatePricePerWholeQNT(NRS.convertToNQT(price), decimals),
        secretPhrase: config.secretPhrase
    };
    data = Object.assign(
        data,
        NRS.getMandatoryParams()
    );
    NRS.sendRequest("placeAskOrder", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
