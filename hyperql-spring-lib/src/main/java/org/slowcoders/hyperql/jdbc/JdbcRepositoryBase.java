package org.slowcoders.hyperql.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slowcoders.hyperql.*;
import org.slowcoders.hyperql.jdbc.output.ArrayRowMapper;
import org.slowcoders.hyperql.jdbc.storage.BatchUpsert;
import org.slowcoders.hyperql.jdbc.output.IdListMapper;
import org.slowcoders.hyperql.jdbc.output.JsonRowMapper;
import org.slowcoders.hyperql.jdbc.storage.JdbcSchema;
import org.slowcoders.hyperql.parser.HyperFilter;
import org.slowcoders.hyperql.parser.HqlParser;
import org.slowcoders.hyperql.schema.QColumn;
import org.slowcoders.hyperql.schema.QSchema;
import org.slowcoders.hyperql.util.KVEntity;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import jakarta.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class JdbcRepositoryBase<ID> extends HyperRepository<ID> {

    protected final JdbcStorage storage;
    private final JdbcTemplate jdbc;

    protected HqlParser hqlParser;

    private final IdListMapper<ID> idListMapper = new IdListMapper<>(this);

    protected JdbcRepositoryBase(JdbcStorage storage, QSchema schema) {
        super(schema);
        storage.registerTable(this);
        this.hqlParser = new HqlParser(storage.getObjectMapper());

        this.storage = storage;
        this.jdbc = storage.getJdbcTemplate();
    }

    public final JdbcStorage getStorage() {
        return storage;
    }

    public final List<Map> find(Iterable<ID> idList, HyperSelect select) {
        List<Map> res = find(new JdbcQuery(this, select, HyperFilter.of(schema, idList)));
        return res;
    }
    public Map find(ID id, HyperSelect select) {
        List<Map> res = find(new JdbcQuery(this, select, HyperFilter.of(schema, id)));
        return res.size() == 0 ? null : res.get(0);
    }

    @Override
    public HyperQuery createQuery(Map<String, Object> filter) {
        HyperFilter hqlFilter = hqlParser.parse(schema, filter);
        return new JdbcQuery(this, HyperSelect.of(filter), hqlFilter);
    }


    //    @Override
    protected ResultSetExtractor<List<KVEntity>> createColumnMapRowMapper(JdbcQuery query, OutputFormat outputFormat) {
        switch (outputFormat) {
            case Object:
                return new JsonRowMapper(query.getResultMappings(), storage.getObjectMapper());
            default:
                return new ArrayRowMapper(query.getResultMappings(), null);
        }
    }

    @Override
    public List<Map> findAll(HyperSelect select, Sort sort) {
        return find(new JdbcQuery(this, select, HyperFilter.of(this.schema)).sort(sort));
    }

    public List<Map> find(HyperQuery query0) {
        return find(query0, OutputFormat.Object);
    }

    public List find(HyperQuery query0, OutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = OutputFormat.Object;
        }
        JdbcQuery query = (JdbcQuery) query0;
        Class jpaEntityType = query.getJpaEntityType();
        boolean isNative = jpaEntityType == null || outputFormat != OutputFormat.Object;
        boolean isRepeat = (query.getExecutedQuery() != null && query.getExtraInfo() == (Boolean)isNative);

        String sql = isRepeat ? query.getExecutedQuery() :
                storage.createQueryGenerator(isNative).createSelectQuery(query);
        query.executedQuery = sql;
        query.extraInfo = isNative;

        List res;
        if (!isNative) {
            Query jpaQuery = storage.getEntityManager().createQuery(sql);
            if (query.getLimit() > 1) {
                jpaQuery = jpaQuery.setMaxResults(query.getLimit());
            }
            if (query.getOffset() > 0) {
                jpaQuery = jpaQuery.setFirstResult(query.getOffset());
            }
            res = jpaQuery.getResultList();
        }
        else {
            String s = getPaginationQuery(query);
            if (s.length() > 0) {
                sql += s;
            }
            res = jdbc.query(sql, createColumnMapRowMapper(query, outputFormat));
        }
        return res;
    }

    static String getPaginationQuery(HyperQuery query) {
        String s = "";
        long limit = query.getLimit();
        if (limit > 0 || query.getOffset() > 0) {
            if (limit <= 0) limit = Long.MAX_VALUE;
            s += "\nLIMIT " + limit;
        }
        if (query.getOffset() > 0) {
            s += "\nOFFSET " + query.getOffset();
        }
        return s;
    }

    public long count(JdbcQuery query) {
        HyperFilter filter = query == null ? null : query.getFilter();
        if (filter == null) {
            filter = new HyperFilter(this.schema);
        }
        String sqlCount = storage.createQueryGenerator().createCountQuery(filter);
        long count = jdbc.queryForObject(sqlCount, Long.class);
        return count;
    }

    // Insert Or Update Entity
    @Override
    public List<ID> insert(Collection<? extends Map<String, Object>> entities, InsertPolicy insertPolicy) {
        if (entities.isEmpty()) return Collections.emptyList();

        List<ID> idList = BatchUpsert.execute(jdbc, (JdbcSchema) this.getSchema(), entities, insertPolicy);
        return idList;
    }

    public ID insert_raw(Map<String, Object> properties, InsertPolicy insertPolicy) {
        ID id = this.insert(Collections.singletonList(properties), insertPolicy).get(0);
        return id;
    }

    public Map insert(Map<String, Object> properties, InsertPolicy insertPolicy) {
        ID id = insert_raw(properties, insertPolicy);
        return find(id, HyperSelect.of(properties));
    }

    @Override
    public void update(Iterable<ID> idList, Map<String, Object> updateSet) {
        HyperFilter filter = HyperFilter.of(schema, idList);
        String sql = storage.createQueryGenerator().createUpdateQuery(filter, updateSet);
        jdbc.update(sql);
    }

    @Override
    public void update(Map<String, Object> filter, Map<String, Object> updateSet) {
        HyperFilter hyperFilter = HyperFilter.of(schema, filter);
        String sql = storage.createQueryGenerator().createUpdateQuery(hyperFilter, updateSet);
        jdbc.update(sql);
    }

    @Override
    public void delete(ID id) {
        HyperFilter filter = HyperFilter.of(schema, id);
        String sql = storage.createQueryGenerator().createDeleteQuery(filter);
        jdbc.update(sql);
    }

    @Override
    public void delete(Iterable<ID> idList) {
        HyperFilter filter = HyperFilter.of(schema, idList);
        String sql = storage.createQueryGenerator().createDeleteQuery(filter);
        jdbc.update(sql);
    }


    public void removeEntityCache(ID id) {
        // do nothing.
    }

    public ID getEntityId(Map entity) {
        return schema.getEnityId(entity);
    }

    public ID convertId(Object v) {
        /** 참고. 2023.01.31
         * PathVariable 또는 RequestParam 에 사용된 ID 는 ConversionService 를 통해서 parsing 된다.
         * 해당 ID 는 Jql 검색을 통해 얻은 Json Value 값이므로, ObjectMapper 를 통한 Parsing 또한 가능하나,
         * StorageController 와 TableController 동작 호환성을 위해 ConversionService 를 사용한다.
         */
        ObjectMapper om = storage.getObjectMapper();
        List<QColumn> pkColumns = schema.getPKColumns();
        if (pkColumns.size() == 1) {
            return (ID)om.convertValue(v, pkColumns.get(0).getValueType());
        }
        String pks[] = ((String)v).split("|");
        if (pks.length != pkColumns.size()) {
            throw new RuntimeException("invalid primary keys: " + v);
        }
        Object ids[] = new Object[pks.length];
        for (int i = 0; i < pks.length; i++) {
            ids[i] = om.convertValue(pks[i], pkColumns.get(i).getValueType());
        }
        return (ID)ids;
    }

}