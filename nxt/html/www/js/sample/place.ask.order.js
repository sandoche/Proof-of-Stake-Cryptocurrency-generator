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
