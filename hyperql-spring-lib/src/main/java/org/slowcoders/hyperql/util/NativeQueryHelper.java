package org.slowcoders.hyperql.util;

import org.hibernate.transform.ResultTransformer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeQueryHelper {
    private final EntityManager em;

    public NativeQueryHelper(EntityManager em) {
        this.em = em;
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        Query query = em.createNativeQuery(sql);
        query.unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new ResultTransformer() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        HashMap map = new HashMap();
                        for (int i = tuple.length; --i >= 0; ) {
                            map.put(aliases[i], tuple[i]);
                        }
                        return map;
                    }

                    @Override
                    public List transformList(List collection) {
                        return collection;
                    }
                });
        try {
            List<Map<String, Object>> res = query.getResultList();
            return res;
        }
        catch (Exception e) {
            throw new RuntimeException("invalid SQL: ---\n" + sql);
        }
    }
}
