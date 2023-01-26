package org.eipgrid.jql.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eipgrid.jql.JqlEntity;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.JqlService;
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

public class JDBCRepositoryBase<ENTITY, ID> /*extends JDBCQueryBuilder*/ implements JqlRepository<ENTITY, ID> {

    private final static HashMap<Class<?>, JDBCRepositoryBase> loadedServices = new HashMap<>();
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final QueryGenerator sqlGenerator;
    private final JqlService service;
    private final QSchema schema;
    private final JqlParser jqlParser;
    private String lastGeneratedSql;


    protected JDBCRepositoryBase(JqlService service, QSchema schema) {
        this.service = service;
        this.sqlGenerator = service.getQueryGenerator();
        this.jdbc = service.getJdbcTemplate();
        this.objectMapper = service.getJsonConverter().getObjectMapper();
        this.schema = schema;
        this.jqlParser = new JqlParser(service.getConversionService());
    }

    protected JDBCRepositoryBase(JqlService service, Class<?> entityType) {
        this(service, service.loadSchema(entityType)); //  JQSchema.loadSchema(entityType));
    }

    public JqlService getService() {
        return service;
    }

    public String getTableName() {
        return schema.getTableName();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public QSchema getSchema() {
        return schema;
    }

    @Override
    public Class getEntityType() {
        return (Class) KVEntity.class;
    }

    public boolean hasGeneratedId() {
        return schema.hasGeneratedId();
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

    @Override
    public <T> T find(ID id, Class<T> entityType) {
        List<T> res = find(JqlQuery.of(this, id), entityType);
        return res.size() == 0 ? null : res.get(0);
    }

    protected ResultSetExtractor<List<KVEntity>> getColumnMapRowMapper(JqlFilter filter) {
        return new JsonRowMapper(filter.getResultMappings(), service.getObjectMapper());
    }

    public <T> List<T> find(JqlQuery query, Class<T> entityType) {
        String sql = sqlGenerator.createSelectQuery(query);
        this.lastGeneratedSql = sql;
        List res = jdbc.query(sql, getColumnMapRowMapper(query.getFilter()));
        if (entityType != JqlEntity.class) {
            ConversionService converter = service.getConversionService();
            for (int i = res.size(); --i >= 0; ) {
                ENTITY v = (ENTITY)converter.convert(res.get(i), entityType);
                res.set(i, v);
            }
        }
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

    @Override
    public <T> List<T> list(Collection<ID> idList, Class<T> entityType) {
        return find(JqlQuery.of(this, idList), entityType);
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



    public String getLastExecutedSql() {
        return this.lastGeneratedSql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JDBCRepositoryBase that = (JDBCRepositoryBase) o;
        return Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }
}