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

import nxt.*;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_ASSET_PROPERTY_NAME_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_ASSET_PROPERTY_VALUE_LENGTH;

public class SetAssetProperty extends CreateTransaction {
    static final SetAssetProperty instance = new SetAssetProperty();

    private SetAssetProperty() {
        super(new APITag[]{APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "property", "value");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account senderAccount = ParameterParser.getSenderAccount(req);
        Asset asset = ParameterParser.getAsset(req);

        String property = Convert.nullToEmpty(req.getParameter("property")).trim();
        String value = Convert.nullToEmpty(req.getParameter("value")).trim();

        if (property.length() > Constants.MAX_ASSET_PROPERTY_NAME_LENGTH || property.length() == 0) {
            return INCORRECT_ASSET_PROPERTY_NAME_LENGTH;
        }

        if (value.length() > Constants.MAX_ASSET_PROPERTY_VALUE_LENGTH) {
            return INCORRECT_ASSET_PROPERTY_VALUE_LENGTH;
        }

        Attachment attachment = new Attachment.ColoredCoinsAssetProperty(asset.getId(), property, value);
        return createTransaction(req, senderAccount, asset.getAccountId(), 0, attachment);
    }
}
