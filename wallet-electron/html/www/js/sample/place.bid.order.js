var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    const decimals = 2;
    var quantity = 123.45;
    var price = 1.2;
    var data = {
        asset: "6094526212840718212",
        quantityQNT: NRS.convertToQNT(quantity, decimals),
        priceNQT: NRS.calculatePricePerWholeQNT(NRS.convertToNQT(price), decimals),
        secretPhrase: config.secretPhrase
    };
    data = Object.assign(
        data,
        NRS.getMandatoryParams()
    );
    NRS.sendRequest("placeBidOrder", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
