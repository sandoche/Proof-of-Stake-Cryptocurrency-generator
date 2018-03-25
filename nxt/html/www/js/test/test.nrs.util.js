/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
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

QUnit.module("nrs.util");

QUnit.test("convertToNXT", function (assert) {
    assert.equal(NRS.convertToNXT(200000000), "2", "whole");
    assert.equal(NRS.convertToNXT(20000000), "0.2", "fraction");
    assert.equal(NRS.convertToNXT(-200000000), "-2", "negative");
    assert.equal(NRS.convertToNXT(-20000000), "-0.2", "fraction.negative");
    assert.equal(NRS.convertToNXT(-220000000), "-2.2", "whole.fraction.negative");
    assert.equal(NRS.convertToNXT(2), "0.00000002", "nqt");
    assert.equal(NRS.convertToNXT(-2), "-0.00000002", "nqt.negative");
    assert.equal(NRS.convertToNXT(new BigInteger(String(2))), "0.00000002", "input.object");
    assert.equal(NRS.convertToNXT("hi"), "0.00000188", "alphanumeric"); // strange behavior of BigInteger don't do that
    assert.throws(function () {
        NRS.convertToNXT(null);
    }, {
        "message": "Cannot read property 'compareTo' of null",
        "name": "TypeError"
    }, "null.value");
});

QUnit.test("format", function (assert) {
    assert.equal(NRS.format("12345"), Number("12345").toLocaleString(), "escaped");
    assert.equal(NRS.format("12345", true), Number(12345).toLocaleString(), "not.escaped");
    assert.equal(NRS.format("-12345", false), Number(-12345).toLocaleString(), "neg");
    assert.equal(NRS.format("-12345", true), Number("-12345").toLocaleString(), "neg.not.escaped");
    assert.equal(NRS.format("-12345.67", true), Number("-12345.67").toLocaleString(), "decimal.not.good"); // bug ?
    assert.equal(NRS.format({ amount: 1234, negative: '-', mantissa: ".567"}, true), Number(-1234.567).toLocaleString(), "object");
    assert.equal(NRS.format("12.34", false, 4), "12.3400", "zero.pad");
    assert.equal(NRS.format("12", false, 4), "12.0000", "zero.pad.whole");
    assert.equal(NRS.format("12.", false, 4), "12.0000", "zero.pad.whole");
    assert.equal(NRS.format("12.34567", false, 4), "12.34567", "zero.pad.not.necessary");
    assert.equal(NRS.format("12", false, 0), "12", "zero.to.pad");
});

QUnit.test("formatAmount", function (assert) {
    assert.equal(NRS.formatAmount("12345", false, false), "0.00012345", "nqt");
    assert.equal(NRS.formatAmount("12345", true, false), "0.00012345", "nqt.rounding");
    assert.equal(NRS.formatAmount("1234500000", false, false), "12.345", "string");
    assert.equal(NRS.formatAmount("1234500000", true, false), "12.345", "string.no.rounding");
    assert.equal(NRS.formatAmount(12.345, false, false), "12.345", "number");
    assert.equal(NRS.formatAmount(12.345, true, false), "12.35", "number.rounding");
    assert.equal(NRS.formatAmount(12.343, true, false), "12.34", "number.rounding");
    assert.equal(NRS.formatAmount("123456700000", false, true), Number("1234.567").toLocaleString(), "1000separator");
    assert.equal(NRS.formatAmount("123456700000000", true, true), Number("1234567").toLocaleString(), "nxt.rounding");
    assert.equal(NRS.formatAmount("123456780000000", true, false), Number("1234567.8").toLocaleString(), "thousands.separator.escaped");
});

QUnit.test("formatVolume", function (assert) {
    assert.equal(NRS.formatVolume(1), "1 B", "byte");
    assert.equal(NRS.formatVolume(1000), "1'000 B", "thousand");
    assert.equal(NRS.formatVolume(1024), "1 KB", "kilo");
    assert.equal(NRS.formatVolume(1000000), "977 KB", "million");
    assert.equal(NRS.formatVolume(1024*1024), "1 MB", "million");
    assert.equal(NRS.formatVolume(2*1024*1024 + 3*1024 + 4), "2 MB", "combination");
});

