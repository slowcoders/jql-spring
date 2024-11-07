package org.slowcoders.hyperql.sample.jdbc.starwars.service;

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
public class SecuredAuthorService {

    private final HyperRepository<Long> authorEntitySet;

    SecuredAuthorService(HyperStorage storage) {
        authorEntitySet = storage.loadRepository("starwars.author");
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
        HyperFilter where = parser.parse(authorEntitySet.getSchema(), filter);
        JdbcQuery<Object> query = new JdbcQuery<>((JdbcRepositoryBase<?>) authorEntitySet, HyperSelect.Auto, where);
        List<Object> res = query.getResultList(OutputFormat.Object);
        System.out.println(res);
    }

    public HyperRepository<Long> getAuthorEntitySet() {
        return authorEntitySet;
    }

    public void deleteAuthor(Collection<Long> idList, String accessToken) {
        if ("1234".equals(accessToken)) {
            authorEntitySet.delete(idList);
        } else {
            throw new RuntimeException("Not authorized");
        }
    }

    public Long addNewAuthor(Map<String, Object> properties) {
        if (properties.get("metadata") == null) {
            properties.put("metadata", createNote());
        }

        return authorEntitySet.insert(properties);
    }

    private KVEntity createNote() {
        KVEntity entity = new KVEntity();
        entity.put("autoCreated", new Date());
        return entity;
    }

}
