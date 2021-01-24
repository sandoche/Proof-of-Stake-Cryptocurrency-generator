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

QUnit.module("nxt.localstorage");

function init() {
    NRS.localStorageDrop("items");
    var data = [];
    data.push({"id": "1", "name": "abc" });
    data.push({"id": "2", "name": "def" });
    data.push({"id": "3", "name": "ghi" });
    NRS.storageInsert("items", "id", data, function() {});
}

function initAutoInc() {
    NRS.localStorageDrop("items");
    var data = [];
    data.push({"key": "111", "value": "abc" });
    data.push({"key": "222", "value": "def" });
    data.push({"key": "333", "value": "ghi" });
    NRS.storageInsert("items", "key", data, function() {}, true);
}

QUnit.test("select", function (assert) {
    init();
    NRS.storageSelect("items", null, function (error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 3);
        assert.equal(response[0]["id"], "1");
        assert.equal(response[1]["id"], "2");
        assert.equal(response[2]["id"], "3");
    });
    NRS.storageSelect("items", [{id: "1"}, {id: "3"}], function (error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 2);
        assert.equal(response[0]["id"], "1");
        assert.equal(response[1]["id"], "3");
    });
    NRS.storageSelect("items", [{id: "1"}, {name: "def"}], function (error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 2);
        assert.equal(response[0]["id"], "1");
        assert.equal(response[1]["name"], "def");
    })
});

QUnit.test("select.non.existent", function (assert) {
    init();
    NRS.storageSelect("dummy", null, function (error, response) {
        assert.equal(error, "No items to select");
        assert.equal(response.length, 0);
    });
});

QUnit.test("insert", function (assert) {
    init();
    NRS.storageInsert("items", "id", [{"id": "4", "name": "zzz" }], function(error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 4);
        assert.equal(response[3]["id"], "4");
        assert.equal(response[3]["name"], "zzz");
    });
});

QUnit.test("insert.unique", function (assert) {
    init();
    NRS.storageInsert("items", "id", [{"id": "3", name: "qqq"}], function(error) {
        assert.equal(error, "Key already exists: 3");
    });
});

QUnit.test("insert.autoinc", function (assert) {
    NRS.localStorageDrop("items");
    var data = [];
    data.push({"id": "1", "name": "abc" });
    NRS.storageInsert("items", "id", data, function(error) {
        if (error) {
            assert.equal(error, "Cannot use auto increment id since data already contains id value");
        }
    }, true);
    initAutoInc();
    data = [];
    data.push({"key": "444", "value": "qqq" });
    NRS.storageInsert("items", "key", data, function(error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 4);
        assert.equal(response[0]["id"], 1);
        assert.equal(response[1]["id"], 2);
        assert.equal(response[2]["id"], 3);
        assert.equal(response[3]["id"], 4);
    }, true);
});

QUnit.test("update", function (assert) {
    init();
    NRS.storageUpdate("items", {"id": "3", name: "qqq"}, [{id: "3"}], function () {
        NRS.storageSelect("items", null, function (error, response) {
            assert.equal(error, null);
            assert.equal(response.length, 3);
            assert.equal(response[2]["name"], "qqq");
        })
    });
});

QUnit.test("update.autoinc", function (assert) {
    initAutoInc();
    NRS.storageUpdate("items", {"key": "333", value: "qqq"}, [{key: "333"}], function () {
        NRS.storageSelect("items", null, function (error, response) {
            assert.equal(error, null);
            assert.equal(response.length, 3);
            assert.equal(response[2]["value"], "qqq");
            assert.equal(response[2]["id"], 3);
        })
    });
});

QUnit.test("delete", function (assert) {
    init();
    NRS.storageDelete("items", [{"id": "2"}], function () {
        NRS.storageSelect("items", null, function (error, response) {
            assert.equal(error, null);
            assert.equal(response.length, 2);
            assert.equal(response[0]["id"], "1");
            assert.equal(response[1]["id"], "3");
        })
    });
    NRS.storageDelete("items", [{"id": "4"}], function () {
        NRS.storageSelect("items", null, function (error, response) {
            assert.equal(error, null);
            assert.equal(response.length, 2);
        })
    });
    NRS.storageDelete("items", [{"id": "3"}], function () {
        NRS.storageSelect("items", null, function (error, response) {
            assert.equal(error, null);
            assert.equal(response.length, 1);
            assert.equal(response[0]["id"], "1");
        })
    });
    NRS.localStorageDrop("items");
    NRS.storageDelete("items", null, function (error, response) {
        assert.equal(error, null);
        assert.equal(response.length, 0);
    });
});
