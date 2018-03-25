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

package nxt.db;

import nxt.Db;
import nxt.util.Logger;
import nxt.util.ReadWriteUpdateLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.h2.api.Trigger;
import org.h2.tools.SimpleResultSet;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * FullTextTrigger provides Lucene search support.  Each searchable database has
 * a database trigger defined.  The Lucene index is updated whenever a row is
 * inserted, updated or deleted.  The DB_ID column is used to identify each row
 * and will be returned as the COLUMNS and KEYS values in the search results.
 *
 * Schema, table and column names are converted to uppercase to match the
 * way H2 stores the information.  Function aliases and triggers are created in
 * the default schema (PUBLIC).
 *
 * The database aliases are defined as follows:
 *   CREATE ALIAS FTL_CREATE_INDEX FOR "nxt.db.FullTextTrigger.createIndex"
 *       CALL FTL_CREATE(schema, table, columnList)
 *   CREATE ALIAS FTL_DROP_INDEX FOR "nxt.db.FullTextTrigger.dropIndex"
 *       CALL FTL_DROP(schema, table)
 *   CREATE ALIAS FTL_SEARCH FOR "nxt.db.FullTextTrigger.search"
 *       CALL FTL_SEARCH(schema, table, query, limit, offset)
 *
 * FTL_CREATE_INDEX is called to create a fulltext index for a table.  It is
 * provided as a convenience for use in NxtDbVersion when creating a new index
 * after the database has been created.
 *
 * FTL_DROP_INDEX is called to drop a fulltext index for a table.  It is
 * provided as a convenience for use in NxtDbVersion when dropping a table
 * after the database has been created.
 *
 * FTL_SEARCH is used to return the search result set as part of a SELECT statement.
 * The result set columns are the following:
 *   SCHEMA  - the schema name (String)
 *   TABLE   - the table name (String)
 *   COLUMNS - the primary key columns (String[]) - this is always DB_ID for NRS
 *   KEYS    - the primary key values (Long[]) - DB_ID value for the row
 *   SCORE   - the search hit score (Float)
 *
 * The table index trigger is defined as follows:
 *   CREATE TRIGGER trigger_name AFTER INSERT,UPDATE,DELETE ON table_name FOR EACH ROW CALL "nxt.db.FullTextTrigger"
 */
public class FullTextTrigger implements Trigger, TransactionalDb.TransactionCallback {

    /** NRS is active */
    private static volatile boolean isActive = false;

    /** Index triggers */
    private static final ConcurrentHashMap<String, FullTextTrigger> indexTriggers = new ConcurrentHashMap<>();

    /** Default filesystem */
    private static final FileSystem fileSystem = FileSystems.getDefault();

    /** Index lock */
    private static final ReadWriteUpdateLock indexLock = new ReadWriteUpdateLock();

    /** Lucene index path */
    private static Path indexPath;

    /** Lucene directory */
    private static Directory directory;

    /** Lucene index reader (thread-safe) */
    private static DirectoryReader indexReader;

    /** Lucene index searcher (thread-safe) */
    private static IndexSearcher indexSearcher;

    /** Lucene index writer (thread-safe) */
    private static IndexWriter indexWriter;

    /** Lucene analyzer (thread-safe) */
    private static final Analyzer analyzer = new StandardAnalyzer();

    /** Index trigger is enabled */
    private volatile boolean isEnabled = false;

    /** Table name (schema.table) */
    private String tableName;

    /** Column names */
    private final List<String> columnNames = new ArrayList<>();

    /** Column types */
    private final List<String> columnTypes = new ArrayList<>();

    /** Database identifier column ordinal */
    private int dbColumn = -1;

    /** Indexed column ordinals */
    private final List<Integer>indexColumns = new ArrayList<>();

    /** Pending table updates */
    private final List<TableUpdate> tableUpdates = new ArrayList<>();

    /**
     * This method is called by NRS initialization to indicate NRS is active.
     *
     * This is required since database triggers will be initialized when the database
     * is opened outside the NRS environment (for example, from the H2 console)
     *
     * The database triggers cannot be re-activated after they have been deactivated.
     *
     * @param   active              TRUE to enable database triggers
     */
    public static void setActive(boolean active) {
        isActive = active;
        if (!active) {
            indexTriggers.values().forEach((trigger) -> trigger.isEnabled = false);
            indexTriggers.clear();
            removeIndexAccess();
        }
    }