QUnit.test("formatWeight", function (assert) {
    assert.equal(NRS.formatWeight(1), "1", "byte");
    assert.equal(NRS.formatWeight(1000), "1&#39;000", "thousand");
    assert.equal(NRS.formatWeight(12345), "12&#39;345", "number");
});

QUnit.test("calculateOrderPricePerWholeQNT", function (assert) {
    assert.equal(NRS.calculateOrderPricePerWholeQNT(100000000, 0), "1", "no.decimals.one");
    assert.equal(NRS.calculateOrderPricePerWholeQNT(1, 4), "0.0001", "fraction");
    assert.equal(NRS.calculateOrderPricePerWholeQNT(-123400000000, 8), "-123400000000", "eight.decimals");
    assert.equal(NRS.calculateOrderPricePerWholeQNT(-123400000000, 4), "-12340000", "four.decimals");
    assert.equal(NRS.calculateOrderPricePerWholeQNT(-123400000000, 0), "-1234", "no.decimals");
});

QUnit.test("formatOrderPricePerWholeQNT", function (assert) {
    assert.equal(NRS.formatOrderPricePerWholeQNT(100000000, 0), "1", "no.decimals.one");
    assert.equal(NRS.formatOrderPricePerWholeQNT(1, 4), "0.0001", "fraction");
    assert.equal(NRS.formatOrderPricePerWholeQNT(-123400000000, 8), Number("-123400000000".escapeHTML()).toLocaleString(), "eight.decimals");
    assert.equal(NRS.formatOrderPricePerWholeQNT(-123400000000, 4), Number("-12340000".escapeHTML()).toLocaleString(), "four.decimals");
    assert.equal(NRS.formatOrderPricePerWholeQNT(-123400000000, 0), Number("-1234".escapeHTML()).toLocaleString(), "no.decimals");
});

QUnit.test("calculatePricePerWholeQNT", function (assert) {
    assert.equal(NRS.calculatePricePerWholeQNT(100000000, 0), "100000000", "no.decimals.one");
    assert.equal(NRS.calculatePricePerWholeQNT(100000000, 4), "10000", "four.decimals");
    assert.equal(NRS.calculatePricePerWholeQNT(100000000, 8), "1", "eight.decimals");
    assert.equal(NRS.calculatePricePerWholeQNT(-123400000000, 8), "-1234".escapeHTML(), "eight.decimals");
    assert.equal(NRS.calculatePricePerWholeQNT(-123400000000, 4), "-12340000".escapeHTML(), "four.decimals");
    assert.equal(NRS.calculatePricePerWholeQNT(-123400000000, 0), "-123400000000".escapeHTML(), "no.decimals");
    assert.throws(function () {
        NRS.calculatePricePerWholeQNT(100000001, 8);
    }, "Invalid input.", "invalid.input");
});

QUnit.test("calculateOrderTotalNQT", function (assert) {
    assert.equal(NRS.calculateOrderTotalNQT(12, 34), "408", "multiplication");
});

QUnit.test("calculateOrderTotal", function (assert) {
    assert.equal(NRS.calculateOrderTotal(12, 34), "0.00000408", "multiplication");
});

QUnit.test("calculatePercentage", function (assert) {
    assert.equal(NRS.calculatePercentage(6, 15), "40.00", "pct1");
    assert.equal(NRS.calculatePercentage(5, 15), "33.33", "pct1");
    assert.equal(NRS.calculatePercentage(10, 15), "66.67", "pct3");
    assert.equal(NRS.calculatePercentage(10, 15, 0), "66.66", "pct3.round0");
    assert.equal(NRS.calculatePercentage(10, 15, 1), "66.67", "pct3.round1");
    assert.equal(NRS.calculatePercentage(10, 15, 2), "66.67", "pct3.round2");
    assert.equal(NRS.calculatePercentage(10, 15, 3), "66.67", "pct3.round3");
});

QUnit.test("amountToPrecision", function (assert) {
    assert.equal(NRS.amountToPrecision(12, 0), "12", "multiplication");
    assert.equal(NRS.amountToPrecision(12., 0), "12", "multiplication");
    assert.equal(NRS.amountToPrecision(12.0, 0), "12", "multiplication");
    assert.equal(NRS.amountToPrecision(12.345600, 4), "12.3456", "multiplication");
    assert.equal(NRS.amountToPrecision(12.3456, 4), "12.3456", "multiplication");
    assert.equal(NRS.amountToPrecision(12.3456, 3), "12.345", "multiplication");
    assert.equal(NRS.amountToPrecision(12.3456, 2), "12.34", "multiplication");
    assert.equal(NRS.amountToPrecision(12.3006, 2), "12.30", "multiplication");
});

