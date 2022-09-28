package org.slowcoders.jql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JQLRepository<ENTITY, ID> {

    Class<ENTITY> getEntityType();

    List<ENTITY> list(Collection<ID> idList);

    Page<ENTITY> list(Pageable pageRequest);

    Iterable<ENTITY> list(Sort sort, int limit);

    Iterable<ENTITY> listAll();

    default ENTITY get(ID id) throws IllegalArgumentException {
        ENTITY entity = find(id);
        if (entity == null) throw new IllegalArgumentException("entity not found: " + id);
        return entity;
    }

    ENTITY find(ID id);

    Iterable<ENTITY> find(Map<String, Object> jqlFilter, int limit);

    Iterable<ENTITY> find(Map<String, Object> jqlFilter, Sort sort, int limit);

    Page<ENTITY> find(Map<String, Object> jqlFilter, Pageable pageReq);

    ENTITY findTop(Map<String, Object> jqlFilter, Sort sort);


    long count(Map<String, Object> jqlFilter);


    void update(ID id, Map<String, Object> updateSet) throws IOException;

    void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException;

    ID insert(ENTITY entity);

    ID insert(Map<String, Object> dataSet) throws IOException;

    List<ID> insertAll(Collection<Map<String, Object>> dataSet) throws IOException;

    void delete(ID id);

    int delete(Collection<ID> idList);

    void clearEntityCache(ID id);
}
