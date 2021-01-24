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
    var data = {
        recipient: NRS.getAccountIdFromPublicKey(config.recipientPublicKey),
        secretPhrase: config.secretPhrase,
        encryptedMessageIsPrunable: "true"
    };
    data = Object.assign(
        data,
        NRS.getMandatoryParams(),
        NRS.encryptMessage(NRS, "message to recipient", config.secretPhrase, config.recipientPublicKey, false)
    );
    NRS.sendRequest("sendMessage", data, function (response) {
        NRS.logConsole("sendMessage1 response:" + JSON.stringify(response));
        // Now send a response message
        var senderSecretPhrase = "rshw9abtpsa2";
        loader.setCurrentAccount(senderSecretPhrase); // change the account which submits the transactions
        var data = {
            recipient: NRS.getAccountId(config.secretPhrase),
            secretPhrase: senderSecretPhrase,
            encryptedMessageIsPrunable: "true"
        };
        data = Object.assign(
            data,
            NRS.getMandatoryParams(),
            NRS.encryptMessage(NRS, "response message", senderSecretPhrase, NRS.getPublicKey(converters.stringToHexString(config.secretPhrase), false), false)
        );
        NRS.sendRequest("sendMessage", data, function (response) {
            NRS.logConsole("sendMessage2 response:" + JSON.stringify(response));
        });
    });
});