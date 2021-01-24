/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2020 Jelurida IP B.V.                                     *
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

function RemoteNode(peerData, useAnnouncedAddress) {
    this.address = peerData.address;
    this.announcedAddress = peerData.announcedAddress;
    this.port = peerData.apiPort;
    this.isSsl = peerData.isSsl ? true : false; // For now only nodes specified by the user can use SSL since we need trusted certificate
    this.useAnnouncedAddress = useAnnouncedAddress === true;
    this.connectionTime = new Date();
}

RemoteNode.prototype.getUrl = function () {
    return (this.isSsl ? "https://" : "http://") + (this.useAnnouncedAddress ? this.announcedAddress : this.address) + ":" + this.port;
};

function RemoteNodesManager(isTestnet) {
    this.isTestnet = isTestnet;
    this.nodes = {};
    this.blacklistTable = {}; //key is the address, value is the time until the address is blacklisted

    // Bootstrap connections
    this.bc = {
        success: 0, // Successful connections counter
        fail: 0, // Failed connections counter
        counter: 0, // Total connection attempts counter
        target: 0, // Target number of successful connections
        index: 0, // Next connection index
        bootstrapComplete: false //True when the bootstrap is complete and next callbacks should be ignored
    };
    this.init();
}

function isOldVersion(version) {
    var parts = String(version).split(".");
    if (parts.length == 3) {
        if (parseInt(parts[0], 10) < 1) {
            return true;
        }
        return parseInt(parts[1], 10) < 10;
    } else {
        return true;
    }
}

function isRemoteNodeConnectable(nodeData, isSslAllowed) {
    if (nodeData.services instanceof Array && (nodeData.services.indexOf("API") >= 0 || (isSslAllowed && nodeData.services.indexOf("API_SSL") >= 0))) {
        if (!NRS.isRequireCors() || nodeData.services.indexOf("CORS") >= 0) {
            return !isOldVersion(nodeData.version);
        }
    }
    return false;
}

RemoteNodesManager.prototype.addRemoteNodes = function (peersData) {
    var mgr = this;
    $.each(peersData, function(index, peerData) {
        if (isRemoteNodeConnectable(peerData, false)) {
            var newNode = new RemoteNode(peerData);
            mgr.nodes[peerData.address] = newNode;
            NRS.logConsole("Found remote node " + peerData.address + " blacklisted " + mgr.isBlacklisted(peerData.address));
        }
    });
};

RemoteNodesManager.prototype.blacklistAddress = function(address) {
    var blacklistedUntil = new Date().getTime() + 30 * 60 * 1000;
    NRS.logConsole("Blacklist " + address + " until " + new Date(blacklistedUntil).format("isoDateTime")
            + (this.isBlacklisted(address) ? " - period extended" : ""));
    this.blacklistTable[address] = blacklistedUntil;
};

RemoteNodesManager.prototype.isBlacklisted = function(address) {
    var blacklistedUntil = this.blacklistTable[address];
    return blacklistedUntil !== undefined && new Date().getTime() < blacklistedUntil;
}

RemoteNodesManager.prototype.addBootstrapNode = function (resolve, reject) {
    var node = new RemoteNode({
        address: NRS.mobileSettings.remote_node_address,
        announcedAddress: NRS.mobileSettings.remote_node_address,
        apiPort: NRS.mobileSettings.remote_node_port,
        isSsl: NRS.mobileSettings.is_remote_node_ssl
    });
    var mgr = this;
    NRS.logConsole("Connecting to configured address " + node.address + " on port " + node.port + " using ssl " + node.isSsl);
    NRS.sendRequest("getBlockchainStatus", { "_extra": node }, function(response, data) {
        if (response.blockchainState && response.blockchainState != "UP_TO_DATE" || response.isDownloading) {
            NRS.logConsole("Warning: Bootstrap node blockchain state is " + response.blockchainState);
        }
        if (response.errorCode || !isRemoteNodeConnectable(response, true)) {
            if (response.errorCode) {
                NRS.logConsole("Bootstrap node cannot be used " + response.errorDescription);
            } else {
                NRS.logConsole("Bootstrap node does not provide the required services");
            }
            $.growl("Cannot connect to configured node, connecting to a random node");
            mgr.addBootstrapNodes(resolve, reject);
            return;
        }
        var node = data["_extra"];
        NRS.logConsole("Adding bootstrap node " + node.address);
        mgr.nodes[node.address] = node;
        resolve();
    }, { noProxy: true, remoteNode: node });
};

