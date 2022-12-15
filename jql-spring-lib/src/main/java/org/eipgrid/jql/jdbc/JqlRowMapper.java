package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class JqlRowMapper implements ResultSetExtractor<List<KVEntity>> {
    private final List<JqlResultMapping> resultMappings;
    private ResultCache resultCacheRoot;
    private CachedEntity baseEntity = null;
    private ArrayList<KVEntity> results = new ArrayList<>();
    private MappedColumn[] mappedColumns;

    public JqlRowMapper(List<JqlResultMapping> rowMappings) {
        this.resultMappings = rowMappings;
    }

    @Override
    public List<KVEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        this.mappedColumns = initMappedColumns(rs);
        this.resultCacheRoot = new ResultCache(0, resultMappings.size());

        final int cntMapping = resultMappings.size();
        while (rs.next()) {
            int idxColumn = 0;
            JqlResultMapping lastMapping = null;
            ResultCache resultCache = resultCacheRoot;
            read_mapping:
            for (int idxMapping = 0; idxMapping < cntMapping; idxMapping++) {
                JqlResultMapping mapping = resultMappings.get(idxMapping);
                if (mapping.getParentNode() != lastMapping) {
                    resultCache = resultCacheRoot;
                }
                lastMapping = mapping;

                List<JqlColumn> columns = mapping.getSelectedColumns();
                if (columns.size() == 0) continue;

                boolean isCached = mapping.isArrayNode();
                if (!isCached) {
                    readColumns(rs, idxColumn, columns.size());
                    idxColumn += columns.size();
                    continue;
                }

                resultCache = resultCache.getCache(idxMapping);
                List<JqlColumn> pkColumns = mapping.getSchema().getPKColumns();
                int pkIndex = idxColumn;
                for (int pkCount = pkColumns.size(); --pkCount >= 0; pkIndex++) {
                    Object value = getColumnValue(rs, pkIndex + 1);
                    mappedColumns[pkIndex].value = value;
                    if (value == null) {
                        continue read_mapping;
                    }
                }
                Object key = makeCacheKey(pkColumns, idxColumn);
                CachedEntity cachedEntity = resultCache.get(key);
                if (cachedEntity == null) {
                    cachedEntity = readColumns(rs, idxColumn, columns.size());
                    resultCache.put(key, cachedEntity);
                }
                else if (idxMapping > 0) {
                    CachedEntity entity = makeBaseEntity(mapping);
                    if (cachedEntity.addParent(entity)) {
                        String[] entityPath = mapping.getEntityMappingPath();
                        String key2 = entityPath[entityPath.length - 1];
                        List data = (List) entity.get(key2);
                        if (data == null) {
                            data = new ArrayList();
                            entity.put(key2, data);
                        }
                        data.add(cachedEntity);
                    }
                }
                idxColumn += columns.size();
            }
        }
        return results;
    }

    private Object makeCacheKey(List<JqlColumn> pkColumns, int idxColumn) {
        if (pkColumns.size() == 1) {
            return mappedColumns[idxColumn].value;
        }
        throw new RuntimeException("not implemented");
    }

    private static void putValue(CachedEntity entity, MappedColumn mappedColumn, Object value) {
        CachedEntity node = entity;
        JqlColumn column = mappedColumn.jqlColumn;
        String fieldName = column.getJsonKey(); /* .getJavaFieldName();
        for (JqlColumn pk; (pk = column.getJoinedPrimaryColumn()) != null; ) {
            CachedEntity pkEntity = (CachedEntity) node.get(fieldName);
            if (pkEntity == null) {
                pkEntity = new CachedEntity(node);
                node.put(fieldName, pkEntity);
            }
            node = pkEntity;
            column = pk;
            fieldName = column.getJavaFieldName();
        }
        */
        Object old = node.put(fieldName, value);
        if (old != null && !old.equals(value)) {
            throw new RuntimeException("something wrong");
        }
    }

    private CachedEntity readColumns(ResultSet rs, int idxColumn, int count) throws SQLException {
        int columnCount = idxColumn + count;
        CachedEntity currEntity;
        if (idxColumn == 0) {
            currEntity = baseEntity = new CachedEntity(null);
            results.add(baseEntity);
        } else {
            currEntity = makeSubEntity(mappedColumns[idxColumn].mapping);
        }

        for (; idxColumn < columnCount; ) {
            MappedColumn mappedColumn = mappedColumns[idxColumn];
            Object value = getColumnValue(rs, ++idxColumn);
            mappedColumn.value = value;
            putValue(currEntity, mappedColumn, value);
        }
        return currEntity;
    }

    private CachedEntity makeBaseEntity(JqlResultMapping currMapping) {
        CachedEntity currEntity = baseEntity;
        String[] entityPath = currMapping.getEntityMappingPath();
        int idxLastPath = entityPath.length - 1;
        for (int i = 0; i < idxLastPath; i++) {
            currEntity = makeSubEntity(currEntity, entityPath[i], false);
        }
        return currEntity;
    }

    private CachedEntity makeSubEntity(JqlResultMapping currMapping) {
        CachedEntity currEntity = makeBaseEntity(currMapping);
        String[] entityPath = currMapping.getEntityMappingPath();
        currEntity = makeSubEntity(currEntity, entityPath[entityPath.length - 1], currMapping.isArrayNode());
        return currEntity;
    }

    private CachedEntity makeSubEntity(CachedEntity entity, String key, boolean isArray) {
        Object subEntity = entity.get(key);
        if (subEntity == null) {
            subEntity = new CachedEntity(entity);
            if (isArray) {
                ArrayList<Object> array = new ArrayList<>();
                array.add(subEntity);
                entity.put(key, array);
            } else {
                entity.put(key, subEntity);
            }
        } else if (isArray) {
            if (subEntity instanceof CachedEntity) {
                /** TODO remove this tricky code */
                ArrayList<Object> array = new ArrayList<>();
                entity.put(key, array);
                array.add(subEntity);
            } else {
                ArrayList<Object> array = (ArrayList<Object>) subEntity;
                subEntity = new CachedEntity(entity);
                array.add(subEntity);
            }
        } else if (subEntity instanceof ArrayList) {
            ArrayList<CachedEntity> list = (ArrayList<CachedEntity>) subEntity;
            subEntity = list.get(list.size()-1);
        }
        return (CachedEntity)subEntity;
    }

    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }


    private MappedColumn[] initMappedColumns(ResultSet rs) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        MappedColumn[] mappedColumns = new MappedColumn[columnCount];
        ColumnMappingHelper helper = new ColumnMappingHelper();

        int idxColumn = 0;
        for (JqlResultMapping mapping : resultMappings) {
            List<JqlColumn> columns = mapping.getSelectedColumns();
            if (columns.size() == 0) {
                continue;
            }
            helper.reset(mapping.getEntityMappingPath());
            for (JqlColumn column : columns) {
                String[] path = helper.getEntityMappingPath(column);
                mappedColumns[idxColumn++] = new MappedColumn(mapping, column, path);
            }
        }
        if (idxColumn != columnCount) {
            throw new RuntimeException("Something wrong!");
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

    private static class MappedColumn {
        final JqlColumn jqlColumn;
        final JqlResultMapping mapping;
        final String[] mappingPath;
        private Object   value;

        public MappedColumn(JqlResultMapping mapping, JqlColumn column, String[] path) {
            this.mapping = mapping;
            this.jqlColumn = column;
            this.mappingPath = path;
        }
    }

    private static class ResultCache extends HashMap<Object, CachedEntity> {
        private final int id;
        private final ResultCache[] caches;

        ResultCache(int id, int cntSlot) {
            this.id = id;
            caches = new ResultCache[cntSlot];
        }

        ResultCache getCache(int idxSlot) {
            if (idxSlot == this.id) return this;

            ResultCache cache = caches[idxSlot];
            if (cache == null) {
                caches[idxSlot] = cache = new ResultCache(idxSlot, caches.length);
            }
            return cache;
        }
    }

    private static class CachedEntity extends KVEntity {
        private static int g_sno;
        private final int id;
        private HashSet<CachedEntity> parents = new HashSet<>();

        CachedEntity(CachedEntity parent) {
            this.id = ++g_sno;
            if (parent != null) {
                parents.add(parent);
            }
        }

        final boolean addParent(CachedEntity parent) {
            if (!this.parents.contains(parent)) {
                parents.add(parent);
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }
    }
}
