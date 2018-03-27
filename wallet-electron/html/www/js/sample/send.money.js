var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    var data = {
        recipient: NRS.getAccountIdFromPublicKey(config.recipientPublicKey), // public key to account id
        recipientPublicKey: config.recipientPublicKey, // Optional - public key announcement to init a new account
        amountNQT: NRS.convertToNQT("1.234"), // NXT to NQT conversion
        secretPhrase: config.secretPhrase,
        encryptedMessageIsPrunable: "true" // Optional - make the attached message prunable
    };
    // Compose the request data
    data = Object.assign(
        data,
        NRS.getMandatoryParams(),
        NRS.encryptMessage(NRS, "note to myself", config.secretPhrase, NRS.getPublicKey(converters.stringToHexString(config.secretPhrase)), true),
        NRS.encryptMessage(NRS, "message to recipient", config.secretPhrase, config.recipientPublicKey, false)
    );
    // Submit the request to the remote node using the standard client function which performs local signing for transactions
    // and validates the data returned from the server.
    // This method will only send the passphrase to the server in requests for which the passphrase is required like startForging
    // It will never submit the passphrase for transaction requests
    NRS.sendRequest("sendMoney", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
