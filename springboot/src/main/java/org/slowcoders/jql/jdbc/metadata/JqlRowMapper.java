package org.slowcoders.jql.jdbc.metadata;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JqlRowMapper implements RowMapper<KVEntity> {
    private final JqlSchema schema;
    private final HashMap<String, ArrayList<String>> joinMap = new HashMap<>();

    public JqlRowMapper(JqlSchema schema) {
        this.schema = schema;
    }

    @Override
    public KVEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        JqlSchema jqlSchema = schema;
        KVEntity baseEntity = new KVEntity();
        KVEntity subEntity = baseEntity;
        String currTableName = null;
        String currDbSchema = null;

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int idxColumn = 1; idxColumn <= columnCount; idxColumn++) {
            String column = JdbcUtils.lookupColumnName(rsmd, idxColumn);
            String tableName = rsmd.getTableName(idxColumn);
            String dbSchema = rsmd.getSchemaName(idxColumn);
            if (!tableName.equals(currTableName) || !dbSchema.equals(currDbSchema)) {
                if (currTableName != null) {
                    jqlSchema = (JqlSchema) schema.getSchemaLoader().loadSchema(dbSchema, tableName);
                    ArrayList<String> fieldPath = getJoinedFieldPath(jqlSchema);
                    subEntity = baseEntity;
                    for (int i = fieldPath.size(); --i >= 0; ) {
                        subEntity = makeSubEntity(subEntity, fieldPath.get(i));
                    }
                }
                currTableName = tableName;
                currDbSchema = dbSchema;
            }
            JqlColumn jqlColumn = jqlSchema.getColumn(column);
            String fieldName = jqlColumn.getJsonName();
            Object value = getColumnValue(rs, idxColumn);

            KVEntity entity = subEntity;
            for (int p; (p = fieldName.indexOf('.')) > 0; ) {
                entity = makeSubEntity(entity, fieldName.substring(0, p));
                fieldName = fieldName.substring(p + 1);
            }
            entity.putIfAbsent(fieldName, value);
        }
        return baseEntity;
    }

    private ArrayList<String> getJoinedFieldPath(JqlSchema jqlSchema) {
        ArrayList<String> fieldPath = this.joinMap.get(jqlSchema.getTableName());
        if (fieldPath == null) {
            fieldPath = new ArrayList<>();
            String fieldName = jqlSchema.getEntityClassName();
            if (!resolveJsonPath(schema, fieldPath, fieldName)) {
                throw new RuntimeException("unknown joined table " + jqlSchema.getTableName());
            };
        }
        return fieldPath;
    }

    private KVEntity makeSubEntity(KVEntity entity, String key) {
        KVEntity subEntity = (KVEntity) entity.get(key);
        if (subEntity == null) {
            subEntity = new KVEntity();
            entity.put(key, subEntity);
        }
        return subEntity;
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }

    private static boolean resolveJsonPath(JqlSchema schema, ArrayList<String> fieldPath, String fieldName) {
        if (schema.getJoinedColumnSet(fieldName) != null) {
            fieldPath.add(fieldName);
            return true;
        }
        for (String fk : schema.getJoinedFieldNames()) {
            List<JqlColumn> fks = schema.getJoinedColumnSet(fk);
            JqlSchema pkSchema = fks.get(0).getJoinedPrimaryColumn().getSchema();
            if (resolveJsonPath(pkSchema, fieldPath, fieldName)) {
                fieldPath.add(pkSchema.getEntityClassName());
                return true;
            }
        }
        return false;
    }


}
