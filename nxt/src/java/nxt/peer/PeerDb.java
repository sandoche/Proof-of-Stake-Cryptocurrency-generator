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

package nxt.peer;

import nxt.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class PeerDb {

    static class Entry {
        private final String address;
        private final long services;
        private final int lastUpdated;

        Entry(String address, long services, int lastUpdated) {
            this.address = address;
            this.services = services;
            this.lastUpdated = lastUpdated;
        }

        public String getAddress() {
            return address;
        }

        public long getServices() {
            return services;
        }

        public int getLastUpdated() {
            return lastUpdated;
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj != null && (obj instanceof Entry) && address.equals(((Entry)obj).address));
        }
    }

    static List<Entry> loadPeers() {
        List<Entry> peers = new ArrayList<>();
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM peer");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                peers.add(new Entry(rs.getString("address"), rs.getLong("services"), rs.getInt("last_updated")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return peers;
    }

    static void deletePeers(Collection<Entry> peers) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("DELETE FROM peer WHERE address = ?")) {
            for (Entry peer : peers) {
                pstmt.setString(1, peer.getAddress());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static void updatePeers(Collection<Entry> peers) {
        try (Connection con = Db.db.getConnection();
                PreparedStatement pstmt = con.prepareStatement("MERGE INTO peer "
                        + "(address, services, last_updated) KEY(address) VALUES(?, ?, ?)")) {
            for (Entry peer : peers) {
                pstmt.setString(1, peer.getAddress());
                pstmt.setLong(2, peer.getServices());
                pstmt.setInt(3, peer.getLastUpdated());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static void updatePeer(PeerImpl peer) {
        try (Connection con = Db.db.getConnection();
                PreparedStatement pstmt = con.prepareStatement("MERGE INTO peer "
                        + "(address, services, last_updated) KEY(address) VALUES(?, ?, ?)")) {
            pstmt.setString(1, peer.getAnnouncedAddress());
            pstmt.setLong(2, peer.getServices());
            pstmt.setInt(3, peer.getLastUpdated());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
}
