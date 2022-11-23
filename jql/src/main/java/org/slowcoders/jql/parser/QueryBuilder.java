package org.slowcoders.jql.parser;

import org.slowcoders.jql.JqlSchema;
import org.springframework.data.domain.Sort;

import java.util.Map;

public interface QueryBuilder {
    String createSelectQuery(JqlQuery where, Sort sort, int offset, int limit);

    String createCountQuery(JqlQuery where);

    String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet);

    String createDeleteQuery(JqlQuery where);

    String prepareFindByIdStatement(JqlSchema schema);

    String createInsertStatement(JqlSchema schema, Map entity, boolean ignoreConflict);
}
