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

qrcodeDecoder = {};
qrcodeDecoder.callback = null;

qrcodeDecoder.decode = function() {
    var canvasElem = $('#qr-canvas');
    var canvas = canvasElem[0];
    var dataurl = canvas.toDataURL('image/jpeg');
    var regex = /base64,(.*)/;
    var base64Array = regex.exec(dataurl);
    if(base64Array == null) {
        return;
    }
    var base64 = base64Array[1];
    NRS.sendRequest("decodeQRCode", { "qrCodeBase64": base64 },
        function(response) {
            if(qrcode.callback != null && 'qrCodeData' in response) {
                if (response.qrCodeData == "") {
                    return;
                }
                qrcode.callback(response.qrCodeData);
            }
        },
        { isAsync: false, doNotEscape: true }
    );
};
