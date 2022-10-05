package org.slowcoders.jql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JQLRepository<ENTITY, ID> {

    Class<ENTITY> getEntityType();

    ID convertId(Object v);

    Iterable<ENTITY> find(Map<String, Object> jqlFilter, Sort sort, int limit);

    default Iterable<ENTITY> find(Map<String, Object> jqlFilter, int limit) {
        return find(jqlFilter, null, limit);
    }

    Page<ENTITY> find(Map<String, Object> jqlFilter, @NotNull Pageable pageReq);

    ENTITY findTop(Map<String, Object> jqlFilter, Sort sort);

    ENTITY find(ID id);


    default ENTITY get(ID id) throws IllegalArgumentException {
        ENTITY entity = find(id);
        if (entity == null) throw new IllegalArgumentException("entity not found: " + id);
        return entity;
    }

    List<ENTITY> list(Collection<ID> idList);

    default Page<ENTITY> list(@NotNull Pageable pageRequest) {
        return find(null, pageRequest);
    }

    default Iterable<ENTITY> list(Sort sort, int limit) {
        return find(null, sort, limit);
    }

    default Iterable<ENTITY> listAll() {
        return find(null, null, -1);
    }



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
