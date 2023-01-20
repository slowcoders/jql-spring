package org.eipgrid.jql.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.JqlEntity;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.schema.QColumn;
import org.eipgrid.jql.schema.QSchema;
import org.eipgrid.jql.JqlRepository;
import org.eipgrid.jql.parser.JqlParser;
import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.util.KVEntity;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.util.*;

public class JDBCRepositoryBase<ID> /*extends JDBCQueryBuilder*/ implements JqlRepository<ID> {

    private final static HashMap<Class<?>, JDBCRepositoryBase> loadedServices = new HashMap<>();
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final QueryGenerator sqlGenerator;
    private final JdbcJqlService service;
    private final List<QColumn> pkColumns;
    private final QSchema schema;
    private final JqlParser jqlParser;
    private String lastGeneratedSql;

    public JDBCRepositoryBase(JdbcJqlService service, Class<?> entityType) {
        this(service, service.loadSchema(entityType)); //  JQSchema.loadSchema(entityType));
    }

    public JDBCRepositoryBase(JdbcJqlService service, QSchema schema) {
        this.service = service;
        this.sqlGenerator = service.getQueryGenerator();
        this.jdbc = service.getJdbcTemplate();
        this.objectMapper = service.getJsonConverter().getObjectMapper();
        this.schema = schema;
        this.pkColumns = schema.getPKColumns();
        this.jqlParser = new JqlParser(service.getConversionService());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public QSchema getSchema() {
        return schema;
    }

    @Override
    public Class<KVEntity> getEntityType() {
        return (Class) KVEntity.class;
    }

    @Override
    public ID convertId(Object v) {
        ConversionService cvtService = service.getConversionService();
        List<QColumn> pkColumns = schema.getPKColumns();
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
            QColumn pk = pkColumns.get(i);
            Object raw_v = raw_values == single_pk_value ? id : raw_values[i];
            Object k_v = cvtService.convert(raw_v, pk.getJavaType());
            map.put(pk.getJsonKey(), k_v);
        }
        return map;
    }

    @Override
    public JqlEntity find(ID id) {
        List<JqlEntity> res = find_impl(JqlQuery.of(this, id));
        return res.size() > 0 ? res.get(0) : null;
    }

    protected ResultSetExtractor<List<KVEntity>> getColumnMapRowMapper(JqlFilter filter) {
        return new JsonRowMapper(filter.getResultMappings(), service.getObjectMapper());
    }

    protected List<JqlEntity> find_impl(JqlQuery query) {
        String sql = sqlGenerator.createSelectQuery(query);
        this.lastGeneratedSql = sql;
        List<JqlEntity> res = (List)jdbc.query(sql, getColumnMapRowMapper(query.getFilter()));
        return res;
    }

    public List<JqlEntity> find(JqlQuery query) {
        String sql = sqlGenerator.createSelectQuery(query);
        this.lastGeneratedSql = sql;
        List<JqlEntity> res = (List)jdbc.query(sql, getColumnMapRowMapper(query.getFilter()));
        return res;
    }

    @Override
    public JqlFilter buildFilter(Map<String, Object> filter) {
        return jqlParser.parse(this.schema, filter);
    }

    @Override
    public long count(JqlFilter filter) {
        String sqlCount = sqlGenerator.createCountQuery(filter);
        long count = jdbc.queryForObject(sqlCount, Long.class);
        return count;
    }

//    @Override
//    public List<KVEntity> find(Map<String, Object> jsFilter, JqlRequest columns) {
//        return this.find_impl(jsFilter, columns);
//    }

    @Override
    public List<JqlEntity> list(Collection<ID> idList) {
        return find_impl(JqlQuery.of(this, idList));
    }


    @Override
    public ID insert(Map<String, Object> entity) {
        if (true) {
            Collection<Map<String, Object>> list = new ArrayList<>();
            list.add(entity);
            return insert(list).get(0);
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
    public List<ID> insert(Collection<Map<String, Object>> entities) {
        if (entities.isEmpty()) return null;

        BatchUpsert batch = new BatchUpsert(this.getSchema(), entities, true);
        jdbc.batchUpdate(batch.getSql(), batch);
        return batch.getEntityIDs();
    }


    @Override
    public void update(ID id, Map<String, Object> updateSet) throws IOException {
        this.update(Collections.singletonList(id), updateSet);
    }

    @Override
    public void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException {
        JqlFilter filter = JqlFilter.of(schema, idList);
        String sql = sqlGenerator.createUpdateQuery(filter, updateSet);
        jdbc.update(sql);
    }

    @Override
    public void delete(ID id) {
        JqlFilter filter = JqlFilter.of(schema, id);
        String sql = sqlGenerator.createDeleteQuery(filter);
        jdbc.update(sql);
    }

    @Override
    public int delete(Collection<ID> idList) {
        JqlFilter filter = JqlFilter.of(schema, idList);
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
            return JDBCRepositoryBase.loadedServices.get(entityType);
        }
    }



    public String getLastExecutedSql() {
        return this.lastGeneratedSql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JDBCRepositoryBase<?> that = (JDBCRepositoryBase<?>) o;
        return Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }
}