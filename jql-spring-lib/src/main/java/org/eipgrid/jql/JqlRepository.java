package org.eipgrid.jql;

import org.eipgrid.jql.parser.JqlFilter;
import org.eipgrid.jql.schema.QSchema;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JqlRepository<ID> {

    Class<?> getEntityType();

    ID convertId(Object v);

    default List<JqlEntity> find(JqlQuery request) { throw new RuntimeException("not impl"); }

    JqlEntity find(ID id);

    default JqlEntity get(ID id) {
        JqlEntity entity = find(id);
        if (entity == null) throw new IllegalArgumentException(getEntityType().getSimpleName() +
                " not found: " + id);
        return entity;
    }

    List<JqlEntity> list(Collection<ID> idList);

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
