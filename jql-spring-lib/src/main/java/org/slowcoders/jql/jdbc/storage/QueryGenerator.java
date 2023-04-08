package org.slowcoders.jql.jdbc.storage;

import org.slowcoders.jql.JqlEntitySet;
import org.slowcoders.jql.jdbc.JdbcQuery;
import org.slowcoders.jql.parser.JqlFilter;

import java.util.Map;

public interface QueryGenerator {
    String createSelectQuery(JdbcQuery query);

    String createCountQuery(JqlFilter where);

    String createUpdateQuery(JqlFilter where, Map<String, Object> updateSet);

    String createDeleteQuery(JqlFilter where);

    String createInsertStatement(JdbcSchema schema, Map<String, Object> entity, JqlEntitySet.InsertPolicy insertPolicy);

    String prepareBatchInsertStatement(JdbcSchema schema, JqlEntitySet.InsertPolicy insertPolicy);

}