QUnit.test("convertToNQT", function (assert) {
    assert.equal(NRS.convertToNQT(1), "100000000", "one");
    assert.equal(NRS.convertToNQT(1.), "100000000", "one.dot");
    assert.equal(NRS.convertToNQT(1.0), "100000000", "one.dot.zero");
    assert.equal(NRS.convertToNQT(.1), "10000000", "dot.one");
    assert.equal(NRS.convertToNQT(0.1), "10000000", "zero.dot.one");
    assert.equal(NRS.convertToNQT("0.00000001"), "1", "nqt");
    assert.throws(function () {
        NRS.convertToNQT(0.00000001); // since it's passed as 1e-8
    }, "Invalid input.", "invalid.input");
});

QUnit.test("convertToQNTf", function (assert) {
    assert.equal(NRS.convertToQNTf(1, 0), "1", "one");
    assert.equal(NRS.convertToQNTf(1, 3), "0.001", "milli");
    assert.equal(NRS.convertToQNTf(1000, 3), "1", "three.decimals");
    assert.equal(NRS.convertToQNTf(1234567, 3), "1234.567", "multi");
    assert.deepEqual(NRS.convertToQNTf(1234567, 3, true), { amount: "1234", mantissa: ".567" }, "object");
});

QUnit.test("convertToQNT", function (assert) {
    assert.equal(NRS.convertToQNT(1, 0), "1", "one");
    assert.equal(NRS.convertToQNT(1, 3), "1000", "thousand");
    assert.equal(NRS.convertToQNT(1000, 3), "1000000", "million");
    assert.equal(NRS.convertToQNT(1.234, 3), "1234", "multi");
    assert.equal(NRS.convertToQNT(0.1234, 4), "1234", "decimal");
    assert.throws(function() { NRS.convertToQNT(0.12345, 4) }, "Fraction can only have 4 decimals max.", "too.many.decimals");
});

QUnit.test("formatQuantity", function (assert) {
    assert.equal(NRS.formatQuantity(1, 0), "1", "one");
    assert.equal(NRS.formatQuantity(10000000, 3, true), Number("10000").toLocaleString(), "thousand");
    assert.equal(NRS.formatQuantity(1234, 2, true), Number("12.34").toLocaleString(), "thousand");
    assert.equal(NRS.formatQuantity(123456, 2, true), Number("1234.56").toLocaleString(), "thousand");
    assert.equal(NRS.formatQuantity(1234567, 2, true), Number("12345.67").toLocaleString(), "thousand");
});

QUnit.test("formatAmount", function (assert) {
    assert.equal(NRS.formatAmount(1), "1", "one");
    assert.equal(NRS.formatAmount(10000000, false, true), Number("10000000").toLocaleString(), "million");
    assert.equal(NRS.formatAmount(12.34, true), Number("12.34").toLocaleString(), "thousand");
    assert.equal(NRS.formatAmount(12.345, true), Number("12.35").toLocaleString(), "thousand");
});

QUnit.test("formatTimestamp", function (assert) {
    var date = new Date(0);
    assert.equal(NRS.formatTimestamp(0, true, true), date.toLocaleDateString(), "start.date");
});

