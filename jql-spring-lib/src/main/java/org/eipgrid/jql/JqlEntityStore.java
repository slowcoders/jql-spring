package org.eipgrid.jql;

import org.eipgrid.jql.parser.JqlFilter;

import java.io.IOException;
import java.util.*;

public interface JqlEntityStore<ID> {

    JqlFilter createFilter(Map<String, Object> filter);


    List<?> find(JqlQuery query, OutputFormat outputType);


    List<?> find(Collection<ID> id);
    default Object find(ID id) {
        List<?> res = find(Collections.singletonList(id));
        return res.size() > 0 ? res.get(0) : null;
    }


    List<Object[]> listPrimaryKeys(JqlQuery query);


    long count(JqlFilter filter);

    List<ID> insert(Collection<Map<String, Object>> entities) throws IOException;
    default ID insert(Map<String, Object> entity) throws IOException {
        return insert(Collections.singletonList(entity)).get(0);
    }

    void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException;
    default void update(ID id, Map<String, Object> updateSet) throws IOException {
        update(Collections.singletonList(id), updateSet);
    }

    int delete(Collection<ID> idList);
    default void delete(ID id) {
        delete(Collections.singletonList(id));
    }


    default void clearEntityCaches() {}
}