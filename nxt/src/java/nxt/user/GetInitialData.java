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

import nxt.Block;
import nxt.Constants;
import nxt.Nxt;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;

public final class GetInitialData extends UserServlet.UserRequestHandler {

    static final GetInitialData instance = new GetInitialData();

    private GetInitialData() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {

        JSONArray unconfirmedTransactions = new JSONArray();
        JSONArray activePeers = new JSONArray(), knownPeers = new JSONArray(), blacklistedPeers = new JSONArray();
        JSONArray recentBlocks = new JSONArray();

        try (DbIterator<? extends Transaction> transactions = Nxt.getTransactionProcessor().getAllUnconfirmedTransactions()) {
            while (transactions.hasNext()) {
                Transaction transaction = transactions.next();

                JSONObject unconfirmedTransaction = new JSONObject();
                unconfirmedTransaction.put("index", Users.getIndex(transaction));
                unconfirmedTransaction.put("timestamp", transaction.getTimestamp());
                unconfirmedTransaction.put("deadline", transaction.getDeadline());
                unconfirmedTransaction.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
                unconfirmedTransaction.put("amountNQT", transaction.getAmountNQT());
                unconfirmedTransaction.put("feeNQT", transaction.getFeeNQT());
                unconfirmedTransaction.put("sender", Long.toUnsignedString(transaction.getSenderId()));
                unconfirmedTransaction.put("id", transaction.getStringId());

                unconfirmedTransactions.add(unconfirmedTransaction);
            }
        }

        for (Peer peer : Peers.getAllPeers()) {

            if (peer.isBlacklisted()) {

                JSONObject blacklistedPeer = new JSONObject();
                blacklistedPeer.put("index", Users.getIndex(peer));
                blacklistedPeer.put("address", peer.getHost());
                blacklistedPeer.put("announcedAddress", Convert.truncate(peer.getAnnouncedAddress(), "-", 25, true));
                blacklistedPeer.put("software", peer.getSoftware());
                blacklistedPeers.add(blacklistedPeer);

            } else if (peer.getState() == Peer.State.NON_CONNECTED) {

                JSONObject knownPeer = new JSONObject();
                knownPeer.put("index", Users.getIndex(peer));
                knownPeer.put("address", peer.getHost());
                knownPeer.put("announcedAddress", Convert.truncate(peer.getAnnouncedAddress(), "-", 25, true));
                knownPeer.put("software", peer.getSoftware());
                knownPeers.add(knownPeer);

            } else {

                JSONObject activePeer = new JSONObject();
                activePeer.put("index", Users.getIndex(peer));
                if (peer.getState() == Peer.State.DISCONNECTED) {
                    activePeer.put("disconnected", true);
                }
                activePeer.put("address", peer.getHost());
                activePeer.put("announcedAddress", Convert.truncate(peer.getAnnouncedAddress(), "-", 25, true));
                activePeer.put("weight", peer.getWeight());
                activePeer.put("downloaded", peer.getDownloadedVolume());
                activePeer.put("uploaded", peer.getUploadedVolume());
                activePeer.put("software", peer.getSoftware());
                activePeers.add(activePeer);
            }
        }

        try (DbIterator<? extends Block> lastBlocks = Nxt.getBlockchain().getBlocks(0, 59)) {
            for (Block block : lastBlocks) {
                JSONObject recentBlock = new JSONObject();
                recentBlock.put("index", Users.getIndex(block));
                recentBlock.put("timestamp", block.getTimestamp());
                recentBlock.put("numberOfTransactions", block.getTransactions().size());
                recentBlock.put("totalAmountNQT", block.getTotalAmountNQT());
                recentBlock.put("totalFeeNQT", block.getTotalFeeNQT());
                recentBlock.put("payloadLength", block.getPayloadLength());
                recentBlock.put("generator", Long.toUnsignedString(block.getGeneratorId()));
                recentBlock.put("height", block.getHeight());
                recentBlock.put("version", block.getVersion());
                recentBlock.put("block", block.getStringId());
                recentBlock.put("baseTarget", BigInteger.valueOf(block.getBaseTarget()).multiply(BigInteger.valueOf(100000))
                        .divide(BigInteger.valueOf(Constants.INITIAL_BASE_TARGET)));

                recentBlocks.add(recentBlock);
            }
        }

        JSONObject response = new JSONObject();
        response.put("response", "processInitialData");
        response.put("version", Nxt.VERSION);
        if (unconfirmedTransactions.size() > 0) {
            response.put("unconfirmedTransactions", unconfirmedTransactions);
        }
        if (activePeers.size() > 0) {
            response.put("activePeers", activePeers);
        }
        if (knownPeers.size() > 0) {
            response.put("knownPeers", knownPeers);
        }
        if (blacklistedPeers.size() > 0) {
            response.put("blacklistedPeers", blacklistedPeers);
        }
        if (recentBlocks.size() > 0) {
            response.put("recentBlocks", recentBlocks);
        }

        return response;
    }
}