QUnit.test("getAccountLink", function (assert) {
    NRS.contacts = {};

    assert.equal(NRS.getAccountLink({}, "dummy"), "/", "non.existing");
    assert.equal(NRS.getAccountLink({ entity: 5873880488492319831 }, "entity"), "<a href='#' data-user='NXT-XKA2-7VJU-VZSY-7R335' class='show_account_modal_action user-info'>/</a>", "numeric");
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>NXT-XK4R-7VJU-6EQG-7R335</a>", "RS");
    assert.equal(NRS.getAccountLink({ entity: 5873880488492319831, entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>NXT-XK4R-7VJU-6EQG-7R335</a>", "numeric.and.RS");
    NRS.contacts = { "NXT-XK4R-7VJU-6EQG-7R335": { name: "foo" }};
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>foo</a>", "contact");
    NRS.accountRS = "NXT-XK4R-7VJU-6EQG-7R335";
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>You</a>", "you");
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity", "NXT-XK4R-7VJU-6EQG-7R335", "account"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>Account</a>", "force.account.name");
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity", undefined, undefined, true), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info'>NXT-XK4R-7VJU-6EQG-7R335</a>", "maintain.rs.format");
    assert.equal(NRS.getAccountLink({ entityRS: "NXT-XK4R-7VJU-6EQG-7R335" }, "entity", undefined, undefined, undefined, "btn btn-xs"), "<a href='#' data-user='NXT-XK4R-7VJU-6EQG-7R335' class='show_account_modal_action user-info btn btn-xs'>You</a>", "add.class");
    NRS.contacts = null;
    NRS.accountRS = null;
    NRS.constants.GENESIS = 1739068987193023818;
    NRS.constants.GENESIS_RS = "NXT-MR8N-2YLS-3MEQ-3CMAJ";
    assert.equal(NRS.getAccountLink({ entityRS: NRS.constants.GENESIS_RS }, "entity"), "<a href='#' data-user='NXT-MR8N-2YLS-3MEQ-3CMAJ' class='show_account_modal_action user-info'>Genesis</a>", "genesis");
});

QUnit.test("generateToken", function (assert) {
    NRS.constants.EPOCH_BEGINNING = 1385294400000;
    var token = NRS.generateToken("myToken", "rshw9abtpsa2");
    assert.ok(token.indexOf("e9cl0jgba7lnp7gke9rdp7hg3uvcl5cnd23") == 0);
    assert.equal(token.length, 160);
});

QUnit.test("utf8", function (assert) {
    // compare the two UTF8 conversion methods
    var str = "Hello World";
    var bytes1 = NRS.getUtf8Bytes(str);
    var bytes2 = NRS.strToUTF8Arr(str);
    assert.deepEqual(bytes1, bytes2);
    // Hebrew
    str = "אבג";
    bytes1 = NRS.getUtf8Bytes(str);
    bytes2 = NRS.strToUTF8Arr(str);
    assert.deepEqual(bytes1, bytes2);
    // Chinese Simplified
    str = "简体中文网页";
    bytes1 = NRS.getUtf8Bytes(str);
    bytes2 = NRS.strToUTF8Arr(str);
    assert.deepEqual(bytes1, bytes2);
    // Chinese Traditional
    str = "繁體中文網頁";
    bytes1 = NRS.getUtf8Bytes(str);
    bytes2 = NRS.strToUTF8Arr(str);
    assert.deepEqual(bytes1, bytes2);
});

QUnit.test("versionCompare", function (assert) {
    assert.equal(NRS.versionCompare("1.6.4", "1.7.5"), "-1", "after");
    assert.equal(NRS.versionCompare("1.7.5", "1.6.4"), "1", "before");
    assert.equal(NRS.versionCompare("1.6.4", "1.6.4"), "0", "same");
    assert.equal(NRS.versionCompare("1.6.4e", "1.6.5e"), "-1", "after.e");
    assert.equal(NRS.versionCompare("1.6.5e", "1.6.4e"), "1", "before.e");
    assert.equal(NRS.versionCompare("1.6.4e", "1.6.4e"), "0", "same.e");
    assert.equal(NRS.versionCompare("1.7.5", "1.8.0e"), "-1", "after.ga.vs.e");
    assert.equal(NRS.versionCompare("1.7.5e", "1.8.0"), "-1", "after.e.vs.ga");
    assert.equal(NRS.versionCompare("1.8.0e", "1.8.0"), "1", "same.e.before.ga");
});

QUnit.test("numberOfDecimals", function (assert) {
    NRS.getLocale();
    var rows = [{price: "1.23"}, {price: "1.234"}];
    assert.equal(NRS.getNumberOfDecimals(rows, "price"), 3, "no.callback");
    rows = [{price: "123000000"}, {price: "123400000"}];
    assert.equal(NRS.getNumberOfDecimals(rows, "price", function(val) {
        return NRS.formatAmount(val.price);
    }), 3, "with.callback");
});
