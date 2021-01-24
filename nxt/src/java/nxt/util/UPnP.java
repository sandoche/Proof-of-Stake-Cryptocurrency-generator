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

package nxt.util;

import nxt.Nxt;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;

import java.net.InetAddress;
import java.util.Map;

/**
 * Forward ports using the UPnP protocol.
 */
public class UPnP {

    /** Initialization done */
    private static boolean initDone = false;

    /** UPnP gateway device */
    private static GatewayDevice gateway = null;

    /** Local address */
    private static InetAddress localAddress;

    /** External address */
    private static InetAddress externalAddress;

    /**
     * Add a port to the UPnP mapping
     *
     * @param   port                Port to add
     */
    public static synchronized void addPort(int port) {
        if (!initDone)
            init();
        //
        // Ignore the request if we didn't find a gateway device
        //
        if (gateway == null)
            return;
        //
        // Forward the port
        //
        try {
            if (gateway.addPortMapping(port, port, localAddress.getHostAddress(), "TCP",
                                       Nxt.APPLICATION + " " + Nxt.VERSION)) {
                Logger.logDebugMessage("Mapped port [" + externalAddress.getHostAddress() + "]:" + port);
            } else {
                Logger.logDebugMessage("Unable to map port " + port);
            }
        } catch (Exception exc) {
            Logger.logErrorMessage("Unable to map port " + port + ": " + exc.toString());
        }
    }

    /**
     * Delete a port from the UPnP mapping
     *
     * @param   port                Port to delete
     */
    public static synchronized void deletePort(int port) {
        if (!initDone || gateway == null)
            return;
        //
        // Delete the port
        //
        try {
            if (gateway.deletePortMapping(port, "TCP")) {
                Logger.logDebugMessage("Mapping deleted for port " + port);
            } else {
                Logger.logDebugMessage("Unable to delete mapping for port " + port);
            }
        } catch (Exception exc) {
            Logger.logErrorMessage("Unable to delete mapping for port " + port + ": " + exc.toString());
        }
    }

    /**
     * Return the local address
     *
     * @return                      Local address or null if the address is not available
     */
    public static synchronized InetAddress getLocalAddress() {
        if (!initDone)
            init();
        return localAddress;
    }

    /**
     * Return the external address
     *
     * @return                      External address or null if the address is not available
     */
    public static synchronized InetAddress getExternalAddress() {
        if (!initDone)
            init();
        return externalAddress;
    }

    /**
     * Initialize the UPnP support
     */
    private static void init() {
        initDone = true;
        //
        // Discover the gateway devices on the local network
        //
        try {
            Logger.logInfoMessage("Looking for UPnP gateway device...");
            GatewayDevice.setHttpReadTimeout(Nxt.getIntProperty("nxt.upnpGatewayTimeout", GatewayDevice.getHttpReadTimeout()));
            GatewayDiscover discover = new GatewayDiscover();
            discover.setTimeout(Nxt.getIntProperty("nxt.upnpDiscoverTimeout", discover.getTimeout()));
            Map<InetAddress, GatewayDevice> gatewayMap = discover.discover();
            if (gatewayMap == null || gatewayMap.isEmpty()) {
                Logger.logDebugMessage("There are no UPnP gateway devices");
            } else {
                gatewayMap.forEach((addr, device) ->
                        Logger.logDebugMessage("UPnP gateway device found on " + addr.getHostAddress()));
                gateway = discover.getValidGateway();
                if (gateway == null) {
                    Logger.logDebugMessage("There is no connected UPnP gateway device");
                } else {
                    localAddress = gateway.getLocalAddress();
                    externalAddress = InetAddress.getByName(gateway.getExternalIPAddress());
                    Logger.logDebugMessage("Using UPnP gateway device on " + localAddress.getHostAddress());
                    Logger.logInfoMessage("External IP address is " + externalAddress.getHostAddress());
                }
            }
        } catch (Exception exc) {
            Logger.logErrorMessage("Unable to discover UPnP gateway devices: " + exc.toString());
        }
    }
}
