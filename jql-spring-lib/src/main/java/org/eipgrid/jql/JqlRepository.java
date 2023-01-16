package org.eipgrid.jql;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JqlRepository<ENTITY, ID> {

    Class<ENTITY> getEntityType();

    ID convertId(Object v);

    List<ENTITY> find(Map<String, Object> jsQuery, JqlSelect columns);

    ENTITY find(ID id);

    List<ENTITY> list(Collection<ID> idList);

    long count(Map<String, Object> jsQuery);


    ID insert(ENTITY entity);

    ID insert(Map<String, Object> dataSet) throws IOException;

    List<ID> insert(Collection<Map<String, Object>> dataSet) throws IOException;


    void update(ID id, Map<String, Object> updateSet) throws IOException;

    void update(Collection<ID> idList, Map<String, Object> updateSet) throws IOException;

//    void update(Map<String, Object> filter, Map<String, Object> updateSet) throws IOException;

    void delete(ID id);

    int delete(Collection<ID> idList);

//    void delete(Map<String, Object> filter) throws IOException;

    void clearEntityCache(ID id);

}
