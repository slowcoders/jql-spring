package org.slowcoders.hyperql.jdbc.storage;

import org.slowcoders.hyperql.EntitySet;
import org.slowcoders.hyperql.jdbc.JdbcQuery;
import org.slowcoders.hyperql.parser.HyperFilter;

import java.util.List;
import java.util.Map;

public interface QueryGenerator {
    String createSelectQuery(JdbcQuery query);

    String createCountQuery(HyperFilter where);

    String createUpdateQuery(HyperFilter where, Map<String, Object> updateSet);

    String createDeleteQuery(HyperFilter where);

    String createInsertStatement(JdbcSchema schema, Map<String, Object> entity, EntitySet.InsertPolicy insertPolicy);

    String prepareBatchInsertStatement(JdbcSchema schema, List<JdbcColumn> columns, EntitySet.InsertPolicy insertPolicy);

}