    /**
     * Initialize the fulltext support for a new database
     *
     * This method should be called from NxtDbVersion when performing the database version update
     * that enables NRS fulltext search support
     */
    public static void init() {
        String ourClassName = FullTextTrigger.class.getName();
        try (Connection conn = Db.db.getConnection();
                Statement stmt = conn.createStatement();
                Statement qstmt = conn.createStatement()) {
            //
            // Check if we have already been initialized.
            //
            boolean alreadyInitialized = true;
            boolean triggersExist = false;
            try (ResultSet rs = qstmt.executeQuery("SELECT JAVA_CLASS FROM INFORMATION_SCHEMA.TRIGGERS "
                    + "WHERE SUBSTRING(TRIGGER_NAME, 0, 4) = 'FTL_'")) {
                while (rs.next()) {
                    triggersExist = true;
                    if (!rs.getString(1).startsWith(ourClassName)) {
                        alreadyInitialized = false;
                    }
                }
            }
            if (triggersExist && alreadyInitialized) {
                Logger.logInfoMessage("NRS fulltext support is already initialized");
                return;
            }
            //
            // We need to delete an existing Lucene index since the V3 file format is not compatible with V5
            //
            getIndexPath(conn);
            removeIndexFiles(conn);
            //
            // Drop the H2 Lucene V3 function aliases
            //
            stmt.execute("DROP ALIAS IF EXISTS FTL_INIT");
            stmt.execute("DROP ALIAS IF EXISTS FTL_CREATE_INDEX");
            stmt.execute("DROP ALIAS IF EXISTS FTL_DROP_INDEX");
            stmt.execute("DROP ALIAS IF EXISTS FTL_DROP_ALL");
            stmt.execute("DROP ALIAS IF EXISTS FTL_REINDEX");
            stmt.execute("DROP ALIAS IF EXISTS FTL_SEARCH");
            stmt.execute("DROP ALIAS IF EXISTS FTL_SEARCH_DATA");
            Logger.logInfoMessage("H2 fulltext function aliases dropped");
            //
            // Create our schema and table
            //
            stmt.execute("CREATE SCHEMA IF NOT EXISTS FTL");
            stmt.execute("CREATE TABLE IF NOT EXISTS FTL.INDEXES "
                    + "(SCHEMA VARCHAR, TABLE VARCHAR, COLUMNS VARCHAR, PRIMARY KEY(SCHEMA, TABLE))");
            Logger.logInfoMessage("NRS fulltext schema created");
            //
            // Drop existing triggers and create our triggers.  H2 will initialize the trigger
            // when it is created.  H2 has already initialized the existing triggers and they
            // will be closed when dropped.  The H2 Lucene V3 trigger initialization will work with
            // Lucene V5, so we are able to open the database using the Lucene V5 library files.
            //
            try (ResultSet rs = qstmt.executeQuery("SELECT * FROM FTL.INDEXES")) {
                while(rs.next()) {
                    String schema = rs.getString("SCHEMA");
                    String table = rs.getString("TABLE");
                    stmt.execute("DROP TRIGGER IF EXISTS FTL_" + table);
                    stmt.execute(String.format("CREATE TRIGGER FTL_%s AFTER INSERT,UPDATE,DELETE ON %s.%s "
                            + "FOR EACH ROW CALL \"%s\"",
                            table, schema, table, ourClassName));
                }
            }
            //
            // Rebuild the Lucene index since the Lucene V3 index is not compatible with Lucene V5
            //
            reindex(conn);
            //
            // Create our function aliases
            //
            stmt.execute("CREATE ALIAS FTL_CREATE_INDEX FOR \"" + ourClassName + ".createIndex\"");
            stmt.execute("CREATE ALIAS FTL_DROP_INDEX FOR \"" + ourClassName + ".dropIndex\"");
            stmt.execute("CREATE ALIAS FTL_SEARCH NOBUFFER FOR \"" + ourClassName + ".search\"");
            Logger.logInfoMessage("NRS fulltext aliases created");
        } catch (SQLException exc) {
            Logger.logErrorMessage("Unable to initialize NRS fulltext search support", exc);
            throw new RuntimeException(exc.toString(), exc);
        }
    }

