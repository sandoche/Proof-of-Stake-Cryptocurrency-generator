/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
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

QUnit.module("nrs.encryption");

QUnit.test("generatePublicKey", function (assert) {
    assert.throws(function() { NRS.generatePublicKey("") }, { message: "Can't generate public key without the user's password." }, "empty.public.key");
    assert.equal(NRS.generatePublicKey("12345678"), "a65ae5bc3cdaa9a0dd66f2a87459bbf663140060e99ae5d4dfe4dbef561fdd37", "public.key");
    assert.equal(NRS.generatePublicKey("hope peace happen touch easy pretend worthless talk them indeed wheel state"), "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b", "public.key");
});

QUnit.test("getPublicKey", function (assert) {
    var publicKey1 = NRS.getPublicKey(converters.stringToHexString("12345678"));
    assert.equal(publicKey1, "a65ae5bc3cdaa9a0dd66f2a87459bbf663140060e99ae5d4dfe4dbef561fdd37", "public.key");
});

QUnit.test("getAccountIdFromPublicKey", function (assert) {
    assert.equal(NRS.getAccountIdFromPublicKey("112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b", true), "NXT-XK4R-7VJU-6EQG-7R335", "account.rs");
    assert.equal(NRS.getAccountIdFromPublicKey("112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b", false), "5873880488492319831", "account.rs");
});

QUnit.test("getPrivateKey", function (assert) {
    assert.equal(NRS.getPrivateKey("12345678"), "e8797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f", "private.key");
});

QUnit.test("encryptDecryptNote", function (assert) {
    var senderPrivateKey = "rshw9abtpsa2";
    var senderPublicKeyHex = NRS.getPublicKey(converters.stringToHexString(senderPrivateKey));
    var receiverPrivateKey = "eOdBVLMgySFvyiTy8xMuRXDTr45oTzB7L5J";
    var receiverPublicKeyHex = NRS.getPublicKey(converters.stringToHexString(receiverPrivateKey));
    var encryptedNote = NRS.encryptNote("MyMessage", { publicKey: receiverPublicKeyHex }, senderPrivateKey);
    assert.equal(encryptedNote.message.length, 96, "message.length");
    assert.equal(encryptedNote.nonce.length, 64, "nonce.length");
    var decryptedNote = NRS.decryptNote(encryptedNote.message, {
        nonce: encryptedNote.nonce,
        publicKey: converters.hexStringToByteArray(senderPublicKeyHex)
    }, receiverPrivateKey);
    assert.equal(decryptedNote.message, "MyMessage", "decrypted");
});

QUnit.test("encryptDecryptData", function (assert) {
    var senderPassphrase = "rshw9abtpsa2";
    var senderPublicKeyHex = NRS.getPublicKey(converters.stringToHexString(senderPassphrase));
    var senderPrivateKeyHex = NRS.getPrivateKey(senderPassphrase);
    var receiverPassphrase = "eOdBVLMgySFvyiTy8xMuRXDTr45oTzB7L5J";
    var receiverPublicKeyHex = NRS.getPublicKey(converters.stringToHexString(receiverPassphrase));
    var receiverPrivateKeyHex = NRS.getPrivateKey(receiverPassphrase);
    var encryptedData = NRS.encryptDataRoof(converters.stringToByteArray("MyMessage"), {
        privateKey: converters.hexStringToByteArray(senderPrivateKeyHex),
        publicKey: converters.hexStringToByteArray(receiverPublicKeyHex)
    });
    assert.equal(encryptedData.data.length, 48, "message.length");
    assert.equal(encryptedData.nonce.length, 32, "nonce.length");
    var decryptedData = NRS.decryptDataRoof(encryptedData.data, {
        nonce: encryptedData.nonce,
        privateKey: converters.hexStringToByteArray(receiverPrivateKeyHex),
        publicKey: converters.hexStringToByteArray(senderPublicKeyHex)
    });
    assert.equal(decryptedData.message, "MyMessage", "decrypted");
    assert.equal(decryptedData.sharedKey.length, 64, "sharedKey");
});

