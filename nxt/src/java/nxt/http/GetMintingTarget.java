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

import nxt.Currency;
import nxt.CurrencyMinting;
import nxt.NxtException;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;

/**
 * Currency miners can use this API to obtain their target hash value for minting currency units
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id
 * <li>account - miner account id
 * <li>units - number of currency units the miner is trying to mint
 * </ul>
 */
public final class GetMintingTarget extends APIServlet.APIRequestHandler {

    static final GetMintingTarget instance = new GetMintingTarget();

    private GetMintingTarget() {
        super(new APITag[] {APITag.MS}, "currency", "account", "units");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Currency currency = ParameterParser.getCurrency(req);
        JSONObject json = new JSONObject();
        json.put("currency", Long.toUnsignedString(currency.getId()));
        long units = ParameterParser.getLong(req, "units", 1, currency.getMaxSupply() - currency.getReserveSupply(), true);
        BigInteger numericTarget = CurrencyMinting.getNumericTarget(currency, units);
        json.put("difficulty", String.valueOf(BigInteger.ZERO.equals(numericTarget) ? -1 : BigInteger.valueOf(2).pow(256).subtract(BigInteger.ONE).divide(numericTarget)));
        json.put("targetBytes", Convert.toHexString(CurrencyMinting.getTarget(numericTarget)));
        json.put("counter", nxt.CurrencyMint.getCounter(currency.getId(), ParameterParser.getAccountId(req, true)));
        return json;
    }

}
