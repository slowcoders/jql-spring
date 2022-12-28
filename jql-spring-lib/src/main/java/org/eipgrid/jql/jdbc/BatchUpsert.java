package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.JqlValueKind;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BatchUpsert<ID> implements BatchPreparedStatementSetterWithKeyHolder {
    private final Map<String, Object>[] entities;
    private final List<JqlColumn> columns;
    private final String sql;
    private final JqlSchema schema;
    private List<Map<String, Object>> generatedKeys;

    public BatchUpsert(JqlSchema schema, Collection<Map<String, Object>> entities, boolean ignoreConflict) {
        this.sql = new SqlGenerator().prepareBatchInsertStatement(schema, ignoreConflict);
        this.schema = schema;
        this.columns = schema.getWritableColumns();
        this.entities = entities.toArray(new Map[entities.size()]);
    }

    public String getSql() {
        return sql;
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        Map<String, Object> entity = entities[i];
        int idx = 0;
        for (JqlColumn col : columns) {
            Object json_v = entity.get(col.getJsonKey());
            Object value = convertJsonValueToColumnValue(col, json_v);
            ps.setObject(++idx, value);
        }
        ps.getGeneratedKeys();
    }

    private Object convertJsonValueToColumnValue(JqlColumn col, Object v) {
        if (v == null) return null;

        if (v.getClass().isEnum()) {
            if (col.getValueKind() == JqlValueKind.Text) {
                return v.toString();
            } else {
                return ((Enum) v).ordinal();
            }
        }
        return v;
    }


    @Override
    public int getBatchSize() {
        return entities.length;
    }

    @Override
    public void setGeneratedKeys(List<Map<String, Object>> keys) {
        this.generatedKeys = keys;
    }

    public List<ID> getEntityIDs() {
        int cntKeys = generatedKeys == null ? 0 : generatedKeys.size();
        List<JqlColumn> pkColumns = schema.getPKColumns();
        ArrayList<ID> ids = new ArrayList<>();
        for (int i = 0; i < entities.length; i++) {
            ID id = (ID)extractEntityId(pkColumns, entities[i], i < cntKeys ? this.generatedKeys.get(i) : null);
            ids.add(id);
        }
        return ids;
    }

    private static Object extractEntityId(List<JqlColumn> pkColumns, Map<String, Object> entity, Map<String, Object> generatedKeys) {
        if (pkColumns.size() > 1) {
            Object[] id = new Object[pkColumns.size()];
            int i = 0;
            for (JqlColumn pk : pkColumns) {
                Object v = getValue(pk.getJsonKey(), entity, generatedKeys);
                id[i++] = v;
            }
            return id;
        }
        else {
            Object id = getValue(pkColumns.get(0).getJsonKey(), entity, generatedKeys);
            return id;
        }
    }

    private static Object getValue(String key, Map<String, Object> entity, Map<String, Object> generatedKeys) {
        if (generatedKeys != null && generatedKeys.containsKey(key)) {
            return generatedKeys.get(key);
        }
        return entity.get(key);
    }


}