// Based on testnet transaction 17867212180997536482
QUnit.test("getSharedKey", function (assert) {
    var privateKey = NRS.getPrivateKey("rshw9abtpsa2");
    var publicKey = "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b";
    var nonce = "67c2be503505d8e6498cd108a5f37c624899dcdae025276d720f608e54cf3177";
    var nonceBytes = converters.hexStringToByteArray(nonce);
    var sharedKeyBytes = NRS.getSharedKey(converters.hexStringToByteArray(privateKey), converters.hexStringToByteArray(publicKey), nonceBytes);
    // Make sure it's the same key produced by the server getSharedKey API
    assert.equal(converters.byteArrayToHexString(sharedKeyBytes), "68dd970a1144cc7595c745541b0318b08aa6ccd8121e061b378fc27ffc5e1cd1");
    var options = {};
    options.sharedKey = sharedKeyBytes;
    var encryptedMessage = "8adee4dee3e3311a631a29553140d177932cf0743c05846d897b24545d6839cbf368fc0b0eec628bfd69e95d006e3eb8";
    var decryptedMessage = NRS.decryptDataRoof(converters.hexStringToByteArray(encryptedMessage), options);
    assert.equal(decryptedMessage.message, "hello world");
    assert.equal(decryptedMessage.sharedKey, converters.byteArrayToHexString(sharedKeyBytes));
});

// Based on testnet transaction 2376600560388810797
QUnit.test("decryptCompressedText", function (assert) {
    var privateKey = NRS.getPrivateKey("rshw9abtpsa2");
    var publicKey = "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b";
    var nonce = "ca627f0252c6ca080067deedbed48f0a651789314fcbe8547815becce1d93cdc";
    var options = {
        privateKey: converters.hexStringToByteArray(privateKey),
        publicKey: converters.hexStringToByteArray(publicKey),
        nonce: converters.hexStringToByteArray(nonce)
    };
    var encryptedMessage = "a1b84c964ca98e0b2a57587c67286caf245d637c18f28938cf38544972dd30ccd3551db86cfceda21b750df076dce267";
    var decryptedMessage = NRS.decryptDataRoof(converters.hexStringToByteArray(encryptedMessage), options);
    assert.equal(decryptedMessage.message, "hello world");
});

// Based on testnet transaction 12445814829537070352
QUnit.test("decryptUncompressedText", function (assert) {
    var privateKey = NRS.getPrivateKey("rshw9abtpsa2");
    var publicKey = "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b";
    var nonce = "c9a707dbfab3d4b8188f6ee4e884fee459f39e1b45f7d7e8ee8ae1100be18854";
    var options = {
        privateKey: converters.hexStringToByteArray(privateKey),
        publicKey: converters.hexStringToByteArray(publicKey),
        nonce: converters.hexStringToByteArray(nonce),
        isCompressed: false
    };
    var encryptedMessage = "c0d97e7261a604f106ec3a17d1a650ef500747bab10e60f94958d1da6689abe9";
    var decryptedMessage = NRS.decryptDataRoof(converters.hexStringToByteArray(encryptedMessage), options);
    assert.equal(decryptedMessage.message.substring(0, 11), "hello world"); // messages less than 16 bytes long does not decrypt correctly
});

// Based on testnet transaction 16098450341097007976
QUnit.test("decryptCompressedBinary", function (assert) {
    var privateKey = NRS.getPrivateKey("rshw9abtpsa2");
    var publicKey = "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b";
    var nonce = "704227105cf3701e2c4e581e43cc4266e1230474443f777d43c75bf4454f0782";
    var options = {
        privateKey: converters.hexStringToByteArray(privateKey),
        publicKey: converters.hexStringToByteArray(publicKey),
        nonce: converters.hexStringToByteArray(nonce),
        isCompressed: true,
        isText: false
    };
    var encryptedMessage = "6c6dbf1aaa0ff170df9d0f15785e9d956d6c1e860288916c7a7651dfcc3b81678b6fb7afd667a2a4e759ea96e615a8ab";
    var decryptedMessage = NRS.decryptDataRoof(converters.hexStringToByteArray(encryptedMessage), options);
    assert.equal(converters.byteArrayToString(converters.hexStringToByteArray(decryptedMessage.message)), "hello world");
});

// Based on testnet transaction 15981469747709703862
QUnit.test("decryptUncompressedBinary", function (assert) {
    var privateKey = NRS.getPrivateKey("rshw9abtpsa2");
    var publicKey = "112e0c5748b5ea610a44a09b1ad0d2bddc945a6ef5edc7551b80576249ba585b";
    var nonce = "bf8a1aa744dd7c95f10efca5cb55f6275f59816357307faab8faac8bf96ec822";
    var options = {
        privateKey: converters.hexStringToByteArray(privateKey),
        publicKey: converters.hexStringToByteArray(publicKey),
        nonce: converters.hexStringToByteArray(nonce),
        isCompressed: false,
        isText: false
    };
    var encryptedMessage = "5e8b0414de46cdb34ac0ad6dd6a30cd31524f5f8c65ae7c1c0fcd463973aa6df";
    var decryptedMessage = NRS.decryptDataRoof(converters.hexStringToByteArray(encryptedMessage), options);
    assert.equal(converters.byteArrayToString(converters.hexStringToByteArray(decryptedMessage.message)).substring(0, 11), "hello world");
});

