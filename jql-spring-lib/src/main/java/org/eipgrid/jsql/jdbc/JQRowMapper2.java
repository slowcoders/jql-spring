package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.schema.JQColumn;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JQRowMapper2 implements ResultSetExtractor<List<KVEntity>> {
    private final List<JQResultMapping> resultMappings;
    private MappedColumn[] mappedColumns;

    public JQRowMapper2(List<JQResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    private static class CacheNode extends HashMap<CacheNode.Key, CacheNode> {
        KVEntity cachedEntity;

        public CacheNode(KVEntity cachedEntity) {
            this.cachedEntity = cachedEntity;
        }

        static class Key {
            Object[] pks;

            Key(Object[] pks)  {
                this.pks = pks;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Key other = (Key) o;
                return Arrays.equals(pks, other.pks);
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(pks);
            }
        }
    }

    @Override
    public List<KVEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {

        initMappedColumns(rs);

        ArrayList<KVEntity> results = new ArrayList<>();
        HashMap<CacheNode.Key, CacheNode> rootEntityCache = new HashMap<>();
        CacheNode.Key searchKey = new CacheNode.Key(null);

        while (rs.next()) {
            int idxColumn = 0;
            int columnCount = mappedColumns.length;
            KVEntity baseEntity = null;
            int lastMappingIndex = resultMappings.size() - 1;

            HashMap<CacheNode.Key, CacheNode> cacheNodes = rootEntityCache;
            check_duplicated_columns:
            for (int i = 0; i < lastMappingIndex; i++) {
                JQResultMapping mapping = resultMappings.get(i);
                if (!mapping.hasArrayDescendantNode()) break;

                Object[] pk = readPrimaryKeys(mapping, rs, idxColumn);
                if (pk == null) {
                    columnCount = idxColumn;
                    break;
                }
                searchKey.pks = pk;
                CacheNode cacheNode = cacheNodes.get(searchKey);
                boolean cacheFound = cacheNode != null;
                if (cacheNode == null) {
                    KVEntity newEntity = readEntity(mapping, baseEntity, rs, idxColumn);
                    cacheNode = new CacheNode(newEntity);
                    cacheNodes.put(new CacheNode.Key(pk), cacheNode);
                    if (i == 0) {
                        results.add(newEntity);
                        baseEntity = cacheNode.cachedEntity;
                    };
                }
                if (i == 0) {
                    baseEntity = cacheNode.cachedEntity;
                }
                cacheNodes = cacheNode;
                idxColumn += mapping.getSelectedColumns().size();
                if (!cacheFound) break;
            }

            if (idxColumn == 0) {
                baseEntity = new KVEntity();
                results.add(baseEntity);
            }
            KVEntity entity = baseEntity;
            JQResultMapping mapping = mappedColumns[0].mapping;
            for (; idxColumn < columnCount; ) {
                MappedColumn mappedColumn = mappedColumns[idxColumn];
                if (mapping != mappedColumn.mapping) {
                    mapping = mappedColumn.mapping;
                    entity = makeSubEntity(baseEntity, mapping);
                }
                mappedColumn.value = getColumnValue(rs, ++idxColumn);
                entity.putIfAbsent(mappedColumn.fieldName, mappedColumn.value);
            }
        }
        return results;
    }

    private KVEntity makeSubEntity(KVEntity entity, JQResultMapping mapping) {
        if (entity == null) {
            return new KVEntity();
        }
        String[] entityPath = mapping.getEntityMappingPath();
        int idxLastPath = entityPath.length - 1;
        for (int i = 0; i < idxLastPath; i++) {
            entity = makeSubEntity(entity, entityPath[i], false);
        }
        entity = makeSubEntity(entity, entityPath[idxLastPath], mapping.isArrayNode());
        return entity;
    }

    private KVEntity readEntity(JQResultMapping mapping, KVEntity baseEntity, ResultSet rs, int idxColumn) throws SQLException {
        KVEntity entity = makeSubEntity(baseEntity, mapping);
        for (int i = mapping.getSelectedColumns().size(); --i >= 0; ) {
            MappedColumn mc = mappedColumns[idxColumn];
            Object v = getColumnValue(rs, ++idxColumn);
            entity.put(mc.fieldName, v);
        }
        return entity;
    }

    private Object[] readPrimaryKeys(JQResultMapping mapping, ResultSet rs, int pkIndex) throws SQLException {
        List<JQColumn> pkColumns = mapping.getSchema().getPKColumns();
        Object[] pks = new Object[pkColumns.size()];
        for (int idxPk = 0; idxPk < pks.length; idxPk++) {
            Object value = getColumnValue(rs, ++pkIndex);
            if (value == null) return null;
            pks[idxPk] = value;
        }
        return pks;
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
        } else if (subEntity instanceof ArrayList) {
            ArrayList<KVEntity> list = (ArrayList<KVEntity>) subEntity;
            subEntity = list.get(list.size()-1);
        }
        return (KVEntity)subEntity;
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }


    private void initMappedColumns(ResultSet rs) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        MappedColumn[] mappedColumns = new MappedColumn[columnCount];
        ColumnMappingHelper helper = new ColumnMappingHelper();

        int idxColumn = 0;
        for (JQResultMapping mapping : resultMappings) {
            helper.reset(mapping.getEntityMappingPath());
            for (JQColumn column : mapping.getSelectedColumns()) {
                String[] path = helper.getEntityMappingPath(column);
                mappedColumns[idxColumn++] = new MappedColumn(mapping, column, path);
            }
        }
        if (idxColumn != columnCount) {
            throw new RuntimeException("Something wrong!");
        }
        this.mappedColumns = mappedColumns;
    }


    private static class ColumnMappingHelper extends HashMap<String, ColumnMappingHelper> {
        String[] entityPath;

        void reset(String[] jsonPath) {
            this.entityPath = jsonPath;
            this.clear();
        }

        public String[] getEntityMappingPath(JQColumn column) {
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

    private static class MappedColumn {
        final JQColumn column;
        final JQResultMapping mapping;
        final String[] mappingPath;
        final String fieldName;
        Object value;

        public MappedColumn(JQResultMapping mapping, JQColumn column, String[] path) {
            this.mapping = mapping;
            this.column = column;
            this.mappingPath = path;
            this.fieldName = column.resolveJavaFieldName();
        }
    }
}
