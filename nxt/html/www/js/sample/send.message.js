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