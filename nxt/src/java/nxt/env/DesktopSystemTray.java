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

package nxt.env;

import nxt.Block;
import nxt.Constants;
import nxt.Db;
import nxt.Generator;
import nxt.Nxt;
import nxt.http.API;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class DesktopSystemTray {

    public static final int DELAY = 1000;

    private SystemTray tray;
    private final JFrame wrapper = new JFrame();
    private JDialog statusDialog;
    private JPanel statusPanel;
    private ImageIcon imageIcon;
    private TrayIcon trayIcon;
    private MenuItem openWalletInBrowser;
    private MenuItem viewLog;
    private SystemTrayDataProvider dataProvider;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM, Locale.getDefault());

    void createAndShowGUI() {
        if (!SystemTray.isSupported()) {
            Logger.logInfoMessage("SystemTray is not supported");
            return;
        }
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final PopupMenu popup = new PopupMenu();
        imageIcon = new ImageIcon("html/www/img/nxt-icon-32x32.png", "tray icon");
        trayIcon = new TrayIcon(imageIcon.getImage());
        trayIcon.setImageAutoSize(true);
        tray = SystemTray.getSystemTray();

        MenuItem shutdown = new MenuItem("Shutdown");
        openWalletInBrowser = new MenuItem("Open Wallet in Browser");
        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            openWalletInBrowser.setEnabled(false);
        }
        MenuItem showDesktopApplication = new MenuItem("Show Desktop Application");
        MenuItem refreshDesktopApplication = new MenuItem("Refresh Wallet");
        if (!Nxt.isDesktopApplicationEnabled()) {
            showDesktopApplication.setEnabled(false);
            refreshDesktopApplication.setEnabled(false);
        }
        viewLog = new MenuItem("View Log File");
        if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            viewLog.setEnabled(false);
        }
        MenuItem status = new MenuItem("Status");

        popup.add(status);
        popup.add(viewLog);
        popup.addSeparator();
        popup.add(openWalletInBrowser);
        popup.add(showDesktopApplication);
        popup.add(refreshDesktopApplication);
        popup.addSeparator();
        popup.add(shutdown);
        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("Initializing");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.logInfoMessage("TrayIcon could not be added", e);
            return;
        }

        trayIcon.addActionListener(e -> displayStatus());

        openWalletInBrowser.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(dataProvider.getWallet());
            } catch (IOException ex) {
                Logger.logInfoMessage("Cannot open wallet in browser", ex);
            }
        });

        showDesktopApplication.addActionListener(e -> {
            try {
                Class.forName("nxtdesktop.DesktopApplication").getMethod("launch").invoke(null);
            } catch (ReflectiveOperationException exception) {
                Logger.logInfoMessage("nxtdesktop.DesktopApplication failed to launch", exception);
            }
        });

        refreshDesktopApplication.addActionListener(e -> {
            try {
                Class.forName("nxtdesktop.DesktopApplication").getMethod("refresh").invoke(null);
            } catch (ReflectiveOperationException exception) {
                Logger.logInfoMessage("nxtdesktop.DesktopApplication failed to refresh", exception);
            }
        });

        viewLog.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(dataProvider.getLogFile());
            } catch (IOException ex) {
                Logger.logInfoMessage("Cannot view log", ex);
            }
        });

        status.addActionListener(e -> displayStatus());

        shutdown.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog (null,
                    "Sure you want to shutdown NXT?\n\nIf you do, this will stop forging, shufflers and account monitors.\n\n",
                    "Shutdown",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Logger.logInfoMessage("Shutdown requested by System Tray");
                System.exit(0); // Implicitly invokes shutdown using the shutdown hook
            }
        });

        ActionListener statusUpdater = evt -> {
            if (statusDialog == null || !statusDialog.isVisible()) {
                return;
            }
            displayStatus();
        };
        new Timer(DELAY, statusUpdater).start();
    }

    private void displayStatus() {
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        Collection<Generator> allGenerators = Generator.getAllGenerators();

        StringBuilder generators = new StringBuilder();
        for (Generator generator : allGenerators) {
            generators.append(Convert.rsAccount(generator.getAccountId())).append(' ');
        }
        Object optionPaneBackground = UIManager.get("OptionPane.background");
        UIManager.put("OptionPane.background", Color.WHITE);
        Object panelBackground = UIManager.get("Panel.background");
        UIManager.put("Panel.background", Color.WHITE);
        Object textFieldBackground = UIManager.get("TextField.background");
        UIManager.put("TextField.background", Color.WHITE);
        Container statusPanelParent = null;
        if (statusDialog != null && statusPanel != null) {
            statusPanelParent = statusPanel.getParent();
            statusPanelParent.remove(statusPanel);
        }
        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        addLabelRow(statusPanel, "Installation");
        addDataRow(statusPanel, "Application", Nxt.APPLICATION);
        addDataRow(statusPanel, "Version", Nxt.VERSION);
        addDataRow(statusPanel, "Network", (Constants.isTestnet) ? "TestNet" : "MainNet");
        addDataRow(statusPanel, "Working offline", "" + Constants.isOffline);
        addDataRow(statusPanel, "Wallet", String.valueOf(API.getWelcomePageUri()));
        addDataRow(statusPanel, "Peer port", String.valueOf(Peers.getDefaultPeerPort()));
        addDataRow(statusPanel, "Program folder", String.valueOf(Paths.get(".").toAbsolutePath().getParent()));
        addDataRow(statusPanel, "User folder", String.valueOf(Paths.get(Nxt.getUserHomeDir()).toAbsolutePath()));
        addDataRow(statusPanel, "Database URL", Db.db == null ? "unavailable" : Db.db.getUrl());
        addEmptyRow(statusPanel);

        if (lastBlock != null) {
            addLabelRow(statusPanel, "Last Block");
            addDataRow(statusPanel, "Height", String.valueOf(lastBlock.getHeight()));
            addDataRow(statusPanel, "Timestamp", String.valueOf(lastBlock.getTimestamp()));
            addDataRow(statusPanel, "Time", String.valueOf(new Date(Convert.fromEpochTime(lastBlock.getTimestamp()))));
            addDataRow(statusPanel, "Seconds passed", String.valueOf(Nxt.getEpochTime() - lastBlock.getTimestamp()));
            addDataRow(statusPanel, "Forging", String.valueOf(allGenerators.size() > 0));
            if (allGenerators.size() > 0) {
                addDataRow(statusPanel, "Forging accounts", generators.toString());
            }
        }

        addEmptyRow(statusPanel);
        addLabelRow(statusPanel, "Environment");
        addDataRow(statusPanel, "Number of peers", String.valueOf(Peers.getAllPeers().size()));
        addDataRow(statusPanel, "Available processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        addDataRow(statusPanel, "Max memory", humanReadableByteCount(Runtime.getRuntime().maxMemory()));
        addDataRow(statusPanel, "Total memory", humanReadableByteCount(Runtime.getRuntime().totalMemory()));
        addDataRow(statusPanel, "Free memory", humanReadableByteCount(Runtime.getRuntime().freeMemory()));
        addDataRow(statusPanel, "Process id", Nxt.getProcessId());
        addEmptyRow(statusPanel);
        addDataRow(statusPanel, "Updated", dateFormat.format(new Date()));
        if (statusDialog == null || !statusDialog.isVisible()) {
            JOptionPane pane = new JOptionPane(statusPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, imageIcon);
            statusDialog = pane.createDialog(wrapper, "NXT Server Status");
            statusDialog.setVisible(true);
            statusDialog.dispose();
        } else {
            if (statusPanelParent != null) {
                statusPanelParent.add(statusPanel);
                statusPanelParent.revalidate();
            }
            statusDialog.getContentPane().validate();
            statusDialog.getContentPane().repaint();
            EventQueue.invokeLater(statusDialog::toFront);
        }
        UIManager.put("OptionPane.background", optionPaneBackground);
        UIManager.put("Panel.background", panelBackground);
        UIManager.put("TextField.background", textFieldBackground);
    }

    private void addDataRow(JPanel parent, String text, String value) {
        JPanel rowPanel = new JPanel();
        if (!"".equals(value)) {
            rowPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        }
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        if (!"".equals(text) && !"".equals(value)) {
            text += ':';
        }
        JLabel textLabel = new JLabel(text);
        // textLabel.setFont(textLabel.getFont().deriveFont(Font.BOLD));
        rowPanel.add(textLabel);
        rowPanel.add(Box.createRigidArea(new Dimension(140 - textLabel.getPreferredSize().width, 0)));
        JTextField valueField = new JTextField(value);
        valueField.setEditable(false);
        valueField.setBorder(BorderFactory.createEmptyBorder());
        rowPanel.add(valueField);
        rowPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        parent.add(rowPanel);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void addLabelRow(JPanel parent, String text) {
        addDataRow(parent, text, "");
    }

    private void addEmptyRow(JPanel parent) {
        addLabelRow(parent, "");
    }

    void setToolTip(final SystemTrayDataProvider dataProvider) {
        SwingUtilities.invokeLater(() -> {
            trayIcon.setToolTip(dataProvider.getToolTip());
            openWalletInBrowser.setEnabled(dataProvider.getWallet() != null && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
            viewLog.setEnabled(dataProvider.getWallet() != null);
            DesktopSystemTray.this.dataProvider = dataProvider;
        });
    }

    void shutdown() {
        SwingUtilities.invokeLater(() -> tray.remove(trayIcon));
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "" + ("KMGTPE").charAt(exp-1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    void alert(String message) {
        JOptionPane.showMessageDialog(null, message, "Initialization Error", JOptionPane.ERROR_MESSAGE);
    }
}
