/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.user;

import nxt.Generator;
import nxt.crypto.Crypto;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentLinkedQueue;

final class User {

    private volatile String secretPhrase;
    private volatile byte[] publicKey;
    private volatile boolean isInactive;
    private final String userId;
    private final ConcurrentLinkedQueue<JSONStreamAware> pendingResponses = new ConcurrentLinkedQueue<>();
    private AsyncContext asyncContext;

    User(String userId) {
        this.userId = userId;
    }

    String getUserId() {
        return this.userId;
    }

    byte[] getPublicKey() {
        return publicKey;
    }

    String getSecretPhrase() {
        return secretPhrase;
    }

    boolean isInactive() {
        return isInactive;
    }

    void setInactive(boolean inactive) {
        this.isInactive = inactive;
    }

    void enqueue(JSONStreamAware response) {
        pendingResponses.offer(response);
    }

    void lockAccount() {
        Generator.stopForging(secretPhrase);
        secretPhrase = null;
    }

    long unlockAccount(String secretPhrase) {
        this.publicKey = Crypto.getPublicKey(secretPhrase);
        this.secretPhrase = secretPhrase;
        return Generator.startForging(secretPhrase).getAccountId();
    }

    synchronized void processPendingResponses(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONArray responses = new JSONArray();
        JSONStreamAware pendingResponse;
        while ((pendingResponse = pendingResponses.poll()) != null) {
            responses.add(pendingResponse);
        }
        if (responses.size() > 0) {
            JSONObject combinedResponse = new JSONObject();
            combinedResponse.put("responses", responses);
            if (asyncContext != null) {
                asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
                try (Writer writer = asyncContext.getResponse().getWriter()) {
                    combinedResponse.writeJSONString(writer);
                }
                asyncContext.complete();
                asyncContext = req.startAsync();
                asyncContext.addListener(new UserAsyncListener());
                asyncContext.setTimeout(5000);
            } else {
                resp.setContentType("text/plain; charset=UTF-8");
                try (Writer writer = resp.getWriter()) {
                    combinedResponse.writeJSONString(writer);
                }
            }
        } else {
            if (asyncContext != null) {
                asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
                try (Writer writer = asyncContext.getResponse().getWriter()) {
                    JSON.emptyJSON.writeJSONString(writer);
                }
                asyncContext.complete();
            }
            asyncContext = req.startAsync();
            asyncContext.addListener(new UserAsyncListener());
            asyncContext.setTimeout(5000);
        }
    }

    synchronized void send(JSONStreamAware response) {
        if (asyncContext == null) {

            if (isInactive) {
                // user not seen recently, no responses should be collected
                return;
            }
            if (pendingResponses.size() > 1000) {
                pendingResponses.clear();
                // stop collecting responses for this user
                isInactive = true;
                if (secretPhrase == null) {
                    // but only completely remove users that don't have unlocked accounts
                    Users.remove(this);
                }
                return;
            }

            pendingResponses.offer(response);

        } else {

            JSONArray responses = new JSONArray();
            JSONStreamAware pendingResponse;
            while ((pendingResponse = pendingResponses.poll()) != null) {

                responses.add(pendingResponse);

            }
            responses.add(response);

            JSONObject combinedResponse = new JSONObject();
            combinedResponse.put("responses", responses);

            asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");

            try (Writer writer = asyncContext.getResponse().getWriter()) {
                combinedResponse.writeJSONString(writer);
            } catch (IOException e) {
                Logger.logMessage("Error sending response to user", e);
            }

            asyncContext.complete();
            asyncContext = null;

        }

    }


    private final class UserAsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent asyncEvent) throws IOException { }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {

            synchronized (User.this) {
                asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");

                try (Writer writer = asyncContext.getResponse().getWriter()) {
                    JSON.emptyJSON.writeJSONString(writer);
                }

                asyncContext.complete();
                asyncContext = null;
            }

        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException { }

        @Override
        public void onTimeout(AsyncEvent asyncEvent) throws IOException {

            synchronized (User.this) {
                asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");

                try (Writer writer = asyncContext.getResponse().getWriter()) {
                    JSON.emptyJSON.writeJSONString(writer);
                }

                asyncContext.complete();
                asyncContext = null;
            }

        }

    }

}
