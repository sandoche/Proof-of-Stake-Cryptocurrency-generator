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

package nxtdesktop;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import nxt.Nxt;
import nxt.http.API;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The class itself and methods in this class are invoked from JavaScript therefore has to be public
 */
@SuppressWarnings("WeakerAccess")
public class JavaScriptBridge {

    DesktopApplication application;
    private Clipboard clipboard;

    public JavaScriptBridge(DesktopApplication application) {
        this.application = application;
    }

    public void log(String message) {
        Logger.logInfoMessage(message);
    }

    @SuppressWarnings("unused")
    public void openBrowser(String account) {
        final String url = API.getWelcomePageUri().toString() + "?account=" + account;
        Platform.runLater(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                Logger.logInfoMessage("Cannot open " + API.getWelcomePageUri().toString() + " error " + e.getMessage());
            }
        });
    }

    @SuppressWarnings("unused")
    public String readContactsFile() {
        String fileName = "contacts.json";
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(Nxt.getUserHomeDir(), fileName));
        } catch (IOException e) {
            Logger.logInfoMessage("Cannot read file " + fileName + " error " + e.getMessage());
            JSONObject response = new JSONObject();
            response.put("error", "contacts_file_not_found");
            response.put("file", fileName);
            response.put("folder", Nxt.getUserHomeDir());
            response.put("type", "1");
            return JSON.toJSONString(response);
        }
        try {
            return new String(bytes, "utf8");
        } catch (UnsupportedEncodingException e) {
            Logger.logInfoMessage("Cannot parse file " + fileName + " content error " + e.getMessage());
            JSONObject response = new JSONObject();
            response.put("error", "unsupported_encoding");
            response.put("type", "2");
            return JSON.toJSONString(response);
        }
    }

    public String getAdminPassword() {
        return API.adminPassword;
    }

    @SuppressWarnings("unused")
    public void popupHandlerURLChange(String newValue) {
        application.popupHandlerURLChange(newValue);
    }

    @SuppressWarnings("unused")
    public boolean copyText(String text) {
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
            if (clipboard == null) {
                return false;
            }
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        return clipboard.setContent(content);
    }

}