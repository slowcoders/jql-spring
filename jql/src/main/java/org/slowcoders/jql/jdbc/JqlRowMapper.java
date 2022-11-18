package org.slowcoders.jql.jdbc;

import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JqlRowMapper implements ResultSetExtractor<List<KVEntity>> {
    static class MappedColumn {
        private JqlColumn jqlColumn;
        private String[] entityPath;
        private Object   value;
        private boolean  isArray;
    }
    private final List<JqlResultMapping> resultMappings;

    public JqlRowMapper(List<JqlResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    @Override
    public List<KVEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        MappedColumn[] mappedColumns = initMappedColumns(rs);

        KVEntity baseEntity = null;
        ArrayList<KVEntity> results = new ArrayList<>();

        while (rs.next()) {
            int idxColumn = 0;
            int columnCount = mappedColumns.length;
            if (baseEntity != null) {
                int lastMappingIndex = resultMappings.size() - 1;
                check_duplicated_columns:
                for (int i = 0; i < lastMappingIndex; i++) {
                    JqlResultMapping mapping = resultMappings.get(i);
                    List<JqlColumn> pkColumns = mapping.getSchema().getPKColumns();
                    int pkIndex = idxColumn;
                    for (int pkCount = pkColumns.size(); --pkCount >= 0; pkIndex++) {
                        Object value = getColumnValue(rs, pkIndex + 1);
                        if (value == null) {
                            columnCount = idxColumn;
                            break check_duplicated_columns;
                        }
                        if (!value.equals(mappedColumns[pkIndex].value)) {
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
            String[] currentPath = mappedColumns[0].entityPath;
            for (; idxColumn < columnCount; ) {
                MappedColumn mappedColumn = mappedColumns[idxColumn];
                String[] entityPath = mappedColumn.entityPath;
                if (currentPath != entityPath) {
                    currentPath = entityPath;
                    entity = baseEntity;
                    int idxLastPath = entityPath.length - 1;
                    for (int i = 0; i < idxLastPath; i++) {
                        entity = makeSubEntity(entity, entityPath[i], false);
                    }
                    entity = makeSubEntity(entity, entityPath[idxLastPath], mappedColumn.isArray);
                }
                JqlColumn jqlColumn = mappedColumn.jqlColumn;
                String fieldName = jqlColumn.getJavaFieldName();

                Object value = getColumnValue(rs, ++idxColumn);
                mappedColumn.value = value;
                entity.putIfAbsent(fieldName, value);
            }
        }
        return results;
    }

    private KVEntity makeSubEntity(KVEntity entity, String key, boolean isArray) {
        Object subEntity = entity.get(key);
        if (subEntity == null) {
            subEntity = new KVEntity();
            if (isArray) {
                ArrayList<Object> array = new ArrayList<>();
                array.add(subEntity);
                entity.put(key, array);
            } else {
                entity.put(key, subEntity);
            }
        } else if (isArray) {
            ArrayList<Object> array = (ArrayList<Object>) subEntity;
            subEntity = new KVEntity();
            array.add(subEntity);
        }
        return (KVEntity)subEntity;
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }


    private MappedColumn[] initMappedColumns(ResultSet rs) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        MappedColumn[] mappedColumns = new MappedColumn[columnCount];
        ColumnMappingHelper helper = new ColumnMappingHelper();

        int cntColumn = 0;
        int idxMappingColumn = 0;
        int idxMapping = 0;
        boolean isArray = false;
        List<JqlColumn> columns = null;
        for (int idxColumn = 0; idxColumn < columnCount; idxColumn++) {
            MappedColumn mappedColumn = new MappedColumn();
            mappedColumns[idxColumn] = mappedColumn;
            if (++idxMappingColumn >= cntColumn) {
                JqlResultMapping rowMapping;
                do {
                    rowMapping = resultMappings.get(idxMapping++);
                    columns = rowMapping.getSelectedColumns();
                    cntColumn = columns.size();
                } while (cntColumn == 0);
                helper.reset(rowMapping.getEntityMappingPath());
                idxMappingColumn = 0;
                isArray = !rowMapping.isUniqueInRow();
            }
            JqlColumn jqlColumn = columns.get(idxMappingColumn);
            mappedColumn.entityPath = helper.getEntityMappingPath(jqlColumn);
            mappedColumn.jqlColumn = jqlColumn;
            mappedColumn.isArray = isArray;
        }
        return mappedColumns;
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
