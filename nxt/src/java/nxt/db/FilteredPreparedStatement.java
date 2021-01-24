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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.util.Calendar;

/**
 * Wrapper for a SQL PreparedStatement
 *
 * The wrapper forwards all methods to the wrapped prepared statement
 */
public class FilteredPreparedStatement extends FilteredStatement implements PreparedStatement {

    private final PreparedStatement stmt;
    private final String sql;

    public FilteredPreparedStatement(PreparedStatement stmt, String sql) {
        super(stmt);
        this.stmt = stmt;
        this.sql = sql;
    }

    public String getSQL() {
        return sql;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return stmt.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        return stmt.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        stmt.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        stmt.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        stmt.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        stmt.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        stmt.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        stmt.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        stmt.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        stmt.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        stmt.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        stmt.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        stmt.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        stmt.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {
        stmt.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {
        stmt.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
        stmt.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
        stmt.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        stmt.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        stmt.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        stmt.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        return stmt.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        stmt.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException {
        stmt.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef (int parameterIndex, Ref x) throws SQLException {
        stmt.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob (int parameterIndex, Blob x) throws SQLException {
        stmt.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob (int parameterIndex, Clob x) throws SQLException {
        stmt.setClob(parameterIndex, x);
    }

    @Override
    public void setArray (int parameterIndex, Array x) throws SQLException {
        stmt.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return stmt.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        stmt.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
        stmt.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
        stmt.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull (int parameterIndex, int sqlType, String typeName) throws SQLException {
        stmt.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
        stmt.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return stmt.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        stmt.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        stmt.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        stmt.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        stmt.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        stmt.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        stmt.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        stmt.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        stmt.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        stmt.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {
        stmt.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {
        stmt.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, java.io.Reader reader, long length) throws SQLException {
        stmt.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, java.io.InputStream x) throws SQLException {
        stmt.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, java.io.InputStream x) throws SQLException {
        stmt.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, java.io.Reader reader) throws SQLException {
        stmt.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        stmt.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        stmt.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        stmt.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        stmt.setNClob(parameterIndex, reader);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        stmt.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        stmt.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return stmt.executeLargeUpdate();
    }

    @Deprecated
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        stmt.setUnicodeStream(parameterIndex, x, length);
    }
}
