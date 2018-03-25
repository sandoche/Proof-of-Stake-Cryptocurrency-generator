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

import java.util.HashMap;
import java.util.Map;

public enum APITag {

    ACCOUNTS("Accounts"), ACCOUNT_CONTROL("Account Control"), ALIASES("Aliases"), AE("Asset Exchange"), BLOCKS("Blocks"),
    CREATE_TRANSACTION("Create Transaction"), DGS("Digital Goods Store"), FORGING("Forging"), MESSAGES("Messages"),
    MS("Monetary System"), NETWORK("Networking"), PHASING("Phasing"), SEARCH("Search"), INFO("Server Info"),
    SHUFFLING("Shuffling"), DATA("Tagged Data"), TOKENS("Tokens"), TRANSACTIONS("Transactions"), VS("Voting System"),
    UTILS("Utils"), DEBUG("Debug"), ADDONS("Add-ons");

    private static final Map<String, APITag> apiTags = new HashMap<>();
    static {
        for (APITag apiTag : values()) {
            if (apiTags.put(apiTag.getDisplayName(), apiTag) != null) {
                throw new RuntimeException("Duplicate APITag name: " + apiTag.getDisplayName());
            }
        }
    }

    public static APITag fromDisplayName(String displayName) {
        APITag apiTag = apiTags.get(displayName);
        if (apiTag == null) {
            throw new IllegalArgumentException("Invalid APITag name: " + displayName);
        }
        return apiTag;
    }

    private final String displayName;

    APITag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
