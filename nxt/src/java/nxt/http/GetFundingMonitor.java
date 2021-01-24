/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
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

import nxt.Account;
import nxt.FundingMonitor;
import nxt.HoldingType;
import nxt.crypto.Crypto;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Get a funding monitor
 * <p>
 * The monitors for a single funding account will be returned when the secret phrase is specified.
 * A single monitor will be returned if holding and property are specified.
 * Otherwise, all monitors for the funding account will be returned
 * The administrator password is not required and will be ignored.
 * <p>
 * When the administrator password is specified, all monitors will be returned
 * unless the funding account is also specified.  A single monitor will be returned if
 * holding and property are specified.  Otherwise, all monitors for the
 * funding account will be returned.
 * <p>
 * Holding type codes are listed in getConstants.
 * In addition, the holding identifier must be specified when the holding type is ASSET or CURRENCY.
 */
public class GetFundingMonitor extends APIServlet.APIRequestHandler {

    static final GetFundingMonitor instance = new GetFundingMonitor();

    private GetFundingMonitor() {
        super(new APITag[] {APITag.ACCOUNTS}, "holdingType", "holding", "property", "secretPhrase",
                "includeMonitoredAccounts", "account", "adminPassword");
    }
    /**
     * Process the request
     *
     * @param   req                 Client request
     * @return                      Client response
     * @throws  ParameterException        Unable to process request
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        long account = ParameterParser.getAccountId(req, false);
        boolean includeMonitoredAccounts = "true".equalsIgnoreCase(req.getParameter("includeMonitoredAccounts"));
        if (secretPhrase == null) {
            API.verifyPassword(req);
        }
        List<FundingMonitor> monitors;
        if (secretPhrase != null || account != 0) {
            if (secretPhrase != null) {
                if (account != 0) {
                    if (Account.getId(Crypto.getPublicKey(secretPhrase)) != account) {
                        return JSONResponses.INCORRECT_ACCOUNT;
                    }
                } else {
                    account = Account.getId(Crypto.getPublicKey(secretPhrase));
                }
            }
            final long accountId = account;
            final HoldingType holdingType = ParameterParser.getHoldingType(req);
            final long holdingId = ParameterParser.getHoldingId(req, holdingType);
            final String property = ParameterParser.getAccountProperty(req, false);
            Filter<FundingMonitor> filter;
            if (property != null) {
                filter = (monitor) -> monitor.getAccountId() == accountId &&
                        monitor.getProperty().equals(property) &&
                        monitor.getHoldingType() == holdingType &&
                        monitor.getHoldingId() == holdingId;
            } else {
                filter = (monitor) -> monitor.getAccountId() == accountId;
            }
            monitors = FundingMonitor.getMonitors(filter);
        } else {
            monitors = FundingMonitor.getAllMonitors();
        }
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        monitors.forEach(monitor -> {
            JSONObject monitorJSON = JSONData.accountMonitor(monitor, includeMonitoredAccounts);
            jsonArray.add(monitorJSON);
        });
        response.put("monitors", jsonArray);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
