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

package nxt.http;

import nxt.Account;
import nxt.Asset;
import nxt.Attachment;
import nxt.NxtException;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class DeleteAssetProperty extends CreateTransaction {

    static final DeleteAssetProperty instance = new DeleteAssetProperty();

    private DeleteAssetProperty() {
        super(new APITag[]{APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "property", "setter");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Account senderAccount = ParameterParser.getSenderAccount(req);
        Asset asset = ParameterParser.getAsset(req);
        long setterId = ParameterParser.getAccountId(req, "setter", false);
        if (setterId == 0) {
            setterId = senderAccount.getId();
        }
        String property = Convert.nullToEmpty(req.getParameter("property")).trim();
        if (property.isEmpty()) {
            return JSONResponses.MISSING_PROPERTY;
        }
        Asset.AssetProperty assetProperty = asset.getProperty(setterId, property);
        if (assetProperty == null) {
            return JSONResponses.UNKNOWN_PROPERTY;
        }
        if (assetProperty.getSetterId() != senderAccount.getId() && asset.getAccountId() != senderAccount.getId()) {
            return JSONResponses.INCORRECT_PROPERTY;
        }
        Attachment attachment = new Attachment.ColoredCoinsAssetPropertyDelete(assetProperty.getId());

        return createTransaction(req, senderAccount, asset.getAccountId(), 0, attachment);
    }
}