    /**
     * Reindex all of the indexed tables
     *
     * @param   conn                SQL connection
     * @throws  SQLException        Unable to reindex tables
     */
    public static void reindex(Connection conn) throws SQLException {
        Logger.logInfoMessage("Rebuilding the Lucene search index");
        try {
            //
            // Delete the current Lucene index
            //
            removeIndexFiles(conn);
            //
            // Reindex each table
            //
            for (FullTextTrigger trigger : indexTriggers.values()) {
                trigger.reindexTable(conn);
            }
        } catch (SQLException exc) {
            throw new SQLException("Unable to rebuild the Lucene index", exc);
        }
        Logger.logInfoMessage("Lucene search index successfully rebuilt");
    }

    /**
     * Create the fulltext index for a table
     *
     * @param   conn                SQL connection
     * @param   schema              Schema name
     * @param   table               Table name
     * @param   columnList          Indexed column names separated by commas
     * @throws  SQLException        Unable to create fulltext index
     */
    public static void createIndex(Connection conn, String schema, String table, String columnList)
                                    throws SQLException {
        String upperSchema = schema.toUpperCase(Locale.ROOT);
        String upperTable = table.toUpperCase(Locale.ROOT);
        String tableName = upperSchema + "." + upperTable;
        getIndexAccess(conn);
        //
        // Drop an existing index and the associated database trigger
        //
        dropIndex(conn, schema, table);
        //
        // Update our schema and create a new database trigger.  Note that the trigger
        // will be initialized when it is created.
        //
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(String.format("INSERT INTO FTL.INDEXES (schema, table, columns) "
                    + "VALUES('%s', '%s', '%s')",
                    upperSchema, upperTable, columnList.toUpperCase(Locale.ROOT)));
            stmt.execute(String.format("CREATE TRIGGER FTL_%s AFTER INSERT,UPDATE,DELETE ON %s "
                    + "FOR EACH ROW CALL \"%s\"",
                    upperTable, tableName, FullTextTrigger.class.getName()));
        }
        //
        // Index the table
        //
        FullTextTrigger trigger = indexTriggers.get(tableName);
        if (trigger == null) {
            Logger.logErrorMessage("NRS fulltext trigger for table " + tableName + " was not initialized");
        } else {
            try {
                trigger.reindexTable(conn);
                Logger.logInfoMessage("Lucene search index created for table " + tableName);
            } catch (SQLException exc) {
                Logger.logErrorMessage("Unable to create Lucene search index for table " + tableName);
                throw new SQLException("Unable to create Lucene search index for table " + tableName, exc);
            }
        }
    }

    /**
     * Drop the fulltext index for a table
     *
     * @param   conn                SQL connection
     * @param   schema              Schema name
     * @param   table               Table name
     * @throws  SQLException        Unable to drop fulltext index
     */
    public static void dropIndex(Connection conn, String schema, String table) throws SQLException {
        String upperSchema = schema.toUpperCase(Locale.ROOT);
        String upperTable = table.toUpperCase(Locale.ROOT);
        boolean reindex = false;
        //
        // Drop an existing database trigger
        //
        try (Statement qstmt = conn.createStatement();
                Statement stmt = conn.createStatement()) {
            try (ResultSet rs = qstmt.executeQuery(String.format(
                    "SELECT COLUMNS FROM FTL.INDEXES WHERE SCHEMA = '%s' AND TABLE = '%s'",
                    upperSchema, upperTable))) {
                if (rs.next()) {
                    stmt.execute("DROP TRIGGER IF EXISTS FTL_" + upperTable);
                    stmt.execute(String.format("DELETE FROM FTL.INDEXES WHERE SCHEMA = '%s' AND TABLE = '%s'",
                            upperSchema, upperTable));
                    reindex = true;
                }
            }
        }
        //
        // Rebuild the Lucene index
        //
        if (reindex) {
            reindex(conn);
        }
    }

    /**
     * Drop all fulltext indexes
     *
     * @param   conn                SQL connection
     * @throws  SQLException        Unable to drop fulltext indexes
     */
    public static void dropAll(Connection conn) throws SQLException {
        //
        // Drop existing triggers
        //
        try (Statement qstmt = conn.createStatement();
                Statement stmt = conn.createStatement();
                ResultSet rs = qstmt.executeQuery("SELECT TABLE FROM FTL.INDEXES")) {
            while(rs.next()) {
                String table = rs.getString(1);
                stmt.execute("DROP TRIGGER IF EXISTS FTL_" + table);
            }
            stmt.execute("TRUNCATE TABLE FTL.INDEXES");
            indexTriggers.clear();
        }
        //
        // Delete the Lucene index
        //
        removeIndexFiles(conn);
    }

    /**
     * Search the Lucene index
     *
     * The result set will have the following columns:
     *   SCHEMA  - Schema name (String)
     *   TABLE   - Table name (String)
     *   COLUMNS - Primary key column names (String[]) - this is always DB_ID
     *   KEYS    - Primary key values (Long[]) - this is always the DB_ID value for the table row
     *   SCORE   - Lucene score (Float)
     *
     * @param   conn                SQL connection
     * @param   schema              Schema name
     * @param   table               Table name
     * @param   queryText           Query expression
     * @param   limit               Number of rows to return
     * @param   offset              Offset with result set
     * @return                      Search results
     * @throws  SQLException        Unable to search the index
     */
    public static ResultSet search(Connection conn, String schema, String table, String queryText, int limit, int offset)
                                    throws SQLException {
        //
        // Get Lucene index access
        //
        getIndexAccess(conn);
        //
        // Create the result set columns
        //
        SimpleResultSet result = new SimpleResultSet();
        result.addColumn("SCHEMA", Types.VARCHAR, 0, 0);
        result.addColumn("TABLE", Types.VARCHAR, 0, 0);
        result.addColumn("COLUMNS", Types.ARRAY, 0, 0);
        result.addColumn("KEYS", Types.ARRAY, 0, 0);
        result.addColumn("SCORE", Types.FLOAT, 0, 0);
        //
        // Perform the search
        //
        // The _QUERY field contains the table and row identification (schema.table;keyName;keyValue)
        // The _TABLE field is used to limit the search results to the current table
        // The _DATA field contains the indexed row data (this is the default search field)
        // The _MODIFIED field contains the row modification time (YYYYMMDDhhmmss) in GMT
        //
        indexLock.readLock().lock();
        try {
            QueryParser parser = new QueryParser("_DATA", analyzer);
            parser.setDateResolution("_MODIFIED", DateTools.Resolution.SECOND);
            parser.setDefaultOperator(QueryParser.Operator.AND);
            Query query = parser.parse("_TABLE:" + schema.toUpperCase(Locale.ROOT) + "." + table.toUpperCase(Locale.ROOT) + " AND (" + queryText + ")");
            TopDocs documents = indexSearcher.search(query, limit);
            ScoreDoc[] hits = documents.scoreDocs;
            int resultCount = Math.min(hits.length, (limit == 0 ? hits.length : limit));
            int resultOffset = Math.min(offset, resultCount);
            for (int i=resultOffset; i<resultCount; i++) {
                Document document = indexSearcher.doc(hits[i].doc);
                String[] indexParts = document.get("_QUERY").split(";");
                String[] nameParts = indexParts[0].split("\\.");
                result.addRow(nameParts[0],
                              nameParts[1],
                              new String[] {indexParts[1]},
                              new Long[] {Long.parseLong(indexParts[2])},
                              hits[i].score);
            }
        } catch (ParseException exc) {
            Logger.logDebugMessage("Lucene parse exception for query: " + queryText + "\n" + exc.getMessage());
            throw new SQLException("Lucene parse exception for query: " + queryText + "\n" + exc.getMessage());
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to search Lucene index", exc);
            throw new SQLException("Unable to search Lucene index", exc);
        } finally {
            indexLock.readLock().unlock();
        }
        return result;
    }

    /**
     * Initialize the trigger (Trigger interface)
     *
     * @param   conn                Database connection
     * @param   schema              Database schema name
     * @param   trigger             Database trigger name
     * @param   table               Database table name
     * @param   before              TRUE if trigger is called before database operation
     * @param   type                Trigger type
     * @throws  SQLException        A SQL error occurred
     */
    @Override
    public void init(Connection conn, String schema, String trigger, String table, boolean before, int type)
                                    throws SQLException {
        //
        // Ignore the trigger if NRS is not active or this is a temporary table copy
        //
        if (!isActive || table.contains("_COPY_")) {
            return;
        }
        //
        // Access the Lucene index
        //
        // We need to get the access just once, either in a trigger or in a function alias
        //
        getIndexAccess(conn);
        //
        // Get table and index information
        //
        tableName = schema + "." + table;
        try (Statement stmt = conn.createStatement()) {
            //
            // Get the table column information
            //
            // NRS tables use DB_ID as the primary index
            //
            try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + table + " FROM " + schema)) {
                int index = 0;
                while (rs.next()) {
                    String columnName = rs.getString("FIELD");
                    String columnType = rs.getString("TYPE");
                    columnType = columnType.substring(0, columnType.indexOf('('));
                    columnNames.add(columnName);
                    columnTypes.add(columnType);
                    if (columnName.equals("DB_ID")) {
                        dbColumn = index;
                    }
                    index++;
                }
            }
            if (dbColumn < 0) {
                Logger.logErrorMessage("DB_ID column not found for table " + tableName);
                return;
            }
            //
            // Get the indexed columns
            //
            // Indexed columns must be strings (VARCHAR)
            //
            try (ResultSet rs = stmt.executeQuery(String.format(
                    "SELECT COLUMNS FROM FTL.INDEXES WHERE SCHEMA = '%s' AND TABLE = '%s'",
                    schema, table))) {
                if (rs.next()) {
                    String[] columns = rs.getString(1).split(",");
                    for (String column : columns) {
                        int pos = columnNames.indexOf(column);
                        if (pos >= 0) {
                            if (columnTypes.get(pos).equals("VARCHAR")) {
                                indexColumns.add(pos);
                            } else {
                                Logger.logErrorMessage("Indexed column " + column + " in table " + tableName + " is not a string");
                            }
                        } else {
                            Logger.logErrorMessage("Indexed column " + column + " not found in table " + tableName);
                        }
                    }
                }
            }
            if (indexColumns.isEmpty()) {
                Logger.logErrorMessage("No indexed columns found for table " + tableName);
                return;
            }
            //
            // Trigger is enabled
            //
            isEnabled = true;
            indexTriggers.put(tableName, this);
        } catch (SQLException exc) {
            Logger.logErrorMessage("Unable to get table information", exc);
        }
    }

    /**
     * Close the trigger (Trigger interface)
     */
    @Override
    public void close() {
        if (isEnabled) {
            isEnabled = false;
            indexTriggers.remove(tableName);
        }
    }

    /**
     * Remove the trigger (Trigger interface)
     */
    @Override
    public void remove() {
        if (isEnabled) {
            isEnabled = false;
            indexTriggers.remove(tableName);
        }
    }

    /**
     * Trigger has fired (Trigger interface)
     *
     * @param   conn                Database connection
     * @param   oldRow              The old row or null
     * @param   newRow              The new row or null
     */
    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) {
        //
        // Ignore the trigger if it is not enabled
        //
        if (!isEnabled) {
            return;
        }
        //
        // Commit the change immediately if we are not in a transaction
        //
        if (!Db.db.isInTransaction()) {
            try {
                commitRow(oldRow, newRow);
                commitIndex();
            } catch (SQLException exc) {
                Logger.logErrorMessage("Unable to update the Lucene index", exc);
            }
            return;
        }
        //
        // Save the table update until the update is committed or rolled back.  Note
        // that the current thread is the application thread performing the update operation.
        //
        synchronized(tableUpdates) {
            tableUpdates.add(new TableUpdate(Thread.currentThread(), oldRow, newRow));
        }
        //
        // Register our transaction callback
        //
        Db.db.registerCallback(this);
    }

    /**
     * Commit the table changes for the current transaction (TransactionCallback interface)
     */
    @Override
    public void commit() {
        Thread thread = Thread.currentThread();
        try {
            //
            // Update the Lucene index.  Note that a database transaction is associated
            // with a single thread.  So we will commit just those updates generated
            // by the current thread.
            //
            boolean commit = false;
            synchronized(tableUpdates) {
                Iterator<TableUpdate> updateIt = tableUpdates.iterator();
                while (updateIt.hasNext()) {
                    TableUpdate update = updateIt.next();
                    if (update.getThread() == thread) {
                        commitRow(update.getOldRow(), update.getNewRow());
                        updateIt.remove();
                        commit = true;
                    }
                }
            }
            //
            // Commit the index updates
            //
            if (commit) {
                commitIndex();
            }
        } catch (SQLException exc) {
            Logger.logErrorMessage("Unable to update the Lucene index", exc);
        }
    }

    /**
     * Discard the table changes for the current transaction (TransactionCallback interface)
     */
    @Override
    public void rollback() {
        Thread thread = Thread.currentThread();
        synchronized(tableUpdates) {
            tableUpdates.removeIf(update -> update.getThread() == thread);
        }
    }

    /**
     * Update the Lucene index for a committed row
     *
     * @param   oldRow              Old row column data
     * @param   newRow              New row column data
     * @throws  SQLException        Unable to commit row
     */
    private void commitRow(Object[] oldRow, Object[] newRow) throws SQLException {
        if (oldRow != null) {
            if (newRow != null) {
                indexRow(newRow);
            } else {
                deleteRow(oldRow);
            }
        } else if (newRow != null) {
            indexRow(newRow);
        }
    }

    /**
     * Reindex the table
     *
     * @param   conn                SQL connection
     * @throws  SQLException        Unable to reindex table
     */
    private void reindexTable(Connection conn) throws SQLException {
        if (indexColumns.isEmpty()) {
            return;
        }
        //
        // Build the SELECT statement for just the indexed columns
        //
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DB_ID");
        for (int index : indexColumns) {
            sb.append(", ").append(columnNames.get(index));
        }
        sb.append(" FROM ").append(tableName);
        Object[] row = new Object[columnNames.size()];
        //
        // Index each row in the table
        //
        try (Statement qstmt = conn.createStatement();
                ResultSet rs = qstmt.executeQuery(sb.toString())) {
            while (rs.next()) {
                row[dbColumn] = rs.getObject(1);
                int i = 2;
                for (int index : indexColumns) {
                    row[index] = rs.getObject(i++);
                }
                indexRow(row);
            }
        }
        //
        // Commit the index updates
        //
        commitIndex();
    }

    /**
     * Index a row
     *
     * @param   row                 Row column data
     * @throws  SQLException        Unable to index row
     */
    private void indexRow(Object[] row) throws SQLException {
        indexLock.readLock().lock();
        try {
            String query = tableName + ";" + columnNames.get(dbColumn) + ";" + (Long)row[dbColumn];
            Document document = new Document();
            document.add(new StringField("_QUERY", query, Field.Store.YES));
            long now = System.currentTimeMillis();
            document.add(new TextField("_MODIFIED", DateTools.timeToString(now, DateTools.Resolution.SECOND), Field.Store.NO));
            document.add(new TextField("_TABLE", tableName, Field.Store.NO));
            StringJoiner sj = new StringJoiner(" ");
            for (int index : indexColumns) {
                String data = (row[index] != null ? (String)row[index] : "NULL");
                document.add(new TextField(columnNames.get(index), data, Field.Store.NO));
                sj.add(data);
            }
            document.add(new TextField("_DATA", sj.toString(), Field.Store.NO));
            indexWriter.updateDocument(new Term("_QUERY", query), document);
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to index row", exc);
            throw new SQLException("Unable to index row", exc);
        } finally {
            indexLock.readLock().unlock();
        }
    }

    /**
     * Delete an indexed row
     *
     * @param   row                     Row being deleted
     * @throws  SQLException            Unable to delete row
     */
    private void deleteRow(Object[] row) throws SQLException {
        String query = tableName + ";" + columnNames.get(dbColumn) + ";" + (Long)row[dbColumn];
        indexLock.readLock().lock();
        try {
            indexWriter.deleteDocuments(new Term("_QUERY", query));
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to delete indexed row", exc);
            throw new SQLException("Unable to delete indexed row", exc);
        } finally {
            indexLock.readLock().unlock();
        }
    }

    /**
     * Commit the index updates
     *
     * @throws  SQLException        Unable to commit index updates
     */
    private static void commitIndex() throws SQLException {
        indexLock.writeLock().lock();
        try {
            indexWriter.commit();
            DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
            if (newReader != null) {
                indexReader.close();
                indexReader = newReader;
                indexSearcher = new IndexSearcher(indexReader);
            }
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to commit Lucene index updates", exc);
            throw new SQLException("Unable to commit Lucene index updates", exc);
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * Get the Lucene index path
     *
     * @param   conn                SQL connection
     * @throws  SQLException        Unable to get the Lucene index path
     */
    private static void getIndexPath(Connection conn) throws SQLException {
        indexLock.writeLock().lock();
        try {
            if (indexPath == null) {
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("CALL DATABASE_PATH()")) {
                    rs.next();
                    indexPath = fileSystem.getPath(rs.getString(1));
                    if (!Files.exists(indexPath)) {
                        Files.createDirectory(indexPath);
                    }
                } catch (IOException exc) {
                    Logger.logErrorMessage("Unable to create the Lucene index directory", exc);
                    throw new SQLException("Unable to create the Lucene index directory", exc);
                }
            }
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * Get the Lucene index access
     *
     * @param   conn                SQL connection
     * @throws  SQLException        Unable to access the Lucene index
     */
    private static void getIndexAccess(Connection conn) throws SQLException {
        if (!isActive) {
            throw new SQLException("NRS is no longer active");
        }
        boolean obtainedUpdateLock = false;
        if (!indexLock.writeLock().hasLock()) {
            indexLock.updateLock().lock();
            obtainedUpdateLock = true;
        }
        try {
            if (indexPath == null || indexWriter == null) {
                indexLock.writeLock().lock();
                try {
                    if (indexPath == null) {
                        getIndexPath(conn);
                    }
                    if (directory == null) {
                        directory = FSDirectory.open(indexPath);
                    }
                    if (indexWriter == null) {
                        IndexWriterConfig config = new IndexWriterConfig(analyzer);
                        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                        indexWriter = new IndexWriter(directory, config);
                        Document document = new Document();
                        document.add(new StringField("_QUERY", "_CONTROL_DOCUMENT_", Field.Store.YES));
                        indexWriter.updateDocument(new Term("_QUERY", "_CONTROL_DOCUMENT_"), document);
                        indexWriter.commit();
                        indexReader = DirectoryReader.open(directory);
                        indexSearcher = new IndexSearcher(indexReader);
                    }
                } finally {
                    indexLock.writeLock().unlock();
                }
            }
        } catch (IOException | SQLException exc) {
            Logger.logErrorMessage("Unable to access the Lucene index", exc);
            throw new SQLException("Unable to access the Lucene index", exc);
        } finally {
            if (obtainedUpdateLock) {
                indexLock.updateLock().unlock();
            }
        }
    }

    /**
     * Remove Lucene index access
     */
    private static void removeIndexAccess() {
        indexLock.writeLock().lock();
        try {
            if (indexSearcher != null) {
                indexSearcher = null;
            }
            if (indexReader != null) {
                indexReader.close();
                indexReader = null;
            }
            if (indexWriter != null) {
                indexWriter.close();
                indexWriter = null;
            }
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to remove Lucene index access", exc);
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * Remove the Lucene index files
     *
     * @param   conn                SQL connection
     * @throws  SQLException        I/O error occurred
     */
    private static void removeIndexFiles(Connection conn) throws SQLException {
        indexLock.writeLock().lock();
        try {
            //
            // Remove current Lucene index access
            //
            removeIndexAccess();
            //
            // Delete the index files
            //
            getIndexPath(conn);
            try (Stream<Path> stream = Files.list(indexPath)) {
                Path[] paths = stream.toArray(Path[]::new);
                for (Path path : paths) {
                    Files.delete(path);
                }
            }
            Logger.logInfoMessage("Lucene search index deleted");
            //
            // Get Lucene index access once more
            //
            getIndexAccess(conn);
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to remove Lucene index files", exc);
            throw new SQLException("Unable to remove Lucene index files", exc);
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * Table update
     */
    private static class TableUpdate {

        /** Transaction thread */
        private final Thread thread;

        /** Old table row */
        private final Object[] oldRow;

        /** New table row */
        private final Object[] newRow;

        /**
         * Create the table update
         *
         * @param   thread          Transaction thread
         * @param   oldRow          Old table row or null
         * @param   newRow          New table row or null
         */
        public TableUpdate(Thread thread, Object[] oldRow, Object[] newRow) {
            this.thread = thread;
            this.oldRow = oldRow;
            this.newRow = newRow;
        }

        /**
         * Return the transaction thread
         *
         * @return                  Transaction thread
         */
        public Thread getThread() {
            return thread;
        }

        /**
         * Return the old table row
         *
         * @return                  Old table row or null
         */
        public Object[] getOldRow() {
            return oldRow;
        }

        /**
         * Return the new table row
         *
         * @return                  New table row or null
         */
        public Object[] getNewRow() {
            return newRow;
        }
    }
}
