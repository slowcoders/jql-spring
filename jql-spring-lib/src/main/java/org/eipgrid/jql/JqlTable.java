package org.eipgrid.jql;

import org.springframework.data.domain.Sort;

import java.util.*;

public interface JqlTable<ENTITY, ID> {

    JqlQuery<ENTITY> createQuery(Map<String, Object> filter);

    ENTITY find(ID id, JqlSelect select);

    List<ENTITY> find(Iterable<ID> idList, JqlSelect select);

    List<ENTITY> findAll(JqlSelect select, Sort sort);

    ID insert(Map<String, Object> properties);

    List<ID> insert(Collection<? extends Map<String, Object>> entities);
    
    ENTITY insertEntity(Map<String, Object> properties);

    void update(Iterable<ID> idList, Map<String, Object> properties);
    default void update(ID id, Map<String, Object> updateSet) {
        update(Collections.singletonList(id), updateSet);
    }

    void delete(Iterable<ID> idList);
    default void delete(ID id) {
        delete(Collections.singletonList(id));
    }

}