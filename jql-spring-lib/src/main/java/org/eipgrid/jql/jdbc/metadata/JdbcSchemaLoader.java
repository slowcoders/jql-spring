package org.eipgrid.jql.jdbc.metadata;

import org.eipgrid.jql.*;
import org.eipgrid.jql.jdbc.QueryGenerator;
import org.eipgrid.jql.jdbc.SqlGenerator;
import org.eipgrid.jql.jpa.JpaSchema;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QJoin;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.schema.SchemaLoader;
import org.eipgrid.jql.util.AttributeNameConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class JdbcSchemaLoader extends SchemaLoader implements QueryGenerator {
    private final JdbcTemplate jdbc;
    private final String defaultSchema;
    private String catalog;
    private final HashMap<String, QSchema> metadataMap = new HashMap<>();

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


    public QSchema loadSchema(String tablePath0, Class<?> ormType) {
        if (tablePath0 == null) {
            QSchema schema = new JpaSchema(this, tablePath0, ormType);
            throw new RuntimeException("not impl");
        }

        final String tablePath = tablePath0.toLowerCase();
        QSchema schema = metadataMap.get(tablePath);
        if (schema == null) {
            schema = jdbc.execute(new ConnectionCallback<QSchema>() {
                @Override
                public QSchema doInConnection(Connection conn) throws SQLException, DataAccessException {
                    return loadSchema(conn, tablePath, ormType);
                }
            });
        }
        return schema;
    }

    private QSchema loadSchema(Connection conn, String tablePath, Class<?> ormType) throws SQLException {
        JdbcSchema schema = new JdbcSchema(JdbcSchemaLoader.this, tablePath);
        metadataMap.put(tablePath, schema);

        int dot_p = tablePath.indexOf('.');
        String dbSchema = dot_p <= 0 ? defaultSchema : tablePath.substring(0, dot_p);
        String tableName = tablePath.substring(dot_p + 1);
        ArrayList<String> primaryKeys = getPrimaryKeys(conn, dbSchema, tableName);
        HashMap<String, ArrayList<String>> uniqueConstraints = getUniqueConstraints(conn, dbSchema, tableName);
        if (primaryKeys.size() == 0) {
            for (ArrayList<String> keys : uniqueConstraints.values()) {
                if (primaryKeys.size() == 0 || keys.size() < primaryKeys.size()) {
                    primaryKeys = keys;
                }
            }
        }
        ArrayList<QColumn> columns = getColumns(conn, dbSchema, tableName, schema, primaryKeys);
        processForeignKeyConstraints(conn, schema, dbSchema, tableName, columns);
        schema.init(columns, uniqueConstraints, ormType);
        return schema;
    }

    @Override
    protected HashMap<String, QJoin> loadJoinMap(QSchema schema) {
        EntityJoinHelper exportedJoins = jdbc.execute(new ConnectionCallback<EntityJoinHelper>() {
            @Override
            public EntityJoinHelper doInConnection(Connection conn) throws SQLException, DataAccessException {
                return getChildJoins(conn, (JdbcSchema) schema, schema.getNamespace(), schema.getSimpleTableName());
            }
        });
        return createJoinMap((JdbcSchema) schema, exportedJoins);
    }

    private HashMap<String, QJoin> createJoinMap(JdbcSchema schema, EntityJoinHelper mappedJoins) {
        HashMap<String, QJoin> joinMap = new HashMap<>();
        for (QJoin join : schema.getForeignKeyConstraints().values()) {
            joinMap.put(join.getJsonKey(), join);
        }
        for (QJoin fkJoin : mappedJoins.values()) {
            List<QColumn> fkColumns = fkJoin.getForeignKeyColumns();
            QJoin childJoin = new QJoin(schema, fkColumns);
            joinMap.put(childJoin.getJsonKey(), childJoin);
            JdbcSchema childSchema = (JdbcSchema) fkJoin.getBaseSchema();
            Collection<QJoin> joins = childSchema.getForeignKeyConstraints().values();
            for (QJoin j2 : joins) {
                assert(!j2.isInverseMapped());
                if (j2 != fkJoin) {
                    QJoin associative = new QJoin(schema, fkColumns, j2);
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

    private HashMap<String, ArrayList<String>> getUniqueConstraints(Connection conn, String dbSchema, String tableName) throws SQLException {
        HashMap<String, ArrayList<String>> indexMap = new HashMap<>();

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

            ArrayList<String> indexes = indexMap.get(index_name);
            if (indexes == null) {
                indexes = new ArrayList<>();
                indexMap.put(index_name, indexes);
            }
            indexes.add(column_name);
        }
        return indexMap;
    }

    private JdbcColumn getColumn(ArrayList<QColumn> columns, String columnName) {
        for (QColumn column : columns) {
            if (columnName.equals(column.getPhysicalName())) {
                return (JdbcColumn)column;
            }
        }
        throw new RuntimeException("column not found: " + columnName);
    }

    private void processForeignKeyConstraints(Connection conn, JdbcSchema fkSchema, String dbSchema, String tableName, ArrayList<QColumn> columns) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getImportedKeys(catalog, dbSchema, tableName);
        while (rs.next()) {
            JoinData join = new JoinData(rs, this);
            JdbcColumn fk = getColumn(columns, join.fkColumnName);
            fk.bindPrimaryKey(new ColumnBinder(this, join.pkTableQName, join.pkColumnName));
            fkSchema.addForeignKeyConstraint(join.fk_name, fk);
        }
    }

    private EntityJoinHelper getChildJoins(Connection conn, JdbcSchema pkSchema, String dbSchema, String tableName) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getExportedKeys(catalog, dbSchema, tableName);

        EntityJoinHelper joins = new EntityJoinHelper(pkSchema);
        while (rs.next()) {
            JoinData join = new JoinData(rs, this);
            // @TODO loadSchema with ORM type
            JdbcSchema fkSchema = (JdbcSchema) loadSchema(join.fkTableQName, null);
            QJoin fkJoin = fkSchema.getForeignKeyConstraints().get(join.fk_name);
            assert(fkJoin != null);
            joins.put(fkSchema, fkJoin);
        }
        return joins;
    }

    private ArrayList<QColumn> getColumns(Connection conn, String dbSchema, String tableName, QSchema schema, ArrayList<String> primaryKeys) throws SQLException {
        //HashMap<String, JqlIndex> indexes = getUniqueConstraints(conn, dbSchema, tableName);
        Map<String, String> comments = getColumnComments(conn, dbSchema, tableName);
        ArrayList<QColumn> columns = new ArrayList<>();
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

    public String createDDL(QSchema schema) {
//        SQLWriter sb = new SQLWriter(schema);
//        sb.write("const " + schema.getTableName() + "Schema = [\n");
//        for (JQColumn col : schema.getColumns()) {
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

    @Override
    public String createSelectQuery(JqlQuery query) {
        return createSqlGenerator().createSelectQuery(query);
    }

    @Override
    public String createCountQuery(JqlFilter where) {
        return createSqlGenerator().createCountQuery(where);
    }

    @Override
    public String createUpdateQuery(JqlFilter where, Map<String, Object> updateSet) {
        return createSqlGenerator().createUpdateQuery(where, updateSet);
    }

    @Override
    public String createDeleteQuery(JqlFilter where) {
        return createSqlGenerator().createDeleteQuery(where);
    }

    @Override
    public String prepareFindByIdStatement(QSchema schema) {
        return createSqlGenerator().prepareFindByIdStatement(schema);
    }

    @Override
    public String createInsertStatement(QSchema schema, Map entity, boolean ignoreConflict) {
        return createSqlGenerator().createInsertStatement(schema, entity, ignoreConflict);
    }

    protected SqlGenerator createSqlGenerator() {
        return new SqlGenerator();
    }
}
