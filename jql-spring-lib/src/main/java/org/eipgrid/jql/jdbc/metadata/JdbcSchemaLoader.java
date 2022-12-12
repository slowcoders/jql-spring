package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlSchemaJoin;
import org.eipgrid.jql.SchemaLoader;
import org.eipgrid.jql.util.AttributeNameConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class JdbcSchemaLoader extends SchemaLoader {
    private final JdbcTemplate jdbc;
    private final String defaultSchema;
    private String catalog;
    private final HashMap<String, JqlSchema> metadataMap = new HashMap<>();

    public JdbcSchemaLoader(DataSource dataSource, AttributeNameConverter nameConverter) {
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
        JqlSchema schema = metadataMap.get(tablePath);
        if (schema == null) {
            schema = jdbc.execute(new ConnectionCallback<JqlSchema>() {
                @Override
                public JqlSchema doInConnection(Connection conn) throws SQLException, DataAccessException {
                    return loadSchema(conn, tablePath);
                }
            });
        }
        return schema;
    }

    private JqlSchema loadSchema(Connection conn, String tablePath) throws SQLException {
        JdbcSchema schema = new JdbcSchema(JdbcSchemaLoader.this, tablePath);
        metadataMap.put(tablePath, schema);

        int dot_p = tablePath.indexOf('.');
        String dbSchema = dot_p <= 0 ? defaultSchema : tablePath.substring(0, dot_p);
        String tableName = tablePath.substring(dot_p + 1);
        ArrayList<String> primaryKeys = getPrimaryKeys(conn, dbSchema, tableName);
        ArrayList<JqlColumn> columns = getColumns(conn, dbSchema, tableName, schema, primaryKeys);
        HashMap<String, List<String>> uniqueConstraints = getUniqueConstraints(conn, dbSchema, tableName);
        processForeignKeyConstraints(conn, schema, dbSchema, tableName, columns);
        schema.init(columns, uniqueConstraints);
        return schema;
    }

    @Override
    protected HashMap<String, JqlSchemaJoin> loadJoinMap(JqlSchema pkSchema) {
        SchemaJoinHelper exportedJoins = jdbc.execute(new ConnectionCallback<SchemaJoinHelper>() {
            @Override
            public SchemaJoinHelper doInConnection(Connection conn) throws SQLException, DataAccessException {
                return getChildJoins(conn, (JdbcSchema)pkSchema, pkSchema.getNamespace(), pkSchema.getSimpleTableName());
            }
        });
        return createJoinMap((JdbcSchema)pkSchema, exportedJoins);
    }

    private HashMap<String, JqlSchemaJoin> createJoinMap(JdbcSchema schema, SchemaJoinHelper mappedJoins) {
        HashMap<String, JqlSchemaJoin> joinMap = new HashMap<>();
        for (JqlSchemaJoin join : schema.getForeignKeyConstraints().values()) {
            joinMap.put(join.getJsonKey(), join);
        }
        for (JqlSchemaJoin fkJoin : mappedJoins.values()) {
            List<JqlColumn> fkColumns = fkJoin.getForeignKeyColumns();
            JqlSchemaJoin childJoin = new JqlSchemaJoin(schema, fkColumns);
            joinMap.put(childJoin.getJsonKey(), childJoin);
            JdbcSchema childSchema = (JdbcSchema) fkJoin.getBaseSchema();
            Collection<JqlSchemaJoin> joins = childSchema.getForeignKeyConstraints().values();
            for (JqlSchemaJoin j2 : joins) {
                assert(!j2.isInverseMapped());
                if (j2 != fkJoin) {
                    JqlSchemaJoin associative = new JqlSchemaJoin(schema, fkColumns, j2);
                    joinMap.put(associative.getJsonKey(), associative);
                }
            }
        }
        return joinMap;
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

    private HashMap<String, List<String>> getUniqueConstraints(Connection conn, String dbSchema, String tableName) throws SQLException {
        HashMap<String, List<String>> indexMap = new HashMap<>();

        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(catalog, dbSchema, tableName, true, false);
        while (rs.next()) {
            String table_schem = rs.getString("table_schem");
            String table_name = rs.getString("table_name");
            String index_qualifier = rs.getString("index_qualifier");
            String index_name = rs.getString("index_name");
            String column_name = rs.getString("column_name");
            String filter_condition = rs.getString("filter_condition");
            boolean is_unique = !rs.getBoolean("non_unique");
            String sort = rs.getString("asc_or_desc");
            int type = rs.getInt("type");
            int ordinal_position = rs.getInt("ordinal_position");
            int cardinality = rs.getInt("cardinality");
            int pages = rs.getInt("pages");

            String table_cat = rs.getString("table_cat");
            assert(table_cat == null);
            assert(is_unique);

            List<String> indexes = indexMap.get(index_name);
            if (indexes == null) {
                indexes = new ArrayList<>();
                indexMap.put(index_name, indexes);
            }
            indexes.add(column_name);
        }
        return indexMap;
    }

    private JdbcColumn getColumn(ArrayList<JqlColumn> columns, String columnName) {
        for (JqlColumn column : columns) {
            if (columnName.equals(column.getColumnName())) {
                return (JdbcColumn)column;
            }
        }
        throw new RuntimeException("column not found: " + columnName);
    }

    private void processForeignKeyConstraints(Connection conn, JdbcSchema fkSchema, String dbSchema, String tableName, ArrayList<JqlColumn> columns) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getImportedKeys(catalog, dbSchema, tableName);
        while (rs.next()) {
            JoinData join = new JoinData(rs, this);
            JdbcColumn fk = getColumn(columns, join.fkColumnName);
            fk.bindPrimaryKey(new ColumnBinder(this, join.pkTableQName, join.pkColumnName));
            fkSchema.addForeignKeyConstraint(join.fk_name, fk);
        }
    }

    private SchemaJoinHelper getChildJoins(Connection conn, JdbcSchema pkSchema, String dbSchema, String tableName) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getExportedKeys(catalog, dbSchema, tableName);

        SchemaJoinHelper joins = new SchemaJoinHelper(pkSchema);
        while (rs.next()) {
            JoinData join = new JoinData(rs, this);
            JdbcSchema fkSchema = (JdbcSchema) loadSchema(join.fkTableQName);
            JqlSchemaJoin fkJoin = fkSchema.getForeignKeyConstraints().get(join.fk_name);
            joins.put(fkSchema, fkJoin);
        }
        return joins;
    }

    private ArrayList<JqlColumn> getColumns(Connection conn, String dbSchema, String tableName, JqlSchema schema, ArrayList<String> primaryKeys) throws SQLException {
        //HashMap<String, JqlIndex> indexes = getUniqueConstraints(conn, dbSchema, tableName);
        Map<String, String> comments = getColumnComments(conn, dbSchema, tableName);
        ArrayList<JqlColumn> columns = new ArrayList<>();
        String qname = dbSchema == null ? tableName : dbSchema + "." + tableName;
        ResultSet rs = conn.createStatement().executeQuery("select * from " + qname + " limit 1");
        ResultSetMetaData md = rs.getMetaData();
        int cntColumn = md.getColumnCount();
        for (int col = 0; ++col <= cntColumn; ) {
            String columnName = md.getColumnName(col);
            //ColumnBinder joinedPK = joinedPKs.get(columnName);
            String comment = comments.get(columnName);
            JdbcColumn ci = new JdbcColumn(schema, md, col, null, comment, primaryKeys);
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

    static class JoinData {
        String pk_name;
        String pktable_schem;
        String pktable_name;

        // 참고) fk_name 은 column-name 이 아니라, fk constraint 의 name 이다.
        String fk_name;
        String fktable_schem;
        String fktable_name;

        String pkColumnName;
        String fkColumnName;
        String fkTableQName;
        String pkTableQName;

        int key_seq;
        int update_rule;
        int delete_rule;
        int deferrability;
        String pktable_cat;
        String fktable_cat;

        JoinData(ResultSet rs, SchemaLoader loader) throws SQLException {
            this.pk_name = rs.getString("pk_name");
            this.pktable_schem = rs.getString("pktable_schem");
            this.pktable_name  = rs.getString("pktable_name");

            // 참고) fk_name 은 column-name 이 아니라, fk constraint 의 name 이다.
            this.fk_name = rs.getString("fk_name");
            this.fktable_schem = rs.getString("fktable_schem");
            this.fktable_name  = rs.getString("fktable_name");

            this.pkColumnName = rs.getString("pkcolumn_name");
            this.fkColumnName = rs.getString("fkcolumn_name");
            this.fkTableQName = loader.makeTablePath(fktable_schem, fktable_name);
            this.pkTableQName = loader.makeTablePath(pktable_schem, pktable_name);

            this.key_seq = rs.getInt("key_seq");
            this.update_rule = rs.getInt("update_rule");
            this.delete_rule = rs.getInt("delete_rule");
            this.deferrability = rs.getInt("deferrability");
            this.pktable_cat = rs.getString("pktable_cat");
            this.fktable_cat = rs.getString("fktable_cat");
            assert (pktable_cat == null && fktable_cat == null);
        }
    }
}
