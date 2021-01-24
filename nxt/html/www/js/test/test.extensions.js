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

QUnit.module("extensions");

QUnit.test("escapeHtml", function (assert) {
    assert.equal("&\"<>'".escapeHTML(), "&amp;&quot;&lt;&gt;&#39;", "all.escaped.chars");
    assert.equal("<script>alert('hi');</script>".escapeHTML(), "&lt;script&gt;alert(&#39;hi&#39;);&lt;/script&gt;", "xss.prevention");
});

QUnit.test("autoLink", function (assert) {
    assert.equal("<script>alert('hi');</script>".autoLink(), "&lt;script&gt;alert(&#39;hi&#39;);&lt;/script&gt;", "xss.prevention");
    assert.equal("Click https://nxt.org".autoLink(), "Click <a href='https://nxt.org' target='_blank'>https://nxt.org</a>", "simple.link");
    assert.equal("https://www.google.co.il/?gfe_rd=cr&ei=X1J6Vu-QBceF8Qe6wKAY&gws_rd=ssl#q=%D7%A9%D7%9C%D7%95%D7%9D".autoLink(),
        "<a href='https://www.google.co.il/?gfe_rd=cr&amp;ei=X1J6Vu-QBceF8Qe6wKAY&amp;gws_rd=ssl#q=%D7%A9%D7%9C%D7%95%D7%9D' target='_blank'>https://www.google.co.il/?gfe_rd=cr&amp;ei=X1J6Vu-QBceF8Qe6wKAY&amp;gws_rd=ssl#q=%D7%A9%D7%9C%D7%95%D7%9D</a>",
        "complex.link");
});

