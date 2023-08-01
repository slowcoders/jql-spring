package org.slowcoders.hyperql;

import org.springframework.data.domain.Sort;

import java.util.*;

public interface EntitySet<ENTITY, ID> {

    enum InsertPolicy {
        ErrorOnConflict,
        IgnoreOnConflict,
        UpdateOnConflict,
    }

    HyperQuery createQuery(Map<String, Object> hqlFilter);

    List<ENTITY> findAll(HyperSelect select, Sort sort);

    default long count(Map<String, Object> hqlFilter) {
        return createQuery(hqlFilter).count();
    }

    ENTITY find(ID id, HyperSelect select);

    default ENTITY find(ID id) { return find(id, null); }

    default ENTITY get(ID id) {
        ENTITY entity = find(id, null);
        if (entity == null) throw new IllegalArgumentException("Entity not found: id = " + id);
        return entity;
    }

    List<ENTITY> find(Iterable<ID> idList, HyperSelect select);

    List<ID> insert(Collection<? extends Map<String, Object>> entities, InsertPolicy insertPolicy);
    default List<ID> insert(Collection<? extends Map<String, Object>> entities) {
        return insert(entities, InsertPolicy.ErrorOnConflict);
    }
    default List<ID> insertOrUpdate(Collection<? extends Map<String, Object>> entities) {
        return insert(entities, InsertPolicy.UpdateOnConflict);
    }

    ENTITY insert(Map<String, Object> properties, InsertPolicy insertPolicy);
    default ENTITY insert(Map<String, Object> properties) {
        return insert(properties, InsertPolicy.ErrorOnConflict);
    }
    default ENTITY insertOrUpdate(Map<String, Object> properties) {
        return insert(properties, InsertPolicy.UpdateOnConflict);
    }

    void update(Iterable<ID> idList, Map<String, Object> properties);
    default void update(ID id, Map<String, Object> updateSet) {
        update(Collections.singletonList(id), updateSet);
    }

    void delete(Iterable<ID> idList);
    default void delete(ID id) {
        delete(Collections.singletonList(id));
    }

}