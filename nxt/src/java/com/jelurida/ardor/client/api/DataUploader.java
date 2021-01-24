/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.http.callers.UploadTaggedDataCall;

import java.net.URL;

/**
 * Upload a file data to a remote node.
 * The code loads data and submits it as cloud data to a remote testnet node.
 */
public class DataUploader {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://testnxt.jelurida.com/nxt");
        DataUploader dataUploader = new DataUploader();
        dataUploader.upload(url);
    }

    private void upload(URL url) {
        byte[] bytes = "Hello World".getBytes();
        String name = this.getClass().getSimpleName();
        JO response = UploadTaggedDataCall.create().file(bytes).description("sample class").filename(name + ".class").channel("classes").tags("class").
                name(name).isText(false).remote(url).trustRemoteCertificate(true).secretPhrase(SECRET_PHRASE).feeNQT(100000000).call();
        System.out.println(response);
    }

}
