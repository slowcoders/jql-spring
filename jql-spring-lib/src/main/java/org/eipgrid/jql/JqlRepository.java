package org.eipgrid.jql;

import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.parser.JqlParser;
import org.eipgrid.jql.schema.QSchema;

import java.io.IOException;
import java.util.*;

public abstract class JqlRepository<ENTITY, ID> implements JqlTable<ID> {

    protected final JqlService service;

    protected final QSchema schema;
    protected JqlParser jqlParser;

    protected JqlRepository(JqlService service, QSchema schema) {
        this.service = service;
        this.schema = schema;
        this.jqlParser = new JqlParser(service.getObjectMapper());
    }


    public final JqlService getService() {
        return service;
    }

    public final String getTableName() {
        return schema.getTableName();
    }

    public final QSchema getSchema() { return schema; }

    public final Class<ENTITY> getEntityType() { return (Class<ENTITY>)schema.getEntityType(); }

    public abstract ID convertId(Object id);

    public JqlFilter createFilter(Map<String, Object> filter) {
        return jqlParser.parse(this.schema, filter);
    }

    public final boolean hasGeneratedId() {
        return schema.hasGeneratedId();
    }

    protected void setGenerateQuery(JqlQuery query, String generatedQuery, Object extraInfo) {
        query.executedQuery = generatedQuery;
        query.extraInfo = extraInfo;
    }

    public abstract <T> List<T> find(JqlQuery query, Class<T> entityType);

    public List<ENTITY> find(JqlQuery query) { return find(query, getEntityType()); }

    public List<Map<String, Object>> find_raw(JqlQuery query) { return (List)find(query, Map.class); }


    public <T> T find(ID id, Class<T> entityType) {
        List<T> res = find(new JqlQuery(this, null, JqlFilter.of(schema, id)), entityType);
        return res.size() == 0 ? null : res.get(0);
    }

    public ENTITY find(ID id) { return find(id, getEntityType()); }

    public Map<String, Object> find_raw(ID id) { return find(id, Map.class); }


    public <T> T get(ID id, Class<T> entityType) {
        T entity = find(id, entityType);
        if (entity == null) throw new IllegalArgumentException(getEntityType().getSimpleName() +
                " not found: " + id);
        return entity;
    }

    public ENTITY get(ID id) { return get(id, getEntityType()); }

    public Map<String, Object> get_raw(ID id) { return get(id, Map.class); }



    public final <T> List<T> find(Collection<ID> idList, Class<T> entityType) {
        List<T> res = find(new JqlQuery(this, null, JqlFilter.of(schema, idList)), entityType);
        return res;
    }

    public List<ENTITY> find(Collection<ID> idList) { return find(idList, getEntityType()); }

    public List<Map<String, Object>> find_raw(Collection<ID> idList) { return (List)find(idList, Map.class); }


    public abstract long count(JqlFilter filter);



    public abstract List<ID> insert(Collection<Map<String, Object>> entities);

    public abstract void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException;

    public abstract void delete(Collection<ID> idList);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JqlRepository that = (JqlRepository) o;
        return Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

}
