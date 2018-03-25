package nxt.addons;

import nxt.Account;
import nxt.Block;
import nxt.BlockchainProcessor;
import nxt.Db;
import nxt.FxtDistribution;
import nxt.Nxt;
import nxt.NxtException;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.JSONResponses;
import nxt.http.ParameterParser;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Listener;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class JPLSnapshot implements AddOn {

    public APIServlet.APIRequestHandler getAPIRequestHandler() {
        return new JPLSnapshotAPI("newGenesisAccounts", new APITag[] {APITag.ADDONS}, "height");
    }

    public String getAPIRequestType() {
        return "downloadJPLSnapshot";
    }


    /**
     * <p>The downloadJPLSnapshot API can be used to generate a genesis block JSON for a clone to satisfy the JPL 10% sharedrop
     * requirement to existing NXT holders.</p>
     *
     * <p>This utility takes a snapshot of account balances and public keys on the Nxt blockchain as of the specified height,
     * scales down the balance of each account proportionately so that the total of balances of sharedrop accounts is equal
     * to 10% of the total of all balances, and merges this data with the supplied new genesis accounts and balances.</p>
     *
     * <p>Note that using a height more than 800 blocks in the past will normally require a blockchain rescan, which takes a
     * few hours to complete. Do not interrupt this process.</p>
     *
     * <p>Request parameters</p>
     * <ul><li>newGenesisAccounts - a JSON formatted file containing all new account public keys and balances to be included
     * in the clone genesis block</li>
     * <li>height - the Nxt blockchain height at which to take the snapshot</li>
     * </ul>
     *
     * <p>Response</p>
     * <ul><li>A JSON formatted file, genesisAccounts.json, containing all public keys, new accounts and sharedrop accounts,
     * and their initial balances, which should be placed in the conf/data directory of the clone blockchain.</li>
     * </ul>
     *
     * <p>Input file format</p>
     * The input file should contain a map of account numbers to coin balances, and a list of account public keys. Account
     * numbers can be specified in either numeric or RS format. Supplying the public key for each account is optional, but
     * recommended. Here is an example input file, which allocates 300M each to the accounts with passwords "0", "1" and "2",
     * for a total of 900M to new accounts, resulting in 100M automatically allocated to existing NXT holders:
     * <pre>
     * {
     *     "balances": {
     *         "NXT-NZKH-MZRE-2CTT-98NPZ": 30000000000000000,
     *         "NXT-X5JH-TJKJ-DVGC-5T2V8": 30000000000000000,
     *         "NXT-LTR8-GMHB-YG56-4NWSE": 30000000000000000
     *     },
     *     "publicKeys": [
     *         "bf0ced0472d8ba3df9e21808e98e61b34404aad737e2bae1778cebc698b40f37",
     *         "39dc2e813bb45ff063a376e316b10cd0addd7306555ca0dd2890194d37960152",
     *         "011889a0988ccbed7f488878c62c020587de23ebbbae9ba56dd67fd9f432f808"
     *     ]
     * }
     * </pre>
     */
    public static class JPLSnapshotAPI extends APIServlet.APIRequestHandler {

        private JPLSnapshotAPI(String fileParameter, APITag[] apiTags, String... origParameters) {
            super(fileParameter, apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest request, HttpServletResponse response) throws NxtException {
            int height = ParameterParser.getHeight(request);
            if (height <= 0 || height > Nxt.getBlockchain().getHeight()) {
                return JSONResponses.INCORRECT_HEIGHT;
            }
            JSONObject inputJSON = new JSONObject();
            try {
                Part part = request.getPart("newGenesisAccounts");
                if (part != null) {
                    ParameterParser.FileData fileData = new ParameterParser.FileData(part).invoke();
                    String input = Convert.toString(fileData.getData());
                    if (!input.trim().isEmpty()) {
                        inputJSON = (JSONObject) JSONValue.parseWithException(input);
                    }
                }
            } catch (IOException | ServletException | ParseException e) {
                return JSONResponses.INCORRECT_FILE;
            }
            JPLSnapshotListener listener = new JPLSnapshotListener(height, inputJSON);
            Nxt.getBlockchainProcessor().addListener(listener, BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
            Nxt.getBlockchainProcessor().scan(height - 1, false);
            Nxt.getBlockchainProcessor().removeListener(listener, BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
            StringBuilder sb = new StringBuilder(1024);
            JSON.encodeObject(listener.getSnapshot(), sb);
            response.setHeader("Content-Disposition", "attachment; filename=genesisAccounts.json");
            response.setContentLength(sb.length());
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(sb.toString());
            } catch (IOException e) {
                return JSONResponses.RESPONSE_WRITE_ERROR;
            }
            return null;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected boolean requirePost() {
            return true;
        }

        @Override
        protected boolean requirePassword() {
            return true;
        }

        @Override
        protected boolean requireFullClient() {
            return true;
        }

        @Override
        protected boolean allowRequiredBlockParameters() {
            return false;
        }

    }

    private static class JPLSnapshotListener implements Listener<Block> {

        private final int height;
        private final JSONObject inputJSON;
        private final SortedMap<String, Object> snapshot = new TreeMap<>();

        private JPLSnapshotListener(int height, JSONObject inputJSON) {
            this.height = height;
            this.inputJSON = inputJSON;
        }

        @Override
        public void notify(Block block) {
            if (block.getHeight() == height) {
                SortedMap<String, String> snapshotPublicKeys = snapshotPublicKeys();
                JSONArray inputPublicKeys = (JSONArray)inputJSON.get("publicKeys");
                if (inputPublicKeys != null) {
                    Logger.logInfoMessage("Loading " + inputPublicKeys.size() + " input public keys");
                    inputPublicKeys.forEach(publicKey -> {
                        String account = Long.toUnsignedString(Account.getId(Convert.parseHexString((String)publicKey)));
                        String snapshotPublicKey = snapshotPublicKeys.putIfAbsent(account, (String)publicKey);
                        if (snapshotPublicKey != null && !snapshotPublicKey.equals(publicKey)) {
                            throw new RuntimeException("Public key collision, input " + publicKey + ", snapshot contains " + snapshotPublicKey);
                        }
                    });
                }
                JSONArray publicKeys = new JSONArray();
                publicKeys.addAll(snapshotPublicKeys.values());
                snapshot.put("publicKeys", publicKeys);
                SortedMap<String, Long> snapshotNxtBalances = snapshotNxtBalances();
                BigInteger snapshotTotal = BigInteger.valueOf(snapshotNxtBalances.values().stream().mapToLong(Long::longValue).sum());
                JSONObject inputBalances = (JSONObject)inputJSON.get("balances");
                if (inputBalances != null) {
                    Logger.logInfoMessage("Loading " + inputBalances.size() + " input account balances");
                    BigInteger inputTotal = BigInteger.valueOf(inputBalances.values().stream().mapToLong(value -> (Long) value).sum());
                    if (!inputTotal.equals(BigInteger.ZERO)) {
                        snapshotNxtBalances.entrySet().forEach(entry -> {
                            long snapshotBalance = entry.getValue();
                            long adjustedBalance = BigInteger.valueOf(snapshotBalance).multiply(inputTotal)
                                    .divide(snapshotTotal).divide(BigInteger.valueOf(9)).longValueExact();
                            entry.setValue(adjustedBalance);
                        });
                    }
                    inputBalances.entrySet().forEach(entry -> {
                        long accountId = Convert.parseAccountId((String)((Map.Entry)entry).getKey());
                        String account = Long.toUnsignedString(accountId);
                        long inputBalance = (Long)((Map.Entry)entry).getValue();
                        snapshotNxtBalances.merge(account, inputBalance, (a, b) -> a + b);
                    });
                }
                snapshot.put("balances", snapshotNxtBalances);
            }
        }

        private SortedMap<String, Object> getSnapshot() {
            return snapshot;
        }

        private SortedMap<String, String> snapshotPublicKeys() {
            SortedMap<String, String> map = new TreeMap<>();
            try (Connection con = Db.db.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("SELECT public_key FROM public_key WHERE public_key IS NOT NULL "
                         + "AND height <= ? ORDER by account_id")) {
                pstmt.setInt(1, height);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] publicKey = rs.getBytes("public_key");
                        long accountId = Account.getId(publicKey);
                        map.put(Long.toUnsignedString(accountId), Convert.toHexString(publicKey));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return map;
        }

        private SortedMap<String, Long> snapshotNxtBalances() {
            SortedMap<String, Long> map = new TreeMap<>();
            try (Connection con = Db.db.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("SELECT id, balance FROM account WHERE LATEST=true")) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        long accountId = rs.getLong("id");
                        if (accountId == FxtDistribution.FXT_ISSUER_ID) {
                            Logger.logInfoMessage("Skip FXT issuer balance of " + rs.getLong("balance"));
                            continue;
                        }
                        long balance = rs.getLong("balance");
                        if (balance <= 0) {
                            if (balance < 0) {
                                Logger.logInfoMessage("Skip negative balance of " + balance);
                            }
                            continue;
                        }
                        String account = Long.toUnsignedString(accountId);
                        map.put(account, balance);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return map;
        }
    }
}
