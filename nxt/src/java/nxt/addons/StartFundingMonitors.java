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

package nxt.addons;

import nxt.FundingMonitor;
import nxt.HoldingType;
import nxt.util.Logger;
import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.util.List;

public final class StartFundingMonitors extends StartAuto {

    @Override
    protected String getFilenameProperty() {
        return "nxt.startFundingMonitorsFile";
    }

    @Override
    protected void processFile(BufferedReader reader) {
        startFundingMonitors(JO.parse(reader));
    }

    static JSONArray startFundingMonitors(JO monitorsJSON) {
        JSONArray result = new JSONArray();
        List<JO> monitors = monitorsJSON.getJoList("monitors");
        for (JO monitorJSON : monitors) {
            boolean isStarted = startFundingMonitor(monitorJSON);
            monitorJSON.put("isStarted", isStarted);
            result.add(monitorJSON.toJSONObject());
            if (isStarted) {
                Logger.logInfoMessage("Started funding monitor: " + monitorJSON.toJSONString());
            } else {
                Logger.logInfoMessage("Funding monitor already started: " + monitorJSON.toJSONString());
            }
        }
        return result;
    }

    private static boolean startFundingMonitor(JO monitorJSON) {
        String secretPhrase = monitorJSON.getString("secretPhrase");
        if (secretPhrase == null) {
            throw new RuntimeException("Monitor secretPhrase not defined");
        }
        HoldingType holdingType = HoldingType.get(monitorJSON.getByte("holdingType", (byte)0));
        long holdingId = monitorJSON.getEntityId("holding");
        String property = monitorJSON.getString("property");
        long amount = monitorJSON.getLong("amount");
        long threshold = monitorJSON.getLong("threshold");
        int interval = monitorJSON.getInt("interval");
        return FundingMonitor.startMonitor(holdingType, holdingId, property, amount, threshold, interval, secretPhrase);
    }
}

