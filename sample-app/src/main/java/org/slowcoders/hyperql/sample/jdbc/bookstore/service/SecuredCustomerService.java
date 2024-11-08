package org.slowcoders.hyperql.sample.jdbc.bookstore.service;

import org.slowcoders.hyperql.HyperRepository;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.HyperStorage;
import static org.slowcoders.hyperql.qb.Filter.*;

import org.slowcoders.hyperql.OutputFormat;
import org.slowcoders.hyperql.jdbc.JdbcQuery;
import org.slowcoders.hyperql.jdbc.JdbcRepositoryBase;
import org.slowcoders.hyperql.parser.HqlParser;
import org.slowcoders.hyperql.parser.HyperFilter;
import org.slowcoders.hyperql.qb.Filter;
import org.slowcoders.hyperql.util.KVEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SecuredCustomerService {

    private final HyperRepository<Long> customerEntitySet;

    SecuredCustomerService(HyperStorage storage) {
        customerEntitySet = storage.loadRepository("bookstore.customer");
        // testQueryBuilder(storage);
    }

    private void testQueryBuilder(HyperStorage storage) {
        Filter filter = _and_(
                _like_("name", "%e%"),
                _in_("book_",
                        _or_(
                                _ge_("length", 12),
                                _le_("length", 10)
                        )
                )
        );
        HqlParser parser = new HqlParser(storage.getObjectMapper());
        HyperFilter where = parser.parse(customerEntitySet.getSchema(), filter);
        JdbcQuery<Object> query = new JdbcQuery<>((JdbcRepositoryBase<?>) customerEntitySet, HyperSelect.Auto, where);
        List<Object> res = query.getResultList(OutputFormat.Object);
        System.out.println(res);
    }

    public HyperRepository<Long> getCustomerEntitySet() {
        return customerEntitySet;
    }

    public void deleteCustomer(Collection<Long> idList, String accessToken) {
        if ("1234".equals(accessToken)) {
            customerEntitySet.delete(idList);
        } else {
            throw new RuntimeException("Not customerized");
        }
    }

    public Long addNewCustomer(Map<String, Object> properties) {
        if (properties.get("metadata") == null) {
            properties.put("metadata", createNote());
        }

        return customerEntitySet.insert(properties);
    }

    private KVEntity createNote() {
        KVEntity entity = new KVEntity();
        entity.put("autoCreated", new Date());
        return entity;
    }

}
