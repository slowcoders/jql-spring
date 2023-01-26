package org.eipgrid.jql;

import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QSchema;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JqlRepository<ENTITY, ID> {

    Class<ENTITY> getEntityType();

    ID convertId(Object v);

    <T> List<T> find(JqlQuery query, Class<T> entityType);

    default List<ENTITY> find(JqlQuery query) { return find(query, getEntityType()); }

    default List<JqlEntity> find_raw(JqlQuery query) { return find(query, JqlEntity.class); }

    <T> T find(ID id, Class<T> entityType);

    default ENTITY find(ID id) { return find(id, getEntityType()); }

    default JqlEntity find_raw(ID id) { return find(id, JqlEntity.class); }

    default <T> T get(ID id, Class<T> entityType) {
        T entity = find(id, entityType);
        if (entity == null) throw new IllegalArgumentException(getEntityType().getSimpleName() +
                " not found: " + id);
        return entity;
    }

    default ENTITY get(ID id) { return get(id, getEntityType()); }

    default JqlEntity get_raw(ID id) { return get(id, JqlEntity.class); }

    <T> List<T> list(Collection<ID> idList, Class<T> entityType);

    default List<ENTITY> list(Collection<ID> idList) { return list(idList, getEntityType()); }

    default List<JqlEntity> list_raw(Collection<ID> idList) { return list(idList, JqlEntity.class); }

    long count(JqlFilter filter);

    ID insert(Map<String, Object> dataSet) throws IOException;

    List<ID> insert(Collection<Map<String, Object>> dataSet) throws IOException;


    void update(ID id, Map<String, Object> updateSet) throws IOException;

    void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException;

//    void update(JqlFilter filter, Map<String, Object> updateSet) throws IOException;

    void delete(ID id);

    int delete(Collection<ID> idList);

//    void delete(JqlFilter filter) throws IOException;

    void clearEntityCache(ID id);

    JqlFilter buildFilter(Map<String, Object> filter);

    QSchema getSchema();

}
