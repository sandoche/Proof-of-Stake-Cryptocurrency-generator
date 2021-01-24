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

package nxt.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbKey {

    abstract class Factory<T> {

        private final String pkClause;
        private final String pkColumns;
        private final String selfJoinClause;

        protected Factory(String pkClause, String pkColumns, String selfJoinClause) {
            this.pkClause = pkClause;
            this.pkColumns = pkColumns;
            this.selfJoinClause = selfJoinClause;
        }

        public abstract DbKey newKey(T t);

        public abstract DbKey newKey(ResultSet rs) throws SQLException;

        public T newEntity(DbKey dbKey) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public final String getPKClause() {
            return pkClause;
        }

        public final String getPKColumns() {
            return pkColumns;
        }

        // expects tables to be named a and b
        public final String getSelfJoinClause() {
            return selfJoinClause;
        }

    }

    int setPK(PreparedStatement pstmt) throws SQLException;

    int setPK(PreparedStatement pstmt, int index) throws SQLException;


    abstract class LongKeyFactory<T> extends Factory<T> {

        private final String idColumn;

        public LongKeyFactory(String idColumn) {
            super(" WHERE " + idColumn + " = ? ",
                    idColumn,
                    " a." + idColumn + " = b." + idColumn + " ");
            this.idColumn = idColumn;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new LongKey(rs.getLong(idColumn));
        }

        public DbKey newKey(long id) {
            return new LongKey(id);
        }

    }

    abstract class StringKeyFactory<T> extends Factory<T> {

        private final String idColumn;

        public StringKeyFactory(String idColumn) {
            super(" WHERE " + idColumn + " = ? ",
                    idColumn,
                    " a." + idColumn + " = b." + idColumn + " ");
            this.idColumn = idColumn;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new StringKey(rs.getString(idColumn));
        }

        public DbKey newKey(String id) {
            return new StringKey(id);
        }

    }

    abstract class LinkKeyFactory<T> extends Factory<T> {

        private final String idColumnA;
        private final String idColumnB;

        public LinkKeyFactory(String idColumnA, String idColumnB) {
            super(" WHERE " + idColumnA + " = ? AND " + idColumnB + " = ? ",
                    idColumnA + ", " + idColumnB,
                    " a." + idColumnA + " = b." + idColumnA + " AND a." + idColumnB + " = b." + idColumnB + " ");
            this.idColumnA = idColumnA;
            this.idColumnB = idColumnB;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new LinkKey(rs.getLong(idColumnA), rs.getLong(idColumnB));
        }

        public DbKey newKey(long idA, long idB) {
            return new LinkKey(idA, idB);
        }

    }

    final class LongKey implements DbKey {

        private final long id;

        private LongKey(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public int setPK(PreparedStatement pstmt) throws SQLException {
            return setPK(pstmt, 1);
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, id);
            return index + 1;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LongKey && ((LongKey)o).id == id;
        }

        @Override
        public int hashCode() {
            return (int)(id ^ (id >>> 32));
        }

    }

    final class StringKey implements DbKey {

        private final String id;

        private StringKey(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public int setPK(PreparedStatement pstmt) throws SQLException {
            return setPK(pstmt, 1);
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setString(index, id);
            return index + 1;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StringKey && (id != null ? id.equals(((StringKey)o).id) : ((StringKey)o).id == null);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

    }

    final class LinkKey implements DbKey {

        private final long idA;
        private final long idB;

        private LinkKey(long idA, long idB) {
            this.idA = idA;
            this.idB = idB;
        }

        public long[] getId() {
            return new long[]{idA, idB};
        }

        @Override
        public int setPK(PreparedStatement pstmt) throws SQLException {
            return setPK(pstmt, 1);
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setLong(index + 1, idB);
            return index + 2;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LinkKey && ((LinkKey) o).idA == idA && ((LinkKey) o).idB == idB;
        }

        @Override
        public int hashCode() {
            return (int)(idA ^ (idA >>> 32)) ^ (int)(idB ^ (idB >>> 32));
        }

    }

}
