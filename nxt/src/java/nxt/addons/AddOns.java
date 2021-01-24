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

package nxt.addons;

import nxt.Nxt;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AddOns {

    private static final List<AddOn> addOns;
    static {
        List<AddOn> addOnsList = new ArrayList<>();
        Nxt.getStringListProperty("nxt.addOns").forEach(addOn -> {
            Class addOnClass = null;
            try {
                try {
                    addOnClass = Class.forName(addOn);
                } catch (ClassNotFoundException e) {
                    if (addOn.indexOf('.') == -1) {
                        addOn = "nxt.addons." + addOn;
                        addOnClass = Class.forName(addOn);
                    }
                }
                if (addOnClass == null) {
                    Logger.logErrorMessage("Add-on %s not found", addOn);
                } else {
                    addOnsList.add((AddOn) addOnClass.getConstructor().newInstance());
                }
            } catch (ReflectiveOperationException e) {
                Logger.logErrorMessage(e.getMessage(), e);
            }
        });
        addOns = Collections.unmodifiableList(addOnsList);
        if (!addOns.isEmpty() && !Nxt.getBooleanProperty("nxt.disableSecurityPolicy")) {
            System.setProperty("java.security.policy", Nxt.isDesktopApplicationEnabled() ? "nxtdesktop.policy" : "nxt.policy");
            Logger.logMessage("Setting security manager with policy " + System.getProperty("java.security.policy"));
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkConnect(String host, int port) {
                    // Allow all connections
                }
                @Override
                public void checkConnect(String host, int port, Object context) {
                    // Allow all connections
                }
            });
        }
        addOns.forEach(addOn -> {
            Logger.logInfoMessage("Initializing " + addOn.getClass().getName());
            addOn.init();
        });
    }

    public static void init() {}

    public static void shutdown() {
        addOns.forEach(addOn -> {
            Logger.logShutdownMessage("Shutting down " + addOn.getClass().getName());
            addOn.shutdown();
        });
    }

    public static void registerAPIRequestHandlers(Map<String,APIServlet.APIRequestHandler> map) {
        for (AddOn addOn : addOns) {
            Map<String, APIServlet.APIRequestHandler> apiRequests = addOn.getAPIRequests();
            if (apiRequests != null) {
                apiRequests = new LinkedHashMap<>(apiRequests); // defensive copy, preserve order
            }

            APIServlet.APIRequestHandler requestHandler = addOn.getAPIRequestHandler();
            String apiRequestType = addOn.getAPIRequestType();
            if (requestHandler != null && apiRequestType != null) {
                if (apiRequests == null) {
                    apiRequests = new HashMap<>();
                }
                apiRequests.put(apiRequestType, requestHandler);
            }
            if (apiRequests == null) {
                continue;
            }

            // Register the Addon APIs
            for(Map.Entry<String, APIServlet.APIRequestHandler> apiRequest : apiRequests.entrySet()){
                requestHandler = apiRequest.getValue();
                if (!requestHandler.getAPITags().contains(APITag.ADDONS)) {
                    Logger.logErrorMessage("Add-on " + addOn.getClass().getName()
                            + " attempted to register request handler which is not tagged as APITag.ADDONS, skipping");
                    continue;
                }
                String requestType = apiRequest.getKey();
                if (requestType == null) {
                    Logger.logErrorMessage("Add-on " + addOn.getClass().getName() + " requestType not defined");
                    continue;
                }
                if (map.get(requestType) != null) {
                    Logger.logErrorMessage("Add-on " + addOn.getClass().getName() + " attempted to override requestType " + requestType + ", skipping");
                    continue;
                }
                Logger.logMessage("Add-on " + addOn.getClass().getName() + " registered new API: " + requestType);
                map.put(requestType, requestHandler);
            }
        }
    }

    private AddOns() {}

}
