package org.eipgrid.jql.jdbc;

import org.eipgrid.jql.schema.JQSchema;
import org.eipgrid.jql.JqlSelect;
import org.eipgrid.jql.parser.JqlQuery;

import java.util.Map;

public interface QueryGenerator {
    String createSelectQuery(JqlQuery where, JqlSelect columns);

    String createCountQuery(JqlQuery where);

    String createUpdateQuery(JqlQuery where, Map<String, Object> updateSet);

    String createDeleteQuery(JqlQuery where);

    String prepareFindByIdStatement(JQSchema schema);

    String createInsertStatement(JQSchema schema, Map entity, boolean ignoreConflict);
}
