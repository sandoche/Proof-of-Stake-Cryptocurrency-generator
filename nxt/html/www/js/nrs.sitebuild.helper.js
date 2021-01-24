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

/**
 * @depends {nrs.sitebuild.js}
 */

(function () {
    "use strict";
    var lastScript = $('script.sitebuild').last();
    var progress = $('#progress');

    var action = lastScript.attr('data-action');
    var path = lastScript.attr('data-path');

    if (action) {
        if (path !== null) {
            NRS[action](path);
        } else {
            NRS[action]();
        }
    }


    var total = parseInt(progress.attr('data-total'));
    var done = parseInt(progress.attr('data-done')) + 1;
    var progressPercent = (done / total) * 100;

    progress.attr('style', 'width: ' + progressPercent + '%');
    progress.attr('data-done', done);
})();

