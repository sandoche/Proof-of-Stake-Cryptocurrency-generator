// Auto generated code, do not modify
package nxt.http.callers;

import java.util.Arrays;
import java.util.List;

public enum ApiSpec {
    getLastExchanges(null, "currencies", "currencies", "currencies", "requireBlock", "requireLastBlock"),

    startFundingMonitor(null, "holdingType", "holding", "property", "amount", "threshold", "interval", "secretPhrase"),

    getExpectedAskOrders(null, "asset", "sortByPrice", "requireBlock", "requireLastBlock"),

    getAccountPublicKey(null, "account", "requireBlock", "requireLastBlock"),

    detectMimeType("file", "data", "filename", "isText"),

    getBlocks(null, "firstIndex", "lastIndex", "timestamp", "includeTransactions", "includeExecutedPhased", "requireBlock", "requireLastBlock"),

    getAssetsByIssuer(null, "account", "account", "account", "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    getExchangesByOffer(null, "offer", "includeCurrencyInfo", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAllOpenBidOrders(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    dgsPurchase(null, "goods", "priceNQT", "quantity", "deliveryDeadlineTimestamp", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAccountBlockCount(null, "account", "requireBlock", "requireLastBlock"),

    deleteAlias(null, "alias", "aliasName", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    decodeFileToken("file", "token"),

    getPlugins(null, ""),

    getDataTagsLike(null, "tagPrefix", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getFundingMonitor(null, "holdingType", "holding", "property", "secretPhrase", "includeMonitoredAccounts", "account", "adminPassword"),

    getPolls(null, "account", "firstIndex", "lastIndex", "timestamp", "includeFinished", "finishedOnly", "requireBlock", "requireLastBlock"),

    downloadTaggedData(null, "transaction", "retrieve", "requireBlock", "requireLastBlock"),

    getDataTags(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getPollVote(null, "poll", "account", "includeWeights", "requireBlock", "requireLastBlock"),

    getAssetDeletes(null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    addPeer(null, "peer"),

    getSharedKey(null, "account", "secretPhrase", "nonce"),

    decodeToken(null, "website", "token"),

    popOff(null, "numBlocks", "height", "keepTransactions"),

    getAccountPhasedTransactions(null, "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAvailableToBuy(null, "currency", "units", "requireBlock", "requireLastBlock"),

    getNextBlockGenerators(null, "limit"),

    getExpectedAssetDeletes(null, "asset", "account", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    startForging(null, "secretPhrase"),

    getTaggedDataExtendTransactions(null, "transaction", "requireBlock", "requireLastBlock"),

    getAssetAccounts(null, "asset", "height", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getCurrencyFounders(null, "currency", "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    currencyBuy(null, "currency", "rateNQT", "units", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    decodeQRCode(null, "qrCodeBase64"),

    getAllExchanges(null, "timestamp", "firstIndex", "lastIndex", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getCurrencyTransfers(null, "currency", "account", "firstIndex", "lastIndex", "timestamp", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getExpectedOrderCancellations(null, "requireBlock", "requireLastBlock"),

    eventRegister(null, "event", "event", "event", "add", "remove"),

    scan(null, "numBlocks", "height", "validate"),

    hexConvert(null, "string"),

    getPhasingOnlyControl(null, "account", "requireBlock", "requireLastBlock"),

    getDGSTagCount(null, "inStockOnly", "requireBlock", "requireLastBlock"),

    getOffer(null, "offer", "requireBlock", "requireLastBlock"),

    encodeQRCode(null, "qrCodeData", "width", "height"),

    getChannelTaggedData(null, "channel", "account", "firstIndex", "lastIndex", "includeData", "requireBlock", "requireLastBlock"),

    getAvailableToSell(null, "currency", "units", "requireBlock", "requireLastBlock"),

    cancelBidOrder(null, "order", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    shufflingCancel(null, "shuffling", "cancellingAccount", "shufflingStateHash", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAssetProperties(null, "asset", "setter", "property", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAccount(null, "account", "includeLessors", "includeAssets", "includeCurrencies", "includeEffectiveBalance", "requireBlock", "requireLastBlock"),

    blacklistAPIProxyPeer(null, "peer"),

    getPeer(null, "peer"),

    getAccountCurrentAskOrderIds(null, "account", "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getUnconfirmedTransactionIds(null, "account", "account", "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAccountShufflings(null, "account", "includeFinished", "includeHoldingInfo", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getExpectedSellOffers(null, "currency", "account", "sortByRate", "requireBlock", "requireLastBlock"),

    dgsPriceChange(null, "goods", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAliasesLike(null, "aliasPrefix", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    dgsListing("messageFile", "name", "description", "tags", "quantity", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getBidOrder(null, "order", "requireBlock", "requireLastBlock"),

    sendMessage(null, "recipient", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAllBroadcastedTransactions(null, "requireBlock", "requireLastBlock"),

    placeBidOrder(null, "asset", "quantityQNT", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAccountBlocks(null, "account", "timestamp", "firstIndex", "lastIndex", "includeTransactions", "requireBlock", "requireLastBlock"),

    getShuffling(null, "shuffling", "includeHoldingInfo", "requireBlock", "requireLastBlock"),

    setAPIProxyPeer(null, "peer"),

    getAccountCurrencies(null, "account", "currency", "height", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getExpectedTransactions(null, "account", "account", "account", "requireBlock", "requireLastBlock"),

    getAccountCurrentBidOrderIds(null, "account", "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAllPhasingOnlyControls(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    dgsRefund(null, "purchase", "refundNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAssetIds(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getTaggedData(null, "transaction", "includeData", "retrieve", "requireBlock", "requireLastBlock"),

    searchAccounts(null, "query", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAccountLedger(null, "account", "firstIndex", "lastIndex", "eventType", "event", "holdingType", "holding", "includeTransactions", "includeHoldingInfo", "requireBlock", "requireLastBlock"),

    getAccountAssets(null, "account", "asset", "height", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    deleteAccountProperty(null, "recipient", "property", "setter", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getBlockchainTransactions(null, "account", "timestamp", "type", "subtype", "firstIndex", "lastIndex", "numberOfConfirmations", "withMessage", "phasedOnly", "nonPhasedOnly", "includeExpiredPrunable", "includePhasingResult", "executedOnly", "requireBlock", "requireLastBlock"),

    sendMoney(null, "recipient", "amountNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    extendTaggedData("file", "transaction", "name", "description", "tags", "type", "channel", "isText", "filename", "data", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getMyInfo(null, ""),

    getAccountTaggedData(null, "account", "firstIndex", "lastIndex", "includeData", "requireBlock", "requireLastBlock"),

    getAllTrades(null, "timestamp", "firstIndex", "lastIndex", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getStackTraces(null, "depth"),

    rsConvert(null, "account"),

    searchTaggedData(null, "query", "tag", "channel", "account", "firstIndex", "lastIndex", "includeData", "requireBlock", "requireLastBlock"),

    getAllTaggedData(null, "firstIndex", "lastIndex", "includeData", "requireBlock", "requireLastBlock"),

    getDGSPendingPurchases(null, "seller", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getECBlock(null, "timestamp", "requireBlock", "requireLastBlock"),

    generateFileToken("file", "secretPhrase"),

    searchDGSGoods(null, "query", "tag", "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts", "requireBlock", "requireLastBlock"),

    getAccountPhasedTransactionCount(null, "account", "requireBlock", "requireLastBlock"),

    getCurrencyAccounts(null, "currency", "height", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    shufflingCreate(null, "holding", "holdingType", "amount", "participantCount", "registrationPeriod", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAlias(null, "alias", "aliasName", "requireBlock", "requireLastBlock"),

    getPhasingPolls(null, "transaction", "transaction", "transaction", "countVotes", "requireBlock", "requireLastBlock"),

    markHost(null, "secretPhrase", "host", "weight", "date"),

    canDeleteCurrency(null, "account", "currency", "requireBlock", "requireLastBlock"),

    managePeersNetworking(null, "operation"),

    getPhasingPollVote(null, "transaction", "account", "requireBlock", "requireLastBlock"),

    stopFundingMonitor(null, "holdingType", "holding", "property", "secretPhrase", "account", "adminPassword"),

    getTime(null, ""),

    buyAlias(null, "alias", "aliasName", "amountNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    searchPolls(null, "query", "firstIndex", "lastIndex", "includeFinished", "requireBlock", "requireLastBlock"),

    eventWait(null, "timeout"),

    castVote(null, "poll", "vote00", "vote01", "vote02", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getMintingTarget(null, "currency", "account", "units", "requireBlock", "requireLastBlock"),

    generateToken(null, "website", "secretPhrase"),

    longConvert(null, "id"),

    getBlockId(null, "height", "requireBlock", "requireLastBlock"),

    getLastTrades(null, "assets", "assets", "assets", "requireBlock", "requireLastBlock"),

    getExpectedBidOrders(null, "asset", "sortByPrice", "requireBlock", "requireLastBlock"),

    getBidOrderIds(null, "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getBlockchainStatus(null, ""),

    getConstants(null, ""),

    getTransaction(null, "transaction", "fullHash", "includePhasingResult", "requireBlock", "requireLastBlock"),

    getBlock(null, "block", "height", "timestamp", "includeTransactions", "includeExecutedPhased", "requireBlock", "requireLastBlock"),

    verifyTaggedData("file", "transaction", "name", "description", "tags", "type", "channel", "isText", "filename", "data", "requireBlock", "requireLastBlock"),

    getExchangesByExchangeRequest(null, "transaction", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getPrunableMessage(null, "transaction", "secretPhrase", "sharedKey", "retrieve", "requireBlock", "requireLastBlock"),

    dividendPayment(null, "holding", "holdingType", "asset", "height", "amountNQTPerQNT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    broadcastTransaction(null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON"),

    currencySell(null, "currency", "rateNQT", "units", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    blacklistPeer(null, "peer"),

    dgsDelivery(null, "purchase", "discountNQT", "goodsToEncrypt", "goodsIsText", "goodsData", "goodsNonce", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    setAccountProperty(null, "recipient", "property", "value", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getShufflers(null, "account", "shufflingFullHash", "secretPhrase", "adminPassword", "includeParticipantState"),

    getDGSGoodsPurchaseCount(null, "goods", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    sendTransaction(null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON"),

    getAssignedShufflings(null, "account", "includeHoldingInfo", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getGuaranteedBalance(null, "account", "numberOfConfirmations", "requireBlock", "requireLastBlock"),

    fullHashToId(null, "fullHash"),

    getExpectedBuyOffers(null, "currency", "account", "sortByRate", "requireBlock", "requireLastBlock"),

    getAskOrders(null, "asset", "firstIndex", "lastIndex", "showExpectedCancellations", "requireBlock", "requireLastBlock"),

    stopForging(null, "secretPhrase", "adminPassword"),

    getAccountExchangeRequests(null, "account", "currency", "includeCurrencyInfo", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    downloadPrunableMessage(null, "transaction", "secretPhrase", "sharedKey", "retrieve", "save", "requireBlock", "requireLastBlock"),

    getAsset(null, "asset", "includeCounts", "requireBlock", "requireLastBlock"),

    clearUnconfirmedTransactions(null, ""),

    getHoldingShufflings(null, "holding", "stage", "includeFinished", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAssetDividends(null, "asset", "firstIndex", "lastIndex", "timestamp", "includeHoldingInfo", "requireBlock", "requireLastBlock"),

    getAssetPhasedTransactions(null, "asset", "account", "withoutWhitelist", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAccountCurrentBidOrders(null, "account", "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    dgsQuantityChange(null, "goods", "deltaQuantity", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getExpectedCurrencyTransfers(null, "currency", "account", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    cancelAskOrder(null, "order", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    searchAssets(null, "query", "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    getDataTagCount(null, "requireBlock", "requireLastBlock"),

    bootstrapAPIProxy(null, ""),

    dgsDelisting(null, "goods", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    deleteCurrency(null, "currency", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAssetTransfers(null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getBalance(null, "account", "includeEffectiveBalance", "height", "requireBlock", "requireLastBlock"),

    getCurrencyPhasedTransactions(null, "currency", "account", "withoutWhitelist", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    setPhasingOnlyControl(null, "controlVotingModel", "controlQuorum", "controlMinBalance", "controlMinBalanceModel", "controlHolding", "controlWhitelisted", "controlWhitelisted", "controlWhitelisted", "controlMaxFees", "controlMinDuration", "controlMaxDuration", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getCurrencies(null, "currencies", "currencies", "currencies", "includeCounts", "requireBlock", "requireLastBlock"),

    getDGSGoods(null, "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts", "requireBlock", "requireLastBlock"),

    currencyReserveIncrease(null, "currency", "amountPerUnitNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    deleteAssetShares(null, "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    setLogging(null, "logLevel", "communicationEvent", "communicationEvent", "communicationEvent"),

    getAliasCount(null, "account", "requireBlock", "requireLastBlock"),

    getTransactionBytes(null, "transaction", "requireBlock", "requireLastBlock"),

    retrievePrunedTransaction(null, "transaction"),

    getExpectedAssetTransfers(null, "asset", "account", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getAllAssets(null, "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    getInboundPeers(null, "includePeerInfo"),

    hash(null, "hashAlgorithm", "secret", "secretIsText"),

    createPoll(null, "name", "description", "finishHeight", "votingModel", "minNumberOfOptions", "maxNumberOfOptions", "minRangeValue", "maxRangeValue", "minBalance", "minBalanceModel", "holding", "option00", "option01", "option02", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    verifyPrunableMessage(null, "transaction", "message", "messageIsText", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "compressMessageToEncrypt", "requireBlock", "requireLastBlock"),

    getDGSPurchase(null, "purchase", "secretPhrase", "sharedKey", "requireBlock", "requireLastBlock"),

    getReferencingTransactions(null, "transaction", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getForging(null, "secretPhrase", "adminPassword"),

    readMessage(null, "transaction", "secretPhrase", "sharedKey", "retrieve", "requireBlock", "requireLastBlock"),

    luceneReindex(null, ""),

    deleteAssetProperty(null, "asset", "property", "setter", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    fullReset(null, ""),

    getAccountBlockIds(null, "account", "timestamp", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getPollResult(null, "poll", "votingModel", "holding", "minBalance", "minBalanceModel", "requireBlock", "requireLastBlock"),

    getDGSPurchaseCount(null, "seller", "buyer", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    getAllWaitingTransactions(null, "requireBlock", "requireLastBlock"),

    decryptFrom(null, "account", "data", "nonce", "decryptedMessageIsText", "uncompressDecryptedMessage", "secretPhrase"),

    getAccountAssetCount(null, "account", "height", "requireBlock", "requireLastBlock"),

    getAssets(null, "assets", "assets", "assets", "includeCounts", "requireBlock", "requireLastBlock"),

    getCurrenciesByIssuer(null, "account", "account", "account", "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    getPeers(null, "active", "state", "service", "service", "service", "includePeerInfo"),

    getAllShufflings(null, "includeFinished", "includeHoldingInfo", "finishedOnly", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    placeAskOrder(null, "asset", "quantityQNT", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    rebroadcastUnconfirmedTransactions(null, ""),

    getAllCurrencies(null, "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    setAccountInfo(null, "name", "description", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getDGSGood(null, "goods", "includeCounts", "requireBlock", "requireLastBlock"),

    getAskOrderIds(null, "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAccountCurrencyCount(null, "account", "height", "requireBlock", "requireLastBlock"),

    decodeHallmark(null, "hallmark"),

    getAskOrder(null, "order", "requireBlock", "requireLastBlock"),

    getExpectedExchangeRequests(null, "account", "currency", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getCurrencyIds(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    shufflingProcess(null, "shuffling", "recipientSecretPhrase", "recipientPublicKey", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    requeueUnconfirmedTransactions(null, ""),

    signTransaction(null, "unsignedTransactionJSON", "unsignedTransactionBytes", "prunableAttachmentJSON", "secretPhrase", "validate", "requireBlock", "requireLastBlock"),

    getAliases(null, "timestamp", "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    trimDerivedTables(null, ""),

    getSellOffers(null, "currency", "account", "availableOnly", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getLog(null, "count"),

    getAssetHistory(null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "deletesOnly", "increasesOnly", "requireBlock", "requireLastBlock"),

    deleteScheduledTransaction(null, "transaction"),

    getAccountLedgerEntry(null, "ledgerId", "includeTransaction", "includeHoldingInfo"),

    transferAsset(null, "recipient", "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    stopShuffler(null, "account", "shufflingFullHash", "secretPhrase", "adminPassword"),

    publishExchangeOffer(null, "currency", "buyRateNQT", "sellRateNQT", "totalBuyLimit", "totalSellLimit", "initialBuySupply", "initialSellSupply", "expirationHeight", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getLinkedPhasedTransactions(null, "linkedFullHash", "requireBlock", "requireLastBlock"),

    approveTransaction(null, "transactionFullHash", "transactionFullHash", "transactionFullHash", "revealedSecret", "revealedSecretIsText", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getDGSTagsLike(null, "tagPrefix", "inStockOnly", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    parseTransaction(null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "requireBlock", "requireLastBlock"),

    getCurrency(null, "currency", "code", "includeCounts", "requireBlock", "requireLastBlock"),

    increaseAssetShares(null, "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getBidOrders(null, "asset", "firstIndex", "lastIndex", "showExpectedCancellations", "requireBlock", "requireLastBlock"),

    getDGSGoodsCount(null, "seller", "inStockOnly", "requireBlock", "requireLastBlock"),

    getCurrencyAccountCount(null, "currency", "height", "requireBlock", "requireLastBlock"),

    getDGSPurchases(null, "seller", "buyer", "firstIndex", "lastIndex", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    getShufflingParticipants(null, "shuffling", "requireBlock", "requireLastBlock"),

    getAccountLessors(null, "account", "height", "requireBlock", "requireLastBlock"),

    setAssetProperty(null, "asset", "property", "value", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getScheduledTransactions(null, "account"),

    startShuffler(null, "secretPhrase", "shufflingFullHash", "recipientSecretPhrase", "recipientPublicKey"),

    getPoll(null, "poll", "requireBlock", "requireLastBlock"),

    getVoterPhasedTransactions(null, "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    transferCurrency(null, "recipient", "currency", "units", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    leaseBalance(null, "period", "recipient", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    setAlias(null, "aliasName", "aliasURI", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    shutdown(null, "scan"),

    getDGSExpiredPurchases(null, "seller", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    searchCurrencies(null, "query", "firstIndex", "lastIndex", "includeCounts", "requireBlock", "requireLastBlock"),

    shufflingRegister(null, "shufflingFullHash", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    currencyReserveClaim(null, "currency", "units", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getPollVotes(null, "poll", "firstIndex", "lastIndex", "includeWeights", "requireBlock", "requireLastBlock"),

    getAccountCurrentAskOrders(null, "account", "asset", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    scheduleCurrencyBuy(null, "currency", "rateNQT", "units", "offerIssuer", "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "adminPassword", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getDGSTags(null, "inStockOnly", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getOrderTrades(null, "askOrder", "bidOrder", "includeAssetInfo", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getEpochTime(null, "unixtime"),

    sellAlias(null, "alias", "aliasName", "recipient", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    dumpPeers(null, "version", "weight", "connect", "adminPassword"),

    getAllOpenAskOrders(null, "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getAllPrunableMessages(null, "firstIndex", "lastIndex", "timestamp", "requireBlock", "requireLastBlock"),

    dgsFeedback(null, "purchase", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getPhasingPoll(null, "transaction", "countVotes", "requireBlock", "requireLastBlock"),

    shufflingVerify(null, "shuffling", "shufflingStateHash", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getDGSGoodsPurchases(null, "goods", "buyer", "firstIndex", "lastIndex", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    getAssetAccountCount(null, "asset", "height", "requireBlock", "requireLastBlock"),

    getPhasingPollVotes(null, "transaction", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    retrievePrunedData(null, ""),

    getUnconfirmedTransactions(null, "account", "account", "account", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    encryptTo(null, "recipient", "messageToEncrypt", "messageToEncryptIsText", "compressMessageToEncrypt", "secretPhrase"),

    getBuyOffers(null, "currency", "account", "availableOnly", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getState(null, "includeCounts", "adminPassword"),

    issueCurrency(null, "name", "code", "description", "type", "initialSupply", "reserveSupply", "maxSupply", "issuanceHeight", "minReservePerUnitNQT", "minDifficulty", "maxDifficulty", "ruleset", "algorithm", "decimals", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAccountId(null, "secretPhrase", "publicKey"),

    issueAsset(null, "name", "description", "quantityQNT", "decimals", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getTrades(null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getPrunableMessages(null, "account", "otherAccount", "secretPhrase", "firstIndex", "lastIndex", "timestamp", "requireBlock", "requireLastBlock"),

    calculateFullHash(null, "unsignedTransactionBytes", "unsignedTransactionJSON", "signatureHash"),

    currencyMint(null, "currency", "nonce", "units", "counter", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    uploadTaggedData("file", "name", "description", "tags", "type", "channel", "isText", "filename", "data", "secretPhrase", "publicKey", "feeNQT", "deadline", "referencedTransactionFullHash", "broadcast", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "recipientPublicKey", "ecBlockId", "ecBlockHeight"),

    getAccountProperties(null, "recipient", "property", "setter", "firstIndex", "lastIndex", "requireBlock", "requireLastBlock"),

    getExchanges(null, "currency", "account", "firstIndex", "lastIndex", "timestamp", "includeCurrencyInfo", "requireBlock", "requireLastBlock");

    private final String fileParameter;

    private final List<String> parameters;

    ApiSpec(String fileParameter, String... parameters) {
        this.fileParameter = fileParameter;
        this.parameters = Arrays.asList(parameters);}

    public String getFileParameter() {
        return fileParameter;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
