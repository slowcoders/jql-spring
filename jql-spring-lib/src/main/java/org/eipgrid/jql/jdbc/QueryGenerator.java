package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.JQSchema;
import org.eipgrid.jql.JQSelect;
import org.eipgrid.jql.parser.JqlQuery;

import java.util.Map;

public interface QueryBuilder {
    String createSelectQuery(JqlQuery where, JQSelect columns);

    String createCountQuery(JqlQuery where);

    String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet);

    String createDeleteQuery(JqlQuery where);

    String prepareFindByIdStatement(JQSchema schema);

    String createInsertStatement(JQSchema schema, Map entity, boolean ignoreConflict);
}
