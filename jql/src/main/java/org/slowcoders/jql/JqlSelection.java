package org.slowcoders.jql;

import org.slowcoders.jql.util.KVEntity;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public interface JqlSelection {

    /**
     * @param sort sort options
     * @param limit max entity count to read
     * @param offset start index to read
     * @return read entities
     */
    List<KVEntity> read(Sort sort, int limit, int offset);

    /**
     * @return count of entities in this selection
     */
    long count();

    /**
     * @param updateSet
     * @return updated entity count
     */
    long updateAll(Map<String, Object> updateSet);

    /**
     * @return deleted entity count;
     */
    long deleteAll();
}
