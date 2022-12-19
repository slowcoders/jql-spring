package org.eipgrid.jql.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.JqlColumn;
import org.eipgrid.jql.JqlSchema;
import org.eipgrid.jql.spring.JQLRepository;
import org.eipgrid.jql.parser.JqlParser;
import org.eipgrid.jql.parser.JqlQuery;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.core.convert.ConversionService;
import org.eipgrid.jql.JqlSelect;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.util.*;

public class JDBCRepositoryBase<ID> /*extends JDBCQueryBuilder*/ implements JQLRepository<KVEntity, ID> {

    private final static HashMap<Class<?>, JDBCRepositoryBase>jqlServices = new HashMap<>();
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final SqlGenerator sqlGenerator;
    private final JQLJdbcService service;
    private final List<JqlColumn> pkColumns;
    private final JqlSchema jqlSchema;
    private String lastGeneratedSql;

    public JDBCRepositoryBase(JQLJdbcService service, Class<?> entityType) {
        this(service, service.loadSchema(entityType)); //  JqlSchema.loadSchema(entityType));
    }

    public JDBCRepositoryBase(JQLJdbcService service, JqlSchema jqlSchema) {
        this.service = service;
        this.sqlGenerator = new SqlGenerator();
        this.jdbc = service.getJdbcTemplate();
        this.objectMapper = service.getJsonConverter().getObjectMapper();
        this.jqlSchema = jqlSchema;
        this.pkColumns = jqlSchema.getPKColumns();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public JqlSchema getSchema() {
        return jqlSchema;
    }

    @Override
    public Class<KVEntity> getEntityType() {
        return (Class) KVEntity.class;
    }

    @Override
    public ID convertId(Object v) {
        ConversionService cvtService = service.getConversionService();
        List<JqlColumn> pkColumns = jqlSchema.getPKColumns();
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
            map.put(pk.getJsonKey(), k_v);
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
        List<KVEntity> res = find_impl(filter, JqlSelect.Whole);
        return res.size() > 0 ? res.get(0) : null;
    }

    protected ResultSetExtractor<List<KVEntity>> getColumnMapRowMapper(JqlQuery filter) {
        return new JqlRowMapper(filter.getResultMappings());
    }

    protected List<KVEntity> find_impl(Map<String, Object> jqlFilter, JqlSelect columns) {
        JqlQuery query = this.buildQuery(jqlFilter);
        String sql = sqlGenerator.createSelectQuery(query, columns);
        this.lastGeneratedSql = sql;
        List<KVEntity> res = (List)jdbc.query(sql, getColumnMapRowMapper(query));
        return res;
    }

//    @Override
//    public Page<KVEntity> find(Map<String, Object> jqlFilter, @NotNull Pageable pageReq) {
//        int size = pageReq.getPageSize();
//        int offset = (pageReq.getPageNumber()) * size;
//        List<KVEntity> res = find_impl(jqlFilter, pageReq.getSort(), offset, size);
//        long count = count(jqlFilter);
//        return new PageImpl(res, pageReq, count);
//    }

    @Override
    public long count(Map<String, Object> jqlFilter) {
        JqlQuery query = this.buildQuery(jqlFilter);
        String sqlCount = sqlGenerator.createCountQuery(query);
        long count = jdbc.queryForObject(sqlCount, Long.class);
        return count;
    }

    @Override
    public List<KVEntity> find(Map<String, Object> jqlFilter, JqlSelect columns) {
        return this.find_impl(jqlFilter, columns);
    }

    @Override
    public List<KVEntity> list(Collection<ID> idList) {
        Map<String, Object> filter = createJqlFilterWithIdList( idList);
        return find_impl(filter, JqlSelect.Whole);
    }

    @Override
    public KVEntity findTop(Map<String, Object> jqlFilter, Sort sort) {
        List<KVEntity> res = this.find_impl(jqlFilter, JqlSelect.by(null, sort, 0, 1));
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

        BatchUpsert batch = sqlGenerator.prepareInsert(this.getSchema(), entities);
        jdbc.batchUpdate(batch.getSql(), batch);
        return batch.getEntityIDs();
    }


    @Override
    public void update(ID id, Map<String, Object> updateSet) throws IOException {
        this.update(Collections.singletonList(id), updateSet);
    }

    @Override
    public void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException {
        JqlQuery filter = buildQuery(createJqlFilterWithIdList(idList));
        String sql = sqlGenerator.createUpdateQuery(filter, updateSet);
        jdbc.update(sql);
    }

    @Override
    public void delete(ID id) {
        JqlQuery filter = buildQuery(createJqlFilterWithId(id));
        String sql = sqlGenerator.createDeleteQuery(filter);
        jdbc.update(sql);
    }

    @Override
    public int delete(Collection<ID> idList) {
        JqlQuery filter = buildQuery(createJqlFilterWithIdList(idList));
        String sql = sqlGenerator.createDeleteQuery(filter);
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

    private static final Map _emptyMap = new HashMap();
    public JqlQuery buildQuery(Map<String, Object> filter) {
        Map filterMap;
        if (filter instanceof Map) {
            filterMap = (Map) filter;
        }
        else if (filter != null) {
            filterMap = new HashMap();
            throw new RuntimeException("Not implemented");
            //filterMap.put(this.pkColName + "@eq", filter);
            /** 참고 ID 검색 함수를 아래와 같이 처리할 수도 있다. (단, FetchType.EAGER 로 인한 다중 query 발생)
             Expression<?> column = root.get(this.pkColName).as(this.idType);
             return JQLOperator.EQ.createPredicate(cb, column, filter);
             */
        }
//        else if (!fetchData) {
//            return new JqlQuery(this.jqlSchema);
//        }
        else {
            /**
             * 참고) 왜 불필요하게 JQLParser 를 수행하는가?
             * CriteriaQuery 는 FetchType.EAGER 가 지정된 Column 에 대해
             * 자동으로 fetch 하는 기능이 없다. (2022.02.18 현재) 이에 대량 쿼리 발생시
             * 그 개수만큼 Join 된 칼럼을 읽어오는 추가적인 쿼리가 발생한다.
             * Parser 는 내부에서 이를 별도 검사하여 처리한다.
             */
            filterMap = _emptyMap;
        }

        JqlParser parser = new JqlParser(this.getSchema(), this.service.getConversionService());
        JqlQuery where = parser.parse(filterMap);
        return where;
    }

    public String getLastExecutedSql() {
        return this.lastGeneratedSql;
    }

}