RemoteNodesManager.prototype.addBootstrapNodes = function (resolve, reject) {
    NRS.logConsole("addBootstrapNodes: client protocol is '" + window.location.protocol + "'");
    if (!NRS.isRemoteNodeConnectionAllowed()) {
        NRS.logConsole($.t("https_client_cannot_connect_remote_nodes"));
        $.growl($.t("https_client_cannot_connect_remote_nodes"));
        var mobileSettingsModal = $("#mobile_settings_modal");
        mobileSettingsModal.find("input[name=is_offline]").val("true");
        mobileSettingsModal.modal("show");
        return false;
    }
    var peersData = this.REMOTE_NODES_BOOTSTRAP.peers;
    peersData = NRS.getRandomPermutation(peersData);
    var mgr = this;
    mgr.bc.target = NRS.mobileSettings.is_testnet ? 2 : NRS.mobileSettings.bootstrap_nodes_count;
    var batchSize = Math.min(peersData.length, 3*mgr.bc.target);
    var data = {state: "CONNECTED", includePeerInfo: true};

    //executes resolve() or reject() according to the counters. Returns true if next batch should be started
    function checkCounters() {
        if (mgr.bc.bootstrapComplete) {
            NRS.logConsole("Ignore: bootstrap is complete");
            return false;
        }
        if (mgr.bc.success >= mgr.bc.target) {
            NRS.logConsole("Resolve: found " + mgr.bc.target + " nodes, start client");
            resolve();
            mgr.bc.bootstrapComplete = true;
            return false;
        }
        if (mgr.bc.counter >= peersData.length) {
            NRS.logConsole("Connection failed, connected only to " + mgr.bc.success + " nodes in " + mgr.bc.counter + " attempts. Target is " + mgr.bc.target);
            reject();
            mgr.bc.bootstrapComplete = true;
            return false;
        }
        return mgr.bc.counter == mgr.bc.index;
    }

    function startNextBatch() {
        var batch = [];
        for (; mgr.bc.index < peersData.length && batch.length < batchSize; mgr.bc.index ++) {
            var peerData = peersData[mgr.bc.index];
            if (!isRemoteNodeConnectable(peerData, false)) {
                NRS.logConsole("Reject: bootstrap node " + peerData.address + " required services not available" +
                    (peerData.services ? ", node services " + peerData.services : ""));
                mgr.bc.counter ++;
                mgr.bc.fail ++;
                continue;
            }
            var node = new RemoteNode(peerData, true);
            if (!node.port) {
                NRS.logConsole("Reject: bootstrap node " + node.address + ", api port undefined");
                mgr.bc.counter ++;
                mgr.bc.fail ++;
                continue;
            }
            batch.push(node);
        }
        for (var i = 0; i < batch.length; i++) {
            var node = batch[i];
            data["_extra"] = node;
            NRS.logConsole("Connecting to bootstrap node " + node.address + " port " + node.port);
            NRS.sendRequest("getBlockchainStatus", data, function(response, data) {
                mgr.bc.counter ++;
                if (response.errorCode) {
                    // Here we don't know which node it was
                    NRS.logConsole("Reject: bootstrap node returned error " + response.errorDescription);
                    mgr.bc.fail ++;
                    if (checkCounters()) {
                        startNextBatch();
                    }
                    return;
                }
                var responseNode = data["_extra"];
                if (response.blockchainState && response.blockchainState != "UP_TO_DATE" || response.isDownloading) {
                    NRS.logConsole("Reject: bootstrap node " + responseNode.address + " blockchain state is " + response.blockchainState);
                    mgr.bc.fail ++;
                    if (checkCounters()) {
                        startNextBatch();
                    }
                    return;
                }
                if (!isRemoteNodeConnectable(response, false)) {
                    NRS.logConsole("Reject: bootstrap node " + responseNode.address + " required service not available, node services " + responseNode.services);
                    mgr.bc.fail ++;
                    if (checkCounters()) {
                        startNextBatch();
                    }
                    return;
                }
                NRS.logConsole("Accept: adding bootstrap node " + responseNode.address + " response time " + (new Date() - responseNode.connectionTime) + " ms");
                mgr.nodes[responseNode.address] = responseNode;
                mgr.bc.success ++;
                if (checkCounters()) {
                    startNextBatch();
                }
            }, { noProxy: true, timeout: 5000, remoteNode: node });
        }
    }
    startNextBatch();
};

RemoteNodesManager.prototype.getRandomNode = function (ignoredAddresses) {
    var addresses = Object.keys(this.nodes);
    if (addresses.length == 0) {
        NRS.logConsole("Cannot get random node. No nodes available");
        return null;
    }
    var index = Math.floor((Math.random() * addresses.length));
    var startIndex = index;
    var node;
    do {
        var address = addresses[index];
        if ((ignoredAddresses instanceof Array && ignoredAddresses.indexOf(address) >= 0)
                || this.isBlacklisted(address)) {
            node = null;
        } else {
            node = this.nodes[address];
        }
        index = (index+1) % addresses.length;
    } while(node == null && index != startIndex);

    return node;
};

RemoteNodesManager.prototype.getRandomNodes = function (count, ignoredAddresses) {
    var processedAddresses = [];
    if (ignoredAddresses instanceof Array) {
        processedAddresses.concat(ignoredAddresses)
    }

    var result = [];
    for (var i = 0; i < count; i++) {
        var node = this.getRandomNode(processedAddresses);
        if (node) {
            processedAddresses.push(node.address);
            result.push(node);
        }
    }
    return result;
};

RemoteNodesManager.prototype.findMoreNodes = function (isReschedule) {
    var nodesMgr = this;
    var node = this.getRandomNode();
    if (node == null) {
        return;
    }
    var data = {state: "CONNECTED", includePeerInfo: true};
    NRS.sendRequest("getPeers", data, function (response) {
        if (response.peers) {
            nodesMgr.addRemoteNodes(response.peers);
        }
        if (isReschedule) {
            setTimeout(function () {
                nodesMgr.findMoreNodes(true);
            }, 30000);
        }
    }, { noProxy: true, remoteNode: node });
};

RemoteNodesManager.prototype.init = function () {
    if (NRS.isMobileApp()) {
        //load the remote nodes bootstrap file only for mobile wallet
        jQuery.ajaxSetup({ async: false });
        $.getScript(this.isTestnet ? "js/data/remotenodesbootstrap.testnet.js" : "js/data/remotenodesbootstrap.mainnet.js");
        jQuery.ajaxSetup({async: true});
    }
};
