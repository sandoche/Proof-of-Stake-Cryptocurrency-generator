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

package nxt.http;

import nxt.AccountLedger;
import nxt.AccountLedger.LedgerEntry;
import nxt.AccountLedger.LedgerEvent;
import nxt.AccountLedger.LedgerHolding;
import nxt.NxtException;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * The GetAccountLedger API will return entries from the account ledger.  The
 * account ledger tracks all account changes as determined by the nxt.ledgerAccounts,
 * nxt.ledgerLogUnconfirmed and nxt.ledgerTrimKeep properties.
 * </p>
 * <table>
 *   <caption><b>Request parameters</b></caption>
 *   <thead>
 *     <tr>
 *       <th>Name</th>
 *       <th>Description</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>account</td>
 *       <td>Account identifier or Reed-Solomon identifier
 *           This is an optional parameter and restricts the search to entries matching the account identifier.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>adminPassword</td>
 *       <td>Administrator password.
 *           The administrator password is required if more than nxt.maxAPIRecords entries are to be searched.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>event</td>
 *       <td>Event identifier.
 *           The event identifier is ignored unless 'eventType' is also specified.
 *           This is an optional parameter and restricts the search to entries matching both 'eventType'
 *           and 'event'.  Note that the asset identifier, currency identifier or digital goods identifier
 *           is the same as the transaction identifier of the creating transaction.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>eventType</td>
 *       <td>Event type.
 *           This is an optional parameter and restricts the search to entries matching 'eventType'.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>holding</td>
 *       <td>Holding identifier.
 *           The holding identifier is ignored unless 'holdingType' is also specified.
 *           This is an optional parameter and restricts the search to entries matching both 'holdingType'
 *           and 'holding'.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>holdingType</td>
 *       <td>Holding type.
 *           This is an optional parameter and restricts the search to entries matching 'holdingType'.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>includeTransactions</td>
 *       <td>Specify TRUE to include the transaction associated with a ledger entry.  The default is FALSE.</td>
 *     </tr>
 *     <tr>
 *       <td>includeHoldingInfo</td>
 *       <td>Specify TRUE to include the corresponding asset or currency info (name, decimals) with each ledger entry.  The default is FALSE.</td>
 *     </tr>
 *     <tr>
 *       <td>firstIndex</td>
 *       <td>Return matching entries starting from this index, inclusive, default is 0. Sort is always most recent first.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>lastIndex</td>
 *       <td>The index of the last matching entry to return, inclusive.
 *           The maximum number of entries returned is limited by the nxt.maxAPIRecords property
 *           unless the administrator password is specified.
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * <br>
 * <table>
 *   <caption><b>Ledger entry fields</b></caption>
 *   <thead>
 *     <tr>
 *       <th>Name</th>
 *       <th>Description</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>account</td>
 *       <td>Account identifier.</td>
 *     </tr>
 *     <tr>
 *       <td>accountRS</td>
 *       <td>Account Reed-Solomon identifier.</td>
 *     </tr>
 *     <tr>
 *       <td>balance</td>
 *       <td>Update balance for the holding identified by 'holdingType'.</td>
 *     </tr>
 *     <tr>
 *       <td>block</td>
 *       <td>Block that created the ledger entry.  The current ledger entry will be removed if the block is
 *           removed from the blockchain.  A new ledger entry will be created when either the block is
 *           added to the blockchain again or the transaction is included in a different block.</td>
 *     </tr>
 *     <tr>
 *       <td>change</td>
 *       <td>Change in the balance for the holding identified by 'holdingType'.</td>
 *     </tr>
 *     <tr>
 *       <td>event</td>
 *       <td>The block or transaction associated with the event.</td>
 *     </tr>
 *     <tr>
 *       <td>eventType</td>
 *       <td>Event causing the account change.</td>
 *     </tr>
 *     <tr>
 *       <td>height</td>
 *       <td>The block height associated with the event.</td>
 *     </tr>
 *     <tr>
 *       <td>holding</td>
 *       <td>The item identifier for an asset or currency balance.</td>
 *     </tr>
 *     <tr>
 *       <td>holdingType</td>
 *       <td>The item being changed (account balance, asset balance or currency balance).</td>
 *     </tr>
 *     <tr>
 *       <td>isTransactionEvent</td>
 *       <td>TRUE if the event is associated with a transaction and FALSE if it is associated with a block.</td>
 *     </tr>
 *     <tr>
 *       <td>ledgerId</td>
 *       <td>The ledger entry identifier.  This is a counter that is incremented each time
 *           a new entry is added to the account ledger.  The ledger entry identifier is unique
 *           to the peer returning the ledger entry and will be different for each peer in the
 *           network.  A new ledger entry identifier will be assigned if a ledger entry is removed
 *           and then added again.
 *       </td>
 *     </tr>
 *     <tr>
 *       <td>timestamp</td>
 *       <td>The block timestamp associated with the event.</td>
 *     </tr>
 *     <tr>
 *       <td>transaction</td>
 *       <td>Transaction associated with the event if 'includeTransactions' is TRUE.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * <br>
 * <table>
 *   <caption><b>Values returned for 'holdingType'</b></caption>
 *   <thead>
 *     <tr>
 *       <th>Name</th>
 *       <th>Description</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>ASSET_BALANCE</td>
 *       <td>Change in the asset balance.  The asset identifier is the 'holding'.</td>
 *     </tr>
 *     <tr>
 *       <td>CURRENCY_BALANCE</td>
 *       <td>Change in the currency balance.  The currency identifier is the 'holding'.</td>
 *     </tr>
 *     <tr>
 *       <td>NXT_BALANCE</td>
 *       <td>Change in the NXT balance for the account.  There is no 'holding'.</td>
 *     </tr>
 *     <tr>
 *       <td>UNCONFIRMED_ASSET_BALANCE</td>
 *       <td>Change in the unconfirmed asset balance.  The asset identifier is the 'holding'.</td>
 *     </tr>
 *     <tr>
 *       <td>UNCONFIRMED_CURRENCY_BALANCE</td>
 *       <td>Change in the unconfirmed currency balance.  The currency identifier is the 'holding'.</td>
 *     </tr>
 *     <tr>
 *       <td>UNCONFIRMED_NXT_BALANCE</td>
 *       <td>Change in the unconfirmed NXT balance for the account.  There is no 'holding'.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class GetAccountLedger extends APIServlet.APIRequestHandler {

    /** GetAccountLedger instance */
    static final GetAccountLedger instance = new GetAccountLedger();

    /**
     * Create the GetAccountLedger instance
     */
    private GetAccountLedger() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "firstIndex", "lastIndex",
                "eventType", "event", "holdingType", "holding", "includeTransactions", "includeHoldingInfo");
    }

    /**
     * Process the GetAccountLedger API request
     *
     * @param   req                 API request
     * @return                      API response
     * @throws  NxtException        Invalid request
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        //
        // Process the request parameters
        //
        long accountId = ParameterParser.getAccountId(req, "account", false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        String eventType = Convert.emptyToNull(req.getParameter("eventType"));
        LedgerEvent event = null;
        long eventId = 0;
        if (eventType != null) {
            try {
                event = LedgerEvent.valueOf(eventType);
                eventId = ParameterParser.getUnsignedLong(req, "event", false);
            } catch (RuntimeException e) {
                throw new ParameterException(JSONResponses.incorrect("eventType"));
            }
        }
        String holdingType = Convert.emptyToNull(req.getParameter("holdingType"));
        LedgerHolding holding = null;
        long holdingId = 0;
        if (holdingType != null) {
            try {
                holding = LedgerHolding.valueOf(holdingType);
                holdingId = ParameterParser.getUnsignedLong(req, "holding", false);
            } catch (RuntimeException e) {
                throw new ParameterException(JSONResponses.incorrect("holdingType"));
            }
        }
        boolean includeTransactions = "true".equalsIgnoreCase(req.getParameter("includeTransactions"));
        boolean includeHoldingInfo = "true".equalsIgnoreCase(req.getParameter("includeHoldingInfo"));

        //
        // Get the ledger entries
        //
        List<LedgerEntry> ledgerEntries = AccountLedger.getEntries(accountId, event, eventId,
                                                                   holding, holdingId, firstIndex, lastIndex);
        //
        // Return the response
        //
        JSONArray responseEntries = new JSONArray();
        ledgerEntries.forEach((entry) -> {
            JSONObject responseEntry = new JSONObject();
            JSONData.ledgerEntry(responseEntry, entry, includeTransactions, includeHoldingInfo);
            responseEntries.add(responseEntry);
        });
        JSONObject response = new JSONObject();
        response.put("entries", responseEntries);
        return response;
    }
}
