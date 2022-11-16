package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class JqlRowMapper implements RowMapper<KVEntity> {
    private final List<JqlResultMapping> resultMappings;
    private JqlColumn[] mappedColumns;
    private String[][] entityPaths;

    public JqlRowMapper(List<JqlResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    @Override
    public KVEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (mappedColumns == null) {
            initMappedColumns(rs);
        }

        KVEntity baseEntity = new KVEntity();
        KVEntity entity = baseEntity;
        String[] currentPath = null;
        final int columnCount = mappedColumns.length;
        for (int idxColumn = 0; idxColumn < columnCount; ) {
            String[] entityPath = entityPaths[idxColumn];
            if (currentPath != entityPath) {
                currentPath = entityPath;
                entity = baseEntity;
                for (int i = 0; i < entityPath.length; i++) {
                    entity = makeSubEntity(entity, entityPath[i]);
                }
            }
            JqlColumn jqlColumn = mappedColumns[idxColumn];
            String fieldName = jqlColumn.getJavaFieldName();

            Object value = getColumnValue(rs, ++idxColumn);
            entity.putIfAbsent(fieldName, value);
        }
        return baseEntity;
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


    private void initMappedColumns(ResultSet rs) throws SQLException {

        String currTableName = null;
        String currDbSchema = null;
        int idxFetch = 0;

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        this.mappedColumns = new JqlColumn[columnCount];
        this.entityPaths = new String[columnCount][];
        ColumnMappingHelper helper = new ColumnMappingHelper();

        JqlSchema jqlSchema = null;
        JqlResultMapping rowMapping = null;
        for (int idxColumn = 1; idxColumn <= columnCount; idxColumn++) {
            String columnName = JdbcUtils.lookupColumnName(rsmd, idxColumn);
            String tableName = rsmd.getTableName(idxColumn);
            String dbSchema = rsmd.getSchemaName(idxColumn);
            if (!tableName.equals(currTableName) || !dbSchema.equals(currDbSchema)) {
                do {
                    rowMapping = resultMappings.get(idxFetch++);
                } while (!rowMapping.hasSelectedColumns());
                jqlSchema = rowMapping.getSchema();
                if (!jqlSchema.getSimpleTableName().equals(tableName)) {
                    throw new RuntimeException("wrong result mappings");
                }
                helper.reset(rowMapping.getEntityMappingPath());
                currTableName = tableName;
                currDbSchema = dbSchema;
            }
            JqlColumn jqlColumn = jqlSchema.getColumn(columnName);
            this.entityPaths[idxColumn-1] = helper.getEntityMappingPath(jqlColumn);
            this.mappedColumns[idxColumn-1] = jqlColumn;
        }
    }

    private static class ColumnMappingHelper extends HashMap<String, ColumnMappingHelper> {
        String[] entityPath;

        void reset(String[] jsonPath) {
            this.entityPath = jsonPath;
            this.clear();
        }

        public String[] getEntityMappingPath(JqlColumn column) {
            String jsonKey = column.getJsonKey();
            ColumnMappingHelper cache = this;
            for (int p; (p = jsonKey.indexOf('.')) > 0; ) {
                cache = cache.register(entityPath, jsonKey.substring(0, p));
                jsonKey = jsonKey.substring(p + 1);
            }
            return cache.entityPath;

        }

        public ColumnMappingHelper register(String[] basePath, String key) {
            ColumnMappingHelper cache = this.get(key);
            if (cache == null) {
                cache = new ColumnMappingHelper();
                cache.entityPath = toJsonPath(basePath, key);
            }
            return cache;
        }

        private String[] toJsonPath(String[] basePath, String key) {
            String[] jsonPath = new String[basePath.length + 1];
            System.arraycopy(basePath, 0, jsonPath, 0, basePath.length);
            jsonPath[basePath.length] = key;
            return jsonPath;
        }

    }
}
