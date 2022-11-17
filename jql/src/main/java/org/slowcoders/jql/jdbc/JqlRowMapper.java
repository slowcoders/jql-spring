package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JqlRowMapper implements ResultSetExtractor<List<KVEntity>> {
    private final List<JqlResultMapping> resultMappings;
    private JqlColumn[] mappedColumns;
    private String[][] entityPaths;
    private Object[] columnValues;
    private KVEntity baseEntity;
    private final ArrayList<KVEntity> results = new ArrayList<>();

    public JqlRowMapper(List<JqlResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    @Override
    public List<KVEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        initMappedColumns(rs);

        columnValues = new Object[mappedColumns.length];
        int idxColumn = 0;
        int columnCount = mappedColumns.length;
        if (baseEntity != null) {
            int lastMappingIndex = resultMappings.size() - 1;
            check_duplicated_columns:
            for (int i = 0; i < lastMappingIndex; i ++) {
                JqlResultMapping mapping = resultMappings.get(i);
                List<JqlColumn> pkColumns = mapping.getSchema().getPKColumns();
                int pkIndex = idxColumn;
                for (int pkCount = pkColumns.size(); --pkCount >= 0; pkIndex++) {
                    Object value = getColumnValue(rs, pkIndex + 1);
                    if (value == null) {
                        columnCount = idxColumn;
                        break check_duplicated_columns;
                    }
                    if (!value.equals(columnValues[pkIndex])) {
                        break check_duplicated_columns;
                    }
                }
                idxColumn += mapping.getSelectedColumns().size();
            }
        }

        if (idxColumn == 0) {
            baseEntity = new KVEntity();
            results.add(baseEntity);
        }
        KVEntity entity = baseEntity;
        String[] currentPath = null;
        for (; idxColumn < columnCount; ) {
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
            columnValues[idxColumn-1] = value;
            entity.putIfAbsent(fieldName, value);
        }
        return results;
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

        int cntColumn = 0;
        int idxMappingColumn = 0;
        List<JqlColumn> columns = null;
        for (int idxColumn = 1; idxColumn <= columnCount; idxColumn++) {
            if (++idxMappingColumn >= cntColumn) {
                JqlResultMapping rowMapping;
                do {
                    rowMapping = resultMappings.get(idxFetch++);
                    columns = rowMapping.getSelectedColumns();
                    cntColumn = columns.size();
                } while (cntColumn == 0);
                helper.reset(rowMapping.getEntityMappingPath());
                idxMappingColumn = 0;
            }
            JqlColumn jqlColumn = columns.get(idxMappingColumn);
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
