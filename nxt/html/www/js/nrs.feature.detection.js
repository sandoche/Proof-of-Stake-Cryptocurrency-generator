/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {nrs.js}
 */
var NRS = (function (NRS) {
    var isDesktopApplication = navigator.userAgent.indexOf("JavaFX") >= 0;
    var isPromiseSupported = (typeof Promise !== "undefined" && Promise.toString().indexOf("[native code]") !== -1);
    var isMobileDevice = window["cordova"] !== undefined;
    var isLocalHost = false;
    var remoteNode = null;
    var isLoadedOverHttps = ("https:" == window.location.protocol);

    NRS.isPrivateIP = function (ip) {
        if (!/^\d+\.\d+\.\d+\.\d+$/.test(ip)) {
            return false;
        }
        var parts = ip.split('.');
        return parts[0] === '10' || parts[0] == '127' || parts[0] === '172' && (parseInt(parts[1], 10) >= 16 && parseInt(parts[1], 10) <= 31) || parts[0] === '192' && parts[1] === '168';
    };

    if (window.location && window.location.hostname) {
        var hostName = window.location.hostname.toLowerCase();
        isLocalHost = hostName == "localhost" || hostName == "127.0.0.1" || NRS.isPrivateIP(hostName);
    }

    NRS.isIndexedDBSupported = function() {
        return window.indexedDB !== undefined;
    };

    NRS.isExternalLinkVisible = function() {
        // When using JavaFX add a link to a web wallet except on Linux since on Ubuntu it sometimes hangs
        if (NRS.isMobileApp()) {
            return false;
        }
        return !(isDesktopApplication && navigator.userAgent.indexOf("Linux") >= 0);
    };

    NRS.isWebWalletLinkVisible = function() {
        if (NRS.isMobileApp()) {
            return false;
        }
        return isDesktopApplication && navigator.userAgent.indexOf("Linux") == -1;
    };

    NRS.isMobileApp = function () {
        return isMobileDevice || (NRS.mobileSettings && NRS.mobileSettings.is_simulate_app);
    };

    NRS.isEnableMobileAppSimulation = function () {
        return !isMobileDevice;
    };

    NRS.isRequireCors = function () {
        return !isMobileDevice;
    };

    NRS.isPollGetState = function() {
        // When using JavaFX do not poll the server unless it's a working as a proxy
        return !isDesktopApplication || NRS.state && NRS.state.apiProxy;
    };

    NRS.isUpdateRemoteNodes = function() {
        return NRS.state && NRS.state.apiProxy;
    };

    NRS.isRemoteNodeConnectionAllowed = function() {
        // The client always connects to remote nodes over Http since most Https nodes use a test certificate and
        // therefore cannot be used.
        // However, if the client itself is loaded over Https, it cannot connect to nodes over Http since this will
        // result in a mixed content error.
        return !isLoadedOverHttps;
    };

    NRS.isExportContactsAvailable = function() {
        return !isDesktopApplication; // When using JavaFX you cannot export the contact list
    };

    NRS.isFileEncryptionSupported = function() {
        return !isDesktopApplication; // When using JavaFX you cannot read the file to encrypt
    };

    NRS.isShowDummyCheckbox = function() {
        return isDesktopApplication && navigator.userAgent.indexOf("Linux") >= 0; // Correct rendering problem of checkboxes on Linux
    };

    NRS.isDecodePeerHallmark = function() {
        return isPromiseSupported;
    };

    NRS.getRemoteNodeUrl = function() {
        if (!NRS.isMobileApp()) {
            if (!isNode) {
                return "";
            }
            return NRS.getModuleConfig().url;
        }
        if (remoteNode) {
            return remoteNode.getUrl();
        }
        remoteNode = NRS.remoteNodesMgr.getRandomNode();
        if (remoteNode) {
            var url = remoteNode.getUrl();
            NRS.logConsole("Remote node url: " + url);
            return url;
        } else {
            NRS.logConsole("No available remote nodes");
            $.growl($.t("no_available_remote_nodes"));
        }
    };

    NRS.getRemoteNode = function () {
        return remoteNode;
    };

    NRS.resetRemoteNode = function(blacklist) {
        if (remoteNode && blacklist) {
            remoteNode.blacklist();
        }
        remoteNode = null;
    };

    NRS.getDownloadLink = function(url, link) {
        if (NRS.isMobileApp()) {
            var script = "NRS.openMobileBrowser(\"" + url + "\");";
            if (link) {
                link.attr("onclick", script);
                return;
            }
            return "<a onclick='" + script +"' class='btn btn-xs btn-default'>" + $.t("download") + "</a>";
        } else {
            if (link) {
                link.attr("href", url);
                return;
            }
            return "<a href='" + url + "' class='btn btn-xs btn-default'>" + $.t("download") + "</a>";
        }
    };

    NRS.openMobileBrowser = function(url) {
        try {
            // Works on Android 6.0 (does not work in 5.1)
            cordova.InAppBrowser.open(url, '_system');
        } catch(e) {
            NRS.logConsole(e.message);
        }
    };

    NRS.isCordovaScanningEnabled = function () {
        return isMobileDevice;
    };

    NRS.isScanningAllowed = function () {
        return isMobileDevice || isLocalHost || NRS.isTestNet;
    };

    NRS.isCameraPermissionRequired = function () {
        return device && device.platform == "Android" && device.version >= "6.0.0";
    };

    NRS.getShapeShiftUrl = function() {
        return NRS.settings.shape_shift_url;
    };

    NRS.getChangellyUrl = function() {
        return NRS.settings.changelly_url;
    };

    NRS.isForgingSupported = function() {
        return !NRS.isMobileApp() && !(NRS.state && NRS.state.apiProxy);
    };

    NRS.isFundingMonitorSupported = function() {
        return !NRS.isMobileApp() && !(NRS.state && NRS.state.apiProxy);
    };

    NRS.isShufflingSupported = function() {
        return !NRS.isMobileApp() && !(NRS.state && NRS.state.apiProxy);
    };

    NRS.isConfirmResponse = function() {
        return NRS.isMobileApp() || (NRS.state && NRS.state.apiProxy);
    };

    NRS.isDisplayOptionalDashboardTiles = function() {
        return !NRS.isMobileApp();
    };

    NRS.isShowClientOptionsLink = function() {
        return NRS.isMobileApp() || (NRS.state && NRS.state.apiProxy);
    };

    NRS.getGeneratorAccuracyWarning = function() {
        if (isDesktopApplication) {
            return "";
        }
        return $.t("generator_timing_accuracy_warning");
    };

    NRS.isInitializePlugins = function() {
        return !NRS.isMobileApp();
    };

    NRS.isShowRemoteWarning = function() {
        return !isLocalHost;
    };

    NRS.isForgingSafe = function() {
        return isLocalHost;
    };

    NRS.isPassphraseAtRisk = function() {
        return !isLocalHost || NRS.state && NRS.state.apiProxy || NRS.isMobileApp();
    };

    NRS.isWindowPrintSupported = function() {
        return !isDesktopApplication && !isMobileDevice;
    };

    NRS.isDisableScheduleRequest = function() {
        return NRS.isMobileApp() || (NRS.state && NRS.state.apiProxy);
    };

    NRS.getAdminPassword = function() {
        if (window.java) {
            return window.java.getAdminPassword();
        }
        if (isNode) {
            return NRS.getModuleConfig().adminPassword;
        }
        return NRS.settings.admin_password;
    };

    NRS.isAnimationAllowed = function() {
        return !isDesktopApplication;
    };

    NRS.isFileReaderSupported = function() {
        return !isDesktopApplication;
    };

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}
