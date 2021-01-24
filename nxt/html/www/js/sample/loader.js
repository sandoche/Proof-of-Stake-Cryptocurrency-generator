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

try {
    var loader = require("./../nrs.node.bridge.js"); // during development
} catch(e) {
    console.log("Release mode");
}

try {
    loader = require("nxt-blockchain"); // when using the NPM module
} catch(e) {
    console.log("Development mode");
}

loader.config = require("./config.json");
var config = loader.config;

loader.init({
    url: config.url,
    secretPhrase: config.secretPhrase,
    isTestNet: config.isTestNet,
    adminPassword: config.adminPassword
});

module.exports = loader;