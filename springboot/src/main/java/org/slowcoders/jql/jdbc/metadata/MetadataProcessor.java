package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.*;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataProcessor extends SchemaLoader {
    private final JdbcTemplate jdbc;
    private final String defaultSchema;
    private String catalog;
    private final HashMap<String, JqlSchema> metadataMap = new HashMap<>();

    public MetadataProcessor(DataSource dataSource, AttributeNameConverter nameConverter) {
        super(nameConverter);
        this.jdbc = new JdbcTemplate(dataSource);
        this.defaultSchema = jdbc.execute(new ConnectionCallback<String>() {
            @Override
            public String doInConnection(Connection conn) throws SQLException, DataAccessException {
                return conn.getSchema();
            }
        });
    }

    public String getDefaultDBSchema() { return this.defaultSchema; }


    public JqlSchema loadSchema(String tablePath) {
        int dot_p = tablePath.indexOf('.');
        String dbSchema = dot_p <= 0 ? defaultSchema : tablePath.substring(0, dot_p);
        String tableName = tablePath.substring(dot_p + 1);
        JqlSchema schema = metadataMap.get(tablePath);
        if (schema == null) {
            schema = jdbc.execute(new ConnectionCallback<JqlSchema>() {
                @Override
                public JqlSchema doInConnection(Connection conn) throws SQLException, DataAccessException {
                    JdbcSchema schema = new JdbcSchema(MetadataProcessor.this, tablePath);
                    metadataMap.put(tablePath, schema);
                    ArrayList<String> primaryKeys = getPrimaryKeys(conn, dbSchema, tableName);
                    ArrayList<JdbcColumn> columns = getColumns(conn, dbSchema, tableName, schema, primaryKeys);
                    schema.init(columns);
                    return schema;
                }
            });
        }
        return schema;
    }


    private ArrayList<String> getPrimaryKeys(Connection conn, String dbSchema, String tableName) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getPrimaryKeys(catalog, dbSchema, tableName);
        ArrayList<String> keys = new ArrayList<>();
        int next_key_seq = 1;
        while (rs.next()) {
            String key = rs.getString("column_name");
            int seq = rs.getInt("key_seq");
            if (seq != next_key_seq) {
                throw new RuntimeException("something wrong");
            }
            next_key_seq ++;
            keys.add(key);
        }
        return keys;
    }

    public List<String> getTableNames(String dbSchema) throws SQLException {
        List<String> tableNames = jdbc.execute(new ConnectionCallback<List<String>>() {
            @Override
            public List<String> doInConnection(Connection conn) throws SQLException, DataAccessException {
                return getTableNames(conn, dbSchema);
            }
        });
        return tableNames;
    }

    public List<String> getDBSchemas() {
        List<String> dbSchemas = jdbc.execute(new ConnectionCallback<List<String>>() {
            @Override
            public List<String> doInConnection(Connection conn) throws SQLException, DataAccessException {
                DatabaseMetaData md = conn.getMetaData();
                ResultSet rs = md.getSchemas();
                ArrayList<String> names = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString("TABLE_SCHEM");
                    names.add(name);
                }
                return names;
            }
        });
        return dbSchemas;
    }


    private ArrayList<String> getTableNames(Connection conn, String schema) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        String[] types = {"TABLE"};
        ResultSet rs = md.getTables(null, schema, "%", types);
        ArrayList<String> names = new ArrayList<>();
        while (rs.next()) {
            String name = rs.getString("TABLE_NAME");
            names.add(name);
        }
        return names;
    }

    private HashMap<String, JqlIndex> getIndexInfos(Connection conn, String dbSchema, String tableName) throws SQLException {
        HashMap<String, JqlIndex> indexes = new HashMap<>();

        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(catalog, dbSchema, tableName, false, false);
        while (rs.next()) {
            JqlIndex index = new JqlIndex(rs);
            indexes.put(index.column_name, index);
        }
        return indexes;
    }

    private HashMap<String, ColumnBinder> getForeignKeyInfos(Connection conn, String dbSchema, String tableName) throws SQLException {
        HashMap<String, ColumnBinder> foreignKeys = new HashMap<>();

        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getImportedKeys(catalog, dbSchema, tableName);
        while (rs.next()) {
            String pktable_schem = rs.getString("pktable_schem");
            String pktable_name  = rs.getString("pktable_name");

            String fktable_schem = rs.getString("fktable_schem");
            String fktable_name  = rs.getString("fktable_name");

            String pkColumn = rs.getString("pkcolumn_name");
            String fkColumn = rs.getString("fkcolumn_name");
            String fkTableName = makeTablePath(fktable_schem, fktable_name);
            String pkTableName = makeTablePath(pktable_schem, pktable_name);

            int key_seq = rs.getInt("key_seq");
            int update_rule = rs.getInt("update_rule");
            int delete_rule = rs.getInt("delete_rule");
            int deferrability = rs.getInt("deferrability");
            String pktable_cat = rs.getString("pktable_cat");
            String fktable_cat = rs.getString("fktable_cat");
            assert (pktable_cat == null && fktable_cat == null);

            ColumnBinder fk = new ColumnBinder(this, pkTableName, pkColumn);
            foreignKeys.put(fkColumn, fk);
        }
        return foreignKeys;
    }

    private ArrayList<JdbcColumn> getColumns(Connection conn, String dbSchema, String tableName, JqlSchema schema, ArrayList<String> primaryKeys) throws SQLException {
        HashMap<String, JqlIndex> indexes = getIndexInfos(conn, dbSchema, tableName);
        HashMap<String, ColumnBinder> foreignKeys = getForeignKeyInfos(conn, dbSchema, tableName);
        Map<String, String> comments = getColumnComments(conn, dbSchema, tableName);
        ArrayList<JdbcColumn> columns = new ArrayList<>();
        String qname = dbSchema == null ? tableName : dbSchema + "." + tableName;
        ResultSet rs = conn.createStatement().executeQuery("select * from " + qname + " limit 1");
        ResultSetMetaData md = rs.getMetaData();
        int cntColumn = md.getColumnCount();
        for (int col = 0; ++col <= cntColumn; ) {
            String columnName = md.getColumnName(col);
            ColumnBinder fk = foreignKeys.get(columnName);
            JqlIndex jqlIndex = indexes.get(columnName);
            String comment = comments.get(columnName);
            JdbcColumn ci = new JdbcColumn(schema, md, col, fk, jqlIndex, comment, primaryKeys);
            columns.add(ci);
        }
        return columns;
    }

    private Map<String, String> getColumnComments(Connection conn, String dbSchema, String tableName) throws SQLException {
        String sql = "SELECT c.column_name, pgd.description\n" +
                "FROM information_schema.columns c\n" +
                "    inner join pg_catalog.pg_statio_all_tables as st on (c.table_name = st.relname)\n" +
                "    inner join pg_catalog.pg_description pgd on (pgd.objoid=st.relid and\n" +
                "          pgd.objsubid=c.ordinal_position)\n" +
                "where c.table_schema = '" + dbSchema + "' and c.table_name = '" + tableName + "';\n";

        HashMap<String, String> comments = new HashMap<>();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            String columnName = rs.getString("column_name");
            String comment = rs.getString("description");
            comments.put(columnName, comment);
        }
        return comments;
    }

    private void dumResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        while (rs.next()) {
            for (int i = 0; ++i <= colCount; ) {
                String colName = meta.getColumnName(i);
                String value = rs.getString(i);
                System.out.println(colName + ": " + value);
            }
        }
    }

    @Override
    public String createDDL(JqlSchema schema) {
//        SQLWriter sb = new SQLWriter(schema);
//        sb.write("const " + schema.getTableName() + "Schema = [\n");
//        for (JqlColumn col : schema.getColumns()) {
//            sb.write(col.dumpJSONSchema()).write(",\n");
//        }
//        sb.write("]\n");
//        return sb.toString();
        throw new RuntimeException("not implemented");
    }


}
