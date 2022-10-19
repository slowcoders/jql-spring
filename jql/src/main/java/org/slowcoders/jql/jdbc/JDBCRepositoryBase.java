package org.slowcoders.jql.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.jql.JQLRepository;
import org.slowcoders.jql.JQLService;
import org.slowcoders.jql.JqlColumn;
import org.slowcoders.jql.JqlSchema;
import org.slowcoders.jql.jdbc.metadata.JqlRowMapper;
import org.slowcoders.jql.util.KVEntity;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

public class JDBCRepositoryBase<ID> /*extends JDBCQueryBuilder*/ implements JQLRepository<KVEntity, ID> {

    private final static HashMap<Class<?>, JDBCRepositoryBase>jqlServices = new HashMap<>();
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final SqlGenerator sqlGenerator;
    private final JQLJdbcService service;
    private final List<JqlColumn> pkColumns;

    public JDBCRepositoryBase(JQLJdbcService service, Class<?> entityType) {
        this(service, service.loadSchema(entityType)); //  JqlSchema.loadSchema(entityType));
    }

    public JDBCRepositoryBase(JQLJdbcService service, JqlSchema jqlSchema) {
        this.service = service;
        this.sqlGenerator = new SqlGenerator(service.getConversionService(), jqlSchema);
        this.jdbc = service.getJdbcTemplate();
        this.objectMapper = service.getJsonConverter().getObjectMapper();
        this.pkColumns = jqlSchema.getPKColumns();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public JqlSchema getSchema() {
        return sqlGenerator.getSchema();
    }

    @Override
    public Class<KVEntity> getEntityType() {
        return (Class) KVEntity.class;
    }

    @Override
    public ID convertId(Object v) {
        ConversionService cvtService = service.getConversionService();
        List<JqlColumn> pkColumns = sqlGenerator.getSchema().getPKColumns();
        if (pkColumns.size() == 1) {
            return (ID)service.getConversionService().convert(v, pkColumns.get(0).getJavaType());
        }
        String pks[] = ((String)v).split("|");
        if (pks.length != pkColumns.size()) {
            throw new RuntimeException("invalid primary keys: " + v);
        }
        Object ids[] = new Object[pks.length];
        for (int i = 0; i < pks.length; i++) {
            ids[i] = cvtService.convert(pks[i], pkColumns.get(i).getJavaType());
        }
        return (ID)ids;
    }

    private static String[] single_pk_value = new String[1];
    public Map<String, Object> createJqlFilterWithId(Object id) {
        String raw_values[] = pkColumns.size() == 1 ? single_pk_value : ((String)id).split(":");
        if (raw_values.length != pkColumns.size()) {
            throw new RuntimeException("invalid primary keys: " + id);
        }

        ConversionService cvtService = service.getConversionService();
        KVEntity map = new KVEntity();
        for (int i = 0; i < raw_values.length; i++) {
            JqlColumn pk = pkColumns.get(i);
            Object raw_v = raw_values == single_pk_value ? id : raw_values[i];
            Object k_v = cvtService.convert(raw_v, pk.getJavaType());
            map.put(pk.getJsonName(), k_v);
        }
        return map;
    }

    public Map<String, Object> createJqlFilterWithIdList(Collection idList) {
        if (pkColumns.size() == 1) {
            return createJqlFilterWithId(idList);
        }
        if (idList.size() == 1) {
            return createJqlFilterWithId(idList.iterator().next());
        }
        if (true) {
            throw new IllegalArgumentException("JQL does not support ID-list of composite primary keys");
        }
        ArrayList list = new ArrayList();
        for (Object id : idList) {
            list.add(createJqlFilterWithId(id));
        }
        return newSimpleMap("@in", list);
    }

    public static KVEntity newSimpleMap(String key, Object value) {
        KVEntity map = new KVEntity();
        map.put(key, value);
        return map;
    }
    @Override
    public KVEntity find(ID id) {
        Map<String, Object> filter = createJqlFilterWithId(id);
        SearchQuery query = sqlGenerator.select(filter);
        List<KVEntity> res = query.execute(jdbc, null, 1, 0);
        return res.size() == 0 ? null : res.get(0);
//        String sql = sqlGenerator.findById(id);
//        return jdbc.queryForObject(sql, getColumnMapRowMapper());
    }

//    protected RowMapper<KVEntity> getColumnMapRowMapper() {
//        return new JqlRowMapper(this.getSchema());
//    }

    @Override
    public Page<KVEntity> find(Map<String, Object> jqlFilter, @NotNull Pageable pageReq) {
        int size = pageReq.getPageSize();
        int offset = (pageReq.getPageNumber()) * size;
        SearchQuery query = sqlGenerator.select(jqlFilter);
        List<KVEntity> res = query.execute(jdbc, pageReq.getSort(), size, offset);
        long count = count(jqlFilter);
        return new PageImpl(res, pageReq, count);
    }

    @Override
    public long count(Map<String, Object> jqlFilter) {
        String sqlCount = sqlGenerator.count(jqlFilter);
        long count = jdbc.queryForObject(sqlCount, Long.class);
        return count;
    }

    @Override
    public Iterable<KVEntity> find(Map<String, Object> jqlFilter, Sort sort, int limit) {
        SearchQuery query = sqlGenerator.select(jqlFilter);
        List<KVEntity> res = query.execute(jdbc, sort, limit, 0);
//        String query = sqlGenerator.select(jqlFilter, sort, limit, 0);
//        List<KVEntity> res = (List)jdbc.query(query, getColumnMapRowMapper());
        return res;
    }

    @Override
    public List<KVEntity> list(Collection<ID> idList) {
        Map<String, Object> filter = createJqlFilterWithIdList( idList);
        SearchQuery query = sqlGenerator.select(filter);
        List<KVEntity> res = query.execute(jdbc, null, -1, 0);
//        String query = sqlGenerator.select(filter, null, idList.size(), 0);
//        List<KVEntity> res = (List)jdbc.query(query, getColumnMapRowMapper());
        return res;
    }

    @Override
    public KVEntity findTop(Map<String, Object> jqlFilter, Sort sort) {
        SearchQuery query = sqlGenerator.select(jqlFilter);
        List<KVEntity> res = query.execute(jdbc, sort, 1, 0);
//        List<KVEntity> res = (List)jdbc.query(query, getColumnMapRowMapper());
        return res.size() > 0 ? res.get(0) : null;
    }

    public ID insert(KVEntity entity) {
        return insert((Map<String, Object>)entity);
    }


    @Override
    public ID insert(Map<String, Object> entity) {
        if (true) {
            Collection<Map<String, Object>> list = new ArrayList<>();
            list.add(entity);
            return insertAll(list).get(0);
        }
        else {
//            ID pk;
//            if (super.hasGeneratedId()) {
//                pk = jdbcInsert.executeAndReturnKey(entity);
//            } else {
//                jdbcInsert.execute(entity);
//                pk = getSchema().getEntityID(entity);
//            }
            return null;
        }
    }

    // Insert Or Update Entity
    @Override
    public List<ID> insertAll(Collection<Map<String, Object>> entities) {
        if (entities.isEmpty()) return null;

        BatchUpsert batch = sqlGenerator.prepareInsert(entities);
        jdbc.batchUpdate(batch.getSql(), batch);
        return batch.getEntityIDs();
    }


    @Override
    public void update(ID id, Map<String, Object> updateSet) throws IOException {
        this.update(Collections.singletonList(id), updateSet);
    }

    @Override
    public void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException {
        Map<String, Object> filter = createJqlFilterWithIdList(idList);
        String sql = sqlGenerator.update(filter, updateSet);
        jdbc.update(sql);
    }

    @Override
    public void delete(ID id) {
        Map<String, Object> filter = createJqlFilterWithId(id);
        String sql = sqlGenerator.delete(filter);
        jdbc.update(sql);
    }

    @Override
    public int delete(Collection<ID> idList) {
        Map<String, Object> filter = createJqlFilterWithIdList(idList);
        String sql = sqlGenerator.delete(filter);
        return jdbc.update(sql);
    }

    @Override
    public void clearEntityCache(ID id) {
        // do nothing.
    }

    protected JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }


    public static class Util {
        public static JDBCRepositoryBase findRepository(Class<?> entityType) {
            return JDBCRepositoryBase.jqlServices.get(entityType);
        }
    }


}