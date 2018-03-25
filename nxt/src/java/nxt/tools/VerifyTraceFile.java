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

package nxt.tools;

import nxt.Constants;
import nxt.Genesis;
import nxt.util.Convert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VerifyTraceFile {

    private static final List<String> balanceHeaders = Arrays.asList("balance", "unconfirmed balance");
    private static final List<String> deltaHeaders = Arrays.asList("transaction amount", "transaction fee", "dividend",
            "generation fee", "trade cost", "purchase cost", "discount", "refund", "exchange cost", "currency cost");
    private static final List<String> assetQuantityHeaders = Arrays.asList("asset balance", "unconfirmed asset balance");
    private static final List<String> deltaAssetQuantityHeaders = Arrays.asList("asset quantity", "trade quantity");
    private static final List<String> currencyBalanceHeaders = Arrays.asList("currency balance", "unconfirmed currency balance");
    private static final List<String> deltaCurrencyUnitHeaders = Arrays.asList("currency units", "exchange quantity");

    private static boolean isBalance(String header) {
        return balanceHeaders.contains(header);
    }

    private static boolean isDelta(String header) {
        return deltaHeaders.contains(header);
    }

    private static boolean isAssetQuantity(String header) {
        return assetQuantityHeaders.contains(header);
    }

    private static boolean isDeltaAssetQuantity(String header) {
        return deltaAssetQuantityHeaders.contains(header);
    }
    private static boolean isCurrencyBalance(String header) {
        return currencyBalanceHeaders.contains(header);
    }

    private static boolean isDeltaCurrencyUnits(String header) {
        return deltaCurrencyUnitHeaders.contains(header);
    }

    public static void main(String[] args) {
        String fileName = args.length == 1 ? args[0] : "nxt-trace.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            String[] headers = unquote(line.split("\t"));

            Map<String,Map<String,Long>> totals = new HashMap<>();
            Map<String,Map<String,Map<String,Long>>> accountAssetTotals = new HashMap<>();
            Map<String,Long> issuedAssetQuantities = new HashMap<>();
            Map<String,Long> accountAssetQuantities = new HashMap<>();
            Map<String,Map<String,Map<String,Long>>> accountCurrencyTotals = new HashMap<>();
            Map<String,Long> issuedCurrencyUnits = new HashMap<>();
            Map<String,Long> accountCurrencyUnits = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                String[] values = unquote(line.split("\t"));
                Map<String,String> valueMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    valueMap.put(headers[i], values[i]);
                }
                String accountId = valueMap.get("account");
                Map<String, Long> accountTotals = totals.computeIfAbsent(accountId, k -> new HashMap<>());
                Map<String, Map<String, Long>> accountAssetMap = accountAssetTotals.computeIfAbsent(accountId, k -> new HashMap<>());
                String event = valueMap.get("event");
                if ("asset issuance".equals(event)) {
                    String assetId = valueMap.get("asset");
                    issuedAssetQuantities.put(assetId, Long.parseLong(valueMap.get("asset quantity")));
                }
                if ("asset transfer".equals(event) && Genesis.CREATOR_ID == Convert.parseUnsignedLong(accountId)) {
                    String assetId = valueMap.get("asset");
                    long deletedQuantity = Long.parseLong(valueMap.get("asset quantity"));
                    long currentQuantity = issuedAssetQuantities.get(assetId);
                    issuedAssetQuantities.put(assetId, currentQuantity - deletedQuantity);
                }
                if ("asset delete".equals(event)) {
                    String assetId = valueMap.get("asset");
                    long deletedQuantity = - Long.parseLong(valueMap.get("asset quantity"));
                    long currentQuantity = issuedAssetQuantities.get(assetId);
                    issuedAssetQuantities.put(assetId, currentQuantity - deletedQuantity);
                }
                Map<String, Map<String, Long>> accountCurrencyMap = accountCurrencyTotals.computeIfAbsent(accountId, k -> new HashMap<>());
                if ("currency issuance".equals(event)) {
                    String currencyId = valueMap.get("currency");
                    issuedCurrencyUnits.put(currencyId, Long.parseLong(valueMap.get("currency units")));
                }
                if ("crowdfunding".equals(event)) {
                    String currencyId = valueMap.get("currency");
                    issuedCurrencyUnits.put(currencyId, Long.parseLong(valueMap.get("crowdfunding")));
                }
                if ("currency mint".equals(event)) {
                    String currencyId = valueMap.get("currency");
                    issuedCurrencyUnits.put(currencyId, Math.addExact(nullToZero(issuedCurrencyUnits.get(currencyId)), Long.parseLong(valueMap.get("currency units"))));
                }
                if ("currency claim".equals(event)) {
                    String currencyId = valueMap.get("currency");
                    issuedCurrencyUnits.put(currencyId, Math.addExact(nullToZero(issuedCurrencyUnits.get(currencyId)), Long.parseLong(valueMap.get("currency units"))));
                }
                if ("currency delete".equals(event) || "undo crowdfunding".equals(event)) {
                    String currencyId = valueMap.get("currency");
                    issuedCurrencyUnits.put(currencyId, 0L);
                }
                for (Map.Entry<String,String> mapEntry : valueMap.entrySet()) {
                    String header = mapEntry.getKey();
                    String value = mapEntry.getValue();
                    if (value == null || "".equals(value.trim())) {
                        continue;
                    }
                    if (isBalance(header)) {
                        accountTotals.put(header, Long.parseLong(value));
                    } else if (isDelta(header)) {
                        long previousValue = nullToZero(accountTotals.get(header));
                        accountTotals.put(header, Math.addExact(previousValue, Long.parseLong(value)));
                    } else if (isAssetQuantity(header)) {
                        String assetId = valueMap.get("asset");
                        Map<String, Long> assetTotals = accountAssetMap.computeIfAbsent(assetId, k -> new HashMap<>());
                        assetTotals.put(header, Long.parseLong(value));
                    } else if (isDeltaAssetQuantity(header)) {
                        String assetId = valueMap.get("asset");
                        Map<String, Long> assetTotals = accountAssetMap.computeIfAbsent(assetId, k -> new HashMap<>());
                        long previousValue = nullToZero(assetTotals.get(header));
                        assetTotals.put(header, Math.addExact(previousValue, Long.parseLong(value)));
                    } else if (isCurrencyBalance(header)) {
                        String currencyId = valueMap.get("currency");
                        Map<String, Long> currencyTotals = accountCurrencyMap.computeIfAbsent(currencyId, k -> new HashMap<>());
                        currencyTotals.put(header, Long.parseLong(value));
                    } else if (isDeltaCurrencyUnits(header)) {
                        String currencyId = valueMap.get("currency");
                        Map<String, Long> currencyTotals = accountCurrencyMap.computeIfAbsent(currencyId, k -> new HashMap<>());
                        long previousValue = nullToZero(currencyTotals.get(header));
                        currencyTotals.put(header, Math.addExact(previousValue, Long.parseLong(value)));
                    }
                }
            }

            final String fxtAssetId = Constants.isTestnet ? "861080501219231688" : "12422608354438203866";
            Set<String> failed = new HashSet<>();
            for (Map.Entry<String,Map<String,Long>> mapEntry : totals.entrySet()) {
                String accountId = mapEntry.getKey();
                Map<String,Long> accountValues = mapEntry.getValue();
                System.out.println("account: " + accountId);
                for (String balanceHeader : balanceHeaders) {
                    System.out.println(balanceHeader + ": " + nullToZero(accountValues.get(balanceHeader)));
                }
                System.out.println("totals:");
                long totalDelta = 0;
                for (String header : deltaHeaders) {
                    long delta = nullToZero(accountValues.get(header));
                    totalDelta = Math.addExact(totalDelta, delta);
                    System.out.println(header + ": " + delta);
                }
                System.out.println("total confirmed balance change: " + totalDelta);
                long balance = nullToZero(accountValues.get("balance"));
                if (balance != totalDelta) {
                    System.out.println("ERROR: balance doesn't match total change!!!");
                    failed.add(accountId);
                }
                Map<String,Map<String,Long>> accountAssetMap = accountAssetTotals.get(accountId);
                for (Map.Entry<String,Map<String,Long>> assetMapEntry : accountAssetMap.entrySet()) {
                    String assetId = assetMapEntry.getKey();
                    if (assetId.equals(fxtAssetId)) {
                        continue;
                    }
                    Map<String,Long> assetValues = assetMapEntry.getValue();
                    System.out.println("asset: " + assetId);
                    for (Map.Entry<String,Long> assetValueEntry : assetValues.entrySet()) {
                        System.out.println(assetValueEntry.getKey() + ": " + assetValueEntry.getValue());
                    }
                    long totalAssetDelta = 0;
                    for (String header : deltaAssetQuantityHeaders) {
                        long delta = nullToZero(assetValues.get(header));
                        totalAssetDelta = Math.addExact(totalAssetDelta, delta);
                    }
                    System.out.println("total confirmed asset quantity change: " + totalAssetDelta);
                    long assetBalance = nullToZero(assetValues.get("asset balance"));
                    if (assetBalance != totalAssetDelta && (Genesis.CREATOR_ID != Convert.parseUnsignedLong(accountId) || assetBalance != 0)) {
                        System.out.println("ERROR: asset balance doesn't match total asset quantity change!!!");
                        failed.add(accountId);
                    }
                    long previousAssetQuantity = nullToZero(accountAssetQuantities.get(assetId));
                    accountAssetQuantities.put(assetId, Math.addExact(previousAssetQuantity, assetBalance));
                }
                Map<String,Map<String,Long>> accountCurrencyMap = accountCurrencyTotals.get(accountId);
                for (Map.Entry<String,Map<String,Long>> currencyMapEntry : accountCurrencyMap.entrySet()) {
                    String currencyId = currencyMapEntry.getKey();
                    Map<String,Long> currencyValues = currencyMapEntry.getValue();
                    System.out.println("currency: " + currencyId);
                    for (Map.Entry<String,Long> currencyValueEntry : currencyValues.entrySet()) {
                        System.out.println(currencyValueEntry.getKey() + ": " + currencyValueEntry.getValue());
                    }
                    long totalCurrencyDelta = 0;
                    for (String header : deltaCurrencyUnitHeaders) {
                        long delta = nullToZero(currencyValues.get(header));
                        totalCurrencyDelta = Math.addExact(totalCurrencyDelta, delta);
                    }
                    System.out.println("total confirmed currency units change: " + totalCurrencyDelta);
                    long currencyBalance = nullToZero(currencyValues.get("currency balance"));
                    if (currencyBalance != totalCurrencyDelta) {
                        System.out.println("ERROR: currency balance doesn't match total currency units change!!!");
                        failed.add(accountId);
                    }
                    long previousCurrencyQuantity = nullToZero(accountCurrencyUnits.get(currencyId));
                    accountCurrencyUnits.put(currencyId, Math.addExact(previousCurrencyQuantity, currencyBalance));
                }
                System.out.println();
            }
            Set<String> failedAssets = new HashSet<>();
            for (Map.Entry<String,Long> assetEntry : issuedAssetQuantities.entrySet()) {
                String assetId = assetEntry.getKey();
                if (assetId.equals(fxtAssetId)) {
                    continue;
                }
                long issuedAssetQuantity = assetEntry.getValue();
                if (issuedAssetQuantity != nullToZero(accountAssetQuantities.get(assetId))) {
                    System.out.println("ERROR: asset " + assetId + " balances don't match, issued: "
                            + issuedAssetQuantity
                            + ", total of account balances: " + accountAssetQuantities.get(assetId));
                    failedAssets.add(assetId);
                }
            }
            Set<String> failedCurrencies = new HashSet<>();
            for (Map.Entry<String,Long> currencyEntry : issuedCurrencyUnits.entrySet()) {
                String currencyId = currencyEntry.getKey();
                long issuedCurrencyQuantity = currencyEntry.getValue();
                if (issuedCurrencyQuantity != nullToZero(accountCurrencyUnits.get(currencyId))) {
                    System.out.println("ERROR: currency " + currencyId + " balances don't match, issued: "
                            + issuedCurrencyQuantity
                            + ", total of account balances: " + accountCurrencyUnits.get(currencyId));
                    failedCurrencies.add(currencyId);
                }
            }
            if (failed.size() > 0) {
                System.out.println("ERROR: " + failed.size() + " accounts have incorrect balances");
                System.out.println(failed);
            } else {
                System.out.println("SUCCESS: all " + totals.size() + " account balances and asset balances match the transaction and trade totals!");
            }
            if (failedAssets.size() > 0) {
                System.out.println("ERROR: " + failedAssets.size() + " assets have incorrect balances");
                System.out.println(failedAssets);
            } else {
                System.out.println("SUCCESS: all " + issuedAssetQuantities.size() + " assets quantities are correct!");
            }
            if (failedCurrencies.size() > 0) {
                System.out.println("ERROR: " + failedCurrencies.size() + " currencies have incorrect balances");
                System.out.println(failedCurrencies);
            } else {
                System.out.println("SUCCESS: all " + issuedCurrencyUnits.size() + " currency units are correct!");
            }

        } catch (IOException e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }

    private static final String beginQuote = "^\"";
    private static final String endQuote = "\"$";

    private static String[] unquote(String[] values) {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].replaceFirst(beginQuote, "").replaceFirst(endQuote, "");
        }
        return result;
    }

    private static long nullToZero(Long l) {
        return l == null ? 0 : l;
    }

}
